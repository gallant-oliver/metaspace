package io.zeta.metaspace.model.fileinfo;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 业务路径
     */
    private String businessPath;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

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