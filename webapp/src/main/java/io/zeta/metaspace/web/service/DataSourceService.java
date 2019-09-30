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

import static io.zeta.metaspace.web.metadata.BaseFields.ATTRIBUTE_QUALIFIED_NAME;
import static io.zeta.metaspace.web.metadata.BaseFields.RMDB_INSTANCE;
import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;

import io.zeta.metaspace.model.dataSource.DataSource;
import io.zeta.metaspace.model.dataSource.DataSourceBody;
import io.zeta.metaspace.model.dataSource.DataSourceConnection;
import io.zeta.metaspace.model.dataSource.DataSourceHead;
import io.zeta.metaspace.model.dataSource.DataSourceCheckMessage;
import io.zeta.metaspace.model.dataSource.DataSourceInfo;
import io.zeta.metaspace.model.dataSource.DataSourceSearch;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.metadata.mysql.MysqlMetaDataProvider;
import io.zeta.metaspace.web.util.AESUtils;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.commons.configuration.Configuration;
import org.apache.poi.ss.usermodel.*;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStoreV2;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.*;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DataSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceService.class);
    @Autowired
    private DataSourceDAO dataSourceDAO;
    @Inject
    protected AtlasGraph graph;

    @Autowired
    private DataSourceDAO datasourceDAO;
    @Autowired
    private AtlasEntityStoreV2 atlasEntityStoreV2;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private MysqlMetaDataProvider mysqlMetaDataProvider;


    private MetaspaceGremlinQueryProvider gremlinQueryProvider = MetaspaceGremlinQueryProvider.INSTANCE;


    /**
     * 添加数据源
     *
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
        } catch (Exception e) {
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
            if (dataSourceDAO.isSourceId(dataSourceBody.getSourceId()) == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }
            String userId = AdminUtils.getUserData().getUserId();
            if (noCreateUserId(dataSourceBody.getSourceId(),userId)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"没有编辑该数据源的权限");
            }
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return dataSourceDAO.updateRely(userId,dataSourceBody);
        }catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
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
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 删除数据源
     *
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
            return dataSourceInfo;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源信息获取失败");
        }
    }

    /**
     * 测试连接
     *
     * @param dataSourceConnection
     * @return
     */
    public boolean testConnection(DataSourceConnection dataSourceConnection) {
        dataSourceConnection.setUrl();
        dataSourceConnection.setDriver();

        try {
            Class.forName(dataSourceConnection.getDriver());
            Connection con = DriverManager.getConnection(dataSourceConnection.getUrl(), dataSourceConnection.getUserName(), dataSourceConnection.getPassword());
            con.close();
            return true;
        } catch (Exception e) {
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
    public PageResult<DataSourceHead> searchDataSources(int limit, int offset, String sortby, String order, String sourceName, String sourceType, String createTime, String updateTime, String updateUserName) throws AtlasBaseException {
        try {
            DataSourceSearch dataSourceSearch = new DataSourceSearch(sourceName, sourceType, createTime, updateTime, updateUserName);
            PageResult<DataSourceHead> pageResult = new PageResult<>();
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

            parameters.setSortby(sortby);
            parameters.setOrder(order);

            if (Objects.nonNull(dataSourceSearch.getSourceName()))
                dataSourceSearch.setSourceName(dataSourceSearch.getSourceName().replaceAll("%", "/%").replaceAll("_", "/_"));
            if (Objects.nonNull(dataSourceSearch.getSourceType()))
                dataSourceSearch.setSourceType(dataSourceSearch.getSourceType().replaceAll("%", "/%").replaceAll("_", "/_"));
            if (Objects.nonNull(dataSourceSearch.getCreateTime()))
                dataSourceSearch.setCreateTime(dataSourceSearch.getCreateTime().replaceAll("%", "/%").replaceAll("_", "/_"));
            if (Objects.nonNull(dataSourceSearch.getUpdateTime()))
                dataSourceSearch.setUpdateTime(dataSourceSearch.getUpdateTime().replaceAll("%", "/%").replaceAll("_", "/_"));
            if (Objects.nonNull(dataSourceSearch.getUpdateUserName()))
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
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e.getMessage());
        }
    }

    /**
     * 判断sourceName是否存在
     *
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL异常");
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

    public boolean isAuthorizeUser(String sourceId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            return dataSourceDAO.isAuthorizeUser(sourceId,userId)!=0;
        }catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取用户id失败"+e.getMessage());
        }


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
            AtlasEntity.AtlasEntityWithExtInfo info = mysqlMetaDataProvider.findInstance(sourceId);
            return Objects.nonNull(info);
        }catch (AtlasBaseException e){
            LOG.error(e.getMessage());
            return false;
        }
    }

    /**
     * 导出数据源
     *
     * @return
     * @throws AtlasBaseException
     */
    public File exportExcel(List<String> sourceIds) throws AtlasBaseException {
        try {
            boolean existOnPg = dataSourceDAO.exportDataSource() > 0 ? true : false;
            List<DataSourceBody> datasourceList = null;
            if (existOnPg) {
                datasourceList = dataSourceDAO.getDataSource(sourceIds);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无数据源");
            }
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
//            if (existOnPg) {
//                for (DataSourceBody dataSourceBody : datasourceList) {
//                    data = new ArrayList<>();
//
//                    data.add(dataSourceBody.getSourceName());
//                    data.add(dataSourceBody.getSourceType());
//                    data.add(dataSourceBody.getDescription());
//                    data.add(dataSourceBody.getIp());
//                    data.add(dataSourceBody.getPort());
//                    data.add(dataSourceBody.getUserName());
//                    data.add(dataSourceBody.getPassword());
//                    data.add(dataSourceBody.getJdbcParameter());
//                    data.add(dataSourceBody.getDatabase());
//                    datas.add(data);
//                }
//            } else {
//                for (String dataSource : sourceList) {
//                    data = new ArrayList<>();
//                    data.add(dataSource);
//                    datas.add(data);
//                }
//            }
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
            File file = new File("DataSource."+ new Timestamp(System.currentTimeMillis()).toString().substring(0,10) + ".xlsx");
            FileOutputStream output = new FileOutputStream(file);
            workbook.write(output);
            output.flush();
            output.close();

            return file;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导出Excel失败");
        }
    }

    /**
     * 导入数据源
     */
    public DataSourceCheckMessage checkDataSourceName(List<String> dataSourceList, List<DataSourceBody> dataSourceWithDisplayList) throws AtlasBaseException {
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
                    setNewDataSource(dataSource);

                } else {
                    String sourceId = dataSourceDAO.getSourceIdBySourceName(sourceName);
                    if (getRely(sourceId)){
                        dataSourceCheckInfo.setErrorMessage("更新数据源失败，数据源存在依赖");
                        errorDataSourceCount++;
                        errorDataSourceList.add(sourceName);
                    }else if (noCreateUserId(sourceId,userId)){
                        dataSourceCheckInfo.setErrorMessage("更新数据源失败，没有更新该数据源的权限");
                        errorDataSourceCount++;
                        errorDataSourceList.add(sourceName);
                    }else{
                        dataSourceCheckInfo.setErrorMessage("更新数据源");
                        dataSource.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                        dataSourceDAO.updateDataSource(userId,dataSource);
                    }
                }
                recordDataSourceList.add(sourceName);

                String sourceType = dataSource.getSourceType();
                String description = dataSource.getDescription();
                String Ip = dataSource.getIp();
                String Port = dataSource.getPort();
                String userName = dataSource.getUserName();
                String password = dataSource.getPassword();
                String jdbcParameter = dataSource.getJdbcParameter();
                String database = dataSource.getDatabase();
                dataSourceCheckInfo.setSourceType(sourceType);
                dataSourceCheckInfo.setDatabase(database);
                dataSourceCheckInfo.setDescription(description);
                dataSourceCheckInfo.setIp(Ip);
                dataSourceCheckInfo.setPort(Port);
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
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    @Transactional
    public DataSourceCheckMessage importDataSource(File file) throws AtlasBaseException {
        try {
            //提取excel数据
            List<DataSourceBody> dataSourceMap = convertExceltoMap(file);
            List<String> dataSourceList= dataSourceDAO.getDataSourceList();

            return checkDataSourceName(dataSourceList, dataSourceMap);
        } catch (AtlasBaseException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    //把excel文档导入到一个list里
    public List<DataSourceBody> convertExceltoMap (File file)throws AtlasBaseException {
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
            for (int i=0;i < 9;i++){
                if (!attributes.get(i).equals(row.getCell(i).getStringCellValue())){
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Excel表头错误，表头名称"+row.getCell(i).getStringCellValue()+"应为"+attributes.get(i));
                }
            }

            for (int i = 1; i < rowNum; i++) {

                row = sheet.getRow(i);
                Cell tmpCell = null;
                for (int j =0; j < 9;j++){
                    tmpCell = row.getCell(j);
                    if (Objects.nonNull(tmpCell)){
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

                if (Objects.nonNull(sourceNameCell)){
                    sourceName = sourceNameCell.getStringCellValue();
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第"+i+"行数据源名字不能为空");
                }
                if (Objects.nonNull(sourceTypeCell)){
                    sourceType = sourceTypeCell.getStringCellValue();
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第"+i+"行数据源类型不能为空");
                }
                description = Objects.nonNull(descriptionCell) ? descriptionCell.getStringCellValue() : null;
                if (Objects.nonNull(ipCell)){
                    ip = ipCell.getStringCellValue();
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第"+i+"行数据源ip不能为空");
                }
                if (Objects.nonNull(portCell)){
                    port = portCell.getStringCellValue();
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第"+i+"行数据源端口不能为空");
                }
                if (Objects.nonNull(userNameCell)){
                    userName = userNameCell.getStringCellValue();
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第"+i+"行数据库用户名不能为空");
                }
                if (Objects.nonNull(passwordCell)){
                    password = passwordCell.getStringCellValue();
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第"+i+"行数据库用户密码不能为空");
                }
                jdbcParameter = Objects.nonNull(jdbcParameterCell) ? jdbcParameterCell.getStringCellValue() : null;
                if (Objects.nonNull(databaseCell)){
                    database = databaseCell.getStringCellValue();
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "第"+i+"行数据库名不能为空");
                }


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
            e.printStackTrace();
                LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());

        }

    }
}