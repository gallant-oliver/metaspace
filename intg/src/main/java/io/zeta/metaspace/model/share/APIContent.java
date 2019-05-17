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
 * @date 2019/4/16 16:27
 */
package io.zeta.metaspace.model.share;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/16 16:27
 */
public class APIContent {

    private List<APIDetail> apis_detail;

    public List<APIDetail> getApis_detail() {
        return apis_detail;
    }

    public void setApis_detail(List<APIDetail> apis_detail) {
        this.apis_detail = apis_detail;
    }

    public static class APIDetail {
        private String api_id;
        private String api_name;
        private String api_desc;
        private String api_version;
        private List<String> api_owner;
        private List<Organization> organization_list;
        private String api_catalog;
        private String create_time;
        private String uri;
        private String method;
        private String upstream_url;
        private String swagger_content;

        public APIDetail(String api_id, String api_name, String api_desc, String api_version, List<String> api_owner, List<Organization> organization_list, String api_catalog, String create_time, String uri, String method, String upstream_url, String swagger_content) {
            this.api_id = api_id;
            this.api_name = api_name;
            this.api_desc = api_desc;
            this.api_version = api_version;
            this.api_owner = api_owner;
            this.organization_list = organization_list;
            this.api_catalog = api_catalog;
            this.create_time = create_time;
            this.uri = uri;
            this.method = method;
            this.upstream_url = upstream_url;
            this.swagger_content = swagger_content;
        }

        public APIDetail(String api_id, String api_name, String api_desc, String api_version, List<Organization> organization_list, String api_catalog, String create_time, String uri, String method, String upstream_url, String swagger_content) {
            this.api_id = api_id;
            this.api_name = api_name;
            this.api_desc = api_desc;
            this.api_version = api_version;
            this.organization_list = organization_list;
            this.api_catalog = api_catalog;
            this.create_time = create_time;
            this.uri = uri;
            this.method = method;
            this.upstream_url = upstream_url;
            this.swagger_content = swagger_content;
        }

        public static class Organization {
            private String organization;
            private String organization_type;

            public Organization(String organization, String organization_type) {
                this.organization = organization;
                this.organization_type = organization_type;
            }

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

        public String getApi_id() {
            return api_id;
        }

        public void setApi_id(String api_id) {
            this.api_id = api_id;
        }

        public String getApi_name() {
            return api_name;
        }

        public void setApi_name(String api_name) {
            this.api_name = api_name;
        }

        public String getApi_desc() {
            return api_desc;
        }

        public void setApi_desc(String api_desc) {
            this.api_desc = api_desc;
        }

        public String getApi_version() {
            return api_version;
        }

        public void setApi_version(String api_version) {
            this.api_version = api_version;
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

        public String getApi_catalog() {
            return api_catalog;
        }

        public void setApi_catalog(String api_catalog) {
            this.api_catalog = api_catalog;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getUpstream_url() {
            return upstream_url;
        }

        public void setUpstream_url(String upstream_url) {
            this.upstream_url = upstream_url;
        }

        public String getSwagger_content() {
            return swagger_content;
        }

        public void setSwagger_content(String swagger_content) {
            this.swagger_content = swagger_content;
        }
    }
}
