package io.zeta.metaspace.model.fileinfo;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * file_comment文件归档备注
 *
 * @author w
 */
@Data
public class FileComment implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 备注内容
     */
    private String name;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    /**
     * 创建人（邮箱）
     */
    private String createUser;

    private static final long serialVersionUID = 1L;
}