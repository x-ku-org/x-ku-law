package cn.xku.law.law.service;

import cn.xku.law.law.domain.LawProcessTaskDO;
import com.baomidou.mybatisplus.extension.service.IService;

/** 法规处理管线任务入队服务。两条接入入口（采集批量 / 管理员上传）建好版本后调用本服务入队。 */
public interface LawProcessTaskService extends IService<LawProcessTaskDO> {

    /**
     * 为某法规版本入队一条处理任务（pending）。若该版本已有在途任务则跳过，幂等。
     *
     * @param documentId 法规文档 ID
     * @param versionId  法规版本 ID
     * @param fileId     正文文件 ID，可为空（仅元数据时管线跳过提取/分段）
     * @return 新建任务 ID；已存在在途任务时返回 null
     */
    Long enqueue(Long documentId, Long versionId, Long fileId);
}
