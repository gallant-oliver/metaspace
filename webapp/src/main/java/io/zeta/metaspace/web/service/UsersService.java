package io.zeta.metaspace.web.service;

import com.google.common.collect.Lists;
import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.security.RoleResource;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.model.user.UserInfoGroup;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.service.dataquality.WarningGroupService;
import io.zeta.metaspace.web.util.ParamUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryPath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersService {
    private static final Logger LOG = LoggerFactory.getLogger(UsersService.class);
    @Autowired
    private TenantService tenantService;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private WarningGroupService warningGroupService;
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    BusinessCatalogueService businessCatalogueService;

    @Bean(name = "getUserService")
    public UsersService getUserService() {
        return usersService;
    }


    public void addUser(Map data) throws AtlasBaseException {
        try {
            String userId = data.get("AccountGuid").toString();
            String account = data.get("LoginEmail").toString();
            String displayName = data.get("DisplayName").toString();
            if (userDAO.ifUserExists(userId) == 0) {
                User user = new User();
                user.setUserId(userId);
                user.setAccount(account);
                user.setUsername(displayName);
                List<UserInfo.Role> roles = new ArrayList<>();
                UserInfo.Role role = new UserInfo.Role();
                roles.add(role);
                String msadmin = "msadmin";
                /*if (msadmin.equals(user.getUsername())) {
                    role.setRoleId(SystemRole.ADMIN.getCode());
                } else {
                    role.setRoleId(SystemRole.ADMIN.getCode());
                }*/

                role.setRoleId(SystemRole.ADMIN.getCode());
                user.setRoles(roles);
                userDAO.addUser(user);
            }
        } catch (Exception e) {
            LOG.error("添加用户失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加用户失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public UserInfo getUserInfoById(String userId) throws AtlasBaseException {
        try {
            UserInfo info = new UserInfo();
            //user
            User userTmp = userDAO.getUser(userId);
            UserInfo.User user = new UserInfo.User();
            user.setUserId(userTmp.getUserId());
            user.setUsername(userTmp.getUsername());
            user.setAccount(userTmp.getAccount());
            info.setUser(user);
            return info;
        } catch (Exception e) {
            LOG.error("获取用户信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户信息失败");
        }
    }

    public void addUserCategory(List list,CategoryEntityV2 entity,int categoryType,String tenantId){
        String guid = entity.getGuid();
        String name = entity.getName();
        String pathStr = categoryDAO.queryPathByGuid(guid,tenantId);
        String path = pathStr.substring(1, pathStr.length() - 1);
        path = path.replace(",", ".").replace("\"", "");
        String level2Category = null;
        String[] pathArr = path.split("\\.");
        int level = pathArr.length;
        int length = 2;
        if (level >= length) {
            level2Category = pathArr[1];
        }
        if (categoryType==0){
            UserInfo.Category category = new UserInfo.Category(guid, name, level, level2Category);
            list.add(category);
        }else if (categoryType==1){
            UserInfo.Category category = new UserInfo.Category(guid, name, level, level2Category);
            list.add(category);
        }
    }


    public PageResult<User> getUserList(Parameters parameters) throws AtlasBaseException {

        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        try {
            PageResult<User> userPageResult = new PageResult<>();
            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            List<User> userList = userDAO.getUserList(query, limit, offset);
            userPageResult.setLists(userList);
            long userTotalSize = 0;
            if (userList.size()!=0){
                userTotalSize = userList.get(0).getTotal();
            }
            userPageResult.setCurrentSize(userList.size());
            userPageResult.setTotalSize(userTotalSize);
            return userPageResult;
        } catch (Exception e) {
            LOG.error("获取用户列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户列表失败");
        }
    }

    public boolean ifPrivilege(List<String> categoryGuid, String tableGuid, String tenantId) {
        if (CollectionUtils.isEmpty(categoryGuid)) {
           return false;
        }
        Integer privilegeCount = userDAO.getPrivilegeFromTable(categoryGuid, tableGuid, tenantId);
        if(null != privilegeCount && privilegeCount>0){
            return true;
        }
        privilegeCount = userDAO.getPrivilegeFromDb(categoryGuid, tableGuid, tenantId);
        if(null != privilegeCount && privilegeCount>0){
            return true;
        }
        return false;
    }

    public Item getUserItems(String tenantId) throws AtlasBaseException {
        try {
            Item item = new Item();
            item.setModules(new HashSet<>(tenantService.getModule(tenantId)));
            return item;
        } catch (Exception e) {
            LOG.error("获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户功能模块失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public UserInfoGroup getUserInfoByIdV2(String tenantId,String userId) throws AtlasBaseException {
        try {
            UserInfoGroup info = new UserInfoGroup();
            //user
            User userTmp = userDAO.getUser(userId);
            UserInfo.User user = new UserInfo.User();
            user.setUserId(userTmp.getUserId());
            user.setUsername(userTmp.getUsername());
            user.setAccount(userTmp.getAccount());
            info.setUser(user);
            //userGroups
            List<CategoryPrivilege> technicalCategoryList = dataManageService.getTechnicalCategoryByUserId(tenantId, userId);
            List<String> guidList = technicalCategoryList.stream().map(item -> item.getGuid()).collect(Collectors.toList());
            List<CategoryPath> technicalPaths = categoryDAO.getPathByIds(guidList, 0, tenantId);
            Map<String,String> technicalPathMap = new HashMap<>();
            technicalPaths.forEach(path->{
                String categoryPath = path.getPath().replace("\"", "").replace("{", "").replace("}", "").replace(",", "/");
                technicalPathMap.put(path.getGuid(),categoryPath);
            });
            List<UserInfo.Category> technicals = new ArrayList<>();
            for (CategoryPrivilege categoryPrivilege : technicalCategoryList) {
                UserInfo.Category technicalCategory = new UserInfo.Category(categoryPrivilege);
                String path=technicalPathMap.get(categoryPrivilege.getGuid());
                technicalCategory.setPath(path);
                technicals.add(technicalCategory);
            }
            info.setTechnicalCategory(technicals);

            Map<String, CategoryPrivilegeV2> businessCategories = userGroupService.getUserPrivilegeCategory(tenantId, 1, false);
            List<UserInfo.Category> business = new ArrayList<>();
            if (businessCategories.size()!=0){
                List<CategoryPath> businessPaths = categoryDAO.getPathByIds(Lists.newArrayList(businessCategories.keySet()), 1, tenantId);
                Map<String,String> businessPathMap = new HashMap<>();
                businessPaths.forEach(path->{
                    String categoryPath = path.getPath().replace("\"", "").replace("{", "").replace("}", "").replace(",", "/");
                    businessPathMap.put(path.getGuid(),categoryPath);
                });
                for (CategoryPrivilegeV2 category:businessCategories.values()){
                    UserInfo.Category technicalCategory = new UserInfo.Category(category);
                    String path=businessPathMap.get(category.getGuid());
                    technicalCategory.setPath(path);
                    business.add(technicalCategory);
                }
            }
            info.setBusinessCategory(business);

            SecuritySearch search = new SecuritySearch();
            search.setUserName(user.getUsername());
            List<String> list = new ArrayList<>();
            list.add(user.getAccount());
            search.setEmails(list);
            search.setTenantId(tenantId);
            PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, search);
            Map<Integer,UserInfoGroup.Group> modules = new HashMap<>();
            for (UserAndModule userAndModule:userAndModules.getLists()){
                if (userAndModule.getUserName().equals(user.getUsername()) && userAndModule.getEmail().equals(user.getAccount()) && userAndModule.getToolRoleResources()!=null){
                    for (RoleResource roleResource:userAndModule.getToolRoleResources()){
                        ModuleEnum moduleEnum = ModuleEnum.getModuleEnum(roleResource);
                        if (moduleEnum!=null){
                            UserInfo.Module module = moduleEnum.getUserInfoModule();
                            if (modules.containsKey(module.getGroupId())){
                                modules.get(module.getGroupId()).getPrivilege().add(module);
                            }else{
                                UserInfoGroup.Group group = new UserInfoGroup.Group();
                                List<UserInfo.Module> moduleList = new ArrayList<>();
                                moduleList.add(module);
                                group.setGroupId(module.getGroupId());
                                group.setGroupName(module.getGroupName());
                                group.setPrivilege(moduleList);
                                modules.put(module.getGroupId(),group);
                            }
                        }
                    }

                }
            }
            /*
              指标目录的权限配置
              1.新建未分配的目录只有创建者可见，且只有目录编辑权
              2.分配了用户组，read，edit，editItem字段才不为null
             */
            List<CategorycateQueryResult> indicatorCategories = businessCatalogueService.getAllCategories(5, tenantId, userId);
            List<UserInfo.Category> indicatorCategory = new ArrayList<>();
            if (indicatorCategories != null && !indicatorCategories.isEmpty()) {
                List<String> categoryGuids = indicatorCategories.stream().map(CategorycateQueryResult::getGuid).collect(Collectors.toList());
                List<CategoryPath> indicatorPaths = categoryDAO.getPathByIds(categoryGuids, 5, tenantId);
                Map<String, String> indicatorPathMap = new HashMap<>();
                indicatorPaths.forEach(path -> {
                    String categoryPath = path.getPath().replace("\"", "").replace("{", "").replace("}", "").replace(",", "/");
                    indicatorPathMap.put(path.getGuid(), categoryPath);
                });
                indicatorCategory = indicatorCategories.stream().map(item -> {
                    UserInfo.Category category = new UserInfo.Category();
                    category.setGuid(item.getGuid());
                    category.setCategoryName(item.getName());
                    category.setRead(item.getRead());
                    category.setEditItem(item.getEditItem());
                    category.setEditCategory(item.getEditCategory());
                    category.setPath(indicatorPathMap.get(category.getGuid()));
                    return category;
                }).collect(Collectors.toList());
            }
            info.setIndicatorCategory(indicatorCategory);
            info.setModules(new ArrayList<>(modules.values()));
            info.setUserGroups(userDAO.getUserGroupNameByUserId(userId, tenantId));
            return info;
        } catch (AtlasBaseException e){
            throw e;
        } catch (Exception e) {
            LOG.error("获取用户信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户信息失败");
        }
    }

    public PageResult<User> getUserListV2(String tenantId,Parameters parameters) throws AtlasBaseException {


        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        SecuritySearch search = new SecuritySearch();
        search.setTenantId(tenantId);
        search.setUserName(parameters.getQuery());
        PageResult<User> userPageResult = new PageResult<>();
        List<User> users = new ArrayList<>();
        try {
            PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(offset, limit, search);
            for (UserAndModule userAndModule:userAndModules.getLists()){
                User user = userDAO.getUserByName(userAndModule.getUserName(), userAndModule.getEmail());
                if (user==null){
                    continue;
                }
                List<UserGroupIdAndName> userGroupName = userDAO.getUserGroupNameByUserId(user.getUserId(), tenantId);
                user.setUserGroups(userGroupName);
                users.add(user);
            }
            userPageResult.setLists(users);
            long userTotalSize = 0;
            if (users.size()!=0){
                userTotalSize = userAndModules.getTotalSize();
            }
            userPageResult.setCurrentSize(users.size());
            userPageResult.setTotalSize(userTotalSize);
            return userPageResult;
        } catch (AtlasBaseException e){
            throw e;
        }catch (Exception e) {
            LOG.error("获取用户列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户列表失败");
        }
    }

    public User getUserInfo(String userId){
        User userInfo = userDAO.getUserInfo(userId);
        return userInfo;
    }

    public void updateGroupByUser(String userId,List<String> userGroups,String tenantId) throws AtlasBaseException {
        userDAO.deleteGroupByUser(userId,tenantId);
        if (userGroups==null||userGroups.size()==0){
            return;
        }
        userDAO.addGroupByUser(userId,userGroups);
    }

    public List<String> getMailsByGroups(String[] toList) {
        List<WarningGroup> warningGroups = warningGroupService.getByIds(toList);
        List<String> userIds = new ArrayList<>();
        warningGroups.forEach(wg ->
                userIds.addAll(Arrays.asList(wg.getContacts().split(",")))
        );
        if (Boolean.TRUE.equals(ParamUtil.isNull(userIds))){
            return Collections.emptyList();
        }
        return userDAO.getUsersEmailByIds(userIds);
    }
}
