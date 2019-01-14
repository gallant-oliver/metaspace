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
import io.zeta.metaspace.model.dataquality.ExcelReport;
import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.RuleType;
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.model.dataquality.RuleStatus;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DataQualityService {

    @Autowired
    DataQualityDAO qualityDao;

    @Transactional
    public void addTemplate(Template template) throws AtlasBaseException {
        try {
            String templateId = UUID.randomUUID().toString();
            template.setTemplateId(templateId);
            addRulesByTemlpateId(template);
            //template
            qualityDao.insertTemplate(template);
            qualityDao.updateTemplateStatus(0, templateId);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }

    @Transactional
    public void deleteTemplate(String templateId) throws AtlasBaseException {
        try {
            //template
            qualityDao.delTemplate(templateId);
            deleteRulesByTemplateId(templateId);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

    public void addRulesByTemlpateId(Template template) throws AtlasBaseException {
        String templateId = template.getTemplateId();
        List<UserRule> userRules = template.getRules();
        int tableRuleNum = 0;
        int columnRuleNum = 0;
        try {
            for (UserRule rule : userRules) {
                if(rule.getRuleType() == 0)
                    tableRuleNum++;
                else if(rule.getRuleType() == 1)
                    columnRuleNum++;
                rule.setTemplateId(templateId);
                String ruleId = UUID.randomUUID().toString();
                rule.setRuleId(ruleId);
                qualityDao.insertUserRule(rule);
                //threshold
                List<Double> thresholds = rule.getRuleCheckThreshold();
                for (Double threshold : thresholds) {
                    qualityDao.insertThreshold(threshold, ruleId);
                }
            }
            template.setTableRulesNum(tableRuleNum);
            template.setColumnRulesNum(columnRuleNum);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

    @Transactional
    public void updateTemplate(String templateId, Template template) throws AtlasBaseException {
        try {
            template.setTemplateId(templateId);
            deleteRulesByTemplateId(templateId);
            addRulesByTemlpateId(template);
            qualityDao.updateTemplate(template);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

    @Transactional
    public Template viewTemplate(String templateId) throws AtlasBaseException {
        try {
            Template template = qualityDao.queryTemplateById(templateId);
            List<UserRule> userRules = qualityDao.queryUserRuleById(templateId);
            for(UserRule rule : userRules) {
                String ruleId = rule.getRuleId();
                List<Double> thresholds = qualityDao.queryThresholdByRuleId(ruleId);
                rule.setRuleCheckThreshold(thresholds);
            }
            if(Objects.nonNull(userRules) && userRules.size()>0)
                template.setRules(userRules);
            return template;
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

    public void updateTemplateStatus(int templateStatus) throws AtlasBaseException {
        //启动模板
        if(templateStatus == 0) {

        }
        //停止模板
        else if(templateStatus == 1) {

        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的模板状态");
        }
    }

    public List<Workbook> exportExcel(List<String> reportIds) throws SQLException {
        List<Workbook> workbooks = new ArrayList<>();
        String tableSheetName = "TableRuleSheet";
        String columnSheetName = "ColumnRuleSheet";
        List<String> tableAttributes = getTableAttributesHeader();
        List<List<String>> tableData = new ArrayList<>();
        List<String> columnAttributes = getColumnAttributesHeader();
        List<List<String>> columnData = new ArrayList<>();
        ExcelReport excelReport = null;
        for(String reportId : reportIds) {
            List<Report.ReportRule> reportRules = qualityDao.getReport(reportId);
            List<String> resultList = null;
            for(Report.ReportRule rule : reportRules) {
                resultList = new ArrayList<>();
                String ruleId = rule.getRuleResultId();
                //规则名称
                String ruleName = rule.getRuleName();
                resultList.add(ruleName);
                //规则类型
                String ruleType = RuleType.getDescByCode(rule.getRuleType());
                resultList.add(ruleType);
                //规则说明
                String ruleInfo = rule.getRuleInfo();
                resultList.add(ruleInfo);
                //字段名和类型
                if(rule.getRuleType() == 1) {
                    String ruleColumnName = rule.getRuleColumnName();
                    resultList.add(ruleColumnName);
                    String ruleColumnType = rule.getRuleColumnType();
                    resultList.add(ruleColumnType);
                    columnData.add(resultList);
                } else if(rule.getRuleType() == 0) {
                    tableData.add(resultList);
                }
                //校验方式
                String ruleCheckType = CheckExpression.getDescByCode(rule.getRuleCheckType());
                resultList.add(ruleCheckType);
                //阈值大小
                List<Double> thresholds = qualityDao.queryThresholdByRuleId(ruleId);
                String thresholdUnit = rule.getRuleCheckThresholdUnit();
                String thresholdStr = StringUtils.join(thresholds.toArray(), thresholdUnit);
                resultList.add(thresholdStr);
                //校验表达式
                String ruleExpression = CheckExpression.getDescByCode(rule.getRuleCheckExpression());
                resultList.add(ruleExpression);
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
            workbooks.add(wb);
        }
        return workbooks;
    }

    String[] tableAttributeArr = {"规则名称", "规则类型", "规则说明", "阈值类型","校验表达式", "阈值大小", "结果值", "结果状态"};

    public List<String> getTableAttributesHeader() {
        List<String> tableAttributeList = new ArrayList<>();
        for(int i=0; i<tableAttributeArr.length; i++) {
            tableAttributeList.add(tableAttributeArr[i]);
        }
        return tableAttributeList;
    }

    String[] columnAttributes = {"规则名称", "规则类型", "规则说明", "字段名", "字段类型", "阈值类型","校验表达式", "阈值大小", "结果值", "结果状态"};
    public List<String> getColumnAttributesHeader() {
        List<String> columnAttributeList = new ArrayList<>();
        for(int i=0; i<columnAttributes.length; i++) {
            columnAttributeList.add(columnAttributes[i]);
        }
        return columnAttributeList;
    }
}
