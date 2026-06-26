package cn.xku.law.file;

import cn.xku.law.common.client.FileStat;
import cn.xku.law.common.client.FileStorageClient;
import cn.xku.law.common.security.LoginUser;
import cn.xku.law.file.domain.FileObjectDO;
import cn.xku.law.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileAuthorizationIT extends AbstractIntegrationTest {

    @Autowired
    private FileService fileService;

    @MockBean
    private FileStorageClient fileStorageClient;

    @Test
    void sameTenantUserCannotReadOrCompleteAnotherUsersPrivateFile() throws Exception {
        insertUser("bob", "Bob@123", PLATFORM_TENANT_ID);
        insertUser("carol", "Carol@123", PLATFORM_TENANT_ID);

        TokenPair bob = login(PLATFORM_TENANT_CODE, "bob", "Bob@123");
        TokenPair carol = login(PLATFORM_TENANT_CODE, "carol", "Carol@123");

        long normalFileId = insertFile("bob-private.pdf", "normal", "bob", "feedback_attachment");
        getJson("/files/" + normalFileId, carol.accessToken()).andExpect(status().isForbidden());
        getJson("/files/" + normalFileId + "/url", carol.accessToken()).andExpect(status().isForbidden());

        String ownerResp = getJson("/files/" + normalFileId, bob.accessToken())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(ownerResp).get("data").get("id").asLong()).isEqualTo(normalFileId);

        long pendingFileId = insertFile("bob-pending.pdf", "pending", "bob", "feedback_attachment");
        postJson("/files/" + pendingFileId + "/complete", carol.accessToken(), null)
                .andExpect(status().isForbidden());
    }

    @Test
    void systemRegisteredLawFileRemainsTenantShared() throws Exception {
        insertUser("carol", "Carol@123", PLATFORM_TENANT_ID);
        TokenPair carol = login(PLATFORM_TENANT_CODE, "carol", "Carol@123");

        long lawFileId = insertFile("law-version.pdf", "normal", "anonymous", "law_version", 0L);

        getJson("/files/" + lawFileId, carol.accessToken()).andExpect(status().isOk());
        getJson("/files/" + lawFileId + "/url", carol.accessToken()).andExpect(status().isOk());
    }

    /**
     * 模拟 ingest 链路在无 SecurityContext 时落库：公共法规文件 tenant_id=0，
     * 校验 tenant_id≥1 的真实租户用户仍可跨租户读取（共享类型），但读不到私有类型。
     */
    @Test
    void ingestRegisteredLawFileWithPlatformTenantIsReadableByTenantUser() throws Exception {
        insertUser("carol", "Carol@123", PLATFORM_TENANT_ID);
        TokenPair carol = login(PLATFORM_TENANT_CODE, "carol", "Carol@123");

        // ingest 以 tenant_id=0 落库的共享法规文件：跨租户可读
        long lawFileId = insertFile("ingest-law.pdf", "normal", "anonymous", "law_version", 0L);
        getJson("/files/" + lawFileId, carol.accessToken()).andExpect(status().isOk());
        getJson("/files/" + lawFileId + "/url", carol.accessToken()).andExpect(status().isOk());

        long privateFileId = insertFile("ingest-private.pdf", "normal", "anonymous", "feedback_attachment", 0L);
        getJson("/files/" + privateFileId, carol.accessToken()).andExpect(status().isNotFound());
        getJson("/files/" + privateFileId + "/url", carol.accessToken()).andExpect(status().isNotFound());
    }

    @Test
    void lawRefTypeUploadedByTenantUserDoesNotBecomeShared() throws Exception {
        insertUser("bob", "Bob@123", PLATFORM_TENANT_ID);
        insertUser("carol", "Carol@123", PLATFORM_TENANT_ID);

        TokenPair bob = login(PLATFORM_TENANT_CODE, "bob", "Bob@123");
        TokenPair carol = login(PLATFORM_TENANT_CODE, "carol", "Carol@123");

        long userLawFileId = insertFile("bob-law-version.pdf", "normal", "bob", "law_version");
        getJson("/files/" + userLawFileId, bob.accessToken()).andExpect(status().isOk());
        getJson("/files/" + userLawFileId, carol.accessToken()).andExpect(status().isForbidden());
        getJson("/files/" + userLawFileId + "/url", carol.accessToken()).andExpect(status().isForbidden());
    }

    @Test
    void promoteLawUploadForProcessingMovesOwnerFileToSharedTenant() {
        insertUser("bob", "Bob@123", PLATFORM_TENANT_ID);
        long userLawFileId = insertFile("bob-law-upload.pdf", "normal", "bob", "law");

        runAs("bob", PLATFORM_TENANT_ID, () -> fileService.promoteForLawProcessing(userLawFileId));

        SecurityContextHolder.clearContext();
        FileObjectDO processed = fileService.loadForProcessing(userLawFileId);
        assertThat(processed).isNotNull();
        assertThat(processed.getTenantId()).isZero();
        assertThat(processed.getRefType()).isEqualTo("law_version");
    }

    @Test
    void sharedFallbackDoesNotExposeOtherTenantLawFiles() throws Exception {
        insertUser("carol", "Carol@123", PLATFORM_TENANT_ID);
        TokenPair carol = login(PLATFORM_TENANT_CODE, "carol", "Carol@123");

        long otherTenantLawFileId = insertFile("other-tenant-law-version.pdf", "normal", "mallory", "law_version", 2L);

        getJson("/files/" + otherTenantLawFileId, carol.accessToken()).andExpect(status().isNotFound());
        getJson("/files/" + otherTenantLawFileId + "/url", carol.accessToken()).andExpect(status().isNotFound());
    }

    @Test
    void registerExistingReusesExistingObjectKey() {
        String objectKey = "law_version/retry-law.pdf";
        long existingFileId = insertFile("retry-law.pdf", "normal", "anonymous", "law_version", 0L);

        Long first = fileService.registerExisting(objectKey, "retry-law.pdf", "law_version");
        Long second = fileService.registerExisting(objectKey, "retry-law.pdf", "law_version");

        assertThat(first).isEqualTo(existingFileId);
        assertThat(second).isEqualTo(existingFileId);
    }

    @Test
    void registerExistingCreatesFileOnceForNewObjectKey() {
        String objectKey = "law_version/new-law.pdf";
        when(fileStorageClient.statObject(objectKey))
                .thenReturn(new FileStat(2048, "\"etag-value\"", "application/pdf"));

        Long first = fileService.registerExisting(objectKey, "new-law.pdf", "law_version");
        Long second = fileService.registerExisting(objectKey, "new-law.pdf", "law_version");

        assertThat(second).isEqualTo(first);
        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lr_file_object WHERE object_key=?",
                Integer.class, objectKey);
        assertThat(rows).isEqualTo(1);
    }

    private long insertFile(String fileName, String status, String creator, String refType) {
        return insertFile(fileName, status, creator, refType, PLATFORM_TENANT_ID);
    }

    private long insertFile(String fileName, String status, String creator, String refType, long tenantId) {
        String objectKey = refType + "/" + fileName;
        jdbcTemplate.update("""
                INSERT INTO lr_file_object
                    (file_name, original_name, mime_type, storage_type, object_key, file_size,
                     ref_type, status, creator, updater, tenant_id)
                VALUES (?, ?, 'application/pdf', 'minio', ?, 1024, ?, ?, ?, ?, ?)
                """, fileName, fileName, objectKey, refType, status, creator, creator, tenantId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM lr_file_object WHERE object_key=? AND tenant_id=?",
                Long.class, objectKey, tenantId);
    }

    private static void runAs(String username, long tenantId, Runnable runnable) {
        LoginUser user = LoginUser.builder()
                .userId(99L)
                .username(username)
                .tenantId(tenantId)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));
        try {
            runnable.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
