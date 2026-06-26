package cn.xku.law.search.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 检索历史，对应 lr_search_history */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_search_history")
public class SearchHistoryDO extends BaseDO {

    private Long userId;
    private String keyword;
    /** keyword/advanced/semantic/ai */
    private String searchType;
    private String filtersJson;
    private Integer resultCount;
    private LocalDateTime searchTime;
    private String ip;
    private String userAgent;
}
