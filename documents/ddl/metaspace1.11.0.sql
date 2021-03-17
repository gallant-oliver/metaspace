ALTER TABLE sync_task_definition ADD COLUMN IF NOT EXISTS category_guid text;
COMMENT ON COLUMN sync_task_definition.category_guid IS '技术目录guid';
ALTER TABLE tableinfo ADD COLUMN IF NOT EXISTS task_id text;
COMMENT ON COLUMN tableinfo.task_id IS '任务id';