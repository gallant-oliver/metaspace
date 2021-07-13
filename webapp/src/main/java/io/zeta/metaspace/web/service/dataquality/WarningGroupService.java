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

import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.datasource.DataSource;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.utils.GsonUtils;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.dao.dataquality.WarningGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeansUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WarningGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(WarningGroupService.class);

    @Autowired
    private WarningGroupDAO warningGroupDAO;

    @Autowired
    private TaskManageDAO taskManageDAO;

    public int insert(WarningGroup warningGroup,String tenantId) throws AtlasBaseException {
        try {
            warningGroup.setId(UUID.randomUUID().toString());
            warningGroup.setCreateTime(DateUtils.currentTimestamp());
            warningGroup.setUpdateTime(DateUtils.currentTimestamp());
            warningGroup.setCreator(AdminUtils.getUserData().getUserId());
            warningGroup.setDelete(false);
            return warningGroupDAO.insert(warningGroup,tenantId);
        } catch (Exception e) {
            LOG.error("添加告警组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加告警组失败");
        }
    }

    public WarningGroup getById(String id) throws AtlasBaseException {
        try {
            WarningGroup warningGroup = warningGroupDAO.getById(id);
            return warningGroup;
        } catch (Exception e) {
            LOG.error("获取告警组详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组详情失败");
        }
    }

    public WarningGroup getByName(String name,String id,String tenantId) throws AtlasBaseException {
        return warningGroupDAO.getByName(name,id,tenantId);
    }

    public void deleteById(String number) throws AtlasBaseException {
        warningGroupDAO.deleteById(number);
    }

    public void deleteByIdList(List<String> numberList) throws AtlasBaseException {
        try {
            for (String guid : numberList) {
                Integer count = warningGroupDAO.countWarningGroupUserd(guid);
                if (null != count && count > 0) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"当前告警组正在被使用，不允许删除");
                }
            }
            warningGroupDAO.deleteByIdList(numberList);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("删除告警组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除告警组失败");
        }
    }

    public int update(WarningGroup warningGroup) throws AtlasBaseException {
        try {
            warningGroup.setUpdateTime(DateUtils.currentTimestamp());
            WarningGroup old = getById(warningGroup.getId());
            BeansUtil.copyPropertiesIgnoreNull(warningGroup, old);
            return warningGroupDAO.update(old);
        } catch (Exception e) {
            LOG.error("更新告警组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新告警组失败");
        }
    }

    public PageResult<WarningGroup> search(Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            List<WarningGroup> list = warningGroupDAO.search(parameters,tenantId);
            PageResult<WarningGroup> pageResult = new PageResult<>();
            long totalSize = 0;
            if (list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("搜索告警组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索告警组失败");
        }
    }

    public PageResult<WarningGroup> getWarningGroupList(Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            String query = parameters.getQuery();
            if (Objects.nonNull(query)) {
                parameters.setQuery(query.replaceAll("_", "/_").replaceAll("%", "/%"));
            }
            List<WarningGroup> list;
            try {
                list = warningGroupDAO.getWarningGroup(parameters, tenantId);
            }catch (SQLException e){
                LOG.error("SQL执行异常", e);
                list = new ArrayList<>();
            }
            for (WarningGroup group : list) {
                String numberStr = group.getContacts();
                String[] numberArr = numberStr.split(",");
                group.setNumberCount(numberArr.length);
            }
            PageResult<WarningGroup> pageResult = new PageResult<>();
            long totalSize =0;
            if (list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取告警组列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组列表失败");
        }
    }


    public PageResult<TaskWarningHeader> getWarningList(Integer warningType, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            PageResult<TaskWarningHeader> pageResult = new PageResult<>();
            List<TaskWarningHeader> warningList = warningGroupDAO.getWarningList(warningType, parameters,tenantId);
            for (TaskWarningHeader warning : warningList) {
                List<TaskWarningHeader.WarningGroupHeader> groupHeaderList = warningGroupDAO.getWarningGroupList(warning.getTaskId(), 0);
                warning.setWarningGroupList(groupHeaderList);
            }
            long totalSize = 0;
            if (warningList.size()!=0){
                totalSize = warningList.get(0).getTotal();
            }
            pageResult.setTotalSize(totalSize);
            pageResult.setLists(warningList);
            pageResult.setCurrentSize(warningList.size());
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取告警列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警列表失败");
        }
    }

    public PageResult<TaskErrorHeader> getErrorWarningList(Integer errorType, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            PageResult<TaskErrorHeader> pageResult = new PageResult<>();
            List<TaskErrorHeader> warningList = warningGroupDAO.getErrorWarningList(errorType, parameters,tenantId);
            for (TaskErrorHeader error : warningList) {
                List<TaskWarningHeader.WarningGroupHeader> groupHeaderList = warningGroupDAO.getWarningGroupList(error.getTaskId(), 0);
                error.setWarningGroupList(groupHeaderList);
            }
            long totalSize = 0;
            if (warningList.size()!=0){
                totalSize = warningList.get(0).getTotal();
            }
            pageResult.setTotalSize(totalSize);
            pageResult.setLists(warningList);
            pageResult.setCurrentSize(warningList.size());
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取异常列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取异常列表失败");
        }
    }



    public void closeTaskExecutionWarning(Integer warningType, List<String> taskIdList) throws AtlasBaseException {
        try {
            warningGroupDAO.closeTaskExecutionWarning(warningType, taskIdList);
        } catch (Exception e) {
            LOG.error("关闭失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭失败");
        }
    }
    public void closeWarns(List<String> warnNos) throws AtlasBaseException {
        try {
            if(CollectionUtils.isEmpty(warnNos)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭告警失败，告警编号为空");
            }
            Map<String, List<Integer>> idAndGrades = new HashMap<>();
            warnNos.stream().forEach(warnNo -> {
                String[] idAndGrade = warnNo.split("_");
                if(idAndGrade.length != 2){
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭失败,告警编号【" + warnNo + "】错误，格式应为id_warnGrade");
                }
                if(!idAndGrades.containsKey(idAndGrade[0])){
                    List<Integer> grades = new ArrayList<>();
                    idAndGrades.put(idAndGrade[0],grades);
                }
                idAndGrades.get(idAndGrade[0]).add(Integer.valueOf(idAndGrade[1]));
            });

            Set<Map.Entry<String, List<Integer>>> entries = idAndGrades.entrySet();
            for (Map.Entry<String, List<Integer>> entry: entries){
                String id = entry.getKey();
                RuleExecute currentRuleExecute = warningGroupDAO.getRuleExecute(id);
                List<Integer> grades = entry.getValue();
                grades.stream().forEach(grade ->{
                    switch (grade){
                        case 0:currentRuleExecute.setGeneralWarningCheckStatus(2);
                            break;
                        case 1:currentRuleExecute.setOrangeWarningCheckStatus(2);
                            break;
                        default:currentRuleExecute.setRedWarningCheckStatus(2);
                    }
                });
                warningGroupDAO.closeRuleExecuteWarn(currentRuleExecute);
                if(!haveWarn(currentRuleExecute)){
                    warningGroupDAO.closeExecutionWarning(currentRuleExecute.getTaskExecuteId());
                }
            }
        } catch (Exception e) {
            LOG.error("关闭失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭失败");
        }
    }

    public void closeErrors(List<String> errorIds) throws AtlasBaseException {
        try {
            if(CollectionUtils.isEmpty(errorIds)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭异常失败，异常编号为空");
            }
            for (String id: errorIds){
                RuleExecute ruleExecute = warningGroupDAO.getRuleExecute(id);
                if(ruleExecute == null){
                    continue;
                }
                warningGroupDAO.closeRuleExecuteError(id);
                if(!haveError(ruleExecute)){
                    warningGroupDAO.closeExecutionError(ruleExecute.getTaskExecuteId());
                }
            }
        } catch (Exception e) {
            LOG.error("关闭失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭失败");
        }
    }

    public boolean haveError(RuleExecute currentRuleExecute){

        List<RuleExecute> ruleExecutes = warningGroupDAO.getRuleExecutes(currentRuleExecute.getTaskExecuteId());
        for (RuleExecute ruleExecute: ruleExecutes){
            if(ruleExecute.getErrorStatus() != null && ruleExecute.getErrorStatus() == 1){
                //有异常未关闭
                return true;
            }
        }
        //没有任何异常了
        return false;
    }

    public boolean haveWarn(RuleExecute currentRuleExecute){
        List<RuleExecute> ruleExecutes = warningGroupDAO.getRuleExecutes(currentRuleExecute.getTaskExecuteId());
        for (RuleExecute ruleExecute: ruleExecutes){
            if(ruleExecute.getGeneralWarningCheckStatus() != null && ruleExecute.getGeneralWarningCheckStatus() == 1){
                //有普通告警
                return true;
            }
            if(ruleExecute.getOrangeWarningCheckStatus() != null && ruleExecute.getOrangeWarningCheckStatus()== 1){
                //有黄色告警
                return true;
            }
            if(ruleExecute.getRedWarningCheckStatus() != null && ruleExecute.getRedWarningCheckStatus()== 1){
                //有红色告警
                return true;
            }
        }
        //没有任何告警了
        return false;
    }

    public WarningInfo getWarningInfo(String executionId,String tenantId) throws AtlasBaseException {
        try {
            //任务名称、告警发生时间、告警关闭时间、告警处理人
            WarningInfo info = warningGroupDAO.getWarningBasicInfo(executionId);
//            Map<String, Table> idToTable = new HashMap<>();
//            Map<String, Column> idToColumn = new HashMap<>();
            List<WarningInfo.SubTaskWarning> subTaskList =warningGroupDAO.getSubTaskWarning(executionId);
            for (WarningInfo.SubTaskWarning subTask : subTaskList) {
                String subTaskId = subTask.getSubTaskId();
                List<WarningInfo.SubTaskRuleWarning> subTaskRuleWarningList = warningGroupDAO.getSubTaskRuleWarning(executionId, subTaskId,tenantId);
                for (WarningInfo.SubTaskRuleWarning subTaskRuleWarning : subTaskRuleWarningList) {
                    String objectId = subTaskRuleWarning.getObjectId();
//                    Integer dataSourceType = subTask.getRuleType();
                    int scope = subTaskRuleWarning.getScope();
                    int type = subTaskRuleWarning.getType();
                    if(0 == scope) { //表级别检测
                        //由于非HIVE数据源已经和任务等模块在元数据上接耦合，所以下方注释逻辑已经不适用
//                        Table table = null;
//                        if(idToTable.containsKey(objectId)) {
//                            table = idToTable.get(objectId);
//                        } else {
//                            table = taskManageDAO.getDbAndTableName(objectId);
//                            idToTable.put(objectId, table);
//                        }
//                        if(table == null) {
//                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到表信息，当前表已被删除!");
//                        }
//                        String dbName = table.getDatabaseName();
//                        String tableName = table.getTableName();
                        CustomizeParam paraminfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                        subTaskRuleWarning.setDbName(paraminfo.getSchema());
                        subTaskRuleWarning.setTableName(paraminfo.getTable());
                    } else if(1 == scope) { //字段检测规则
                        //同上方注释
//                        Column column = null;
//                        if(idToColumn.containsKey(objectId)) {
//                            column = idToColumn.get(objectId);
//                            idToColumn.put(objectId, column);
//                        } else {
//                            column = taskManageDAO.getDbAndTableAndColumnName(objectId);
//                        }
//                        String dbName = column.getDatabaseName();
//                        String tableName = column.getTableName();
//                        String columnName = column.getColumnName();
                        CustomizeParam paraminfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                        subTaskRuleWarning.setDbName(paraminfo.getSchema());
                        subTaskRuleWarning.setTableName(paraminfo.getTable());
                        subTaskRuleWarning.setColumnName(paraminfo.getColumn());
                    }else if(2 ==scope && 31 == type){  // 一致性规则
                        List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                        }.getType());
                        ConsistencyParam consistencyParam = params.stream().filter(p -> p.isStandard()).collect(Collectors.toList()).get(0);
                        subTaskRuleWarning.setDbName(consistencyParam.getSchema());
                        subTaskRuleWarning.setTableName(consistencyParam.getTable());
                        subTaskRuleWarning.setColumnName(consistencyParam.getCompareFields().stream().collect(Collectors.joining(",")));
                    }else if(2 ==scope && 32 == type){  // 自定义规则
                       //无法获取库表
                    }

                    subTaskRuleWarning.setWarningMessage(subTaskRuleWarning.getRuleName() + "为" + subTaskRuleWarning.getResult() + subTaskRuleWarning.getUnit());
                }
                subTask.setSubTaskList(subTaskRuleWarningList);
            }
            info.setSubTaskList(subTaskList);

            //通知对象

            return info;
        } catch (Exception e) {
            LOG.error("获取告警信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警信息失败");
        }
    }

    public PageResult<WarnInformation> getWarns(Parameters parameters,String tenantId, int warnType){
        List<WarnInformation> warns = warningGroupDAO.getWarns(parameters, warnType, tenantId);
        return getDetails(parameters, warns, error -> error.getWarnNo() + "_" + error.getWarnGrade());
    }


    public PageResult<WarnInformation> getErrors(Parameters parameters,String tenantId, int errorType){

        List<WarnInformation> errors = warningGroupDAO.getErrors(parameters, errorType, tenantId);
        return getDetails(parameters, errors, error -> error.getWarnNo());
    }


    public PageResult<WarnInformation> getDetails(Parameters parameters, List<WarnInformation> errors, Function<WarnInformation, String> apply){
        PageResult<WarnInformation> pageResult = new PageResult<>();
        pageResult.setOffset(parameters.getOffset());
        if(CollectionUtils.isEmpty(errors)){
            pageResult.setTotalSize(0);
            pageResult.setCurrentSize(0);
            return pageResult;
        }
        for (WarnInformation error : errors) {
            String unit = error.getUnit();
            if(null == unit){
                error.setUnit("");
            }
            String errorNo = apply.apply(error);
            error.setWarnNo(errorNo);
            List<TaskWarningHeader.WarningGroupHeader> groupHeaderList = warningGroupDAO.getWarningGroupList(error.getTaskId(), 0);
            error.setWarnGroupNames(groupHeaderList);
            int scope = error.getScope();
            if(0 == scope || 1 == scope) {
                //表列级别检测
                CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(error.getObjectId(), CustomizeParam.class);
                if(!"hive".equalsIgnoreCase(paramInfo.getDataSourceId())){
                    DataSource dataSource = warningGroupDAO.getDataSource(paramInfo.getDataSourceId());
                    if(Objects.isNull(dataSource)){
                        error.setObject("数据源已被删除");
                        continue;
                    }
                    paramInfo.setDataSourceName(dataSource.getSourceName());
                    paramInfo.setSchema(paramInfo.getSchema() + "." + dataSource.getDatabase());
                }else{
                    paramInfo.setDataSourceName("hive");
                }
                error.setObject(paramInfo);
            } else if(2 ==scope && 31 == error.getType()){
                // 一致性规则
                List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(error.getObjectId(), new TypeToken<List<ConsistencyParam>>() {
                }.getType());
                StringBuilder builder = new StringBuilder();
                int size = params.get(0).getJoinFields().size();
                int allSize = size + params.get(0).getCompareFields().size();
                for(int i = 0; i < allSize; i ++){
                    builder.append("(");
                    for (ConsistencyParam param: params) {
                        List<String> joinFields = param.getJoinFields();
                        List<String> compareFields = param.getCompareFields();
                        if(size>i){
                            builder.append(joinFields.get(i)).append(";");
                        }else{
                            builder.append(compareFields.get(i - size)).append(";");
                        }
                    }
                    builder = builder.delete(builder.length() -1, builder.length());
                    builder.append(") ");
                }
                error.setObject(builder.toString());
            }else{
                error.setObject(error.getSql()==null?"":error.getSql());
            }
        }
        Iterator<WarnInformation> it = errors.iterator();
        while(it.hasNext()){
            WarnInformation warnInformation = (WarnInformation)it.next();
            if(warnInformation.getObject().equals("数据源已被删除")){
                it.remove();
            }
        }
        pageResult.setTotalSize(errors.get(0).getTotal());
        pageResult.setLists(errors);
        pageResult.setCurrentSize(errors.size());
        return pageResult;
    }

    public ErrorInfo getErrorInfo(String executionRuleId) throws AtlasBaseException {
        try {
            return warningGroupDAO.getErrorInfo(executionRuleId);
        } catch (Exception e) {
            LOG.error("获取异常信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取异常信息ssss失败");
        }
    }

}
