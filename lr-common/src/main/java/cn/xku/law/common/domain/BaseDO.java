package cn.xku.law.common.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 所有持久化实体的公共字段，对应 lr_* 表中 7 个通用列 */
@Data
public abstract class BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者用户名，由 MetaObjectFillHandler 自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最后更新者用户名，由 MetaObjectFillHandler 自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标志，对应 DB bit(1) DEFAULT b'0'，false=未删除 */
    @TableLogic
    private Boolean deleted;

    /** 租户ID；0 表示平台公共数据，由多租户插件在查询时自动注入 */
    private Long tenantId;
}
