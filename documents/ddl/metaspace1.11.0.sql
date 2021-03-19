ALTER TABLE sync_task_definition ADD COLUMN IF NOT EXISTS category_guid text;
COMMENT ON COLUMN sync_task_definition.category_guid IS '技术目录guid';