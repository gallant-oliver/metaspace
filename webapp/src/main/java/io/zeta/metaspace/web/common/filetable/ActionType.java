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


package io.zeta.metaspace.web.common.filetable;

/**
 * Created by sunmingqi on 2016/12/21.
 */

/**
 * 上传数据任务类型：
 * insert 新建表
 * append
 * overwrite
 */
public enum ActionType {
    INSERT("INSERT"),
    APPEND("APPEND"),
    OVERWRITE("OVERWRITE");

    private final String type;

    ActionType(String type) {
        this.type = type;
    }

    public String type() {
        return this.type;
    }

}

