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

package io.zeta.metaspace.model.datasource;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2019/9/11 11:15
 */
public class DataSourceAuthorizeUserId {
    private String sourceId;
    private List<String> authorizeUserIds;
    private List<String> noAuthorizeUserIds;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public List<String> getAuthorizeUserIds() {
        return authorizeUserIds;
    }

    public void setAuthorizeUserIds(List<String> authorizeUserIds) {
        this.authorizeUserIds = authorizeUserIds;
    }

    public List<String> getNoAuthorizeUserIds() {
        return noAuthorizeUserIds;
    }

    public void setNoAuthorizeUserIds(List<String> noAuthorizeUserIds) {
        this.noAuthorizeUserIds = noAuthorizeUserIds;
    }
}
