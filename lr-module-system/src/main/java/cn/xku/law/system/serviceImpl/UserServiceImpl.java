package cn.xku.law.system.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.PermissionCacheManager;
import cn.xku.law.system.convert.UserConvert;
import cn.xku.law.system.domain.UserDO;
import cn.xku.law.system.domain.dto.UserCreateDTO;
import cn.xku.law.system.domain.dto.UserQueryDTO;
import cn.xku.law.system.domain.dto.UserUpdateDTO;
import cn.xku.law.system.domain.UserRoleDO;
import cn.xku.law.system.domain.vo.UserVO;
import cn.xku.law.system.mapper.UserMapper;
import cn.xku.law.system.mapper.UserRoleMapper;
import cn.xku.law.system.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final UserConvert convert;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheManager permissionCacheManager;
    private final UserRoleMapper userRoleMapper;

    @Override
    public PageResult<UserVO> pageUsers(UserQueryDTO query) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<UserDO>()
                .like(StringUtils.hasText(query.getUsername()), UserDO::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getRealName()), UserDO::getRealName, query.getRealName())
                .like(StringUtils.hasText(query.getMobile()), UserDO::getMobile, query.getMobile())
                .eq(StringUtils.hasText(query.getUserType()), UserDO::getUserType, query.getUserType())
                .eq(StringUtils.hasText(query.getStatus()), UserDO::getStatus, query.getStatus())
                .orderByDesc(UserDO::getCreateTime);
        IPage<UserDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public UserVO getUserById(Long id) {
        UserDO user = this.getById(id);
        if (user == null) throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        return convert.toVO(user);
    }

    @Override
    public Long createUser(UserCreateDTO dto) {
        UserDO user = convert.toDO(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        this.save(user);
        return user.getId();
    }

    @Override
    public void updateUser(Long id, UserUpdateDTO dto) {
        UserDO user = this.getById(id);
        if (user == null) throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        convert.updateDO(dto, user);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            user.setPasswordUpdateTime(LocalDateTime.now());
        }
        this.updateById(user);
        permissionCacheManager.evictUser(id);
    }

    @Override
    public void removeUser(Long id) {
        if (!this.removeById(id)) throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        permissionCacheManager.evictUser(id);
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        return userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getUserId, userId))
                .stream()
                .map(UserRoleDO::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        UserDO user = this.getById(userId);
        if (user == null) throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        userRoleMapper.physicalDeleteByUserId(userId);
        if (roleIds != null) {
            roleIds.stream().filter(Objects::nonNull).distinct().forEach(roleId -> {
                UserRoleDO link = new UserRoleDO();
                link.setUserId(userId);
                link.setRoleId(roleId);
                userRoleMapper.insert(link);
            });
        }
        permissionCacheManager.evictUser(userId);
    }
}
