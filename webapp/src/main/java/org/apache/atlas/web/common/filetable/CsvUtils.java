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

import com.gridsum.gdp.library.commons.exception.NotSupportedException;
import com.gridsum.gdp.library.commons.exception.VerifyException;
import com.gridsum.gdp.library.commons.utils.FileUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.atlas.web.model.filetable.Workbook;
import org.zeta.metaspace.web.util.FileUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CsvUtils {

    private CsvUtils() {
        //do nothing else
    }

    /**
     * 根据分割符生成默认文件列名
     *
     * @param filePath
     * @param fileEncode
     * @param delimiter
     * @throws IOException
     * @returnu
     */
    public static CsvHeader detectCsvHeader(String filePath, String fileEncode, String delimiter, boolean isIncludeHeader) throws IOException {
        Reader fileReader = new InputStreamReader(FileUtils.getInputStream(filePath), fileEncode);
        CsvFormatPredefined csvFormat = CsvFormatPredefined.of(delimiter);
        if (csvFormat == null) {
            throw NotSupportedException.notSupport("FieldDelimiter", delimiter);
        }
        try (CSVParser parser = new CSVParser(fileReader, csvFormat.format())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (!iterator.hasNext()) {
                throw new RuntimeException("上传数据文件内容不能为空!");
            }
            CSVRecord csvRecord = iterator.next();
            String[] headers = new String[csvRecord.size()];
            String[] comments = null;
            //如果包含文件头则把文件头放到comment里
            if (isIncludeHeader) {
                comments = new String[csvRecord.size()];
                for (int i = 0; i < csvRecord.size(); i++) {
                    comments[i] = csvRecord.get(i);
                }
            }
            return new CsvHeader(headers, comments);
        }
    }


    /**
     * 根据分割符生成默认文件列名
     *
     * @param input
     * @param fileEncode
     * @param delimiter
     * @throws IOException
     * @returnu
     */
    public static CsvHeader detectCsvHeader(InputStream input, String fileEncode, String delimiter, boolean isIncludeHeader) throws IOException {
        Reader fileReader = new InputStreamReader(FileUtil.getInputStream(input), fileEncode);
        CsvFormatPredefined csvFormat = CsvFormatPredefined.of(delimiter);
        if (csvFormat == null) {
            throw NotSupportedException.notSupport("FieldDelimiter", delimiter);
        }
        try (CSVParser parser = new CSVParser(fileReader, csvFormat.format())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (!iterator.hasNext()) {
                throw new RuntimeException("上传数据文件内容不能为空!");
            }
            CSVRecord csvRecord = iterator.next();
            String[] headers = new String[csvRecord.size()];
            String[] comments = null;
            //如果包含文件头则把文件头放到comment里
            if (isIncludeHeader) {
                comments = new String[csvRecord.size()];
                for (int i = 0; i < csvRecord.size(); i++) {
                    comments[i] = csvRecord.get(i);
                }
            }
            return new CsvHeader(headers, comments);
        }
    }

    /**
     * 根据出现次数来猜测分隔符
     *
     * @param filePath
     * @param fileCode
     * @return
     */
    public static String detectDelimiter(String filePath, String fileCode) {
        CsvFormatPredefined format = CsvFormatPredefined.COMMA;
        int maxSize = 0;
        for (CsvFormatPredefined csvFormat : CsvFormatPredefined.values()) {
            String lastLine = "";
            int index = 0;
            try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(FileUtils.getInputStream(filePath), fileCode))) {
                String line = null;
                while ((line = fileReader.readLine()) != null) {
                    int currentCount = CharMatcher.anyOf(csvFormat.delimiter()).countIn(line);
                    int lastCount = CharMatcher.anyOf(csvFormat.delimiter()).countIn(lastLine);
                    lastLine = line;
                    if (currentCount == 0) {
                        continue;
                    }
                    if (Strings.isNullOrEmpty(lastLine)) {
                        lastCount = currentCount;
                    }
                    if (lastCount == currentCount && currentCount > maxSize) {
                        maxSize = currentCount;
                        format = csvFormat;
                    }
                    if (index++ > 10) {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return format.delimiter();
    }

    public static UploadPreview getHeadersWithPreview(String file, String fileEncode, String delimiter, boolean includeHeader, CsvHeader csvHeader, int previewSize) throws IOException {
        Reader fileReader = new InputStreamReader(FileUtils.getInputStream(file), fileEncode);
        CsvFormatPredefined csvFormatPredefined = CsvFormatPredefined.of(delimiter);
        if (csvFormatPredefined == null) {
            throw NotSupportedException.notSupport("FieldDelimiter", delimiter);
        }
        UploadPreview preview = new UploadPreview();
        // 2016.12.09
        // CSV文件默认无 header，不把首行作为header
        // 首行是否是header，预览数据的时候由用户决定
        //CSVFormat csvFormat = csvFormatPredefined.format().withHeader(csvHeader.getHeaders());
        CSVFormat csvFormat = csvFormatPredefined.format().withIgnoreEmptyLines();

        try (CSVParser parser = new CSVParser(fileReader, csvFormat)) {
            preview.setHeaders(csvHeader.getColumnExtList());
            preview.setFieldDelimiter(csvFormatPredefined.name());
            preview.setFileEncode(fileEncode);
            preview.setIncludeHeader(includeHeader);
            List<List<String>> previewValues = Lists.newArrayListWithExpectedSize(previewSize);
            int index = 0;
            Iterator<CSVRecord> iterator = parser.iterator();
            List<String> tableHeads = new ArrayList<String>(1);

            // skip first line if includeHeader == true
            if (includeHeader) {
                if (iterator.hasNext()) {
                    CSVRecord record = iterator.next();
                    for (int i = 0; i < csvHeader.size(); i++) {
                        try {
                            tableHeads.add(record.get(i));
                        } catch (Exception e) {
                            throw new IOException(String.format("csv格式错误 行:%s 列:%s", index + 1, i + 1));
                        }
                    }
                }
            } else {
                if (iterator.hasNext()) {
                    List<String> values = Lists.newArrayListWithCapacity(csvHeader.size());
                    CSVRecord record = iterator.next();
                    for (int i = 0; i < csvHeader.size(); i++) {
                        try {
                            tableHeads.add(record.get(i));
                            values.add(record.get(i));
                        } catch (Exception e) {
                            throw new IOException(String.format("csv格式错误 行:%s 列:%s", index + 1, i + 1));
                        }
                    }
                    previewValues.add(values);
                    index++;
                }
            }
            while (index < previewSize && iterator.hasNext()) {
                List<String> values = Lists.newArrayListWithCapacity(csvHeader.size());
                CSVRecord record = iterator.next();
                for (int i = 0; i < csvHeader.size(); i++) {
                    try {
                        values.add(record.get(i));
                    } catch (Exception e) {
                        if (includeHeader) {
                            index++;
                        }
                        throw new IOException(String.format("csv格式错误 行:%s 列:%s", index + 1, i + 1));
                    }
                }
                previewValues.add(values);
                index++;
            }
            if (index == 0) {
                throw new VerifyException("至少有一行数据！");
            }
            preview.setTableHeads(tableHeads);
            preview.setRows(previewValues);
            preview.setSize(index);
            preview.setFileType(FileType.CSV);
            return preview;
        }
    }

    public static Workbook obtainWorkbook(String filePath, String fileEncode, String delimiter) throws IOException {
        Reader fileReader = new InputStreamReader(FileUtils.getInputStream(filePath), fileEncode);
        CsvFormatPredefined csvFormatPredefined = CsvFormatPredefined.of(delimiter);
        if (csvFormatPredefined == null) {
            throw NotSupportedException.notSupport("FieldDelimiter", delimiter);
        }
        Workbook workbook = new Workbook();
        // 2016.12.09
        // CSV文件默认无 header，不把首行作为header
        // 首行是否是header，预览数据的时候由用户决定
        //CSVFormat csvFormat = csvFormatPredefined.format().withHeader(csvHeader.getHeaders());
        CSVFormat csvFormat = csvFormatPredefined.format().withIgnoreEmptyLines();

        try (CSVParser parser = new CSVParser(fileReader, csvFormat)) {
            workbook.setFieldDelimiter(csvFormatPredefined.name());
            workbook.setFileEncode(fileEncode);
            List<List<String>> sheet = new ArrayList<List<String>>();
            int index = 0;
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                List<String> values = new ArrayList<String>();
                CSVRecord record = iterator.next();
                int i=0;
                for (Object temp:record) {
                    try {
                        values.add((String)temp);
                        i++;
                    } catch (Exception e) {
                        throw new IOException(String.format("csv格式错误 行:%s 列:%s", index + 1, i + 1));
                    }
                }
                sheet.add(values);
                index++;
            }
            if (index == 0) {
                throw new VerifyException("至少有一行数据！");
            }
            workbook.setFileType(FileType.CSV);
            workbook.addSheet("csv",sheet);
            return workbook;
        }
    }
}
