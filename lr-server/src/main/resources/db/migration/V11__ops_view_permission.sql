SET NAMES utf8mb4;

-- 运维监控查看权限：平台管理员可查看采集/处理管线/索引/向量等调度任务状态并重试失败项。
INSERT INTO `lr_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `sort_order`, `status`, `tenant_id`, `deleted`) VALUES
(0, 'system:ops:view', '运维任务监控', 'api', 92, 'enabled', 0, b'0');

INSERT INTO `lr_role_permission` (`role_id`, `permission_id`, `tenant_id`, `deleted`)
SELECT 1, `id`, 0, b'0'
FROM `lr_permission`
WHERE `permission_code` = 'system:ops:view';
