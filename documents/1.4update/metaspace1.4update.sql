-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS "public"."organization";
CREATE TABLE "public"."organization" (
  "checked" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "disable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "id" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "isopen" bool DEFAULT NULL,
  "isvm" int8 DEFAULT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "open" bool DEFAULT NULL,
  "pid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "pkid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "ptype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table organization
-- ----------------------------
ALTER TABLE "public"."organization" ADD CONSTRAINT "organization_pkey" PRIMARY KEY ("pkid");

-- ----------------------------
-- Table structure for table2owner
-- ----------------------------
CREATE TABLE "public"."table2owner" (
  "tableguid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "ownerid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "keeper" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table table2owner
-- ----------------------------
ALTER TABLE "public"."table2owner" ADD CONSTRAINT "table2owner_pkey" PRIMARY KEY ("tableguid", "ownerid");

-- ----------------------------
-- Table structure for report_error
-- ----------------------------
CREATE TABLE "public"."report_error" (
  "errorid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "templateid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "reportid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "ruleid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "content" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "retrycount" int2 DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table report_error
-- ----------------------------
ALTER TABLE "public"."report_error" ADD CONSTRAINT "report_error_pkey" PRIMARY KEY ("errorid");

-- ----------------------------
-- Table structure for apigroup
-- ----------------------------
CREATE TABLE "public"."apigroup" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "parentguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updater" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table apigroup
-- ----------------------------
ALTER TABLE "public"."apigroup" ADD CONSTRAINT "apigroup_pkey" PRIMARY KEY ("guid");


-- ----------------------------
-- Table structure for apiinfo
-- ----------------------------
CREATE TABLE "public"."apiinfo" (
  "guid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "tableguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "dbguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "keeper" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "maxrownumber" float8 DEFAULT NULL,
  "fields" json DEFAULT NULL,
  "version" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "protocol" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "requestmode" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "returntype" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "path" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
  "generatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updater" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "updatetime" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "groupguid" varchar COLLATE "pg_catalog"."default" DEFAULT NULL,
  "star" bool DEFAULT NULL,
  "publish" bool DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table apiinfo
-- ----------------------------
ALTER TABLE "public"."apiinfo" ADD CONSTRAINT "apiinfo_pkey" PRIMARY KEY ("guid");

-- ----------------------------
-- Table structure for user2apistar
-- ----------------------------
CREATE TABLE "public"."user2apistar" (
  "apiguid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
  "userid" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL
)
;

-- ----------------------------
-- Primary Key structure for table user2apistar
-- ----------------------------
ALTER TABLE "public"."user2apistar" ADD CONSTRAINT "user2apistar_pkey" PRIMARY KEY ("apiguid", "userid");

-- ----------------------------
-- Drop table table_category
-- ----------------------------
DROP TABLE table_category;

-- ----------------------------
-- ALERT table 
-- ----------------------------
alter table business_relation add column generatetime varchar;
alter table table_relation add column generatetime varchar;
alter table template add column generatetime varchar;
alter table businessinfo add column trusttable varchar;
alter table tableinfo add column databaseguid varchar;
alter table tableinfo add column databasestatus varchar;


-- ----------------------------
-- Data setup
-- ----------------------------
INSERT INTO "public"."apigroup" VALUES ('1', '全部分组', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."apigroup" VALUES ('0', '未分组', '1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."module" VALUES (8, '技术目录', 0);
INSERT INTO "public"."module" VALUES (9, '业务目录', 0);
INSERT INTO "public"."module" VALUES (10, 'API信息', 1);
INSERT INTO "public"."role" VALUES ('6', '业务目录管理员', '业务目录管理员', '6', NULL, 1, NULL, 1, 0, 1);
INSERT INTO "public"."role" VALUES ('7', '技术目录管理员', '技术目录管理员', '7', NULL, 1, NULL, 1, 0, 1);
INSERT INTO "public"."privilege" VALUES ('6', '业务目录管理员', '业务目录管理员', NULL, 1, 0);
INSERT INTO "public"."privilege" VALUES ('7', '技术目录管理员', '技术目录管理员', NULL, 1, 0);
INSERT INTO "public"."privilege2module" VALUES ('1', 8);
INSERT INTO "public"."privilege2module" VALUES ('1', 9);
INSERT INTO "public"."privilege2module" VALUES ('1', 10);
INSERT INTO "public"."privilege2module" VALUES ('2', 2);
INSERT INTO "public"."privilege2module" VALUES ('6', 2);
INSERT INTO "public"."privilege2module" VALUES ('6', 9);
INSERT INTO "public"."privilege2module" VALUES ('7', 1);
INSERT INTO "public"."privilege2module" VALUES ('7', 2);
INSERT INTO "public"."privilege2module" VALUES ('7', 8);
INSERT INTO "public"."privilege2module" VALUES ('7', 7);
UPDATE module set modulename='业务信息' where moduleid='4';
UPDATE role set edit=1 where roleid='2';
-- ----------------------------
-- Data quality
-- ----------------------------
INSERT INTO "public"."systemrule" VALUES (13, '字段平均值变化', '相比上一周期，字段平均值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (14, '字段汇总值变化', '相比上一周期，字段汇总值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (15, '字段最小值变化', '相比上一周期，字段最小值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (16, '
字段最大值变化', '相比上一周期，字段最大值变化', 1, '');
INSERT INTO "public"."systemrule" VALUES (29, '字段重复值个数/总行数', '计算字段重复值行数所占的比例', 1, '%');
INSERT INTO "public"."systemrule" VALUES (28, '字段空值个数/总行数', '计算字段空值行数所占的比例', 1, '%');
INSERT INTO "public"."systemrule" VALUES (27, '字段唯一值个数/总行数', '计算字段唯一值行数所占的比例', 1, '%');
INSERT INTO "public"."systemrule" VALUES (3, '表大小变化', '相比上一周期，表大小变化', 0, '字节');
INSERT INTO "public"."systemrule" VALUES (0, '表行数变化率', '相比上一周期，表行数变化率', 0, '%');
INSERT INTO "public"."systemrule" VALUES (2, '表行数变化', '相比上一周期，表行数变化', 0, '行');
INSERT INTO "public"."systemrule" VALUES (1, '表大小变化率', '相比上一周期，表大小变化率', 0, '%');
INSERT INTO "public"."systemrule" VALUES (6, '字段平均值变化率', '相比上一周期，字段平均值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (4, '当前表行数', '表行数是否符合预期', 0, '行');
INSERT INTO "public"."systemrule" VALUES (5, '当前表大小', '表大小是否符合预期', 0, '字节');
INSERT INTO "public"."systemrule" VALUES (20, '字段平均值', '计算字段平均值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (21, '字段汇总值', '计算字段汇总值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (22, '字段最小值', '计算字段最小值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (23, '字段最大值
', '计算字段最大值', 1, NULL);
INSERT INTO "public"."systemrule" VALUES (7, '字段汇总值变化率', '相比上一周期，字段汇总值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (8, '字段最小值变化率', '相比上一周期，字段最小值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (9, '字段最大值变化率', '相比上一周期，字段最大值变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (10, '字段唯一值个数变化率', '相比上一周期，字段唯一值个数变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (11, '字段空值个数变化率', '相比上一周期，字段空值个数变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (12, '字段重复值个数变化率', '相比上一周期，字段重复值个数变化率', 1, '%');
INSERT INTO "public"."systemrule" VALUES (24, '字段唯一值个数', '计算字段唯一值个数', 1, '个');
INSERT INTO "public"."systemrule" VALUES (25, '字段空值个数', '计算字段空值个数', 1, '个');
INSERT INTO "public"."systemrule" VALUES (26, '字段重复值个数', '计算字段重复值个数', 1, '个');
INSERT INTO "public"."systemrule" VALUES (17, '字段唯一值个数变化', '相比上一周期，字段唯一值个数变化', 1, '个');
INSERT INTO "public"."systemrule" VALUES (18, '字段空值个数变化', '相比上一周期，字段空值个数变化', 1, '个');
INSERT INTO "public"."systemrule" VALUES (19, '字段重复值个数变化', '相比上一周期，字段重复值个数变化
', 1, '个');
INSERT INTO "public"."rule2buildtype" VALUES (0, 0);
INSERT INTO "public"."rule2buildtype" VALUES (1, 0);
INSERT INTO "public"."rule2buildtype" VALUES (2, 0);
INSERT INTO "public"."rule2buildtype" VALUES (3, 0);
INSERT INTO "public"."rule2buildtype" VALUES (4, 0);
INSERT INTO "public"."rule2buildtype" VALUES (5, 0);
INSERT INTO "public"."rule2buildtype" VALUES (6, 0);
INSERT INTO "public"."rule2buildtype" VALUES (7, 0);
INSERT INTO "public"."rule2buildtype" VALUES (8, 0);
INSERT INTO "public"."rule2buildtype" VALUES (9, 0);
INSERT INTO "public"."rule2buildtype" VALUES (10, 0);
INSERT INTO "public"."rule2buildtype" VALUES (11, 0);
INSERT INTO "public"."rule2buildtype" VALUES (12, 0);
INSERT INTO "public"."rule2buildtype" VALUES (13, 0);
INSERT INTO "public"."rule2buildtype" VALUES (14, 0);
INSERT INTO "public"."rule2buildtype" VALUES (15, 0);
INSERT INTO "public"."rule2buildtype" VALUES (16, 0);
INSERT INTO "public"."rule2buildtype" VALUES (17, 0);
INSERT INTO "public"."rule2buildtype" VALUES (18, 0);
INSERT INTO "public"."rule2buildtype" VALUES (19, 0);
INSERT INTO "public"."rule2buildtype" VALUES (20, 0);
INSERT INTO "public"."rule2buildtype" VALUES (21, 0);
INSERT INTO "public"."rule2buildtype" VALUES (22, 0);
INSERT INTO "public"."rule2buildtype" VALUES (23, 0);
INSERT INTO "public"."rule2buildtype" VALUES (24, 0);
INSERT INTO "public"."rule2buildtype" VALUES (25, 0);
INSERT INTO "public"."rule2buildtype" VALUES (26, 0);
INSERT INTO "public"."rule2buildtype" VALUES (27, 0);
INSERT INTO "public"."rule2buildtype" VALUES (28, 0);
INSERT INTO "public"."rule2buildtype" VALUES (29, 0);
INSERT INTO "public"."rule2buildtype" VALUES (4, 1);
INSERT INTO "public"."rule2buildtype" VALUES (5, 1);
INSERT INTO "public"."rule2buildtype" VALUES (20, 1);
INSERT INTO "public"."rule2buildtype" VALUES (21, 1);
INSERT INTO "public"."rule2buildtype" VALUES (22, 1);
INSERT INTO "public"."rule2buildtype" VALUES (23, 1);
INSERT INTO "public"."rule2buildtype" VALUES (24, 1);
INSERT INTO "public"."rule2buildtype" VALUES (25, 1);
INSERT INTO "public"."rule2buildtype" VALUES (26, 1);
INSERT INTO "public"."rule2buildtype" VALUES (27, 1);
INSERT INTO "public"."rule2buildtype" VALUES (28, 1);
INSERT INTO "public"."rule2buildtype" VALUES (29, 1);
INSERT INTO "public"."rule2checktype" VALUES (1, 1);
INSERT INTO "public"."rule2checktype" VALUES (0, 0);
INSERT INTO "public"."rule2checktype" VALUES (0, 1);
INSERT INTO "public"."rule2checktype" VALUES (1, 0);
INSERT INTO "public"."rule2checktype" VALUES (2, 0);
INSERT INTO "public"."rule2checktype" VALUES (3, 0);
INSERT INTO "public"."rule2checktype" VALUES (4, 0);
INSERT INTO "public"."rule2checktype" VALUES (5, 0);
INSERT INTO "public"."rule2checktype" VALUES (6, 0);
INSERT INTO "public"."rule2checktype" VALUES (7, 0);
INSERT INTO "public"."rule2checktype" VALUES (8, 0);
INSERT INTO "public"."rule2checktype" VALUES (9, 0);
INSERT INTO "public"."rule2checktype" VALUES (10, 0);
INSERT INTO "public"."rule2checktype" VALUES (11, 0);
INSERT INTO "public"."rule2checktype" VALUES (12, 0);
INSERT INTO "public"."rule2checktype" VALUES (13, 0);
INSERT INTO "public"."rule2checktype" VALUES (14, 0);
INSERT INTO "public"."rule2checktype" VALUES (15, 0);
INSERT INTO "public"."rule2checktype" VALUES (16, 0);
INSERT INTO "public"."rule2checktype" VALUES (17, 0);
INSERT INTO "public"."rule2checktype" VALUES (18, 0);
INSERT INTO "public"."rule2checktype" VALUES (19, 0);
INSERT INTO "public"."rule2checktype" VALUES (20, 0);
INSERT INTO "public"."rule2checktype" VALUES (21, 0);
INSERT INTO "public"."rule2checktype" VALUES (22, 0);
INSERT INTO "public"."rule2checktype" VALUES (23, 0);
INSERT INTO "public"."rule2checktype" VALUES (24, 0);
INSERT INTO "public"."rule2checktype" VALUES (25, 0);
INSERT INTO "public"."rule2checktype" VALUES (26, 0);
INSERT INTO "public"."rule2checktype" VALUES (27, 0);
INSERT INTO "public"."rule2checktype" VALUES (28, 0);
INSERT INTO "public"."rule2checktype" VALUES (29, 0);
INSERT INTO "public"."rule2checktype" VALUES (6, 1);
INSERT INTO "public"."rule2checktype" VALUES (7, 1);
INSERT INTO "public"."rule2checktype" VALUES (8, 1);
INSERT INTO "public"."rule2checktype" VALUES (9, 1);
INSERT INTO "public"."rule2checktype" VALUES (10, 1);
INSERT INTO "public"."rule2checktype" VALUES (11, 1);
INSERT INTO "public"."rule2checktype" VALUES (12, 1);
INSERT INTO "public"."rule2checktype" VALUES (27, 1);
INSERT INTO "public"."rule2checktype" VALUES (28, 1);
INSERT INTO "public"."rule2checktype" VALUES (29, 1);
INSERT INTO "public"."rule2datatype" VALUES (6, 1);
INSERT INTO "public"."rule2datatype" VALUES (7, 1);
INSERT INTO "public"."rule2datatype" VALUES (8, 1);
INSERT INTO "public"."rule2datatype" VALUES (9, 1);
INSERT INTO "public"."rule2datatype" VALUES (10, 1);
INSERT INTO "public"."rule2datatype" VALUES (11, 1);
INSERT INTO "public"."rule2datatype" VALUES (12, 1);
INSERT INTO "public"."rule2datatype" VALUES (13, 1);
INSERT INTO "public"."rule2datatype" VALUES (14, 1);
INSERT INTO "public"."rule2datatype" VALUES (15, 1);
INSERT INTO "public"."rule2datatype" VALUES (16, 1);
INSERT INTO "public"."rule2datatype" VALUES (17, 1);
INSERT INTO "public"."rule2datatype" VALUES (18, 1);
INSERT INTO "public"."rule2datatype" VALUES (19, 1);
INSERT INTO "public"."rule2datatype" VALUES (20, 1);
INSERT INTO "public"."rule2datatype" VALUES (21, 1);
INSERT INTO "public"."rule2datatype" VALUES (22, 1);
INSERT INTO "public"."rule2datatype" VALUES (23, 1);
INSERT INTO "public"."rule2datatype" VALUES (24, 1);
INSERT INTO "public"."rule2datatype" VALUES (25, 1);
INSERT INTO "public"."rule2datatype" VALUES (26, 1);
INSERT INTO "public"."rule2datatype" VALUES (27, 1);
INSERT INTO "public"."rule2datatype" VALUES (28, 1);
INSERT INTO "public"."rule2datatype" VALUES (29, 1);
INSERT INTO "public"."rule2datatype" VALUES (17, 2);
INSERT INTO "public"."rule2datatype" VALUES (18, 2);
INSERT INTO "public"."rule2datatype" VALUES (19, 2);
INSERT INTO "public"."rule2datatype" VALUES (24, 2);
INSERT INTO "public"."rule2datatype" VALUES (25, 2);
INSERT INTO "public"."rule2datatype" VALUES (26, 2);
INSERT INTO "public"."rule2datatype" VALUES (27, 2);
INSERT INTO "public"."rule2datatype" VALUES (28, 2);
INSERT INTO "public"."rule2datatype" VALUES (29, 2);
INSERT INTO "public"."rule2datatype" VALUES (10, 2);
INSERT INTO "public"."rule2datatype" VALUES (11, 2);
INSERT INTO "public"."rule2datatype" VALUES (12, 2);

alter table report_error OWNER TO metaspace;
alter table apigroup OWNER TO metaspace;
alter table apiinfo OWNER TO metaspace;
alter table user2apistar OWNER TO metaspace;
alter table table2owner OWNER TO metaspace;
alter table organization OWNER TO metaspace;

----------------------
---- clean old data
----------------------
delete from table_relation where tableguid in (select tableguid from tableinfo where tablename like 'values__tmp__table__');
delete from tableinfo;
delete from table_relation where relationshipguid not in (select tmp.relationshipguid from (select tableguid,table_relation.relationshipguid,level,MAX(level) OVER(PARTITION BY tableguid) from table_relation,category where categorytype=0 and table_relation.categoryguid=category.guid) tmp where level = max );
update businessInfo as upinfo set trustTable = (select tableGuid from business2table where businessId=upinfo.businessId limit 1) where upinfo.businessId in (SELECT businessid from businessinfo WHERE trusttable is null)
