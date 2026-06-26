-- RBAC 初始化种子数据
-- 版本：V2
-- 说明：预置平台管理员角色及全量权限码，用于开发/测试环境。
--       admin 账号密码：Admin@123（BCrypt rounds=12）。

SET NAMES utf8mb4;

-- ===== 平台租户（登录时 tenantCode='platform'） =====
INSERT INTO `lr_tenant` (`id`, `tenant_code`, `tenant_name`, `tenant_type`, `status`, `max_users`, `tenant_id`, `deleted`) VALUES
(1, 'platform', '平台', 'platform', 'enabled', 0, 0, b'0');

-- ===== 权限码：系统管理 =====
INSERT INTO `lr_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `sort_order`, `status`, `tenant_id`, `deleted`) VALUES
-- 用户管理
(1,  0, 'system:user:list',   '用户列表', 'api', 10, 'enabled', 0, b'0'),
(2,  0, 'system:user:create', '新建用户', 'api', 11, 'enabled', 0, b'0'),
(3,  0, 'system:user:update', '更新用户', 'api', 12, 'enabled', 0, b'0'),
(4,  0, 'system:user:delete', '删除用户', 'api', 13, 'enabled', 0, b'0'),
-- 角色管理
(5,  0, 'system:role:list',   '角色列表', 'api', 20, 'enabled', 0, b'0'),
(6,  0, 'system:role:create', '新建角色', 'api', 21, 'enabled', 0, b'0'),
(7,  0, 'system:role:update', '更新角色', 'api', 22, 'enabled', 0, b'0'),
(8,  0, 'system:role:delete', '删除角色', 'api', 23, 'enabled', 0, b'0'),
-- 权限管理
(9,  0, 'system:permission:list',   '权限列表', 'api', 30, 'enabled', 0, b'0'),
(10, 0, 'system:permission:create', '新建权限', 'api', 31, 'enabled', 0, b'0'),
(11, 0, 'system:permission:update', '更新权限', 'api', 32, 'enabled', 0, b'0'),
(12, 0, 'system:permission:delete', '删除权限', 'api', 33, 'enabled', 0, b'0'),
-- 通知
(13, 0, 'notification:send', '发送通知', 'api', 40, 'enabled', 0, b'0'),
-- 法规管理
(14, 0, 'law:document:create', '新建法规', 'api', 50, 'enabled', 0, b'0'),
(15, 0, 'law:document:update', '更新法规', 'api', 51, 'enabled', 0, b'0'),
(16, 0, 'law:document:delete', '删除法规', 'api', 52, 'enabled', 0, b'0'),
-- 法规版本
(17, 0, 'law:version:create',  '新建版本',  'api', 60, 'enabled', 0, b'0'),
(18, 0, 'law:version:update',  '更新版本',  'api', 61, 'enabled', 0, b'0'),
(19, 0, 'law:version:delete',  '删除版本',  'api', 62, 'enabled', 0, b'0'),
(20, 0, 'law:version:publish', '发布版本',  'api', 63, 'enabled', 0, b'0');

-- ===== 平台管理员角色（系统全局角色，tenant_id=0） =====
INSERT INTO `lr_role` (`id`, `role_code`, `role_name`, `role_type`, `data_scope`, `status`, `sort_order`, `tenant_id`, `deleted`) VALUES
(1, 'platform_admin', '平台管理员', 'system', 'all', 'enabled', 1, 0, b'0');

-- ===== 平台管理员拥有全部权限 =====
INSERT INTO `lr_role_permission` (`role_id`, `permission_id`, `tenant_id`, `deleted`) VALUES
(1,  1, 0, b'0'),
(1,  2, 0, b'0'),
(1,  3, 0, b'0'),
(1,  4, 0, b'0'),
(1,  5, 0, b'0'),
(1,  6, 0, b'0'),
(1,  7, 0, b'0'),
(1,  8, 0, b'0'),
(1,  9, 0, b'0'),
(1, 10, 0, b'0'),
(1, 11, 0, b'0'),
(1, 12, 0, b'0'),
(1, 13, 0, b'0'),
(1, 14, 0, b'0'),
(1, 15, 0, b'0'),
(1, 16, 0, b'0'),
(1, 17, 0, b'0'),
(1, 18, 0, b'0'),
(1, 19, 0, b'0'),
(1, 20, 0, b'0');

-- ===== 测试账号 admin (密码: Admin@123，BCrypt rounds=12) =====
-- 注意：tenant_id=1 匹配上方平台租户的 id=1（登录时 tenantCode='platform'）
INSERT INTO `lr_user` (`id`, `username`, `password_hash`, `real_name`, `user_type`, `status`, `tenant_id`, `deleted`) VALUES
(1, 'admin', '$2a$12$hGpnGiGsZEpvGPAB12ZwtuWPT1GBiHMhNq6F7VVh0O2IkLLOmjGli', '系统管理员', 'admin', 'enabled', 1, b'0');

-- ===== admin → platform_admin 角色绑定（tenant_id=1 同用户租户） =====
INSERT INTO `lr_user_role` (`user_id`, `role_id`, `tenant_id`, `deleted`) VALUES
(1, 1, 1, b'0');
