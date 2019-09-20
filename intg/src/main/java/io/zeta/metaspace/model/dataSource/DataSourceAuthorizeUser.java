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

package io.zeta.metaspace.model.dataSource;

import io.zeta.metaspace.model.user.UserIdAndName;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2019/9/10 17:50
 */
public class DataSourceAuthorizeUser {
    private int totalSize;
    private List<UserIdAndName> users;

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public List<UserIdAndName> getUsers() {
        return users;
    }

    public void setUsers(List<UserIdAndName> users) {
        this.users = users;
    }

}
