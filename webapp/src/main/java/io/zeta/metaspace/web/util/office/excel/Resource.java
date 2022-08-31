package io.zeta.metaspace.web.util.office.excel;

import org.apache.poi.hssf.usermodel.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cary on 6/15/17.
 */
public class Resource {
    private static final Logger LOG = LoggerFactory.getLogger(Resource.class);
    /**
     * 中文字体支持
     */
    protected static BaseFont BASE_FONT_CHINESE;
    static {
        try {
            BASE_FONT_CHINESE = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            // 搜尋系統,載入系統內的字型(慢)
            FontFactory.registerDirectories();
        } catch (Exception e) {
            LOG.error("中文字体支持注册字体失败", e);
        }
    }

    /**
     * 將 POI Font 轉換到 iText Font
     * @param font
     * @return
     */
    public static Font getFont(HSSFFont font) {
        try {
            Font iTextFont = FontFactory.getFont(font.getFontName(),
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    font.getFontHeightInPoints());
            return iTextFont;
        } catch (Exception e) {
            LOG.error("将 POI Font 转换到 iText Font失败", e);
        }
        return null;
    }
}