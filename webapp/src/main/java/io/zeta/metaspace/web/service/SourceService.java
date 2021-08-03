package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.source.CodeInfo;
import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.source.DataSourceInfo;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.dao.sourceinfo.CodeSourceInfoStatusDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SourceService {

    @Autowired
    private DatabaseDAO databaseDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CodeSourceInfoStatusDAO codeSourceInfoStatusDAO;

    /**
     * 获取数据源下某种类型的数据库
     *
     * @param dataSourceId   数据源Id
     * @param dataSourceType 数据库类型
     * @return
     */
    public List<DataBaseInfo> getDatabaseByType(String dataSourceId, String tenantId) {
        return databaseDAO.getDataBaseCode(dataSourceId, tenantId);
    }

    public List<User> getUserList() {
        return userDAO.getAllUserByValid();
    }

    public List<CodeInfo> getStatusList() {
        return codeSourceInfoStatusDAO.getAll();
    }

    public List<DataSourceInfo> getDatasourceList() {
        List<DataSourceInfo> dataSourceInfoList = new ArrayList<>();
        return dataSourceInfoList;
    }
}
