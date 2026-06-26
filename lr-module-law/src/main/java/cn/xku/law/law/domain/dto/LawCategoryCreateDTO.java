package cn.xku.law.law.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LawCategoryCreateDTO {

    private Long parentId;
    @NotBlank
    private String categoryCode;
    @NotBlank
    private String categoryName;
    private String categoryType;
    private Integer sortOrder;
    private String status;
}
