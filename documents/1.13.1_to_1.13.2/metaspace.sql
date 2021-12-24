-- 数据质量-数据标准 sql
-- 升级数据
UPDATE data_standard
SET delete= TRUE
WHERE delete = FALSE;

-- 删除"数据标准内容"列
ALTER TABLE data_standard
DROP COLUMN content;

-- 修改字段
ALTER TABLE data_standard
    ALTER COLUMN version SET NOT NULL;
ALTER TABLE data_standard
    ALTER COLUMN version SET DEFAULT 0;

-- 新增字段
ALTER TABLE data_standard
    ADD name VARCHAR DEFAULT '' NOT NULL;
ALTER TABLE data_standard
    ADD standard_type INT DEFAULT 2 NOT NULL;
ALTER TABLE data_standard
    ADD data_type VARCHAR(16);
ALTER TABLE data_standard
    ADD data_length INT;
ALTER TABLE data_standard
    ADD allowable_value_flag BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE data_standard
    ADD allowable_value VARCHAR;
ALTER TABLE data_standard
    ADD standard_level INT;

COMMENT ON TABLE data_standard IS '数据标准';
COMMENT ON COLUMN data_standard.name IS '标准名称';
COMMENT ON COLUMN data_standard.standard_type IS '数据标准类型: 1 - 数据标准 2 - 命名标准';
COMMENT ON COLUMN data_standard.data_length IS '数据长度,非0正整数';
COMMENT ON COLUMN data_standard.allowable_value_flag IS '是否有允许值';
COMMENT ON COLUMN data_standard.allowable_value IS '允许值,用'';''分隔';
COMMENT ON COLUMN data_standard.standard_level IS '标准层级: 1-贴源层、2-基础层、3-通用层、4-应用层';
COMMENT ON COLUMN data_standard.data_type IS '枚举值:字符型(STRING)、双精度(DOUBLE)、长整型(BIGINT)、布尔类型(BOOLEAN)、高精度(DECIMAL)、日期类型(DATE)、时间戳类型(TIMESTAMP)';
COMMENT ON COLUMN data_standard.version IS '版本号: 默认当前版本为0,历史版本号均为非0正整数';

-- 数据质量-规则管理 sql
ALTER TABLE data_quality_rule_template
    ADD data_standard_code VARCHAR;

COMMENT ON COLUMN data_quality_rule_template.data_standard_code IS '参照数据标准编码: data_standard.number';