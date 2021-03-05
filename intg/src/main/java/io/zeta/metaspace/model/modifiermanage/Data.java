package io.zeta.metaspace.model.modifiermanage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@lombok.Data
public class Data implements Serializable {
    private String mark;
    private String name;
    private String desc;
    private String typeId;
    private String id;
    private String creator;
    //@JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonIgnore
    private Integer total;
    private Integer count;
    private List<String> Ids;


}
