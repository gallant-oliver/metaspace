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
 * @date 2019/7/2 20:38
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.SQLException;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/2 20:38
 */
public interface ColumnDAO {


    @Insert({"<script>",
            "insert into column_info(column_guid,column_name,table_guid,type,display_name,display_operator,display_updatetime,status,description)values",
            "<foreach collection='columnList' item='columnInfo' index='index'  separator=','>",
            "(#{columnInfo.columnId},#{columnInfo.columnName},#{columnInfo.tableId},#{columnInfo.type},#{columnInfo.displayName},#{columnInfo.displayNameOperator},#{columnInfo.displayNameUpdateTime},#{columnInfo.status},#{columnInfo.description})",
            "</foreach>",
            "</script>"})
    public int addColumnDisplayInfo(@Param("columnList")List<Column> columnList);

    @Select("select count(*) from column_info where table_guid=#{tableGuid} and status='ACTIVE'")
    public int tableColumnExist(@Param("tableGuid")String tableGuid);



    @Update({" <script>",
             " update column_info set display_name=#{columnInfo.displayName},display_operator=#{columnInfo.displayNameOperator},display_updatetime=#{columnInfo.displayNameUpdateTime}",
             " where ",
             " column_guid=#{columnInfo.columnId}",
             " </script>"})
    public int updateColumnInfo(@Param("columnInfo")Column info);


    @Select("select column_guid as columnId,column_name as columnName,type from column_info where table_guid=#{tableGuid} and status='ACTIVE'")
    public List<Column> getColumnInfoList(@Param("tableGuid")String tableGuid);

    @Select("select column_guid from column_info where table_guid=#{tableGuid} and column_name = #{columnName} and status='ACTIVE'")
    String getColumnGuid(@Param("tableGuid")String tableGuid, @Param("columnName")String columnName);

    @Update("update column_info set status = 'DELETED' where column_guid=#{tableGuid}")
    String deleteColumn(@Param("columnGuid")String columnGuid);

    @Select("select column_name as columnName, display_name as displayName from column_info where table_guid=#{tableGuid} and status='ACTIVE' order by column_name")
    public List<Column> getColumnNameWithDisplayList(@Param("tableGuid")String tableGuid);

    @Update("update tableInfo set display_name=#{displayName},display_operator=#{displayOperator}, display_updatetime=#{displayUpdateTime} where tableGuid=#{tableGuid}")
    public int updateTableDisplay(@Param("tableGuid")String tableGuid, @Param("displayName")String displayName, @Param("displayOperator")String displayOperator, @Param("displayUpdateTime")String displayUpdateTime);

    @Select({" <script>",
             " select count(*)over() total,column_guid as columnId, column_name as columnName, display_name as displayName, display_updatetime as displayNameUpdateTime, type",
             " from column_info",
             " where table_guid=#{tableGuid}",
             " and (column_name like '%${queryText}%' ESCAPE '/' or display_name like '%${queryText}%' ESCAPE '/')",
             " and status !='DELETED' ",
             " <if test='columnTypes!= null'>",
             " and type in ",
             " <foreach item='type' index='index' collection='columnTypes' separator=',' open='(' close=')'>" +
             " #{type}" +
             " </foreach>" +
             " </if>",
             " <if test='sortColumn!=null'>",
             " order by ${sortColumn}",
             "</if>",
             " <if test='sortColumn!=null and sortOrder!=null'>",
             " ${sortOrder}",
             " </if>",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<Column> getTableColumnList(@Param("tableGuid")String tableGuid, @Param("queryText")String queryText, @Param("sortColumn")String sortColumn,
                                           @Param("sortOrder")String sortOrder, @Param("limit")int limit, @Param("offset")int offset,@Param("columnTypes") List<String> columnTypes);



    @Select("select column_guid as columnId, column_name as columnName, display_name as displayName, display_updatetime as displayNameUpdateTime from column_info where column_guid=#{columnId}")
    public Column getColumnInfoByGuid(@Param("columnId")String columnId);

    @Select("select display_name from tableInfo where tableGuid=#{tableGuid}")
    public String getTableDisplayInfoByGuid(@Param("tableGuid")String tableGuid);

    @Select("select tableName,dbName as databaseName from tableInfo where tableGuid=#{tableGuid}")
    public TableHeader getTableHeaderInfo(@Param("tableGuid")String tableGuid);

    @Update("update column_info set status=#{status} where column_guid=#{columnId}")
    public int updateColumnStatus(@Param("columnId") String columnId, @Param("status") String status);

    @Update("update column_info set status=#{status} where column_guid in (#{columnIds})")
    public int updateColumnStatusBatch(@Param("columnIds") String columnIds, @Param("status") String status);

    @Update("update column_info set status=#{status} where table_guid in (#{tableGuids})")
    public int updateColumnStatusByTableGuids(@Param("tableGuids") String tableGuids, @Param("status") String status);

    @Update("update column_info set column_name=#{columnName},type=#{type},status=#{status},description=#{description} where column_guid=#{columnId}")
    public int updateColumnBasicInfo(@Param("columnId")String columnId, @Param("columnName")String columnName, @Param("type")String type, @Param("status")String status, @Param("description")String description);

    @Select("<script>" +
            "select column_guid columnId,column_name columnName,type,table_guid tableId from column_info where (description is null or description='') and table_guid in " +
            " <foreach item='table' index='index' collection='tables' separator=',' open='(' close=')'>" +
            " #{table.tableId}" +
            " </foreach>" +
            "</script>")
    public List<Column> checkDescriptionColumnByTableIds(@Param("tables")List<Table> tables);
}
