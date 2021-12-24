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
