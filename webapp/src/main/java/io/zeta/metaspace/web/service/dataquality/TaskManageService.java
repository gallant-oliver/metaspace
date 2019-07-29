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
 * @date 2019/7/24 10:29
 */
package io.zeta.metaspace.web.service.dataquality;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 10:29
 */

import io.zeta.metaspace.model.dataquality2.DataQualitySubTask;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskObject;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskRule;
import io.zeta.metaspace.model.dataquality2.DataQualityTask;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.dataquality2.ObjectType;
import io.zeta.metaspace.model.dataquality2.RuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskHeader;
import io.zeta.metaspace.model.dataquality2.TaskInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.task.quartz.QuartJob;
import io.zeta.metaspace.web.task.quartz.QuartzJob;
import io.zeta.metaspace.web.task.quartz.QuartzManager;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskManageService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManageService.class);
    public static String JOB_GROUP_NAME = "METASPACE_JOBGROUP";
    public static String TRIGGER_NAME = "METASPACE_TRIGGER";
    public static String TRIGGER_GROUP_NAME = "METASPACE_TRIGGERGROUP";

    @Autowired
    TaskManageDAO taskManageDAO;

    @Autowired
    RuleService ruleService;

    @Autowired
    QuartzManager quartzManager;

    public PageResult<TaskHeader> getTaskList(Integer my, Parameters parameters) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            List<TaskHeader> list = taskManageDAO.getTaskList(my, userId, parameters);
            PageResult<TaskHeader> pageResult = new PageResult<>();
            long totalSize = taskManageDAO.countTaskList(my, userId, parameters);
            pageResult.setOffset(parameters.getOffset());
            pageResult.setSum(totalSize);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.info(e.toString());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void deleteTaskList(List<String> taskIdList) throws AtlasBaseException {
        try {
            if(Objects.nonNull(taskIdList)) {
                taskManageDAO.deleteTaskList(taskIdList);
                taskManageDAO.deleteSubTaskList(taskIdList);
                taskManageDAO.deleteSubTaskObjectList(taskIdList);
                taskManageDAO.deleteSubTaskRuleList(taskIdList);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public List<RuleHeader> getValidRuleList(String groupId, int objType, List<String> objIdList) throws AtlasBaseException {
        try {
            List<RuleHeader> ruleList = taskManageDAO.getRuleListByCategoryId(groupId);
            if(Objects.nonNull(ruleList)) {
                if(ObjectType.TABLE == ObjectType.of(objType)) {
                    return ruleList.stream().filter(rule -> 0==rule.getScope()).collect(Collectors.toList());
                } else {
                    List<String> columnTypeList = taskManageDAO.getColumnTypeList(objIdList);
                    boolean allNumericValue = true;
                    for (String columnType : columnTypeList) {
                        allNumericValue = HiveNumericType.isNumericType(columnType);
                        if(!allNumericValue) {
                            break;
                        }
                    }
                    if(allNumericValue) {
                        return ruleList.stream().filter(rule -> 1==rule.getScope()).collect(Collectors.toList());
                    } else {
                        List<String> numericTypeTempateIdlist = taskManageDAO.getNumericTypeTemplateRuleId();
                        return ruleList.stream().filter(rule -> !numericTypeTempateIdlist.contains(rule.getRuleTemplateId())).collect(Collectors.toList());
                    }
                }
            }
            return null;
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void addTask(TaskInfo taskInfo) throws AtlasBaseException {
        Timestamp currentTime = DateUtils.currentTimestamp();
        addDataQualityTask(currentTime, taskInfo);
    }

    public int addDataQualityTask(Timestamp currentTime, TaskInfo taskInfo) throws AtlasBaseException {
        try {
            DataQualityTask dataQualityTask = new DataQualityTask();
            //id
            String guid = UUID.randomUUID().toString();
            dataQualityTask.setId(guid);
            //任务名称
            dataQualityTask.setName(taskInfo.getTaskName());
            //任务级别
            dataQualityTask.setLevel(taskInfo.getLevel());
            //描述
            dataQualityTask.setDescription(taskInfo.getDescription());
            //cron表达式
            dataQualityTask.setCronExpression(taskInfo.getCronExpression());
            //执行起始时间
            dataQualityTask.setStartTime(taskInfo.getStartTime());
            //执行截止时间
            dataQualityTask.setEndTime(taskInfo.getEndTime());
            //异常告警组
            List<String> errorGroupList = taskInfo.getErrorWarningGroupList();
            StringJoiner errorGroupIdJoiner = new StringJoiner(",");
            if (Objects.nonNull(errorGroupList)) {
                errorGroupList.forEach(errorId -> errorGroupIdJoiner.add(errorId));
            }
            dataQualityTask.setErrorWarningGroupIds(errorGroupIdJoiner.toString());
            //创建时间
            dataQualityTask.setCreateTime(currentTime);
            //更新时间
            dataQualityTask.setUpdateTime(currentTime);
            //创建者
            String userId = AdminUtils.getUserData().getUserId();
            dataQualityTask.setCreator(userId);
            //是否已删除
            dataQualityTask.setDelete(false);
            //是否开启
            dataQualityTask.setEnable(false);
            //子任务
            List<TaskInfo.SubTask> subTaskList = taskInfo.getTaskList();
            addDataQualitySubTask(guid, currentTime, subTaskList);
            return taskManageDAO.addDataQualityTask(dataQualityTask);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void addDataQualitySubTask(String taskId, Timestamp currentTime, List<TaskInfo.SubTask> subTaskList) throws AtlasBaseException {
        try {
            for(int i=0, size=subTaskList.size(); i<size; i++) {
                TaskInfo.SubTask subTask = subTaskList.get(i);
                DataQualitySubTask dataQualitySubTask = new DataQualitySubTask();
                //id
                String guid = UUID.randomUUID().toString();
                dataQualitySubTask.setId(guid);
                //taskId
                dataQualitySubTask.setTaskId(taskId);
                //数据源类型
                dataQualitySubTask.setDataSourceType(subTask.getDataSourceType());
                //序号
                dataQualitySubTask.setSequence(i+1);
                //创建时间
                dataQualitySubTask.setCreateTime(currentTime);
                //更新时间
                dataQualitySubTask.setUpdateTime(currentTime);
                //是否已删除
                dataQualitySubTask.setDelete(false);
                //subTaskRule
                List<TaskInfo.SubTaskRule> subTaskRuleList = subTask.getSubTaskRuleList();
                addDataQualitySubTaskRule(guid, currentTime, subTaskRuleList);
                //object
                List<String> objectIdList = subTask.getObjectIdList();
                addDataQualitySubTaskObject(guid, currentTime, objectIdList);
                taskManageDAO.addDataQualitySubTask(dataQualitySubTask);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void addDataQualitySubTaskObject(String subTaskId, Timestamp currentTime, List<String> objectIdList) throws AtlasBaseException {
        try {
            for (int i = 0, size = objectIdList.size(); i < size; i++) {
                String objectId = objectIdList.get(i);
                DataQualitySubTaskObject dataQualitySubTaskObject = new DataQualitySubTaskObject();
                //id
                String guid = UUID.randomUUID().toString();
                dataQualitySubTaskObject.setId(guid);
                //子任务Id
                dataQualitySubTaskObject.setSubTaskId(subTaskId);
                //对象Id
                dataQualitySubTaskObject.setObjectId(objectId);
                //序号
                dataQualitySubTaskObject.setSequence(i + 1);
                //创建时间
                dataQualitySubTaskObject.setCreateTime(currentTime);
                //更新时间
                dataQualitySubTaskObject.setUpdateTime(currentTime);
                //是否已删除
                dataQualitySubTaskObject.setDelete(false);
                taskManageDAO.addDataQualitySubTaskObject(dataQualitySubTaskObject);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void addDataQualitySubTaskRule(String subTaskId, Timestamp currentTime, List<TaskInfo.SubTaskRule> subTaskRuleList) throws AtlasBaseException {
        try {
            for (int i=0,size=subTaskRuleList.size(); i<size; i++) {
                TaskInfo.SubTaskRule rule = subTaskRuleList.get(i);
                DataQualitySubTaskRule subTaskRule = new DataQualitySubTaskRule();
                //id
                String guid = UUID.randomUUID().toString();
                subTaskRule.setId(guid);
                //子任务Id
                subTaskRule.setSubTaskId(subTaskId);
                //规则Id
                subTaskRule.setRuleId(rule.getRuleId());
                //校验阈值
                subTaskRule.setCheckThreshold(rule.getCheckThreshold());
                //order
                subTaskRule.setSequence(i+1);

                List<TaskInfo.Warning> warningList = rule.getWarnings();
                if (Objects.nonNull(warningList)) {
                    for (TaskInfo.Warning warning : warningList) {
                        Integer warningType = warning.getWarningType();
                        if (1 == warningType) {
                            //橙色校验类型
                            Integer warningCheckType = warning.getWarningCheckType();
                            subTaskRule.setOrangeCheckType(warningCheckType);
                            //橙色校验表达式
                            Integer warningCheckExpression = warning.getWarningCheckExpression();
                            subTaskRule.setOrangeCheckExpression(warningCheckExpression);
                            //橙色阈值
                            String warningThreshold = warning.getWarningCheckThreshold();
                            subTaskRule.setOrangeThreshold(warningThreshold);
                            //橙色告警组
                            List<String> warningNotificationGroup = warning.getWarningNotificationIdList();
                            StringJoiner warningGroupJoiner = new StringJoiner(",");
                            if (Objects.nonNull(warningNotificationGroup) && warningNotificationGroup.size() > 0) {
                                warningNotificationGroup.forEach(groupId -> warningGroupJoiner.add(groupId));
                                subTaskRule.setOrangeWarningGroupIds(warningGroupJoiner.toString());
                            }
                        } else if (2 == warningType) {
                            //红色校验类型
                            Integer warningCheckType = warning.getWarningCheckType();
                            subTaskRule.setRedCheckType(warningCheckType);
                            //红色校验表达式
                            Integer warningCheckExpression = warning.getWarningCheckExpression();
                            subTaskRule.setRedCheckExpression(warningCheckExpression);
                            //红色阈值
                            String warningThreshold = warning.getWarningCheckThreshold();
                            subTaskRule.setRedThreshold(warningThreshold);
                            //红色告警组
                            List<String> warningNotificationGroup = warning.getWarningNotificationIdList();
                            StringJoiner warningGroupJoiner = new StringJoiner(",");
                            if (Objects.nonNull(warningNotificationGroup) && warningNotificationGroup.size() > 0) {
                                warningNotificationGroup.forEach(groupId -> warningGroupJoiner.add(groupId));
                                subTaskRule.setRedWarningGroupIds(warningGroupJoiner.toString());
                            }
                        }
                    }
                }
                //创建时间
                subTaskRule.setCreateTime(currentTime);
                //更新时间
                subTaskRule.setUpdateTime(currentTime);
                //是否已删除
                subTaskRule.setDelete(false);
                taskManageDAO.addDataQualitySubTaskRule(subTaskRule);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public DataQualityTask getTaskBasicInfo(String guid) {
        return taskManageDAO.getTaskBasicInfo(guid);
    }


    public void startTask(String taskId) throws AtlasBaseException {
        try {
            String qrtzName = taskManageDAO.getQrtzJobByTaskId(taskId);
            if (Objects.isNull(qrtzName) || qrtzName.trim().length() == 0) {
                addQuartzJob(taskId);
            } else {
                String jobGroupName = JOB_GROUP_NAME + qrtzName;
                quartzManager.resumeJob(qrtzName, jobGroupName);
            }
            //设置任务状态为【启用】
            taskManageDAO.updateTaskStatus(taskId, true);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void stopTask(String taskId) throws AtlasBaseException {
        try {
            String jobName = taskManageDAO.getQrtzJobByTaskId(taskId);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            quartzManager.pauseJob(jobName, jobGroupName);
            //设置模板状态为【暂停】
            taskManageDAO.updateTaskStatus(taskId, false);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭模板失败");
        }
    }


    public void addQuartzJob(String taskId) throws AtlasBaseException {
        try {
            long currentTime = System.currentTimeMillis();
            String jobName = String.valueOf(currentTime);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            String triggerName = TRIGGER_NAME + jobName;
            String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
            DataQualityTask task = taskManageDAO.getQrtzInfoByTemplateId(taskId);
            String cron = task.getCronExpression();
            Timestamp startTime = task.getStartTime();
            Timestamp endTime = task.getEndTime();
            Integer level = task.getLevel();
            quartzManager.addCronJobWithTimeRange(jobName, jobGroupName, triggerName, triggerGroupName, QuartzJob.class, cron, level, startTime, endTime);
            //添加qrtzName
            taskManageDAO.updateTaskQrtzName(taskId, jobName);

        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

}
