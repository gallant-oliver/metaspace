ALTER TABLE "public"."tableinfo"
  ADD COLUMN "description" varchar;
  COMMENT ON COLUMN "public"."tableinfo"."description" IS '描述';

ALTER TABLE "public"."category"
  ADD COLUMN "createtime" timestamptz(6);
  COMMENT ON COLUMN "public"."category"."createtime" IS '创建时间';