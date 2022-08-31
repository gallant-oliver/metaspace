-- ----------------------------
-- Table structure for file_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."file_info";
CREATE TABLE "public"."file_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "file_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "file_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "file_size" int8,
  "file_path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "create_user" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "delete" bool,
  "business_path" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."file_info"."id" IS 'id';
COMMENT ON COLUMN "public"."file_info"."file_name" IS '文件名称';
COMMENT ON COLUMN "public"."file_info"."file_type" IS '文件格式';
COMMENT ON COLUMN "public"."file_info"."file_size" IS '文件大小（B）';
COMMENT ON COLUMN "public"."file_info"."file_path" IS '文件路径';
COMMENT ON COLUMN "public"."file_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."file_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."file_info"."create_user" IS '创建人（邮箱）';
COMMENT ON COLUMN "public"."file_info"."delete" IS '是否删除';
COMMENT ON COLUMN "public"."file_info"."business_path" IS '业务目录';
COMMENT ON TABLE "public"."file_info" IS '文件归档-文件信息表';

-- ----------------------------
-- Indexes structure for table file_info
-- ----------------------------
CREATE INDEX "idx_file_info_create_time" ON "public"."file_info" USING btree (
  "create_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_file_info_file_name" ON "public"."file_info" USING btree (
  "file_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table file_info
-- ----------------------------
ALTER TABLE "public"."file_info" ADD CONSTRAINT "file_info_pkey" PRIMARY KEY ("id");

DROP TABLE IF EXISTS "public"."file_comment";
CREATE TABLE "public"."file_comment" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "file_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6),
  "create_user" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."file_comment"."id" IS 'id';
COMMENT ON COLUMN "public"."file_comment"."file_id" IS '文件id';
COMMENT ON COLUMN "public"."file_comment"."name" IS '备注内容';
COMMENT ON COLUMN "public"."file_comment"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."file_comment"."create_user" IS '创建人（邮箱）';
COMMENT ON TABLE "public"."file_comment" IS '文件备注表';

-- ----------------------------
-- Indexes structure for table file_comment
-- ----------------------------
CREATE INDEX "idx_file_comment_file_id" ON "public"."file_comment" USING btree (
  "file_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table file_comment
-- ----------------------------
ALTER TABLE "public"."file_comment" ADD CONSTRAINT "file_comment_pkey" PRIMARY KEY ("id");

DROP TABLE IF EXISTS "public"."message_center";
CREATE TABLE "public"."message_center" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int2 NOT NULL,
  "module" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "status" int2 NOT NULL,
  "tenantid" varchar(64) COLLATE "pg_catalog"."default",
  "create_user" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "operation_time" timestamp(6),
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "delete" bool
)
;
COMMENT ON COLUMN "public"."message_center"."id" IS 'id';
COMMENT ON COLUMN "public"."message_center"."name" IS '标题内容';
COMMENT ON COLUMN "public"."message_center"."type" IS '消息类型（0：资源审核信息、1：用户组信息、2：数据服务、3：需求审批）';
COMMENT ON COLUMN "public"."message_center"."module" IS '所属模块（系统管理/用户组管理）';
COMMENT ON COLUMN "public"."message_center"."status" IS '0未读、1已读';
COMMENT ON COLUMN "public"."message_center"."tenantid" IS '租户id';
COMMENT ON COLUMN "public"."message_center"."create_user" IS '创建人（邮箱）';
COMMENT ON COLUMN "public"."message_center"."operation_time" IS '操作时间';
COMMENT ON COLUMN "public"."message_center"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."message_center"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."message_center"."delete" IS '是否删除';
COMMENT ON TABLE "public"."message_center" IS '消息中心表';

-- ----------------------------
-- Primary Key structure for table message_center
-- ----------------------------
ALTER TABLE "public"."message_center" ADD CONSTRAINT "message_center_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- 消息表增加流程进度字段
ALTER TABLE message_center ADD COLUMN process int2 NOT NULL DEFAULT 6;

COMMENT ON COLUMN "public"."message_center"."process" IS '流程进度(0-已审批、1-未审批、2-已授权、3-已移除、4-已处理、5-已反馈、6-待处理';

-- 给现有租户增加表描述空值规则
insert into data_quality_rule_template(id,name,scope,unit,description,delete,create_time,update_time,rule_type,code,enable,type,tenantid) select * from (select '32' as id,'表描述空值校验' as name,3 as scope,'个' as unit,'数据表的描述信息完整性' as description,false as delete,now() as create_time,now() as update_time,'rule_2' as rule_type,'32' as code,true as enable,33 as type) a,(select distinct id as tenantid from tenant) t;

-- source_info表增加索引
CREATE INDEX "source_info_database_id" ON "public"."source_info" USING btree (
  "database_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
COMMENT ON INDEX "public"."source_info_database_id" IS '数据库id索引';

-- tableInfo表增加索引
CREATE INDEX "tableInfo_databaseguid" ON "public"."tableinfo" USING btree (
  "databaseguid" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "tableInfo_tablename" ON "public"."tableinfo" USING btree (
  "tablename" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);


-- 业务对象表新增'流程名称'和'相关制度文件名称'两个字段

ALTER TABLE businessinfo ADD COLUMN process_name VARCHAR(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying;

COMMENT ON COLUMN "public"."businessinfo"."process_name" IS '流程名称';

ALTER TABLE businessinfo ADD COLUMN system_file_name VARCHAR(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying;

COMMENT ON COLUMN "public"."businessinfo"."system_file_name" IS '相关制度文件名称';

-- 将字段时间类型由timestamptz改为timestamp，否则映射实体类型LocalDateTime会报转换错误

ALTER TABLE "public"."api_poly" ALTER COLUMN create_time type timestamp(6);

ALTER TABLE "public"."api_poly" ALTER COLUMN update_time type timestamp(6);
--***************************************************************************************************************************************************


ALTER TABLE approval_group_relation DROP CONSTRAINT fk1;
ALTER TABLE approval_group_relation DROP CONSTRAINT fk2;
ALTER TABLE users DROP CONSTRAINT user_pkey;
ALTER TABLE users ADD CONSTRAINT user_pkey PRIMARY KEY ("account");


-- 分布式锁表shedlock
CREATE TABLE "public"."shedlock"(
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name));
