package cn.xku.law.ai.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 「立即作答」请求：按 streamId 停止当前在途问答的继续检索。 */
@Data
public class StopAskDTO {
    /** 由 ask 流的 meta 事件下发的流 ID。 */
    @NotBlank(message = "streamId 不能为空")
    private String streamId;
}
