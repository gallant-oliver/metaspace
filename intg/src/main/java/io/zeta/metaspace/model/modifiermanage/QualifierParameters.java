package io.zeta.metaspace.model.modifiermanage;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.zeta.metaspace.model.metadata.Parameters;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class QualifierParameters extends Parameters {
    //类型ID
    private String typeId;
    //修饰词ID
    private String id;
    //创建时间
    //  @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    //开始创建时间
    //@JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startTime;
    //结束创建时间
    // @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp endTime;
}
