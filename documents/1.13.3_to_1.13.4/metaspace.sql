alter table source_info_derive_column_info add column tags text;
comment on column "source_info_derive_column_info"."tags" is '关联标签数组';

alter table source_info_derive_column_info add column desensitization_rules varchar(255);
comment on column "source_info_derive_column_info"."desensitization_rules" is '目标字段脱敏规则';

alter table source_info_derive_column_info add column source_table_guid varchar(255);
comment on column "source_info_derive_column_info"."source_table_guid" is '源表的guid';

alter table source_info_derive_column_info add column custom bool;
comment on column "source_info_derive_column_info"."custom" is '是否手动添加';

alter table source_info_derive_table_info add column operator varchar(100);
comment on column "source_info_derive_table_info"."operator" is '操作人';

alter table source_info_derive_table_info add column file_name varchar(100);
comment on column "source_info_derive_table_info"."file_name" is '上次文件名称';

alter table source_info_derive_table_info add column file_path varchar(255);
comment on column "source_info_derive_table_info"."file_path" is '上次文件路径';

alter table source_info_derive_table_info add column incremental_field varchar(255);
comment on column "source_info_derive_table_info"."incremental_field" is '增量字段';

ALTER TABLE data_quality_task_rule_execute ALTER COLUMN error_msg TYPE TEXT;

alter table source_info_derive_table_info alter source_table_guid drop not null;

-- ----------------------------
-- 老数据同步到新加的字段中
-- ----------------------------
UPDATE source_info_derive_column_info c1
SET source_table_guid = t1.source_table_guid
    FROM
	source_info_derive_table_info t1,
	source_info_derive_table_column_relation tc
WHERE
    tc.table_id = t1.id
  AND tc.table_guid = t1.table_guid
  AND tc.column_guid = c1.column_guid
  AND t1.version = - 1;

UPDATE source_info_derive_column_info
SET custom = FALSE
WHERE
	custom IS NULL

UPDATE source_info_derive_table_info SET operator = creator;

-- 指标图书馆

ALTER TABLE public.indicator_threshold_log ADD actual_value varchar(32) NULL;
COMMENT ON COLUMN public.indicator_threshold_log.actual_value IS '实际值';

ALTER TABLE public.indicator_threshold_log ADD threshold_setting_id int8 NULL;
COMMENT ON COLUMN public.indicator_threshold_log.threshold_setting_id IS '阈值设置id';

alter table public.indicator_threshold_setting add indicator_version_id int8 NULL
COMMENT ON COLUMN public.indicator_threshold_setting.indicator_version_id IS '指标版本id';

alter table public.indicator_threshold_setting add is_current_version bool NULL
COMMENT ON COLUMN public.indicator_threshold_setting.is_current_version IS '是否当前版本';

ALTER TABLE "public"."business_indicators" add "operations_people_ids" text[] COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."business_indicators"."operations_people_ids" IS '运维负责人ID列表';
