package cn.xku.law.system.domain.vo;

import lombok.Data;

@Data
public class DictTypeVO {
    private Long id;
    private String dictCode;
    private String dictName;
    private String status;
    private String remark;
}
