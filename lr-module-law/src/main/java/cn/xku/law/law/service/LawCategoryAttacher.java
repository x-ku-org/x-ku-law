package cn.xku.law.law.service;

/**
 * 把法规文档挂接到「按类型+名称」的分类上。采集/上传接入时调用，幂等：
 * 分类按 (categoryType, categoryName) 取或建，关联按 (documentId, categoryId) 去重。
 *
 * <p>用于落地两类自动分类：地区（region，来自 FLK zdjgCodeId 省份码）与
 * 法规分类/标准类型（subject，来自 FLK flfgCodeId / GB std_code 前缀）。
 */
public interface LawCategoryAttacher {

    /**
     * 确保 (type, name) 分类存在并把文档挂上去。name 为空则忽略（不建空分类）。
     *
     * @param documentId   法规文档 ID
     * @param categoryType 分类类型：subject/region/industry/legal_level
     * @param categoryName 分类名称（如「广东省」「行政法规」「推荐性国家标准」）
     */
    void attach(Long documentId, String categoryType, String categoryName);
}
