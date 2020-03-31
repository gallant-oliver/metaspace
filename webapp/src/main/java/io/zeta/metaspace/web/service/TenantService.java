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
import io.zeta.metaspace.web.cache.MetaspaceContext;
import io.zeta.metaspace.web.dao.TenantDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lixiang03
 * @Data 2020/2/25 15:44
 */
@Service
public class TenantService {
    public final static String SECURITY_CENTER_HOST = "security.center.host";
    private final static String TICKET_KEY = "X-SSO-FullticketId";
    private final static String TENANT_LIST = "/service/tools/tenant/metaspace";
    private final static String TENANT_USER_MODULE= "/service/tools/users";
    private final static String TENANT_MODULE="/service/tools/toolRoles";
    private final static String POOL="/service/cluster/pools";
    private final static String TENANT_USER_DATABASE="/service/tools/tables/";
    private final static String TENANT_DATABASE="/service/tenant/databases";
    private final static String toolName="metaspace";
    public final static String defaultTenant="default";
    private static boolean isStandalone;
    private final static String METASPACE_STANDALONE = "metaspace.standalone";
    private static Configuration conf;
    private static String SECURITY_HOST;

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private TenantDAO tenantDAO;
    private static Logger LOG = Logger.getLogger(TenantService.class);
    static {
        try {
            conf = ApplicationProperties.get();
            isStandalone = conf.getBoolean(METASPACE_STANDALONE);
            SECURITY_HOST = conf.getString(SECURITY_CENTER_HOST);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isStandalone(){
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
        if (offset!=0){
            queryParamMap.put("startIndex",offset);
        }
        Gson gson = new Gson();
        String json = gson.toJson(securitySearch);
        int retryCount = 0;
        while(retryCount < 3) {
            String string = OKHttpClient.doPost(SECURITY_HOST + TENANT_USER_MODULE, hashMap, queryParamMap, json);
            if (string == null||string.length()==0) {
                retryCount++;
                continue;
            }
            PageResult pageResult = new PageResult();
            Map map = gson.fromJson(string, HashMap.class);
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

                    if (first.isPresent()&&(first.get().getUsername().equals(userAndModule.getUserName())||first.get().getAccount().equals(userAndModule.getEmail()))){
                        userDAO.updateUser(userAndModule,new Timestamp(System.currentTimeMillis()));
                    }else if (!first.isPresent()){
                        userDAO.insertUser(userAndModule,new Timestamp(System.currentTimeMillis()));
                    }
                }
                pageResult.setLists(userAndModules);
                pageResult.setCurrentSize(userAndModules.size());
            }
            return pageResult;
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "从安全中心获取用户和用户权限失败");
    }


    /**
     * 获取metaspace的租户
     * @return
     */
    public List<Tenant> getTenants() throws AtlasBaseException {
        if (isStandalone){
            List<Tenant> tenants = new ArrayList<>();
            Tenant tenant = new Tenant();
            tenant.setTenantId(defaultTenant);
            tenant.setProjectName("独立部署");
            tenants.add(tenant);
            return tenants;
        }
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
        hashMap.put("User-Agent","Chrome");
        int retryCount = 0;
        while(retryCount < 3) {
            String string = OKHttpClient.doGet(SECURITY_HOST+TENANT_LIST,null,hashMap);
            Gson gson = new Gson();
            List<Tenant> list = gson.fromJson(string, new TypeToken<List<Tenant>>(){}.getType());
            List<String> tenantIds = tenantDAO.getAllTenantId();
            List<Tenant> addTenant = list.stream().filter(tenant -> !tenantIds.contains(tenant.getTenantId())).collect(Collectors.toList());
            if (addTenant!=null&&addTenant.size()!=0)
                tenantDAO.addTenants(addTenant);
            if (list==null||list.size()==0)
                throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED,"该用户没有此工具的租户");
            return list;
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"从安全中心获取租户列表失败");
    }


    /**
     * 获取当前用户在当前租户下的metaspace的功能权限
     * @param tenantId
     * @return
     */
    public List<Module> getModule(String tenantId) throws AtlasBaseException {
        Gson gson = new Gson();
        String key = String.format("getModule(TICKET_KEY:%s,tenantId:%s)",AdminUtils.getSSOTicket(),tenantId);
        Object o = MetaspaceContext.get(key);
        if (o!=null){
            try{
                return gson.fromJson(gson.toJson(o), new TypeToken<List<Module>>(){}.getType());
            }catch(Exception e){
                LOG.warn("获取缓存失败", e);
            }

        }

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
        hashMap.put("User-Agent","Chrome");
        hashMap.put("tenantId",tenantId);
        hashMap.put("toolName",toolName);
        int retryCount = 0;
        while(retryCount < 3) {
            String string = OKHttpClient.doGet(SECURITY_HOST+TENANT_MODULE,null,hashMap);
            List<RoleResource> list = gson.fromJson(string, new TypeToken<List<RoleResource>>(){}.getType());
            List<Module> modules = new ArrayList<>();
            for (RoleResource roleResource:list){
                ModuleEnum moduleEnum = ModuleEnum.getModuleEnum(roleResource);
                if (moduleEnum==null){
                    continue;
                }
                modules.add(moduleEnum.getModule());
            }
            MetaspaceContext.set(key,modules);
            return modules;
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"从安全中心获取当前用户的功能权限错误");
    }

    /**
     * 获取yarn队列
     * @param tenantId
     * @return
     */
    public Pool getPools(String tenantId) throws AtlasBaseException {
        Gson gson = new Gson();
        String key = String.format("getPools(TICKET_KEY:%s,tenantId:%s)",AdminUtils.getSSOTicket(),tenantId);
        Object o = MetaspaceContext.get(key);
        if (o!=null){
            try{
                return gson.fromJson(gson.toJson(o), Pool.class);
            }catch(Exception e){
                LOG.warn("获取缓存失败", e);
            }
        }
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
        hashMap.put("User-Agent","Chrome");
        hashMap.put("tenant-id",tenantId);
        int retryCount = 0;
        while(retryCount < 3) {
            String string = OKHttpClient.doGet(SECURITY_HOST+POOL,null,hashMap);
            Pool pool = gson.fromJson(string, Pool.class);
            MetaspaceContext.set(key,pool);
            return pool;
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"从安全中心获取当前用户的资源池错误");
    }

    /**
     * 获取用户有权限的库
     * @param tenantId
     * @return
     */
    public List<String> getDatabaseByUser(String userEmail,String tenantId) throws AtlasBaseException {
        Gson gson = new Gson();
        String key = String.format("getDatabaseByUser(TICKET_KEY:%s,userEmail:%s,tenantId:%s)",AdminUtils.getSSOTicket(),userEmail,tenantId);
        Object o = MetaspaceContext.get(key);
        if (o!=null){
            try{
                return gson.fromJson(gson.toJson(o), new TypeToken<List<String>>(){}.getType());
            }catch(Exception e){
                LOG.warn("获取缓存失败", e);
            }
        }
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TICKET_KEY,AdminUtils.getSSOTicket());
        hashMap.put("User-Agent","Chrome");
        hashMap.put("tenant-id",tenantId);
        int retryCount = 0;
        while(retryCount < 3) {
            String string = OKHttpClient.doGet(SECURITY_HOST+TENANT_USER_DATABASE+userEmail,null,hashMap);
            HashMap<String,Object> databases = gson.fromJson(string, new TypeToken<HashMap<String,Object>>() {
            }.getType());
            ArrayList<String> strings = new ArrayList<>(databases.keySet());
            MetaspaceContext.set(key,strings);
            return strings;
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"从安全中心获取当前用户的hive库权限错误");
    }

    /**
     * 获取对应租户的所有库
     * @param tenantId
     * @return
     */
    public List<String> getDatabase(String tenantId) throws AtlasBaseException {
        Gson gson = new Gson();
        String key = String.format("getDatabase(tenantId:%s)",tenantId);
        Object o = MetaspaceContext.get(key);
        if (o!=null){
            try{
                return gson.fromJson(gson.toJson(o), new TypeToken<List<String>>(){}.getType());
            }catch(Exception e){
                LOG.warn("获取缓存失败", e);
            }
        }
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("User-Agent","Chrome");
        int retryCount = 0;
        HashMap<String,String> query = new HashMap<>();
        query.put("tenantId",tenantId);
        while(retryCount < 3) {
            String string = OKHttpClient.doGet(SECURITY_HOST+TENANT_DATABASE,query,hashMap);
            Map map = gson.fromJson(string, HashMap.class);
            Object data = map.get("data");
            List<String> dbs = new ArrayList<>();
            TenantDatabaseList tenantDatabaseList = gson.fromJson(gson.toJson(data), TenantDatabaseList.class);
            for (TenantDatabaseList.TenantDatabase tenantDatabase:tenantDatabaseList.getTenantDatabaseList()){
                if (tenantDatabase.getTenantId().equals(tenantId)){
                    dbs.addAll(tenantDatabase.getDatabases().stream().map(database -> database.getName()).collect(Collectors.toList()));
                    break;
                }
            }
            MetaspaceContext.set(key,dbs);
            return dbs;
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"从安全中心获取当前用户的hive库权限错误");
    }

    /**
     * 获取所有租户的权限库
     * @return
     */
    public TenantDatabaseList getDatabase() throws AtlasBaseException {
        Gson gson = new Gson();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("User-Agent","Chrome");
        int retryCount = 0;
        HashMap<String,String> query = new HashMap<>();
        while(retryCount < 3) {
            try {            String string = OKHttpClient.doGet(SECURITY_HOST+TENANT_DATABASE,query,hashMap);
                Map map = gson.fromJson(string, HashMap.class);
                Object data = map.get("data");
                TenantDatabaseList tenantDatabaseList = gson.fromJson(gson.toJson(data), TenantDatabaseList.class);
                return tenantDatabaseList;
            }catch (Exception e){
                LOG.error(e);
                throw e;
            }

        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"从安全中心获取当前用户的hive库权限错误");
    }
}
