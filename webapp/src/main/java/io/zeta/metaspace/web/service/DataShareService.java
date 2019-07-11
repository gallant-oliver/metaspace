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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/3/26 19:56
 */
package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.models.Info;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Yaml;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.AddRelationTable;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIContent;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.DataType;
import io.zeta.metaspace.model.share.FilterColumn;
import io.zeta.metaspace.model.share.JsonQueryResult;
import io.zeta.metaspace.model.share.QueryInfo;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.model.share.QueryResult;
import io.zeta.metaspace.model.share.XmlQueryResult;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.SSLClient;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;

import io.zeta.metaspace.web.util.ImpalaJdbcUtils;
import io.zeta.metaspace.web.util.QualityEngine;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 19:56
 */
@SuppressWarnings("CheckStyle")
@Service
public class DataShareService {

    private static final Logger LOG = LoggerFactory.getLogger(DataShareService.class);

    public static final String ATLAS_REST_ADDRESS = "atlas.rest.address";
    public static final String METASPACE_MOBIUS_ADDRESS = "metaspace.mobius.url";
    private static String engine;

    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    DataManageService dataManageService;
    @Autowired
    private MetaDataService metaDataService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private ColumnDAO columnDAO;

    ExecutorService pool = Executors.newFixedThreadPool(100);

    static {
        try {
            org.apache.commons.configuration.Configuration conf = ApplicationProperties.get();
            engine = conf.getString("metaspace.quality.engine");
        }  catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public int insertAPIInfo(APIInfo info) throws AtlasBaseException {
        try {
            boolean same = querySameName(info.getName());
            if(same) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字的API");
            }
            String guid = UUID.randomUUID().toString();
            //guid
            info.setGuid(guid);
            String user = AdminUtils.getUserData().getUserId();
            //keeper
            info.setKeeper(user);
            //updater
            info.setUpdater(user);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long currentTime = System.currentTimeMillis();
            String currentTimeFormat = sdf.format(currentTime);
            //generateTime
            info.setGenerateTime(currentTimeFormat);
            //updateTime
            info.setUpdateTime(currentTimeFormat);
            //publish
            info.setPublish(false);
            //star
            info.setStar(false);
            //path
            String[] pathList = info.getPath().split("/");
            String path = pathList[pathList.length-1];
            info.setPath(path);
            int count = shareDAO.samePathCount(path);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "重复路径");
            }
            return shareDAO.insertAPIInfo(info);
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "插入失败");
        }
    }

    /**
     * 查询同名
     * @param name
     * @return
     */
    public boolean querySameName(String name) {
        return shareDAO.querySameName(name)==0?false:true;
    }

    /**
     * 删除API
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    public int deleteAPIInfo(String guid) throws AtlasBaseException {
        try {
            return shareDAO.deleteAPIInfo(guid);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    /**
     * 更新API信息
     * @param guid
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public int updateAPIInfo(String guid, APIInfo info) throws AtlasBaseException {
        try {
            String apiName = info.getName();
            APIInfo currentAPI = shareDAO.getAPIInfoByGuid(guid);
            int count = shareDAO.querySameName(apiName);
            if(Objects.isNull(currentAPI)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到当前API信息");
            }
            if(count > 0 && !currentAPI.getName().equals(apiName)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字的API");
            }
            String[] pathList = info.getPath().split("/");
            String path = pathList[pathList.length-1];
            info.setPath(path);
            info.setGuid(guid);
            String user = AdminUtils.getUserData().getUserId();
            //updater
            info.setUpdater(user);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long currentTime = System.currentTimeMillis();
            String currentTimeFormat = sdf.format(currentTime);
            //updateTime
            info.setUpdateTime(currentTimeFormat);
            return shareDAO.updateAPIInfo(info);
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改失败");
        }
    }

    /**
     * API详情
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    public APIInfo getAPIInfo(String guid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            APIInfo info = shareDAO.getAPIInfoByGuid(guid);
            String tableGuid = info.getTableGuid();
            String tableDisplayName = columnDAO.getTableDisplayInfoByGuid(tableGuid);
            if(Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                info.setTableDisplayName(info.getTableName());
            } else {
                info.setTableDisplayName(tableDisplayName);
            }
            if(Objects.isNull(info)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到API信息");
            }
            String version = info.getVersion();
            String path = info.getPath();
            StringJoiner pathJoiner = new StringJoiner("/");
            pathJoiner.add("api").add(version).add("share").add(path);
            info.setPath("/" + pathJoiner.toString());
            List<APIInfo.Field> fields = getQueryFileds(guid);
            //owner.name
            List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(info.getTableGuid());
            List<String> dataOwnerName = new ArrayList<>();
            if(Objects.nonNull(dataOwner) && dataOwner.size()>0) {
                dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
            }
            info.setDataOwner(dataOwnerName);

            List<APIInfo.Field> fieldsWithDisplay = new ArrayList<>();
            List<Column> columnList = columnDAO.getColumnNameWithDisplayList(info.getTableGuid());
            Map<String, String> columnName2DisplayMap = new HashMap();
            columnList.forEach(column -> {
                String columnName = column.getColumnName();
                String columnDisplay = column.getDisplayName();
                if(Objects.isNull(columnDisplay) || "".equals(columnDisplay.trim())) {
                    columnName2DisplayMap.put(columnName, columnName);
                } else {
                    columnName2DisplayMap.put(columnName, columnDisplay);
                }
            });
            for(APIInfo.Field field : fields) {
                APIInfo.FieldWithDisplay fieldWithDisplay = new APIInfo.FieldWithDisplay();
                fieldWithDisplay.setFieldInfo(field);
                String displayName = columnName2DisplayMap.get(field.getColumnName());
                if(Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    fieldWithDisplay.setDisplayName(field.getColumnName());
                } else {
                    fieldWithDisplay.setDisplayName(displayName);
                }
                fieldsWithDisplay.add(fieldWithDisplay);
            }

            info.setFields(fieldsWithDisplay);
            int count = shareDAO.getStarCount(userId, guid);
            if(count > 0) {
                info.setStar(true);
            } else {
                info.setStar(false);
            }
            int userAPICount = shareDAO.countUserAPI(userId, guid);
            info.setEdit(userAPICount==0?false:true);
            //keeper
            String keeperGuid = info.getKeeper();
            User keeperUser = userDAO.getUser(keeperGuid);
            String keeper = keeperUser.getUsername();
            info.setKeeper(keeper);
            //updater
            String updaterGuid = info.getUpdater();
            User updaterUser = userDAO.getUser(updaterGuid);
            String updater = updaterUser.getUsername();
            info.setUpdater(updater);
            return info;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取信息失败");
        }
    }

    /**
     * 获取API列表
     * @param guid
     * @param my
     * @param publish
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<APIInfoHeader> getAPIList(String guid, Integer my, String publish, Parameters parameters) throws AtlasBaseException {
        try {
            Parameters tablePara = new Parameters();
            tablePara.setLimit(-1);
            tablePara.setOffset(0);
            tablePara.setQuery("");
            PageResult<AddRelationTable> tablePageResult = searchService.getPermissionTablePageResultV2(tablePara);
            List<String> permissionTableList = new ArrayList<>();
            if(Objects.nonNull(tablePageResult)) {
                List<AddRelationTable> tableList = tablePageResult.getLists();
                tableList.stream().forEach(table -> permissionTableList.add(table.getTableId()));
            }
            String userId = AdminUtils.getUserData().getUserId();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            String query = parameters.getQuery();
            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            List<APIInfoHeader> list = shareDAO.getAPIList(guid, my, publish, userId, query, limit, offset);
            List<String> starAPIList = shareDAO.getUserStarAPI(userId);
            for(APIInfoHeader header : list) {
                if(permissionTableList.contains(header.getTableGuid())) {
                    header.setEnableClone(true);
                } else {
                    header.setEnableClone(false);
                }
                //keeper
                String keeperGuid = header.getKeeper();
                User keeperUser = userDAO.getUser(keeperGuid);
                String keeper = keeperUser.getUsername();
                header.setKeeper(keeper);
                //updater
                String updaterGuid = header.getUpdater();
                User updaterUser = userDAO.getUser(updaterGuid);
                String updater = updaterUser.getUsername();
                header.setUpdater(updater);
                if(starAPIList.contains(header.getGuid())) {
                    header.setStar(true);
                } else {
                    header.setStar(false);
                }
                List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(header.getTableGuid());
                List<String> dataOwnerName = new ArrayList<>();
                if(Objects.nonNull(dataOwner) && dataOwner.size()>0) {
                    dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
                }
                header.setDataOwner(dataOwnerName);
            }
            int apiCount = shareDAO.getAPICount(guid, my, publish, userId, query);
            pageResult.setOffset(offset);
            pageResult.setSum(apiCount);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public List<APIInfo.Field> getQueryFileds(String guid) throws AtlasBaseException {
        try {
            Gson gson = new Gson();
            Object fields = shareDAO.getQueryFiledsByGuid(guid);
            PGobject pGobject = (PGobject)fields;
            String value = pGobject.getValue();
            Type type = new  TypeToken<List<APIInfo.Field>>(){}.getType();
            List<APIInfo.Field> values = gson.fromJson(value, type);
            return values;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    public int starAPI(String apiGuid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            return shareDAO.insertAPIStar(userId, apiGuid);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新收藏状态失败");
        }
    }

    public int unStarAPI(String apiGuid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            return shareDAO.deleteAPIStar(userId, apiGuid);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新收藏状态失败");
        }
    }

    public int publishAPI(List<String> guidList) throws AtlasBaseException {
        try {
            checkTableStatus(guidList);
            Configuration configuration = ApplicationProperties.get();
            APIContent content = generateAPIContent(guidList);
            Gson gson = new Gson();
            String jsonStr = gson.toJson(content, APIContent.class);
            String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS) + "/svc/create";
            String res = SSLClient.doPost(mobiusURL, jsonStr);
            LOG.info(res);
            Map response = convertMobiusResponse(res);
            String error_id = String.valueOf(response.get("error-id"));
            String error_reason = String.valueOf(response.get("reason"));
            if(!"0.0".equals(error_id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "发布到云平台失败：" + error_reason);
            }
            return shareDAO.updatePublishStatus(guidList, true);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }

    public void checkTableStatus(List<String> guidList) throws AtlasBaseException {
        for(String apiGuid: guidList){
            String status = shareDAO.getTableStatusByAPIGuid(apiGuid);
            if("DELETED".equals(status.trim())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API关联表已被删除");
            }
        }
    }

    public Map convertMobiusResponse(String message) {
        Gson gson = new Gson();
        Map response = gson.fromJson(message, Map.class);
        return response;
    }

    public int unpublishAPI(List<String> apiGuid) throws AtlasBaseException {
        try {
            Configuration configuration = ApplicationProperties.get();
            String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS)  + "/svc/delete";
            Map param = new HashMap();
            param.put("api_id_list", apiGuid);
            Gson gson = new Gson();
            String jsonStr = gson.toJson(param, Map.class);
            String res = SSLClient.doPut(mobiusURL, jsonStr);
            LOG.info(res);
            Map response = convertMobiusResponse(res);
            String error_id = String.valueOf(response.get("error-id"));
            String error_reason = String.valueOf(response.get("reason"));
            if(!"0.0".equals(error_id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "云平台撤销发布失败：" + error_reason);
            }
            return shareDAO.updatePublishStatus(apiGuid, false);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }

    public APIContent generateAPIContent(List<String> guidList) throws Exception {
        APIContent content = new APIContent();
        List<APIContent.APIDetail> contentList = new ArrayList<>();
        for(String api_id : guidList) {
            APIInfo info = shareDAO.getAPIInfoByGuid(api_id);
            String tableGuid = info.getTableGuid();
            String api_name = info.getName();
            String api_desc = info.getDescription();
            String api_version = info.getVersion();
            //String userId = info.getKeeper();
            //String api_owner = userDAO.getUserAccount(userId);
            List<String> owners = new ArrayList<>();
            List<APIContent.APIDetail.Organization> organizations = getOrganization(tableGuid);
            String api_catalog = shareDAO.getGroupByAPIGuid(api_id);
            String create_time = info.getGenerateTime();
            String uri = getURL(info);
            String method = info.getRequestMode();
            String upstream_url ="http://" + getLocalIP() + "/api/metaspace";
            String swagger_content = generateSwaggerContent(info);
            APIContent.APIDetail detail = new APIContent.APIDetail(api_id, api_name, api_desc, api_version, owners, organizations, api_catalog, create_time, uri, method, upstream_url, swagger_content);
            contentList.add(detail);
        }
        content.setApis_detail(contentList);
        return content;
    }

    public List<APIContent.APIDetail.Organization> getOrganization(String guid) throws AtlasBaseException {
        List<APIContent.APIDetail.Organization> list = new ArrayList<>();
        //pkId
        List<String> owners = metaDataService.getDataOwnerId(guid);
        for(String owner : owners) {
            APIContent.APIDetail.Organization organization = new APIContent.APIDetail.Organization(owner, null);
            list.add(organization);
        }
        return list;
    }


    public String getURL(APIInfo info) {
        String version = info.getVersion();
        String pathStr = info.getPath();
        StringJoiner pathJoiner = new StringJoiner("/");
        pathJoiner.add("api").add(version).add("share").add(pathStr);
        pathStr = "/" + pathJoiner.toString();
        return pathStr;
    }

    public static String getLocalIP() throws AtlasException {
            InetAddress addr = null;
        Configuration configuration = null;
            try {
                configuration = ApplicationProperties.get();
                addr = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (AtlasException e) {
                throw e;
            }


            byte[] ipAddr = addr.getAddress();
            String ipAddrStr = "";
            for (int i = 0; i < ipAddr.length; i++) {
                if (i > 0) {
                    ipAddrStr += ".";
                }
                ipAddrStr += ipAddr[i] & 0xFF;
            }
            //System.out.println(ipAddrStr);
            String hostStr = configuration.getString(ATLAS_REST_ADDRESS);
            /*String[] hostArr = hostStr.split("http://");*/
            String[] hostArr = hostStr.split(":");
            Swagger swagger = new Swagger();
            //host
            /*String host = hostArr[1];*/
            String port = hostArr[hostArr.length - 1];
            ipAddrStr += ":" + port;
            return ipAddrStr;
    }

    public String generateSwaggerContent(APIInfo info) throws Exception {
        try {

            String ip=getLocalIP();
            Swagger swagger = new Swagger();
            swagger.setHost(ip);
            //basePath
            swagger.setBasePath("/api/metaspace");
            //scheme
            swagger.setSchemes(Collections.singletonList(Scheme.HTTP));
            //path
            String pathStr = getURL(info);
            Map pathMap = new HashMap();
            Path path = new Path();
            pathMap.put(pathStr, path);
            swagger.setPaths(pathMap);

            Info swaggerInfo = new Info();
            //title
            swaggerInfo.setTitle("DataShare");
            //version
            swaggerInfo.setVersion(info.getVersion());
            swagger.setInfo(swaggerInfo);

            Operation operation = new Operation();
            operation.setConsumes(Collections.singletonList("application/json"));
            operation.setProduces(Collections.singletonList("application/json"));

            List<Parameter> parameters = new ArrayList<>();
            //description
            String desc = info.getDescription();
            operation.setSummary(desc);

            BodyParameter parameter = new BodyParameter();
            parameter.setName(info.getName());
            parameter.setIn("body");
            parameter.setRequired(true);
            ModelImpl requestModel = new ModelImpl();
            ModelImpl responseModel = new ModelImpl();

            requestModel.setRequired(Arrays.asList("columns", "filters", "offset", "limit"));
            Map<String, Property> propertyMap = new HashMap<>();

            List<String> columnList = new ArrayList<>();
            List<APIInfo.Field> values = getQueryFileds(info.getGuid());

            /*Gson gson = new Gson();
            for (Object obj : getQueryFileds(info.getGuid())) {
                Type type = new TypeToken<APIInfo.Field>() {
                }.getType();
                APIInfo.Field field = gson.fromJson(gson.toJson(obj), type);
                values.add(field);
            }*/

            //response
            ArrayProperty responseProperty = new ArrayProperty();
            Map<String, Property> responsePropertyMap = new HashMap<>();
            ObjectProperty resObjectProperty = new ObjectProperty();

            //columns
            ArrayProperty columnsProperty = new ArrayProperty();
            columnsProperty.setRequired(true);
            columnsProperty.setType("array");
            StringProperty columnItem = new StringProperty();
            columnItem.setType("string");

            //filters
            Map<String, Property> filterPropertyMap = new HashMap<>();
            ObjectProperty filtersProperty = new ObjectProperty();
            filtersProperty.setRequired(true);
            filtersProperty.setType("object");

            for (APIInfo.Field field : values) {
                String columnName = field.getColumnName();
                String type = field.getType();
                Boolean filter = field.getFilter();
                Boolean fill = field.getFill();
                String defaultValue = field.getDefaultValue();
                Boolean userDefaultValue = field.getUseDefaultValue();
                //column
                columnList.add(columnName);
                DataType dataType = DataType.parseOf(type);
                if (DataType.BOOLEAN == dataType) {
                    //response
                    BooleanProperty resColumnProperty = new BooleanProperty();
                    resColumnProperty.setType(type);
                    responsePropertyMap.put(columnName, resColumnProperty);
                    //filter
                    if(filter) {
                        BooleanProperty filterColumnProperty = new BooleanProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(userDefaultValue) {
                                filterColumnProperty.setDefault(Boolean.valueOf(defaultValue));
                            }
                            /*if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Boolean.valueOf(defaultValue));*/
                        }
                        filterColumnProperty.setType(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    }
                } else if (DataType.INT == dataType) {
                    //response
                    IntegerProperty resColumnProperty = new IntegerProperty();
                    resColumnProperty.setType("integer");
                    responsePropertyMap.put(columnName, resColumnProperty);

                    //filter
                    if(filter) {
                        IntegerProperty filterColumnProperty = new IntegerProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(userDefaultValue) {
                                filterColumnProperty.setDefault(Integer.valueOf(defaultValue));
                            }
                            /*if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Integer.valueOf(field.getDefaultValue()));*/
                        }
                        filterColumnProperty.setType("integer");
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    }
                } else if (DataType.DOUBLE == dataType) {
                    //response
                    DoubleProperty resColumnProperty = new DoubleProperty();
                    resColumnProperty.setType("number");
                    responsePropertyMap.put(columnName, resColumnProperty);

                    //filter
                    if(filter) {
                        DoubleProperty filterColumnProperty = new DoubleProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(userDefaultValue) {
                                filterColumnProperty.setDefault(Double.valueOf(defaultValue));
                            }
                            /*if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Double.valueOf(field.getDefaultValue()));*/
                        }
                        filterColumnProperty.setType("number");
                        filterColumnProperty.setFormat(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    }
                } else if (DataType.FLOAT == dataType) {
                    //response
                    FloatProperty resColumnProperty = new FloatProperty();
                    resColumnProperty.setType("number");
                    responsePropertyMap.put(columnName, resColumnProperty);

                    if(filter) {
                        FloatProperty filterColumnProperty = new FloatProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(userDefaultValue) {
                                filterColumnProperty.setDefault(Float.valueOf(defaultValue));
                            }
                            /*if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Float.valueOf(field.getDefaultValue()));*/
                        }
                        filterColumnProperty.setType("number");
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    }
                } else {
                    //response
                    StringProperty resColumnProperty = new StringProperty();
                    resColumnProperty.setType("string");
                    responsePropertyMap.put(columnName, resColumnProperty);

                    //filter
                    if(filter) {
                        StringProperty filterColumnProperty = new StringProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(userDefaultValue) {
                                filterColumnProperty.setDefault(defaultValue);
                            }
                            /*if (Objects.nonNull(defaultValue))
                                filterColumnProperty.setDefault(field.getDefaultValue());*/
                        }
                        filterColumnProperty.setType(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    }
                }
            }
            columnItem.setEnum(columnList);
            columnsProperty.setItems(columnItem);
            propertyMap.put("columns", columnsProperty);

            //response
            resObjectProperty.setProperties(responsePropertyMap);
            responseProperty.setItems(resObjectProperty);

            filtersProperty.setProperties(filterPropertyMap);
            propertyMap.put("filters", filtersProperty);
            //offset
            IntegerProperty offset = new IntegerProperty();
            offset.setDefault(0);
            offset.setRequired(true);
            propertyMap.put("offset", offset);
            //limit
            LongProperty limit = new LongProperty();
            limit.setRequired(true);
            Long max = info.getMaxRowNumber();
            if(Objects.nonNull(max)) {
                limit.setDefault(max);
            } else {
                limit.setDefault(10L);
            }
            propertyMap.put("limit", limit);
            //property
            requestModel.setProperties(propertyMap);
            //model
            parameter.setSchema(requestModel);

            parameters.add(parameter);
            operation.setParameters(parameters);
            path.setPost(operation);

            responseModel.setProperties(responsePropertyMap);

            //tags
            List<String> tags = new ArrayList<>();
            tags.add(info.getName());
            operation.setTags(tags);

            Map<String, Response> responseMap = new HashMap<>();
            Response successResponse = new Response();
            successResponse.setSchema(responseProperty);
            successResponse.setDescription("successful operation");
            responseMap.put("200", successResponse);
            Response badResponse = new Response();
            badResponse.setDescription("Invalid status value");
            responseMap.put("400", badResponse);
            operation.setResponses(responseMap);
            String yamlOutput = Yaml.pretty().writeValueAsString(swagger);
            String content = yamlOutput.substring(yamlOutput.indexOf("\n") + 1);
            if(LOG.isDebugEnabled()) {
                LOG.debug(content);
            }
            return content;
        } catch (NumberFormatException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     *
     * @param randomName
     * @param parameter
     * @return
     * @throws AtlasBaseException
     */
    public List<LinkedHashMap> testAPI(String randomName, QueryParameter parameter) throws AtlasBaseException {
        try {
            String tableGuid = parameter.getTableGuid();
            String tableStatus = shareDAO.getTableStatusByGuid(tableGuid);
            if("DELETED".equals(tableStatus)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API关联表已被删除");
            }
            HashMap<String, List> columnMap = new HashMap<>();
            List<QueryParameter.Parameter> parameters = parameter.getParameter();
            parameters.stream().forEach(p -> columnMap.put(p.getColumnName(), p.getValue()));
            Map columnTypeMap = getColumnType(tableGuid, columnMap.keySet());
            String tableName = shareDAO.queryTableNameByGuid(tableGuid);
            String dbName = shareDAO.querydbNameByGuid(tableGuid);
            Long limit = parameter.getLimit();
            Long offset = parameter.getOffset();
            if(Objects.isNull(limit) || Objects.isNull(offset)) {
                throw new AtlasBaseException("limit和offset不允许为空");
            }
            List<String> queryColumns = parameter.getQueryFields();
            long maxRowNumber = parameter.getMaxRowNumber();
            limit = Objects.nonNull(limit)?Math.min(limit, maxRowNumber):maxRowNumber;
            offset = Objects.nonNull(offset)?offset:0;
            //Map sqlMap = getQuerySQL(tableName, columnTypeMap, parameters, queryColumns, limit, offset, true);
            String sql = getQuerySQL(tableName, columnTypeMap, parameters, queryColumns, limit, offset, true);
            APITask task = new APITask(randomName, sql, dbName, true);
            /*Future<List<LinkedHashMap>> futureResult = pool.submit(task);
            List<LinkedHashMap> result = futureResult.get();*/
            Future<Map> futureResultMap = pool.submit(task);
            Map resultMap = futureResultMap.get();
            List<LinkedHashMap> result = (List<LinkedHashMap>)resultMap.get("queryResult");

            return result;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    class APITask implements Callable<Map> {
        private String name;
        private String querySql;
        private String dbName;
        private Boolean test;
        APITask(String name, String querySql, String dbName, Boolean test) {
            this.name = name;
            this.querySql = querySql;
            this.dbName = dbName;
            this.test = test;
        }
        @Override
        public Map call() throws Exception {
            Connection conn = null;
            try {
                if (Objects.nonNull(name)) {
                    Thread.currentThread().setName(name);
                }

                Map resultMap = new HashMap();
                Long count = 0L;


                if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                    conn = ImpalaJdbcUtils.getSystemConnection(dbName);
                } else {
                    conn = HiveJdbcUtils.getSystemConnection(dbName);
                }
                ResultSet resultSet = null;
                if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                    resultSet = ImpalaJdbcUtils.selectBySQLWithSystemCon(conn, querySql);
                } else {
                    resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(conn, querySql);
                }


                List<LinkedHashMap> result = new ArrayList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(columnName);
                        if(!test && i==columnCount) {
                            if(Objects.nonNull(value)) {
                                count = Long.parseLong(value.toString());
                            } else {
                                count = (long)map.size();
                            }
                            continue;
                        }
                        if(Objects.nonNull(test) && test && Objects.nonNull(value)) {
                            map.put(columnName, String.valueOf(value));
                        } else {
                            map.put(columnName, value);
                        }
                    }
                    result.add(map);
                }
                resultMap.put("queryResult", result);
                resultMap.put("queryCount", count);
                return resultMap;
            } catch (AtlasBaseException e) {
                LOG.error(e.getMessage());
                throw e;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                throw e;
            } finally {
                if(Objects.nonNull(conn))
                    conn.close();
            }
        }
    }


    /**
     * 取消查询线程
     * @param name
     * @throws AtlasBaseException
     */
    public void cancelAPIThread(String name) throws AtlasBaseException {
        try {
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            int countThreads = currentGroup.activeCount();
            Thread[] lstThreads = new Thread[countThreads];
            currentGroup.enumerate(lstThreads);
            for (int i = 0; i < countThreads; i++) {
                if (lstThreads[i].getName().equals(name)) {
                    lstThreads[i].interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务取消失败");
        }
    }


    public QueryResult queryAPIData(String path, QueryInfo queryInfo, String acceptHeader) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfo(path);
            if(Objects.isNull(info)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "路径错误，未查询到相关API信息");
            }
            String tableGuid = info.getTableGuid();
            Boolean publish = info.getPublish();
            if(Objects.isNull(publish)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API发布情况未知");
            }
            if (Objects.nonNull(publish) && !publish) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API未发布");
            }
            //查询数据分享API字段详情
            Object fieldsObject = shareDAO.getAPIFields(path);
            PGobject pGobject = (PGobject) fieldsObject;
            String valueObject = pGobject.getValue();
            Gson gson = new Gson();
            List<Map> values = gson.fromJson(valueObject, List.class);
            Set<String> fields = new HashSet<>();
            List<FilterColumn> filterFileds = new ArrayList<>();
            for (Map map : values) {
                String columnName = map.get("columnName").toString();
                Boolean filter = Boolean.parseBoolean(map.get("filter").toString());

                fields.add(columnName);
                if (filter) {
                    Boolean fill = Boolean.parseBoolean(map.get("fill").toString());
                    String defaultValue = Objects.nonNull(map.get("defaultValue"))?map.get("defaultValue").toString():String.valueOf("");
                    Boolean useDefault = Objects.nonNull(map.get("useDefaultValue"))?Boolean.parseBoolean(map.get("useDefaultValue").toString()):false;
                    FilterColumn column = new FilterColumn(columnName, defaultValue, fill, useDefault);
                    filterFileds.add(column);
                }
            }

            //请求中查询字段
            List<String> queryFiles = queryInfo.getColumns();
            //请求中过滤字段
            Map filterColumnMap = queryInfo.getFilters();

            //检查请求中查询字段是否包含于详情中字段
            checkFieldName(fields, queryFiles);
            //获取字段类型
            Map columnTypeMap = getColumnType(tableGuid, fields);
            //校验过滤字段范围
            checkFilterField(filterFileds, filterColumnMap);
            //校验过滤字段取值
            List<QueryParameter.Parameter> kvList = checkFieldValue(filterFileds, filterColumnMap);
            //limit
            Long limit = queryInfo.getLimit();
            //offset
            Long offset = queryInfo.getOffset();
            //对比limit和maxRowNumber
            long maxRowNumber = info.getMaxRowNumber();
            if(Objects.nonNull(limit)) {
                limit = Math.min(limit, maxRowNumber);
            } else {
                limit = maxRowNumber;
            }
            String tableName = shareDAO.queryTableNameByGuid(tableGuid);
            String dbName = shareDAO.querydbNameByGuid(tableGuid);
            //sql
            String querySql = getQuerySQL(tableName, columnTypeMap, kvList, queryFiles, limit, offset, false);

            //query任务
            String queryName = String.valueOf(System.currentTimeMillis());
            APITask task = new APITask(queryName, querySql, dbName, false);

            Future<Map> futureResultMap = pool.submit(task);
            Map resultMap = futureResultMap.get();
            List<LinkedHashMap<String,Object>> queryDataList = (List<LinkedHashMap<String,Object>>)resultMap.get("queryResult");

            Long count = Long.parseLong(resultMap.get("queryCount").toString());

            if(acceptHeader.contains("xml")) {
                XmlQueryResult queryResult = new XmlQueryResult();
                XmlQueryResult.QueryData queryData = new XmlQueryResult.QueryData();
                queryData.setData(queryDataList);
                queryResult.setTotalCount(count);
                queryResult.setDatas(queryData);
                return queryResult;
            } else {
                JsonQueryResult queryResult = new JsonQueryResult();
                queryResult.setDatas(queryDataList);
                queryResult.setTotalCount(count);
                return queryResult;
            }
        } catch (ExecutionException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST , e.getCause().getMessage());
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST , "查询失败");
        }
    }

    /**
     * 查询字段是否在允许范围内
     * @param fields
     * @param queryFields
     * @throws AtlasBaseException
     */
    public void checkFieldName(Set<String> fields, List<String> queryFields) throws AtlasBaseException {
        for(String field : queryFields) {
            if(!fields.contains(field)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的查询字段:" + field);
            }
        }
    }

    public void checkFilterField(List<FilterColumn> filterFields, Map queryFilterFields) throws AtlasBaseException {
        List<String> filterFieldList = new ArrayList<>();
        filterFields.stream().forEach(column -> filterFieldList.add(column.getColumnName()));
        for(Object field : queryFilterFields.keySet()) {
            if(!filterFieldList.contains(field.toString())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的过滤查询字段:" + field.toString());
            }
        }
    }

    /**
     * 校验查询字段取值
     * @param filterFields
     * @param queryFilterFields
     * @return
     * @throws AtlasBaseException
     */
    public List<QueryParameter.Parameter> checkFieldValue(List<FilterColumn> filterFields, Map queryFilterFields) throws AtlasBaseException {
        List<QueryParameter.Parameter> kvList = new ArrayList<>();
        for(FilterColumn field : filterFields) {
            //必传值
            if(field.getFill()) {
                //未传值
                if(!queryFilterFields.containsKey(field.getColumnName())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "请求失败，请在请求参数中给出必传过滤字段:" + field.getColumnName());
                } else {
                    QueryParameter.Parameter parameter = new QueryParameter.Parameter();
                    parameter.setColumnName(field.getColumnName());
                    String key = field.getColumnName();
                    Object value = queryFilterFields.get(key);
                    if(value instanceof String[]) {
                        String[] values = (String[])value;
                        parameter.setValue(Arrays.asList(values));
                    } else if(value instanceof List) {
                        parameter.setValue((List<Object>) value);
                    } else {
                        List<Object> values = new ArrayList<>();
                        values.add(value);
                        parameter.setValue(values);
                    }
                    kvList.add(parameter);
                }
                //非必传
            } else {
                QueryParameter.Parameter parameter = new QueryParameter.Parameter();
                parameter.setColumnName(field.getColumnName());

                //已传值
                if(queryFilterFields.containsKey(field.getColumnName())) {
                    String key = field.getColumnName();
                    Object value = queryFilterFields.get(key);
                    if(value instanceof String[]) {
                        String[] values = (String[])value;
                        parameter.setValue(Arrays.asList(values));
                    } else if(value instanceof List) {
                        parameter.setValue((List<Object>) value);
                    } else {
                        List<Object> values = new ArrayList<>();
                        values.add(value);
                        parameter.setValue(values);
                    }
                //未传值
                } else {
                    Boolean use = field.getUseDefaultValue();
                    //使用默认值
                    if(use) {
                        parameter.setValue(Arrays.asList(field.getValue()));
                    }
                }
                if(Objects.nonNull(parameter.getValue()))
                    kvList.add(parameter);
            }
        }
        return kvList;
    }


    public Map<String, String> getColumnType(String tableGuid, Set<String> columSet) throws AtlasBaseException {
        Map<String, String> columnTypeMap = new HashMap();
        List<Column> columnList = metaDataService.getTableInfoById(tableGuid).getColumns();
        Map<String, String> columnMap = new HashMap<>();
        columnList.forEach(column -> columnMap.put(column.getColumnName(), column.getType()));
        for (String columnName : columSet) {
            if (columnMap.containsKey(columnName)) {
                String type = columnMap.get(columnName);
                if(type.contains("char")) {
                    type = "string";
                } else if(type.contains("smallint")) {
                    type = "int";
                } else if(type.contains("tinyint")) {
                    type = "int";
                } else if(type.contains("decimal")) {
                    type = "decimal";
                }
                columnTypeMap.put(columnName, type);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未知的查询字段");
            }
        }
        return columnTypeMap;
    }

    /**
     * 拼接查询SQL
     * @param tableName
     * @param columnTypeMap
     * @param kvList
     * @param limit
     * @param offset
     * @return
     */
    public String getQuerySQL(String tableName, Map<String, String> columnTypeMap, List<QueryParameter.Parameter> kvList, List<String> queryColumns, Long limit, Long offset, boolean test) throws AtlasBaseException {
        String columnName = null;
        try {
            if(offset<0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "offset取值异常，需大于等于0");
            }
            if(limit<0 && limit!=-1) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "limit取值异常，需大于等于0或未-1");
            }
            if(Objects.isNull(queryColumns) || queryColumns.size()==0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询字段columns不能为空");
            }
            String orderColumnName = queryColumns.get(0);
            StringBuffer querySql = new StringBuffer();
            //StringBuffer countSql = new StringBuffer();
            querySql.append("select ");
            //countSql.append("select count(1) ");
            StringJoiner columnJoiner = new StringJoiner(",");
            queryColumns.stream().forEach(column -> columnJoiner.add(column));
            querySql.append(columnJoiner.toString());
            if(!test) {
                querySql.append(",count(*) over() ");
            }
            querySql.append(" from ");
            querySql.append(tableName);
            //countSql.append(" from ");
            //countSql.append(tableName);
            //过滤条件
            if (Objects.nonNull(kvList) && kvList.size() > 0) {
                querySql.append(" where ");
                //countSql.append(" where ");
                StringJoiner filterJoiner = new StringJoiner(" and ");
                for (QueryParameter.Parameter kv : kvList) {
                    StringBuffer valueBuffer = new StringBuffer();
                    StringJoiner valueJoiner = new StringJoiner(",");
                    columnName = kv.getColumnName();
                    String type = columnTypeMap.get(columnName);
                    DataType dataType = DataType.parseOf(type);

                    List<Object> valueList = kv.getValue();
                    //验证设置取值是否正确
                    if(DataType.TIMESTAMP != dataType && DataType.DATE != dataType &&DataType.TIME!= dataType)
                        valueList.stream().forEach(value -> dataType.valueOf(value).get());
                    if(DataType.BOOLEAN == dataType) {
                        for(Object value : valueList) {
                            if(!value.equals(true) && !value.equals(false) && !value.equals("true") && !value.equals("false") && !value.equals("0") && !value.equals("1")) {
                                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, kv.getColumnName() + "取值需为bool类型");
                            }
                        }
                    }
                    kv.getValue().forEach(value -> {
                        String str = (DataType.STRING == dataType || DataType.DATE == dataType || DataType.TIMESTAMP== dataType || DataType.TIMESTAMP == dataType || "".equals(value.toString()))?("\"" + value.toString() + "\""):(value.toString());
                        valueJoiner.add(str);
                    });

                    if (valueList.size() > 1) {
                        valueBuffer.append(kv.getColumnName()).append(" in ").append("(").append(valueJoiner.toString()).append(")");
                    } else {
                        valueBuffer.append(kv.getColumnName()).append("=").append(valueJoiner.toString());
                    }
                    filterJoiner.add(valueBuffer.toString());
                }
                querySql.append(filterJoiner.toString());
                //countSql.append(filterJoiner.toString());
            }
            //limit
            if (Objects.nonNull(limit) && -1 != limit) {
                querySql.append(" order by " + orderColumnName);
                querySql.append(" limit ");
                querySql.append(limit);
            }
            //offset
            if(Objects.nonNull(offset)) {
                querySql.append(" offset ");
                querySql.append(offset);
            }
            LOG.info("querySQL：" + querySql.toString());
            LOG.info("countSQL：" + querySql.toString());
            /*Map result = new HashMap();
            result.put("query", querySql.toString());
            result.put("count", countSql.toString());*/
            return querySql.toString();
        } catch (NumberFormatException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, columnName + "取值与类型不匹配");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL查询语句异常");
        }
    }

    public List<Column> getTableColumnList(String tableGuid) throws AtlasBaseException {
        List<Column> columnList = metaDataService.getTableInfoById(tableGuid).getColumns();
        columnList.forEach(column -> {
            String columnName = column.getColumnName();
            String displayName = column.getDisplayName();
            if(Objects.isNull(displayName) && "".equals(displayName.trim())) {
                column.setDisplayName(columnName);
            }
        });
        return columnList;
    }
}
