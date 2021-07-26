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

    public Annex(){}

    public Annex(String annexId, String fileName, String fileType, String path) {
        this.annexId = annexId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.path = path;
    }
}
