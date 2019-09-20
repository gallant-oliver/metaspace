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


import io.zeta.metaspace.model.dataSource.DataSourceAuthorizeUser;
import io.zeta.metaspace.model.dataSource.DataSourceAuthorizeUserId;
import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;

import io.zeta.metaspace.model.dataSource.DataSource;
import io.zeta.metaspace.model.dataSource.DataSourceBody;
import io.zeta.metaspace.model.dataSource.DataSourceConnection;
import io.zeta.metaspace.model.dataSource.DataSourceHead;
import io.zeta.metaspace.model.dataSource.DataSourceInfo;
import io.zeta.metaspace.model.dataSource.DataSourceSearch;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.util.AESUtils;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStoreV2;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;

import java.sql.SQLException;
import java.sql.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DataSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceService.class);
    @Autowired
    private DataSourceDAO dataSourceDAO;
    @Autowired
    private AtlasEntityStoreV2 atlasEntityStoreV2;




    /**
     * 添加数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    @Transactional
    public int setNewDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            dataSourceDAO.addAuthorize(dataSourceBody.getSourceId(),userId);
            return dataSourceDAO.add(userId,dataSourceBody);
        }catch (AtlasBaseException e) {
            throw e;
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 更新无依赖数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    public int updateNoRelyDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException, SQLException {
        try {
            if (dataSourceDAO.isSourceId(dataSourceBody.getSourceId())==0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源id不存在");
            }
            String userId = AdminUtils.getUserData().getUserId();
            if (noCreateUserId(dataSourceBody.getSourceId(),userId)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"没有编辑该数据源的权限");
            }
            if(getRely(dataSourceBody.getSourceId())){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源有依赖,无法编辑ip等属性");
            }
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            dataSourceBody.setPassword(AESUtils.AESEncode(dataSourceBody.getPassword()));
            return dataSourceDAO.updateNoRely(userId,dataSourceBody);
        }catch (AtlasBaseException e) {
            throw e;
        }catch(Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 更新有依赖数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    public int updateRelyDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException, SQLException {
        try {
            if (dataSourceDAO.isSourceId(dataSourceBody.getSourceId())==0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源id不存在");
            }
            String userId = AdminUtils.getUserData().getUserId();
            if (noCreateUserId(dataSourceBody.getSourceId(),userId)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"没有编辑该数据源的权限");
            }
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return dataSourceDAO.updateRely(userId,dataSourceBody);
        }catch (AtlasBaseException e) {
            throw e;
        }catch(Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 获取数据源名字
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public String getSourceNameForSourceId(String sourceId) throws AtlasBaseException {
        try {
            String sourceName = dataSourceDAO.getSourceNameForSourceId(sourceId);
            return sourceName;
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 删除数据源
     * @param sourceIds
     * @return
     * @throws AtlasBaseException
     */
    @Transactional
    public int deleteDataSource(List<String> sourceIds) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            for (String sourceId:sourceIds){
                if (!(dataSourceDAO.isSourceId(sourceId)==0) && noCreateUserId(sourceId,userId)){
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"存在无权限删除的数据源");
                }
                if(getRely(sourceId)){
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"存在有依赖的数据源");
                }
            }
            dataSourceDAO.deleteDataSource(sourceIds);
            return dataSourceDAO.deleteAuthorizeBySourceId(sourceIds);
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 获取数据源详情
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public DataSourceInfo getDataSourceInfo(String sourceId) throws AtlasBaseException {
        try {
            DataSourceInfo dataSourceInfo =  dataSourceDAO.getDataSourceInfo(sourceId);
            if (Objects.isNull(dataSourceInfo)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源id不存在");
            }
            dataSourceInfo.setPassword(AESUtils.AESDecode(dataSourceInfo.getPassword()));
            return dataSourceInfo;
        }catch (AtlasBaseException e) {
            throw e;
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源信息获取失败");
        }
    }

    /**
     * 测试连接
     * @param dataSourceConnection
     * @return
     */
    public boolean testConnection(DataSourceConnection dataSourceConnection){
        dataSourceConnection.setUrl();
        dataSourceConnection.setDriver();

        try {
            Class.forName(dataSourceConnection.getDriver());
            Connection con = DriverManager.getConnection(dataSourceConnection.getUrl(),dataSourceConnection.getUserName(),dataSourceConnection.getPassword());
            con.close();
            return true;
        }catch(Exception e){
            LOG.error(e.getMessage());
            return false;
        }
    }

    public DataSourceConnection getDataSourceConnection(String sourceId){
        DataSourceConnection dataSourceConnection = dataSourceDAO.getConnectionBySourceId(sourceId);
        dataSourceConnection.setPassword(AESUtils.AESDecode(dataSourceConnection.getPassword()));
        return dataSourceConnection;
    }

    /**
     * 查询数据源
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
    public PageResult<DataSourceHead> searchDataSources(int limit, int offset, String sortby, String order, String sourceName,String sourceType,String createTime,String updateTime,String updateUserName) throws AtlasBaseException {
        try {
            DataSourceSearch dataSourceSearch = new DataSourceSearch(sourceName,sourceType,createTime,updateTime,updateUserName);
            PageResult<DataSourceHead> pageResult = new PageResult<>();
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            if (!StringUtils.isEmpty(sortby)){
                if (sortby.equals("sourceName")){
                    sortby="source_name";
                }else if (sortby.equals("sourceType")){
                    sortby="source_type";
                }else if (sortby.equals("createTime")){
                    sortby="create_time";
                }else if (sortby.equals("updateTime")){
                    sortby="update_time";
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"排序类型错误");
                }
            }

            parameters.setSortby(sortby);
            parameters.setOrder(order);

            if(Objects.nonNull(dataSourceSearch.getSourceName()))
                dataSourceSearch.setSourceName(dataSourceSearch.getSourceName().replaceAll("%", "/%").replaceAll("_", "/_"));
            if(Objects.nonNull(dataSourceSearch.getSourceType()))
                dataSourceSearch.setSourceType(dataSourceSearch.getSourceType().replaceAll("%", "/%").replaceAll("_", "/_"));
            if(Objects.nonNull(dataSourceSearch.getCreateTime()))
                dataSourceSearch.setCreateTime(dataSourceSearch.getCreateTime().replaceAll("%", "/%").replaceAll("_", "/_"));
            if(Objects.nonNull(dataSourceSearch.getUpdateTime()))
                dataSourceSearch.setUpdateTime(dataSourceSearch.getUpdateTime().replaceAll("%", "/%").replaceAll("_", "/_"));
            if(Objects.nonNull(dataSourceSearch.getUpdateUserName()))
                dataSourceSearch.setUpdateUserName(dataSourceSearch.getUpdateUserName().replaceAll("%", "/%").replaceAll("_", "/_"));
            String userId = AdminUtils.getUserData().getUserId();

            List<DataSourceHead> list = dataSourceDAO.searchDataSources(parameters,dataSourceSearch,userId);
            for (DataSourceHead head:list) {
                String sourceId = head.getSourceId();
                boolean rely = getRely(sourceId);
                head.setRely(rely);
                boolean permission = !noCreateUserId(sourceId,userId);
                head.setPermission(permission);
            }
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            if (list.size()!=0){
                pageResult.setTotalSize(list.get(0).getTotalSize());
            }else{
                pageResult.setTotalSize(0);
            }

            return pageResult;
        }catch (AtlasBaseException e){
            throw e;
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 判断sourceName是否存在
     * @param sourceName
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public int isSourceName(String sourceName,String sourceId) throws AtlasBaseException {
        try {
            return dataSourceDAO.isSourceName(sourceName,sourceId);
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"SQL异常");
        }
    }

    /**
     * 获取授权用户
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public DataSourceAuthorizeUser getAuthorizeUser(String sourceId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            if (noCreateUserId(sourceId,userId)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"没有授权该数据源的权限");
            }
            DataSourceAuthorizeUser dataSourceAuthorizeUser = new DataSourceAuthorizeUser();
            List<UserIdAndName> authorizeUsers = dataSourceDAO.getAuthorizeUser(sourceId,userId);
            dataSourceAuthorizeUser.setUsers(authorizeUsers);
            int totalSize = 0;
            if (authorizeUsers.size()!=0){
                totalSize = authorizeUsers.get(0).getTotalSize();
            }
            dataSourceAuthorizeUser.setTotalSize(totalSize);
            return dataSourceAuthorizeUser;
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 获取未授权用户
     * @param sourceId
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public DataSourceAuthorizeUser getNoAuthorizeUser(String sourceId,String query) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            if (noCreateUserId(sourceId,userId)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"没有授权该数据源的权限");
            }
            if(Objects.nonNull(query))
                query.replaceAll("%", "/%").replaceAll("_", "/_");

            DataSourceAuthorizeUser dataSourceAuthorizeUser = new DataSourceAuthorizeUser();
            List<UserIdAndName> noAuthorizeUsers = dataSourceDAO.getNoAuthorizeUser(sourceId,query);
            dataSourceAuthorizeUser.setUsers(noAuthorizeUsers);
            int totalSize = 0;
            if (noAuthorizeUsers.size()!=0){
                totalSize = noAuthorizeUsers.get(0).getTotalSize();
            }
            dataSourceAuthorizeUser.setTotalSize(totalSize);
            return dataSourceAuthorizeUser;
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 判断是否是创建人
     * @param sourceId
     * @param userId
     * @return
     */
    public boolean noCreateUserId(String sourceId,String userId){
        return dataSourceDAO.isCreateUser(sourceId,userId)==0;
    }

    /**
     * 数据源授权
     * @param dataSourceAuthorizeUserId
     * @throws AtlasBaseException
     */
    @Transactional
    public void dataSourceAuthorize(DataSourceAuthorizeUserId dataSourceAuthorizeUserId) throws AtlasBaseException {
        try {

            String sourceId = dataSourceAuthorizeUserId.getSourceId();
            String userId = AdminUtils.getUserData().getUserId();
            if (noCreateUserId(sourceId,userId)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"没有授权该数据源的权限");
            }

            List<String> authorizeUserIds = dataSourceAuthorizeUserId.getAuthorizeUserIds();
            List<String> noAuthorizeUserIds = dataSourceAuthorizeUserId.getNoAuthorizeUserIds();
            List<String> oldAuthorizeUserIds = dataSourceDAO.getAuthorizeUserIds(sourceId);
            List<String> newAuthorizeUserIds = new ArrayList<>();
            if (Objects.nonNull(authorizeUserIds) && authorizeUserIds.size()!=0){
                for (String authorizeUserId:authorizeUserIds){
                    if (!oldAuthorizeUserIds.contains(authorizeUserId)){
                        newAuthorizeUserIds.add(authorizeUserId);
                    }
                }
                if (Objects.nonNull(newAuthorizeUserIds) && newAuthorizeUserIds.size()!=0){
                    dataSourceDAO.addAuthorizes(sourceId,newAuthorizeUserIds);
                }
            }
            if (Objects.nonNull(noAuthorizeUserIds) && noAuthorizeUserIds.size()!=0){
                dataSourceDAO.deleteAuthorize(sourceId,noAuthorizeUserIds);
            }

        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }

    }


    /**
     * 判断数据源是否依赖
     * @param sourceId
     * @return
     */
    public boolean getRely(String sourceId) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableInfoById({})", sourceId);
        }
        if (Objects.isNull(sourceId)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询数据源id异常");
        }
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = atlasEntityStoreV2.getById(sourceId);
            return Objects.nonNull(info);
        }catch (AtlasBaseException e){
            LOG.error(e.getMessage());
            return false;
        }
    }


}
