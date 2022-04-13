alter table "public"."column_info" add column sort int2;
comment on column "public"."column_info"."sort" is '字段排序';

-- 处理历史数据
UPDATE "public"."column_info" t1
SET sort = t2.num
FROM( SELECT "column_guid", ROW_NUMBER ( ) OVER ( PARTITION BY "table_guid" ORDER BY "column_name" ASC ) AS num FROM "public"."column_info" WHERE status = 'ACTIVE' ) t2
WHERE t1.column_guid = t2.column_guid AND sort is null;

-- 创建data_quality_task_rule_execute表，创建时间字段索引（用于数据管理的告警列表的筛选）
CREATE INDEX data_quality_task_rule_execute_create_time_idx ON public.data_quality_task_rule_execute (create_time);

-- 指标图书馆

ALTER TABLE public.indicator_threshold_log ADD actual_value varchar(32) NULL;
COMMENT ON COLUMN public.indicator_threshold_log.actual_value IS '实际值';

ALTER TABLE public.indicator_threshold_log ADD threshold_setting_id int8 NULL;
COMMENT ON COLUMN public.indicator_threshold_log.threshold_setting_id IS '阈值设置id';

ALTER TABLE "public"."business_indicators" add "operations_people_ids" text[] COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."business_indicators"."operations_people_ids" IS '运维负责人ID列表';



