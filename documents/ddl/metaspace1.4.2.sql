/*
 Navicat Premium Data Transfer

 Source Server         : 10.201.50.202
 Source Server Type    : PostgreSQL
 Source Server Version : 100005
 Source Host           : 10.201.50.202:5432
 Source Catalog        : postgres
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 100005
 File Encoding         : 65001

 Date: 31/05/2019 12:49:43
*/


-- ----------------------------
-- Table structure for apigroup
-- ----------------------------
DROP TABLE IF EXISTS "public"."apigroup";
CREATE TABLE "public"."apigroup" (
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
ALTER TABLE "public"."apigroup" OWNER TO "postgres";
COMMENT ON COLUMN "public"."apigroup"."guid" IS '唯一ID';
COMMENT ON COLUMN "public"."apigroup"."name" IS 'API分组名称';
COMMENT ON COLUMN "public"."apigroup"."parentguid" IS '父节点ID';
COMMENT ON COLUMN "public"."apigroup"."description" IS '描述';
COMMENT ON COLUMN "public"."apigroup"."generator" IS '创建人';
COMMENT ON COLUMN "public"."apigroup"."generatetime" IS '创建时间';
COMMENT ON COLUMN "public"."apigroup"."updater" IS '更新人';
COMMENT ON COLUMN "public"."apigroup"."updatetime" IS '更新时间';

-- ----------------------------
-- Table structure for apiinfo
-- ----------------------------
DROP TABLE IF EXISTS "public"."apiinfo";
CREATE TABLE "public"."apiinfo" (
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
ALTER TABLE "public"."apiinfo" OWNER TO "postgres";
COMMENT ON COLUMN "public"."apiinfo"."guid" IS '唯一ID';
COMMENT ON COLUMN "public"."apiinfo"."name" IS 'API名称';
COMMENT ON COLUMN "public"."apiinfo"."tableguid" IS '关联表guid';
COMMENT ON COLUMN "public"."apiinfo"."dbguid" IS '关联表所属库guid';
COMMENT ON COLUMN "public"."apiinfo"."keeper" IS '创建人';
COMMENT ON COLUMN "public"."apiinfo"."maxrownumber" IS '查询结果最大行数';
COMMENT ON COLUMN "public"."apiinfo"."fields" IS '查询条件';
COMMENT ON COLUMN "public"."apiinfo"."version" IS '版本';
COMMENT ON COLUMN "public"."apiinfo"."description" IS '描述';
COMMENT ON COLUMN "public"."apiinfo"."protocol" IS '协议类型';
COMMENT ON COLUMN "public"."apiinfo"."requestmode" IS '请求方式';
COMMENT ON COLUMN "public"."apiinfo"."returntype" IS '返回类型';
COMMENT ON COLUMN "public"."apiinfo"."path" IS '路径';
COMMENT ON COLUMN "public"."apiinfo"."generatetime" IS '创建时间';
COMMENT ON COLUMN "public"."apiinfo"."updater" IS '更新人';
COMMENT ON COLUMN "public"."apiinfo"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."apiinfo"."groupguid" IS 'API分组ID';
COMMENT ON COLUMN "public"."apiinfo"."star" IS '（已废弃）';
COMMENT ON COLUMN "public"."apiinfo"."publish" IS '是否发布';

-- ----------------------------
-- Table structure for business2table
-- ----------------------------
DROP TABLE IF EXISTS "public"."business2table";
CREATE TABLE "public"."business2table" (
  "businessid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."business2table" OWNER TO "postgres";
COMMENT ON COLUMN "public"."business2table"."businessid" IS '业务对象ID';
COMMENT ON COLUMN "public"."business2table"."tableguid" IS '表ID';

-- ----------------------------
-- Table structure for business_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_relation";
CREATE TABLE "public"."business_relation" (
  "categoryguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "relationshipguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "businessid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."business_relation" OWNER TO "postgres";
COMMENT ON COLUMN "public"."business_relation"."categoryguid" IS '技术目录ID';
COMMENT ON COLUMN "public"."business_relation"."relationshipguid" IS '唯一ID';
COMMENT ON COLUMN "public"."business_relation"."businessid" IS '业务对象ID';
COMMENT ON COLUMN "public"."business_relation"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for businessinfo
-- ----------------------------
DROP TABLE IF EXISTS "public"."businessinfo";
CREATE TABLE "public"."businessinfo" (
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
ALTER TABLE "public"."businessinfo" OWNER TO "postgres";
COMMENT ON COLUMN "public"."businessinfo"."businessid" IS '唯一ID';
COMMENT ON COLUMN "public"."businessinfo"."departmentid" IS '业务部门ID（业务目录）';
COMMENT ON COLUMN "public"."businessinfo"."name" IS '业务对象名称';
COMMENT ON COLUMN "public"."businessinfo"."module" IS '业务模块';
COMMENT ON COLUMN "public"."businessinfo"."description" IS '业务描述';
COMMENT ON COLUMN "public"."businessinfo"."owner" IS '所有者';
COMMENT ON COLUMN "public"."businessinfo"."manager" IS '管理者';
COMMENT ON COLUMN "public"."businessinfo"."maintainer" IS '维护者';
COMMENT ON COLUMN "public"."businessinfo"."dataassets" IS '相关数据资产';
COMMENT ON COLUMN "public"."businessinfo"."businesslastupdate" IS '业务对象最后更新时间';
COMMENT ON COLUMN "public"."businessinfo"."businessoperator" IS '业务对象更新人';
COMMENT ON COLUMN "public"."businessinfo"."technicallastupdate" IS '技术对象最后更新时间';
COMMENT ON COLUMN "public"."businessinfo"."technicaloperator" IS '技术对象更新人';
COMMENT ON COLUMN "public"."businessinfo"."technicalstatus" IS '技术信息补充状态（1为已补充，0为未补充）';
COMMENT ON COLUMN "public"."businessinfo"."businessstatus" IS '业务信息补充状态（1为已补充，0为未补充）';
COMMENT ON COLUMN "public"."businessinfo"."submitter" IS '创建人';
COMMENT ON COLUMN "public"."businessinfo"."ticketnumber" IS 'ticketNumber';
COMMENT ON COLUMN "public"."businessinfo"."submissiontime" IS '创建时间';
COMMENT ON COLUMN "public"."businessinfo"."level2categoryid" IS '所属二级部门';
COMMENT ON COLUMN "public"."businessinfo"."trusttable" IS '唯一信任数据';

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS "public"."category";
CREATE TABLE "public"."category" (
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
ALTER TABLE "public"."category" OWNER TO "postgres";
COMMENT ON COLUMN "public"."category"."guid" IS '目录唯一ID';
COMMENT ON COLUMN "public"."category"."description" IS '描述';
COMMENT ON COLUMN "public"."category"."name" IS '目录名称';
COMMENT ON COLUMN "public"."category"."upbrothercategoryguid" IS '上面节点ID';
COMMENT ON COLUMN "public"."category"."downbrothercategoryguid" IS '下面节点ID';
COMMENT ON COLUMN "public"."category"."parentcategoryguid" IS '父节点ID';
COMMENT ON COLUMN "public"."category"."qualifiedname" IS '（已废弃）';
COMMENT ON COLUMN "public"."category"."categorytype" IS '目录类型（技术/业务）';
COMMENT ON COLUMN "public"."category"."level" IS '当前层级';

-- ----------------------------
-- Table structure for module
-- ----------------------------
DROP TABLE IF EXISTS "public"."module";
CREATE TABLE "public"."module" (
  "moduleid" int4 NOT NULL,
  "modulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" int4
)
;
ALTER TABLE "public"."module" OWNER TO "postgres";
COMMENT ON COLUMN "public"."module"."moduleid" IS '权限id';
COMMENT ON COLUMN "public"."module"."modulename" IS '权限名';
COMMENT ON COLUMN "public"."module"."type" IS '模块类型，0管理权限1授权模块';

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS "public"."organization";
CREATE TABLE "public"."organization" (
  "checked" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "disable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "id" varchar COLLATE "pg_catalog"."default",
  "isopen" bool,
  "isvm" int8 NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "open" bool,
  "pid" varchar COLLATE "pg_catalog"."default",
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ptype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "updatetime" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."organization" OWNER TO "postgres";
COMMENT ON COLUMN "public"."organization"."id" IS '组织架构ID';
COMMENT ON COLUMN "public"."organization"."isvm" IS '是否虚拟';
COMMENT ON COLUMN "public"."organization"."name" IS '组织架构名称';
COMMENT ON COLUMN "public"."organization"."pid" IS '当前组织架构父节点ID';
COMMENT ON COLUMN "public"."organization"."pkid" IS '主键ID';
COMMENT ON COLUMN "public"."organization"."updatetime" IS '更新时间';

-- ----------------------------
-- Table structure for privilege
-- ----------------------------
DROP TABLE IF EXISTS "public"."privilege";
CREATE TABLE "public"."privilege" (
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "privilegename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "createtime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "edit" int2,
  "delete" int2
)
;
ALTER TABLE "public"."privilege" OWNER TO "postgres";
COMMENT ON COLUMN "public"."privilege"."privilegeid" IS '方案id';
COMMENT ON COLUMN "public"."privilege"."privilegename" IS '方案名';
COMMENT ON COLUMN "public"."privilege"."description" IS '方案描述';
COMMENT ON COLUMN "public"."privilege"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."privilege"."edit" IS '是否可编辑1可编辑0不可';
COMMENT ON COLUMN "public"."privilege"."delete" IS '是否可删除1可删除0不可';

-- ----------------------------
-- Table structure for privilege2module
-- ----------------------------
DROP TABLE IF EXISTS "public"."privilege2module";
CREATE TABLE "public"."privilege2module" (
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "moduleid" int4 NOT NULL
)
;
ALTER TABLE "public"."privilege2module" OWNER TO "postgres";
COMMENT ON COLUMN "public"."privilege2module"."privilegeid" IS '方案id';
COMMENT ON COLUMN "public"."privilege2module"."moduleid" IS '功能id';

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_blob_triggers";
CREATE TABLE "public"."qrtz_blob_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "blob_data" bytea
)
;
ALTER TABLE "public"."qrtz_blob_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_calendars";
CREATE TABLE "public"."qrtz_calendars" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "calendar" bytea NOT NULL
)
;
ALTER TABLE "public"."qrtz_calendars" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_cron_triggers";
CREATE TABLE "public"."qrtz_cron_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "cron_expression" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "time_zone_id" varchar(80) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."qrtz_cron_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_fired_triggers";
CREATE TABLE "public"."qrtz_fired_triggers" (
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
ALTER TABLE "public"."qrtz_fired_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_job_details";
CREATE TABLE "public"."qrtz_job_details" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "description" varchar(250) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "job_class_name" varchar(250) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "is_durable" bool NOT NULL,
  "is_nonconcurrent" bool NOT NULL,
  "is_update_data" bool NOT NULL,
  "requests_recovery" bool NOT NULL,
  "job_data" bytea
)
;
ALTER TABLE "public"."qrtz_job_details" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_locks";
CREATE TABLE "public"."qrtz_locks" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "lock_name" varchar(40) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."qrtz_locks" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_paused_trigger_grps";
CREATE TABLE "public"."qrtz_paused_trigger_grps" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."qrtz_paused_trigger_grps" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_scheduler_state";
CREATE TABLE "public"."qrtz_scheduler_state" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "instance_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "last_checkin_time" int8 NOT NULL,
  "checkin_interval" int8 NOT NULL
)
;
ALTER TABLE "public"."qrtz_scheduler_state" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_simple_triggers";
CREATE TABLE "public"."qrtz_simple_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "repeat_count" int8 NOT NULL,
  "repeat_interval" int8 NOT NULL,
  "times_triggered" int8 NOT NULL
)
;
ALTER TABLE "public"."qrtz_simple_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_simprop_triggers";
CREATE TABLE "public"."qrtz_simprop_triggers" (
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
ALTER TABLE "public"."qrtz_simprop_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_triggers";
CREATE TABLE "public"."qrtz_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "description" varchar(250) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "next_fire_time" int8,
  "prev_fire_time" int8,
  "priority" int4,
  "trigger_state" varchar(16) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_type" varchar(8) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "start_time" int8 NOT NULL,
  "end_time" int8,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "misfire_instr" int2,
  "job_data" bytea
)
;
ALTER TABLE "public"."qrtz_triggers" OWNER TO "postgres";

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS "public"."report";
CREATE TABLE "public"."report" (
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
ALTER TABLE "public"."report" OWNER TO "postgres";
COMMENT ON COLUMN "public"."report"."reportid" IS '唯一ID';
COMMENT ON COLUMN "public"."report"."reportname" IS '报表名称';
COMMENT ON COLUMN "public"."report"."templatename" IS '模板名称';
COMMENT ON COLUMN "public"."report"."periodcron" IS '生成周期的Cron表达式，生成1次传空';
COMMENT ON COLUMN "public"."report"."orangealerts" IS '橙色告警数';
COMMENT ON COLUMN "public"."report"."redalerts" IS '红色告警数';
COMMENT ON COLUMN "public"."report"."source" IS '源库表';
COMMENT ON COLUMN "public"."report"."buildtype" IS '生成方式，0代表周期生成，1代表生成1次';
COMMENT ON COLUMN "public"."report"."reportproducedate" IS '报表生成日期';
COMMENT ON COLUMN "public"."report"."templateid" IS '模板Id';
COMMENT ON COLUMN "public"."report"."alert" IS '告警提示，1开启，0关闭';

-- ----------------------------
-- Table structure for report_error
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_error";
CREATE TABLE "public"."report_error" (
  "errorid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "templateid" varchar COLLATE "pg_catalog"."default",
  "reportid" varchar COLLATE "pg_catalog"."default",
  "ruleid" varchar COLLATE "pg_catalog"."default",
  "content" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "retrycount" int2
)
;
ALTER TABLE "public"."report_error" OWNER TO "postgres";
COMMENT ON COLUMN "public"."report_error"."errorid" IS '唯一标识';
COMMENT ON COLUMN "public"."report_error"."templateid" IS '模板Id';
COMMENT ON COLUMN "public"."report_error"."reportid" IS '报表Id';
COMMENT ON COLUMN "public"."report_error"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."report_error"."content" IS '错误内容';
COMMENT ON COLUMN "public"."report_error"."generatetime" IS '发生时间';
COMMENT ON COLUMN "public"."report_error"."retrycount" IS '重试次数';

-- ----------------------------
-- Table structure for report_userrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_userrule";
CREATE TABLE "public"."report_userrule" (
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
ALTER TABLE "public"."report_userrule" OWNER TO "postgres";
COMMENT ON COLUMN "public"."report_userrule"."reportid" IS '唯一标识';
COMMENT ON COLUMN "public"."report_userrule"."reportrulevalue" IS '规则分析得到的实际值，与阈值做对比';
COMMENT ON COLUMN "public"."report_userrule"."reportrulestatus" IS '规则校验后的结果,0（正常），1（橙色），2（红色）';
COMMENT ON COLUMN "public"."report_userrule"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."report_userrule"."ruletype" IS '规则类型';
COMMENT ON COLUMN "public"."report_userrule"."rulename" IS '规则名称';
COMMENT ON COLUMN "public"."report_userrule"."ruleinfo" IS '规则说明';
COMMENT ON COLUMN "public"."report_userrule"."rulecolumnname" IS '字段名称';
COMMENT ON COLUMN "public"."report_userrule"."rulecolumntype" IS '字段类型';
COMMENT ON COLUMN "public"."report_userrule"."rulechecktype" IS '规则检测方式，0(固定值)，1（波动值）';
COMMENT ON COLUMN "public"."report_userrule"."rulecheckexpression" IS '规则校验表达式,0(=),1(!=),2(>),3(>=),4(<),5(<=)';
COMMENT ON COLUMN "public"."report_userrule"."rulecheckthresholdunit" IS '规则校验阈值单位';
COMMENT ON COLUMN "public"."report_userrule"."refvalue" IS '上周期任务取值（用于周期规则）';
COMMENT ON COLUMN "public"."report_userrule"."templateruleid" IS '模板Id';
COMMENT ON COLUMN "public"."report_userrule"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for report_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_userrule2threshold";
CREATE TABLE "public"."report_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."report_userrule2threshold" OWNER TO "postgres";
COMMENT ON COLUMN "public"."report_userrule2threshold"."thresholdvalue" IS '规则阈值';
COMMENT ON COLUMN "public"."report_userrule2threshold"."ruleid" IS '规则Id';

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS "public"."role";
CREATE TABLE "public"."role" (
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
  "valid" BOOLEAN,
  "creator" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."role" OWNER TO "postgres";
COMMENT ON COLUMN "public"."role"."roleid" IS '角色id';
COMMENT ON COLUMN "public"."role"."rolename" IS '角色名';
COMMENT ON COLUMN "public"."role"."description" IS '角色描述';
COMMENT ON COLUMN "public"."role"."privilegeid" IS '方案id';
COMMENT ON COLUMN "public"."role"."updatetime" IS '角色更新时间';
COMMENT ON COLUMN "public"."role"."status" IS '角色是否启用，0未启用，1已启用';
COMMENT ON COLUMN "public"."role"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."role"."disable" IS '是否可禁用1可0不可';
COMMENT ON COLUMN "public"."role"."delete" IS '是否可删除1可0不可';
COMMENT ON COLUMN "public"."role"."edit" IS '是否可编辑1可0不可';
COMMENT ON COLUMN "public"."role"."valid" IS '角色是否有效';
COMMENT ON COLUMN "public"."role"."creator" IS '创建者';
COMMENT ON COLUMN "public"."role"."updater" IS '更新者';

-- ----------------------------
-- Table structure for role2category
-- ----------------------------
DROP TABLE IF EXISTS "public"."role2category";
CREATE TABLE "public"."role2category" (
  "roleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "categoryid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "operation" int2
)
;
ALTER TABLE "public"."role2category" OWNER TO "postgres";
COMMENT ON COLUMN "public"."role2category"."roleid" IS '角色Id';
COMMENT ON COLUMN "public"."role2category"."categoryid" IS '目录Id';
COMMENT ON COLUMN "public"."role2category"."operation" IS '是否允许操作，0不允许，1允许';

-- ----------------------------
-- Table structure for rule2buildtype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2buildtype";
CREATE TABLE "public"."rule2buildtype" (
  "ruleid" int2 NOT NULL,
  "buildtype" int2 NOT NULL
)
;
ALTER TABLE "public"."rule2buildtype" OWNER TO "postgres";
COMMENT ON COLUMN "public"."rule2buildtype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."rule2buildtype"."buildtype" IS '生成方式,0代表周期生成，1代表生成1次';

-- ----------------------------
-- Table structure for rule2checktype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2checktype";
CREATE TABLE "public"."rule2checktype" (
  "ruleid" int2 NOT NULL,
  "checktype" int2 NOT NULL
)
;
ALTER TABLE "public"."rule2checktype" OWNER TO "postgres";
COMMENT ON COLUMN "public"."rule2checktype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."rule2checktype"."checktype" IS '规则检测方式，0(固定值)，1（波动值）';

-- ----------------------------
-- Table structure for rule2datatype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2datatype";
CREATE TABLE "public"."rule2datatype" (
  "ruleid" int2 NOT NULL,
  "datatype" int2 NOT NULL
)
;
ALTER TABLE "public"."rule2datatype" OWNER TO "postgres";
COMMENT ON COLUMN "public"."rule2datatype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."rule2datatype"."datatype" IS '允许的数据类型，1数值型2非数值型';

-- ----------------------------
-- Table structure for statistical
-- ----------------------------
DROP TABLE IF EXISTS "public"."statistical";
CREATE TABLE "public"."statistical" (
  "statisticalid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "date" int8,
  "statistical" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "statisticaltypeid" int4
)
;
ALTER TABLE "public"."statistical" OWNER TO "postgres";
COMMENT ON COLUMN "public"."statistical"."statisticalid" IS '统计信息id';
COMMENT ON COLUMN "public"."statistical"."date" IS '日期';
COMMENT ON COLUMN "public"."statistical"."statistical" IS '数据量';
COMMENT ON COLUMN "public"."statistical"."statisticaltypeid" IS '统计类型';

-- ----------------------------
-- Table structure for statisticaltype
-- ----------------------------
DROP TABLE IF EXISTS "public"."statisticaltype";
CREATE TABLE "public"."statisticaltype" (
  "statisticaltypeid" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."statisticaltype" OWNER TO "postgres";
COMMENT ON COLUMN "public"."statisticaltype"."statisticaltypeid" IS '统计类型id';
COMMENT ON COLUMN "public"."statisticaltype"."name" IS '统计类型名称';

-- ----------------------------
-- Table structure for systemrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."systemrule";
CREATE TABLE "public"."systemrule" (
  "ruleid" int2 NOT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default",
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default",
  "ruletype" int2,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."systemrule" OWNER TO "postgres";
COMMENT ON COLUMN "public"."systemrule"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."systemrule"."rulename" IS '规则名称';
COMMENT ON COLUMN "public"."systemrule"."ruleinfo" IS '规则信息';
COMMENT ON COLUMN "public"."systemrule"."ruletype" IS '规则类型，0为表，1为字段';
COMMENT ON COLUMN "public"."systemrule"."rulecheckthresholdunit" IS '规则校验阈值单位';

-- ----------------------------
-- Table structure for table2owner
-- ----------------------------
DROP TABLE IF EXISTS "public"."table2owner";
CREATE TABLE "public"."table2owner" (
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ownerid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "keeper" varchar(255) COLLATE "pg_catalog"."default",
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."table2owner" OWNER TO "postgres";
COMMENT ON COLUMN "public"."table2owner"."tableguid" IS '表Id';
COMMENT ON COLUMN "public"."table2owner"."ownerid" IS '组织架构Id';
COMMENT ON COLUMN "public"."table2owner"."keeper" IS '创建人';
COMMENT ON COLUMN "public"."table2owner"."generatetime" IS '创建时间';
COMMENT ON COLUMN "public"."table2owner"."pkid" IS '组织架构pkId';

-- ----------------------------
-- Table structure for table2tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."table2tag";
CREATE TABLE "public"."table2tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."table2tag" OWNER TO "postgres";
COMMENT ON COLUMN "public"."table2tag"."tagid" IS '标签Id';
COMMENT ON COLUMN "public"."table2tag"."tableguid" IS '表Id';

-- ----------------------------
-- Table structure for table_bak
-- ----------------------------
DROP TABLE IF EXISTS "public"."table_bak";
CREATE TABLE "public"."table_bak" (
  "tableguid" varchar(255) COLLATE "pg_catalog"."default",
  "tablename" varchar(255) COLLATE "pg_catalog"."default",
  "dbname" varchar(255) COLLATE "pg_catalog"."default",
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "createtime" varchar(255) COLLATE "pg_catalog"."default",
  "databaseguid" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."table_bak" OWNER TO "postgres";

-- ----------------------------
-- Table structure for table_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."table_relation";
CREATE TABLE "public"."table_relation" (
  "relationshipguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "categoryguid" varchar COLLATE "pg_catalog"."default",
  "tableguid" varchar COLLATE "pg_catalog"."default",
  "generatetime" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."table_relation" OWNER TO "postgres";
COMMENT ON COLUMN "public"."table_relation"."relationshipguid" IS '唯一标识';
COMMENT ON COLUMN "public"."table_relation"."categoryguid" IS '技术目录Id';
COMMENT ON COLUMN "public"."table_relation"."tableguid" IS '表guid';
COMMENT ON COLUMN "public"."table_relation"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for tableinfo
-- ----------------------------
DROP TABLE IF EXISTS "public"."tableinfo";
CREATE TABLE "public"."tableinfo" (
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "tablename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "dbname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "createtime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "databaseguid" varchar(255) COLLATE "pg_catalog"."default",
  "databasestatus" varchar(255) COLLATE "pg_catalog"."default",
  "subordinatesystem" varchar COLLATE "pg_catalog"."default",
  "subordinatedatabase" varchar COLLATE "pg_catalog"."default",
  "systemadmin" varchar COLLATE "pg_catalog"."default",
  "datawarehouseadmin" varchar COLLATE "pg_catalog"."default",
  "datawarehousedescription" varchar COLLATE "pg_catalog"."default",
  "catalogadmin" varchar COLLATE "pg_catalog"."default",
  "display_name" varchar COLLATE "pg_catalog"."default",
  "display_updatetime" varchar COLLATE "pg_catalog"."default",
  "display_operator" varchar COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."tableinfo" OWNER TO "postgres";
COMMENT ON COLUMN "public"."tableinfo"."tableguid" IS '表唯一标识';
COMMENT ON COLUMN "public"."tableinfo"."tablename" IS '表名称';
COMMENT ON COLUMN "public"."tableinfo"."dbname" IS '所属库名称';
COMMENT ON COLUMN "public"."tableinfo"."status" IS '表状态（ACTIVE/DELETED）';
COMMENT ON COLUMN "public"."tableinfo"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."tableinfo"."databaseguid" IS '库Id';
COMMENT ON COLUMN "public"."tableinfo"."databasestatus" IS '库状态（ACTIVE/DELETED）';
COMMENT ON COLUMN "public"."tableinfo"."subordinatesystem" IS '源系统';
COMMENT ON COLUMN "public"."tableinfo"."subordinatedatabase" IS '源数据库';
COMMENT ON COLUMN "public"."tableinfo"."systemadmin" IS '源系统管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehouseadmin" IS '数仓管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehousedescription" IS '数仓描述';
COMMENT ON COLUMN "public"."tableinfo"."catalogadmin" IS '目录管理员';
COMMENT ON COLUMN "public"."tableinfo"."display_name" IS '表别名';
COMMENT ON COLUMN "public"."tableinfo"."display_updatetime" IS '表别名修改时间';
COMMENT ON COLUMN "public"."tableinfo"."display_operator" IS '表别名修改人';

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."tag";
CREATE TABLE "public"."tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tagname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."tag" OWNER TO "postgres";
COMMENT ON COLUMN "public"."tag"."tagid" IS '标签唯一标识';
COMMENT ON COLUMN "public"."tag"."tagname" IS '标签名称';


DROP TABLE IF EXISTS "public"."column_info";
CREATE TABLE "public"."column_info" (
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
COMMENT ON COLUMN "public"."column_info"."column_guid" IS '字段Id';
COMMENT ON COLUMN "public"."column_info"."column_name" IS '字段名称';
COMMENT ON COLUMN "public"."column_info"."display_name" IS '中文别名';
COMMENT ON COLUMN "public"."column_info"."display_updatetime" IS '中文别名更新时间';
COMMENT ON COLUMN "public"."column_info"."table_guid" IS '表Id';
COMMENT ON COLUMN "public"."column_info"."display_operator" IS '中文别名更新人';
COMMENT ON COLUMN "public"."column_info"."status" IS '字段状态';
COMMENT ON COLUMN "public"."column_info"."type" IS '字段类型';

-- ----------------------------
-- Table structure for template
-- ----------------------------
DROP TABLE IF EXISTS "public"."template";
CREATE TABLE "public"."template" (
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
ALTER TABLE "public"."template" OWNER TO "postgres";
COMMENT ON COLUMN "public"."template"."templateid" IS '模板唯一标识';
COMMENT ON COLUMN "public"."template"."tableid" IS '数据表Id';
COMMENT ON COLUMN "public"."template"."buildtype" IS '生成方式,0代表周期生成，1代表生成1次';
COMMENT ON COLUMN "public"."template"."periodcron" IS '生成周期的Cron表达式，生成1次传空';
COMMENT ON COLUMN "public"."template"."starttime" IS '模板启用时间';
COMMENT ON COLUMN "public"."template"."templatestatus" IS '模板状态0,"未启用"1,"已启动"2,"生成报告中"3 ,"暂停中"4, "已完成"';
COMMENT ON COLUMN "public"."template"."templatename" IS '模板名称';
COMMENT ON COLUMN "public"."template"."tablerulesnum" IS '表规则数量';
COMMENT ON COLUMN "public"."template"."columnrulesnum" IS '字段规则数量';
COMMENT ON COLUMN "public"."template"."source" IS '源库表';
COMMENT ON COLUMN "public"."template"."finishedpercent" IS '完成百分比';
COMMENT ON COLUMN "public"."template"."shutdown" IS '（未使用）';
COMMENT ON COLUMN "public"."template"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for template2qrtz_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."template2qrtz_job";
CREATE TABLE "public"."template2qrtz_job" (
  "templateid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "qrtz_job" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."template2qrtz_job" OWNER TO "postgres";
COMMENT ON COLUMN "public"."template2qrtz_job"."templateid" IS '任务模板ID';
COMMENT ON COLUMN "public"."template2qrtz_job"."qrtz_job" IS 'quartz任务标识';

-- ----------------------------
-- Table structure for template_userrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."template_userrule";
CREATE TABLE "public"."template_userrule" (
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
ALTER TABLE "public"."template_userrule" OWNER TO "postgres";
COMMENT ON COLUMN "public"."template_userrule"."ruleid" IS '模板规则唯一标识';
COMMENT ON COLUMN "public"."template_userrule"."rulename" IS '规则名称';
COMMENT ON COLUMN "public"."template_userrule"."ruleinfo" IS '规则信息';
COMMENT ON COLUMN "public"."template_userrule"."rulecolumnname" IS '字段名称';
COMMENT ON COLUMN "public"."template_userrule"."rulecolumntype" IS '字段类型';
COMMENT ON COLUMN "public"."template_userrule"."rulechecktype" IS '规则检测方式，0(固定值)，1（波动值）';
COMMENT ON COLUMN "public"."template_userrule"."rulecheckexpression" IS '规则校验表达式,0(=),1(!=),2(>),3(>=),4(<),5(<=)';
COMMENT ON COLUMN "public"."template_userrule"."rulecheckthresholdunit" IS '规则校验阈值单位';
COMMENT ON COLUMN "public"."template_userrule"."templateid" IS '模板Id';
COMMENT ON COLUMN "public"."template_userrule"."datatype" IS '数据类型1数值型2非数值型';
COMMENT ON COLUMN "public"."template_userrule"."ruletype" IS '规则类型0表规则1字段规则';
COMMENT ON COLUMN "public"."template_userrule"."systemruleid" IS '所属系统规则Id';
COMMENT ON COLUMN "public"."template_userrule"."generatetime" IS '创建时间';

-- ----------------------------
-- Table structure for template_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."template_userrule2threshold";
CREATE TABLE "public"."template_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."template_userrule2threshold" OWNER TO "postgres";
COMMENT ON COLUMN "public"."template_userrule2threshold"."thresholdvalue" IS '规则阈值';
COMMENT ON COLUMN "public"."template_userrule2threshold"."ruleid" IS '模板规则ID';

-- ----------------------------
-- Table structure for user2apistar
-- ----------------------------
DROP TABLE IF EXISTS "public"."user2apistar";
CREATE TABLE "public"."user2apistar" (
  "apiguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "userid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."user2apistar" OWNER TO "postgres";
COMMENT ON COLUMN "public"."user2apistar"."apiguid" IS 'API信息ID';
COMMENT ON COLUMN "public"."user2apistar"."userid" IS '用户ID';

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS "public"."users";
CREATE TABLE "public"."users" (
  "userid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "username" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "account" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "roleid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."users" OWNER TO "postgres";
COMMENT ON COLUMN "public"."users"."userid" IS '用户id';
COMMENT ON COLUMN "public"."users"."username" IS '用户名';
COMMENT ON COLUMN "public"."users"."account" IS '用户账号';
COMMENT ON COLUMN "public"."users"."roleid" IS '用户角色id';

-- ----------------------------
-- Primary Key structure for table apigroup
-- ----------------------------
ALTER TABLE "public"."apigroup" ADD CONSTRAINT "apigroup_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table apiinfo
-- ----------------------------
ALTER TABLE "public"."apiinfo" ADD CONSTRAINT "apiinfo_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table business2table
-- ----------------------------
ALTER TABLE "public"."business2table" ADD CONSTRAINT "business2table_pkey" PRIMARY KEY ("businessid", "tableguid");

-- ----------------------------
-- Primary Key structure for table business_relation
-- ----------------------------
ALTER TABLE "public"."business_relation" ADD CONSTRAINT "business_relation_pkey" PRIMARY KEY ("relationshipguid");

-- ----------------------------
-- Primary Key structure for table businessinfo
-- ----------------------------
ALTER TABLE "public"."businessinfo" ADD CONSTRAINT "business_pkey" PRIMARY KEY ("businessid");

-- ----------------------------
-- Primary Key structure for table category
-- ----------------------------
ALTER TABLE "public"."category" ADD CONSTRAINT "table_catalog_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table module
-- ----------------------------
ALTER TABLE "public"."module" ADD CONSTRAINT "privilege_pkey" PRIMARY KEY ("moduleid");

-- ----------------------------
-- Primary Key structure for table organization
-- ----------------------------
ALTER TABLE "public"."organization" ADD CONSTRAINT "organization_pkey" PRIMARY KEY ("pkid");

-- ----------------------------
-- Primary Key structure for table privilege
-- ----------------------------
ALTER TABLE "public"."privilege" ADD CONSTRAINT "buleprint_pkey" PRIMARY KEY ("privilegeid");

-- ----------------------------
-- Primary Key structure for table privilege2module
-- ----------------------------
ALTER TABLE "public"."privilege2module" ADD CONSTRAINT "blueprint2privilege_pkey" PRIMARY KEY ("privilegeid", "moduleid");

-- ----------------------------
-- Primary Key structure for table qrtz_blob_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_blob_triggers" ADD CONSTRAINT "qrtz_blob_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_calendars
-- ----------------------------
ALTER TABLE "public"."qrtz_calendars" ADD CONSTRAINT "qrtz_calendars_pkey" PRIMARY KEY ("sched_name", "calendar_name");

-- ----------------------------
-- Primary Key structure for table qrtz_cron_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_cron_triggers" ADD CONSTRAINT "qrtz_cron_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Indexes structure for table qrtz_fired_triggers
-- ----------------------------
CREATE INDEX "idx_qrtz_ft_inst_job_req_rcvry" ON "public"."qrtz_fired_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "instance_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "requests_recovery" "pg_catalog"."bool_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_ft_j_g" ON "public"."qrtz_fired_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_ft_jg" ON "public"."qrtz_fired_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_ft_t_g" ON "public"."qrtz_fired_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_ft_tg" ON "public"."qrtz_fired_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_ft_trig_inst_name" ON "public"."qrtz_fired_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "instance_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table qrtz_fired_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_fired_triggers" ADD CONSTRAINT "qrtz_fired_triggers_pkey" PRIMARY KEY ("sched_name", "entry_id");

-- ----------------------------
-- Indexes structure for table qrtz_job_details
-- ----------------------------
CREATE INDEX "idx_qrtz_j_grp" ON "public"."qrtz_job_details" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_j_req_recovery" ON "public"."qrtz_job_details" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "requests_recovery" "pg_catalog"."bool_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table qrtz_job_details
-- ----------------------------
ALTER TABLE "public"."qrtz_job_details" ADD CONSTRAINT "qrtz_job_details_pkey" PRIMARY KEY ("sched_name", "job_name", "job_group");

-- ----------------------------
-- Primary Key structure for table qrtz_locks
-- ----------------------------
ALTER TABLE "public"."qrtz_locks" ADD CONSTRAINT "qrtz_locks_pkey" PRIMARY KEY ("sched_name", "lock_name");

-- ----------------------------
-- Primary Key structure for table qrtz_paused_trigger_grps
-- ----------------------------
ALTER TABLE "public"."qrtz_paused_trigger_grps" ADD CONSTRAINT "qrtz_paused_trigger_grps_pkey" PRIMARY KEY ("sched_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_scheduler_state
-- ----------------------------
ALTER TABLE "public"."qrtz_scheduler_state" ADD CONSTRAINT "qrtz_scheduler_state_pkey" PRIMARY KEY ("sched_name", "instance_name");

-- ----------------------------
-- Primary Key structure for table qrtz_simple_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simple_triggers" ADD CONSTRAINT "qrtz_simple_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simprop_triggers" ADD CONSTRAINT "qrtz_simprop_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Indexes structure for table qrtz_triggers
-- ----------------------------
CREATE INDEX "idx_qrtz_t_c" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "calendar_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_g" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_j" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_jg" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_n_g_state" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_n_state" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_next_fire_time" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_misfire" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_st" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_st_misfire" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_nft_st_misfire_grp" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_qrtz_t_state" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_triggers" ADD CONSTRAINT "qrtz_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table report
-- ----------------------------
ALTER TABLE "public"."report" ADD CONSTRAINT "report_pkey" PRIMARY KEY ("reportid");

-- ----------------------------
-- Primary Key structure for table report_error
-- ----------------------------
ALTER TABLE "public"."report_error" ADD CONSTRAINT "report_error_pkey" PRIMARY KEY ("errorid");

-- ----------------------------
-- Primary Key structure for table report_userrule
-- ----------------------------
ALTER TABLE "public"."report_userrule" ADD CONSTRAINT "report_ruleresult_pkey" PRIMARY KEY ("ruleid");

-- ----------------------------
-- Primary Key structure for table report_userrule2threshold
-- ----------------------------
ALTER TABLE "public"."report_userrule2threshold" ADD CONSTRAINT "report_threshold_value_pkey" PRIMARY KEY ("thresholdvalue", "ruleid");

-- ----------------------------
-- Primary Key structure for table role
-- ----------------------------
ALTER TABLE "public"."role" ADD CONSTRAINT "role_pkey" PRIMARY KEY ("roleid");

-- ----------------------------
-- Primary Key structure for table role2category
-- ----------------------------
ALTER TABLE "public"."role2category" ADD CONSTRAINT "role2category_pkey" PRIMARY KEY ("roleid", "categoryid");

-- ----------------------------
-- Primary Key structure for table rule2buildtype
-- ----------------------------
ALTER TABLE "public"."rule2buildtype" ADD CONSTRAINT "rule2buildtype_pkey" PRIMARY KEY ("ruleid", "buildtype");

-- ----------------------------
-- Primary Key structure for table rule2checktype
-- ----------------------------
ALTER TABLE "public"."rule2checktype" ADD CONSTRAINT "rule2checktypeid_pkey" PRIMARY KEY ("ruleid", "checktype");

-- ----------------------------
-- Primary Key structure for table rule2datatype
-- ----------------------------
ALTER TABLE "public"."rule2datatype" ADD CONSTRAINT "rule2datatype_pkey" PRIMARY KEY ("datatype", "ruleid");

-- ----------------------------
-- Primary Key structure for table statistical
-- ----------------------------
ALTER TABLE "public"."statistical" ADD CONSTRAINT "statistical_pkey" PRIMARY KEY ("statisticalid");

-- ----------------------------
-- Primary Key structure for table statisticaltype
-- ----------------------------
ALTER TABLE "public"."statisticaltype" ADD CONSTRAINT "statisticaltype_pkey" PRIMARY KEY ("statisticaltypeid");

-- ----------------------------
-- Primary Key structure for table systemrule
-- ----------------------------
ALTER TABLE "public"."systemrule" ADD CONSTRAINT "rule_pkey" PRIMARY KEY ("ruleid");

-- ----------------------------
-- Primary Key structure for table table2owner
-- ----------------------------
ALTER TABLE "public"."table2owner" ADD CONSTRAINT "table2owner_pkey" PRIMARY KEY ("tableguid", "ownerid", "pkid");

-- ----------------------------
-- Primary Key structure for table table2tag
-- ----------------------------
ALTER TABLE "public"."table2tag" ADD CONSTRAINT "tagid2tableid_pkey" PRIMARY KEY ("tagid", "tableguid");

-- ----------------------------
-- Primary Key structure for table table_relation
-- ----------------------------
ALTER TABLE "public"."table_relation" ADD CONSTRAINT "table_relation_pkey" PRIMARY KEY ("relationshipguid");

-- ----------------------------
-- Primary Key structure for table tableinfo
-- ----------------------------
ALTER TABLE "public"."tableinfo" ADD CONSTRAINT "table_pkey" PRIMARY KEY ("tableguid");

-- ----------------------------
-- Primary Key structure for table tag
-- ----------------------------
ALTER TABLE "public"."tag" ADD CONSTRAINT "tag_pkey" PRIMARY KEY ("tagid");

-- ----------------------------
-- Primary Key structure for table column_info
-- ----------------------------
ALTER TABLE "public"."column_info" ADD CONSTRAINT "column_info_pkey" PRIMARY KEY ("column_guid");

-- ----------------------------
-- Primary Key structure for table template
-- ----------------------------
ALTER TABLE "public"."template" ADD CONSTRAINT "template_pkey" PRIMARY KEY ("templateid");

-- ----------------------------
-- Primary Key structure for table template2qrtz_job
-- ----------------------------
ALTER TABLE "public"."template2qrtz_job" ADD CONSTRAINT "template2qrtz_trigger_pkey" PRIMARY KEY ("templateid", "qrtz_job");

-- ----------------------------
-- Primary Key structure for table template_userrule
-- ----------------------------
ALTER TABLE "public"."template_userrule" ADD CONSTRAINT "system_rule_copy1_pkey" PRIMARY KEY ("ruleid");

-- ----------------------------
-- Primary Key structure for table template_userrule2threshold
-- ----------------------------
ALTER TABLE "public"."template_userrule2threshold" ADD CONSTRAINT "threshold_pkey" PRIMARY KEY ("thresholdvalue", "ruleid");

-- ----------------------------
-- Primary Key structure for table user2apistar
-- ----------------------------
ALTER TABLE "public"."user2apistar" ADD CONSTRAINT "user2apistar_pkey" PRIMARY KEY ("apiguid", "userid");

-- ----------------------------
-- Primary Key structure for table users
-- ----------------------------
ALTER TABLE "public"."users" ADD CONSTRAINT "user_pkey" PRIMARY KEY ("userid");

-- ----------------------------
-- Foreign Keys structure for table qrtz_blob_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_blob_triggers" ADD CONSTRAINT "qrtz_blob_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_cron_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_cron_triggers" ADD CONSTRAINT "qrtz_cron_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simple_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simple_triggers" ADD CONSTRAINT "qrtz_simple_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simprop_triggers" ADD CONSTRAINT "qrtz_simprop_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_triggers" ADD CONSTRAINT "qrtz_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "job_name", "job_group") REFERENCES "public"."qrtz_job_details" ("sched_name", "job_name", "job_group") ON DELETE NO ACTION ON UPDATE NO ACTION;
