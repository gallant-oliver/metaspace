package io.zeta.metaspace.web.service;

import edu.npu.fastexcel.ExcelException;
import io.zeta.metaspace.web.common.filetable.UploadConfig;
import io.zeta.metaspace.web.common.filetable.UploadPreview;
import io.zeta.metaspace.web.model.filetable.Workbook;
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
