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

package org.apache.atlas;

import com.ctrip.framework.apollo.Config;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * @author lixiang03
 * @Data 2020/7/8 18:19
 */
public class ParseKeyword {
    private final static String placeholderPrefix="${";

    private final static String placeholderSuffix="}";

    private final static String simplePrefix="{";

    private final static String valueSeparator=":";

    private final static boolean ignoreUnresolvablePlaceholders=true;

    public static String parseStringValue(
            String value, Config config, Set<String> visitedPlaceholders) {

        StringBuilder result = new StringBuilder(value);

        int startIndex = value.indexOf(placeholderPrefix);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex != -1) {
                String placeholder = result.substring(startIndex + placeholderPrefix.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(originalPlaceholder)) {
                    throw new IllegalArgumentException(
                            "Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
                }
                // Recursive invocation, parsing placeholders contained in the placeholder key.
                placeholder = parseStringValue(placeholder, config,visitedPlaceholders);
                // Now obtain the value for the fully resolved key...
                String propVal = config.getProperty(placeholder,null);
                if (propVal == null && valueSeparator != null) {
                    int separatorIndex = placeholder.indexOf(valueSeparator);
                    if (separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0, separatorIndex);
                        String defaultValue = placeholder.substring(separatorIndex + valueSeparator.length());
                        propVal = config.getProperty(actualPlaceholder,null);
                        if (propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }
                if (propVal != null) {
                    // Recursive invocation, parsing placeholders contained in the
                    // previously resolved placeholder value.
                    propVal = parseStringValue(propVal, config,visitedPlaceholders);
                    result.replace(startIndex, endIndex + placeholderSuffix.length(), propVal);
                    startIndex = result.indexOf(placeholderPrefix, startIndex + propVal.length());
                }
                else if (ignoreUnresolvablePlaceholders) {
                    // Proceed with unprocessed value.
                    startIndex = result.indexOf(placeholderPrefix, endIndex + placeholderSuffix.length());
                }
                visitedPlaceholders.remove(originalPlaceholder);
            }
            else {
                startIndex = -1;
            }
        }

        return result.toString();
    }

    private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (StringUtils.substringMatch(buf, index, placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + placeholderSuffix.length();
                }
                else {
                    return index;
                }
            }
            else if (StringUtils.substringMatch(buf, index, simplePrefix)) {
                withinNestedPlaceholder++;
                index = index + simplePrefix.length();
            }
            else {
                index++;
            }
        }
        return -1;
    }
}
