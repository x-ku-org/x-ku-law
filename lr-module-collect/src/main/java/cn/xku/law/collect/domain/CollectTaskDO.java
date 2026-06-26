package cn.xku.law.collect.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 采集任务，对应 lr_collect_task。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_collect_task")
public class CollectTaskDO extends BaseDO {

    private Long sourceId;
    private String taskName;
    /** 采集类型：list/detail/file/api */
    private String collectType;
    private String targetUrl;
    private String cronExpr;
    /** 解析器编码，运行期据此选择来源/解析逻辑，如 flk / gb */
    private String parserCode;
    private String status;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
}
