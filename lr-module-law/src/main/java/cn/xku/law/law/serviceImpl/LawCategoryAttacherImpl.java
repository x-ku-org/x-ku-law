package cn.xku.law.law.serviceImpl;

import cn.xku.law.law.domain.LawCategoryDO;
import cn.xku.law.law.domain.LawDocumentCategoryDO;
import cn.xku.law.law.mapper.LawCategoryMapper;
import cn.xku.law.law.mapper.LawDocumentCategoryMapper;
import cn.xku.law.law.service.LawCategoryAttacher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawCategoryAttacherImpl implements LawCategoryAttacher {

    private final LawCategoryMapper categoryMapper;
    private final LawDocumentCategoryMapper documentCategoryMapper;

    @Override
    public void attach(Long documentId, String categoryType, String categoryName) {
        if (documentId == null || !StringUtils.hasText(categoryName) || !StringUtils.hasText(categoryType)) {
            return;
        }
        Long categoryId = resolveOrCreateCategory(categoryType, categoryName.trim());
        linkIfAbsent(documentId, categoryId);
    }

    /** 按 (type, name) 取或建分类。category_code 由 type+name 派生，保证跨次运行稳定、不重复。 */
    private Long resolveOrCreateCategory(String type, String name) {
        String code = categoryCode(type, name);
        LawCategoryDO existing = categoryMapper.selectOne(new LambdaQueryWrapper<LawCategoryDO>()
                .eq(LawCategoryDO::getCategoryCode, code)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing.getId();
        }
        LawCategoryDO category = new LawCategoryDO();
        category.setParentId(0L);
        category.setCategoryCode(code);
        category.setCategoryName(name);
        category.setCategoryType(type);
        category.setSortOrder(0);
        category.setStatus("enabled");
        try {
            categoryMapper.insert(category);
            return category.getId();
        } catch (DuplicateKeyException e) {
            LawCategoryDO raced = categoryMapper.selectOne(new LambdaQueryWrapper<LawCategoryDO>()
                    .eq(LawCategoryDO::getCategoryCode, code)
                    .last("LIMIT 1"));
            if (raced != null) {
                return raced.getId();
            }
            throw e;
        }
    }

    /** (documentId, categoryId) 关联去重后插入。 */
    private void linkIfAbsent(Long documentId, Long categoryId) {
        Long exists = documentCategoryMapper.selectCount(new LambdaQueryWrapper<LawDocumentCategoryDO>()
                .eq(LawDocumentCategoryDO::getDocumentId, documentId)
                .eq(LawDocumentCategoryDO::getCategoryId, categoryId));
        if (exists != null && exists > 0) {
            return;
        }
        LawDocumentCategoryDO link = new LawDocumentCategoryDO();
        link.setDocumentId(documentId);
        link.setCategoryId(categoryId);
        try {
            documentCategoryMapper.insert(link);
        } catch (DuplicateKeyException e) {
            log.debug("[CategoryAttach] duplicate link doc={} cat={}, ignored", documentId, categoryId);
        }
    }

    /** category_code = {type}:{name 的短哈希}，稳定且满足唯一键 uk_category_code(tenant_id, category_code)。 */
    private static String categoryCode(String type, String name) {
        return type + ":" + sha1(name).substring(0, 16);
    }

    private static String sha1(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }
}
