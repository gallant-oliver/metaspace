-- ----------------------------
-- Table structure for connector
-- ----------------------------
DROP TABLE IF EXISTS "public"."connector";
CREATE TABLE "public"."connector" (
"id" varchar(64) COLLATE "default" NOT NULL,
"connector_name" varchar(256) COLLATE "default",
"connector.class" varchar(512) COLLATE "default",
"type" varchar(32) COLLATE "default",
"db_ip" varchar(128) COLLATE "default",
"db_port" int4,
"pdb_name" varchar(128) COLLATE "default",
"db_name" varchar(128) COLLATE "default",
"user_name" varchar(256) COLLATE "default",
"pass_word" varchar(256) COLLATE "default",
"connector_url" varchar(256) COLLATE "default",
"status" varchar(32) COLLATE "default",
"is_deleted" bool DEFAULT true
);
COMMENT ON COLUMN "public"."connector"."connector_name" IS 'connector名称';
COMMENT ON COLUMN "public"."connector"."connector.class" IS 'connector实现类';
COMMENT ON COLUMN "public"."connector"."type" IS '类型:MYSQL,ORACLE,POSTGRE';
COMMENT ON COLUMN "public"."connector"."db_ip" IS 'ip或host';
COMMENT ON COLUMN "public"."connector"."db_port" IS '端口号';
COMMENT ON COLUMN "public"."connector"."pdb_name" IS 'oracle pdb';
COMMENT ON COLUMN "public"."connector"."db_name" IS '数据库';
COMMENT ON COLUMN "public"."connector"."user_name" IS '用户名';
COMMENT ON COLUMN "public"."connector"."pass_word" IS '数据库密码';
COMMENT ON COLUMN "public"."connector"."connector_url" IS 'connector请求路径';
COMMENT ON COLUMN "public"."connector"."status" IS 'connector状态：RUNNING,STOP';
COMMENT ON COLUMN "public"."connector"."is_deleted" IS '是否已删除';

ALTER TABLE "public"."connector" ADD PRIMARY KEY ("id");


-- ----------------------------
-- Table structure for db_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."db_info";
CREATE TABLE "public"."db_info" (
"guid" varchar(64) COLLATE "default" NOT NULL,
"name" varchar(256) COLLATE "default",
"owner" varchar(256) COLLATE "default",
"connector_id" varchar(64) COLLATE "default",
"db_type" varchar(64) COLLATE "default"
);
COMMENT ON COLUMN "public"."db_info"."guid" IS '唯一标识符';
COMMENT ON COLUMN "public"."db_info"."owner" IS '创建者';
COMMENT ON COLUMN "public"."db_info"."connector_id" IS 'connector主键';
COMMENT ON COLUMN "public"."db_info"."db_type" IS '数据库类型';

ALTER TABLE "public"."db_info" ADD PRIMARY KEY ("guid");

-- ----------------------------
-- Table structure for source_db
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_db";
CREATE TABLE "public"."source_db" (
"id" varchar(64) COLLATE "default" NOT NULL,
"source_id" varchar(64) COLLATE "default" NOT NULL,
"db_guid" varchar(64) COLLATE "default" NOT NULL
);
COMMENT ON COLUMN "public"."source_db"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."source_db"."db_guid" IS '数据库id';
ALTER TABLE "public"."source_db" ADD PRIMARY KEY ("id");

ALTER TABLE tableinfo ADD COLUMN IF NOT EXISTS db_guid VARCHAR(64);