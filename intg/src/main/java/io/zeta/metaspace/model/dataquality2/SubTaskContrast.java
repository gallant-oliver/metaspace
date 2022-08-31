package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author w
 */
@Data
public class SubTaskContrast {
    /**
     * 执行结果
     */
    private Float result;
    /**
     * 执行时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp executeTime;

}
