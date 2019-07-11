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

import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.datastandard.DataStandardQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.DataStandardDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.exception.AtlasBaseException;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DataStandardService {

    private static final Logger LOG = LoggerFactory.getLogger(DataStandardService.class);

    @Autowired
    DataStandardDAO dataStandardDAO;

    public int insert(DataStandard dataStandard) throws AtlasBaseException {
        dataStandard.setId(UUIDUtils.alphaUUID());
        dataStandard.setCreateTime(DateUtils.currentTimestamp());
        dataStandard.setUpdateTime(DateUtils.currentTimestamp());
        dataStandard.setOperator(AdminUtils.getUserData().getUserId());
        dataStandard.setVersion(1);
        dataStandard.setDelete(false);
        return dataStandardDAO.insert(dataStandard);
    }

    public void batchInsert(String categoryId, List<DataStandard> dataList) throws AtlasBaseException {
        for (DataStandard dataStandard : dataList) {
            dataStandard.setId(UUIDUtils.alphaUUID());
            dataStandard.setCreateTime(DateUtils.currentTimestamp());
            dataStandard.setUpdateTime(DateUtils.currentTimestamp());
            dataStandard.setOperator(AdminUtils.getUserData().getUserId());
            dataStandard.setVersion(1);
            dataStandard.setCategoryId(categoryId);
            dataStandard.setDelete(false);
        }
        dataStandardDAO.batchInsert(dataList);
    }

    public DataStandard getById(String id) throws AtlasBaseException {
        DataStandard dataStandard = dataStandardDAO.getById(id);
        String path = CategoryRelationUtils.getPath(dataStandard.getCategoryId());
        dataStandard.setPath(path);
        return dataStandardDAO.getById(id);
    }

    public List<DataStandard> getByNumber(String number) throws AtlasBaseException {
        return dataStandardDAO.getByNumber(number);
    }

    public void deleteByNumber(String number) throws AtlasBaseException {
        dataStandardDAO.deleteByNumber(number);
    }

    public void deleteByNumberList(List<String> numberList) throws AtlasBaseException {
        dataStandardDAO.deleteByNumberList(numberList);
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
        dataStandard.setVersion(old.getVersion() + 1);
        dataStandard.setCategoryId(old.getCategoryId());
        dataStandard.setDelete(false);
        return dataStandardDAO.insert(dataStandard);
    }

    public PageResult<DataStandard> queryPageByCatetoryId(String categoryId, Parameters parameters) throws AtlasBaseException {
        List<DataStandard> list = queryByCatetoryId(categoryId, parameters);
        PageResult<DataStandard> pageResult = new PageResult<>();
        long sum = dataStandardDAO.countByByCatetoryId(categoryId);
        pageResult.setOffset(parameters.getOffset());
        pageResult.setSum(sum);
        pageResult.setCount(list.size());
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
        long sum = dataStandardDAO.countBySearch(parameters.getQuery(), parameters.getCategoryId());
        pageResult.setOffset(parameters.getOffset());
        pageResult.setSum(sum);
        pageResult.setCount(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public PageResult<DataStandard> history(String number, Parameters parameters) {
        List<DataStandard> list = dataStandardDAO.history(number, parameters);
        PageResult<DataStandard> pageResult = new PageResult<>();
        long sum = dataStandardDAO.countByHistory(parameters.getQuery());
        pageResult.setOffset(parameters.getOffset());
        pageResult.setSum(sum);
        pageResult.setCount(list.size());
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

    public void importDataStandard(String categoryId, InputStream fileInputStream) throws Exception {
        List<DataStandard> dataList = file2Data(fileInputStream);
        if(dataList.isEmpty()){
            throw new AtlasBaseException("没有数据。");
        }
        List<String> numberList = dataList.stream().map(DataStandard::getNumber).collect(Collectors.toList());
        List<String> existDataStandard = queryByNumberList(numberList).stream().map(DataStandard::getNumber).collect(Collectors.toList());
        if (!existDataStandard.isEmpty()) {
            throw new AtlasBaseException("标准编号为: " + Joiner.on("、").join(existDataStandard) + "已存在，请修改后上传。");
        }
        batchInsert(categoryId, dataList);
    }

    private List<DataStandard> file2Data(InputStream fileInputStream) throws Exception {
        List<DataStandard> dataList = new ArrayList<>();
        Workbook workbook = new WorkbookFactory().create(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum() + 1;
        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            DataStandard data = new DataStandard();
            data.setNumber(row.getCell(0).getStringCellValue());
            data.setContent(row.getCell(1).getStringCellValue());
            data.setDescription(row.getCell(2).getStringCellValue());
            dataList.add(data);
        }
        return dataList;
    }

    public static String filename(String filePath) throws UnsupportedEncodingException {
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        filename = URLEncoder.encode(filename, "UTF-8");
        return filename;
    }


}
