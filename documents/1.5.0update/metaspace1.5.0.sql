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
ALTER TABLE "public"."api_module" OWNER TO "postgres";

-- ----------------------------
-- Records of api_module
-- ----------------------------
BEGIN;
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
INSERT INTO "public"."api_module" VALUES ('/businessManage/datashare/{apiGuid}', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}/technical', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/table/{guid}', 'GET', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category', 'GET', 1, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage', 'POST', 5, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}/datashare', 'POST', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/datashare/test/{randomName}', 'POST', 2, 'f');
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
INSERT INTO "public"."api_module" VALUES ('/businessManage/datashare/test/{randomName}', 'PUT', 2, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}', 'PUT', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/businessManage/{businessId}/business', 'PUT', 4, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/organization', 'PUT', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category/{categoryGuid}', 'DELETE', 8, 'f');
INSERT INTO "public"."api_module" VALUES ('/technical/category/relation', 'DELETE', 3, 'f');
INSERT INTO "public"."api_module" VALUES ('privilege', 'OPTION', 6, 't');
COMMIT;

-- ----------------------------
-- Primary Key structure for table api_module
-- ----------------------------
ALTER TABLE "public"."api_module" ADD CONSTRAINT "api_module_pkey" PRIMARY KEY ("path", "method");

GRANT ALL PRIVILEGES ON api_module TO metaspace;


CREATE TABLE "public"."operate_log" (
  "id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "number" varchar(20) COLLATE "pg_catalog"."default",
  "userid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(20) COLLATE "pg_catalog"."default",
  "module" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "content" varchar COLLATE "pg_catalog"."default",
  "result" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "ip" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "createtime" timestamptz(0) NOT NULL
)
;
ALTER TABLE "public"."operate_log" OWNER TO "postgres";
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

grant all PRIVILEGES on operate_log to metaspace;


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

 Date: 13/08/2019 18:27:53
*/


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
ALTER TABLE "public"."data_standard" OWNER TO "postgres";
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

grant all PRIVILEGES on data_standard to metaspace;


update module set modulename = '数据分享' where moduleid='10';

insert into module values ('11', '数据标准', '1'), ('12', '日志审计', '1');

insert into privilege2module values('1',11),('1','12');