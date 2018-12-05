package org.zeta.metaspace.web.model.filetable;

import org.zeta.metaspace.web.common.filetable.FileType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Workbook {

    private String fieldDelimiter;
    private String fileEncode;
    private FileType fileType;
    private List<String> sheetNames;
    /**
     * excel通过sheetName缓存表
     */
    private Map<String, List<List<String>>> sheets;
    /**
     * 文件存入缓存时间
     */
    private long createTime = 0;

    public List<String> getSheetNames() {
        return sheetNames;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Workbook() {
        this.sheetNames = new ArrayList<String>();
        this.sheets = new LinkedHashMap<String, List<List<String>>>();
    }

    public List<List<String>> getSheet(int index) throws Exception {
        int i = 0;
        for (Map.Entry entry : sheets.entrySet()) {
            if (i == index) {
                return (List<List<String>>) entry.getValue();
            }
            i++;
        }
        throw new Exception("index sheet 不存在！！！");
    }

    public List<List<String>> getSheet(String sheetName) throws Exception {
        List<List<String>> tempList = sheets.get(sheetName);
        if (tempList == null && tempList.isEmpty()) {
            throw new Exception("index sheet 不存在！！！");
        }
        return tempList;
    }

    public void addSheet(String sheetName, List<List<String>> sheet) {
        this.sheets.put(sheetName, sheet);
    }

    public void addSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public List<String> obtainHeadNames(String sheetName, boolean isIncludeHead) {
        List<String> headNames = this.sheets.get(sheetName).get(0);
        if (isIncludeHead) {
            this.sheets.get(sheetName).remove(0);
        }
        return headNames;
    }


    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public String getFileEncode() {
        return fileEncode;
    }

    public void setFileEncode(String fileEncode) {
        this.fileEncode = fileEncode;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public void remove(String sheetName) {
        sheets.remove(sheetName);
    }
}
