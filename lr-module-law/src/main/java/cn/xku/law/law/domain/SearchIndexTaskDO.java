package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 检索索引同步任务，对应 lr_search_index_task */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_search_index_task")
public class SearchIndexTaskDO extends BaseDO {

    private String refType;
    private Long refId;
    private String indexName;
    /** upsert/delete/rebuild */
    private String actionType;
    /** pending/processing/done/failed */
    private String syncStatus;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime lastSyncTime;
}
