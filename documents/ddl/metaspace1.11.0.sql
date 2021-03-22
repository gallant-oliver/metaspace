ALTER TABLE sync_task_definition ADD COLUMN IF NOT EXISTS category_guid text;
COMMENT ON COLUMN sync_task_definition.category_guid IS '技术目录guid';
COMMENT ON COLUMN "public"."data_quality_task_execute"."execute_status" IS '执行状态:1-执行中,2-成功,3-失败,0-未执行,4-取消';
COMMENT ON COLUMN "public"."data_quality_task"."current_execution_status" IS '执行状态:1-执行中,2-成功,3-失败,0-待执行,4-取消';