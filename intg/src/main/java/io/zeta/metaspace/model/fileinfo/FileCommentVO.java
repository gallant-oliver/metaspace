package io.zeta.metaspace.model.fileinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * file_comment文件归档备注VO
 *
 * @author w
 */
@Data
public class FileCommentVO implements Serializable {
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

    /**
     * 是否可以删除（true为可以，false为不可）
     */
    private Boolean canDel;

    private static final long serialVersionUID = 1L;
}