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

package org.apache.atlas.web.common.filetable;

import com.google.common.base.Ascii;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.UUID;

/**
 * 数据类型
 */
@XmlType
@XmlEnum(String.class)
public enum DataType {
    @XmlEnumValue("STRING")STRING("string", "CHAR", "VARCHAR"),
    @XmlEnumValue("INT")INT("int", "TINYINT", "SMALLINT", "INTEGER"),
    @XmlEnumValue("BIGINT")BIGINT("long"),
    @XmlEnumValue("FLOAT")FLOAT("float"),
    @XmlEnumValue("DOUBLE")DOUBLE("double", "REAL"),
    @XmlEnumValue("DECIMAL")DECIMAL("decimal"),
    @XmlEnumValue("BOOLEAN")BOOLEAN("boolean"),
    @XmlEnumValue("TIMESTAMP")TIMESTAMP("string"),
    @XmlEnumValue("ARRAY")ARRAY("array"),
    @XmlEnumValue("MAP")MAP("map"),
    @XmlEnumValue("STRUCT")STRUCT("struct"),
    @XmlEnumValue("UNKNOWN")UNKNOWN("null");

    public final String avroType;
    public final String[] aliases;

    DataType(String avroType, String... aliases) {
        this.avroType = avroType;
        this.aliases = aliases;
    }

    public static DataType convertColumnType(int jdbcType) {
        final DataType type;
        switch (jdbcType) {
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                type = INT;
                break;
            case Types.BIGINT:
                type = BIGINT;
                break;
            case Types.FLOAT:
                type = FLOAT;
                break;
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
                type = DOUBLE;
                break;
            case Types.DECIMAL:
                type = DECIMAL;
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                type = STRING;
                break;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                type = TIMESTAMP;
                break;
            case Types.BOOLEAN:
                type = BOOLEAN;
                break;
            default:
                type = UNKNOWN;
        }
        return type;
    }

    public static DataType convertDataType(Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }

        final DataType type;
        if (cls == String.class) {
            type = DataType.STRING;
        } else if (cls == Boolean.class || cls == boolean.class) {
            type = DataType.BOOLEAN;
        } else if (cls == Character.class || cls == char.class || cls == Character[].class || cls == char[].class) {
            type = DataType.STRING;
        } else if (cls == Byte.class || cls == byte.class) {
            type = DataType.INT;
        } else if (cls == Short.class || cls == short.class) {
            type = DataType.INT;
        } else if (cls == Integer.class || cls == int.class) {
            type = DataType.INT;
        } else if (cls == Long.class || cls == long.class || cls == BigInteger.class) {
            type = DataType.BIGINT;
        } else if (cls == Float.class || cls == float.class) {
            type = DataType.FLOAT;
        } else if (cls == Double.class || cls == double.class) {
            type = DataType.DOUBLE;
        } else if (cls == BigDecimal.class) {
            type = DataType.DECIMAL;
        } else if (cls == java.sql.Date.class) {
            type = DataType.TIMESTAMP;
        } else if (cls == Timestamp.class) {
            type = DataType.TIMESTAMP;
        } else if (cls == Time.class) {
            type = DataType.TIMESTAMP;
        } else if (Date.class.isAssignableFrom(cls)) {
            type = DataType.TIMESTAMP;
        } else if (cls == UUID.class) {
            type = STRING;
        } else {
            type = DataType.UNKNOWN;
        }
        return type;
    }

    public static DataType parseOf(String expr) {
        for (DataType dataType : DataType.values()) {
            if (Ascii.equalsIgnoreCase(dataType.name(), expr)) {
                return dataType;
            }
        }
        for (DataType dataType : DataType.values()) {
            if (Ascii.equalsIgnoreCase(dataType.avroType, expr)) {
                return dataType;
            }
        }
        for (DataType dataType : DataType.values()) {
            if (dataType.aliases != null && dataType.aliases.length > 0) {
                for (String aliase : dataType.aliases) {
                    if (Ascii.equalsIgnoreCase(aliase, expr)) {
                        return dataType;
                    }
                }
            }
        }
        return UNKNOWN;
    }

    public static Object value(DataType dataType, Object object) {
        if (dataType == null) {
            return object;
        }
        return dataType.value(object);
    }

    public Object value(Object object) {
        if (object == null) {
            return null;
        }
        String strObj = object.toString().trim();
        switch (this) {
            case BIGINT:
                return Double.valueOf(strObj).longValue();
            case BOOLEAN:
                return Boolean.parseBoolean(strObj);
            case DOUBLE:
            case DECIMAL:
                return Double.parseDouble(strObj);
            case FLOAT:
                return Double.valueOf(strObj).floatValue();
            case INT:
                return Double.valueOf(strObj).intValue();
            case TIMESTAMP:
            case STRING:
                return strObj;
            default:
                return object;
        }
    }

    public static Iterable transform(DataType dataType, Iterable<Object> iterables) {
        if (dataType == null) {
            return iterables;
        }
        return dataType.transform(iterables);
    }

    public Iterable transform(Iterable<Object> iterables) {
        if (iterables == null) {
            return null;
        }
        if (Iterables.isEmpty(iterables)) {
            throw new IllegalArgumentException("At least one params");
        }
        switch (this) {
            case BIGINT:
                return Iterables.transform(iterables, new Function<Object, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable Object o) {
                        return o == null ? 0l : Long.parseLong(o.toString());
                    }
                });
            case BOOLEAN:
                return Iterables.transform(iterables, new Function<Object, Boolean>() {
                    @Nullable
                    @Override
                    public Boolean apply(@Nullable Object o) {
                        return o != null && Boolean.parseBoolean(o.toString());
                    }
                });
            case DECIMAL:
            case DOUBLE:
                return Iterables.transform(iterables, new Function<Object, Double>() {
                    @Nullable
                    @Override
                    public Double apply(@Nullable Object o) {
                        return o == null ? 0l : Double.parseDouble(o.toString());
                    }
                });
            case FLOAT:
                return Iterables.transform(iterables, new Function<Object, Float>() {
                    @Nullable
                    @Override
                    public Float apply(@Nullable Object o) {
                        return o == null ? 0l : Float.parseFloat(o.toString());
                    }
                });
            case INT:
                return Iterables.transform(iterables, new Function<Object, Integer>() {
                    @Nullable
                    @Override
                    public Integer apply(@Nullable Object o) {
                        return o == null ? 0 : Integer.parseInt(o.toString());
                    }
                });
            default:
                return Iterables.transform(iterables, new Function<Object, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Object o) {
                        return o == null ? "" : o.toString();
                    }
                });
        }
    }
}
