package io.zeta.metaspace.model.po.requirements;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.zeta.metaspace.model.enums.FilterOperation;
import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"guid", "createTime", "updateTime", "delete"})
public class RequirementsColumnPO {
    private String guid;
    
    private String requirementsId;
    
    private String tableId;
    
    private String columnId;
    /**
     * 操作符 {@link FilterOperation#getDesc()}
     */
    private String operator;
    
    private String sampleData;
    
    private String description;
    
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    private Integer delete;

}