package cn.xku.law.search.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SavedSearchCreateDTO {

    @NotBlank
    private String name;
    private String keyword;
    private String filtersJson;
    private Boolean notifyEnabled;
    private String status;
}
