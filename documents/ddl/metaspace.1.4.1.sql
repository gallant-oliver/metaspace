ALTER TABLE "public"."tableinfo"
  ADD COLUMN "subordinatesystem" varchar COLLATE "pg_catalog"."default",
  ADD COLUMN "subordinatedatabase" varchar COLLATE "pg_catalog"."default",
  ADD COLUMN "systemadmin" varchar COLLATE "pg_catalog"."default",
  ADD COLUMN "datawarehouseadmin" varchar COLLATE "pg_catalog"."default",
  ADD COLUMN "datawarehousedescription" varchar COLLATE "pg_catalog"."default",
  ADD COLUMN "catalogadmin" varchar COLLATE "pg_catalog"."default";

COMMENT ON COLUMN "public"."tableinfo"."subordinatesystem" IS '源系统';
COMMENT ON COLUMN "public"."tableinfo"."subordinatedatabase" IS '源数据库';
COMMENT ON COLUMN "public"."tableinfo"."systemadmin" IS '源系统管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehouseadmin" IS '数仓管理员';
COMMENT ON COLUMN "public"."tableinfo"."datawarehousedescription" IS '数仓描述';
COMMENT ON COLUMN "public"."tableinfo"."catalogadmin" IS '目录管理员';

ALTER TABLE "public"."role"
  ADD COLUMN "valid" BOOLEAN,
  ADD COLUMN "creator" varchar COLLATE "pg_catalog"."default",
  ADD COLUMN "updater" varchar COLLATE "pg_catalog"."default";

COMMENT ON COLUMN "public"."role"."valid" IS '角色是否有效';
COMMENT ON COLUMN "public"."role"."creator" IS '创建者';
COMMENT ON COLUMN "public"."role"."updater" IS '更新者';

UPDATE "public"."role" set "valid"=true;

/*UPDATE "public"."role" set "creator"='' where "role"."roleid" in ('1','2','3','4','5','6','7');*/

/*此处需要根据现场环境管理员ID设置creator和updater*/
UPDATE "public"."role" set "creator"='',"updater"='';

UPDATE "public"."role" set "createtime"= to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss'), "updatetime"= to_char(current_timestamp, 'yyyy-mm-dd hh24:mi:ss') where "roleid" in ('1','2', '3', '4', '5', '6', '7')

