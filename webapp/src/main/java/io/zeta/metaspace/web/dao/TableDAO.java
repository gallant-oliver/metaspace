package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.DataOwner;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.MetaDataRelatedAPI;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.share.APIInfoHeader;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.SQLException;
import java.util.List;

public interface TableDAO {
    @Select("select * from tableinfo where tableguid=#{guid}")
    public TableInfo getTableInfoByTableguid(String guid) throws SQLException;

    @Select("select generatetime from table_relation where tableguid=#{guid}")
    public String getDateByTableguid(String guid);

    @Select("select businessinfo.businessid,businessinfo.name businessObject,category.name department,businessinfo.submitter businessLeader from business2table,businessinfo,category where businessinfo.businessid=business2table.businessid and businessinfo.departmentid=category.guid and business2table.tableguid=#{guid} and category.tenantid=#{tenantId}")
    public List<Table.BusinessObject> getBusinessObjectByTableguid(@Param("guid") String guid,@Param("tenantId") String tenantId);

    @Insert("insert into tableinfo(tableguid,tablename,dbname,status,createtime,databaseguid,databasestatus,description,source_id) values(#{table.tableGuid},#{table.tableName},#{table.dbName},#{table.status},#{table.createTime},#{table.databaseGuid},#{table.databaseStatus},#{table.description},#{table.sourceId})")
    public int addTable(@Param("table") TableInfo table);

    @Update("update tableinfo set tablename=#{table.tableName},dbname=#{table.dbName},description=#{table.description} where tableguid=#{table.tableGuid}")
    public int updateTable(@Param("table") TableInfo table);

    @Select("select tableguid from tableinfo except select tableguid from table_relation")
    public List<String> getNewTable();

    @Insert({"<script>insert into table_relation values",
            "<foreach item='item' index='index' collection='tableRelations'",
            "open=' ' separator=',' close=' '>",
            "(#{item.relationshipGuid},#{item.categoryGuid},#{item.tableGuid},#{item.generateTime})",
            "</foreach>",
            "</script>"})
    public int addRelations(@Param("tableRelations") List<TableRelation> tableRelations);

    @Select("select count(1) from tableinfo where tableguid=#{tableGuid}")
    public Integer ifTableExists(String tableGuid);

    @Select("select organization.name,table2owner.tableGuid,table2owner.pkId from organization,table2owner where organization.pkId=table2owner.pkId and tableGuid=#{tableGuid}")
    public List<DataOwnerHeader> getDataOwnerList(@Param("tableGuid") String tableGuid);

    @Select("select pkId from table2owner where tableGuid=#{tableGuid}")
    public List<String> getDataOwnerIdList(@Param("tableGuid") String tableGuid);

    @Delete({"<script>",
             "delete from table2owner where tableGuid=#{tableGuid} and pkId in ",
             "<foreach item='guid' index='index' collection='ownerList' separator=',' open='(' close=')'>" ,
             "#{guid}",
             "</foreach>",
             "</script>"})
    public int deleteTableOwner(@Param("tableGuid")String tableGuid, @Param("ownerList")List<String> ownerList);

    @Delete("delete from tableInfo where tableGuid=#{tableGuid}")
    public int deleteTableInfo(@Param("tableGuid")String tableGuid);

    @Delete("delete from table2owner where tableGuid=#{tableGuid}")
    public int deleteTableRelatedOwner(@Param("tableGuid")String tableGuid);


    @Update("update tableInfo set subordinateSystem=#{info.subordinateSystem},subordinateDatabase=#{info.subordinateDatabase} where tableGuid=#{tableGuid}")
    public int updateTableEditInfo(@Param("tableGuid")String tableGuid, @Param("info")Table info);

    @Update("update tableInfo set subordinatesystem=#{info.subordinateSystem},subordinatedatabase=#{info.subordinateDatabase},systemadmin=#{info.systemAdmin},datawarehouseadmin=#{info.dataWarehouseAdmin},datawarehousedescription=#{info.dataWarehouseDescription},catalogAdmin=#{info.catalogAdmin} where tableGuid=#{tableGuid}")
    public int updateTableInfo(@Param("tableGuid")String tableGuid, @Param("info")Table info);

    @Select("select tablename from tableInfo where tableguid=#{guid} and status='ACTIVE'")
    public String getTableNameByTableGuid(String guid);

    @Select("select tableName,dbName as databaseName from tableInfo where tableGuid=#{guid}")
    public Table getDbAndTableName(@Param("guid")String guid);

    @Select({"<script>",
            " select count(*)over() total,guid,name,tableGuid,path,requestMode,version,username as creator from apiInfo join users on users.userid=apiInfo.keeper ",
            "where tableGuid=#{tableGuid} and apiInfo.tenantid=#{tenantId}",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<MetaDataRelatedAPI> getTableInfluenceWithAPI(@Param("tableGuid")String tableGuid, @Param("limit") int limit, @Param("offset") int offset,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select dbname||'.'||tablename from tableinfo " +
            " where tableguid in " +
            " <foreach item='id' index='index' collection='tableIds' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public List<String> getTableNames(@Param("tableIds")List<String> tableIds);

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
             "<foreach item='name' index='index' collection='dbNames' separator=',' open='(' close=')'>" ,
             "#{name}",
             "</foreach>",
             "</script>"})
    public List<String> getTableByDB(@Param("dbNames")List<String> dbNames);

    @Select({"<script>",
             "select tableguid from tableinfo " +
             "</script>"})
    public List<String> getTables();
    @Select("")
    List<String> getOptionalDbBySourceId(@Param("dataSourceId")String dataSourceId, @Param("active")String active);
}
