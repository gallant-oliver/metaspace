-------------------时间限定----------------------------------
CREATE TABLE "public"."time_limit" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "grade" char(1) COLLATE "pg_catalog"."default",
  "start_time" timestamptz(6),
  "end_time" timestamptz(6),
  "creator" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "state" varchar COLLATE "pg_catalog"."default",
  "version" int4 NOT NULL,
  "delete" bool,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "tenantid" varchar COLLATE "pg_catalog"."default",
  "publisher" varchar COLLATE "pg_catalog"."default",
  "approveid" varchar COLLATE "pg_catalog"."default",
  "mark" varchar COLLATE "pg_catalog"."default",
  "time_type" varchar COLLATE "pg_catalog"."default",
  "time_range" int4,
  CONSTRAINT "timelimit_pkey" PRIMARY KEY ("id", "version")
)
;
ALTER TABLE "public"."time_limit"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."time_limit"."id" IS '表主键';
COMMENT ON COLUMN "public"."time_limit"."name" IS '时间限定名称';
COMMENT ON COLUMN "public"."time_limit"."description" IS '描述';
COMMENT ON COLUMN "public"."time_limit"."start_time" IS '启始时间';
COMMENT ON COLUMN "public"."time_limit"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."time_limit"."creator" IS '创建人：用户外健';
COMMENT ON COLUMN "public"."time_limit"."publisher" IS '发布人：用户外健';
COMMENT ON COLUMN "public"."time_limit"."updater" IS '更新人';
COMMENT ON COLUMN "public"."time_limit"."state" IS '状态';
COMMENT ON COLUMN "public"."time_limit"."version" IS '版本号';
COMMENT ON COLUMN "public"."time_limit"."delete" IS '删除标记';
COMMENT ON COLUMN "public"."time_limit"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."Untitled"."update_time" IS '更新时间';

-------------------修饰词----------------------------------
CREATE TABLE "public"."qualifier" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "mark" varchar COLLATE "pg_catalog"."default",
  "creator" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_user" varchar COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "desc" text COLLATE "pg_catalog"."default",
  "tenantid" varchar COLLATE "pg_catalog"."default",
  "typeid" varchar COLLATE "pg_catalog"."default",
  CONSTRAINT "qualifier_pkey" PRIMARY KEY ("id")
)
;
ALTER TABLE "public"."qualifier"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."qualifier"."id" IS '修饰词ID';
COMMENT ON COLUMN "public"."qualifier"."name" IS '修饰词名称';
COMMENT ON COLUMN "public"."qualifier"."mark" IS '修饰词标识';
COMMENT ON COLUMN "public"."qualifier"."creator" IS '创建人';
COMMENT ON COLUMN "public"."qualifier"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."qualifier"."update_user" IS '更新人';
COMMENT ON COLUMN "public"."qualifier"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."qualifier"."desc" IS '修饰词描述 ';`
COMMENT ON COLUMN "public"."qualifier"."tenantid" IS '租户ID ';`
COMMENT ON COLUMN "public"."qualifier"."typeid" IS '类型ID ';

-------------------修饰词类型----------------------------------
CREATE TABLE "public"."qualifier_type" (
  "type_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "type_name" varchar COLLATE "pg_catalog"."default",
  "type_mark" varchar COLLATE "pg_catalog"."default",
  "creator" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_user" varchar COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "type_desc" text COLLATE "pg_catalog"."default",
  "tenantid" varchar COLLATE "pg_catalog"."default",
  CONSTRAINT "qualifier_type_pkey" PRIMARY KEY ("type_id")
)
;
ALTER TABLE "public"."qualifier_type"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."qualifier_type"."type_id" IS '修饰词类型ID';
COMMENT ON COLUMN "public"."qualifier_type"."type_name" IS '修饰词类型名称';
COMMENT ON COLUMN "public"."qualifier_type"."type_mark" IS '修饰词类型标识';
COMMENT ON COLUMN "public"."qualifier_type"."creator" IS '创建人';
COMMENT ON COLUMN "public"."qualifier_type"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."qualifier_type"."update_user" IS '更新人';
COMMENT ON COLUMN "public"."qualifier_type"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."qualifier_type"."type_desc" IS '修饰词类型目录描述 ';
COMMENT ON COLUMN "public"."qualifier_type"."tenantid" IS '租户ID ';

-------------------原子指标----------------------------------
CREATE TABLE "public"."index_atomic_info" (
  "index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_identification" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(600) COLLATE "pg_catalog"."default",
  "central" bool NOT NULL DEFAULT false,
  "index_field_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "approval_group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_state" int2 NOT NULL,
  "version" int2,
  "source_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "db_name" varchar COLLATE "pg_catalog"."default",
  "table_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "column_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "business_caliber" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "business_leader" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "technical_caliber" varchar(255) COLLATE "pg_catalog"."default",
  "technical_leader" varchar(255) COLLATE "pg_catalog"."default",
  "creator" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6) NOT NULL,
  "updater" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "publisher" varchar(255) COLLATE "pg_catalog"."default",
  "publish_time" timestamptz(6)
)
;

ALTER TABLE "public"."index_atomic_info"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."index_atomic_info"."index_id" IS '指标id';

COMMENT ON COLUMN "public"."index_atomic_info"."index_name" IS '指标名称';

COMMENT ON COLUMN "public"."index_atomic_info"."index_identification" IS '指标标识';

COMMENT ON COLUMN "public"."index_atomic_info"."description" IS '指标描述';

COMMENT ON COLUMN "public"."index_atomic_info"."central" IS '是否核心指标';

COMMENT ON COLUMN "public"."index_atomic_info"."index_field_id" IS '指标域id';

COMMENT ON COLUMN "public"."index_atomic_info"."tenant_id" IS '租户id';

COMMENT ON COLUMN "public"."index_atomic_info"."approval_group_id" IS '审批组id';

COMMENT ON COLUMN "public"."index_atomic_info"."index_state" IS '指标状态；1 新建(未发布过)，2 已发布，3 已下线，4 审核中';

COMMENT ON COLUMN "public"."index_atomic_info"."version" IS '版本号，每发布一次，记录一次历史版本';

COMMENT ON COLUMN "public"."index_atomic_info"."source_id" IS '数据源id';

COMMENT ON COLUMN "public"."index_atomic_info"."db_name" IS '数据库名';

COMMENT ON COLUMN "public"."index_atomic_info"."table_id" IS '表id';

COMMENT ON COLUMN "public"."index_atomic_info"."column_id" IS '字段id';

COMMENT ON COLUMN "public"."index_atomic_info"."business_caliber" IS '业务口径';

COMMENT ON COLUMN "public"."index_atomic_info"."business_leader" IS '业务负责人';

COMMENT ON COLUMN "public"."index_atomic_info"."technical_caliber" IS '技术口径';

COMMENT ON COLUMN "public"."index_atomic_info"."technical_leader" IS '技术负责人';

COMMENT ON COLUMN "public"."index_atomic_info"."creator" IS '创建人';

COMMENT ON COLUMN "public"."index_atomic_info"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."index_atomic_info"."updater" IS '更新人';

COMMENT ON COLUMN "public"."index_atomic_info"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."index_atomic_info"."publisher" IS '发布人';

COMMENT ON COLUMN "public"."index_atomic_info"."publish_time" IS '发布时间';

-------------------派生指标----------------------------------
CREATE TABLE "public"."index_derive_info" (
  "index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_atomic_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "time_limit_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_identification" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(600) COLLATE "pg_catalog"."default",
  "central" bool NOT NULL DEFAULT false,
  "index_field_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "approval_group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_state" int2 NOT NULL,
  "version" int2,
  "business_caliber" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "business_leader" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "technical_caliber" varchar(255) COLLATE "pg_catalog"."default",
  "technical_leader" varchar(255) COLLATE "pg_catalog"."default",
  "creator" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6) NOT NULL,
  "updater" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "publisher" varchar(255) COLLATE "pg_catalog"."default",
  "publish_time" timestamptz(6)
)
;

ALTER TABLE "public"."index_derive_info"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."index_derive_info"."index_id" IS '指标id';

COMMENT ON COLUMN "public"."index_derive_info"."index_atomic_id" IS '原子指标id';

COMMENT ON COLUMN "public"."index_derive_info"."time_limit_id" IS '时间限定id';

COMMENT ON COLUMN "public"."index_derive_info"."index_name" IS '指标名称';

COMMENT ON COLUMN "public"."index_derive_info"."index_identification" IS '指标标识';

COMMENT ON COLUMN "public"."index_derive_info"."description" IS '指标描述';

COMMENT ON COLUMN "public"."index_derive_info"."central" IS '是否核心指标';

COMMENT ON COLUMN "public"."index_derive_info"."index_field_id" IS '指标域id';

COMMENT ON COLUMN "public"."index_derive_info"."tenant_id" IS '租户id';

COMMENT ON COLUMN "public"."index_derive_info"."approval_group_id" IS '审批组id';

COMMENT ON COLUMN "public"."index_derive_info"."index_state" IS '指标状态；1 新建(未发布过)，2 已发布，3 已下线，4 审核中';

COMMENT ON COLUMN "public"."index_derive_info"."version" IS '版本号，每发布一次，记录一次历史版本';

COMMENT ON COLUMN "public"."index_derive_info"."business_caliber" IS '业务口径';

COMMENT ON COLUMN "public"."index_derive_info"."business_leader" IS '业务负责人';

COMMENT ON COLUMN "public"."index_derive_info"."technical_caliber" IS '技术口径';

COMMENT ON COLUMN "public"."index_derive_info"."technical_leader" IS '技术负责人';

COMMENT ON COLUMN "public"."index_derive_info"."creator" IS '创建人';

COMMENT ON COLUMN "public"."index_derive_info"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."index_derive_info"."updater" IS '更新人';

COMMENT ON COLUMN "public"."index_derive_info"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."index_derive_info"."publisher" IS '发布人';

COMMENT ON COLUMN "public"."index_derive_info"."publish_time" IS '发布时间';

-------------------复合指标----------------------------------
CREATE TABLE "public"."index_composite_info" (
  "index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_identification" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(600) COLLATE "pg_catalog"."default",
  "central" bool NOT NULL DEFAULT false,
  "index_field_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "approval_group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "index_state" int2 NOT NULL,
  "version" int2,
  "expression" text COLLATE "pg_catalog"."default",
  "business_caliber" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "business_leader" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "technical_caliber" varchar(255) COLLATE "pg_catalog"."default",
  "technical_leader" varchar(255) COLLATE "pg_catalog"."default",
  "creator" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6) NOT NULL,
  "updater" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "publisher" varchar(255) COLLATE "pg_catalog"."default",
  "publish_time" timestamptz(6)
)
;

ALTER TABLE "public"."index_composite_info"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."index_composite_info"."index_id" IS '指标id';

COMMENT ON COLUMN "public"."index_composite_info"."index_name" IS '指标名称';

COMMENT ON COLUMN "public"."index_composite_info"."index_identification" IS '指标标识';

COMMENT ON COLUMN "public"."index_composite_info"."description" IS '指标描述';

COMMENT ON COLUMN "public"."index_composite_info"."central" IS '是否核心指标';

COMMENT ON COLUMN "public"."index_composite_info"."index_field_id" IS '指标域id';

COMMENT ON COLUMN "public"."index_composite_info"."tenant_id" IS '租户id';

COMMENT ON COLUMN "public"."index_composite_info"."approval_group_id" IS '审批组id';

COMMENT ON COLUMN "public"."index_composite_info"."index_state" IS '指标状态；1 新建(未发布过)，2 已发布，3 已下线，4 审核中';

COMMENT ON COLUMN "public"."index_composite_info"."version" IS '版本号，每发布一次，记录一次历史版本';

COMMENT ON COLUMN "public"."index_composite_info"."expression" IS '表达式';

COMMENT ON COLUMN "public"."index_composite_info"."business_caliber" IS '业务口径';

COMMENT ON COLUMN "public"."index_composite_info"."business_leader" IS '业务负责人';

COMMENT ON COLUMN "public"."index_composite_info"."technical_caliber" IS '技术口径';

COMMENT ON COLUMN "public"."index_composite_info"."technical_leader" IS '技术负责人';

COMMENT ON COLUMN "public"."index_composite_info"."creator" IS '创建人';

COMMENT ON COLUMN "public"."index_composite_info"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."index_composite_info"."updater" IS '更新人';

COMMENT ON COLUMN "public"."index_composite_info"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."index_composite_info"."publisher" IS '发布人';

COMMENT ON COLUMN "public"."index_composite_info"."publish_time" IS '发布时间';

-------------------派生指标与复合指标关系表----------------------------------
CREATE TABLE "public"."index_derive_composite_relation" (
  "derive_index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "composite_index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."index_derive_composite_relation"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."index_derive_composite_relation"."derive_index_id" IS '派生指标id';

COMMENT ON COLUMN "public"."index_derive_composite_relation"."composite_index_id" IS '复合指标id';

-------------------派生指标与修饰词关系表----------------------------------
CREATE TABLE "public"."index_derive_modifier_relation" (
  "derive_index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "modifier_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."index_derive_modifier_relation"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."index_derive_modifier_relation"."derive_index_id" IS '派生指标id';

COMMENT ON COLUMN "public"."index_derive_modifier_relation"."modifier_id" IS '修饰词id';

-------------------审核组----------------------------------
CREATE TABLE "public"."approval_group" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "creator" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "updater" varchar COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "tenantid" varchar COLLATE "pg_catalog"."default",
  "valid" bool,
  CONSTRAINT "approval_group_pkey" PRIMARY KEY ("id")
)
;
ALTER TABLE "public"."approval_group"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."approval_group"."id" IS '审批组ID';

COMMENT ON COLUMN "public"."approval_group"."name" IS '审批组名称';

COMMENT ON COLUMN "public"."approval_group"."description" IS '描述 ';

COMMENT ON COLUMN "public"."approval_group"."creator" IS '创建人';

COMMENT ON COLUMN "public"."approval_group"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."approval_group"."updater" IS '更新人';

COMMENT ON COLUMN "public"."approval_group"."update_time" IS '更新时间 ';


-------------------审批组模块关系表----------------------------------
CREATE TABLE "public"."approval_group_module_relation" (
  "group_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "module_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  CONSTRAINT "approval_group_module_relation_pk" PRIMARY KEY ("group_id", "module_id")
)
;
ALTER TABLE "public"."approval_group_module_relation"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."approval_group_module_relation"."group_id" IS '审批组ID';

COMMENT ON COLUMN "public"."approval_group_module_relation"."module_id" IS '模块ID';

-------------------审批组用户关系表----------------------------------
CREATE TABLE "public"."approval_group_relation" (
  "group_id" varchar(40) COLLATE "pg_catalog"."default",
  "user_id" varchar(40) COLLATE "pg_catalog"."default",
  CONSTRAINT "fk1" FOREIGN KEY ("user_id") REFERENCES "public"."users" ("userid") ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT "fk2" FOREIGN KEY ("group_id") REFERENCES "public"."approval_group" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION
)
;
ALTER TABLE "public"."approval_group_relation"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."approval_group_relation"."group_id" IS '审批组ID';

COMMENT ON COLUMN "public"."approval_group_relation"."user_id" IS '用户ID';

-------------------审批项----------------------------------
CREATE TABLE "public"."approval_item" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "object_id" varchar COLLATE "pg_catalog"."default",
  "object_name" varchar COLLATE "pg_catalog"."default",
  "business_type" char(1) COLLATE "pg_catalog"."default",
  "approve_type" char(1) COLLATE "pg_catalog"."default",
  "status" char(1) COLLATE "pg_catalog"."default",
  "approve_group" varchar COLLATE "pg_catalog"."default",
  "approver" varchar COLLATE "pg_catalog"."default",
  "approve_time" timestamptz(6),
  "submitter" varchar COLLATE "pg_catalog"."default",
  "commit_time" timestamptz(6),
  "reason" text COLLATE "pg_catalog"."default",
  "module_id" varchar COLLATE "pg_catalog"."default",
  "version" int2,
  "tenant_id" varchar COLLATE "pg_catalog"."default",
  CONSTRAINT "approval_item_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "fk1" FOREIGN KEY ("approve_group") REFERENCES "public"."approval_group" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION
)
;
ALTER TABLE "public"."approval_item"
  OWNER TO "metaspace";

COMMENT ON COLUMN "public"."approval_item"."id" IS '审批项ID';

COMMENT ON COLUMN "public"."approval_item"."object_id" IS '送审对象ID';

COMMENT ON COLUMN "public"."approval_item"."object_name" IS '送审对象名称';

COMMENT ON COLUMN "public"."approval_item"."business_type" IS '业务类型编码';

COMMENT ON COLUMN "public"."approval_item"."approve_type" IS '审核类型';

COMMENT ON COLUMN "public"."approval_item"."status" IS '审核状态';

COMMENT ON COLUMN "public"."approval_item"."commit_time" IS '送审时间';

COMMENT ON COLUMN "public"."approval_item"."reason" IS '驳回原因';

-------------------更新目录表结构----------------------------------
ALTER TABLE sync_task_definition ADD COLUMN IF NOT EXISTS category_guid text;
COMMENT ON COLUMN sync_task_definition.category_guid IS '技术目录guid';
COMMENT ON COLUMN "public"."data_quality_task_execute"."execute_status" IS '执行状态:1-执行中,2-成功,3-失败,0-未执行,4-取消';
COMMENT ON COLUMN "public"."data_quality_task"."current_execution_status" IS '执行状态:1-执行中,2-成功,3-失败,0-待执行,4-取消';
ALTER TABLE data_quality_task_rule_execute ADD COLUMN IF NOT EXISTS general_warning_check_status int2;
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."general_warning_check_status" IS '一般告警：0-无告警,1-有告警，2-已关闭';
ALTER TABLE data_quality_task_execute ADD COLUMN IF NOT EXISTS general_warning_count int4;
COMMENT ON COLUMN "public"."data_quality_task_execute"."general_warning_count" IS '普通告警数';
ALTER TABLE data_quality_task ADD COLUMN IF NOT EXISTS general_warning_total_count int8 DEFAULT 0;
COMMENT ON COLUMN "public"."data_quality_task"."general_warning_total_count" IS '普通告警总数统计';
ALTER TABLE category ADD COLUMN IF NOT EXISTS creator varchar COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."category"."creator" IS '创建人';
ALTER TABLE category ADD COLUMN IF NOT EXISTS creator varchar COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."category"."updater" IS '更新人';
ALTER TABLE category ADD COLUMN IF NOT EXISTS updatetime timestamptz(6) ;
COMMENT ON COLUMN "public"."category"."updatetime" IS '更新时间';
ALTER TABLE category ADD COLUMN IF NOT EXISTS code varchar COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."category"."code" IS '编码';

---------------------------------------为旧租户添加默认域-------------------------------------------------------
insert into category
select t1.guid,t1.description,t1."name",t1.upbrothercategoryguid,t3.guid as downbrothercategoryguid,t1.parentcategoryguid,t1.qualifiedname,t1.categorytype,t1."level",t1.safe,t3.tenantid,null,null,null,null,t1.code from
(select guid,description,"name" ,upbrothercategoryguid ,parentcategoryguid,qualifiedname ,categorytype ,"level" ,safe ,code
from category c2 where guid='index_field_default' and categorytype =5 limit 1) t1
,
(select distinct c3.tenantid,t2.guid from category c3
left join
(select tenantid,guid from category c2 where guid <>'index_field_default' and categorytype =5 and "level" =1 and upbrothercategoryguid is null) t2
on c3.tenantid=t2.tenantid
where c3.tenantid not in (select tenantid from category c2 where guid='index_field_default' and categorytype =5)
) t3 ;

update category set upbrothercategoryguid ='index_field_default' where guid in
(select guid from category c2 where guid <>'index_field_default' and categorytype =5 and "level" =1 and upbrothercategoryguid is null);


