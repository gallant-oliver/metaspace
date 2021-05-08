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


import io.zeta.metaspace.model.approvegroup.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import io.zeta.metaspace.web.dao.ApproveGroupDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author lixiang03
 * @Data 2020/2/24 15:49
 */
@Service
public class ApproveGroupService {



    @Autowired
    UserGroupDAO userGroupDAO;

    @Autowired
    TenantService tenantService;

    @Autowired
    ApproveGroupDAO approveGroupDAO;

    public PageResult<ApproveGroupListAndSearchResult> getApproveGroupListAndSearch(String tenantId, Parameters params) throws AtlasBaseException {
        PageResult<ApproveGroupListAndSearchResult> commonResult = new PageResult<>();

        if (params.getQuery() != null) {  //模糊查询的特殊字符转译
             params.setQuery(params.getQuery().replaceAll("%", "/%").replaceAll("_", "/_"));
        }

        //从安全中心获取所有用户,用于统计组内的成员数量
        SecuritySearch securitySearch = new SecuritySearch();
        securitySearch.setTenantId(tenantId);
        PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, securitySearch);
        List<String> userIds = userAndModules.getLists().stream().map(UserAndModule::getAccountGuid).collect(Collectors.toList());

        List<ApproveGroupListAndSearchResult> lists = approveGroupDAO.getApproveGroup(tenantId, params.getOffset(), params.getLimit(), params.getSortby(),params.getOrder(), params.getQuery(),userIds);

        if (lists == null || lists.size() == 0) {
            return commonResult;
        }

        for (ApproveGroupListAndSearchResult searchResult : lists) {
            if (userIds == null || userIds.size() == 0) {
                searchResult.setMember("0");
            }
            String userName = approveGroupDAO.getUserNameById(searchResult.getCreator());
            List<String> module = approveGroupDAO.selectApproveGroupModule(searchResult.getId());
            String modules = module.stream().map(groupId -> ModuleEnum.getModuleShowName(Integer.parseInt(groupId))).collect(Collectors.joining(","));
            searchResult.setCreator(userName);
            searchResult.setModules(modules);
        }
        commonResult.setTotalSize(lists.get(0).getTotalSize());
        commonResult.setLists(lists);
        commonResult.setCurrentSize(lists.size());
        return commonResult;

    }

    /**
     * 审批组组详情
     */
    public PageResult<ApproveGroupListAndSearchResult> getApproveGroupByModuleId(ApproveGroupParas paras,String tenantId) {
        List<ApproveGroupListAndSearchResult> lists = approveGroupDAO.getApproveGroupByModuleId(tenantId, paras);
        PageResult<ApproveGroupListAndSearchResult> result = new PageResult<>();
        if(lists == null || lists.size() ==0 ){
            return result;
        }
        result.setTotalSize(lists.get(0).getTotalSize());
        result.setLists(lists);
        result.setCurrentSize(lists.size());
        return result;
    }


    /**
     * 审批组组详情
     */
    public List<ApproveGroup> getApproveGroupByIDs(List<String> ids) {
        List<ApproveGroup> approveGroups = approveGroupDAO.getApproveGroupByIDs(ids);
        for(ApproveGroup group : approveGroups){
            String userName = userGroupDAO.getUserNameById(group.getCreator());
            group.setCreator(userName);
        }
        return approveGroups;
    }

    /**
     * 审批组组详情
     */
    public ApproveGroup getApproveGroupById(String id) {
        ApproveGroup approveGroups = approveGroupDAO.getApproveGroupByID(id);
        String userName = userGroupDAO.getUserNameById(approveGroups.getCreator());
        approveGroups.setCreator(userName);
        return approveGroups;
    }


    /**
     * 获取审批模块
     */
    public List<ModuleSelect> getApproveModule() {
        List<ModuleEnum> collect = ModuleEnum.getApproveModuleEnum();
        List<ModuleSelect> result = new ArrayList<>();
        for(ModuleEnum module : collect){
            result.add(new ModuleSelect(String.valueOf(module.getId()),module.getName(),false));
        }
        return result;
    }

    /**
     * 审批模块编辑
     */
    public List<ModuleSelect> getApproveModule(String groupId) {
        List<ModuleSelect> approveModule = getApproveModule();
        //查询审批组已有关系模块
        List<String> modules = approveGroupDAO.selectApproveGroupModule(groupId);
        List<ModuleSelect> result = approveModule.stream().map(moduleSelect -> {
            if (modules.contains(moduleSelect.getModuleId())) {
                moduleSelect.setSelected(true);
            }
            return moduleSelect;
        }).collect(Collectors.toList());
        return result;

    }


    /**
     * 二.用户组详情>
     */
    public List<String> getApproveGroupNameByIDs(List<String> ids) {
        if (ids==null||ids.size()==0){
            return new ArrayList<>();
        }
        List<ApproveGroup> approveGroupMapperDetail = approveGroupDAO.getApproveGroupByIDs(ids);
        return approveGroupMapperDetail.stream().map(group -> group.getName()).collect(Collectors.toList());
    }





    /**
     * 三.新建审批组
     */
    @Transactional(rollbackFor=Exception.class)
    public void addApproveGroup(String tenantId, ApproveGroup approveGroup) throws AtlasBaseException {

        String uuID = UUID.randomUUID().toString();
        approveGroup.setId(uuID);

        if (isNameById(tenantId, approveGroup.getName(), approveGroup.getId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审批组名称" + approveGroup.getName() + "已存在");
        }

        String creator = AdminUtils.getUserData().getUserId();
        approveGroup.setCreator(creator);
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp currentTime = new Timestamp(currentTimeMillis);
        approveGroup.setCreateTime(currentTime);
        approveGroup.setUpdateTime(currentTime);
        approveGroupDAO.addGroup(tenantId, approveGroup); // 添加审批组信息
        //添加审批组模块关系
        approveGroupDAO.addModuleToGroupByIDs(uuID,approveGroup.getModules());
    }

    //判断租户是否已经存在，true为存在，false为不存在
    public boolean isNameById(String tenantId, String name, String id) {
        Integer nameById = approveGroupDAO.isNameById(tenantId, name,id);
        return nameById != 0;
    }

    /**
     * 四.删除审批组信息
     */
    @Transactional(rollbackFor=Exception.class)
    public void deleteApproveGroupByIDs(List<String> ids) {
        approveGroupDAO.deleteApproveGroupModule(ids); //删除审批组对应模块关系
        approveGroupDAO.deleteGroupUserRelation(ids);  //删除审批组对应用户关系
        approveGroupDAO.deleteApproveGroupByIDs(ids);  //删除审批组
    }

    /**
     * 五.审批组成员列表及搜索
     */

    public PageResult<MemberListAndSearchResult> getApproveGroupMemberListAndSearch(String id,int offset, int limit, String search,String tenantId) throws AtlasBaseException {

        PageResult<MemberListAndSearchResult> commonResult = new PageResult<>();

        if (search != null) {
            search = search.replaceAll("%", "/%").replaceAll("_", "/_");
        }

        SecuritySearch securitySearch = new SecuritySearch();
        securitySearch.setUserName(search);
        securitySearch.setTenantId(tenantId);
        PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, securitySearch);
        List<String> userIds = userAndModules.getLists().stream().map(UserAndModule::getAccountGuid).collect(Collectors.toList());
        if (userIds == null || userIds.size() == 0) {
            return commonResult;
        }

        List<MemberListAndSearchResult> lists = approveGroupDAO.getMemberListAndSearch(id,offset, limit, null,userIds);

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

    public PageResult<ApproveGroupMemberSearch> getApproveGroupMemberSearch(String tenantId, String groupId, int offset, int limit, String search) throws AtlasBaseException {
        PageResult<ApproveGroupMemberSearch> commonResult = new PageResult<>();

        if (search != null) {
            search = search.replaceAll("%", "/%").replaceAll("_", "/_");
        }

        List<String> GroupUsersAll = new ArrayList<>();
        SecuritySearch securitySearch = new SecuritySearch();
        securitySearch.setUserName(search);
        securitySearch.setTenantId(tenantId);
        PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(offset, limit, securitySearch);
        for (UserAndModule userAndModule : userAndModules.getLists()) {
            GroupUsersAll.add(userAndModule.getUserName());
        }
        List<String> GroupUsersNow = approveGroupDAO.getUserNameByGroupId(tenantId, groupId);

        //排除已经存在的用户
        List<String> userNameList = GroupUsersAll.stream().filter(str -> !GroupUsersNow.stream().anyMatch(s -> s.equals(str))).collect(Collectors.toList());
        if (userNameList == null || userNameList.size() == 0) {
            return commonResult;
        }
        if (userNameList.size()==0){
            return commonResult;
        }
        List<ApproveGroupMemberSearch> lists = approveGroupDAO.getApproveGroupMemberSearch(userNameList, offset, limit);
        if (lists == null || lists.size() == 0) {
            return commonResult;
        }
        commonResult.setLists(lists);
        commonResult.setCurrentSize(lists.size());
        commonResult.setTotalSize(lists.get(0).getTotalSize());
        return commonResult;

    }


    /**
     * 审批组添加成员
     */

    public void addUserGroupByID(String groupId, List<String> userIds) {
        if (userIds == null||userIds.size()==0) {
            return;
        }
        List<String> userIdByGroupId = approveGroupDAO.getUserIdByApproveGroup(groupId);  //查询已有的用户列表
        List<String> filterUserIds = userIds.stream().filter(s -> !userIdByGroupId.contains(s)).collect(Collectors.toList());
        if (filterUserIds!=null&&filterUserIds.size()!=0){
            approveGroupDAO.addUserGroupByID(groupId, filterUserIds);
        }
    }


    /**
     * 八.用户组移除成员
     */
    public void deleteUserByGroupId(String groupId, List<String> userIds) {
        if (userIds == null|| userIds.size()==0) {
            return;
        }
        approveGroupDAO.deleteUserByGroupId(groupId, userIds);
    }


    /**
     * 十五.修改用户组管理信息
     */
    @Transactional(rollbackFor=Exception.class)
    public void updateUserGroupInformation(String groupId, ApproveGroup approveGroup,String tenantId) throws AtlasBaseException {

        //规定：用户组名称长度范围必须是大于0且小于等于64，描述的长度范围必须是大于等于0且小于等于256
        if (!existGroupId(groupId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "您的审批组id:" + groupId + "不存在，无法修改审批组管理信息，请确保您的审批组id输入正确!");
        }
        if (isNameById(tenantId, approveGroup.getName(), approveGroup.getId())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审批组名称" + approveGroup.getName() + "已存在");
        }
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp updateTime = new Timestamp(currentTimeMillis);
        approveGroup.setUpdater(AdminUtils.getUserData().getUserId()); //添加更新用户的ID
        approveGroupDAO.updateApproveGroupInformation(groupId, approveGroup, updateTime);
        //删除模块关系
        approveGroupDAO.deleteApproveGroupModuleById(approveGroup.getId());
        //添加新的模块授权关系
        approveGroupDAO.addModuleToGroupByIDs(groupId,approveGroup.getModules());
    }


    //判断用户组Id是否已经存在，true为存在，false为不存在
    public boolean existGroupId(String groupId) {
        Integer id = approveGroupDAO.existGroupId(groupId);
        return id != 0;
    }

}
