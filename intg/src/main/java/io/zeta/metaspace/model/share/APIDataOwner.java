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
 * @date 2019/4/23 21:20
 */
package io.zeta.metaspace.model.share;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/23 21:20
 */
public class APIDataOwner {
    private List<String> api_id_list;
    private List<String> api_owner;
    private List<Organization> organization_list;

    public List<String> getApi_id_list() {
        return api_id_list;
    }

    public void setApi_id_list(List<String> api_id_list) {
        this.api_id_list = api_id_list;
    }

    public List<String> getApi_owner() {
        return api_owner;
    }

    public void setApi_owner(List<String> api_owner) {
        this.api_owner = api_owner;
    }

    public List<Organization> getOrganization_list() {
        return organization_list;
    }

    public void setOrganization_list(List<Organization> organization_list) {
        this.organization_list = organization_list;
    }

    public static class Organization {
        private String organization;
        private String organization_type;

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getOrganization_type() {
            return organization_type;
        }

        public void setOrganization_type(String organization_type) {
            this.organization_type = organization_type;
        }
    }
}
