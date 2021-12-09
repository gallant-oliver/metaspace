package io.zeta.metaspace.model.dto.requirements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    /**
     * 文件原始名称
     */
    private String fileName;
    /**
     * HDFS服务器文件存储路径 eg:"/A/B/C.jpg"
     */
    private String filePath;
}
