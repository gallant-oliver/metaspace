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
package io.zeta.metaspace.web.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import io.zeta.metaspace.model.datastandard.CategoryAndDataStandard;
import io.zeta.metaspace.model.datastandard.DataStandAndRule;
import io.zeta.metaspace.model.datastandard.DataStandAndTable;
import io.zeta.metaspace.model.datastandard.DataStandToRule;
import io.zeta.metaspace.model.datastandard.DataStandToTable;
import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.datastandard.DataStandardHead;
import io.zeta.metaspace.model.datastandard.DataStandardQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.DataStandardDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class DataStandardService {

    private static final Logger LOG = LoggerFactory.getLogger(DataStandardService.class);

    @Autowired
    DataStandardDAO dataStandardDAO;
    @Autowired
    DataManageService dataManageService;
    @Autowired
    TableDAO tableDAO;

    public int insert(DataStandard dataStandard) throws AtlasBaseException {
        String regexp = "^[A-Z0-9]+$";
        if(!dataStandard.getNumber().matches(regexp)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编号内容格式错误，请输入大写英文字母或数字");
        }
        dataStandard.setId(UUIDUtils.alphaUUID());
        dataStandard.setCreateTime(DateUtils.currentTimestamp());
        dataStandard.setUpdateTime(DateUtils.currentTimestamp());
        dataStandard.setOperator(AdminUtils.getUserData().getUserId());
        dataStandard.setVersion(1);
        dataStandard.setDelete(false);
        return dataStandardDAO.insert(dataStandard);
    }

    public void batchInsert(String categoryId, List<DataStandard> dataList) throws AtlasBaseException {
        int startIndex = 0;
        int endIndex = 0;
        List<DataStandard> subList = null;
        for (DataStandard dataStandard : dataList) {
            dataStandard.setId(UUIDUtils.alphaUUID());
            dataStandard.setCreateTime(DateUtils.currentTimestamp());
            dataStandard.setUpdateTime(DateUtils.currentTimestamp());
            dataStandard.setOperator(AdminUtils.getUserData().getUserId());
            dataStandard.setVersion(1);
            dataStandard.setCategoryId(categoryId);
            dataStandard.setDelete(false);
            endIndex++;
            if(endIndex%500==0) {
                subList = dataList.subList(startIndex, endIndex);
                dataStandardDAO.batchInsert(subList);
                startIndex = endIndex;
            }
        }
        subList = dataList.subList(startIndex, endIndex);
        dataStandardDAO.batchInsert(subList);
    }

    public DataStandard getById(String id) throws AtlasBaseException {
        DataStandard dataStandard = dataStandardDAO.getById(id);
        String path = CategoryRelationUtils.getPath(dataStandard.getCategoryId());
        dataStandard.setPath(path);
        return dataStandard;
    }

    public List<DataStandard> getByNumber(String number) throws AtlasBaseException {
        try {
            return dataStandardDAO.getByNumber(number);
        } catch (Exception e) {
            LOG.error("获取标准失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取标准失败");
        }
    }
    @Transactional
    public void deleteByNumber(String number) throws AtlasBaseException {
        try {
            dataStandardDAO.deleteStandard2RuleByRuleId(number);
            dataStandardDAO.deleteStandard2TableByNumber(number);
            dataStandardDAO.deleteByNumber(number);
        } catch (Exception e) {
            LOG.error("删除失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    public void deleteByNumberList(List<String> numberList) throws AtlasBaseException {
        try {
            dataStandardDAO.deleteByNumberList(numberList);
        } catch (Exception e) {
            LOG.error("批量删除失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "批量删除失败");
        }
    }

    /**
     * 更新也是插入, 版本号+1, 创建时间用最早版本的
     */
    public int update(DataStandard dataStandard) throws AtlasBaseException {
        DataStandard old = getById(dataStandard.getId());
        dataStandard.setId(UUIDUtils.uuid());
        dataStandard.setNumber(old.getNumber());
        dataStandard.setCreateTime(old.getCreateTime());
        dataStandard.setUpdateTime(DateUtils.currentTimestamp());
        dataStandard.setOperator(AdminUtils.getUserData().getUserId());
        dataStandard.setCategoryId(old.getCategoryId());
        dataStandard.setDelete(false);
        return dataStandardDAO.update(dataStandard);
    }

    public PageResult<DataStandard> queryPageByCatetoryId(String categoryId, Parameters parameters) throws AtlasBaseException {
        List<DataStandard> list = queryByCatetoryId(categoryId, parameters);
        PageResult<DataStandard> pageResult = new PageResult<>();
        long totalSize = 0;
        if (list.size()!=0){
            totalSize = list.get(0).getTotal();
        }
        pageResult.setTotalSize(totalSize);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public List<DataStandard> queryByCatetoryId(String categoryId, Parameters parameters) throws AtlasBaseException {
        String path = CategoryRelationUtils.getPath(categoryId);
        List<DataStandard> list = dataStandardDAO.queryByCatetoryId(categoryId, parameters)
                .stream()
                .map(dataStandard -> {
                    dataStandard.setPath(path);
                    return dataStandard;
                }).collect(Collectors.toList());
        return list;
    }

    public PageResult<DataStandard> search(DataStandardQuery parameters) {
        List<DataStandard> list = dataStandardDAO.search(parameters)
                .stream()
                .map(dataStandard -> {
                    String path = null;
                    try {
                        path = CategoryRelationUtils.getPath(dataStandard.getCategoryId());
                    } catch (AtlasBaseException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    dataStandard.setPath(path);
                    return dataStandard;
                }).collect(Collectors.toList());

        PageResult<DataStandard> pageResult = new PageResult<>();
        long totalSize = 0;
        if (list.size()!=0){
            totalSize = list.get(0).getTotal();
        }
        pageResult.setTotalSize(totalSize);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public PageResult<DataStandard> history(String number, Parameters parameters) {
        List<DataStandard> list = dataStandardDAO.history(number, parameters.getLimit(), parameters.getOffset(), parameters.getQuery());
        PageResult<DataStandard> pageResult = new PageResult<>();
        long totalSize = 0;
        if (list.size()!=0){
            totalSize = list.get(0).getTotal();
        }
        pageResult.setTotalSize(totalSize);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public List<DataStandard> queryByIds(List<String> ids) {
        List<DataStandard> list = dataStandardDAO.queryByIds(ids)
                .stream()
                .map(dataStandard -> {
                    String path = null;
                    try {
                        path = CategoryRelationUtils.getPath(dataStandard.getCategoryId());
                    } catch (AtlasBaseException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    dataStandard.setPath(path);
                    return dataStandard;
                }).collect(Collectors.toList());

        return list;
    }

    public List<DataStandard> queryByNumberList(List<String> numberList) {
        List<DataStandard> list = dataStandardDAO.queryByNumberList(numberList)
                .stream()
                .map(dataStandard -> {
                    String path = null;
                    try {
                        path = CategoryRelationUtils.getPath(dataStandard.getCategoryId());
                    } catch (AtlasBaseException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    dataStandard.setPath(path);
                    return dataStandard;
                }).collect(Collectors.toList());

        return list;
    }

    public File exportExcel(String categoryId) throws Exception {
        List<DataStandard> data = queryByCatetoryId(categoryId, null);
        Workbook workbook = data2workbook(data);
        return workbook2file(workbook);
    }

    public File exportExcel(List<String> ids) throws IOException {
        List<DataStandard> data = queryByIds(ids);
        Workbook workbook = data2workbook(data);
        return workbook2file(workbook);
    }

    private Workbook data2workbook(List<DataStandard> list) {
        List<List<String>> dataList = list.stream().map(standard -> {
            String createTime = DateUtils.formatDateTime(standard.getCreateTime().getTime());
            String updateTime = DateUtils.formatDateTime(standard.getUpdateTime().getTime());
            List<String> data = Lists.newArrayList(standard.getNumber(), standard.getContent(), standard.getDescription(), standard.getPath(), standard.getOperator(), createTime, updateTime);
            return data;
        }).collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        PoiExcelUtils.createSheet(workbook, "数据标准", Lists.newArrayList("标准编号", "标准内容", "标准描述", "标准路径", "标准编辑人", "标准创建时间", "标准修改时间"), dataList);
        return workbook;
    }

    private File workbook2file(Workbook workbook) throws IOException {
        File tmpFile = File.createTempFile("DataStandardExport", ".xlsx");
        try (FileOutputStream output = new FileOutputStream(tmpFile)) {
            workbook.write(output);
            output.flush();
            output.close();
        }
        return tmpFile;
    }

    public void importDataStandard(String categoryId, File fileInputStream) throws Exception {
        List<DataStandard> dataList = file2Data(fileInputStream);
        if(dataList.isEmpty()){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "上传数据为空");
        }
        List<String> numberList = dataList.stream().map(DataStandard::getNumber).collect(Collectors.toList());
        List<String> existDataStandard = queryByNumberList(numberList).stream().map(DataStandard::getNumber).collect(Collectors.toList());
        if (!existDataStandard.isEmpty()) {
            List<String> showList = existDataStandard.subList(0, Math.min(existDataStandard.size(), 5));
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "标准编号为: " + Joiner.on("、").join(showList) + "已存在，请修改后上传。");
        }
        batchInsert(categoryId, dataList);
    }

    private List<DataStandard> file2Data(File file) throws Exception {
        try {
            List<DataStandard> dataList = new ArrayList<>();
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = sheet.getLastRowNum() + 1;
            String regexp = "^[A-Z0-9]+$";
            for (int i = 1; i < rowNum; i++) {
                Row row = sheet.getRow(i);
                DataStandard data = new DataStandard();
                Cell numberCell = row.getCell(0);
                if(Objects.isNull(numberCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "标准编号不能为空");
                }
                data.setNumber(numberCell.getStringCellValue());

                if(!data.getNumber().matches(regexp)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编号内容格式错误，请输入大写英文字母或数字");
                }
                Cell contentCell = row.getCell(1);
                if(Objects.isNull(contentCell)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "标准内容不能为空");
                }
                data.setContent(contentCell.getStringCellValue());

                Cell discriptionCell = row.getCell(2);
                if(Objects.isNull(discriptionCell)) {
                    data.setDescription("");
                } else {
                    data.setDescription(discriptionCell.getStringCellValue());
                }
                dataList.add(data);
            }
            return dataList;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public List<CategoryPrivilege> getCategory(Integer categoryType) throws AtlasBaseException {
        List<CategoryPrivilege> result = dataManageService.getAll(categoryType);
        for (CategoryPrivilege category : result) {
            String parentGuid = category.getParentCategoryGuid();
            CategoryPrivilege.Privilege privilege = null;
            if(parentGuid == null) {
                privilege = new CategoryPrivilege.Privilege(false, false, false, true, true, false, true, true, false,false);
            } else {
                privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
            }
            category.setPrivilege(privilege);
        }
        return result;
    }

    public CategoryPrivilege addCategory(CategoryInfoV2 categoryInfo) throws Exception {
        return dataManageService.createCategory(categoryInfo, categoryInfo.getCategoryType());
    }

    public void deleteCategory(String categoryGuid) throws AtlasBaseException {
        try {
            if(dataStandardDAO.countByByCatetoryId(categoryGuid) > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前下还存在标准，请清空标准后，再删除目录");
            }
            dataManageService.deleteCategory(categoryGuid);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public void updateCategory(CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        dataManageService.updateCategory(categoryInfo, categoryInfo.getCategoryType());
    }

    @Transactional
    public void assignTableToStandard(DataStandAndTable dataStandAndTable,String tableName) throws AtlasBaseException {
        try {
            dataStandAndTable.setOperator(AdminUtils.getUserData().getUserId());
            dataStandAndTable.setCreateTime(DateUtils.currentTimestamp());
            dataStandardDAO.deleteByTableId(dataStandAndTable.getTableGuid());
            for (String number : dataStandAndTable.getNumbers()){
                String content = dataStandardDAO.getContentByNumber(number);
                if (content==null){
                    LOG.error("数据标准不存在或已删除");
                    continue;
                }
                try{
                    dataStandardDAO.assignTableToStandard(number,dataStandAndTable);
                }catch (Exception e) {
                    LOG.error("表"+ tableName +" 依赖标准 " + content + " 失败,错误信息:" + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("依赖标准更新失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional
    public void assignRuleToStandard(DataStandAndRule dataStandAndRule,String ruleName) throws AtlasBaseException {
        try {
            dataStandAndRule.setOperator(AdminUtils.getUserData().getUserId());
            dataStandAndRule.setCreateTime(DateUtils.currentTimestamp());
            dataStandardDAO.deleteByRuleId(dataStandAndRule.getRuleId());
            for (String number : dataStandAndRule.getNumbers()){
                String content = dataStandardDAO.getContentByNumber(number);
                if (content==null){
                    LOG.error("数据标准不存在或已删除");
                    continue;
                }
                try{
                    dataStandardDAO.assignRuleToStandard(number,dataStandAndRule);
                }catch (Exception e) {
                    LOG.error("质量规则"+ ruleName +" 依赖标准 " + content + " 失败,错误信息:" + e.getMessage(), e);
                }

            }
        } catch (Exception e) {
            LOG.error("依赖标准更新失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public List<DataStandardHead> getDataStandardByTable(String tableGuid) throws AtlasBaseException {
        try {
            List<DataStandardHead> dataStandards = dataStandardDAO.getDataStandardByTableGuid(tableGuid);
            dataStandards = dataStandards.stream().map(dataStandard -> {
                String[] pathIds = null;
                try {
                    pathIds = CategoryRelationUtils.getPathIds(dataStandard.getCategoryId());
                } catch (AtlasBaseException e) {
                    LOG.error(e.getMessage(), e);
                }
                dataStandard.setPathIds(pathIds);
                return dataStandard;
            }).collect(Collectors.toList());
            return dataStandards;
        } catch (Exception e) {
            LOG.error("获取表的依赖数据标准失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public List<DataStandardHead> getDataStandardByRule(String ruleId) throws AtlasBaseException {
        try {
            List<DataStandardHead> dataStandards = dataStandardDAO.getDataStandardByRuleId(ruleId);
            dataStandards = dataStandards.stream().map(dataStandard -> {
                String[] pathIds = null;
                try {
                    pathIds = CategoryRelationUtils.getPathIds(dataStandard.getCategoryId());
                } catch (AtlasBaseException e) {
                    LOG.error(e.getMessage(), e);
                }
                dataStandard.setPathIds(pathIds);
                return dataStandard;
            }).collect(Collectors.toList());
            return dataStandards;
        } catch (Exception e) {
            LOG.error("获取规则的依赖数据标准失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public PageResult<DataStandToTable> getTableByNumber(String number,Parameters parameters) throws AtlasBaseException {
        try {
            List<DataStandToTable> list = dataStandardDAO.getTableByNumber(number,parameters);
            PageResult<DataStandToTable> pageResult = new PageResult<>();
            long totalSize = 0;
            if (list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;

        } catch (Exception e) {
            LOG.error("获取元数据关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public PageResult<DataStandToRule> getRuleByNumber(String number,Parameters parameters) throws AtlasBaseException {
        try {
            List<DataStandToRule> list = dataStandardDAO.getRuleByNumber(number,parameters);
            PageResult<DataStandToRule> pageResult = new PageResult<>();
            long totalSize = 0;
            if (list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取数据质量关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public List<CategoryAndDataStandard> getCategoryAndStandard() throws AtlasBaseException {
        try {
            List<CategoryAndDataStandard> categoryAndDataStandards = new ArrayList<>();
            List<CategoryPrivilege> categoryPrivileges = getCategory(3);
            for (CategoryPrivilege categoryPrivilege:categoryPrivileges){
                CategoryAndDataStandard categoryAndDataStandard = new CategoryAndDataStandard(categoryPrivilege);
                List<DataStandardHead> dataStandardHeads = dataStandardDAO.getStandardByCategoyrId(categoryAndDataStandard.getGuid());
                if (dataStandardHeads!=null && dataStandardHeads.size()!=0){
                    try {
                        String[] pathIds = CategoryRelationUtils.getPathIds(categoryAndDataStandard.getGuid());
                        dataStandardHeads = dataStandardHeads.stream().map(dataStandard -> {
                            dataStandard.setPathIds(pathIds);
                            return dataStandard;
                        }).collect(Collectors.toList());
                    } catch (AtlasBaseException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                if (dataStandardHeads!=null){
                    categoryAndDataStandard.setDataStandards(dataStandardHeads);
                }
                categoryAndDataStandards.add(categoryAndDataStandard);
            }
            return categoryAndDataStandards;
        } catch (Exception e) {
            LOG.error("获取所有目录和数据标准失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

}
