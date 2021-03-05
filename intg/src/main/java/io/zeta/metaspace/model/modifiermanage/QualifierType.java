package io.zeta.metaspace.model.modifiermanage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class QualifierType {
    //类型ID
    private String qualifierTypeId;
    //类型名称
    private String qualifierTypeName;
    //类型标识
    private String qualifierTypeMark;
    //创建人
    private String creator;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    //创建时间
    private Timestamp createTime;
    //更新人
    private String updateUser;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    //更新时间
    private Timestamp updateTime;
    //类型描述
    private String qualifierTypeDesc;
    //租户ID
    private String tenantId;

}
