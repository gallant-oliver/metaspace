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
 * @author liwenfeng@gridsum.com
 * @date 2022/7/5 10:29
 */
package io.zeta.metaspace.web.service;

/*
 * @description
 * @author liwenfeng
 * @date 2022/7/5 10:29
 */

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.MessageCenterDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MessageCenterService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageCenterService.class);

    @Autowired
    private MessageCenterDAO messageCenterDAO;


    public PageResult<Map<String, Object>> getMyMessageList(Integer type, String tenantId, Integer status, String search, long offset, long limit) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            if (StringUtils.isNotBlank(search)) {
                search = search.replaceAll("_", "/_").replaceAll("%", "/%");
            }
            List<Map<String, Object>> list;
            list = messageCenterDAO.getMyMessageList(type, tenantId, userId, status, search, offset, limit);

            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = (long) (list.get(0).get("total"));
            }
            PageResult<Map<String, Object>> pageResult = new PageResult<>();
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取消息分页列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取消息分页列表失败");
        }
    }

    public Result getMessageDetail(String id, String tenantId) {
        try {
            if (StringUtils.isBlank(id)){
                return ReturnUtil.error("500", "id is null!");
            }
            String userId = AdminUtils.getUserData().getUserId();
            Map<String, Object> message = messageCenterDAO.getMessageDetail(id, tenantId, userId);
            List<String> idList = new ArrayList<>();
            idList.add(id);
            messageCenterDAO.batchToRead(idList);
            return ReturnUtil.success(message);
        } catch (Exception e) {
            LOG.error("获取消息分页列表失败", e);
            return ReturnUtil.error("500", "获取消息详情失败", e);
        }
    }

    public Result getUnReadNum(String tenantId) {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            List<Integer> typeList = new ArrayList<>(Arrays.asList(0, 1, 2, 3, -1));
            Map<String, Object> map = new HashMap<>();
            for (Integer type : typeList) {
                int num = messageCenterDAO.getUnReadNum(type, tenantId, userId);
                switch (type) {
                    case 0:
                        map.put("resourceCount", num);
                        break;
                    case 1:
                        map.put("resourceCount", num);
                        break;
                    case 2:
                        map.put("resourceCount", num);
                        break;
                    case 3:
                        map.put("resourceCount", num);
                        break;
                    case -1:
                        map.put("allCount", num);
                        break;
                    default:
                        return ReturnUtil.error("500", "getUnReadNum fail!");
                }
            }
            return ReturnUtil.success(map);
        } catch (Exception e) {
            LOG.error("获取未读消息数量失败", e);
            return ReturnUtil.error("500", "获取未读消息数量失败", e);
        }
    }

    public Result batchToRead(List<String> ids) {
        try {
            if (CollectionUtils.isEmpty(ids)) {
                return ReturnUtil.error("500", "ids is null!");
            }
            int count = messageCenterDAO.batchToRead(ids);
            if (count > 0) {
                return ReturnUtil.success(count);
            } else {
                return ReturnUtil.error("500", "update count is zero!");
            }
        } catch (Exception e) {
            LOG.error("批量标记已读失败", e);
            return ReturnUtil.error("500", "批量标记已读失败", e);
        }
    }

    public Result batchDelte(List<String> ids, String delAll, String tenantId) {
        try {
            if (CollectionUtils.isEmpty(ids)) {
                return ReturnUtil.error("500", "ids is null!");
            }
            if (StringUtils.isBlank(delAll)) {
                return ReturnUtil.error("500", "delAll is null!");
            }
            String userId = AdminUtils.getUserData().getUserId();
            int count = messageCenterDAO.batchDelte(ids, tenantId, userId, delAll);
            if (count > 0) {
                return ReturnUtil.success(count);
            } else {
                return ReturnUtil.error("500", "update count is zero!");
            }
        } catch (Exception e) {
            LOG.error("批量删除消息失败", e);
            return ReturnUtil.error("500", "批量删除消息失败", e);
        }
    }

    public Result addMessage(MessageEntity message, String tenantId) {
        try {
            message.setId(UUIDUtils.uuid());
            message.setTenantid(tenantId);
            message.setDelete(false);
            message.setCreateUser(AdminUtils.getUserData().getAccount());
            messageCenterDAO.addMessage(message);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("新增消息失败", e);
            return ReturnUtil.error("500", "新增消息失败", e);
        }
    }


}
