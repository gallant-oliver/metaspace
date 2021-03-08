package io.zeta.metaspace.model.modifiermanage;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
public class Qualifier {

    private String id;
    private String name;
    private String mark;
    private String creator;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    private String updateUser;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    private String desc;
    private String tenantId;
    private String typeId;
    private List<io.zeta.metaspace.model.modifiermanage.Data> dataList;

}