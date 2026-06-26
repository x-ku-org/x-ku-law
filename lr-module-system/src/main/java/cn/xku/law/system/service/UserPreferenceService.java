package cn.xku.law.system.service;

import java.util.Map;

/** 用户个性化偏好读写；当前作用于 lr_user_preference 键值表。 */
public interface UserPreferenceService {

    /** 返回指定用户的全部偏好（key → value）。 */
    Map<String, String> getPreferences(Long userId);

    /**
     * 按 key upsert 偏好。preference_group 由 key 前缀自动推导（'.' 之前的段，缺省 general），
     * 遵循 uk_user_preference(tenant_id, user_id, preference_key)。
     */
    void savePreferences(Long userId, Map<String, String> kv);
}
