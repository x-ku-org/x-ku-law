package cn.xku.law.common.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHit {
    /** ES 文档 ID */
    private String id;
    /** key = 字段名，value = 高亮片段列表 */
    private Map<String, List<String>> highlights;
}
