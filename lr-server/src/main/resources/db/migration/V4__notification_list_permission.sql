SET NAMES utf8mb4;

INSERT INTO `lr_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `sort_order`, `status`, `tenant_id`, `deleted`) VALUES
(0, 'notification:list', '通知列表', 'api', 82, 'enabled', 0, b'0');

INSERT INTO `lr_role_permission` (`role_id`, `permission_id`, `tenant_id`, `deleted`)
SELECT 1, `id`, 0, b'0'
FROM `lr_permission`
WHERE `permission_code` = 'notification:list';
