package cn.xku.law.law.service;

/**
 * 把法规文档挂接到「按类型+名称」的标签上。处理管线（AI 元数据富集）/采集/上传时调用，幂等：
 * 标签按 (tagType, tagName) 取或建，关联按 (documentId, tagId) 去重。
 *
 * <p>与 {@link LawCategoryAttacher} 同构：category 是受控分类维度（region/subject/industry），
 * tag 是更自由的关键词维度（多由 AI 从正文抽取，如「数据安全」「行政许可」）。
 */
public interface LawTagAttacher {

    /**
     * 确保 (type, name) 标签存在并把文档挂上去。name 为空则忽略（不建空标签）。
     *
     * @param documentId 法规文档 ID
     * @param tagType    标签类型：law/topic/user/system
     * @param tagName    标签名称（如「数据安全」「行政许可」）
     */
    void attach(Long documentId, String tagType, String tagName);

    /** 便捷重载：默认 tagType=law。 */
    default void attach(Long documentId, String tagName) {
        attach(documentId, "law", tagName);
    }
}
