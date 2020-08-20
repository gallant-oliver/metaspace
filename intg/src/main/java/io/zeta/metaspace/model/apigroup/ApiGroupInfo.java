// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.model.apigroup;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/8/12 13:44
 */
public class ApiGroupInfo {
    private String id;
    private String name;
    private String description;
    private String approveJson;
    private List<String> approve;
    private List<ApiCategory> apis;
    private boolean publish;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    private String creator;

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApproveJson() {
        return approveJson;
    }

    public void setApproveJson(String approveJson) {
        this.approveJson = approveJson;
    }

    public List<String> getApprove() {
        return approve;
    }

    public void setApprove(List<String> approve) {
        this.approve = approve;
    }

    public List<ApiCategory> getApis() {
        return apis;
    }

    public void setApis(List<ApiCategory> apis) {
        this.apis = apis;
    }
}
