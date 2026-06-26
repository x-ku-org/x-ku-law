-- 补充法规二级与字典写操作权限码，与平台管理员角色绑定
-- V2 已有 1-20，本脚本从 21 开始，勿修改 V2

SET NAMES utf8mb4;

-- ===== 新增权限码 =====
INSERT INTO `lr_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `sort_order`, `status`, `tenant_id`, `deleted`) VALUES
-- 法规条款
(21, 0, 'law:article:create', '新建法规条款', 'api', 70, 'enabled', 0, b'0'),
(22, 0, 'law:article:update', '更新法规条款', 'api', 71, 'enabled', 0, b'0'),
(23, 0, 'law:article:delete', '删除法规条款', 'api', 72, 'enabled', 0, b'0'),
-- 法规分类
(24, 0, 'law:category:create', '新建法规分类', 'api', 73, 'enabled', 0, b'0'),
(25, 0, 'law:category:update', '更新法规分类', 'api', 74, 'enabled', 0, b'0'),
(26, 0, 'law:category:delete', '删除法规分类', 'api', 75, 'enabled', 0, b'0'),
-- 法规关系
(27, 0, 'law:relation:create', '新建法规关系', 'api', 76, 'enabled', 0, b'0'),
(28, 0, 'law:relation:update', '更新法规关系', 'api', 77, 'enabled', 0, b'0'),
(29, 0, 'law:relation:delete', '删除法规关系', 'api', 78, 'enabled', 0, b'0'),
-- 字典管理
(30, 0, 'system:dict:create', '新建字典', 'api', 79, 'enabled', 0, b'0'),
(31, 0, 'system:dict:update', '更新字典', 'api', 80, 'enabled', 0, b'0'),
(32, 0, 'system:dict:delete', '删除字典', 'api', 81, 'enabled', 0, b'0');

-- ===== 平台管理员绑定全部新权限 =====
INSERT INTO `lr_role_permission` (`role_id`, `permission_id`, `tenant_id`, `deleted`) VALUES
(1, 21, 0, b'0'),
(1, 22, 0, b'0'),
(1, 23, 0, b'0'),
(1, 24, 0, b'0'),
(1, 25, 0, b'0'),
(1, 26, 0, b'0'),
(1, 27, 0, b'0'),
(1, 28, 0, b'0'),
(1, 29, 0, b'0'),
(1, 30, 0, b'0'),
(1, 31, 0, b'0'),
(1, 32, 0, b'0');
