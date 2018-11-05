package org.apache.atlas.web.common.filetable;

import org.apache.atlas.web.model.Workbook;

import java.util.HashMap;

public class UploadFileCache extends HashMap<String, Workbook> {
    private volatile static UploadFileCache uploadFileCache = null;

    private UploadFileCache() {
        super();
    }

    public static UploadFileCache create() {
        if (uploadFileCache == null) {
            synchronized (UploadFileCache.class) {
                if (uploadFileCache == null) {
                    uploadFileCache = new UploadFileCache();
                }
            }
        }
        return uploadFileCache;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("uploadFileCache:[");
        for (String key : this.keySet()) {
            result.append(key + ",");
        }
        result.append("]");
        return result.toString().replace(",]", "]");
    }
}