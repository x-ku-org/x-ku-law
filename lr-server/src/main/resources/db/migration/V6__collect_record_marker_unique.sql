SET NAMES utf8mb4;

-- 采集接入用 lr_collect_record 作为“运行文件夹处理标记”（content_hash 存文件夹名，如 gblaw20260501/）。
-- 多实例下两个调度器可能同时发现无标记、各插一行 pending 并各自 CAS 成功，导致同一文件夹被重复接入。
-- 加 (task_id, content_hash) 唯一约束：同一任务下同一文件夹只允许一行标记，并发插入第二条会触发
-- DuplicateKey，应用据此回查同一行，再由 claimRecord 的 CAS 选出唯一处理者。
-- 说明：唯一索引含 NULL 列时该行不受约束，故普通采集记录（content_hash 为 NULL）不受影响。
ALTER TABLE `lr_collect_record`
  ADD UNIQUE KEY `uk_collect_record_marker` (`task_id`, `content_hash`);
