package io.zeta.metaspace.model.ip.restriction;

import lombok.Data;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

@Data
public class IpRestriction {
    private String id;
    //名称
    private String name;
    //描述
    private String description;
    //类型
    private IpRestrictionType type;
    //Ip 列表
    private List<String> ipList;
    //是否启用
    private boolean enable;

    private long total;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
}
