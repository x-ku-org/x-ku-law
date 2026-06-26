package cn.xku.law.workspace.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户收藏，对应 lr_favorite */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_favorite")
public class FavoriteDO extends BaseDO {

    private Long userId;
    /** law_document/law_article/topic/search */
    private String refType;
    private Long refId;
    private String folderName;
    private String titleSnapshot;
}
