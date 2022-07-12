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

import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.enums.MessagePush;
import io.zeta.metaspace.model.enums.ProcessEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableRelationDTO;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
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
import java.util.stream.Collectors;

@Service
public class MessageCenterService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageCenterService.class);

    @Autowired
    private MessageCenterDAO messageCenterDAO;
    @Autowired
    private UserGroupDAO userGroupDAO;
    @Autowired
    private DataSourceService dataSourceDAO;
    @Autowired
    private DatabaseDAO databaseDAO;
    @Autowired
    private DataShareDAO dataShareDAO;
    @Autowired
    private GroupDeriveTableRelationDAO groupDeriveTableRelationDAO;
    @Autowired
    private UserDAO userDAO;

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

    //数据源，数据库，项目，保密表，重要表消息推送
    public void perAddMessage(int operateType, int type, String groupId, List<String> ids, String tenantId) {
        try {
            List<String> user = userGroupDAO.getAllUserByGroupId(groupId);
            List<String> accounts = user.stream().distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return;
            }
            String groupName = userGroupDAO.getUserGroupByID(groupId).getName();
            MessagePush messagePush = perGetMessagePush(type, operateType);
            ProcessEnum processEnum = getProcess(operateType);
            List<String> name = getMessageName(type, operateType, ids, tenantId);
            for (String n : name) {
                for (String account : accounts) {
                    MessageEntity messageEntity = new MessageEntity(messagePush, MessagePush.getFormattedMessageName(messagePush.name, groupName, n)
                            , processEnum.code);
                    messageEntity.setCreateUser(account);
                    addMessage(messageEntity, tenantId);
                }
            }
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException("消息推送失败",e);
        }
    }

    //业务目录，技术目录，指标目录权限分配消息推送
    public void directoryMessagePush(int type, int operateType, String userGroupName, List<String> userAccounts, String category, String tenantId) {
        try {
            if (CollectionUtils.isEmpty(userAccounts)) {
                return;
            }
            MessageEntity messageEntity = getMessageEntityByType(type, operateType, userGroupName, category);
            for (String account : userAccounts) {
                messageEntity.setCreateUser(account);
                addMessage(messageEntity, tenantId);
            }
        } catch (Exception e) {
            throw new AtlasBaseException("消息推送失败", e);
        }
    }

    //重要表和保密表的移除权限，一个衍生表可能同是保密和重要
    public void tableDelMessagePush(List<String> ids) {
        try {
            List<GroupDeriveTableRelationDTO> deriveInfoByIds = groupDeriveTableRelationDAO.getDeriveInfoByIds(ids);
            List<String> users = userGroupDAO.getAllUserByGroupId(deriveInfoByIds.get(0).getUserGroupId());
            for (GroupDeriveTableRelationDTO dto : deriveInfoByIds) {
                tableDelMessagePush(dto,users);
            }
        } catch (Exception e) {
            throw new AtlasBaseException("消息推送失败", e);
        }
    }

    public void userGroupMessage(int operateType, String groupId, List<String> users, String tenantId) {
        try {
            if (CollectionUtils.isEmpty(users)) {
                return;
            }
            String groupName = userGroupDAO.getUserGroupByID(groupId).getName();
            if (groupName == null) {
                return;
            }
            MessageEntity messageEntity = userGroupGetMessageEntity(operateType, groupName);
            List<User> usersByIds = userDAO.getUsersByIds(users);
            for (User user : usersByIds) {
                messageEntity.setCreateUser(user.getAccount());
                addMessage(messageEntity, tenantId);
            }
        } catch (AtlasException e) {
            throw new AtlasBaseException("消息推送失败",e);
        }
    }

    private MessageEntity getMessageEntityByType(int type,int operateType,String userGroupName, String category){
        MessageEntity messageEntity = null;
        if (type == 0 && operateType == CommonConstant.CHANGE) {
            messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_CHANGE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_CHANGE.name, userGroupName, category),
                    ProcessEnum.PROCESS_APPROVED_AUTHORIZED.code);
        }
        if (type == 0 && operateType == CommonConstant.REMOVE) {
            messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_REMOVE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_REMOVE.name, userGroupName, category),
                    ProcessEnum.PROCESS_APPROVED_NOT_AUTHORIZED.code);
        }
        if (type == 1 && operateType == CommonConstant.CHANGE) {
            messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_BUSINESS_CATEGORY_CHANGE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_CHANGE.name, userGroupName, category),
                    ProcessEnum.PROCESS_APPROVED_AUTHORIZED.code);
        }
        if (type == 1 && operateType == CommonConstant.REMOVE) {
            messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_BUSINESS_CATEGORY_REMOVE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_REMOVE.name, userGroupName, category),
                    ProcessEnum.PROCESS_APPROVED_NOT_AUTHORIZED.code);
        }
        if (type == 5 && operateType == CommonConstant.CHANGE) {
            messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_INDICATOR_CATEGORY_CHANGE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_INDICATOR_CATEGORY_CHANGE.name, userGroupName, category),
                    ProcessEnum.PROCESS_APPROVED_AUTHORIZED.code);
        }
        if (type == 5 && operateType == CommonConstant.REMOVE) {
            messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_INDICATOR_CATEGORY_REMOVE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_INDICATOR_CATEGORY_REMOVE.name, userGroupName, category)
                    , ProcessEnum.PROCESS_APPROVED_NOT_AUTHORIZED.code);
        }
        return messageEntity;
    }

    private void tableDelMessagePush(GroupDeriveTableRelationDTO dto,List<String> users){
        //重要表推送
        if (dto.getImportancePrivilege() != null && dto.getImportancePrivilege()) {
            MessageEntity messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_IMPORT_TABLE_REMOVE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_IMPORT_TABLE_REMOVE.name, dto.getUserGroupName(),
                            dto.getTableName()), ProcessEnum.PROCESS_APPROVED_NOT_AUTHORIZED.code);
            for (String account : users) {
                messageEntity.setCreateUser(account);
                addMessage(messageEntity, dto.getTenantId());
            }
        }
        //私密表推送
        if (dto.getSecurityPrivilege() != null && dto.getSecurityPrivilege()) {
            MessageEntity messageEntity = new MessageEntity(MessagePush.USER_GROUP_PERMISSION_SECURITY_TABLE_REMOVE, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_PERMISSION_SECURITY_TABLE_REMOVE.name, dto.getUserGroupName(),
                            dto.getTableName()), ProcessEnum.PROCESS_APPROVED_NOT_AUTHORIZED.code);
            for (String account : users) {
                messageEntity.setCreateUser(account);
                addMessage(messageEntity, dto.getTenantId());
            }
        }
    }

    private ProcessEnum getProcess(int operateType) throws AtlasBaseException{
        if (operateType == CommonConstant.ADD || operateType == CommonConstant.CHANGE) {
            return ProcessEnum.PROCESS_APPROVED_AUTHORIZED;
        }
        if (operateType == CommonConstant.REMOVE) {
            return ProcessEnum.PROCESS_APPROVED_NOT_AUTHORIZED;
        }
        throw new AtlasBaseException("非法操作类型");
    }

    private MessagePush perGetMessagePush(int type,int operateType){
        if (type == CommonConstant.DATA_SOURCE) {
            if (operateType == CommonConstant.ADD) {
                return MessagePush.USER_GROUP_PERMISSION_DATA_SOURCE_ADD;
            }
            if (operateType == CommonConstant.CHANGE) {
                return MessagePush.USER_GROUP_PERMISSION_DATA_SOURCE_CHANGE;
            }
            if (operateType == CommonConstant.REMOVE) {
                return MessagePush.USER_GROUP_PERMISSION_DATA_SOURCE_REMOVE;
            }
        }
        if (type == CommonConstant.DATA_BASE) {
            if (operateType == CommonConstant.ADD) {
                return MessagePush.USER_GROUP_PERMISSION_DATA_BASE_ADD;
            }
            if (operateType == CommonConstant.REMOVE) {
                return MessagePush.USER_GROUP_PERMISSION_DATA_BASE_REMOVE;
            }
        }
        if (type == CommonConstant.PROJECT) {
            if (operateType == CommonConstant.ADD) {
                return MessagePush.USER_GROUP_PERMISSION_API_PROJECT_BASE_ADD;
            }
            if (operateType == CommonConstant.REMOVE) {
                return MessagePush.USER_GROUP_PERMISSION_API_PROJECT_REMOVE;
            }
        }
        if (type == CommonConstant.IMPORT_TABLE && operateType == CommonConstant.ADD) {
            return MessagePush.USER_GROUP_PERMISSION_IMPORT_TABLE_ADD;
        }

        if (type == CommonConstant.SECURITY_TABLE && operateType == CommonConstant.ADD) {
            return MessagePush.USER_GROUP_PERMISSION_SECURITY_TABLE_ADD;
        }
        throw new AtlasBaseException("存在非法类型");
    }

    private List<String> getMessageName(int type,int operateType,List<String> ids,String tenantId){
        List<String> name = new ArrayList<>();
        if (type == CommonConstant.DATA_SOURCE) {
            name = dataSourceDAO.getSourceNameForSourceIds(ids);
        }
        if (type == CommonConstant.DATA_BASE) {
            name = databaseDAO.getDbName(ids);
        }
        if (type == CommonConstant.PROJECT) {
            name = dataShareDAO.getProjectName(ids, tenantId);
        }
        if (type == CommonConstant.IMPORT_TABLE && operateType == CommonConstant.ADD) {
            name = groupDeriveTableRelationDAO.getDeriveNameInfoByIds(ids);
        }

        if (type == CommonConstant.SECURITY_TABLE && operateType == CommonConstant.ADD) {
            name = groupDeriveTableRelationDAO.getDeriveNameInfoByIds(ids);
        }
        return name;
    }

    private MessageEntity userGroupGetMessageEntity(int operateType,String groupName) throws AtlasException {
        if (operateType == CommonConstant.ADD) {
            return new MessageEntity(MessagePush.USER_GROUP_USER_MEMBER_ADD.type, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_USER_MEMBER_ADD.name, groupName), MessagePush.
                    USER_GROUP_USER_MEMBER_ADD.module, ProcessEnum.PROCESS_APPROVED_AUTHORIZED.code);
        }
        if (operateType == CommonConstant.REMOVE) {
            return new MessageEntity(MessagePush.USER_GROUP_USER_MEMBER_REMOVE.type, MessagePush.
                    getFormattedMessageName(MessagePush.USER_GROUP_USER_MEMBER_REMOVE.name, groupName), MessagePush.
                    USER_GROUP_USER_MEMBER_REMOVE.module, ProcessEnum.PROCESS_APPROVED_NOT_AUTHORIZED.code);
        }
        throw new AtlasException("存在非法类型");
    }

}
