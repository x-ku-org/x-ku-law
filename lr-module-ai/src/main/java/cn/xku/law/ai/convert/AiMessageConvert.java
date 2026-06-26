package cn.xku.law.ai.convert;

import cn.xku.law.ai.domain.AiCitationDO;
import cn.xku.law.ai.domain.AiMessageDO;
import cn.xku.law.ai.domain.vo.AiCitationVO;
import cn.xku.law.ai.domain.vo.AiMessageVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiMessageConvert {

    AiMessageVO toVO(AiMessageDO entity);

    List<AiMessageVO> toVOList(List<AiMessageDO> list);

    // cite 标识用「[n]」形式（n = citationOrder = 回答正文里的 [n]），与前端正文 [n] 对应；
    @Mapping(target = "id", expression = "java(entity.getCitationOrder() == null ? null : \"[\" + entity.getCitationOrder() + \"]\")")
    @Mapping(target = "source", source = "sourceTitle")
    @Mapping(target = "article", source = "articleLabel")
    @Mapping(target = "excerpt", source = "quoteText")
    @Mapping(target = "confidence", source = "confidenceScore")
    AiCitationVO toCitationVO(AiCitationDO entity);

    /** 填充前端友好字段：role 取 messageRole，content 按角色取问/答。 */
    @AfterMapping
    default void fillFriendlyFields(AiMessageDO entity, @MappingTarget AiMessageVO vo) {
        vo.setRole(entity.getMessageRole());
        vo.setContent("user".equals(entity.getMessageRole())
                ? entity.getQuestionText()
                : entity.getAnswerText());
    }
}
