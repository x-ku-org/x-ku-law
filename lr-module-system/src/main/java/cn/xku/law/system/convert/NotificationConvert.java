package cn.xku.law.system.convert;

import cn.xku.law.system.domain.NotificationDO;
import cn.xku.law.system.domain.dto.NotificationCreateDTO;
import cn.xku.law.system.domain.vo.NotificationVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationConvert {
    NotificationVO toVO(NotificationDO notification);
    List<NotificationVO> toVOList(List<NotificationDO> list);
    NotificationDO toDO(NotificationCreateDTO dto);
}
