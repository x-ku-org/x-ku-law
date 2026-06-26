package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 法规状态变更历史，对应 lr_law_status_change */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_status_change")
public class LawStatusChangeDO extends BaseDO {

    private Long documentId;
    private String oldStatus;
    private String newStatus;
    private String changeReason;
    private LocalDateTime changeDate;
    private Long operatorUserId;
}
