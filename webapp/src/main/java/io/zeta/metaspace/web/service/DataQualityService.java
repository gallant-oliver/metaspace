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
 * @date 2019/1/8 19:41
 */
package io.zeta.metaspace.web.service;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/8 19:41
 */

import io.zeta.metaspace.model.dataquality.CheckExpression;
import io.zeta.metaspace.model.dataquality.DataType;
import io.zeta.metaspace.model.dataquality.ExcelReport;
import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.ReportError;
import io.zeta.metaspace.model.dataquality.RuleCheckType;
import io.zeta.metaspace.model.dataquality.RuleType;
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.dataquality.TemplateStatus;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.model.dataquality.RuleStatus;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.ColumnQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.ReportResult;
import io.zeta.metaspace.model.result.TableColumnRules;
import io.zeta.metaspace.model.result.TemplateResult;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.task.quartz.QuartJob;
import io.zeta.metaspace.web.task.quartz.QuartzManager;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DataQualityService {
    private static final Logger LOG = LoggerFactory.getLogger(HomePageService.class);
    public static String JOB_GROUP_NAME = "METASPACE_JOBGROUP";
    public static String TRIGGER_NAME = "METASPACE_TRIGGER";
    public static String TRIGGER_GROUP_NAME = "METASPACE_TRIGGERGROUP";

    @Autowired
    DataQualityDAO qualityDao;
    @Autowired
    QuartzManager quartzManager;
    @Autowired
    private MetaDataService metadataService;

    @Transactional(rollbackFor=Exception.class)
    public void addTemplate(Template template,String tenantId) throws AtlasBaseException {
        try {
            String templateId = UUID.randomUUID().toString();
            String templateName = template.getTemplateName();
            int sameNameCount = qualityDao.countTemplateName(templateName,templateId,tenantId);
            if(sameNameCount > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "模板名称重复");
            } else {
                template.setTemplateId(templateId);
                addRulesByTemlpateId(template);
                template.setGenerateTime(System.currentTimeMillis());
                //template
                qualityDao.insertTemplate(template,tenantId);
                qualityDao.updateFinishedPercent(templateId, 0F);
                //设置模板状态为【未启用】
                qualityDao.updateTemplateStatus(TemplateStatus.NOT_RUNNING.code, templateId);
            }
        }  catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加模板失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void deleteTemplate(String templateId) throws AtlasBaseException {
        try {
            //template
            qualityDao.delTemplate(templateId);
            qualityDao.deleteTemplate2QrtzByTemplateId(templateId);
            //template2qurt
            deleteRulesByTemplateId(templateId);
            //job
            String jobName = qualityDao.getJobByTemplateId(templateId);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            String triggerName  = TRIGGER_NAME + jobName;
            String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
            quartzManager.removeJob(jobName, jobGroupName, triggerName, triggerGroupName);

        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("删除模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除模板失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void addRulesByTemlpateId(Template template) throws AtlasBaseException {
        String templateId = template.getTemplateId();
        List<UserRule> userRules = template.getRules();
        int tableRuleNum = 0;
        int columnRuleNum = 0;
        try {
            Long interval = 0L;
            for (UserRule rule : userRules) {
                if(rule.getRuleType() == 0)
                    tableRuleNum++;
                else if(rule.getRuleType() == 1)
                    columnRuleNum++;
                rule.setTemplateId(templateId);
                String ruleId = UUID.randomUUID().toString();
                Long generateTime = System.currentTimeMillis();
                generateTime += (interval++);
                rule.setRuleId(ruleId);
                rule.setGenerateTime(generateTime);
                qualityDao.insertUserRule(rule);
                //threshold
                List<Double> thresholds = rule.getRuleCheckThreshold();
                for (Double threshold : thresholds) {
                    qualityDao.insertTemplateRuleThreshold(threshold, ruleId);
                }
            }
            template.setTableRulesNum(tableRuleNum);
            template.setColumnRulesNum(columnRuleNum);
        } catch (SQLException e) {
            LOG.error( "数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("添加规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加规则失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void deleteRulesByTemplateId(String templateId) throws AtlasBaseException {
        try {
            //rules
            List<String> ruleIds = qualityDao.queryRuleIdByTemplateId(templateId);
            qualityDao.delRuleByTemplateId(templateId);
            //threshold
            for (String ruleId : ruleIds) {
                qualityDao.deleteThresholdByRuleId(ruleId);
            }
        } catch (SQLException e) {
            LOG.error( "数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("删除规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除规则失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void updateTemplate(String templateId, Template template) throws AtlasBaseException {
        try {
            template.setTemplateId(templateId);
            deleteRulesByTemplateId(templateId);
            addRulesByTemlpateId(template);
            qualityDao.updateTemplate(template);
        } catch (SQLException e) {
            LOG.error( "数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("更新模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新模板失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public Template viewTemplate(String templateId) throws AtlasBaseException {
        try {
            Template template = qualityDao.queryTemplateById(templateId);
            List<UserRule> userRules = qualityDao.queryTemplateUserRuleById(templateId);
            for(UserRule rule : userRules) {
                String ruleId = rule.getRuleId();
                List<Double> thresholds = qualityDao.queryTemplateThresholdByRuleId(ruleId);
                rule.setRuleCheckThreshold(thresholds);
            }
            if(Objects.nonNull(userRules) && userRules.size()>0)
                template.setRules(userRules);
            return template;
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("查询异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void startTemplate(String templateId, int templateStatus) throws AtlasBaseException {
        //启动模板
        try {
            qualityDao.updateFinishedPercent(templateId, 0F);
            if(templateStatus == TemplateStatus.NOT_RUNNING.code) {
                addQuartzJob(templateId);
            } else if(templateStatus == TemplateStatus.SUSPENDING.code) {
                qualityDao.deleteReportError(templateId);
                String cron = qualityDao.getCronByTemplateId(templateId);
                if(Objects.nonNull(cron) && StringUtils.isNotEmpty(cron)) {
                    String jobName = qualityDao.getJobByTemplateId(templateId);
                    String jobGroupName = JOB_GROUP_NAME + jobName;
                    quartzManager.resumeJob(jobName, jobGroupName);
                } else {
                    //单次执行任务重新执行提交job
                    addQuartzJob(templateId);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long currentTime = System.currentTimeMillis();
                String currentTimeFormat = sdf.format(currentTime);
                qualityDao.updateTemplateStartTime(templateId, currentTimeFormat);
                //设置模板状态为【已启用】
                qualityDao.updateTemplateStatus(TemplateStatus.RUNNING.code, templateId);
            } else if(templateStatus == TemplateStatus.FINISHED.code) {
                //单次执行任务重新执行提交job
                addQuartzJob(templateId);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的模板状态");
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("启动模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "启动模板失败");
        }
    }

    public void addQuartzJob(String templateId) throws AtlasBaseException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long currentTime = System.currentTimeMillis();
            String currentTimeFormat = sdf.format(currentTime);
            qualityDao.updateTemplateStartTime(templateId, currentTimeFormat);
            String jobName = String.valueOf(currentTime);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            String triggerName = TRIGGER_NAME + jobName;
            String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
            String cron = qualityDao.getCronByTemplateId(templateId);
            quartzManager.addJob(jobName, jobGroupName, triggerName, triggerGroupName, QuartJob.class, cron);
            //设置模板状态为【已启用】
            qualityDao.insertTemplate2QrtzTrigger(templateId, jobName);
            qualityDao.updateTemplateStatus(TemplateStatus.RUNNING.code, templateId);
        } catch (Exception e) {
            LOG.error("添加任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

    public void stopTemplate(String templateId) throws AtlasBaseException {
        try {
            String jobName = qualityDao.getJobByTemplateId(templateId);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            quartzManager.pauseJob(jobName, jobGroupName);
            String cron = qualityDao.getCronByTemplateId(templateId);

            if(Objects.isNull(cron) || StringUtils.isEmpty(cron)) {
                qualityDao.deleteTemplate2QrtzByTemplateId(templateId);
            }
            qualityDao.updateFinishedPercent(templateId, 0F);
            //设置模板状态为【暂停】
            qualityDao.updateTemplateStatus(TemplateStatus.SUSPENDING.code, templateId);
        } catch (Exception e) {
            LOG.error("关闭模板失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭模板失败");
        }
    }

    public File exportExcel(List<String> reportIds) throws AtlasBaseException {
        Map<String,Workbook> workbookMap = new HashMap();
        String tableSheetName = "TableRuleSheet";
        String columnSheetName = "ColumnRuleSheet";
        List<String> tableAttributes = getTableAttributesHeader();
        List<List<String>> tableData;
        List<String> columnAttributes = getColumnAttributesHeader();
        List<List<String>> columnData;
        ExcelReport excelReport = null;
        try {
            for (String reportId : reportIds) {
                tableData = new ArrayList<>();
                columnData = new ArrayList<>();
                String reportName = qualityDao.getReportName(reportId);
                List<Report.ReportRule> reportRules = qualityDao.getReportRuleList(reportId);
                List<String> resultList = null;
                for (Report.ReportRule rule : reportRules) {
                    resultList = new ArrayList<>();
                    //字段名和类型
                    if (rule.getRuleType() == 1) {
                        String ruleColumnName = rule.getRuleColumnName();
                        resultList.add(ruleColumnName);
                        String ruleColumnType = rule.getRuleColumnType();
                        resultList.add(ruleColumnType);
                        columnData.add(resultList);
                    } else if (rule.getRuleType() == 0) {
                        tableData.add(resultList);
                    }
                    String ruleId = rule.getRuleId();
                    //规则名称
                    String ruleName = rule.getRuleName();
                    resultList.add(ruleName);
                    //规则类型
                    String ruleType = RuleType.getDescByCode(rule.getRuleType());
                    resultList.add(ruleType);
                    //规则说明
                    String ruleInfo = rule.getRuleInfo();
                    resultList.add(ruleInfo);
                    //校验方式
                    String ruleCheckType = RuleCheckType.getDescByCode(rule.getRuleCheckType());
                    resultList.add(ruleCheckType);
                    //校验表达式
                    String ruleExpression = CheckExpression.getDescByCode(rule.getRuleCheckExpression());
                    resultList.add(ruleExpression);
                    //阈值大小
                    List<Double> thresholds = qualityDao.queryReportThresholdByRuleId(ruleId);
                    String thresholdUnit = rule.getRuleCheckThresholdUnit();
                    List<String> thresholdAndUnit = thresholds.stream().map(value -> value + thresholdUnit).collect(Collectors.toList());
                    String thresholdStr = StringUtils.join(thresholdAndUnit.toArray(), " ");
                    resultList.add(thresholdStr);
                    //结果值
                    Double ruleValue = rule.getReportRuleValue();
                    resultList.add(ruleValue.toString());
                    //状态
                    String ruleStatusDesc = RuleStatus.getDescByCode(rule.getReportRuleStatus());
                    resultList.add(ruleStatusDesc);
                }
                excelReport = new ExcelReport();
                excelReport.setTableSheetName(tableSheetName);
                excelReport.setColumnSheetName(columnSheetName);
                excelReport.setTableAttributes(tableAttributes);
                excelReport.setTableData(tableData);
                excelReport.setColumnAttributes(columnAttributes);
                excelReport.setColumnData(columnData);
                Workbook wb = PoiExcelUtils.createExcelFile(excelReport, "xlsx");
                workbookMap.put(reportName, wb);
            }
            List<File> files = convertBookToFile(workbookMap);
            File zipFile = getZipFile(files);
            return zipFile;
        } catch (Exception e) {
            LOG.error("导出Excel失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导出Excel失败");
        }
    }



    public List<File> convertBookToFile(Map<String,Workbook> workbooks) throws IOException {
        List<File> files = new ArrayList<>();
        for(String key : workbooks.keySet()) {
            File file = new File(key + ".xlsx");
            FileOutputStream output = new FileOutputStream(file);
            Workbook workbook = workbooks.get(key);
            workbook.write(output);
            output.flush();
            output.close();
            files.add(file);
        }
        return files;
    }

    public File getZipFile(List<File> inputs) throws IOException  {
        File zipFile = new File("report.zip");
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
        for (int i = 0; i < inputs.size(); i++) {
            File file = inputs.get(i);
            zout.putNextEntry(new ZipEntry(file.getName()));
            zout.write(FileUtils.readFileToByteArray(file));
            zout.closeEntry();
            file.delete();
        }
        zout.close();
        return zipFile;
    }

    String[] tableAttributeArr = {"规则名称", "规则类型", "规则说明", "阈值类型","校验表达式", "阈值大小", "结果值", "结果状态"};

    public List<String> getTableAttributesHeader() {
        List<String> tableAttributeList = new ArrayList<>();
        for(int i=0; i<tableAttributeArr.length; i++) {
            tableAttributeList.add(tableAttributeArr[i]);
        }
        return tableAttributeList;
    }

    String[] columnAttributes = {"字段名", "字段类型", "规则名称", "规则类型", "规则说明", "阈值类型","校验表达式", "阈值大小", "结果值", "结果状态"};
    public List<String> getColumnAttributesHeader() {
        List<String> columnAttributeList = new ArrayList<>();
        for(int i=0; i<columnAttributes.length; i++) {
            columnAttributeList.add(columnAttributes[i]);
        }
        return columnAttributeList;
    }

    @Cacheable(value = "downloadCache", key = "#downloadId")
    public List<String> getDownloadList(List<String> downLoadList, String downloadId) {
        if(Objects.nonNull(downLoadList))
            return downLoadList;
        return new ArrayList<>();
    }

    public int updateAlertStatus(String reportId, int status) throws AtlasBaseException {
        try {
            return qualityDao.updateAlertStatus(reportId, status);
        } catch (Exception e) {
            LOG.error("更新模板状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新模板状态失败");
        }
    }

    public Float getFinishedPercent(String templateId) throws AtlasBaseException {
        try {
            return qualityDao.getFinishedPercent(templateId);
        } catch (Exception e) {
            LOG.error("获取完成度失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取完成度失败");
        }
    }

    public Integer getTemplateStatus(String templateId) throws AtlasBaseException {
        try {
            return qualityDao.getTemplateStatus(templateId);
        } catch (Exception e) {
            LOG.error("获取模板状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取模板状态失败");
        }
    }

    public Template getTemplate(String templateId) throws AtlasBaseException {
        try {
            return qualityDao.getTemplate(templateId);
        } catch (Exception e) {
            LOG.error("获取模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取模板失败");
        }
    }

    public List<TemplateResult> getTemplates(String tableId,String tenantId) throws SQLException {
        List<TemplateResult> templateResults = qualityDao.getTemplateResults(tableId,tenantId);
        return templateResults;
    }

    public PageResult<ReportResult> getReports(String tableGuid, String templateId, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            PageResult<ReportResult> pageResult = new PageResult<>();
            Integer offset = parameters.getOffset();
            Integer limit = parameters.getLimit();
            List<ReportResult> reports = null;
            long total = 0;
            String all = "-1";
            if (all.equals(templateId)) {
                reports = qualityDao.getReportsByTableGuid(tableGuid, offset, limit,tenantId);
                if (reports.size()!=0){
                    total = reports.get(0).getTotal();
                }
            } else {
                reports = qualityDao.getReports(tableGuid, templateId, offset, limit);
                if (reports.size()!=0){
                    total = reports.get(0).getTotal();
                }
            }
            pageResult.setLists(reports);
            pageResult.setCurrentSize(reports.size());
            pageResult.setTotalSize(total);
            return pageResult;
        } catch (Exception e) {
            LOG.info("获取报告失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告失败");
        }
    }

    public Report getReport(String reportId) throws SQLException, AtlasBaseException {
        try {
            List<Report> reports = qualityDao.getReport(reportId);
            if (reports.size() == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该报表不存在");
            }
            Report report = reports.get(0);
            List<Report.ReportRule> reportRule = qualityDao.getReportRule(reportId);
            for (Report.ReportRule rule : reportRule) {
                String ruleResultId = rule.getRuleId();
                List<Double> reportThresholdValue = qualityDao.getReportThresholdValue(ruleResultId);
                rule.setRuleCheckThreshold(reportThresholdValue);
            }
            report.setRules(reportRule);

            return report;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.info("获取报告失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告失败");
        }
    }

    public TableColumnRules getRules(String tableId, int buildType) throws SQLException, AtlasBaseException {
        try {
            TableColumnRules tableColumnRules = new TableColumnRules();
            List<TableColumnRules.ColumnsRule> columnsRules = new ArrayList<>();
            List<TableColumnRules.SystemRule> tableSystemRules = qualityDao.getTableSystemRules(RuleType.TABLE.code, buildType);
            addCheckRules(tableSystemRules);
            ColumnQuery columnQuery = new ColumnQuery();
            columnQuery.setGuid(tableId);
            List<Column> columns = metadataService.getColumnInfoById(columnQuery, true);
            Table tableInfoById = metadataService.getTableInfoById(tableId,null);
            tableColumnRules.setSource(tableInfoById.getDatabaseName() + "." + tableInfoById.getTableName());
            for (Column column : columns) {
                TableColumnRules.ColumnsRule columnsRule = new TableColumnRules.ColumnsRule();
                String columnName = column.getColumnName();
                String type = column.getType();
                columnsRule.setRuleColumnName(columnName);
                columnsRule.setRuleColumnType(type);
                DataType datatype = getDatatype(type);
                List<TableColumnRules.SystemRule> columnSystemRules = qualityDao.getColumnSystemRules(RuleType.COLUMN.code, datatype.getCode(), buildType);
                addCheckRules(columnSystemRules);
                columnsRule.setColumnRules(columnSystemRules);
                columnsRules.add(columnsRule);
            }
            tableColumnRules.setTableRules(tableSystemRules);
            tableColumnRules.setColumnsRules(columnsRules);

            return tableColumnRules;
        } catch (Exception e) {
            LOG.info("获取规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则失败");
        }
    }

    private void addCheckRules(List<TableColumnRules.SystemRule> tableSystemRules) throws AtlasBaseException {
        try {
            for (TableColumnRules.SystemRule tableSystemRule : tableSystemRules) {
                int ruleId = tableSystemRule.getRuleId();
                List<Integer> checktypes = qualityDao.getChecktypes(ruleId);
                tableSystemRule.setRuleAllowCheckType(checktypes);
            }
        } catch (Exception e) {
            LOG.info("添加校验规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加校验规则失败");
        }
    }

    private DataType getDatatype(String type) {
        ArrayList<String> numeric = new ArrayList<>();
        numeric.add("tinyint");
        numeric.add("smallint");
        numeric.add("int");
        numeric.add("integer");
        numeric.add("bigint");
        numeric.add("float");
        numeric.add("double");
        numeric.add("double precision");
        numeric.add("decimal");
        numeric.add("numeric");
        if (numeric.contains(type))
            return DataType.NUMERIC;

        return DataType.UNNUMERIC;
    }

    public ReportError getLastReportError(String templateId) throws AtlasBaseException {
        try {
            Float percent = qualityDao.getFinishedPercent(templateId);
            ReportError error = null;
            error = qualityDao.getLastError(templateId);
            if(Objects.isNull(error)) {
                error = new ReportError();
                error.setTemplateId(templateId);
            }
            error.setPercent(percent);
            Integer status = getTemplateStatus(templateId);
            if(Objects.nonNull(status)) {
                error.setStatus(status);
            }
            return error;
        } catch (Exception e) {
            LOG.error("获取错误信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取错误信息失败");
        }
    }
}
