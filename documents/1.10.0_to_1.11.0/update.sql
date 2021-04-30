
-- ----------------------------
-- Table structure for data_quality_rule_template
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_quality_rule_template";
CREATE TABLE "public"."data_quality_rule_template" (
  "name" varchar COLLATE "pg_catalog"."default",
  "scope" int4,
  "unit" varchar COLLATE "pg_catalog"."default",
  "description" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(0),
  "update_time" timestamptz(0),
  "delete" bool,
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "rule_type" varchar(36) COLLATE "pg_catalog"."default",
  "type" int8,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "creator" varchar(255) COLLATE "pg_catalog"."default",
  "sql" text COLLATE "pg_catalog"."default",
  "enable" varchar(255) COLLATE "pg_catalog"."default",
  "code" varchar COLLATE "pg_catalog"."default",
  CONSTRAINT "data_quality_rule_template_pkey" PRIMARY KEY ("id", "tenantid")
)
;

COMMENT ON COLUMN "public"."data_quality_rule_template"."name" IS '名字';

COMMENT ON COLUMN "public"."data_quality_rule_template"."scope" IS '作用域';

COMMENT ON COLUMN "public"."data_quality_rule_template"."unit" IS '返回值';

COMMENT ON COLUMN "public"."data_quality_rule_template"."description" IS '描述';

COMMENT ON COLUMN "public"."data_quality_rule_template"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."data_quality_rule_template"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."data_quality_rule_template"."delete" IS '状态';

COMMENT ON COLUMN "public"."data_quality_rule_template"."id" IS 'id';

COMMENT ON COLUMN "public"."data_quality_rule_template"."rule_type" IS '类型';

COMMENT ON COLUMN "public"."data_quality_rule_template"."type" IS '类型';

COMMENT ON COLUMN "public"."data_quality_rule_template"."tenantid" IS '租户id';

COMMENT ON COLUMN "public"."data_quality_rule_template"."creator" IS '创建者';

COMMENT ON COLUMN "public"."data_quality_rule_template"."sql" IS '自定义规则的sql语句';

COMMENT ON COLUMN "public"."data_quality_rule_template"."enable" IS '规则状态';

COMMENT ON COLUMN "public"."data_quality_rule_template"."code" IS '规则编码';
-- ----------------------------
-- Records of data_quality_rule_template
-- ----------------------------
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段汇总值变化', 1, '', '相比上一周期，字段汇总值变化', current_timestamp, current_timestamp, 'f', '20', 'rule_5', 14,'all',null,'',true,'20');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最小值变化', 1, '', '相比上一周期，字段最小值变化', current_timestamp, current_timestamp, 'f', '21', 'rule_5', 15,'all',null,'',true,'21');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最大值变化', 1, '', '相比上一周期，字段最大值变化', current_timestamp, current_timestamp, 'f', '22', 5, 16,null,'',true,'22');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段平均值变化率', 1, '%', '相比上一周期，字段平均值变化率', current_timestamp, current_timestamp, 'f', '23', 'rule_5', 6,'all',null,'',true,'23');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段平均值', 1, NULL, '计算字段平均值', current_timestamp, current_timestamp, 'f', '24', 'rule_5', 20,'all',null,'',true,'24');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段汇总值', 1, NULL, '计算字段汇总值', current_timestamp, current_timestamp, 'f', '25', 'rule_5', 21,'all',null,'',true,'25');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('表大小变化', 0, '字节', '相比上一周期，表大小变化', current_timestamp, current_timestamp, 'f', '1', 'rule_1', 3,'all',null,'',true,'1');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('表行数变化率', 0, '%', '相比上一周期，表行数变化率', current_timestamp, current_timestamp, 'f', '2', 'rule_1', 'all',null,'',true,'2');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('表行数变化', 0, '行', '相比上一周期，表行数变化', current_timestamp, current_timestamp, 'f', '3', 'rule_1', 2,'all',null,'',true,'3');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('表大小变化率', 0, '%', '相比上一周期，表大小变化率', current_timestamp, current_timestamp, 'f', '4', 'rule_1', 1,'all',null,'',true,'4');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('当前表行数', 0, '行', '表行数是否符合预期', current_timestamp, current_timestamp, 'f', '5', 'rule_1', 4,'all',null,'',true,'5');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('当前表大小', 0, '字节', '表大小是否符合预期', current_timestamp, current_timestamp, 'f', '6', 'rule_1', 5,'all',null,'',true,'6');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数/总行数', 1, '%', '计算字段空值行数所占的比例', current_timestamp, current_timestamp, 'f', '7', 'rule_2', 28,'all',null,'',true,'7');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数变化率', 1, '%', '相比上一周期，字段空值个数变化率', current_timestamp, current_timestamp, 'f', '8', 'rule_2', 11,'all',null,'',true,'8');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数', 1, '个', '计算字段空值个数', current_timestamp, current_timestamp, 'f', '9', 'rule_2', 25,'all',null,'',true,'9');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数/总行数', 1, '%', '计算字段重复值行数所占的比例', current_timestamp, current_timestamp, 'f', '15', 'rule_4', 29,'all',null,'',true,'15');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数变化率', 1, '%', '相比上一周期，字段重复值个数变化率', current_timestamp, current_timestamp, 'f', '16', 'rule_4', 12,'all',null,'',true,'16');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段空值个数变化', 1, '个', '相比上一周期，字段空值个数变化', current_timestamp, current_timestamp, 'f', '10', 'rule_2', 18,'all',null,'',true,'10');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数/总行数', 1, '%', '计算字段唯一值行数所占的比例', current_timestamp, current_timestamp, 'f', '11', 'rule_3', 27,'all',null,'',true,'11');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数变化率', 1, '%', '相比上一周期，字段唯一值个数变化率', current_timestamp, current_timestamp, 'f', '12', 'rule_3', 10,'all',null,'',true,'12');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数', 1, '个', '计算字段重复值个数', current_timestamp, current_timestamp, 'f', '17', 'rule_4', 26,'all',null,'',true,'17');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数', 1, '个', '计算字段唯一值个数', current_timestamp, current_timestamp, 'f', '13', 'rule_3', 24,'all',null,'',true,'13');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段唯一值个数变化', 1, '个', '相比上一周期，字段唯一值个数变化', current_timestamp, current_timestamp, 'f', '14', 'rule_3', 17,'all',null,'',true,'14');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段重复值个数变化', 1, '个', '相比上一周期，字段重复值个数变化', current_timestamp, current_timestamp, 'f', '18', 'rule_4', 19,'all',null,'',true,'18');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段平均值变化', 1, '', '相比上一周期，字段平均值变化', current_timestamp, current_timestamp, 'f', '19', 'rule_5', 13,'all',null,'',true,'19');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最小值', 1, NULL, '计算字段最小值', current_timestamp, current_timestamp, 'f', '26', 'rule_5', 22,'all',null,'',true,'26');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最大值', 1, NULL, '计算字段最大值', current_timestamp, current_timestamp, 'f', '27', 'rule_5', 23,'all',null,'',true,'27');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段汇总值变化率', 1, '%', '相比上一周期，字段汇总值变化率', current_timestamp, current_timestamp, 'f', '28', 'rule_5', 7,'all',null,'',true,'28');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最小值变化率', 1, '%', '相比上一周期，字段最小值变化率', current_timestamp, current_timestamp, 'f', '29', 'rule_5', 8,'all',null,'',true,'29');
--INSERT INTO "public"."data_quality_rule_template" VALUES ('字段最大值变化率', 1, '%', '相比上一周期，字段最大值变化率', current_timestamp, current_timestamp, 'f', '30', 'rule_5', 9,'all',null,'',true,'30');


-- ----------------------------
-- Primary Key structure for table data_quality_rule_template
-- ----------------------------
--ALTER TABLE "public"."data_quality_rule_template" ADD CONSTRAINT "data_quality_rule_template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Table structure for operate_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."operate_log";
CREATE TABLE "public"."operate_log" (
  "id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "number" varchar(20) COLLATE "pg_catalog"."default",
  "userid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(20) COLLATE "pg_catalog"."default",
  "module" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "content" varchar COLLATE "pg_catalog"."default",
  "result" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "ip" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(0) NOT NULL,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."operate_log"."number" IS '日志序号';
COMMENT ON COLUMN "public"."operate_log"."userid" IS '用户id';
COMMENT ON COLUMN "public"."operate_log"."type" IS '操作类型';
COMMENT ON COLUMN "public"."operate_log"."module" IS '功能模块';
COMMENT ON COLUMN "public"."operate_log"."content" IS '操作内容';
COMMENT ON COLUMN "public"."operate_log"."result" IS '操作结果';
COMMENT ON COLUMN "public"."operate_log"."ip" IS '客户端ip地址';
COMMENT ON COLUMN "public"."operate_log"."createtime" IS '记录时间';

-- ----------------------------
-- Primary Key structure for table operate_log
-- ----------------------------
ALTER TABLE "public"."operate_log" ADD CONSTRAINT "operate_log_pkey" PRIMARY KEY ("id");

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
  "delete" bool,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
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
  "checked" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "disable" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "id" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "isopen" bool DEFAULT NULL,
  "isvm" int8 DEFAULT NULL,
  "name" text COLLATE "pg_catalog"."default" DEFAULT NULL,
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
  "updatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

COMMENT ON COLUMN "public"."apigroup"."guid" IS '分组id';
COMMENT ON COLUMN "public"."apigroup"."name" IS '分组名字';
COMMENT ON COLUMN "public"."apigroup"."parentguid" IS '父目录id';
COMMENT ON COLUMN "public"."apigroup"."description" IS '描述';
COMMENT ON COLUMN "public"."apigroup"."generator" IS '创建者';
COMMENT ON COLUMN "public"."apigroup"."generatetime" IS '创建时间';
COMMENT ON COLUMN "public"."apigroup"."updater" IS '更新人';
COMMENT ON COLUMN "public"."apigroup"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."apigroup"."tenantid" IS '租户id';

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
  "publish" bool DEFAULT NULL,
  "used_count" int8,
  "manager" varchar(255) COLLATE "pg_catalog"."default",
  "desensitize" bool DEFAULT FALSE ,
  "sourcetype" varchar(255) COLLATE "pg_catalog"."default",
  "schemaname" varchar(255) COLLATE "pg_catalog"."default",
  "tablename" varchar(255) COLLATE "pg_catalog"."default",
  "dbname" varchar(255) COLLATE "pg_catalog"."default",
  "sourceid" varchar(255) COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  "pool" varchar(255) COLLATE "pg_catalog"."default"
)
;

COMMENT ON COLUMN "public"."apiinfo"."guid" IS 'apiid';
COMMENT ON COLUMN "public"."apiinfo"."name" IS 'api名字';
COMMENT ON COLUMN "public"."apiinfo"."tableguid" IS '表id';
COMMENT ON COLUMN "public"."apiinfo"."dbguid" IS '库id';
COMMENT ON COLUMN "public"."apiinfo"."keeper" IS '创建人';
COMMENT ON COLUMN "public"."apiinfo"."maxrownumber" IS '最大行数';
COMMENT ON COLUMN "public"."apiinfo"."fields" IS '返回的字段';
COMMENT ON COLUMN "public"."apiinfo"."version" IS '版本';
COMMENT ON COLUMN "public"."apiinfo"."description" IS '描述';
COMMENT ON COLUMN "public"."apiinfo"."protocol" IS '请求类型';
COMMENT ON COLUMN "public"."apiinfo"."requestmode" IS '请求方式';
COMMENT ON COLUMN "public"."apiinfo"."returntype" IS '返回类型';
COMMENT ON COLUMN "public"."apiinfo"."path" IS '地址';
COMMENT ON COLUMN "public"."apiinfo"."generatetime" IS '创建时间';
COMMENT ON COLUMN "public"."apiinfo"."updater" IS '更新人';
COMMENT ON COLUMN "public"."apiinfo"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."apiinfo"."groupguid" IS '分组id';
COMMENT ON COLUMN "public"."apiinfo"."publish" IS '发布';
COMMENT ON COLUMN "public"."apiinfo"."used_count" IS '使用count';
COMMENT ON COLUMN "public"."apiinfo"."manager" IS '管理者';
COMMENT ON COLUMN "public"."apiinfo"."sourcetype" IS '数据源类型';
COMMENT ON COLUMN "public"."apiinfo"."schemaname" IS 'schema名字';
COMMENT ON COLUMN "public"."apiinfo"."tablename" IS '表名字';
COMMENT ON COLUMN "public"."apiinfo"."dbname" IS '库名字';
COMMENT ON COLUMN "public"."apiinfo"."sourceid" IS '数据源id';
COMMENT ON COLUMN "public"."apiinfo"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."apiinfo"."pool" IS '资源池';

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
  "trusttable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
)
;

COMMENT ON COLUMN "public"."businessinfo"."businessid" IS 'id';
COMMENT ON COLUMN "public"."businessinfo"."departmentid" IS '目录id';
COMMENT ON COLUMN "public"."businessinfo"."name" IS '名字';
COMMENT ON COLUMN "public"."businessinfo"."module" IS '业务模块';
COMMENT ON COLUMN "public"."businessinfo"."description" IS '描述';
COMMENT ON COLUMN "public"."businessinfo"."owner" IS '所有者';
COMMENT ON COLUMN "public"."businessinfo"."manager" IS '管理者';
COMMENT ON COLUMN "public"."businessinfo"."maintainer" IS '维护者';
COMMENT ON COLUMN "public"."businessinfo"."dataassets" IS '相关数据资产';
COMMENT ON COLUMN "public"."businessinfo"."businesslastupdate" IS '更新时间';
COMMENT ON COLUMN "public"."businessinfo"."businessoperator" IS '更新人';
COMMENT ON COLUMN "public"."businessinfo"."technicallastupdate" IS '更新时间';
COMMENT ON COLUMN "public"."businessinfo"."technicaloperator" IS '技术更新人';
COMMENT ON COLUMN "public"."businessinfo"."technicalstatus" IS '技术状态';
COMMENT ON COLUMN "public"."businessinfo"."businessstatus" IS '业务状态';
COMMENT ON COLUMN "public"."businessinfo"."submitter" IS '创建人';
COMMENT ON COLUMN "public"."businessinfo"."submissiontime" IS '创建时间';
COMMENT ON COLUMN "public"."businessinfo"."level2categoryid" IS '二级目录';
COMMENT ON COLUMN "public"."businessinfo"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."businessinfo"."ticketnumber" IS '业务对象标识，使用的是创建时间';
COMMENT ON COLUMN "public"."businessinfo"."trusttable" IS '唯一信任表id';

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
  "level" int2 DEFAULT NULL,
  "safe" varchar(225) COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  "createtime" timestamptz(6)
)
;

COMMENT ON COLUMN "public"."category"."guid" IS '目录id';
COMMENT ON COLUMN "public"."category"."description" IS '目录描述';
COMMENT ON COLUMN "public"."category"."name" IS '目录名字';
COMMENT ON COLUMN "public"."category"."upbrothercategoryguid" IS '同级上层目录';
COMMENT ON COLUMN "public"."category"."downbrothercategoryguid" IS '统计下层目录';
COMMENT ON COLUMN "public"."category"."parentcategoryguid" IS '父目录';
COMMENT ON COLUMN "public"."category"."categorytype" IS '类型';
COMMENT ON COLUMN "public"."category"."level" IS '级别';
COMMENT ON COLUMN "public"."category"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."category"."createtime" IS '创建时间';

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
  "statisticaltypeid" int4 DEFAULT NULL,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
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
  "display_operator" varchar COLLATE "pg_catalog"."default",
  "description" varchar COLLATE "pg_catalog"."default"
)
;

COMMENT ON COLUMN "public"."tableinfo"."tableguid" IS '表id';
COMMENT ON COLUMN "public"."tableinfo"."tablename" IS '表名字';
COMMENT ON COLUMN "public"."tableinfo"."dbname" IS '库名字';
COMMENT ON COLUMN "public"."tableinfo"."status" IS '状态';
COMMENT ON COLUMN "public"."tableinfo"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."tableinfo"."dataowner" IS 'owner';
COMMENT ON COLUMN "public"."tableinfo"."databaseguid" IS '库id';
COMMENT ON COLUMN "public"."tableinfo"."databasestatus" IS '库状态';
COMMENT ON COLUMN "public"."tableinfo"."subordinatesystem" IS '所属系统';
COMMENT ON COLUMN "public"."tableinfo"."subordinatedatabase" IS '所属数据库';
COMMENT ON COLUMN "public"."tableinfo"."systemadmin" IS '源系统管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehouseadmin" IS '数仓管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehousedescription" IS '数仓描述';
COMMENT ON COLUMN "public"."tableinfo"."catalogadmin" IS '目录管理员';
COMMENT ON COLUMN "public"."tableinfo"."description" IS '表描述';

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
  "type" varchar COLLATE "pg_catalog"."default",
  "description" varchar COLLATE "pg_catalog"."default"
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
COMMENT ON COLUMN "public"."column_info"."description" IS '字段描述';

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."tag";
CREATE TABLE "public"."tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "tagname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
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
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "valid" bool
)
;
COMMENT ON COLUMN "public"."users"."userid" IS '用户id';
COMMENT ON COLUMN "public"."users"."username" IS '用户名';
COMMENT ON COLUMN "public"."users"."account" IS '用户账号';

INSERT INTO "public"."users"("userid","username","account","create_time","update_time","valid") VALUES ('1', 'admin', 'admin', current_timestamp, current_timestamp, 't');


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
ALTER TABLE "public"."category" ADD CONSTRAINT "table_catalog_pkey" PRIMARY KEY ("guid", "tenantid");

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

--INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level,safe,tenantid) VALUES('1','贴源层',NUll,'2',0,1,'1','all');
--INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level,safe,tenantid) VALUES('2','基础层','1','3',0,1,'1','all');
--INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level,safe,tenantid) VALUES('3','规范层','2','4',0,1,'1','all');
--INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level,safe,tenantid) VALUES('4','通过层','3','5',0,1,'1','all');
--INSERT INTO category(guid,name,upbrothercategoryguid,downbrothercategoryguid,categorytype,level,safe,tenantid) VALUES('5','应用层','4',NULL,0,1,'1','all');
--
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-1', '基础类数据标准','基础类数据标准',null,null,'Standard-2',1,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-2', '指标类数据标准','指标类数据标准',null,'Standard-1',null,1,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-3', '参考数据标准','参考数据标准','Standard-1',null,'Standard-4',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-4', '主数据标准','主数据标准','Standard-1','Standard-3','Standard-5',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-5', '逻辑数据模型标准','逻辑数据模型标准','Standard-1','Standard-4','Standard-6',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-6', '物理数据模型标准','物理数据模型标准','Standard-1','Standard-5','Standard-7',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-7', '元数据标准','元数据标准','Standard-1','Standard-6','Standard-8',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-8', '公共代码标准','公共代码标准','Standard-1','Standard-7','Standard-9',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-9', '编码标准','编码标准','Standard-1','Standard-8',null,2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-10', '基础指标标准','基础指标标准','Standard-2',null,'Standard-11',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-11', '计算指标标准','计算指标标准','Standard-2','Standard-10',null,2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-12', '业务元数据标准','业务元数据标准','Standard-7',null,'Standard-13',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-13', '技术元数据标准','技术元数据标准','Standard-7','Standard-12','Standard-14',2,3,'all');
--INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,level,categorytype,tenantid) VALUES ('Standard-14', '管理元数据标准','业管理元数据标准','Standard-7','Standard-13',null,2,3,'all');

--INSERT INTO "public"."privilege" VALUES ('2', '访客', '访客', NULL, 0, 0);
--INSERT INTO "public"."privilege" VALUES ('1', 'Admin', '平台管理员', NULL, 0, 0);
--INSERT INTO "public"."privilege" VALUES ('3', '管理', '技术权限', NULL, 1, 0);
--INSERT INTO "public"."privilege" VALUES ('4', '业务', '业务权限', NULL, 1, 0);
--INSERT INTO "public"."privilege" VALUES ('5', '技术', '业务对象管理', NULL, 1, 0);
--INSERT INTO "public"."privilege" VALUES ('6', '业务目录管理员', '业务目录管理员', NULL, 1, 0);
--INSERT INTO "public"."privilege" VALUES ('7', '技术目录管理员', '技术目录管理员', NULL, 1, 0);
--INSERT INTO "public"."statisticaltype" VALUES (1, '数据库总量');
--INSERT INTO "public"."statisticaltype" VALUES (2, '数据表总量');
--INSERT INTO "public"."statisticaltype" VALUES (3, '业务对象总量');
--INSERT INTO "public"."statisticaltype" VALUES (4, '业务对象已补充');
--INSERT INTO "public"."statisticaltype" VALUES (5, '业务对象未补充');
--INSERT INTO "public"."role" VALUES ('4', '业务', '业务数据负责人', '4', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
--INSERT INTO "public"."role" VALUES ('5', '技术', '技术数据负责人', '5', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
--INSERT INTO "public"."role" VALUES ('1', '平台管理员', '平台管理员', '1', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 0, 0, 0, true);
--INSERT INTO "public"."role" VALUES ('3', '管理员', '管理员', '3', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
--INSERT INTO "public"."role" VALUES ('6', '业务目录管理员', '业务目录管理员', '6', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
--INSERT INTO "public"."role" VALUES ('7', '技术目录管理员', '技术目录管理员', '7', to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), 1, 0, 1, true);
--INSERT INTO "public"."module" VALUES (5, '业务对象管理', 1);
--INSERT INTO "public"."module" VALUES (6, '权限', 1);
--INSERT INTO "public"."module" VALUES (7, '元数据管理', 1);
--INSERT INTO "public"."module" VALUES (1, '技术数据', 1);
--INSERT INTO "public"."module" VALUES (2, '业务对象', 1);
--INSERT INTO "public"."module" VALUES (3, '编辑技术信息', 0);
--INSERT INTO "public"."module" VALUES (8, '管理技术目录', 0);
--INSERT INTO "public"."module" VALUES (9, '管理业务目录', 0);
--INSERT INTO "public"."module" VALUES (4, '编辑业务信息', 0);
--INSERT INTO "public"."module" VALUES (11, '数据标准', 1);
--INSERT INTO "public"."module" VALUES (12, '日志审计', 1);
--INSERT INTO "public"."module" VALUES (10, '数据分享', 1);
--INSERT INTO "public"."module" VALUES (13, '数据质量', 1);
--INSERT INTO "public"."module" VALUES (14, '数据源管理', 1);
--INSERT INTO "public"."privilege2module" VALUES ('1', 1);
--INSERT INTO "public"."privilege2module" VALUES ('1', 2);
--INSERT INTO "public"."privilege2module" VALUES ('1', 3);
--INSERT INTO "public"."privilege2module" VALUES ('1', 4);
--INSERT INTO "public"."privilege2module" VALUES ('1', 5);
--INSERT INTO "public"."privilege2module" VALUES ('1', 6);
--INSERT INTO "public"."privilege2module" VALUES ('1', 7);
--INSERT INTO "public"."privilege2module" VALUES ('1', 8);
--INSERT INTO "public"."privilege2module" VALUES ('1', 9);
--INSERT INTO "public"."privilege2module" VALUES ('1', 10);
--INSERT INTO "public"."privilege2module" VALUES ('1', 11);
--INSERT INTO "public"."privilege2module" VALUES ('1', 12);
--INSERT INTO "public"."privilege2module" VALUES ('1', 13);
--INSERT INTO "public"."privilege2module" VALUES ('1', 14);
--INSERT INTO "public"."privilege2module" VALUES ('2', 2);
--INSERT INTO "public"."privilege2module" VALUES ('3', 1);
--INSERT INTO "public"."privilege2module" VALUES ('3', 2);
--INSERT INTO "public"."privilege2module" VALUES ('3', 3);
--INSERT INTO "public"."privilege2module" VALUES ('3', 4);
--INSERT INTO "public"."privilege2module" VALUES ('3', 5);
--INSERT INTO "public"."privilege2module" VALUES ('3', 7);
--INSERT INTO "public"."privilege2module" VALUES ('3', 8);
--INSERT INTO "public"."privilege2module" VALUES ('3', 9);
--INSERT INTO "public"."privilege2module" VALUES ('3', 10);
--INSERT INTO "public"."privilege2module" VALUES ('3', 11);
--INSERT INTO "public"."privilege2module" VALUES ('3', 12);
--INSERT INTO "public"."privilege2module" VALUES ('3', 13);
--INSERT INTO "public"."privilege2module" VALUES ('3', 14);
--INSERT INTO "public"."privilege2module" VALUES ('4', 2);
--INSERT INTO "public"."privilege2module" VALUES ('4', 4);
--INSERT INTO "public"."privilege2module" VALUES ('4', 9);
--INSERT INTO "public"."privilege2module" VALUES ('5', 1);
--INSERT INTO "public"."privilege2module" VALUES ('5', 2);
--INSERT INTO "public"."privilege2module" VALUES ('5', 3);
--INSERT INTO "public"."privilege2module" VALUES ('5', 5);
--INSERT INTO "public"."privilege2module" VALUES ('5', 7);
--INSERT INTO "public"."privilege2module" VALUES ('5', 8);
--INSERT INTO "public"."privilege2module" VALUES ('5', 10);
--INSERT INTO "public"."privilege2module" VALUES ('6', 2);
--INSERT INTO "public"."privilege2module" VALUES ('6', 9);
--INSERT INTO "public"."privilege2module" VALUES ('7', 1);
--INSERT INTO "public"."privilege2module" VALUES ('7', 8);
--INSERT INTO "public"."privilege2module" VALUES ('7', 7);
--INSERT INTO "public"."privilege2module" VALUES ('7', 2);

--INSERT INTO "public"."apigroup" VALUES ('1', '全部分组', NULL, NULL, NULL, NULL, NULL, NULL,'all');
--INSERT INTO "public"."apigroup" VALUES ('0', '未分组', '1', NULL, NULL, NULL, NULL, NULL,'all');
--INSERT INTO "public"."systemrule" VALUES (13, '字段平均值变化', '相比上一周期，字段平均值变化', 1, '');
--INSERT INTO "public"."systemrule" VALUES (14, '字段汇总值变化', '相比上一周期，字段汇总值变化', 1, '');
--INSERT INTO "public"."systemrule" VALUES (15, '字段最小值变化', '相比上一周期，字段最小值变化', 1, '');
--INSERT INTO "public"."systemrule" VALUES (16, '字段最大值变化', '相比上一周期，字段最大值变化', 1, '');
--INSERT INTO "public"."systemrule" VALUES (29, '字段重复值个数/总行数', '计算字段重复值行数所占的比例', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (28, '字段空值个数/总行数', '计算字段空值行数所占的比例', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (27, '字段唯一值个数/总行数', '计算字段唯一值行数所占的比例', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (3, '表大小变化', '相比上一周期，表大小变化', 0, '字节');
--INSERT INTO "public"."systemrule" VALUES (0, '表行数变化率', '相比上一周期，表行数变化率', 0, '%');
--INSERT INTO "public"."systemrule" VALUES (2, '表行数变化', '相比上一周期，表行数变化', 0, '行');
--INSERT INTO "public"."systemrule" VALUES (1, '表大小变化率', '相比上一周期，表大小变化率', 0, '%');
--INSERT INTO "public"."systemrule" VALUES (6, '字段平均值变化率', '相比上一周期，字段平均值变化率', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (4, '当前表行数', '表行数是否符合预期', 0, '行');
--INSERT INTO "public"."systemrule" VALUES (5, '当前表大小', '表大小是否符合预期', 0, '字节');
--INSERT INTO "public"."systemrule" VALUES (20, '字段平均值', '计算字段平均值', 1, NULL);
--INSERT INTO "public"."systemrule" VALUES (21, '字段汇总值', '计算字段汇总值', 1, NULL);
--INSERT INTO "public"."systemrule" VALUES (22, '字段最小值', '计算字段最小值', 1, NULL);
--INSERT INTO "public"."systemrule" VALUES (23, '字段最大值
--', '计算字段最大值', 1, NULL);
--INSERT INTO "public"."systemrule" VALUES (7, '字段汇总值变化率', '相比上一周期，字段汇总值变化率', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (8, '字段最小值变化率', '相比上一周期，字段最小值变化率', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (9, '字段最大值变化率', '相比上一周期，字段最大值变化率', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (10, '字段唯一值个数变化率', '相比上一周期，字段唯一值个数变化率', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (11, '字段空值个数变化率', '相比上一周期，字段空值个数变化率', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (12, '字段重复值个数变化率', '相比上一周期，字段重复值个数变化率', 1, '%');
--INSERT INTO "public"."systemrule" VALUES (24, '字段唯一值个数', '计算字段唯一值个数', 1, '个');
--INSERT INTO "public"."systemrule" VALUES (25, '字段空值个数', '计算字段空值个数', 1, '个');
--INSERT INTO "public"."systemrule" VALUES (26, '字段重复值个数', '计算字段重复值个数', 1, '个');
--INSERT INTO "public"."systemrule" VALUES (17, '字段唯一值个数变化', '相比上一周期，字段唯一值个数变化', 1, '个');
--INSERT INTO "public"."systemrule" VALUES (18, '字段空值个数变化', '相比上一周期，字段空值个数变化', 1, '个');
--INSERT INTO "public"."systemrule" VALUES (19, '字段重复值个数变化', '相比上一周期，字段重复值个数变化
--', 1, '个');
--INSERT INTO "public"."rule2buildtype" VALUES (0, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (1, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (2, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (3, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (4, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (5, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (6, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (7, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (8, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (9, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (10, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (11, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (12, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (13, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (14, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (15, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (16, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (17, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (18, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (19, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (20, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (21, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (22, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (23, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (24, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (25, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (26, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (27, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (28, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (29, 0);
--INSERT INTO "public"."rule2buildtype" VALUES (4, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (5, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (20, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (21, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (22, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (23, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (24, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (25, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (26, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (27, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (28, 1);
--INSERT INTO "public"."rule2buildtype" VALUES (29, 1);
--INSERT INTO "public"."rule2checktype" VALUES (1, 1);
--INSERT INTO "public"."rule2checktype" VALUES (0, 0);
--INSERT INTO "public"."rule2checktype" VALUES (0, 1);
--INSERT INTO "public"."rule2checktype" VALUES (1, 0);
--INSERT INTO "public"."rule2checktype" VALUES (2, 0);
--INSERT INTO "public"."rule2checktype" VALUES (3, 0);
--INSERT INTO "public"."rule2checktype" VALUES (4, 0);
--INSERT INTO "public"."rule2checktype" VALUES (5, 0);
--INSERT INTO "public"."rule2checktype" VALUES (6, 0);
--INSERT INTO "public"."rule2checktype" VALUES (7, 0);
--INSERT INTO "public"."rule2checktype" VALUES (8, 0);
--INSERT INTO "public"."rule2checktype" VALUES (9, 0);
--INSERT INTO "public"."rule2checktype" VALUES (10, 0);
--INSERT INTO "public"."rule2checktype" VALUES (11, 0);
--INSERT INTO "public"."rule2checktype" VALUES (12, 0);
--INSERT INTO "public"."rule2checktype" VALUES (13, 0);
--INSERT INTO "public"."rule2checktype" VALUES (14, 0);
--INSERT INTO "public"."rule2checktype" VALUES (15, 0);
--INSERT INTO "public"."rule2checktype" VALUES (16, 0);
--INSERT INTO "public"."rule2checktype" VALUES (17, 0);
--INSERT INTO "public"."rule2checktype" VALUES (18, 0);
--INSERT INTO "public"."rule2checktype" VALUES (19, 0);
--INSERT INTO "public"."rule2checktype" VALUES (20, 0);
--INSERT INTO "public"."rule2checktype" VALUES (21, 0);
--INSERT INTO "public"."rule2checktype" VALUES (22, 0);
--INSERT INTO "public"."rule2checktype" VALUES (23, 0);
--INSERT INTO "public"."rule2checktype" VALUES (24, 0);
--INSERT INTO "public"."rule2checktype" VALUES (25, 0);
--INSERT INTO "public"."rule2checktype" VALUES (26, 0);
--INSERT INTO "public"."rule2checktype" VALUES (27, 0);
--INSERT INTO "public"."rule2checktype" VALUES (28, 0);
--INSERT INTO "public"."rule2checktype" VALUES (29, 0);
--INSERT INTO "public"."rule2checktype" VALUES (6, 1);
--INSERT INTO "public"."rule2checktype" VALUES (7, 1);
--INSERT INTO "public"."rule2checktype" VALUES (8, 1);
--INSERT INTO "public"."rule2checktype" VALUES (9, 1);
--INSERT INTO "public"."rule2checktype" VALUES (10, 1);
--INSERT INTO "public"."rule2checktype" VALUES (11, 1);
--INSERT INTO "public"."rule2checktype" VALUES (12, 1);
--INSERT INTO "public"."rule2checktype" VALUES (27, 1);
--INSERT INTO "public"."rule2checktype" VALUES (28, 1);
--INSERT INTO "public"."rule2checktype" VALUES (29, 1);
--INSERT INTO "public"."rule2datatype" VALUES (6, 1);
--INSERT INTO "public"."rule2datatype" VALUES (7, 1);
--INSERT INTO "public"."rule2datatype" VALUES (8, 1);
--INSERT INTO "public"."rule2datatype" VALUES (9, 1);
--INSERT INTO "public"."rule2datatype" VALUES (10, 1);
--INSERT INTO "public"."rule2datatype" VALUES (11, 1);
--INSERT INTO "public"."rule2datatype" VALUES (12, 1);
--INSERT INTO "public"."rule2datatype" VALUES (13, 1);
--INSERT INTO "public"."rule2datatype" VALUES (14, 1);
--INSERT INTO "public"."rule2datatype" VALUES (15, 1);
--INSERT INTO "public"."rule2datatype" VALUES (16, 1);
--INSERT INTO "public"."rule2datatype" VALUES (17, 1);
--INSERT INTO "public"."rule2datatype" VALUES (18, 1);
--INSERT INTO "public"."rule2datatype" VALUES (19, 1);
--INSERT INTO "public"."rule2datatype" VALUES (20, 1);
--INSERT INTO "public"."rule2datatype" VALUES (21, 1);
--INSERT INTO "public"."rule2datatype" VALUES (22, 1);
--INSERT INTO "public"."rule2datatype" VALUES (23, 1);
--INSERT INTO "public"."rule2datatype" VALUES (24, 1);
--INSERT INTO "public"."rule2datatype" VALUES (25, 1);
--INSERT INTO "public"."rule2datatype" VALUES (26, 1);
--INSERT INTO "public"."rule2datatype" VALUES (27, 1);
--INSERT INTO "public"."rule2datatype" VALUES (28, 1);
--INSERT INTO "public"."rule2datatype" VALUES (29, 1);
--INSERT INTO "public"."rule2datatype" VALUES (17, 2);
--INSERT INTO "public"."rule2datatype" VALUES (18, 2);
--INSERT INTO "public"."rule2datatype" VALUES (19, 2);
--INSERT INTO "public"."rule2datatype" VALUES (24, 2);
--INSERT INTO "public"."rule2datatype" VALUES (25, 2);
--INSERT INTO "public"."rule2datatype" VALUES (26, 2);
--INSERT INTO "public"."rule2datatype" VALUES (27, 2);
--INSERT INTO "public"."rule2datatype" VALUES (28, 2);
--INSERT INTO "public"."rule2datatype" VALUES (29, 2);
--INSERT INTO "public"."rule2datatype" VALUES (10, 2);
--INSERT INTO "public"."rule2datatype" VALUES (11, 2);
--INSERT INTO "public"."rule2datatype" VALUES (12, 2);





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
  "delete" bool,
  "pool" varchar(255) COLLATE "pg_catalog"."default",
  "config" varchar COLLATE "pg_catalog"."default"
)
;

COMMENT ON COLUMN "public"."data_quality_sub_task"."task_id" IS '所属任务id';

COMMENT ON COLUMN "public"."data_quality_sub_task"."datasource_type" IS '数据源类型:1-表,2-字段';

COMMENT ON COLUMN "public"."data_quality_sub_task"."sequence" IS '子任务顺序';

COMMENT ON COLUMN "public"."data_quality_sub_task"."delete" IS '是否删除';

COMMENT ON COLUMN "public"."data_quality_sub_task"."pool" IS '资源池';

COMMENT ON COLUMN "public"."data_quality_sub_task"."config" IS 'spark配置';

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
  "current_execution_status" int2,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  "pool" varchar(255) COLLATE "pg_catalog"."default"
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
  "rule_id" varchar(36) COLLATE "pg_catalog"."default"
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
  "delete" bool,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
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
-- Primary Key structure for table warning_group
-- ----------------------------
ALTER TABLE "public"."warning_group" ADD CONSTRAINT "warning_group_pkey" PRIMARY KEY ("id");


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
INSERT INTO "public"."api_module" VALUES ('audit', 'OPTION', 26, 't');
INSERT INTO "public"."api_module" VALUES ('dataquality', 'OPTION', 13, 't');
INSERT INTO "public"."api_module" VALUES ('datashare', 'OPTION', 27, 't');
INSERT INTO "public"."api_module" VALUES ('data', 'OPTION', 24, 't');
INSERT INTO "public"."api_module" VALUES ('columns', 'OPTION', 25, 't');
INSERT INTO "public"."api_module" VALUES ('role', 'OPTION', 6, 't');
INSERT INTO "public"."api_module" VALUES ('privilege', 'OPTION', 6, 't');
INSERT INTO "public"."api_module" VALUES ('datastandard', 'OPTION', 11, 't');
INSERT INTO "public"."api_module" VALUES ('operatelog', 'OPTION', 12, 't');
INSERT INTO "public"."api_module" VALUES ('datasource', 'OPTION', 14, 't');
INSERT INTO "public"."api_module" VALUES ('/dataquality/ruleTemplate/{templateId}/report', 'POST', 20, 'f');
INSERT INTO "public"."api_module" VALUES ('/dataquality/ruleTemplate/{executionId}/record', 'GET', 20, 'f');
INSERT INTO "public"."api_module" VALUES ('/dataquality/ruleTemplate/task/{taskId}', 'GET', 20, 'f');
INSERT INTO "public"."api_module" VALUES ('/dataquality/rule', 'OPTION', 21, 't');
INSERT INTO "public"."api_module" VALUES ('/dataquality/warninggroup', 'OPTION', 22, 't');
INSERT INTO "public"."api_module" VALUES ('/dataquality/warning', 'OPTION', 22, 't');
INSERT INTO "public"."api_module" VALUES ('/dataquality/taskManage', 'OPTION', 23, 't');
INSERT INTO "public"."api_module" VALUES ('authorization', 'OPTION', 19, 't');
INSERT INTO "public"."api_module" VALUES ('businesses', 'OPTION', 2, 't');
INSERT INTO "public"."api_module" VALUES ('userGroups', 'OPTION', 16, 't');
INSERT INTO "public"."api_module" VALUES ('technical', 'OPTION', 1, 't');

-- ----------------------------
-- Primary Key structure for table api_module
-- ----------------------------
ALTER TABLE "public"."api_module" ADD CONSTRAINT "api_module_pkey" PRIMARY KEY ("path", "method");

-- ----------------------------
-- Table structure for data_source
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_source";
CREATE TABLE "public"."data_source" (
  "source_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "source_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6) NOT NULL,
  "update_time" timestamptz(6) NOT NULL,
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "ip" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "port" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "username" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "password" varchar(255) COLLATE "pg_catalog"."default",
  "database" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "jdbc_parameter" varchar COLLATE "pg_catalog"."default",
  "manager" varchar COLLATE "pg_catalog"."default",
  "oracle_db" varchar COLLATE "pg_catalog"."default",
  "isapi" bool,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "servicetype" varchar(36) COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."data_source"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."data_source"."source_name" IS '数据源名称';
COMMENT ON COLUMN "public"."data_source"."source_type" IS '数据源类型';
COMMENT ON COLUMN "public"."data_source"."description" IS '数据源描述';
COMMENT ON COLUMN "public"."data_source"."create_time" IS '数据源创建时间';
COMMENT ON COLUMN "public"."data_source"."update_time" IS '数据源更新时间';
COMMENT ON COLUMN "public"."data_source"."update_user_id" IS '数据源更新人id';
COMMENT ON COLUMN "public"."data_source"."ip" IS '数据源主机ip';
COMMENT ON COLUMN "public"."data_source"."port" IS '数据源端口';
COMMENT ON COLUMN "public"."data_source"."username" IS '数据库用户名';
COMMENT ON COLUMN "public"."data_source"."password" IS '数据库用户密码';
COMMENT ON COLUMN "public"."data_source"."database" IS '数据库名';
COMMENT ON COLUMN "public"."data_source"."jdbc_parameter" IS 'jdbc连接参数';
COMMENT ON COLUMN "public"."data_source"."create_user_id" IS '数据源创建人id';
COMMENT ON COLUMN "public"."data_source"."oracle_db" IS 'oracle数据库';
COMMENT ON COLUMN "public"."data_source"."isapi" IS '是否是api数据源';

-- ----------------------------
-- Primary Key structure for table data_source
-- ----------------------------
ALTER TABLE "public"."data_source" ADD CONSTRAINT "data_source_pkey1" PRIMARY KEY ("source_id");

DROP TABLE IF EXISTS "public"."datasource_group_relation";
CREATE TABLE "public"."datasource_group_relation" (
  "source_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "privilege_code" varchar(36) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Primary Key structure for table datasource_group_relation
-- ----------------------------
ALTER TABLE "public"."datasource_group_relation" ADD CONSTRAINT "datasource_group_relation_pkey" PRIMARY KEY ("source_id", "group_id");

-- ----------------------------
-- Table structure for data_source_authorize
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_source_authorize";
CREATE TABLE "public"."data_source_authorize" (
  "source_id" varchar(225) COLLATE "pg_catalog"."default" NOT NULL,
  "authorize_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."data_source_authorize"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."data_source_authorize"."authorize_user_id" IS '授权人id';

-- ----------------------------
-- Primary Key structure for table data_source_authorize
-- ----------------------------
ALTER TABLE "public"."data_source_authorize" ADD CONSTRAINT "data_source_authorize_pkey" PRIMARY KEY ("source_id", "authorize_user_id");


-- ----------------------------
-- Table structure for metadata_subscribe
-- ----------------------------
DROP TABLE IF EXISTS "public"."metadata_subscribe";
CREATE TABLE "public"."metadata_subscribe" (
  "user_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "table_guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6)
)
;

-- ----------------------------
-- Primary Key structure for table metadata_subscribe
-- ----------------------------
ALTER TABLE "public"."metadata_subscribe" ADD CONSTRAINT "metadata_subscribe_pkey" PRIMARY KEY ("user_id", "table_guid");


-- ----------------------------
-- Table structure for report2ruletemplate
-- ----------------------------
DROP TABLE IF EXISTS "public"."report2ruletemplate";
CREATE TABLE "public"."report2ruletemplate" (
  "rule_template_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "data_quality_execute_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "creator" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6)
)
;

-- ----------------------------
-- Primary Key structure for table report2ruletemplate
-- ----------------------------
ALTER TABLE "public"."report2ruletemplate" ADD CONSTRAINT "report2ruletype_pkey" PRIMARY KEY ("rule_template_id", "data_quality_execute_id");


DROP TABLE IF EXISTS "public"."data_standard2table";
CREATE TABLE "public"."data_standard2table" (
  "number" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(6),
  "operator" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Primary Key structure for table data_standard2table
-- ----------------------------
ALTER TABLE "public"."data_standard2table" ADD CONSTRAINT "data_standard2table_pkey" PRIMARY KEY ("number", "tableguid");

DROP TABLE IF EXISTS "public"."data_standard2data_quality_rule";
CREATE TABLE "public"."data_standard2data_quality_rule" (
  "number" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ruleid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(6),
  "operator" varchar COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Primary Key structure for table data_standard2data_quality_rule
-- ----------------------------
ALTER TABLE "public"."data_standard2data_quality_rule" ADD CONSTRAINT "data_standard2data_quality_rule_pkey" PRIMARY KEY ("number", "ruleid");

-- ----------------------------
-- Table structure for user2role
-- ----------------------------
DROP TABLE IF EXISTS "public"."user2role";
CREATE TABLE "public"."user2role" (
  "userid" varchar(225) COLLATE "pg_catalog"."default" NOT NULL,
  "roleid" varchar(225) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Primary Key structure for table user2role
-- ----------------------------
ALTER TABLE "public"."user2role" ADD CONSTRAINT "user2role_pkey" PRIMARY KEY ("userid", "roleid");

INSERT INTO "public"."user2role"("userid","roleid") VALUES ('1', '1');

-- ----------------------------
-- Table structure for data_source_api_authorize
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_source_api_authorize";
CREATE TABLE "public"."data_source_api_authorize" (
  "source_id" varchar(225) COLLATE "pg_catalog"."default" NOT NULL,
  "authorize_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- Primary Key structure for table data_source_api_authorize
-- ----------------------------
ALTER TABLE "public"."data_source_api_authorize" ADD CONSTRAINT "data_source_api_authorize_pkey" PRIMARY KEY ("source_id", "authorize_user_id");

-- ----------------------------
-- Table structure for table_metadata_history
-- ----------------------------
DROP TABLE IF EXISTS "public"."table_metadata_history";
CREATE TABLE "public"."table_metadata_history" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "creator" varchar(255) COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "database_name" varchar(255) COLLATE "pg_catalog"."default",
  "table_type" varchar(255) COLLATE "pg_catalog"."default",
  "partition_table" bool,
  "table_format" varchar(255) COLLATE "pg_catalog"."default",
  "store_location" varchar(255) COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "version" int8 NOT NULL
)
;


-- Primary Key structure for table table_metadata_history
-- ----------------------------
ALTER TABLE "public"."table_metadata_history" ADD CONSTRAINT "table_metadata_history_pkey" PRIMARY KEY ("guid", "version");


-- ----------------------------
-- Table structure for column_metadata_history
-- ----------------------------
DROP TABLE IF EXISTS "public"."column_metadata_history";
CREATE TABLE "public"."column_metadata_history" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "type" varchar(255) COLLATE "pg_catalog"."default",
  "table_guid" varchar COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "version" int8 NOT NULL,
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "partition_field" bool,
  "creator" varchar(255) COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."column_metadata_history"."guid" IS '字段guid';
COMMENT ON COLUMN "public"."column_metadata_history"."name" IS '字段名称';
COMMENT ON COLUMN "public"."column_metadata_history"."type" IS '类型';
COMMENT ON COLUMN "public"."column_metadata_history"."table_guid" IS '所属表guid';
COMMENT ON COLUMN "public"."column_metadata_history"."description" IS '描述';
COMMENT ON COLUMN "public"."column_metadata_history"."version" IS '版本';
COMMENT ON COLUMN "public"."column_metadata_history"."status" IS '状态';
COMMENT ON COLUMN "public"."column_metadata_history"."partition_field" IS '是否为分区字段';

-- ----------------------------
-- Primary Key structure for table column_metadata_history
-- ----------------------------
ALTER TABLE "public"."column_metadata_history" ADD CONSTRAINT "column_metadata_history_pkey" PRIMARY KEY ("guid", "version");

DROP TABLE IF EXISTS "public"."tenant";
CREATE TABLE "public"."tenant" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."tenant"."id" IS '租户id';
COMMENT ON COLUMN "public"."tenant"."name" IS '租户名字';

-- ----------------------------
-- Primary Key structure for table tenant
-- ----------------------------
ALTER TABLE "public"."tenant" ADD CONSTRAINT "tenant_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Table structure for user_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_group";
CREATE TABLE "public"."user_group" (
  "id" varchar(255) COLLATE "pg_catalog"."default",
  "tenant" varchar(255) COLLATE "pg_catalog"."default",
  "name" varchar(64) COLLATE "pg_catalog"."default",
  "creator" varchar(40) COLLATE "pg_catalog"."default",
  "description" varchar(256) COLLATE "pg_catalog"."default",
  "createtime" timestamptz(6),
  "updatetime" timestamptz(6),
  "valid" bool,
  "authorize_user" varchar(255) COLLATE "pg_catalog"."default",
  "authorize_time" timestamptz(6)
)
;

COMMENT ON COLUMN "public"."user_group"."id" IS 'id';
COMMENT ON COLUMN "public"."user_group"."tenant" IS '租户';
COMMENT ON COLUMN "public"."user_group"."name" IS '名字';
COMMENT ON COLUMN "public"."user_group"."creator" IS '创建人';
COMMENT ON COLUMN "public"."user_group"."description" IS '描述';
COMMENT ON COLUMN "public"."user_group"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."user_group"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."user_group"."valid" IS '状态';
COMMENT ON COLUMN "public"."user_group"."authorize_user" IS '授权人';
COMMENT ON COLUMN "public"."user_group"."authorize_time" IS '授权时间';

-- ----------------------------
-- Table structure for user_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_group_relation";
CREATE TABLE "public"."user_group_relation" (
  "group_id" varchar(40) COLLATE "pg_catalog"."default",
  "user_id" varchar(40) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for category_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."category_group_relation";
CREATE TABLE "public"."category_group_relation" (
  "category_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "read" bool,
  "edit_category" bool,
  "edit_item" bool
)
;

-- ----------------------------
-- Table structure for api_audit
-- ----------------------------
DROP TABLE IF EXISTS "public"."api_audit";
create table "public"."api_audit"
(
	id varchar(255) not null constraint api_audit_pk primary key,
	api_guid varchar(255),
	api_version varchar(255),
	api_version_num int4,
	applicant varchar(255),
	applicant_name varchar(255),
	status varchar(255),
	reason varchar,
	updater varchar(255),
	tenant_id varchar(255),
	create_time timestamp(6) default now(),
	update_time timestamp(6) default now()
);

comment on column "public"."api_audit".id is '审核记录 id';
comment on column "public"."api_audit".api_guid is 'Api Guid';
comment on column "public"."api_audit".api_version is 'Api 版本号';
comment on column "public"."api_audit".api_version_num is 'Api 版本号标识';
comment on column "public"."api_audit".applicant is '申请人 Id';
comment on column "public"."api_audit".create_time is '创建时间';
comment on column "public"."api_audit".update_time is '更新时间';
comment on column "public"."api_audit".status is '审核状态';
comment on column "public"."api_audit".reason is '驳回原因';
comment on column "public"."api_audit".tenant_id is '租户id';
comment on column "public"."api_audit".applicant_name is '申请人名称';
comment on column "public"."api_audit".updater is '更新人 Id';

DROP TABLE IF EXISTS "public"."api";
CREATE TABLE "public"."api" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tableguid" varchar COLLATE "pg_catalog"."default",
  "dbguid" varchar COLLATE "pg_catalog"."default",
  "version" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "description" varchar(256) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "protocol" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "requestmode" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "path" varchar(256) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "creator" varchar COLLATE "pg_catalog"."default",
  "createtime" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "updatetime" varchar COLLATE "pg_catalog"."default",
  "categoryguid" varchar COLLATE "pg_catalog"."default",
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "sourcetype" varchar(255) COLLATE "pg_catalog"."default",
  "schemaname" varchar(255) COLLATE "pg_catalog"."default",
  "tablename" varchar(255) COLLATE "pg_catalog"."default",
  "dbname" varchar(255) COLLATE "pg_catalog"."default",
  "sourceid" varchar(255) COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  "pool" varchar(255) COLLATE "pg_catalog"."default",
  "approve" bool DEFAULT false,
  "log" bool DEFAULT false,
  "version_num" int4 NOT NULL,
  "param" json,
  "returnparam" json,
  "sortparam" json,
  "projectid" varchar(255) COLLATE "pg_catalog"."default",
  "valid" bool,
  "mobius_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."api"."guid" IS 'id';
COMMENT ON COLUMN "public"."api"."name" IS '名字';
COMMENT ON COLUMN "public"."api"."tableguid" IS '表id';
COMMENT ON COLUMN "public"."api"."dbguid" IS '库id';
COMMENT ON COLUMN "public"."api"."version" IS '版本';
COMMENT ON COLUMN "public"."api"."description" IS '描述';
COMMENT ON COLUMN "public"."api"."protocol" IS '参数协议';
COMMENT ON COLUMN "public"."api"."requestmode" IS '请求方式';
COMMENT ON COLUMN "public"."api"."path" IS '路径';
COMMENT ON COLUMN "public"."api"."creator" IS '创建人';
COMMENT ON COLUMN "public"."api"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."api"."updater" IS '更新人';
COMMENT ON COLUMN "public"."api"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."api"."categoryguid" IS '目录id';
COMMENT ON COLUMN "public"."api"."status" IS '上下架状态';
COMMENT ON COLUMN "public"."api"."sourcetype" IS '数据类型';
COMMENT ON COLUMN "public"."api"."schemaname" IS 'schema名字';
COMMENT ON COLUMN "public"."api"."tablename" IS '表名字';
COMMENT ON COLUMN "public"."api"."dbname" IS '数据库名字';
COMMENT ON COLUMN "public"."api"."sourceid" IS '数据源id';
COMMENT ON COLUMN "public"."api"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."api"."pool" IS '资源池';
COMMENT ON COLUMN "public"."api"."approve" IS 'api认证';
COMMENT ON COLUMN "public"."api"."log" IS '访问日志是否开启';
COMMENT ON COLUMN "public"."api"."version_num" IS '版本';
COMMENT ON COLUMN "public"."api"."param" IS '过滤参数';
COMMENT ON COLUMN "public"."api"."returnparam" IS '返回参数';
COMMENT ON COLUMN "public"."api"."sortparam" IS '排序参数';
COMMENT ON COLUMN "public"."api"."projectid" IS '项目id';
COMMENT ON COLUMN "public"."api"."valid" IS '是否删除';

-- ----------------------------
-- Primary Key structure for table api
-- ----------------------------
ALTER TABLE "public"."api" ADD CONSTRAINT "api_pkey" PRIMARY KEY ("guid", "version_num");


DROP TABLE IF EXISTS "public"."api_audit";
CREATE TABLE "public"."api_audit" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "api_guid" varchar(255) COLLATE "pg_catalog"."default",
  "api_version" varchar(255) COLLATE "pg_catalog"."default",
  "applicant" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT now(),
  "update_time" timestamp(6) DEFAULT now(),
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "reason" varchar COLLATE "pg_catalog"."default",
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "applicant_name" varchar(255) COLLATE "pg_catalog"."default",
  "updater" varchar(255) COLLATE "pg_catalog"."default",
  "api_version_num" int4 DEFAULT 1
)
;
COMMENT ON COLUMN "public"."api_audit"."id" IS '审核记录 id';
COMMENT ON COLUMN "public"."api_audit"."api_guid" IS 'Api Guid';
COMMENT ON COLUMN "public"."api_audit"."api_version" IS 'Api 版本号';
COMMENT ON COLUMN "public"."api_audit"."applicant" IS '申请人 Id';
COMMENT ON COLUMN "public"."api_audit"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."api_audit"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."api_audit"."status" IS '审核状态';
COMMENT ON COLUMN "public"."api_audit"."reason" IS '驳回原因';
COMMENT ON COLUMN "public"."api_audit"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."api_audit"."applicant_name" IS '申请人名称';
COMMENT ON COLUMN "public"."api_audit"."updater" IS '更新人 Id';

-- ----------------------------
-- Primary Key structure for table api_audit
-- ----------------------------
ALTER TABLE "public"."api_audit" ADD CONSTRAINT "api_audit_pk" PRIMARY KEY ("id");


DROP TABLE IF EXISTS "public"."api_category";
CREATE TABLE "public"."api_category" (
  "guid" text COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "name" text COLLATE "pg_catalog"."default" NOT NULL,
  "upbrothercategoryguid" text COLLATE "pg_catalog"."default",
  "downbrothercategoryguid" text COLLATE "pg_catalog"."default",
  "parentcategoryguid" text COLLATE "pg_catalog"."default",
  "qualifiedname" text COLLATE "pg_catalog"."default",
  "projectid" varchar(225) COLLATE "pg_catalog"."default" NOT NULL,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "level" int2,
  "createtime" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."api_category"."guid" IS 'id';
COMMENT ON COLUMN "public"."api_category"."description" IS '描述';
COMMENT ON COLUMN "public"."api_category"."name" IS '名字';
COMMENT ON COLUMN "public"."api_category"."upbrothercategoryguid" IS '同级上层目录';
COMMENT ON COLUMN "public"."api_category"."downbrothercategoryguid" IS '同级上层目录';
COMMENT ON COLUMN "public"."api_category"."parentcategoryguid" IS '父目录';
COMMENT ON COLUMN "public"."api_category"."projectid" IS '项目id';
COMMENT ON COLUMN "public"."api_category"."tenantid" IS '租户';
COMMENT ON COLUMN "public"."api_category"."level" IS '级别';
COMMENT ON COLUMN "public"."api_category"."createtime" IS '创建时间';

-- ----------------------------
-- Primary Key structure for table api_category
-- ----------------------------
ALTER TABLE "public"."api_category" ADD CONSTRAINT "api_category_pkey" PRIMARY KEY ("guid");


DROP TABLE IF EXISTS "public"."api_group";
CREATE TABLE "public"."api_group" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "approve" json,
  "createtime" timestamptz(6),
  "publish" bool,
  "updater" varchar(255) COLLATE "pg_catalog"."default",
  "updatetime" timestamptz(6),
  "tenantid" varchar(36) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "projectid" varchar(36) COLLATE "pg_catalog"."default",
  "mobius_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."api_group"."id" IS '分组id';
COMMENT ON COLUMN "public"."api_group"."name" IS '分组名字';
COMMENT ON COLUMN "public"."api_group"."description" IS '描述';
COMMENT ON COLUMN "public"."api_group"."creator" IS '创建者';
COMMENT ON COLUMN "public"."api_group"."approve" IS '审批人';
COMMENT ON COLUMN "public"."api_group"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."api_group"."publish" IS '是否发布';
COMMENT ON COLUMN "public"."api_group"."updater" IS '更新人';
COMMENT ON COLUMN "public"."api_group"."updatetime" IS '更新时间时间';
COMMENT ON COLUMN "public"."api_group"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."api_group"."projectid" IS '项目id';

-- ----------------------------
-- Primary Key structure for table api_group
-- ----------------------------
ALTER TABLE "public"."api_group" ADD CONSTRAINT "api_group_pkey" PRIMARY KEY ("id");

DROP TABLE IF EXISTS "public"."api_group_log";
CREATE TABLE "public"."api_group_log" (
  "group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "userid" varchar(255) COLLATE "pg_catalog"."default",
  "time" varchar(255) COLLATE "pg_catalog"."default"
)
;


DROP TABLE IF EXISTS "public"."api_log";
CREATE TABLE "public"."api_log" (
  "apiid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "userid" varchar(255) COLLATE "pg_catalog"."default",
  "time" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."api_log"."apiid" IS 'apiid';
COMMENT ON COLUMN "public"."api_log"."type" IS '操作类型';
COMMENT ON COLUMN "public"."api_log"."userid" IS '操作人';
COMMENT ON COLUMN "public"."api_log"."time" IS '操作时间';



DROP TABLE IF EXISTS "public"."api_relation";
CREATE TABLE "public"."api_relation" (
  "apiid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "groupid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "version" varchar(255) COLLATE "pg_catalog"."default",
  "update_status" bool,
  "update_time" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."api_relation"."apiid" IS 'apiid';
COMMENT ON COLUMN "public"."api_relation"."groupid" IS '分组id';
COMMENT ON COLUMN "public"."api_relation"."version" IS '关联api版本';
COMMENT ON COLUMN "public"."api_relation"."update_status" IS '更新状态';
COMMENT ON COLUMN "public"."api_relation"."update_time" IS '更新时间开始时间';

-- ----------------------------
-- Primary Key structure for table api_relation
-- ----------------------------
ALTER TABLE "public"."api_relation" ADD CONSTRAINT "api_relation_pkey" PRIMARY KEY ("apiid", "groupid");



DROP TABLE IF EXISTS "public"."project";
CREATE TABLE "public"."project" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "creator" varchar(35) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(6) NOT NULL,
  "manager" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "valid" bool NOT NULL
)
;

-- ----------------------------
-- Primary Key structure for table project
-- ----------------------------
ALTER TABLE "public"."project" ADD CONSTRAINT "project_pkey" PRIMARY KEY ("id");



DROP TABLE IF EXISTS "public"."project_group_relation";
CREATE TABLE "public"."project_group_relation" (
  "project_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Primary Key structure for table project_group_relation
-- ----------------------------
ALTER TABLE "public"."project_group_relation" ADD CONSTRAINT "project_group_relation_pkey" PRIMARY KEY ("project_id", "group_id");

-- API 脱敏相关数据表结构

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

-- API黑白名单
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

--元数据采集任务定义
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

--元数据采集任务执行实例
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
COMMENT ON COLUMN "public"."time_limit"."grade" IS '粒度';
COMMENT ON COLUMN "public"."time_limit"."start_time" IS '启始时间';
COMMENT ON COLUMN "public"."time_limit"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."time_limit"."creator" IS '创建人：用户外健';
COMMENT ON COLUMN "public"."time_limit"."publisher" IS '发布人：用户外健';
COMMENT ON COLUMN "public"."time_limit"."updater" IS '更新人';
COMMENT ON COLUMN "public"."time_limit"."state" IS '状态';
COMMENT ON COLUMN "public"."time_limit"."version" IS '版本号';
COMMENT ON COLUMN "public"."time_limit"."delete" IS '删除标记';
COMMENT ON COLUMN "public"."time_limit"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."time_limit"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."time_limit"."tenantid" IS '租户ID';
COMMENT ON COLUMN "public"."time_limit"."tenantid" IS '标识';
COMMENT ON COLUMN "public"."time_limit"."time_type" IS '类型';
COMMENT ON COLUMN "public"."time_limit"."time_range" IS '前后范围';
COMMENT ON COLUMN "public"."time_limit"."approveid" IS '前后范围';




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
COMMENT ON COLUMN "public"."qualifier"."desc" IS '修饰词描述 ';
COMMENT ON COLUMN "public"."qualifier"."tenantid" IS '租户ID ';
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

COMMENT ON COLUMN "public"."index_atomic_info"."version" IS '版本号，每下线一次，记录一次历史版本，初始版本号为0，每下线一次，版本号+1';

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

COMMENT ON COLUMN "public"."index_derive_info"."version" IS '版本号，每下线一次，记录一次历史版本，初始版本号为0，每下线一次，版本号+1';

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

COMMENT ON COLUMN "public"."index_composite_info"."version" IS '版本号，每下线一次，记录一次历史版本，初始版本号为0，每下线一次，版本号+1';

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
COMMENT ON COLUMN "public"."approval_group"."tenantid" IS '租户ID';
COMMENT ON COLUMN "public"."approval_group"."valid" IS '有效';



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
COMMENT ON COLUMN "public"."approval_item"."approve_group" IS '审批组';
COMMENT ON COLUMN "public"."approval_item"."approver" IS '审批人';
COMMENT ON COLUMN "public"."approval_item"."approve_time" IS '审批时间';
COMMENT ON COLUMN "public"."approval_item"."submitter" IS '提交人';
COMMENT ON COLUMN "public"."approval_item"."module_id" IS '模块ID';
COMMENT ON COLUMN "public"."approval_item"."version" IS '版本';
COMMENT ON COLUMN "public"."approval_item"."tenant_id" IS '租户ID';
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
ALTER TABLE category ADD COLUMN IF NOT EXISTS updater varchar COLLATE "pg_catalog"."default";
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
