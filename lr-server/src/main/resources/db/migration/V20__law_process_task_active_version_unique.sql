SET NAMES utf8mb4;

-- 同一 version 只允许存在一条「在途」处理任务，从 DB 层根治并发重复入队。
-- 背景：LawProcessTaskServiceImpl.enqueue 原先靠 countActiveByVersion>0 这一非原子检查去重，
-- 采集批量与管理员上传两条入口并发入队时可能都读到 0，从而为同一 version 入队两条 pending 任务，
-- 被两个线程分别 CAS 领取后同时「删旧条款 → 重插条款」，既触发死锁，又会撞 uk_article_no 唯一键。
-- 用生成列 active_version_id：仅当任务在途（未删除 + pending/processing）时取 version_id，否则为 NULL；
-- 对其建唯一键。NULL 不受唯一约束，故 done/failed/已删除的历史任务不占键、互不影响。

-- 存量清理：旧逻辑（非原子 count 去重）可能已为同一 version 落下多条在途任务。
-- 若不先清理，下面的 ADD UNIQUE KEY 会因重复值报 1062 而导致本次迁移整体失败、服务无法启动。
-- 同一 version 的在途任务只保留 id 最小的一条，其余软删除（不占唯一键）。
UPDATE `lr_law_process_task` t
JOIN (
  SELECT `version_id`, MIN(`id`) AS keep_id
  FROM `lr_law_process_task`
  WHERE `deleted` = b'0' AND `process_status` IN ('pending', 'processing')
  GROUP BY `version_id`
  HAVING COUNT(*) > 1
) keep ON t.`version_id` = keep.`version_id`
SET t.`deleted` = b'1'
WHERE t.`deleted` = b'0'
  AND t.`process_status` IN ('pending', 'processing')
  AND t.`id` <> keep.keep_id;

ALTER TABLE `lr_law_process_task`
  ADD COLUMN `active_version_id` bigint
    GENERATED ALWAYS AS (
      CASE WHEN `deleted` = b'0' AND `process_status` IN ('pending', 'processing')
           THEN `version_id` ELSE NULL END
    ) VIRTUAL COMMENT '在途任务的 version_id（pending/processing 且未删除时等于 version_id，否则 NULL），用于唯一去重',
  ADD UNIQUE KEY `uk_active_version` (`active_version_id`);
