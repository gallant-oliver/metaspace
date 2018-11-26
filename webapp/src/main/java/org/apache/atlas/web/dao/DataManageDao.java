/*
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
*/
/**
 * @author sunhaoning@gridsum.com
 * @date 2018/11/19 11:49
 *//*

package org.apache.atlas.web.dao;

import com.google.gson.Gson;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntity;
import org.apache.atlas.repository.tablestat.TableStatService;
import org.apache.atlas.repository.util.HbaseUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

*/
/*
 * @description
 * @author sunhaoning
 * @date 2018/11/19 11:49
 *//*

public class DataManageDao {

    private static final Logger LOG = LoggerFactory.getLogger(TableStatService.class);
    private static final String CATEGORY_TABLE = "table_category";

    public void add(CategoryEntity category) throws Exception {
        Gson gson = new Gson();
        try (Table table = HbaseUtils.getConn().getTable(TableName.valueOf(CATEGORY_TABLE))) {
            try {
                Put put = new Put(Bytes.toBytes(category.getGuid()));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("guid"), Bytes.toBytes(category.getGuid()));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(category.getName()));
                if(Objects.nonNull(category.getDescription()))
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("description"), Bytes.toBytes(category.getDescription()));
                if(Objects.nonNull(category.getUpBrothCategoryGuid()))
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("upBrothCategoryGuid"), Bytes.toBytes(category.getUpBrothCategoryGuid()));
                if(Objects.nonNull(category.getDownBrothCategoryGuid()))
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("downBrothCategoryGuid"), Bytes.toBytes(category.getDownBrothCategoryGuid()));
                if(Objects.nonNull(category.getParentCategoryGuid()))
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("parentCategoryGuid"), Bytes.toBytes(category.getParentCategoryGuid()));
                if(Objects.nonNull(category.getChildrenCategories()))
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("childrenCategoriesGuid"), Bytes.toBytes(category.getChildrenCategories().toArray().toString()));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("qualifiedName"), Bytes.toBytes(category.getQualifiedName()));
                table.put(put);
            } catch (Exception e) {

            }
        }
    }

    public void update(String guid, String columnName, String value) throws Exception {
        try (Table table = HbaseUtils.getConn().getTable(TableName.valueOf(CATEGORY_TABLE))) {
            try {
                Put put = new Put(Bytes.toBytes(guid));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes(columnName), Bytes.toBytes(value));
                table.put(put);
            } catch (Exception e) {

            }
        }
    }


    public Map<String, Object> query(String guid) throws Exception {
        try (Connection conn = HbaseUtils.getConn()) {
            Table table = conn.getTable(TableName.valueOf(CATEGORY_TABLE));
            Get get = new Get(Bytes.toBytes(guid));
            Result rs = table.get(get);
            if(rs.isEmpty())
                return null;
            return resultToMap(rs);
        }
    }

    public String query(String guid, String columnName) throws Exception {
        try (Connection conn = HbaseUtils.getConn()) {
            Table table = conn.getTable(TableName.valueOf(CATEGORY_TABLE));
            Get get = new Get(guid.getBytes());
            get.addColumn("info".getBytes(), columnName.getBytes());
            Result rs = table.get(get);
            if(rs.containsColumn("info".getBytes(), columnName.getBytes())) {
                return Bytes.toString(rs.getValue("info".getBytes(),columnName.getBytes()));
            } else {
                return null;
            }
        }
    }

    public void deleteByRowKey(String guid) throws Exception {
        try (Connection conn = HbaseUtils.getConn()) {
            Table table = conn.getTable(TableName.valueOf(CATEGORY_TABLE));
            Delete delete = new Delete(Bytes.toBytes(guid));
            table.delete(delete);
        }
    }

    public static Map<String, Object> resultToMap(Result result) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Cell> listCell = result.listCells();
        Map<String, Object> tempMap = new HashMap<String, Object>();
        String rowname = "";
        List<String> familynamelist = new ArrayList<String>();
        for (Cell cell : listCell) {
            byte[] rowArray = cell.getRowArray();
            byte[] familyArray = cell.getFamilyArray();
            byte[] qualifierArray = cell.getQualifierArray();
            byte[] valueArray = cell.getValueArray();
            int rowoffset = cell.getRowOffset();
            int familyoffset = cell.getFamilyOffset();
            int qualifieroffset = cell.getQualifierOffset();
            int valueoffset = cell.getValueOffset();
            int rowlength = cell.getRowLength();
            int familylength = cell.getFamilyLength();
            int qualifierlength = cell.getQualifierLength();
            int valuelength = cell.getValueLength();

            byte[] temprowarray = new byte[rowlength];
            System.arraycopy(rowArray, rowoffset, temprowarray, 0, rowlength);
            String temprow = Bytes.toString(temprowarray);
            //            System.out.println(Bytes.toString(temprowarray));

            byte[] tempqulifierarray = new byte[qualifierlength];
            System.arraycopy(qualifierArray, qualifieroffset, tempqulifierarray, 0, qualifierlength);
            String tempqulifier = Bytes.toString(tempqulifierarray);
            //            System.out.println(Bytes.toString(tempqulifierarray));

            byte[] tempfamilyarray = new byte[familylength];
            System.arraycopy(familyArray, familyoffset, tempfamilyarray, 0, familylength);
            String tempfamily = Bytes.toString(tempfamilyarray);
            //            System.out.println(Bytes.toString(tempfamilyarray));

            byte[] tempvaluearray = new byte[valuelength];
            System.arraycopy(valueArray, valueoffset, tempvaluearray, 0, valuelength);
            String tempvalue = Bytes.toString(tempvaluearray);
            //            System.out.println(Bytes.toString(tempvaluearray));


            tempMap.put(tempfamily + ":" + tempqulifier, tempvalue);
            //            long t= cell.getTimestamp();
            //            tempMap.put("timestamp",t);
            rowname = temprow;
            String familyname = tempfamily;
            if (familynamelist.indexOf(familyname) < 0) {
                familynamelist.add(familyname);
            }
        }
        resMap.put("rowname", rowname);
        for (String familyname : familynamelist) {
            HashMap<String, Object> tempFilterMap = new HashMap<String, Object>();
            for (String key : tempMap.keySet()) {
                String[] keyArray = key.split(":");
                if (keyArray[0].equals(familyname)) {
                    tempFilterMap.put(keyArray[1], tempMap.get(key));
                }
            }
            resMap.put(familyname, tempFilterMap);
        }
        return resMap;
    }
}
*/
