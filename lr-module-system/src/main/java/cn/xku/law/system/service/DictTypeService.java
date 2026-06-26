package cn.xku.law.system.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.DictTypeDO;
import cn.xku.law.system.domain.dto.DictTypeCreateDTO;
import cn.xku.law.system.domain.dto.DictTypeQueryDTO;
import cn.xku.law.system.domain.vo.DictTypeVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DictTypeService extends IService<DictTypeDO> {
    PageResult<DictTypeVO> pageDictTypes(DictTypeQueryDTO query);
    Long createDictType(DictTypeCreateDTO dto);
    void updateDictType(Long id, DictTypeCreateDTO dto);
    void removeDictType(Long id);
}
