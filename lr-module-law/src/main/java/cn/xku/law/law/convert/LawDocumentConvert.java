package cn.xku.law.law.convert;

import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.dto.LawDocumentCreateDTO;
import cn.xku.law.law.domain.vo.LawDocumentVO;
import org.mapstruct.*;

import java.util.List;

/** LawDocument 对象转换器，由 MapStruct 在编译期生成实现类 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LawDocumentConvert {

    LawDocumentVO toVO(LawDocumentDO doc);

    List<LawDocumentVO> toVOList(List<LawDocumentDO> docs);

    LawDocumentDO toDO(LawDocumentCreateDTO dto);

    /** 用 DTO 中的非空字段覆盖 DO，用于 update 场景 */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(LawDocumentCreateDTO dto, @MappingTarget LawDocumentDO doc);
}
