package cn.xku.law.ai.serviceImpl;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.ai.convert.AiMessageConvert;
import cn.xku.law.ai.domain.AiCitationDO;
import cn.xku.law.ai.domain.AiFeedbackDO;
import cn.xku.law.ai.domain.AiMessageDO;
import cn.xku.law.ai.domain.dto.AiAnswerCommand;
import cn.xku.law.ai.domain.dto.AiMessageQueryDTO;
import cn.xku.law.ai.domain.vo.AiCitationVO;
import cn.xku.law.ai.domain.vo.AiMessageVO;
import cn.xku.law.ai.mapper.AiCitationMapper;
import cn.xku.law.ai.mapper.AiFeedbackMapper;
import cn.xku.law.ai.mapper.AiMessageMapper;
import cn.xku.law.ai.service.AiMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessageDO>
        implements AiMessageService {

    private final AiMessageConvert convert;
    private final AiCitationMapper citationMapper;
    private final AiFeedbackMapper feedbackMapper;

    @Override
    public PageResult<AiMessageVO> pageMessages(AiMessageQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<AiMessageDO> wrapper = new LambdaQueryWrapper<AiMessageDO>()
                .eq(AiMessageDO::getUserId, userId)
                .eq(query.getSessionId() != null, AiMessageDO::getSessionId, query.getSessionId())
                .orderByAsc(AiMessageDO::getCreateTime);
        IPage<AiMessageDO> page = this.page(query.toPage(), wrapper);
        List<AiMessageDO> records = page.getRecords();
        List<AiMessageVO> vos = convert.toVOList(records);
        attachCitations(records, vos);
        attachLiked(userId, records, vos);
        return PageResult.of(page.getTotal(), vos);
    }

    /** 标记当前用户已点赞的回答，供前端恢复「已赞」状态。 */
    private void attachLiked(Long userId, List<AiMessageDO> records, List<AiMessageVO> vos) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> messageIds = records.stream().map(AiMessageDO::getId).toList();
        List<AiFeedbackDO> likes = feedbackMapper.selectList(
                new LambdaQueryWrapper<AiFeedbackDO>()
                        .eq(AiFeedbackDO::getUserId, userId)
                        .eq(AiFeedbackDO::getFeedbackType, "like")
                        .in(AiFeedbackDO::getMessageId, messageIds));
        Set<Long> likedIds = likes.stream().map(AiFeedbackDO::getMessageId).collect(Collectors.toSet());
        for (AiMessageVO vo : vos) {
            vo.setLiked(likedIds.contains(vo.getId()));
        }
    }

    /** 批量加载并挂接每条消息的引用依据，避免 N+1。 */
    private void attachCitations(List<AiMessageDO> records, List<AiMessageVO> vos) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> messageIds = records.stream().map(AiMessageDO::getId).toList();
        List<AiCitationDO> citations = citationMapper.selectList(
                new LambdaQueryWrapper<AiCitationDO>()
                        .in(AiCitationDO::getMessageId, messageIds)
                        .orderByAsc(AiCitationDO::getCitationOrder));
        Map<Long, List<AiCitationVO>> byMessage = citations.stream()
                .collect(Collectors.groupingBy(AiCitationDO::getMessageId,
                        Collectors.mapping(convert::toCitationVO, Collectors.toList())));
        for (AiMessageVO vo : vos) {
            vo.setCitations(byMessage.getOrDefault(vo.getId(), List.of()));
        }
    }

    @Override
    public Long appendUserMessage(Long sessionId, Long userId, String question) {
        AiMessageDO message = new AiMessageDO();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setMessageRole("user");
        message.setQuestionText(question);
        message.setTokensPrompt(0);
        message.setTokensCompletion(0);
        message.setLatencyMs(0);
        message.setRiskLevel("normal");
        this.save(message);
        return message.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long appendAssistantMessage(AiAnswerCommand command) {
        AiMessageDO message = new AiMessageDO();
        message.setSessionId(command.getSessionId());
        message.setUserId(command.getUserId());
        message.setMessageRole("assistant");
        message.setAnswerText(command.getAnswerText());
        message.setModelCode(command.getModelCode());
        message.setTokensPrompt(command.getTokensPrompt() != null ? command.getTokensPrompt() : 0);
        message.setTokensCompletion(command.getTokensCompletion() != null ? command.getTokensCompletion() : 0);
        message.setLatencyMs(command.getLatencyMs() != null ? command.getLatencyMs() : 0);
        message.setRetrievalParamsJson(command.getRetrievalParamsJson());
        message.setRiskLevel(command.getRiskLevel() != null ? command.getRiskLevel() : "normal");
        this.save(message);

        Long messageId = message.getId();
        List<AiCitationDO> citations = command.getCitations();
        if (citations != null && !citations.isEmpty()) {
            int order = 1;
            for (AiCitationDO citation : citations) {
                citation.setMessageId(messageId);
                if (citation.getCitationOrder() == null) {
                    citation.setCitationOrder(order);
                }
                order++;
                citationMapper.insert(citation);
            }
        }
        return messageId;
    }
}
