DROP TABLE IF EXISTS "public"."database_group_relation";
CREATE TABLE "public"."database_group_relation" (
"id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
"group_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
"source_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
"database_guid" varchar(36) COLLATE "pg_catalog"."default" NOT NULL
);
COMMENT ON COLUMN "public"."database_group_relation"."id" IS 'id';
COMMENT ON COLUMN "public"."database_group_relation"."group_id" IS '用户组id';
COMMENT ON COLUMN "public"."database_group_relation"."source_id" IS '数据源id';
COMMENT ON COLUMN "public"."database_group_relation"."database_guid" IS '数据库id';
ALTER TABLE "public"."database_group_relation" ADD CONSTRAINT "database_group_relation_pkey" PRIMARY KEY ("id");

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
  "instance_guid" varchar COLLATE "pg_catalog"."default"
);
COMMENT ON COLUMN "public"."db_info"."database_guid" IS '唯一标识符';
COMMENT ON COLUMN "public"."db_info"."database_name" IS '数据库名称';
COMMENT ON COLUMN "public"."db_info"."owner" IS '创建者';
COMMENT ON COLUMN "public"."db_info"."db_type" IS '数据库类型';
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

ALTER TABLE table_relation ADD COLUMN IF NOT EXISTS "tenant_id" VARCHAR(64);

CREATE TABLE "source_info" (
  "id" varchar(50) NOT NULL,
  "category_id" varchar(64) ,
  "database_id" varchar(64) NOT NULL,
  "database_alias" varchar(255) NOT NULL,
  "planning_package_code" varchar(255),
  "planning_package_name" varchar(255),
  "extract_tool" varchar(255),
  "extract_cycle" varchar(255),
  "security" bool,
  "security_cycle" varchar(255),
  "importance" bool,
  "description" text,
  "approve_id" varchar(50),
  "approve_group_id" varchar(64),
  "updater" varchar(255),
  "creator" varchar(255) NOT NULL,
  "status" varchar(50) NOT NULL,
  "annex_id" varchar(64),
  "bo_name" varchar(255) NOT NULL,
  "bo_department_name" varchar(100) NOT NULL,
  "bo_email" varchar(100) NOT NULL,
  "bo_tel" varchar(100) NOT NULL,
  "to_name" varchar(255) NOT NULL,
  "to_department_name" varchar(100) NOT NULL,
  "to_email" varchar(100) NOT NULL,
  "to_tel" varchar(100) NOT NULL,
  "technical_leader" varchar(255) NOT NULL,
  "business_leader" varchar(255) NOT NULL,
  "tenant_id" varchar(50) NOT NULL,
  "version" int8 NOT NULL,
  "update_time" timestamptz,
  "record_time" timestamptz NOT NULL,
  "create_time" timestamptz NOT NULL,
  "modify_time" timestamptz NOT NULL,
  "data_source_id" varchar(64),
  PRIMARY KEY ("id","version")
);

COMMENT ON COLUMN "source_info"."category_id" IS '数据层/区Id';
COMMENT ON COLUMN "source_info"."database_id" IS '所选数据库实例id';
COMMENT ON COLUMN "source_info"."database_alias" IS '数据库别名（中文名）';
COMMENT ON COLUMN "source_info"."planning_package_code" IS '规划包编号';
COMMENT ON COLUMN "source_info"."planning_package_name" IS '规划包名称';
COMMENT ON COLUMN "source_info"."extract_tool" IS '抽取工具';
COMMENT ON COLUMN "source_info"."extract_cycle" IS '抽取周期';
COMMENT ON COLUMN "source_info"."security" IS '是否保密';
COMMENT ON COLUMN "source_info"."security_cycle" IS '保密期限';
COMMENT ON COLUMN "source_info"."importance" IS '是否重要';
COMMENT ON COLUMN "source_info"."description" IS '描述';
COMMENT ON COLUMN "source_info"."approve_id" IS '对接审批系统id';
COMMENT ON COLUMN "source_info"."approve_group_id" IS '送审审批组id';
COMMENT ON COLUMN "source_info"."updater" IS '更新人账号';
COMMENT ON COLUMN "source_info"."creator" IS '记录人账号';
COMMENT ON COLUMN "source_info"."status" IS '源信息状态';
COMMENT ON COLUMN "source_info"."annex_id" IS '附件id';
COMMENT ON COLUMN "source_info"."bo_name" IS '业务对接人姓名(business_owner_name)';
COMMENT ON COLUMN "source_info"."bo_department_name" IS '业务对接人部门名称(business_owner_department_name)';
COMMENT ON COLUMN "source_info"."bo_email" IS '业务对接人邮件地址(business_owner_email)';
COMMENT ON COLUMN "source_info"."bo_tel" IS '业务对接人电话(business_owner_tel)';
COMMENT ON COLUMN "source_info"."to_name" IS '技术对接人姓名(tech_owner)';
COMMENT ON COLUMN "source_info"."to_department_name" IS '技术对接人部门名称(technology_owner_department_name)';
COMMENT ON COLUMN "source_info"."to_email" IS '技术对接人邮件地址(technology_owner_email)';
COMMENT ON COLUMN "source_info"."to_tel" IS '技术对接人电话(technology_owner_tel)';
COMMENT ON COLUMN "source_info"."technical_leader" IS '技术主管';
COMMENT ON COLUMN "source_info"."business_leader" IS '业务主管';
COMMENT ON COLUMN "source_info"."tenant_id" IS '租户id';
COMMENT ON COLUMN "source_info"."version" IS '版本号';
COMMENT ON COLUMN "source_info"."update_time" IS '信息最后更新时间';
COMMENT ON COLUMN "source_info"."record_time" IS '信息录入时间';
COMMENT ON COLUMN "source_info"."create_time" IS '数据创建时间戳';
COMMENT ON COLUMN "source_info"."modify_time" IS '数据修改时间戳';
COMMENT ON COLUMN "source_info"."data_source_id" IS '数据源id';
COMMENT ON TABLE "source_info" IS '用于储存源信息';

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

CREATE TABLE "annex" (
  "annex_id" varchar(50) NOT NULL,
  "file_name" varchar(255) NOT NULL,
  "file_type" varchar(20) NOT NULL,
  "path" varchar(255) NOT NULL,
  "create_time" timestamptz,
  "modify_time" timestamptz,
  PRIMARY KEY ("annex_id")
);
COMMENT ON COLUMN "annex"."annex_id" IS '主键';
COMMENT ON COLUMN "annex"."file_name" IS '文件名';
COMMENT ON COLUMN "annex"."file_type" IS '文件类型';
COMMENT ON COLUMN "annex"."path" IS '文件地址';

-- 附件表增加文件大小字段
ALTER TABLE public.annex
    ADD COLUMN file_size bigint;

COMMENT ON COLUMN public.annex.file_size
    IS '文件大小';

CREATE TABLE "code_annex_type" (
  "code" varchar(50) NOT NULL,
	"action" varchar(50) NOT NULL,
  PRIMARY KEY ("code")
);
COMMENT ON COLUMN "code_annex_type"."code" IS '附件类型';
COMMENT ON COLUMN "code_annex_type"."action" IS '对应的功能模块';

INSERT INTO "public"."code_annex_type" ("code", "action") VALUES ('png', 'sourceInfo');
INSERT INTO "public"."code_annex_type" ("code", "action") VALUES ('pdf', 'sourceInfo');
INSERT INTO "public"."code_annex_type" ("code", "action") VALUES ('doc', 'sourceInfo');
INSERT INTO "public"."code_annex_type" ("code", "action") VALUES ('xls', 'sourceInfo');
INSERT INTO "public"."code_annex_type" ("code", "action") VALUES ('xlsx', 'sourceInfo');
-- 增加上传文件类型脚本
insert into code_annex_type (code,action) values('jpg','sourceInfo'),('jpeg','sourceInfo'),('gif','sourceInfo');

CREATE TABLE "code_source_info_status" (
  "code" varchar(50) NOT NULL,
  "name" varchar(255) NOT NULL,
  PRIMARY KEY ("code")
);
COMMENT ON COLUMN "code_source_info_status"."code" IS '源信息状态码';
COMMENT ON COLUMN "code_source_info_status"."name" IS '源信息状态名';

INSERT INTO "public"."code_source_info_status" ("code", "name") VALUES ('0', '待发布');
INSERT INTO "public"."code_source_info_status" ("code", "name") VALUES ('1', '待审批');
INSERT INTO "public"."code_source_info_status" ("code", "name") VALUES ('2', '发布审核不通过');
INSERT INTO "public"."code_source_info_status" ("code", "name") VALUES ('3', '发布审核通过');

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
  "create_time" timestamptz NOT NULL,
  "update_time" timestamptz
)
;
COMMENT ON COLUMN "public"."table_data_source_relation"."id" IS '主键id';
COMMENT ON COLUMN "public"."table_data_source_relation"."category_id" IS '目录id';
COMMENT ON COLUMN "public"."table_data_source_relation"."table_id" IS '表id';
COMMENT ON COLUMN "public"."table_data_source_relation"."data_source_id" IS '数据源id';
COMMENT ON COLUMN "public"."table_data_source_relation"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."table_data_source_relation"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."table_data_source_relation"."update_time" IS '更新时间';

CREATE TABLE "source_info_relation2parent_category" (
  "source_info_id" varchar(100) NOT NULL,
  "parent_category_id" varchar(100) NOT NULL,
  "create_time" timestamptz,
  "modify_time" timestamptz
);
COMMENT ON COLUMN "source_info_relation2parent_category"."source_info_id" IS '源信息id';
COMMENT ON COLUMN "source_info_relation2parent_category"."parent_category_id" IS '源信息关联库目录创建前的父级目录id';

ALTER TABLE "annex" ADD CONSTRAINT "fk_annex_file_type" FOREIGN KEY ("file_type") REFERENCES "code_annex_type" ("code") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "source_info" ADD CONSTRAINT "fk_source_info_status" FOREIGN KEY ("status") REFERENCES "code_source_info_status" ("code") ON DELETE NO ACTION ON UPDATE CASCADE;
ALTER TABLE "source_info"  ADD CONSTRAINT "fk_source_info_tenant_id" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- 源信息登记-衍生表信息表
-- ----------------------------
DROP TABLE IF EXISTS "public"."source_info_derive_table_column_relation";
DROP TABLE IF EXISTS "public"."source_info_derive_table_info";
DROP TABLE IF EXISTS "public"."source_info_derive_column_info";

CREATE TABLE "public"."source_info_derive_table_info" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "table_guid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "table_name_en" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "table_name_zh" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "procedure" varchar(100) COLLATE "pg_catalog"."default",
  "category_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "db_type" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "db_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "source_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "business_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "update_frequency" varchar(100) COLLATE "pg_catalog"."default",
  "etl_policy" varchar(100) COLLATE "pg_catalog"."default",
  "incre_standard" varchar(100) COLLATE "pg_catalog"."default",
  "clean_rule" varchar(100) COLLATE "pg_catalog"."default",
  "filter" varchar(100) COLLATE "pg_catalog"."default",
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
COMMENT ON COLUMN "public"."source_info_derive_table_info"."db_type" IS '目标层级/库类型（HIVE或ORACLE）';
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
-- 源信息登记-衍生字段信息表
-- ----------------------------
CREATE TABLE "public"."source_info_derive_column_info" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "column_guid" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "column_name_en" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "column_name_zh" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "data_type" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "source_column_guid" varchar(100) COLLATE "pg_catalog"."default",
  "primary_key" bool NOT NULL,
  "remove_sensitive" bool NOT NULL,
  "mapping_rule" text COLLATE "pg_catalog"."default",
  "mapping_describe" varchar(255) COLLATE "pg_catalog"."default",
  "group_field" bool NOT NULL,
  "secret" bool NOT NULL,
  "secret_period" varchar(100) COLLATE "pg_catalog"."default",
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
-- 源信息登记-衍生表-字段关系表
-- ----------------------------
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

CREATE TABLE IF NOT EXISTS "public"."column_tag" (
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

CREATE TABLE IF NOT EXISTS "public"."column_tag_relation_to_column" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "column_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "tag_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."column_tag_relation_to_column"."id" IS '关联主键';
COMMENT ON COLUMN "public"."column_tag_relation_to_column"."column_id" IS '字段id';
COMMENT ON COLUMN "public"."column_tag_relation_to_column"."tag_id" IS '字段id';

ALTER TABLE category ADD sort int2;
COMMENT ON COLUMN public.category.sort IS '排序';
ALTER TABLE "public"."category" ADD COLUMN "private_status" varchar(50) DEFAULT 'PUBLIC';
COMMENT ON COLUMN "public"."category"."private_status" IS '私密状态';
ALTER TABLE "public"."category_group_relation" ADD PRIMARY KEY ("category_id", "group_id");
ALTER TABLE column_info ADD partition_field bool;
COMMENT ON COLUMN public.column_info.partition_field IS '是否为分区字段';

TRUNCATE TABLE organization;
ALTER TABLE "organization" DROP CONSTRAINT IF EXISTS "organization_pkey";
ALTER TABLE "organization" ADD CONSTRAINT  "organization_pkey" PRIMARY KEY("id");

DELETE FROM tableinfo WHERE source_id is not NULL and source_id != '' AND source_id != 'hive';
CREATE INDEX "idx_column_info_table_guid" ON "public"."column_info" USING btree (
  "table_guid" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_column_metadata_history_table_guid" ON "public"."column_metadata_history" USING btree (
  "table_guid" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

ALTER TABLE "db_info" DROP CONSTRAINT IF EXISTS "db_info_pkey";
insert into db_info (database_guid,database_name,owner,db_type,status,instance_guid)
SELECT DISTINCT databaseguid,dbname,owner,'HIVE' as db_type,databasestatus,'hive' as instance_guid FROM tableinfo WHERE source_id = 'hive' AND
status = 'ACTIVE' AND databasestatus = 'ACTIVE';
delete FROM  column_info WHERE table_guid in (
select DISTINCT table_guid from column_info  as co left join tableinfo as tb on co.table_guid = tb.tableguid where tb.tableguid is null
);
delete FROM column_metadata_history WHERE table_guid in (
SELECT DISTINCT table_guid from column_metadata_history  as co LEFT JOIN tableinfo as tb on co.table_guid = tb.tableguid WHERE tb.tableguid is null
);
DELETE FROM category WHERE categorytype = 1;
TRUNCATE TABLE table_relation;
TRUNCATE TABLE business2table;
TRUNCATE TABLE businessinfo;
TRUNCATE TABLE business_relation;
TRUNCATE TABLE category_group_relation