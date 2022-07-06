package io.zeta.metaspace.model.fileinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author huangrongwen
 * @Description: 前端列表VO
 * @date 2022/7/69:37
 */
@Data
public class FileInfoVO {
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
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    /**
     * 最新备注
     */
    private String newComment;
    /**
     * 总数
     */
    @JsonIgnore
    private Long total;
}
