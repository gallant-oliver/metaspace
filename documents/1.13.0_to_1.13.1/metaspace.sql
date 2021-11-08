CREATE TABLE IF NOT EXISTS public.user_permission
(
    user_id character varying(50) NOT NULL,
    username character varying(64) NOT NULL,
    account character varying(64) NOT NULL,
    permissions boolean NOT NULL DEFAULT false,
    create_time timestamp without time zone NOT NULL
);


COMMENT ON TABLE public.user_permission
    IS '用户权限表';


COMMENT ON COLUMN public.user_permission.user_id
    IS '用户id';

COMMENT ON COLUMN public.user_permission.username
    IS '用户名';

COMMENT ON COLUMN public.user_permission.account
    IS '账号';

COMMENT ON COLUMN public.user_permission.permissions
    IS '是否具有全局权限';

COMMENT ON COLUMN public.user_permission.create_time
    IS '创建时间';

--删除业务目录历史数据

delete from  public.category  where  categorytype='1';

--新增字段

ALTER TABLE public.category ADD COLUMN publish bool;

ALTER TABLE public.category ADD COLUMN information text;

ALTER TABLE public.category ADD COLUMN approval_id VARCHAR(64);

COMMENT ON COLUMN public.category.publish IS '是否发布： t-已发布  f-未发布';

COMMENT ON COLUMN public.category.information IS '审批组说明';

COMMENT ON COLUMN public.category.approval_id IS '审批记录id';

TRUNCATE TABLE "public".businessinfo;
TRUNCATE TABLE "public".business2table;
TRUNCATE TABLE "public".business_relation;

ALTER TABLE "public"."businessinfo" ADD COLUMN "publish" bool;
ALTER TABLE "public"."businessinfo" ADD COLUMN "status" varchar(10);
ALTER TABLE "public"."businessinfo" ADD COLUMN "publish_desc" varchar(256);
ALTER TABLE "public"."businessinfo" ADD COLUMN "approve_group_id" varchar(64);
ALTER TABLE "public"."businessinfo" ADD COLUMN "approve_id" varchar(64);
ALTER TABLE "public"."businessinfo" ADD COLUMN "create_mode" int2;
ALTER TABLE "public"."businessinfo" ADD COLUMN "private_status" varchar(64);
ALTER TABLE "public"."businessinfo" ADD COLUMN "submitter_read" bool;

COMMENT ON COLUMN "public"."businessinfo"."publish" IS '发布开关';
COMMENT ON COLUMN "public"."businessinfo"."status" IS '业务对象状态：0待发布，1待审批，2审核不通过，3审核通过';
COMMENT ON COLUMN "public"."businessinfo"."publish_desc" IS '发布说明信息';
COMMENT ON COLUMN "public"."businessinfo"."approve_group_id" IS '审批组id';
COMMENT ON COLUMN "public"."businessinfo"."approve_id" IS '审批id';
COMMENT ON COLUMN "public"."businessinfo"."create_mode" IS '创建方式：0手动添加，1上传文件';
COMMENT ON COLUMN "public"."businessinfo"."private_status" IS '私密状态';
COMMENT ON COLUMN "public"."businessinfo"."submitter_read" IS '是否创建人可见';

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


ALTER TABLE "public"."business2table" ADD COLUMN "relation_type" int2;
ALTER TABLE "public"."business2table" ADD COLUMN "source_id" varchar(64);
COMMENT ON COLUMN "public"."business2table"."relation_type" IS '关联类型：0通过业务对象挂载功能挂载到该业务对象的表；1通过衍生表登记模块登记关联到该业务对象上的表';
COMMENT ON COLUMN "public"."business2table"."source_id" IS '数据源id';

ALTER TABLE "public"."business2table" DROP CONSTRAINT "business2table_pkey";

ALTER TABLE "public"."source_info_derive_column_info" ADD COLUMN "sort" int4 DEFAULT 0;


drop table "public".role;

drop table "public".user2role;

drop table "public".role2category;


ALTER TABLE "public"."source_info_derive_table_info" 
  ADD COLUMN "importance" bool,
  ADD COLUMN "security" bool;

COMMENT ON COLUMN "public"."source_info_derive_table_info"."importance" IS '重要性';

COMMENT ON COLUMN "public"."source_info_derive_table_info"."security" IS '保密性';


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

-- ----------------------------
-- Primary Key structure for table group_table_relation
-- ----------------------------
ALTER TABLE "public"."group_table_relation"   ADD CONSTRAINT "group_table_relation_pkey" PRIMARY KEY ("derive_table_id","user_group_id","tenant_id");
