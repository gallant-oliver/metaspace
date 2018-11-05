package org.apache.atlas.web.service;

import edu.npu.fastexcel.ExcelException;
import org.apache.atlas.web.common.filetable.UploadConfig;
import org.apache.atlas.web.common.filetable.UploadPreview;
import org.apache.atlas.web.model.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 数据预览服务
 **/
public interface PreviewUploadService {

    Logger LOGGER = LoggerFactory.getLogger(PreviewUploadService.class);

    /**
     * 解析文件到workbook
     * @param filePath
     * @return
     * @throws Exception
     */
    Workbook parser(String filePath) throws Exception;

    /**
     * 获取预览数据
     * @param
     * @return
     * @author Qingyun Yu
     * @date 2017..
     */
    UploadPreview previewUpload(String filePath, int size) throws ExcelException, Exception;

    /**
     * 获取指定sheetName的预览数据
     * @param filePath
     * @param uploadConfig
     * @param size
     * @return
     * @throws ExcelException
     */
    UploadPreview previewUpload(String filePath, UploadConfig uploadConfig, int size) throws ExcelException, Exception;
}
