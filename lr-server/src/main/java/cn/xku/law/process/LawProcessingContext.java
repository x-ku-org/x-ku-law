package cn.xku.law.process;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 处理管线上下文：在各 {@link LawProcessingStage} 之间传递。
 * 前置阶段产出（如提取的正文）写入本上下文，供后续阶段读取，避免重复 IO。
 */
@Getter
@Setter
@ToString
public class LawProcessingContext {

    private final Long taskId;
    private final Long documentId;
    private final Long versionId;
    /** 正文文件 ID，可为空（仅元数据接入时无文件） */
    private final Long fileId;

    /** 文本提取阶段产出的正文；后续分段阶段消费。null/空表示无可分段正文。 */
    private String extractedText;

    public LawProcessingContext(Long taskId, Long documentId, Long versionId, Long fileId) {
        this.taskId = taskId;
        this.documentId = documentId;
        this.versionId = versionId;
        this.fileId = fileId;
    }
}
