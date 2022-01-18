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

-- ----------------------------
-- 老数据同步到新加的字段中
-- ----------------------------
UPDATE source_info_derive_column_info C
SET source_table_guid = T.source_table_guid
FROM
    source_info_derive_table_info T,
    source_info_derive_table_column_relation tc
WHERE
    C.column_guid = tc.column_guid
    AND T.ID = tc.table_id
	
UPDATE source_info_derive_column_info 
SET custom = FALSE 
WHERE
	custom IS NULL

UPDATE source_info_derive_table_info SET operator = creator;