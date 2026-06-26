package cn.xku.law.search.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 保存检索，对应 lr_saved_search */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_saved_search")
public class SavedSearchDO extends BaseDO {

    private Long userId;
    private String name;
    private String keyword;
    private String filtersJson;
    private Boolean notifyEnabled;
    private String status;
}
