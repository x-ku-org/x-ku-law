SET NAMES utf8mb4;

-- 修复并发处理时的 Lock wait timeout：
-- SegmentationStage 重跑前用 `DELETE ... WHERE version_id = ?` 清场（lr_law_article / lr_law_article_segment）。
-- 这两张表原先无可用于 version_id 单列过滤的索引（segment 无 version_id 索引；article 的
-- idx_article_doc 以 document_id 打头，version_id 非最左列），导致 DELETE 全表扫描并对扫描到的行加锁。
-- 当处理管线并发处理多个版本（app.process.concurrency>1）时，这些全表扫描 DELETE 互相阻塞，
-- 触发 MySQLTransactionRollbackException: Lock wait timeout exceeded。
-- 加 version_id 索引后，DELETE 只锁该版本自身的行，不同版本互不阻塞。
CREATE INDEX idx_article_version ON lr_law_article (version_id);
CREATE INDEX idx_segment_version ON lr_law_article_segment (version_id);
