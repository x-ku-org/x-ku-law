package cn.xku.law.search.convert;

import cn.xku.law.search.domain.SavedSearchDO;
import cn.xku.law.search.domain.dto.SavedSearchCreateDTO;
import cn.xku.law.search.domain.vo.SavedSearchVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SavedSearchConvert {

    SavedSearchVO toVO(SavedSearchDO entity);

    List<SavedSearchVO> toVOList(List<SavedSearchDO> list);

    SavedSearchDO toDO(SavedSearchCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(SavedSearchCreateDTO dto, @MappingTarget SavedSearchDO entity);
}
