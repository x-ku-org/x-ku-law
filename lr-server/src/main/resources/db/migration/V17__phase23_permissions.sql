SET NAMES utf8mb4;

-- 第 2/3 阶段补全新增的管理端权限，授予平台管理员（role_id=1）：
--   system:ops:audit   — 运维后台查看数据质量问题 / 审核留痕、标记问题已解决
--   ai:feedback:manage — 查看与处理 AI 回答反馈（点赞/纠错/转人工）
-- 说明：普通用户提交反馈 POST /ai/feedback 仅需登录鉴权，不需要单独权限。
INSERT INTO `lr_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `sort_order`, `status`, `tenant_id`, `deleted`) VALUES
(0, 'system:ops:audit', '数据治理与审核留痕', 'api', 93, 'enabled', 0, b'0'),
(0, 'ai:feedback:manage', 'AI 反馈处理', 'api', 94, 'enabled', 0, b'0');

INSERT INTO `lr_role_permission` (`role_id`, `permission_id`, `tenant_id`, `deleted`)
SELECT 1, `id`, 0, b'0'
FROM `lr_permission`
WHERE `permission_code` IN ('system:ops:audit', 'ai:feedback:manage');
