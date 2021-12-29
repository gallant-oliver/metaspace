// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/4/9 16:21
 */
package io.zeta.metaspace.model.share;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import schemacrawler.schema.ColumnDataType;

import javax.xml.bind.annotation.XmlEnumValue;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 注意不同数据类型在不同DB中的转换，如 bigint和long
 */
public enum DataType {
    @XmlEnumValue("STRING")
    STRING("string", "字符串"),
    @XmlEnumValue("INT")
    INT("int", "整数"),
    @XmlEnumValue("BIGINT")
    BIGINT("long", "大整数"),
    @XmlEnumValue("FLOAT")
    FLOAT("float", "单精度浮点数"),
    @XmlEnumValue("DOUBLE")
    DOUBLE("double", "双精度浮点数"),
    @XmlEnumValue("DECIMAL")
    DECIMAL("decimal", "高精度浮点数"),
    @XmlEnumValue("BOOLEAN")
    BOOLEAN("boolean", "布尔值"),
    @XmlEnumValue("TIMESTAMP")
    TIMESTAMP("timestamp", "时间戳"),
    @XmlEnumValue("DATE")
    DATE("date", "日期"),
    @XmlEnumValue("TIME")
    TIME("time", "时间"),
    @XmlEnumValue("CLOB")
    CLOB("clob", "大数据字段类型"),

    @XmlEnumValue("UNKNOWN")
    UNKNOWN("unknown", "未知");


    public final String avroType;
    public final String caption;
    private static final String[] dateFormats = new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyyMMdd"};

    DataType(String avroType, String caption) {
        this.avroType = avroType;
        this.caption = caption;
    }

    public static DataType[] values(Boolean withBool) {
        Set<DataType> typeSet = Sets.newHashSet();
        for (DataType DataType : DataType.values()) {
            if (DataType.equals(BOOLEAN)) {
                if (withBool) {
                    typeSet.add(BOOLEAN);
                }
            } else {
                typeSet.add(DataType);
            }

        }
        return typeSet.toArray(new DataType[0]);
    }


    public static DataType valueOf(ColumnDataType columnDataType) {
        return valueOf(columnDataType.getJavaSqlType().getVendorTypeNumber());
    }

    public static DataType convertType(String type) {
        final DataType typeGroup;
        if(StringUtils.isNotBlank(type) && type.contains("VARCHAR")){
            type = "VARCHAR";
        }
        switch (type) {
            case "STRING":
            case "CHAR":
            case "NCHAR":
            case "NVARCHAR":
            case "VARCHAR":
            case "VARCHAR2":
            case "NVARCHAR2":
            case "LONGNVARCHAR":
            case "LONGVARCHAR":
            case "ROWID":
            case "NROWID":
                typeGroup = DataType.STRING;
                break;
            case "CLOB":
                typeGroup = DataType.CLOB;
                break;
            case "LONG":
            case "BIGINT":
                typeGroup = DataType.BIGINT;
                break;
            case "NUMBER":
            case "DOUBLE":
            case "FLOAT":
            case "NUMERIC":
            case "REAL":
                typeGroup = DataType.DOUBLE;
                break;

            case "INT":
            case "INTEGER":
            case "SMALLINT":
            case "TINYINT":
            case "INT UNSIGNED":
                typeGroup = DataType.INT;
                break;
            case "DECIMAL":
                typeGroup = DataType.DECIMAL;
                break;
            case "DATE":
                typeGroup = DataType.DATE;
                break;
            case "TIME":
                typeGroup = DataType.TIME;
                break;
            case "TIMESTAMP":
            case "TIMESTAMP_WITH_TIMEZONE":
            case "TIME_WITH_TIMEZONE":
                typeGroup = DataType.TIMESTAMP;
                break;
            default:
                typeGroup = DataType.UNKNOWN;
                break;
        }
        return typeGroup;
    }

    public static DataType valueOf(int javaSqlType) {
        final DataType typeGroup;
        switch (javaSqlType) {
            case java.sql.Types.BIT:
            case java.sql.Types.BOOLEAN:
                typeGroup = DataType.BOOLEAN;
                break;
            case java.sql.Types.CHAR:
            case java.sql.Types.LONGNVARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.VARCHAR:
                typeGroup = DataType.STRING;
                break;

            case java.sql.Types.BIGINT:
                typeGroup = DataType.BIGINT;
                break;
            case java.sql.Types.INTEGER:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.TINYINT:
                typeGroup = DataType.INT;
                break;
            case java.sql.Types.DECIMAL:
                typeGroup = DataType.DECIMAL;
                break;
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.NUMERIC:
            case java.sql.Types.REAL:
                typeGroup = DataType.DOUBLE;
                break;
            case java.sql.Types.DATE:
                typeGroup = DataType.DATE;
                break;
            case java.sql.Types.TIME:
                typeGroup = DataType.TIME;
                break;
            case java.sql.Types.TIMESTAMP:
            case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
            case java.sql.Types.TIME_WITH_TIMEZONE:
                typeGroup = DataType.TIMESTAMP;
                break;
            default:
                typeGroup = DataType.UNKNOWN;
                break;
        }
        return typeGroup;
    }

    public String getAvroType() {
        return avroType;
    }

    public String getCaption() {
        return caption;
    }

    public static DataType parseOf(String expr) {
        for (DataType DataType : DataType.values()) {
            if (DataType.name().equalsIgnoreCase(expr)) {
                return DataType;
            }
        }
        for (DataType DataType : DataType.values()) {
            if (DataType.avroType.equalsIgnoreCase(expr)) {
                return DataType;
            }
        }
        return UNKNOWN;
    }

    public Optional<Object> valueOf(Object object, DateTimeFormatter... timeFormatters) {
        if (object == null) {
            return Optional.empty();
        }
        String objStr = object.toString().trim();
        switch (this) {
            case STRING:
                return Optional.of(objStr);
            case INT:
                return Optional.of(objectToNumber(objStr).intValue());
            case BIGINT:
                return Optional.of(objectToNumber(objStr).longValue());
            case FLOAT:
                return Optional.of(objectToNumber(objStr).floatValue());
            case DOUBLE:
            case DECIMAL:
                return Optional.of(objectToNumber(objStr).doubleValue());
            case BOOLEAN:
                boolean result;
                try {
                    result = Boolean.parseBoolean(objStr);
                } catch (Exception e) {
                    result = objectToNumber(objStr).intValue() == 1;
                }
                return Optional.of(result);
            case TIMESTAMP:
            case DATE:
            case TIME:
                for (int i = 0; i < dateFormats.length; i++) {
                    //TODO 需要修改
                    String format = dateFormats[i];
                    try {
                        DateTimeFormat.forPattern(format).parseDateTime(objStr);
                        return Optional.of(objStr);
                    } catch (Exception e) {
                        throw new UnsupportedOperationException(String.format("can not parseDateTime to format [%s]", format));
                    }
                }
            default:
                throw new UnsupportedOperationException("Unsupported DataType[" + this + "].");
        }
    }

    public static Iterable transform(DataType dataType, Iterable<Object> iterables) {
        if (dataType == null) {
            return iterables;
        }
        return dataType.transform(iterables);
    }

    public Iterable transform(Iterable<Object> iterables) {
        if (iterables == null || Iterables.isEmpty(iterables)) {
            //
        }
        switch (this) {
            case BIGINT:
            case BOOLEAN:
            case DECIMAL:
            case DOUBLE:
            case FLOAT:
            case STRING:
            case INT:
                return StreamSupport.stream(iterables.spliterator(), false).map(o -> valueOf(o).get()).collect(Collectors.toList());
            case TIMESTAMP:
            case DATE:
            case TIME:
                return StreamSupport.stream(iterables.spliterator(), false).map(o -> valueOf(o).get()).collect(Collectors.toList());
            default:
                return StreamSupport.stream(iterables.spliterator(), false).map(o -> o == null ? "" : o.toString()).collect(Collectors.toList());
        }
    }

    private Number objectToNumber(Object object) {
        if (object == null) {
            return 0;
        }
        try {
            return NumberFormat.getInstance().parse(object.toString());
        } catch (ParseException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    public boolean supportPartition() {
        switch (this) {
            case STRING:
            case INT:
            case BIGINT:
                return true;
        }
        return false;
    }
}
