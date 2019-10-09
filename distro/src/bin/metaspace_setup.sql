
-- ----------------------------
-- Table structure for data_standard
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_standard";
CREATE TABLE "public"."data_standard" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "number" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "content" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar COLLATE "pg_catalog"."default",
  "createtime" timestamptz(0),
  "updatetime" timestamptz(0),
  "operator" varchar COLLATE "pg_catalog"."default",
  "version" int4,
  "categoryid" varchar COLLATE "pg_catalog"."default",
  "delete" bool
)
;
COMMENT ON COLUMN "public"."data_standard"."number" IS '标准编号';
COMMENT ON COLUMN "public"."data_standard"."content" IS '标准内容';
COMMENT ON COLUMN "public"."data_standard"."description" IS '描述';
COMMENT ON COLUMN "public"."data_standard"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."data_standard"."updatetime" IS '修改时间';
COMMENT ON COLUMN "public"."data_standard"."operator" IS '操作人';
COMMENT ON COLUMN "public"."data_standard"."version" IS '版本';
COMMENT ON COLUMN "public"."data_standard"."categoryid" IS '所属组Id';
COMMENT ON COLUMN "public"."data_standard"."delete" IS '是否删除';

-- ----------------------------
-- Primary Key structure for table data_standard
-- ----------------------------
ALTER TABLE "public"."data_standard" ADD CONSTRAINT "data_standard_pkey" PRIMARY KEY ("id", "number");

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
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ownerid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "keeper" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Primary Key structure for table table2owner
-- ----------------------------
ALTER TABLE "public"."table2owner" ADD CONSTRAINT "table2owner_pkey" PRIMARY KEY ("tableguid", "ownerid", "pkid");

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
INSERT INTO "public"."module" VALUES (5, '业务对象管理', 1);
INSERT INTO "public"."module" VALUES (6, '权限', 1);
INSERT INTO "public"."module" VALUES (7, '元数据管理', 1);
INSERT INTO "public"."module" VALUES (1, '技术数据', 1);
INSERT INTO "public"."module" VALUES (2, '业务对象', 1);
INSERT INTO "public"."module" VALUES (3, '编辑技术信息', 0);
INSERT INTO "public"."module" VALUES (8, '管理技术目录', 0);
INSERT INTO "public"."module" VALUES (9, '管理业务目录', 0);
INSERT INTO "public"."module" VALUES (4, '编辑业务信息', 0);
INSERT INTO "public"."module" VALUES (11, '数据标准', 1);
INSERT INTO "public"."module" VALUES (12, '日志审计', 1);
INSERT INTO "public"."module" VALUES (10, '数据分享', 1);
INSERT INTO "public"."module" VALUES (13, '数据质量', 1);
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
INSERT INTO "public"."privilege2module" VALUES ('3', 13);
INSERT INTO "public"."privilege2module" VALUES ('1', 13);
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


-- ----------------------------
-- Table structure for data_quality_rule_template
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_rule_template";
CREATE TABLE "public"."data_quality_rule_template" (
  "name" varchar COLLATE "pg_catalog"."default",
  "type" int4,
  "scope" int4,
  "unit" varchar COLLATE "pg_catalog"."default",
  "description" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "delete" bool,
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "category_id" varchar COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of data_quality_rule_template
-- ----------------------------
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段汇总值变化', 14, 1, '', '相比上一周期，字段汇总值变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '20', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最小值变化', 15, 1, '', '相比上一周期，字段最小值变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '21', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('
字段最大值变化', 16, 1, '', '相比上一周期，字段最大值变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '22', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段平均值变化率', 6, 1, '%', '相比上一周期，字段平均值变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '23', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段平均值', 20, 1, NULL, '计算字段平均值', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '24', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段汇总值', 21, 1, NULL, '计算字段汇总值', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '25', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('表大小变化', 3, 0, '字节', '相比上一周期，表大小变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '1', '1');
INSERT INTO "public"."data_quality_rule_template" VALUES ('表行数变化率', 0, 0, '%', '相比上一周期，表行数变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '2', '1');
INSERT INTO "public"."data_quality_rule_template" VALUES ('表行数变化', 2, 0, '行', '相比上一周期，表行数变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '3', '1');
INSERT INTO "public"."data_quality_rule_template" VALUES ('表大小变化率', 1, 0, '%', '相比上一周期，表大小变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '4', '1');
INSERT INTO "public"."data_quality_rule_template" VALUES ('当前表行数', 4, 0, '行', '表行数是否符合预期', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '5', '1');
INSERT INTO "public"."data_quality_rule_template" VALUES ('当前表大小', 5, 0, '字节', '表大小是否符合预期', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '6', '1');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数/总行数', 28, 1, '%', '计算字段空值行数所占的比例', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '7', '2');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数变化率', 11, 1, '%', '相比上一周期，字段空值个数变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '8', '2');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数', 25, 1, '个', '计算字段空值个数', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '9', '2');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数变化', 18, 1, '个', '相比上一周期，字段空值个数变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '10', '2');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数/总行数', 27, 1, '%', '计算字段唯一值行数所占的比例', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '11', '3');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数变化率', 10, 1, '%', '相比上一周期，字段唯一值个数变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '12', '3');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数', 24, 1, '个', '计算字段唯一值个数', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '13', '3');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数变化', 17, 1, '个', '相比上一周期，字段唯一值个数变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '14', '3');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数/总行数', 29, 1, '%', '计算字段重复值行数所占的比例', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '15', '4');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数变化率', 12, 1, '%', '相比上一周期，字段重复值个数变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '16', '4');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数', 26, 1, '个', '计算字段重复值个数', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '17', '4');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数变化', 19, 1, '个', '相比上一周期，字段重复值个数变化
', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '18', '4');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段平均值变化', 13, 1, '', '相比上一周期，字段平均值变化', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '19', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最小值', 22, 1, NULL, '计算字段最小值', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '26', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最大值
', 23, 1, NULL, '计算字段最大值', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '27', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段汇总值变化率', 7, 1, '%', '相比上一周期，字段汇总值变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '28', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最小值变化率', 8, 1, '%', '相比上一周期，字段最小值变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '29', '5');
INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最大值变化率', 9, 1, '%', '相比上一周期，字段最大值变化率', '2019-07-23 10:15:21+08', '2019-07-23 10:15:21+08', 'f', '30', '5');

-- ----------------------------
-- Primary Key structure for table data_quality_rule_template
-- ----------------------------
ALTER TABLE "public"."data_quality_rule_template" ADD CONSTRAINT "data_quality_rule_template_pkey" PRIMARY KEY ("id");


-- ----------------------------
-- Table structure for data_quality_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_rule";
CREATE TABLE "public"."data_quality_rule" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "rule_template_id" varchar COLLATE "pg_catalog"."default",
  "name" varchar COLLATE "pg_catalog"."default",
  "code" varchar COLLATE "pg_catalog"."default",
  "category_id" varchar COLLATE "pg_catalog"."default",
  "enable" bool,
  "description" varchar COLLATE "pg_catalog"."default",
  "check_type" int4,
  "check_expression_type" int4,
  "creator" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "delete" bool,
  "check_threshold_min_value" float8,
  "check_threshold_max_value" float8,
  "scope" int2,
  "check_threshold_unit" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."data_quality_rule"."rule_template_id" IS '规则模版id';
COMMENT ON COLUMN "public"."data_quality_rule"."code" IS '规则编码';
COMMENT ON COLUMN "public"."data_quality_rule"."category_id" IS '分组id';
COMMENT ON COLUMN "public"."data_quality_rule"."enable" IS '是否开启';
COMMENT ON COLUMN "public"."data_quality_rule"."description" IS '描述';
COMMENT ON COLUMN "public"."data_quality_rule"."check_type" IS '0-固定值,1-波动值';
COMMENT ON COLUMN "public"."data_quality_rule"."check_expression_type" IS '校验表达式的类型,>=、=等的代码值';
COMMENT ON COLUMN "public"."data_quality_rule"."check_threshold_min_value" IS '校验阈值最小值';
COMMENT ON COLUMN "public"."data_quality_rule"."check_threshold_max_value" IS '校验阈值最大值';
COMMENT ON COLUMN "public"."data_quality_rule"."scope" IS '作用域';
COMMENT ON COLUMN "public"."data_quality_rule"."check_threshold_unit" IS '单位';


-- ----------------------------
-- Table structure for data_quality_sub_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_sub_task";
CREATE TABLE "public"."data_quality_sub_task" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar COLLATE "pg_catalog"."default",
  "datasource_type" int4,
  "sequence" int4,
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "delete" bool
)
;
COMMENT ON COLUMN "public"."data_quality_sub_task"."task_id" IS '所属任务id';
COMMENT ON COLUMN "public"."data_quality_sub_task"."datasource_type" IS '数据源类型:1-表,2-字段';
COMMENT ON COLUMN "public"."data_quality_sub_task"."sequence" IS '子任务顺序';
COMMENT ON COLUMN "public"."data_quality_sub_task"."delete" IS '是否删除';

-- ----------------------------
-- Table structure for data_quality_sub_task_object
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_sub_task_object";
CREATE TABLE "public"."data_quality_sub_task_object" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "subtask_id" varchar COLLATE "pg_catalog"."default",
  "object_id" varchar COLLATE "pg_catalog"."default",
  "sequence" int4,
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "delete" bool,
  "task_id" varchar COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."subtask_id" IS '所属子任务id';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."object_id" IS '表或字段id';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."sequence" IS '数据源顺序';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."task_id" IS '所属任务id';


-- ----------------------------
-- Table structure for data_quality_sub_task_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_sub_task_rule";
CREATE TABLE "public"."data_quality_sub_task_rule" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "subtask_id" varchar COLLATE "pg_catalog"."default",
  "ruleid" varchar COLLATE "pg_catalog"."default",
  "check_threshold_min_value" float8,
  "orange_check_type" int4,
  "orange_check_expression_type" int4,
  "red_check_type" int4,
  "red_check_expression_type" int4,
  "red_warning_groupid" varchar COLLATE "pg_catalog"."default",
  "sequence" int4,
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "delete" bool,
  "orange_threshold_min_value" float8,
  "orange_threshold_max_value" float8,
  "red_threshold_min_value" float8,
  "red_threshold_max_value" float8,
  "check_threshold_max_value" float8,
  "check_type" int4,
  "check_expression_type" int4,
  "check_threshold_unit" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."subtask_id" IS '所属子任务id';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."ruleid" IS '规则id';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_threshold_min_value" IS '校验阈值最小值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_check_type" IS '橙色校验类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_check_expression_type" IS '橙色校验表达式类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."sequence" IS '规则顺序';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_threshold_min_value" IS ' 橙色告警最小阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_threshold_max_value" IS '橙色告警最大阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."red_threshold_min_value" IS '红色告警最小阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."red_threshold_max_value" IS '红色告警最大阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_threshold_max_value" IS '校验阈值最大值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_threshold_unit" IS '单位';


-- ----------------------------
-- Table structure for data_quality_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_task";
CREATE TABLE "public"."data_quality_task" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "level" int4,
  "description" varchar COLLATE "pg_catalog"."default",
  "cron_expression" varchar COLLATE "pg_catalog"."default",
  "enable" bool,
  "start_time" timestamptz(0),
  "end_time" timestamptz(0),
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "creator" varchar COLLATE "pg_catalog"."default",
  "delete" bool,
  "number" int8 NOT NULL,
  "qrtz_job" varchar(255) COLLATE "pg_catalog"."default",
  "execution_count" int8,
  "orange_warning_total_count" int8,
  "red_warning_total_count" int8,
  "error_total_count" int8,
  "updater" varchar COLLATE "pg_catalog"."default",
  "current_execution_percent" float4,
  "current_execution_status" int2
)
;
COMMENT ON COLUMN "public"."data_quality_task"."name" IS '任务名';
COMMENT ON COLUMN "public"."data_quality_task"."level" IS '任务级别:1-普通,2-重要,3-非常重要';
COMMENT ON COLUMN "public"."data_quality_task"."description" IS '任务描述';
COMMENT ON COLUMN "public"."data_quality_task"."cron_expression" IS 'cron表达式';
COMMENT ON COLUMN "public"."data_quality_task"."enable" IS '是否启用';
COMMENT ON COLUMN "public"."data_quality_task"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."data_quality_task"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."data_quality_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_task"."creator" IS '创建人';
COMMENT ON COLUMN "public"."data_quality_task"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_task"."number" IS '任务ID';
COMMENT ON COLUMN "public"."data_quality_task"."qrtz_job" IS 'jobName';
COMMENT ON COLUMN "public"."data_quality_task"."execution_count" IS '执行次数';
COMMENT ON COLUMN "public"."data_quality_task"."orange_warning_total_count" IS '橙色告警次数统计';
COMMENT ON COLUMN "public"."data_quality_task"."red_warning_total_count" IS '红色告警次数统计';
COMMENT ON COLUMN "public"."data_quality_task"."error_total_count" IS '执行失败统计次数';
COMMENT ON COLUMN "public"."data_quality_task"."updater" IS '修改人';

-- ----------------------------
-- Primary Key structure for table data_quality_task
-- ----------------------------
ALTER TABLE "public"."data_quality_task" ADD CONSTRAINT "data_quality_task_pkey" PRIMARY KEY ("id");


CREATE SEQUENCE number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE "public"."data_quality_task" ALTER COLUMN number set default nextval('number_seq');


-- ----------------------------
-- Table structure for data_quality_task_execute
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_task_execute";
CREATE TABLE "public"."data_quality_task_execute" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar COLLATE "pg_catalog"."default",
  "percent" float8,
  "execute_status" int4,
  "executor" varchar COLLATE "pg_catalog"."default",
  "error_msg" text COLLATE "pg_catalog"."default",
  "execute_time" timestamptz(0),
  "closer" varchar COLLATE "pg_catalog"."default",
  "close_time" timestamptz(0),
  "cost_time" int8,
  "orange_warning_count" int4,
  "red_warning_count" int4,
  "rule_error_count" int4,
  "warning_status" int2,
  "number" varchar COLLATE "pg_catalog"."default",
  "counter" int8,
  "error_status" int2
)
;
COMMENT ON COLUMN "public"."data_quality_task_execute"."task_id" IS '所属任务id';
COMMENT ON COLUMN "public"."data_quality_task_execute"."percent" IS '执行进度';
COMMENT ON COLUMN "public"."data_quality_task_execute"."execute_status" IS '执行状态:1-执行中,2-成功,3-失败,0-未执行';
COMMENT ON COLUMN "public"."data_quality_task_execute"."executor" IS '执行者id';
COMMENT ON COLUMN "public"."data_quality_task_execute"."error_msg" IS '程序执行错误日志';
COMMENT ON COLUMN "public"."data_quality_task_execute"."closer" IS '告警处理人';
COMMENT ON COLUMN "public"."data_quality_task_execute"."close_time" IS '告警处理时间';
COMMENT ON COLUMN "public"."data_quality_task_execute"."cost_time" IS '执行耗时';
COMMENT ON COLUMN "public"."data_quality_task_execute"."orange_warning_count" IS '橙色告警数';
COMMENT ON COLUMN "public"."data_quality_task_execute"."red_warning_count" IS '红色告警数';
COMMENT ON COLUMN "public"."data_quality_task_execute"."rule_error_count" IS '规则异常数';
COMMENT ON COLUMN "public"."data_quality_task_execute"."warning_status" IS '告警状态: 状态:0-无告警,1-告警中,2-告警已关闭';
COMMENT ON COLUMN "public"."data_quality_task_execute"."number" IS '编号';
COMMENT ON COLUMN "public"."data_quality_task_execute"."counter" IS '任务执行次数';
COMMENT ON COLUMN "public"."data_quality_task_execute"."error_status" IS '告警状态: 状态:0-无告警,1-告警中,2-告警已关闭';

-- ----------------------------
-- Table structure for data_quality_task_rule_execute
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_task_rule_execute";
CREATE TABLE "public"."data_quality_task_rule_execute" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "task_execute_id" varchar COLLATE "pg_catalog"."default",
  "task_id" varchar COLLATE "pg_catalog"."default",
  "subtask_id" varchar COLLATE "pg_catalog"."default",
  "subtask_object_id" varchar COLLATE "pg_catalog"."default",
  "subtask_rule_id" varchar COLLATE "pg_catalog"."default",
  "result" float8,
  "check_status" int4,
  "waring_send_status" int2,
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "reference_value" float8,
  "orange_warning_check_status" int2,
  "red_warning_check_status" int2,
  "error_msg" varchar(255) COLLATE "pg_catalog"."default",
  "warning_status" int2,
  "error_status" int2,
  "rule_id" varchar(32) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."task_id" IS '所属任务id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."subtask_id" IS '所属子任务id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."subtask_object_id" IS '所属子任务对象id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."subtask_rule_id" IS '所属子任务规则id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."result" IS '规则执行结果(sql直接结果)';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."check_status" IS '0-合格,1-不合格,2-失败';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."waring_send_status" IS '告警状态:1-待发送,2发送中,3发送成功,4发送失败';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."reference_value" IS '计算变化值/变化率中计算值存储';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."orange_warning_check_status" IS '0-无告警,1-有告警';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."red_warning_check_status" IS '0-无告警,1-有告警';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."error_msg" IS '错误信息';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."warning_status" IS '告警状态: 状态:0-无告警,1-告警中,2-告警已关闭';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."error_status" IS '告警状态: 状态:0-无告警,1-告警中,2-告警已关闭';


-- ----------------------------
-- Table structure for data_quality_task2warning_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_task2warning_group";
CREATE TABLE "public"."data_quality_task2warning_group" (
  "task_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "warning_group_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "warning_type" int2 NOT NULL
)
;

-- ----------------------------
-- Primary Key structure for table data_quality_task2warning_group
-- ----------------------------
ALTER TABLE "public"."data_quality_task2warning_group" ADD CONSTRAINT "data_quality_task2warning_group_pkey" PRIMARY KEY ("task_id", "warning_group_id", "warning_type");


-- ----------------------------
-- Table structure for warning_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."warning_group";
CREATE TABLE "public"."warning_group" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "type" int4,
  "contacts" varchar COLLATE "pg_catalog"."default",
  "category_id" varchar COLLATE "pg_catalog"."default",
  "description" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "creator" varchar COLLATE "pg_catalog"."default",
  "delete" bool
)
;
COMMENT ON COLUMN "public"."warning_group"."name" IS '告警组名称';
COMMENT ON COLUMN "public"."warning_group"."type" IS '告警类型:0-系统,1-邮件,2-短信,';
COMMENT ON COLUMN "public"."warning_group"."contacts" IS '联系人';
COMMENT ON COLUMN "public"."warning_group"."category_id" IS '告警组id，共用规则分组';
COMMENT ON COLUMN "public"."warning_group"."description" IS '描述';
COMMENT ON COLUMN "public"."warning_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."warning_group"."creator" IS '创建者id';


-- ----------------------------
-- Table structure for api_module
-- ----------------------------
DROP TABLE IF EXISTS "public"."api_module";
CREATE TABLE "public"."api_module" (
  "path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "method" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "module_id" int2,
  "prefix_check" bool
)
;

-- ----------------------------
-- Records of api_module
-- ----------------------------
INSERT INTO "public"."api_module" VALUES ('/businesses/categories/{categoryGuid}', 'DELETE', 9, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/{businessId}', 'DELETE', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/{businessId}', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/{businessId}/technical', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/datashare/{apiGuid}', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/categories', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/table/{guid}', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/excel/{tableGuid}/template', 'GET', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/category/{categoryId}', 'POST', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/relations', 'POST', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/category/relations/{categoryId}', 'POST', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/{businessId}/datashare', 'POST', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/datashare/test/{randomName}', 'POST', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/categories', 'POST', 9, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/{businessId}/tables', 'POST', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/table/{guid}/columns', 'POST', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/excel/import/{guid}', 'POST', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/{businessId}', 'PUT', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/datashare/test/{randomName}', 'PUT', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/{businessId}/technical', 'PUT', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/categories/{categoryId}', 'PUT', 9, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/table/{guid}/columns', 'PUT', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/businesses/table', 'PUT', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('datashare', 'OPTION', 10, 't');
INSERT INTO "public"."api_module" VALUES ('role', 'OPTION', 6, 't');
INSERT INTO "public"."api_module" VALUES ('/technical/category', 'GET', 1, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage', 'POST', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/search/database/{categoryId}', 'POST', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/search/database/table/{databaseGuid}/{categoryId}', 'POST', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/search/table/{categoryId}', 'POST', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category', 'POST', 8, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/update/category', 'POST', 8, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category/{categoryGuid}/assignedEntities', 'POST', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category/relations/{categoryGuid}', 'POST', 1, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/table/relations', 'POST', 1, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/owner/table', 'POST', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/organization/{pId}', 'POST', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/organization', 'POST', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}', 'PUT', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}/business', 'PUT', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/organization', 'PUT', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category/{categoryGuid}', 'DELETE', 8, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category/relation', 'DELETE', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('privilege', 'OPTION', 6, 't');
INSERT INTO "public"."api_module" VALUES ('datastandard', 'OPTION', 11, 't');
INSERT INTO "public"."api_module" VALUES ('operatelog', 'OPTION', 12, 't');
INSERT INTO "public"."api_module" VALUES ('/businessManage/datashare/test/{randomName}', 'PUT', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}/datashare', 'POST', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/datashare/test/{randomName}', 'POST', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/datashare/{apiGuid}', 'GET', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}', 'GET', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}/technical', 'GET', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/table/{guid}', 'GET', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('dataquality', 'OPTION', 13, 't');

-- ----------------------------
-- Primary Key structure for table api_module
-- ----------------------------
ALTER TABLE "public"."api_module" ADD CONSTRAINT "api_module_pkey" PRIMARY KEY ("path", "method");
