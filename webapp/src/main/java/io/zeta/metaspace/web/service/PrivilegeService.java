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

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.PrivilegeHeader;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.privilege.SystemPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
            String privilegeId = privilege.getPrivilegeId();
            List<Integer> modules = privilege.getModules();
            List<String> roleIds = privilege.getRoles();
            privilegeDAO.addModule2Privilege(privilegeId, modules);
            privilegeDAO.updateRolePrivilege(privilegeId, roleIds);
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
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public List<Module> getAllModule() throws AtlasBaseException {
        try {
            return privilegeDAO.getAllModule();
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public void delPrivilege(String privilegeId) throws AtlasBaseException {
        try {
            privilegeDAO.deletePrivilege(privilegeId);
            privilegeDAO.deletePrivilege2Module(privilegeId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    @Transactional
    public void updatePrivilege(String privilegeId, PrivilegeInfo privilege) throws AtlasBaseException {
        try {
            privilege.setPrivilegeId(privilegeId);
            privilegeDAO.updatePrivilege(privilege);
            String guetPrivigeId = SystemPrivilege.GUEST.getCode();
            privilegeDAO.deleteRelatedRoleByPrivilegeId(privilegeId, guetPrivigeId);
            List<String> roleIds = new ArrayList<>();
            privilege.getRoles().forEach(role -> roleIds.add(role.getRoleId()));
            privilegeDAO.updateRoleWithNewPrivilege(privilegeId, roleIds.toArray(new String[roleIds.size()]));
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public PageResult<PrivilegeInfo> getPrivilegeList(String query, int limit, int offset) throws AtlasBaseException {
        try {
            PageResult<PrivilegeInfo> rolePageResult = new PageResult<>();
            List<PrivilegeInfo> privilegeList = privilegeDAO.getPrivilegeList(query, limit, offset);
            long privilegeCount = privilegeDAO.getRolesCount(query);
            rolePageResult.setLists(privilegeList);
            rolePageResult.setCount(privilegeCount);
            rolePageResult.setSum(privilegeList.size());
            return rolePageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public PrivilegeInfo getPrivilegeInfo(String privilegeId) throws AtlasBaseException {
        try {
            PrivilegeInfo privilege = privilegeDAO.getPrivilegeInfo(privilegeId);
            //roles
            List<Role> roleList = privilegeDAO.getRelatedRoleWithPrivilege(privilegeId);
            privilege.setRoles(roleList);
            //modules
            List<Module> moduleList = privilegeDAO.getRelatedModuleWithPrivilege(privilegeId);
            privilege.setModules(moduleList);
            return privilege;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
}
