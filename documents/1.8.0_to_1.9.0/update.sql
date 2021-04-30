ALTER TABLE "public"."column_info" 
  ADD COLUMN "description" varchar COLLATE "pg_catalog"."default";

COMMENT ON COLUMN "public"."column_info"."description" IS '字段描述';

ALTER TABLE "public"."category_group_relation" 
  ADD COLUMN "read" bool,
  ADD COLUMN "edit_category" bool,
  ADD COLUMN "edit_item" bool;

delete from api_module;
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
  "valid" bool
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
  "projectid" varchar(36) COLLATE "pg_catalog"."default"
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


insert into category_group_relation(category_id,group_id,read,edit_category,edit_item) (WITH RECURSIVE categoryTree AS  
( 
select c.category_id::text guid,c.group_id,g.tenant from category_group_relation c join user_group g on c.group_id=g.id 
    UNION  
    SELECT category.guid,categoryTree.group_id,categoryTree.tenant from categoryTree 
    JOIN category on categoryTree.guid = category.parentCategoryGuid and category.tenantid=categoryTree.tenant
)
select guid,group_id,true,false,false from categoryTree);
delete from category_group_relation where read is null;