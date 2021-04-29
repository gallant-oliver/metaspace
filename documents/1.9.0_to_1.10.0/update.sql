----------------------------------------
--1.9.0-1.9.1
----------------------------------------
ALTER TABLE "public"."api"
  ADD COLUMN "mobius_id" varchar(255) COLLATE "pg_catalog"."default";

COMMENT ON COLUMN "public"."api"."mobius_id" IS '云平台id';

ALTER TABLE "public"."api_group"
  ADD COLUMN "mobius_id" varchar(255) COLLATE "pg_catalog"."default";

COMMENT ON COLUMN "public"."api_group"."mobius_id" IS '云平台id';


----------------------------------------
--1.9.1-1.10.0
----------------------------------------
create table ip_restriction
(
  id text not null,
  name text,
  description text,
  type text,
  ip_list text,
  enable boolean,
  creator_id text,
  create_time timestamptz(6),
  update_time timestamptz(6),
  tenant_id text
);

comment on table ip_restriction is '黑白名单';

comment on column ip_restriction.id is '黑白名单ID';

comment on column ip_restriction.name is '名称';

comment on column ip_restriction.description is '描述';

comment on column ip_restriction.type is '类型';

comment on column ip_restriction.ip_list is 'Ip 列表';

comment on column ip_restriction.enable is '是否启用';

comment on column ip_restriction.creator_id is '创建者Id';

comment on column ip_restriction.create_time is '创建时间';

comment on column ip_restriction.update_time is '更新时间';

comment on column ip_restriction.tenant_id is '租户';

create unique index ip_restriction_id_uindex
  on ip_restriction (id);

alter table ip_restriction
  add constraint ip_restriction_pk
    primary key (id);

CREATE TABLE "public"."desensitization_rule" (
  "id" text NOT NULL,
  "name" text ,
  "description" text ,
  "type" text ,
  "params" text ,
  "enable" bool,
  "creator_id" text,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "tenant_id" text,
  CONSTRAINT "desensitization_rule_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."desensitization_rule"."id" IS '脱敏规则id';

COMMENT ON COLUMN "public"."desensitization_rule"."name" IS '名称';

COMMENT ON COLUMN "public"."desensitization_rule"."description" IS '描述';

COMMENT ON COLUMN "public"."desensitization_rule"."type" IS '脱敏算法类型';

COMMENT ON COLUMN "public"."desensitization_rule"."params" IS '脱敏算法参数';

COMMENT ON COLUMN "public"."desensitization_rule"."enable" IS '是否启用';

comment on column ip_restriction.creator_id is '创建者Id';

COMMENT ON COLUMN "public"."desensitization_rule"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."desensitization_rule"."update_time" IS '升级时间';

COMMENT ON COLUMN "public"."desensitization_rule"."tenant_id" IS '租户 Id';


CREATE TABLE "public"."api_poly" (
  "id" text  NOT NULL,
  "api_id" text ,
  "api_version" text ,
  "poly" jsonb DEFAULT NULL,
  "status" text ,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  CONSTRAINT "api_poly_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON COLUMN "public"."api_poly"."id" IS '策略id';

COMMENT ON COLUMN "public"."api_poly"."api_id" IS 'api id';

COMMENT ON COLUMN "public"."api_poly"."api_version" IS 'api 版本';

COMMENT ON COLUMN "public"."api_poly"."poly" IS '策略详情';

COMMENT ON COLUMN "public"."api_poly"."status" IS '审核状态';

COMMENT ON COLUMN "public"."api_poly"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."api_poly"."update_time" IS '升级时间';


ALTER TABLE "public"."api_audit"
  ADD COLUMN "api_poly_id" text;

COMMENT ON COLUMN "public"."api_audit"."api_poly_id" IS 'api策略id';


ALTER TABLE "public"."api"
  ADD COLUMN "api_poly_entity" jsonb;

COMMENT ON COLUMN "public"."api"."api_poly_entity" IS '初始策略';

-- auto-generated definition
create table sync_task_definition
(
    id              text not null
        constraint sync_task_definition_pk
            primary key,
    name            text,
    creator         text,
    create_time     timestamp(6) with time zone,
    update_time     timestamp(6) with time zone,
    enable          boolean,
    cron_start_time timestamp(6) with time zone,
    cron_end_time   timestamp(6) with time zone,
    crontab         text,
    data_source_id  text,
    sync_all        boolean,
    schemas         text,
    tenant_id       text,
    description     text
);

comment on column sync_task_definition.id is 'id';

comment on column sync_task_definition.name is '名称';

comment on column sync_task_definition.creator is '创建者';

comment on column sync_task_definition.create_time is '创建时间';

comment on column sync_task_definition.update_time is '更新时间';

comment on column sync_task_definition.enable is '是否启动定时';

comment on column sync_task_definition.cron_start_time is '定时开始时间';

comment on column sync_task_definition.cron_end_time is '定时结束时间';

comment on column sync_task_definition.crontab is '定时表达式';

comment on column sync_task_definition.data_source_id is '数据源 id';

comment on column sync_task_definition.sync_all is '是否同步所有数据库';

comment on column sync_task_definition.schemas is '指定数据库列表';

comment on column sync_task_definition.tenant_id is '租户';

alter table sync_task_definition
    owner to metaspace;

-- auto-generated definition
create table sync_task_instance
(
    id            text not null
        constraint sync_task_instance_pk
            primary key,
    name          text,
    executor      text,
    status        text,
    start_time    timestamp(6) with time zone,
    update_time   timestamp(6) with time zone,
    log           text default ''::text,
    definition_id text
);

comment on column sync_task_instance.id is 'id';

comment on column sync_task_instance.name is '任务实例名称';

comment on column sync_task_instance.executor is '执行者名称';

comment on column sync_task_instance.status is '状态';

comment on column sync_task_instance.start_time is '开始时间';

comment on column sync_task_instance.update_time is '更新时间';

comment on column sync_task_instance.log is '日志';

comment on column sync_task_instance.definition_id is '租户';

alter table sync_task_instance
    owner to metaspace;


alter table tableinfo
  add source_id text;
COMMENT ON COLUMN "public"."tableinfo"."source_id" IS '数据源id';

update  tableinfo set source_id='hive' where source_id is null;


ALTER TABLE "public"."data_quality_rule_template"
  ADD COLUMN "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  ADD COLUMN "creator" varchar(36) COLLATE "pg_catalog"."default",
    ADD COLUMN "code" varchar(255) COLLATE "pg_catalog"."default",
  ADD COLUMN "sql" text COLLATE "pg_catalog"."default",
  ADD COLUMN "enable" boolean;

COMMENT ON COLUMN "public"."data_quality_rule_template"."tenantid" IS '租户id';

COMMENT ON COLUMN "public"."data_quality_rule_template"."creator" IS '创建者';

COMMENT ON COLUMN "public"."data_quality_rule_template"."sql" IS '自定义规则的sql语句';

COMMENT ON COLUMN "public"."data_quality_rule_template"."enable" IS '规则状态';

ALTER TABLE "public"."data_quality_sub_task"
  ADD COLUMN "pool" varchar(255) COLLATE "pg_catalog"."default",
  ADD COLUMN "config" varchar COLLATE "pg_catalog"."default";

COMMENT ON COLUMN "public"."data_quality_sub_task"."pool" IS '资源池';

COMMENT ON COLUMN "public"."data_quality_sub_task"."config" IS 'spark配置';

update data_quality_rule_template set tenantid = '1';

ALTER TABLE "public"."data_quality_rule_template"
  DROP CONSTRAINT "data_quality_rule_template_pkey",
  ALTER COLUMN "tenantid" SET NOT NULL,
  ADD CONSTRAINT "data_quality_rule_template_pkey" PRIMARY KEY ("id", "tenantid");

--
insert into data_quality_rule_template("name",scope,unit,description,create_time,update_time,delete,id,rule_type,type,tenantid,enable,code)
select "name",scope,unit,description,create_time,update_time,delete,id,rule_type,type,unnest(t.arr) as tenantid,true,id from (
select ARRAY_AGG(id) arr from tenant
) t,data_quality_rule_template r;

ALTER TABLE public.data_quality_rule_template ALTER COLUMN rule_type TYPE varchar(255) USING rule_type::varchar;

update public.data_quality_rule_template set rule_type = 'rule_' || rule_type;

delete from category where categorytype=4;

insert into category (guid,description,name,upbrothercategoryguid,downbrothercategoryguid,parentcategoryguid,categorytype,level,createtime,tenantid)
select c.*,unnest(t.arr) as tenantid from (
select 'rule_1' guid,'表体积' description, '表体积' "name",null upbrothercategoryguid, 'rule_2' downbrothercategoryguid,null parentcategoryguid,4 categorytype,1 "level",now() createtime
union
select 'rule_2' guid,'空值校验' description, '空值校验' "name",'rule_1' upbrothercategoryguid, 'rule_3' downbrothercategoryguid,null parentcategoryguid,4 categorytype,1 "level",now() createtime
union
select 'rule_3' guid,'唯一值校验' description, '唯一值校验' "name",'rule_2' upbrothercategoryguid, 'rule_4' downbrothercategoryguid,null parentcategoryguid,4 categorytype,1 "level",now() createtime
union
select 'rule_4' guid,'重复值校验' description, '重复值校验' "name",'rule_3' upbrothercategoryguid, 'rule_5' downbrothercategoryguid,null parentcategoryguid,4 categorytype,1 "level",now() createtime
union
select 'rule_5' guid,'数值型校验' description, '数值型校验' "name",'rule_4' upbrothercategoryguid, 'rule_6' downbrothercategoryguid,null parentcategoryguid,4 categorytype,1 "level",now() createtime
union
select 'rule_6' guid,'一致性校验' description, '一致性校验' "name",'rule_5' upbrothercategoryguid, null downbrothercategoryguid,null parentcategoryguid,4 categorytype,1 "level",now() createtime
) c,
(
select ARRAY_AGG(id) arr from tenant
) t;

update data_quality_sub_task_object set object_id =(
select '{"schema":"'||dbname||'","table":"'||tablename||'","column":"'||arr[1]||'"}' from
(select tablename,dbname,array_agg(column_name) arr from column_info join tableinfo on column_info.table_guid=tableinfo.tableguid where column_info.column_guid=object_id or tableinfo.tableguid=object_id group by tablename,dbname) t);

update data_quality_sub_task_rule set ruleid =(select rule_template_id from data_quality_rule where id=ruleid);

update data_quality_sub_task set pool = (select data_quality_task.pool from data_quality_task where data_quality_task.id=task_id);







