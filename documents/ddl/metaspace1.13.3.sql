/*
 Navicat Premium Data Transfer

 Source Server         : 单测-postgres
 Source Server Type    : PostgreSQL
 Source Server Version : 100005
 Source Host           : 10.200.200.178:5432
 Source Catalog        : metaspace_test
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 100005
 File Encoding         : 65001

 Date: 08/11/2021 15:05:08
*/


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
COMMENT ON COLUMN "public"."annex"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."annex"."modify_time" IS '修改时间';
COMMENT ON COLUMN "public"."annex"."file_size" IS '文件大小';
COMMENT ON TABLE "public"."annex" IS '附件信息表';

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
COMMENT ON TABLE "public"."api" IS '接口信息表';

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
COMMENT ON COLUMN "public"."api_audit"."api_version_num" IS 'api版本序列号';
COMMENT ON COLUMN "public"."api_audit"."api_poly_id" IS 'api策略id';
COMMENT ON TABLE "public"."api_audit" IS '接口审核记录表';

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
COMMENT ON COLUMN "public"."api_category"."qualifiedname" IS '多层级目录名称';
COMMENT ON COLUMN "public"."api_category"."projectid" IS '项目id';
COMMENT ON COLUMN "public"."api_category"."tenantid" IS '租户';
COMMENT ON COLUMN "public"."api_category"."level" IS '级别';
COMMENT ON COLUMN "public"."api_category"."createtime" IS '创建时间';
COMMENT ON TABLE "public"."api_category" IS '接口目录表';

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
COMMENT ON TABLE "public"."api_group" IS '接口分组信息表';

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
COMMENT ON COLUMN "public"."api_group_log"."group_id" IS '分组id';
COMMENT ON COLUMN "public"."api_group_log"."type" IS '操作类型：insert，publish，unpublish，update，uplevel';
COMMENT ON COLUMN "public"."api_group_log"."userid" IS '操作人id';
COMMENT ON COLUMN "public"."api_group_log"."time" IS '操作时间';
COMMENT ON TABLE "public"."api_group_log" IS '接口分组日志信息表';

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
COMMENT ON TABLE "public"."api_log" IS '接口操作日志表';

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
COMMENT ON COLUMN "public"."api_module"."path" IS '接口url';
COMMENT ON COLUMN "public"."api_module"."method" IS '接口类型';
COMMENT ON COLUMN "public"."api_module"."module_id" IS '模块id';
COMMENT ON COLUMN "public"."api_module"."prefix_check" IS '是否校验';
COMMENT ON TABLE "public"."api_module" IS '接口模块关联表';

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
COMMENT ON TABLE "public"."api_poly" IS '接口策略信息关联信息';

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
COMMENT ON TABLE "public"."api_relation" IS '接口分组关联表';

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
COMMENT ON COLUMN "public"."apigroup"."guid" IS '分组id';
COMMENT ON COLUMN "public"."apigroup"."name" IS '分组名字';
COMMENT ON COLUMN "public"."apigroup"."parentguid" IS '父目录id';
COMMENT ON COLUMN "public"."apigroup"."description" IS '描述';
COMMENT ON COLUMN "public"."apigroup"."generator" IS '创建者';
COMMENT ON COLUMN "public"."apigroup"."generatetime" IS '创建时间';
COMMENT ON COLUMN "public"."apigroup"."updater" IS '更新人';
COMMENT ON COLUMN "public"."apigroup"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."apigroup"."tenantid" IS '租户id';
COMMENT ON TABLE "public"."apigroup" IS '接口分组表';

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
COMMENT ON COLUMN "public"."apiinfo"."star" IS '是否重要';
COMMENT ON COLUMN "public"."apiinfo"."publish" IS '发布';
COMMENT ON COLUMN "public"."apiinfo"."used_count" IS '使用count';
COMMENT ON COLUMN "public"."apiinfo"."manager" IS '管理者';
COMMENT ON COLUMN "public"."apiinfo"."desensitize" IS '是否敏感';
COMMENT ON COLUMN "public"."apiinfo"."sourcetype" IS '数据源类型';
COMMENT ON COLUMN "public"."apiinfo"."schemaname" IS 'schema名字';
COMMENT ON COLUMN "public"."apiinfo"."tablename" IS '表名字';
COMMENT ON COLUMN "public"."apiinfo"."dbname" IS '库名字';
COMMENT ON COLUMN "public"."apiinfo"."sourceid" IS '数据源id';
COMMENT ON COLUMN "public"."apiinfo"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."apiinfo"."pool" IS '资源池';
COMMENT ON TABLE "public"."apiinfo" IS '接口信息表';

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
COMMENT ON COLUMN "public"."approval_group"."id" IS '审批组ID';
COMMENT ON COLUMN "public"."approval_group"."name" IS '审批组名称';
COMMENT ON COLUMN "public"."approval_group"."description" IS '描述';
COMMENT ON COLUMN "public"."approval_group"."creator" IS '创建人';
COMMENT ON COLUMN "public"."approval_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."approval_group"."updater" IS '更新人';
COMMENT ON COLUMN "public"."approval_group"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."approval_group"."tenantid" IS '租户ID';
COMMENT ON COLUMN "public"."approval_group"."valid" IS '有效';
COMMENT ON TABLE "public"."approval_group" IS '审批组信息';

-- ----------------------------
-- Table structure for approval_group_module_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."approval_group_module_relation";
CREATE TABLE "public"."approval_group_module_relation" (
  "group_id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "module_id" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."approval_group_module_relation"."group_id" IS '审批组ID';
COMMENT ON COLUMN "public"."approval_group_module_relation"."module_id" IS '模块ID';
COMMENT ON TABLE "public"."approval_group_module_relation" IS '审批组模块关联表';

-- ----------------------------
-- Table structure for approval_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."approval_group_relation";
CREATE TABLE "public"."approval_group_relation" (
  "group_id" varchar(40) COLLATE "pg_catalog"."default",
  "user_id" varchar(40) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."approval_group_relation"."group_id" IS '审批组ID';
COMMENT ON COLUMN "public"."approval_group_relation"."user_id" IS '用户ID';
COMMENT ON TABLE "public"."approval_group_relation" IS '审批组用户关系表';

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
COMMENT ON COLUMN "public"."approval_item"."id" IS '审批项ID';
COMMENT ON COLUMN "public"."approval_item"."object_id" IS '送审对象ID';
COMMENT ON COLUMN "public"."approval_item"."object_name" IS '送审对象名称';
COMMENT ON COLUMN "public"."approval_item"."business_type" IS '业务类型编码';
COMMENT ON COLUMN "public"."approval_item"."approve_type" IS '审核类型';
COMMENT ON COLUMN "public"."approval_item"."status" IS '审核状态';
COMMENT ON COLUMN "public"."approval_item"."approve_group" IS '审批组';
COMMENT ON COLUMN "public"."approval_item"."approver" IS '审批人';
COMMENT ON COLUMN "public"."approval_item"."approve_time" IS '审批时间';
COMMENT ON COLUMN "public"."approval_item"."submitter" IS '提交人';
COMMENT ON COLUMN "public"."approval_item"."commit_time" IS '送审时间';
COMMENT ON COLUMN "public"."approval_item"."reason" IS '驳回原因';
COMMENT ON COLUMN "public"."approval_item"."module_id" IS '模块ID';
COMMENT ON COLUMN "public"."approval_item"."version" IS '版本';
COMMENT ON COLUMN "public"."approval_item"."tenant_id" IS '租户ID';
COMMENT ON TABLE "public"."approval_item" IS '审批记录表';

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
  "apply_group_id" varchar(255) COLLATE "pg_catalog"."default",
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
COMMENT ON COLUMN "public"."atom_indicator_apply"."apply_group_id" IS '审批组id';
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
  "dimension_metadata_relation_id" int8,
  "logic_type" int4,
  "indicator_type" varchar(100) COLLATE "pg_catalog"."default",
  "indicator_measure" int4,
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
COMMENT ON COLUMN "public"."atom_indicator_logic"."dimension_metadata_relation_id" IS '维度映射id';
COMMENT ON COLUMN "public"."atom_indicator_logic"."logic_type" IS '逻辑配置类型（1基于原子指标配置 2自定义sql）';
COMMENT ON COLUMN "public"."atom_indicator_logic"."indicator_type" IS '类别（维度 度量）';
COMMENT ON COLUMN "public"."atom_indicator_logic"."indicator_measure" IS '是否为指标度量(1是 0否)';
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
COMMENT ON COLUMN "public"."atom_indicator_version"."version_id" IS '原子指标版本号(0暂存版本 普通版本从1递增)';
COMMENT ON COLUMN "public"."atom_indicator_version"."atom_indicator_code" IS '原子指标编码';
COMMENT ON COLUMN "public"."atom_indicator_version"."atom_indicator_name" IS '原子指标名称';
COMMENT ON COLUMN "public"."atom_indicator_version"."business_indicator_id" IS '业务指标id';
COMMENT ON COLUMN "public"."atom_indicator_version"."remark" IS '备注';
COMMENT ON COLUMN "public"."atom_indicator_version"."data_source_id" IS '数据源id';
COMMENT ON COLUMN "public"."atom_indicator_version"."data_base_id" IS '数据库id';
COMMENT ON COLUMN "public"."atom_indicator_version"."data_table_id" IS '数据表id';
COMMENT ON COLUMN "public"."atom_indicator_version"."release_status" IS '发布状态(0未发布，1已发布，2审核中)';
COMMENT ON COLUMN "public"."atom_indicator_version"."version_type" IS '版本类型(1发布版本 2暂存版本 3待审核版本)';
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
  "tableguid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "relation_type" int2,
  "source_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."business2table"."businessid" IS '业务对象ID';
COMMENT ON COLUMN "public"."business2table"."tableguid" IS '数据表ID';
COMMENT ON COLUMN "public"."business2table"."relation_type" IS '关联类型：0通过业务对象挂载功能挂载到该业务对象的表；1通过衍生表登记模块登记关联到该业务对象上的表';
COMMENT ON COLUMN "public"."business2table"."source_id" IS '数据源id';
COMMENT ON TABLE "public"."business2table" IS '业务对象和数据表映射关系表';

-- ----------------------------
-- Table structure for business_2_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_2_group";
CREATE TABLE "public"."business_2_group" (
  "business_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "read" bool
)
;
COMMENT ON COLUMN "public"."business_2_group"."business_id" IS '业务对象id';
COMMENT ON COLUMN "public"."business_2_group"."group_id" IS '可见用户id';
COMMENT ON COLUMN "public"."business_2_group"."read" IS '查看权限';
COMMENT ON TABLE "public"."business_2_group" IS '业务对象和用户组权限关系表';

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
COMMENT ON TABLE "public"."business_catalog" IS '业务目录表';

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
COMMENT ON TABLE "public"."business_catalog_log" IS '业务目录日志表';

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
COMMENT ON TABLE "public"."business_index_tag_relations" IS '业务指标标签关联表';

-- ----------------------------
-- Table structure for business_indicator_apply
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_indicator_apply";
CREATE TABLE "public"."business_indicator_apply" (
  "id" int8 NOT NULL,
  "business_indicator_id" int8,
  "audit_status" int4,
  "apply_status" int4,
  "apply_group_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."business_indicator_apply"."id" IS '主键id';
COMMENT ON COLUMN "public"."business_indicator_apply"."business_indicator_id" IS '审批中业务指标id';
COMMENT ON COLUMN "public"."business_indicator_apply"."audit_status" IS '审批状态(1审批通过 2审批不通过 )';
COMMENT ON COLUMN "public"."business_indicator_apply"."apply_status" IS '申请状态(1发布申请 2取消发布申请 3发布申请撤销 4取消发布撤销)';
COMMENT ON COLUMN "public"."business_indicator_apply"."apply_group_id" IS '审批组id';
COMMENT ON COLUMN "public"."business_indicator_apply"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."business_indicator_apply"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."business_indicator_apply"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."business_indicator_apply"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."business_indicator_apply"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."business_indicator_apply" IS '业务指标申请表';

-- ----------------------------
-- Table structure for business_indicators
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_indicators";
CREATE TABLE "public"."business_indicators" (
  "id" int8 NOT NULL,
  "business_indicator_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "business_indicator_coding" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "indicator_group" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "business_indicator_cal" varchar(100) COLLATE "pg_catalog"."default",
  "business_implication" varchar(500) COLLATE "pg_catalog"."default",
  "business_parent_id" int8,
  "business_parent_name" varchar(255) COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default",
  "design_formulas" varchar(500) COLLATE "pg_catalog"."default",
  "statistical_cycle" varchar(100) COLLATE "pg_catalog"."default",
  "refresh_rate" varchar(100) COLLATE "pg_catalog"."default",
  "statistical_dimension" varchar(100) COLLATE "pg_catalog"."default",
  "technical_indicator" int8,
  "technical_indicator_type" int4,
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
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "temporary_relese" int4
)
;
COMMENT ON COLUMN "public"."business_indicators"."id" IS '业务指标ID';
COMMENT ON COLUMN "public"."business_indicators"."business_indicator_name" IS '业务指标名称';
COMMENT ON COLUMN "public"."business_indicators"."business_indicator_coding" IS '业务指标编码';
COMMENT ON COLUMN "public"."business_indicators"."indicator_group" IS '业务指标组';
COMMENT ON COLUMN "public"."business_indicators"."business_indicator_cal" IS '指标口径';
COMMENT ON COLUMN "public"."business_indicators"."business_implication" IS '指标含义';
COMMENT ON COLUMN "public"."business_indicators"."business_parent_id" IS '上级指标ID（已经发布的业务指标）';
COMMENT ON COLUMN "public"."business_indicators"."business_parent_name" IS '上级指标名称（展示：指标名称（指标口径））';
COMMENT ON COLUMN "public"."business_indicators"."remark" IS '备注';
COMMENT ON COLUMN "public"."business_indicators"."design_formulas" IS '技术公式';
COMMENT ON COLUMN "public"."business_indicators"."statistical_cycle" IS '统计周期（当前仅支持每日、每周、每月）';
COMMENT ON COLUMN "public"."business_indicators"."refresh_rate" IS '刷新频率（每时、12小时、每日、每月）';
COMMENT ON COLUMN "public"."business_indicators"."statistical_dimension" IS '统计维度。选项为指标维度管理中已配置的维度（忽略业务指标统计维度与技术指标维度不同的情况）';
COMMENT ON COLUMN "public"."business_indicators"."technical_indicator" IS '技术指标（单选，选项为技术指标中已发布的技术指标）';
COMMENT ON COLUMN "public"."business_indicators"."technical_indicator_type" IS '技术指标类别（1：原生指标，2：衍生指标，3：复合指标）';
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
COMMENT ON COLUMN "public"."business_indicators"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."business_indicators"."temporary_relese" IS '是否暂存发布版本（1：标记暂存发布版本，审核失败需要回退到上一发布版本，0/其它不是暂存发布）';
COMMENT ON TABLE "public"."business_indicators" IS '业务指标表';

-- ----------------------------
-- Table structure for business_indicators_history
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_indicators_history";
CREATE TABLE "public"."business_indicators_history" (
  "id" int8 NOT NULL,
  "business_indicator_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "business_indicator_coding" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "indicator_group" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "business_indicator_cal" varchar(100) COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default",
  "design_formulas" varchar(500) COLLATE "pg_catalog"."default",
  "statistical_cycle" varchar(100) COLLATE "pg_catalog"."default",
  "refresh_rate" varchar(100) COLLATE "pg_catalog"."default",
  "statistical_dimension" varchar(255) COLLATE "pg_catalog"."default",
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
  "state" int4,
  "create_time" timestamp(6),
  "create_user_id" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "update_user_id" varchar(64) COLLATE "pg_catalog"."default",
  "deleted" int4 NOT NULL DEFAULT 1,
  "business_implication" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "business_parent_id" int8,
  "business_parent_name" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "technical_indicator_type" int4,
  "business_indicator_id" int8 NOT NULL,
  "is_temporary" int4
)
;
COMMENT ON COLUMN "public"."business_indicators_history"."id" IS '业务指标历史ID';
COMMENT ON COLUMN "public"."business_indicators_history"."business_indicator_name" IS '业务指标名称';
COMMENT ON COLUMN "public"."business_indicators_history"."business_indicator_coding" IS '业务指标编码';
COMMENT ON COLUMN "public"."business_indicators_history"."indicator_group" IS '业务指标组';
COMMENT ON COLUMN "public"."business_indicators_history"."business_indicator_cal" IS '指标口径';
COMMENT ON COLUMN "public"."business_indicators_history"."remark" IS '备注';
COMMENT ON COLUMN "public"."business_indicators_history"."design_formulas" IS '技术公式';
COMMENT ON COLUMN "public"."business_indicators_history"."statistical_cycle" IS '统计周期（当前仅支持每日、每周、每月）';
COMMENT ON COLUMN "public"."business_indicators_history"."refresh_rate" IS '刷新频率（每时、12小时、每日、每月）';
COMMENT ON COLUMN "public"."business_indicators_history"."statistical_dimension" IS '统计维度。选项为指标维度管理中已配置的维度（忽略业务指标统计维度与技术指标维度不同的情况）';
COMMENT ON COLUMN "public"."business_indicators_history"."technical_indicator" IS '技术指标（单选，选项为技术指标中已发布的技术指标）';
COMMENT ON COLUMN "public"."business_indicators_history"."measurement_object" IS '测量对象';
COMMENT ON COLUMN "public"."business_indicators_history"."technical_unit" IS '计量单位';
COMMENT ON COLUMN "public"."business_indicators_history"."data_precision" IS '数据精度（当前仅支持小数点后0、1、2、3、4、5、6位小数）';
COMMENT ON COLUMN "public"."business_indicators_history"."is_secret" IS '是否保密（0：否，1是）';
COMMENT ON COLUMN "public"."business_indicators_history"."secret_age" IS '保密年限（选择保密时显示）';
COMMENT ON COLUMN "public"."business_indicators_history"."is_important" IS '是否重要（0：否，1是）';
COMMENT ON COLUMN "public"."business_indicators_history"."source_type" IS '指标来源类型';
COMMENT ON COLUMN "public"."business_indicators_history"."data_provider" IS '数据提供方';
COMMENT ON COLUMN "public"."business_indicators_history"."attribute_management_department" IS '业务归口管理部门';
COMMENT ON COLUMN "public"."business_indicators_history"."operations_people" IS '运维负责人';
COMMENT ON COLUMN "public"."business_indicators_history"."state" IS '状态（已成功1，未发布0，审核中2）';
COMMENT ON COLUMN "public"."business_indicators_history"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."business_indicators_history"."create_user_id" IS '创建的用户ID';
COMMENT ON COLUMN "public"."business_indicators_history"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."business_indicators_history"."update_user_id" IS '修改的用户ID';
COMMENT ON COLUMN "public"."business_indicators_history"."deleted" IS '是否启用（1：启用，0：删除）';
COMMENT ON COLUMN "public"."business_indicators_history"."business_implication" IS '指标含义';
COMMENT ON COLUMN "public"."business_indicators_history"."business_parent_id" IS '上级指标ID（已经发布的业务指标）';
COMMENT ON COLUMN "public"."business_indicators_history"."business_parent_name" IS '上级指标名称（展示：指标名称（指标口径））';
COMMENT ON COLUMN "public"."business_indicators_history"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."business_indicators_history"."technical_indicator_type" IS '技术指标类别（1：原生指标，2：衍生指标，3：复合指标）';
COMMENT ON COLUMN "public"."business_indicators_history"."business_indicator_id" IS '业务指标ID';
COMMENT ON COLUMN "public"."business_indicators_history"."is_temporary" IS '是否暂存版本（1：暂存版本，0不是暂存）';
COMMENT ON TABLE "public"."business_indicators_history" IS '业务指标历史表';

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
COMMENT ON COLUMN "public"."business_operation_records"."id" IS '业务指标操作记录表ID';
COMMENT ON COLUMN "public"."business_operation_records"."business_indicator_id" IS '业务指标表ID';
COMMENT ON COLUMN "public"."business_operation_records"."operations_records" IS '操作记录';
COMMENT ON COLUMN "public"."business_operation_records"."create_user_id" IS '操作人ID（从sso获取的userinfo）';
COMMENT ON COLUMN "public"."business_operation_records"."create_time" IS '操作时间';
COMMENT ON COLUMN "public"."business_operation_records"."create_user_name" IS '操作人';
COMMENT ON TABLE "public"."business_operation_records" IS '业务指标操作记录表';

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
COMMENT ON COLUMN "public"."business_relation"."categoryguid" IS '目录id';
COMMENT ON COLUMN "public"."business_relation"."relationshipguid" IS '关联关系id';
COMMENT ON COLUMN "public"."business_relation"."businessid" IS '业务对象id';
COMMENT ON COLUMN "public"."business_relation"."generatetime" IS '关联时间';
COMMENT ON TABLE "public"."business_relation" IS '业务对象关联表';

-- ----------------------------
-- Table structure for business_tags
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_tags";
CREATE TABLE "public"."business_tags" (
  "id" int8 NOT NULL,
  "tag_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."business_tags"."id" IS '业务标签表ID';
COMMENT ON COLUMN "public"."business_tags"."tag_name" IS '业务标签名称';
COMMENT ON COLUMN "public"."business_tags"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."business_tags"."create_user_id" IS '创建的用户ID';
COMMENT ON COLUMN "public"."business_tags"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."business_tags"."update_user_id" IS '更新的用户ID';
COMMENT ON COLUMN "public"."business_tags"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."business_tags"."tenant_id" IS '租户ID';
COMMENT ON TABLE "public"."business_tags" IS '业务指标标签表';

-- ----------------------------
-- Table structure for business_unit
-- ----------------------------
DROP TABLE IF EXISTS "public"."business_unit";
CREATE TABLE "public"."business_unit" (
  "id" int8 NOT NULL,
  "technical_unit" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6),
  "create_user_id" varchar(64) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "update_user_id" varchar(64) COLLATE "pg_catalog"."default",
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."business_unit"."id" IS '业务指标计量单位主键ID';
COMMENT ON COLUMN "public"."business_unit"."technical_unit" IS '计量单位';
COMMENT ON COLUMN "public"."business_unit"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."business_unit"."create_user_id" IS '创建的用户ID';
COMMENT ON COLUMN "public"."business_unit"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."business_unit"."update_user_id" IS '修改的用户ID';
COMMENT ON COLUMN "public"."business_unit"."deleted" IS '是否启用（1：启用，0：删除）';
COMMENT ON TABLE "public"."business_unit" IS '业务指标计量单位表';

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
  "tenantid" varchar(36) COLLATE "pg_catalog"."default",
  "publish" bool,
  "status" varchar(10) COLLATE "pg_catalog"."default",
  "publish_desc" varchar(256) COLLATE "pg_catalog"."default",
  "approve_group_id" varchar(64) COLLATE "pg_catalog"."default",
  "approve_id" varchar(64) COLLATE "pg_catalog"."default",
  "create_mode" int2,
  "private_status" varchar(64) COLLATE "pg_catalog"."default",
  "submitter_read" bool
)
;
COMMENT ON COLUMN "public"."businessinfo"."businessid" IS '业务对象id';
COMMENT ON COLUMN "public"."businessinfo"."departmentid" IS '业务部门id';
COMMENT ON COLUMN "public"."businessinfo"."name" IS '业务对象名称';
COMMENT ON COLUMN "public"."businessinfo"."module" IS '业务模块';
COMMENT ON COLUMN "public"."businessinfo"."description" IS '业务描述';
COMMENT ON COLUMN "public"."businessinfo"."owner" IS '所有者';
COMMENT ON COLUMN "public"."businessinfo"."manager" IS '管理者';
COMMENT ON COLUMN "public"."businessinfo"."maintainer" IS '维护者';
COMMENT ON COLUMN "public"."businessinfo"."dataassets" IS '相关数据资产';
COMMENT ON COLUMN "public"."businessinfo"."businesslastupdate" IS '业务信息更新人';
COMMENT ON COLUMN "public"."businessinfo"."businessoperator" IS '业务信息更新时间';
COMMENT ON COLUMN "public"."businessinfo"."technicallastupdate" IS '技术信息更新人';
COMMENT ON COLUMN "public"."businessinfo"."technicaloperator" IS '技术信息更新时间';
COMMENT ON COLUMN "public"."businessinfo"."technicalstatus" IS '技术信息状态';
COMMENT ON COLUMN "public"."businessinfo"."businessstatus" IS '业务信息状态';
COMMENT ON COLUMN "public"."businessinfo"."submitter" IS '提交者';
COMMENT ON COLUMN "public"."businessinfo"."ticketnumber" IS '业务对象标识';
COMMENT ON COLUMN "public"."businessinfo"."submissiontime" IS '提交时间';
COMMENT ON COLUMN "public"."businessinfo"."level2categoryid" IS '二级目录';
COMMENT ON COLUMN "public"."businessinfo"."trusttable" IS '唯一信任表';
COMMENT ON COLUMN "public"."businessinfo"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."businessinfo"."publish" IS '发布开关';
COMMENT ON COLUMN "public"."businessinfo"."status" IS '业务对象状态：0待发布，1待审批，2审核不通过，3审核通过';
COMMENT ON COLUMN "public"."businessinfo"."publish_desc" IS '发布说明信息';
COMMENT ON COLUMN "public"."businessinfo"."approve_group_id" IS '审批组id';
COMMENT ON COLUMN "public"."businessinfo"."approve_id" IS '审批id';
COMMENT ON COLUMN "public"."businessinfo"."create_mode" IS '创建方式：0手动添加，1上传文件';
COMMENT ON COLUMN "public"."businessinfo"."private_status" IS '私密状态';
COMMENT ON COLUMN "public"."businessinfo"."submitter_read" IS '是否创建人可见';
COMMENT ON TABLE "public"."businessinfo" IS '业务对象表';

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
  "private_status" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'PUBLIC'::character varying,
  "publish" bool,
  "information" text COLLATE "pg_catalog"."default",
  "approval_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."category"."guid" IS '目录id';
COMMENT ON COLUMN "public"."category"."description" IS '描述';
COMMENT ON COLUMN "public"."category"."name" IS '名称';
COMMENT ON COLUMN "public"."category"."upbrothercategoryguid" IS '上一个节点';
COMMENT ON COLUMN "public"."category"."downbrothercategoryguid" IS '下一个节点';
COMMENT ON COLUMN "public"."category"."parentcategoryguid" IS '父目录';
COMMENT ON COLUMN "public"."category"."qualifiedname" IS '限定名';
COMMENT ON COLUMN "public"."category"."categorytype" IS '目录类型';
COMMENT ON COLUMN "public"."category"."level" IS '级别';
COMMENT ON COLUMN "public"."category"."safe" IS '是否安全';
COMMENT ON COLUMN "public"."category"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."category"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."category"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."category"."creator" IS '创建人';
COMMENT ON COLUMN "public"."category"."updater" IS '更新人';
COMMENT ON COLUMN "public"."category"."code" IS '编码';
COMMENT ON COLUMN "public"."category"."sort" IS '排序';
COMMENT ON COLUMN "public"."category"."private_status" IS '私密状态';
COMMENT ON COLUMN "public"."category"."publish" IS '是否发布： t-已发布  f-未发布';
COMMENT ON COLUMN "public"."category"."information" IS '审批组说明';
COMMENT ON COLUMN "public"."category"."approval_id" IS '审批记录id';
COMMENT ON TABLE "public"."category" IS '目录表';

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
COMMENT ON COLUMN "public"."category_group_relation"."category_id" IS '目录id';
COMMENT ON COLUMN "public"."category_group_relation"."group_id" IS '用户组id';
COMMENT ON COLUMN "public"."category_group_relation"."read" IS '读权限';
COMMENT ON COLUMN "public"."category_group_relation"."edit_category" IS '编辑目录权限';
COMMENT ON COLUMN "public"."category_group_relation"."edit_item" IS '内容编辑权限';
COMMENT ON TABLE "public"."category_group_relation" IS '目录关联用户组表';

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
COMMENT ON TABLE "public"."code_source_info_status" IS '源信息字典表';

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
COMMENT ON TABLE "public"."column_info" IS '数据列信息';

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
COMMENT ON COLUMN "public"."column_metadata_history"."creator" IS '创建人';
COMMENT ON COLUMN "public"."column_metadata_history"."updater" IS '更新人';
COMMENT ON COLUMN "public"."column_metadata_history"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."column_metadata_history"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."column_metadata_history" IS '数据列信息历史记录';

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
COMMENT ON TABLE "public"."column_tag" IS '数据列标签表';

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
COMMENT ON TABLE "public"."column_tag_relation_to_column" IS '数据列标签和列关联表';

-- ----------------------------
-- Table structure for composite_indicator
-- ----------------------------
DROP TABLE IF EXISTS "public"."composite_indicator";
CREATE TABLE "public"."composite_indicator" (
  "id" int8 NOT NULL,
  "composite_indicator_code" varchar(100) COLLATE "pg_catalog"."default",
  "composite_indicator_version_id" int8,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."composite_indicator"."id" IS '主键id';
COMMENT ON COLUMN "public"."composite_indicator"."composite_indicator_code" IS '复合指标编码';
COMMENT ON COLUMN "public"."composite_indicator"."composite_indicator_version_id" IS '复合指标展示id';
COMMENT ON COLUMN "public"."composite_indicator"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."composite_indicator"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."composite_indicator"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."composite_indicator"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."composite_indicator"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."composite_indicator"."tenant_id" IS '租户id';
COMMENT ON TABLE "public"."composite_indicator" IS '复合指标表';

-- ----------------------------
-- Table structure for composite_indicator_version
-- ----------------------------
DROP TABLE IF EXISTS "public"."composite_indicator_version";
CREATE TABLE "public"."composite_indicator_version" (
  "id" int8 NOT NULL,
  "composite_indicator_id" int8,
  "version_id" int4,
  "composite_indicator_code" varchar(100) COLLATE "pg_catalog"."default",
  "composite_indicator_name" varchar(100) COLLATE "pg_catalog"."default",
  "business_indicators_id" int8,
  "remark" varchar(500) COLLATE "pg_catalog"."default",
  "logic_type" int4,
  "data_source_id" varchar(100) COLLATE "pg_catalog"."default",
  "data_base_id" varchar(100) COLLATE "pg_catalog"."default",
  "dimension_metadata_relation_id" int8[],
  "expression" jsonb,
  "composite_indicator_sql" text COLLATE "pg_catalog"."default",
  "schedule" int4,
  "schedule_time" varchar(100) COLLATE "pg_catalog"."default",
  "release_status" int4,
  "version_type" int4,
  "visible" int4,
  "release_time" timestamp(6),
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "dimension_id" int8[],
  "threshold_setting" int4
)
;
COMMENT ON COLUMN "public"."composite_indicator_version"."id" IS '主键id';
COMMENT ON COLUMN "public"."composite_indicator_version"."composite_indicator_id" IS '复合指标id';
COMMENT ON COLUMN "public"."composite_indicator_version"."version_id" IS '复合指标版本号(0暂存版本 普通版本从1递增)';
COMMENT ON COLUMN "public"."composite_indicator_version"."composite_indicator_code" IS '复合指标编码';
COMMENT ON COLUMN "public"."composite_indicator_version"."composite_indicator_name" IS '复合指标名称';
COMMENT ON COLUMN "public"."composite_indicator_version"."business_indicators_id" IS '关联业务指标id';
COMMENT ON COLUMN "public"."composite_indicator_version"."remark" IS '备注';
COMMENT ON COLUMN "public"."composite_indicator_version"."logic_type" IS '逻辑配置类型（1基于指标配置 2自定义sql）';
COMMENT ON COLUMN "public"."composite_indicator_version"."data_source_id" IS '数据源id';
COMMENT ON COLUMN "public"."composite_indicator_version"."data_base_id" IS '数据库id';
COMMENT ON COLUMN "public"."composite_indicator_version"."dimension_metadata_relation_id" IS '指标维度映射id';
COMMENT ON COLUMN "public"."composite_indicator_version"."expression" IS '配置表达式 json格式实例 {"operation": "sum() + 100", "indicators": [{"index": 3, "indicatorId": 123456677771234, "indicatorType": 1}]}';
COMMENT ON COLUMN "public"."composite_indicator_version"."composite_indicator_sql" IS '自定义sql';
COMMENT ON COLUMN "public"."composite_indicator_version"."schedule" IS '调度周期(1每日 2每周 3每月)';
COMMENT ON COLUMN "public"."composite_indicator_version"."schedule_time" IS '调度时间yyyy-MM-dd HH:mm';
COMMENT ON COLUMN "public"."composite_indicator_version"."release_status" IS '发布状态(0未发布，1已发布，2审核中)';
COMMENT ON COLUMN "public"."composite_indicator_version"."version_type" IS '版本类型(1发布版本 2暂存版本 3待审核版本)';
COMMENT ON COLUMN "public"."composite_indicator_version"."visible" IS '是否可见(0不可见 1可见)';
COMMENT ON COLUMN "public"."composite_indicator_version"."release_time" IS '发布时间';
COMMENT ON COLUMN "public"."composite_indicator_version"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."composite_indicator_version"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."composite_indicator_version"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."composite_indicator_version"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."composite_indicator_version"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."composite_indicator_version"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."composite_indicator_version"."dimension_id" IS '指标维度ID';
COMMENT ON COLUMN "public"."composite_indicator_version"."threshold_setting" IS '阈值设置（0 未设置， 1 已设置）';
COMMENT ON TABLE "public"."composite_indicator_version" IS '复合指标版本表';

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
COMMENT ON TABLE "public"."connector" IS 'oracle连接表';

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
COMMENT ON COLUMN "public"."data_quality_rule"."id" IS 'id';
COMMENT ON COLUMN "public"."data_quality_rule"."rule_template_id" IS '规则模版id';
COMMENT ON COLUMN "public"."data_quality_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."data_quality_rule"."code" IS '规则编码';
COMMENT ON COLUMN "public"."data_quality_rule"."category_id" IS '分组id';
COMMENT ON COLUMN "public"."data_quality_rule"."enable" IS '是否开启';
COMMENT ON COLUMN "public"."data_quality_rule"."description" IS '描述';
COMMENT ON COLUMN "public"."data_quality_rule"."check_type" IS '0-固定值,1-波动值';
COMMENT ON COLUMN "public"."data_quality_rule"."check_expression_type" IS '校验表达式的类型,>=、=等的代码值';
COMMENT ON COLUMN "public"."data_quality_rule"."creator" IS '创建人';
COMMENT ON COLUMN "public"."data_quality_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."data_quality_rule"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_rule"."check_threshold_min_value" IS '校验阈值最小值';
COMMENT ON COLUMN "public"."data_quality_rule"."check_threshold_max_value" IS '校验阈值最大值';
COMMENT ON COLUMN "public"."data_quality_rule"."scope" IS '作用域';
COMMENT ON COLUMN "public"."data_quality_rule"."check_threshold_unit" IS '单位';
COMMENT ON COLUMN "public"."data_quality_rule"."tenantid" IS '租户id';
COMMENT ON TABLE "public"."data_quality_rule" IS '数据质量规则表';

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
COMMENT ON COLUMN "public"."data_quality_rule_template"."name" IS '模板名称';
COMMENT ON COLUMN "public"."data_quality_rule_template"."scope" IS '范围';
COMMENT ON COLUMN "public"."data_quality_rule_template"."unit" IS '单位';
COMMENT ON COLUMN "public"."data_quality_rule_template"."description" IS '描述';
COMMENT ON COLUMN "public"."data_quality_rule_template"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_rule_template"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."data_quality_rule_template"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_rule_template"."id" IS '主键id';
COMMENT ON COLUMN "public"."data_quality_rule_template"."rule_type" IS '规则类型';
COMMENT ON COLUMN "public"."data_quality_rule_template"."type" IS '类型';
COMMENT ON COLUMN "public"."data_quality_rule_template"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."data_quality_rule_template"."creator" IS '创建者';
COMMENT ON COLUMN "public"."data_quality_rule_template"."code" IS '编号';
COMMENT ON COLUMN "public"."data_quality_rule_template"."sql" IS '自定义规则的sql语句';
COMMENT ON COLUMN "public"."data_quality_rule_template"."enable" IS '规则状态';
COMMENT ON TABLE "public"."data_quality_rule_template" IS '数据质量规则模板';

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
COMMENT ON COLUMN "public"."data_quality_sub_task"."id" IS 'id';
COMMENT ON COLUMN "public"."data_quality_sub_task"."task_id" IS '所属任务id';
COMMENT ON COLUMN "public"."data_quality_sub_task"."datasource_type" IS '数据源类型:1-表,2-字段';
COMMENT ON COLUMN "public"."data_quality_sub_task"."sequence" IS '子任务顺序';
COMMENT ON COLUMN "public"."data_quality_sub_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_sub_task"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."data_quality_sub_task"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_sub_task"."pool" IS '资源池';
COMMENT ON COLUMN "public"."data_quality_sub_task"."config" IS 'spark配置';
COMMENT ON TABLE "public"."data_quality_sub_task" IS '数据质量子任务';

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
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."id" IS 'id';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."subtask_id" IS '所属子任务id';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."object_id" IS '表或字段id';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."sequence" IS '数据源顺序';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_sub_task_object"."task_id" IS '所属任务id';
COMMENT ON TABLE "public"."data_quality_sub_task_object" IS '数据质量子任务对象表';

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
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."id" IS 'id';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."subtask_id" IS '所属子任务id';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."ruleid" IS '规则id';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_threshold_min_value" IS '校验阈值最小值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_check_type" IS '橙色校验类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_check_expression_type" IS '橙色校验表达式类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."red_check_type" IS '红色校验类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."red_check_expression_type" IS '红色校验表达式类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."red_warning_groupid" IS '红色告警分组编号';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."sequence" IS '规则顺序';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_threshold_min_value" IS ' 橙色告警最小阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."orange_threshold_max_value" IS '橙色告警最大阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."red_threshold_min_value" IS '红色告警最小阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."red_threshold_max_value" IS '红色告警最大阈值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_threshold_max_value" IS '校验阈值最大值';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_type" IS '校验类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_expression_type" IS '校验表达式类型';
COMMENT ON COLUMN "public"."data_quality_sub_task_rule"."check_threshold_unit" IS '单位';
COMMENT ON TABLE "public"."data_quality_sub_task_rule" IS '数据质量子任务规则';

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
COMMENT ON COLUMN "public"."data_quality_task"."id" IS '主键id';
COMMENT ON COLUMN "public"."data_quality_task"."name" IS '任务名';
COMMENT ON COLUMN "public"."data_quality_task"."level" IS '任务级别:1-普通,2-重要,3-非常重要';
COMMENT ON COLUMN "public"."data_quality_task"."description" IS '任务描述';
COMMENT ON COLUMN "public"."data_quality_task"."cron_expression" IS 'cron表达式';
COMMENT ON COLUMN "public"."data_quality_task"."enable" IS '是否启用';
COMMENT ON COLUMN "public"."data_quality_task"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."data_quality_task"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."data_quality_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_task"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."data_quality_task"."creator" IS '创建人';
COMMENT ON COLUMN "public"."data_quality_task"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."data_quality_task"."number" IS '任务ID';
COMMENT ON COLUMN "public"."data_quality_task"."qrtz_job" IS 'jobName';
COMMENT ON COLUMN "public"."data_quality_task"."execution_count" IS '执行次数';
COMMENT ON COLUMN "public"."data_quality_task"."orange_warning_total_count" IS '橙色告警次数统计';
COMMENT ON COLUMN "public"."data_quality_task"."red_warning_total_count" IS '红色告警次数统计';
COMMENT ON COLUMN "public"."data_quality_task"."error_total_count" IS '执行失败统计次数';
COMMENT ON COLUMN "public"."data_quality_task"."updater" IS '修改人';
COMMENT ON COLUMN "public"."data_quality_task"."current_execution_percent" IS '当前进度百分比';
COMMENT ON COLUMN "public"."data_quality_task"."current_execution_status" IS '执行状态:1-执行中,2-成功,3-失败,0-待执行,4-取消';
COMMENT ON COLUMN "public"."data_quality_task"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."data_quality_task"."pool" IS '资源池';
COMMENT ON COLUMN "public"."data_quality_task"."general_warning_total_count" IS '普通告警总数统计';
COMMENT ON TABLE "public"."data_quality_task" IS '数据质量任务';

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
COMMENT ON COLUMN "public"."data_quality_task2warning_group"."task_id" IS '任务id';
COMMENT ON COLUMN "public"."data_quality_task2warning_group"."warning_group_id" IS '告警分组id';
COMMENT ON COLUMN "public"."data_quality_task2warning_group"."warning_type" IS '告警类型';
COMMENT ON TABLE "public"."data_quality_task2warning_group" IS '数据质量任务告警分组表';

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
COMMENT ON COLUMN "public"."data_quality_task_execute"."id" IS '主键id';
COMMENT ON COLUMN "public"."data_quality_task_execute"."task_id" IS '所属任务id';
COMMENT ON COLUMN "public"."data_quality_task_execute"."percent" IS '执行进度';
COMMENT ON COLUMN "public"."data_quality_task_execute"."execute_status" IS '执行状态:1-执行中,2-成功,3-失败,0-未执行,4-取消';
COMMENT ON COLUMN "public"."data_quality_task_execute"."executor" IS '执行者id';
COMMENT ON COLUMN "public"."data_quality_task_execute"."error_msg" IS '程序执行错误日志';
COMMENT ON COLUMN "public"."data_quality_task_execute"."execute_time" IS '执行时间';
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
COMMENT ON TABLE "public"."data_quality_task_execute" IS '数据质量任务执行结果';

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
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."id" IS 'id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."task_execute_id" IS '任务id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."task_id" IS '所属任务id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."subtask_id" IS '所属子任务id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."subtask_object_id" IS '所属子任务对象id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."subtask_rule_id" IS '所属子任务规则id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."result" IS '规则执行结果(sql直接结果)';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."check_status" IS '0-合格,1-不合格,2-失败';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."waring_send_status" IS '告警状态:1-待发送,2发送中,3发送成功,4发送失败';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."reference_value" IS '计算变化值/变化率中计算值存储';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."orange_warning_check_status" IS '0-无告警,1-有告警';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."red_warning_check_status" IS '0-无告警,1-有告警';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."error_msg" IS '错误信息';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."warning_status" IS '告警状态: 状态:0-无告警,1-告警中,2-告警已关闭';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."error_status" IS '告警状态: 状态:0-无告警,1-告警中,2-告警已关闭';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."rule_id" IS '规则id';
COMMENT ON COLUMN "public"."data_quality_task_rule_execute"."general_warning_check_status" IS '一般告警：0-无告警,1-有告警，2-已关闭';
COMMENT ON TABLE "public"."data_quality_task_rule_execute" IS '数据质量子任务执行结果';

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
COMMENT ON COLUMN "public"."data_source"."isapi" IS '是否接口添加';
COMMENT ON COLUMN "public"."data_source"."oracle_db" IS 'oracel数据库实例';
COMMENT ON COLUMN "public"."data_source"."servicetype" IS '服务类型';
COMMENT ON COLUMN "public"."data_source"."tenantid" IS '租户id';
COMMENT ON TABLE "public"."data_source" IS '数据源表';

-- ----------------------------
-- Table structure for data_source_api_authorize
-- ----------------------------
DROP TABLE IF EXISTS "public"."data_source_api_authorize";
CREATE TABLE "public"."data_source_api_authorize" (
  "source_id" varchar(225) COLLATE "pg_catalog"."default" NOT NULL,
  "authorize_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."data_source_api_authorize"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."data_source_api_authorize"."authorize_user_id" IS '用户id';
COMMENT ON TABLE "public"."data_source_api_authorize" IS '数据源api授权表';

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
COMMENT ON TABLE "public"."data_source_authorize" IS '数据源授权表';

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
COMMENT ON COLUMN "public"."data_standard"."id" IS '主键id';
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
COMMENT ON TABLE "public"."data_standard" IS '数据标准表';

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
COMMENT ON COLUMN "public"."data_standard2data_quality_rule"."number" IS '编号';
COMMENT ON COLUMN "public"."data_standard2data_quality_rule"."ruleid" IS '规则id';
COMMENT ON COLUMN "public"."data_standard2data_quality_rule"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."data_standard2data_quality_rule"."operator" IS '更新人';
COMMENT ON TABLE "public"."data_standard2data_quality_rule" IS '数据标准-数据质量规则表';

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
COMMENT ON COLUMN "public"."data_standard2table"."number" IS '编号';
COMMENT ON COLUMN "public"."data_standard2table"."tableguid" IS '数据表id';
COMMENT ON COLUMN "public"."data_standard2table"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."data_standard2table"."operator" IS '更新人';
COMMENT ON TABLE "public"."data_standard2table" IS '数据标准-数据表关联';

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
COMMENT ON TABLE "public"."database_group_relation" IS '数据源数据库和用户组关联表';

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
COMMENT ON COLUMN "public"."datasource_group_relation"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."datasource_group_relation"."group_id" IS '用户组id';
COMMENT ON COLUMN "public"."datasource_group_relation"."privilege_code" IS '授权代码';
COMMENT ON TABLE "public"."datasource_group_relation" IS '数据源和用户组关联表';

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
COMMENT ON TABLE "public"."db_category_relation" IS '数据库和目录关联表';

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
COMMENT ON TABLE "public"."db_info" IS '数据库信息表';

-- ----------------------------
-- Table structure for derive_indicator
-- ----------------------------
DROP TABLE IF EXISTS "public"."derive_indicator";
CREATE TABLE "public"."derive_indicator" (
  "id" int8 NOT NULL,
  "derive_indicator_code" varchar(100) COLLATE "pg_catalog"."default",
  "derive_indicator_name" varchar(100) COLLATE "pg_catalog"."default",
  "derive_indicator_version_id" int8,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."derive_indicator"."id" IS '主键id';
COMMENT ON COLUMN "public"."derive_indicator"."derive_indicator_code" IS '衍生指标编码';
COMMENT ON COLUMN "public"."derive_indicator"."derive_indicator_name" IS '衍生指标名称';
COMMENT ON COLUMN "public"."derive_indicator"."derive_indicator_version_id" IS '衍生指标展示id';
COMMENT ON COLUMN "public"."derive_indicator"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."derive_indicator"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."derive_indicator"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."derive_indicator"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."derive_indicator"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."derive_indicator"."tenant_id" IS '租户id';
COMMENT ON TABLE "public"."derive_indicator" IS '衍生指标表';

-- ----------------------------
-- Table structure for derive_indicator_qualifier
-- ----------------------------
DROP TABLE IF EXISTS "public"."derive_indicator_qualifier";
CREATE TABLE "public"."derive_indicator_qualifier" (
  "id" int8 NOT NULL,
  "derive_indicator_id" int8,
  "qualifier_id" int8,
  "condition_id" int8,
  "qualifier_field_type" int4,
  "qualifier_field_id" varchar(100) COLLATE "pg_catalog"."default",
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."id" IS '主键id';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."derive_indicator_id" IS '衍生指标id';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."qualifier_id" IS '修饰词id';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."condition_id" IS '条件id';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."qualifier_field_type" IS '字段类型';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."qualifier_field_id" IS '字段id';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."derive_indicator_qualifier"."tenant_id" IS '租户id';

-- ----------------------------
-- Table structure for derive_indicator_version
-- ----------------------------
DROP TABLE IF EXISTS "public"."derive_indicator_version";
CREATE TABLE "public"."derive_indicator_version" (
  "id" int8 NOT NULL,
  "derive_indicator_id" int8,
  "version_id" int4,
  "derive_indicator_code" varchar(100) COLLATE "pg_catalog"."default",
  "derive_indicator_name" varchar(100) COLLATE "pg_catalog"."default",
  "business_indicators_id" int8,
  "remark" varchar(500) COLLATE "pg_catalog"."default",
  "logic_type" int4,
  "higher_level_indicator_id" int8,
  "technical_indicator_type" int4,
  "dimension_metadata_relation_id" int8[],
  "time_limit_id" int8,
  "time_limit_field_type" int4,
  "time_limit_field_id" varchar(100) COLLATE "pg_catalog"."default",
  "common_qualifier_id" int8[],
  "exclusive_qualifier" jsonb,
  "expression" jsonb,
  "derive_indicator_sql" text COLLATE "pg_catalog"."default",
  "release_status" int4,
  "version_type" int4,
  "visible" int4,
  "release_time" timestamp(6),
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "threshold_setting" int4
)
;
COMMENT ON COLUMN "public"."derive_indicator_version"."id" IS '主键id';
COMMENT ON COLUMN "public"."derive_indicator_version"."derive_indicator_id" IS '衍生指标id';
COMMENT ON COLUMN "public"."derive_indicator_version"."version_id" IS '衍生指标版本号(0暂存版本 普通版本从1递增)';
COMMENT ON COLUMN "public"."derive_indicator_version"."derive_indicator_code" IS '衍生指标编码';
COMMENT ON COLUMN "public"."derive_indicator_version"."derive_indicator_name" IS '衍生指标名称';
COMMENT ON COLUMN "public"."derive_indicator_version"."business_indicators_id" IS '关联业务指标id';
COMMENT ON COLUMN "public"."derive_indicator_version"."remark" IS '备注';
COMMENT ON COLUMN "public"."derive_indicator_version"."logic_type" IS '逻辑配置类型（1基于原子指标配置 2自定义sql）';
COMMENT ON COLUMN "public"."derive_indicator_version"."higher_level_indicator_id" IS '上级技术指标id';
COMMENT ON COLUMN "public"."derive_indicator_version"."technical_indicator_type" IS '上级技术指标类别（1：原生指标，2：衍生指标，3：复合指标）';
COMMENT ON COLUMN "public"."derive_indicator_version"."dimension_metadata_relation_id" IS '指标维度id';
COMMENT ON COLUMN "public"."derive_indicator_version"."time_limit_id" IS '时间限定id';
COMMENT ON COLUMN "public"."derive_indicator_version"."time_limit_field_type" IS '时间限定字段类型';
COMMENT ON COLUMN "public"."derive_indicator_version"."time_limit_field_id" IS '时间限定字段id';
COMMENT ON COLUMN "public"."derive_indicator_version"."common_qualifier_id" IS '公共修饰词id';
COMMENT ON COLUMN "public"."derive_indicator_version"."exclusive_qualifier" IS '专属修饰词';
COMMENT ON COLUMN "public"."derive_indicator_version"."expression" IS '配置表达式 json格式实例 {"indicator": {"index": 3, "indicatorId": 1235678901236}, "operation": "sum() + 100"}';
COMMENT ON COLUMN "public"."derive_indicator_version"."derive_indicator_sql" IS '自定义sql';
COMMENT ON COLUMN "public"."derive_indicator_version"."release_status" IS '发布状态(0未发布，1已发布，2审核中)';
COMMENT ON COLUMN "public"."derive_indicator_version"."version_type" IS '版本类型(1发布版本 2暂存版本 3待审核版本)';
COMMENT ON COLUMN "public"."derive_indicator_version"."visible" IS '是否可见(0不可见 1可见)';
COMMENT ON COLUMN "public"."derive_indicator_version"."release_time" IS '发布时间';
COMMENT ON COLUMN "public"."derive_indicator_version"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."derive_indicator_version"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."derive_indicator_version"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."derive_indicator_version"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."derive_indicator_version"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."derive_indicator_version"."tenant_id" IS '租户id';
COMMENT ON TABLE "public"."derive_indicator_version" IS '衍生指标版本表';

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
COMMENT ON TABLE "public"."desensitization_rule" IS '脱敏规则表';

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
COMMENT ON TABLE "public"."dimension" IS '维度表';

-- ----------------------------
-- Table structure for dimension_history
-- ----------------------------
DROP TABLE IF EXISTS "public"."dimension_history";
CREATE TABLE "public"."dimension_history" (
  "id" int8 NOT NULL,
  "dimension_id" int8,
  "content" text COLLATE "pg_catalog"."default",
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."dimension_history"."id" IS '主键id';
COMMENT ON COLUMN "public"."dimension_history"."dimension_id" IS '维度id';
COMMENT ON COLUMN "public"."dimension_history"."content" IS '修改内容';
COMMENT ON COLUMN "public"."dimension_history"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."dimension_history"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."dimension_history"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."dimension_history"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."dimension_history"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."dimension_history" IS '原子指标申请表';

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
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "database_id" varchar(255) COLLATE "pg_catalog"."default",
  "datasource_id" varchar(255) COLLATE "pg_catalog"."default"
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
COMMENT ON COLUMN "public"."dimension_metadata_relation"."database_id" IS '数据库id';
COMMENT ON COLUMN "public"."dimension_metadata_relation"."datasource_id" IS '数据源id';
COMMENT ON TABLE "public"."dimension_metadata_relation" IS '指标维度和元数据关系表';

-- ----------------------------
-- Table structure for group_table_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."group_table_relation";
CREATE TABLE "public"."group_table_relation" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "derive_table_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "importance_privilege" bool,
  "security_privilege" bool,
  "user_group_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."group_table_relation"."id" IS '主键';
COMMENT ON COLUMN "public"."group_table_relation"."derive_table_id" IS '衍生表id';
COMMENT ON COLUMN "public"."group_table_relation"."importance_privilege" IS '重要权限';
COMMENT ON COLUMN "public"."group_table_relation"."security_privilege" IS '保密权限';
COMMENT ON COLUMN "public"."group_table_relation"."user_group_id" IS '用户组id';
COMMENT ON COLUMN "public"."group_table_relation"."tenant_id" IS '租户id';
COMMENT ON TABLE "public"."group_table_relation" IS '用户组和衍生表关系表';

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
COMMENT ON TABLE "public"."index_atomic_info" IS '原子指标信息表';

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
COMMENT ON TABLE "public"."index_composite_info" IS '复合指标信息表';

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
COMMENT ON TABLE "public"."index_derive_composite_relation" IS '派生指标和复合指标关系表';

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
COMMENT ON TABLE "public"."index_derive_info" IS '派生指标信息表';

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
COMMENT ON TABLE "public"."index_derive_modifier_relation" IS '派生指标修饰词关系表';

-- ----------------------------
-- Table structure for indicator_lineage_trace
-- ----------------------------
DROP TABLE IF EXISTS "public"."indicator_lineage_trace";
CREATE TABLE "public"."indicator_lineage_trace" (
  "id" int8 NOT NULL,
  "business_type" char(4) COLLATE "pg_catalog"."default",
  "business_id" int8 NOT NULL,
  "parent_id" int8
)
;
COMMENT ON COLUMN "public"."indicator_lineage_trace"."id" IS '指标血缘关系主键ID';
COMMENT ON COLUMN "public"."indicator_lineage_trace"."business_type" IS '指标类型（1：原生指标，2：衍生指标，3：复合指标）';
COMMENT ON COLUMN "public"."indicator_lineage_trace"."business_id" IS '指标主键ID';
COMMENT ON COLUMN "public"."indicator_lineage_trace"."parent_id" IS '上级血缘ID';
COMMENT ON TABLE "public"."indicator_lineage_trace" IS '指标血源关系表';

-- ----------------------------
-- Table structure for indicator_ref
-- ----------------------------
DROP TABLE IF EXISTS "public"."indicator_ref";
CREATE TABLE "public"."indicator_ref" (
  "id" int8 NOT NULL,
  "gvp_id" varchar(64) COLLATE "pg_catalog"."default",
  "indicator_id" int8,
  "indicator_type" int4,
  "indicator_name" varchar(100) COLLATE "pg_catalog"."default",
  "dashboard" varchar(100) COLLATE "pg_catalog"."default",
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."indicator_ref"."id" IS '主键id';
COMMENT ON COLUMN "public"."indicator_ref"."gvp_id" IS 'gvp的引用id';
COMMENT ON COLUMN "public"."indicator_ref"."indicator_id" IS '指标id';
COMMENT ON COLUMN "public"."indicator_ref"."indicator_type" IS '指标类型';
COMMENT ON COLUMN "public"."indicator_ref"."indicator_name" IS '指标名称';
COMMENT ON COLUMN "public"."indicator_ref"."dashboard" IS '引用的仪表盘';
COMMENT ON COLUMN "public"."indicator_ref"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."indicator_ref"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."indicator_ref"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."indicator_ref"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."indicator_ref"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."indicator_ref" IS '指标引用表';

-- ----------------------------
-- Table structure for indicator_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."indicator_relation";
CREATE TABLE "public"."indicator_relation" (
  "id" int8 NOT NULL,
  "indicator_version_id" int8,
  "indicator_id" int8,
  "indicator_type" int4,
  "indicator_version_type" int4
)
;
COMMENT ON COLUMN "public"."indicator_relation"."id" IS '主键id';
COMMENT ON COLUMN "public"."indicator_relation"."indicator_version_id" IS '指标版本id';
COMMENT ON COLUMN "public"."indicator_relation"."indicator_id" IS '指标id';
COMMENT ON COLUMN "public"."indicator_relation"."indicator_type" IS '指标类型';
COMMENT ON COLUMN "public"."indicator_relation"."indicator_version_type" IS '指标版本对应指标类型';
COMMENT ON TABLE "public"."indicator_relation" IS '指标关系表';

-- ----------------------------
-- Table structure for indicator_status
-- ----------------------------
DROP TABLE IF EXISTS "public"."indicator_status";
CREATE TABLE "public"."indicator_status" (
  "id" int8 NOT NULL,
  "indicator_id" int8,
  "indicator_type" int8,
  "status" int8,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."indicator_status"."id" IS '主键id';
COMMENT ON COLUMN "public"."indicator_status"."indicator_id" IS '指标id';
COMMENT ON COLUMN "public"."indicator_status"."indicator_type" IS '指标类型';
COMMENT ON COLUMN "public"."indicator_status"."status" IS '状态';
COMMENT ON COLUMN "public"."indicator_status"."create_user_id" IS '创建用户';
COMMENT ON COLUMN "public"."indicator_status"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."indicator_status"."update_user_id" IS '更新用户';
COMMENT ON COLUMN "public"."indicator_status"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."indicator_status"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."indicator_status" IS '指标状态表';

-- ----------------------------
-- Table structure for indicator_threshold_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."indicator_threshold_log";
CREATE TABLE "public"."indicator_threshold_log" (
  "indicator_id" int8 NOT NULL,
  "indicator_type" int2 NOT NULL,
  "result" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "content" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) NOT NULL,
  "id" int8 NOT NULL,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4
)
;
COMMENT ON COLUMN "public"."indicator_threshold_log"."indicator_id" IS '指标id';
COMMENT ON COLUMN "public"."indicator_threshold_log"."indicator_type" IS '指标类型';
COMMENT ON COLUMN "public"."indicator_threshold_log"."result" IS '阈值检测结果';
COMMENT ON COLUMN "public"."indicator_threshold_log"."content" IS '检测信息';
COMMENT ON COLUMN "public"."indicator_threshold_log"."create_time" IS '记录时间';
COMMENT ON COLUMN "public"."indicator_threshold_log"."id" IS '主键';
COMMENT ON COLUMN "public"."indicator_threshold_log"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."indicator_threshold_log"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."indicator_threshold_log"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."indicator_threshold_log"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."indicator_threshold_log" IS '指标阈值检测信息表';

-- ----------------------------
-- Table structure for indicator_threshold_setting
-- ----------------------------
DROP TABLE IF EXISTS "public"."indicator_threshold_setting";
CREATE TABLE "public"."indicator_threshold_setting" (
  "id" int8 NOT NULL,
  "indicator_id" int8,
  "indicator_type" int4,
  "left_operator" varchar(255) COLLATE "pg_catalog"."default",
  "left_operate_value" varchar(255) COLLATE "pg_catalog"."default",
  "warning_group" varchar(255)[] COLLATE "pg_catalog"."default",
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "right_operate_value" varchar(255) COLLATE "pg_catalog"."default",
  "right_operator" varchar(255) COLLATE "pg_catalog"."default",
  "check_frequency" varchar(255) COLLATE "pg_catalog"."default",
  "check_time" timestamp(6),
  "enable" bool,
  "day_of_check_frequency" int4
)
;
COMMENT ON COLUMN "public"."indicator_threshold_setting"."id" IS '主键id';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."indicator_id" IS '指标id';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."indicator_type" IS '指标类型';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."left_operator" IS '度量左侧操作符';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."left_operate_value" IS '度量左侧比较值';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."warning_group" IS '告警组';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."create_user_id" IS '创建人';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."right_operate_value" IS '度量右侧比较值';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."right_operator" IS '度量右侧操作符';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."check_frequency" IS '检测频率';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."check_time" IS '检测时间';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."enable" IS '启用状态';
COMMENT ON COLUMN "public"."indicator_threshold_setting"."day_of_check_frequency" IS '检测频率日期节点';
COMMENT ON TABLE "public"."indicator_threshold_setting" IS '指标度量设置表';

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
COMMENT ON COLUMN "public"."metadata_subscribe"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."metadata_subscribe"."table_guid" IS '表id';
COMMENT ON COLUMN "public"."metadata_subscribe"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."metadata_subscribe" IS '用户及表关联关系信息';

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
COMMENT ON TABLE "public"."module" IS '模块表';

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
COMMENT ON COLUMN "public"."operate_log"."id" IS '主键id';
COMMENT ON COLUMN "public"."operate_log"."number" IS '日志序号';
COMMENT ON COLUMN "public"."operate_log"."userid" IS '用户id';
COMMENT ON COLUMN "public"."operate_log"."type" IS '操作类型';
COMMENT ON COLUMN "public"."operate_log"."module" IS '功能模块';
COMMENT ON COLUMN "public"."operate_log"."content" IS '操作内容';
COMMENT ON COLUMN "public"."operate_log"."result" IS '操作结果';
COMMENT ON COLUMN "public"."operate_log"."ip" IS '客户端ip地址';
COMMENT ON COLUMN "public"."operate_log"."createtime" IS '记录时间';
COMMENT ON COLUMN "public"."operate_log"."tenantid" IS '租户id';
COMMENT ON TABLE "public"."operate_log" IS '操作日志表';

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS "public"."organization";
CREATE TABLE "public"."organization" (
  "checked" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "disable" varchar COLLATE "pg_catalog"."default",
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
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
COMMENT ON COLUMN "public"."organization"."checked" IS '是否校验';
COMMENT ON COLUMN "public"."organization"."disable" IS '是否无用';
COMMENT ON COLUMN "public"."organization"."id" IS '组织架构ID';
COMMENT ON COLUMN "public"."organization"."isopen" IS '是否公开';
COMMENT ON COLUMN "public"."organization"."isvm" IS '是否虚拟';
COMMENT ON COLUMN "public"."organization"."name" IS '组织架构名称';
COMMENT ON COLUMN "public"."organization"."open" IS '公开状态';
COMMENT ON COLUMN "public"."organization"."pid" IS '当前组织架构父节点ID';
COMMENT ON COLUMN "public"."organization"."pkid" IS '主键ID';
COMMENT ON COLUMN "public"."organization"."ptype" IS '当前组织架构父节点类型';
COMMENT ON COLUMN "public"."organization"."type" IS '当前组织节点类型';
COMMENT ON COLUMN "public"."organization"."updatetime" IS '更新时间';
COMMENT ON TABLE "public"."organization" IS '组织表';

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
COMMENT ON TABLE "public"."privilege" IS '方案表';

-- ----------------------------
-- Table structure for privilege2module
-- ----------------------------
DROP TABLE IF EXISTS "public"."privilege2module";
CREATE TABLE "public"."privilege2module" (
  "privilegeid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "moduleid" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."privilege2module"."privilegeid" IS '方案id';
COMMENT ON COLUMN "public"."privilege2module"."moduleid" IS '功能id';
COMMENT ON TABLE "public"."privilege2module" IS '方案模块关联表';

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
COMMENT ON COLUMN "public"."project"."id" IS '项目id';
COMMENT ON COLUMN "public"."project"."name" IS '项目名称';
COMMENT ON COLUMN "public"."project"."creator" IS '创建人';
COMMENT ON COLUMN "public"."project"."description" IS '描述';
COMMENT ON COLUMN "public"."project"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."project"."manager" IS '管理人';
COMMENT ON COLUMN "public"."project"."tenantid" IS '租户';
COMMENT ON COLUMN "public"."project"."valid" IS '是否有效';
COMMENT ON TABLE "public"."project" IS '项目表';

-- ----------------------------
-- Table structure for project_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."project_group_relation";
CREATE TABLE "public"."project_group_relation" (
  "project_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."project_group_relation"."project_id" IS '项目id';
COMMENT ON COLUMN "public"."project_group_relation"."group_id" IS '用户组id';
COMMENT ON TABLE "public"."project_group_relation" IS '项目用户组关联表';

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
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."trigger_name" IS '调度器名称';
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."trigger_group" IS '调度器所属组';
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."blob_data" IS '数据集';
COMMENT ON TABLE "public"."qrtz_blob_triggers" IS '计划调度表';

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
COMMENT ON COLUMN "public"."qrtz_calendars"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_calendars"."calendar_name" IS '日程名称';
COMMENT ON COLUMN "public"."qrtz_calendars"."calendar" IS '日程记录';
COMMENT ON TABLE "public"."qrtz_calendars" IS '计划日程表';

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
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."trigger_name" IS '调度器名称';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."trigger_group" IS '调度器分组';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."cron_expression" IS '执行规则';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."time_zone_id" IS '时区';
COMMENT ON TABLE "public"."qrtz_cron_triggers" IS '计划调度规则表';

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
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."entry_id" IS '记录id';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."trigger_name" IS '调度器名称';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."trigger_group" IS '调度器分组';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."instance_name" IS '实例名称';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."fired_time" IS '触发时间';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."sched_time" IS '调度时间';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."priority" IS '优先级';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."state" IS '状态';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."job_name" IS '任务名';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."job_group" IS '任务组';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."is_nonconcurrent" IS '是否非共点的';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."requests_recovery" IS '是否请求恢复';
COMMENT ON TABLE "public"."qrtz_fired_triggers" IS '计划调度触发表';

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
COMMENT ON COLUMN "public"."qrtz_job_details"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_name" IS '任务名称';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_group" IS '任务分组';
COMMENT ON COLUMN "public"."qrtz_job_details"."description" IS '描述';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_class_name" IS '任务执行类名';
COMMENT ON COLUMN "public"."qrtz_job_details"."is_durable" IS '是否可持久';
COMMENT ON COLUMN "public"."qrtz_job_details"."is_nonconcurrent" IS '是否不一致';
COMMENT ON COLUMN "public"."qrtz_job_details"."is_update_data" IS '是否可修改数据';
COMMENT ON COLUMN "public"."qrtz_job_details"."requests_recovery" IS '是否可恢复';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_data" IS '任务数据';
COMMENT ON TABLE "public"."qrtz_job_details" IS '计划任务详情表';

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_locks";
CREATE TABLE "public"."qrtz_locks" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "lock_name" varchar(40) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."qrtz_locks"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_locks"."lock_name" IS '锁名称';
COMMENT ON TABLE "public"."qrtz_locks" IS '计划调度锁定表';

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_paused_trigger_grps";
CREATE TABLE "public"."qrtz_paused_trigger_grps" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."qrtz_paused_trigger_grps"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_paused_trigger_grps"."trigger_group" IS '调度器分组';
COMMENT ON TABLE "public"."qrtz_paused_trigger_grps" IS '计划调度器分组关联表';

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
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."instance_name" IS '实例名称';
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."last_checkin_time" IS '最后登记时间';
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."checkin_interval" IS '登记间隔';
COMMENT ON TABLE "public"."qrtz_scheduler_state" IS '计划执行时间记录表';

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
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."trigger_name" IS '触发器名';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."trigger_group" IS '触发器所属组';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."repeat_count" IS '重复次数';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."repeat_interval" IS '重复间隔';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."times_triggered" IS '触发次数';
COMMENT ON TABLE "public"."qrtz_simple_triggers" IS '存储SimpleTrigger';

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
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."trigger_name" IS '触发器名称';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."trigger_group" IS '触发器所属组';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."str_prop_1" IS '触发器参数1';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."str_prop_2" IS '触发器参数2';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."str_prop_3" IS '触发器参数3';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."int_prop_1" IS '整形-触发器参数1';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."int_prop_2" IS '整形-触发器参数2';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."long_prop_1" IS 'long-触发器参数1';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."long_prop_2" IS 'long-触发器参数2';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."dec_prop_1" IS '数值型-触发器参数1';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."dec_prop_2" IS '数值型-触发器参数2';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."bool_prop_1" IS '布尔-触发器参数1';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."bool_prop_2" IS '布尔-触发器参数2';
COMMENT ON TABLE "public"."qrtz_simprop_triggers" IS '存储CalendarIntervalTrigger和DailyTimeIntervalTrigger两种类型的触发器';

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
COMMENT ON COLUMN "public"."qrtz_triggers"."sched_name" IS '计划任务名';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_name" IS '触发器名';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_group" IS '触发器所属组';
COMMENT ON COLUMN "public"."qrtz_triggers"."job_name" IS '任务名';
COMMENT ON COLUMN "public"."qrtz_triggers"."job_group" IS '任务组';
COMMENT ON COLUMN "public"."qrtz_triggers"."description" IS '描述信息';
COMMENT ON COLUMN "public"."qrtz_triggers"."next_fire_time" IS '下次触发事件';
COMMENT ON COLUMN "public"."qrtz_triggers"."prev_fire_time" IS '上次触发时间';
COMMENT ON COLUMN "public"."qrtz_triggers"."priority" IS '优先级';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_state" IS '状态';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_type" IS '触发器类型';
COMMENT ON COLUMN "public"."qrtz_triggers"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."qrtz_triggers"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."qrtz_triggers"."calendar_name" IS '日程名';
COMMENT ON COLUMN "public"."qrtz_triggers"."misfire_instr" IS '调度规则';
COMMENT ON COLUMN "public"."qrtz_triggers"."job_data" IS '任务数据';
COMMENT ON TABLE "public"."qrtz_triggers" IS '存储定义的trigger';

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
COMMENT ON COLUMN "public"."qualifier"."id" IS '主键id';
COMMENT ON COLUMN "public"."qualifier"."name" IS '修饰词名';
COMMENT ON COLUMN "public"."qualifier"."mark" IS '备注';
COMMENT ON COLUMN "public"."qualifier"."creator" IS '创建者';
COMMENT ON COLUMN "public"."qualifier"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."qualifier"."update_user" IS '更新者';
COMMENT ON COLUMN "public"."qualifier"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."qualifier"."desc" IS '描述信息';
COMMENT ON COLUMN "public"."qualifier"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."qualifier"."typeid" IS '类型id';
COMMENT ON TABLE "public"."qualifier" IS '修饰词表';

-- ----------------------------
-- Table structure for qualifier_ind
-- ----------------------------
DROP TABLE IF EXISTS "public"."qualifier_ind";
CREATE TABLE "public"."qualifier_ind" (
  "id" int8 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "mark" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "deleted" int2 NOT NULL DEFAULT 1,
  "content" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "content_view" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."qualifier_ind"."id" IS '主键id';
COMMENT ON COLUMN "public"."qualifier_ind"."name" IS '名称';
COMMENT ON COLUMN "public"."qualifier_ind"."mark" IS '编号';
COMMENT ON COLUMN "public"."qualifier_ind"."description" IS '描述';
COMMENT ON COLUMN "public"."qualifier_ind"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."qualifier_ind"."create_user_id" IS '创建者id';
COMMENT ON COLUMN "public"."qualifier_ind"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."qualifier_ind"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."qualifier_ind"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."qualifier_ind"."deleted" IS '逻辑删除';
COMMENT ON COLUMN "public"."qualifier_ind"."content" IS '运算规则内容';
COMMENT ON COLUMN "public"."qualifier_ind"."create_name" IS '创建者名字';
COMMENT ON COLUMN "public"."qualifier_ind"."update_name" IS '更新者名字';
COMMENT ON COLUMN "public"."qualifier_ind"."content_view" IS '前端展示修饰词格式';

-- ----------------------------
-- Table structure for qualifier_ind_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."qualifier_ind_log";
CREATE TABLE "public"."qualifier_ind_log" (
  "id" int8 NOT NULL,
  "log_id" int8 NOT NULL,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "change" varchar(2000) COLLATE "pg_catalog"."default" NOT NULL,
  "log_time" timestamptz(6) NOT NULL,
  "create_time" timestamptz(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "deleted" int2 DEFAULT 1,
  "create_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."qualifier_ind_log"."id" IS '主键id';
COMMENT ON COLUMN "public"."qualifier_ind_log"."log_id" IS '时间限定的id';
COMMENT ON COLUMN "public"."qualifier_ind_log"."create_user_id" IS '创建该记录的用户id';
COMMENT ON COLUMN "public"."qualifier_ind_log"."change" IS '变更';
COMMENT ON COLUMN "public"."qualifier_ind_log"."log_time" IS '记录创建时间，也是修改时间';
COMMENT ON COLUMN "public"."qualifier_ind_log"."create_name" IS '创建者名字';

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
COMMENT ON COLUMN "public"."qualifier_type"."type_id" IS '修饰词类型ID';
COMMENT ON COLUMN "public"."qualifier_type"."type_name" IS '修饰词类型名称';
COMMENT ON COLUMN "public"."qualifier_type"."type_mark" IS '修饰词类型标识';
COMMENT ON COLUMN "public"."qualifier_type"."creator" IS '创建人';
COMMENT ON COLUMN "public"."qualifier_type"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."qualifier_type"."update_user" IS '更新人';
COMMENT ON COLUMN "public"."qualifier_type"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."qualifier_type"."type_desc" IS '修饰词类型目录描述';
COMMENT ON COLUMN "public"."qualifier_type"."tenantid" IS '租户ID';
COMMENT ON TABLE "public"."qualifier_type" IS '修饰词类型';

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
COMMENT ON COLUMN "public"."report"."reportid" IS '报告id';
COMMENT ON COLUMN "public"."report"."reportname" IS '报告名称';
COMMENT ON COLUMN "public"."report"."templatename" IS '模板名称';
COMMENT ON COLUMN "public"."report"."periodcron" IS '生成周期的Cron表达式';
COMMENT ON COLUMN "public"."report"."orangealerts" IS '橙色告警数';
COMMENT ON COLUMN "public"."report"."redalerts" IS '红色告警数';
COMMENT ON COLUMN "public"."report"."source" IS '源库表';
COMMENT ON COLUMN "public"."report"."buildtype" IS '生成方式，0代表周期生成，1代表生成1次';
COMMENT ON COLUMN "public"."report"."reportproducedate" IS '报表生成日期';
COMMENT ON COLUMN "public"."report"."templateid" IS '模板Id';
COMMENT ON COLUMN "public"."report"."alert" IS '告警提示，1开启，0关闭';
COMMENT ON TABLE "public"."report" IS '报告表';

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
COMMENT ON COLUMN "public"."report2ruletemplate"."rule_template_id" IS '规则模板id';
COMMENT ON COLUMN "public"."report2ruletemplate"."data_quality_execute_id" IS '数据治理执行id';
COMMENT ON COLUMN "public"."report2ruletemplate"."creator" IS '创建者';
COMMENT ON COLUMN "public"."report2ruletemplate"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."report2ruletemplate" IS '报告模板';

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
COMMENT ON COLUMN "public"."report_error"."errorid" IS '报告错误id';
COMMENT ON COLUMN "public"."report_error"."templateid" IS '模板id';
COMMENT ON COLUMN "public"."report_error"."reportid" IS '报告id';
COMMENT ON COLUMN "public"."report_error"."ruleid" IS '规则id';
COMMENT ON COLUMN "public"."report_error"."content" IS '内容';
COMMENT ON COLUMN "public"."report_error"."generatetime" IS '生成时间';
COMMENT ON COLUMN "public"."report_error"."retrycount" IS '重试次数';
COMMENT ON TABLE "public"."report_error" IS '报告错误信息';

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
COMMENT ON COLUMN "public"."report_userrule"."reportid" IS '报告id';
COMMENT ON COLUMN "public"."report_userrule"."reportrulevalue" IS '规则值';
COMMENT ON COLUMN "public"."report_userrule"."reportrulestatus" IS '规则状态';
COMMENT ON COLUMN "public"."report_userrule"."ruleid" IS '规则id';
COMMENT ON COLUMN "public"."report_userrule"."ruletype" IS '规则类型';
COMMENT ON COLUMN "public"."report_userrule"."rulename" IS '规则名';
COMMENT ON COLUMN "public"."report_userrule"."ruleinfo" IS '规则信息';
COMMENT ON COLUMN "public"."report_userrule"."rulecolumnname" IS '规则列名';
COMMENT ON COLUMN "public"."report_userrule"."rulecolumntype" IS '规则列类型';
COMMENT ON COLUMN "public"."report_userrule"."rulecheckexpression" IS '校验表达式';
COMMENT ON COLUMN "public"."report_userrule"."templateruleid" IS '模板规则id';
COMMENT ON COLUMN "public"."report_userrule"."generatetime" IS '生成时间';
COMMENT ON TABLE "public"."report_userrule" IS '用户规则';

-- ----------------------------
-- Table structure for report_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."report_userrule2threshold";
CREATE TABLE "public"."report_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."report_userrule2threshold"."thresholdvalue" IS '阈值';
COMMENT ON COLUMN "public"."report_userrule2threshold"."ruleid" IS '规则id';
COMMENT ON TABLE "public"."report_userrule2threshold" IS '用户规则阈值记录';

-- ----------------------------
-- Table structure for rule2buildtype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2buildtype";
CREATE TABLE "public"."rule2buildtype" (
  "ruleid" int2 NOT NULL,
  "buildtype" int2 NOT NULL
)
;
COMMENT ON COLUMN "public"."rule2buildtype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."rule2buildtype"."buildtype" IS '生成方式,0代表周期生成，1代表生成1次';
COMMENT ON TABLE "public"."rule2buildtype" IS '规则与生成方式表';

-- ----------------------------
-- Table structure for rule2checktype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2checktype";
CREATE TABLE "public"."rule2checktype" (
  "ruleid" int2 NOT NULL,
  "checktype" int2 NOT NULL
)
;
COMMENT ON COLUMN "public"."rule2checktype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."rule2checktype"."checktype" IS '规则检测方式，0(固定值)，1（波动值）';
COMMENT ON TABLE "public"."rule2checktype" IS '规则与检测方式关系表';

-- ----------------------------
-- Table structure for rule2datatype
-- ----------------------------
DROP TABLE IF EXISTS "public"."rule2datatype";
CREATE TABLE "public"."rule2datatype" (
  "ruleid" int2 NOT NULL,
  "datatype" int2 NOT NULL
)
;
COMMENT ON COLUMN "public"."rule2datatype"."ruleid" IS '规则Id';
COMMENT ON COLUMN "public"."rule2datatype"."datatype" IS '允许的数据类型，1数值型2非数值型';
COMMENT ON TABLE "public"."rule2datatype" IS '规则与数据类型关系表';

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
COMMENT ON TABLE "public"."source_db" IS '数据源和数据库关系表';

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
COMMENT ON TABLE "public"."source_info" IS '源信息登记表';

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
  "permission_field" bool NOT NULL,
  "sort" int4 DEFAULT 0
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
COMMENT ON COLUMN "public"."source_info_derive_column_info"."sort" IS '排序';
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
  "state" int4 NOT NULL,
  "importance" bool,
  "security" bool
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
COMMENT ON COLUMN "public"."source_info_derive_table_info"."importance" IS '重要性';
COMMENT ON COLUMN "public"."source_info_derive_table_info"."security" IS '保密性';
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
COMMENT ON COLUMN "public"."source_info_relation2parent_category"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."source_info_relation2parent_category"."modify_time" IS '更新时间';
COMMENT ON TABLE "public"."source_info_relation2parent_category" IS '源信息登记发布成功前与父目录关系表';

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
COMMENT ON COLUMN "public"."statistical"."statisticalid" IS '主键';
COMMENT ON COLUMN "public"."statistical"."date" IS '统计日期';
COMMENT ON COLUMN "public"."statistical"."statistical" IS '统计值';
COMMENT ON COLUMN "public"."statistical"."statisticaltypeid" IS '统计类型id';
COMMENT ON COLUMN "public"."statistical"."tenantid" IS '租户id';
COMMENT ON TABLE "public"."statistical" IS '统计结果表';

-- ----------------------------
-- Table structure for statisticaltype
-- ----------------------------
DROP TABLE IF EXISTS "public"."statisticaltype";
CREATE TABLE "public"."statisticaltype" (
  "statisticaltypeid" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."statisticaltype"."statisticaltypeid" IS '统计类型id';
COMMENT ON COLUMN "public"."statisticaltype"."name" IS '统计类型名称';
COMMENT ON TABLE "public"."statisticaltype" IS '统计类型名';

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
COMMENT ON COLUMN "public"."sync_task_definition"."description" IS '描述';
COMMENT ON COLUMN "public"."sync_task_definition"."category_guid" IS '技术目录guid';
COMMENT ON TABLE "public"."sync_task_definition" IS '同步任务定义表';

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
COMMENT ON TABLE "public"."sync_task_instance" IS '同步任务实例表';

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
COMMENT ON COLUMN "public"."systemrule"."ruleid" IS '主键';
COMMENT ON COLUMN "public"."systemrule"."rulename" IS '规则名';
COMMENT ON COLUMN "public"."systemrule"."ruleinfo" IS '规则信息';
COMMENT ON COLUMN "public"."systemrule"."ruletype" IS '规则类型';
COMMENT ON COLUMN "public"."systemrule"."rulecheckthresholdunit" IS '规则阈值检测单位';
COMMENT ON TABLE "public"."systemrule" IS '系统规则信息表';

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
COMMENT ON COLUMN "public"."table2owner"."tableguid" IS '数据表id';
COMMENT ON COLUMN "public"."table2owner"."ownerid" IS '所属人id';
COMMENT ON COLUMN "public"."table2owner"."keeper" IS '管理员';
COMMENT ON COLUMN "public"."table2owner"."generatetime" IS '生成时间';
COMMENT ON COLUMN "public"."table2owner"."pkid" IS 'pkid';
COMMENT ON TABLE "public"."table2owner" IS '数据表owner';

-- ----------------------------
-- Table structure for table2tag
-- ----------------------------
DROP TABLE IF EXISTS "public"."table2tag";
CREATE TABLE "public"."table2tag" (
  "tagid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."table2tag"."tagid" IS '标签id';
COMMENT ON COLUMN "public"."table2tag"."tableguid" IS '表id';
COMMENT ON TABLE "public"."table2tag" IS '表与标签关系表';

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
COMMENT ON TABLE "public"."table_data_source_relation" IS '数据源和表的关系';

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
COMMENT ON COLUMN "public"."table_metadata_history"."guid" IS '主键id';
COMMENT ON COLUMN "public"."table_metadata_history"."name" IS '数据名';
COMMENT ON COLUMN "public"."table_metadata_history"."creator" IS '创建人';
COMMENT ON COLUMN "public"."table_metadata_history"."updater" IS '更新人';
COMMENT ON COLUMN "public"."table_metadata_history"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."table_metadata_history"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."table_metadata_history"."database_name" IS '数据库名';
COMMENT ON COLUMN "public"."table_metadata_history"."table_type" IS '表类型';
COMMENT ON COLUMN "public"."table_metadata_history"."partition_table" IS '是否为分块表';
COMMENT ON COLUMN "public"."table_metadata_history"."table_format" IS '表格式';
COMMENT ON COLUMN "public"."table_metadata_history"."store_location" IS '物理存储位置';
COMMENT ON COLUMN "public"."table_metadata_history"."description" IS '描述';
COMMENT ON COLUMN "public"."table_metadata_history"."status" IS '状态';
COMMENT ON COLUMN "public"."table_metadata_history"."version" IS '版本';
COMMENT ON TABLE "public"."table_metadata_history" IS '数据表元数据历史表';

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
COMMENT ON COLUMN "public"."table_relation"."relationshipguid" IS '唯一标识';
COMMENT ON COLUMN "public"."table_relation"."categoryguid" IS '技术目录Id';
COMMENT ON COLUMN "public"."table_relation"."tableguid" IS '表guid';
COMMENT ON COLUMN "public"."table_relation"."generatetime" IS '创建时间';
COMMENT ON COLUMN "public"."table_relation"."tenant_id" IS '租户id';
COMMENT ON TABLE "public"."table_relation" IS '表与目录关系表';

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
COMMENT ON COLUMN "public"."tableinfo"."tableguid" IS '表id';
COMMENT ON COLUMN "public"."tableinfo"."tablename" IS '表名';
COMMENT ON COLUMN "public"."tableinfo"."dbname" IS '数据库名';
COMMENT ON COLUMN "public"."tableinfo"."status" IS '状态';
COMMENT ON COLUMN "public"."tableinfo"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."tableinfo"."dataowner" IS '数据归属人';
COMMENT ON COLUMN "public"."tableinfo"."databaseguid" IS '数据库id';
COMMENT ON COLUMN "public"."tableinfo"."databasestatus" IS '数据库状态';
COMMENT ON COLUMN "public"."tableinfo"."subordinatesystem" IS '下级系统';
COMMENT ON COLUMN "public"."tableinfo"."subordinatedatabase" IS '下级数据库';
COMMENT ON COLUMN "public"."tableinfo"."systemadmin" IS '系统管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehouseadmin" IS '数仓管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehousedescription" IS '数仓描述';
COMMENT ON COLUMN "public"."tableinfo"."catalogadmin" IS '目录管理员';
COMMENT ON COLUMN "public"."tableinfo"."display_name" IS '默认名';
COMMENT ON COLUMN "public"."tableinfo"."display_updatetime" IS '默认更新时间';
COMMENT ON COLUMN "public"."tableinfo"."display_operator" IS '默认操作人';
COMMENT ON COLUMN "public"."tableinfo"."description" IS '描述';
COMMENT ON COLUMN "public"."tableinfo"."source_id" IS '源id';
COMMENT ON COLUMN "public"."tableinfo"."type" IS '类型';
COMMENT ON COLUMN "public"."tableinfo"."owner" IS '拥有者';
COMMENT ON TABLE "public"."tableinfo" IS '数据表信息表';

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
COMMENT ON COLUMN "public"."tag"."tagid" IS '标签id';
COMMENT ON COLUMN "public"."tag"."tagname" IS '标签名';
COMMENT ON COLUMN "public"."tag"."tenantid" IS '租户id';
COMMENT ON TABLE "public"."tag" IS '数据表标签表';

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
COMMENT ON COLUMN "public"."template"."templateid" IS '主键id';
COMMENT ON COLUMN "public"."template"."tableid" IS '表id';
COMMENT ON COLUMN "public"."template"."buildtype" IS '构建类型';
COMMENT ON COLUMN "public"."template"."periodcron" IS '分块表达式';
COMMENT ON COLUMN "public"."template"."starttime" IS '起始时间';
COMMENT ON COLUMN "public"."template"."templatestatus" IS '模板状态';
COMMENT ON COLUMN "public"."template"."templatename" IS '模板名';
COMMENT ON COLUMN "public"."template"."tablerulesnum" IS '表规则值';
COMMENT ON COLUMN "public"."template"."columnrulesnum" IS '字段规则值';
COMMENT ON COLUMN "public"."template"."source" IS '源id';
COMMENT ON COLUMN "public"."template"."finishedpercent" IS '完成百分比';
COMMENT ON COLUMN "public"."template"."shutdown" IS '是否中止';
COMMENT ON COLUMN "public"."template"."generatetime" IS '生成时间';
COMMENT ON TABLE "public"."template" IS '数据表模板表';

-- ----------------------------
-- Table structure for template2qrtz_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."template2qrtz_job";
CREATE TABLE "public"."template2qrtz_job" (
  "templateid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "qrtz_job" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."template2qrtz_job"."templateid" IS '模板id';
COMMENT ON COLUMN "public"."template2qrtz_job"."qrtz_job" IS '定时任务';
COMMENT ON TABLE "public"."template2qrtz_job" IS '模板任务表';

-- ----------------------------
-- Table structure for template_userrule
-- ----------------------------
DROP TABLE IF EXISTS "public"."template_userrule";
CREATE TABLE "public"."template_userrule" (
  "ruleid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "rulename" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
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
COMMENT ON COLUMN "public"."template_userrule"."rulename" IS '规则名';
COMMENT ON COLUMN "public"."template_userrule"."ruleinfo" IS '规则信息';
COMMENT ON COLUMN "public"."template_userrule"."rulecolumnname" IS '规则栏目名';
COMMENT ON COLUMN "public"."template_userrule"."rulecolumntype" IS '规则栏目类型';
COMMENT ON COLUMN "public"."template_userrule"."rulechecktype" IS '规则检测方式';
COMMENT ON COLUMN "public"."template_userrule"."rulecheckexpression" IS '规则检测表达式';
COMMENT ON COLUMN "public"."template_userrule"."rulecheckthresholdunit" IS '规则检测预警单位';
COMMENT ON COLUMN "public"."template_userrule"."templateid" IS '模板id';
COMMENT ON COLUMN "public"."template_userrule"."datatype" IS '数据类型';
COMMENT ON COLUMN "public"."template_userrule"."ruletype" IS '规则类型';
COMMENT ON COLUMN "public"."template_userrule"."systemruleid" IS '系统规则id';
COMMENT ON COLUMN "public"."template_userrule"."generatetime" IS '生成时间';
COMMENT ON TABLE "public"."template_userrule" IS '模板用户规则表';

-- ----------------------------
-- Table structure for template_userrule2threshold
-- ----------------------------
DROP TABLE IF EXISTS "public"."template_userrule2threshold";
CREATE TABLE "public"."template_userrule2threshold" (
  "thresholdvalue" float8 NOT NULL,
  "ruleid" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."template_userrule2threshold"."thresholdvalue" IS '阈值';
COMMENT ON COLUMN "public"."template_userrule2threshold"."ruleid" IS '规则id';
COMMENT ON TABLE "public"."template_userrule2threshold" IS '模板规则阈值';

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
COMMENT ON TABLE "public"."tenant" IS '租户表';

-- ----------------------------
-- Table structure for threshold_setting
-- ----------------------------
DROP TABLE IF EXISTS "public"."threshold_setting";
CREATE TABLE "public"."threshold_setting" (
  "id" int8 NOT NULL,
  "indicator_id" int8,
  "indicator_type" int4,
  "operator" varchar(255) COLLATE "pg_catalog"."default",
  "operate_values" jsonb,
  "create_time" timestamp(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "deleted" int4,
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."threshold_setting"."id" IS '主键id';
COMMENT ON COLUMN "public"."threshold_setting"."indicator_id" IS '指标id';
COMMENT ON COLUMN "public"."threshold_setting"."indicator_type" IS '指标类型';
COMMENT ON COLUMN "public"."threshold_setting"."operator" IS '比较操作符';
COMMENT ON COLUMN "public"."threshold_setting"."operate_values" IS '操作值';
COMMENT ON COLUMN "public"."threshold_setting"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."threshold_setting"."update_user_id" IS '更新人';
COMMENT ON COLUMN "public"."threshold_setting"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."threshold_setting"."deleted" IS '逻辑删除位(0已删除 1未删除)';
COMMENT ON TABLE "public"."threshold_setting" IS '指标阈值设置表';

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
COMMENT ON COLUMN "public"."time_limit"."id" IS '主键id';
COMMENT ON COLUMN "public"."time_limit"."name" IS '名称';
COMMENT ON COLUMN "public"."time_limit"."description" IS '描述';
COMMENT ON COLUMN "public"."time_limit"."grade" IS '等级';
COMMENT ON COLUMN "public"."time_limit"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."time_limit"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."time_limit"."creator" IS '创建者';
COMMENT ON COLUMN "public"."time_limit"."updater" IS '更新者';
COMMENT ON COLUMN "public"."time_limit"."state" IS '状态';
COMMENT ON COLUMN "public"."time_limit"."version" IS '版本';
COMMENT ON COLUMN "public"."time_limit"."delete" IS '删除标记';
COMMENT ON COLUMN "public"."time_limit"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."time_limit"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."time_limit"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."time_limit"."publisher" IS '发布者';
COMMENT ON COLUMN "public"."time_limit"."approveid" IS '审批者';
COMMENT ON COLUMN "public"."time_limit"."mark" IS '编码';
COMMENT ON COLUMN "public"."time_limit"."time_type" IS '相对时间单位';
COMMENT ON COLUMN "public"."time_limit"."time_range" IS '对时间范围';
COMMENT ON TABLE "public"."time_limit" IS '时间限定表';

-- ----------------------------
-- Table structure for time_limit_ind
-- ----------------------------
DROP TABLE IF EXISTS "public"."time_limit_ind";
CREATE TABLE "public"."time_limit_ind" (
  "id" int8 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "mark" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "start_time" date,
  "end_time" date,
  "deleted" int2 NOT NULL DEFAULT 1,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamptz(6) NOT NULL,
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "time_type" varchar(32) COLLATE "pg_catalog"."default",
  "time_range" varchar(32) COLLATE "pg_catalog"."default",
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_name" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."time_limit_ind"."id" IS '主键id';
COMMENT ON COLUMN "public"."time_limit_ind"."name" IS '名称';
COMMENT ON COLUMN "public"."time_limit_ind"."mark" IS '唯一编码';
COMMENT ON COLUMN "public"."time_limit_ind"."description" IS '描述';
COMMENT ON COLUMN "public"."time_limit_ind"."start_time" IS '绝对时间开始时间';
COMMENT ON COLUMN "public"."time_limit_ind"."end_time" IS '绝对时间结束时间';
COMMENT ON COLUMN "public"."time_limit_ind"."deleted" IS '是否删除';
COMMENT ON COLUMN "public"."time_limit_ind"."create_user_id" IS '创建者id';
COMMENT ON COLUMN "public"."time_limit_ind"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."time_limit_ind"."update_user_id" IS '更新者id';
COMMENT ON COLUMN "public"."time_limit_ind"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."time_limit_ind"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."time_limit_ind"."time_type" IS '相对时间单位：Y年，M月，D日';
COMMENT ON COLUMN "public"."time_limit_ind"."time_range" IS '相对时间范围：0当前单位，-1过去一单位';
COMMENT ON COLUMN "public"."time_limit_ind"."create_name" IS '创建者名字';
COMMENT ON COLUMN "public"."time_limit_ind"."update_name" IS '更新者名字';
COMMENT ON TABLE "public"."time_limit_ind" IS '时间限定指标';

-- ----------------------------
-- Table structure for time_limit_ind_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."time_limit_ind_log";
CREATE TABLE "public"."time_limit_ind_log" (
  "id" int8 NOT NULL,
  "log_id" int8 NOT NULL,
  "create_user_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "change" varchar(2000) COLLATE "pg_catalog"."default" NOT NULL,
  "log_time" timestamptz(6) NOT NULL,
  "create_time" timestamptz(6),
  "update_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "update_time" timestamptz(6),
  "deleted" int2 DEFAULT 1,
  "log_name" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."time_limit_ind_log"."id" IS '主键id';
COMMENT ON COLUMN "public"."time_limit_ind_log"."log_id" IS '时间限定的id';
COMMENT ON COLUMN "public"."time_limit_ind_log"."create_user_id" IS '创建该记录的用户id';
COMMENT ON COLUMN "public"."time_limit_ind_log"."change" IS '变更';
COMMENT ON COLUMN "public"."time_limit_ind_log"."log_time" IS '记录创建时间，也是修改时间';
COMMENT ON COLUMN "public"."time_limit_ind_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."time_limit_ind_log"."update_user_id" IS '更新者';
COMMENT ON COLUMN "public"."time_limit_ind_log"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."time_limit_ind_log"."deleted" IS '是否删除';
COMMENT ON COLUMN "public"."time_limit_ind_log"."log_name" IS '编辑者名字';
COMMENT ON TABLE "public"."time_limit_ind_log" IS '时间限定日志表';

-- ----------------------------
-- Table structure for user2apistar
-- ----------------------------
DROP TABLE IF EXISTS "public"."user2apistar";
CREATE TABLE "public"."user2apistar" (
  "apiguid" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "userid" varchar COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."user2apistar"."apiguid" IS 'api主键';
COMMENT ON COLUMN "public"."user2apistar"."userid" IS '用户id';
COMMENT ON TABLE "public"."user2apistar" IS '用户api关系表';

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
COMMENT ON COLUMN "public"."user_group"."id" IS '主键';
COMMENT ON COLUMN "public"."user_group"."tenant" IS '租户';
COMMENT ON COLUMN "public"."user_group"."name" IS '用户组名称';
COMMENT ON COLUMN "public"."user_group"."creator" IS '创建者';
COMMENT ON COLUMN "public"."user_group"."description" IS '描述';
COMMENT ON COLUMN "public"."user_group"."createtime" IS '创建时间';
COMMENT ON COLUMN "public"."user_group"."updatetime" IS '更新时间';
COMMENT ON COLUMN "public"."user_group"."valid" IS '是否有效';
COMMENT ON COLUMN "public"."user_group"."authorize_user" IS '操作者';
COMMENT ON COLUMN "public"."user_group"."authorize_time" IS '授权时间';
COMMENT ON TABLE "public"."user_group" IS '用户组';

-- ----------------------------
-- Table structure for user_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_group_relation";
CREATE TABLE "public"."user_group_relation" (
  "group_id" varchar(40) COLLATE "pg_catalog"."default",
  "user_id" varchar(40) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."user_group_relation"."group_id" IS '用户组id';
COMMENT ON COLUMN "public"."user_group_relation"."user_id" IS '用户id';
COMMENT ON TABLE "public"."user_group_relation" IS '用户和用户组关联表';

-- ----------------------------
-- Table structure for user_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_permission";
CREATE TABLE "public"."user_permission" (
  "user_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "username" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "account" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "permissions" bool NOT NULL DEFAULT false,
  "create_time" timestamp(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."user_permission"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."user_permission"."username" IS '用户名';
COMMENT ON COLUMN "public"."user_permission"."account" IS '账号';
COMMENT ON COLUMN "public"."user_permission"."permissions" IS '是否具有全局权限';
COMMENT ON COLUMN "public"."user_permission"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."user_permission" IS '用户权限表';

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
COMMENT ON COLUMN "public"."users"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."users"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."users"."valid" IS '有效标记';
COMMENT ON TABLE "public"."users" IS '用户表';

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
COMMENT ON COLUMN "public"."warning_group"."id" IS '主键';
COMMENT ON COLUMN "public"."warning_group"."name" IS '告警组名称';
COMMENT ON COLUMN "public"."warning_group"."type" IS '告警类型:0-系统,1-邮件,2-短信,';
COMMENT ON COLUMN "public"."warning_group"."contacts" IS '联系人';
COMMENT ON COLUMN "public"."warning_group"."category_id" IS '告警组id，共用规则分组';
COMMENT ON COLUMN "public"."warning_group"."description" IS '描述';
COMMENT ON COLUMN "public"."warning_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."warning_group"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."warning_group"."creator" IS '创建者id';
COMMENT ON COLUMN "public"."warning_group"."delete" IS '删除标记';
COMMENT ON COLUMN "public"."warning_group"."tenantid" IS '租户id';
COMMENT ON TABLE "public"."warning_group" IS '告警组';

-- ----------------------------
-- View structure for test_tenant_group_user_relation
-- ----------------------------
DROP VIEW IF EXISTS "public"."test_tenant_group_user_relation";
CREATE VIEW "public"."test_tenant_group_user_relation" AS  SELECT tenant.id AS tenant_id,
    tenant.name AS tenant_name,
    user_group.id AS user_group_id,
    user_group.name AS user_group_name,
    users.userid AS user_id,
    users.username AS user_name,
    users.account AS user_account
   FROM (((tenant
     JOIN user_group ON ((((user_group.tenant)::text = (tenant.id)::text) AND (user_group.valid = true))))
     JOIN user_group_relation ON (((user_group_relation.group_id)::text = (user_group.id)::text)))
     JOIN users ON ((((users.userid)::text = (user_group_relation.user_id)::text) AND (users.valid = true))));

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."number_seq"', 75, true);

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
-- Primary Key structure for table atom_indicator_version
-- ----------------------------
ALTER TABLE "public"."atom_indicator_version" ADD CONSTRAINT "atom_indicator_version_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table business_indicator_apply
-- ----------------------------
ALTER TABLE "public"."business_indicator_apply" ADD CONSTRAINT "business_indicator_apply_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_indicators
-- ----------------------------
ALTER TABLE "public"."business_indicators" ADD CONSTRAINT "business_indicators_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_indicators_history
-- ----------------------------
ALTER TABLE "public"."business_indicators_history" ADD CONSTRAINT "business_indicators_copy1_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_operation_records
-- ----------------------------
ALTER TABLE "public"."business_operation_records" ADD CONSTRAINT "business_operation_records_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_relation
-- ----------------------------
ALTER TABLE "public"."business_relation" ADD CONSTRAINT "business_relation_pkey" PRIMARY KEY ("relationshipguid");

-- ----------------------------
-- Primary Key structure for table business_tags
-- ----------------------------
ALTER TABLE "public"."business_tags" ADD CONSTRAINT "business_tags_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_unit
-- ----------------------------
ALTER TABLE "public"."business_unit" ADD CONSTRAINT "business_unit_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table composite_indicator
-- ----------------------------
ALTER TABLE "public"."composite_indicator" ADD CONSTRAINT "composite_indicator_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table composite_indicator_version
-- ----------------------------
ALTER TABLE "public"."composite_indicator_version" ADD CONSTRAINT "composite_indicator_version_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table derive_indicator
-- ----------------------------
ALTER TABLE "public"."derive_indicator" ADD CONSTRAINT "derive_indicator_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table derive_indicator_qualifier
-- ----------------------------
ALTER TABLE "public"."derive_indicator_qualifier" ADD CONSTRAINT "derive_indicator_qualifier_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table derive_indicator_version
-- ----------------------------
ALTER TABLE "public"."derive_indicator_version" ADD CONSTRAINT "derive_indicator_version_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table group_table_relation
-- ----------------------------
ALTER TABLE "public"."group_table_relation" ADD CONSTRAINT "group_table_relation_pkey" PRIMARY KEY ("derive_table_id", "user_group_id", "tenant_id");

-- ----------------------------
-- Primary Key structure for table indicator_lineage_trace
-- ----------------------------
ALTER TABLE "public"."indicator_lineage_trace" ADD CONSTRAINT "indicator_lineage_trace_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table indicator_threshold_log
-- ----------------------------
ALTER TABLE "public"."indicator_threshold_log" ADD CONSTRAINT "indicator_threshold_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table indicator_threshold_setting
-- ----------------------------
ALTER TABLE "public"."indicator_threshold_setting" ADD CONSTRAINT "threshold_setting_pkey" PRIMARY KEY ("id");

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
ALTER TABLE "public"."organization" ADD CONSTRAINT "organization_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table qualifier_ind
-- ----------------------------
ALTER TABLE "public"."qualifier_ind" ADD CONSTRAINT "qualifier_ind_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table qualifier_ind_log
-- ----------------------------
ALTER TABLE "public"."qualifier_ind_log" ADD CONSTRAINT "time_limit_log_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table time_limit_ind
-- ----------------------------
ALTER TABLE "public"."time_limit_ind" ADD CONSTRAINT "time_limit_ind_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table time_limit_ind_log
-- ----------------------------
ALTER TABLE "public"."time_limit_ind_log" ADD CONSTRAINT "qualifier_ind_log_copy1_pkey" PRIMARY KEY ("id");

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
-- Foreign Keys structure for table business_indicator_apply
-- ----------------------------
ALTER TABLE "public"."business_indicator_apply" ADD CONSTRAINT "business_indicator_apply_fk" FOREIGN KEY ("business_indicator_id") REFERENCES "public"."business_indicators" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

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

-- 数据质量-数据标准 sql
-- 升级数据
UPDATE data_standard
SET delete= TRUE
WHERE delete = FALSE;

-- 删除"数据标准内容"列
ALTER TABLE data_standard
DROP COLUMN content;

-- 修改字段
ALTER TABLE data_standard
    ALTER COLUMN version SET NOT NULL;
ALTER TABLE data_standard
    ALTER COLUMN version SET DEFAULT 0;

-- 新增字段
ALTER TABLE data_standard
    ADD name VARCHAR DEFAULT '' NOT NULL;
ALTER TABLE data_standard
    ADD standard_type INT DEFAULT 2 NOT NULL;
ALTER TABLE data_standard
    ADD data_type VARCHAR(16);
ALTER TABLE data_standard
    ADD data_length INT;
ALTER TABLE data_standard
    ADD allowable_value_flag BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE data_standard
    ADD allowable_value VARCHAR;
ALTER TABLE data_standard
    ADD standard_level INT;

COMMENT ON TABLE data_standard IS '数据标准';
COMMENT ON COLUMN data_standard.name IS '标准名称';
COMMENT ON COLUMN data_standard.standard_type IS '数据标准类型: 1 - 数据标准 2 - 命名标准';
COMMENT ON COLUMN data_standard.data_length IS '数据长度,非0正整数';
COMMENT ON COLUMN data_standard.allowable_value_flag IS '是否有允许值';
COMMENT ON COLUMN data_standard.allowable_value IS '允许值,用'';''分隔';
COMMENT ON COLUMN data_standard.standard_level IS '标准层级: 1-贴源层、2-基础层、3-通用层、4-应用层';
COMMENT ON COLUMN data_standard.data_type IS '枚举值:字符型(STRING)、双精度(DOUBLE)、长整型(BIGINT)、布尔类型(BOOLEAN)、高精度(DECIMAL)、日期类型(DATE)、时间戳类型(TIMESTAMP)';
COMMENT ON COLUMN data_standard.version IS '版本号: 默认当前版本为0,历史版本号均为非0正整数';

-- 数据质量-规则管理 sql
ALTER TABLE data_quality_rule_template
    ADD data_standard_code VARCHAR;

COMMENT ON COLUMN data_quality_rule_template.data_standard_code IS '参照数据标准编码: data_standard.number';


CREATE TABLE "public"."requirements" (
  "guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default",
  "num" varchar(255) COLLATE "pg_catalog"."default",
  "resource_type" int2,
  "version" varchar(255) COLLATE "pg_catalog"."default",
  "agreement" varchar(100) COLLATE "pg_catalog"."default",
  "request_mode" varchar(100) COLLATE "pg_catalog"."default",
  "aiming_field" text COLLATE "pg_catalog"."default",
  "file_name" varchar(255) COLLATE "pg_catalog"."default",
  "file_path" varchar(255) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "business_id" varchar(255) COLLATE "pg_catalog"."default",
  "table_id" varchar(255) COLLATE "pg_catalog"."default",
  "source_id" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(255) COLLATE "pg_catalog"."default",
  "creator" varchar(255) COLLATE "pg_catalog"."default",
  "status" int2,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete" int2
)
;
COMMENT ON COLUMN "public"."requirements"."guid" IS '主键id';
COMMENT ON COLUMN "public"."requirements"."name" IS '需求名称';
COMMENT ON COLUMN "public"."requirements"."num" IS '需求编号';
COMMENT ON COLUMN "public"."requirements"."resource_type" IS '资源类型 1：API 2：中间库 3：消息队列';
COMMENT ON COLUMN "public"."requirements"."version" IS '版本';
COMMENT ON COLUMN "public"."requirements"."agreement" IS '参数协议 HTTP/HTTPS';
COMMENT ON COLUMN "public"."requirements"."request_mode" IS '请求方式 GET/POST';
COMMENT ON COLUMN "public"."requirements"."aiming_field" IS '目标字段ID 多选，以数组形式保存';
COMMENT ON COLUMN "public"."requirements"."file_name" IS '文件名称';
COMMENT ON COLUMN "public"."requirements"."file_path" IS '文件地址';
COMMENT ON COLUMN "public"."requirements"."description" IS '描述';
COMMENT ON COLUMN "public"."requirements"."business_id" IS '业务对象id';
COMMENT ON COLUMN "public"."requirements"."table_id" IS '表ID';
COMMENT ON COLUMN "public"."requirements"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."requirements"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."requirements"."creator" IS '创建人ID';
COMMENT ON COLUMN "public"."requirements"."status" IS '1、待下发  2、已下发（待处理）  3、已处理（未反馈） 4、已反馈  -1、退回';
COMMENT ON COLUMN "public"."requirements"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."requirements"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."requirements"."delete" IS '是否删除 0 未删除 1:已删除';
COMMENT ON TABLE "public"."requirements" IS '需求管理表';

-- ----------------------------
-- Table structure for requirements_api
-- ----------------------------
CREATE TABLE "public"."requirements_api" (
  "guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "requirements_id" varchar(64) COLLATE "pg_catalog"."default",
  "project_id" varchar(64) COLLATE "pg_catalog"."default",
  "category_id" varchar(64) COLLATE "pg_catalog"."default",
  "api_id" varchar(64) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."requirements_api"."guid" IS '主键id';
COMMENT ON COLUMN "public"."requirements_api"."requirements_id" IS '需求管理表id';
COMMENT ON COLUMN "public"."requirements_api"."project_id" IS '项目ID';
COMMENT ON COLUMN "public"."requirements_api"."category_id" IS '目录id';
COMMENT ON COLUMN "public"."requirements_api"."api_id" IS 'api表ID';
COMMENT ON COLUMN "public"."requirements_api"."description" IS '描述';
COMMENT ON COLUMN "public"."requirements_api"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."requirements_api"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."requirements_api" IS '需求反馈结果表-api';

-- ----------------------------
-- Table structure for requirements_column
-- ----------------------------
CREATE TABLE "public"."requirements_column" (
  "guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "requirements_id" varchar(64) COLLATE "pg_catalog"."default",
  "table_id" varchar(64) COLLATE "pg_catalog"."default",
  "column_id" varchar(64) COLLATE "pg_catalog"."default",
  "operator" varchar(50) COLLATE "pg_catalog"."default",
  "sample_data" varchar(255) COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete" int2
)
;
COMMENT ON COLUMN "public"."requirements_column"."guid" IS '主键id';
COMMENT ON COLUMN "public"."requirements_column"."requirements_id" IS '需求管理表id';
COMMENT ON COLUMN "public"."requirements_column"."table_id" IS '表ID';
COMMENT ON COLUMN "public"."requirements_column"."column_id" IS '字段id';
COMMENT ON COLUMN "public"."requirements_column"."operator" IS '操作符';
COMMENT ON COLUMN "public"."requirements_column"."sample_data" IS '示例数据';
COMMENT ON COLUMN "public"."requirements_column"."description" IS '备注';
COMMENT ON COLUMN "public"."requirements_column"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."requirements_column"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."requirements_column"."delete" IS '是否删除 0 未删除 1:已删除';
COMMENT ON TABLE "public"."requirements_column" IS '需求管理-字段映射关系表';

-- ----------------------------
-- Table structure for requirements_database
-- ----------------------------
CREATE TABLE "public"."requirements_database" (
  "guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "requirements_id" varchar(64) COLLATE "pg_catalog"."default",
  "middle_type" varchar(255) COLLATE "pg_catalog"."default",
  "database" varchar(255) COLLATE "pg_catalog"."default",
  "table_name_en" varchar(255) COLLATE "pg_catalog"."default",
  "table_name_ch" varchar(255) COLLATE "pg_catalog"."default",
  "status" int2,
  "description" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "creator" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."requirements_database"."guid" IS '主键id';
COMMENT ON COLUMN "public"."requirements_database"."requirements_id" IS '需求管理表id';
COMMENT ON COLUMN "public"."requirements_database"."middle_type" IS '中间库类型';
COMMENT ON COLUMN "public"."requirements_database"."database" IS '数据库';
COMMENT ON COLUMN "public"."requirements_database"."table_name_en" IS '数据表英文名称';
COMMENT ON COLUMN "public"."requirements_database"."table_name_ch" IS '数据表中文名称';
COMMENT ON COLUMN "public"."requirements_database"."status" IS '1 上线 2 下线';
COMMENT ON COLUMN "public"."requirements_database"."description" IS '描述';
COMMENT ON COLUMN "public"."requirements_database"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."requirements_database"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."requirements_database"."creator" IS '创建人ID';
COMMENT ON TABLE "public"."requirements_database" IS '数据管理反馈结果-中间库';

-- ----------------------------
-- Table structure for requirements_mq
-- ----------------------------
CREATE TABLE "public"."requirements_mq" (
  "guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "requirements_id" varchar(64) COLLATE "pg_catalog"."default",
  "mq_name_en" varchar(255) COLLATE "pg_catalog"."default",
  "mq_name_ch" varchar(255) COLLATE "pg_catalog"."default",
  "format" varchar(255) COLLATE "pg_catalog"."default",
  "status" int2,
  "description" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "creator" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."requirements_mq"."guid" IS '主键id';
COMMENT ON COLUMN "public"."requirements_mq"."requirements_id" IS '需求管理表id';
COMMENT ON COLUMN "public"."requirements_mq"."mq_name_en" IS '消息队列英文名称';
COMMENT ON COLUMN "public"."requirements_mq"."mq_name_ch" IS '消息队列中文名称';
COMMENT ON COLUMN "public"."requirements_mq"."format" IS '格式';
COMMENT ON COLUMN "public"."requirements_mq"."status" IS '状态 1 上线 2下线';
COMMENT ON COLUMN "public"."requirements_mq"."description" IS '描述';
COMMENT ON COLUMN "public"."requirements_mq"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."requirements_mq"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."requirements_mq"."creator" IS '创建人ID';
COMMENT ON TABLE "public"."requirements_mq" IS '需求反馈结果表-消息队列';

-- ----------------------------
-- Table structure for requirements_result
-- ----------------------------
CREATE TABLE "public"."requirements_result" (
  "guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "requirements_id" varchar(64) COLLATE "pg_catalog"."default",
  "type" int2,
  "group_id" varchar(64) COLLATE "pg_catalog"."default",
  "user_id" varchar(64) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6)
)
;
COMMENT ON COLUMN "public"."requirements_result"."guid" IS '主键id';
COMMENT ON COLUMN "public"."requirements_result"."requirements_id" IS '需求管理表id';
COMMENT ON COLUMN "public"."requirements_result"."type" IS '1 同意 2 拒绝';
COMMENT ON COLUMN "public"."requirements_result"."group_id" IS '用户组id';
COMMENT ON COLUMN "public"."requirements_result"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."requirements_result"."description" IS '描述';
COMMENT ON COLUMN "public"."requirements_result"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."requirements_result"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."requirements_result" IS '需求处理结果表';

-- ----------------------------
-- Indexes structure for table requirements
-- ----------------------------
CREATE INDEX "re_idx_business_id" ON "public"."requirements" USING btree (
  "business_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "re_idx_creator" ON "public"."requirements" USING btree (
  "creator" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "re_idx_tenant_id" ON "public"."requirements" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table requirements
-- ----------------------------
ALTER TABLE "public"."requirements" ADD CONSTRAINT "requirements_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Indexes structure for table requirements_api
-- ----------------------------
CREATE INDEX "re_api_requirements_id" ON "public"."requirements_api" USING btree (
  "requirements_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table requirements_api
-- ----------------------------
ALTER TABLE "public"."requirements_api" ADD CONSTRAINT "requirements_api_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Indexes structure for table requirements_column
-- ----------------------------
CREATE INDEX "re_column_idx_column_id" ON "public"."requirements_column" USING btree (
  "column_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "re_column_idx_requirements_id" ON "public"."requirements_column" USING btree (
  "requirements_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table requirements_column
-- ----------------------------
ALTER TABLE "public"."requirements_column" ADD CONSTRAINT "requirements_column_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Indexes structure for table requirements_database
-- ----------------------------
CREATE INDEX "re_database_requirements_id" ON "public"."requirements_database" USING btree (
  "requirements_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table requirements_database
-- ----------------------------
ALTER TABLE "public"."requirements_database" ADD CONSTRAINT "requirements_database_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Indexes structure for table requirements_mq
-- ----------------------------
CREATE INDEX "re_mq_requirements_id" ON "public"."requirements_mq" USING btree (
  "requirements_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table requirements_mq
-- ----------------------------
ALTER TABLE "public"."requirements_mq" ADD CONSTRAINT "requirements_mq_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Indexes structure for table requirements_result
-- ----------------------------
CREATE INDEX "re_result_idx_requirements_id" ON "public"."requirements_result" USING btree (
  "requirements_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table requirements_result
-- ----------------------------
ALTER TABLE "public"."requirements_result" ADD CONSTRAINT "requirements_result_pkey" PRIMARY KEY ("guid");
