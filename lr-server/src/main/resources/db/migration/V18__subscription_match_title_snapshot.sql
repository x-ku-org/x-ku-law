SET NAMES utf8mb4;

-- 预警中心改版：命中记录直接展示「命中的法规标题」。
-- 命中标题在生成命中时已知（DocumentMatchContext.title），但此前未持久化，
-- 列表只能回退成「订阅命中 #id」。订阅模块仅依赖 lr-common、不引用法规模块，
-- 因此按 Favorite.title_snapshot 的既有快照做法，落一份标题快照，避免跨模块联表。
ALTER TABLE `lr_subscription_match`
  ADD COLUMN `title_snapshot` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '命中法规标题快照' AFTER `article_id`;
