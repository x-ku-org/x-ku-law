package cn.xku.law.support.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服工单，对应 lr_ticket。
 * 此包下其余 DO（TicketMessageDO）结构相同，TODO: 按需补全。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_ticket")
public class TicketDO extends BaseDO {

    private Long userId;
    private String ticketNo;
    private String subject;
    private String ticketType;
    private String status;
    private Long assigneeId;
}
