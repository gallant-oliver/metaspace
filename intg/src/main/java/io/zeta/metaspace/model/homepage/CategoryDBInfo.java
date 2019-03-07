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
 * @date 2019/3/5 11:01
 */
package io.zeta.metaspace.model.homepage;

import java.io.Serializable;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/5 11:01
 */
public class CategoryDBInfo implements Serializable {
    private String guid;
    private String name;
    private long logicDBTotal;
    private long entityDBTotal;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLogicDBTotal() {
        return logicDBTotal;
    }

    public void setLogicDBTotal(long logicDBTotal) {
        this.logicDBTotal = logicDBTotal;
    }

    public long getEntityDBTotal() {
        return entityDBTotal;
    }

    public void setEntityDBTotal(long entityDBTotal) {
        this.entityDBTotal = entityDBTotal;
    }
}
