package cn.xku.law.common.client.dto;

/**
 * 向量 kNN 检索命中：文档 ID + 相似度分值（ES `_score`）。
 * cosine 相似度下 score ∈ [0,1]（越大越相似）。
 */
public record VectorMatch(String id, double score) {
}
