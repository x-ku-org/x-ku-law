SET NAMES utf8mb4;

-- ============================================================================
-- 结构化先行 / AI 旁路：AI（摘要+解读）从结构化主管线解耦为独立队列。
-- 结构化回填（提取/分段/发布/向量/对比）完全不触发 LLM；当 app.process.ai.enabled=true 时，
-- AiTaskEnqueueStage 为每个发布版本入队一条 lr_law_ai_task，由 LawAiTaskProcessor 消费，
-- 跑 AI 阶段（元数据富集 → 解读）并把解读结果落到 lr_law_interpretation。
-- ============================================================================

-- AI 处理任务队列：结构镜像 lr_law_process_task（CAS 领取 / 重试 / 超时恢复）。
-- 平台运营侧表，tenant_id 恒为 0，受 TenantLineHandlerImpl 白名单放行。
CREATE TABLE `lr_law_ai_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI处理任务ID',
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
  -- 同一版本只入队一条在途 AI 任务：入队前 countActiveByVersion 去重，唯一键兜底防并发重复入队。
  UNIQUE KEY uk_ai_task_version (`version_id`,`deleted`),
  KEY idx_ai_task_status (`process_status`,`retry_count`),
  KEY idx_ai_task_tenant_deleted (`tenant_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='法规AI处理任务表（摘要/解读，旁路）';

-- 法规解读结果：整篇文档级解读，每个已发布版本一条。
-- 公共法规数据链路，tenant_id=0，全租户可读；写入由 InterpretationStage 幂等（按 version 先删后插）。
CREATE TABLE `lr_law_interpretation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '解读ID',
  `document_id` bigint DEFAULT NULL COMMENT '法规文档ID',
  `version_id` bigint NOT NULL COMMENT '法规版本ID',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成所用AI模型标识',
  `interpretation_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '整篇解读正文',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'done' COMMENT '状态：done/failed',
  `token_count` int DEFAULT NULL COMMENT '约略token数（可空）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号；0表示平台公共数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY uk_interpretation_version (`version_id`,`deleted`),
  KEY idx_interpretation_document (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='法规解读结果表（整篇文档级）';
