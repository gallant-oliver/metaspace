-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS "public"."organization";
CREATE TABLE "public"."organization" (
  "checked" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "disable" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "id" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "isopen" bool DEFAULT NULL,
  "isvm" int8 DEFAULT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "open" bool DEFAULT NULL,
  "pid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "ptype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table organization
-- ----------------------------
ALTER TABLE "public"."organization" ADD CONSTRAINT "organization_pkey" PRIMARY KEY ("pkid");

-- ----------------------------
-- Table structure for apigroup
-- ----------------------------
DROP TABLE IF EXISTS "public"."apigroup";
CREATE TABLE "public"."apigroup" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "parentguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updater" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for apiinfo
-- ----------------------------
DROP TABLE IF EXISTS "public"."apiinfo";
CREATE TABLE "public"."apiinfo" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "dbguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "keeper" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "maxrownumber" float8 DEFAULT NULL,
  "fields" json DEFAULT NULL,
  "version" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "protocol" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "requestmode" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "returntype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "path" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updater" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "groupguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "star" bool DEFAULT NULL,
  "publish" bool DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for business2table
-- ----------------------------
DROP TABLE IF EXISTS "public"."business2table";
CREATE TABLE "public"."business2table" (
  "businessid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

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
  "technicalstatus" int2 DEFAULT NULL,
  "businessstatus" int2 DEFAULT NULL,
  "submitter" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "ticketnumber" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "submissiontime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "level2categoryid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "trusttable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS "public"."category";
CREATE TABLE "public"."category" (
  "guid" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "description" text COLLATE "pg_catalog"."default" DEFAULT NULL,
  "name" text COLLATE "pg_catalog"."default" DEFAULT NULL,
  "upbrothercategoryguid" text COLLATE "pg_catalog"."default" DEFAULT NULL,
  "downbrothercategoryguid" text COLLATE "pg_catalog"."default" DEFAULT NULL,
  "parentcategoryguid" text COLLATE "pg_catalog"."default" DEFAULT NULL,
  "qualifiedname" text COLLATE "pg_catalog"."default" DEFAULT NULL,
  "categorytype" int2 DEFAULT NULL,
  "level" int2 DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for module
-- ----------------------------
DROP TABLE IF EXISTS "public"."module";
CREATE TABLE "public"."module" (
  "moduleid" int4 NOT NULL DEFAULT NULL,
  "modulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" int4 DEFAULT NULL
)
;
COMMENT ON COLUMN "public"."module"."moduleid" IS '权限id';
COMMENT ON COLUMN "public"."module"."modulename" IS '权限名';
COMMENT ON COLUMN "public"."module"."type" IS '模块类型';

-- ----------------------------
-- Table structure for privilege
-- ----------------------------
DROP TABLE IF EXISTS "public"."privilege";
CREATE TABLE "public"."privilege" (
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "privilegename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "createtime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "edit" int2 DEFAULT NULL,
  "delete" int2 DEFAULT NULL
)
;
COMMENT ON COLUMN "public"."privilege"."privilegeid" IS '方案id';
COMMENT ON COLUMN "public"."privilege"."privilegename" IS '方案名';
COMMENT ON COLUMN "public"."privilege"."description" IS '方案描述';
COMMENT ON COLUMN "public"."privilege"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."privilege"."edit" IS '是否可编辑';
COMMENT ON COLUMN "public"."privilege"."delete" IS '是否可删除';

-- ----------------------------
-- Table structure for privilege2module
-- ----------------------------
DROP TABLE IF EXISTS "public"."privilege2module";
CREATE TABLE "public"."privilege2module" (
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "moduleid" int4 NOT NULL DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_blob_triggers";
CREATE TABLE "public"."qrtz_blob_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "blob_data" bytea DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_calendars";
CREATE TABLE "public"."qrtz_calendars" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "calendar" bytea NOT NULL DEFAULT NULL
)
;

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
  "fired_time" int8 NOT NULL DEFAULT NULL,
  "sched_time" int8 NOT NULL DEFAULT NULL,
  "priority" int4 NOT NULL DEFAULT NULL,
  "state" varchar(16) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_nonconcurrent" bool DEFAULT NULL,
  "requests_recovery" bool DEFAULT NULL
)
;

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
  "is_durable" bool NOT NULL DEFAULT NULL,
  "is_nonconcurrent" bool NOT NULL DEFAULT NULL,
  "is_update_data" bool NOT NULL DEFAULT NULL,
  "requests_recovery" bool NOT NULL DEFAULT NULL,
  "job_data" bytea DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_locks";
CREATE TABLE "public"."qrtz_locks" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "lock_name" varchar(40) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_paused_trigger_grps";
CREATE TABLE "public"."qrtz_paused_trigger_grps" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_scheduler_state";
CREATE TABLE "public"."qrtz_scheduler_state" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "instance_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "last_checkin_time" int8 NOT NULL DEFAULT NULL,
  "checkin_interval" int8 NOT NULL DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_simple_triggers";
CREATE TABLE "public"."qrtz_simple_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "repeat_count" int8 NOT NULL DEFAULT NULL,
  "repeat_interval" int8 NOT NULL DEFAULT NULL,
  "times_triggered" int8 NOT NULL DEFAULT NULL
)
;

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
  "int_prop_1" int4 DEFAULT NULL,
  "int_prop_2" int4 DEFAULT NULL,
  "long_prop_1" int8 DEFAULT NULL,
  "long_prop_2" int8 DEFAULT NULL,
  "dec_prop_1" numeric(13,4) DEFAULT NULL::numeric,
  "dec_prop_2" numeric(13,4) DEFAULT NULL::numeric,
  "bool_prop_1" bool DEFAULT NULL,
  "bool_prop_2" bool DEFAULT NULL
)
;

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
  "next_fire_time" int8 DEFAULT NULL,
  "prev_fire_time" int8 DEFAULT NULL,
  "priority" int4 DEFAULT NULL,
  "trigger_state" varchar(16) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_type" varchar(8) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "start_time" int8 NOT NULL DEFAULT NULL,
  "end_time" int8 DEFAULT NULL,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "misfire_instr" int2 DEFAULT NULL,
  "job_data" bytea DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS "public"."report";
CREATE TABLE "public"."report" (
  "reportid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "reportname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "templatename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "periodcron" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "orangealerts" int8 DEFAULT NULL,
  "redalerts" int8 DEFAULT NULL,
  "source" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "buildtype" int4 DEFAULT NULL,
  "reportproducedate" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "templateid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "alert" int2 DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for report_error
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_error";
CREATE TABLE "public"."report_error" (
  "errorid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "templateid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "reportid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "ruleid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "content" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "retrycount" int2 DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for report_userrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_userrule";
CREATE TABLE "public"."report_userrule" (
  "reportid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "reportrulevalue" float8 DEFAULT NULL,
  "reportrulestatus" int2 DEFAULT NULL,
  "ruleid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "ruletype" int4 DEFAULT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumnname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumntype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulechecktype" int4 DEFAULT NULL,
  "rulecheckexpression" int4 DEFAULT NULL,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "refvalue" float8 DEFAULT NULL,
  "templateruleid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" float8 DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for report_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_userrule2threshold";
CREATE TABLE "public"."report_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL DEFAULT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

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
  "status" int2 DEFAULT NULL,
  "createtime" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "disable" int2 DEFAULT NULL,
  "delete" int2 DEFAULT NULL,
  "edit" int2 DEFAULT NULL,
  "valid" BOOLEAN,
  "creator" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."role"."roleid" IS '角色id';
COMMENT ON COLUMN "public"."role"."rolename" IS '角色名';
COMMENT ON COLUMN "public"."role"."description" IS '角色描述';
COMMENT ON COLUMN "public"."role"."privilegeid" IS '方案id';
COMMENT ON COLUMN "public"."role"."updatetime" IS '角色更新时间';
COMMENT ON COLUMN "public"."role"."status" IS '角色是否启用，0未启用，1已启用';
COMMENT ON COLUMN "public"."role"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."role"."disable" IS '是否可禁用';
COMMENT ON COLUMN "public"."role"."delete" IS '是否可删除';
COMMENT ON COLUMN "public"."role"."edit" IS '是否可编辑';
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
  "operation" int2 DEFAULT NULL
)
;
COMMENT ON COLUMN "public"."role2category"."operation" IS '是否允许操作，0不允许，1允许';

-- ----------------------------
-- Table structure for rule2buildtype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2buildtype";
CREATE TABLE "public"."rule2buildtype" (
  "ruleid" int2 NOT NULL DEFAULT NULL,
  "buildtype" int2 NOT NULL DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for rule2checktype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2checktype";
CREATE TABLE "public"."rule2checktype" (
  "ruleid" int2 NOT NULL DEFAULT NULL,
  "checktype" int2 NOT NULL DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for rule2datatype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2datatype";
CREATE TABLE "public"."rule2datatype" (
  "ruleid" int2 NOT NULL DEFAULT NULL,
  "datatype" int2 NOT NULL DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for statistical
-- ----------------------------
DROP TABLE IF EXISTS "public"."statistical";
CREATE TABLE "public"."statistical" (
  "statisticalid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "date" int8 DEFAULT NULL,
  "statistical" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "statisticaltypeid" int4 DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for statisticaltype
-- ----------------------------
DROP TABLE IF EXISTS "public"."statisticaltype";
CREATE TABLE "public"."statisticaltype" (
  "statisticaltypeid" int4 NOT NULL DEFAULT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for systemrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."systemrule";
CREATE TABLE "public"."systemrule" (
  "ruleid" int2 NOT NULL DEFAULT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "ruletype" int2 DEFAULT NULL,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for table2tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."table2tag";
CREATE TABLE "public"."table2tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for table_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."table_relation";
CREATE TABLE "public"."table_relation" (
  "relationshipguid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "categoryguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

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
  "dataowner" json DEFAULT NULL,
  "databaseguid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
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

-- ----------------------------
-- Table structure for column_info
-- ----------------------------
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
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."tag";
CREATE TABLE "public"."tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "tagname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for template
-- ----------------------------
DROP TABLE IF EXISTS "public"."template";
CREATE TABLE "public"."template" (
  "templateid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "tableid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "buildtype" int2 DEFAULT NULL,
  "periodcron" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "starttime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "templatestatus" int2 DEFAULT NULL,
  "templatename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tablerulesnum" int2 DEFAULT NULL,
  "columnrulesnum" int2 DEFAULT NULL,
  "source" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "finishedpercent" numeric(53,2) DEFAULT NULL::numeric,
  "shutdown" bool DEFAULT NULL,
  "generatetime" float8 DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for template2qrtz_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."template2qrtz_job";
CREATE TABLE "public"."template2qrtz_job" (
  "templateid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "qrtz_job" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for template_userrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."template_userrule";
CREATE TABLE "public"."template_userrule" (
  "ruleid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumnname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulecolumntype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rulechecktype" int2 DEFAULT NULL,
  "rulecheckexpression" int2 DEFAULT NULL,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "templateid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "datatype" int2 DEFAULT NULL,
  "ruletype" int2 DEFAULT NULL,
  "systemruleid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" float8 DEFAULT NULL
)
;

-- ----------------------------
-- Table structure for template_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."template_userrule2threshold";
CREATE TABLE "public"."template_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL DEFAULT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for user2apistar
-- ----------------------------
DROP TABLE IF EXISTS "public"."user2apistar";
CREATE TABLE "public"."user2apistar" (
  "apiguid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "userid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL
)
;
-- ----------------------------
-- Table structure for table2owner
-- ----------------------------
DROP TABLE IF EXISTS "public"."table2owner";
CREATE TABLE "public"."table2owner" (
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "ownerid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "keeper" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table table2owner
-- ----------------------------
ALTER TABLE "public"."table2owner" ADD CONSTRAINT "table2owner_pkey" PRIMARY KEY ("tableguid", "ownerid");

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
-- Primary Key structure for table column_info
-- ----------------------------
ALTER TABLE "public"."column_info" ADD CONSTRAINT "column_info_pkey" PRIMARY KEY ("column_guid");

-- ----------------------------
-- Primary Key structure for table tag
-- ----------------------------
ALTER TABLE "public"."tag" ADD CONSTRAINT "tag_pkey" PRIMARY KEY ("tagid");

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
ALTER TABLE "public"."qrtz_blob_triggers" ADD CONSTRAINT "qrtz_blob_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_cron_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_cron_triggers" ADD CONSTRAINT "qrtz_cron_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simple_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simple_triggers" ADD CONSTRAINT "qrtz_simple_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simprop_triggers" ADD CONSTRAINT "qrtz_simprop_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_triggers" ADD CONSTRAINT "qrtz_triggers_sched_name_fkey" FOREIGN KEY ("sched_name", "job_name", "job_group") REFERENCES "qrtz_job_details" ("sched_name", "job_name", "job_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level) VALUES('1','贴源层',NUll,'2',0,1);
INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level) VALUES('2','基础层','1','3',0,1);
INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level) VALUES('3','规范层','2','4',0,1);
INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level) VALUES('4','通过层','3','5',0,1);
INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level) VALUES('5','应用层','4',NULL,0,1);
INSERT INTO "public"."privilege" VALUES ('2', '访客', '访客', NULL, 0, 0);
INSERT INTO "public"."privilege" VALUES ('1', 'Admin', '平台管理员', NULL, 0, 0);
INSERT INTO "public"."privilege" VALUES ('3', '管理', '技术权限', NULL, 1, 0);
INSERT INTO "public"."privilege" VALUES ('4', '业务', '业务权限', NULL, 1, 0);
INSERT INTO "public"."privilege" VALUES ('5', '技术', '业务对象管理', NULL, 1, 0);
INSERT INTO "public"."privilege" VALUES ('6', '业务目录管理员', '业务目录管理员', NULL, 1, 0);
INSERT INTO "public"."privilege" VALUES ('7', '技术目录管理员', '技术目录管理员', NULL, 1, 0);
INSERT INTO "public"."statisticaltype" VALUES (1, '数据库总量');
INSERT INTO "public"."statisticaltype" VALUES (2, '数据表总量');
INSERT INTO "public"."statisticaltype" VALUES (3, '业务对象总量');
INSERT INTO "public"."statisticaltype" VALUES (4, '业务对象已补充');
INSERT INTO "public"."statisticaltype" VALUES (5, '业务对象未补充');
INSERT INTO "public"."role" VALUES ('2', '访客', '访客', '2', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 0, 0, 1, true);
INSERT INTO "public"."role" VALUES ('4', '业务', '业务数据负责人', '4', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
INSERT INTO "public"."role" VALUES ('5', '技术', '技术数据负责人', '5', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
INSERT INTO "public"."role" VALUES ('1', '平台管理员', '平台管理员', '1', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 0, 0, 0, true);
INSERT INTO "public"."role" VALUES ('3', '管理员', '管理员', '3', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
INSERT INTO "public"."role" VALUES ('6', '业务目录管理员', '业务目录管理员', '6', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
INSERT INTO "public"."role" VALUES ('7', '技术目录管理员', '技术目录管理员', '7', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
INSERT INTO "public"."module" VALUES (1, '技术数据', 1);
INSERT INTO "public"."module" VALUES (2, '业务对象', 1);
INSERT INTO "public"."module" VALUES (3, '技术信息', 0);
INSERT INTO "public"."module" VALUES (4, '业务信息', 0);
INSERT INTO "public"."module" VALUES (5, '业务对象管理', 1);
INSERT INTO "public"."module" VALUES (6, '权限', 1);
INSERT INTO "public"."module" VALUES (7, '元数据管理', 1);
INSERT INTO "public"."module" VALUES (8, '技术目录', 0);
INSERT INTO "public"."module" VALUES (9, '业务目录', 0);
INSERT INTO "public"."module" VALUES (10, 'API信息', 1);
INSERT INTO "public"."privilege2module" VALUES ('1', 1);
INSERT INTO "public"."privilege2module" VALUES ('1', 2);
INSERT INTO "public"."privilege2module" VALUES ('1', 3);
INSERT INTO "public"."privilege2module" VALUES ('1', 4);
INSERT INTO "public"."privilege2module" VALUES ('1', 5);
INSERT INTO "public"."privilege2module" VALUES ('1', 6);
INSERT INTO "public"."privilege2module" VALUES ('1', 7);
INSERT INTO "public"."privilege2module" VALUES ('1', 8);
INSERT INTO "public"."privilege2module" VALUES ('1', 9);
INSERT INTO "public"."privilege2module" VALUES ('1', 10);
INSERT INTO "public"."privilege2module" VALUES ('2', 2);
INSERT INTO "public"."privilege2module" VALUES ('3', 1);
INSERT INTO "public"."privilege2module" VALUES ('3', 2);
INSERT INTO "public"."privilege2module" VALUES ('3', 3);
INSERT INTO "public"."privilege2module" VALUES ('3', 4);
INSERT INTO "public"."privilege2module" VALUES ('3', 5);
INSERT INTO "public"."privilege2module" VALUES ('3', 7);
INSERT INTO "public"."privilege2module" VALUES ('3', 8);
INSERT INTO "public"."privilege2module" VALUES ('3', 9);
INSERT INTO "public"."privilege2module" VALUES ('3', 10);
INSERT INTO "public"."privilege2module" VALUES ('4', 2);
INSERT INTO "public"."privilege2module" VALUES ('4', 4);
INSERT INTO "public"."privilege2module" VALUES ('4', 9);
INSERT INTO "public"."privilege2module" VALUES ('5', 1);
INSERT INTO "public"."privilege2module" VALUES ('5', 2);
INSERT INTO "public"."privilege2module" VALUES ('5', 3);
INSERT INTO "public"."privilege2module" VALUES ('5', 5);
INSERT INTO "public"."privilege2module" VALUES ('5', 7);
INSERT INTO "public"."privilege2module" VALUES ('5', 8);
INSERT INTO "public"."privilege2module" VALUES ('5', 10);
INSERT INTO "public"."privilege2module" VALUES ('6', 2);
INSERT INTO "public"."privilege2module" VALUES ('6', 9);
INSERT INTO "public"."privilege2module" VALUES ('7', 1);
INSERT INTO "public"."privilege2module" VALUES ('7', 8);
INSERT INTO "public"."privilege2module" VALUES ('7', 7);
INSERT INTO "public"."privilege2module" VALUES ('7', 2);
INSERT INTO "public"."apigroup" VALUES ('1', '全部分组', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."apigroup" VALUES ('0', '未分组', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."systemrule" VALUES (13, '字段平均值变化', '相比上一周期，字段平均值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (14, '字段汇总值变化', '相比上一周期，字段汇总值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (15, '字段最小值变化', '相比上一周期，字段最小值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (16, '
字段最大值变化', '相比上一周期，字段最大值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (29, '字段重复值个数/总行数', '计算字段重复值行数所占的比例', 1, '%');
INSERT INTO "public"."systemrule" VALUES (28, '字段空值个数/总行数', '计算字段空值行数所占的比例', 1, '%');
INSERT INTO "public"."systemrule" VALUES (27, '字段唯一值个数/总行数', '计算字段唯一值行数所占的比例', 1, '%');
INSERT INTO "public"."systemrule" VALUES (3, '表大小变化', '相比上一周期，表大小变化', 0, '字节');
INSERT INTO "public"."systemrule" VALUES (0, '表行数变化率', '相比上一周期，表行数变化率', 0, '%');
INSERT INTO "public"."systemrule" VALUES (2, '表行数变化', '相比上一周期，表行数变化', 0, '行');
INSERT INTO "public"."systemrule" VALUES (1, '表大小变化率', '相比上一周期，表大小变化率', 0, '%');
INSERT INTO "public"."systemrule" VALUES (6, '字段平均值变化率', '相比上一周期，字段平均值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (4, '当前表行数', '表行数是否符合预期', 0, '行');
INSERT INTO "public"."systemrule" VALUES (5, '当前表大小', '表大小是否符合预期', 0, '字节');
INSERT INTO "public"."systemrule" VALUES (20, '字段平均值', '计算字段平均值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (21, '字段汇总值', '计算字段汇总值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (22, '字段最小值', '计算字段最小值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (23, '字段最大值
', '计算字段最大值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (7, '字段汇总值变化率', '相比上一周期，字段汇总值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (8, '字段最小值变化率', '相比上一周期，字段最小值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (9, '字段最大值变化率', '相比上一周期，字段最大值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (10, '字段唯一值个数变化率', '相比上一周期，字段唯一值个数变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (11, '字段空值个数变化率', '相比上一周期，字段空值个数变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (12, '字段重复值个数变化率', '相比上一周期，字段重复值个数变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (24, '字段唯一值个数', '计算字段唯一值个数', 1, '个');
INSERT INTO "public"."systemrule" VALUES (25, '字段空值个数', '计算字段空值个数', 1, '个');
INSERT INTO "public"."systemrule" VALUES (26, '字段重复值个数', '计算字段重复值个数', 1, '个');
INSERT INTO "public"."systemrule" VALUES (17, '字段唯一值个数变化', '相比上一周期，字段唯一值个数变化', 1, '个');
INSERT INTO "public"."systemrule" VALUES (18, '字段空值个数变化', '相比上一周期，字段空值个数变化', 1, '个');
INSERT INTO "public"."systemrule" VALUES (19, '字段重复值个数变化', '相比上一周期，字段重复值个数变化
', 1, '个');
INSERT INTO "public"."rule2buildtype" VALUES (0, 0);
INSERT INTO "public"."rule2buildtype" VALUES (1, 0);
INSERT INTO "public"."rule2buildtype" VALUES (2, 0);
INSERT INTO "public"."rule2buildtype" VALUES (3, 0);
INSERT INTO "public"."rule2buildtype" VALUES (4, 0);
INSERT INTO "public"."rule2buildtype" VALUES (5, 0);
INSERT INTO "public"."rule2buildtype" VALUES (6, 0);
INSERT INTO "public"."rule2buildtype" VALUES (7, 0);
INSERT INTO "public"."rule2buildtype" VALUES (8, 0);
INSERT INTO "public"."rule2buildtype" VALUES (9, 0);
INSERT INTO "public"."rule2buildtype" VALUES (10, 0);
INSERT INTO "public"."rule2buildtype" VALUES (11, 0);
INSERT INTO "public"."rule2buildtype" VALUES (12, 0);
INSERT INTO "public"."rule2buildtype" VALUES (13, 0);
INSERT INTO "public"."rule2buildtype" VALUES (14, 0);
INSERT INTO "public"."rule2buildtype" VALUES (15, 0);
INSERT INTO "public"."rule2buildtype" VALUES (16, 0);
INSERT INTO "public"."rule2buildtype" VALUES (17, 0);
INSERT INTO "public"."rule2buildtype" VALUES (18, 0);
INSERT INTO "public"."rule2buildtype" VALUES (19, 0);
INSERT INTO "public"."rule2buildtype" VALUES (20, 0);
INSERT INTO "public"."rule2buildtype" VALUES (21, 0);
INSERT INTO "public"."rule2buildtype" VALUES (22, 0);
INSERT INTO "public"."rule2buildtype" VALUES (23, 0);
INSERT INTO "public"."rule2buildtype" VALUES (24, 0);
INSERT INTO "public"."rule2buildtype" VALUES (25, 0);
INSERT INTO "public"."rule2buildtype" VALUES (26, 0);
INSERT INTO "public"."rule2buildtype" VALUES (27, 0);
INSERT INTO "public"."rule2buildtype" VALUES (28, 0);
INSERT INTO "public"."rule2buildtype" VALUES (29, 0);
INSERT INTO "public"."rule2buildtype" VALUES (4, 1);
INSERT INTO "public"."rule2buildtype" VALUES (5, 1);
INSERT INTO "public"."rule2buildtype" VALUES (20, 1);
INSERT INTO "public"."rule2buildtype" VALUES (21, 1);
INSERT INTO "public"."rule2buildtype" VALUES (22, 1);
INSERT INTO "public"."rule2buildtype" VALUES (23, 1);
INSERT INTO "public"."rule2buildtype" VALUES (24, 1);
INSERT INTO "public"."rule2buildtype" VALUES (25, 1);
INSERT INTO "public"."rule2buildtype" VALUES (26, 1);
INSERT INTO "public"."rule2buildtype" VALUES (27, 1);
INSERT INTO "public"."rule2buildtype" VALUES (28, 1);
INSERT INTO "public"."rule2buildtype" VALUES (29, 1);
INSERT INTO "public"."rule2checktype" VALUES (1, 1);
INSERT INTO "public"."rule2checktype" VALUES (0, 0);
INSERT INTO "public"."rule2checktype" VALUES (0, 1);
INSERT INTO "public"."rule2checktype" VALUES (1, 0);
INSERT INTO "public"."rule2checktype" VALUES (2, 0);
INSERT INTO "public"."rule2checktype" VALUES (3, 0);
INSERT INTO "public"."rule2checktype" VALUES (4, 0);
INSERT INTO "public"."rule2checktype" VALUES (5, 0);
INSERT INTO "public"."rule2checktype" VALUES (6, 0);
INSERT INTO "public"."rule2checktype" VALUES (7, 0);
INSERT INTO "public"."rule2checktype" VALUES (8, 0);
INSERT INTO "public"."rule2checktype" VALUES (9, 0);
INSERT INTO "public"."rule2checktype" VALUES (10, 0);
INSERT INTO "public"."rule2checktype" VALUES (11, 0);
INSERT INTO "public"."rule2checktype" VALUES (12, 0);
INSERT INTO "public"."rule2checktype" VALUES (13, 0);
INSERT INTO "public"."rule2checktype" VALUES (14, 0);
INSERT INTO "public"."rule2checktype" VALUES (15, 0);
INSERT INTO "public"."rule2checktype" VALUES (16, 0);
INSERT INTO "public"."rule2checktype" VALUES (17, 0);
INSERT INTO "public"."rule2checktype" VALUES (18, 0);
INSERT INTO "public"."rule2checktype" VALUES (19, 0);
INSERT INTO "public"."rule2checktype" VALUES (20, 0);
INSERT INTO "public"."rule2checktype" VALUES (21, 0);
INSERT INTO "public"."rule2checktype" VALUES (22, 0);
INSERT INTO "public"."rule2checktype" VALUES (23, 0);
INSERT INTO "public"."rule2checktype" VALUES (24, 0);
INSERT INTO "public"."rule2checktype" VALUES (25, 0);
INSERT INTO "public"."rule2checktype" VALUES (26, 0);
INSERT INTO "public"."rule2checktype" VALUES (27, 0);
INSERT INTO "public"."rule2checktype" VALUES (28, 0);
INSERT INTO "public"."rule2checktype" VALUES (29, 0);
INSERT INTO "public"."rule2checktype" VALUES (6, 1);
INSERT INTO "public"."rule2checktype" VALUES (7, 1);
INSERT INTO "public"."rule2checktype" VALUES (8, 1);
INSERT INTO "public"."rule2checktype" VALUES (9, 1);
INSERT INTO "public"."rule2checktype" VALUES (10, 1);
INSERT INTO "public"."rule2checktype" VALUES (11, 1);
INSERT INTO "public"."rule2checktype" VALUES (12, 1);
INSERT INTO "public"."rule2checktype" VALUES (27, 1);
INSERT INTO "public"."rule2checktype" VALUES (28, 1);
INSERT INTO "public"."rule2checktype" VALUES (29, 1);
INSERT INTO "public"."rule2datatype" VALUES (6, 1);
INSERT INTO "public"."rule2datatype" VALUES (7, 1);
INSERT INTO "public"."rule2datatype" VALUES (8, 1);
INSERT INTO "public"."rule2datatype" VALUES (9, 1);
INSERT INTO "public"."rule2datatype" VALUES (10, 1);
INSERT INTO "public"."rule2datatype" VALUES (11, 1);
INSERT INTO "public"."rule2datatype" VALUES (12, 1);
INSERT INTO "public"."rule2datatype" VALUES (13, 1);
INSERT INTO "public"."rule2datatype" VALUES (14, 1);
INSERT INTO "public"."rule2datatype" VALUES (15, 1);
INSERT INTO "public"."rule2datatype" VALUES (16, 1);
INSERT INTO "public"."rule2datatype" VALUES (17, 1);
INSERT INTO "public"."rule2datatype" VALUES (18, 1);
INSERT INTO "public"."rule2datatype" VALUES (19, 1);
INSERT INTO "public"."rule2datatype" VALUES (20, 1);
INSERT INTO "public"."rule2datatype" VALUES (21, 1);
INSERT INTO "public"."rule2datatype" VALUES (22, 1);
INSERT INTO "public"."rule2datatype" VALUES (23, 1);
INSERT INTO "public"."rule2datatype" VALUES (24, 1);
INSERT INTO "public"."rule2datatype" VALUES (25, 1);
INSERT INTO "public"."rule2datatype" VALUES (26, 1);
INSERT INTO "public"."rule2datatype" VALUES (27, 1);
INSERT INTO "public"."rule2datatype" VALUES (28, 1);
INSERT INTO "public"."rule2datatype" VALUES (29, 1);
INSERT INTO "public"."rule2datatype" VALUES (17, 2);
INSERT INTO "public"."rule2datatype" VALUES (18, 2);
INSERT INTO "public"."rule2datatype" VALUES (19, 2);
INSERT INTO "public"."rule2datatype" VALUES (24, 2);
INSERT INTO "public"."rule2datatype" VALUES (25, 2);
INSERT INTO "public"."rule2datatype" VALUES (26, 2);
INSERT INTO "public"."rule2datatype" VALUES (27, 2);
INSERT INTO "public"."rule2datatype" VALUES (28, 2);
INSERT INTO "public"."rule2datatype" VALUES (29, 2);
INSERT INTO "public"."rule2datatype" VALUES (10, 2);
INSERT INTO "public"."rule2datatype" VALUES (11, 2);
INSERT INTO "public"."rule2datatype" VALUES (12, 2);