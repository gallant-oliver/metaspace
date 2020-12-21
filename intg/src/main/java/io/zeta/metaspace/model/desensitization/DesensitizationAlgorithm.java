package io.zeta.metaspace.model.desensitization;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.math.DoubleMath;
import lombok.Data;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;


public enum DesensitizationAlgorithm {

    MD5("MD5 哈希", "哈希", new String[]{"盐值"}, (field, params) -> Hashing.md5().newHasher().putString(field.toString() + params.get(0).toString(), Charsets.UTF_8).hash().toString()),
    SHA1("SHA1 哈希", "哈希", new String[]{"盐值"}, (field, params) -> Hashing.sha1().newHasher().putString(field.toString() + params.get(0).toString(), Charsets.UTF_8).hash().toString()),
    SHA256("SHA256 哈希", "哈希", new String[]{"盐值"}, (field, params) -> Hashing.sha256().newHasher().putString(field.toString() + params.get(0).toString(), Charsets.UTF_8).hash().toString()),
    HMAC("HMAC SHA256 哈希", "哈希", new String[]{"密钥", "盐值"}, (field, params) -> Hashing.hmacSha256(params.get(0).getBytes()).newHasher().putString(field.toString() + params.get(1).toString(), Charsets.UTF_8).hash().toString()),

    NumericalTypeTruncation("数值类型截断", "截断", new String[]{"保留小数点前几位"}, (field, params) -> {
        Double filedValue = Double.valueOf(field.toString());
        long n = Long.parseLong(params.get(0));
        if (n > 0) {
            long multiple = new Double(Math.pow(10, n)).longValue();
            return filedValue.longValue() / multiple * multiple;
        } else if (n < 0) {
            String format = "%." + Math.abs(n) + "f";
            return Double.valueOf(String.format(format, filedValue));
        }
        return field;
    }),

    KeepOut("保留前n后m", "掩码", new String[]{"n", "m"},
            (field, params) -> {
                if (field == null || StringUtils.isEmpty(field.toString())) {
                    return null;
                }
                StringBuilder result = new StringBuilder(field.toString());
                int n = Integer.parseInt(params.get(0));
                int m = Integer.parseInt(params.get(1));
                if (n + m >= result.length()) {
                    return field;
                }
                return replace(result, n, result.length() - m).toString();
            },
            (params) -> params.stream().map(Integer::parseInt).allMatch(i -> i >= 1)
    ),

    MaskOut("掩码前n后m", "掩码", new String[]{"n", "m"},
            (field, params) -> {
                if (field == null || StringUtils.isEmpty(field.toString())) {
                    return null;
                }
                StringBuilder result = new StringBuilder(field.toString());
                int n = Integer.parseInt(params.get(0));
                int m = Integer.parseInt(params.get(1));
                if (n + m >= result.length()) {
                    return StringUtils.repeat("x", result.length());
                }
                result = replace(result, 0, n);
                result = replace(result, result.length() - m, result.length());
                return result.toString();
            },
            (params) -> params.stream().map(Integer::parseInt).allMatch(i -> i >= 1)
    ),

    KeepTo("保留从n至m", "掩码", new String[]{"n", "m"},
            (field, params) -> {
                if (field == null || StringUtils.isEmpty(field.toString())) {
                    return null;
                }
                StringBuilder result = new StringBuilder(field.toString());
                int n = Integer.parseInt(params.get(0));
                int m = Integer.parseInt(params.get(1));
                if (n > m || n > result.length()) {
                    return StringUtils.repeat("x", result.length());
                }
                result = replace(result, 0, n - 1);
                result = replace(result, m, result.length());
                return result.toString();
            },
            (params) -> params.stream().map(Integer::parseInt).allMatch(i -> i >= 1)
    ),

    MaskTo("掩码从n至m", "掩码", new String[]{"n", "m"},
            (field, params) -> {
                if (field == null || StringUtils.isEmpty(field.toString())) {
                    return null;
                }
                StringBuilder result = new StringBuilder(field.toString());
                int n = Integer.parseInt(params.get(0));
                int m = Integer.parseInt(params.get(1));
                int count = field.toString().length();
                if (n > m || n > result.length()) {
                    return field;
                }
                result = replace(result, n - 1, Math.min(m, count));
                return result.toString();
            },
            (params) -> params.stream().map(Integer::parseInt).allMatch(i -> i >= 1)
    ),

    MaskTime("日期类型掩码", "掩码", new String[]{"原始日期格式", "掩盖日期格式"},
            (field, params) -> {
                if (field == null || StringUtils.isEmpty(field.toString())) {
                    return null;
                }
                try {
                    SimpleDateFormat originalFormat = new SimpleDateFormat(params.get(0));
                    SimpleDateFormat maskFormat = new SimpleDateFormat(params.get(1));
                    return maskFormat.format(originalFormat.parse(field.toString()));
                } catch (ParseException e) {
                    return StringUtils.repeat("x", field.toString().length());
                }
            },
            (params) -> params.stream().noneMatch(Objects::isNull)
    ),

    MaskPhone("手机号掩码", "掩码", new String[0],
            (field, params) -> field != null && StringUtils.isNotEmpty(field.toString()) ? replace(new StringBuilder(field.toString()), 3, 7) : field
    ),

    MaskIdCard("身份证号掩码", "掩码", new String[0],
            (field, params) -> field != null && StringUtils.isNotEmpty(field.toString()) ? replace(new StringBuilder(field.toString()), 6, field.toString().length()) : field
    ),

    MaskIp("IP地址掩码", "掩码", new String[0],
            (field, params) -> {
                if (field != null && StringUtils.isNotEmpty(field.toString())) {
                    String[] ipItem = field.toString().split("\\.");
                    if (ipItem.length == 4) {
                        ipItem[2] = StringUtils.repeat("x", 3);
                        ipItem[3] = StringUtils.repeat("x", 3);
                        return String.join(".", ipItem);
                    }
                    return StringUtils.repeat("x", field.toString().length());
                }
                return field;
            }
    ),

    MaskMac("MAC地址掩码", "掩码", new String[0],
            (field, params) -> {
                if (field != null && StringUtils.isNotEmpty(field.toString())) {
                    String regex = null;
                    if (field.toString().contains("-")) {
                        regex = "-";
                    } else if (field.toString().contains(":")) {
                        regex = ":";
                    }
                    if (regex != null) {
                        String[] macItem = field.toString().split(regex);
                        if (macItem.length == 6) {
                            for (int i = 3; i < macItem.length; i++) {
                                macItem[i] = StringUtils.repeat("x", macItem[2].length());
                            }
                            return String.join(regex, macItem);
                        }
                    }
                    return StringUtils.repeat("x", field.toString().length());
                }
                return field;
            }
    );

    DesensitizationAlgorithm(String name, String type, String[] paramNames, BiFunction<Object, List<String>, Object> handle) {
        this.name = name;
        this.type = type;
        this.paramNames = paramNames;
        this.handle = handle;
        this.verifyParam = null;
    }

    DesensitizationAlgorithm(String name, String type, String[] paramNames, BiFunction<Object, List<String>, Object> handle, Function<List<String>, Boolean> verifyParam) {
        this.name = name;
        this.type = type;
        this.paramNames = paramNames;
        this.handle = handle;
        this.verifyParam = verifyParam;
    }

    private String name;
    private String type;
    private String[] paramNames;

    @JsonIgnore
    private BiFunction<Object, List<String>, Object> handle;
    @JsonIgnore
    private Function<List<String>, Boolean> verifyParam;

    public String getName() {
        return name;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public BiFunction<Object, List<String>, Object> getHandle() {
        return handle;
    }

    public String getType() {
        return type;
    }

    public Function<List<String>, Boolean> getVerifyParam() {
        return verifyParam;
    }

    public static StringBuilder replace(StringBuilder result, int start, int end) {
        return replace(result, start, end, "x");
    }

    public static StringBuilder replace(StringBuilder result, int start, int end, String str) {
        if (start > end) {
            return result;
        }
        return result.replace(start, end, StringUtils.repeat(str, end - start));

    }
}
