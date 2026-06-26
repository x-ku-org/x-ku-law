package cn.xku.law.collect.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 解析出的单条条款（扁平结构；章节层级为后续扩展）。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedArticle {

    /** 条款号，如 "第一条" */
    private String articleNo;
    private String articleTitle;
    private String contentText;
    /** 文档内顺序，从 1 开始 */
    private Integer articleOrder;
    /** 层级深度，扁平解析固定为 1 */
    private Integer articleLevel;
}
