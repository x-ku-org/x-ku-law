package cn.xku.law.law.convert;

import cn.xku.law.law.domain.LawArticleDO;
import cn.xku.law.law.domain.dto.LawArticleCreateDTO;
import cn.xku.law.law.domain.vo.LawArticleVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LawArticleConvert {

    LawArticleVO toVO(LawArticleDO entity);

    List<LawArticleVO> toVOList(List<LawArticleDO> list);

    LawArticleDO toDO(LawArticleCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(LawArticleCreateDTO dto, @MappingTarget LawArticleDO entity);
}
