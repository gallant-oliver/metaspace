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


import com.google.common.collect.Lists;
import io.zeta.metaspace.model.datasource.DataSourceIdAndName;
import io.zeta.metaspace.model.datasource.SourceAndPrivilege;
import io.zeta.metaspace.model.datasource.DataSourceIdAndName;
import io.zeta.metaspace.model.datasource.SourceAndPrivilege;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.CategoryGroupAndUser;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.CategoryPrivilegeV2;
import io.zeta.metaspace.model.result.CategoryUpdate;
import io.zeta.metaspace.model.result.GroupPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.result.UpdateCategory;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.share.ProjectHeader;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupCategories;
import io.zeta.metaspace.model.usergroup.UserGroupPrivileges;
import io.zeta.metaspace.model.usergroup.UserPrivilegeDataSource;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupMemberSearch;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * @author lixiang03
 * @Data 2020/2/24 15:49
 */
@Service
public class UserGroupService {
    public static List<String> systemCategory = new ArrayList<String>(){{
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
    }};
    @Autowired
    UserGroupDAO userGroupDAO;
    @Autowired
    TenantService tenantService;
    @Autowired
    CategoryDAO categoryDAO;

    public PageResult<UserGroupListAndSearchResult> getUserGroupListAndSearch(String tenantId, int offset, int limit, String sortBy, String order, String query) {
        PageResult<UserGroupListAndSearchResult> commonResult = new PageResult<>();

        if (query != null) {
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
        }

        List<UserGroupListAndSearchResult> lists = userGroupDAO.getUserGroupSortByUpdateTime(tenantId, offset, limit, sortBy,order, query);


        if (lists == null || lists.size() == 0) {
            return commonResult;
        }


        for (UserGroupListAndSearchResult searchResult : lists) {
            String userName = userGroupDAO.getUserNameById(searchResult.getCreator());
            searchResult.setCreator(userName);
            String authorize = userGroupDAO.getUserNameById(searchResult.getAuthorize());
            searchResult.setAuthorize(authorize);
        }


        commonResult.setTotalSize(lists.get(0).getTotalSize());
        commonResult.setLists(lists);
        commonResult.setCurrentSize(lists.size());
        return commonResult;

    }


    /**
     * 二.用户组详情
     */
    public UserGroup getUserGroupByID(String id) {
        UserGroup userGroupMapperDetail = userGroupDAO.getUserGroupByID(id);
        String userName = userGroupDAO.getUserNameById(userGroupMapperDetail.getCreator());
        userGroupMapperDetail.setCreator(userName);
        return userGroupMapperDetail;
    }

    /**
     * 二.用户组详情
     */
    public List<String> getUserGroupByIDs(List<String> ids) {
        if (ids==null||ids.size()==0){
            return new ArrayList<>();
        }
        List<UserGroup> userGroupMapperDetail = userGroupDAO.getUserGroupByIDs(ids);
        return userGroupMapperDetail.stream().map(userGroup -> userGroup.getName()).collect(Collectors.toList());
    }


    /**
     * 三.新建用户组
     */
    public void addUserGroup(String tenantId, UserGroup userGroup) throws AtlasBaseException {

        String uuID = UUID.randomUUID().toString();
        userGroup.setId(uuID);


        if (isNameById(tenantId, userGroup.getName(), userGroup.getId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户组名称" + userGroup.getName() + "已存在");
        }

        String creator = AdminUtils.getUserData().getUserId();
        userGroup.setCreator(creator);
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp currentTime = new Timestamp(currentTimeMillis);
        userGroup.setCreateTime(currentTime);
        userGroup.setUpdateTime(currentTime);

        userGroupDAO.addUserGroup(tenantId, userGroup);
    }

    //判断租户是否已经存在，true为存在，false为不存在
    public boolean isNameById(String tenantId, String name, String id) {
        Integer nameById = userGroupDAO.isNameById(tenantId, name, id);
        return nameById != 0;
    }

    /**
     * 四.删除用户组信息
     */
    @Transactional(rollbackFor=Exception.class)
    public void deleteUserGroupByID(String id) {

        userGroupDAO.deleteUserGroupByID(id);
        userGroupDAO.deleteUserGroupRelationByID(id);
        userGroupDAO.deleteCategoryGroupRelationByID(id);
        userGroupDAO.deleteUserGroupDataSourceRelationByID(id);
        userGroupDAO.deleteUserGroupProjectRelationByID(id);
    }

    /**
     * 五.用户组成员列表及搜索
     */

    public PageResult<MemberListAndSearchResult> getUserGroupMemberListAndSearch(String id,int offset, int limit, String search) {

        PageResult<MemberListAndSearchResult> commonResult = new PageResult<>();

        if (search != null) {
            search = search.replaceAll("%", "/%").replaceAll("_", "/_");
        }


        List<MemberListAndSearchResult> lists = userGroupDAO.getMemberListAndSearch(id,offset, limit, search);

        if (lists == null || lists.size() == 0) {
            return commonResult;
        }

        commonResult.setCurrentSize(lists.size());
        commonResult.setLists(lists);
        commonResult.setTotalSize(lists.get(0).getTotalSize());
        return commonResult;

    }


    /**
     * 六.用户组添加成员列表及搜索
     */

    public PageResult<UserGroupMemberSearch> getUserGroupMemberSearch(String tenantId, String groupId, int offset, int limit, String search) throws AtlasBaseException {
        PageResult<UserGroupMemberSearch> commonResult = new PageResult<>();

        if (search != null) {
            search = search.replaceAll("%", "/%").replaceAll("_", "/_");
        }

        List<String> userGroupId1 = new ArrayList<>();
        SecuritySearch securitySearch = new SecuritySearch();
        securitySearch.setUserName(search);
        securitySearch.setTenantId(tenantId);
        PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(offset, limit, securitySearch);


        for (UserAndModule userAndModule : userAndModules.getLists()) {
            userGroupId1.add(userAndModule.getUserName());
        }

        List<String> userGroupId2 = userGroupDAO.getUserNameByGroupId(tenantId, groupId);

        List<String> userNameList = userGroupId1.stream().filter(str -> !userGroupId2.stream().anyMatch(s -> s.equals(str))).collect(Collectors.toList());

        List<UserGroupMemberSearch> lists = userGroupDAO.getUserGroupMemberSearch(userNameList, offset, limit);


        if (lists == null || lists.size() == 0) {
            return commonResult;
        }
        commonResult.setLists(lists);
        commonResult.setCurrentSize(lists.size());
        commonResult.setTotalSize(lists.get(0).getTotalSize());
        return commonResult;

    }


    /**
     * 七.用户组添加成员
     */

    public void addUserGroupByID(String groupId, List<String> userIds) {
        if (userIds == null||userIds.size()==0) {
            return;
        }
        List<String> userIdByUserGroup = userGroupDAO.getUserIdByUserGroup(groupId);
        List<String> filterUserIds = userIds.stream().filter(s -> !userIdByUserGroup.contains(s)).collect(Collectors.toList());
        if (filterUserIds!=null&&filterUserIds.size()!=0){
            userGroupDAO.addUserGroupByID(groupId, filterUserIds);
        }
    }


    /**
     * 八.用户组移除成员
     */
    public void deleteUserByGroupId(String groupId, List<String> userIds) {
        if (userIds == null|| userIds.size()==0) {
            return;
        }
        userGroupDAO.deleteUserByGroupId(groupId, userIds);

    }


    /**
     * 十五.修改用户组管理信息
     */

    public void updateUserGroupInformation(String groupId, UserGroup userGroup) throws AtlasBaseException {

        //规定：用户组名称长度范围必须是大于0且小于等于64，描述的长度范围必须是大于等于0且小于等于256
        if (!existGroupId(groupId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "您的用户组id:" + groupId + "不存在，无法修改用户组管理信息，请确保您的用户组id输入正确!");
        }
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp updateTime = new Timestamp(currentTimeMillis);

        userGroupDAO.updateUserGroupInformation(groupId, userGroup, updateTime);

    }


    //判断用户组Id是否已经存在，true为存在，false为不存在
    public boolean existGroupId(String groupId) {
        Integer id = userGroupDAO.existGroupId(groupId);
        return id != 0;
    }



    /**
     * 获取用户目录权限
     * @param userGroupId
     * @param tenant
     * @param all
     * @return
     */
    @Transactional(rollbackFor=Exception.class)
    public UserGroupCategories getPrivileges(String userGroupId,String tenant,boolean all) throws AtlasBaseException {
        UserGroupCategories userGroupCategories = new UserGroupCategories();
        User user = AdminUtils.getUserData();
        List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenant);
        boolean isnull = userGroups == null || userGroups.size() == 0;
        if(isnull && all == false) {
            return userGroupCategories;
        }

        List<String> userGroupIds = userGroups.stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());

        List<RoleModulesCategories.Category> bcategorys = getCategorys(userGroupId, userGroupIds, 1,tenant,all);

        userGroupCategories.setBusinessCategories(bcategorys);
        List<RoleModulesCategories.Category> tcategorys = getCategorys(userGroupId, userGroupIds, 0,tenant,all);
        userGroupCategories.setTechnicalCategories(tcategorys);
        userGroupCategories.setEdit(1);
        return userGroupCategories;
    }

    @Transactional(rollbackFor=Exception.class)
    public List<RoleModulesCategories.Category> getCategorys(String userGroupId, List<String> userGroupIds, int categorytype,String tenant,boolean all) {
        //用户有权限的Category
        //上级不打勾，不展示，去重;同级，下级不打勾，展示
        Map<String, RoleModulesCategories.Category> userCategorys = new HashMap<>();
        if (all) {
            List<RoleModulesCategories.Category> allCategorys = userGroupDAO.getAllCategorys(categorytype, tenant);
            setMap(userCategorys, allCategorys, 0, true);
        }
        for (String userGroup:userGroupIds){
            Map<String, RoleModulesCategories.Category> userGroupCategorys = getUserStringCategoryMap(userGroup, categorytype,tenant);
            for (Map.Entry<String, RoleModulesCategories.Category> stringCategoryEntry : userGroupCategorys.entrySet()) {
                String key = stringCategoryEntry.getKey();
                RoleModulesCategories.Category value = stringCategoryEntry.getValue();
                if (userCategorys.containsKey(key)) {
                    value.setShow(userCategorys.get(key).isShow() || value.isShow());
                }
                userCategorys.put(key,value);
            }
        }


        //用户组有权限的Category
        //上级不打勾，不展示，去重;同级，下级打勾，不展示
        Map<String, RoleModulesCategories.Category> categorys = getGroupStringCategoryMap(userGroupId, categorytype,tenant);

        //结果合并
        //上级合并去重；同级，下级有相同的，或逻辑判断打勾和展示并合并；最后上下级合并
        //求交集
        Map<String, RoleModulesCategories.Category> result = new HashMap<>();
        for (Map.Entry<String, RoleModulesCategories.Category> stringCategoryEntry : categorys.entrySet()) {
            String key = stringCategoryEntry.getKey();
            RoleModulesCategories.Category value = stringCategoryEntry.getValue();
            if (userCategorys.containsKey(key)) {
                value.setShow(userCategorys.get(key).isShow());
            }
            result.put(key, value);

        }
        //合并剩下的子集
        for (Map.Entry<String, RoleModulesCategories.Category> stringCategoryEntry : userCategorys.entrySet()) {
            String key = stringCategoryEntry.getKey();
            RoleModulesCategories.Category value = stringCategoryEntry.getValue();
            if (!result.containsKey(key)) {
                result.put(key, value);
            }
        }

        List<RoleModulesCategories.Category> resultList = new ArrayList<>(result.values());
        setOtherCategory(categorytype, resultList,tenant);
        CategoryRelationUtils.cleanInvalidBrother(resultList);
        return resultList;
    }

    /**
     * 获取用户组权限
     * @param userGroupId
     * @param categorytype
     * @param tenant
     * @return
     */
    @Transactional(rollbackFor=Exception.class)
    public Map<String, RoleModulesCategories.Category> getGroupStringCategoryMap(String userGroupId, int categorytype,String tenant) {
        Map<String, RoleModulesCategories.Category> categorys = new HashMap<>();
        List<String> businessCategories = userGroupDAO.getCategorysByTypeIds(userGroupId, categorytype,tenant);
        if (businessCategories.size() > 0) {
            List<RoleModulesCategories.Category> childCategorys = userGroupDAO.getChildCategorys(businessCategories, categorytype,tenant);
            List<RoleModulesCategories.Category> parentCategorys = userGroupDAO.getParentCategorys(businessCategories, categorytype,tenant);
            List<RoleModulesCategories.Category> privilegeCategorys = userGroupDAO.getCategorysByType(userGroupId, categorytype,tenant);
            //得到角色的带权限的目录树
            setMap(categorys, childCategorys, 1, false);
            setMap(categorys, parentCategorys, 0, false);
            setMap(categorys, privilegeCategorys, 1, false);
        }
        return categorys;
    }

    /**
     * 获取用户权限
     * @param userGroupId
     * @param categorytype
     * @param tenant
     * @return
     */
    @Transactional(rollbackFor=Exception.class)
    public Map<String, RoleModulesCategories.Category> getUserStringCategoryMap(String userGroupId, int categorytype,String tenant) {
        Map<String, RoleModulesCategories.Category> userCategorys = new HashMap<>();
        List<String> userBusinessCategories = userGroupDAO.getCategorysByTypeIds(userGroupId, categorytype,tenant);
        if (userBusinessCategories.size() > 0) {
            List<RoleModulesCategories.Category> userChildCategorys = userGroupDAO.getChildCategorys(userBusinessCategories, categorytype,tenant);
            List<RoleModulesCategories.Category> userParentCategorys = userGroupDAO.getParentCategorys(userBusinessCategories, categorytype,tenant);
            List<RoleModulesCategories.Category> userPrivilegeCategorys = userGroupDAO.getCategorysByType(userGroupId, categorytype,tenant);
            //得到用户的带权限的目录树
            setMap(userCategorys, userChildCategorys, 0, true);
            setMap(userCategorys, userParentCategorys, 0, false);
            setMap(userCategorys, userPrivilegeCategorys, 0, true);
        }

        return userCategorys;
    }
    private void setMap(Map<String, RoleModulesCategories.Category> categorys, List<RoleModulesCategories.Category> allCategorys, int status, boolean show) {
        for (RoleModulesCategories.Category allCategory : allCategorys) {
            RoleModulesCategories.Category category = new RoleModulesCategories.Category(allCategory);
            category.setStatus(status);
            category.setShow(show);
            category.setHide(false);
            categorys.put(category.getGuid(), category);
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void setOtherCategory(int categorytype, List<RoleModulesCategories.Category> resultList,String tenantId) {
        List<RoleModulesCategories.Category> otherCategorys = userGroupDAO.getOtherCategorys(resultList, categorytype,tenantId);
        for (RoleModulesCategories.Category otherCategory : otherCategorys) {
            otherCategory.setShow(false);
            otherCategory.setHide(true);
            otherCategory.setStatus(0);

        }
        resultList.addAll(otherCategorys);
    }

    /**
     * 修改用户组方案及授权范围
     * @param userGroupId
     * @param userGroupCategories
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor=Exception.class)
    public void putPrivileges(String userGroupId, UserGroupCategories userGroupCategories) throws AtlasBaseException {
        userGroupDAO.deleteUserGroup2category(userGroupId);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        userGroupDAO.updateCategory(userGroupId, currentTime,AdminUtils.getUserData().getUserId());
        List<RoleModulesCategories.Category> businessCategories = userGroupCategories.getBusinessCategories();
        List<RoleModulesCategories.Category> technicalCategories = userGroupCategories.getTechnicalCategories();
        if (businessCategories != null) {
            for (RoleModulesCategories.Category businessCategory : businessCategories) {
                userGroupDAO.addUserGroup2category(userGroupId, businessCategory.getGuid());
            }
        }

        //
        if (technicalCategories != null) {
            for (RoleModulesCategories.Category technicalCategory : technicalCategories) {
                userGroupDAO.addUserGroup2category(userGroupId, technicalCategory.getGuid());
            }
        }
    }


    /**
     * 获取用户目录树，有权限首级目录不能加关联
     * 1.4新权限 有管理目录权限的可以编辑目录和添加关联，其他人只能看
     * 业务目录的，有管理目录权限的编辑目录，有编辑业务信息权限的可以创建业务对象和编辑业务对象
     * @param userGroupId
     * @param categorytype
     * @param modulesByUser
     * @param tenantId
     * @return
     */
    @Transactional(rollbackFor=Exception.class)
    public List<CategoryPrivilege> getUserCategory(String userGroupId, int categorytype,List<Module> modulesByUser,String tenantId) throws AtlasBaseException {
        List<CategoryPrivilege> userCategorys = new ArrayList<>();
        List<Integer> modules = new ArrayList<>();
        for (Module module : modulesByUser) {
            modules.add(module.getModuleId());
        }
        int ruleType=4;
        int dateStanderType=3;
        if (dateStanderType == categorytype||ruleType == categorytype) {
            List<RoleModulesCategories.Category> allCategorys;
            if (categorytype==0){
                List<String> dbNames = tenantService.getDatabase(tenantId);
                allCategorys = userGroupDAO.getAllCategorysAndCount(categorytype,tenantId,dbNames);
            }else{
                allCategorys = userGroupDAO.getAllCategorysAndCount(categorytype,tenantId,new ArrayList<>());
            }
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
            addPrivilege(userCategorys, allCategorys, privilege, categorytype);
        } else {

            List<String> userBusinessCategories = userGroupDAO.getCategorysByTypeIds(userGroupId, categorytype,tenantId);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys;
                List<RoleModulesCategories.Category> userPrivilegeCategorys;
                if (categorytype==0){
                    List<String> dbNames = tenantService.getDatabase(tenantId);
                    userChildCategorys = userGroupDAO.getChildCategorysAndCount(userBusinessCategories, categorytype,tenantId,dbNames);
                    userPrivilegeCategorys = userGroupDAO.getCategorysByTypeAndCount(userGroupId, categorytype,tenantId,dbNames);
                }else{
                    userChildCategorys = userGroupDAO.getChildCategorysAndCount(userBusinessCategories, categorytype,tenantId,new ArrayList<>());
                    userPrivilegeCategorys = userGroupDAO.getCategorysByTypeAndCount(userGroupId, categorytype,tenantId,new ArrayList<>());
                }
                List<RoleModulesCategories.Category> userParentCategorys = userGroupDAO.getParentCategorys(userBusinessCategories, categorytype,tenantId);
                //按角色方案
                CategoryPrivilege.Privilege childPrivilege = null;
                CategoryPrivilege.Privilege parentPrivilege = null;
                CategoryPrivilege.Privilege ownerPrivilege = null;
                //技术目录
                switch (categorytype) {
                    //技术目录
                    case 0: {
                        //按角色方案
                        if (modules.contains(ModuleEnum.TECHNICALEDIT.getId()) && modules.contains(ModuleEnum.TECHNICALADMIN.getId())) {
                            //按勾选的目录
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, true, false, true, true, true,false);

                        } else if (modules.contains(ModuleEnum.TECHNICALADMIN.getId())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, false, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, false, false, false, false, true,false);
                        } else if (modules.contains(ModuleEnum.TECHNICALEDIT.getId())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                        } else {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                        }
                        break;
                    }
                    //业务目录
                    case 1: {
                        //按角色方案
                        if (modules.contains(ModuleEnum.BUSINESSEDIT.getId()) && modules.contains(ModuleEnum.BUSINESSADMIN.getId())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, true, true, true, true, true,false);
                        } else if (modules.contains(ModuleEnum.BUSINESSADMIN.getId())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, true, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, false, true, false, true, true,false);
                        } else if (modules.contains(ModuleEnum.BUSINESSEDIT.getId())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                        } else {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                        }
                        break;
                    }
                    default:break;
                }
                addPrivilege(userCategorys, userChildCategorys, childPrivilege, categorytype);
                addPrivilege(userCategorys, userParentCategorys, parentPrivilege, categorytype);
                addPrivilege(userCategorys, userPrivilegeCategorys, ownerPrivilege, categorytype);
            }
        }
        addOtherCategory(categorytype, userCategorys,tenantId);
        return userCategorys;
    }

    public List<CategoryPrivilege> getAdminCategory(int categorytype,String tenantId) throws AtlasBaseException {
        List<CategoryPrivilege> userCategorys = new ArrayList<>();
        List<RoleModulesCategories.Category> allCategorys;
        if (categorytype==0){
            List<String> dbNames = tenantService.getDatabase(tenantId);
            allCategorys = userGroupDAO.getAllCategorysAndCount(categorytype,tenantId,dbNames);
        }else{
            allCategorys = userGroupDAO.getAllCategorysAndCount(categorytype,tenantId,new ArrayList<>());
        }
        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, true, true, true, false, true, false, false, true,false);
        addPrivilege(userCategorys, allCategorys, privilege, categorytype);
        return userCategorys;
    }

    public List<CategoryPrivilege> getAdminCategoryView(int categoryType,String tenantId) throws AtlasBaseException {
        List<CategoryPrivilege> userCategories = getAdminCategory(categoryType,tenantId);
        for (CategoryPrivilege category:userCategories){
            category.getPrivilege().setAsh(false);
        }
        return userCategories;
    }

    private void addPrivilege(List<CategoryPrivilege> userCategorys, List<RoleModulesCategories.Category> allCategorys, CategoryPrivilege.Privilege privilege, int categorytype) {
        for (RoleModulesCategories.Category category : allCategorys) {
            CategoryPrivilege.Privilege privilegeinfo = new CategoryPrivilege.Privilege(privilege);
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(category);
            //系统系统目录不允许删除和编辑
            if (systemCategory.contains(category.getGuid())) {
                privilegeinfo.setDelete(false);
                if (privilegeinfo.isEdit()){
                    privilegeinfo.setEditSafe(true);
                }
                privilegeinfo.setEdit(false);
            }
            //技术目录一级目录不允许删关联
            if (categorytype == 0 && category.getLevel() == 1) {
                privilegeinfo.setDeleteRelation(false);
            }
            categoryPrivilege.setPrivilege(privilegeinfo);
            userCategorys.add(categoryPrivilege);
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void addOtherCategory(int categorytype, List<CategoryPrivilege> resultList,String tenantId) {
        List<RoleModulesCategories.Category> otherCategorys = userGroupDAO.getOtherCategorys2(resultList, categorytype,tenantId);
        ArrayList<CategoryPrivilege> others = new ArrayList<>();
        for (RoleModulesCategories.Category otherCategory : otherCategorys) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(otherCategory);
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(true, true, false, false, false, false, false, false, false,false);
            categoryPrivilege.setPrivilege(privilege);
            others.add(categoryPrivilege);
        }
        resultList.addAll(others);
    }

    /**
     * 获取权限数据源
     * @param groupId
     * @param offset
     * @param limit
     * @param search
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<SourceAndPrivilege> getSourceBySearch(String groupId, int offset, int limit, String search) {
        PageResult<SourceAndPrivilege> commonResult = new PageResult<>();

        if (search != null) {
            search = search.replaceAll("%", "/%").replaceAll("_", "/_");
        }
        List<SourceAndPrivilege> lists = userGroupDAO.getSourceBySearch(groupId, offset, limit, search);
        if (lists == null || lists.size() == 0) {
            return commonResult;
        }

        commonResult.setLists(lists);
        commonResult.setCurrentSize(lists.size());
        commonResult.setTotalSize(lists.get(0).getTotalSize());
        return commonResult;
    }

    /**
     * 无权限数据源
     * @param tenantId
     * @param groupId
     * @param offset
     * @param limit
     * @param search
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<DataSourceIdAndName> getNoSourceBySearch(String tenantId, String groupId, int offset, int limit, String search) {
        PageResult<DataSourceIdAndName> commonResult = new PageResult<>();

        if (search != null) {
            search = search.replaceAll("%", "/%").replaceAll("_", "/_");
        }
        List<DataSourceIdAndName> lists = null;
        lists = userGroupDAO.getNoSourceBySearch(tenantId, groupId, offset, limit, search);
        if (lists == null || lists.size() == 0) {
            return commonResult;
        }

        commonResult.setLists(lists);
        commonResult.setCurrentSize(lists.size());
        commonResult.setTotalSize(lists.get(0).getTotalSize());
        return commonResult;
    }

    /**
     * 添加数据源
     * @param groupId
     * @param privileges
     * @return
     * @throws AtlasBaseException
     */
    public void addDataSourceByGroupId(String groupId, UserGroupPrivileges privileges) {
        if (privileges.getSourceIds()==null||privileges.getSourceIds().size()==0){
            return;
        }
        userGroupDAO.addDataSourceByGroupId(groupId, privileges.getSourceIds(),privileges.getPrivilegeCode());
    }

    /**
     * 修改数据源权限
     * @param groupId
     * @param privileges
     * @return
     * @throws AtlasBaseException
     */
    public void updateDataSourceByGroupId(String groupId, UserGroupPrivileges privileges) {
        if (privileges.getSourceIds()==null||privileges.getSourceIds().size()==0){
            return;
        }
        userGroupDAO.updateDataSourceByGroupId(groupId,privileges);
    }

    /**删除数据源权限
     * @param groupId
     * @param sourceIds
     * @return
     * @throws AtlasBaseException
     */
    public void deleteDataSourceByGroupId(String groupId, List<String> sourceIds) {
        if (sourceIds == null||sourceIds.size()==0) {
            return;
        }
        userGroupDAO.deleteDataSourceByGroupId(groupId, sourceIds);
    }

    /**
     * 获取权限列表
     * @return
     */
    public List<Map<String, String>> getDataSourcePrivileges() {
        List<Map<String, String>> dataSourcePrivileges = new ArrayList<>();
        Map<String,String> read = new HashMap<>();
        read.put("privilegeName", UserPrivilegeDataSource.READ.getPrivilegeName());
        read.put("privilegeCode",UserPrivilegeDataSource.READ.getPrivilege());
        dataSourcePrivileges.add(read);
        Map<String,String> write = new HashMap<>();
        write.put("privilegeName", UserPrivilegeDataSource.WRITE.getPrivilegeName());
        write.put("privilegeCode",UserPrivilegeDataSource.WRITE.getPrivilege());
        dataSourcePrivileges.add(write);
        return dataSourcePrivileges;
    }

    /**
     * 判断目录是否是用户有权限的目录
     * @param userId
     * @param categoryId
     * @param tenantId
     * @param type
     * @return
     */
    public boolean isPrivilegeCategory(String userId,String categoryId,String tenantId,int type){
        //获取用户的权限节点目录
        List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(userId, tenantId);
        List<String> categoryIds = new ArrayList<>();
        for (UserGroup userGroup:userGroups){
            List<String> privilegeCategory = userGroupDAO.getCategorysByTypeIds(userGroup.getId(), type, tenantId);
            categoryIds.addAll(privilegeCategory);
        }
        if (categoryIds.size()==0){
            return false;
        }

        //如果在权限节点当中，则代表有权限
        if (categoryIds.contains(categoryId)){
            return true;
        }

        //获取当前目录的父目录，如果父目录在权限节点当中，则有权限，否则无权限
        ArrayList<String> list = new ArrayList<String>() {
            {
                add(categoryId);
            }
        };
        List<RoleModulesCategories.Category> parentCategorys = userGroupDAO.getParentCategorys(list, type, tenantId);
        if (parentCategorys==null||parentCategorys.size()==0){
            return false;
        }
        for (RoleModulesCategories.Category category:parentCategorys){
            if (categoryIds.contains(category.getGuid())){
                return true;
            }
        }

        return false;
    }

    /**
     * 添加项目
     * @param groupId
     * @param projectIds
     * @return
     * @throws AtlasBaseException
     */
    public void addProjectByGroupId(String groupId, List<String> projectIds) {
        if (projectIds==null||projectIds.size()==0){
            return;
        }
        userGroupDAO.addProjectByGroupId(groupId, projectIds);
    }

    /**
     * 获取项目列表
     * @param isPrivilege
     * @param groupId
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<ProjectHeader> getProject(boolean isPrivilege, String groupId, Parameters parameters, String tenantId) throws AtlasBaseException {
        PageResult pageResult = new PageResult();
        List<ProjectHeader> userGroups;
        if (isPrivilege==false && groupId==null){
            userGroups=userGroupDAO.getAllProject(parameters,tenantId);
        }else if (isPrivilege==false){
            userGroups=userGroupDAO.getNoRelationProject(groupId,parameters,tenantId);
        }else {
            userGroups=userGroupDAO.getRelationProject(groupId,parameters,tenantId);
        }
        if (userGroups==null||userGroups.size()==0){
            return pageResult;
        }
        pageResult.setCurrentSize(userGroups.size());
        pageResult.setLists(userGroups);
        pageResult.setTotalSize(userGroups.get(0).getTotalSize());
        return pageResult;
    }

    /**
     * 批量删除权限项目
     * @param projects
     * @param userGroupId
     * @throws AtlasBaseException
     */
    public void deleteProject(List<String> projects,String userGroupId) throws AtlasBaseException {
        if (projects!=null&&projects.size()!=0){
            userGroupDAO.deleteProjectToUserGroup(userGroupId,projects);
        }
    }

    /**
     * 变更目录权限
     * @param category
     * @param userGroupId
     * @param categoryType
     * @param tenantId
     * @throws SQLException
     */
    public List<CategoryPrivilegeV2> updatePrivileges(CategoryPrivilegeV2 category,String userGroupId,int categoryType,String tenantId,boolean isChild) throws SQLException {
        return updatePrivileges(category,userGroupId,categoryType,tenantId,isChild,new ArrayList<>());
    }

    @Transactional(rollbackFor=Exception.class)
    public List<CategoryPrivilegeV2> updatePrivileges(CategoryPrivilegeV2 category,String userGroupId,int categoryType,String tenantId,boolean isChild,List<String> updateIds) throws SQLException {
        CategoryEntityV2 categoryByGuid = categoryDAO.queryByGuid(category.getGuid(), tenantId);

        //权限校验
        privilegeCheck(category,categoryByGuid.getParentCategoryGuid(),userGroupId);

        ArrayList<String> categoryList = Lists.newArrayList(category.getGuid());
        List<CategoryPrivilegeV2> childCategoriesPrivileges = userGroupDAO.getChildCategoriesPrivileges(categoryList,userGroupId, categoryType, tenantId);
        List<String> childIds = childCategoriesPrivileges.stream().map(categoryPrivilegeV2 -> categoryPrivilegeV2.getGuid()).collect(Collectors.toList());
        for (String updateId:updateIds){
            if (childIds.contains(updateId)){
                categoryList.add(updateId);
            }
        }
        if (!category.getRead()){
            if (isChild){
                for (CategoryPrivilegeV2 childCategory:childCategoriesPrivileges){
                    childCategory.setRead(false);
                    childCategory.setEditCategory(false);
                    childCategory.setEditItem(false);
                }
                userGroupDAO.deleteCategoryPrivilege(childIds,userGroupId);
                return childCategoriesPrivileges;
            }else{
                userGroupDAO.deleteCategoryPrivilege(categoryList,userGroupId);
                return Lists.newArrayList(category);
            }
        }
        List<String> updateCategory = new ArrayList<>();
        List<String> insertCategory = new ArrayList<>();
        for (CategoryPrivilegeV2 childCategory:childCategoriesPrivileges){
            if (childCategory.getRead()==null){
                insertCategory.add(childCategory.getGuid());
                childCategory.setRead(true);
                childCategory.setEditCategory(category.getEditCategory());
                childCategory.setEditItem(category.getEditItem());
            }else{
                updateCategory.add(childCategory.getGuid());
                childCategory.setRead(true);
                childCategory.setEditCategory(category.getEditCategory()||childCategory.getEditCategory());
                childCategory.setEditItem(category.getEditItem()||childCategory.getEditItem());
            }
            if (childCategory.getGuid().equals(category.getGuid())){
                childCategory.setRead(true);
                childCategory.setEditCategory(category.getEditCategory());
                childCategory.setEditItem(category.getEditItem());
            }
        }

        if (updateCategory.size()!=0){
            if (isChild){
                userGroupDAO.updateMandatoryChildCategoryPrivileges(updateCategory,userGroupId,category);
            }else{
                userGroupDAO.updateChildCategoryPrivileges(updateCategory,userGroupId,category);
            }
        }
        if (insertCategory.size()!=0){
            userGroupDAO.addCategoryPrivileges(insertCategory,userGroupId,category);
        }
        userGroupDAO.updateCategoryPrivileges(categoryList,userGroupId,category);
        return childCategoriesPrivileges;
    }

    /**
     * 分配目录权限
     * @param category
     * @param userGroupId
     * @param tenantId
     * @throws SQLException
     */
    @Transactional(rollbackFor=Exception.class)
    public void addPrivileges(UpdateCategory category, String userGroupId, String tenantId) throws SQLException {
        //权限校验
        if (category.getRead()==null||!category.getRead()){
            return;
        }

        ArrayList<String> categorList = Lists.newArrayList(category.getGuid());
        List<CategoryPrivilegeV2> childCategoriesPrivileges = userGroupDAO.getChildCategoriesPrivileges(categorList,userGroupId, category.getType(), tenantId);
        List<String> updateCategory = new ArrayList<>();
        List<String> insertCategory = new ArrayList<>();
        for (CategoryPrivilegeV2 childCategory:childCategoriesPrivileges){
            if (childCategory.getRead()==null){
                insertCategory.add(childCategory.getGuid());
            }else{
                updateCategory.add(childCategory.getGuid());
            }
        }
        CategoryPrivilegeV2 categoryPrivilege = new CategoryPrivilegeV2(category);
        if (updateCategory.size()!=0){
            userGroupDAO.updateChildCategoryPrivileges(updateCategory,userGroupId,categoryPrivilege);
        }
        if (insertCategory.size()!=0){
            userGroupDAO.addCategoryPrivileges(insertCategory,userGroupId,categoryPrivilege);
        }
        userGroupDAO.updateCategoryPrivileges(categorList,userGroupId,categoryPrivilege);
    }

    /**
     * 父目录权限校验
     * @param category
     * @param parentCategoryGuid
     * @param userGroupId
     */
    public void privilegeCheck(CategoryPrivilegeV2 category,String parentCategoryGuid,String userGroupId){
        if (parentCategoryGuid==null||parentCategoryGuid.length()==0){
            return;
        }
        CategoryPrivilegeV2 parentCategory = userGroupDAO.getCategoriesPrivileges(parentCategoryGuid, userGroupId);
        if (parentCategory==null||parentCategory.getRead()==null){
            return;
        }
        if (parentCategory.getRead()&&!category.getRead()){
            category.setRead(true);
        }
        if (parentCategory.getEditCategory()&&!category.getEditCategory()){
            category.setEditCategory(true);
        }
        if (parentCategory.getEditItem()&&!category.getEditItem()){
            category.setEditItem(true);
        }

    }


    public PageResult<CategoryUpdate> getUpdateCategory(CategoryPrivilegeV2 category,String userGroupId,int categoryType,String tenantId,int limit,int offset,boolean isChild) throws SQLException, AtlasBaseException {
        PageResult<CategoryUpdate> pageResult = new PageResult<>();
        List<CategoryUpdate> categoryUpdates = new ArrayList<>();
        CategoryEntityV2 categoryByGuid = categoryDAO.queryByGuid(category.getGuid(), tenantId);
        //权限校验
        privilegeCheck(category,categoryByGuid.getParentCategoryGuid(),userGroupId);

        if (!category.getRead()&&isChild){
            CategoryPrivilegeV2 oldCategory = userGroupDAO.getCategoriesPrivileges(category.getGuid(), userGroupId);
            CategoryUpdate categoryUpdate = new CategoryUpdate(oldCategory);
            categoryUpdate.setOldPrivilege(oldCategory);
            categoryUpdate.setNewPrivilege(category);
            categoryUpdates.add(categoryUpdate);
            pageResult.setCurrentSize(categoryUpdates.size());
            pageResult.setLists(categoryUpdates);
            pageResult.setTotalSize(1);
            return pageResult;
        }

        List<CategoryPrivilegeV2> updateCategory = isChild?userGroupDAO.getMandatoryUpdateChildCategoriesPrivileges(category,userGroupId, categoryType, tenantId,limit,offset):
                                                   userGroupDAO.getUpdateChildCategoriesPrivileges(category,userGroupId, categoryType, tenantId,limit,offset);
        if (updateCategory==null||updateCategory.size()==0){
            pageResult.setLists(new ArrayList<>());
            return pageResult;
        }

        List<String> categoryIds = updateCategory.stream().map(categoryPrivilegeV2 -> categoryPrivilegeV2.getGuid()).collect(Collectors.toList());
        List<CategoryPath> paths = categoryDAO.getPathByIds(categoryIds,categoryType, tenantId);
        Map<String,String> pathMap = new HashMap<>();
        paths.forEach(path->{
            String categoryPath = path.getPath().replace("\"", "").replace("{", "").replace("}", "").replace(",", "/");
            pathMap.put(path.getGuid(),categoryPath);
        });

        for (CategoryPrivilegeV2 categoryPrivilegeV2:updateCategory){
            CategoryUpdate categoryUpdate = new CategoryUpdate(categoryPrivilegeV2);
            categoryUpdate.setOldPrivilege(categoryPrivilegeV2);
            CategoryPrivilegeV2 newPrivilege = new CategoryPrivilegeV2();
            if (isChild){
                newPrivilege=category;
            }else{
                if (category.getRead()&&!categoryPrivilegeV2.getRead()){
                    newPrivilege.setRead(true);
                }else{
                    newPrivilege.setRead(categoryPrivilegeV2.getRead());
                }
                if (category.getEditCategory()&&!categoryPrivilegeV2.getEditCategory()){
                    newPrivilege.setEditCategory(true);
                }else{
                    newPrivilege.setEditCategory(categoryPrivilegeV2.getEditCategory());
                }
                if (category.getEditItem()&&!categoryPrivilegeV2.getEditItem()){
                    newPrivilege.setEditItem(true);
                }else{
                    newPrivilege.setEditItem(categoryPrivilegeV2.getEditItem());
                }
            }
            categoryUpdate.setNewPrivilege(newPrivilege);
            categoryUpdate.setPath(pathMap.get(category.getGuid()));
            categoryUpdates.add(categoryUpdate);
        }
        pageResult.setCurrentSize(categoryUpdates.size());
        pageResult.setLists(categoryUpdates);
        pageResult.setTotalSize(updateCategory.get(0).getTotal());
        return pageResult;
    }

    /**
     * 用户组目录权限展示获取
     * @param groupId
     * @param type
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public List<CategoryGroupAndUser> getPrivilegeCategory(String groupId,int type,String tenantId,boolean isAll) throws AtlasBaseException {
        List<CategoryGroupAndUser> categories = new ArrayList<>();
        List<CategoryPrivilegeV2> userGroupCategory = Lists.newArrayList(getUserGroupPrivilegeCategory(groupId,tenantId,type).values());


        Map<String,CategoryPrivilegeV2> userMap = getUserPrivilegeCategory(tenantId,type,isAll);

        //合并用户组和用户目录权限
        for (CategoryPrivilegeV2 category:userGroupCategory){
            boolean delete = true;
            CategoryGroupAndUser groupAndUser = new CategoryGroupAndUser(category);
            if (category.getRead()){
                groupAndUser.setGroup(true);
            }
            groupAndUser.setGroupPrivilege(category);
            CategoryPrivilegeV2 userCategory = userMap.get(category.getGuid());
            groupAndUser.setUser(true);
            if (userCategory==null){
                groupAndUser.setUser(false);
                userCategory=new CategoryPrivilegeV2(category);
            }
            if (userCategory.getRead()==null||!userCategory.getRead()){
                groupAndUser.setUser(false);
                userCategory.setRead(false);
                userCategory.setEditItem(false);
                userCategory.setEditCategory(false);
            }

            //判断是否可以移除
            delete=isDeleteCategory(Lists.newArrayList(category.getGuid()),userMap,groupId,type,tenantId);

            groupAndUser.setDelete(delete);
            groupAndUser.setUserPrivilege(userCategory);
            categories.add(groupAndUser);
        }

        return categories;
    }

    public boolean isDeleteCategory(List<String> categoryIds,Map<String,CategoryPrivilegeV2> categoryPrivilege,String userGroupId,int type,String tenantId){
        if (categoryIds==null||categoryIds.size()==0){
            return true;
        }
        List<CategoryPrivilegeV2> childCategories = userGroupDAO.getChildCategoriesPrivileges(categoryIds,userGroupId, type, tenantId);
        for (CategoryPrivilegeV2 category : childCategories) {
            CategoryPrivilegeV2 userCategory = categoryPrivilege.get(category.getGuid());
            if (userCategory==null){
                return false;
            }
            if (category.getRead()==null||!category.getRead()){
                continue;
            }
            if (category.getRead()&&!(userCategory.getRead())){
                return false;
            }
            if (category.getEditCategory()&&!userCategory.getEditCategory()){
                return false;
            }
            if (category.getEditItem()&&!userCategory.getEditItem()){
                return false;
            }
        }
        return true;
    }
    /**
     * 用户组权限分配展示列表获取
     * @param groupId
     * @param type
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public List<CategoryGroupAndUser> getNoPrivilegeCategory(String groupId,int type,String tenantId,boolean isAll) throws AtlasBaseException {
        List<CategoryGroupAndUser> categories = new ArrayList<>();
        //获取用户权限目录
        List<CategoryPrivilegeV2> userCategory = Lists.newArrayList(getUserPrivilegeCategory(tenantId,type,isAll).values());

        //获取用户组权限目录
        Map<String,CategoryPrivilegeV2> userGroupMap = getUserGroupPrivilegeCategory(groupId,tenantId,type);

        //合并用户和用户组目录权限，以用户为主
        for (CategoryPrivilegeV2 category:userCategory){
            CategoryGroupAndUser groupAndUser = new CategoryGroupAndUser(category);
            if (category.getRead()){
                groupAndUser.setUser(true);
            }
            groupAndUser.setUserPrivilege(category);
            CategoryPrivilegeV2 userGroupCategory = userGroupMap.get(category.getGuid());

            groupAndUser.setGroup(true);
            if (userGroupCategory==null){
                groupAndUser.setGroup(false);
                userGroupCategory=new CategoryPrivilegeV2(category);
            }
            if (userGroupCategory.getRead()==null||!userGroupCategory.getRead()){
                groupAndUser.setGroup(false);
                userGroupCategory.setRead(false);
                userGroupCategory.setEditItem(false);
                userGroupCategory.setEditCategory(false);

            }
            groupAndUser.setGroupPrivilege(userGroupCategory);
            categories.add(groupAndUser);
        }

        return categories;
    }

    /**
     * 获取用户组目录权限列表
     * @param groupId
     * @param tenantId
     * @param type
     * @return
     */
    public Map<String,CategoryPrivilegeV2> getUserGroupPrivilegeCategory(String groupId,String tenantId,int type){
        Map<String,CategoryPrivilegeV2> groupMap = new HashMap<>();

        //获取用户组权限
        List<CategoryPrivilegeV2> parentCategoryNoPrivilege;
        List<CategoryPrivilegeV2> userGroupCategory = userGroupDAO.getUserGroupCategory(groupId, tenantId, type);
        if (userGroupCategory==null||userGroupCategory.size()==0){
            return new HashMap<>();
        }else{
            List<String> categoryIds = userGroupCategory.stream().map(category->category.getGuid()).collect(Collectors.toList());

            //获取父目录权限
            parentCategoryNoPrivilege = userGroupDAO.getParentCategory(categoryIds, type, tenantId);
        }
        parentCategoryNoPrivilege.forEach(categoryPrivilegeV2 -> {
            categoryPrivilegeV2.setRead(false);
            categoryPrivilegeV2.setEditItem(false);
            categoryPrivilegeV2.setEditCategory(false);
        });
        userGroupCategory.addAll(parentCategoryNoPrivilege);

        //遍历并合并目录权限
        for (CategoryPrivilegeV2 userCategory:userGroupCategory){
            if (groupMap.containsKey(userCategory.getGuid())){
                CategoryPrivilegeV2 categoryPrivilegeV2 = groupMap.get(userCategory.getGuid());
                if (userCategory.getRead()!=null&&userCategory.getRead()){
                    categoryPrivilegeV2.setRead(true);
                }
                if (userCategory.getEditCategory()!=null&&userCategory.getEditCategory()){
                    categoryPrivilegeV2.setEditCategory(true);
                }
                if (userCategory.getEditItem()!=null&&userCategory.getEditItem()){
                    categoryPrivilegeV2.setEditItem(true);
                }
            }else{
                groupMap.put(userCategory.getGuid(),userCategory);
            }
        }
        return groupMap;
    }



    /**
     * 获取用户权限目录列表
     * @param tenantId
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    public Map<String,CategoryPrivilegeV2> getUserPrivilegeCategory(String tenantId,int type,boolean isAll) throws AtlasBaseException {
        List<CategoryPrivilegeV2> userCategories=null;
        Map<String,CategoryPrivilegeV2> userMap = new HashMap<>();
        if (isAll){
            userCategories=userGroupDAO.getAllCategoryPrivilege(type,tenantId);
        }else{
            User user = AdminUtils.getUserData();
            //获取用户组
            List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            if (userGroupIds==null||userGroupIds.size()==0){
                return userMap;
            }
            //获取用户组对应的权限目录
            userCategories = userGroupDAO.getUserGroupsCategory(userGroupIds, tenantId, type);
            List<String> categoryIds = userCategories.stream().map(category->category.getGuid()).collect(Collectors.toList());

            if (categoryIds==null||categoryIds.size()==0){
                return userMap;
            }

            //获取父目录并配置权限
            List<CategoryPrivilegeV2> parentCategory = userGroupDAO.getParentCategory(categoryIds, type, tenantId);
            parentCategory.forEach(categoryPrivilegeV2 -> {
                categoryPrivilegeV2.setRead(false);
                categoryPrivilegeV2.setEditItem(false);
                categoryPrivilegeV2.setEditCategory(false);
            });
            userCategories.addAll(parentCategory);
        }
        //遍历并合并目录权限
        for (CategoryPrivilegeV2 userCategory:userCategories){
            if (userMap.containsKey(userCategory.getGuid())){
                CategoryPrivilegeV2 categoryPrivilegeV2 = userMap.get(userCategory.getGuid());
                if (userCategory.getRead()!=null&&userCategory.getRead()){
                    categoryPrivilegeV2.setRead(true);
                }
                if (userCategory.getEditCategory()!=null&&userCategory.getEditCategory()){
                    categoryPrivilegeV2.setEditCategory(true);
                }
                if (userCategory.getEditItem()!=null&&userCategory.getEditItem()){
                    categoryPrivilegeV2.setEditItem(true);
                }
            }else{
                userMap.put(userCategory.getGuid(),userCategory);
            }
        }
        return userMap;
    }

    @Transactional(rollbackFor=Exception.class)
    public void deleteCategoryPrivilege(List<String> categoryIds,String userGroupId,String tenantId) throws SQLException, AtlasBaseException {
        if (categoryIds==null||categoryIds.size()==0){
            return;
        }
        CategoryEntityV2 firstCategory = categoryDAO.queryByGuid(categoryIds.get(0), tenantId);
        Map<String, CategoryPrivilegeV2> userMap = getUserPrivilegeCategory(tenantId, firstCategory.getCategoryType(), false);
        if (!isDeleteCategory(categoryIds,userMap,userGroupId,firstCategory.getCategoryType(),tenantId)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "权限不足，无法移除，请检查对用户对当前目录或子目录的权限");
        }
        for (String categoryId:categoryIds){
            CategoryEntityV2 categoryByGuid = categoryDAO.queryByGuid(categoryId, tenantId);
            String parentCategoryGuid = categoryByGuid.getParentCategoryGuid();
            if (parentCategoryGuid!=null&&parentCategoryGuid.length()!=0){
                CategoryPrivilegeV2 parentCategory = userGroupDAO.getCategoriesPrivileges(parentCategoryGuid, userGroupId);
                if (parentCategory!=null&&parentCategory.getRead()!=null&&parentCategory.getRead()){
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "父目录有权限，无法移除");
                }
            }

            ArrayList<String> categorList = Lists.newArrayList(categoryId);
            List<CategoryPrivilegeV2> childCategoriesPrivileges = userGroupDAO.getChildCategoriesPrivileges(categorList,userGroupId, categoryByGuid.getCategoryType(), tenantId);

            List<String> ids = childCategoriesPrivileges.stream().map(categoryPrivilegeV2 -> categoryPrivilegeV2.getGuid()).collect(Collectors.toList());
            userGroupDAO.deleteCategoryPrivilege(ids,userGroupId);
        }
    }

    public List<CategoryPrivilege> getUserCategories(int categoryType,String tenantId) throws AtlasBaseException {
        Map<String, CategoryPrivilegeV2> userCategoryMap;
        List<CategoryPrivilege> userCategorys = new ArrayList<>();
        if (categoryType==3||categoryType==4){
            userCategoryMap = getUserPrivilegeCategory(tenantId, categoryType, true);
        }else{
            userCategoryMap = getUserPrivilegeCategory(tenantId, categoryType, false);
        }
        for (CategoryPrivilegeV2 category : userCategoryMap.values()) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(category);
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege();
            if (category.getRead()){
                privilege.setHide(false);
                privilege.setAsh(false);
            }else{
                privilege.setHide(false);
                privilege.setAsh(true);
            }
            CategoryPrivilegeV2 parentCategory = userCategoryMap.get(category.getParentCategoryGuid());
            if (parentCategory!=null&&parentCategory.getEditCategory()){
                privilege.setAddSibling(true);
                privilege.setDelete(true);
                privilege.setMove(true);
            }else{
                privilege.setAddSibling(false);
                privilege.setDelete(false);
                privilege.setMove(false);
            }
            if (category.getEditCategory()){
                privilege.setAddChildren(true);
                privilege.setEdit(true);
            }else{
                privilege.setAddChildren(false);
                privilege.setEdit(false);
            }
            if (category.getEditItem()){
                privilege.setCreateRelation(true);
                privilege.setDeleteRelation(true);
                privilege.setAddOwner(true);
            }else{
                privilege.setCreateRelation(false);
                privilege.setDeleteRelation(false);
                privilege.setAddOwner(false);
            }
            if (systemCategory.contains(category.getGuid())) {
                privilege.setDelete(false);
                if (privilege.isEdit()){
                    privilege.setEditSafe(true);
                }
                privilege.setEdit(false);
            }
            //技术目录一级目录不允许删关联
            if (categoryType == 0 && category.getLevel() == 1) {
                privilege.setDeleteRelation(false);
            }
            categoryPrivilege.setPrivilege(privilege);
            userCategorys.add(categoryPrivilege);
        }
        addOtherCategory(categoryType, userCategorys,tenantId);
        return userCategorys;
    }

    public boolean isSmaller(CategoryPrivilegeV2 category,String userGroupId){
        CategoryPrivilegeV2 parentCategory = userGroupDAO.getCategoriesPrivileges(category.getGuid(), userGroupId);
        if (parentCategory==null||parentCategory.getRead()==null||!parentCategory.getRead()){
            return false;
        }
        if (!category.getEditItem()&&parentCategory.getEditItem()){
            return true;
        }
        if (!category.getEditCategory()&&parentCategory.getEditCategory()){
            return true;
        }
        return false;
    }

    public void addUserGroupPrivilege(UpdateCategory category,String tenantId,int type) throws SQLException, AtlasBaseException {
        List<String> removeCategoryId = new ArrayList<>();
        List<String> categoryIds = category.getGuid();
        for (String guid:categoryIds){
            CategoryEntityV2 categoryByGuid = categoryDAO.queryByGuid(guid, tenantId);
            if (categoryIds.contains(categoryByGuid.getParentCategoryGuid())){
                removeCategoryId.add(guid);
            }
        }
        categoryIds.removeAll(removeCategoryId);
        for (String groupId:category.getUserGroupIds()){
            for (String guid:categoryIds){
                CategoryPrivilegeV2 categoryPrivilegeV2 = new CategoryPrivilegeV2(category);
                categoryPrivilegeV2.setGuid(guid);
                updatePrivileges(categoryPrivilegeV2,groupId,type,tenantId,category.isChild(),removeCategoryId);
            }
        }
    }

    public void deleteGroupPrivilege(List<String> groupIds,String categoryId,String tenantId) throws SQLException, AtlasBaseException {
        if (groupIds==null){
            return;
        }

        ArrayList<String> categoryList = Lists.newArrayList(categoryId);
        CategoryEntityV2 categoryByGuid = categoryDAO.queryByGuid(categoryId, tenantId);
        String parentCategoryGuid = categoryByGuid.getParentCategoryGuid();
        List<RoleModulesCategories.Category> childCategoriesPrivileges = userGroupDAO.getChildCategorys(categoryList, categoryByGuid.getCategoryType(), tenantId);
        List<String> ids = childCategoriesPrivileges.stream().map(categoryPrivilegeV2 -> categoryPrivilegeV2.getGuid()).collect(Collectors.toList());
        ids.addAll(categoryList);

        if (parentCategoryGuid!=null&&parentCategoryGuid.length()!=0){
            for (String userGroupId:groupIds){

                if (parentCategoryGuid!=null&&parentCategoryGuid.length()!=0){
                    CategoryPrivilegeV2 parentCategory = userGroupDAO.getCategoriesPrivileges(parentCategoryGuid, userGroupId);
                    if (parentCategory!=null&&parentCategory.getRead()!=null&&parentCategory.getRead()){
                        UserGroup userGroup = userGroupDAO.getUserGroupByID(userGroupId);
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "父目录对用户组："+userGroup.getName()+"有权限，无法移除");
                    }
                }
            }
        }

        userGroupDAO.deleteGroupPrivilege(ids,groupIds);
    }

    public PageResult<GroupPrivilege> getUserGroupByCategory(String categoryId, String tenantId, int limit, int offset){
        PageResult<GroupPrivilege> pageResult=new PageResult<>();
        List<GroupPrivilege> userGroupByCategory = userGroupDAO.getUserGroupByCategory(categoryId, tenantId, limit, offset);
        if (userGroupByCategory==null||userGroupByCategory.size()==0){
            pageResult.setLists(new ArrayList<>());
            return pageResult;
        }
        pageResult.setLists(userGroupByCategory);
        pageResult.setCurrentSize(userGroupByCategory.size());
        pageResult.setTotalSize(userGroupByCategory.get(0).getTotalSize());
        return pageResult;
    }
}
