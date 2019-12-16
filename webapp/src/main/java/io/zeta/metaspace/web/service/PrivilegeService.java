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
 * @date 2019/2/19 11:19
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.PrivilegeHeader;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.privilege.SystemPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/19 11:19
 */
@Service
public class PrivilegeService {

    private static final Logger LOG = LoggerFactory.getLogger(PrivilegeService.class);

    @Autowired
    private PrivilegeDAO privilegeDAO;

    private static final int TECHNICAL_TYPE = 0;
    private static final int BUSINESS_TYPE = 1;

    @Transactional
    public void addPrivilege(PrivilegeHeader privilege) throws AtlasBaseException {
        try {
            int nameCount = privilegeDAO.getPrivilegeNameCount(privilege.getPrivilegeName());
            if(nameCount > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同权限方案名");
            }
            if(privilege.getModules()==null || privilege.getModules().size()==0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "授权内容不能为空");
            }
            String privilegeId = "m" + UUID.randomUUID().toString();
            privilege.setPrivilegeId(privilegeId);
            //createTime
            long createTime = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatDateStr = sdf.format(createTime);
            privilege.setCreateTime(formatDateStr);

            List<Integer> modules = privilege.getModules();
            List<String> roleIds = privilege.getRoles();
            if(Objects.nonNull(modules)) {
                privilegeDAO.addModule2Privilege(privilegeId, modules);
            }
            if(Objects.nonNull(roleIds)) {
                privilegeDAO.updateRolePrivilege(privilegeId, roleIds);
            }
            privilegeDAO.addPrivilege(privilege);

            if(!modules.contains(2) && !modules.contains(4) && !modules.contains(5)) {
                for(String roleId : roleIds) {
                    privilegeDAO.deleteRole2Category(roleId, BUSINESS_TYPE);
                }
            }
            if(!modules.contains(1) && !modules.contains(3)) {
                for(String roleId: roleIds) {
                    privilegeDAO.deleteRole2Category(roleId, TECHNICAL_TYPE);
                }
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加权限模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加权限模板失败");
        }
    }

    public List<Module> getAllModule() throws AtlasBaseException {
        try {
            return privilegeDAO.getAllModule();
        } catch (Exception e) {
            LOG.error("获取权限模块失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取权限模块失败");
        }
    }

    @Transactional
    public void delPrivilege(String privilegeId) throws AtlasBaseException {
        try {
            int enableDelete = privilegeDAO.getEnableDelete(privilegeId);
            if(enableDelete == 0) {
                throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "无权删除当前权限方案");
            }

            //删除privilege
            privilegeDAO.deletePrivilege(privilegeId);
            //删除privilege2module
            privilegeDAO.deletePrivilege2Module(privilegeId);

            List<Role> roleList = privilegeDAO.getRoleByPrivilegeId(privilegeId);
            List<String> roleIds = new ArrayList<>();
            //删除该privilege下的role关联的category
            privilegeDAO.deleteRole2Category(privilegeId, BUSINESS_TYPE);
            privilegeDAO.deleteRole2Category(privilegeId, TECHNICAL_TYPE);

            roleList.forEach(role -> roleIds.add(role.getRoleId()));
            //删除privilege后将role中分配该privilege的修改为Guet的privilege
            String guetPrivigeId = SystemPrivilege.GUEST.getCode();
            privilegeDAO.updateRolePrivilege(guetPrivigeId, roleIds);

        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("删除模板失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除模板失败");
        }
    }

    @Transactional
    public void updatePrivilege(String privilegeId, PrivilegeHeader privilege) throws AtlasBaseException {
        try {
            PrivilegeInfo currentPrivilege = privilegeDAO.getPrivilegeInfo(privilegeId);
            int nameCount = privilegeDAO.getPrivilegeNameCount(privilege.getPrivilegeName());
            if(nameCount > 0 && !currentPrivilege.getPrivilegeName().equals(privilege.getPrivilegeName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同权限方案名");
            }
            privilege.setPrivilegeId(privilegeId);
            privilegeDAO.updatePrivilege(privilege);
            List<Integer> moduleIds = privilege.getModules();
            List<String> roleIds = privilege.getRoles();

            if(!moduleIds.contains(2) && !moduleIds.contains(4) && !moduleIds.contains(5)) {
                for(String roleId : roleIds) {
                    privilegeDAO.deleteRole2Category(roleId, BUSINESS_TYPE);
                }
            }
            if(!moduleIds.contains(1) && !moduleIds.contains(3)) {
                for(String roleId: roleIds) {
                    privilegeDAO.deleteRole2Category(roleId, TECHNICAL_TYPE);
                }
            }

            String guetPrivigeId = SystemPrivilege.GUEST.getCode();
            //将拥有该privilege的role更新为guet的privilege
            privilegeDAO.deleteRelatedRoleByPrivilegeId(privilegeId, guetPrivigeId);

            //更新role的privilege
            if(Objects.nonNull(roleIds) && roleIds.size() > 0)
                privilegeDAO.updateRoleWithNewPrivilege(privilegeId, roleIds.toArray(new String[roleIds.size()]));

            //module
            privilegeDAO.deleteModule2PrivilegeById(privilegeId);
            if(Objects.nonNull(moduleIds) && moduleIds.size()>0)
                privilegeDAO.addModule2Privilege(privilegeId, moduleIds);

        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新权限模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新权限模板失败");
        }
    }

    public PageResult<PrivilegeInfo> getPrivilegeList(Parameters parameters) throws AtlasBaseException {
        String query = parameters.getQuery();
        query = (query==null ? "":query);
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        try {
            PageResult<PrivilegeInfo> rolePageResult = new PageResult<>();
            List<PrivilegeInfo> privilegeList = null;
            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            privilegeList = privilegeDAO.getPrivilegeList(query, limit, offset);
            for(PrivilegeInfo info : privilegeList) {
                List<Role> roleList = privilegeDAO.getRoleByPrivilegeId(info.getPrivilegeId());
                info.setRoles(roleList);
            }
            //long privilegetotalSize = privilegeDAO.getRolesCount(query);
            long privilegetotalSize = 0;
            if (privilegeList.size()!=0){
                privilegetotalSize = privilegeList.get(0).getTotal();
            }
            //rolePageResult.setOffset(offset);
            rolePageResult.setLists(privilegeList);
            rolePageResult.setCurrentSize(privilegeList.size());
            rolePageResult.setTotalSize(privilegetotalSize);
            return rolePageResult;
        } catch (Exception e) {
            LOG.error("获取权限列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取权限列表失败");
        }
    }

    public PrivilegeInfo getPrivilegeInfo(String privilegeId) throws AtlasBaseException {
        try {
            PrivilegeInfo privilege = privilegeDAO.getPrivilegeInfo(privilegeId);
            //roles
            List<Role> roleList = privilegeDAO.getRelatedRoleWithPrivilege(privilegeId);
            if(Objects.nonNull(roleList))
                privilege.setRoles(roleList);
            //modules
            List<Module> moduleList = privilegeDAO.getRelatedModuleWithPrivilege(privilegeId);
            if(Objects.nonNull(moduleList))
                privilege.setModules(moduleList);
            return privilege;
        } catch (Exception e) {
            LOG.error("获取权限模板详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取权限模板详情失败");
        }
    }

    public PageResult<Role> getAllPermissionRole(Parameters parameters) throws AtlasBaseException {
        try {
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            PageResult<Role> pageResult = new PageResult<>();
            List<Role> roleList = privilegeDAO.getAllPermissionRole(limit, offset);
            //long totalSize = privilegeDAO.getCountAllPermissionRole();
            long totalSize = 0;
            if (roleList.size()!=0){
                totalSize = roleList.get(0).getTotal();
            }
            //pageResult.setOffset(offset);
            pageResult.setLists(roleList);
            pageResult.setCurrentSize(roleList.size());
            pageResult.setTotalSize(totalSize);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取角色列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取角色列表失败");
        }
    }
}
