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
import java.text.SimpleDateFormat;
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
            List<Map<String, Object>> list = messageCenterDAO.getMyMessageList(type, tenantId, userId, status, search, offset, limit);

            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = Long.parseLong(list.get(0).get("total").toString());
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
            if (StringUtils.isBlank(id)) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("id"));
            }
            String userId = AdminUtils.getUserData().getUserId();
            Map<String, Object> message = messageCenterDAO.getMessageDetail(id, tenantId, userId);
            List<String> idList = new ArrayList<>();
            idList.add(id);
            messageCenterDAO.batchToRead(idList);
            return ReturnUtil.success(message);
        } catch (Exception e) {
            LOG.error("获取消息详情失败", e);
            return ReturnUtil.error(AtlasErrorCode.MESSAGE_DETAIL_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_DETAIL_RESULT.getFormattedErrorMessage("获取消息详情失败"), e);
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
                        map.put("userGroupCount", num);
                        break;
                    case 2:
                        map.put("dataCount", num);
                        break;
                    case 3:
                        map.put("needCount", num);
                        break;
                    case -1:
                        map.put("allCount", num);
                        break;
                    default:
                        return ReturnUtil.error(AtlasErrorCode.MESSAGE_URREAD_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_URREAD_RESULT.getFormattedErrorMessage("获取未读消息数量失败"));
                }
            }
            return ReturnUtil.success(map);
        } catch (Exception e) {
            LOG.error("获取未读消息数量失败", e);
            return ReturnUtil.error(AtlasErrorCode.MESSAGE_URREAD_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_URREAD_RESULT.getFormattedErrorMessage("获取未读消息数量失败"));
        }
    }

    public Result batchToRead(List<String> ids) {
        try {
            if (CollectionUtils.isEmpty(ids)) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("ids"));
            }
            int count = messageCenterDAO.batchToRead(ids);
            if (count > 0) {
                return ReturnUtil.success(count);
            } else {
                return ReturnUtil.error(AtlasErrorCode.MESSAGE_UPDATE_COUNT_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_UPDATE_COUNT_RESULT.getFormattedErrorMessage("消息更新数量为0"));
            }
        } catch (Exception e) {
            LOG.error("批量标记已读失败", e);
            return ReturnUtil.error(AtlasErrorCode.MESSAGE_BATCH_READ_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_BATCH_READ_RESULT.getFormattedErrorMessage("批量已读失败"));
        }
    }

    public Result batchDelte(List<String> ids, String delAll, String tenantId) {
        try {
            if (StringUtils.isBlank(delAll)) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("delAll"));
            }
            String userId = AdminUtils.getUserData().getUserId();
            int count = messageCenterDAO.batchDelte(ids, tenantId, userId, delAll);
            if (count > 0) {
                return ReturnUtil.success(count);
            } else {
                return ReturnUtil.error(AtlasErrorCode.MESSAGE_UPDATE_COUNT_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_UPDATE_COUNT_RESULT.getFormattedErrorMessage("消息更新数量为0"));
            }
        } catch (Exception e) {
            LOG.error("批量删除消息失败", e);
            return ReturnUtil.error(AtlasErrorCode.MESSAGE_BATCH_DELETE_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_BATCH_DELETE_RESULT.getFormattedErrorMessage("批量删除失败"));
        }
    }

    public Result addMessage(MessageEntity message, String tenantId) {
        try {
            message.setId(UUIDUtils.uuid());
            message.setTenantid(tenantId);
            message.setDelete(false);
            messageCenterDAO.addMessage(message);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("新增消息失败", e);
            return ReturnUtil.error(AtlasErrorCode.MESSAGE_ADD_RESULT.getErrorCode(), AtlasErrorCode.MESSAGE_ADD_RESULT.getFormattedErrorMessage("新增消息失败"));
        }
    }


}
