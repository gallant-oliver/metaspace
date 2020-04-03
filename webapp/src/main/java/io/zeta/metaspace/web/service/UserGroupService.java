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


import io.zeta.metaspace.model.dataSource.DataSourceIdAndName;
import io.zeta.metaspace.model.dataSource.SourceAndPrivilege;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupCategories;
import io.zeta.metaspace.model.usergroup.UserGroupPrivileges;
import io.zeta.metaspace.model.usergroup.UserPrivilegeDataSource;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupMemberSearch;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
    @Autowired
    UserGroupDAO userGroupDAO;
    @Autowired
    TenantService tenantService;
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
    @Transactional
    public void deleteUserGroupByID(String id) {

        userGroupDAO.deleteUserGroupByID(id);
        userGroupDAO.deleteUserGroupRelationByID(id);
        userGroupDAO.deleteCategoryGroupRelationByID(id);
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




    @Transactional
    public UserGroupCategories getPrivileges(String userGroupId,String tenant,boolean all) throws AtlasBaseException {
        UserGroupCategories userGroupCategories = new UserGroupCategories();
        User user = AdminUtils.getUserData();
        List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenant);
        if(userGroups==null||userGroups.size()==0) {
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

    @Transactional
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

    @Transactional
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

    @Transactional
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

    @Transactional
    public void setOtherCategory(int categorytype, List<RoleModulesCategories.Category> resultList,String tenantId) {
        List<RoleModulesCategories.Category> otherCategorys = userGroupDAO.getOtherCategorys(resultList, categorytype,tenantId);
        for (RoleModulesCategories.Category otherCategory : otherCategorys) {
            otherCategory.setShow(false);
            otherCategory.setHide(true);
            otherCategory.setStatus(0);

        }
        resultList.addAll(otherCategorys);
    }
    @Transactional
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
    @Transactional
    public List<CategoryPrivilege> getUserCategory(String userGroupId, int categorytype,List<Module> modulesByUser,String tenantId) {
        List<CategoryPrivilege> userCategorys = new ArrayList<>();
        List<Integer> modules = new ArrayList<>();
        for (Module module : modulesByUser) {
            modules.add(module.getModuleId());
        }
        if (3 == categorytype||4 == categorytype) {
            List<RoleModulesCategories.Category> allCategorys = userGroupDAO.getAllCategorys(categorytype,tenantId);
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
            addPrivilege(userCategorys, allCategorys, privilege, categorytype);
        } else {

            List<String> userBusinessCategories = userGroupDAO.getCategorysByTypeIds(userGroupId, categorytype,tenantId);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys = userGroupDAO.getChildCategorys(userBusinessCategories, categorytype,tenantId);
                List<RoleModulesCategories.Category> userParentCategorys = userGroupDAO.getParentCategorys(userBusinessCategories, categorytype,tenantId);
                List<RoleModulesCategories.Category> userPrivilegeCategorys = userGroupDAO.getCategorysByType(userGroupId, categorytype,tenantId);
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
                }
                addPrivilege(userCategorys, userChildCategorys, childPrivilege, categorytype);
                addPrivilege(userCategorys, userParentCategorys, parentPrivilege, categorytype);
                addPrivilege(userCategorys, userPrivilegeCategorys, ownerPrivilege, categorytype);
            }
        }
        addOtherCategory(categorytype, userCategorys,tenantId);
        return userCategorys;
    }

    public List<CategoryPrivilege> getAdminCategory(int categorytype,String tenantId){
        List<CategoryPrivilege> userCategorys = new ArrayList<>();
        List<RoleModulesCategories.Category> allCategorys = userGroupDAO.getAllCategorys(categorytype,tenantId);
        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, true, true, true, false, true, false, false, true,false);
        addPrivilege(userCategorys, allCategorys, privilege, categorytype);
        return userCategorys;
    }

    private void addPrivilege(List<CategoryPrivilege> userCategorys, List<RoleModulesCategories.Category> allCategorys, CategoryPrivilege.Privilege privilege, int categorytype) {
        String[] systemCategoryGuids = {"1", "2", "3", "4", "5"};
        List<String> lists = Arrays.asList(systemCategoryGuids);
        for (RoleModulesCategories.Category category : allCategorys) {
            CategoryPrivilege.Privilege privilegeinfo = new CategoryPrivilege.Privilege(privilege);
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(category);
            //系统系统目录不允许删除和编辑
            if (lists.contains(category.getGuid())) {
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

    @Transactional
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

    public void addDataSourceByGroupId(String groupId, UserGroupPrivileges privileges) {
        if (privileges.getSourceIds()==null||privileges.getSourceIds().size()==0){
            return;
        }
        userGroupDAO.addDataSourceByGroupId(groupId, privileges.getSourceIds(),privileges.getPrivilegeCode());
    }

    public void updateDataSourceByGroupId(String groupId, UserGroupPrivileges privileges) {
        if (privileges.getSourceIds()==null||privileges.getSourceIds().size()==0){
            return;
        }
        userGroupDAO.updateDataSourceByGroupId(groupId,privileges);
    }

    public void deleteDataSourceByGroupId(String groupId, List<String> sourceIds) {
        if (sourceIds == null||sourceIds.size()==0) {
            return;
        }
        userGroupDAO.deleteDataSourceByGroupId(groupId, sourceIds);
    }

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
}
