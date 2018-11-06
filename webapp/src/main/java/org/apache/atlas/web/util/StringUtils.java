package org.apache.atlas.web.util;

import org.apache.atlas.web.config.FiletableConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description 对字符串的操作（提取文件类型、获取文件路径）
 **/
public class StringUtils {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(?<=filename=)[\"]?(?<filename>[^\"]*)");
    private static final Pattern FILE_TYPE_PATTERN = Pattern.compile("(?<name>\\w*).(?<type>\\w*)$");

    public static String obtainFileName(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isEmpty()) {
            return null;
        }

        Matcher matcher = FILE_NAME_PATTERN.matcher(contentDisposition);
        return matcher.find() ? matcher.group("filename") : null;
    }

    public static String obtainFilePath(String jobId) {
        return FiletableConfig.getUploadPath() + jobId + ".upload";
    }

    public static String obtainFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        Matcher matcher = FILE_TYPE_PATTERN.matcher(fileName);
        return matcher.find() ? matcher.group("type") : null;
    }


    /**
     * 获取字符串中的字母
     *
     * @param str
     * @return
     */
    public static String extractString(String str) {
        if (str == null) {
            str = "";
        }
        char temp[] = str.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : temp) {
            if (Character.isDigit(c)) {
                break;
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * 字母递增，Z之后返回AA
     *
     * @param str
     * @return
     */
    public static String autoIncrementString(String str) {
        if (str == null) {
            str = "";
        }
        char chs[] = str.toCharArray();
        char ch = chs[chs.length - 1];
        int index = 1;
        StringBuilder result = new StringBuilder();
        while (ch == 'Z') {
            chs[chs.length - index] = 'A';
            index++;
            if (index > chs.length) {
                break;
            }
            ch = chs[chs.length - index];
        }
        if (index > chs.length) {
            result.append('A');
        } else {
            chs[chs.length - index] = (char) (chs[chs.length - index] + 1);
        }
        result.append(chs);
        return result.toString();
    }

    public static String cutOutString(String target, String suffix) {
        if (target == null) {
            throw new IllegalArgumentException("target is null");
        }
        if (suffix == null) {
            throw new IllegalArgumentException("suffix is null");
        }
        String result = null;
        int index = target.length();
        if (!"".equals(suffix) && target.indexOf(suffix) > 0) {
            index = target.indexOf(suffix);
        }
        result = target.substring(0, index);
        return result;
    }

}
