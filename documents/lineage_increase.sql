
ALTER TABLE tableinfo ADD COLUMN type VARCHAR(32) DEFAULT 'table';

ALTER TABLE tableinfo ADD COLUMN owner VARCHAR(32);
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
  "start_scn" int8 DEFAULT -1
);
ALTER TABLE "public"."connector" ADD CONSTRAINT "connector_pkey" PRIMARY KEY ("id");
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
  "instance_guid" varchar COLLATE "pg_catalog"."default",
  "category_id" varchar(64) COLLATE "pg_catalog"."default"
);
COMMENT ON COLUMN "public"."db_info"."database_guid" IS '唯一标识符';
COMMENT ON COLUMN "public"."db_info"."database_name" IS '数据库名称';
COMMENT ON COLUMN "public"."db_info"."owner" IS '创建者';
COMMENT ON COLUMN "public"."db_info"."db_type" IS '数据库类型';
COMMENT ON COLUMN "public"."db_info"."status" IS '状态:已删除-DELETED;未删除-ACTIVE';
COMMENT ON COLUMN "public"."db_info"."database_description" IS '数据库描述';
COMMENT ON COLUMN "public"."db_info"."instance_guid" IS '图数据库中数据源（实例）guid';
COMMENT ON COLUMN "public"."db_info"."category_id" IS '数据库关联的目录id';
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

ALTER TABLE table_relation ADD COLUMN IF NOT EXISTS "tenant_id" VARCHAR(64);

-- ALTER TABLE tableinfo DROP COLUMN IF EXISTS "source_id";