package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.TechnicalCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface DbDAO {

    @Select("select database_guid as databaseId, database_name, owner, db_type, status, instance_guid, database_description from db_info where database_guid = #{databaseId}")
    Database getDb(@Param("databaseId")String databaseId);

    @Delete("delete from db_info " +
            "where database_guid != #{dbGuid} and instance_guid = #{instanceGuid} and lower(database_name) = lower(#{dbName}) ")
    void deleteIfExitDbByName(@Param("dbGuid")String dbGuid, @Param("instanceGuid")String instanceGuid, @Param("dbName")String dbName);

    @Select({"<script>",
                "select t5.database_guid as databaseId, t5.database_name, t5.owner, t5.db_type, t5.status, t5.instance_guid, t5.database_description ",
                "from  data_source t2 join data_source t3 on (t2.source_type = t3.source_type and t2.ip = t3.ip and t2.port = cast(t3.port as char(16)) and t2.source_id = #{datasourceId})",
                "join source_db t4 on t3.source_id = t4.source_id ",
                "join db_info t5 on t4.db_guid = t5.database_guid where t5.database_name in ",
                "<if test='dbNames != null and dbNames.size()>0' >",
                    "<foreach collection='dbNames' item='dbName' index='index' separator=',' open='(' close=')'>" ,
                        "#{dbName}",
                    "</foreach>",
                "</if>",
            "</script>"
    })
    List<Database> getSourceDbs(@Param("datasourceId")String datasourceId, @Param("dbNames")List<String> dbNames);

//    @Select("select database_guid as databaseId, database_name, owner, db_type, status, instance_guid, database_description from db_info where is_deleted = false")
//    List<Database> getAllDbs();


    @Update("update db_info set database_name = #{database.databaseName},owner = #{database.owner}, status = #{database.status}, " +
            "database_Description = #{database.databaseDescription} where database_guid = #{database.databaseId}")
    void updateDb(@Param("database")Database database);

    @Insert("insert into db_info(database_guid, database_name, owner, db_type, status, instance_guid, database_description)" +
            "values(#{database.databaseId}, #{database.databaseName}, #{database.owner}, #{database.dbType}, #{database.status}, " +
            "#{database.instanceId}, #{database.databaseDescription})")
    void insertDb(@Param("database")Database database);

    @Select("select id from source_db where source_id = #{sourceId} and db_guid = #{databaseId}")
    String getSourceDbRelationId(@Param("databaseId")String databaseId, @Param("sourceId")String sourceId);

    @Delete("delete from source_db where db_guid in ('${databaseIds}')")
    void deleteSourceDbRelationId(@Param("databaseIds")String databaseIds);

    @Update("update db_info set status=#{status} where database_guid in ('${databaseGuids}')")
    int updateDatabaseStatusBatch(@Param("databaseGuids") String databaseGuids, @Param("status") String status);

    @Insert("insert into source_db(id, source_id, db_guid) values (#{id}, #{sourceId}, #{databaseId})")
    void insertSourceDbRelation(@Param("id")String id, @Param("databaseId")String databaseId, @Param("sourceId")String sourceId);
	
	@Select({"<script>",
            "select t2.category_id as guid,t1.db_type as dbType ,t1.database_guid as dbId, t2.data_source_id as sourceId",
            " from db_info t1",
            " inner join source_info t2 ",
            " on t1.database_guid = t2.database_id ",
            " where t2.tenant_id = #{tenantId} and t2.version = 0 and t2.category_id in ",
            " <foreach item='categoryId' index='index' collection='categoryIds' separator=',' open='(' close=')'>",
            " #{categoryId}",
            " </foreach>",
            "</script>"})
    List<TechnicalCategory> queryDbTypeByCategoryIds(@Param("tenantId") String tenantId, @Param("categoryIds") List<String> categoryIds);


    @Select({"<script>",
            "select database_guid as id,database_name as name from db_info where database_guid = #{dbId}",
            "union ",
            "select source_id as id,source_name as name from data_source where source_id = #{sourceId}",
            "</script>"})
    List<Map<String, String>> queryDbNameAndSourceNameByIds(@Param("dbId") String dbId, @Param("sourceId") String sourceId);
}

