package cn.xku.law.workspace.convert;

import cn.xku.law.workspace.domain.FeedbackDO;
import cn.xku.law.workspace.domain.dto.FeedbackCreateDTO;
import cn.xku.law.workspace.domain.vo.FeedbackVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeedbackConvert {

    FeedbackVO toVO(FeedbackDO entity);

    List<FeedbackVO> toVOList(List<FeedbackDO> list);

    FeedbackDO toDO(FeedbackCreateDTO dto);
}
