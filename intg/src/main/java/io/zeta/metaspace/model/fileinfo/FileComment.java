package io.zeta.metaspace.model.fileinfo;

import java.io.Serializable;
import java.util.Date;

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
    private Date createTime;

    /**
     * 创建人（邮箱）
     */
    private String createUser;

    private static final long serialVersionUID = 1L;
}