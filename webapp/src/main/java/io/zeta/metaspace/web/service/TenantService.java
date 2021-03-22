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

import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.Pool;
import io.zeta.metaspace.model.security.RoleResource;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.Tenant;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.security.TenantDatabaseList;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.TenantDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.CategoryUtil;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.cache.Cache;

/**
 * @author lixiang03
 * @Data 2020/2/25 15:44
 */
@Service
public class TenantService {
    public final static String USER_CACHE_EXPIRE = "user.info.expire";
    public final static String SECURITY_CENTER_HOST = "security.center.host";
    private final static String TICKET_KEY = "X-SSO-FullticketId";
    private final static String TENANT_LIST = "/service/tools/tenant/metaspace";
    private final static String TENANT_USER_MODULE= "/service/tools/users";
    private final static String TENANT_MODULE="/service/tools/toolRoles";
    private final static String POOL="/service/cluster/pools";
    private final static String TENANT_USER_DATABASE="/service/tools/tables/";
    private final static String TENANT_DATABASE="/service/external/getHiveDatabase";
    private final static String toolName="metaspace";
    public final static String defaultTenant="default";
    private static boolean isStandalone;
    private final static String METASPACE_STANDALONE = "metaspace.standalone";
    private static Configuration conf;
    private static String SECURITY_HOST;
    private static int USER_INFO_EXPIRE ;
    private static Cache<String, PageResult<UserAndModule>> userModulesCache;
    private static Cache<String, List<Tenant>> tenantsCache;
    private static Cache<String, List<Module>> modulesCache;
    private static Cache<String, Pool> poolCache;
    private static Cache<String, List<String>> databaseCache;
    private final String successStatusCode="200";


    @Autowired
    private UserDAO userDAO;
    @Autowired
    private TenantDAO tenantDAO;
    private static Logger LOG = Logger.getLogger(TenantService.class);
    static {
        try {
            conf = ApplicationProperties.get();
            USER_INFO_EXPIRE = conf.getInt(USER_CACHE_EXPIRE, 30);
            userModulesCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(USER_INFO_EXPIRE, TimeUnit.MINUTES).build();
            tenantsCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(USER_INFO_EXPIRE, TimeUnit.MINUTES).build();
            modulesCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(USER_INFO_EXPIRE, TimeUnit.MINUTES).build();
            poolCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(USER_INFO_EXPIRE, TimeUnit.MINUTES).build();
            databaseCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(USER_INFO_EXPIRE, TimeUnit.MINUTES).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isStandalone(){
        isStandalone = conf.getBoolean(METASPACE_STANDALONE,false);
        return isStandalone;
    }

    /**
     * 获取当前租户下metaspace的所有用户和用户功能权限
     * @param offset
     * @param limit
     * @param securitySearch
     * @return
     */
    public PageResult<UserAndModule> getUserAndModule(int offset, int limit, SecuritySearch securitySearch) throws AtlasBaseException {
        Object status=null;
        Object msgDesc=null;
        try {
            SECURITY_HOST = conf.getString(SECURITY_CENTER_HOST);
            String cacheKey = getCacheKey(securitySearch.getTenantId())+limit+offset+securitySearch.toString();
            PageResult<UserAndModule> pageResult = userModulesCache.getIfPresent(cacheKey);
            if (pageResult!=null) {
                return pageResult;
            }
            pageResult = new PageResult<>();
            if (securitySearch.getToolName()==null){
                securitySearch.setToolName(toolName);
            }
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put(TICKET_KEY, AdminUtils.getSSOTicket());
            hashMap.put("User-Agent","Chrome");
            hashMap.put("X-XSRF-HEADER","valid");
            HashMap<String,Object> queryParamMap = new HashMap<>();
            if (limit!=-1&&limit!=0){
                queryParamMap.put("pageSize",limit);
            }
            queryParamMap.put("startIndex",offset);
            Gson gson = new Gson();
            String json = gson.toJson(securitySearch);
            int retryCount = 0;
            int retries = 3;
            while(retryCount < retries) {
                String string = OKHttpClient.doPost(SECURITY_HOST + TENANT_USER_MODULE, hashMap, queryParamMap, json);
                if (string == null||string.length()==0) {
                    retryCount++;
                    continue;
                }
                Map map = gson.fromJson(string, HashMap.class);
                status = map.get("statusCode");
                if (status==null||!status.toString().startsWith(successStatusCode)){
                    msgDesc=map.get("msgDesc");
                    retryCount++;
                    continue;
                }
                Object data = map.get("data");
                List<UserAndModule> userAndModules = new ArrayList<>();
                if (data instanceof Map) {
                    Object userVoList = ((Map) data).getOrDefault("userVoList", new ArrayList<>());
                    Object totalCount = ((Map) data).getOrDefault("totalCount", 0);
                    pageResult.setTotalSize(((Double)totalCount).intValue());
                    userAndModules = gson.fromJson(gson.toJson(userVoList), new TypeToken<List<UserAndModule>>() {
                    }.getType());
                    for (UserAndModule userAndModule:userAndModules){
                        if (userAndModule.getAccountGuid()==null){
                            continue;
                        }
                        List<User> users = userDAO.getAllUser();
                        Optional<User> first = users.stream().filter(user -> user.getUserId().equals(userAndModule.getAccountGuid())).findFirst();
                        if (first.isPresent()){
                            boolean isUserName = first.get().getUsername().equals(userAndModule.getUserName());
                            boolean isEmail = first.get().getAccount().equals(userAndModule.getEmail());
                            if ((isUserName || isEmail)){
                                userDAO.updateUser(userAndModule,new Timestamp(System.currentTimeMillis()));
                            }
                        }else{
                            userDAO.insertUser(userAndModule,new Timestamp(System.currentTimeMillis()));
                        }
                    }
                    pageResult.setLists(userAndModules);
                    pageResult.setCurrentSize(userAndModules.size());
                }
                userModulesCache.put(cacheKey,pageResult);
                return pageResult;
            }
        }catch (AtlasBaseException e){
            throw new AtlasBaseException(e.getAtlasErrorCode(),e,"从安全中心获取用户和用户权限失败:"+e.getMessage());
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"从安全中心获取用户和用户权限失败:"+e.getMessage());
        }
        throw getAtlasBaseException(status,msgDesc,"从安全中心获取用户和用户权限失败");
    }


    /**
     * 获取metaspace的租户
     * @return
     */
    @Transactional(rollbackFor=Exception.class)
    public List<Tenant> getTenants() throws AtlasBaseException {
        Object status=null;
        Object msgDesc=null;
        try {
            SECURITY_HOST = conf.getString(SECURITY_CENTER_HOST);
            //isStandalone = conf.getBoolean(METASPACE_STANDALONE,false);
            isStandalone=false;
            if (isStandalone){
                List<Tenant> tenants = new ArrayList<>();
                Tenant tenant = new Tenant();
                tenant.setTenantId(defaultTenant);
                tenant.setProjectName("独立部署");
                tenants.add(tenant);
                return tenants;
            }

            String cacheKey = AdminUtils.getSSOTicket();
            List<Tenant> list = tenantsCache.getIfPresent(cacheKey);
            if (list!=null) {
                return list;
            }
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
            hashMap.put("User-Agent","Chrome");
            User user = AdminUtils.getUserData();
            String userName = userDAO.getUserName(user.getUserId());
            if (userName==null){
                userDAO.addUser(user);
            }
            int retryCount = 0;
            int retries = 3;
            while(retryCount < retries) {
                String string = OKHttpClient.doGet(SECURITY_HOST+TENANT_LIST,null,hashMap);
                Gson gson = new Gson();
                try {
                    list = gson.fromJson(string, new TypeToken<List<Tenant>>(){}.getType());
                }catch (Exception e){
                    Map map = gson.fromJson(string, HashMap.class);
                    status = map.get("statusCode");
                    msgDesc = map.get("msgDesc");
                    retryCount++;
                    continue;
                }
                if (list==null||list.size()==0)
                    return new ArrayList<>();
                List<String> tenantIds = tenantDAO.getAllTenantId();
                List<Tenant> addTenant = list.stream().filter(tenant -> !tenantIds.contains(tenant.getTenantId())).collect(Collectors.toList());
                if (addTenant!=null&&addTenant.size()!=0){
                    tenantDAO.addTenants(addTenant);
                    CategoryUtil.initCategorySql(addTenant);
                }
                tenantsCache.put(cacheKey,list);
                return list;
            }
        }catch (AtlasBaseException e){
            throw new AtlasBaseException(e.getMessage(),e.getAtlasErrorCode(),e,"从安全中心获取租户列表失败");
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"从安全中心获取租户列表失败:");
        }
        throw getAtlasBaseException(status,msgDesc,"从安全中心获取租户列表失败");
    }


    /**
     * 获取当前用户在当前租户下的metaspace的功能权限
     * @param tenantId
     * @return
     */
    public List<Module> getModule(String tenantId) throws AtlasBaseException {
        Object status=null;
        Object msgDesc=null;
        String cacheKey = getCacheKey(tenantId);
        List<Module> modules = modulesCache.getIfPresent(cacheKey);

        if (modules!=null) {
            return modules;
        }
        Gson gson = new Gson();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
        hashMap.put("User-Agent","Chrome");
        hashMap.put("tenantId",tenantId);
        hashMap.put("toolName",toolName);
        try {
            SECURITY_HOST = conf.getString(SECURITY_CENTER_HOST);
            int retryCount = 0;
            int retries = 3;
            while(retryCount < retries) {
                String string = OKHttpClient.doGet(SECURITY_HOST + TENANT_MODULE, null, hashMap);
                List<RoleResource> list;
                try {
                    list = gson.fromJson(string, new TypeToken<List<RoleResource>>() {
                    }.getType());
                }catch (Exception e){
                    Map map = gson.fromJson(string, HashMap.class);
                    status = map.get("statusCode");
                    msgDesc = map.get("msgDesc");
                    retryCount++;
                    continue;
                }
                modules = new ArrayList<>();
                for (RoleResource roleResource : list) {
                    ModuleEnum moduleEnum = ModuleEnum.getModuleEnum(roleResource);
                    if (moduleEnum == null) {
                        continue;
                    }
                    modules.add(moduleEnum.getModule());
                }
                Collections.sort(modules);
                modulesCache.put(cacheKey, modules);
                return modules;
            }
        }catch (AtlasBaseException e){
            throw new AtlasBaseException(e.getAtlasErrorCode(),e,"从安全中心获取当前用户的功能权限错误:"+e.getMessage());
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"从安全中心获取当前用户的功能权限错误:"+e.getMessage());
        }
        throw getAtlasBaseException(status,msgDesc,"从安全中心获取当前用户的功能权限错误");
    }

    /**
     * 获取yarn队列
     * @param tenantId
     * @return
     */
    public Pool getPools(String tenantId) throws AtlasBaseException {
        Object status=null;
        Object msgDesc=null;
        String cacheKey = getCacheKey(tenantId);
        Pool pool = poolCache.getIfPresent(cacheKey);
        if (pool!=null) {
            return pool;
        }
        Gson gson = new Gson();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
        hashMap.put("User-Agent","Chrome");
        hashMap.put("tenant-id",tenantId);
        try {
            SECURITY_HOST = conf.getString(SECURITY_CENTER_HOST);
            int retryCount = 0;
            int retries = 3;
            while(retryCount < retries) {
                String string = OKHttpClient.doGet(SECURITY_HOST + POOL, null, hashMap);
                Map map = gson.fromJson(string, HashMap.class);
                status = map.get("statusCode");
                if (status!=null){
                    msgDesc = map.get("msgDesc");
                    retryCount++;
                    continue;
                }
                pool = gson.fromJson(string, Pool.class);
                poolCache.put(cacheKey, pool);
                return pool;
            }
        }catch (AtlasBaseException e){
            throw new AtlasBaseException(e.getAtlasErrorCode(),e,"从安全中心获取当前用户的资源池错误:"+e.getMessage());
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"从安全中心获取当前用户的资源池错误:"+e.getMessage());
        }
        throw getAtlasBaseException(status,msgDesc,"从安全中心获取当前用户的资源池错误");

    }

    /**
     * 获取用户有权限的库
     * @param tenantId
     * @return
     */
    public List<String> getDatabaseByUser(String userEmail,String tenantId) throws AtlasBaseException {
        Object status=null;
        Object msgDesc=null;
        String cacheKey = getCacheKey(tenantId);
        List<String> strings = databaseCache.getIfPresent(cacheKey);
        if (strings!=null) {
            return strings;
        }
        Gson gson = new Gson();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
        hashMap.put("User-Agent","Chrome");
        hashMap.put("tenant-id",tenantId);
        try {
            SECURITY_HOST = conf.getString(SECURITY_CENTER_HOST);
            String string = OKHttpClient.doGet(SECURITY_HOST+TENANT_USER_DATABASE+userEmail,null,hashMap);
            int retryCount = 0;
            int retries = 3;
            while(retryCount < retries) {
                HashMap<String, Object> databases = gson.fromJson(string, new TypeToken<HashMap<String, Object>>() {
                }.getType());
                status = databases.get("statusCode");
                if (status!=null){
                    msgDesc = databases.get("msgDesc");
                    retryCount++;
                    continue;
                }
                strings = new ArrayList<>(databases.keySet());
                databaseCache.put(cacheKey, strings);
                return strings;
            }
        }catch (AtlasBaseException e){
            throw new AtlasBaseException(e.getAtlasErrorCode(),e,"从安全中心获取当前用户的hive库权限错误:"+e.getMessage());
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"从安全中心获取当前用户的hive库权限错误");
        }
        throw getAtlasBaseException(status,msgDesc,"从安全中心获取当前用户的hive库权限错误");
    }

    /**
     * 获取对应租户的所有库
     * @param tenantId
     * @return
     */
    public List<String> getDatabase(String tenantId) throws AtlasBaseException {
        Object status=null;
        Object msgDesc=null;
        String cacheKey = tenantId;
        List<String> dbs = databaseCache.getIfPresent(cacheKey);
        if (dbs!=null) {
            return dbs;
        }
        Gson gson = new Gson();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("User-Agent","Chrome");
        HashMap<String,String> query = new HashMap<>();
        query.put("tenantId",tenantId);
        try {
            int retryCount = 0;
            int retries = 3;
            while(retryCount < retries) {
                SECURITY_HOST = conf.getString(SECURITY_CENTER_HOST);
                String string = OKHttpClient.doGet(SECURITY_HOST + TENANT_DATABASE, query, hashMap);
                Map map = gson.fromJson(string, HashMap.class);
                status = map.get("statusCode");
                if (status==null||!status.toString().startsWith(successStatusCode)){
                    msgDesc=map.get("msgDesc");
                    retryCount++;
                    continue;
                }
                Object data = map.get("data");
                List<Map<String,String>> databaseList = gson.fromJson(gson.toJson(data), new TypeToken<List<Map<String, String>>>() {
                }.getType());
                dbs = databaseList.stream().map(database->database.get("resourceContent")).collect(Collectors.toList());
                databaseCache.put(cacheKey, dbs);
                return dbs;
            }
        }catch (AtlasBaseException e){
            throw new AtlasBaseException(e.getAtlasErrorCode(),e,"从安全中心获取当前用户的hive库权限错误:"+e.getMessage());
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"从安全中心获取当前用户的hive库权限错误:"+e.getMessage());
        }
        throw getAtlasBaseException(status,msgDesc,"从安全中心获取当前用户的hive库权限错误");
    }

    public AtlasBaseException getAtlasBaseException(Object status,Object msgDesc,String message){
        StringBuffer detail = new StringBuffer();
        detail.append("安全中心返回错误码:");
        if (status!=null){
            detail.append(Double.valueOf(status.toString()).longValue());
        }else{
            detail.append("null");
        }

        detail.append(", ");
        detail.append("错误信息:");
        detail.append(Objects.toString(msgDesc));
        return new AtlasBaseException(detail.toString(),AtlasErrorCode.BAD_REQUEST, message);
    }

    public String getCacheKey(String tenantId) throws AtlasBaseException {
        return AdminUtils.getSSOTicket()+tenantId;
    }
    public void cleanCache() throws AtlasBaseException {
        List<Tenant> tenants = tenantsCache.getIfPresent(AdminUtils.getSSOTicket());
        if (tenants==null){
            return;
        }
        for (Tenant tenant:tenants){
            modulesCache.invalidate(getCacheKey(tenant.getTenantId()));
            poolCache.invalidate(getCacheKey(tenant.getTenantId()));
            databaseCache.invalidate(getCacheKey(tenant.getTenantId()));
            tenantsCache.invalidate(AdminUtils.getSSOTicket());
        }
        userModulesCache.invalidateAll();
        databaseCache.invalidateAll();
    }
}
