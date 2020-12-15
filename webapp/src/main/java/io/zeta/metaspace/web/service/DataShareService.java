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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.*;
import io.swagger.util.Yaml;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.apigroup.ApiGroupInfo;
import io.zeta.metaspace.model.apigroup.ApiVersion;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.model.datasource.DataSourceTypeInfo;
import io.zeta.metaspace.model.desensitization.ApiDesensitization;
import io.zeta.metaspace.model.desensitization.DesensitizationRule;
import io.zeta.metaspace.model.ip.restriction.ApiIpRestriction;
import io.zeta.metaspace.model.ip.restriction.IpRestriction;
import io.zeta.metaspace.model.ip.restriction.IpRestrictionType;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.AddRelationTable;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.security.Pool;
import io.zeta.metaspace.model.security.Queue;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.utils.SqlBuilderUtils;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 19:56
 */
@SuppressWarnings("CheckStyle")
@Service
public class DataShareService {

    private static final Logger LOG = LoggerFactory.getLogger(DataShareService.class);

    public static final String METASPACE_MOBIUS_ADDRESS = "metaspace.mobius.url";
    private static String engine;
    private static Configuration conf;

    @Autowired
    ApiGroupDAO apiGroupDAO;
    @Autowired
    ApiGroupService groupService;
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
    @Autowired
    private TenantService tenantService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private ApiGroupService apiGroupService;
    @Autowired
    private ApiPolyDao apiPolyDao;
    @Autowired
    private DesensitizationDAO desensitizationDAO;
    @Autowired
    private IpRestrictionDAO ipRestrictionDAO;

    Map<String, CompletableFuture> taskMap = new HashMap<>();

    ExecutorService pool = Executors.newFixedThreadPool(100);

    static {
        try {
            conf = ApplicationProperties.get();
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public int insertAPIInfo(APIInfo info, String tenantId) throws AtlasBaseException {
        try {
            boolean same = querySameName(info.getName(), tenantId);
            if (same) {
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
            String path = pathList[pathList.length - 1];
            info.setPath(path);
            int count = shareDAO.samePathCount(path);
            if (count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "重复路径");
            }
            //使用次数初始化为0
            info.setUsedCount(0);
            //脱敏开关
            info.setDesensitize(info.getDesensitize() == null ? false : info.getDesensitize());
            return shareDAO.insertAPIInfo(info, tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("创建API信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建API信息失败");
        }
    }

    /**
     * 查询同名
     *
     * @param name
     * @return
     */
    public boolean querySameName(String name, String tenantId) {
        return shareDAO.querySameName(name, tenantId) == 0 ? false : true;
    }

    /**
     * 删除API
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    public int deleteAPIInfo(String guid) throws AtlasBaseException {
        try {
            User userInfo = AdminUtils.getUserData();
            String userId = userInfo.getUserId();
            Boolean manage = shareDAO.countManager(guid, userId) == 0 ? false : true;
            if (!manage) {
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
     *
     * @param guid
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public int updateAPIInfo(String guid, APIInfo info, String tenantId) throws AtlasBaseException {
        try {
            User userInfo = AdminUtils.getUserData();
            String userId = userInfo.getUserId();
            List<String> userGroups = userDAO.getUserGroupIdByUser(userId, tenantId);
            Boolean manage = shareDAO.countManager(guid, userId) == 0 ? false : true;
            if (!manage) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户无权限删除此API");
            }

            String apiName = info.getName();
            APIInfo currentAPI = shareDAO.getAPIInfoByGuid(guid);
            int count = shareDAO.querySameName(apiName, tenantId);
            if (Objects.isNull(currentAPI)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到当前API信息");
            }
            if (count > 0 && !currentAPI.getName().equals(apiName)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字的API");
            }
            String[] pathList = info.getPath().split("/");
            String path = pathList[pathList.length - 1];
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
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    public APIInfo getAPIInfo(String guid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            APIInfo info = shareDAO.getAPIInfoByGuid(guid);
            if (Objects.isNull(info)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到API信息");
            }

            DataSourceType sourceType = DataSourceType.getType(info.getSourceType());
            Map<String, String> columnName2DisplayMap = new HashMap();
            if (sourceType.isBuildIn()) {
                String tableGuid = info.getTableGuid();
                Table table = shareDAO.getTableByGuid(tableGuid);
                info.setTableName(table.getTableName());
                info.setDbName(table.getDatabaseName());
                info.setTableDisplayName(StringUtils.isNotEmpty(table.getDisplayName()) ? table.getDisplayName() : table.getTableName());
                String tableDisplayName = columnDAO.getTableDisplayInfoByGuid(tableGuid);
                if (Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                    info.setTableDisplayName(info.getTableName());
                } else {
                    info.setTableDisplayName(tableDisplayName);
                }

                List<Column> columnList = columnDAO.getColumnNameWithDisplayList(info.getTableGuid());
                columnList.forEach(column -> {
                    String columnName = column.getColumnName();
                    String columnDisplay = column.getDisplayName();
                    if (Objects.isNull(columnDisplay) || "".equals(columnDisplay.trim())) {
                        columnName2DisplayMap.put(columnName, columnName);
                    } else {
                        columnName2DisplayMap.put(columnName, columnDisplay);
                    }
                });
            } else {
                info.setSourceName(dataSourceDAO.getSourceNameForSourceId(info.getSourceId()));
                info.setTableDisplayName(info.getTableName());
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
            if (Objects.nonNull(dataOwner) && dataOwner.size() > 0) {
                dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
            }
            info.setDataOwner(dataOwnerName);
            List<APIInfo.Field> fieldsWithDisplay = new ArrayList<>();
            for (APIInfo.Field field : fields) {
                APIInfo.FieldWithDisplay fieldWithDisplay = new APIInfo.FieldWithDisplay();
                fieldWithDisplay.setFieldInfo(field);
                String displayName = field.getColumnName();
                if (sourceType.isBuildIn()) {
                    String name = columnName2DisplayMap.get(field.getColumnName());
                    displayName = StringUtils.isEmpty(name) ? displayName : name;
                }
                fieldWithDisplay.setDisplayName(displayName);
                fieldsWithDisplay.add(fieldWithDisplay);
            }
            info.setFields(fieldsWithDisplay);
            int count = shareDAO.getStarCount(userId, guid);
            if (count > 0) {
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
        if (info.getPublish()) {
            return false;
        }
        if (userId.equals(info.getManager())) {
            return true;
        }
        User user = userDAO.getUserInfo(userId);
        if (user.getRoles() != null && user.getRoles().contains(SystemRole.ADMIN.getCode())) {
            return true;
        }
        return false;
    }

    /**
     * 获取API列表
     *
     * @param guid
     * @param my
     * @param publish
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<APIInfoHeader> getAPIList(String guid, Integer my, String publish, Parameters parameters, String tenantId) throws AtlasBaseException {
        try {
            Parameters tablePara = new Parameters();
            tablePara.setLimit(-1);
            tablePara.setOffset(0);
            tablePara.setQuery("");
            PageResult<AddRelationTable> tablePageResult = searchService.getPermissionTablePageResultV2(tablePara, tenantId);
            List<String> permissionTableList = new ArrayList<>();
            if (Objects.nonNull(tablePageResult)) {
                List<AddRelationTable> tableList = tablePageResult.getLists();
                tableList.stream().forEach(table -> permissionTableList.add(table.getTableId()));
            }
            String userId = AdminUtils.getUserData().getUserId();

            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            String query = parameters.getQuery();
            if (Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            List<APIInfoHeader> list = shareDAO.getAPIList(guid, my, publish, userId, query, limit, offset, tenantId);
            List<String> starAPIList = shareDAO.getUserStarAPI(userId, tenantId);
            for (APIInfoHeader header : list) {
                header.setEnableEditManager(userId.equals(header.getManager()));
                if (permissionTableList.contains(header.getTableGuid())) {
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
                if (manager != null) {
                    header.setManager(manager.getUsername());
                    header.setManagerDeleted(manager.getValid() ? false : true);
                } else {
                    header.setManagerDeleted(false);
                }
                if (starAPIList.contains(header.getGuid())) {
                    header.setStar(true);
                } else {
                    header.setStar(false);
                }
                List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(header.getTableGuid());
                List<String> dataOwnerName = new ArrayList<>();
                if (Objects.nonNull(dataOwner) && dataOwner.size() > 0) {
                    dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
                }
                header.setDataOwner(dataOwnerName);
            }
            int apiTotalSize = 0;
            if (list.size() != 0) {
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
            PGobject pGobject = (PGobject) fields;
            String value = pGobject.getValue();
            Type type = new TypeToken<List<APIInfo.Field>>() {
            }.getType();
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

    public int publishAPI(List<String> guidList, String tenantId) throws Exception {
        APIContent content = generateAPIContent(guidList, tenantId);
        Gson gson = new Gson();
        String jsonStr = gson.toJson(content, APIContent.class);
        String mobiusURL = conf.getString(METASPACE_MOBIUS_ADDRESS) + "/svc/create";
        int retryCount = 0;
        String errorId = null;
        String errorReason = null;
        int retries = 3;
        String proper = "0.0";
        while (retryCount < retries) {
            String res = OKHttpClient.doPost(mobiusURL, jsonStr);
            LOG.info(res);
            if (Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }

        if (!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "发布到云平台失败");
        }

        return shareDAO.updatePublishStatus(guidList, true);
    }

    public void checkTableStatus(String apiGuid) throws AtlasBaseException {
        String status = shareDAO.getTableStatusByAPIGuid(apiGuid);
        String deletedStatus = "DELETED";
        if (deletedStatus.equals(status.trim())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API关联表已被删除");
        }
    }

    public void checkApiPermission(String apiGuid, String tenantId) throws AtlasBaseException {
        List<String> tableIds = searchService.getUserTableIds(tenantId);
        if (Objects.isNull(tableIds) || tableIds.size() == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限操作当前API关联表");
        }
        List<String> apiIds = shareDAO.getAPIIdsByRelatedTable(tableIds, tenantId);
        if (!apiIds.contains(apiGuid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限操作当前API关联表");
        }
    }

    public Map convertMobiusResponse(String message) {
        Gson gson = new Gson();
        Map response = gson.fromJson(message, Map.class);
        return response;
    }

    public int unpublishAPI(List<String> apiGuidList, String tenantId) throws AtlasBaseException, AtlasException {
        for (String apiGuid : apiGuidList) {
            APIInfo info = shareDAO.getAPIInfoByGuid(apiGuid);
            DataSourceType sourceType = DataSourceType.getType(info.getSourceType());
            if (sourceType == DataSourceType.HIVE) {
                checkTableStatus(apiGuid);
                checkApiPermission(apiGuid, tenantId);
            }
        }

        String mobiusURL = conf.getString(METASPACE_MOBIUS_ADDRESS) + "/svc/delete";
        Map param = new HashMap();
        param.put("api_id_list", apiGuidList);
        Gson gson = new Gson();
        String jsonStr = gson.toJson(param, Map.class);

        int retryCount = 0;
        String errorId = null;
        String errorReason = null;
        int retries = 3;
        String proper = "0.0";
        while (retryCount < retries) {
            String res = OKHttpClient.doPut(mobiusURL, jsonStr);
            LOG.info(res);
            if (Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    break;
                } else {
                    retryCount++;
                }
            }
        }
        if (!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "云平台撤销发布失败");
        }
        return shareDAO.updatePublishStatus(apiGuidList, false);
    }

    public APIContent generateAPIContent(List<String> guidList, String tenantId) throws Exception {
        APIContent content = new APIContent();
        List<APIContent.APIDetail> contentList = new ArrayList<>();
        for (String apiId : guidList) {
            APIInfo info = shareDAO.getAPIInfoByGuid(apiId);
            DataSourceType sourceType = DataSourceType.getType(info.getSourceType());
            if (sourceType == DataSourceType.HIVE) {
                checkTableStatus(apiId);
                checkApiPermission(apiId, tenantId);
                String tableGuid = info.getTableGuid();
                Table table = shareDAO.getTableByGuid(tableGuid);
                info.setTableName(table.getTableName());
                info.setDbName(table.getDatabaseName());
                info.setTableDisplayName(StringUtils.isNotEmpty(table.getDisplayName()) ? table.getDisplayName() : table.getTableName());
            }
            String tableGuid = info.getTableGuid();
            String apiName = info.getName();
            String apiDesc = info.getDescription();
            String apiVersion = info.getVersion();
            List<String> owners = new ArrayList<>();
            User user = userDAO.getUserInfo(info.getManager());
            owners.add(user.getAccount());
            List<APIContent.APIDetail.Organization> organizations = getOrganization(tableGuid);
            String apiCatalog = shareDAO.getGroupByAPIGuid(apiId);
            String createTime = info.getGenerateTime();
            String uri = getURL(info);
            String method = info.getRequestMode();
            String upstreamUrl = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace";
            String swaggerContent = generateSwaggerContent(info);
            APIContent.APIDetail detail = new APIContent.APIDetail(apiId, apiName, apiDesc, apiVersion, owners, organizations, apiCatalog, createTime, uri, method, upstreamUrl, swaggerContent);
            contentList.add(detail);
        }
        content.setApis_detail(contentList);
        return content;
    }

    public List<APIContent.APIDetail.Organization> getOrganization(String guid) throws AtlasBaseException {
        List<APIContent.APIDetail.Organization> list = new ArrayList<>();
        //pkId
        List<String> owners = metaDataService.getDataOwnerId(guid);
        for (String owner : owners) {
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

    public static String getMetaspaceHost() {
        String url = MetaspaceConfig.getMetaspaceUrl();
        return url.substring(url.indexOf("://") + 3, url.length());
    }

    public static Scheme getRequestMode() {
        String url = MetaspaceConfig.getMetaspaceUrl();
        String prefix = url.substring(0, url.indexOf("://"));
        String https = "https";
        if (https.equals(prefix)) {
            return Scheme.HTTPS;
        }
        return Scheme.HTTP;
    }

    public String generateSwaggerContent(APIInfo info) throws Exception {
        try {
            String host = getMetaspaceHost();
            Swagger swagger = new Swagger();
            swagger.setHost(host);
            //basePath
            swagger.setBasePath("/api/metaspace");
            //scheme
            swagger.setSchemes(Collections.singletonList(getRequestMode()));
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
                    if (filter) {
                        BooleanProperty filterColumnProperty = new BooleanProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if (userDefaultValue) {
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
                    if (filter) {
                        IntegerProperty filterColumnProperty = new IntegerProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if (userDefaultValue) {
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
                    if (filter) {
                        DoubleProperty filterColumnProperty = new DoubleProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if (userDefaultValue) {
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

                    if (filter) {
                        FloatProperty filterColumnProperty = new FloatProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if (userDefaultValue) {
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
                    if (filter) {
                        StringProperty filterColumnProperty = new StringProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if (userDefaultValue) {
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
            if (Objects.nonNull(max)) {
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(content);
            }
            return content;
        } catch (NumberFormatException e) {
            LOG.error("创建swagger内容失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建swagger内容失败");
        }
    }

    /**
     * @param randomName
     * @param parameter
     * @return
     * @throws AtlasBaseException
     */
    public List<LinkedHashMap> testAPI(String randomName, QueryParameter parameter) throws AtlasBaseException {
        //limit offset
        Long limit = parameter.getLimit();
        Long offset = parameter.getOffset();
        checkLimitAndOffset(limit, offset, parameter.getMaxRowNumber());

        String tableName = null;
        String dbName = null;
        DataSourceType sourceType = null;
        AdapterSource adapterSource = null;
        AdapterTransformer transformer = null;
        try {
            if (parameter instanceof HiveQueryParameter) {
                sourceType = DataSourceType.HIVE;
                String tableGuid = ((HiveQueryParameter) parameter).getTableGuid();
                String tableStatus = shareDAO.getTableStatusByGuid(tableGuid);
                dbName = shareDAO.querydbNameByGuid(tableGuid);
                tableName = shareDAO.queryTableNameByGuid(tableGuid);
                String deletedStatus = "DELETED";
                if (deletedStatus.equals(tableStatus)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API关联表已被删除");
                }
                engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
                if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                    adapterSource = AdapterUtils.getImpalaAdapterSource();
                } else {
                    adapterSource = AdapterUtils.getHiveAdapterSource();
                }
                transformer = AdapterUtils.getAdapter(sourceType.getName()).getAdapterTransformer();
            } else {
                String sourceId = ((RelationalQueryParameter) parameter).getSourceId();
                dbName = ((RelationalQueryParameter) parameter).getSchemaName();
                tableName = ((RelationalQueryParameter) parameter).getTableName();

                adapterSource = dataSourceService.getAdapterSource(sourceId);
                sourceType = DataSourceType.getType(adapterSource.getDataSourceInfo().getSourceType());
                transformer = adapterSource.getAdapter().getAdapterTransformer();
            }

            List<QueryParameter.Field> queryColumns = parameter.getQueryFields();
            String querySql = queryColumns.stream().map(QueryParameter.Field::getColumnName).map(transformer::caseSensitive).collect(Collectors.joining(","));
            String filterSql = getFilterSql(queryColumns, transformer);
            CompletableFuture<List<LinkedHashMap<String, Object>>> future = null;
            String sql = SqlBuilderUtils.buildQuerySql(transformer, dbName, tableName, querySql, filterSql, null, limit, offset);

            AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();

            String pool = parameter.getPool();
            Connection connection = adapterSource.getConnection(MetaspaceConfig.getHiveAdmin(), dbName, pool);
            future = CompletableFuture.supplyAsync(() -> {
                try {
                    return adapterExecutor.queryResult(connection, sql);
                } catch (Exception e) {
                    LOG.error("查询失败", e);
                }
                return null;
            });
            taskMap.put(randomName, future);
            List<LinkedHashMap> result = new ArrayList<>(future.get());
            taskMap.remove(randomName);
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("API接口查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    public void checkDataType(List<QueryParameter.Field> fields) throws AtlasBaseException {
        StringJoiner joiner = new StringJoiner(",");
        for (QueryParameter.Field field : fields) {
            String type = field.getType().toUpperCase();
            if ("BLOB".equals(type) || "BFILE".equals(type) || "BINARY".equals(type)) {
                joiner.add(field.getColumnName() + ":" + field.getType());
            }
        }
        if (!joiner.toString().isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持展示的数据字段及类型：" + "[" + joiner.toString() + "]");
        }

    }

    public void checkLimitAndOffset(Long limit, Long offset, Long maxRowNumber) throws AtlasBaseException {
        if (Objects.isNull(limit) || Objects.isNull(offset)) {
            throw new AtlasBaseException("limit和offset不允许为空");
        }
        limit = Objects.nonNull(limit) ? Math.min(limit, maxRowNumber) : maxRowNumber;
        offset = Objects.nonNull(offset) ? offset : 0;
        if (offset < 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "offset取值异常，需大于等于0");
        }
        if (limit < 0 && limit != -1) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "limit取值异常，需大于等于0或未-1");
        }
    }

    public String getFilterSql(List<QueryParameter.Field> queryColumns, AdapterTransformer transformer) throws AtlasBaseException {
        String columnName = null;
        StringJoiner filterJoiner = new StringJoiner(" and ");
        List<QueryParameter.Field> filterColumns = queryColumns.stream().filter(QueryParameter.Field::getFilter).collect(Collectors.toList());
        for (QueryParameter.Field field : filterColumns) {
            String columnType = field.getType();
            DataType dataType = DataType.convertType(columnType.toUpperCase());
            columnName = field.getColumnName();
            checkDataType(dataType, field.getValueList());
            List<String> valueList = field.getValueList().stream().map(Object::toString).map(str -> {
                if (Arrays.asList(DataType.STRING, DataType.CLOB, DataType.DATE, DataType.TIMESTAMP, DataType.TIME).contains(dataType) || "".equals(str)) {
                    return String.format("'%s'", str);
                }
                return str;
            }).collect(Collectors.toList());
            filterJoiner.add(SqlBuilderUtils.getFilterConditionStr(transformer, dataType, columnName, valueList));
        }
        return filterJoiner.toString();
    }

    public void checkDataType(DataType dataType, List<Object> valueList) throws AtlasBaseException {
        if (DataType.TIMESTAMP != dataType && DataType.DATE != dataType && DataType.TIME != dataType && DataType.CLOB != dataType)
            valueList.stream().forEach(value -> dataType.valueOf(value).get());
        else if (DataType.BOOLEAN == dataType) {
            for (Object value : valueList) {
                if (!value.equals(true) && !value.equals(false) && !value.equals("true") && !value.equals("false") && !value.equals("0") && !value.equals("1")) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取值需为bool类型");
                }
            }
        } else if (DataType.UNKNOWN == dataType) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持的数据类型");
        }
    }

    /**
     * 取消查询线程
     *
     * @param name
     * @throws AtlasBaseException
     */
    public void cancelAPIThread(String name) throws AtlasBaseException {
        try {
            CompletableFuture<Map> future = taskMap.get(name);
            if (future != null) {
                future.cancel(true);
            }
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
            if (Objects.isNull(info)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "路径错误，未查询到相关API信息");
            }
            //判断是否已发布
            Boolean publish = info.getPublish();
            if (Objects.isNull(publish)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API发布情况未知");
            }
            if (Objects.nonNull(publish) && !publish) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API未发布");
            }
            //获取dbName和tableName
            String tableName = null;
            String dbName = null;

            //API请求类型：Hive/Oracle
            DataSourceType sourceType = DataSourceType.getType(info.getSourceType());
            AdapterSource adapterSource = null;
            AdapterTransformer transformer = AdapterUtils.getAdapter(sourceType.getName()).getAdapterTransformer();

            if (DataSourceType.HIVE.equals(sourceType)) {
                String tableGuid = info.getTableGuid();
                tableName = shareDAO.queryTableNameByGuid(tableGuid);
                dbName = shareDAO.querydbNameByGuid(tableGuid);

                engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
                if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                    adapterSource = AdapterUtils.getImpalaAdapterSource();
                } else {
                    adapterSource = AdapterUtils.getHiveAdapterSource();
                }
            } else {
                tableName = info.getTableName();
                dbName = info.getSchemaName();
                adapterSource = dataSourceService.getAdapterSource(info.getSourceId());
            }
            //查询数据分享API字段详情
            Object fieldsObject = shareDAO.getAPIFields(path);
            PGobject pGobject = (PGobject) fieldsObject;
            String valueObject = pGobject.getValue();
            Gson gson = new Gson();
            List<Map> fieldMap = gson.fromJson(valueObject, List.class);
            Boolean desensitize = info.getDesensitize() == null ? false : info.getDesensitize();
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
                if (field.getFilter()) {
                    filterColumnSet.add(columnName);
                }
                if (field.getSensitive() != null && field.getSensitive()) {
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
            if (Objects.nonNull(limit)) {
                limit = Math.min(limit, maxRowNumber);
            } else {
                limit = maxRowNumber;
            }
            String querySql = queryColumnList.stream().map(transformer::caseSensitive).collect(Collectors.joining(","));
            String filterSql = getFilterSql(infoFieldList, transformer);

            //sql
            String sql = SqlBuilderUtils.buildQuerySql(transformer, dbName, tableName, querySql, filterSql, null, limit, offset);

            AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
            Connection connection = adapterSource.getConnection(MetaspaceConfig.getHiveAdmin(), dbName, info.getPool());
            PageResult<LinkedHashMap<String, Object>> pageResult = adapterExecutor.queryResult(connection, sql, adapterExecutor::extractResultSetToPageResult);

            List<LinkedHashMap<String, Object>> queryDataList = pageResult.getLists();
            if (desensitize) {
                processSensitiveData(sensitiveColumnSet, queryDataList);
            }
            long count = pageResult.getTotalSize();
            //数据格式转换
            String xml = "xml";
            if (acceptHeader.contains(xml)) {
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public void processSensitiveData(Set<String> sensitiveColumnSet, List<LinkedHashMap<String, Object>> dataList) {
        for (LinkedHashMap<String, Object> data : dataList) {
            for (String key : data.keySet()) {
                if (sensitiveColumnSet.contains(key)) {
                    String value = data.get(key).toString();
                    int length = value.length();
                    int replaceLen = length / 3;
                    StringJoiner joiner = new StringJoiner("");
                    String tmp = "";
                    if (replaceLen != 0) {
                        tmp = value.substring(0, length - replaceLen);
                    } else {
                        replaceLen = length;
                    }
                    for (int i = 0; i < replaceLen; i++) {
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
            Boolean sensitive = Boolean.parseBoolean(map.get("sensitive") == null ? Boolean.FALSE.toString() : map.get("sensitive").toString());
            if (filter) {
                Boolean fill = Boolean.parseBoolean(map.get("fill").toString());
                String defaultValue = Objects.nonNull(map.get("defaultValue")) ? map.get("defaultValue").toString() : String.valueOf("");
                Boolean useDefault = Objects.nonNull(map.get("useDefaultValue")) ? Boolean.parseBoolean(map.get("useDefaultValue").toString()) : false;
                //必传值
                if (fill) {
                    if (filterColumnMap.containsKey(columnName)) {
                        Object value = filterColumnMap.get(columnName);
                        if (value instanceof List) {
                            valueList = (List<Object>) value;
                        } else {
                            valueList = new ArrayList<>();
                            valueList.add(value);
                        }
                    } else {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "请求失败，请在请求参数中给出必传过滤字段:" + columnName);
                    }
                } else {
                    //已传值
                    if (filterColumnMap.containsKey(columnName)) {
                        Object value = filterColumnMap.get(columnName);
                        if (value instanceof List) {
                            valueList = (List<Object>) value;
                        } else {
                            valueList = new ArrayList<>();
                            valueList.add(value);
                        }
                        //未传值
                    } else {
                        //使用默认值
                        if (useDefault) {
                            valueList = Arrays.asList(defaultValue);
                        } else {
                            filter = false;
                        }
                    }
                }
            }
            field = new QueryParameter.Field(columnName, type, filter, valueList, sensitive);
            resultFieldList.add(field);
        }
        return resultFieldList;
    }

    /**
     * 查询字段是否在允许范围内
     *
     * @param infoFields
     * @param queryFields
     * @throws AtlasBaseException
     */
    public void checkFieldName(Set<String> infoFields, List<String> queryFields) throws AtlasBaseException {
        for (String field : queryFields) {
            if (!infoFields.contains(field)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的查询字段:" + field);
            }
        }
    }

    /**
     * 查询过滤字段是否在创建API信息中
     *
     * @param filterFields
     * @param queryFilterFields
     * @throws AtlasBaseException
     */
    public void checkFilterField(Set<String> filterFields, Map queryFilterFields) throws AtlasBaseException {
        for (Object field : queryFilterFields.keySet()) {
            if (!filterFields.contains(field.toString())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的过滤查询字段:" + field.toString());
            }
        }
    }

    public List<Column> getTableColumnList(String tableGuid, String tenantId) throws AtlasBaseException {
        List<Column> columnList = metaDataService.getTableInfoById(tableGuid, tenantId).getColumns().stream().filter(column -> "ACTIVE".equals(column.getStatus())).collect(Collectors.toList());
        columnList.forEach(column -> {
            String columnName = column.getColumnName();
            String displayName = column.getDisplayName();
            if (Objects.isNull(displayName) && "".equals(displayName.trim())) {
                column.setDisplayName(columnName);
            }
        });
        return columnList;
    }

    /**
     * 获取管理者列表
     *
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public PageResult getUserList(Parameters parameters, String tenantId) throws AtlasBaseException {
        PageResult pageResult = new PageResult();
        long userTotalSize = 0;
        List<User> userList = new ArrayList<>();
        try {
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                userList = userDAO.getUserList(null, parameters.getLimit(), parameters.getOffset());

                if (userList.size() != 0) {
                    userTotalSize = userList.get(0).getTotal();
                }
            } else {
                SecuritySearch securitySearch = new SecuritySearch();
                securitySearch.setTenantId(tenantId);
                PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, securitySearch);
                for (UserAndModule userAndModule : userAndModules.getLists()) {
                    boolean isDataShare = !userAndModule.getToolRoleResources().stream().anyMatch(module -> ModuleEnum.DATASHARE.getTenantModule().equalsIgnoreCase(module.getRoleName()));
                    if (userAndModule.getToolRoleResources() == null
                            || isDataShare) {//含有数据分享模块
                        continue;
                    }
                    User user = userDAO.getUserByName(userAndModule.getUserName(), userAndModule.getEmail());
                    userList.add(user);
                }
            }
            pageResult.setLists(userList);
            pageResult.setCurrentSize(userList.size());
            pageResult.setTotalSize(userList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取用户列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户列表失败");
        }
    }

    public void updateManager(String apiGuid, String userId) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfoByGuid(apiGuid);
            if (true == info.getPublish()) {
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
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新管理者失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新管理者失败");
        }
    }


    public PageResult getDataSourceList(Parameters parameters, String type, String tenantId) throws AtlasBaseException {
        return dataSourceService.searchDataSources(parameters.getLimit(), parameters.getOffset(), null, null, parameters.getQuery(), type.toUpperCase(), null, null, null, true, tenantId);
    }

    public PageResult getDataList(SEARCH_TYPE searchType, Parameters parameters, String sourceId, String... ids) throws AtlasBaseException {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(dataSourceService.getUnencryptedDataSourceInfo(sourceId));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        switch (searchType) {
            case SCHEMA: {
                return adapterExecutor.getSchemaPage(parameters);
            }
            case TABLE: {
                String schemaName = ids[0];
                return adapterExecutor.getTablePage(schemaName, parameters);
            }
            case COLUMN: {
                String schemaName = ids[0];
                String tableName = ids[1];
                return adapterExecutor.getColumnPage(schemaName, tableName, parameters);
            }
            default:
                break;
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据源元数据失败");
    }

    public enum SEARCH_TYPE {
        SCHEMA, TABLE, COLUMN
    }

    public List<Queue> getPools(String tenantId) throws AtlasBaseException {
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
        Pool pools = tenantService.getPools(tenantId);
        if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
            return pools.getImpala();
        } else {
            return pools.getHive();
        }
    }

    /**
     * 新增项目
     *
     * @param tenantId
     * @param projectInfo
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertProject(ProjectInfo projectInfo, String tenantId) throws AtlasBaseException {
        String id = UUID.randomUUID().toString();
        projectInfo.setId(id);
        int count = shareDAO.sameProjectName(id, projectInfo.getName(), tenantId);
        if (count != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目名已存在");
        }
        projectInfo.setCreateTime(DateUtils.currentTimestamp());
        projectInfo.setCreator(AdminUtils.getUserData().getUserId());
        shareDAO.insertProject(projectInfo, tenantId);
        CategoryUtil.initApiCategory(tenantId, id);
        if (projectInfo.getUserGroups() != null && projectInfo.getUserGroups().size() != 0) {
            shareDAO.addProjectToUserGroup(projectInfo.getId(), projectInfo.getUserGroups());
        }
    }

    /**
     * 查询项目
     *
     * @param tenantId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<ProjectInfo> searchProject(Parameters parameters, String tenantId) throws AtlasBaseException {
        PageResult<ProjectInfo> commonResult = new PageResult<>();

        String query = parameters.getQuery();
        if (query != null) {
            parameters.setQuery(query.replaceAll("%", "/%").replaceAll("_", "/_"));
        }

        //校验租户先是否有用户
        SecuritySearch securitySearch = new SecuritySearch();
        securitySearch.setTenantId(tenantId);
        PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, securitySearch);
        List<String> userIds = userAndModules.getLists().stream().map(UserAndModule::getAccountGuid).collect(Collectors.toList());

        List<ProjectInfo> lists = shareDAO.searchProject(parameters, AdminUtils.getUserData().getUserId(), tenantId, userIds);
        if (lists == null || lists.size() == 0) {
            return commonResult;
        }
        String userId = AdminUtils.getUserData().getUserId();
        for (ProjectInfo projectInfo : lists) {
            if (userIds == null || userIds.size() == 0) {
                projectInfo.setUserCount(0);
            }
            projectInfo.setEditManager(false);
            if (projectInfo.getManagerId().equals(userId)) {
                projectInfo.setEditManager(true);
            }
        }
        commonResult.setTotalSize(lists.get(0).getTotal());
        commonResult.setLists(lists);
        commonResult.setCurrentSize(lists.size());
        return commonResult;
    }

    /**
     * 编辑项目
     *
     * @param projectInfo
     * @param tenantId
     * @throws AtlasBaseException
     */

    public void updateProject(ProjectInfo projectInfo, String tenantId) throws AtlasBaseException {
        String projectManager = shareDAO.getProjectManager(projectInfo.getId());
        if (!projectManager.equals(AdminUtils.getUserData().getUserId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "非项目管理者无法编辑项目");
        }
        int count = shareDAO.sameProjectName(projectInfo.getId(), projectInfo.getName(), tenantId);
        if (count != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目名已存在");
        }
        shareDAO.updateProject(projectInfo);
    }

    /**
     * 新增权限用户组
     *
     * @param userGroups
     * @param projectId
     * @return
     * @throws AtlasBaseException
     */
    public void addUserGroups(List<String> userGroups, String projectId) throws AtlasBaseException {
        String projectManager = shareDAO.getProjectManager(projectId);
        if (!projectManager.equals(AdminUtils.getUserData().getUserId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "非项目管理者无法编辑项目权限");
        }
        int count = shareDAO.sameProjectToUserGroup(projectId, userGroups);
        if (count != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目权限用户组中已经存在部分选中用户组，请刷新后重试添加");
        }
        if (userGroups != null && userGroups.size() != 0) {
            shareDAO.addProjectToUserGroup(projectId, userGroups);
        }

    }

    /**
     * 获取权限用户组列表
     *
     * @param isPrivilege
     * @param projectId
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<UserGroupIdAndName> getUserGroups(boolean isPrivilege, String projectId, Parameters parameters, String tenantId) throws AtlasBaseException {
        PageResult pageResult = new PageResult();
        List<UserGroupIdAndName> userGroups;
        if (isPrivilege == false && projectId == null) {
            userGroups = shareDAO.getAllUserGroups(parameters, tenantId);
        } else if (isPrivilege == false) {
            userGroups = shareDAO.getNoRelationUserGroups(projectId, parameters, tenantId);
        } else {
            userGroups = shareDAO.getRelationUserGroups(projectId, parameters, tenantId);
        }
        if (userGroups == null || userGroups.size() == 0) {
            return pageResult;
        }
        pageResult.setCurrentSize(userGroups.size());
        pageResult.setLists(userGroups);
        pageResult.setTotalSize(userGroups.get(0).getTotalSize());
        return pageResult;
    }

    /**
     * 批量删除权限用户组
     *
     * @param userGroups
     * @param projectId
     * @throws AtlasBaseException
     */
    public void deleteUserGroups(List<String> userGroups, String projectId) throws AtlasBaseException {
        String projectManager = shareDAO.getProjectManager(projectId);
        if (!projectManager.equals(AdminUtils.getUserData().getUserId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "非项目管理者无法编辑项目权限");
        }
        if (userGroups != null && userGroups.size() != 0) {
            shareDAO.deleteProjectToUserGroup(projectId, userGroups);
        }
    }

    /**
     * 批量删除项目
     *
     * @param projectIds
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(List<String> projectIds) throws AtlasBaseException {

        if (projectIds == null || projectIds.size() == 0) {
            return;
        }
        List<String> projectManagers = shareDAO.getProjectsManager(projectIds);
        String userId = AdminUtils.getUserData().getUserId();
        if (projectManagers.size() != 1 || !projectManagers.get(0).equals(userId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "非项目管理者无法删除项目权限");
        }
        shareDAO.deleteProject(projectIds);
        shareDAO.deleteProjectRelation(projectIds);
        //删除项目下的api
        shareDAO.deleteApiByProject(projectIds);
        shareDAO.deleteCategoryByProject(projectIds);
        List<String> groupIds = apiGroupDAO.getApiGroupIdByProject(projectIds);
        groupService.deleteApiGroup(groupIds);
        // todo
        List<String> apiMobiusByProjects = shareDAO.getApiMobiusByProjects(projectIds);
        apiMobiusByProjects.forEach(this::deleteApiMobius);
        List<String> mobiusGroupIds = apiGroupDAO.getMobiusByProjects(projectIds);
        mobiusGroupIds.forEach(apiGroupService::deleteMobiusGroup);
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
                if (!userAndModule.getToolRoleResources().stream().anyMatch(module -> ModuleEnum.APIMANAGE.getAlias().equalsIgnoreCase(module.getRoleName()))) {
                    continue;
                }
                UserIdAndName user = new UserIdAndName();
                user.setUserName(userAndModule.getUserName());
                user.setAccount(userAndModule.getEmail());
                user.setUserId(userDAO.getUserIdByName(userAndModule.getUserName()));
                users.add(user);
            }
            return users;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败:" + e.getMessage());
        }
    }

    /**
     * 获取项目详情
     *
     * @param id
     * @return
     */
    public ProjectInfo getProjectInfoById(String id) {
        return shareDAO.getProjectInfoById(id);
    }

    /**
     * 批量获取项目详情
     *
     * @param ids
     * @return
     */
    public List<ProjectInfo> getProjectInfoByIds(List<String> ids) {
        return shareDAO.getProjectInfoByIds(ids);
    }

    /**
     * 新增api
     *
     * @param info
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertAPIInfoV2(ApiInfoV2 info, String tenantId, boolean submit) throws AtlasBaseException {
        if (!projectPrivateByProject(info.getProjectId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        //版本正则
        String versionRegex = "^v[a-zA-Z0-9\\-\\.]{1,20}$";
        //path正则
        String pathRegex = "^[a-zA-Z0-9/\\{\\}\\*\\%\\-\\_\\.\\+]{1,255}$";
        if (!info.getVersion().matches(versionRegex)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "版本格式不对");
        }
        if (!(info.getPath() == null || info.getPath().matches(pathRegex) || info.getPath().length() == 0)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "path格式不对");
        }

        String guid = UUID.randomUUID().toString();
        int i = shareDAO.queryApiSameName(info.getName(), tenantId, info.getProjectId(), guid);
        if (i != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字的API");
        }
        checkApiPolyEntity(info, info.getApiPolyEntity());

        //guid
        info.setGuid(guid);
        String user = AdminUtils.getUserData().getUserId();

        info.setCreator(user);
        info.setUpdater(user);
        Timestamp timestamp = DateUtils.currentTimestamp();
        //generateTime
        info.setCreateTime(timestamp);
        //updateTime
        info.setUpdateTime(timestamp);
        //star
        info.setStatus(ApiStatusEnum.DRAFT.getName());
        addApiLog(ApiLogEnum.INSERT, info.getGuid(), AdminUtils.getUserData().getUserId());
        shareDAO.insertAPIInfoV2(info, tenantId);
        if (submit) {
            submitApi(guid, info.getVersion(), tenantId);
        }
    }

    /**
     * 更新api
     *
     * @param info
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAPIInfoV2(ApiInfoV2 info, String tenantId, boolean submit) throws AtlasBaseException {
        //版本正则
        String versionRegex = "^v[a-zA-Z0-9\\-\\.]{1,20}$";
        //path正则
        String pathRegex = "^[a-zA-Z0-9/\\{\\}\\*\\%\\-\\_\\.\\+]{1,255}$";
        if (!(info.getVersion().matches(versionRegex) || info.getPath() == null || info.getPath().length() == 0)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "版本格式不对");
        }
        if (!(info.getPath() == null || info.getPath().matches(pathRegex) || info.getPath().length() == 0)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "path格式不对");
        }

        if (!projectPrivateByApi(info.getGuid())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        if (StringUtils.isEmpty(info.getGuid())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新 Api 的 guid 不能为空");
        }
        String maxVersion = shareDAO.getMaxVersion(info.getGuid());
        ApiInfoV2 apiInfo = shareDAO.getApiInfoByVersion(info.getGuid(), maxVersion);
        if (apiInfo == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新 Api 不存在");
        }

        if (isApiSameVersion(info.getGuid(), info.getVersion())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "版本号已存在，请重新设置");
        }
        ApiStatusEnum apiStatus = ApiStatusEnum.getApiStatusEnum(apiInfo.getStatus());
        if (ApiStatusEnum.AUDIT == apiStatus) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核状态，无法编辑");
        }

        checkApiPolyEntity(apiInfo, apiInfo.getApiPolyEntity());

        info.setName(apiInfo.getName());
        info.setCreateTime(apiInfo.getCreateTime());
        info.setCreator(apiInfo.getCreator());

        String user = AdminUtils.getUserData().getUserId();
        info.setUpdater(user);
        Timestamp timestamp = DateUtils.currentTimestamp();
        //updateTime
        info.setUpdateTime(timestamp);
        info.setStatus(ApiStatusEnum.DRAFT.getName());

        if (ApiStatusEnum.DRAFT == apiStatus) {
            shareDAO.updateApiInfoV2OnDraft(info, tenantId, apiInfo.getVersionNum());
        } else {
            shareDAO.updateAPIInfoV2(info, tenantId);
        }
        addApiLog(ApiLogEnum.UPDATE, info.getGuid(), AdminUtils.getUserData().getUserId());
        if (submit) {
            submitApi(info.getGuid(), info.getVersion(), tenantId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAPIInfoV2ApiPolyEntity(String apiId, String version, ApiPolyEntity apiPolyEntity, String tenantId) throws AtlasBaseException {
        try {
            if (!projectPrivateByApi(apiId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
            }
            ApiInfoV2 apiInfo = shareDAO.getApiInfoByVersion(apiId, version);
            if (apiInfo == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "api不存在");
            }

            if (Stream.of(ApiStatusEnum.UP, ApiStatusEnum.DOWN).map(ApiStatusEnum::getName).noneMatch(s -> s.equals(apiInfo.getStatus()))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未审核的api无法只编辑策略");
            }

            long count = apiPolyDao.countApiPolyByStatus(apiId, version, AuditStatusEnum.NEW);
            if (count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在待审核的 API 策略");
            }

            checkApiPolyEntity(apiInfo, apiPolyEntity);

            //插入策略表中
            ApiPoly apiPoly = new ApiPoly(apiId, version, apiPolyEntity);
            apiPolyDao.insert(apiPoly);

            //添加审核记录
            auditService.insertApiAudit(tenantId, apiId, version, apiInfo.getVersionNum(), apiPoly.getId());
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "更新 API 策略失败 : " + e.getMessage());
        }
    }

    public void checkApiPolyEntity(ApiInfoV2 apiInfo, ApiPolyEntity apiPolyEntity) {
        if (apiPolyEntity != null) {
            if (apiPolyEntity.getDesensitization() != null) {
                apiPolyEntity.getDesensitization().forEach(d -> {
                    if (desensitizationDAO.getRule(d.getRuleId()) == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "脱敏规则不存在，id : " + d.getRuleId());
                    }
                });
            }
            if (apiPolyEntity.getIpRestriction() != null && apiPolyEntity.getIpRestriction().getType() != null) {
                IpRestrictionType type = apiPolyEntity.getIpRestriction().getType();
                if (CollectionUtils.isEmpty(apiPolyEntity.getIpRestriction().getIpRestrictionIds())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "配置了黑白名单策略，列表不能为空");
                }
                apiPolyEntity.getIpRestriction().getIpRestrictionIds().forEach(id -> {
                    IpRestriction ipRestriction = ipRestrictionDAO.getIpRestriction(id);
                    if (ipRestriction == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "黑白名单策略不存在，id : " + id);
                    }
                    if (!type.equals(ipRestriction.getType())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API 的黑白名单策略不能混合类型配置");
                    }
                });
            }
        }
    }

    /**
     * 删除api
     *
     * @param ids
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApi(List<String> ids, String tenantId) throws AtlasBaseException {
        if (ids == null || ids.size() == 0) {
            return;
        }
        List<ApiInfoV2> apiInfoByIds = getApiInfoByIds(ids);
        for (ApiInfoV2 apiInfoV2 : apiInfoByIds) {
            if (!projectPrivateByProject(apiInfoV2.getProjectId())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
            }
            if (ApiStatusEnum.UP.getName().equals(apiInfoV2.getStatus())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在上架的api，无法删除");
            }
            if (ApiStatusEnum.AUDIT.getName().equals(apiInfoV2.getStatus())) {
                auditService.cancelApiAudit(tenantId, apiInfoV2.getGuid(), apiInfoV2.getVersion());
            }
        }
        List<String> apiMobiusIds = shareDAO.getApiMobiusIdsByIds(ids);
        shareDAO.deleteApiByIds(ids);
        apiGroupDAO.deleteRelationByApi(ids);
        addApiLogs(ApiLogEnum.DELETE, ids, AdminUtils.getUserData().getUserId());
        for (String apiMobiuId : apiMobiusIds) {
            if (StringUtils.isNotEmpty(apiMobiuId)) {
                deleteApiMobius(apiMobiuId);
            }
        }
    }

    /**
     * 删除api版本
     *
     * @param api
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApiVersion(ApiVersion api, String tenantId) throws AtlasBaseException {
        if (!projectPrivateByApi(api.getApiId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        ApiInfoV2 apiInfo = shareDAO.getApiInfoByVersion(api.getApiId(), api.getVersion());
        if (apiInfo == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "api不存在");
        }
        if (ApiStatusEnum.UP.getName().equals(apiInfo.getStatus())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在上架的api，无法删除");
        }
        if (ApiStatusEnum.AUDIT.getName().equals(apiInfo.getStatus())) {
            auditService.cancelApiAudit(tenantId, api.getApiId(), api.getVersion());
        }
        shareDAO.deleteApiVersion(api);
        apiGroupDAO.deleteRelationByApiVersion(api);
        String apiMobiusId = shareDAO.getApiMobiusIdByVersion(api.getApiId(), api.getVersion());
        deleteApiMobius(apiMobiusId);
    }

    public void deleteApiMobius(String id) {
        Map<String, String> map = new HashMap<>();
        String ticket = AdminUtils.getSSOTicket();
        map.put("X-SSO-FullticketId", ticket);
        int retries = 3;
        int retryCount = 0;
        String mobiusURL = DataServiceUtil.mobiusUrl + "/v3/open/api";
        mobiusURL = mobiusURL + "?id=" + id;
        String errorId = null;
        String errorReason = null;
        String proper = "0.0";
        while (retryCount < retries) {
            String res = OKHttpClient.doDelete(mobiusURL, map);
            LOG.info(res);
            if (Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }
        if (!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "云平台无法删除api");
        }
    }

    /**
     * 获取api最新版本详情
     *
     * @param id
     * @return
     * @throws AtlasBaseException
     */
    public ApiInfoV2 getApiInfoMaxVersion(String id) throws AtlasBaseException {
        try {
            String maxVersion = shareDAO.getMaxVersion(id);
            ApiInfoV2 apiInfo = shareDAO.getApiInfoByVersion(id, maxVersion);
            DataSourceType sourceType = DataSourceType.getType(apiInfo.getSourceType());
            if (sourceType == DataSourceType.HIVE) {
                String tableGuid = apiInfo.getTableGuid();
                Table table = shareDAO.getTableByGuid(tableGuid);
                apiInfo.setTableName(table.getTableName());
                apiInfo.setDbName(table.getDatabaseName());
            } else {
                apiInfo.setSourceName(dataSourceDAO.getSourceNameForSourceId(apiInfo.getSourceId()));
            }
            String creatorName = userDAO.getUserName(apiInfo.getCreator());
            apiInfo.setCreator(creatorName);
            String updaterName = userDAO.getUserName(apiInfo.getUpdater());
            apiInfo.setUpdater(updaterName);
            String categoryName = shareDAO.getCategoryById(apiInfo.getCategoryGuid()).getName();
            apiInfo.setCategoryName(categoryName);
            auditService.getParam(id, apiInfo, maxVersion);
            return apiInfo;
        } catch (Exception e) {
            LOG.error("获取api信息", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取api信息");
        }
    }

    public ApiInfoV2 getApiInfoByVersion(String id, String version, String apiPolyId) throws AtlasBaseException {
        ApiInfoV2 apiInfo = getApiInfoByVersion(id, version);
        if (StringUtils.isNotEmpty(apiPolyId)) {
            ApiPoly apiPoly = apiPolyDao.getApiPoly(apiPolyId);
            if (apiPoly == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API 策略不存在");
            }
            apiInfo.setApiPolyEntity(initApiPolyEntity(apiPoly.getPoly()));
        }
        return apiInfo;
    }


    public ApiPolyEntity initApiPolyEntity(ApiPolyEntity apiPolyEntity) {
        if (apiPolyEntity != null) {
            List<ApiDesensitization> desensitization = apiPolyEntity.getDesensitization();
            if (CollectionUtils.isNotEmpty(desensitization)) {
                desensitization = desensitization.stream().peek(item -> {
                    DesensitizationRule desensitizationRule = desensitizationDAO.getRule(item.getRuleId());
                    item.setRuleName(desensitizationRule == null ? null : desensitizationRule.getName());
                }).filter(item -> Objects.nonNull(item.getRuleName())).collect(Collectors.toList());
                apiPolyEntity.setDesensitization(desensitization);
            }
            ApiIpRestriction apiIpRestriction = apiPolyEntity.getIpRestriction();
            if (apiIpRestriction != null && apiIpRestriction.getType() != null) {
                if (CollectionUtils.isNotEmpty(apiIpRestriction.getIpRestrictionIds())) {
                    List<IpRestriction> ipRestrictions = apiIpRestriction.getIpRestrictionIds().stream().map(id -> ipRestrictionDAO.getIpRestriction(id)).filter(Objects::nonNull).collect(Collectors.toList());
                    apiIpRestriction.setIpRestrictionIds(ipRestrictions.stream().map(IpRestriction::getId).collect(Collectors.toList()));
                    apiIpRestriction.setIpRestrictionNames(ipRestrictions.stream().map(IpRestriction::getName).collect(Collectors.toList()));
                }
                apiPolyEntity.setIpRestriction(apiIpRestriction);
            }
        }
        return apiPolyEntity;
    }

    /**
     * 根据版本获取api详情
     *
     * @param id
     * @return
     * @throws AtlasBaseException
     */
    public ApiInfoV2 getApiInfoByVersion(String id, String version) throws AtlasBaseException {
        try {
            ApiInfoV2 apiInfo = shareDAO.getApiInfoByVersion(id, version);
            DataSourceType sourceType = DataSourceType.getType(apiInfo.getSourceType());
            if (sourceType == DataSourceType.HIVE) {
                String tableGuid = apiInfo.getTableGuid();
                Table table = shareDAO.getTableByGuid(tableGuid);
                apiInfo.setTableName(table.getTableName());
                apiInfo.setDbName(table.getDatabaseName());
            } else {
                apiInfo.setSourceName(dataSourceDAO.getSourceNameForSourceId(apiInfo.getSourceId()));
            }
            String creatorName = userDAO.getUserName(apiInfo.getCreator());
            apiInfo.setCreator(creatorName);
            String updaterName = userDAO.getUserName(apiInfo.getUpdater());
            apiInfo.setUpdater(updaterName);
            String categoryName = shareDAO.getCategoryById(apiInfo.getCategoryGuid()).getName();
            apiInfo.setCategoryName(categoryName);
            auditService.getParam(id, apiInfo, version);
            List<ApiGroupInfo> apiGroupByApiVersion = apiGroupDAO.getApiGroupByApiVersion(id, version);
            apiInfo.setApiGroup(apiGroupByApiVersion);
            ApiPoly apiPoly = getApiPoly(id, version);
            if (apiPoly != null) {
                apiInfo.setApiPolyEntity(initApiPolyEntity(apiPoly.getPoly()));
            }else {
                apiInfo.setApiPolyEntity(initApiPolyEntity(apiInfo.getApiPolyEntity()));
            }
            return apiInfo;
        } catch (Exception e) {
            LOG.error("获取api版本详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "获取api版本详情失败:" + e.getMessage());
        }
    }


    public ApiPoly getApiPoly(String id, String version) {
        return apiPolyDao.getEffectiveApiPoly(id, version, AtlasConfiguration.METASPACE_API_POLY_EFFECTIVE_TIME.getLong());
    }

    public List<ApiVersion> getApiVersion(String apiId) throws AtlasBaseException {
        if (!projectPrivateByApi(apiId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        List<ApiVersion> apiVersions = shareDAO.getApiVersion(apiId);
        return apiVersions;
    }

    /**
     * 判断相同版本
     *
     * @param id
     * @param version
     * @return
     */
    public boolean isApiSameVersion(String id, String version) {
        int i = shareDAO.queryApiSameVersion(id, version);
        if (i != 0) {
            return true;
        }
        return false;
    }

    /**
     * 创建目录
     *
     * @param info
     * @param projectId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public CategoryPrivilege createCategory(CategoryInfoV2 info, String projectId, String tenantId) throws AtlasBaseException {
        if (!projectPrivateByProject(projectId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        CategoryEntityV2 entity = new CategoryEntityV2();
        StringBuffer qualifiedName = new StringBuffer();
        String categoryId = UUID.randomUUID().toString();
        String name = info.getName();
        if (Objects.isNull(name)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
        }
        User user = AdminUtils.getUserData();
        //guid
        entity.setGuid(categoryId);
        //name
        entity.setName(name);
        //createtime
        entity.setCreateTime(io.zeta.metaspace.utils.DateUtils.currentTimestamp());
        //description
        entity.setDescription(info.getDescription());
        int count = shareDAO.querySameNameCategory(entity.getName(), projectId, tenantId, categoryId);
        if (count > 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
        String lastCategoryId = shareDAO.queryLastCategory(projectId, tenantId);
        qualifiedName.append(entity.getName());
        entity.setQualifiedName(qualifiedName.toString());
        entity.setLevel(1);
        entity.setUpBrotherCategoryGuid(lastCategoryId);
        shareDAO.add(entity, projectId, tenantId);
        shareDAO.updateDownBrotherCategoryGuid(lastCategoryId, entity.getGuid(), tenantId);
        CategoryPrivilege returnEntity = new CategoryPrivilege();
        returnEntity.setGuid(entity.getGuid());
        returnEntity.setName(entity.getName());
        returnEntity.setDescription(entity.getDescription());
        returnEntity.setLevel(1);
        returnEntity.setParentCategoryGuid(null);
        returnEntity.setUpBrotherCategoryGuid(lastCategoryId);
        returnEntity.setDownBrotherCategoryGuid(null);
        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        returnEntity.setPrivilege(privilege);
        return returnEntity;
    }

    /**
     * 更新目录
     *
     * @param info
     * @param projectId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public String updateCategory(CategoryInfoV2 info, String projectId, String tenantId) throws AtlasBaseException {
        if (!projectPrivateByProject(projectId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        try {
            String name = info.getName();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            StringBuffer qualifiedName = new StringBuffer();
            qualifiedName.append(name);
            int count = shareDAO.querySameNameCategory(name, projectId, tenantId, tenantId);
            if (count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            }
            CategoryEntity entity = new CategoryEntity();
            entity.setGuid(info.getGuid());
            entity.setName(info.getName());
            entity.setQualifiedName(qualifiedName.toString());
            entity.setDescription(info.getDescription());
            shareDAO.updateCategoryInfo(entity, tenantId);
            return "success";
        } catch (Exception e) {
            LOG.error("更新目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新目录失败");
        }
    }

    public CategoryEntityV2 getCategory(String guid, String tenantId) {
        return shareDAO.queryByGuid(guid, tenantId);
    }

    /**
     * 删除目录
     *
     * @param categoryDelete
     * @param tenantId
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(CategoryDelete categoryDelete, String tenantId) throws Exception {
        if (!projectPrivateByProject(categoryDelete.getProjectId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        try {
            CategoryEntityV2 currentCatalog = shareDAO.queryByGuid(categoryDelete.getId(), tenantId);
            if (Objects.isNull(currentCatalog)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取当前目录信息失败");
            }
            String upBrotherCategoryGuid = currentCatalog.getUpBrotherCategoryGuid();
            String downBrotherCategoryGuid = currentCatalog.getDownBrotherCategoryGuid();
            if (StringUtils.isNotEmpty(upBrotherCategoryGuid)) {
                shareDAO.updateDownBrotherCategoryGuid(upBrotherCategoryGuid, downBrotherCategoryGuid, tenantId);
            }
            if (StringUtils.isNotEmpty(downBrotherCategoryGuid)) {
                shareDAO.updateUpBrotherCategoryGuid(downBrotherCategoryGuid, upBrotherCategoryGuid, tenantId);
            }
            shareDAO.deleteCategory(categoryDelete.getId(), tenantId);
            if (categoryDelete.isDeleteApi()) {
                List<String> apiIds = shareDAO.getApiIdByCategory(categoryDelete.getId());
                if (apiIds != null && apiIds.size() != 0) {
                    addApiLogs(ApiLogEnum.DELETE, apiIds, AdminUtils.getUserData().getUserId());
                    shareDAO.deleteApiByCategory(categoryDelete.getId());
                    apiGroupDAO.deleteRelationByCategory(categoryDelete.getId());
                    List<String> apiMobiusByCategory = shareDAO.getApiMobiusByCategory(categoryDelete.getId());
                    apiMobiusByCategory.forEach(this::deleteApiMobius);
                }
            } else {
                CategoryEntityV2 initCategory = shareDAO.getCategoryByName(CategoryUtil.apiCategoryName, categoryDelete.getProjectId(), tenantId);
                if (initCategory == null) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "初始化默认目录不存在，请确认目录结构");
                }
                String newCategoryId = initCategory.getGuid();
                shareDAO.upDateApiByCategory(categoryDelete.getId(), newCategoryId);
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("删除目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除目录失败");
        }
    }

    /**
     * 查询目录
     *
     * @param projectId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public List<CategoryPrivilege> getCategoryByProject(String projectId, String tenantId) throws AtlasBaseException {
        if (!projectPrivateByProject(projectId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        List<CategoryPrivilege> categories = shareDAO.getCategoryByProject(projectId, tenantId);
        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        for (CategoryPrivilege category : categories) {
            category.setPrivilege(privilege);
            if (CategoryUtil.apiCategoryName.equals(category.getName())) {
                CategoryPrivilege.Privilege initPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, true, false, true, true, false, false);
                category.setPrivilege(initPrivilege);
            }
        }
        return categories;
    }

    /**
     * 查询api
     *
     * @param parameters
     * @param projectId
     * @param categoryId
     * @param status
     * @param approve
     * @param tenantId
     * @return
     */
    public PageResult<ApiHead> searchApi(Parameters parameters, String projectId, String categoryId, String status, String approve, String tenantId) throws AtlasBaseException {
        if (!projectPrivateByProject(projectId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        Boolean approveBool = null;
        if (approve != null && approve.length() != 0) {
            approveBool = Boolean.valueOf(approve);
        }
        PageResult<ApiHead> pageResult = new PageResult<>();
        List<ApiHead> apiHeads = shareDAO.searchApi(parameters, projectId, categoryId, status, approveBool, tenantId);
        List<ApiHead> apiHeadForeach = new ArrayList<>();
        apiHeadForeach.addAll(apiHeads);
        if (apiHeads != null && apiHeads.size() != 0) {
            pageResult.setTotalSize(apiHeads.get(0).getTotal());
            pageResult.setCurrentSize(apiHeads.size());
        }
        pageResult.setLists(apiHeads);
        return pageResult;
    }

    /**
     * 迁移目录
     *
     * @param moveApi
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveApi(MoveApi moveApi) throws AtlasBaseException {
        if (!projectPrivateByCategory(moveApi.getNewCategoryId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        if (moveApi.getApiIds() == null || moveApi.getApiIds().size() == 0) {
            return;
        }
        shareDAO.moveApi(moveApi);
        addApiLogs(ApiLogEnum.MOVE, moveApi.getApiIds(), AdminUtils.getUserData().getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateApiStatus(String guid, boolean status) throws AtlasBaseException {
        if (!projectPrivateByApi(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        if (status) {
            upStatus(guid);
        } else {
            downStatus(guid);
        }
        updateMobiusApiStatus(guid, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void upStatus(String guid) throws AtlasBaseException {
        Timestamp updateTime = DateUtils.currentTimestamp();
        shareDAO.updateApiStatus(guid, ApiStatusEnum.UP.getName(), updateTime);
        addApiLog(ApiLogEnum.UPSTATUS, guid, AdminUtils.getUserData().getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitApi(String id, String version, String tenantId) throws AtlasBaseException {
        if (!projectPrivateByApi(id)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有project权限");
        }
        Timestamp updateTime = DateUtils.currentTimestamp();
        ApiInfoV2 apiInfo = shareDAO.getApiInfoByVersion(id, version);
        if (apiInfo == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "api或版本不存在");
        }
        if (!ApiStatusEnum.DRAFT.getName().equals(apiInfo.getStatus())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "对应api的版本已经提交，请勿重复提交");
        }
        shareDAO.updateApiVersionStatus(id, version, ApiStatusEnum.AUDIT.getName(), updateTime);
        auditService.insertApiAudit(tenantId, id, version, apiInfo.getVersionNum());
        addApiLog(ApiLogEnum.SUBMIT, id, AdminUtils.getUserData().getUserId());
    }

    public void downStatus(String guid) throws AtlasBaseException {
        Timestamp updateTime = DateUtils.currentTimestamp();
        addApiLog(ApiLogEnum.DOWNSTATUS, guid, AdminUtils.getUserData().getUserId());
        shareDAO.updateApiStatus(guid, ApiStatusEnum.DOWN.getName(), updateTime);
    }

    public void updateMobiusApiStatus(String guid, boolean status) {
        {
            Map<String, String> map = new HashMap<>();
            List<String> apiMobiusIds = shareDAO.getApiMobiusIds(guid);
            if (status) {
                map.put("status", "release");
            } else {
                map.put("status", "draft");
            }
            for (String apiMobiusId : apiMobiusIds) {
                map.put("id", apiMobiusId);
                Gson gson = new Gson();
                String jsonStr = gson.toJson(map);
                int retries = 3;
                int retryCount = 0;
                String mobiusURL = DataServiceUtil.mobiusUrl + "/v3/open/api/status";
                String errorId = null;
                String errorReason = null;
                String proper = "0.0";
                while (retryCount < retries) {
                    String res = OKHttpClient.doPut(mobiusURL, jsonStr);
                    LOG.info(res);
                    if (Objects.nonNull(res)) {
                        Map response = convertMobiusResponse(res);
                        errorId = String.valueOf(response.get("error-id"));
                        errorReason = String.valueOf(response.get("reason"));
                        if (proper.equals(errorId)) {
                            break;
                        } else {
                            retryCount++;
                        }
                    } else {
                        retryCount++;
                    }
                }
                if (!proper.equals(errorId)) {
                    StringBuffer detail = new StringBuffer();
                    detail.append("云平台返回错误码:");
                    detail.append(errorId);
                    detail.append("错误信息:");
                    detail.append(errorReason);
                    throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "更新云平台api状态失败");
                }
            }
        }
    }

    public void addApiLog(ApiLogEnum apiLogEnum, String apiId, String userId) {
        ApiLog apiLog = new ApiLog();
        apiLog.setApiId(apiId);
        apiLog.setType(apiLogEnum.getName());
        apiLog.setCreator(userId);
        apiLog.setDate(DateUtils.currentTimestamp());
        shareDAO.addApiLog(apiLog);
    }

    public void addApiLogs(ApiLogEnum apiLogEnum, List<String> apiIds, String userId) {
        Timestamp timestamp = DateUtils.currentTimestamp();
        List<ApiLog> apiLogs = apiIds.stream().map(apiId -> {
            ApiLog apiLog = new ApiLog();
            apiLog.setApiId(apiId);
            apiLog.setType(apiLogEnum.getName());
            apiLog.setCreator(userId);
            apiLog.setDate(timestamp);
            return apiLog;
        }).collect(Collectors.toList());
        shareDAO.addApiLogs(apiLogs);
    }

    public PageResult<ApiLog> getApiLog(Parameters param, String apiId) throws AtlasBaseException {
        PageResult<ApiLog> pageResult = new PageResult<>();
        String type = null;
        if ("api".contains(param.getQuery())) {
            param.setQuery("");
        } else {
            type = ApiLogEnum.getName(param.getQuery());
        }
        if (type == null) {
            type = "";
        }
        List<ApiLog> apiLogs = shareDAO.getApiLog(param, apiId, type);
        for (ApiLog log : apiLogs) {
            String str = String.format(ApiLogEnum.getStr(log.getType()), log.getCreator());
            log.setStr(str);
        }
        pageResult.setLists(apiLogs);
        if (apiLogs == null || apiLogs.size() == 0) {
            return pageResult;
        }
        pageResult.setCurrentSize(apiLogs.size());
        pageResult.setTotalSize(apiLogs.get(0).getTotal());
        return pageResult;
    }


    public Map testAPIV2(String randomName, ApiInfoV2 apiInfo, long limit, long offset) throws AtlasBaseException {
        String tableName = null;
        String dbName = null;

        DataSourceType sourceType = DataSourceType.getType(apiInfo.getSourceType());
        if (!sourceType.isBuildIn()) {
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(apiInfo.getSourceId());
            sourceType = DataSourceType.getType(dataSourceInfo.getSourceType());
        }
        AdapterSource adapterSource = null;
        AdapterTransformer transformer = AdapterUtils.getAdapter(sourceType.getName()).getAdapterTransformer();
        try {
            if (sourceType.isBuildIn()) {
                String tableGuid = apiInfo.getTableGuid();
                String tableStatus = shareDAO.getTableStatusByGuid(tableGuid);
                dbName = shareDAO.querydbNameByGuid(tableGuid);
                tableName = shareDAO.queryTableNameByGuid(tableGuid);
                String deletedStatus = "DELETED";
                if (deletedStatus.equals(tableStatus)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API关联表已被删除");
                }
                if (DataSourceType.IMPALA.equals(sourceType)) {
                    adapterSource = AdapterUtils.getImpalaAdapterSource();
                } else {
                    adapterSource = AdapterUtils.getHiveAdapterSource();
                }
            } else {
                dbName = apiInfo.getSchemaName();
                tableName = apiInfo.getTableName();
                adapterSource = dataSourceService.getAdapterSource(apiInfo.getSourceId());
            }
            checkLimitAndOffsetV2(limit, offset);
            List<ApiInfoV2.FieldV2> filterColumns = apiInfo.getParam();
            List<ApiInfoV2.FieldV2> returnParam = apiInfo.getReturnParam();
            List<ApiInfoV2.FieldV2> sortParam = apiInfo.getSortParam();
            String querySql = returnParam.stream().filter(column -> column.getColumnName() != null).map(column -> transformer.caseSensitive(column.getColumnName()) + " as " + transformer.caseSensitive(column.getName())).collect(Collectors.joining(","));
            String filterSql = getFilterSqlV2(filterColumns, transformer);
            String sortSql = getSortSqlV2(sortParam, returnParam, sourceType);
            String sql = SqlBuilderUtils.buildQuerySql(transformer, dbName, tableName, querySql, filterSql, sortSql, limit, offset);

            AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();

            String pool = apiInfo.getPool();
            Connection connection = adapterSource.getConnection(MetaspaceConfig.getHiveAdmin(), dbName, pool);
            CompletableFuture<PageResult<LinkedHashMap<String, Object>>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return adapterExecutor.queryResult(connection, sql, adapterExecutor::extractResultSetToPageResult);
                } catch (Exception e) {
                    LOG.error("查询失败", e);
                }
                return null;
            });
            taskMap.put(randomName, future);
            PageResult<LinkedHashMap<String, Object>> pageResult = future.get();
            taskMap.remove(randomName);

            ApiPolyEntity apiPolyEntity = apiInfo.getApiPolyEntity();

            List<LinkedHashMap<String, Object>> result = pageResult.getLists();
            if (apiPolyEntity != null && CollectionUtils.isNotEmpty(apiPolyEntity.getDesensitization())) {
                result = processSensitiveDataV2(apiPolyEntity.getDesensitization(), result);
            }

            Map resultMap = new HashMap();
            resultMap.put("queryResult", result);
            resultMap.put("queryCount", pageResult.getTotalSize());
            return resultMap;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("API接口查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    public void checkLimitAndOffsetV2(Long limit, Long offset) throws AtlasBaseException {
        if (Objects.isNull(limit) || Objects.isNull(offset)) {
            throw new AtlasBaseException("limit和offset不允许为空");
        }
        offset = Objects.nonNull(offset) ? offset : 0;
        if (offset < 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "offset取值异常，需大于等于0");
        }
        if (limit < 0 && limit != -1) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "limit取值异常，需大于等于0或未-1");
        }
    }

    public String getFilterSqlV2(List<ApiInfoV2.FieldV2> queryColumns, AdapterTransformer transformer) throws AtlasBaseException {
        if (queryColumns == null) {
            return null;
        }
        String columnName = null;
        StringJoiner filterJoiner = new StringJoiner(" and ");
        List<ApiInfoV2.FieldV2> filterColumns = queryColumns.stream().filter(column -> column.getColumnName() != null).collect(Collectors.toList());
        for (ApiInfoV2.FieldV2 field : filterColumns) {
            columnName = field.getColumnName();
            int minSize = Integer.valueOf(field.getMinSize());
            int maxSize = Integer.valueOf(field.getMaxSize());
            Object value = field.getValue();
            if (minSize != 0 && value.toString().length() < minSize) {
                throw new AtlasBaseException("字段长度不够");
            }
            if (maxSize != 0 && value.toString().length() > maxSize) {
                throw new AtlasBaseException("字段长度过长");
            }
            //未传值
            if (value == null || value.toString().length() == 0) {
                if (field.isFill()) {
                    throw new AtlasBaseException("必填字段必须填写值");
                } else if (field.isUseDefaultValue()) {
                    value = field.getDefaultValue();
                } else {
                    continue;
                }
            }
            String columnType = field.getColumnType();
            DataType dataType = DataType.convertType(columnType.toUpperCase());
            checkDataTypeV2(dataType, value);
            String str = (DataType.STRING == dataType || DataType.CLOB == dataType || DataType.DATE == dataType || DataType.TIMESTAMP == dataType || DataType.TIMESTAMP == dataType || "".equals(value.toString())) ? ("\'" + value.toString() + "\'") : (value.toString());

            String filterStr = SqlBuilderUtils.getFilterConditionStr(transformer, dataType, columnName, Lists.newArrayList(str));
            filterJoiner.add(filterStr);
        }
        return filterJoiner.toString();
    }

    public String getSortSqlV2(List<ApiInfoV2.FieldV2> sortColumns, List<ApiInfoV2.FieldV2> returnColumns, DataSourceType searchType) throws AtlasBaseException {
        StringJoiner columnJoiner = new StringJoiner(",");
        if (sortColumns == null) {
            return null;
        }
        if (searchType == DataSourceType.ORACLE) {
            sortColumns.stream().filter(column -> column.getColumnName() != null).forEach(column -> {
                String order = "asc";
                if ("asc".equalsIgnoreCase(column.getOrder())) {
                    order = "asc";
                } else if ("desc".equalsIgnoreCase(column.getOrder())) {
                    order = "desc";
                }
                columnJoiner.add("\"" + column.getColumnName() + "\" " + order);
            });
        } else if (searchType == DataSourceType.HIVE) {
            sortColumns.stream().filter(column -> column.getColumnName() != null).forEach(column -> {

            });
            for (ApiInfoV2.FieldV2 column : sortColumns) {
                if (column.getColumnName() == null) {
                    continue;
                }
                String order = "asc";
                if ("asc".equalsIgnoreCase(column.getOrder())) {
                    order = "asc";
                } else if ("desc".equalsIgnoreCase(column.getOrder())) {
                    order = "desc";
                }
                Optional<ApiInfoV2.FieldV2> first = returnColumns.stream().filter(co -> co.getColumnName().equals(column.getColumnName())).findFirst();
                if (!first.isPresent()) {
                    throw new AtlasBaseException("排序字段必须是查询字段");
                }
                columnJoiner.add("`" + first.get().getName() + "` " + order);
            }
        }
        return columnJoiner.toString();
    }

    public void checkDataTypeV2(DataType dataType, Object value) throws AtlasBaseException {
        if (DataType.TIMESTAMP != dataType && DataType.DATE != dataType && DataType.TIME != dataType && DataType.CLOB != dataType)
            dataType.valueOf(value);
        else if (DataType.BOOLEAN == dataType) {
            if (!value.equals(true) && !value.equals(false) && !value.equals("true") && !value.equals("false") && !value.equals("0") && !value.equals("1")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取值需为bool类型");
            }
        } else if (DataType.UNKNOWN == dataType) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持的数据类型");
        }
    }

    /**
     * 批量获取api详情
     *
     * @param ids
     * @return
     */
    public List<ApiInfoV2> getApiInfoByIds(List<String> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        List<ApiInfoV2> apiInfoByIds = shareDAO.getApiInfoByIds(ids);
        if (apiInfoByIds == null) {
            return new ArrayList<>();
        }
        return apiInfoByIds;
    }

    /**
     * 查询同名
     *
     * @param name
     * @return
     */
    public boolean querySameNameV2(String name, String tenantId, String projectId) {
        return !(shareDAO.queryApiSameName(name, tenantId, projectId, "") == 0);
    }

    public void handleIpRestriction(String ip, ApiIpRestriction apiIpRestriction) {
        if (apiIpRestriction.getType() != null) {
            IpRestrictionType type = apiIpRestriction.getType();
            List<IpRestriction> ipRestrictions = apiIpRestriction.getIpRestrictionIds().stream().map(id -> ipRestrictionDAO.getIpRestriction(id)).filter(Objects::nonNull).collect(Collectors.toList());
            if (ipRestrictions.stream().filter(i -> type.equals(i.getType())).count() != ipRestrictions.size()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API 的黑白名单策略不能混合类型配置");
            }
            List<String> allIp = ipRestrictions.stream().map(IpRestriction::getIpList).flatMap(Collection::stream).distinct().collect(Collectors.toList());
            switch (type) {
                case WHITE:
                    if (allIp.stream().noneMatch(item -> new IpAddressMatcher(item).matches(ip))) {
                        throw new AtlasBaseException("API 请求IP 不在白名单策略范围内，拒绝处理");
                    }
                    break;
                case BLACK:
                    if (allIp.stream().anyMatch(item -> new IpAddressMatcher(item).matches(ip))) {
                        throw new AtlasBaseException("API 请求IP 在黑名单策略范围内，拒绝处理");
                    }
                    break;
                default:
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API 的黑白名单策略类型错误");
            }
        }
    }

    public QueryResult queryApiDataV2(HttpServletRequest request) throws AtlasBaseException {
        String requestURL = request.getRequestURL().toString();
        String path = requestURL.replaceFirst(".*/api/metaspace/dataservice/", "");
        String[] split = path.split("/");
        String id = split[0];
        String version = split[1];
        ApiInfoV2 apiInfoByVersion = getApiInfoByVersion(id, version);

        ApiPolyEntity apiPolyEntity = apiInfoByVersion.getApiPolyEntity();

        if (apiPolyEntity != null && apiPolyEntity.getIpRestriction() != null) {
            handleIpRestriction(RequestIpUtils.getRealRequestIpAddress(request), apiPolyEntity.getIpRestriction());
        }

        if (!apiInfoByVersion.getRequestMode().equals(request.getMethod())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "请求方式错误");
        }
        Map<String, String> queryMap = new HashMap<>();
        if (apiInfoByVersion == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API已删除或不存在");
        }
        if (!ApiStatusEnum.UP.getName().equals(apiInfoByVersion.getStatus())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API未上架");
        }
        getFieldsValue(request, apiInfoByVersion, split, queryMap);
        String page_num = queryMap.get("page_num");
        String page_size = queryMap.get("page_size");
        long limit = 10;
        long offset = 0;
        if (page_size != null) {
            limit = Long.valueOf(page_size);
        }
        if (page_num != null && Long.valueOf(page_num) > 0) {
            offset = (Long.valueOf(page_num) - 1) * limit;
        }
        Map resultMap = testAPIV2(id, apiInfoByVersion, limit, offset);
        List<LinkedHashMap<String, Object>> result = (List<LinkedHashMap<String, Object>>) resultMap.get("queryResult");

        Long count = Long.parseLong(resultMap.get("queryCount").toString());

//        String xml = "xml";
//        String acceptHeader = request.getHeader("Accept");
//        if(acceptHeader.contains(xml)) {
//            XmlQueryResult queryResult = new XmlQueryResult();
//            XmlQueryResult.QueryData queryData = new XmlQueryResult.QueryData();
//            queryData.setData(result);
//            queryResult.setTotalCount(count);
//            queryResult.setDatas(queryData);
//            shareDAO.updateUsedCount(path);
//            return queryResult;
//        } else {
//
//        }
        JsonQueryResult queryResult = new JsonQueryResult();
        queryResult.setDatas(result);
        queryResult.setTotalCount(count);
        shareDAO.updateUsedCount(path);
        return queryResult;
    }

    public List<LinkedHashMap<String, Object>> processSensitiveDataV2(List<ApiDesensitization> desensitization, List<LinkedHashMap<String, Object>> result) {
        Map<String, DesensitizationRule> desensitizationRuleMap = new HashMap<>();
        for (ApiDesensitization apiDesensitization : desensitization) {
            DesensitizationRule rule = desensitizationDAO.getRule(apiDesensitization.getRuleId());
            if (rule != null && rule.isEnable()) {
                desensitizationRuleMap.put(apiDesensitization.getField(), rule);
            }
        }

        for (LinkedHashMap<String, Object> itemMap : result) {
            for (String filed : itemMap.keySet()) {
                DesensitizationRule rule = desensitizationRuleMap.get(filed);
                if (rule != null) {
                    Object value = rule.getType().getHandle().apply(itemMap.get(filed), rule.getParams());
                    itemMap.put(filed, value);
                }
            }
        }
        return result;
    }

    public void getFieldsValue(HttpServletRequest request, ApiInfoV2 apiInfo, String[] paths, Map<String, String> queryMap) throws AtlasBaseException {

        String apiPath = apiInfo.getPath();
        List<ApiInfoV2.FieldV2> param = apiInfo.getParam();
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() != 0) {
            String[] querys = queryString.split("&");
            for (String query : querys) {
                String[] entity = query.split("=");
                queryMap.put(entity[0], entity[1]);
            }
        }

        for (ApiInfoV2.FieldV2 field : param) {
            String value = null;
            String name = field.getName();
            String place = field.getPlace();
            if ("PATH".equals(place)) {
                String[] apiPaths = apiPath.split("/");
                for (int i = 0; i < apiPaths.length; i++) {
                    if (apiPaths[i].equals("{" + name + "}")) {
                        value = paths[i + 1];
                    }
                }
            }
            if ("HEADER".equals(place)) {
                value = request.getHeader(name);
            }
            if ("QUERY".equals(place)) {
                value = queryMap.get(name);
            }
            field.setValue(value);
        }
    }

    public boolean projectPrivateByApi(String apiId) throws AtlasBaseException {
        String maxVersion = shareDAO.getMaxVersion(apiId);
        ApiInfoV2 apiInfo = shareDAO.getApiInfoByVersion(apiId, maxVersion);
        if (apiInfo == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API已删除或不存在");
        }
        return projectPrivateByProject(apiInfo.getProjectId());
    }

    public boolean projectPrivateByProject(String projectId) throws AtlasBaseException {
        String userId = AdminUtils.getUserData().getUserId();
        ProjectInfo projectInfo = shareDAO.getProjectInfoById(projectId);
        if (projectInfo == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目不存在");
        }
        if (userId.contains(projectInfo.getManager())) {
            return true;
        }
        int count = shareDAO.projectPrivateByProject(userId, projectId);
        if (count != 0) {
            return true;
        }
        return false;
    }

    public boolean projectPrivateByCategory(String categoryId) throws AtlasBaseException {
        String projectId = shareDAO.getProjectIdByCategory(categoryId);
        if (projectId == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录不存在");
        }
        return projectPrivateByProject(projectId);
    }

    public List<DataSourceTypeInfo> getDataSourceType() {
        List<DataSourceTypeInfo> typeNames = Arrays.stream(DataSourceType.values()).map(dataSourceType -> new DataSourceTypeInfo(dataSourceType.getName(), dataSourceType.isBuildIn())).collect(Collectors.toList());
        return typeNames;
    }
}


