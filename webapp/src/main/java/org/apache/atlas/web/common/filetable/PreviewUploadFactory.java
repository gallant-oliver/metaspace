package org.apache.atlas.web.common.filetable;


import org.apache.atlas.web.service.PreviewUploadCsvServiceImpl;
import org.apache.atlas.web.service.PreviewUploadService;
import org.apache.atlas.web.service.PreviewUploadXlsServiceImpl;
import org.apache.atlas.web.service.PreviewUploadXlsxServiceImpl;

public class PreviewUploadFactory {

    public static PreviewUploadService create(String fileType) {
        fileType = fileType.toLowerCase();
        if (Constants.UPLOAD_CVS.equals(fileType)) {
            return new PreviewUploadCsvServiceImpl();
        } else if (Constants.UPLOAD_XLSX.equals(fileType)) {
            return new PreviewUploadXlsxServiceImpl();
        } else if (Constants.UPLOAD_XLS.equals(fileType)) {
            return new PreviewUploadXlsServiceImpl();
        } else {
            throw new VerifyError("不支持的格式");
        }
    }
}
