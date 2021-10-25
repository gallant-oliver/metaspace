-- ----------------------------
-- Sequence structure for number_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."number_seq";
CREATE SEQUENCE "public"."number_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for annex
-- ----------------------------
DROP TABLE IF EXISTS "public"."annex";
CREATE TABLE "public"."annex" (
  "annex_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "file_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "file_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6),
  "modify_time" timestamptz(6),
  "file_size" int8
)
;
COMMENT ON COLUMN "public"."annex"."annex_id" IS '主键';
COMMENT ON COLUMN "public"."annex"."file_name" IS '文件名';
COMMENT ON COLUMN "public"."annex"."file_type" IS '文件类型';
COMMENT ON COLUMN "public"."annex"."path" IS '文件地址';
COMMENT ON COLUMN "public"."annex"."file_size" IS '文件大小';

-- ----------------------------
-- Table structure for api
-- ----------------------------
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
  "mobius_id" varchar(255) COLLATE "pg_catalog"."default",
  "api_poly_entity" jsonb
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
COMMENT ON COLUMN "public"."api"."mobius_id" IS '云平台id';
COMMENT ON COLUMN "public"."api"."api_poly_entity" IS '初始策略';

-- ----------------------------
-- Table structure for api_audit
-- ----------------------------
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
  "api_version_num" int4 DEFAULT 1,
  "api_poly_id" text COLLATE "pg_catalog"."default"
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
COMMENT ON COLUMN "public"."api_audit"."api_poly_id" IS 'api策略id';

-- ----------------------------
-- Table structure for api_category
-- ----------------------------
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
-- Table structure for api_group
-- ----------------------------
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
COMMENT ON COLUMN "public"."api_group"."mobius_id" IS '云平台id';

-- ----------------------------
-- Table structure for api_group_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."api_group_log";
CREATE TABLE "public"."api_group_log" (
  "group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "userid" varchar(255) COLLATE "pg_catalog"."default",
  "time" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for api_log
-- ----------------------------
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
-- Table structure for api_poly
-- ----------------------------
DROP TABLE IF EXISTS "public"."api_poly";
CREATE TABLE "public"."api_poly" (
  "id" text COLLATE "pg_catalog"."default" NOT NULL,
  "api_id" text COLLATE "pg_catalog"."default",
  "api_version" text COLLATE "pg_catalog"."default",
  "poly" jsonb,
  "status" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."api_poly"."id" IS '策略id';
COMMENT ON COLUMN "public"."api_poly"."api_id" IS 'api id';
COMMENT ON COLUMN "public"."api_poly"."api_version" IS 'api 版本';
COMMENT ON COLUMN "public"."api_poly"."poly" IS '策略详情';
COMMENT ON COLUMN "public"."api_poly"."status" IS '审核状态';
COMMENT ON COLUMN "public"."api_poly"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."api_poly"."update_time" IS '升级时间';

-- ----------------------------
-- Table structure for api_relation
-- ----------------------------
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
-- Table structure for apigroup
-- ----------------------------
DROP TABLE IF EXISTS "public"."apigroup";
CREATE TABLE "public"."apigroup" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "parentguid" varchar COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "updatetime" varchar COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
)
;

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
  "publish" bool,
  "used_count" int8,
  "manager" varchar(255) COLLATE "pg_catalog"."default",
  "desensitize" bool,
  "sourcetype" varchar(255) COLLATE "pg_catalog"."default",
  "schemaname" varchar(255) COLLATE "pg_catalog"."default",
  "tablename" varchar(255) COLLATE "pg_catalog"."default",
  "dbname" varchar(255) COLLATE "pg_catalog"."default",
  "sourceid" varchar(255) COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  "pool" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for approval_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."approval_group";
CREATE TABLE "public"."approval_group" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "creator" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "updater" varchar COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "tenantid" varchar COLLATE "pg_catalog"."default",
  "valid" bool
)
;

-- ----------------------------
-- Table structure for approval_group_module_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."approval_group_module_relation";
CREATE TABLE "public"."approval_group_module_relation" (
  "group_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "module_id" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Table structure for approval_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."approval_group_relation";
CREATE TABLE "public"."approval_group_relation" (
  "group_id" varchar(40) COLLATE "pg_catalog"."default",
  "user_id" varchar(40) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for approval_item
-- ----------------------------
DROP TABLE IF EXISTS "public"."approval_item";
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
  "tenant_id" varchar COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for atom_indicator
-- ----------------------------
DROP TABLE IF EXISTS "public"."atom_indicator";
CREATE TABLE "public"."atom_indicator" (
  "id" int8 NOT NULL,
  "atom_indicator_code" varchar(100) COLLATE "pg_catalog"."default",
  "atom_indicator_version_id" int8,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."atom_indicator"."id" IS '主键id';
COMMENT ON COLUMN "public"."atom_indicator"."atom_indicator_code" IS '原子指标编码';
COMMENT ON COLUMN "public"."atom_indicator"."atom_indicator_version_id" IS '当前展示版本id';
COMMENT ON COLUMN "public"."atom_indicator"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."atom_indicator"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."atom_indicator"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."atom_indicator"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."atom_indicator"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."atom_indicator"."tenant_id" IS '租户id';
COMMENT ON TABLE "public"."atom_indicator" IS '原子指标表';

-- ----------------------------
-- Table structure for atom_indicator_apply
-- ----------------------------
DROP TABLE IF EXISTS "public"."atom_indicator_apply";
CREATE TABLE "public"."atom_indicator_apply" (
  "id" int8 NOT NULL,
  "atom_indicator_id" int8,
  "audit_status" int4,
  "apply_status" int4,
  "apply_atom_indicator_id" int8,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."atom_indicator_apply"."id" IS '主键id';
COMMENT ON COLUMN "public"."atom_indicator_apply"."atom_indicator_id" IS '原子指标id';
COMMENT ON COLUMN "public"."atom_indicator_apply"."audit_status" IS '审批状态(1审批通过 2审批不通过 )';
COMMENT ON COLUMN "public"."atom_indicator_apply"."apply_status" IS '申请状态(1发布申请 2取消发布申请 3发布申请撤销 4取消发布撤销)';
COMMENT ON COLUMN "public"."atom_indicator_apply"."apply_atom_indicator_id" IS '审批中版本id';
COMMENT ON COLUMN "public"."atom_indicator_apply"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."atom_indicator_apply"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."atom_indicator_apply"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."atom_indicator_apply"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."atom_indicator_apply"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."atom_indicator_apply" IS '原子指标申请表';

-- ----------------------------
-- Table structure for atom_indicator_logic
-- ----------------------------
DROP TABLE IF EXISTS "public"."atom_indicator_logic";
CREATE TABLE "public"."atom_indicator_logic" (
  "id" int8 NOT NULL,
  "atom_indicator_id" int8,
  "logic_type" int4,
  "indicator_type" varchar(100) COLLATE "pg_catalog"."default",
  "source_field" varchar(100) COLLATE "pg_catalog"."default",
  "field_alias" varchar(255) COLLATE "pg_catalog"."default",
  "data_type" varchar(255) COLLATE "pg_catalog"."default",
  "atom_indicator_sql" text COLLATE "pg_catalog"."default",
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."atom_indicator_logic"."id" IS '主键id';
COMMENT ON COLUMN "public"."atom_indicator_logic"."atom_indicator_id" IS '原子指标id';
COMMENT ON COLUMN "public"."atom_indicator_logic"."logic_type" IS '逻辑配置类型（1基于原子指标配置 2自定义sql）';
COMMENT ON COLUMN "public"."atom_indicator_logic"."indicator_type" IS '类别（维度 度量）';
COMMENT ON COLUMN "public"."atom_indicator_logic"."source_field" IS '源表字段';
COMMENT ON COLUMN "public"."atom_indicator_logic"."field_alias" IS '源表字段别名（另存为）';
COMMENT ON COLUMN "public"."atom_indicator_logic"."data_type" IS '数据类型';
COMMENT ON COLUMN "public"."atom_indicator_logic"."atom_indicator_sql" IS '自定义sql';
COMMENT ON COLUMN "public"."atom_indicator_logic"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."atom_indicator_logic"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."atom_indicator_logic"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."atom_indicator_logic"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."atom_indicator_logic"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."atom_indicator_logic" IS '原子指标逻辑配置表';

-- ----------------------------
-- Table structure for atom_indicator_version
-- ----------------------------
DROP TABLE IF EXISTS "public"."atom_indicator_version";
CREATE TABLE "public"."atom_indicator_version" (
  "id" int8 NOT NULL,
  "atom_indicator_id" int8,
  "version_id" int4,
  "atom_indicator_code" varchar(100) COLLATE "pg_catalog"."default",
  "atom_indicator_name" varchar(100) COLLATE "pg_catalog"."default",
  "business_indicator_id" int8,
  "remark" text COLLATE "pg_catalog"."default",
  "data_source_id" varchar(100) COLLATE "pg_catalog"."default",
  "data_base_id" varchar(100) COLLATE "pg_catalog"."default",
  "data_table_id" varchar(100) COLLATE "pg_catalog"."default",
  "release_status" int4,
  "version_type" int4,
  "visible" int4,
  "release_time" timestamp(6),
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."atom_indicator_version"."id" IS '主键id';
COMMENT ON COLUMN "public"."atom_indicator_version"."atom_indicator_id" IS '原子指标id';
COMMENT ON COLUMN "public"."atom_indicator_version"."version_id" IS '原子指标版本号(0暂存版本 普通版本递增)';
COMMENT ON COLUMN "public"."atom_indicator_version"."atom_indicator_code" IS '原子指标编码';
COMMENT ON COLUMN "public"."atom_indicator_version"."atom_indicator_name" IS '原子指标名称';
COMMENT ON COLUMN "public"."atom_indicator_version"."business_indicator_id" IS '业务指标id';
COMMENT ON COLUMN "public"."atom_indicator_version"."remark" IS '备注';
COMMENT ON COLUMN "public"."atom_indicator_version"."data_source_id" IS '数据源id';
COMMENT ON COLUMN "public"."atom_indicator_version"."data_base_id" IS '数据库id';
COMMENT ON COLUMN "public"."atom_indicator_version"."data_table_id" IS '数据表id';
COMMENT ON COLUMN "public"."atom_indicator_version"."release_status" IS '发布状态(0未发布，1已发布，2审核中)';
COMMENT ON COLUMN "public"."atom_indicator_version"."version_type" IS '版本类型(1发布版本 2暂存版本 3暂存)';
COMMENT ON COLUMN "public"."atom_indicator_version"."visible" IS '是否可见(0不可见 1可见)';
COMMENT ON COLUMN "public"."atom_indicator_version"."release_time" IS '发布时间';
COMMENT ON COLUMN "public"."atom_indicator_version"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."atom_indicator_version"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."atom_indicator_version"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."atom_indicator_version"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."atom_indicator_version"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."atom_indicator_version" IS '原子指标信息版本表';

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
-- Table structure for business_catalog
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_catalog";
CREATE TABLE "public"."business_catalog" (
  "id" int8 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_id" int8 NOT NULL,
  "order_id" int4 NOT NULL,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "role_group" varchar(255) COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "deleted" int2 NOT NULL,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."business_catalog"."id" IS '主键';
COMMENT ON COLUMN "public"."business_catalog"."name" IS '名称';
COMMENT ON COLUMN "public"."business_catalog"."parent_id" IS '父目录';
COMMENT ON COLUMN "public"."business_catalog"."order_id" IS '同级排序';
COMMENT ON COLUMN "public"."business_catalog"."create_user_id" IS '创建者';
COMMENT ON COLUMN "public"."business_catalog"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."business_catalog"."update_user_id" IS '更新者';
COMMENT ON COLUMN "public"."business_catalog"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."business_catalog"."role_group" IS '可访问角色组';
COMMENT ON COLUMN "public"."business_catalog"."description" IS '描述';
COMMENT ON COLUMN "public"."business_catalog"."deleted" IS '是否删除';
COMMENT ON COLUMN "public"."business_catalog"."tenant_id" IS '租户id';

-- ----------------------------
-- Table structure for business_catalog_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_catalog_log";
CREATE TABLE "public"."business_catalog_log" (
  "id" int8 NOT NULL,
  "operate_dir_id" int8 NOT NULL,
  "operate_user" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "operate_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "operate_time" timestamptz(6) NOT NULL,
  "audit_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "audit_pass_time" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."business_catalog_log"."id" IS '主键';
COMMENT ON COLUMN "public"."business_catalog_log"."operate_dir_id" IS '操作的目录id';
COMMENT ON COLUMN "public"."business_catalog_log"."operate_user" IS '操作发起者';
COMMENT ON COLUMN "public"."business_catalog_log"."operate_type" IS '操作类型';
COMMENT ON COLUMN "public"."business_catalog_log"."operate_time" IS '操作发起时间';
COMMENT ON COLUMN "public"."business_catalog_log"."audit_user_id" IS '审批人id';
COMMENT ON COLUMN "public"."business_catalog_log"."audit_pass_time" IS '审批通过时间';
COMMENT ON COLUMN "public"."business_catalog_log"."description" IS '描述';
COMMENT ON COLUMN "public"."business_catalog_log"."tenant_id" IS '租户id';

-- ----------------------------
-- Table structure for business_index_tag_relations
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_index_tag_relations";
CREATE TABLE "public"."business_index_tag_relations" (
  "id" int8 NOT NULL,
  "business_indicator_id" int8 NOT NULL,
  "tag_id" int8 NOT NULL,
  "deleted" int4 DEFAULT 1
)
;
COMMENT ON COLUMN "public"."business_index_tag_relations"."id" IS '业务指标标签关系表ID';
COMMENT ON COLUMN "public"."business_index_tag_relations"."business_indicator_id" IS '业务指标ID';
COMMENT ON COLUMN "public"."business_index_tag_relations"."tag_id" IS '标签ID';
COMMENT ON COLUMN "public"."business_index_tag_relations"."deleted" IS '删除标记（1：启用，0：删除）';

-- ----------------------------
-- Table structure for business_indicators
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_indicators";
CREATE TABLE "public"."business_indicators" (
  "id" int8 NOT NULL,
  "business_indicator_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "business_indicator_coding" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "indicator_group" int8 NOT NULL,
  "business_indicator_cal" varchar(100) COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default",
  "design_formulas" varchar(500) COLLATE "pg_catalog"."default",
  "statistical_cycle" varchar(100) COLLATE "pg_catalog"."default",
  "refresh_rate" varchar(100) COLLATE "pg_catalog"."default",
  "statistical_dimension" varchar(100) COLLATE "pg_catalog"."default",
  "technical_indicator" int8,
  "measurement_object" varchar(100) COLLATE "pg_catalog"."default",
  "technical_unit" varchar(100) COLLATE "pg_catalog"."default",
  "data_precision" int4,
  "is_secret" int4,
  "secret_age" int4,
  "is_important" int4,
  "source_type" varchar(100) COLLATE "pg_catalog"."default",
  "data_provider" varchar(100) COLLATE "pg_catalog"."default",
  "attribute_management_department" varchar(100) COLLATE "pg_catalog"."default",
  "operations_people" varchar(100) COLLATE "pg_catalog"."default",
  "state" int4 NOT NULL DEFAULT 0,
  "create_time" timestamp(6),
  "create_user_id" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "update_user_id" varchar(64) COLLATE "pg_catalog"."default",
  "deleted" int4 NOT NULL DEFAULT 1,
  "business_implication" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."business_indicators"."id" IS '业务指标ID，自增';
COMMENT ON COLUMN "public"."business_indicators"."business_indicator_name" IS '业务指标名称';
COMMENT ON COLUMN "public"."business_indicators"."business_indicator_coding" IS '业务指标编码';
COMMENT ON COLUMN "public"."business_indicators"."indicator_group" IS '业务指标组';
COMMENT ON COLUMN "public"."business_indicators"."business_indicator_cal" IS '指标口径';
COMMENT ON COLUMN "public"."business_indicators"."remark" IS '备注';
COMMENT ON COLUMN "public"."business_indicators"."design_formulas" IS '技术公式';
COMMENT ON COLUMN "public"."business_indicators"."statistical_cycle" IS '统计周期（当前仅支持每日、每周、每月）';
COMMENT ON COLUMN "public"."business_indicators"."refresh_rate" IS '刷新频率（每时、12小时、每日、每月）';
COMMENT ON COLUMN "public"."business_indicators"."statistical_dimension" IS '统计维度。选项为指标维度管理中已配置的维度（忽略业务指标统计维度与技术指标维度不同的情况）';
COMMENT ON COLUMN "public"."business_indicators"."technical_indicator" IS '技术指标（单选，选项为技术指标中已发布的技术指标）';
COMMENT ON COLUMN "public"."business_indicators"."measurement_object" IS '测量对象';
COMMENT ON COLUMN "public"."business_indicators"."technical_unit" IS '计量单位';
COMMENT ON COLUMN "public"."business_indicators"."data_precision" IS '数据精度（当前仅支持小数点后0、1、2、3、4位小数）';
COMMENT ON COLUMN "public"."business_indicators"."is_secret" IS '是否保密（0：否，1是）';
COMMENT ON COLUMN "public"."business_indicators"."secret_age" IS '保密年限（选择保密时显示）';
COMMENT ON COLUMN "public"."business_indicators"."is_important" IS '是否重要（0：否，1是）';
COMMENT ON COLUMN "public"."business_indicators"."source_type" IS '指标来源类型';
COMMENT ON COLUMN "public"."business_indicators"."data_provider" IS '数据提供方';
COMMENT ON COLUMN "public"."business_indicators"."attribute_management_department" IS '业务归口管理部门';
COMMENT ON COLUMN "public"."business_indicators"."operations_people" IS '运维负责人';
COMMENT ON COLUMN "public"."business_indicators"."state" IS '状态（已成功1，未发布0，审核中2）';
COMMENT ON COLUMN "public"."business_indicators"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."business_indicators"."create_user_id" IS '创建的用户ID';
COMMENT ON COLUMN "public"."business_indicators"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."business_indicators"."update_user_id" IS '修改的用户ID';
COMMENT ON COLUMN "public"."business_indicators"."deleted" IS '是否启用（1：启用，0：删除）';
COMMENT ON COLUMN "public"."business_indicators"."business_implication" IS '指标含义';

-- ----------------------------
-- Table structure for business_operation_records
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_operation_records";
CREATE TABLE "public"."business_operation_records" (
  "id" int8 NOT NULL,
  "business_indicator_id" int8,
  "operations_records" varchar(500) COLLATE "pg_catalog"."default",
  "create_user_id" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(0),
  "create_user_name" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."business_operation_records"."id" IS '业务指标操作记录表';
COMMENT ON COLUMN "public"."business_operation_records"."business_indicator_id" IS '业务指标表ID';
COMMENT ON COLUMN "public"."business_operation_records"."operations_records" IS '操作记录';
COMMENT ON COLUMN "public"."business_operation_records"."create_user_id" IS '操作人ID（从sso获取的userinfo）';
COMMENT ON COLUMN "public"."business_operation_records"."create_time" IS '操作时间';
COMMENT ON COLUMN "public"."business_operation_records"."create_user_name" IS '操作人';

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
  "technicalstatus" int2,
  "businessstatus" int2,
  "submitter" varchar COLLATE "pg_catalog"."default",
  "ticketnumber" varchar COLLATE "pg_catalog"."default",
  "submissiontime" varchar COLLATE "pg_catalog"."default",
  "level2categoryid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "trusttable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
)
;

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
  "level" int2,
  "safe" varchar(255) COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(6),
  "updatetime" timestamptz(6),
  "creator" varchar COLLATE "pg_catalog"."default",
  "updater" varchar COLLATE "pg_catalog"."default",
  "code" varchar COLLATE "pg_catalog"."default",
  "sort" int2,
  "private_status" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'PUBLIC'::character varying
)
;
COMMENT ON COLUMN "public"."category"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."category"."creator" IS '创建人';
COMMENT ON COLUMN "public"."category"."updater" IS '更新人';
COMMENT ON COLUMN "public"."category"."code" IS '编码';
COMMENT ON COLUMN "public"."category"."sort" IS '排序';
COMMENT ON COLUMN "public"."category"."private_status" IS '私密状态';

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
-- Table structure for code_annex_type
-- ----------------------------
DROP TABLE IF EXISTS "public"."code_annex_type";
CREATE TABLE "public"."code_annex_type" (
  "code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "action" varchar(50) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."code_annex_type"."code" IS '附件类型';
COMMENT ON COLUMN "public"."code_annex_type"."action" IS '对应的功能模块';

-- ----------------------------
-- Table structure for code_source_info_status
-- ----------------------------
DROP TABLE IF EXISTS "public"."code_source_info_status";
CREATE TABLE "public"."code_source_info_status" (
  "code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."code_source_info_status"."code" IS '源信息状态码';
COMMENT ON COLUMN "public"."code_source_info_status"."name" IS '源信息状态名';

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
  "description" varchar COLLATE "pg_catalog"."default",
  "partition_field" bool
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
COMMENT ON COLUMN "public"."column_info"."partition_field" IS '是否为分区字段';

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
  "update_time" timestamptz(6)
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
-- Table structure for column_tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."column_tag";
CREATE TABLE "public"."column_tag" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "modify_time" timestamp(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."column_tag"."id" IS '标签主键';
COMMENT ON COLUMN "public"."column_tag"."name" IS '标签名';
COMMENT ON COLUMN "public"."column_tag"."tenant_id" IS '创建租户id';
COMMENT ON COLUMN "public"."column_tag"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."column_tag"."modify_time" IS '修改时间';

-- ----------------------------
-- Table structure for column_tag_relation_to_column
-- ----------------------------
DROP TABLE IF EXISTS "public"."column_tag_relation_to_column";
CREATE TABLE "public"."column_tag_relation_to_column" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "column_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "tag_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."column_tag_relation_to_column"."id" IS '关联主键';
COMMENT ON COLUMN "public"."column_tag_relation_to_column"."column_id" IS '字段id';
COMMENT ON COLUMN "public"."column_tag_relation_to_column"."tag_id" IS '字段id';

-- ----------------------------
-- Table structure for connector
-- ----------------------------
DROP TABLE IF EXISTS "public"."connector";
CREATE TABLE "public"."connector" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(256) COLLATE "pg_catalog"."default",
  "connector_class" varchar(512) COLLATE "pg_catalog"."default",
  "db_type" varchar(32) COLLATE "pg_catalog"."default",
  "db_ip" varchar(128) COLLATE "pg_catalog"."default",
  "db_port" int4 DEFAULT 0,
  "pdb_name" varchar(128) COLLATE "pg_catalog"."default",
  "db_name" varchar(128) COLLATE "pg_catalog"."default",
  "db_user" varchar(256) COLLATE "pg_catalog"."default",
  "db_password" varchar(256) COLLATE "pg_catalog"."default",
  "is_deleted" bool DEFAULT false,
  "tasks_max" int4 DEFAULT 1,
  "db_fetch_size" int4 DEFAULT 10,
  "start_scn" int8 DEFAULT '-1'::integer
)
;
COMMENT ON COLUMN "public"."connector"."id" IS 'id';
COMMENT ON COLUMN "public"."connector"."name" IS 'connector名称';
COMMENT ON COLUMN "public"."connector"."connector_class" IS 'connector实现类';
COMMENT ON COLUMN "public"."connector"."db_type" IS '类型:MYSQL,ORACLE,POSTGRE';
COMMENT ON COLUMN "public"."connector"."db_ip" IS 'ip';
COMMENT ON COLUMN "public"."connector"."db_port" IS '端口号';
COMMENT ON COLUMN "public"."connector"."pdb_name" IS 'oracle pdb';
COMMENT ON COLUMN "public"."connector"."db_name" IS '采集最小单元：oracle-实例名称；mysql，PG-数据库名称';
COMMENT ON COLUMN "public"."connector"."db_user" IS '用户名';
COMMENT ON COLUMN "public"."connector"."db_password" IS '数据库密码';
COMMENT ON COLUMN "public"."connector"."is_deleted" IS '是否已删除';
COMMENT ON COLUMN "public"."connector"."tasks_max" IS '最大任务数';
COMMENT ON COLUMN "public"."connector"."db_fetch_size" IS '每次读取的条数';
COMMENT ON COLUMN "public"."connector"."start_scn" IS '数据库初始日志时间戳';

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
  "check_threshold_unit" varchar(255) COLLATE "pg_catalog"."default",
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
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
  "creator" varchar(36) COLLATE "pg_catalog"."default",
  "code" varchar(255) COLLATE "pg_catalog"."default",
  "sql" text COLLATE "pg_catalog"."default",
  "enable" bool
)
;
COMMENT ON COLUMN "public"."data_quality_rule_template"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."data_quality_rule_template"."creator" IS '创建者';
COMMENT ON COLUMN "public"."data_quality_rule_template"."sql" IS '自定义规则的sql语句';
COMMENT ON COLUMN "public"."data_quality_rule_template"."enable" IS '规则状态';

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
  "number" int8 NOT NULL DEFAULT nextval('number_seq'::regclass),
  "qrtz_job" varchar(255) COLLATE "pg_catalog"."default",
  "execution_count" int8,
  "orange_warning_total_count" int8,
  "red_warning_total_count" int8,
  "error_total_count" int8,
  "updater" varchar COLLATE "pg_catalog"."default",
  "current_execution_percent" float4,
  "current_execution_status" int2,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  "pool" varchar(255) COLLATE "pg_catalog"."default",
  "general_warning_total_count" int8 DEFAULT 0
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
COMMENT ON COLUMN "public"."data_quality_task"."current_execution_status" IS '执行状态:1-执行中,2-成功,3-失败,0-待执行,4-取消';
COMMENT ON COLUMN "public"."data_quality_task"."general_warning_total_count" IS '普通告警总数统计';

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
  "error_status" int2,
  "general_warning_count" int4
)
;
COMMENT ON COLUMN "public"."data_quality_task_execute"."task_id" IS '所属任务id';
COMMENT ON COLUMN "public"."data_quality_task_execute"."percent" IS '执行进度';
COMMENT ON COLUMN "public"."data_quality_task_execute"."execute_status" IS '执行状态:1-执行中,2-成功,3-失败,0-未执行,4-取消';
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
COMMENT ON COLUMN "public"."data_quality_task_execute"."general_warning_count" IS '普通告警数';

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
  "rule_id" varchar(36) COLLATE "pg_catalog"."default",
  "general_warning_check_status" int2
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
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."general_warning_check_status" IS '一般告警：0-无告警,1-有告警，2-已关闭';

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
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "manager" varchar(255) COLLATE "pg_catalog"."default",
  "isapi" bool,
  "oracle_db" varchar(255) COLLATE "pg_catalog"."default",
  "servicetype" varchar(255) COLLATE "pg_catalog"."default",
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
COMMENT ON COLUMN "public"."data_source"."manager" IS '数据源管理者id';

-- ----------------------------
-- Table structure for data_source_api_authorize
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_source_api_authorize";
CREATE TABLE "public"."data_source_api_authorize" (
  "source_id" varchar(225) COLLATE "pg_catalog"."default" NOT NULL,
  "authorize_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

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
COMMENT ON COLUMN "public"."data_standard"."tenantid" IS '租户id';

-- ----------------------------
-- Table structure for data_standard2data_quality_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_standard2data_quality_rule";
CREATE TABLE "public"."data_standard2data_quality_rule" (
  "number" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ruleid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(6),
  "operator" varchar COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for data_standard2table
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_standard2table";
CREATE TABLE "public"."data_standard2table" (
  "number" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(6),
  "operator" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for database_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."database_group_relation";
CREATE TABLE "public"."database_group_relation" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "source_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "database_guid" varchar(36) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."database_group_relation"."id" IS 'id';
COMMENT ON COLUMN "public"."database_group_relation"."group_id" IS '用户组id';
COMMENT ON COLUMN "public"."database_group_relation"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."database_group_relation"."database_guid" IS '数据库id';

-- ----------------------------
-- Table structure for datasource_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."datasource_group_relation";
CREATE TABLE "public"."datasource_group_relation" (
  "source_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "privilege_code" varchar(36) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Table structure for db_category_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."db_category_relation";
CREATE TABLE "public"."db_category_relation" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "category_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "db_guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."db_category_relation"."id" IS 'id';
COMMENT ON COLUMN "public"."db_category_relation"."category_id" IS '目录id';
COMMENT ON COLUMN "public"."db_category_relation"."db_guid" IS '数据库id';
COMMENT ON COLUMN "public"."db_category_relation"."tenant_id" IS '租户id';

-- ----------------------------
-- Table structure for db_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."db_info";
CREATE TABLE "public"."db_info" (
  "database_guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "database_name" varchar(256) COLLATE "pg_catalog"."default",
  "owner" varchar(256) COLLATE "pg_catalog"."default",
  "db_type" varchar(64) COLLATE "pg_catalog"."default",
  "status" varchar(64) COLLATE "pg_catalog"."default",
  "database_description" varchar(255) COLLATE "pg_catalog"."default",
  "instance_guid" varchar COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."db_info"."database_guid" IS '唯一标识符';
COMMENT ON COLUMN "public"."db_info"."database_name" IS '数据库名称';
COMMENT ON COLUMN "public"."db_info"."owner" IS '创建者';
COMMENT ON COLUMN "public"."db_info"."db_type" IS '数据库类型';
COMMENT ON COLUMN "public"."db_info"."status" IS '状态:已删除-DELETED;未删除-ACTIVE';
COMMENT ON COLUMN "public"."db_info"."database_description" IS '数据库描述';
COMMENT ON COLUMN "public"."db_info"."instance_guid" IS '图数据库中数据源（实例）guid';

-- ----------------------------
-- Table structure for desensitization_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."desensitization_rule";
CREATE TABLE "public"."desensitization_rule" (
  "id" text COLLATE "pg_catalog"."default" NOT NULL,
  "name" text COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "type" text COLLATE "pg_catalog"."default",
  "params" text COLLATE "pg_catalog"."default",
  "enable" bool,
  "creator_id" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "tenant_id" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."desensitization_rule"."id" IS '脱敏规则id';
COMMENT ON COLUMN "public"."desensitization_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."desensitization_rule"."description" IS '描述';
COMMENT ON COLUMN "public"."desensitization_rule"."type" IS '脱敏算法类型';
COMMENT ON COLUMN "public"."desensitization_rule"."params" IS '脱敏算法参数';
COMMENT ON COLUMN "public"."desensitization_rule"."enable" IS '是否启用';
COMMENT ON COLUMN "public"."desensitization_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."desensitization_rule"."update_time" IS '升级时间';
COMMENT ON COLUMN "public"."desensitization_rule"."tenant_id" IS '租户 Id';

-- ----------------------------
-- Table structure for dimension
-- ----------------------------
DROP TABLE IF EXISTS "public"."dimension";
CREATE TABLE "public"."dimension" (
  "id" int8 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "code" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "describe" varchar(255) COLLATE "pg_catalog"."default",
  "data_type" int4,
  "explain_people" varchar(255) COLLATE "pg_catalog"."default",
  "note" varchar(255) COLLATE "pg_catalog"."default",
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "deleted" int4,
  "dimension_id" int8,
  "source_id" varchar(255) COLLATE "pg_catalog"."default",
  "database_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."dimension"."id" IS '主键id';
COMMENT ON COLUMN "public"."dimension"."name" IS '维度名称';
COMMENT ON COLUMN "public"."dimension"."code" IS '维度编码';
COMMENT ON COLUMN "public"."dimension"."describe" IS '维度描述';
COMMENT ON COLUMN "public"."dimension"."data_type" IS '数据类型 (0字符 1整数 2日期)';
COMMENT ON COLUMN "public"."dimension"."explain_people" IS '解释人';
COMMENT ON COLUMN "public"."dimension"."note" IS '备注';
COMMENT ON COLUMN "public"."dimension"."update_user_id" IS '修改人id';
COMMENT ON COLUMN "public"."dimension"."create_user_id" IS '创建人id';
COMMENT ON COLUMN "public"."dimension"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."dimension"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."dimension"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."dimension"."deleted" IS '逻辑删除位';
COMMENT ON COLUMN "public"."dimension"."dimension_id" IS '维度id';
COMMENT ON COLUMN "public"."dimension"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."dimension"."database_id" IS '数据库id';

-- ----------------------------
-- Table structure for dimension_metadata_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."dimension_metadata_relation";
CREATE TABLE "public"."dimension_metadata_relation" (
  "id" int8 NOT NULL,
  "target_field" varchar(255) COLLATE "pg_catalog"."default",
  "note" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "dimension_id" int8 NOT NULL,
  "deleted" int4 NOT NULL,
  "table_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."dimension_metadata_relation"."id" IS '主键id';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."target_field" IS '目标字段';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."note" IS '备注';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."dimension_id" IS '维度id';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."deleted" IS '逻辑删除位';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."table_id" IS '数据表id';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."create_user_id" IS '创建人id';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."update_user_id" IS '修改人id';

-- ----------------------------
-- Table structure for index_atomic_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."index_atomic_info";
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

-- ----------------------------
-- Table structure for index_composite_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."index_composite_info";
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

-- ----------------------------
-- Table structure for index_derive_composite_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."index_derive_composite_relation";
CREATE TABLE "public"."index_derive_composite_relation" (
  "derive_index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "composite_index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."index_derive_composite_relation"."derive_index_id" IS '派生指标id';
COMMENT ON COLUMN "public"."index_derive_composite_relation"."composite_index_id" IS '复合指标id';

-- ----------------------------
-- Table structure for index_derive_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."index_derive_info";
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

-- ----------------------------
-- Table structure for index_derive_modifier_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."index_derive_modifier_relation";
CREATE TABLE "public"."index_derive_modifier_relation" (
  "derive_index_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "modifier_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."index_derive_modifier_relation"."derive_index_id" IS '派生指标id';
COMMENT ON COLUMN "public"."index_derive_modifier_relation"."modifier_id" IS '修饰词id';

-- ----------------------------
-- Table structure for ip_restriction
-- ----------------------------
DROP TABLE IF EXISTS "public"."ip_restriction";
CREATE TABLE "public"."ip_restriction" (
  "id" text COLLATE "pg_catalog"."default" NOT NULL,
  "name" text COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "type" text COLLATE "pg_catalog"."default",
  "ip_list" text COLLATE "pg_catalog"."default",
  "enable" bool,
  "creator_id" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "tenant_id" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."ip_restriction"."id" IS '黑白名单ID';
COMMENT ON COLUMN "public"."ip_restriction"."name" IS '名称';
COMMENT ON COLUMN "public"."ip_restriction"."description" IS '描述';
COMMENT ON COLUMN "public"."ip_restriction"."type" IS '类型';
COMMENT ON COLUMN "public"."ip_restriction"."ip_list" IS 'Ip 列表';
COMMENT ON COLUMN "public"."ip_restriction"."enable" IS '是否启用';
COMMENT ON COLUMN "public"."ip_restriction"."creator_id" IS '创建者Id';
COMMENT ON COLUMN "public"."ip_restriction"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ip_restriction"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."ip_restriction"."tenant_id" IS '租户';
COMMENT ON TABLE "public"."ip_restriction" IS '黑白名单';

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
-- Table structure for module
-- ----------------------------
DROP TABLE IF EXISTS "public"."module";
CREATE TABLE "public"."module" (
  "moduleid" int4 NOT NULL,
  "modulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" int4
)
;
COMMENT ON COLUMN "public"."module"."moduleid" IS '权限id';
COMMENT ON COLUMN "public"."module"."modulename" IS '权限名';
COMMENT ON COLUMN "public"."module"."type" IS '模块类型';

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
COMMENT ON COLUMN "public"."operate_log"."tenantid" IS '租户id';

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS "public"."organization";
CREATE TABLE "public"."organization" (
  "checked" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "disable" varchar COLLATE "pg_catalog"."default",
  "id" varchar COLLATE "pg_catalog"."default",
  "isopen" bool,
  "isvm" int8,
  "name" text COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "open" bool,
  "pid" varchar COLLATE "pg_catalog"."default",
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ptype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "updatetime" varchar COLLATE "pg_catalog"."default"
)
;

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
  "moduleid" int4 NOT NULL
)
;

-- ----------------------------
-- Table structure for project
-- ----------------------------
DROP TABLE IF EXISTS "public"."project";
CREATE TABLE "public"."project" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "creator" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(6) NOT NULL,
  "manager" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "valid" bool NOT NULL
)
;

-- ----------------------------
-- Table structure for project_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."project_group_relation";
CREATE TABLE "public"."project_group_relation" (
  "project_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL
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
  "blob_data" bytea
)
;

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
  "last_checkin_time" int8 NOT NULL,
  "checkin_interval" int8 NOT NULL
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
  "repeat_count" int8 NOT NULL,
  "repeat_interval" int8 NOT NULL,
  "times_triggered" int8 NOT NULL
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

-- ----------------------------
-- Table structure for qualifier
-- ----------------------------
DROP TABLE IF EXISTS "public"."qualifier";
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
  "typeid" varchar COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for qualifier_type
-- ----------------------------
DROP TABLE IF EXISTS "public"."qualifier_type";
CREATE TABLE "public"."qualifier_type" (
  "type_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "type_name" varchar COLLATE "pg_catalog"."default",
  "type_mark" varchar COLLATE "pg_catalog"."default",
  "creator" varchar COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_user" varchar COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "type_desc" text COLLATE "pg_catalog"."default",
  "tenantid" varchar COLLATE "pg_catalog"."default"
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
  "orangealerts" int8,
  "redalerts" int8,
  "source" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "buildtype" int4,
  "reportproducedate" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "templateid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "alert" int2
)
;

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

-- ----------------------------
-- Table structure for report_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_userrule2threshold";
CREATE TABLE "public"."report_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for rule2buildtype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2buildtype";
CREATE TABLE "public"."rule2buildtype" (
  "ruleid" int2 NOT NULL,
  "buildtype" int2 NOT NULL
)
;

-- ----------------------------
-- Table structure for rule2checktype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2checktype";
CREATE TABLE "public"."rule2checktype" (
  "ruleid" int2 NOT NULL,
  "checktype" int2 NOT NULL
)
;

-- ----------------------------
-- Table structure for rule2datatype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2datatype";
CREATE TABLE "public"."rule2datatype" (
  "ruleid" int2 NOT NULL,
  "datatype" int2 NOT NULL
)
;

-- ----------------------------
-- Table structure for source_db
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_db";
CREATE TABLE "public"."source_db" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "db_guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."source_db"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."source_db"."db_guid" IS '数据库id';

-- ----------------------------
-- Table structure for source_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_info";
CREATE TABLE "public"."source_info" (
  "id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "category_id" varchar(64) COLLATE "pg_catalog"."default",
  "database_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "database_alias" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "planning_package_code" varchar(255) COLLATE "pg_catalog"."default",
  "planning_package_name" varchar(255) COLLATE "pg_catalog"."default",
  "extract_tool" varchar(255) COLLATE "pg_catalog"."default",
  "extract_cycle" varchar(255) COLLATE "pg_catalog"."default",
  "security" bool,
  "security_cycle" varchar(255) COLLATE "pg_catalog"."default",
  "importance" bool,
  "description" text COLLATE "pg_catalog"."default",
  "approve_id" varchar(50) COLLATE "pg_catalog"."default",
  "approve_group_id" varchar(64) COLLATE "pg_catalog"."default",
  "updater" varchar(255) COLLATE "pg_catalog"."default",
  "creator" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "status" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "annex_id" varchar(64) COLLATE "pg_catalog"."default",
  "bo_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "bo_department_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "bo_email" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "bo_tel" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "to_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "to_department_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "to_email" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "to_tel" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "technical_leader" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "business_leader" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "version" int8 NOT NULL,
  "update_time" timestamptz(6),
  "record_time" timestamptz(6) NOT NULL,
  "create_time" timestamptz(6) NOT NULL,
  "modify_time" timestamptz(6) NOT NULL,
  "data_source_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."source_info"."category_id" IS '数据层/区Id';
COMMENT ON COLUMN "public"."source_info"."database_id" IS '所选数据库实例id';
COMMENT ON COLUMN "public"."source_info"."database_alias" IS '数据库别名（中文名）';
COMMENT ON COLUMN "public"."source_info"."planning_package_code" IS '规划包编号';
COMMENT ON COLUMN "public"."source_info"."planning_package_name" IS '规划包名称';
COMMENT ON COLUMN "public"."source_info"."extract_tool" IS '抽取工具';
COMMENT ON COLUMN "public"."source_info"."extract_cycle" IS '抽取周期';
COMMENT ON COLUMN "public"."source_info"."security" IS '是否保密';
COMMENT ON COLUMN "public"."source_info"."security_cycle" IS '保密期限';
COMMENT ON COLUMN "public"."source_info"."importance" IS '是否重要';
COMMENT ON COLUMN "public"."source_info"."description" IS '描述';
COMMENT ON COLUMN "public"."source_info"."approve_id" IS '对接审批系统id';
COMMENT ON COLUMN "public"."source_info"."approve_group_id" IS '送审审批组id';
COMMENT ON COLUMN "public"."source_info"."updater" IS '更新人账号';
COMMENT ON COLUMN "public"."source_info"."creator" IS '记录人账号';
COMMENT ON COLUMN "public"."source_info"."status" IS '源信息状态';
COMMENT ON COLUMN "public"."source_info"."annex_id" IS '附件id';
COMMENT ON COLUMN "public"."source_info"."bo_name" IS '业务对接人姓名(business_owner_name)';
COMMENT ON COLUMN "public"."source_info"."bo_department_name" IS '业务对接人部门名称(business_owner_department_name)';
COMMENT ON COLUMN "public"."source_info"."bo_email" IS '业务对接人邮件地址(business_owner_email)';
COMMENT ON COLUMN "public"."source_info"."bo_tel" IS '业务对接人电话(business_owner_tel)';
COMMENT ON COLUMN "public"."source_info"."to_name" IS '技术对接人姓名(tech_owner)';
COMMENT ON COLUMN "public"."source_info"."to_department_name" IS '技术对接人部门名称(technology_owner_department_name)';
COMMENT ON COLUMN "public"."source_info"."to_email" IS '技术对接人邮件地址(technology_owner_email)';
COMMENT ON COLUMN "public"."source_info"."to_tel" IS '技术对接人电话(technology_owner_tel)';
COMMENT ON COLUMN "public"."source_info"."technical_leader" IS '技术主管';
COMMENT ON COLUMN "public"."source_info"."business_leader" IS '业务主管';
COMMENT ON COLUMN "public"."source_info"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."source_info"."version" IS '版本号';
COMMENT ON COLUMN "public"."source_info"."update_time" IS '信息最后更新时间';
COMMENT ON COLUMN "public"."source_info"."record_time" IS '信息录入时间';
COMMENT ON COLUMN "public"."source_info"."create_time" IS '数据创建时间戳';
COMMENT ON COLUMN "public"."source_info"."modify_time" IS '数据修改时间戳';
COMMENT ON COLUMN "public"."source_info"."data_source_id" IS '数据源id';
COMMENT ON TABLE "public"."source_info" IS '用于储存源信息';

-- ----------------------------
-- Table structure for source_info_derive_column_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_info_derive_column_info";
CREATE TABLE "public"."source_info_derive_column_info" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "column_guid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "column_name_en" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "column_name_zh" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "data_type" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "source_column_guid" varchar(100) COLLATE "pg_catalog"."default",
  "primary_key" bool NOT NULL,
  "remove_sensitive" bool NOT NULL,
  "mapping_rule" varchar(512) COLLATE "pg_catalog"."default",
  "mapping_describe" varchar(512) COLLATE "pg_catalog"."default",
  "group_field" bool NOT NULL,
  "secret" bool NOT NULL,
  "secret_period" varchar(64) COLLATE "pg_catalog"."default",
  "important" bool NOT NULL,
  "remark" text COLLATE "pg_catalog"."default",
  "tenant_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "table_guid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "permission_field" bool NOT NULL
)
;
COMMENT ON COLUMN "public"."source_info_derive_column_info"."id" IS '主键id';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."column_guid" IS '字段的guid';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."column_name_en" IS '字段英文名';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."column_name_zh" IS '字段中文名（描述信息）';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."data_type" IS '数据类型';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."source_column_guid" IS '来源字段的guid（column_info）';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."primary_key" IS '是否是主键';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."remove_sensitive" IS '是否脱敏';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."mapping_rule" IS '映射规则';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."mapping_describe" IS '映射说明';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."group_field" IS '是否是分组字段';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."secret" IS '是否保密';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."secret_period" IS '保密期限';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."important" IS '是否重要';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."remark" IS '备注';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."table_guid" IS '表的guid';
COMMENT ON COLUMN "public"."source_info_derive_column_info"."permission_field" IS '是否为权限字段';
COMMENT ON TABLE "public"."source_info_derive_column_info" IS '衍生表对应的字段';

-- ----------------------------
-- Table structure for source_info_derive_table_column_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_info_derive_table_column_relation";
CREATE TABLE "public"."source_info_derive_table_column_relation" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "table_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "column_guid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "table_guid" varchar(100) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."source_info_derive_table_column_relation"."id" IS '主键id';
COMMENT ON COLUMN "public"."source_info_derive_table_column_relation"."table_id" IS '衍生表的主键id';
COMMENT ON COLUMN "public"."source_info_derive_table_column_relation"."column_guid" IS '衍生表字段的guid';
COMMENT ON COLUMN "public"."source_info_derive_table_column_relation"."table_guid" IS '衍生表的guid';
COMMENT ON TABLE "public"."source_info_derive_table_column_relation" IS '衍生表和字段的关联关系';

-- ----------------------------
-- Table structure for source_info_derive_table_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_info_derive_table_info";
CREATE TABLE "public"."source_info_derive_table_info" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "table_guid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "table_name_en" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "table_name_zh" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "procedure" varchar(256) COLLATE "pg_catalog"."default",
  "category_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "db_type" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "db_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "source_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "business_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "update_frequency" varchar(256) COLLATE "pg_catalog"."default",
  "etl_policy" varchar(256) COLLATE "pg_catalog"."default",
  "incre_standard" varchar(256) COLLATE "pg_catalog"."default",
  "clean_rule" varchar(256) COLLATE "pg_catalog"."default",
  "filter" varchar(256) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(100) COLLATE "pg_catalog"."default",
  "remark" text COLLATE "pg_catalog"."default",
  "version" int4 NOT NULL,
  "source_table_guid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "creator" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(0) NOT NULL,
  "updater" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "update_time" timestamp(0) NOT NULL,
  "ddl" text COLLATE "pg_catalog"."default",
  "dml" text COLLATE "pg_catalog"."default",
  "state" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."source_info_derive_table_info"."id" IS '主键id';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."table_guid" IS '衍生表的guid';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."table_name_en" IS '表英文名';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."table_name_zh" IS '表中文名';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."procedure" IS '存储过程';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."category_id" IS '技术目录对应id';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."db_type" IS '目标层级/库类型';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."db_id" IS '目标数据库id';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."source_id" IS '目标数据源id';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."business_id" IS '业务目录对应id';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."update_frequency" IS '更新频率';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."etl_policy" IS 'etl策略';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."incre_standard" IS '增量抽取标准';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."clean_rule" IS '目标清洗规则';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."filter" IS '过滤条件';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."remark" IS '备注';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."version" IS '版本号';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."source_table_guid" IS '源表的guid';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."creator" IS '设计人';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."updater" IS '修改人';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."ddl" IS '生成的ddl语句';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."dml" IS '生成的dml语句';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."state" IS '0：未提交，1：已提交';
COMMENT ON TABLE "public"."source_info_derive_table_info" IS '衍生表信息表';

-- ----------------------------
-- Table structure for source_info_relation2parent_category
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_info_relation2parent_category";
CREATE TABLE "public"."source_info_relation2parent_category" (
  "source_info_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_category_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6),
  "modify_time" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."source_info_relation2parent_category"."source_info_id" IS '源信息id';
COMMENT ON COLUMN "public"."source_info_relation2parent_category"."parent_category_id" IS '源信息关联库目录创建前的父级目录id';

-- ----------------------------
-- Table structure for statistical
-- ----------------------------
DROP TABLE IF EXISTS "public"."statistical";
CREATE TABLE "public"."statistical" (
  "statisticalid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "date" int8,
  "statistical" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "statisticaltypeid" int4,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for statisticaltype
-- ----------------------------
DROP TABLE IF EXISTS "public"."statisticaltype";
CREATE TABLE "public"."statisticaltype" (
  "statisticaltypeid" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for sync_task_definition
-- ----------------------------
DROP TABLE IF EXISTS "public"."sync_task_definition";
CREATE TABLE "public"."sync_task_definition" (
  "id" text COLLATE "pg_catalog"."default" NOT NULL,
  "name" text COLLATE "pg_catalog"."default",
  "creator" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "enable" bool,
  "cron_start_time" timestamptz(6),
  "cron_end_time" timestamptz(6),
  "crontab" text COLLATE "pg_catalog"."default",
  "data_source_id" text COLLATE "pg_catalog"."default",
  "sync_all" bool,
  "schemas" text COLLATE "pg_catalog"."default",
  "tenant_id" text COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "category_guid" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sync_task_definition"."id" IS 'id';
COMMENT ON COLUMN "public"."sync_task_definition"."name" IS '名称';
COMMENT ON COLUMN "public"."sync_task_definition"."creator" IS '创建者';
COMMENT ON COLUMN "public"."sync_task_definition"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sync_task_definition"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sync_task_definition"."enable" IS '是否启动定时';
COMMENT ON COLUMN "public"."sync_task_definition"."cron_start_time" IS '定时开始时间';
COMMENT ON COLUMN "public"."sync_task_definition"."cron_end_time" IS '定时结束时间';
COMMENT ON COLUMN "public"."sync_task_definition"."crontab" IS '定时表达式';
COMMENT ON COLUMN "public"."sync_task_definition"."data_source_id" IS '数据源 id';
COMMENT ON COLUMN "public"."sync_task_definition"."sync_all" IS '是否同步所有数据库';
COMMENT ON COLUMN "public"."sync_task_definition"."schemas" IS '指定数据库列表';
COMMENT ON COLUMN "public"."sync_task_definition"."tenant_id" IS '租户';
COMMENT ON COLUMN "public"."sync_task_definition"."category_guid" IS '技术目录guid';

-- ----------------------------
-- Table structure for sync_task_instance
-- ----------------------------
DROP TABLE IF EXISTS "public"."sync_task_instance";
CREATE TABLE "public"."sync_task_instance" (
  "id" text COLLATE "pg_catalog"."default" NOT NULL,
  "name" text COLLATE "pg_catalog"."default",
  "executor" text COLLATE "pg_catalog"."default",
  "status" text COLLATE "pg_catalog"."default",
  "start_time" timestamptz(6),
  "update_time" timestamptz(6),
  "log" text COLLATE "pg_catalog"."default" DEFAULT ''::text,
  "definition_id" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sync_task_instance"."id" IS 'id';
COMMENT ON COLUMN "public"."sync_task_instance"."name" IS '任务实例名称';
COMMENT ON COLUMN "public"."sync_task_instance"."executor" IS '执行者名称';
COMMENT ON COLUMN "public"."sync_task_instance"."status" IS '状态';
COMMENT ON COLUMN "public"."sync_task_instance"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."sync_task_instance"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sync_task_instance"."log" IS '日志';
COMMENT ON COLUMN "public"."sync_task_instance"."definition_id" IS '租户';

-- ----------------------------
-- Table structure for systemrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."systemrule";
CREATE TABLE "public"."systemrule" (
  "ruleid" int2 NOT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "ruleinfo" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "ruletype" int2,
  "rulecheckthresholdunit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
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
-- Table structure for table2tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."table2tag";
CREATE TABLE "public"."table2tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Table structure for table_data_source_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."table_data_source_relation";
CREATE TABLE "public"."table_data_source_relation" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "category_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "table_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "data_source_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6) NOT NULL,
  "update_time" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."table_data_source_relation"."id" IS '主键id';
COMMENT ON COLUMN "public"."table_data_source_relation"."category_id" IS '目录id';
COMMENT ON COLUMN "public"."table_data_source_relation"."table_id" IS '表id';
COMMENT ON COLUMN "public"."table_data_source_relation"."data_source_id" IS '数据源id';
COMMENT ON COLUMN "public"."table_data_source_relation"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."table_data_source_relation"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."table_data_source_relation"."update_time" IS '更新时间';

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

-- ----------------------------
-- Table structure for table_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."table_relation";
CREATE TABLE "public"."table_relation" (
  "relationshipguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "categoryguid" varchar COLLATE "pg_catalog"."default",
  "tableguid" varchar COLLATE "pg_catalog"."default",
  "generatetime" varchar COLLATE "pg_catalog"."default",
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default"
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
  "dataowner" json,
  "databaseguid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
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
  "description" varchar COLLATE "pg_catalog"."default",
  "source_id" text COLLATE "pg_catalog"."default",
  "type" varchar(32) COLLATE "pg_catalog"."default" DEFAULT 'table'::character varying,
  "owner" varchar(32) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."tableinfo"."description" IS '描述';

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."tag";
CREATE TABLE "public"."tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tagname" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tenantid" varchar(36) COLLATE "pg_catalog"."default"
)
;

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

-- ----------------------------
-- Table structure for template2qrtz_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."template2qrtz_job";
CREATE TABLE "public"."template2qrtz_job" (
  "templateid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "qrtz_job" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

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

-- ----------------------------
-- Table structure for template_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."template_userrule2threshold";
CREATE TABLE "public"."template_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for tenant
-- ----------------------------
DROP TABLE IF EXISTS "public"."tenant";
CREATE TABLE "public"."tenant" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."tenant"."id" IS '租户id';
COMMENT ON COLUMN "public"."tenant"."name" IS '租户名字';

-- ----------------------------
-- Table structure for time_limit
-- ----------------------------
DROP TABLE IF EXISTS "public"."time_limit";
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
  "time_range" int4
)
;

-- ----------------------------
-- Table structure for user2apistar
-- ----------------------------
DROP TABLE IF EXISTS "public"."user2apistar";
CREATE TABLE "public"."user2apistar" (
  "apiguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "userid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;

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
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS "public"."users";
CREATE TABLE "public"."users" (
  "userid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "username" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "account" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "roleid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "valid" bool
)
;
COMMENT ON COLUMN "public"."users"."userid" IS '用户id';
COMMENT ON COLUMN "public"."users"."username" IS '用户名';
COMMENT ON COLUMN "public"."users"."account" IS '用户账号';
COMMENT ON COLUMN "public"."users"."roleid" IS '用户角色id';

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
-- View structure for test_view
-- ----------------------------
DROP VIEW IF EXISTS "public"."test_view";
CREATE VIEW "public"."test_view" AS  SELECT tag.tagid,
    tag.tagname,
    tag.tenantid
   FROM tag;

-- ----------------------------
-- Primary Key structure for table annex
-- ----------------------------
ALTER TABLE "public"."annex" ADD CONSTRAINT "annex_pkey" PRIMARY KEY ("annex_id");

-- ----------------------------
-- Primary Key structure for table api
-- ----------------------------
ALTER TABLE "public"."api" ADD CONSTRAINT "api_pkey" PRIMARY KEY ("guid", "version_num");

-- ----------------------------
-- Primary Key structure for table api_audit
-- ----------------------------
ALTER TABLE "public"."api_audit" ADD CONSTRAINT "api_audit_pk" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table api_category
-- ----------------------------
ALTER TABLE "public"."api_category" ADD CONSTRAINT "api_category_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table api_group
-- ----------------------------
ALTER TABLE "public"."api_group" ADD CONSTRAINT "api_group_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table api_module
-- ----------------------------
ALTER TABLE "public"."api_module" ADD CONSTRAINT "api_module_pkey" PRIMARY KEY ("path", "method");

-- ----------------------------
-- Primary Key structure for table api_poly
-- ----------------------------
ALTER TABLE "public"."api_poly" ADD CONSTRAINT "api_poly_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table api_relation
-- ----------------------------
ALTER TABLE "public"."api_relation" ADD CONSTRAINT "api_relation_pkey" PRIMARY KEY ("apiid", "groupid");

-- ----------------------------
-- Primary Key structure for table apigroup
-- ----------------------------
ALTER TABLE "public"."apigroup" ADD CONSTRAINT "apigroup_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table apiinfo
-- ----------------------------
ALTER TABLE "public"."apiinfo" ADD CONSTRAINT "apiinfo_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Primary Key structure for table approval_group
-- ----------------------------
ALTER TABLE "public"."approval_group" ADD CONSTRAINT "approval_group_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table approval_group_module_relation
-- ----------------------------
ALTER TABLE "public"."approval_group_module_relation" ADD CONSTRAINT "approval_group_module_relation_pk" PRIMARY KEY ("group_id", "module_id");

-- ----------------------------
-- Primary Key structure for table approval_item
-- ----------------------------
ALTER TABLE "public"."approval_item" ADD CONSTRAINT "approval_item_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table atom_indicator
-- ----------------------------
ALTER TABLE "public"."atom_indicator" ADD CONSTRAINT "atom_indicator_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table atom_indicator_apply
-- ----------------------------
ALTER TABLE "public"."atom_indicator_apply" ADD CONSTRAINT "atom_indicator_apply_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table atom_indicator_logic
-- ----------------------------
ALTER TABLE "public"."atom_indicator_logic" ADD CONSTRAINT "atom_indicator_logic_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table atom_indicator_version
-- ----------------------------
ALTER TABLE "public"."atom_indicator_version" ADD CONSTRAINT "unique_atom_indicator_version" UNIQUE ("atom_indicator_id", "version_id");

-- ----------------------------
-- Primary Key structure for table atom_indicator_version
-- ----------------------------
ALTER TABLE "public"."atom_indicator_version" ADD CONSTRAINT "atom_indicator_version_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business2table
-- ----------------------------
ALTER TABLE "public"."business2table" ADD CONSTRAINT "business2table_pkey" PRIMARY KEY ("businessid", "tableguid");

-- ----------------------------
-- Primary Key structure for table business_catalog
-- ----------------------------
ALTER TABLE "public"."business_catalog" ADD CONSTRAINT "business_catalog_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_catalog_log
-- ----------------------------
ALTER TABLE "public"."business_catalog_log" ADD CONSTRAINT "business_catalog_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_index_tag_relations
-- ----------------------------
ALTER TABLE "public"."business_index_tag_relations" ADD CONSTRAINT "business_index_tag_relations_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_indicators
-- ----------------------------
ALTER TABLE "public"."business_indicators" ADD CONSTRAINT "business_indicators_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_operation_records
-- ----------------------------
ALTER TABLE "public"."business_operation_records" ADD CONSTRAINT "business_operation_records_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table category_group_relation
-- ----------------------------
ALTER TABLE "public"."category_group_relation" ADD CONSTRAINT "category_group_relation_pkey" PRIMARY KEY ("category_id", "group_id");

-- ----------------------------
-- Primary Key structure for table code_annex_type
-- ----------------------------
ALTER TABLE "public"."code_annex_type" ADD CONSTRAINT "code_annex_type_pkey" PRIMARY KEY ("code");

-- ----------------------------
-- Primary Key structure for table code_source_info_status
-- ----------------------------
ALTER TABLE "public"."code_source_info_status" ADD CONSTRAINT "code_source_info_status_pkey" PRIMARY KEY ("code");

-- ----------------------------
-- Primary Key structure for table column_info
-- ----------------------------
ALTER TABLE "public"."column_info" ADD CONSTRAINT "column_info_pkey" PRIMARY KEY ("column_guid");

-- ----------------------------
-- Primary Key structure for table column_metadata_history
-- ----------------------------
ALTER TABLE "public"."column_metadata_history" ADD CONSTRAINT "column_metadata_history_pkey" PRIMARY KEY ("guid", "version");

-- ----------------------------
-- Primary Key structure for table connector
-- ----------------------------
ALTER TABLE "public"."connector" ADD CONSTRAINT "connector_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_quality_rule_template
-- ----------------------------
ALTER TABLE "public"."data_quality_rule_template" ADD CONSTRAINT "data_quality_rule_template_pkey" PRIMARY KEY ("id", "tenantid");

-- ----------------------------
-- Primary Key structure for table data_quality_task
-- ----------------------------
ALTER TABLE "public"."data_quality_task" ADD CONSTRAINT "data_quality_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_quality_task2warning_group
-- ----------------------------
ALTER TABLE "public"."data_quality_task2warning_group" ADD CONSTRAINT "data_quality_task2warning_group_pkey" PRIMARY KEY ("task_id", "warning_group_id", "warning_type");

-- ----------------------------
-- Primary Key structure for table data_source
-- ----------------------------
ALTER TABLE "public"."data_source" ADD CONSTRAINT "data_source_pkey1" PRIMARY KEY ("source_id");

-- ----------------------------
-- Primary Key structure for table data_source_api_authorize
-- ----------------------------
ALTER TABLE "public"."data_source_api_authorize" ADD CONSTRAINT "data_source_api_authorize_pkey" PRIMARY KEY ("source_id", "authorize_user_id");

-- ----------------------------
-- Primary Key structure for table data_source_authorize
-- ----------------------------
ALTER TABLE "public"."data_source_authorize" ADD CONSTRAINT "data_source_authorize_pkey" PRIMARY KEY ("source_id", "authorize_user_id");

-- ----------------------------
-- Primary Key structure for table data_standard
-- ----------------------------
ALTER TABLE "public"."data_standard" ADD CONSTRAINT "data_standard_pkey" PRIMARY KEY ("id", "number");

-- ----------------------------
-- Primary Key structure for table database_group_relation
-- ----------------------------
ALTER TABLE "public"."database_group_relation" ADD CONSTRAINT "database_group_relation_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table datasource_group_relation
-- ----------------------------
ALTER TABLE "public"."datasource_group_relation" ADD CONSTRAINT "datasource_group_relation_pkey" PRIMARY KEY ("source_id", "group_id");

-- ----------------------------
-- Primary Key structure for table db_info
-- ----------------------------
ALTER TABLE "public"."db_info" ADD CONSTRAINT "db_info_pkey" PRIMARY KEY ("database_guid");

-- ----------------------------
-- Primary Key structure for table desensitization_rule
-- ----------------------------
ALTER TABLE "public"."desensitization_rule" ADD CONSTRAINT "desensitization_rule_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table dimension
-- ----------------------------
ALTER TABLE "public"."dimension" ADD CONSTRAINT "increase_dimension_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table dimension_metadata_relation
-- ----------------------------
ALTER TABLE "public"."dimension_metadata_relation" ADD CONSTRAINT "dimension_mapping_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table ip_restriction
-- ----------------------------
CREATE UNIQUE INDEX "ip_restriction_id_uindex" ON "public"."ip_restriction" USING btree (
  "id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ip_restriction
-- ----------------------------
ALTER TABLE "public"."ip_restriction" ADD CONSTRAINT "ip_restriction_pk" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metadata_subscribe
-- ----------------------------
ALTER TABLE "public"."metadata_subscribe" ADD CONSTRAINT "metadata_subscribe_pkey" PRIMARY KEY ("user_id", "table_guid");

-- ----------------------------
-- Primary Key structure for table module
-- ----------------------------
ALTER TABLE "public"."module" ADD CONSTRAINT "privilege_pkey" PRIMARY KEY ("moduleid");

-- ----------------------------
-- Primary Key structure for table operate_log
-- ----------------------------
ALTER TABLE "public"."operate_log" ADD CONSTRAINT "operate_log_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table project
-- ----------------------------
ALTER TABLE "public"."project" ADD CONSTRAINT "project_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table project_group_relation
-- ----------------------------
ALTER TABLE "public"."project_group_relation" ADD CONSTRAINT "project_group_relation_pkey" PRIMARY KEY ("project_id", "group_id");

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
-- Primary Key structure for table qualifier
-- ----------------------------
ALTER TABLE "public"."qualifier" ADD CONSTRAINT "qualifier_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table qualifier_type
-- ----------------------------
ALTER TABLE "public"."qualifier_type" ADD CONSTRAINT "qualifier_type_pkey" PRIMARY KEY ("type_id");

-- ----------------------------
-- Primary Key structure for table report
-- ----------------------------
ALTER TABLE "public"."report" ADD CONSTRAINT "report_pkey" PRIMARY KEY ("reportid");

-- ----------------------------
-- Primary Key structure for table report2ruletemplate
-- ----------------------------
ALTER TABLE "public"."report2ruletemplate" ADD CONSTRAINT "report2ruletype_pkey" PRIMARY KEY ("rule_template_id", "data_quality_execute_id");

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
-- Primary Key structure for table source_db
-- ----------------------------
ALTER TABLE "public"."source_db" ADD CONSTRAINT "source_db_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table source_info
-- ----------------------------
CREATE INDEX "index_database_alias" ON "public"."source_info" USING btree (
  "database_alias" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
COMMENT ON INDEX "public"."index_database_alias" IS '中文名索引';
CREATE INDEX "index_status" ON "public"."source_info" USING btree (
  "status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
COMMENT ON INDEX "public"."index_status" IS '状态索引';

-- ----------------------------
-- Primary Key structure for table source_info
-- ----------------------------
ALTER TABLE "public"."source_info" ADD CONSTRAINT "source_info_pkey" PRIMARY KEY ("id", "version");

-- ----------------------------
-- Uniques structure for table source_info_derive_column_info
-- ----------------------------
ALTER TABLE "public"."source_info_derive_column_info" ADD CONSTRAINT "unique_derive_column_info_column_guid" UNIQUE ("column_guid");

-- ----------------------------
-- Primary Key structure for table source_info_derive_column_info
-- ----------------------------
ALTER TABLE "public"."source_info_derive_column_info" ADD CONSTRAINT "pk_derive_column" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table source_info_derive_table_column_relation
-- ----------------------------
CREATE INDEX "idx_drive_table_column_relation_column_guid" ON "public"."source_info_derive_table_column_relation" USING hash (
  "column_guid" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops"
);
CREATE INDEX "idx_drive_table_column_relation_table_id" ON "public"."source_info_derive_table_column_relation" USING hash (
  "table_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops"
);
CREATE INDEX "idx_drive_table_column_relation_yable_guid" ON "public"."source_info_derive_table_column_relation" USING hash (
  "table_guid" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops"
);

-- ----------------------------
-- Primary Key structure for table source_info_derive_table_column_relation
-- ----------------------------
ALTER TABLE "public"."source_info_derive_table_column_relation" ADD CONSTRAINT "pk_derive_table_cloumn_relation_id" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table source_info_derive_table_info
-- ----------------------------
CREATE INDEX "idx_derive_table_create_time" ON "public"."source_info_derive_table_info" USING btree (
  "create_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_derive_table_guid_version" ON "public"."source_info_derive_table_info" USING btree (
  "table_guid" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "version" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_derive_table_name_en" ON "public"."source_info_derive_table_info" USING hash (
  "table_name_en" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops"
);
CREATE INDEX "idx_derive_table_name_zh" ON "public"."source_info_derive_table_info" USING hash (
  "table_name_zh" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops"
);
CREATE INDEX "idx_derive_table_tenant_id" ON "public"."source_info_derive_table_info" USING hash (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops"
);

-- ----------------------------
-- Primary Key structure for table source_info_derive_table_info
-- ----------------------------
ALTER TABLE "public"."source_info_derive_table_info" ADD CONSTRAINT "pk_derive_table" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table statistical
-- ----------------------------
ALTER TABLE "public"."statistical" ADD CONSTRAINT "statistical_pkey" PRIMARY KEY ("statisticalid");

-- ----------------------------
-- Primary Key structure for table statisticaltype
-- ----------------------------
ALTER TABLE "public"."statisticaltype" ADD CONSTRAINT "statisticaltype_pkey" PRIMARY KEY ("statisticaltypeid");

-- ----------------------------
-- Primary Key structure for table sync_task_definition
-- ----------------------------
ALTER TABLE "public"."sync_task_definition" ADD CONSTRAINT "sync_task_definition_pk" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sync_task_instance
-- ----------------------------
ALTER TABLE "public"."sync_task_instance" ADD CONSTRAINT "sync_task_instance_pk" PRIMARY KEY ("id");

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
-- Primary Key structure for table table_metadata_history
-- ----------------------------
ALTER TABLE "public"."table_metadata_history" ADD CONSTRAINT "table_metadata_history_pkey" PRIMARY KEY ("guid", "version");

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
-- Primary Key structure for table tenant
-- ----------------------------
ALTER TABLE "public"."tenant" ADD CONSTRAINT "tenant_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table time_limit
-- ----------------------------
ALTER TABLE "public"."time_limit" ADD CONSTRAINT "timelimit_pkey" PRIMARY KEY ("id", "version");

-- ----------------------------
-- Primary Key structure for table user2apistar
-- ----------------------------
ALTER TABLE "public"."user2apistar" ADD CONSTRAINT "user2apistar_pkey" PRIMARY KEY ("apiguid", "userid");

-- ----------------------------
-- Primary Key structure for table users
-- ----------------------------
ALTER TABLE "public"."users" ADD CONSTRAINT "user_pkey" PRIMARY KEY ("userid");

-- ----------------------------
-- Foreign Keys structure for table annex
-- ----------------------------
ALTER TABLE "public"."annex" ADD CONSTRAINT "fk_annex_file_type" FOREIGN KEY ("file_type") REFERENCES "public"."code_annex_type" ("code") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table approval_group_relation
-- ----------------------------
ALTER TABLE "public"."approval_group_relation" ADD CONSTRAINT "fk1" FOREIGN KEY ("user_id") REFERENCES "public"."users" ("userid") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."approval_group_relation" ADD CONSTRAINT "fk2" FOREIGN KEY ("group_id") REFERENCES "public"."approval_group" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table approval_item
-- ----------------------------
ALTER TABLE "public"."approval_item" ADD CONSTRAINT "fk1" FOREIGN KEY ("approve_group") REFERENCES "public"."approval_group" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

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

-- ----------------------------
-- Foreign Keys structure for table source_info
-- ----------------------------
ALTER TABLE "public"."source_info" ADD CONSTRAINT "fk_source_info_status" FOREIGN KEY ("status") REFERENCES "public"."code_source_info_status" ("code") ON DELETE NO ACTION ON UPDATE CASCADE;
ALTER TABLE "public"."source_info" ADD CONSTRAINT "fk_source_info_tenant_id" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

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
INSERT INTO "public"."module" VALUES (14, '数据源管理', 1);
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
INSERT INTO "public"."privilege2module" VALUES ('1', 11);
INSERT INTO "public"."privilege2module" VALUES ('1', 12);
INSERT INTO "public"."privilege2module" VALUES ('1', 13);
INSERT INTO "public"."privilege2module" VALUES ('1', 14);
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
INSERT INTO "public"."privilege2module" VALUES ('3', 11);
INSERT INTO "public"."privilege2module" VALUES ('3', 12);
INSERT INTO "public"."privilege2module" VALUES ('3', 13);
INSERT INTO "public"."privilege2module" VALUES ('3', 14);
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

INSERT INTO "public"."apigroup" VALUES ('1', '全部分组', NULL, NULL, NULL, NULL, NULL, NULL,'all');
INSERT INTO "public"."apigroup" VALUES ('0', '未分组', '1', NULL, NULL, NULL, NULL, NULL,'all');
INSERT INTO "public"."systemrule" VALUES (13, '字段平均值变化', '相比上一周期，字段平均值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (14, '字段汇总值变化', '相比上一周期，字段汇总值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (15, '字段最小值变化', '相比上一周期，字段最小值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (16, '字段最大值变化', '相比上一周期，字段最大值变化', 1, '');
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
