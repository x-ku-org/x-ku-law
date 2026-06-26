package cn.xku.law.search.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class LawSearchResultVO {
    private Long versionId;
    private Long documentId;
    private String title;
    private String docNumber;
    private String effectLevel;
    private String status;
    private String publishAuthority;
    private LocalDate effectiveDate;
    /** key = 字段名，value = 高亮片段（含 <em> 标签） */
    private Map<String, List<String>> highlights;
}
