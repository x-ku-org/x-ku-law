SET NAMES utf8mb4;

-- AI 引用时效标注：current（现行有效）/ superseded（历史版本）/ repealed（已废止/失效）。
-- 用于在证据账册标注废止/失效条款，并参与答案风险分级（命中失效→warning）。
ALTER TABLE `lr_ai_citation`
    ADD COLUMN `validity_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
        NOT NULL DEFAULT 'current' COMMENT '时效：current/superseded/repealed' AFTER `article_label`;
