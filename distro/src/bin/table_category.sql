DROP TABLE IF EXISTS "public"."table_category";
CREATE TABLE "public"."table_category" (
  "guid" text COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "name" text COLLATE "pg_catalog"."default",
  "upbrothercategoryguid" text COLLATE "pg_catalog"."default",
  "downbrothercategoryguid" text COLLATE "pg_catalog"."default",
  "parentcategoryguid" text COLLATE "pg_catalog"."default",
  "qualifiedname" text COLLATE "pg_catalog"."default"
)
;

ALTER TABLE "public"."table_category" ADD CONSTRAINT "table_catalog_pkey" PRIMARY KEY ("guid");
DROP TABLE IF EXISTS "public"."table_relation";
CREATE TABLE "public"."table_relation" (
  "relationshipguid" text COLLATE "pg_catalog"."default" NOT NULL,
  "categoryguid" text COLLATE "pg_catalog"."default",
  "tablename" text COLLATE "pg_catalog"."default",
  "dbname" text COLLATE "pg_catalog"."default",
  "tableguid" text COLLATE "pg_catalog"."default",
  "path" text COLLATE "pg_catalog"."default",
  "status" text COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."table_relation" ADD CONSTRAINT "table_relation_pkey" PRIMARY KEY ("relationshipguid");
