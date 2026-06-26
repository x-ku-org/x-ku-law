package cn.xku.law.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * 集成测试基类：用 Testcontainers 起真实 MySQL 8 + Redis，Flyway 自动跑 V1–V3 迁移与种子数据。
 * 容器以静态单例方式启动，跨测试类复用（JVM 退出时由 Ryuk 清理）。
 * <p>默认关闭全文检索（{@code app.search.enabled=false}，走 NoOpSearchClient，无需 ES）；
 * 需要真实 ES 的检索用例请继承 {@link AbstractSearchIntegrationTest}。
 * <p><b>需本机 Docker。</b>集成测试统一以 {@code *IT} 命名，由 failsafe 在 {@code mvn verify} 阶段运行。
 * 种子账号：tenantCode={@code platform}、admin/{@code Admin@123}（拥有全部权限）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public abstract class AbstractIntegrationTest {

    protected static final long PLATFORM_TENANT_ID = 1L;
    protected static final String PLATFORM_TENANT_CODE = "platform";

    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("lr_law")
            .withUsername("lr")
            .withPassword("lr");

    protected static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static {
        MYSQL.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void registerInfraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected StringRedisTemplate redisTemplate;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * 每个用例后清理：flush Redis（会话/权限缓存）+ 删除测试期插入的业务行，保留 Flyway 种子
     * （admin 用户 id=1、platform_admin 角色 id=1、种子权限 id≤32）。容器单例复用，必须显式隔离。
     */
    @AfterEach
    void cleanUpState() {
        java.util.Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        jdbcTemplate.execute("DELETE FROM lr_search_index_task");
        jdbcTemplate.execute("DELETE FROM lr_file_object");
        jdbcTemplate.execute("DELETE FROM lr_subscription_rule");
        jdbcTemplate.execute("DELETE FROM lr_law_version");
        jdbcTemplate.execute("DELETE FROM lr_law_document");
        jdbcTemplate.execute("DELETE FROM lr_user_role WHERE user_id <> 1");
        jdbcTemplate.execute("DELETE FROM lr_role_permission WHERE role_id <> 1");
        jdbcTemplate.execute("DELETE FROM lr_role WHERE id <> 1");
        jdbcTemplate.execute("DELETE FROM lr_permission WHERE id > 33");
        jdbcTemplate.execute("DELETE FROM lr_user WHERE id <> 1");
    }

    // ===== 登录 / token =====

    protected record TokenPair(String accessToken, String refreshToken) {}

    protected TokenPair login(String tenantCode, String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("tenantCode", tenantCode, "username", username, "password", password));
        String json = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(json).get("data");
        if (data == null || data.isNull()) {
            throw new IllegalStateException("登录失败: " + json);
        }
        return new TokenPair(data.get("accessToken").asText(), data.get("refreshToken").asText());
    }

    protected TokenPair loginAdmin() throws Exception {
        return login(PLATFORM_TENANT_CODE, "admin", "Admin@123");
    }

    protected String refreshAccess(String refreshToken) throws Exception {
        String json = mockMvc.perform(post("/auth/refresh").header("X-Refresh-Token", refreshToken))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("data").asText();
    }

    // ===== 带 token 的请求 =====

    protected ResultActions getJson(String uri, String accessToken) throws Exception {
        return mockMvc.perform(get(uri).header("Authorization", "Bearer " + accessToken));
    }

    protected ResultActions postJson(String uri, String accessToken, Object body) throws Exception {
        return mockMvc.perform(post(uri).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions putJson(String uri, String accessToken, Object body) throws Exception {
        return mockMvc.perform(put(uri).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions deleteJson(String uri, String accessToken) throws Exception {
        return mockMvc.perform(delete(uri).header("Authorization", "Bearer " + accessToken));
    }

    // ===== 数据夹具（raw JDBC，绕过租户拦截器以便显式控制 tenant_id） =====

    protected long insertUser(String username, String rawPassword, long tenantId) {
        jdbcTemplate.update("INSERT INTO lr_user (username, password_hash, tenant_id) VALUES (?,?,?)",
                username, passwordEncoder.encode(rawPassword), tenantId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM lr_user WHERE username=? AND tenant_id=?", Long.class, username, tenantId);
    }

    protected long insertRole(String roleCode, long tenantId) {
        jdbcTemplate.update("INSERT INTO lr_role (role_code, role_name, status, tenant_id) VALUES (?,?, 'enabled', ?)",
                roleCode, roleCode, tenantId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM lr_role WHERE role_code=? AND tenant_id=?", Long.class, roleCode, tenantId);
    }

    protected void bindUserRole(long userId, long roleId, long tenantId) {
        jdbcTemplate.update("INSERT INTO lr_user_role (user_id, role_id, tenant_id) VALUES (?,?,?)",
                userId, roleId, tenantId);
    }

    protected void bindRolePermission(long roleId, long permissionId, long tenantId) {
        jdbcTemplate.update("INSERT INTO lr_role_permission (role_id, permission_id, tenant_id) VALUES (?,?,?)",
                roleId, permissionId, tenantId);
    }

    /** 取种子权限码对应的 permission id（V2/V3 已插入）。 */
    protected long permissionId(String code) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM lr_permission WHERE permission_code=?", Long.class, code);
    }
}
