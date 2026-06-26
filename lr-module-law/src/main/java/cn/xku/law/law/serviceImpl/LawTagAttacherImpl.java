package cn.xku.law.law.serviceImpl;

import cn.xku.law.law.domain.LawDocumentTagDO;
import cn.xku.law.law.domain.TagDO;
import cn.xku.law.law.mapper.LawDocumentTagMapper;
import cn.xku.law.law.mapper.TagMapper;
import cn.xku.law.law.service.LawTagAttacher;
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
public class LawTagAttacherImpl implements LawTagAttacher {

    private final TagMapper tagMapper;
    private final LawDocumentTagMapper documentTagMapper;

    @Override
    public void attach(Long documentId, String tagType, String tagName) {
        if (documentId == null || !StringUtils.hasText(tagName) || !StringUtils.hasText(tagType)) {
            return;
        }
        Long tagId = resolveOrCreateTag(tagType.trim(), tagName.trim());
        linkIfAbsent(documentId, tagId);
    }

    /** 按 (type, name) 取或建标签。tag_code 由 type+name 派生，保证跨次运行稳定、不重复。 */
    private Long resolveOrCreateTag(String type, String name) {
        String code = tagCode(type, name);
        TagDO existing = tagMapper.selectOne(new LambdaQueryWrapper<TagDO>()
                .eq(TagDO::getTagCode, code)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing.getId();
        }
        TagDO tag = new TagDO();
        tag.setTagCode(code);
        tag.setTagName(name);
        tag.setTagType(type);
        tag.setStatus("enabled");
        try {
            tagMapper.insert(tag);
            return tag.getId();
        } catch (DuplicateKeyException e) {
            TagDO raced = tagMapper.selectOne(new LambdaQueryWrapper<TagDO>()
                    .eq(TagDO::getTagCode, code)
                    .last("LIMIT 1"));
            if (raced != null) {
                return raced.getId();
            }
            throw e;
        }
    }

    /** (documentId, tagId) 关联去重后插入。 */
    private void linkIfAbsent(Long documentId, Long tagId) {
        Long exists = documentTagMapper.selectCount(new LambdaQueryWrapper<LawDocumentTagDO>()
                .eq(LawDocumentTagDO::getDocumentId, documentId)
                .eq(LawDocumentTagDO::getTagId, tagId));
        if (exists != null && exists > 0) {
            return;
        }
        LawDocumentTagDO link = new LawDocumentTagDO();
        link.setDocumentId(documentId);
        link.setTagId(tagId);
        try {
            documentTagMapper.insert(link);
        } catch (DuplicateKeyException e) {
            log.debug("[TagAttach] duplicate link doc={} tag={}, ignored", documentId, tagId);
        }
    }

    /** tag_code = {type}:{name 的短哈希}，稳定且满足唯一键 uk_tag_code(tenant_id, tag_code)。 */
    private static String tagCode(String type, String name) {
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
