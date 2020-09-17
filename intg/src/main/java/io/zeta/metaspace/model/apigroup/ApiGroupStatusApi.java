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

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/8/12 18:24
 */
public class ApiGroupStatusApi {
    private ApiVersion oldApi;
    private ApiVersion newApi;
    private String updateDescription;

    public ApiVersion getOldApi() {
        return oldApi;
    }

    public void setOldApi(ApiVersion oldApi) {
        this.oldApi = oldApi;
    }

    public ApiVersion getNewApi() {
        return newApi;
    }

    public void setNewApi(ApiVersion newApi) {
        this.newApi = newApi;
    }

    public String getUpdateDescription() {
        return updateDescription;
    }

    public void setUpdateDescription(String updateDescription) {
        this.updateDescription = updateDescription;
    }
}
