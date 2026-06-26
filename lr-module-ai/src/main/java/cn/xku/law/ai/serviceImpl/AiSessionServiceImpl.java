package cn.xku.law.ai.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.OwnerValidator;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.ai.convert.AiSessionConvert;
import cn.xku.law.ai.domain.AiSessionDO;
import cn.xku.law.ai.domain.dto.AiSessionQueryDTO;
import cn.xku.law.ai.domain.vo.AiSessionVO;
import cn.xku.law.ai.mapper.AiSessionMapper;
import cn.xku.law.ai.service.AiSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiSessionServiceImpl extends ServiceImpl<AiSessionMapper, AiSessionDO>
        implements AiSessionService {

    private final AiSessionConvert convert;

    @Override
    public PageResult<AiSessionVO> pageSessions(AiSessionQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<AiSessionDO> wrapper = new LambdaQueryWrapper<AiSessionDO>()
                .eq(AiSessionDO::getUserId, userId)
                .eq(StringUtils.hasText(query.getScenarioType()),
                        AiSessionDO::getScenarioType, query.getScenarioType())
                .eq(StringUtils.hasText(query.getStatus()), AiSessionDO::getStatus, query.getStatus())
                .orderByDesc(AiSessionDO::getLastMessageTime);
        IPage<AiSessionDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public AiSessionVO getSessionById(Long id) {
        AiSessionDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.AI_SESSION_NOT_FOUND);
        }
        OwnerValidator.checkOwner(entity.getUserId());
        return convert.toVO(entity);
    }

    @Override
    public void removeSession(Long id) {
        AiSessionDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.AI_SESSION_NOT_FOUND);
        OwnerValidator.checkOwner(entity.getUserId());
        this.removeById(id);
    }

    /** 会话标题最大长度（取首问截断）。 */
    private static final int TITLE_MAX_LEN = 40;

    @Override
    public Long ensureSession(Long sessionId, Long userId, String firstQuestion,
                              String scenarioType, String modelCode) {
        LocalDateTime now = LocalDateTime.now();
        if (sessionId != null) {
            AiSessionDO entity = this.getById(sessionId);
            if (entity == null) {
                throw new AppException(ErrorCode.AI_SESSION_NOT_FOUND);
            }
            OwnerValidator.checkOwner(entity.getUserId());
            AiSessionDO update = new AiSessionDO();
            update.setId(sessionId);
            update.setLastMessageTime(now);
            if (StringUtils.hasText(modelCode)) {
                update.setModelCode(modelCode);
            }
            this.updateById(update);
            return sessionId;
        }
        AiSessionDO created = new AiSessionDO();
        created.setUserId(userId);
        created.setSessionTitle(buildTitle(firstQuestion));
        created.setScenarioType(StringUtils.hasText(scenarioType) ? scenarioType : "qa");
        created.setModelCode(modelCode);
        created.setStatus("normal");
        created.setLastMessageTime(now);
        this.save(created);
        return created.getId();
    }

    @Override
    public void updateTitle(Long sessionId, String title) {
        if (sessionId == null || !StringUtils.hasText(title)) {
            return;
        }
        AiSessionDO update = new AiSessionDO();
        update.setId(sessionId);
        update.setSessionTitle(title);
        this.updateById(update);
    }

    @Override
    public void updateContextSummary(Long sessionId, String summary, Long summaryUptoMessageId) {
        if (sessionId == null) {
            return;
        }
        AiSessionDO update = new AiSessionDO();
        update.setId(sessionId);
        update.setContextSummary(summary);
        update.setSummaryUptoMessageId(summaryUptoMessageId);
        this.updateById(update);
    }

    private String buildTitle(String firstQuestion) {
        if (!StringUtils.hasText(firstQuestion)) {
            return "新会话";
        }
        String oneLine = firstQuestion.strip().replaceAll("\\s+", " ");
        return oneLine.length() > TITLE_MAX_LEN ? oneLine.substring(0, TITLE_MAX_LEN) + "…" : oneLine;
    }
}
