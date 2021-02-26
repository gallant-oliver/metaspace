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
 * @date 2019/2/19 11:36
 */
package io.zeta.metaspace.model.privilege;

import io.zeta.metaspace.model.user.UserInfo;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/19 11:36
 */
public class Module {
    private int moduleId;
    private String moduleName;
    private int type;
    private int approve;

    public int getApprove() {
        return approve;
    }

    public void setApprove(int approve) {
        this.approve = approve;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Module() {
    }

    public Module(UserInfo.Module module) {
        this.moduleId = module.getModuleId();
        this.moduleName = module.getModuleName();
        this.type = module.getType();
    }

}
