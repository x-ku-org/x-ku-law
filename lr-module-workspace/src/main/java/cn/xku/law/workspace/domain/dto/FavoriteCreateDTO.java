package cn.xku.law.workspace.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteCreateDTO {

    @NotBlank
    private String refType;
    @NotNull
    private Long refId;
    private String folderName;
    private String titleSnapshot;
}
