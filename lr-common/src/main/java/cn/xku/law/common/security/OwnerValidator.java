package cn.xku.law.common.security;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;

/** 用户私有资源归属校验：比对实体 userId 与当前登录用户，不匹配则抛 403 */
public final class OwnerValidator {

    private OwnerValidator() {}

    public static void checkOwner(Long entityUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(entityUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
}
