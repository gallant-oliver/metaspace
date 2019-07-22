/*
 Navicat Premium Data Transfer

 Source Server         : metaspace-10.201.50.202
 Source Server Type    : PostgreSQL
 Source Server Version : 100005
 Source Host           : 10.201.50.202:5432
 Source Catalog        : postgres
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 100005
 File Encoding         : 65001


 Date: 17/07/2019 11:40:49

*/


-- ----------------------------
-- Table structure for api_module
-- ----------------------------
DROP TABLE IF EXISTS "api_module";
CREATE TABLE "api_module" (
  "path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "method" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "module_id" int2,
  "prefix_check" bool
)
;
ALTER TABLE "api_module" OWNER TO "postgres";

-- ----------------------------
-- Table structure for apigroup
-- ----------------------------
DROP TABLE IF EXISTS "apigroup";
CREATE TABLE "apigroup" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "parentguid" varchar COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "generator" varchar(255) COLLATE "pg_catalog"."default",
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "updatetime" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "apigroup" OWNER TO "postgres";
COMMENT ON COLUMN "apigroup"."guid" IS '唯一ID';
COMMENT ON COLUMN "apigroup"."name" IS 'API分组名称';
COMMENT ON COLUMN "apigroup"."parentguid" IS '父节点ID';
COMMENT ON COLUMN "apigroup"."description" IS '描述';
COMMENT ON COLUMN "apigroup"."generator" IS '创建人';
COMMENT ON COLUMN "apigroup"."generatetime" IS '创建时间';
COMMENT ON COLUMN "apigroup"."updater" IS '更新人';
COMMENT ON COLUMN "apigroup"."updatetime" IS '更新时间';

-- ----------------------------
-- Table structure for apiinfo
-- ----------------------------
DROP TABLE IF EXISTS "apiinfo";
CREATE TABLE "apiinfo" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tableguid" varchar COLLATE "pg_catalog"."default",
  "dbguid" varchar COLLATE "pg_catalog"."default",
  "keeper" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "maxrownumber" float8,
  "fields" json,
  "version" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "protocol" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "requestmode" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "returntype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "path" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "updatetime" varchar COLLATE "pg_catalog"."default",
  "groupguid" varchar COLLATE "pg_catalog"."default",
  "star" bool,
  "publish" bool
)
;
ALTER TABLE "apiinfo" OWNER TO "postgres";
COMMENT ON COLUMN "apiinfo"."guid" IS '唯一ID';
COMMENT ON COLUMN "apiinfo"."name" IS 'API名称';
COMMENT ON COLUMN "apiinfo"."tableguid" IS '关联表guid';
COMMENT ON COLUMN "apiinfo"."dbguid" IS '关联表所属库guid';
COMMENT ON COLUMN "apiinfo"."keeper" IS '创建人';
COMMENT ON COLUMN "apiinfo"."maxrownumber" IS '查询结果最大行数';
COMMENT ON COLUMN "apiinfo"."fields" IS '查询条件';
COMMENT ON COLUMN "apiinfo"."version" IS '版本';
COMMENT ON COLUMN "apiinfo"."description" IS '描述';
COMMENT ON COLUMN "apiinfo"."protocol" IS '协议类型';
COMMENT ON COLUMN "apiinfo"."requestmode" IS '请求方式';
COMMENT ON COLUMN "apiinfo"."returntype" IS '返回类型';
COMMENT ON COLUMN "apiinfo"."path" IS '路径';
COMMENT ON COLUMN "apiinfo"."generatetime" IS '创建时间';
COMMENT ON COLUMN "apiinfo"."updater" IS '更新人';
COMMENT ON COLUMN "apiinfo"."updatetime" IS '更新时间';
COMMENT ON COLUMN "apiinfo"."groupguid" IS 'API分组ID';
COMMENT ON COLUMN "apiinfo"."star" IS '（已废弃）';
COMMENT ON COLUMN "apiinfo"."publish" IS '是否发布';

-- ----------------------------
-- Table structure for business2table
-- ----------------------------
DROP TABLE IF EXISTS "business2table";
CREATE TABLE "business2table" (
  "businessid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "creator" varchar(255) COLLATE "pg_catalog"."default",
  "generatetime" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "business2table" OWNER TO "postgres";
COMMENT ON COLUMN "business2table"."businessid" IS '业务对象ID';
COMMENT ON COLUMN "business2table"."tableguid" IS '表ID';
COMMENT ON COLUMN "business2table"."creator" IS '创建者';
COMMENT ON COLUMN "business2table"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for business_relation
-- ----------------------------
DROP TABLE IF EXISTS "business_relation";
CREATE TABLE "business_relation" (
  "categoryguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "relationshipguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "businessid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "business_relation" OWNER TO "postgres";
COMMENT ON COLUMN "business_relation"."categoryguid" IS '技术目录ID';
COMMENT ON COLUMN "business_relation"."relationshipguid" IS '唯一ID';
COMMENT ON COLUMN "business_relation"."businessid" IS '业务对象ID';
COMMENT ON COLUMN "business_relation"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for businessinfo
-- ----------------------------
DROP TABLE IF EXISTS "businessinfo";
CREATE TABLE "businessinfo" (
  "businessid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "departmentid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "module" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "owner" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "manager" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "maintainer" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "dataassets" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "businesslastupdate" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "businessoperator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "technicallastupdate" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "technicaloperator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "technicalstatus" int2,
  "businessstatus" int2,
  "submitter" varchar COLLATE "pg_catalog"."default",
  "ticketnumber" varchar COLLATE "pg_catalog"."default",
  "submissiontime" varchar COLLATE "pg_catalog"."default",
  "level2categoryid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "trusttable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "businessinfo" OWNER TO "postgres";
COMMENT ON COLUMN "businessinfo"."businessid" IS '唯一ID';
COMMENT ON COLUMN "businessinfo"."departmentid" IS '业务部门ID（业务目录）';
COMMENT ON COLUMN "businessinfo"."name" IS '业务对象名称';
COMMENT ON COLUMN "businessinfo"."module" IS '业务模块';
COMMENT ON COLUMN "businessinfo"."description" IS '业务描述';
COMMENT ON COLUMN "businessinfo"."owner" IS '所有者';
COMMENT ON COLUMN "businessinfo"."manager" IS '管理者';
COMMENT ON COLUMN "businessinfo"."maintainer" IS '维护者';
COMMENT ON COLUMN "businessinfo"."dataassets" IS '相关数据资产';
COMMENT ON COLUMN "businessinfo"."businesslastupdate" IS '业务对象最后更新时间';
COMMENT ON COLUMN "businessinfo"."businessoperator" IS '业务对象更新人';
COMMENT ON COLUMN "businessinfo"."technicallastupdate" IS '技术对象最后更新时间';
COMMENT ON COLUMN "businessinfo"."technicaloperator" IS '技术对象更新人';
COMMENT ON COLUMN "businessinfo"."technicalstatus" IS '技术信息补充状态（1为已补充，0为未补充）';
COMMENT ON COLUMN "businessinfo"."businessstatus" IS '业务信息补充状态（1为已补充，0为未补充）';
COMMENT ON COLUMN "businessinfo"."submitter" IS '创建人';
COMMENT ON COLUMN "businessinfo"."ticketnumber" IS 'ticketNumber';
COMMENT ON COLUMN "businessinfo"."submissiontime" IS '创建时间';
COMMENT ON COLUMN "businessinfo"."level2categoryid" IS '所属二级部门';
COMMENT ON COLUMN "businessinfo"."trusttable" IS '唯一信任数据';

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS "category";
CREATE TABLE "category" (
  "guid" text COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "name" text COLLATE "pg_catalog"."default",
  "upbrothercategoryguid" text COLLATE "pg_catalog"."default",
  "downbrothercategoryguid" text COLLATE "pg_catalog"."default",
  "parentcategoryguid" text COLLATE "pg_catalog"."default",
  "qualifiedname" text COLLATE "pg_catalog"."default",
  "categorytype" int2,
  "level" int2
)
;
ALTER TABLE "category" OWNER TO "postgres";
COMMENT ON COLUMN "category"."guid" IS '目录唯一ID';
COMMENT ON COLUMN "category"."description" IS '描述';
COMMENT ON COLUMN "category"."name" IS '目录名称';
COMMENT ON COLUMN "category"."upbrothercategoryguid" IS '上面节点ID';
COMMENT ON COLUMN "category"."downbrothercategoryguid" IS '下面节点ID';
COMMENT ON COLUMN "category"."parentcategoryguid" IS '父节点ID';
COMMENT ON COLUMN "category"."qualifiedname" IS '（已废弃）';
COMMENT ON COLUMN "category"."categorytype" IS '目录类型（技术/业务）';
COMMENT ON COLUMN "category"."level" IS '当前层级';

-- ----------------------------
-- Table structure for column_info
-- ----------------------------
DROP TABLE IF EXISTS "column_info";
CREATE TABLE "column_info" (
  "column_guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "column_name" varchar COLLATE "pg_catalog"."default",
  "display_name" varchar COLLATE "pg_catalog"."default",
  "display_updatetime" varchar COLLATE "pg_catalog"."default",
  "table_guid" varchar COLLATE "pg_catalog"."default",
  "display_operator" varchar COLLATE "pg_catalog"."default",
  "status" varchar COLLATE "pg_catalog"."default",
  "type" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "column_info" OWNER TO "postgres";
COMMENT ON COLUMN "column_info"."column_guid" IS '字段Id';
COMMENT ON COLUMN "column_info"."column_name" IS '字段名称';
COMMENT ON COLUMN "column_info"."display_name" IS '中文别名';
COMMENT ON COLUMN "column_info"."display_updatetime" IS '中文别名更新时间';
COMMENT ON COLUMN "column_info"."table_guid" IS '表Id';
COMMENT ON COLUMN "column_info"."display_operator" IS '中文别名更新人';
COMMENT ON COLUMN "column_info"."status" IS '字段状态';
COMMENT ON COLUMN "column_info"."type" IS '字段类型';

-- ----------------------------
-- Table structure for data_standard
-- ----------------------------
DROP TABLE IF EXISTS "data_standard";
CREATE TABLE "data_standard" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "number" varchar COLLATE "pg_catalog"."default",
  "content" varchar COLLATE "pg_catalog"."default",
  "description" varchar COLLATE "pg_catalog"."default",
  "createtime" timestamptz(0),
  "updatetime" timestamptz(0),
  "operator" varchar COLLATE "pg_catalog"."default",
  "version" int4,
  "categoryid" varchar COLLATE "pg_catalog"."default",
  "delete" bool
)
;
ALTER TABLE "data_standard" OWNER TO "postgres";

COMMENT ON COLUMN "data_standard"."id" IS '唯一id';
COMMENT ON COLUMN "data_standard"."number" IS '标准编号';
COMMENT ON COLUMN "data_standard"."content" IS '标准内容';
COMMENT ON COLUMN "data_standard"."description" IS '标准比描述';
COMMENT ON COLUMN "data_standard"."createtime" IS '创建时间';
COMMENT ON COLUMN "data_standard"."updatetime" IS '更新时间';
COMMENT ON COLUMN "data_standard"."operator" IS '编辑人id';
COMMENT ON COLUMN "data_standard"."version" IS '版本号（每次+1）';
COMMENT ON COLUMN "data_standard"."categoryid" IS '所属分类';
COMMENT ON COLUMN "data_standard"."delete" IS 'true表示已删除';

-- ----------------------------
-- Table structure for module
-- ----------------------------
DROP TABLE IF EXISTS "module";
CREATE TABLE "module" (
  "moduleid" int4 NOT NULL,
  "modulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" int4
)
;
ALTER TABLE "module" OWNER TO "postgres";
COMMENT ON COLUMN "module"."moduleid" IS '权限id';
COMMENT ON COLUMN "module"."modulename" IS '权限名';
COMMENT ON COLUMN "module"."type" IS '模块类型，0管理权限1授权模块';

-- ----------------------------
-- Table structure for operate_log
-- ----------------------------
DROP TABLE IF EXISTS "operate_log";
CREATE TABLE "operate_log" (
  "id" text COLLATE "pg_catalog"."default",
  "number" text COLLATE "pg_catalog"."default",
  "userid" text COLLATE "pg_catalog"."default",
  "type" text COLLATE "pg_catalog"."default",
  "object" text COLLATE "pg_catalog"."default",
  "result" text COLLATE "pg_catalog"."default",
  "ip" text COLLATE "pg_catalog"."default",
  "createtime" timestamptz(0)
)
;
ALTER TABLE "operate_log" OWNER TO "postgres";

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS "organization";
CREATE TABLE "organization" (
  "checked" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "disable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "id" varchar COLLATE "pg_catalog"."default",
  "isopen" bool,
  "isvm" int8,
  "name" varchar COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "open" bool,
  "pid" varchar COLLATE "pg_catalog"."default",
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ptype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "updatetime" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "organization" OWNER TO "postgres";

-- ----------------------------
-- Table structure for privilege
-- ----------------------------
DROP TABLE IF EXISTS "privilege";
CREATE TABLE "privilege" (
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "privilegename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "createtime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "edit" int2,
  "delete" int2
)
;
ALTER TABLE "privilege" OWNER TO "postgres";
COMMENT ON COLUMN "privilege"."privilegeid" IS '方案id';
COMMENT ON COLUMN "privilege"."privilegename" IS '方案名';
COMMENT ON COLUMN "privilege"."description" IS '方案描述';
COMMENT ON COLUMN "privilege"."createtime" IS '创建时间';
COMMENT ON COLUMN "privilege"."edit" IS '是否可编辑1可编辑0不可';
COMMENT ON COLUMN "privilege"."delete" IS '是否可删除1可删除0不可';

-- ----------------------------
-- Table structure for privilege2module
-- ----------------------------
DROP TABLE IF EXISTS "privilege2module";
CREATE TABLE "privilege2module" (
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "moduleid" int4 NOT NULL
)
;
ALTER TABLE "privilege2module" OWNER TO "postgres";
COMMENT ON COLUMN "privilege2module"."privilegeid" IS '方案id';
COMMENT ON COLUMN "privilege2module"."moduleid" IS '功能id';

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_blob_triggers";
CREATE TABLE "qrtz_blob_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "blob_data" bytea
)
;
ALTER TABLE "qrtz_blob_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_calendars";
CREATE TABLE "qrtz_calendars" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "calendar" bytea NOT NULL
)
;
ALTER TABLE "qrtz_calendars" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_cron_triggers";
CREATE TABLE "qrtz_cron_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "cron_expression" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "time_zone_id" varchar(80) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "qrtz_cron_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_fired_triggers";
CREATE TABLE "qrtz_fired_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "entry_id" varchar(95) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "instance_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "fired_time" int8 NOT NULL,
  "sched_time" int8 NOT NULL,
  "priority" int4 NOT NULL,
  "state" varchar(16) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_nonconcurrent" bool,
  "requests_recovery" bool
)
;
ALTER TABLE "qrtz_fired_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_job_details";
CREATE TABLE "qrtz_job_details" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(250) COLLATE "pg_catalog"."default",
  "job_class_name" varchar(250) COLLATE "pg_catalog"."default" NOT NULL,
  "is_durable" bool NOT NULL,
  "is_nonconcurrent" bool NOT NULL,
  "is_update_data" bool NOT NULL,
  "requests_recovery" bool NOT NULL,
  "job_data" bytea
)
;
ALTER TABLE "qrtz_job_details" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_locks";
CREATE TABLE "qrtz_locks" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "lock_name" varchar(40) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "qrtz_locks" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_paused_trigger_grps";
CREATE TABLE "qrtz_paused_trigger_grps" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "qrtz_paused_trigger_grps" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_scheduler_state";
CREATE TABLE "qrtz_scheduler_state" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "instance_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "last_checkin_time" int8 NOT NULL,
  "checkin_interval" int8 NOT NULL
)
;
ALTER TABLE "qrtz_scheduler_state" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_simple_triggers";
CREATE TABLE "qrtz_simple_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "repeat_count" int8 NOT NULL,
  "repeat_interval" int8 NOT NULL,
  "times_triggered" int8 NOT NULL
)
;
ALTER TABLE "qrtz_simple_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_simprop_triggers";
CREATE TABLE "qrtz_simprop_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "str_prop_1" varchar(512) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "str_prop_2" varchar(512) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "str_prop_3" varchar(512) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "int_prop_1" int4,
  "int_prop_2" int4,
  "long_prop_1" int8,
  "long_prop_2" int8,
  "dec_prop_1" numeric(13,4) DEFAULT NULL::numeric,
  "dec_prop_2" numeric(13,4) DEFAULT NULL::numeric,
  "bool_prop_1" bool,
  "bool_prop_2" bool
)
;
ALTER TABLE "qrtz_simprop_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_triggers";
CREATE TABLE "qrtz_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(250) COLLATE "pg_catalog"."default",
  "next_fire_time" int8,
  "prev_fire_time" int8,
  "priority" int4,
  "trigger_state" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_type" varchar(8) COLLATE "pg_catalog"."default" NOT NULL,
  "start_time" int8 NOT NULL,
  "end_time" int8,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default",
  "misfire_instr" int2,
  "job_data" bytea
)
;
ALTER TABLE "qrtz_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS "report";
CREATE TABLE "report" (
  "reportid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "reportname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "templatename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "periodcron" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "orangealerts" int8,
  "redalerts" int8,
  "source" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "buildtype" int4,
  "reportproducedate" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "templateid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "alert" int2
)
;
ALTER TABLE "report" OWNER TO "postgres";
COMMENT ON COLUMN "report"."reportid" IS '唯一ID';
COMMENT ON COLUMN "report"."reportname" IS '报表名称';
COMMENT ON COLUMN "report"."templatename" IS '模板名称';
COMMENT ON COLUMN "report"."periodcron" IS '生成周期的Cron表达式，生成1次传空';
COMMENT ON COLUMN "report"."orangealerts" IS '橙色告警数';
COMMENT ON COLUMN "report"."redalerts" IS '红色告警数';
COMMENT ON COLUMN "report"."source" IS '源库表';
COMMENT ON COLUMN "report"."buildtype" IS '生成方式，0代表周期生成，1代表生成1次';
COMMENT ON COLUMN "report"."reportproducedate" IS '报表生成日期';
COMMENT ON COLUMN "report"."templateid" IS '模板Id';
COMMENT ON COLUMN "report"."alert" IS '告警提示，1开启，0关闭';

-- ----------------------------
-- Table structure for report_error
-- ----------------------------
DROP TABLE IF EXISTS "report_error";
CREATE TABLE "report_error" (
  "errorid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "templateid" varchar COLLATE "pg_catalog"."default",
  "reportid" varchar COLLATE "pg_catalog"."default",
  "ruleid" varchar COLLATE "pg_catalog"."default",
  "content" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "retrycount" int2
)
;
ALTER TABLE "report_error" OWNER TO "postgres";
COMMENT ON COLUMN "report_error"."errorid" IS '唯一标识';
COMMENT ON COLUMN "report_error"."templateid" IS '模板Id';
COMMENT ON COLUMN "report_error"."reportid" IS '报表Id';
COMMENT ON COLUMN "report_error"."ruleid" IS '规则Id';
COMMENT ON COLUMN "report_error"."content" IS '错误内容';
COMMENT ON COLUMN "report_error"."generatetime" IS '发生时间';
COMMENT ON COLUMN "report_error"."retrycount" IS '重试次数';

-- ----------------------------
-- Table structure for report_userrule
-- ----------------------------
DROP TABLE IF EXISTS "report_userrule";
CREATE TABLE "report_userrule" (
  "reportid" varchar COLLATE "pg_catalog"."default",
  "reportrulevalue" float8,
  "reportrulestatus" int2,
  "ruleid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ruletype" int4,
  "rulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumnname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumntype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulechecktype" int4,
  "rulecheckexpression" int4,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "refvalue" float8,
  "templateruleid" varchar COLLATE "pg_catalog"."default",
  "generatetime" float8
)
;
ALTER TABLE "report_userrule" OWNER TO "postgres";
COMMENT ON COLUMN "report_userrule"."reportid" IS '唯一标识';
COMMENT ON COLUMN "report_userrule"."reportrulevalue" IS '规则分析得到的实际值，与阈值做对比';
COMMENT ON COLUMN "report_userrule"."reportrulestatus" IS '规则校验后的结果,0（正常），1（橙色），2（红色）';
COMMENT ON COLUMN "report_userrule"."ruleid" IS '规则Id';
COMMENT ON COLUMN "report_userrule"."ruletype" IS '规则类型';
COMMENT ON COLUMN "report_userrule"."rulename" IS '规则名称';
COMMENT ON COLUMN "report_userrule"."ruleinfo" IS '规则说明';
COMMENT ON COLUMN "report_userrule"."rulecolumnname" IS '字段名称';
COMMENT ON COLUMN "report_userrule"."rulecolumntype" IS '字段类型';
COMMENT ON COLUMN "report_userrule"."rulechecktype" IS '规则检测方式，0(固定值)，1（波动值）';
COMMENT ON COLUMN "report_userrule"."rulecheckexpression" IS '规则校验表达式,0(=),1(!=),2(>),3(>=),4(<),5(<=)';
COMMENT ON COLUMN "report_userrule"."rulecheckthresholdunit" IS '规则校验阈值单位';
COMMENT ON COLUMN "report_userrule"."refvalue" IS '上周期任务取值（用于周期规则）';
COMMENT ON COLUMN "report_userrule"."templateruleid" IS '模板Id';
COMMENT ON COLUMN "report_userrule"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for report_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "report_userrule2threshold";
CREATE TABLE "report_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "report_userrule2threshold" OWNER TO "postgres";
COMMENT ON COLUMN "report_userrule2threshold"."thresholdvalue" IS '规则阈值';
COMMENT ON COLUMN "report_userrule2threshold"."ruleid" IS '规则Id';

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS "role";
CREATE TABLE "role" (
  "roleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "rolename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "updatetime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" int2,
  "createtime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "disable" int2,
  "delete" int2,
  "edit" int2,
  "valid" bool,
  "creator" varchar(255) COLLATE "pg_catalog"."default",
  "updater" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "role" OWNER TO "postgres";
COMMENT ON COLUMN "role"."roleid" IS '角色id';
COMMENT ON COLUMN "role"."rolename" IS '角色名';
COMMENT ON COLUMN "role"."description" IS '角色描述';
COMMENT ON COLUMN "role"."privilegeid" IS '方案id';
COMMENT ON COLUMN "role"."updatetime" IS '角色更新时间';
COMMENT ON COLUMN "role"."status" IS '角色是否启用，0未启用，1已启用';
COMMENT ON COLUMN "role"."createtime" IS '创建时间';
COMMENT ON COLUMN "role"."disable" IS '是否可禁用1可0不可';
COMMENT ON COLUMN "role"."delete" IS '是否可删除1可0不可';
COMMENT ON COLUMN "role"."edit" IS '是否可编辑1可0不可';
COMMENT ON COLUMN "role"."valid" IS '是否有效(删除后无效)';
COMMENT ON COLUMN "role"."creator" IS '创建者';
COMMENT ON COLUMN "role"."updater" IS '修改者';

-- ----------------------------
-- Table structure for role2category
-- ----------------------------
DROP TABLE IF EXISTS "role2category";
CREATE TABLE "role2category" (
  "roleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "categoryid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "operation" int2
)
;
ALTER TABLE "role2category" OWNER TO "postgres";
COMMENT ON COLUMN "role2category"."roleid" IS '角色Id';
COMMENT ON COLUMN "role2category"."categoryid" IS '目录Id';
COMMENT ON COLUMN "role2category"."operation" IS '是否允许操作，0不允许，1允许';

-- ----------------------------
-- Table structure for rule2buildtype
-- ----------------------------
DROP TABLE IF EXISTS "rule2buildtype";
CREATE TABLE "rule2buildtype" (
  "ruleid" int2 NOT NULL,
  "buildtype" int2 NOT NULL
)
;
ALTER TABLE "rule2buildtype" OWNER TO "postgres";
COMMENT ON COLUMN "rule2buildtype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "rule2buildtype"."buildtype" IS '生成方式,0代表周期生成，1代表生成1次';

-- ----------------------------
-- Table structure for rule2checktype
-- ----------------------------
DROP TABLE IF EXISTS "rule2checktype";
CREATE TABLE "rule2checktype" (
  "ruleid" int2 NOT NULL,
  "checktype" int2 NOT NULL
)
;
ALTER TABLE "rule2checktype" OWNER TO "postgres";
COMMENT ON COLUMN "rule2checktype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "rule2checktype"."checktype" IS '规则检测方式，0(固定值)，1（波动值）';

-- ----------------------------
-- Table structure for rule2datatype
-- ----------------------------
DROP TABLE IF EXISTS "rule2datatype";
CREATE TABLE "rule2datatype" (
  "ruleid" int2 NOT NULL,
  "datatype" int2 NOT NULL
)
;
ALTER TABLE "rule2datatype" OWNER TO "postgres";
COMMENT ON COLUMN "rule2datatype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "rule2datatype"."datatype" IS '允许的数据类型，1数值型2非数值型';

-- ----------------------------
-- Table structure for statistical
-- ----------------------------
DROP TABLE IF EXISTS "statistical";
CREATE TABLE "statistical" (
  "statisticalid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "date" int8,
  "statistical" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "statisticaltypeid" int4
)
;
ALTER TABLE "statistical" OWNER TO "postgres";
COMMENT ON COLUMN "statistical"."statisticalid" IS '统计信息id';
COMMENT ON COLUMN "statistical"."date" IS '日期';
COMMENT ON COLUMN "statistical"."statistical" IS '数据量';
COMMENT ON COLUMN "statistical"."statisticaltypeid" IS '统计类型';

-- ----------------------------
-- Table structure for statisticaltype
-- ----------------------------
DROP TABLE IF EXISTS "statisticaltype";
CREATE TABLE "statisticaltype" (
  "statisticaltypeid" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "statisticaltype" OWNER TO "postgres";
COMMENT ON COLUMN "statisticaltype"."statisticaltypeid" IS '统计类型id';
COMMENT ON COLUMN "statisticaltype"."name" IS '统计类型名称';

-- ----------------------------
-- Table structure for systemrule
-- ----------------------------
DROP TABLE IF EXISTS "systemrule";
CREATE TABLE "systemrule" (
  "ruleid" int2 NOT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default",
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default",
  "ruletype" int2,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "systemrule" OWNER TO "postgres";
COMMENT ON COLUMN "systemrule"."ruleid" IS '规则Id';
COMMENT ON COLUMN "systemrule"."rulename" IS '规则名称';
COMMENT ON COLUMN "systemrule"."ruleinfo" IS '规则信息';
COMMENT ON COLUMN "systemrule"."ruletype" IS '规则类型，0为表，1为字段';
COMMENT ON COLUMN "systemrule"."rulecheckthresholdunit" IS '规则校验阈值单位';

-- ----------------------------
-- Table structure for table2owner
-- ----------------------------
DROP TABLE IF EXISTS "table2owner";
CREATE TABLE "table2owner" (
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ownerid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "keeper" varchar(255) COLLATE "pg_catalog"."default",
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "table2owner" OWNER TO "postgres";
COMMENT ON COLUMN "table2owner"."tableguid" IS '表Id';
COMMENT ON COLUMN "table2owner"."ownerid" IS '组织架构Id';
COMMENT ON COLUMN "table2owner"."keeper" IS '创建人';
COMMENT ON COLUMN "table2owner"."generatetime" IS '创建时间';
COMMENT ON COLUMN "table2owner"."pkid" IS '组织架构pkId';

-- ----------------------------
-- Table structure for table2tag
-- ----------------------------
DROP TABLE IF EXISTS "table2tag";
CREATE TABLE "table2tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "table2tag" OWNER TO "postgres";
COMMENT ON COLUMN "table2tag"."tagid" IS '标签Id';
COMMENT ON COLUMN "table2tag"."tableguid" IS '表Id';

-- ----------------------------
-- Table structure for table_bak
-- ----------------------------
DROP TABLE IF EXISTS "table_bak";
CREATE TABLE "table_bak" (
  "tableguid" varchar(255) COLLATE "pg_catalog"."default",
  "tablename" varchar(255) COLLATE "pg_catalog"."default",
  "dbname" varchar(255) COLLATE "pg_catalog"."default",
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "createtime" varchar(255) COLLATE "pg_catalog"."default",
  "databaseguid" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "table_bak" OWNER TO "postgres";

-- ----------------------------
-- Table structure for table_relation
-- ----------------------------
DROP TABLE IF EXISTS "table_relation";
CREATE TABLE "table_relation" (
  "relationshipguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "categoryguid" varchar COLLATE "pg_catalog"."default",
  "tableguid" varchar COLLATE "pg_catalog"."default",
  "generatetime" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "table_relation" OWNER TO "postgres";
COMMENT ON COLUMN "table_relation"."relationshipguid" IS '唯一标识';
COMMENT ON COLUMN "table_relation"."categoryguid" IS '技术目录Id';
COMMENT ON COLUMN "table_relation"."tableguid" IS '表guid';
COMMENT ON COLUMN "table_relation"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for tableinfo
-- ----------------------------
DROP TABLE IF EXISTS "tableinfo";
CREATE TABLE "tableinfo" (
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "tablename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "dbname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "createtime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "databaseguid" varchar(255) COLLATE "pg_catalog"."default",
  "databasestatus" varchar(255) COLLATE "pg_catalog"."default",
  "subordinatesystem" varchar COLLATE "pg_catalog"."default",
  "subordinatedatabase" varchar(255) COLLATE "pg_catalog"."default",
  "systemadmin" varchar(255) COLLATE "pg_catalog"."default",
  "datawarehouseadmin" varchar(255) COLLATE "pg_catalog"."default",
  "datawarehousedescription" varchar(255) COLLATE "pg_catalog"."default",
  "catalogadmin" varchar(255) COLLATE "pg_catalog"."default",
  "display_name" varchar(255) COLLATE "pg_catalog"."default",
  "display_updatetime" varchar(255) COLLATE "pg_catalog"."default",
  "display_operator" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "tableinfo" OWNER TO "postgres";
COMMENT ON COLUMN "tableinfo"."tableguid" IS '表唯一标识';
COMMENT ON COLUMN "tableinfo"."tablename" IS '表名称';
COMMENT ON COLUMN "tableinfo"."dbname" IS '所属库名称';
COMMENT ON COLUMN "tableinfo"."status" IS '表状态（ACTIVE/DELETED）';
COMMENT ON COLUMN "tableinfo"."createtime" IS '创建时间';
COMMENT ON COLUMN "tableinfo"."databaseguid" IS '库Id';
COMMENT ON COLUMN "tableinfo"."databasestatus" IS '库状态（ACTIVE/DELETED）';
COMMENT ON COLUMN "tableinfo"."subordinatesystem" IS '源系统';
COMMENT ON COLUMN "tableinfo"."subordinatedatabase" IS '源数据库';
COMMENT ON COLUMN "tableinfo"."systemadmin" IS '源系统管理员';
COMMENT ON COLUMN "tableinfo"."datawarehouseadmin" IS '数仓管理员';
COMMENT ON COLUMN "tableinfo"."datawarehousedescription" IS '数仓描述';
COMMENT ON COLUMN "tableinfo"."catalogadmin" IS '目录管理员';
COMMENT ON COLUMN "tableinfo"."display_name" IS '中文别名';
COMMENT ON COLUMN "tableinfo"."display_updatetime" IS '中文别名更新时间';
COMMENT ON COLUMN "tableinfo"."display_operator" IS '中文别名更新人';

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS "tag";
CREATE TABLE "tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tagname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "tag" OWNER TO "postgres";
COMMENT ON COLUMN "tag"."tagid" IS '标签唯一标识';
COMMENT ON COLUMN "tag"."tagname" IS '标签名称';

-- ----------------------------
-- Table structure for template
-- ----------------------------
DROP TABLE IF EXISTS "template";
CREATE TABLE "template" (
  "templateid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tableid" varchar COLLATE "pg_catalog"."default",
  "buildtype" int2,
  "periodcron" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "starttime" varchar COLLATE "pg_catalog"."default",
  "templatestatus" int2,
  "templatename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tablerulesnum" int2,
  "columnrulesnum" int2,
  "source" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "finishedpercent" numeric(53,2) DEFAULT NULL::numeric,
  "shutdown" bool,
  "generatetime" float8
)
;
ALTER TABLE "template" OWNER TO "postgres";
COMMENT ON COLUMN "template"."templateid" IS '模板唯一标识';
COMMENT ON COLUMN "template"."tableid" IS '数据表Id';
COMMENT ON COLUMN "template"."buildtype" IS '生成方式,0代表周期生成，1代表生成1次';
COMMENT ON COLUMN "template"."periodcron" IS '生成周期的Cron表达式，生成1次传空';
COMMENT ON COLUMN "template"."starttime" IS '模板启用时间';
COMMENT ON COLUMN "template"."templatestatus" IS '模板状态0,"未启用"1,"已启动"2,"生成报告中"3 ,"暂停中"4, "已完成"';
COMMENT ON COLUMN "template"."templatename" IS '模板名称';
COMMENT ON COLUMN "template"."tablerulesnum" IS '表规则数量';
COMMENT ON COLUMN "template"."columnrulesnum" IS '字段规则数量';
COMMENT ON COLUMN "template"."source" IS '源库表';
COMMENT ON COLUMN "template"."finishedpercent" IS '完成百分比';
COMMENT ON COLUMN "template"."shutdown" IS '（未使用）';
COMMENT ON COLUMN "template"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for template2qrtz_job
-- ----------------------------
DROP TABLE IF EXISTS "template2qrtz_job";
CREATE TABLE "template2qrtz_job" (
  "templateid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "qrtz_job" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "template2qrtz_job" OWNER TO "postgres";
COMMENT ON COLUMN "template2qrtz_job"."templateid" IS '任务模板ID';
COMMENT ON COLUMN "template2qrtz_job"."qrtz_job" IS 'quartz任务标识';

-- ----------------------------
-- Table structure for template_userrule
-- ----------------------------
DROP TABLE IF EXISTS "template_userrule";
CREATE TABLE "template_userrule" (
  "ruleid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumnname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumntype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulechecktype" int2,
  "rulecheckexpression" int2,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "templateid" varchar COLLATE "pg_catalog"."default",
  "datatype" int2,
  "ruletype" int2,
  "systemruleid" varchar COLLATE "pg_catalog"."default",
  "generatetime" float8
)
;
ALTER TABLE "template_userrule" OWNER TO "postgres";
COMMENT ON COLUMN "template_userrule"."ruleid" IS '模板规则唯一标识';
COMMENT ON COLUMN "template_userrule"."rulename" IS '规则名称';
COMMENT ON COLUMN "template_userrule"."ruleinfo" IS '规则信息';
COMMENT ON COLUMN "template_userrule"."rulecolumnname" IS '字段名称';
COMMENT ON COLUMN "template_userrule"."rulecolumntype" IS '字段类型';
COMMENT ON COLUMN "template_userrule"."rulechecktype" IS '规则检测方式，0(固定值)，1（波动值）';
COMMENT ON COLUMN "template_userrule"."rulecheckexpression" IS '规则校验表达式,0(=),1(!=),2(>),3(>=),4(<),5(<=)';
COMMENT ON COLUMN "template_userrule"."rulecheckthresholdunit" IS '规则校验阈值单位';
COMMENT ON COLUMN "template_userrule"."templateid" IS '模板Id';
COMMENT ON COLUMN "template_userrule"."datatype" IS '数据类型1数值型2非数值型';
COMMENT ON COLUMN "template_userrule"."ruletype" IS '规则类型0表规则1字段规则';
COMMENT ON COLUMN "template_userrule"."systemruleid" IS '所属系统规则Id';
COMMENT ON COLUMN "template_userrule"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for template_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "template_userrule2threshold";
CREATE TABLE "template_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "template_userrule2threshold" OWNER TO "postgres";
COMMENT ON COLUMN "template_userrule2threshold"."thresholdvalue" IS '规则阈值';
COMMENT ON COLUMN "template_userrule2threshold"."ruleid" IS '模板规则ID';

-- ----------------------------
-- Table structure for user2apistar
-- ----------------------------
DROP TABLE IF EXISTS "user2apistar";
CREATE TABLE "user2apistar" (
  "apiguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "userid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "user2apistar" OWNER TO "postgres";
COMMENT ON COLUMN "user2apistar"."apiguid" IS 'API信息ID';
COMMENT ON COLUMN "user2apistar"."userid" IS '用户ID';

-- ----------------------------
-- Table structure for user2role
-- ----------------------------
DROP TABLE IF EXISTS "user2role";
CREATE TABLE "user2role" (
  "userid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "roleid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" varchar COLLATE "pg_catalog"."default",
  "updatetime" varchar COLLATE "pg_catalog"."default",
  "valid" bool,
  "creator" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "user2role" OWNER TO "postgres";
COMMENT ON COLUMN "user2role"."userid" IS '用户ID';
COMMENT ON COLUMN "user2role"."roleid" IS '角色ID';
COMMENT ON COLUMN "user2role"."createtime" IS '创建时间';
COMMENT ON COLUMN "user2role"."updatetime" IS '修改时间';
COMMENT ON COLUMN "user2role"."valid" IS '是否有效(被删除为false)';
COMMENT ON COLUMN "user2role"."creator" IS '创建者';
COMMENT ON COLUMN "user2role"."updater" IS '更新者';

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS "users";
CREATE TABLE "users" (
  "userid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "username" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "account" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "roleid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "users" OWNER TO "postgres";
COMMENT ON COLUMN "users"."userid" IS '用户id';
COMMENT ON COLUMN "users"."username" IS '用户名';
COMMENT ON COLUMN "users"."account" IS '用户账号';
COMMENT ON COLUMN "users"."roleid" IS '用户角色id';

-- ----------------------------
-- Primary Key structure for table api_module
-- ----------------------------
ALTER TABLE "api_module" ADD CONSTRAINT "api_module_pkey" PRIMARY KEY ("path", "method");

-- ----------------------------
-- Primary Key structure for table apigroup
-- ----------------------------
ALTER TABLE "apigroup" ADD CONSTRAINT "apigroup_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table apiinfo
-- ----------------------------
ALTER TABLE "apiinfo" ADD CONSTRAINT "apiinfo_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table business2table
-- ----------------------------
ALTER TABLE "business2table" ADD CONSTRAINT "business2table_pkey" PRIMARY KEY ("businessid", "tableguid");

-- ----------------------------
-- Primary Key structure for table business_relation
-- ----------------------------
ALTER TABLE "business_relation" ADD CONSTRAINT "business_relation_pkey" PRIMARY KEY ("relationshipguid");

-- ----------------------------
-- Primary Key structure for table businessinfo
-- ----------------------------
ALTER TABLE "businessinfo" ADD CONSTRAINT "business_pkey" PRIMARY KEY ("businessid");

-- ----------------------------
-- Primary Key structure for table category
-- ----------------------------
ALTER TABLE "category" ADD CONSTRAINT "table_catalog_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table column_info
-- ----------------------------
ALTER TABLE "column_info" ADD CONSTRAINT "column_info_pkey" PRIMARY KEY ("column_guid");

-- ----------------------------
-- Primary Key structure for table data_standard
-- ----------------------------
ALTER TABLE "data_standard" ADD CONSTRAINT "data_standard_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table module
-- ----------------------------
ALTER TABLE "module" ADD CONSTRAINT "privilege_pkey" PRIMARY KEY ("moduleid");

-- ----------------------------
-- Primary Key structure for table organization
-- ----------------------------
ALTER TABLE "organization" ADD CONSTRAINT "organization_pkey" PRIMARY KEY ("pkid");

-- ----------------------------
-- Primary Key structure for table privilege
-- ----------------------------
ALTER TABLE "privilege" ADD CONSTRAINT "buleprint_pkey" PRIMARY KEY ("privilegeid");

-- ----------------------------
-- Primary Key structure for table privilege2module
-- ----------------------------
ALTER TABLE "privilege2module" ADD CONSTRAINT "blueprint2privilege_pkey" PRIMARY KEY ("privilegeid", "moduleid");

-- ----------------------------
-- Primary Key structure for table qrtz_calendars
-- ----------------------------
ALTER TABLE "qrtz_calendars" ADD CONSTRAINT "qrtz_calendars_pkey" PRIMARY KEY ("sched_name", "calendar_name");

-- ----------------------------
-- Indexes structure for table qrtz_job_details
-- ----------------------------
CREATE INDEX "idx_qrtz_j_grp" ON "qrtz_job_details" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_j_req_recovery" ON "qrtz_job_details" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "requests_recovery" "pg_catalog"."bool_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table qrtz_job_details
-- ----------------------------
ALTER TABLE "qrtz_job_details" ADD CONSTRAINT "qrtz_job_details_pkey" PRIMARY KEY ("sched_name", "job_name", "job_group");

-- ----------------------------
-- Indexes structure for table qrtz_triggers
-- ----------------------------
CREATE INDEX "idx_qrtz_t_c" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "calendar_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_g" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_j" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_jg" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_n_g_state" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_n_state" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_next_fire_time" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_misfire" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_st" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_st_misfire" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_st_misfire_grp" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_state" ON "qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "qrtz_triggers" ADD CONSTRAINT "qrtz_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table report
-- ----------------------------
ALTER TABLE "report" ADD CONSTRAINT "report_pkey" PRIMARY KEY ("reportid");

-- ----------------------------
-- Primary Key structure for table report_error
-- ----------------------------
ALTER TABLE "report_error" ADD CONSTRAINT "report_error_pkey" PRIMARY KEY ("errorid");

-- ----------------------------
-- Primary Key structure for table report_userrule
-- ----------------------------
ALTER TABLE "report_userrule" ADD CONSTRAINT "report_ruleresult_pkey" PRIMARY KEY ("ruleid");

-- ----------------------------
-- Primary Key structure for table report_userrule2threshold
-- ----------------------------
ALTER TABLE "report_userrule2threshold" ADD CONSTRAINT "report_threshold_value_pkey" PRIMARY KEY ("thresholdvalue", "ruleid");

-- ----------------------------
-- Primary Key structure for table role
-- ----------------------------
ALTER TABLE "role" ADD CONSTRAINT "role_pkey" PRIMARY KEY ("roleid");

-- ----------------------------
-- Primary Key structure for table role2category
-- ----------------------------
ALTER TABLE "role2category" ADD CONSTRAINT "role2category_pkey" PRIMARY KEY ("roleid", "categoryid");

-- ----------------------------
-- Primary Key structure for table rule2buildtype
-- ----------------------------
ALTER TABLE "rule2buildtype" ADD CONSTRAINT "rule2buildtype_pkey" PRIMARY KEY ("ruleid", "buildtype");

-- ----------------------------
-- Primary Key structure for table rule2checktype
-- ----------------------------
ALTER TABLE "rule2checktype" ADD CONSTRAINT "rule2checktypeid_pkey" PRIMARY KEY ("ruleid", "checktype");

-- ----------------------------
-- Primary Key structure for table rule2datatype
-- ----------------------------
ALTER TABLE "rule2datatype" ADD CONSTRAINT "rule2datatype_pkey" PRIMARY KEY ("datatype", "ruleid");

-- ----------------------------
-- Primary Key structure for table statistical
-- ----------------------------
ALTER TABLE "statistical" ADD CONSTRAINT "statistical_pkey" PRIMARY KEY ("statisticalid");

-- ----------------------------
-- Primary Key structure for table statisticaltype
-- ----------------------------
ALTER TABLE "statisticaltype" ADD CONSTRAINT "statisticaltype_pkey" PRIMARY KEY ("statisticaltypeid");

-- ----------------------------
-- Primary Key structure for table systemrule
-- ----------------------------
ALTER TABLE "systemrule" ADD CONSTRAINT "rule_pkey" PRIMARY KEY ("ruleid");

-- ----------------------------
-- Primary Key structure for table table2owner
-- ----------------------------
ALTER TABLE "table2owner" ADD CONSTRAINT "table2owner_pkey" PRIMARY KEY ("tableguid", "ownerid", "pkid");

-- ----------------------------
-- Primary Key structure for table table2tag
-- ----------------------------
ALTER TABLE "table2tag" ADD CONSTRAINT "tagid2tableid_pkey" PRIMARY KEY ("tagid", "tableguid");

-- ----------------------------
-- Primary Key structure for table table_relation
-- ----------------------------
ALTER TABLE "table_relation" ADD CONSTRAINT "table_relation_pkey" PRIMARY KEY ("relationshipguid");

-- ----------------------------
-- Primary Key structure for table tableinfo
-- ----------------------------
ALTER TABLE "tableinfo" ADD CONSTRAINT "table_pkey" PRIMARY KEY ("tableguid");

-- ----------------------------
-- Primary Key structure for table tag
-- ----------------------------
ALTER TABLE "tag" ADD CONSTRAINT "tag_pkey" PRIMARY KEY ("tagid");

-- ----------------------------
-- Primary Key structure for table template
-- ----------------------------
ALTER TABLE "template" ADD CONSTRAINT "template_pkey" PRIMARY KEY ("templateid");

-- ----------------------------
-- Primary Key structure for table template2qrtz_job
-- ----------------------------
ALTER TABLE "template2qrtz_job" ADD CONSTRAINT "template2qrtz_trigger_pkey" PRIMARY KEY ("templateid", "qrtz_job");

-- ----------------------------
-- Primary Key structure for table template_userrule
-- ----------------------------
ALTER TABLE "template_userrule" ADD CONSTRAINT "system_rule_copy1_pkey" PRIMARY KEY ("ruleid");

-- ----------------------------
-- Primary Key structure for table template_userrule2threshold
-- ----------------------------
ALTER TABLE "template_userrule2threshold" ADD CONSTRAINT "threshold_pkey" PRIMARY KEY ("thresholdvalue", "ruleid");

-- ----------------------------
-- Primary Key structure for table user2apistar
-- ----------------------------
ALTER TABLE "user2apistar" ADD CONSTRAINT "user2apistar_pkey" PRIMARY KEY ("apiguid", "userid");

-- ----------------------------
-- Primary Key structure for table user2role
-- ----------------------------
ALTER TABLE "user2role" ADD CONSTRAINT "user2role_pkey" PRIMARY KEY ("userid", "roleid");

-- ----------------------------
-- Primary Key structure for table users
-- ----------------------------
ALTER TABLE "users" ADD CONSTRAINT "user_pkey" PRIMARY KEY ("userid");

-- ----------------------------
-- Foreign Keys structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "qrtz_triggers" ADD CONSTRAINT "qrtz_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "job_name", "job_group") REFERENCES "qrtz_job_details" ("sched_name", "job_name", "job_group") ON DELETE NO ACTION ON UPDATE NO ACTION;
