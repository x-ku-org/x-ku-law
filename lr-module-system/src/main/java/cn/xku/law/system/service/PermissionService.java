package cn.xku.law.system.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.PermissionDO;
import cn.xku.law.system.domain.dto.PermissionCreateDTO;
import cn.xku.law.system.domain.dto.PermissionQueryDTO;
import cn.xku.law.system.domain.vo.PermissionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PermissionService extends IService<PermissionDO> {
    PageResult<PermissionVO> pagePermissions(PermissionQueryDTO query);
    List<PermissionVO> listAll();
    Long createPermission(PermissionCreateDTO dto);
    void updatePermission(Long id, PermissionCreateDTO dto);
    void removePermission(Long id);
}
