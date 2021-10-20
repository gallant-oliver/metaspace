package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.source.CodeInfo;
import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.source.DataSourceInfo;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.dao.sourceinfo.CodeSourceInfoStatusDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.commons.collections.CollectionUtils;
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

    @Autowired
    private TenantService tenantService;
    @Autowired
    private UsersService usersService;

    /**
     * 获取数据源下某种类型的数据库
     *
     * @param dataSourceId   数据源Id
     * @param tenantId 租户ID
     * @return
     */
    public List<DataBaseInfo> getDatabaseByType(String dataSourceId, String tenantId) {
        List<String> databases = new ArrayList<>();
        User user = AdminUtils.getUserData();
        if("hive".equalsIgnoreCase(dataSourceId)){
            databases = tenantService.getDatabase(tenantId);
            if(CollectionUtils.isEmpty(databases)){
                return new ArrayList<>();
            }

            return databaseDAO.getHiveDataBaseCode(tenantId, databases, user.getUserId());
        }
        else {
            return databaseDAO.getRBMSDataBaseCode(dataSourceId, tenantId, user.getUserId());
        }
    }

    public List<User> getUserList(String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setQuery("");
        parameters.setLimit(-1);
        parameters.setOffset(0);
        PageResult<User> pageResult = usersService.getUserListV2(tenantId,parameters);
        List<User> userList = pageResult.getLists();
        return userList; //userDAO.getAllUserByValid();
    }

    public List<CodeInfo> getStatusList() {
        List<CodeInfo> all = codeSourceInfoStatusDAO.getAll();
        for (CodeInfo codeInfo : all) {
            codeInfo.setCode(Status.getStatusByValue(codeInfo.getCode()));
        }
        return all;
    }

    public List<DataSourceInfo> getDatasourceList() {
        List<DataSourceInfo> dataSourceInfoList = new ArrayList<>();
        return dataSourceInfoList;
    }
}
