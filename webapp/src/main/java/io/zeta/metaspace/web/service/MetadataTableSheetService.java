package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.web.util.CustomStringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
public class MetadataTableSheetService {

    public void createMetadataTableSheet(Workbook workbook, int index, Table table, CellStyle headerStyle, CellStyle cellStyle) {
        String tableName = table.getTableName();
        String dbName = table.getDatabaseName();
        int rowNumber = 0;
        String sheetName = "表" + index + "-表信息";
        Sheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));

        CellRangeAddress tableAndDbNameRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(tableAndDbNameRangeAddress);
        Row tableAndDbNameRow = sheet.createRow(rowNumber++);
        Cell tableAndDbNameRowCell = tableAndDbNameRow.createCell(0);
        tableAndDbNameRowCell.setCellValue(dbName + "." + tableName + "表信息");
        tableAndDbNameRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);

        CellRangeAddress basicInfoRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(basicInfoRangeAddress);
        Row basicInfoRow = sheet.createRow(rowNumber++);
        Cell basicInfoRowCell = basicInfoRow.createCell(0);
        basicInfoRowCell.setCellValue("基础信息");
        basicInfoRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);

        Row tableNameRow = sheet.createRow(rowNumber++);
        Cell tableNameKeyCell = tableNameRow.createCell(0);
        tableNameKeyCell.setCellValue("表名称");
        tableNameKeyCell.setCellStyle(cellStyle);
        Cell tableNameValueCell = tableNameRow.createCell(1);
        tableNameValueCell.setCellValue(tableName);
        tableNameValueCell.setCellStyle(cellStyle);

        StringJoiner tagJoiner = new StringJoiner(",");
        table.getTags().forEach(tag -> tagJoiner.add(tag.getTagName()));
        Row tagRow = sheet.createRow(rowNumber++);
        Cell tagKeyCell = tagRow.createCell(0);
        tagKeyCell.setCellValue("标签");
        tableNameKeyCell.setCellStyle(cellStyle);
        Cell tagValueCell = tagRow.createCell(1);
        tagValueCell.setCellValue(tagJoiner.toString());
        tagValueCell.setCellStyle(cellStyle);

        String creator = table.getOwner();
        Row creatorRow = sheet.createRow(rowNumber++);
        Cell creatorKeyCell = creatorRow.createCell(0);
        creatorKeyCell.setCellStyle(cellStyle);
        creatorKeyCell.setCellValue("创建人");
        Cell creatorValueCell = creatorRow.createCell(1);
        creatorValueCell.setCellValue(creator);
        creatorValueCell.setCellStyle(cellStyle);

        StringJoiner dataOwnerJoiner = new StringJoiner(",");
        table.getDataOwner().forEach(dataOwnerHeader -> dataOwnerJoiner.add(dataOwnerHeader.getName()));
        Row dataOwnerRow = sheet.createRow(rowNumber++);
        Cell dataOwnerKeyCell = dataOwnerRow.createCell(0);
        dataOwnerKeyCell.setCellValue("数据Owner");
        dataOwnerKeyCell.setCellStyle(cellStyle);
        Cell dataOwnerValueCell = dataOwnerRow.createCell(1);
        dataOwnerValueCell.setCellValue(dataOwnerJoiner.toString());
        dataOwnerValueCell.setCellStyle(cellStyle);

        String updateTime = table.getUpdateTime();
        Row updateTimeRow = sheet.createRow(rowNumber++);
        Cell updateTimeKeyCell = updateTimeRow.createCell(0);
        updateTimeKeyCell.setCellValue("更新时间");
        updateTimeKeyCell.setCellStyle(cellStyle);
        Cell updateTimeValueCell = updateTimeRow.createCell(1);
        updateTimeValueCell.setCellValue(updateTime);
        updateTimeValueCell.setCellStyle(cellStyle);

        Row dbNameRow = sheet.createRow(rowNumber++);
        Cell dbNameKeyCell = dbNameRow.createCell(0);
        dbNameKeyCell.setCellValue("所属数据库");
        dbNameKeyCell.setCellStyle(cellStyle);
        Cell dbNameValueCell = dbNameRow.createCell(1);
        dbNameValueCell.setCellValue(dbName);
        dbNameValueCell.setCellStyle(cellStyle);

        String type = table.getType();
        Row typeRow = sheet.createRow(rowNumber++);
        Cell typeKeyCell = typeRow.createCell(0);
        typeKeyCell.setCellValue("类型");
        typeKeyCell.setCellStyle(cellStyle);
        Cell typeValueCell = typeRow.createCell(1);
        typeValueCell.setCellValue("INTERNAL_TABLE".equals(type) ? "内部表" : "外部表");
        typeValueCell.setCellStyle(cellStyle);

        Boolean isPartitionTable = table.getPartitionTable();
        Row isPartitionTableRow = sheet.createRow(rowNumber++);
        Cell isPartitionTableKeyCell = isPartitionTableRow.createCell(0);
        isPartitionTableKeyCell.setCellValue("分区表");
        isPartitionTableKeyCell.setCellStyle(cellStyle);
        Cell isPartitionTableValueCell = isPartitionTableRow.createCell(1);
        isPartitionTableValueCell.setCellValue((true == isPartitionTable) ? "是" : "否");
        isPartitionTableValueCell.setCellStyle(cellStyle);

        String format = table.getFormat();
        Row formatRow = sheet.createRow(rowNumber++);
        Cell formatKeyCell = formatRow.createCell(0);
        formatKeyCell.setCellValue("格式");
        formatKeyCell.setCellStyle(cellStyle);
        Cell formatValueCell = formatRow.createCell(1);
        formatValueCell.setCellValue(format);
        formatValueCell.setCellStyle(cellStyle);

        String location = table.getLocation();
        Row locationRow = sheet.createRow(rowNumber++);
        Cell locationKeyCell = locationRow.createCell(0);
        locationKeyCell.setCellValue("位置");
        locationKeyCell.setCellStyle(cellStyle);
        Cell locationValueCell = locationRow.createCell(1);
        locationValueCell.setCellValue(location);
        locationValueCell.setCellStyle(cellStyle);

        String description = table.getDescription();
        Row descriptionRow = sheet.createRow(rowNumber++);
        Cell descriptionKeyCell = descriptionRow.createCell(0);
        descriptionKeyCell.setCellValue("描述");
        descriptionKeyCell.setCellStyle(cellStyle);
        Cell descriptionValueCell = descriptionRow.createCell(1);
        descriptionValueCell.setCellValue(description);
        descriptionValueCell.setCellStyle(cellStyle);

        CellRangeAddress sourceSystemRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(sourceSystemRangeAddress);
        Row sourceSystemRow = sheet.createRow(rowNumber++);
        Cell sourceSystemRowCell = sourceSystemRow.createCell(0);
        sourceSystemRowCell.setCellValue("源系统维度");
        sourceSystemRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);

        String subordinateSystem = table.getSubordinateSystem();
        Row subordinateSystemRow = sheet.createRow(rowNumber++);
        Cell subordinateSystemKeyCell = subordinateSystemRow.createCell(0);
        subordinateSystemKeyCell.setCellValue("所属系统");
        subordinateSystemKeyCell.setCellStyle(cellStyle);
        Cell subordinateSystemValueCell = subordinateSystemRow.createCell(1);
        subordinateSystemValueCell.setCellValue(subordinateSystem);
        subordinateSystemValueCell.setCellStyle(cellStyle);

        String subordinateDatabase = table.getSubordinateDatabase();
        Row subordinateDatabaseRow = sheet.createRow(rowNumber++);
        Cell subordinateDatabaseKeyCell = subordinateDatabaseRow.createCell(0);
        subordinateDatabaseKeyCell.setCellValue("所属数据库");
        subordinateDatabaseKeyCell.setCellStyle(cellStyle);
        Cell subordinateDatabaseValueCell = subordinateDatabaseRow.createCell(1);
        subordinateDatabaseValueCell.setCellValue(subordinateDatabase);
        subordinateDatabaseValueCell.setCellStyle(cellStyle);

        String systemAdmin = table.getSystemAdmin();
        Row systemAdminRow = sheet.createRow(rowNumber++);
        Cell systemAdminKeyCell = systemAdminRow.createCell(0);
        systemAdminKeyCell.setCellValue("源系统管理员");
        systemAdminKeyCell.setCellStyle(cellStyle);
        Cell systemAdminValueCell = systemAdminRow.createCell(1);
        systemAdminValueCell.setCellValue(systemAdmin);
        systemAdminValueCell.setCellStyle(cellStyle);

        String createTime = table.getCreateTime();
        Row createTimeRow = sheet.createRow(rowNumber++);
        Cell createTimeKeyCell = createTimeRow.createCell(0);
        createTimeKeyCell.setCellValue("表创建时间");
        createTimeKeyCell.setCellStyle(cellStyle);
        Cell createTimeValueCell = createTimeRow.createCell(1);
        createTimeValueCell.setCellValue(createTime);
        createTimeValueCell.setCellStyle(cellStyle);

        CellRangeAddress dataWarehouseRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(dataWarehouseRangeAddress);
        Row dataWarehouseRow = sheet.createRow(rowNumber++);
        Cell dataWarehouseRowCell = dataWarehouseRow.createCell(0);
        dataWarehouseRowCell.setCellValue("数仓维度");
        dataWarehouseRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);

        String dataWarehouseAdmin = table.getDataWarehouseAdmin();
        Row dataWarehouseAdminRow = sheet.createRow(rowNumber++);
        Cell dataWarehouseAdminKeyCell = dataWarehouseAdminRow.createCell(0);
        dataWarehouseAdminKeyCell.setCellValue("数仓管理员");
        dataWarehouseAdminKeyCell.setCellStyle(cellStyle);
        Cell dataWarehouseAdminValueCell = dataWarehouseAdminRow.createCell(1);
        dataWarehouseAdminValueCell.setCellValue(dataWarehouseAdmin);
        dataWarehouseAdminValueCell.setCellStyle(cellStyle);

        String dataWarehouseDescription = table.getDataWarehouseDescription();
        Row dataWarehouseDescriptionRow = sheet.createRow(rowNumber++);
        Cell dataWarehouseDescriptionKeyCell = dataWarehouseDescriptionRow.createCell(0);
        dataWarehouseDescriptionKeyCell.setCellValue("描述");
        dataWarehouseAdminKeyCell.setCellStyle(cellStyle);
        Cell dataWarehouseDescriptionValueCell = dataWarehouseDescriptionRow.createCell(1);
        dataWarehouseDescriptionValueCell.setCellValue(dataWarehouseDescription);
        dataWarehouseDescriptionValueCell.setCellStyle(cellStyle);

        CellRangeAddress catalogRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(catalogRangeAddress);
        Row catalogRangeAddressRow = sheet.createRow(rowNumber++);
        Cell catalogRangeAddressRowCell = catalogRangeAddressRow.createCell(0);
        catalogRangeAddressRowCell.setCellValue("目录维度");
        catalogRangeAddressRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);

        StringJoiner catalogJoiner = new StringJoiner(",");
        table.getRelations().forEach(relation -> catalogJoiner.add(relation));
        Row catalogRow = sheet.createRow(rowNumber++);
        Cell catalogKeyCell = catalogRow.createCell(0);
        catalogKeyCell.setCellValue("所属目录");
        catalogKeyCell.setCellStyle(cellStyle);
        Cell catalogValueCell = catalogRow.createCell(1);
        catalogValueCell.setCellValue(catalogJoiner.toString());
        catalogValueCell.setCellStyle(cellStyle);

        String catalogAdmin = table.getCatalogAdmin();
        Row catalogAdminRow = sheet.createRow(rowNumber++);
        Cell catalogAdminKeyCell = catalogAdminRow.createCell(0);
        catalogAdminKeyCell.setCellValue("目录管理员");
        catalogAdminKeyCell.setCellStyle(cellStyle);
        Cell catalogAdminValueCell = catalogAdminRow.createCell(1);
        catalogAdminValueCell.setCellValue(catalogAdmin);
        catalogAdminValueCell.setCellStyle(cellStyle);

        String relationTime = table.getRelationTime();
        Row relationTimeRow = sheet.createRow(rowNumber++);
        Cell relationTimeKeyCell = relationTimeRow.createCell(0);
        relationTimeKeyCell.setCellValue("数据关联时间");
        relationTimeKeyCell.setCellStyle(cellStyle);
        Cell relationTimeValueCell = relationTimeRow.createCell(1);
        relationTimeValueCell.setCellValue(relationTime);
        relationTimeValueCell.setCellStyle(cellStyle);

        List<Table.BusinessObject> businessObjectList = table.getBusinessObjects();
        for (Table.BusinessObject businessObject : businessObjectList) {
            CellRangeAddress businessRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
            sheet.addMergedRegion(businessRangeAddress);
            Row businessRangeAddressRow = sheet.createRow(rowNumber++);
            Cell businessRangeAddressRowCell = businessRangeAddressRow.createCell(0);
            businessRangeAddressRowCell.setCellValue("业务维度");
            businessRangeAddressRowCell.setCellStyle(headerStyle);

            RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);
            RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);
            RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);
            RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);

            String object = businessObject.getBusinessObject();
            Row objectRow = sheet.createRow(rowNumber++);
            Cell objectKeyCell = objectRow.createCell(0);
            objectKeyCell.setCellValue("对应业务对象");
            objectKeyCell.setCellStyle(cellStyle);
            Cell objectValueCell = objectRow.createCell(1);
            objectValueCell.setCellValue(object);
            objectValueCell.setCellStyle(cellStyle);

            String department = businessObject.getDepartment();
            Row departmentRow = sheet.createRow(rowNumber++);
            Cell departmentKeyCell = departmentRow.createCell(0);
            departmentKeyCell.setCellValue("所属部门");
            departmentKeyCell.setCellStyle(cellStyle);
            Cell departmentValueCell = departmentRow.createCell(1);
            departmentValueCell.setCellValue(department);
            departmentValueCell.setCellStyle(cellStyle);

            String businessLeader = businessObject.getBusinessLeader();
            Row businessLeaderRow = sheet.createRow(rowNumber++);
            Cell businessLeaderKeyCell = businessLeaderRow.createCell(0);
            businessLeaderKeyCell.setCellValue("业务负责人");
            businessLeaderKeyCell.setCellStyle(cellStyle);
            Cell businessLeaderValueCell = businessLeaderRow.createCell(1);
            businessLeaderValueCell.setCellValue(businessLeader);
            businessLeaderValueCell.setCellStyle(cellStyle);
        }


        sheet.autoSizeColumn(0, true);
        sheet.autoSizeColumn(1, true);
    }


    public void createMetadataColumnSheet(Workbook workbook, int index, Table table, CellStyle headerStyle, CellStyle cellStyle) {

        String tableName = table.getTableName();
        String dbName = table.getDatabaseName();
        int rowNumber = 0;
        String sheetName = "表" + index + "-字段信息";
        Sheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));

        List<Column> columnList = table.getColumns();
        List<Column> normalColumnList = columnList.stream().filter(column -> !column.getPartitionKey()).collect(Collectors.toList());
        List<Column> partitionColumnList = columnList.stream().filter(column -> column.getPartitionKey()).collect(Collectors.toList());


        CellRangeAddress tableAndDbNameRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 2);
        sheet.addMergedRegion(tableAndDbNameRangeAddress);
        Row tableAndDbNameRow = sheet.createRow(rowNumber++);
        Cell tableAndDbNameRowCell = tableAndDbNameRow.createCell(0);
        tableAndDbNameRowCell.setCellValue(dbName + "." + tableName + "字段信息");
        tableAndDbNameRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);

        CellRangeAddress normalColumnRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 2);
        sheet.addMergedRegion(normalColumnRangeAddress);
        Row normalColumnRow = sheet.createRow(rowNumber++);
        Cell normalColumnRowCell = normalColumnRow.createCell(0);
        normalColumnRowCell.setCellValue("普通字段");
        normalColumnRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);

        String[] headers = new String[]{"名称", "类型", "描述"};
        Row normalColumnHeaderRow = sheet.createRow(rowNumber++);
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = normalColumnHeaderRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(cellStyle);
        }

        createDataCell(normalColumnList, sheet, rowNumber, cellStyle);
        rowNumber += normalColumnList.size();
        CellRangeAddress partitionColumnRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 2);
        sheet.addMergedRegion(partitionColumnRangeAddress);
        Row partitionColumnRow = sheet.createRow(rowNumber++);
        Cell partitionColumnRowCell = partitionColumnRow.createCell(0);
        partitionColumnRowCell.setCellValue("分区字段");
        partitionColumnRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);

        Row partitionColumnHeaderRow = sheet.createRow(rowNumber++);
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = partitionColumnHeaderRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(cellStyle);
        }

        createDataCell(partitionColumnList, sheet, rowNumber, cellStyle);
    }

    public void createDataCell(List<Column> columnList, Sheet sheet, Integer rowNumber, CellStyle cellStyle) {
        for (int i = 0; i < columnList.size(); i++) {
            Row dataRow = sheet.createRow(rowNumber++);
            Column column = columnList.get(i);
            String columnName = column.getColumnName();
            String type = column.getType();
            String description = column.getDescription();
            Cell columnCell = dataRow.createCell(0);
            columnCell.setCellValue(columnName);
            columnCell.setCellStyle(cellStyle);
            Cell typeCell = dataRow.createCell(1);
            typeCell.setCellValue(type);
            typeCell.setCellStyle(cellStyle);
            Cell descriptionCell = dataRow.createCell(2);
            descriptionCell.setCellValue(description);
            descriptionCell.setCellStyle(cellStyle);
        }
    }
}
