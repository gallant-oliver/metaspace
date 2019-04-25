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
import com.google.gson.internal.LinkedTreeMap;
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
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIContent;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.DataType;
import io.zeta.metaspace.model.share.FilterColumn;
import io.zeta.metaspace.model.share.QueryInfo;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.SSLClient;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import jodd.util.StringUtil;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.hadoop.hbase.client.Put;
import org.json.simple.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

import javax.servlet.http.HttpServletRequest;

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
    private static final String TICKET_KEY = "X-SSO-FullticketId";

    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    DataManageService dataManageService;
    @Autowired
    private MetaDataService metaDataService;
    @Autowired
    private UserDAO userDAO;

    ExecutorService pool = Executors.newFixedThreadPool(100);

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
            if(Objects.isNull(info)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到API信息");
            }
            String version = info.getVersion();
            String path = info.getPath();
            StringJoiner pathJoiner = new StringJoiner("/");
            pathJoiner.add("api").add(version).add("share").add(path);
            info.setPath("/" + pathJoiner.toString());
            List<APIInfo.Field> fields = getQueryFileds(guid);
            List<String> dataOwner = getDataOwner(info.getTableGuid());
            info.setDataOwner(dataOwner);
            info.setFields(fields);
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
            String userId = AdminUtils.getUserData().getUserId();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            String query = parameters.getQuery();
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            List<APIInfoHeader> list = shareDAO.getAPIList(guid, my, publish, userId, query, limit, offset);
            List<String> starAPIList = shareDAO.getUserStarAPI(userId);
            for(APIInfoHeader header : list) {
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
                List<String> dataOwner = getDataOwner(header.getTableGuid());
                header.setDataOwner(dataOwner);
            }

            int apiCount = shareDAO.getAPICount(guid, my, publish, userId, query);
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
            List<APIInfo.Field> values = gson.fromJson(value, List.class);
            return values;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    public List<String> getDataOwner(String guid) throws AtlasBaseException {
        try {
            Gson gson = new Gson();
            Object dataOwnerObject = shareDAO.getDataOwnerByGuid(guid);
            PGobject pGobject = (PGobject)dataOwnerObject;
            List<String> dataOwner = new ArrayList<>();
            if(Objects.nonNull(pGobject)) {
                String value = pGobject.getValue();
                List<Map> onwers = gson.fromJson(value, List.class);
                List<LinkedTreeMap> organization = dataManageService.getOrganization();
                for (LinkedTreeMap map : organization) {
                    String id = map.get("id").toString();
                    String type = map.get("type").toString();
                    for(Map owner : onwers) {
                        if(owner.get("id").toString().equals(id) && owner.get("type").toString().equals(type)) {
                            dataOwner.add(map.get("name").toString());
                        }
                    }
                }
            }
            return dataOwner;
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

    /*public static String doPost(String url, String json) {
        try {
            // 定义HttpClient
            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            PostMethod postMethod = new PostMethod(url);
            String ticket = AdminUtils.getSSOTicket();
            postMethod.addRequestHeader(new Header(TICKET_KEY, ticket));
            RequestEntity requestEntity = new StringRequestEntity(json);
            postMethod.setRequestEntity(requestEntity);
            int result = httpClient.executeMethod(postMethod);
            if (result == HttpStatus.SC_OK) {
                InputStream in = postMethod.getResponseBodyAsStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return URLDecoder.decode(baos.toString(), "UTF-8");
            } else {
                throw new Exception("HTTP ERROR Status: " + postMethod.getStatusCode() + ":" + postMethod.getStatusText());
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    /*public static String doPut(String url, List<String> list) {
        try {
            PutMethod putMethod = new PutMethod(url);

            // 定义HttpClient
            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();

            String ticket = AdminUtils.getSSOTicket();
            putMethod.addRequestHeader(new Header(TICKET_KEY, ticket));

            Map kv = new HashMap();
            kv.put("api_id_list", list);
            Gson gson = new Gson();
            String jsonStr = gson.toJson(kv, Map.class);

            RequestEntity requestEntity = new StringRequestEntity(jsonStr);
            putMethod.setRequestEntity(requestEntity);

            int result = httpClient.executeMethod(putMethod);
            if (result == HttpStatus.SC_OK) {
                InputStream in = putMethod.getResponseBodyAsStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return URLDecoder.decode(baos.toString(), "UTF-8");
            } else {
                throw new Exception("HTTP ERROR Status: " + putMethod.getStatusCode() + ":" + putMethod.getStatusText());
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public int publishAPI(List<String> guidList) throws AtlasBaseException {
        try {
            Configuration configuration = ApplicationProperties.get();
            APIContent content = generateAPIContent(guidList);
            Gson gson = new Gson();
            String jsonStr = gson.toJson(content, APIContent.class);
            String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS) + "/svc/create";
            String res = SSLClient.doPost(mobiusURL, jsonStr);
            LOG.info(res);
            return shareDAO.updatePublishStatus(guidList, true);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
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
            return shareDAO.updatePublishStatus(apiGuid, false);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }

    public APIContent generateAPIContent(List<String> guidList) throws Exception {
        Configuration configuration = ApplicationProperties.get();
        APIContent content = new APIContent();
        List<APIContent.APIDetail> contentList = new ArrayList<>();
        for(String api_id : guidList) {
            APIInfo info = shareDAO.getAPIInfoByGuid(api_id);
            String api_name = info.getName();
            String api_desc = info.getDescription();
            String api_version = info.getVersion();
            //String userId = info.getKeeper();
            //String api_owner = userDAO.getUserAccount(userId);
            List<String> owners = new ArrayList<>();
            List<APIContent.APIDetail.Organization> organizations = getOrganization(api_id);
            String api_catalog = shareDAO.getGroupByAPIGuid(api_id);
            String create_time = info.getGenerateTime();
            String uri = getURL(info);
            String method = info.getRequestMode();
            String upstream_url = configuration.getString(ATLAS_REST_ADDRESS);
            String swagger_content = generateSwaggerContent(info);
            APIContent.APIDetail detail = new APIContent.APIDetail(api_id, api_name, api_desc, api_version, owners, organizations, api_catalog, create_time, uri, method, upstream_url, swagger_content);
            contentList.add(detail);
        }
        content.setApis_detail(contentList);
        return content;
    }

    public List<APIContent.APIDetail.Organization> getOrganization(String guid) {
        Gson gson = new Gson();
        Object dataOwnerObject = shareDAO.getDataOwnerByApiGuid(guid);
        PGobject pGobject = (PGobject)dataOwnerObject;
        List<APIContent.APIDetail.Organization> list = new ArrayList<>();
        if(Objects.nonNull(pGobject)) {
            String value = pGobject.getValue();
            List<Map> owners = gson.fromJson(value, List.class);
            for(Map owner : owners) {
                String id = owner.get("id").toString();
                String type = owner.get("type").toString();
                APIContent.APIDetail.Organization organization = new APIContent.APIDetail.Organization(id, type);
                list.add(organization);
            }
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

    public String generateSwaggerContent(APIInfo info) throws Exception {
        try {
            Configuration configuration = ApplicationProperties.get();
            String hostStr = configuration.getString(ATLAS_REST_ADDRESS);
            String[] hostArr = hostStr.split("http://");
            Swagger swagger = new Swagger();
            //host
            String host = hostArr[1] + "/metaspace";
            swagger.setHost(host);
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
            List<APIInfo.Field> values = new ArrayList<>();

            Gson gson = new Gson();
            for (Object obj : getQueryFileds(info.getGuid())) {
                Type type = new TypeToken<APIInfo.Field>() {
                }.getType();
                APIInfo.Field field = gson.fromJson(gson.toJson(obj), type);
                values.add(field);
            }

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
                            if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Boolean.valueOf(defaultValue));
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
                            if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Integer.valueOf(field.getDefaultValue()));
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
                            if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Double.valueOf(field.getDefaultValue()));
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
                            if (StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Float.valueOf(field.getDefaultValue()));
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
                            if (Objects.nonNull(defaultValue))
                                filterColumnProperty.setDefault(field.getDefaultValue());
                        }
                        filterColumnProperty.setType(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    }
                }


                /*if (filter) {
                    DataType dataType = DataType.parseOf(type);
                    if (DataType.BOOLEAN == dataType) {
                        BooleanProperty filterColumnProperty = new BooleanProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Boolean.valueOf(defaultValue));
                        }
                        filterColumnProperty.setType(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    } else if (DataType.INT == dataType) {
                        IntegerProperty filterColumnProperty = new IntegerProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Integer.valueOf(field.getDefaultValue()));
                        }
                        filterColumnProperty.setType("integer");
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    } else if (DataType.DOUBLE == dataType) {
                        DoubleProperty filterColumnProperty = new DoubleProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Double.valueOf(field.getDefaultValue()));
                        }
                        filterColumnProperty.setType("number");
                        filterColumnProperty.setFormat(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    } else if (DataType.FLOAT == dataType) {
                        FloatProperty filterColumnProperty = new FloatProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(StringUtil.isEmpty(defaultValue))
                                filterColumnProperty.setDefault(Float.valueOf(field.getDefaultValue()));
                        }
                        filterColumnProperty.setType(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    } else {
                        StringProperty filterColumnProperty = new StringProperty();
                        if (fill) {
                            filterColumnProperty.setRequired(true);
                        } else {
                            if(Objects.nonNull(defaultValue))
                                filterColumnProperty.setDefault(field.getDefaultValue());
                        }
                        filterColumnProperty.setType(type);
                        filterPropertyMap.put(columnName, filterColumnProperty);
                    }
                }*/
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
            IntegerProperty limit = new IntegerProperty();
            limit.setRequired(true);
            limit.setDefault(10);
            propertyMap.put("limit", limit);
            //property
            requestModel.setProperties(propertyMap);
            //model
            parameter.setSchema(requestModel);

            parameters.add(parameter);
            operation.setParameters(parameters);
            path.setPost(operation);

            responseModel.setProperties(responsePropertyMap);

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
            System.out.println(content);
            LOG.info(content);
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
    public List<Map> testAPI(String randomName, QueryParameter parameter) throws AtlasBaseException {
        try {
            String tableGuid = parameter.getTableGuid();
            HashMap<String, List> columnMap = new HashMap<>();
            List<QueryParameter.Parameter> parameters = parameter.getParameter();
            parameters.stream().forEach(p -> columnMap.put(p.getColumnName(), p.getValue()));
            Map columnTypeMap = getColumnType(tableGuid, columnMap.keySet());
            String tableName = shareDAO.queryTableNameByGuid(tableGuid);
            String dbName = shareDAO.querydbNameByGuid(tableGuid);
            long limit = parameter.getLimit();
            long offset = parameter.getOffset();
            List<String> queryColumns = parameter.getQueryFields();
            long maxRowNumber = parameter.getMaxRowNumber();
            limit = Math.min(limit, maxRowNumber);
            String sql = getQuerySQL(tableName, columnTypeMap, parameters, queryColumns, limit, offset);

            APITask task = new APITask(randomName, sql, dbName);
            Future<List<Map>> futureResult = pool.submit(task);
            List<Map> result = futureResult.get();
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    class APITask implements Callable<List<Map>> {
        private String name;
        private String sql;
        private String dbName;
        APITask(String name, String sql, String dbName) {
            this.name = name;
            this.sql = sql;
            this.dbName = dbName;
        }
        @Override
        public List<Map> call() throws Exception {
            try {
                if (Objects.nonNull(name)) {
                    Thread.currentThread().setName(name);
                }
                ResultSet resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(sql, dbName);

                List<Map> result = new ArrayList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    Map map = new HashMap();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(columnName);
                        map.put(columnName, value);
                    }
                    result.add(map);
                }
                return result;
            } catch (AtlasBaseException e) {
                LOG.error(e.getMessage());
                throw e;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                throw e;
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

    /*public List<Map> queryAPIData(String path, HttpServletRequest request) throws AtlasBaseException {
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
                String defaultValue = map.get("defaultValue").toString();
                Boolean filter = Boolean.parseBoolean(map.get("filter").toString());
                Boolean fill = Boolean.parseBoolean(map.get("fill").toString());
                Boolean useDefault = Boolean.parseBoolean(map.get("useDefaultValue").toString());
                fields.add(columnName);
                if (filter) {
                    FilterColumn column = new FilterColumn(columnName, defaultValue, fill, useDefault);
                    filterFileds.add(column);
                }
            }
            Map parameterMap = request.getParameterMap();
            //请求中查询字段
            Object columnObj = parameterMap.get("columns");
            String columnStr = "";
            if(columnObj instanceof String[]) {
                String[] str = (String[])columnObj;
                columnStr = str[0];
            }
            String[] columnArr = columnStr.substring(1, columnStr.length()-1).split(",");
            List<String> queryFiles = Arrays.asList(columnArr);
            //请求中过滤字段
            Object filterColumnObj = parameterMap.get("filters");
            String filterColumnStr = "";
            if(filterColumnObj instanceof String[]) {
                String[] str = (String[])filterColumnObj;
                filterColumnStr = str[0];
            }
            String[] filterColumnArr = filterColumnStr.substring(1, filterColumnStr.length()-1).split(";");
            Map filterColumnMap = new HashMap();
            for(String filterColumn : filterColumnArr) {
                String[] kvArr = filterColumn.split(":");
                String columnName = kvArr[0];
                List<String> valueList = new ArrayList<>();
                if(kvArr[1].contains("{")) {
                    String valueStr = kvArr[1];
                    String[] valueArr = valueStr.substring(1, valueStr.length()-1).split(",");
                    valueList = Arrays.asList(valueArr);
                } else {
                    valueList.add(kvArr[1]);
                }
                filterColumnMap.put(columnName, valueList);
            }
            //检查请求中查询字段是否包含于详情中字段
            checkFieldName(fields, queryFiles);
            //获取字段类型
            Map columnTypeMap = getColumnType(tableGuid, fields);
            //校验过滤参数列表
            List<QueryParameter.Parameter> kvList = checkFieldValue(filterFileds, filterColumnMap);
            //limit
            Object limitObj = parameterMap.get("limit");
            Long limit = null;
            if(Objects.nonNull(limitObj) && limitObj instanceof String[]) {
                String[] str = (String[])limitObj;
                limit = Long.parseLong(str[0]);
            }
            //offset
            Object offsetObj = parameterMap.get("offset");
            long offset = 0;
            if(Objects.nonNull(offsetObj) && offsetObj instanceof String[]) {
                String[] str = (String[])offsetObj;
                offset = Long.parseLong(str[0]);
            }
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
            String sql = getQuerySQL(tableName, columnTypeMap, kvList, queryFiles, limit, offset);
            //任务
            String randomName = String.valueOf(System.currentTimeMillis());
            APITask task = new APITask(randomName, sql, dbName);
            Future<List<Map>> futureResult = pool.submit(task);
            List<Map> result = futureResult.get();
            return result;
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
    }*/

    public List<Map> queryAPIData(String path, QueryInfo queryInfo) throws AtlasBaseException {
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
                String defaultValue = map.get("defaultValue").toString();
                Boolean filter = Boolean.parseBoolean(map.get("filter").toString());
                Boolean fill = Boolean.parseBoolean(map.get("fill").toString());
                Boolean useDefault = Boolean.parseBoolean(map.get("useDefaultValue").toString());
                fields.add(columnName);
                if (filter) {
                    FilterColumn column = new FilterColumn(columnName, defaultValue, fill, useDefault);
                    filterFileds.add(column);
                }
            }




            //请求中查询字段
            /*Object columnObj = parameterMap.get("columns");
            String columnStr = "";
            if(columnObj instanceof String[]) {
                String[] str = (String[])columnObj;
                columnStr = str[0];
            }
            String[] columnArr = columnStr.substring(1, columnStr.length()-1).split(",");*/
            List<String> queryFiles = queryInfo.getColumns();
            //请求中过滤字段
           /* Object filterColumnObj = parameterMap.get("filters");
            String filterColumnStr = "";
            if(filterColumnObj instanceof String[]) {
                String[] str = (String[])filterColumnObj;
                filterColumnStr = str[0];
            }
            String[] filterColumnArr = filterColumnStr.substring(1, filterColumnStr.length()-1).split(";");*/
            Map filterColumnMap = queryInfo.getFilters();
            /*for(String filterColumn : filterColumnArr) {
                String[] kvArr = filterColumn.split(":");
                String columnName = kvArr[0];
                List<String> valueList = new ArrayList<>();
                if(kvArr[1].contains("{")) {
                    String valueStr = kvArr[1];
                    String[] valueArr = valueStr.substring(1, valueStr.length()-1).split(",");
                    valueList = Arrays.asList(valueArr);
                } else {
                    valueList.add(kvArr[1]);
                }
                filterColumnMap.put(columnName, valueList);
            }*/
            //检查请求中查询字段是否包含于详情中字段
            checkFieldName(fields, queryFiles);
            //获取字段类型
            Map columnTypeMap = getColumnType(tableGuid, fields);
            //校验过滤参数列表
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
            String sql = getQuerySQL(tableName, columnTypeMap, kvList, queryFiles, limit, offset);
            //任务
            String randomName = String.valueOf(System.currentTimeMillis());
            APITask task = new APITask(randomName, sql, dbName);
            Future<List<Map>> futureResult = pool.submit(task);
            List<Map> result = futureResult.get();
            return result;
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的过滤查询字段:" + field);
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
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "请求错误，未填必传值字段:" + field.getColumnName());
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
    public String getQuerySQL(String tableName, Map<String, String> columnTypeMap, List<QueryParameter.Parameter> kvList, List<String> queryColumns, Long limit, Long offset) throws AtlasBaseException {
        String columnName = null;
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("select ");
            StringJoiner columnJoiner = new StringJoiner(",");
            queryColumns.stream().forEach(column -> columnJoiner.add(column));
            sb.append(columnJoiner.toString());
            sb.append(" from ");
            sb.append(tableName);
            //过滤条件
            if (Objects.nonNull(kvList) && kvList.size() > 0) {
                sb.append(" where ");
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
                        String str = (DataType.STRING == dataType || "".equals(value.toString()))?("\"" + value.toString() + "\""):(value.toString());
                        valueJoiner.add(str);
                    });

                    if (valueList.size() > 1) {
                        valueBuffer.append(kv.getColumnName()).append(" in ").append("(").append(valueJoiner.toString()).append(")");
                    } else {
                        valueBuffer.append(kv.getColumnName()).append("=").append(valueJoiner.toString());
                    }
                    filterJoiner.add(valueBuffer.toString());
                }
                sb.append(filterJoiner.toString());
            }
            //limit
            if (Objects.nonNull(limit) && -1 != limit) {
                sb.append(" limit ");
                sb.append(limit);
            }
            //offset
            sb.append(" offset ");
            sb.append(offset);
            LOG.info("SQL：" + sb.toString());
            return sb.toString();
        } catch (NumberFormatException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, columnName + "取值与类型不匹配");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL查询语句异常");
        }
    }
}
