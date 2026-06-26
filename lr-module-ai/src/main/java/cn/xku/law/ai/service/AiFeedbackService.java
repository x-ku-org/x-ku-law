package cn.xku.law.ai.service;

import cn.xku.law.ai.domain.AiFeedbackDO;
import cn.xku.law.ai.domain.dto.AiFeedbackCreateDTO;
import cn.xku.law.ai.domain.dto.AiFeedbackQueryDTO;
import cn.xku.law.ai.domain.vo.AiFeedbackVO;
import cn.xku.law.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AiFeedbackService extends IService<AiFeedbackDO> {

    /** 用户对某条 AI 回答提交反馈（点赞/纠错/转人工）；校验消息归属。 */
    Long createFeedback(AiFeedbackCreateDTO dto);

    /** 切换某条回答的点赞状态（已赞则取消，未赞则点赞）；返回切换后的点赞状态。校验消息归属。 */
    boolean toggleLike(Long messageId);

    /** 管理员分页查询全部反馈（可按类型/处理状态过滤）。 */
    PageResult<AiFeedbackVO> pageFeedbacks(AiFeedbackQueryDTO query);

    /** 管理员处理反馈：更新处理状态、处理人与处理时间。 */
    void handleFeedback(Long id, String handledStatus);
}
