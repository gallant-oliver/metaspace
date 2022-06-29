package io.zeta.metaspace.model.fileinfo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * file_info文件归档详情
 *
 * @author w
 */
@Data
public class FileInfo implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件格式
     */
    private String fileType;

    /**
     * 文件大小（B）
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人（邮箱）
     */
    private String createUser;

    /**
     * 是否删除
     */
    private Boolean delete;

    private static final long serialVersionUID = 1L;
}