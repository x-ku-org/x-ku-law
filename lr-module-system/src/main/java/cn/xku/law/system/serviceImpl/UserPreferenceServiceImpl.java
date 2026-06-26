package cn.xku.law.system.serviceImpl;

import cn.xku.law.system.domain.UserPreferenceDO;
import cn.xku.law.system.mapper.UserPreferenceMapper;
import cn.xku.law.system.service.UserPreferenceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceMapper preferenceMapper;

    @Override
    public Map<String, String> getPreferences(Long userId) {
        Map<String, String> result = new LinkedHashMap<>();
        if (userId == null) {
            return result;
        }
        List<UserPreferenceDO> list = preferenceMapper.selectList(
                new LambdaQueryWrapper<UserPreferenceDO>().eq(UserPreferenceDO::getUserId, userId));
        for (UserPreferenceDO pref : list) {
            if (StringUtils.hasText(pref.getPreferenceKey())) {
                result.put(pref.getPreferenceKey(), pref.getPreferenceValue());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePreferences(Long userId, Map<String, String> kv) {
        if (userId == null || kv == null || kv.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : kv.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            UserPreferenceDO existing = preferenceMapper.selectOne(
                    new LambdaQueryWrapper<UserPreferenceDO>()
                            .eq(UserPreferenceDO::getUserId, userId)
                            .eq(UserPreferenceDO::getPreferenceKey, key)
                            .last("LIMIT 1"));
            if (existing != null) {
                existing.setPreferenceValue(entry.getValue());
                existing.setPreferenceGroup(groupOf(key));
                preferenceMapper.updateById(existing);
            } else {
                UserPreferenceDO created = new UserPreferenceDO();
                created.setUserId(userId);
                created.setPreferenceKey(key);
                created.setPreferenceValue(entry.getValue());
                created.setPreferenceGroup(groupOf(key));
                try {
                    preferenceMapper.insert(created);
                } catch (DuplicateKeyException e) {
                    // selectOne 与 insert 非原子：并发保存同一 key 时另一请求可能已插入，
                    // uk_user_preference 兜底拦下后回退为按唯一键更新，避免 500。
                    updateValueByKey(userId, key, entry.getValue());
                }
            }
        }
    }

    /** 按 (user_id, preference_key) 唯一键更新值与分组，用于并发插入冲突后的回退。 */
    private void updateValueByKey(Long userId, String key, String value) {
        preferenceMapper.update(null, new LambdaUpdateWrapper<UserPreferenceDO>()
                .eq(UserPreferenceDO::getUserId, userId)
                .eq(UserPreferenceDO::getPreferenceKey, key)
                .set(UserPreferenceDO::getPreferenceValue, value)
                .set(UserPreferenceDO::getPreferenceGroup, groupOf(key)));
    }

    /** group 取 key 中第一个 '.' 之前的段（如 search.sort → search），无前缀时归入 general。 */
    private String groupOf(String key) {
        int dot = key.indexOf('.');
        return dot > 0 ? key.substring(0, dot) : "general";
    }
}
