package cn.xku.law.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictTypeCreateDTO {
    @NotBlank private String dictCode;
    @NotBlank private String dictName;
    private String status;
    private String remark;
}
