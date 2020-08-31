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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.share.ApiInfoV2;

/**
 * @author lixiang03
 * @Data 2020/8/12 18:19
 */
public class ApiVersion {
    private String apiId;
    private String apiName;
    private String description;
    private String version;
    private String status;
    @JsonIgnore
    private int count;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ApiVersion(){}

    public ApiVersion(ApiInfoV2 apiInfoV2){
        if (apiInfoV2!=null){
            this.apiId=apiInfoV2.getGuid();
            this.apiName=apiInfoV2.getName();
            this.description=apiInfoV2.getDescription();
            this.version=apiInfoV2.getVersion();
        }
    }
    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
