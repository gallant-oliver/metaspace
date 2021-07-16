package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.ConnectorEntity;
import io.zeta.metaspace.model.metadata.Database;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface DbDAO {

    @Select("select database_guid as databaseId, database_name, owner, db_type, status, instance_guid, database_description from db_info where database_guid = #{databaseId} and is_deleted = false")
    Database getDb(@Param("databaseId")String databaseId);


    @Update("update db_info set database_name = #{database.databaseName},owner = #{database.owner}, status = #{database.status}, " +
            "database_Description = #{database.databaseDescription} where database_guid = #{database.databaseId} and is_deleted = false")
    void updateDb(@Param("database")Database database);

    @Insert("insert into db_info(database_guid, database_name, owner, db_type, status, instance_guid, database_description)" +
            "values(#{database.databaseId}, #{database.databaseName}, #{database.owner}, #{database.dbType}, #{database.status}, #{database.instanceId}, " +
            " #{database.databaseDescription})")
    void insertDb(@Param("database")Database database);

    @Select("select id from source_db where source_id = #{sourceId} and db_guid = #{databaseId}")
    String getSourceDbRelationId(@Param("databaseId")String databaseId, @Param("sourceId")String sourceId);

    @Insert("insert into source_db(id, source_id, db_guid) values (#{id}, #{sourceId}, #{databaseId})")
    String insertSourceDbRelation(@Param("id")String id, @Param("databaseId")String databaseId, @Param("sourceId")String sourceId);
}

