package cn.xku.law.law.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.LawRelationDO;
import cn.xku.law.law.domain.dto.LawRelationCreateDTO;
import cn.xku.law.law.domain.dto.LawRelationQueryDTO;
import cn.xku.law.law.domain.vo.LawRelationVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LawRelationService extends IService<LawRelationDO> {

    PageResult<LawRelationVO> pageRelations(LawRelationQueryDTO query);

    LawRelationVO getRelationById(Long id);

    Long createRelation(LawRelationCreateDTO dto);

    void updateRelation(Long id, LawRelationCreateDTO dto);

    void removeRelation(Long id);
}
