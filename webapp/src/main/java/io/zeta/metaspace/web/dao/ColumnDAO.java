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

import io.zeta.metaspace.model.dataassets.ColumnInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

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

    @Insert("insert into column_info(column_guid,column_name,table_guid,type,display_name,display_operator,display_updatetime,status,description,partition_field)values" +
            "(#{column.columnId},#{column.columnName},#{column.tableId},#{column.type},#{column.displayName},#{column.displayNameOperator}," +
            "#{column.displayNameUpdateTime},#{column.status},#{column.description},#{column.isPartitionKey})")
    void addColumn(@Param("column")Column column);

    @Select("select count(*) from column_info where table_guid=#{tableGuid} and status='ACTIVE'")
    public int tableColumnExist(@Param("tableGuid")String tableGuid);



    @Update({" <script>",
             " update column_info set column_name = #{columnInfo.columnName}, display_name=#{columnInfo.displayName},display_operator=#{columnInfo.displayNameOperator},display_updatetime=#{columnInfo.displayNameUpdateTime}," +
             " status = #{columnInfo.status}, type = #{columnInfo.type}, partition_field = #{columnInfo.isPartitionKey}, description = #{columnInfo.description}",
             " where ",
             " column_guid=#{columnInfo.columnId}",
             " </script>"})
    public int updateColumnInfo(@Param("columnInfo")Column info);


    @Select("select column_guid as columnId,column_name as columnName,type from column_info where table_guid=#{tableGuid} and status='ACTIVE'")
    public List<Column> getColumnInfoList(@Param("tableGuid")String tableGuid);

    @Select("select column_guid as columnId,column_name as columnName,type,description from column_info where table_guid=#{tableGuid} and status='ACTIVE' ORDER BY column_name")
    List<Column> getColumnInfoListByTableGuid(@Param("tableGuid")String tableGuid);

    @Select("select column_guid as columnId,column_name as columnName,type,description from column_info where column_guid=#{columnGuid} and status='ACTIVE' ")
    Column getColumnInfoByColumnGuid(@Param("columnGuid")String columnGuid);

    @Select("<script>" +
            "select column_guid as columnId,column_name as columnName,type,description from column_info where status='ACTIVE' and column_guid in " +
            " <foreach item='item' index='index' collection='list' separator=',' open='(' close=')'>" +
            " #{item}" +
            " </foreach>" +
            "</script>")
    List<Column> selectListByColumnGuid(@Param("list") Set<String> columnGuid);

    @Select("select column_guid from column_info where table_guid=#{tableGuid} and column_name = #{columnName} and status='ACTIVE'")
    String getColumnGuid(@Param("tableGuid")String tableGuid, @Param("columnName")String columnName);

    @Update("delete from column_info where column_guid=#{columnGuid}")
    void deleteColumn(@Param("columnGuid")String columnGuid);

    @Select("select column_name as columnName, display_name as displayName from column_info where table_guid=#{tableGuid} and status='ACTIVE' order by column_name")
    public List<Column> getColumnNameWithDisplayList(@Param("tableGuid")String tableGuid);

    @Update("update tableInfo set display_name=#{displayName},display_operator=#{displayOperator}, display_updatetime=#{displayUpdateTime} where tableGuid=#{tableGuid}")
    public int updateTableDisplay(@Param("tableGuid")String tableGuid, @Param("displayName")String displayName, @Param("displayOperator")String displayOperator, @Param("displayUpdateTime")String displayUpdateTime);

    @Select({" <script>",
             " select count(*)over() total,column_guid as columnId, column_name as columnName, display_name as displayName, display_updatetime as displayNameUpdateTime, type",
             " from column_info",
             " where table_guid=#{tableGuid}",
             " and (column_name like concat('%',#{queryText},'%')  ESCAPE '/' or display_name like concat('%',#{queryText},'%')  ESCAPE '/')",
             " and status !='DELETED' ",
             " <if test='columnTypes!= null'>",
             " and type in ",
             " <foreach item='type' index='index' collection='columnTypes' separator=',' open='(' close=')'>" +
             " #{type}" +
             " </foreach>" +
             " </if>",
             " <if test='sortColumn!=null'>",
             " order by ${sortColumn}",
                " <if test='sortColumn!=null and sortOrder!=null'>",
                " ${sortOrder}",
                " </if>",
             "</if>",

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

    @Update("update column_info set status=#{status} where column_guid in ('${columnIds}')")
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

    @Select("<script>" +
            "select column_guid  from column_info where column_guid in " +
            " <foreach item='columnId' index='index' collection='columnIds' separator=',' open='(' close=')'>" +
            " #{columnId}" +
            " </foreach>" +
            "</script>")
    public List<String> queryColumnidBycolumnIds(@Param("columnIds")List<String> columnIds);

    @Select("<script>" +
            " SELECT co.column_guid AS columnId,co.COLUMN_NAME AS columnName,tb.databaseguid AS databaseId,tb.dbname AS databaseName,co.description,co.display_name," +
            " co.display_updatetime AS displayNameUpdateTime, co.status,tb.tableguid AS tableId,tb.tablename AS tableName,co.TYPE,partition_field as isPartitionKey" +
            " FROM column_info AS co INNER JOIN tableinfo AS tb ON co.table_guid = tb.tableguid " +
            " WHERE co.status = 'ACTIVE' AND tb.tableguid = #{tableGuid}" +
            " <if test=\"columnName != null and columnName != '' \">" +
            "   AND co.column_name like concat('%',#{columnName},'%')" +
            " </if>" +
            " <if test=\"type != null and type != '' \">" +
            "    AND co.type = #{type}" +
            " </if>" +
            " <if test=\"description != null and description != '' \">" +
            "    AND co.description like concat('%',#{description},'%')" +
            " </if>" +
            " ORDER BY columnName" +
            "</script>")
    List<Column> selectListByGuidOrLike(@Param("tableGuid") String tableGuid, @Param("columnName") String columnName, @Param("type") String type, @Param("description") String description);


    /**
     * 数据资产检索-查询数据表字段衍生登记信息和标签
     */
    List<ColumnInfo> getDeriveColumnInfo(@Param("columnIds")List<String> columnIds,
                                         @Param("tenantId")String tenantId,
                                         @Param("tableId")String tableId);

    List<Column> queryColumns(@Param("columnIds")List<String> columnIds);
}
