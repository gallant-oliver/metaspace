package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.MetaDataRelatedAPI;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableInfoId;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface TableDAO {
    @Select("select * from tableinfo where tableguid=#{guid}")
    TableInfo getTableInfoByTableguid(String guid);

    @Delete("delete from tableinfo where tableguid != 'tableGuid' and databaseguid = #{dbGuid} and lower(tablename) = lower(#{tableName}) and status = 'ACTIVE'")
    void deleteIfExist(@Param("tableGuid")String tableGuid, @Param("dbGuid") String dbGuid, @Param("tableName") String tableName);

    @Select("select generatetime from table_relation where tableguid=#{guid}")
    public String getDateByTableguid(String guid);

    @Select("SELECT DISTINCT db_info.database_guid FROM db_info INNER JOIN source_db on db_info.database_guid=source_db.db_guid INNER JOIN data_source on source_db.source_id = data_source.source_id\n" +
            "WHERE data_source.tenantid = #{tenantId} and db_info.status = 'ACTIVE'")
    List<String> selectDatabaseGuidByTenantId(@Param("tenantId") String tenantId);

    @Select("SELECT tableinfo.databaseguid,tableinfo.dbname FROM tableinfo INNER JOIN data_source on tableinfo.source_id=data_source.source_id WHERE data_source.tenantid = #{tenantId} group by tableinfo.databaseguid,tableinfo.dbname")
    List<TableInfo> selectDatabaseByTenantId(@Param("tenantId") String tenantId);

    @Select("SELECT DISTINCT tableinfo.tableguid FROM tableinfo INNER JOIN data_source on tableinfo.source_id=data_source.source_id WHERE data_source.tenantid = #{tenantId}")
    List<String> selectGuidByTenantId(@Param("tenantId") String tenantId);

    @Select("SELECT  businessinfo.businessid,businessinfo.NAME businessObject,category.NAME department,users.username businessLeader FROM business2table,businessinfo,category,users WHERE businessinfo.businessid = business2table.businessid AND businessinfo.departmentid = category.guid AND users.userid =  businessinfo.submitter AND business2table.tableguid = #{guid} AND category.tenantid = #{tenantId}")
    public List<Table.BusinessObject> getBusinessObjectByTableguid(@Param("guid") String guid, @Param("tenantId") String tenantId);

    @Insert("insert into tableinfo(tableguid,tablename,dbname,status,createtime,databaseguid,databasestatus,description, owner, type)\n" +
            "values(#{table.tableGuid},#{table.tableName},#{table.dbName},#{table.status},#{table.createTime},#{table.databaseGuid},#{table.databaseStatus},#{table.description}" +
            ",#{table.owner},#{table.type})")
    public int addTable(@Param("table") TableInfo table);

    @Update("update tableinfo set tablename=#{table.tableName},status = #{table.status} ,dbname=#{table.dbName},description=#{table.description} where tableguid=#{table.tableGuid}")
    public int updateTable(@Param("table") TableInfo table);

    @Insert("insert into table_relation (relationshipGuid, categoryGuid, tableGuid, generateTime, tenant_id) values" +
            "(#{tableRelation.relationshipGuid},#{tableRelation.categoryGuid},#{tableRelation.tableGuid},#{tableRelation.generateTime}," +
                    "#{tableRelation.tenantId})")
    public int addRelation(@Param("tableRelation") TableRelation tableRelation);

    @Select("select relationshipGuid, categoryGuid, tableGuid, generateTime, tenant_id as tenantId from table_relation " +
            "where categoryGuid = #{tableRelation.categoryGuid} and tableGuid = #{tableRelation.tableGuid} and tenant_id = #{tableRelation.tenantId}")
    TableRelation selectRelation(@Param("tableRelation") TableRelation tableRelation);

    @Select("select count(1) from tableinfo where tableguid=#{tableGuid}")
    public Integer ifTableExists(String tableGuid);

    @Select("select tableguid, databasestatus from tableinfo where dbname =#{dbName} and tablename =#{tableName} and source_id = #{sourceId} and status = 'ACTIVE'")
    TableInfo getTableInfo(@Param("sourceId") String sourceId, @Param("dbName") String dbName, @Param("tableName") String tableName);

    @Select("select tableguid from tableinfo where databaseguid = #{databaseGuid} and status = 'ACTIVE'")
    List<String> getTableGuidsByDbGuid(@Param("databaseGuid") String databaseGuid);

    @Select("select tableguid from tableinfo where databaseguid in (#{databaseGuids}) and status = 'ACTIVE'")
    List<String> getTableGuidByDataBaseGuids(@Param("databaseGuids")String databaseGuids);

    @Update("update tableInfo set databasestatus=#{databaseStatus} where databaseguid in ('${databaseGuids}')")
    void updateTableDatabaseStatusBatch(@Param("databaseGuids") String databaseGuids, @Param("databaseStatus") String databaseStatus);


    @Select("select organization.name,table2owner.tableGuid,table2owner.pkId from organization,table2owner where organization.pkId=table2owner.pkId and tableGuid=#{tableGuid}")
    public List<DataOwnerHeader> getDataOwnerList(@Param("tableGuid") String tableGuid);

    @Select("select pkId from table2owner where tableGuid=#{tableGuid}")
    public List<String> getDataOwnerIdList(@Param("tableGuid") String tableGuid);

    @Delete({"<script>",
            "delete from table2owner where tableGuid=#{tableGuid} and pkId in ",
            "<foreach item='guid' index='index' collection='ownerList' separator=',' open='(' close=')'>",
            "#{guid}",
            "</foreach>",
            "</script>"})
    public int deleteTableOwner(@Param("tableGuid") String tableGuid, @Param("ownerList") List<String> ownerList);

    @Delete("delete from tableInfo where tableGuid=#{tableGuid}")
    public int deleteTableInfo(@Param("tableGuid") String tableGuid);

    @Delete("delete from table2owner where tableGuid=#{tableGuid}")
    public int deleteTableRelatedOwner(@Param("tableGuid") String tableGuid);

    @Update("update table2owner set tableGuid = #{tableGuid} where tableGuid=#{OldTableGuid}")
    int updateTableRelatedOwner(@Param("tableGuid") String tableGuid, @Param("OldTableGuid") String OldTableGuid);

    @Update("update table_relation set tableguid = #{tableGuid} where tableguid=#{OldTableGuid}")
    int updateTableRelations(@Param("tableGuid") String tableGuid, @Param("OldTableGuid") String OldTableGuid);

    @Update("update table2tag set tableguid = #{tableGuid} where tableguid=#{OldTableGuid}")
    int updateTableTags(@Param("tableGuid") String tableGuid, @Param("OldTableGuid") String OldTableGuid);

    @Update("update tableInfo set subordinateSystem=#{info.subordinateSystem},subordinateDatabase=#{info.subordinateDatabase} where tableGuid=#{tableGuid}")
    public int updateTableEditInfo(@Param("tableGuid") String tableGuid, @Param("info") Table info);

    @Update("update tableInfo set subordinatesystem=#{info.subordinateSystem},subordinatedatabase=#{info.subordinateDatabase},systemadmin=#{info.systemAdmin},datawarehouseadmin=#{info.dataWarehouseAdmin},datawarehousedescription=#{info.dataWarehouseDescription},catalogAdmin=#{info.catalogAdmin} where tableGuid=#{tableGuid}")
    public int updateTableInfo(@Param("tableGuid") String tableGuid, @Param("info") Table info);

    @Select("select tablename from tableInfo where tableguid=#{guid} and status='ACTIVE'")
    public String getTableNameByTableGuid(String guid);

    @Select("select tableName,dbName as databaseName from tableInfo where tableGuid=#{guid}")
    public Table getDbAndTableName(@Param("guid") String guid);

    @Select({"<script>",
            " select count(*)over() total,guid,name,tableGuid,path,requestMode,version,username as creator from api join users on users.userid=api.creator ",
            "where tableGuid=#{tableGuid} and api.tenantid=#{tenantId} AND api.valid = true",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<MetaDataRelatedAPI> getTableInfluenceWithAPI(@Param("tableGuid") String tableGuid, @Param("limit") int limit, @Param("offset") int offset, @Param("tenantId") String tenantId);

    @Select("<script>" +
            "select dbname||'.'||tablename from tableinfo " +
            " where tableguid in " +
            " <foreach item='id' index='index' collection='tableIds' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public List<String> getTableNames(@Param("tableIds") List<String> tableIds);

    @Select("<script>" +
            " select count(1) from tableinfo where tablename=#{tableName} and dbname=#{dbName} and tableguid=#{tableGuid} " +
            " <if test=\"description != null\">" +
            "and description=#{description} " +
            " </if>" +
            " <if test=\"description == null\">" +
            "and description is null " +
            " </if>" +
            " </script>")
    public Integer ifTableInfo(TableInfo table);

    @Select({"<script>",
            "select tableguid from tableinfo where dbname in ",
            "<foreach item='name' index='index' collection='dbNames' separator=',' open='(' close=')'>",
            "#{name}",
            "</foreach>",
            "</script>"})
    public List<String> getTableByDB(@Param("dbNames") List<String> dbNames);

    @Select({"<script>",
            "select tableguid from tableinfo " +
                    "</script>"})
    public List<String> getTables();

    @Select("select distinct dbname from tableinfo where source_id=#{dataSourceId} and databasestatus=#{dataBaseStatus}")
    List<String> getOptionalDbBySourceId(@Param("dataSourceId") String dataSourceId, @Param("dataBaseStatus") String dataBaseStatus);

    /**
     * 获取可选的表
     */
    @Select("select * from tableinfo where source_id=#{dataSourceId} and dbname=#{dbName} and status=#{status}")
    List<TableInfo> getTableByDataSourceAndDb(@Param("dataSourceId") String dataSourceId, @Param("dbName") String dbName, @Param("status") String status);

    @Select({"<script>",
            " SELECT tb.source_id AS sourceId,'hive' AS sourceName,tb.databaseguid AS databaseGuid,tb.dbname AS dbName,tb.tableguid AS tableGuid,tb.tablename AS tableName,co.column_guid AS columnGuid,co.COLUMN_NAME AS columnName",
            " FROM tableinfo AS tb INNER JOIN column_info AS co ON tb.tableguid = co.table_guid WHERE tb.source_id = 'hive' ",
            " <if test='dbNameListHive.size == 0 '>",
            " AND 1 != 1",
            " </if>",
            " <if test='dbNameListHive.size > 0 '>",
            " AND tb.dbname IN",
            " <foreach item='item' index='index' collection='dbNameListHive' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " </if>",
            " AND tb.dbname IN",
            " <foreach item='item' index='index' collection='dbNameList' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " AND tb.tablename IN",
            " <foreach item='item' index='index' collection='tableNameList' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " AND co.COLUMN_NAME IN",
            " <foreach item='item' index='index' collection='columnNameList' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " union",
            " SELECT tb.source_id AS sourceId,da.source_name AS sourceName,tb.databaseguid AS databaseGuid,tb.dbname AS dbName,tb.tableguid AS tableGuid,tb.tablename AS tableName,co.column_guid AS columnGuid,co.COLUMN_NAME AS columnName ",
            " FROM tableinfo AS tb INNER JOIN column_info AS co ON tb.tableguid = co.table_guid INNER JOIN data_source AS da ON tb.source_id = da.source_id WHERE da.tenantid = #{tenantId} ",
            " AND da.source_name IN",
            " <foreach item='item' index='index' collection='sourceNameList' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " AND tb.dbname IN",
            " <foreach item='item' index='index' collection='dbNameList' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " AND tb.tablename IN",
            " <foreach item='item' index='index' collection='tableNameList' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " AND co.COLUMN_NAME IN",
            " <foreach item='item' index='index' collection='columnNameList' open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            "</script>"})
    List<TableInfoId> selectListByName(@Param("tenantId") String tenantId, @Param("sourceNameList") Set<String> sourceNameList, @Param("dbNameListHive") List<String> dbNameListHive, @Param("dbNameList") Set<String> dbNameList, @Param("tableNameList") Set<String> tableNameList, @Param("columnNameList") Set<String> columnNameList);
}
