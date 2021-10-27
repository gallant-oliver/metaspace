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

package io.zeta.metaspace.web.service;


import com.google.common.collect.Lists;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.datasource.*;
import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.share.APIIdAndName;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupAndPrivilege;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.model.usergroup.UserPrivilegeDataSource;
import io.zeta.metaspace.utils.AESUtils;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.metadata.BaseFields;
import io.zeta.metaspace.web.service.indexmanager.IndexCounter;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStoreV2;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.metadata.BaseFields.ATTRIBUTE_QUALIFIED_NAME;
import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;
import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;

@Service
public class DataSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceService.class);
    @Autowired
    private DataSourceDAO dataSourceDAO;
    @Inject
    protected AtlasGraph graph;
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private DataSourceDAO datasourceDAO;
    @Autowired
    private MetaDataService metadataService;
    @Autowired
    private AtlasEntityStoreV2 atlasEntityStoreV2;
    @Autowired
    private TenantService tenantService;

    @Autowired
    private AtlasTypeRegistry atlasTypeRegistry;
    @Autowired
    private UserGroupDAO userGroupDAO;
    @Autowired
    private IndexCounter indexCounter;
    @Autowired
    private SyncTaskDefinitionDAO syncTaskDefinitionDAO;
    @Autowired
    private TableDAO tableDAO;
    @Autowired
    private ColumnDAO columnDAO;
    @Autowired
    private DatabaseInfoDAO databaseInfoDAO;
    @Autowired
    private SearchService searchService;


    private AbstractMetaspaceGremlinQueryProvider gremlinQueryProvider = AbstractMetaspaceGremlinQueryProvider.INSTANCE;

    /**
     * 记录检索次数和检索时长
     *
     * @param start
     * @param end
     * @param type
     */
    public void metadataSearchStatistics(Long start, Long end, String type) {
        if ("metadata".equals(type)) {
            indexCounter.plusOne(IndexCounterUtils.METASPACE_METADATA_SEARCH_COUNT);
            IndexCounterUtils.INDEX_MAP.get(IndexCounterUtils.METASPACE_METADATA_SEARCH_DURATION).getAndAdd(end - start);
        } else if ("data_kinship".equals(type)) {
            indexCounter.plusOne(IndexCounterUtils.METASPACE_DATA_KINSHIP_SEARCH_COUNT);
            IndexCounterUtils.INDEX_MAP.get(IndexCounterUtils.METASPACE_DATA_KINSHIP_SEARCH_DURATION).getAndAdd(end - start);
        }
    }

    /**
     * 添加数据源
     *
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public int setNewDataSource(DataSourceBody dataSourceBody, boolean isApi, String tenantId) throws AtlasBaseException {
        if (!dataSourceBody.getIp().matches("(((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2}))(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}")) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ip格式错误");
        }
        try {
            String userId = AdminUtils.getUserData().getUserId();
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            String sourceType = dataSourceBody.getSourceType();
            if (sourceType != null) {
                sourceType = sourceType.toUpperCase();
            }
            dataSourceBody.setSourceType(sourceType);
            dataSourceBody.setManager(userId);
            return dataSourceDAO.add(userId, dataSourceBody, isApi, tenantId);
        } catch (Exception e) {
            LOG.error("数据源添加失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源添加失败：\n" + e.getMessage());
        }
    }

    /**
     * 更新无依赖数据源
     *
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    public int updateNoRelyDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException {
        try {
            if (!dataSourceBody.getIp().matches("(((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2}))(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ip格式错误");
            }
            if (dataSourceDAO.isSourceId(dataSourceBody.getSourceId()) == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(dataSourceBody.getSourceId());
            if (Objects.isNull(dataSourceInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }

            String userId = AdminUtils.getUserData().getUserId();
            UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, dataSourceBody.getSourceId());
            if (!"w".equals(userPrivilegeDataSource.getPrivilege())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有编辑该数据源的权限");
            }
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));

            if (!dataSourceBody.isUserNameChanged()) {
                dataSourceBody.setUserName(dataSourceInfo.getUserName());
            }

            if (!dataSourceBody.isPasswordChanged()) {
                dataSourceBody.setPassword(dataSourceInfo.getPassword());
            } else {
                if (dataSourceBody.getPassword() != null) {
                    dataSourceBody.setPassword(AESUtils.aesEncode(dataSourceBody.getPassword()));
                }
            }

            return dataSourceDAO.updateNoRely(userId, dataSourceBody);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新数据源失败\n" + e.getMessage());
        }
    }

    /**
     * 更新有依赖数据源
     *
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    public int updateRelyDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException {
        try {
            if (dataSourceDAO.isSourceId(dataSourceBody.getSourceId()) == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }
            String userId = AdminUtils.getUserData().getUserId();
            UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, dataSourceBody.getSourceId());
            if (!"w".equals(userPrivilegeDataSource.getPrivilege())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有编辑该数据源的权限");
            }
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return dataSourceDAO.updateRely(userId, dataSourceBody);
        } catch (Exception e) {
            LOG.error("更新数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新数据源失败\n" + e.getMessage());
        }
    }

    /**
     * 获取数据源名字
     *
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public String getSourceNameForSourceId(String sourceId) throws AtlasBaseException {
        try {
            String sourceName = dataSourceDAO.getSourceNameForSourceId(sourceId);
            return sourceName;
        } catch (Exception e) {
            LOG.error("获取数据源名字", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据源名字失败\n" + e.getMessage());
        }
    }

    public List<String> getSourceNameForSourceIds(List<String> sourceIds) throws AtlasBaseException{
        try {
            return dataSourceDAO.getSourceNameForSourceIds(sourceIds);
        } catch (Exception e) {
            LOG.error("获取数据源名字出错", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据源名字失败\n" + e.getMessage());
        }
    }

    /**
     * 删除数据源
     *
     * @param sourceIds
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteDataSource(List<String> sourceIds) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            for (String sourceId : sourceIds) {
                UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, sourceId);
                if (!UserPrivilegeDataSource.MANAGER.getPrivilegeCore().equals(userPrivilegeDataSource.getPrivilegeCore())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在无权限删除的数据源");
                }
            }
            LOG.info("删除数据源操作处理...");
            dataSourceDAO.deleteRelationBySourceId(sourceIds);
            LOG.info("删除数据源和用户组关系成功...");
            dataSourceDAO.deleteDataSource(sourceIds);
            LOG.info("删除数据源主表操作成功..");
            return syncTaskDefinitionDAO.deleteByDataSourceId(sourceIds);
        } catch (Exception e) {
            LOG.error("删除数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除数据源失败\n" + e.getMessage());
        }
    }

    public DataSourceInfo getUnencryptedDataSourceInfo(String sourceId) {
        try {
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(sourceId);
            if (Objects.isNull(dataSourceInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }
            dataSourceInfo.setPassword(AESUtils.aesDecode(dataSourceInfo.getPassword()));
            return dataSourceInfo;
        } catch (Exception e) {
            LOG.error("数据源信息获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源信息获取失败\n" + e.getMessage());
        }
    }

    public DataSourceInfo getAnyDataSourceInfoByTableId(String sourceId, String tableId) {
        try {
            DataSourceInfo dataSourceInfo = null;
            if(StringUtils.isNotBlank(sourceId)){
                dataSourceInfo = dataSourceDAO.getDataSourceInfo(sourceId);
            }
            if(Objects.isNull(dataSourceInfo) && StringUtils.isNotBlank(tableId)){
                dataSourceInfo = dataSourceDAO.getAnyDataSourceInfoByTableGuid(tableId);
            }
            if(Objects.isNull(dataSourceInfo)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "库表" + tableId + "没有匹配到数据源");
            }
            dataSourceInfo.setPassword(AESUtils.aesDecode(dataSourceInfo.getPassword()));
            return dataSourceInfo;
        } catch (Exception e) {
            LOG.error("数据源信息获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源信息获取失败\n" + e.getMessage());
        }
    }

    public DataSourceInfo getAnyOneDataSourceByDbGuid(String dbGuid) {
        try {
            DataSourceInfo dataSourceInfo = dataSourceDAO.getAnyOneDataSourceByDbGuid(dbGuid);
            if (Objects.isNull(dataSourceInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有找到与数据库"+dbGuid+"相关联的数据源");
            }
            dataSourceInfo.setPassword(AESUtils.aesDecode(dataSourceInfo.getPassword()));
            return dataSourceInfo;
        } catch (Exception e) {
            LOG.error("数据源信息获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源信息获取失败\n" + e.getMessage());
        }
    }

    /**
     * 获取数据源详情
     *
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public DataSourceInfo getDataSourceInfo(String sourceId) throws AtlasBaseException {
        try {
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(sourceId);
            if (Objects.isNull(dataSourceInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }
            if (dataSourceInfo.getManagerId() != null) {
                String manager = userDAO.getUserName(dataSourceInfo.getManagerId());
                dataSourceInfo.setManager(manager);
            }
            return dataSourceInfo;
        } catch (Exception e) {
            LOG.error("数据源信息获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源信息获取失败\n" + e.getMessage());
        }
    }

    /**
     * 测试连接
     *
     * @param dataSourceConnection
     * @return
     */
    public boolean testConnection(DataSourceConnection dataSourceConnection) throws AtlasBaseException {
        dataSourceConnection = resetDataSourceConnection(dataSourceConnection);
        try {
            Connection con = getConnection(dataSourceConnection);
            con.close();
            return true;
        } catch (Exception e) {
            LOG.error("连接异常", e);
            return false;
        }
    }

    public boolean testConnection(String sourceId) throws AtlasBaseException {
        try {
            Connection con = getConnection(sourceId);
            con.close();
            return true;
        } catch (Exception e) {
            LOG.error("连接异常", e);
            return false;
        }
    }

    public Connection getConnection(DataSourceInfo dataSourceInfo) {
        return AdapterUtils.getAdapterSource(dataSourceInfo).getConnection();
    }

    public AdapterSource getAdapterSource(String sourceId) {
        return AdapterUtils.getAdapterSource(getUnencryptedDataSourceInfo(sourceId));
    }

    public Connection getConnection(String sourceId) {
        return getConnection(getUnencryptedDataSourceInfo(sourceId));
    }

    /**
     * 查询数据源
     *
     * @param limit
     * @param offset
     * @param sortby
     * @param order
     * @param sourceName
     * @param sourceType
     * @param createTime
     * @param updateTime
     * @param updateUserName
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<DataSourceHead> searchDataSources(int limit, int offset, String sortby, String order, String sourceName, String sourceType, String createTime, String updateTime, String updateUserName, boolean isApi, String tenantId) throws AtlasBaseException {
        try {
            PageResult<DataSourceHead> pageResult = getDataSources(limit,  offset,  sortby,  order,  sourceName,  sourceType,  createTime,  updateTime,  updateUserName,  isApi,  tenantId,false);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源查询失败\n" + e.getMessage());
        }

    }
    public PageResult<DataSourceHead> searchPublicDataSources(int limit, int offset, String sortby, String order, String sourceName, String sourceType, String createTime, String updateTime, String updateUserName, boolean isApi, String tenantId) throws AtlasBaseException {
        try {
            boolean globalConfig = metadataService.isConfigGloble();
            PageResult<DataSourceHead> pageResult = getDataSources(limit,  offset,  sortby,  order,  sourceName,  sourceType,  createTime,  updateTime,  updateUserName,  isApi,  tenantId,globalConfig);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源查询失败\n" + e.getMessage());
        }

    }

    private PageResult<DataSourceHead> getDataSources(int limit, int offset, String sortby, String order, String sourceName, String sourceType, String createTime, String updateTime, String updateUserName, boolean isApi, String tenantId,boolean isGlobalConfig) throws AtlasBaseException {
        PageResult<DataSourceHead> pageResult = new PageResult<>();
        if("hive".equalsIgnoreCase(sourceType)){
            DataSourceHead dataSourceHead = new DataSourceHead();
            dataSourceHead.setSourceId("hive");
            dataSourceHead.setSourceName("hive");
            dataSourceHead.setSourceType("HIVE");
            dataSourceHead.setBizTreeId(EntityUtil.generateBusinessId(tenantId,"hive","",""));
            List<DataSourceHead> list = new ArrayList<>();
            list.add(dataSourceHead);
            pageResult.setTotalSize(list.size());
            pageResult.setLists(list);
            pageResult.setLists(list);
            return pageResult;
        }
        DataSourceSearch dataSourceSearch = new DataSourceSearch(sourceName, sourceType, createTime, updateTime, updateUserName);
        Parameters parameters = new Parameters();
        parameters.setLimit(limit);
        parameters.setOffset(offset);
        if (!StringUtils.isEmpty(sortby)) {
            if (sortby.equals("sourceName")) {
                sortby = "source_name";
            } else if (sortby.equals("sourceType")) {
                sortby = "source_type";
            } else if (sortby.equals("createTime")) {
                sortby = "create_time";
            } else if (sortby.equals("updateTime")) {
                sortby = "update_time";
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "排序类型错误");
            }
        }
        if (sortby == null) {
            sortby = "source_name";
        }

        parameters.setSortby(sortby);
        parameters.setOrder(order);
        if (Objects.nonNull(dataSourceSearch.getSourceName()))
            dataSourceSearch.setSourceName(dataSourceSearch.getSourceName().replaceAll("%", "/%").replaceAll("_", "/_"));
        if (Objects.nonNull(dataSourceSearch.getSourceType()))
            dataSourceSearch.setSourceType(dataSourceSearch.getSourceType().replaceAll("%", "/%").replaceAll("_", "/_").toUpperCase());
        if (Objects.nonNull(dataSourceSearch.getCreateTime()))
            dataSourceSearch.setCreateTime(dataSourceSearch.getCreateTime().replaceAll("%", "/%").replaceAll("_", "/_"));
        if (Objects.nonNull(dataSourceSearch.getUpdateTime()))
            dataSourceSearch.setUpdateTime(dataSourceSearch.getUpdateTime().replaceAll("%", "/%").replaceAll("_", "/_"));
        if (Objects.nonNull(dataSourceSearch.getUpdateUserName()))
            dataSourceSearch.setUpdateUserName(dataSourceSearch.getUpdateUserName().replaceAll("%", "/%").replaceAll("_", "/_"));
        String userId = AdminUtils.getUserData().getUserId();

        List<DataSourceHead> list = null;
        if(isGlobalConfig){
            list = isApi ? dataSourceDAO.searchGlobalApiDataSources(parameters, dataSourceSearch, null, tenantId) : dataSourceDAO.searchGlobalDataSources(parameters, dataSourceSearch, null, tenantId);
        }else{
//            list = isApi ? dataSourceDAO.searchApiDataSources(parameters, dataSourceSearch, userId, tenantId) : dataSourceDAO.searchDataSources(parameters, dataSourceSearch, userId, tenantId);
            list = dataSourceDAO.searchGlobalApiDataSources(parameters, dataSourceSearch, userId, tenantId);
        }

        for (DataSourceHead head : list) {
            if (head.getManager() != null) {
                String manager = userDAO.getUserName(head.getManager());
                head.setManager(manager);
            }
            if (head.getUpdateUserName() != null) {
                head.setUpdateUserName(userDAO.getUserName(head.getUpdateUserName()));
            }
            String sourceId = head.getSourceId();
            head.setRely(false);
            head.setBizTreeId(EntityUtil.generateBusinessId(head.getTenantId(),sourceId,"",""));
            UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, sourceId);
            if (UserPrivilegeDataSource.MANAGER.getPrivilegeName().equals(userPrivilegeDataSource.getPrivilegeName())) {
                head.setEditManager(true);
                head.setPermission(true);
            } else if ("w".equals(userPrivilegeDataSource.getPrivilege())) {
                head.setEditManager(false);
                head.setPermission(true);
            } else {
                head.setEditManager(false);
                head.setPermission(false);
            }
            if (head.getOracleDb() == null || head.getOracleDb().length() == 0) {
                head.setSchema(false);
            } else {
                head.setSchema(true);
            }
        }
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        if (list.size() != 0) {
            pageResult.setTotalSize(list.get(0).getTotalSize());
        } else {
            pageResult.setTotalSize(0);
        }

        return pageResult;
    }

    /**
     * 判断sourceName是否存在
     *
     * @param sourceName
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public int isSourceName(String sourceName, String sourceId, String tenantId) throws AtlasBaseException {
        try {
            return dataSourceDAO.isSourceName(sourceName, sourceId, tenantId);
        } catch (Exception e) {
            LOG.error("SQL异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL异常");
        }
    }

    /**
     * 获取授权用户
     *
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public DataSourceAuthorizeUser getAuthorizeUser(String sourceId, boolean isApi) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            String manager = dataSourceDAO.getManagerBySourceId(sourceId);
            DataSourceAuthorizeUser dataSourceAuthorizeUser = new DataSourceAuthorizeUser();
            List<UserIdAndName> authorizeUsers = isApi ? dataSourceDAO.getApiAuthorizeUser(sourceId, manager) : dataSourceDAO.getAuthorizeUser(sourceId, manager);
            dataSourceAuthorizeUser.setUsers(authorizeUsers);
            int totalSize = 0;
            if (authorizeUsers.size() != 0) {
                totalSize = authorizeUsers.get(0).getTotalSize();
            }
            dataSourceAuthorizeUser.setTotalSize(totalSize);
            return dataSourceAuthorizeUser;
        } catch (Exception e) {
            LOG.error("获取授权用户失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取授权用户失败\n" + e.getMessage());
        }
    }

    /**
     * 获取未授权用户
     *
     * @param sourceId
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public DataSourceAuthorizeUser getNoAuthorizeUser(String sourceId, String query, boolean isApi, String tenantId) throws AtlasBaseException {
        try {
            if (Objects.nonNull(query))
                query.replaceAll("%", "/%").replaceAll("_", "/_");
            String manager = dataSourceDAO.getManagerBySourceId(sourceId);
            DataSourceAuthorizeUser dataSourceAuthorizeUser = new DataSourceAuthorizeUser();
            List<UserIdAndName> authorizeUsers = isApi ? dataSourceDAO.getApiAuthorizeUser(sourceId, manager) : dataSourceDAO.getAuthorizeUser(sourceId, manager);
            List<UserIdAndName> noAuthorizeUsers = new ArrayList<>();
            List<UserIdAndName> allUsers = getManager(tenantId);
            for (UserIdAndName userIdAndName : allUsers) {
                if (authorizeUsers.stream().anyMatch(user -> user.getUserId().equals(userIdAndName.getUserId())) || manager.equals(userIdAndName.getUserId())) {
                    continue;
                }
                noAuthorizeUsers.add(userIdAndName);
            }
            dataSourceAuthorizeUser.setUsers(noAuthorizeUsers);
            dataSourceAuthorizeUser.setTotalSize(noAuthorizeUsers.size());
            return dataSourceAuthorizeUser;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取未授权用户失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取未授权用户失败\n" + e.getMessage());
        }
    }

    /**
     * 判断是否是管理者
     *
     * @param sourceId
     * @param userId
     * @return
     */
    public boolean isManagerUserId(String sourceId, String userId) {
        return dataSourceDAO.isManagerUser(sourceId, userId) != 0;
    }

    public boolean isAuthorizeUser(String sourceId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            return dataSourceDAO.isAuthorizeUser(sourceId, userId) != 0;
        } catch (AtlasBaseException e) {
            LOG.error("获取用户id失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户id失败" + e.getMessage());
        }
    }

    /**
     * 数据源授权
     *
     * @param dataSourceAuthorizeUserId
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void dataSourceAuthorize(DataSourceAuthorizeUserId dataSourceAuthorizeUserId) throws AtlasBaseException {
        try {

            String sourceId = dataSourceAuthorizeUserId.getSourceId();
            String userId = AdminUtils.getUserData().getUserId();
            if (!isManagerUserId(sourceId, userId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有授权该数据源的权限");
            }

            List<String> authorizeUserIds = dataSourceAuthorizeUserId.getAuthorizeUserIds();
            List<String> noAuthorizeUserIds = dataSourceAuthorizeUserId.getNoAuthorizeUserIds();
            List<String> oldAuthorizeUserIds = dataSourceDAO.getAuthorizeUserIds(sourceId);
            List<String> newAuthorizeUserIds = new ArrayList<>();
            if (authorizeUserIds == null)
                authorizeUserIds = new ArrayList<>();
            for (String authorizeUserId : authorizeUserIds) {
                if (!oldAuthorizeUserIds.contains(authorizeUserId)) {
                    newAuthorizeUserIds.add(authorizeUserId);
                }
            }
            if (Objects.nonNull(newAuthorizeUserIds) && newAuthorizeUserIds.size() != 0) {
                dataSourceDAO.addAuthorizes(sourceId, newAuthorizeUserIds);
            }
            if (Objects.nonNull(noAuthorizeUserIds) && noAuthorizeUserIds.size() != 0) {
                dataSourceDAO.deleteAuthorize(sourceId, noAuthorizeUserIds);
            }

        } catch (Exception e) {
            LOG.error("数据源授权异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源授权异常\n" + e.getMessage());
        }
    }

    /**
     * 数据源授权:API权限
     *
     * @param dataSourceAuthorizeUserId
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void dataSourceApiAuthorize(DataSourceAuthorizeUserId dataSourceAuthorizeUserId) throws AtlasBaseException {
        try {

            String sourceId = dataSourceAuthorizeUserId.getSourceId();
            String userId = AdminUtils.getUserData().getUserId();
            if (!isManagerUserId(sourceId, userId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有授权该数据源的权限");
            }

            List<String> authorizeUserIds = dataSourceAuthorizeUserId.getAuthorizeUserIds();
            dataSourceDAO.deleteApiAuthorizeBySourceId(sourceId);
            if (authorizeUserIds != null && authorizeUserIds.size() != 0) {
                dataSourceDAO.addApiAuthorizes(sourceId, authorizeUserIds);
            }
            if (datasourceDAO.isApiAuthorizeUser(sourceId, userId) == 0) {
                datasourceDAO.addApiAuthorize(sourceId, userId);
            }
        } catch (Exception e) {
            LOG.error("数据源授权异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源授权异常\n" + e.getMessage());
        }

    }


    /**
     * 判断数据源是否依赖
     *
     * @param sourceId
     * @return
     */
    public boolean getRely(String sourceId) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableInfoById({})", sourceId);
        }
        if (Objects.isNull(sourceId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询数据源id异常");
        }
        try {
            String uuid = atlasEntityStoreV2.getGuidByUniqueAttributes(atlasTypeRegistry.getEntityTypeByName(BaseFields.RMDB_INSTANCE), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, String.format("%s@%s", sourceId, AtlasConfiguration.ATLAS_CLUSTER_NAME.getString())));
            return Objects.nonNull(uuid);
        } catch (AtlasBaseException e) {
            return false;
        }
    }

    /**
     * 判断数据源是否依赖api
     *
     * @param sourceIds
     * @return
     */
    public List<APIIdAndName> getAPIRely(List<String> sourceIds) throws AtlasBaseException {
        if (Objects.isNull(sourceIds)) {
            return new ArrayList<>();
        }
        return datasourceDAO.getAPIRely(sourceIds);
    }

    /**
     * 导出数据源
     *
     * @return
     * @throws AtlasBaseException
     */
    public File exportExcel(List<String> sourceIds, String tenantId) throws AtlasBaseException {
        try {
            boolean existOnPg = dataSourceDAO.exportDataSource(tenantId) > 0 ? true : false;
            List<DataSourceBody> datasourceList = null;
            if (!existOnPg) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无数据源");
            }
            datasourceList = dataSourceDAO.getDataSource(sourceIds);
            List<String> attributes = new ArrayList<>();
            attributes.add("数据源名称");
            attributes.add("数据源类型");
            attributes.add("描述");
            attributes.add("主机IP");
            attributes.add("端口");
            attributes.add("用户名");
            attributes.add("密码");
            attributes.add("数据库名");
            attributes.add("Jdbc连接参数");
            List<List<String>> datas = new ArrayList<>();
            List<String> data = null;
            for (DataSourceBody dataSourceBody : datasourceList) {
                data = new ArrayList<>();
                data.add(dataSourceBody.getSourceName());
                data.add(dataSourceBody.getSourceType());
                data.add(dataSourceBody.getDescription());
                data.add(dataSourceBody.getIp());
                data.add(dataSourceBody.getPort());
                data.add(dataSourceBody.getUserName());
                data.add(dataSourceBody.getPassword());
                data.add(dataSourceBody.getDatabase());
                data.add(dataSourceBody.getJdbcParameter());
                datas.add(data);
            }
            //文件名定义
            Workbook workbook = PoiExcelUtils.createExcelFile(attributes, datas, XLSX);
            File file = new File("DataSource." + new Timestamp(System.currentTimeMillis()).toString().substring(0, 10) + ".xlsx");
            FileOutputStream output = new FileOutputStream(file);
            workbook.write(output);
            output.flush();
            output.close();

            return file;
        } catch (Exception e) {
            LOG.error("导出Excel失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导出Excel失败");
        }
    }

    /**
     * 导入数据源
     */
    public DataSourceCheckMessage checkDataSourceName(List<String> dataSourceList, List<DataSourceBody> dataSourceWithDisplayList, String tenantId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            DataSourceCheckMessage dataSourceCheckMessage = new DataSourceCheckMessage();
            //数据源错误计数
            int errorDataSourceCount = 0;
            List<String> errorDataSourceList = new ArrayList<>();
            List<DataSourceCheckMessage.DataSourceCheckInfo> dataSourceCheckMessageList = new ArrayList<>();
            //已导入数据源名称
            List<String> recordDataSourceList = new ArrayList<>();
            DataSourceCheckMessage.DataSourceCheckInfo dataSourceCheckInfo = null;
            int index = 0;
            for (DataSourceBody dataSource : dataSourceWithDisplayList) {
                //获取要导入数据源名称
                String sourceName = dataSource.getSourceName();
                dataSourceCheckInfo = new DataSourceCheckMessage.DataSourceCheckInfo();
                dataSourceCheckInfo.setRow(index++);
                dataSourceCheckInfo.setSourceName(sourceName);
                if (recordDataSourceList.contains(sourceName)) {
                    dataSourceCheckInfo.setErrorMessage("导入重复数据源名字");
                    errorDataSourceList.add(sourceName);
                    errorDataSourceCount++;

                } else if (!dataSourceList.contains(sourceName)) {
                    dataSourceCheckInfo.setErrorMessage("插入新数据源");

                    dataSource.setSourceId(UUID.randomUUID().toString());
                    setNewDataSource(dataSource, false, tenantId);

                } else {
                    String sourceId = dataSourceDAO.getSourceIdBySourceName(sourceName, tenantId);
                    if (!isManagerUserId(sourceId, userId)) {
                        dataSourceCheckInfo.setErrorMessage("更新数据源失败，没有更新该数据源的权限");
                        errorDataSourceCount++;
                        errorDataSourceList.add(sourceName);
                    } else {
                        dataSourceCheckInfo.setErrorMessage("更新数据源");
                        dataSource.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                        dataSourceDAO.updateDataSource(userId, dataSource, tenantId);
                    }
                }
                recordDataSourceList.add(sourceName);

                String sourceType = dataSource.getSourceType();
                String description = dataSource.getDescription();
                String ip = dataSource.getIp();
                String port = dataSource.getPort();
                String userName = dataSource.getUserName();
                String password = dataSource.getPassword();
                String jdbcParameter = dataSource.getJdbcParameter();
                String database = dataSource.getDatabase();
                dataSourceCheckInfo.setSourceType(sourceType);
                dataSourceCheckInfo.setDatabase(database);
                dataSourceCheckInfo.setDescription(description);
                dataSourceCheckInfo.setIp(ip);
                dataSourceCheckInfo.setPort(port);
                dataSourceCheckInfo.setUserName(userName);
                dataSourceCheckInfo.setPassword(password);
                dataSourceCheckInfo.setJdbcParameter(jdbcParameter);
                dataSourceCheckMessageList.add(dataSourceCheckInfo);
            }

            dataSourceCheckMessage.setDataSourceCheckInfoList(dataSourceCheckMessageList);
            dataSourceCheckMessage.setErrorDataSourceList(errorDataSourceList);
            dataSourceCheckMessage.setTotalSize(dataSourceCheckMessageList.size());
            dataSourceCheckMessage.setErrorCount(errorDataSourceCount);
            return dataSourceCheckMessage;
        } catch (Exception e) {
            LOG.error("导入数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导入数据源失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DataSourceCheckMessage importDataSource(File file, String tenantId) throws AtlasBaseException {
        try {
            //提取excel数据
            List<DataSourceBody> dataSourceMap = convertExceltoMap(file);
            List<String> dataSourceList = dataSourceDAO.getDataSourceList(tenantId);

            return checkDataSourceName(dataSourceList, dataSourceMap, tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("导入数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    //把excel文档导入到一个list里
    public List<DataSourceBody> convertExceltoMap(File file) throws AtlasBaseException {
        try {
            Workbook workbook = new WorkbookFactory().create(file);
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = sheet.getLastRowNum() + 1;

            List<String> attributes = new ArrayList<>();
            attributes.add("数据源名称");
            attributes.add("数据源类型");
            attributes.add("描述");
            attributes.add("主机IP");
            attributes.add("端口");
            attributes.add("用户名");
            attributes.add("密码");
            attributes.add("数据库名");
            attributes.add("Jdbc连接参数");

            Row row = null;
            Cell sourceNameCell = null;
            Cell sourceTypeCell = null;
            Cell descriptionCell = null;
            Cell ipCell = null;
            Cell portCell = null;
            Cell userNameCell = null;
            Cell passwordCell = null;
            Cell databaseCell = null;
            Cell jdbcParameterCell = null;

            String sourceName = null;
            String sourceType = null;
            String description = null;
            String ip = null;
            String port = null;
            String userName = null;
            String password = null;
            String database = null;
            String jdbcParameter = null;
            List<DataSourceBody> resultList = new ArrayList();
            DataSourceBody dataSource = null;

            row = sheet.getRow(0);
            for (int i = 0; i < 9; i++) {
                if (!attributes.get(i).equals(row.getCell(i).getStringCellValue())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Excel表头错误，表头名称" + row.getCell(i).getStringCellValue() + "应为" + attributes.get(i));
                }
            }

            for (int i = 1; i < rowNum; i++) {

                row = sheet.getRow(i);
                Cell tmpCell = null;
                for (int j = 0; j < 9; j++) {
                    tmpCell = row.getCell(j);
                    if (Objects.nonNull(tmpCell)) {
                        tmpCell.setCellType(CellType.STRING);
                    }
                }
                sourceNameCell = row.getCell(0);
                sourceTypeCell = row.getCell(1);
                descriptionCell = row.getCell(2);
                ipCell = row.getCell(3);
                portCell = row.getCell(4);
                userNameCell = row.getCell(5);
                passwordCell = row.getCell(6);
                databaseCell = row.getCell(7);
                jdbcParameterCell = row.getCell(8);

                if (!Objects.nonNull(sourceNameCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第" + i + "行数据源名字不能为空");
                }
                if (!Objects.nonNull(sourceTypeCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第" + i + "行数据源类型不能为空");
                }
                description = Objects.nonNull(descriptionCell) ? descriptionCell.getStringCellValue() : null;
                if (!Objects.nonNull(ipCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第" + i + "行数据源ip不能为空");
                }
                if (!Objects.nonNull(portCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第" + i + "行数据源端口不能为空");
                }
                if (!Objects.nonNull(userNameCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第" + i + "行数据库用户名不能为空");
                }
                if (!Objects.nonNull(passwordCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第" + i + "行数据库用户密码不能为空");
                }
                if (!Objects.nonNull(databaseCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第" + i + "行数据库名不能为空");
                }
                sourceName = sourceNameCell.getStringCellValue();
                sourceType = sourceTypeCell.getStringCellValue();
                ip = ipCell.getStringCellValue();
                port = portCell.getStringCellValue();
                userName = userNameCell.getStringCellValue();
                password = passwordCell.getStringCellValue();
                database = databaseCell.getStringCellValue();
                jdbcParameter = Objects.nonNull(jdbcParameterCell) ? jdbcParameterCell.getStringCellValue() : null;


                dataSource = new DataSourceBody();
                dataSource.setSourceName(sourceName);
                dataSource.setSourceType(sourceType);
                dataSource.setDescription(description);
                dataSource.setIp(ip);
                dataSource.setPort(port);
                dataSource.setUserName(userName);
                dataSource.setPassword(password);
                dataSource.setJdbcParameter(jdbcParameter);
                dataSource.setDatabase(database);
                resultList.add(dataSource);
            }
            return resultList;
        } catch (Exception e) {
            LOG.error("转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());

        }
    }

    /**
     * 获取更新用户
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<String> getUpdateUserName(boolean isApi, String tenantId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            return isApi ? dataSourceDAO.getApiUpdateUserName(userId, tenantId) : dataSourceDAO.getUpdateUserName(userId, tenantId);
        } catch (AtlasBaseException e) {
            LOG.error("获取更新者失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取更新者失败\n" + e.getMessage());
        }
    }

    /**
     * 变更管理者
     *
     * @param sourceId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateManager(String sourceId, String managerUserId) throws AtlasBaseException {
        try {

            String userId = AdminUtils.getUserData().getUserId();
            UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, sourceId);
            if (!UserPrivilegeDataSource.MANAGER.getPrivilegeCore().equals(userPrivilegeDataSource.getPrivilegeCore())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "非数据源管理者无法变更管理者");
            }

            Timestamp updateTime = new Timestamp(System.currentTimeMillis());
            String oldManagerId = dataSourceDAO.getManagerBySourceId(sourceId);
            if (oldManagerId.equals(managerUserId)) {
                return;
            }
            String isApi = dataSourceDAO.getIsApi(sourceId);
            dataSourceDAO.updateManager(userId, managerUserId, sourceId, updateTime);
            if ("t".equals(isApi)) {
                if (dataSourceDAO.isApiAuthorizeUser(sourceId, managerUserId) == 0) {
                    dataSourceDAO.addApiAuthorize(sourceId, managerUserId);
                }
            } else {
                if (dataSourceDAO.isAuthorizeUser(sourceId, managerUserId) == 0) {
                    dataSourceDAO.addAuthorize(sourceId, managerUserId);
                }
            }
            if (StringUtils.isEmpty(oldManagerId)) {
                return;
            }
            List<String> oldManagerIds = new ArrayList<>();
            oldManagerIds.add(oldManagerId);
            if ("t".equals(isApi)) {
                dataSourceDAO.deleteApiAuthorize(sourceId, oldManagerIds);
            } else {
                dataSourceDAO.deleteAuthorize(sourceId, oldManagerIds);
            }
        } catch (Exception e) {
            LOG.error("更新管理者失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新管理者失败\n" + e.getMessage());
        }
    }

    /**
     * 可成为管理者用户
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<UserIdAndName> getManager(String tenantId) throws AtlasBaseException {
        try {
            SecuritySearch securitySearch = new SecuritySearch();
            securitySearch.setTenantId(tenantId);
            PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, securitySearch);
            List<UserIdAndName> users = new ArrayList<>();
            for (UserAndModule userAndModule : userAndModules.getLists()) {
                if (!userAndModule.getToolRoleResources().stream().anyMatch(module -> ModuleEnum.DATASOURCE.getAlias().equalsIgnoreCase(module.getRoleName()))) {
                    continue;
                }
                UserIdAndName user = new UserIdAndName();
                user.setUserName(userAndModule.getUserName());
                user.setAccount(userAndModule.getEmail());
                user.setUserId(userDAO.getUserIdByAccount(userAndModule.getEmail()));
                users.add(user);
            }
            return users;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败:" + e.getMessage());
        }
    }

    /**
     * 获取ORACLE数据源的schema
     *
     * @param limit
     * @param offset
     * @param dataSourceConnection
     * @return
     * @throws AtlasBaseException
     */
    public PageResult getSchema(int limit, int offset, DataSourceConnection dataSourceConnection) throws AtlasBaseException {
        dataSourceConnection = resetDataSourceConnection(dataSourceConnection);
        AdapterExecutor adapterExecutor = AdapterUtils.getAdapterExecutor(dataSourceConnection);
        Parameters parameters = new Parameters();
        parameters.setOffset(offset);
        parameters.setLimit(limit);
        return adapterExecutor.getSchemaPage(parameters);
    }

    public List<LinkedHashMap> extractResultSetData(ResultSet resultSet) throws AtlasBaseException {
        List<LinkedHashMap> result = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                LinkedHashMap map = new LinkedHashMap();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(columnName);
                    map.put(columnName, value);
                }
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            LOG.error("解析查询结果失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "解析查询结果失败");
        }
    }

    public Long extractSizeData(ResultSet resultSet) throws AtlasBaseException {
        long totalSize = 0;
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                String columnName = metaData.getColumnName(1);
                Object value = resultSet.getObject(columnName);
                totalSize = Long.parseLong(value.toString());
            }
            return totalSize;
        } catch (Exception e) {
            LOG.error("解析查询结果失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "解析查询结果失败");
        }
    }

    /**
     * 判断用户对项目的权限
     *
     * @param userId
     * @param sourceId
     * @return
     */
    public UserPrivilegeDataSource getUserPrivilegesDataSource(String userId, String sourceId) {
        String manager = dataSourceDAO.getManagerBySourceId(sourceId);
        if (manager != null && manager.equals(userId)) {
            return UserPrivilegeDataSource.MANAGER;
        }
        List<String> userPrivilegesDataSource = datasourceDAO.getUserPrivilegesDataSource(userId, sourceId);
        if (userPrivilegesDataSource.contains("w")) {
            return UserPrivilegeDataSource.WRITE;
        }
        if (userPrivilegesDataSource.contains("r")) {
            return UserPrivilegeDataSource.READ;
        }
        return UserPrivilegeDataSource.NOPROVILEGE;
    }

    /**
     * 获取对当前项目无权限的用户组
     *
     * @param tenantId
     * @param parameters
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<UserGroupIdAndName> getNoUserGroupByDataSource(String tenantId, Parameters parameters, String sourceId) throws AtlasBaseException {
        //当前用户有权限才能查看，读
        String userId = AdminUtils.getUserData().getUserId();
        UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, sourceId);
        if (!UserPrivilegeDataSource.MANAGER.getPrivilegeCore().equals(userPrivilegeDataSource.getPrivilegeCore())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不是当前数据源的管理者");
        }

        PageResult<UserGroupIdAndName> pageResult = new PageResult<>();
        if (parameters.getQuery() != null) {
            parameters.setQuery(parameters.getQuery().replaceAll("%", "/%").replaceAll("_", "/_"));
        }
        List<UserGroupIdAndName> list = null;
        if (sourceId == null || sourceId.length() == 0) {
            list = userGroupDAO.getUserGroup(tenantId, parameters);
        } else {
            list = dataSourceDAO.getNoUserGroupByDataSource(tenantId, sourceId, parameters);
        }
        pageResult.setLists(list);
        if (list == null || list.size() == 0) {
            return pageResult;
        }
        pageResult.setCurrentSize(list.size());
        pageResult.setTotalSize(list.get(0).getTotalSize());
        return pageResult;
    }

    /**
     * 获取权限用户组列表
     *
     * @param tenantId
     * @param parameters
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<UserGroupAndPrivilege> getUserGroupByDataSource(String tenantId, Parameters parameters, String sourceId) throws AtlasBaseException {
        PageResult<UserGroupAndPrivilege> pageResult = new PageResult<>();
        if (parameters.getQuery() != null) {
            parameters.setQuery(parameters.getQuery().replaceAll("%", "/%").replaceAll("_", "/_"));
        }
        List<UserGroupAndPrivilege> list = dataSourceDAO.getUserGroupByDataSource(tenantId, sourceId, parameters);
        pageResult.setLists(list);
        if (list == null || list.size() == 0) {
            return pageResult;
        }
        pageResult.setCurrentSize(list.size());
        pageResult.setTotalSize(list.get(0).getTotalSize());
        return pageResult;
    }

    /**
     * 删除用户组权限
     *
     * @param userGroups
     * @param sourceId
     * @throws AtlasBaseException
     */
    public void deleteUserGroupByDataSource(List<String> userGroups, String sourceId) throws AtlasBaseException {
        String userId = AdminUtils.getUserData().getUserId();
        //当前用户有权限才能删除，管理者
        UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, sourceId);
        if (!UserPrivilegeDataSource.MANAGER.getPrivilegeCore().equals(userPrivilegeDataSource.getPrivilegeCore())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不是当前数据源的管理者");
        }

        if (userGroups == null && userGroups.size() == 0) {
            return;
        }
        datasourceDAO.deleteUserGroupsByDataSource(sourceId, userGroups);
    }

    /**
     * 更新用户组权限
     *
     * @param sourceId
     * @param privileges
     * @return
     * @throws Exception
     */
    public void updateUserGroupByDataSource(DataSourcePrivileges privileges, String sourceId) throws AtlasBaseException {
        String userId = AdminUtils.getUserData().getUserId();
        UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, sourceId);
        if (!UserPrivilegeDataSource.MANAGER.getPrivilegeCore().equals(userPrivilegeDataSource.getPrivilegeCore())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不是当前数据源的管理者");
        }

        if (privileges.getUserGroups() == null && privileges.getUserGroups().size() == 0) {
            return;
        }
        datasourceDAO.updateUserGroupsByDataSource(sourceId, privileges);
    }

    /**
     * 新增用户组权限
     *
     * @param id
     * @param privileges
     * @return
     */
    public void addUserGroup2DataSource(String id, DataSourcePrivileges privileges)
            throws Exception {
        String userId = AdminUtils.getUserData().getUserId();
        if (dataSourceDAO.isSourceId(id) == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
        }
        //当前用户有权限才能更新，管理员权限
        UserPrivilegeDataSource userPrivilegeDataSource = getUserPrivilegesDataSource(userId, id);
        if (!UserPrivilegeDataSource.MANAGER.getPrivilegeCore().equals(userPrivilegeDataSource.getPrivilegeCore())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不是当前数据源的管理者");
        }
        List<String> userGroups = privileges.getUserGroups();
        if (userGroups == null || userGroups.size() == 0) {
            return;
        }
        if (!isUserGroup(id, userGroups)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在已经有权限的用户组");
        }
        datasourceDAO.addUserGrooup2DataSource(id, privileges.getUserGroups(), privileges.getPrivilegeCode());
    }

    public Boolean isUserGroup(String sourceId, List<String> userGroups) {
        return datasourceDAO.isUserGroup(sourceId, userGroups) == 0;

    }

    public DataSourceConnection resetDataSourceConnection(DataSourceConnection dataSourceConnection) throws AtlasBaseException {
        if (StringUtils.isNotEmpty(dataSourceConnection.getSourceId())) {
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(dataSourceConnection.getSourceId());
            if (Objects.isNull(dataSourceInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }
            if (!dataSourceConnection.isUserNameChanged()) {
                dataSourceConnection.setUserName(dataSourceInfo.getUserName());
            }
            if (!dataSourceConnection.isPasswordChanged()) {
                String password = AESUtils.aesDecode(dataSourceInfo.getPassword());
                dataSourceConnection.setPassword(password);
            }
        }
        return dataSourceConnection;
    }

    public String processingUsername(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return userName;
        }

        int length = userName.length();
        if (length <= 3) {
            return userName;
        }

        int index = length / 3;

        String prefix = userName.substring(0, index);
        String suffix = userName.substring(length - index);
        return MessageFormat.format("{0}***{1}", prefix, suffix);
    }

    /**
     * 获取数据源类型
     *
     * @return
     */
    public List<DataSourceTypeInfo> getDataSourceType(String type) {
        // 配置的数据源类型
        List<String> confList = Arrays.stream(MetaspaceConfig.getDataSourceType()).map(String::toUpperCase).collect(Collectors.toList());
        if ("dbr".equalsIgnoreCase(type)) {
            List<String> builtInList = Arrays.stream(MetaspaceConfig.getDataSourceTypeBuiltIn()).map(String::toUpperCase).collect(Collectors.toList());
            builtInList.addAll(confList);
            return Arrays.stream(DataSourceType.values()).filter(e -> builtInList.contains(e.getName())).map(dataSourceType -> new DataSourceTypeInfo(dataSourceType.getName(), dataSourceType.getDefaultPort())).collect(Collectors.toList());
        }
        if (!MetaspaceConfig.getDataService()) {
            DataSourceTypeInfo dataSourceTypeInfo = new DataSourceTypeInfo(DataSourceType.ORACLE.getName(), DataSourceType.ORACLE.getDefaultPort());
            return Lists.newArrayList(dataSourceTypeInfo);
        }
        return Arrays.stream(DataSourceType.values()).filter(e -> confList.contains(e.getName())).map(dataSourceType -> new DataSourceTypeInfo(dataSourceType.getName(), dataSourceType.getDefaultPort())).collect(Collectors.toList());
    }

    /**
     * 获取可选数据源列表,只查询oracle数据源
     *
     * @param tenantId
     * @return
     */
    public List<OptionalDataSourceDTO> getOptionalDataSource(String tenantId) {
        //1.获取当前租户下用户所属用户组
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        List<OptionalDataSourceDTO> odsds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(groups)) {
            //2. 获取被授权给用户组的数据源
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<DataSourceBody> dataSourceBodies = dataSourceDAO.getOracleDataSourcesByGroups(groupIds, tenantId);
            if (!CollectionUtils.isEmpty(dataSourceBodies)) {
                //根据id去重
                List<DataSourceBody> unique = dataSourceBodies.stream().collect(
                        Collectors.collectingAndThen(
                                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DataSourceBody::getSourceId))), ArrayList::new));
                List<OptionalDataSourceDTO> rdbms = unique.stream().map(x -> BeanMapper.map(x, OptionalDataSourceDTO.class)).collect(Collectors.toList());
                odsds.addAll(rdbms);
            }
        }
        return odsds;
    }

    public List<DataBaseDTO> getOptionalDb(String sourceId, String tenantId) {
        List<String> dbList;
        List<Database> databaseList=new ArrayList<>();
        List<DataBaseDTO> dataBaseDTOS = new ArrayList<>();
        //获取当前租户下用户所属用户组
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
        if ("hive".equalsIgnoreCase(sourceId)) {
            dbList = tenantService.getDatabase(tenantId);
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(dbList)) {
                return dataBaseDTOS;
            }
            databaseList = databaseInfoDAO.selectByHive(dbList, -1l, 0l);
        } else {
            databaseList = databaseInfoDAO.selectDataBaseBySourceId(sourceId,groupIds, -1l, 0l);
        }
        if (!CollectionUtils.isEmpty(databaseList)) {
            dataBaseDTOS = databaseList.stream().map(x -> BeanMapper.map(x, DataBaseDTO.class)).collect(Collectors.toList());
        }

        return dataBaseDTOS;
    }

    public List<TableDTO> getOptionalTable(String sourceId, String databaseId) {
        List<TableEntity> tableEntityList;
        if ("hive".equalsIgnoreCase(sourceId)) {
            tableEntityList = tableDAO.selectListByHiveDb(databaseId, false, -1l, 0l);
        } else {
            tableEntityList = tableDAO.selectListBySourceIdAndDb(sourceId, databaseId, false, -1l, 0l);
        }
        List<TableDTO> tableDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tableEntityList)) {
            tableDTOS = tableEntityList.stream().map(x -> BeanMapper.map(x, TableDTO.class)).collect(Collectors.toList());
        }
        return tableDTOS;
    }

    public List<ColumnDTO> getOptionalColumn(String sourceId,String tableId) throws IOException, SQLException {
        GuidCount guidCount=new GuidCount();
        guidCount.setCount(5);
        guidCount.setGuid(tableId);
        guidCount.setSourceId(sourceId);
        TableShow tableShow = searchService.getTableShow(guidCount, false);
        List<Map<String,String>> lines = tableShow.getLines();
        TableInfo tableInfo=tableDAO.getTableInfoByTableguid(tableId);
        String tableName=tableInfo.getTableName();
        List<Column> columnInfoList = columnDAO.getColumnInfoList(tableId);
        List<ColumnDTO> columnDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(columnInfoList)) {
            columnDTOS = columnInfoList.stream().map(x -> BeanMapper.map(x, ColumnDTO.class)).collect(Collectors.toList());
        }
        if(columnDTOS.size()>0){
           for(ColumnDTO columnDTO:columnDTOS) {
               List<String> values=new ArrayList<>();
               if(lines.size()>0)
               {
                  for(Map<String,String> map:lines){
                      String param=columnDTO.getColumnName();
                      if("hive".equals(sourceId)){
                          param=tableName+"."+columnDTO.getColumnName();
                      }
                      String value=map.get(param);
                      if(!"NULL".equals(value)){
                        if(StringUtils.isNotBlank(value)) {
                            values.add(value);
                        }
                      }
                  }
                   columnDTO.setColumnValues(values);
               }else{
                   columnDTO.setColumnValues(values);
               }
           }
        }
        return columnDTOS;
    }
}