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
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
            int tableRuleNum = 0;
            int columnRuleNum = 0;
            List<UserRule> userRules = template.getRules();
            for(UserRule rule : userRules) {
                rule.setTemplateId(templateId);
                if(rule.getRuleType() == 0)
                    tableRuleNum++;
                else if(rule.getRuleType() == 1)
                    columnRuleNum++;
                String ruleId = UUID.randomUUID().toString();
                rule.setRuleId(ruleId);
                qualityDao.insertUserRule(rule);

                List<Double> thresholds = rule.getRuleCheckThreshold();
                for(Double threshold : thresholds) {
                    String thresholdId = UUID.randomUUID().toString();
                    qualityDao.insertThreshold(thresholdId, threshold, ruleId);
                }
            }
            template.setTableRuleNum(tableRuleNum);
            template.setColumnRuleNum(columnRuleNum);
            qualityDao.insertTemplate(template);
            qualityDao.updateTemplateStatus(0);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }

    @Transactional
    public void deleteTemplate(String templateId) throws AtlasBaseException {
        try {
            qualityDao.delTemplate(templateId);
            qualityDao.delRuleRelatedTemplate(templateId);
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
            qualityDao.updateTemplate(template);
            List<UserRule> userRules = template.getRules();
            for(UserRule rule: userRules) {
                rule.setTemplateId(templateId);
                qualityDao.updateUserRule(rule);
            }
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

    public HSSFWorkbook exportExcel(List<String> reportIds) {
        String tableSheetName = "TableRuleSheet";
        String columnSheetName = "ColumnRuleSheet";
        List<String> tableAttributes = new ArrayList<>();
        List<List<String>> tableData = new ArrayList<>();
        List<String> columnAttributes = new ArrayList<>();
        List<List<String>> columnData = new ArrayList<>();
        ExcelReport excelReport = null;
        for(String reportId : reportIds) {
            List<Report> reports = qualityDao.getReport(reportId);
            for(Report report : reports) {

                List<Report.ReportRule> rules = report.getRules();
                for(Report.ReportRule rule : rules) {
                    String ruleName = rule.getRuleName();
                    String ruleInfo = rule.getRuleInfo();
                    String ruleCheckType = CheckExpression.getDescByCode(rule.getRuleCheckType());
                    String ruleExpression = CheckExpression.getDescByCode(rule.getRuleCheckExpression());
                }

                excelReport = new ExcelReport();
                excelReport.setTableSheetName(tableSheetName);

                excelReport.setColumnSheetName(columnSheetName);
                excelReport.setTableAttributes(tableAttributes);
                excelReport.setTableData(tableData);
                excelReport.setColumnAttributes(columnAttributes);
                excelReport.setColumnData(columnData);

                PoiExcelUtils.createExcelFile(excelReport, "");
            }
        }

        HSSFWorkbook wb = null;
                //PoiExcelUtils.createExcelFile();

        return wb;
    }
}
