package cn.xku.law.system.domain.vo;

import lombok.Data;

@Data
public class DictDataVO {
    private Long id;
    private Long dictTypeId;
    private String dictCode;
    private String dictLabel;
    private String dictValue;
    private String parentValue;
    private Integer sortOrder;
    private String status;
    private String extJson;
}
