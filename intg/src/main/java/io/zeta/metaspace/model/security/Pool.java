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

package io.zeta.metaspace.model.security;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/28 15:22
 */
public class Pool {

    private String tenantId;

    private String tenantName;

    private List<Queue> yarn;

    private List<Queue> impala;

    private List<Queue> hive;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public List<Queue> getYarn() {
        return yarn;
    }

    public void setYarn(List<Queue> yarn) {
        this.yarn = yarn;
    }

    public List<Queue> getImpala() {
        return impala;
    }

    public void setImpala(List<Queue> impala) {
        this.impala = impala;
    }

    public List<Queue> getHive() {
        return hive;
    }

    public void setHive(List<Queue> hive) {
        this.hive = hive;
    }
}
