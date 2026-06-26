package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 向量同步任务，对应 lr_vector_sync_task */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_vector_sync_task")
public class VectorSyncTaskDO extends BaseDO {

    private String refType;
    private Long refId;
    /** upsert/delete/rebuild */
    private String actionType;
    /** pending/processing/done/failed */
    private String syncStatus;
    private String vectorIndex;
    private String vectorId;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime lastSyncTime;
}
