package cn.xku.law.collect.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 采集记录，对应 lr_collect_record。
 * 本接入流程中用作“运行文件夹处理标记”：request_url 存 minio://bucket/&lt;folder&gt;/，
 * collect_status 走 pending→processing(CAS)→success/failed，配合超时重置实现幂等与重试。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_collect_record")
public class CollectRecordDO extends BaseDO {

    private Long taskId;
    private Long sourceId;
    private String requestUrl;
    private Integer httpStatus;
    private Long rawFileId;
    private String contentHash;
    /** pending/processing/success/failed */
    private String collectStatus;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
