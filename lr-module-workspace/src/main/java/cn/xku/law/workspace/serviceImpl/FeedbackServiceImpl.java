package cn.xku.law.workspace.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.OwnerValidator;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.workspace.convert.FeedbackConvert;
import cn.xku.law.workspace.domain.FeedbackDO;
import cn.xku.law.workspace.domain.dto.FeedbackCreateDTO;
import cn.xku.law.workspace.domain.dto.FeedbackQueryDTO;
import cn.xku.law.workspace.domain.vo.FeedbackVO;
import cn.xku.law.workspace.mapper.FeedbackMapper;
import cn.xku.law.workspace.service.FeedbackService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, FeedbackDO>
        implements FeedbackService {

    private final FeedbackConvert convert;

    @Override
    public PageResult<FeedbackVO> pageFeedbacks(FeedbackQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<FeedbackDO> wrapper = new LambdaQueryWrapper<FeedbackDO>()
                .eq(FeedbackDO::getUserId, userId)
                .eq(StringUtils.hasText(query.getFeedbackType()),
                        FeedbackDO::getFeedbackType, query.getFeedbackType())
                .eq(StringUtils.hasText(query.getStatus()), FeedbackDO::getStatus, query.getStatus())
                .orderByDesc(FeedbackDO::getCreateTime);
        IPage<FeedbackDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public Long createFeedback(FeedbackCreateDTO dto) {
        FeedbackDO entity = convert.toDO(dto);
        entity.setUserId(SecurityUtils.getCurrentUserId());
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void removeFeedback(Long id) {
        FeedbackDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.NOT_FOUND);
        OwnerValidator.checkOwner(entity.getUserId());
        this.removeById(id);
    }
}
