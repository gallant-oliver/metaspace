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

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.collect.HashBiMap;
import io.zeta.metaspace.model.dataquality.CheckExpression;
import io.zeta.metaspace.model.dataquality.ExcelReport;
import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.RuleCheckType;
import io.zeta.metaspace.model.dataquality.RuleType;
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.model.dataquality.RuleStatus;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
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

    public File exportExcel(List<String> reportIds) throws AtlasBaseException {

        Map<String,Workbook> workbookMap = new HashMap();
        String tableSheetName = "TableRuleSheet";
        String columnSheetName = "ColumnRuleSheet";
        List<String> tableAttributes = getTableAttributesHeader();
        List<List<String>> tableData = new ArrayList<>();
        List<String> columnAttributes = getColumnAttributesHeader();
        List<List<String>> columnData = new ArrayList<>();
        ExcelReport excelReport = null;
        try {
            for (String reportId : reportIds) {
                String reportName = qualityDao.getReportName(reportId);
                List<Report.ReportRule> reportRules = qualityDao.getReport(reportId);
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

                    //校验方式
                    String ruleCheckType = RuleCheckType.getDescByCode(rule.getRuleCheckType());
                    resultList.add(ruleCheckType);
                    //校验表达式
                    String ruleExpression = CheckExpression.getDescByCode(rule.getRuleCheckExpression());
                    resultList.add(ruleExpression);
                    //阈值大小
                    List<Double> thresholds = qualityDao.queryThresholdByRuleId(ruleId);
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
                setStyle(wb);
            }
            List<File> files = convertBookToFile(workbookMap);
            File zipFile = getZipFile(files);
            return zipFile;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public void setStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 20); //字体高度
        font.setColor(HSSFFont.COLOR_NORMAL); //字体颜色
        font.setFontName("黑体"); //字体
        font.setBoldweight(Font.BOLDWEIGHT_BOLD); //宽度

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER); //水平布局：居中
        cellStyle.setWrapText(true);

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
        int len;
        byte[] buf = new byte[1024];
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
        for (int i = 0; i < inputs.size(); i++) {
            File file = inputs.get(i);
            FileInputStream in =new FileInputStream(file);
            zout.putNextEntry(new ZipEntry(file.getName()));
            while ((len = in.read(buf)) > 0) {
                zout.write(buf, 0, len);
            }
            zout.closeEntry();
            in.close();
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
}
