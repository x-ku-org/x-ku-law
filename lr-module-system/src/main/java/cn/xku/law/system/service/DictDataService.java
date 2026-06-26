package cn.xku.law.system.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.DictDataDO;
import cn.xku.law.system.domain.dto.DictDataCreateDTO;
import cn.xku.law.system.domain.dto.DictDataQueryDTO;
import cn.xku.law.system.domain.vo.DictDataVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface DictDataService extends IService<DictDataDO> {
    PageResult<DictDataVO> pageDictData(DictDataQueryDTO query);
    List<DictDataVO> listByDictCode(String dictCode);
    /** 批量按字典编码取启用数据，按 dictCode 分组（前端一次性加载下拉/标签词表用）。 */
    Map<String, List<DictDataVO>> listByDictCodes(List<String> dictCodes);
    Long createDictData(DictDataCreateDTO dto);
    void updateDictData(Long id, DictDataCreateDTO dto);
    void removeDictData(Long id);
}
