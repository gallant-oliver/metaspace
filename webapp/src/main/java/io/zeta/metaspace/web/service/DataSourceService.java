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
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;

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
    private DataSourceService dataSourceService;


    private MetaspaceGremlinQueryProvider gremlinQueryProvider = MetaspaceGremlinQueryProvider.INSTANCE;


    /**
     * 添加数据源
     *
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    public int setNewDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return dataSourceDAO.add(userId, dataSourceBody);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    /**
     * 更新数据源
     *
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    public int updateDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException, SQLException {
        try {
            if (dataSourceDAO.isSourceId(dataSourceBody.getSourceId()) == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不存在");
            }
            String userId = AdminUtils.getUserData().getUserId();
            dataSourceBody.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return dataSourceDAO.update(userId, dataSourceBody);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改失败");
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL异常");
        }
    }

    /**
     * 删除数据源
     *
     * @param sourceIds
     * @return
     * @throws AtlasBaseException
     */
    public int deleteDataSource(List<String> sourceIds) throws AtlasBaseException {
        try {
            return dataSourceDAO.deleteDataSource(sourceIds);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库删除失败");
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

            List<DataSourceHead> list = dataSourceDAO.searchDataSources(parameters, dataSourceSearch);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            if (list.size() != 0) {
                pageResult.setTotalSize(list.get(0).getCount());
            } else {
                pageResult.setTotalSize(0);
            }

            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    /**
     * 判断sourceName是否存在
     *
     * @param sourceName
     * @return
     * @throws AtlasBaseException
     */
    public int isSourceName(String sourceName) throws AtlasBaseException {
        try {
            return dataSourceDAO.isSourceName(sourceName);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL异常");
        }
    }

    /**
     * 判断数据源是否被使用
     *
     * @param sourceId
     * @return
     */
    public boolean useDataSource(String sourceId) {
        return false;
    }

    /**
     * 导出数据源
     *
     * @return
     * @throws AtlasBaseException
     */
    public File exportExcel() throws AtlasBaseException {
        try {
            boolean existOnPg = dataSourceDAO.exportDataSource() > 0 ? true : false;
            List<Map> sourceMapList = null;
            List<String> sourceList = new ArrayList<>();
            List<DataSource> datasourceList = null;
            if (existOnPg) {
                datasourceList = dataSourceDAO.getDataSource();
            } else {
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_NAME_LIST);
                String columnQuery = String.format(query);
                sourceMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                sourceMapList.forEach(obj -> {
                    List<String> nameList = (List) obj.get("Asset.name");
                    if (Objects.nonNull(nameList) && nameList.size() > 0) {
                        sourceList.add(nameList.get(0));
                    }
                });
            }
            List<String> attributes = new ArrayList<>();
            attributes.add("数据源名称");
            attributes.add("数据源类型");
            attributes.add("描述");
            attributes.add("主机IP");
            attributes.add("端口");
            attributes.add("用户名");
            attributes.add("密码");
            attributes.add("Jdbc连接参数");
            attributes.add("数据库名");
            List<List<String>> datas = new ArrayList<>();
            List<String> data = null;
            if (existOnPg) {
                for (DataSource datasource : datasourceList) {
                    data = new ArrayList<>();

                    data.add(datasource.getSourceName());
                    data.add(datasource.getSourceType());
                    data.add(datasource.getDescription());
                    data.add(datasource.getIp());
                    data.add(datasource.getPort());
                    data.add(datasource.getUserName());
                    data.add(datasource.getPassword());
                    data.add(datasource.getJdbcParameter());
                    data.add(datasource.getDatabase());
                    datas.add(data);
                }
            } else {
                for (String dataSource : sourceList) {
                    data = new ArrayList<>();
                    data.add(dataSource);
                    datas.add(data);
                }
            }
            //文件名定义
            Workbook workbook = PoiExcelUtils.createExcelFile(attributes, datas, XLSX);
            File file = new File("DataSource" + ".xlsx");
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
    public DataSourceCheckMessage checkDataSourceName(List<String> datasourceList, List<DataSource> dataSourcewithDisplayList) throws AtlasBaseException {
        try {
            //全部字段名称
            //当前需要编辑的字段名称
            //数据源导入情况
            DataSourceCheckMessage dataSourceCheckMessage = new DataSourceCheckMessage();
            //数据源错误计数
            int errorDataSourceCount = 0;
            List<String> errorDataSourceList = new ArrayList<>();
            List<DataSourceCheckMessage.DataSourceCheckInfo> dataSourceCheckMessageList = new ArrayList<>();
            //已导入数据源名字
            List<String> recordDataSourceList = new ArrayList<>();
            DataSourceCheckMessage.DataSourceCheckInfo dataSourceCheckInfo = null;
            int index = 0;
            for (DataSource dataSource : dataSourcewithDisplayList) {
                //获取要导入数据源名称
                String sourceName = dataSource.getSourceName();
                dataSourceCheckInfo = new DataSourceCheckMessage.DataSourceCheckInfo();
                dataSourceCheckInfo.setRow(index++);
                dataSourceCheckInfo.setSourceName(sourceName);
                //是否为重复字段
                if (recordDataSourceList.contains(sourceName)) {
                    dataSourceCheckInfo.setErrorMessage("导入重复字段");
                    errorDataSourceList.add(sourceName);
                    errorDataSourceCount++;
//                    dataSourceCheckMessageList.add(dataSourceCheckInfo);

                    //表中是否存在当前字段
                } else if (!datasourceList.contains(sourceName)) {
                    dataSourceCheckInfo.setErrorMessage("插入新数据库");
                    String database = dataSource.getDatabase();
                    DataSourceBody dataSourceBody = new DataSourceBody();
                    dataSourceBody.setSourceName(dataSource.getSourceName());
                    dataSourceBody.setSourceType(dataSource.getSourceType());
                    dataSourceBody.setDescription(dataSource.getDescription());
                    dataSourceBody.setIp(dataSource.getIp());
                    dataSourceBody.setPort(dataSource.getPort());
                    dataSourceBody.setUserName(dataSource.getUserName());
                    dataSourceBody.setDatabase(dataSource.getDatabase());
                    dataSourceBody.setPassword(dataSource.getPassword());
                    dataSourceBody.setJdbcParameter(dataSource.getJdbcParameter());
                    dataSourceBody.setSourceId(UUID.randomUUID().toString());
                    System.out.println(dataSourceBody);
                    setNewDataSource(dataSourceBody);
                    dataSourceCheckInfo.setDatabase(database);

                } else {
                    dataSourceCheckInfo.setErrorMessage("更新数据库");
                    dataSourceDAO.updateDataSource(dataSource);
//                    dataSourceCheckMessageList.add(dataSourceCheckInfo);
                }
                recordDataSourceList.add(sourceName);

                String sourceType = dataSource.getSourceType();
                String description = dataSource.getDescription();
                String Ip = dataSource.getIp();
                String Port = dataSource.getPort();
                String userName = dataSource.getUserName();
                String password = dataSource.getPassword();
                String jdbcParameter = dataSource.getJdbcParameter();
                dataSourceCheckInfo.setSourceType(sourceType);
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


    public DataSourceCheckMessage importDataSource(File file) throws AtlasBaseException {
        try {
            //提取excel数据
            List<DataSource> dataSourceMap = convertExceltoMap(file);
            //pg中取出dataSource信息，datasouce信息即为一个list
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
    public List<DataSource> convertExceltoMap (File file)throws AtlasBaseException {
        try {
            Workbook workbook = new WorkbookFactory().create(file);
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = sheet.getLastRowNum() + 1;
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
            List resultList = new ArrayList();
            DataSource dataSource = null;

            row = sheet.getRow(0);
            sourceNameCell = row.getCell(0);
            sourceTypeCell = row.getCell(1);
            sourceName = Objects.nonNull(sourceNameCell) ? sourceNameCell.getStringCellValue() : "";
            sourceType = Objects.nonNull(sourceTypeCell) ? sourceTypeCell.getStringCellValue() : "";

            if (!"数据源名称".equals(sourceName) || !"数据源类型".equals(sourceType)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Excel表头错误，表头名称应为【数据源名称】和【数据源类型】");
            }

            for (int i = 1; i < rowNum; i++) {

                 row = sheet.getRow(i);
                sourceNameCell = row.getCell(0);
                sourceTypeCell = row.getCell(1);
                descriptionCell = row.getCell(2);
                ipCell = row.getCell(3);
                portCell = row.getCell(4);
                userNameCell = row.getCell(5);
                passwordCell = row.getCell(6);
                jdbcParameterCell = row.getCell(7);
                databaseCell = row.getCell(8);
//                CellType.BLANK
//                List<String> list = new ArrayList<>();
//                String li = null;
//                try {
//                    row.getCell(8).setCellType(Cell.CELL_TYPE_STRING);
//                    li =row.getCell(8).getStringCellValue();
//                    list.add(li);
//                } catch (NullPointerException e) {
//                    if (li == null) {
//                        list.add("");
//                        continue;
//                    }
//                }
                    row.getCell(0).setCellType(CellType.STRING);
                    row.getCell(1).setCellType(CellType.STRING);
                    row.getCell(2).setCellType(CellType.STRING);
                    row.getCell(3).setCellType(CellType.STRING);
                    row.getCell(4).setCellType(CellType.STRING);
                    row.getCell(5).setCellType(CellType.STRING);
                    row.getCell(6).setCellType(CellType.STRING);
                    row.getCell(7).setCellType(CellType.STRING);
                    row.getCell(8).setCellType(CellType.STRING);
//                row.getCell(8).setCellType(Cell.CELL_TYPE_STRING);

//                    for(Row row : sheet){
//                        int index=0;
//                        for(Cell cell : row){
//                            cell.setCellType(CellType.STRING);
//                            String value = cell.getStringCellValue();
//                            System.out.print("value:"+value+"");
//                            index++;
//                        }
//                        System.out.println();
//                    }
//                passwordCell = row.getCell(0);
//                passwordCell.setCellType.(CellType.STRING);
//                String cellValue = passwordCell.getStringCellValue();

                sourceName = Objects.nonNull(sourceNameCell) ? sourceNameCell.getStringCellValue() : "";
                sourceType = Objects.nonNull(sourceTypeCell) ? sourceTypeCell.getStringCellValue() : "";
                description = Objects.nonNull(descriptionCell) ? descriptionCell.getStringCellValue() : "";
                ip = Objects.nonNull(ipCell) ? ipCell.getStringCellValue() : "";
                port = Objects.nonNull(portCell) ? portCell.getStringCellValue() : "";
                userName = Objects.nonNull(userNameCell) ? userNameCell.getStringCellValue() : "";
                password = Objects.nonNull(passwordCell) ? passwordCell.getStringCellValue() : "";
                jdbcParameter = Objects.nonNull(jdbcParameterCell) ? jdbcParameterCell.getStringCellValue() : "";
                database = Objects.nonNull(databaseCell) ? databaseCell.getStringCellValue() : "";


                dataSource = new DataSource();
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