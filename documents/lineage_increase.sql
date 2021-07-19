-- ----------------------------
-- Table structure for connector
-- ----------------------------
DROP TABLE IF EXISTS "public"."connector";
CREATE TABLE "public"."connector" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "connector_name" varchar(256) COLLATE "pg_catalog"."default",
  "connector_class" varchar(512) COLLATE "pg_catalog"."default",
  "type" varchar(32) COLLATE "pg_catalog"."default",
  "db_ip" varchar(128) COLLATE "pg_catalog"."default",
  "db_port" varchar(32) COLLATE "pg_catalog"."default",
  "pdb_name" varchar(128) COLLATE "pg_catalog"."default",
  "connector_unit" varchar(128) COLLATE "pg_catalog"."default",
  "user_name" varchar(256) COLLATE "pg_catalog"."default",
  "pass_word" varchar(256) COLLATE "pg_catalog"."default",
  "is_deleted" bool DEFAULT true
);
COMMENT ON COLUMN "public"."connector"."connector_name" IS 'connector名称';
COMMENT ON COLUMN "public"."connector"."connector_class" IS 'connector实现类';
COMMENT ON COLUMN "public"."connector"."type" IS '类型:MYSQL,ORACLE,POSTGRE';
COMMENT ON COLUMN "public"."connector"."db_ip" IS 'ip';
COMMENT ON COLUMN "public"."connector"."db_port" IS '端口号';
COMMENT ON COLUMN "public"."connector"."pdb_name" IS 'oracle pdb';
COMMENT ON COLUMN "public"."connector"."connector_unit" IS '采集最小单元：oracle-实例名称；mysql，PG-数据库名称';
COMMENT ON COLUMN "public"."connector"."user_name" IS '用户名';
COMMENT ON COLUMN "public"."connector"."pass_word" IS '数据库密码';
COMMENT ON COLUMN "public"."connector"."is_deleted" IS '是否已删除';
ALTER TABLE "public"."connector" ADD CONSTRAINT "connector_pkey" PRIMARY KEY ("id");




-- ----------------------------
-- Table structure for db_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."db_info";
CREATE TABLE "public"."db_info" (
  "database_guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "database_name" varchar(256) COLLATE "pg_catalog"."default",
  "owner" varchar(256) COLLATE "pg_catalog"."default",
  "db_type" varchar(64) COLLATE "pg_catalog"."default",
  "is_deleted" bool DEFAULT false,
  "status" varchar(64) COLLATE "pg_catalog"."default",
  "database_description" varchar(255) COLLATE "pg_catalog"."default",
  "instance_guid" varchar COLLATE "pg_catalog"."default"
);
COMMENT ON COLUMN "public"."db_info"."database_guid" IS '唯一标识符';
COMMENT ON COLUMN "public"."db_info"."database_name" IS '数据库名称';
COMMENT ON COLUMN "public"."db_info"."owner" IS '创建者';
COMMENT ON COLUMN "public"."db_info"."db_type" IS '数据库类型';
COMMENT ON COLUMN "public"."db_info"."is_deleted" IS '是否删除';
COMMENT ON COLUMN "public"."db_info"."status" IS '状态:已删除-DELETED;未删除-ACTIVE';
COMMENT ON COLUMN "public"."db_info"."database_description" IS '数据库描述';
COMMENT ON COLUMN "public"."db_info"."instance_guid" IS '图数据库中数据源（实例）guid';
ALTER TABLE "public"."db_info" ADD CONSTRAINT "db_info_pkey" PRIMARY KEY ("database_guid");


-- ----------------------------
-- Table structure for source_db
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_db";
CREATE TABLE "public"."source_db" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "db_guid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL
);
COMMENT ON COLUMN "public"."source_db"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."source_db"."db_guid" IS '数据库id';

ALTER TABLE "public"."source_db" ADD CONSTRAINT "source_db_pkey" PRIMARY KEY ("id");

ALTER TABLE tableinfo DROP COLUMN IF EXISTS "source_id";