package cn.xku.law.file;

import cn.xku.law.file.domain.FileObjectDO;
import cn.xku.law.file.dto.FilePresignDTO;
import cn.xku.law.file.vo.FileObjectVO;
import cn.xku.law.file.vo.FilePresignVO;
import com.baomidou.mybatisplus.extension.service.IService;

/** 文件直传业务接口 */
public interface FileService extends IService<FileObjectDO> {

    /** 校验并签发预签名直传 URL，落一条 pending 记录 */
    FilePresignVO presign(FilePresignDTO dto);

    /** 前端直传完成后回调:HEAD 校验对象,置为 normal 并回填真实大小/ETag */
    FileObjectVO complete(Long fileId);

    /** 获取文件临时访问 URL（需已 normal） */
    String getAccessUrl(Long fileId);

    /** 查询文件元信息（含临时访问 URL） */
    FileObjectVO getFile(Long fileId);

    /**
     * 登记一个已存在于对象存储的对象（采集接入流程：文件已由外部上传到已知 objectKey）。
     * 通过 HEAD 读取真实大小/类型/ETag，落一条 normal 状态记录，返回 file_id；对象不存在返回 null。
     */
    Long registerExisting(String objectKey, String originalName, String refType);

    /**
     * 将已完成上传的法规正文文件提升为平台共享文件，供无登录上下文的异步处理管线读取。
     * 仅允许法规接入使用的 refType，且必须是当前上传者自己的 normal 文件。
     */
    void promoteForLawProcessing(Long fileId);

    /**
     * 内部处理管线用：按 fileId 取文件元信息，不做 owner 校验。
     * 先按当前租户查，查不到再跨租户兜底查共享法规文件（tenant_id=0）。供异步处理管线
     * （无 SecurityContext）解析 objectKey 用。返回 null 表示不存在。
     */
    FileObjectDO loadForProcessing(Long fileId);
}
