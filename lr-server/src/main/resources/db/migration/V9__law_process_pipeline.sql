SET NAMES utf8mb4;

-- 法规处理管线任务表：两条接入入口（采集批量 / 管理员上传）建好草稿版本后入队一条 pending 任务，
-- 由 LawProcessTaskProcessor 异步消费，顺序跑「文本提取→分段→发布→解读→变更分析」。
-- 平台运营侧表，tenant_id 恒为 0，受 TenantLineHandlerImpl 白名单放行。
CREATE TABLE `lr_law_process_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '处理任务ID',
  `document_id` bigint DEFAULT NULL COMMENT '法规文档ID',
  `version_id` bigint NOT NULL COMMENT '法规版本ID',
  `file_id` bigint DEFAULT NULL COMMENT '正文文件ID，可空（仅元数据接入）',
  `process_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '处理状态：pending/processing/done/failed',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `started_at` datetime DEFAULT NULL COMMENT '开始处理时间',
  `finished_at` datetime DEFAULT NULL COMMENT '结束时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号；0表示平台公共数据',
  PRIMARY KEY (`id`) USING BTREE,
  KEY idx_process_status (`process_status`,`retry_count`),
  KEY idx_process_version (`version_id`),
  KEY idx_tenant_deleted (`tenant_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='法规处理管线任务表';

-- 管理员上传接入权限
INSERT INTO `lr_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `sort_order`, `status`, `tenant_id`, `deleted`) VALUES
(0, 'law:ingest:upload', '上传法规文件接入', 'api', 91, 'enabled', 0, b'0');

INSERT INTO `lr_role_permission` (`role_id`, `permission_id`, `tenant_id`, `deleted`)
SELECT 1, `id`, 0, b'0'
FROM `lr_permission`
WHERE `permission_code` = 'law:ingest:upload';
