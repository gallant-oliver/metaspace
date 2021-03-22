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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.user.UserInfo;
import lombok.Data;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/19 11:36
 */
@Data
public class Module implements Comparable<Module>{
    private int moduleId;
    private String moduleName;
    private int type;
    private int approve;
    @JsonIgnore
    private int order;

    public Module() {
    }

    public Module(UserInfo.Module module) {
        this.moduleId = module.getModuleId();
        this.moduleName = module.getModuleName();
        this.type = module.getType();
    }

    @Override
    public int compareTo(Module o) {
        return this.order < o.getOrder() ? -1 : 1;
    }
}
