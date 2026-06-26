SET NAMES utf8mb4;

-- AI 对话引用依据补充展示列：
-- AI Agent 问答落库引用时，把来源法规标题与条款标签冗余落到引用行，
-- 使历史会话回读 (lr-module-ai pageMessages) 能直接展示 source/article，
-- 无需跨模块 join lr_law_document / lr_law_article（lr-module-ai 不依赖 law 模块）。
ALTER TABLE lr_ai_citation
    ADD COLUMN source_title  VARCHAR(512) NULL COMMENT '来源法规标题（落库时冗余，便于展示）',
    ADD COLUMN article_label VARCHAR(128) NULL COMMENT '条款标签，如 第十二条（落库时冗余，便于展示）';
