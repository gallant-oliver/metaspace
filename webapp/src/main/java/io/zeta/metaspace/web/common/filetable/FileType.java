package io.zeta.metaspace.web.common.filetable;

public enum FileType {
    CSV("csv"),
    XLSX("xlsx"),
    XLS("xls"),
    ZIP("zip");

    private final String fileExtend;

    FileType(String fileExtend) {
        this.fileExtend = fileExtend;
    }

    public String fileExtend() {
        return this.fileExtend;
    }

    public static FileType of(String value) {
        for (FileType fileType : FileType.values()) {
            if (fileType.fileExtend().equals(value)) {
                return fileType;
            }
        }
        return null;
    }

}
