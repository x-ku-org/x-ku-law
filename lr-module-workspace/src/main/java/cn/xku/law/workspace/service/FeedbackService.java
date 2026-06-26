package cn.xku.law.workspace.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.workspace.domain.FeedbackDO;
import cn.xku.law.workspace.domain.dto.FeedbackCreateDTO;
import cn.xku.law.workspace.domain.dto.FeedbackQueryDTO;
import cn.xku.law.workspace.domain.vo.FeedbackVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FeedbackService extends IService<FeedbackDO> {

    PageResult<FeedbackVO> pageFeedbacks(FeedbackQueryDTO query);

    Long createFeedback(FeedbackCreateDTO dto);

    void removeFeedback(Long id);
}
