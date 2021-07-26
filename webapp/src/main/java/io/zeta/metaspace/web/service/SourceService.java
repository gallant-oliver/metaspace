package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.approvegroup.ApproveGroup;
import io.zeta.metaspace.model.source.CodeInfo;
import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.source.DataSourceInfo;
import io.zeta.metaspace.model.source.SourceUserInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SourceService {

    /**
     * 获取数据源下某种类型的数据库
     *
     * @param dataSourceId   数据源Id
     * @param dataSourceType 数据库类型
     * @return
     */
    public List<DataBaseInfo> getDatabaseByType(String dataSourceId, String dataSourceType) {
        List<DataBaseInfo> databaseInfoList = new ArrayList<>();
        // TODO: 2021/7/22

        return databaseInfoList;
    }

    public List<CodeInfo> getTypeList() {
        List<CodeInfo> dataBaseTypeList = new ArrayList<>();
        // TODO: 2021/7/22

        return dataBaseTypeList;
    }

    public List<SourceUserInfo> getUserList() {
        List<SourceUserInfo> sourceUserInfoList = new ArrayList<>();
        // TODO: 2021/7/22

        return sourceUserInfoList;
    }

    public List<CodeInfo> getStatusList() {
        List<CodeInfo> dataBaseTypeList = new ArrayList<>();
        // TODO: 2021/7/22

        return dataBaseTypeList;
    }

    public List<ApproveGroup> getApproveGroupList() {
        List<ApproveGroup> approveGroupList = new ArrayList<>();
        // TODO: 2021/7/22

        return approveGroupList;
    }

    public List<DataSourceInfo> getDatasourceList(){
        List<DataSourceInfo> dataSourceInfoList = new ArrayList<>();
        return dataSourceInfoList;
    }
}
