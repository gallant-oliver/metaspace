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
import io.zeta.metaspace.model.datastandard.*;
import io.zeta.metaspace.model.enums.DataStandardDataType;
import io.zeta.metaspace.model.enums.DataStandardLevel;
import io.zeta.metaspace.model.enums.DataStandardType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.DataStandardDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ObjectUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.util.ExportDataPathUtils.EXCEL_FORMAT_XLSX;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataStandardService {
    
    private static final Logger LOG = LoggerFactory.getLogger(DataStandardService.class);
    
    private static final List<String> EXPORT_FILE_TITLES =
            Lists.newArrayList("标准编号", "标准名称", "标准类型", "数据类型", "数据长度",
                    "是否有允许值", "允许值", "标准层级", "标准描述", "标准路径", "标准编辑人", "标准创建时间", "标准修改时间");
    
    /**
     * 编码校验规则:只允许英文、数字、下划线、中划线
     */
    private static final String NUMBER_REGEX = "^[0-9a-zA-Z_-]{1,200}$";
    /**
     * 名称校验规则:只允许中文、英文、数字、下划线、中划线
     */
    private static final String NAME_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{1,200}$";
    /**
     * 数据长度校验规则: 正整数
     */
    private static final String DATA_LENGTH_REGEX = "^[1-9]\\d*$";
    
    @Autowired
    private DataStandardDAO dataStandardDAO;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private CategoryDAO categoryDAO;
    
    public int insert(DataStandard dataStandard, String tenantId) throws AtlasBaseException {
        verifyDataStandard(dataStandard);
        
        dataStandard.setId(UUID.randomUUID().toString());
        dataStandard.setCreateTime(DateUtils.currentTimestamp());
        dataStandard.setUpdateTime(DateUtils.currentTimestamp());
        dataStandard.setOperator(AdminUtils.getUserData().getUserId());
        
        dataStandard.setDelete(false);
        return dataStandardDAO.insert(dataStandard, tenantId);
    }
    
    
    private void verifyDataStandard(DataStandard dataStandard) {
        Objects.requireNonNull(dataStandard.getNumber(), "数据标准编号必填!");
        Assert.isTrue(Pattern.matches(NUMBER_REGEX, dataStandard.getNumber()),
                "编号内容格式错误，只允许英文、数字、下划线、中划线");
    
        Objects.requireNonNull(dataStandard.getName(), "数据标准名称必填!");
        Assert.isTrue(Pattern.matches(NAME_REGEX, dataStandard.getName()),
                "名称内容格式错误，只允许中文、英文、数字、下划线、中划线");
    
        Objects.requireNonNull(dataStandard.getStandardType(), "数据标准类型必填!");
        DataStandardType.parseByCode(dataStandard.getStandardType());
    
        ObjectUtils.isTrueThen(dataStandard.getDataLength(), Objects::nonNull,
                v -> Assert.isTrue(Pattern.matches(DATA_LENGTH_REGEX, dataStandard.getDataLength().toString()),
                        "数据长度格式错误，只允许正整数"));
    
        ObjectUtils.isTrueThen(dataStandard.getDataType(), Objects::nonNull, DataStandardDataType::parseByCode);
    
        ObjectUtils.isTrueThen(dataStandard.getStandardLevel(), Objects::nonNull, DataStandardLevel::parseByCode);
    
        ObjectUtils.isTrueThenElseThen(dataStandard.isAllowableValueFlag(), v -> v,
                v -> Assert.isTrue(StringUtils.isNotEmpty(dataStandard.getAllowableValue()),
                        "允许有值时,允许值不能为空!"),
                v -> Assert.isTrue(StringUtils.isEmpty(dataStandard.getAllowableValue()),
                        "不允许有值时,允许值只能为空!"));
    }
    
    private void batchInsert(String categoryId, List<DataStandard> dataList, String tenantId) throws AtlasBaseException {
        int startIndex = 0;
        int endIndex = 0;
        List<DataStandard> subList;
        for (DataStandard dataStandard : dataList) {
            dataStandard.setId(UUID.randomUUID().toString());
            dataStandard.setCreateTime(DateUtils.currentTimestamp());
            dataStandard.setUpdateTime(DateUtils.currentTimestamp());
            dataStandard.setOperator(AdminUtils.getUserData().getUserId());
            dataStandard.setVersion(0);
            dataStandard.setCategoryId(categoryId);
            dataStandard.setDelete(false);
            endIndex++;
            if (endIndex % 500 == 0) {
                subList = dataList.subList(startIndex, endIndex);
                dataStandardDAO.batchInsert(subList, tenantId);
                startIndex = endIndex;
            }
        }
        subList = dataList.subList(startIndex, endIndex);
        dataStandardDAO.batchInsert(subList, tenantId);
    }
    
    public DataStandard getById(String id, String tenantId) throws AtlasBaseException {
        DataStandard dataStandard = dataStandardDAO.getById(id);
        String path = CategoryRelationUtils.getPath(dataStandard.getCategoryId(), tenantId);
        dataStandard.setPath(path);
        return dataStandard;
    }
    
    public Long getByNumber(String number, String tenantId) throws AtlasBaseException {
        return dataStandardDAO.getCountByNumber(number, tenantId);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteByNumber(String number, String tenantId) throws AtlasBaseException {
        try {
            dataStandardDAO.deleteStandard2RuleByRuleId(number, tenantId);
            List<String> database = tenantService.getDatabase(tenantId);
            if (database != null && database.size() != 0) {
                dataStandardDAO.deleteStandard2TableByNumber(number, database);
            }
            
            dataStandardDAO.deleteByNumber(number, tenantId);
        } catch (Exception e) {
            LOG.error("删除失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    public void deleteByNumberList(List<String> numberList,String tenantId) throws AtlasBaseException {
        try {
            dataStandardDAO.deleteByNumberList(numberList,tenantId);
        } catch (Exception e) {
            LOG.error("批量删除失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "批量删除失败");
        }
    }
    
    /**
     * 更新也是插入, 版本号+1, 创建时间用最早版本的
     * 2021-11-15 fix: 版本号=0的是当前版本,历史版本为非零正整数
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(DataStandard dataStandard, String tenantId) throws AtlasBaseException {
        verifyDataStandard(dataStandard);
        
        DataStandard old = dataStandardDAO.getById(dataStandard.getId());
        Assert.notNull(old, "无效的数据标准ID");
        Assert.isTrue(old.getVersion() == 0, String.format("历史数据无法编辑,id:%s", old.getId()));
        Assert.isTrue(Objects.equals(old.getNumber(), dataStandard.getNumber()), "数据标准编码不能修改!");
        
        // 查询最大历史版本号
        int maxHistoryVersion = dataStandardDAO.getMaxHistoryVersion(dataStandard.getNumber());
        
        dataStandard.setId(UUID.randomUUID().toString());
        dataStandard.setNumber(old.getNumber());
        dataStandard.setCategoryId(old.getCategoryId());
        dataStandard.setCreateTime(old.getCreateTime());
        dataStandard.setUpdateTime(DateUtils.currentTimestamp());
        dataStandard.setOperator(AdminUtils.getUserData().getUserId());
        dataStandard.setDelete(false);
        
        // 更新当前版本数据的版本号 version=maxHistoryVersion+1;
        dataStandardDAO.updateVersion(old.getId(), (maxHistoryVersion + 1));
        // 插入当前版本数据
        dataStandardDAO.insert(dataStandard, tenantId);
    }
    
    public PageResult<DataStandard> queryPageByCatetoryId(String categoryId, Parameters parameters, String tenantId) throws AtlasBaseException {
        List<DataStandard> list = queryByCatetoryId(categoryId, parameters, tenantId);
        
        PageResult<DataStandard> pageResult = new PageResult<>();
        pageResult.setTotalSize(CollectionUtils.isNotEmpty(list) ? list.get(0).getTotal() : 0);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }
    
    public List<DataStandard> queryByCatetoryId(String categoryId, Parameters parameters, String tenantId) throws AtlasBaseException {
        String path = CategoryRelationUtils.getPath(categoryId, tenantId);
        return dataStandardDAO.queryByCatetoryId(categoryId, parameters, tenantId)
                .stream()
                .peek(dataStandard -> dataStandard.setPath(path))
                .collect(Collectors.toList());
    }
    
    public PageResult<DataStandard> search(DataStandardQuery parameters, String tenantId) {
        List<DataStandard> list = dataStandardDAO.search(parameters, tenantId)
                .stream()
                .peek(dataStandard ->
                        // TODO 循环查询,待优化
                        dataStandard.setPath(CategoryRelationUtils.getPath(dataStandard.getCategoryId(), tenantId)))
                .collect(Collectors.toList());
        
        return getDataStandardPageResult(list);
    }
    
    public PageResult<DataStandard> history(String number, Parameters parameters, String tenantId) {
        List<DataStandard> list = dataStandardDAO.queryHistoryData(number, parameters, tenantId);
        
        if (CollectionUtils.isNotEmpty(list)) {
            // 处理version=0的数据,set version = maxHistoryVersion+1
            int maxHistoryVersion = list.stream().mapToInt(DataStandard::getVersion).max().orElse(-1);
            list = list.stream()
                    .peek(value -> {
                        if (value.getVersion() == 0) {
                            value.setVersion(maxHistoryVersion + 1);
                        }
                    })
                    .collect(Collectors.toList());
        }
        
        return getDataStandardPageResult(list);
    }
    
    private PageResult<DataStandard> getDataStandardPageResult(List<DataStandard> list) {
        PageResult<DataStandard> pageResult = new PageResult<>();
        pageResult.setTotalSize(list.size() > 0 ? list.get(0).getTotal() : 0);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }
    
    /**
     * 导出指定目录下所有数据标准
     */
    public File exportExcel(String categoryId, String tenantId) throws Exception {
        List<DataStandard> data = queryByCatetoryId(categoryId, null, tenantId);
        Workbook workbook = data2workbook(data);
        return workbook2file(workbook);
    }
    
    /**
     * 导出指定数据标准
     */
    public File exportExcel(List<String> ids, String tenantId) throws IOException {
        List<DataStandard> data = queryByIds(ids, tenantId);
        Workbook workbook = data2workbook(data);
        return workbook2file(workbook);
    }
    
    private File workbook2file(Workbook workbook) throws IOException {
        File tmpFile = File.createTempFile(String.format("DataStandardExport_%s", System.currentTimeMillis()), EXCEL_FORMAT_XLSX);
        try (FileOutputStream output = new FileOutputStream(tmpFile)) {
            workbook.write(output);
            output.flush();
        }
        return tmpFile;
    }
    
    private List<DataStandard> queryByIds(List<String> ids, String tenantId) {
        return dataStandardDAO.queryByIds(ids)
                .stream()
                .peek(dataStandard -> {
                    String path = null;
                    try {
                        // TODO 循环查询 待优化
                        path = CategoryRelationUtils.getPath(dataStandard.getCategoryId(), tenantId);
                    } catch (AtlasBaseException e) {
                        LOG.error("导出所选数据标准异常:", e);
                    }
                    dataStandard.setPath(path);
                }).collect(Collectors.toList());
    }
    
    private Workbook data2workbook(List<DataStandard> list) {
        List<List<String>> dataList = list.stream()
                .map(value -> {
                    String standardType = Objects.isNull(value.getStandardType())
                            ? StringUtils.EMPTY
                            : DataStandardType.parseByCode(value.getStandardType()).getDesc();
                    String standardLevel = Objects.isNull(value.getStandardLevel())
                            ? StringUtils.EMPTY
                            : DataStandardLevel.parseByCode(value.getStandardLevel()).getDesc();
                    String createTime = DateUtils.formatDateTime(value.getCreateTime().getTime());
                    String updateTime = DateUtils.formatDateTime(value.getUpdateTime().getTime());
                    return Lists.newArrayList(
                            value.getNumber(),
                            value.getName(),
                            standardType,
                            value.getDataType(),
                            Objects.toString(value.getDataLength(), StringUtils.EMPTY),
                            String.valueOf(value.isAllowableValueFlag()),
                            value.getAllowableValue(),
                            standardLevel,
                            value.getDescription(),
                            value.getPath(),
                            value.getOperator(),
                            createTime,
                            updateTime
                    );
                }).collect(Collectors.toList());
    
        Workbook workbook = new XSSFWorkbook();
        PoiExcelUtils.createSheet(workbook, "数据标准", EXPORT_FILE_TITLES, dataList);
        return workbook;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void importDataStandard(String categoryId, File fileInputStream, String tenantId)
            throws IOException, InvalidFormatException {
        List<DataStandard> dataList = file2Data(fileInputStream);
        if (dataList.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "上传数据为空");
        }
        
        // 文件内唯一性校验
        Set<String> numberSet = dataList.stream()
                .collect(Collectors.toMap(DataStandard::getNumber, DataStandard::getNumber
                        , (o, n) -> {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,
                                    String.format("标准编号为:%s在文件中重复，请修改后上传。", o));
                        })).keySet();
        Set<String> nameSet = dataList.stream()
                .collect(Collectors.toMap(DataStandard::getName, DataStandard::getName
                        , (o, n) -> {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,
                                    String.format("标准名称为:%s在文件中重复，请修改后上传。", o));
                        })).keySet();
        
        // 数据库唯一性校验
        if (CollectionUtils.isNotEmpty(numberSet)) {
            List<String> existList = dataStandardDAO.queryNumberByNumbers(numberSet, tenantId);
            if (CollectionUtils.isNotEmpty(existList)) {
                List<String> showList = existList.subList(0, Math.min(existList.size(), 5));
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,
                        String.format("标准编号为:%s已存在，请修改后上传。", Joiner.on("、").join(showList)));
            }
        }
        
        if (CollectionUtils.isNotEmpty(nameSet)) {
            List<String> existList = dataStandardDAO.queryNameByNumbers(nameSet, tenantId);
            if (CollectionUtils.isNotEmpty(existList)) {
                List<String> showList = existList.subList(0, Math.min(existList.size(), 5));
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,
                        String.format("标准名称为:%s已存在，请修改后上传。", Joiner.on("、").join(showList)));
            }
        }
        
        batchInsert(categoryId, dataList, tenantId);
    }
    
    private List<DataStandard> file2Data(File file) throws IOException, InvalidFormatException {
        List<DataStandard> dataList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum() + 1;
        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            DataStandard standard = new DataStandard();
            
            // 解析顺序不能修改
            int j = 0;
            Cell numberCell = row.getCell(j++);
            Cell nameCell = row.getCell(j++);
            Cell typeCell = row.getCell(j++);
            Cell dataTypeCell = row.getCell(j++);
            Cell dateLengthCell = row.getCell(j++);
            Cell isAllowValueFlagCell = row.getCell(j++);
            Cell allowValueCell = row.getCell(j++);
            Cell levelCell = row.getCell(j++);
            Cell discriptionCell = row.getCell(j);
    
            ObjectUtils.isTrueThenElseException(numberCell, Objects::nonNull, "标准编号不能为空",
                    v -> standard.setNumber(v.getStringCellValue()));
            ObjectUtils.isTrueThenElseException(nameCell, Objects::nonNull, "标准名称不能为空",
                    v -> standard.setName(v.getStringCellValue()));
            ObjectUtils.isTrueThenElseException(typeCell, Objects::nonNull, "标准类型不能为空",
                    v -> standard.setStandardType(DataStandardType.parseByDesc(typeCell.getStringCellValue()).getCode()));
            ObjectUtils.isTrueThen(dataTypeCell, Objects::nonNull,
                    v -> standard.setDataType(v.getStringCellValue()));
            ObjectUtils.isTrueThenElseException(dateLengthCell, v -> v.getNumericCellValue() <= Integer.MAX_VALUE,
                    "标准数据长度最大值为2147483647",
                    v -> standard.setDataLength((int) v.getNumericCellValue()));
            ObjectUtils.isTrueThenElseException(isAllowValueFlagCell, Objects::nonNull, "是否有允许值不能为空",
                    v -> standard.setAllowableValueFlag(v.getBooleanCellValue()));
            ObjectUtils.isTrueThen(allowValueCell, Objects::nonNull, v -> standard.setAllowableValue(v.getStringCellValue()));
            ObjectUtils.isTrueThen(levelCell, Objects::nonNull,
                    v -> standard.setStandardLevel(DataStandardLevel.parseByDesc(v.getStringCellValue()).getCode()));
            ObjectUtils.isTrueThen(discriptionCell, Objects::nonNull, v -> standard.setDescription(v.getStringCellValue()));
    
            // 校验数据
            verifyDataStandard(standard);
    
            dataList.add(standard);
        }
        return dataList;
    }
    
    public List<CategoryPrivilege> getCategory(Integer categoryType, String tenantId) throws AtlasBaseException {
        List<CategoryPrivilege> result = dataManageService.getAllByUserGroup(categoryType, tenantId);
        for (CategoryPrivilege category : result) {
            String parentGuid = category.getParentCategoryGuid();
            CategoryPrivilege.Privilege privilege = null;
            String parentPattern = "^Standard-([0-9])+$";
            if (parentGuid == null) {
                privilege = new CategoryPrivilege.Privilege(false, false, false, true, true, true, true, true, true, false);
            } else {
                privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
            }
            if (category.getGuid().matches(parentPattern)) {
                privilege.setEdit(false);
                privilege.setDelete(false);
            }
            category.setPrivilege(privilege);
        }
        return result;
    }

    public CategoryPrivilege addCategory(CategoryInfoV2 categoryInfo,String tenantId) throws Exception {
        return dataManageService.createCategory(categoryInfo, categoryInfo.getCategoryType(),tenantId);
    }

    public void deleteCategory(String categoryGuid,String tenantId) throws AtlasBaseException {
        try {
            if (dataStandardDAO.countByByCategoryId(categoryGuid, tenantId) > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前下还存在标准，请清空标准后，再删除目录");
            }
            int childrenNum = categoryDAO.queryChildrenNum(categoryGuid,tenantId);
            if (childrenNum > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录下存在子目录");
            }
            dataManageService.deleteCategory(categoryGuid,tenantId,3);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public void updateCategory(CategoryInfoV2 categoryInfo,String tenantId) throws AtlasBaseException {
        dataManageService.updateCategory(categoryInfo, categoryInfo.getCategoryType(),tenantId);
    }

    @Transactional(rollbackFor=Exception.class)
    public void assignTableToStandard(DataStandAndTable dataStandAndTable,String tableName,String tenantId) throws AtlasBaseException {
        try {
            dataStandAndTable.setOperator(AdminUtils.getUserData().getUserId());
            dataStandAndTable.setCreateTime(DateUtils.currentTimestamp());
            dataStandardDAO.deleteByTableId(dataStandAndTable.getTableGuid());
            for (String number : dataStandAndTable.getNumbers()){
                if (dataStandardDAO.getCountByNumber(number, tenantId) == 0) {
                    LOG.error("数据标准不存在或已删除");
                    continue;
                }
                try{
                    dataStandardDAO.assignTableToStandard(number,dataStandAndTable);
                }catch (Exception e) {
                    LOG.error("表{}依赖标准{}失败,错误信息:", tableName, number, e);
                }
            }
        } catch (Exception e) {
            LOG.error("依赖标准更新失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void assignRuleToStandard(DataStandAndRule dataStandAndRule,String ruleName,String tenantId) throws AtlasBaseException {
        try {
            dataStandAndRule.setOperator(AdminUtils.getUserData().getUserId());
            dataStandAndRule.setCreateTime(DateUtils.currentTimestamp());
            dataStandardDAO.deleteByRuleId(dataStandAndRule.getRuleId());
            for (String number : dataStandAndRule.getNumbers()){
                if (dataStandardDAO.getCountByNumber(number, tenantId) == 0) {
                    LOG.error("数据标准不存在或已删除");
                    continue;
                }
                try{
                    dataStandardDAO.assignRuleToStandard(number,dataStandAndRule);
                }catch (Exception e) {
                    LOG.error("质量规则 {} 依赖标准 {} 失败,错误信息:", ruleName, number, e);
                }

            }
        } catch (Exception e) {
            LOG.error("依赖标准更新失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public List<DataStandardHead> getDataStandardByTable(String tableGuid,String tenantId) throws AtlasBaseException {
        try {
            List<DataStandardHead> dataStandards = dataStandardDAO.getDataStandardByTableGuid(tableGuid,tenantId);
            dataStandards = dataStandards.stream().map(dataStandard -> {
                String[] pathIds = null;
                try {
                    pathIds = CategoryRelationUtils.getPathIds(dataStandard.getCategoryId(),tenantId);
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

    public List<DataStandardHead> getDataStandardByRule(String ruleId,String tenantId) throws AtlasBaseException {
        try {
            List<DataStandardHead> dataStandards = dataStandardDAO.getDataStandardByRuleId(ruleId,tenantId);
            dataStandards = dataStandards.stream().map(dataStandard -> {
                String[] pathIds = null;
                try {
                    pathIds = CategoryRelationUtils.getPathIds(dataStandard.getCategoryId(),tenantId);
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

    public PageResult<DataStandToTable> getTableByNumber(String number,Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            List<String> databases = tenantService.getDatabase(tenantId);
            List<DataStandToTable> list = new ArrayList<>();
            if (databases!=null&&databases.size()!=0)
                list = dataStandardDAO.getTableByNumber(number,parameters,databases);
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

    public PageResult<DataStandToRule> getRuleByNumber(String number,Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            List<DataStandToRule> list = dataStandardDAO.getRuleByNumber(number,parameters,tenantId);
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

    public List<CategoryAndDataStandard> getCategoryAndStandard(String tenantId) throws AtlasBaseException {
        try {
            List<CategoryAndDataStandard> categoryAndDataStandards = new ArrayList<>();
            List<CategoryPrivilege> categoryPrivileges = getCategory(3,tenantId);
            for (CategoryPrivilege categoryPrivilege:categoryPrivileges){
                CategoryAndDataStandard categoryAndDataStandard = new CategoryAndDataStandard(categoryPrivilege);
                List<DataStandardHead> dataStandardHeads = dataStandardDAO.getStandardByCategoyrId(categoryAndDataStandard.getGuid(),tenantId);
                if (dataStandardHeads!=null && dataStandardHeads.size()!=0){
                    try {
                        String[] pathIds = CategoryRelationUtils.getPathIds(categoryAndDataStandard.getGuid(),tenantId);
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
