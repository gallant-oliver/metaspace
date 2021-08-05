package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

/**
 * 附件表
 */
@Data
public class Annex {
    private String annexId;
    private String fileName;
    private String fileType;
    private String path;
    private long fileSize;

    public Annex(){}

    public Annex(String annexId, String fileName, String fileType, String path,long fileSize) {
        this.annexId = annexId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.path = path;
        this.fileSize = fileSize;
    }
}
