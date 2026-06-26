package cn.xku.law.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DictDataCreateDTO {
    @NotNull private Long dictTypeId;
    @NotBlank private String dictCode;
    @NotBlank private String dictLabel;
    @NotBlank private String dictValue;
    private String parentValue;
    private Integer sortOrder;
    private String status;
    private String extJson;
}
