package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.pojo.TableInfo;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface RoleDAO {

   /* @Select("<script>select distinct tableinfo.tableGuid,tableinfo.tableName,tableinfo.dbName,tableinfo.databaseGuid,tableinfo.display_name as displayName from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and tableinfo.status='ACTIVE' and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.databaseGuid=#{db} order by tableinfo.tablename</script>")*/
    @Select("<script>select distinct tableinfo.tableGuid,tableinfo.tableName,tableinfo.dbName,tableinfo.databaseGuid,tableinfo.display_name as displayName " +
            " from tableinfo where  tableinfo.status='ACTIVE' " +
            "     and tableinfo.databaseGuid=#{db} order by tableinfo.tablename</script>")
    public List<TableInfo> getTableInfosByDBId(@Param("guids") List<String> guids, @Param("db") String db);


    @Select("<script>select distinct tableinfo.tableGuid,tableinfo.tableName,tableinfo.dbName,tableinfo.databaseGuid,tableinfo.status,tableinfo.createtime" +
            " FROM tableinfo WHERE tableinfo.status = 'ACTIVE' "+
            " AND tableinfo.databaseGuid=#{db} order by tableinfo.tablename <if test='limit!= -1'>limit #{limit}</if> offset #{offset}</script>")
    public List<TechnologyInfo.Table> getTableInfosByDBIdByParameters(@Param("db") String db, @Param("offset") long offset, @Param("limit") long limit);

    @Select("<script>select count(distinct tableinfo.tableGuid) from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.databaseGuid=#{db} </script>")
    public long getTableInfosByDBIdCount(@Param("guids") List<String> guids, @Param("db") String db);
}
