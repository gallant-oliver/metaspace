package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.DataOwner;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
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

    @Select("select users.username from users,role,role2category,category,table_relation where role2category.roleid=role.roleid and users.roleid=role.roleid and role2category.categoryid=category.guid and category.guid=table_relation.categoryguid and table_relation.tableguid=#{guid}")
    public List<String> getAdminByTableguid(String guid);

    @Select("select generatetime from table_relation where tableguid=#{guid}")
    public String getDateByTableguid(String guid);

    @Select("select businessinfo.name businessObject,category.name department,businessinfo.submitter businessLeader from business2table,businessinfo,category where businessinfo.businessid=business2table.businessid and businessinfo.departmentid=category.guid and business2table.tableguid=#{guid}")
    public List<Table.BusinessObject> getBusinessObjectByTableguid(String guid);

    @Insert("insert into tableinfo(tableguid,tablename,dbname,status,createtime,databaseguid,databasestatus) values(#{table.tableGuid},#{table.tableName},#{table.dbName},#{table.status},#{table.createTime},#{table.databaseGuid},#{table.databaseStatus})")
    public int addTable(@Param("table") TableInfo table);

    @Update("update tableinfo set tablename=#{table.tableName},dbname=#{table.dbName} where tableguid=#{table.tableGuid}")
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

    @Select("select 1 from tableinfo where tableguid=#{tableGuid}")
    public List<Integer> ifTableExists(String tableGuid);

    //@Select("select ownerId from table2owner where tableGuid=#{tableGuid}")
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

}