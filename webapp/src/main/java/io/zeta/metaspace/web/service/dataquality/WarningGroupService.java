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
package io.zeta.metaspace.web.service.dataquality;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import io.zeta.metaspace.model.dataquality2.ErrorInfo;
import io.zeta.metaspace.model.dataquality2.TaskErrorHeader;
import io.zeta.metaspace.model.dataquality2.TaskWarningHeader;
import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.dataquality2.WarningInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.dao.dataquality.WarningGroupDAO;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeansUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarningGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(WarningGroupService.class);

    @Autowired
    private WarningGroupDAO warningGroupDAO;

    @Autowired
    private TaskManageDAO taskManageDAO;

    public int insert(WarningGroup warningGroup) throws AtlasBaseException {
        warningGroup.setId(UUIDUtils.alphaUUID());
        warningGroup.setCreateTime(DateUtils.currentTimestamp());
        warningGroup.setUpdateTime(DateUtils.currentTimestamp());
        warningGroup.setCreator(AdminUtils.getUserData().getUserId());
        warningGroup.setDelete(false);
        return warningGroupDAO.insert(warningGroup);
    }

    public WarningGroup getById(String id) throws AtlasBaseException {
        WarningGroup warningGroup = warningGroupDAO.getById(id);
        String path = CategoryRelationUtils.getPath(warningGroup.getCategoryId());
        warningGroup.setPath(path);
        return warningGroup;
    }

    public WarningGroup getByName(String name) throws AtlasBaseException {
        return warningGroupDAO.getByName(name);
    }

    public void deleteById(String number) throws AtlasBaseException {
        warningGroupDAO.deleteById(number);
    }

    public void deleteByIdList(List<String> numberList) throws AtlasBaseException {
        warningGroupDAO.deleteByIdList(numberList);
    }

    public int update(WarningGroup warningGroup) throws AtlasBaseException {
        warningGroup.setUpdateTime(DateUtils.currentTimestamp());
        WarningGroup old = getById(warningGroup.getId());
        BeansUtil.copyPropertiesIgnoreNull(warningGroup, old);
        return warningGroupDAO.update(old);
    }

    public PageResult<WarningGroup> search(Parameters parameters) throws AtlasBaseException {
        try {
            List<WarningGroup> list = warningGroupDAO.search(parameters).stream()
                    .map(warningGroup -> {
                        String path = null;
                        try {
                            path = CategoryRelationUtils.getPath(warningGroup.getCategoryId());
                        } catch (AtlasBaseException e) {
                            LOG.error(e.getMessage(), e);
                        }
                        warningGroup.setPath(path);
                        return warningGroup;
                    }).collect(Collectors.toList());
            PageResult<WarningGroup> pageResult = new PageResult<>();
            long sum = warningGroupDAO.countBySearch(parameters.getQuery());
            pageResult.setOffset(parameters.getOffset());
            pageResult.setSum(sum);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public PageResult<WarningGroup> getWarningGroupList(Parameters parameters) throws AtlasBaseException {
        try {
            List<WarningGroup> list = warningGroupDAO.getWarningGroup(parameters);
            for (WarningGroup group : list) {
                String numberStr = group.getContacts();
                String[] numberArr = numberStr.split(",");
                group.setNumberCount(numberArr.length);
            }
            PageResult<WarningGroup> pageResult = new PageResult<>();
            long sum = warningGroupDAO.countWarningGroup(parameters);
            pageResult.setOffset(parameters.getOffset());
            pageResult.setSum(sum);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }


    public PageResult<TaskWarningHeader> getWarningList(Integer warningType, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<TaskWarningHeader> pageResult = new PageResult<>();
            List<TaskWarningHeader> warningList = warningGroupDAO.getWarningList(warningType, parameters);
            for (TaskWarningHeader warning : warningList) {
                List<TaskWarningHeader.WarningGroupHeader> groupHeaderList = warningGroupDAO.getWarningGroupList(warning.getTaskId(), 0);
                warning.setWarningGroupList(groupHeaderList);
            }
            Long count = warningGroupDAO.countWarning(warningType, parameters);
            pageResult.setSum(count);
            pageResult.setLists(warningList);
            pageResult.setCount(warningList.size());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public PageResult<TaskErrorHeader> getErrorWarningList(Integer errorType, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<TaskErrorHeader> pageResult = new PageResult<>();
            List<TaskErrorHeader> warningList = warningGroupDAO.getErrorWarningList(errorType, parameters);
            for (TaskErrorHeader error : warningList) {
                List<TaskWarningHeader.WarningGroupHeader> groupHeaderList = warningGroupDAO.getWarningGroupList(error.getTaskId(), 0);
                error.setWarningGroupList(groupHeaderList);
            }
            Long count = warningGroupDAO.countError(errorType, parameters);
            pageResult.setSum(count);
            pageResult.setLists(warningList);
            pageResult.setCount(warningList.size());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void closeTaskExecutionWarning(Integer warningType, List<String> taskIdList) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            warningGroupDAO.closeTaskExecutionWarning(warningType, taskIdList);
            warningGroupDAO.closeAllTaskRuleExecutionWarning(warningType, taskIdList, currentTime, userId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public WarningInfo getWarningInfo(String executionRuleId) throws AtlasBaseException {
        try {
            //任务名称、告警发生时间、告警关闭时间、告警处理人
            WarningInfo info = warningGroupDAO.getWarningBasicInfo(executionRuleId);
            Map<String, Table> idToTable = new HashMap<>();
            Map<String, Column> idToColumn = new HashMap<>();
            List<WarningInfo.SubTaskWarning> subTaskList =warningGroupDAO.getSubTaskWarning(executionRuleId);
            for (WarningInfo.SubTaskWarning subTask : subTaskList) {
                String subTaskId = subTask.getSubTaskId();
                List<WarningInfo.SubTaskRuleWarning> subTaskRuleWarningList = warningGroupDAO.getSubTaskRuleWarning(subTaskId);
                for (WarningInfo.SubTaskRuleWarning subTaskRuleWarning : subTaskRuleWarningList) {
                    String objectId = subTaskRuleWarning.getObjectId();
                    Integer dataSourceType = subTask.getRuleType();
                    if(0 == dataSourceType) {
                        Table table = null;
                        if(idToTable.containsKey(objectId)) {
                            table = idToTable.get(objectId);
                        } else {
                            table = taskManageDAO.getDbAndTableName(objectId);
                            idToTable.put(objectId, table);
                        }
                        String dbName = table.getDatabaseName();
                        String tableName = table.getTableName();
                        subTaskRuleWarning.setDbName(dbName);
                        subTaskRuleWarning.setTableName(tableName);
                    } else if(1 == dataSourceType) {
                        Column column = null;
                        if(idToColumn.containsKey(objectId)) {
                            column = idToColumn.get(objectId);
                            idToColumn.put(objectId, column);
                        } else {
                            column = taskManageDAO.getDbAndTableAndColumnName(objectId);
                        }
                        String dbName = column.getDatabaseName();
                        String tableName = column.getTableName();
                        String columnName = column.getColumnName();
                        subTaskRuleWarning.setDbName(dbName);
                        subTaskRuleWarning.setTableName(tableName);
                        subTaskRuleWarning.setColumnName(columnName);
                    }

                    subTaskRuleWarning.setWarningMessage(subTaskRuleWarning.getRuleName() + "为" + subTaskRuleWarning.getResult() + subTaskRuleWarning.getUnit());
                }
                subTask.setSubTaskList(subTaskRuleWarningList);
            }
            info.setSubTaskList(subTaskList);

            //通知对象
            /*List<String> warningGroupMemberList = warningGroupDAO.getWarningGroupMemberList(executionRuleId);
            Set<String> memberSet = new HashSet<>();
            StringJoiner memberJoiner = new StringJoiner(";");
            for (String warningGroup : warningGroupMemberList) {
                List<String> members = Arrays.asList(warningGroup.split(","));
                members.forEach(member -> memberSet.add(member));
            }
            memberSet.stream().forEach(member -> memberJoiner.add(member));*/

            return info;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public ErrorInfo getErrorInfo(String executionRuleId) throws AtlasBaseException {
        try {
            return warningGroupDAO.getErrorInfo(executionRuleId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

}
