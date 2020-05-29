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
 * @date 2019/3/26 16:01
 */
package io.zeta.metaspace.web.service;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 16:01
 */

import io.zeta.metaspace.model.share.APIGroup;
import io.zeta.metaspace.web.dao.DataShareGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@Service
public class DataShareGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(DataShareGroupService.class);
    @Autowired
    DataShareGroupDAO groupDAO;

    public int insertGroup(APIGroup group,String tenantId) throws AtlasBaseException {
        try {
            String user = AdminUtils.getUserData().getUserId();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            int count = groupDAO.countGroupName(group.getName(),tenantId);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字分组");
            }
            String guid = UUID.randomUUID().toString();
            group.setGuid(guid);
            group.setParentGuid("1");
            group.setGenerator(user);
            group.setGenerateTime(time);
            group.setUpdater(user);
            group.setUpdateTime(time);
            return groupDAO.insertGroup(group,tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加API分组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加API分组失败");
        }
    }

    public int deleteGroup(String guid) throws AtlasBaseException {
        try {
            String initId1="1";
            String initId2="2";
            if(initId1.equals(guid) || initId2.equals(guid)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不允许删除当前分组");
            }
            int count = groupDAO.getGroupRelatedAPI(guid);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "只能删除没有API的分组， 请确认后再次尝试");
            }
            return groupDAO.deleteGroup(guid);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("删除API分组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除API分组失败");
        }
    }

    public int updateGroup(String guid, APIGroup group,String tenantId) throws AtlasBaseException {
        try {
            group.setGuid(guid);
            String user = AdminUtils.getUserData().getUserId();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            group.setUpdater(user);
            group.setUpdateTime(time);
            String currentName = groupDAO.getGroupNameById(group.getGuid());
            int count = groupDAO.countGroupName(group.getName(),tenantId);
            if(count > 0 && !currentName.equals(group.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字分组");
            }
            group.setGuid(guid);
            return groupDAO.updateGroup(group);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新API分组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新API分组失败");
        }
    }

    public List<APIGroup> getGroupList(String tenantId) throws AtlasBaseException {
        try {
            return groupDAO.getGroupList(tenantId);
        }  catch (Exception e) {
            LOG.error("获取API分组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取API分组失败");
        }
    }

    public String getGroupName(String guid) throws AtlasBaseException {
        try {
            return groupDAO.getGroupNameById(guid);
        }  catch (Exception e) {
            LOG.error("获取API分组信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取API分组信息失败");
        }
    }
}
