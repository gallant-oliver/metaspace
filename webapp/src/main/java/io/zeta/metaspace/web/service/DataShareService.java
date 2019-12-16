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
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
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
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.result.AddRelationTable;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    public static final String METASPACE_HA_ENABLE = "atlas.server.ha.enabled";
    public static final String METASPACE_HA_ADDRESS = "metaspace.ha.rest.address";
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
    @Autowired
    private DataSourceDAO dataSourceDAO;
    @Autowired
    private DataSourceService dataSourceService;

    Map<String, CompletableFuture> taskMap = new HashMap<>();

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
            //manager
            info.setManager(user);
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
            //使用次数初始化为0
            info.setUsedCount(0);
            //脱敏开关
            info.setDesensitize(info.getDesensitize()==null?false:info.getDesensitize());
            return shareDAO.insertAPIInfo(info);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("创建API信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建API信息失败");
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
            User userInfo = AdminUtils.getUserData();
            String userId = userInfo.getUserId();
            List<String> roleIds = userDAO.getRoleIdByUserId(userId);
            Boolean manage = shareDAO.countManager(guid, userId)==0?false:true;
            if(!manage && (roleIds!=null&&roleIds.contains(SystemRole.ADMIN.getCode()))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户无权限删除此API");
            }
            return shareDAO.deleteAPIInfo(guid);

        } catch (Exception e) {
            LOG.error("删除API失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除API失败");
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
            User userInfo = AdminUtils.getUserData();
            String userId = userInfo.getUserId();
            List<String> roleIds = userDAO.getRoleIdByUserId(userId);
            Boolean manage = shareDAO.countManager(guid, userId)==0?false:true;
            if(!manage && (roleIds!=null&&roleIds.contains(SystemRole.ADMIN.getCode()))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户无权限删除此API");
            }

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
            throw e;
        } catch (Exception e) {
            LOG.error("修改API信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改API信息失败");
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
            APIInfo.SourceType sourceType = APIInfo.SourceType.getSourceTypeByDesc(info.getSourceType());
            Map<String, String> columnName2DisplayMap = new HashMap();
            if(sourceType == APIInfo.SourceType.ORACLE) {
                info.setSourceName(dataSourceDAO.getSourceNameForSourceId(info.getSourceId()));
                info.setTableDisplayName(info.getTableName());
            } else if(sourceType == APIInfo.SourceType.HIVE) {
                String tableGuid = info.getTableGuid();
                Table table = shareDAO.getTableByGuid(tableGuid);
                info.setTableName(table.getTableName());
                info.setDbName(table.getDatabaseName());
                info.setTableDisplayName(StringUtils.isNotEmpty(table.getDisplayName()) ? table.getDisplayName() : table.getTableName());
                String tableDisplayName = columnDAO.getTableDisplayInfoByGuid(tableGuid);
                if(Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                    info.setTableDisplayName(info.getTableName());
                } else {
                    info.setTableDisplayName(tableDisplayName);
                }

                List<Column> columnList = columnDAO.getColumnNameWithDisplayList(info.getTableGuid());
                columnList.forEach(column -> {
                    String columnName = column.getColumnName();
                    String columnDisplay = column.getDisplayName();
                    if(Objects.isNull(columnDisplay) || "".equals(columnDisplay.trim())) {
                        columnName2DisplayMap.put(columnName, columnName);
                    } else {
                        columnName2DisplayMap.put(columnName, columnDisplay);
                    }
                });

            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源类型异常");
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
            for(APIInfo.Field field : fields) {
                APIInfo.FieldWithDisplay fieldWithDisplay = new APIInfo.FieldWithDisplay();
                fieldWithDisplay.setFieldInfo(field);
                String displayName = field.getColumnName();
                if(sourceType == APIInfo.SourceType.HIVE) {
                    String name = columnName2DisplayMap.get(field.getColumnName());
                    displayName = StringUtils.isEmpty(name) ? displayName : name;
                }
                fieldWithDisplay.setDisplayName(displayName);
                fieldsWithDisplay.add(fieldWithDisplay);
            }
            info.setFields(fieldsWithDisplay);
            int count = shareDAO.getStarCount(userId, guid);
            if(count > 0) {
                info.setStar(true);
            } else {
                info.setStar(false);
            }
            info.setEdit(getAPIEditPrivilege(userId, info));
            String keeper = userDAO.getUserName(info.getKeeper());
            info.setKeeper(keeper);
            //updater
            String updater = userDAO.getUserName(info.getUpdater());
            info.setUpdater(updater);
            //manager
            String manger = userDAO.getUserName(info.getManager());
            info.setManager(manger);
            return info;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取API信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取API信息失败");
        }
    }

    public boolean getAPIEditPrivilege(String userId, APIInfo info) {
        if(info.getPublish()) {
            return false;
        }
        if(userId.equals(info.getManager())) {
            return true;
        }
        User user = userDAO.getUserInfo(userId);
        if(user.getRoles()!=null && user.getRoles().contains("1")) {
            return true;
        }
        return false;
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
            List<String> roleIds = userDAO.getRoleIdByUserId(userId);
            boolean enableEditManager = false;
            if(roleIds!=null&&(roleIds.contains(SystemRole.ADMIN.getCode())||roleIds.contains(SystemRole.MANAGE.getCode()))) {
                enableEditManager = true;
            }

            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            String query = parameters.getQuery();
            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            List<APIInfoHeader> list = shareDAO.getAPIList(guid, my, publish, userId, query, limit, offset);
            List<String> starAPIList = shareDAO.getUserStarAPI(userId);
            for(APIInfoHeader header : list) {
                header.setEnableEditManager(enableEditManager);
                if(permissionTableList.contains(header.getTableGuid())) {
                    header.setEnableClone(true);
                } else {
                    header.setEnableClone(false);
                }
                String keeper = userDAO.getUserName(header.getKeeper());
                header.setKeeper(keeper);
                //updater
                String updater = userDAO.getUserName(header.getUpdater());
                header.setUpdater(updater);
                //manager
                User manager = userDAO.getUserInfo(header.getManager());
                if(manager != null) {
                    header.setManager(manager.getUsername());
                    header.setManagerDeleted(manager.getValid() ? false : true);
                } else {
                    header.setManagerDeleted(false);
                }
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
            int apiTotalSize = 0;
            if (list.size()!=0){
                apiTotalSize = list.get(0).getTotal();
            }
            pageResult.setTotalSize(apiTotalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取API列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取API列表失败");
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
            LOG.error("获取数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    public int starAPI(String apiGuid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            return shareDAO.insertAPIStar(userId, apiGuid);
        } catch (Exception e) {
            LOG.error("更新收藏状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新收藏状态失败");
        }
    }

    public int unStarAPI(String apiGuid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            return shareDAO.deleteAPIStar(userId, apiGuid);
        } catch (Exception e) {
            LOG.error("更新收藏状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新收藏状态失败");
        }
    }

    public int publishAPI(List<String> guidList) throws AtlasBaseException {
        try {
            Configuration configuration = ApplicationProperties.get();
            APIContent content = generateAPIContent(guidList);
            Gson gson = new Gson();
            String jsonStr = gson.toJson(content, APIContent.class);
            String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS) + "/svc/create";
            int retryCount = 0;
            String error_id = null;
            String error_reason = null;
            while(retryCount < 3) {
                String res = OKHttpClient.doPost(mobiusURL, jsonStr);
                LOG.info(res);
                if(Objects.nonNull(res)) {
                    Map response = convertMobiusResponse(res);
                    error_id = String.valueOf(response.get("error-id"));
                    error_reason = String.valueOf(response.get("reason"));
                    if ("0.0".equals(error_id)) {
                        break;
                    } else {
                        retryCount++;
                    }
                } else {
                    retryCount++;
                }
            }
            if(!"0.0".equals(error_id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "发布到云平台失败：" + error_reason);
            }

            return shareDAO.updatePublishStatus(guidList, true);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新发布状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }

    public void checkTableStatus(String apiGuid) throws AtlasBaseException {
        String status = shareDAO.getTableStatusByAPIGuid(apiGuid);
        if("DELETED".equals(status.trim())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API关联表已被删除");
        }
    }

    public void checkApiPermission(String apiGuid) throws AtlasBaseException {
        List<String> tableIds = searchService.getUserTableIds();
        if (Objects.isNull(tableIds) || tableIds.size() == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限操作当前API关联表");
        }
        List<String> apiIds = shareDAO.getAPIIdsByRelatedTable(tableIds);
        if (!apiIds.contains(apiGuid)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限操作当前API关联表");
        }
    }

    public Map convertMobiusResponse(String message) {
        Gson gson = new Gson();
        Map response = gson.fromJson(message, Map.class);
        return response;
    }

    public int unpublishAPI(List<String> apiGuidList) throws AtlasBaseException {
        try {
            for(String apiGuid : apiGuidList) {
                checkApiPermission(apiGuid);
            }
            Configuration configuration = ApplicationProperties.get();
            String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS)  + "/svc/delete";
            Map param = new HashMap();
            param.put("api_id_list", apiGuidList);
            Gson gson = new Gson();
            String jsonStr = gson.toJson(param, Map.class);

            int retryCount = 0;
            String error_id = null;
            String error_reason = null;
            while(retryCount < 3) {
                String res = OKHttpClient.doPut(mobiusURL, jsonStr);
                LOG.info(res);
                if(Objects.nonNull(res)) {
                    Map response = convertMobiusResponse(res);
                    error_id = String.valueOf(response.get("error-id"));
                    error_reason = String.valueOf(response.get("reason"));
                    if ("0.0".equals(error_id)) {
                        break;
                    } else {
                        retryCount++;
                    }
                }
            }
            if(!"0.0".equals(error_id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "云平台撤销发布失败：" + error_reason);
            }
            return shareDAO.updatePublishStatus(apiGuidList, false);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新发布状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }

    public APIContent generateAPIContent(List<String> guidList) throws Exception {
        APIContent content = new APIContent();
        List<APIContent.APIDetail> contentList = new ArrayList<>();
        for(String api_id : guidList) {
            APIInfo info = shareDAO.getAPIInfoByGuid(api_id);
            APIInfo.SourceType sourceType = APIInfo.SourceType.getSourceTypeByDesc(info.getSourceType());
            if(sourceType == APIInfo.SourceType.HIVE) {
                checkTableStatus(api_id);
                checkApiPermission(api_id);
                String tableGuid = info.getTableGuid();
                Table table = shareDAO.getTableByGuid(tableGuid);
                info.setTableName(table.getTableName());
                info.setDbName(table.getDatabaseName());
                info.setTableDisplayName(StringUtils.isNotEmpty(table.getDisplayName()) ? table.getDisplayName() : table.getTableName());
            }
            String tableGuid = info.getTableGuid();
            String api_name = info.getName();
            String api_desc = info.getDescription();
            String api_version = info.getVersion();
            List<String> owners = new ArrayList<>();
            User user = userDAO.getUserInfo(info.getManager());
            owners.add(user.getAccount());
            List<APIContent.APIDetail.Organization> organizations = getOrganization(tableGuid);
            String api_catalog = shareDAO.getGroupByAPIGuid(api_id);
            String create_time = info.getGenerateTime();
            String uri = getURL(info);
            String method = info.getRequestMode();
            String upstream_url = getAccessURL() + "/api/metaspace";
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

    public static String getAccessURL() throws Exception {
        Configuration configuration = ApplicationProperties.get();
        Boolean haEnable = configuration.getBoolean(METASPACE_HA_ENABLE, false);
        if(haEnable) {
            return configuration.getString(METASPACE_HA_ADDRESS, getLocalURL());
        } else {
            return getLocalURL();
        }
    }

    public static String getLocalURL() throws AtlasException {
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
            String hostStr = configuration.getString(ATLAS_REST_ADDRESS);
            String[] hostArr = hostStr.split(":");
            String port = hostArr[hostArr.length - 1];
            ipAddrStr += ":" + port;
            return ipAddrStr;
    }

    public String generateSwaggerContent(APIInfo info) throws Exception {
        try {
            String ip = getAccessURL();
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
            LOG.error("创建swagger内容失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建swagger内容失败");
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
        String tableName = null;
        String dbName = null;
        APIInfo.SourceType sourceType = null;
        try {
            if(parameter instanceof HiveQueryParameter) {
                sourceType = APIInfo.SourceType.HIVE;
                String tableGuid = ((HiveQueryParameter) parameter).getTableGuid();
                String tableStatus = shareDAO.getTableStatusByGuid(tableGuid);
                dbName = shareDAO.querydbNameByGuid(tableGuid);
                tableName = shareDAO.queryTableNameByGuid(tableGuid);
                if("DELETED".equals(tableStatus)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API关联表已被删除");
                }
            } else if(parameter instanceof OracleQueryParameter) {
                sourceType = APIInfo.SourceType.ORACLE;
                dbName = ((OracleQueryParameter) parameter).getSchemaName();
                tableName = ((OracleQueryParameter) parameter).getTableName();
            }
            //limit offset
            Long limit = parameter.getLimit();
            Long offset = parameter.getOffset();
            checkLimitAndOffset(limit, offset, parameter.getMaxRowNumber());
            List<QueryParameter.Field> queryColumns = parameter.getQueryFields();
            List<String> queryNameList = queryColumns.stream().map(column -> column.getColumnName()).collect(Collectors.toList());
            List<LinkedHashMap> result = null;
            String querySql = getQuerySql(queryNameList, sourceType);
            String filterSql = getFilterSql(queryColumns, sourceType);
            CompletableFuture<Map> future = null;
            if(sourceType == APIInfo.SourceType.ORACLE) {
                String sourceId = ((OracleQueryParameter) parameter).getSourceId();
                String sql = OracleJdbcUtils.getQuerySql(dbName, tableName, querySql, filterSql, limit, offset);
                future  = CompletableFuture.supplyAsync(() -> {
                    Map resultMap = null;
                    try {
                        resultMap = getOracleQueryResult(sourceId, sql, null);
                    } catch (Exception e) {
                        LOG.error("查询失败", e);
                    }
                    return resultMap;
                });
            } else if(sourceType == APIInfo.SourceType.HIVE) {
                String sql = HiveJdbcUtils.getQuerySql(tableName, querySql, filterSql, limit, offset);
                String db = dbName;
                future = CompletableFuture.supplyAsync(() -> {
                    Map resultMap = null;
                    try {
                        resultMap = getHiveQueryResult(db, sql, true);
                    } catch (Exception e) {
                        LOG.error("查询失败", e);
                    }
                    return resultMap;
                });
            }
            taskMap.put(randomName, future);
            Map resultMap = future.get();
            result = (List<LinkedHashMap>)resultMap.get("queryResult");
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("API接口查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    public void checkLimitAndOffset(Long limit, Long offset, Long maxRowNumber) throws AtlasBaseException {
        if(Objects.isNull(limit) || Objects.isNull(offset)) {
            throw new AtlasBaseException("limit和offset不允许为空");
        }
        limit = Objects.nonNull(limit)?Math.min(limit, maxRowNumber):maxRowNumber;
        offset = Objects.nonNull(offset)?offset:0;
        if(offset<0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "offset取值异常，需大于等于0");
        }
        if(limit<0 && limit!=-1) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "limit取值异常，需大于等于0或未-1");
        }
    }

    public String getQuerySql(List<String> queryColumns, APIInfo.SourceType searchType) {
        StringJoiner columnJoiner = new StringJoiner(",");
        if(searchType == APIInfo.SourceType.ORACLE) {
            queryColumns.stream().forEach(columnName -> columnJoiner.add("\"" + columnName + "\""));
        } else if(searchType == APIInfo.SourceType.HIVE) {
            queryColumns.stream().forEach(columnName -> columnJoiner.add(columnName));
        }
        return columnJoiner.toString();
    }

    public String getFilterSql(List<QueryParameter.Field> queryColumns, APIInfo.SourceType sourceType) throws AtlasBaseException {
        String columnName = null;
        StringJoiner filterJoiner = new StringJoiner(" and ");
        List<QueryParameter.Field> filterColumns = queryColumns.stream().filter(column -> column.getFilter()).collect(Collectors.toList());
        for(QueryParameter.Field field : filterColumns) {
            StringBuffer filterBuffer = new StringBuffer();
            StringJoiner valueJoiner = new StringJoiner(",");
            columnName = field.getColumnName();
            List<Object> valueList = field.getValueList();
            String columnType = field.getType();
            DataType dataType = DataType.convertType(columnType.toUpperCase());
            checkDataType(dataType, valueList);
            valueList.forEach(value -> {
                String str = (DataType.STRING == dataType || DataType.DATE == dataType || DataType.TIMESTAMP== dataType || DataType.TIMESTAMP == dataType || "".equals(value.toString()))?("\'" + value.toString() + "\'"):(value.toString());
                valueJoiner.add(str);
            });
            if(sourceType == APIInfo.SourceType.ORACLE) {
                if (valueList.size() > 1) {
                    filterBuffer.append("\"").append(columnName).append("\"").append(" in ").append("(").append(valueJoiner.toString()).append(")");
                } else {
                    filterBuffer.append("\"").append(columnName).append("\"").append("=").append(valueJoiner.toString());
                }
            } else if(sourceType == APIInfo.SourceType.HIVE) {
                if (valueList.size() > 1) {
                    filterBuffer.append(columnName).append(" in ").append("(").append(valueJoiner.toString()).append(")");
                } else {
                    filterBuffer.append(columnName).append("=").append(valueJoiner.toString());
                }
            }
            filterJoiner.add(filterBuffer.toString());
        }
        return filterJoiner.toString();
    }

    public void checkDataType(DataType dataType, List<Object> valueList) throws AtlasBaseException {
        if(DataType.TIMESTAMP != dataType && DataType.DATE != dataType &&DataType.TIME!= dataType)
            valueList.stream().forEach(value -> dataType.valueOf(value).get());
        else if(DataType.BOOLEAN == dataType) {
            for(Object value : valueList) {
                if(!value.equals(true) && !value.equals(false) && !value.equals("true") && !value.equals("false") && !value.equals("0") && !value.equals("1")) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取值需为bool类型");
                }
            }
        } else if(DataType.UNKNOWN == dataType) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持的数据类型");
        }
    }

    public Map getOracleQueryResult(String sourceId, String querySql, String countSql) throws AtlasBaseException {
        Map resultMap = new HashMap();
        try {
            Connection conn = dataSourceService.getConnection(sourceId);
            ResultSet resultSet = OracleJdbcUtils.query(conn, querySql);
            List<LinkedHashMap> result = extractResultSetData(resultSet);
            resultMap.put("queryResult", result);
            if(countSql != null) {
                ResultSet countSet = OracleJdbcUtils.query(conn, countSql);
                while(countSet.next()) {
                    Long total = countSet.getLong(1);
                    resultMap.put("queryCount", total);
                }
            }
            return resultMap;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public Map getHiveQueryResult(String dbName, String querySql, Boolean test) throws AtlasBaseException {
        Map resultMap = new HashMap();
        Connection conn = null;
        long count = 0;
        try {
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
                    if(i != columnCount) {
                        if(test) {
                            map.put(columnName, String.valueOf(value));
                        } else {
                            map.put(columnName, value);
                        }
                    } else if(!test && i==columnCount) {
                        if(Objects.nonNull(value)) {
                            count = Long.parseLong(value.toString());
                        } else {
                            count = (long)map.size();
                        }
                        continue;
                    }
                }
                result.add(map);
            }
            resultMap.put("queryResult", result);
            resultMap.put("queryCount", count);
            return resultMap;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    /**
     * 取消查询线程
     * @param name
     * @throws AtlasBaseException
     */
    public void cancelAPIThread(String name) throws AtlasBaseException {
        try {
            CompletableFuture<Map> future = taskMap.get(name);
            future.cancel(true);
        } catch (Exception e) {
            LOG.error("任务取消失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务取消失败");
        }
    }

    @HystrixCommand()
    public QueryResult queryAPIData(String path, QueryInfo queryInfo, String acceptHeader) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfo(path);
            //判断是否查询到API信息
            if(Objects.isNull(info)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "路径错误，未查询到相关API信息");
            }
            //判断是否已发布
            Boolean publish = info.getPublish();
            if(Objects.isNull(publish)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API发布情况未知");
            }
            if (Objects.nonNull(publish) && !publish) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API未发布");
            }
            //API请求类型：Hive/Oracle
            APIInfo.SourceType sourceType = APIInfo.SourceType.getSourceTypeByDesc(info.getSourceType());
            //获取dbName和tableName
            String tableName = null;
            String dbName = null;
            if(sourceType == APIInfo.SourceType.HIVE) {
                String tableGuid = info.getTableGuid();
                tableName = shareDAO.queryTableNameByGuid(tableGuid);
                dbName = shareDAO.querydbNameByGuid(tableGuid);
            } else if(sourceType == APIInfo.SourceType.ORACLE) {
                tableName = info.getTableName();
                dbName = info.getSchemaName();
            }
            //查询数据分享API字段详情
            Object fieldsObject = shareDAO.getAPIFields(path);
            PGobject pGobject = (PGobject) fieldsObject;
            String valueObject = pGobject.getValue();
            Gson gson = new Gson();
            List<Map> fieldMap = gson.fromJson(valueObject, List.class);
            Boolean desensitize = info.getDesensitize()==null?false:info.getDesensitize();
            //请求中查询字段
            List<String> queryColumnList = queryInfo.getColumns();
            //请求中过滤字段
            Map filterColumnMap = queryInfo.getFilters();
            List<QueryParameter.Field> infoFieldList = buildInfoField(fieldMap, filterColumnMap);
            Set<String> infoColumnSet = new HashSet<>();
            Set<String> filterColumnSet = new HashSet<>();
            Set<String> sensitiveColumnSet = new HashSet<>();
            for (QueryParameter.Field field : infoFieldList) {
                String columnName = field.getColumnName();
                infoColumnSet.add(columnName);
                if(field.getFilter()) {
                    filterColumnSet.add(columnName);
                }
                if(field.getSensitive()!=null && field.getSensitive()) {
                    sensitiveColumnSet.add(columnName);
                }
            }
            //检查请求中查询字段是否包含于详情中字段
            checkFieldName(infoColumnSet, queryColumnList);
            //校验过滤字段范围
            checkFilterField(filterColumnSet, filterColumnMap);
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
            String querySql = getQuerySql(queryColumnList, sourceType);
            String filterSql = getFilterSql(infoFieldList, sourceType);
            Map resultMap = null;
            //sql
            if(sourceType == APIInfo.SourceType.HIVE) {
                String sql = HiveJdbcUtils.getQuerySql(tableName, querySql, filterSql, limit, offset);
                String db = dbName;
                resultMap = getHiveQueryResult(db, sql, false);
            } else if(sourceType == APIInfo.SourceType.ORACLE){
                String sourceId = info.getSourceId();
                String query = OracleJdbcUtils.getQuerySql(dbName, tableName, querySql, filterSql, limit, offset);
                String count = OracleJdbcUtils.getCountSql(dbName, tableName, filterSql);
                resultMap = getOracleQueryResult(sourceId, query, count);
            }
            List<LinkedHashMap<String,Object>> queryDataList = (List<LinkedHashMap<String,Object>>)resultMap.get("queryResult");
            if(desensitize) {
                processSensitiveData(sensitiveColumnSet, queryDataList);
            }
            Long count = Long.parseLong(resultMap.get("queryCount").toString());
            //数据格式转换
            if(acceptHeader.contains("xml")) {
                XmlQueryResult queryResult = new XmlQueryResult();
                XmlQueryResult.QueryData queryData = new XmlQueryResult.QueryData();
                queryData.setData(queryDataList);
                queryResult.setTotalCount(count);
                queryResult.setDatas(queryData);
                shareDAO.updateUsedCount(path);
                return queryResult;
            } else {
                JsonQueryResult queryResult = new JsonQueryResult();
                queryResult.setDatas(queryDataList);
                queryResult.setTotalCount(count);
                shareDAO.updateUsedCount(path);
                return queryResult;
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST , "查询失败");
        }
    }

    public void processSensitiveData(Set<String>  sensitiveColumnSet, List<LinkedHashMap<String,Object>> dataList) {
        for(LinkedHashMap<String,Object> data : dataList) {
            for(String key : data.keySet()) {
                if(sensitiveColumnSet.contains(key)) {
                    String value = data.get(key).toString();
                    int length = value.length();
                    int replaceLen = length/3;
                    StringJoiner joiner = new StringJoiner("");
                    String tmp = "";
                    if(replaceLen != 0) {
                        tmp = value.substring(0, length-replaceLen);
                    } else {
                        replaceLen = length;
                    }
                    for(int i=0; i<replaceLen; i++) {
                        joiner.add("*");
                    }
                    value = tmp + joiner.toString();
                    data.put(key, value);
                }
            }
        }
    }

    public List<QueryParameter.Field> buildInfoField(List<Map> fieldList, Map<String, Object> filterColumnMap) throws AtlasBaseException {
        List<QueryParameter.Field> resultFieldList = new ArrayList<>();
        QueryParameter.Field field = null;
        List<Object> valueList = null;
        for (Map map : fieldList) {
            String columnName = map.get("columnName").toString();
            Boolean filter = Boolean.parseBoolean(map.get("filter").toString());
            String type = map.get("type").toString();
            Boolean sensitive = Boolean.parseBoolean(map.get("sensitive")==null?Boolean.FALSE.toString():map.get("sensitive").toString());
            if(filter) {
                Boolean fill = Boolean.parseBoolean(map.get("fill").toString());
                String defaultValue = Objects.nonNull(map.get("defaultValue"))?map.get("defaultValue").toString():String.valueOf("");
                Boolean useDefault = Objects.nonNull(map.get("useDefaultValue"))?Boolean.parseBoolean(map.get("useDefaultValue").toString()):false;
                //必传值
                if(fill) {
                    if(filterColumnMap.containsKey(columnName)) {
                        Object value = filterColumnMap.get(columnName);
                         if(value instanceof List) {
                            valueList = (List<Object>)value;
                        } else {
                            valueList = new ArrayList<>();
                            valueList.add(value);
                        }
                    } else {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "请求失败，请在请求参数中给出必传过滤字段:" + columnName);
                    }
                } else {
                    //已传值
                    if(filterColumnMap.containsKey(columnName)) {
                        Object value = filterColumnMap.get(columnName);
                        if(value instanceof List) {
                            valueList = (List<Object>)value;
                        } else {
                            valueList = new ArrayList<>();
                            valueList.add(value);
                        }
                        //未传值
                    } else {
                        //使用默认值
                        if(useDefault) {
                            valueList = Arrays.asList(defaultValue);
                        } else {
                            filter = false;
                        }
                    }
                }
            }
            field = new QueryParameter.Field(columnName, type, filter, valueList , sensitive);
            resultFieldList.add(field);
        }
        return resultFieldList;
    }

    /**
     * 查询字段是否在允许范围内
     * @param infoFields
     * @param queryFields
     * @throws AtlasBaseException
     */
    public void checkFieldName(Set<String> infoFields, List<String> queryFields) throws AtlasBaseException {
        for(String field : queryFields) {
            if(!infoFields.contains(field)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的查询字段:" + field);
            }
        }
    }

    /**
     * 查询过滤字段是否在创建API信息中
     * @param filterFields
     * @param queryFilterFields
     * @throws AtlasBaseException
     */
    public void checkFilterField(Set<String> filterFields, Map queryFilterFields) throws AtlasBaseException {
        for(Object field : queryFilterFields.keySet()) {
            if(!filterFields.contains(field.toString())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的过滤查询字段:" + field.toString());
            }
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

    public PageResult getUserList(Parameters parameters) throws AtlasBaseException {
        PageResult pageResult = new PageResult();
        try {
            List<User> userList = userDAO.getUserList(null, parameters.getLimit(), parameters.getOffset());
            pageResult.setLists(userList);
            long userTotalSize = 0;
            if (userList.size()!=0){
                userTotalSize = userList.get(0).getTotal();
            }
            pageResult.setCurrentSize(userList.size());
            pageResult.setTotalSize(userTotalSize);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取用户列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户列表失败");
        }
    }

    public void updateManager(String apiGuid, String userId) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfoByGuid(apiGuid);
            if(true == info.getPublish()) {
                List<APIInfoHeader> apiList = new ArrayList<>();
                APIInfoHeader infoHeader = new APIInfoHeader();
                infoHeader.setGuid(apiGuid);
                User userInfo = userDAO.getUserInfo(userId);
                infoHeader.setManager(userInfo.getAccount());
                apiList.add(infoHeader);
                List<TableOwner.Owner> tableOwners = shareDAO.getOwnerList(apiGuid);
                dataManageService.sendToMobius(apiList, tableOwners);
            }
            shareDAO.updateManager(apiGuid, userId);
        } catch (Exception e) {
            LOG.error("更新管理者失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新管理者失败");
        }
    }

    public PageResult getOracleDataSourceList(Parameters parameters) throws AtlasBaseException {
        return dataSourceService.searchDataSources(parameters.getLimit(),parameters.getOffset(),null,null,null,null,null,null,null,true);
    }

    public PageResult getDataList(SEARCH_TYPE searchType, Parameters parameters, String sourceId, String... ids) throws AtlasBaseException {
        PageResult pageResult = new PageResult();
        ResultSet dataSet = null;
        ResultSet countSet = null;
        List<LinkedHashMap> result = null;
        try {
            Connection conn = dataSourceService.getConnection(sourceId);
            switch (searchType) {
                case SCHEMA: {
                    dataSet = OracleJdbcUtils.getSchemaList(conn, parameters.getLimit(), parameters.getOffset());
                    countSet = OracleJdbcUtils.getSchemaCount(conn);
                    break;
                }
                case TABLE: {
                    String schemaName = ids[0];
                    dataSet = OracleJdbcUtils.getTableList(conn, schemaName, parameters.getLimit(), parameters.getOffset());
                    countSet = OracleJdbcUtils.getTableCount(conn, schemaName);
                    break;
                }
                case COLUMN: {
                    String schemaName = ids[0];
                    String tableName = ids[1];
                    dataSet = OracleJdbcUtils.getColumnList(conn, schemaName, tableName, parameters.getLimit(), parameters.getOffset());
                    countSet = OracleJdbcUtils.getColumnCount(conn, schemaName, tableName);
                    break;
                }
            }
            result = extractResultSetData(dataSet);
            long totalSize = extractSizeData(countSet);
            pageResult.setLists(result);
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(result.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        }
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

    public enum SEARCH_TYPE {
        SCHEMA, TABLE, COLUMN
    }
}


