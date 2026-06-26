package cn.xku.law.file;

import cn.xku.law.common.client.FileStat;
import cn.xku.law.common.client.FileStorageClient;
import cn.xku.law.common.constant.SecurityConstants;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.config.StorageProperties;
import cn.xku.law.file.domain.FileObjectDO;
import cn.xku.law.file.dto.FilePresignDTO;
import cn.xku.law.file.mapper.FileObjectMapper;
import cn.xku.law.file.vo.FileObjectVO;
import cn.xku.law.file.vo.FilePresignVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 文件直传业务实现 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileObjectMapper, FileObjectDO> implements FileService {

    private static final String STORAGE_TYPE = "minio";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_NORMAL = "normal";
    private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final Set<String> TENANT_SHARED_REF_TYPES = Set.of(
            "law_document",
            "law_version",
            "raw_document"
    );
    private static final Set<String> LAW_PROCESS_UPLOAD_REF_TYPES = Set.of(
            "law",
            "law_version"
    );

    private final StorageProperties props;
    private final FileStorageClient fileStorageClient;

    @Override
    public FilePresignVO presign(FilePresignDTO dto) {
        validate(dto);

        String ext = extractExt(dto.getOriginalName());
        String fileName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        String dir = StringUtils.hasText(dto.getRefType()) ? sanitizeDir(dto.getRefType()) : "misc";
        String objectKey = dir + "/" + LocalDateTime.now().format(YYYY_MM) + "/" + fileName;

        FileObjectDO entity = new FileObjectDO();
        entity.setFileName(fileName);
        entity.setOriginalName(dto.getOriginalName());
        entity.setFileExt(ext);
        entity.setMimeType(dto.getContentType());
        entity.setStorageType(STORAGE_TYPE);
        entity.setBucketName(props.getBucket());
        entity.setObjectKey(objectKey);
        entity.setFileSize(dto.getFileSize());
        entity.setRefType(dto.getRefType());
        entity.setStatus(STATUS_PENDING);
        // tenant_id 由多租户插件自动注入；creator/时间由 MetaObjectFillHandler 填充
        this.save(entity);

        String uploadUrl = fileStorageClient.generatePresignedPutUrl(
                objectKey, dto.getContentType(), props.getPresignExpireSeconds());

        FilePresignVO vo = new FilePresignVO();
        vo.setFileId(entity.getId());
        vo.setObjectKey(objectKey);
        vo.setUploadUrl(uploadUrl);
        vo.setExpireSeconds(props.getPresignExpireSeconds());
        vo.setMethod("PUT");
        vo.setHeaders(Map.of("Content-Type", dto.getContentType()));
        return vo;
    }

    @Override
    public FileObjectVO complete(Long fileId) {
        FileObjectDO entity = this.getById(fileId);
        if (entity == null) {
            throw new AppException(ErrorCode.SYS_FILE_NOT_FOUND);
        }
        // 已完成则幂等返回
        if (STATUS_NORMAL.equals(entity.getStatus())) {
            checkReadable(entity);
            return toVO(entity);
        }
        checkOwner(entity);

        FileStat stat = fileStorageClient.statObject(entity.getObjectKey());
        if (stat == null) {
            throw new AppException(ErrorCode.SYS_FILE_NOT_UPLOADED);
        }
        if (stat.size() > props.getMaxFileSize()) {
            fileStorageClient.delete(entity.getObjectKey());
            throw new AppException(ErrorCode.SYS_FILE_TOO_LARGE);
        }
        // 以对象实际 Content-Type 为准复核白名单，并要求其与预签名时绑定的类型一致。
        String actualType = stat.contentType();
        if (!StringUtils.hasText(actualType)
                || !contentTypeAllowed(actualType)
                || !actualType.equalsIgnoreCase(entity.getMimeType())) {
            fileStorageClient.delete(entity.getObjectKey());
            throw new AppException(ErrorCode.SYS_FILE_TYPE_NOT_ALLOWED);
        }

        entity.setMimeType(actualType);
        entity.setFileSize(stat.size());
        entity.setSha256(stripQuotes(stat.etag()));
        entity.setStatus(STATUS_NORMAL);
        this.updateById(entity);
        return toVO(entity);
    }

    @Override
    public String getAccessUrl(Long fileId) {
        FileObjectDO entity = loadReadable(fileId);
        if (!STATUS_NORMAL.equals(entity.getStatus())) {
            throw new AppException(ErrorCode.SYS_FILE_NOT_UPLOADED);
        }
        return fileStorageClient.getAccessUrl(entity.getObjectKey());
    }

    @Override
    public Long registerExisting(String objectKey, String originalName, String refType) {
        FileObjectDO existing = baseMapper.selectByObjectKey(objectKey);
        if (existing != null) {
            return existing.getId();
        }

        FileStat stat = fileStorageClient.statObject(objectKey);
        if (stat == null) {
            log.warn("[FileService] registerExisting: object not found in storage, key={}", objectKey);
            return null;
        }
        String fileName = objectKey.contains("/")
                ? objectKey.substring(objectKey.lastIndexOf('/') + 1) : objectKey;
        String nameForExt = StringUtils.hasText(originalName) ? originalName : fileName;
        String ext = extractExt(nameForExt);
        String mime = StringUtils.hasText(stat.contentType()) ? stat.contentType() : guessMime(ext);

        FileObjectDO entity = new FileObjectDO();
        entity.setFileName(fileName);
        entity.setOriginalName(nameForExt);
        entity.setFileExt(ext);
        entity.setMimeType(mime);
        entity.setStorageType(STORAGE_TYPE);
        entity.setBucketName(props.getBucket());
        entity.setObjectKey(objectKey);
        entity.setFileSize(stat.size());
        entity.setSha256(stripQuotes(stat.etag()));
        entity.setRefType(refType);
        entity.setStatus(STATUS_NORMAL);
        entity.setCreator(currentUsernameOrSystem());
        entity.setUpdater(entity.getCreator());
        // ingest 链路无 SecurityContext，多租户插件自动注入 tenant_id=0（平台公共数据）。
        // lr_file_object 默认受租户过滤，故共享法规文件的读取走 loadReadable 中的
        // selectSharedById 跨租户兜底查询，与公共法规主数据（tenant_id=0）保持一致可见。
        try {
            if (TENANT_SHARED_REF_TYPES.contains(refType)) {
                baseMapper.insertShared(entity);
            } else {
                this.save(entity);
            }
        } catch (DuplicateKeyException ex) {
            existing = baseMapper.selectByObjectKey(objectKey);
            if (existing != null) {
                return existing.getId();
            }
            throw ex;
        }
        return entity.getId();
    }

    @Override
    public void promoteForLawProcessing(Long fileId) {
        if (fileId == null) {
            return;
        }
        FileObjectDO entity = this.getById(fileId);
        if (entity == null) {
            entity = baseMapper.selectSharedById(fileId, TENANT_SHARED_REF_TYPES);
        }
        if (entity == null) {
            throw new AppException(ErrorCode.SYS_FILE_NOT_FOUND);
        }
        if (!STATUS_NORMAL.equals(entity.getStatus())) {
            throw new AppException(ErrorCode.SYS_FILE_NOT_UPLOADED);
        }
        if (isTenantSharedLawFile(entity)) {
            return;
        }
        if (!LAW_PROCESS_UPLOAD_REF_TYPES.contains(entity.getRefType())) {
            throw new AppException(ErrorCode.PARAM_ERROR, "file refType is not accepted for law processing");
        }

        String username = SecurityUtils.getCurrentUsername();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        if (!StringUtils.hasText(username)
                || SecurityConstants.ANONYMOUS_USER.equals(username)
                || tenantId == null) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        int updated = baseMapper.promoteSharedLawFile(
                fileId, tenantId, username, "law_version", LAW_PROCESS_UPLOAD_REF_TYPES);
        if (updated == 0) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    @Override
    public FileObjectDO loadForProcessing(Long fileId) {
        FileObjectDO entity = this.getById(fileId);
        if (entity == null) {
            // 异步管线无 SecurityContext，多租户插件按 tenant_id=0 过滤；跨租户兜底查共享法规文件。
            entity = baseMapper.selectSharedById(fileId, TENANT_SHARED_REF_TYPES);
        }
        return entity;
    }

    /** 无 Content-Type 时按扩展名兜底推断 MIME */
    private static String guessMime(String ext) {
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "html", "htm" -> "text/html";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            default -> "application/octet-stream";
        };
    }

    @Override
    public FileObjectVO getFile(Long fileId) {
        return toVO(loadReadable(fileId));
    }

    /**
     * 按 ID 加载文件并校验可读：先按当前租户查（命中租户私有文件）；查不到再按共享法规
     * refType 跨租户兜底查一次（命中 ingest 以 tenant_id=0 落库的公共法规文件）。
     * 兜底查询仅返回共享类型，不会暴露其他租户的私有文件；最终授权由 checkReadable 把关。
     */
    private FileObjectDO loadReadable(Long fileId) {
        FileObjectDO entity = this.getById(fileId);
        if (entity == null) {
            entity = baseMapper.selectSharedById(fileId, TENANT_SHARED_REF_TYPES);
        }
        if (entity == null) {
            throw new AppException(ErrorCode.SYS_FILE_NOT_FOUND);
        }
        checkReadable(entity);
        return entity;
    }

    private void checkReadable(FileObjectDO entity) {
        if (isTenantSharedLawFile(entity)) {
            return;
        }
        checkOwner(entity);
    }

    private void checkOwner(FileObjectDO entity) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername) || !currentUsername.equals(entity.getCreator())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isTenantSharedLawFile(FileObjectDO entity) {
        return Long.valueOf(0L).equals(entity.getTenantId())
                && TENANT_SHARED_REF_TYPES.contains(entity.getRefType());
    }

    private static String currentUsernameOrSystem() {
        String username = SecurityUtils.getCurrentUsername();
        return StringUtils.hasText(username) ? username : "system";
    }

    private void validate(FilePresignDTO dto) {
        if (dto.getFileSize() > props.getMaxFileSize()) {
            throw new AppException(ErrorCode.SYS_FILE_TOO_LARGE);
        }
        if (!contentTypeAllowed(dto.getContentType())) {
            throw new AppException(ErrorCode.SYS_FILE_TYPE_NOT_ALLOWED);
        }
    }

    /** 白名单为空表示不限制；否则要求 contentType 命中白名单 */
    private boolean contentTypeAllowed(String contentType) {
        return CollectionUtils.isEmpty(props.getAllowedContentTypes())
                || props.getAllowedContentTypes().contains(contentType);
    }

    private FileObjectVO toVO(FileObjectDO entity) {
        FileObjectVO vo = new FileObjectVO();
        vo.setId(entity.getId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setFileExt(entity.getFileExt());
        vo.setMimeType(entity.getMimeType());
        vo.setObjectKey(entity.getObjectKey());
        vo.setFileSize(entity.getFileSize());
        vo.setSha256(entity.getSha256());
        vo.setRefType(entity.getRefType());
        vo.setRefId(entity.getRefId());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        if (STATUS_NORMAL.equals(entity.getStatus())) {
            vo.setAccessUrl(fileStorageClient.getAccessUrl(entity.getObjectKey()));
        }
        return vo;
    }

    /** 提取小写扩展名,不含点;无扩展名返回空串 */
    private static String extractExt(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase();
    }

    /** 目录名只保留字母数字下划线短横,防注入路径 */
    private static String sanitizeDir(String refType) {
        String s = refType.replaceAll("[^a-zA-Z0-9_-]", "");
        return s.isEmpty() ? "misc" : s;
    }

    private static String stripQuotes(String etag) {
        if (etag == null) {
            return null;
        }
        return etag.replace("\"", "");
    }
}
