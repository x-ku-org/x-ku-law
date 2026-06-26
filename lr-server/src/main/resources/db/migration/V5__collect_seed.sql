SET NAMES utf8mb4;

-- 采集来源：法律法规数据库(FLK) 与 国家标准全文公开系统(OpenSTD)。
-- 抓取由外部 Python 流程完成并上传至对象存储，Java 侧定时扫描接入。
INSERT INTO `lr_content_source`
  (`source_name`, `source_type`, `source_url`, `access_method`, `sync_enabled`, `status`, `remark`, `tenant_id`, `deleted`)
VALUES
  ('国家法律法规数据库(FLK)', 'official', 'https://flk.npc.gov.cn', 'crawler', b'1', 'enabled', 'Python(Playwright) 抓取后上传对象存储 fglaw<YYYYMMDD>/，Java 定时接入', 0, b'0'),
  ('国家标准全文公开系统(OpenSTD)', 'official', 'https://openstd.samr.gov.cn', 'crawler', b'1', 'enabled', 'Python(openstd_spider) 抓取后上传对象存储 gblaw<YYYYMMDD>/，Java 定时接入', 0, b'0');

-- 采集任务：parser_code 为运行期解析/接入选择键（flk / gb），CRON 仅作展示，实际由外部按月运行 + Java 固定间隔扫描。
INSERT INTO `lr_collect_task`
  (`source_id`, `task_name`, `collect_type`, `target_url`, `cron_expr`, `parser_code`, `status`, `tenant_id`, `deleted`)
SELECT `id`, '法律法规月度接入', 'file', 'https://flk.npc.gov.cn/search', '0 0 3 1 * ?', 'flk', 'enabled', 0, b'0'
FROM `lr_content_source` WHERE `source_name` = '国家法律法规数据库(FLK)';

INSERT INTO `lr_collect_task`
  (`source_id`, `task_name`, `collect_type`, `target_url`, `cron_expr`, `parser_code`, `status`, `tenant_id`, `deleted`)
SELECT `id`, '国家标准月度接入', 'file', 'https://openstd.samr.gov.cn', '0 0 3 1 * ?', 'gb', 'enabled', 0, b'0'
FROM `lr_content_source` WHERE `source_name` = '国家标准全文公开系统(OpenSTD)';
