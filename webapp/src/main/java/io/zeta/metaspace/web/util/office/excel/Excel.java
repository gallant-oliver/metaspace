package io.zeta.metaspace.web.util.office.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cary on 6/15/17.
 */
public class Excel {
    private static final Logger LOG = LoggerFactory.getLogger(Excel.class);
    protected Workbook wb;
    protected Sheet sheet;

    public Excel(InputStream is) {
        try {
            this.wb = WorkbookFactory.create(is);
            this.sheet = wb.getSheetAt(wb.getActiveSheetIndex());
        } catch (FileNotFoundException e) {
            LOG.error("文件没有发现", e);
        } catch (IOException e) {
            LOG.error("文件IO异常", e);
        } catch (InvalidFormatException e) {
            LOG.error("无效的格式", e);
        }
    }

    public Sheet getSheet() {
        return sheet;
    }

    public Workbook getWorkbook(){
        return wb;
    }
}
