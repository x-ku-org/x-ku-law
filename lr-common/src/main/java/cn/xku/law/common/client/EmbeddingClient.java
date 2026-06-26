package cn.xku.law.common.client;

import java.util.List;

/**
 * 文本嵌入客户端接口：把文本转成向量，供 ES dense_vector / kNN 检索使用。
 * 与具体嵌入模型解耦；未接入真实模型时由 NoOpEmbeddingClient 占位。
 */
public interface EmbeddingClient {

    /** 单条文本 -> 向量。 */
    float[] embed(String text);

    /** 批量文本 -> 向量列表，顺序与入参一致。 */
    List<float[]> embedBatch(List<String> texts);

    /** 向量维度，需与 ES dense_vector 映射的 dims 一致。 */
    int dimension();
}
