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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/5/7 11:19
 */
package io.zeta.metaspace.model.share;

/*
 * @description
 * @author sunhaoning
 * @date 2019/5/7 11:19
 */
public class Organization {
    private String checked;
    private String disable;
    private String id;
    private Boolean isOpen;
    private Long isVm;
    private String name;
    private Boolean open;
    private String pId;
    private String pkid;
    private String ptype;
    private String type;
    private String updateTime;
    private String path;

    public Organization() { }

    public Organization(String checked, String disable, String id, Boolean isOpen, Long isVm, String name, Boolean open, String pId, String pkid, String ptype, String type, String updateTime) {
        this.checked = checked;
        this.disable = disable;
        this.id = id;
        this.isOpen = isOpen;
        this.isVm = isVm;
        this.name = name;
        this.open = open;
        this.pId = pId;
        this.pkid = pkid;
        this.ptype = ptype;
        this.type = type;
        this.updateTime = updateTime;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public String getDisable() {
        return disable;
    }

    public void setDisable(String disable) {
        this.disable = disable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean open) {
        isOpen = open;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getPkid() {
        return pkid;
    }

    public void setPkid(String pkid) {
        this.pkid = pkid;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Long getIsVm() {
        return isVm;
    }

    public void setIsVm(Long isVm) {
        this.isVm = isVm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
