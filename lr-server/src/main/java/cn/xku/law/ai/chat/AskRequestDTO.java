package cn.xku.law.ai.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** AI 可溯源问答请求。 */
@Data
public class AskRequestDTO {
    /** 已有会话 ID；为空则新建会话。 */
    private Long sessionId;
    /** 用户问题。 */
    @NotBlank(message = "问题不能为空")
    private String question;
    /** 可选：限定检索范围到某部法规（从法规详情页发起时携带）。 */
    private Long documentId;
    /** 可选：指定模型 provider，缺省用默认 provider。 */
    private String provider;
}
