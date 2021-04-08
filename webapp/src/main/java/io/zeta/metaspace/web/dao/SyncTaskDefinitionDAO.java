package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.desensitization.DesensitizationRule;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.ApiPolyEntity;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.web.typeHandler.ApiPolyEntityTypeHandler;
import io.zeta.metaspace.web.typeHandler.ListStringTypeHandler;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface SyncTaskDefinitionDAO {

    String TABLE_NAME = "sync_task_definition";

    @Select("select count(*) from " + TABLE_NAME + " where name=#{name} and id != #{id} and tenant_id = #{tenantId}")
    int countByName(@Param("id") String id, @Param("name") String name, @Param("tenantId") String tenantId);


    @Insert("INSERT INTO " + TABLE_NAME + " (id, name,description, creator, create_time, update_time, enable, " +
            "cron_start_time, cron_end_time, crontab, data_source_id, sync_all, schemas, tenant_id, category_guid)\n" +
            "VALUES (#{definition.id}, #{definition.name}, #{definition.description}, #{definition.creator}, now(),now(),#{definition.enable}, #{definition.cronStartTime}, #{definition.cronEndTime}, #{definition.crontab}, #{definition.dataSourceId}, #{definition.syncAll}," +
            "#{definition.schemas,typeHandler=io.zeta.metaspace.web.typeHandler.ListStringTypeHandler},#{definition.tenantId}, #{definition.categoryGuid})")
    int insert(@Param("definition") SyncTaskDefinition syncTaskDefinition);

    @Delete({"<script>",
            "DELETE FROM " + TABLE_NAME + " WHERE id in",
            "<foreach collection='ids' item='itemId' index='index' separator=',' open='(' close=')'>",
            "#{itemId}",
            "</foreach>",
            "</script>"})
    int delete(@Param("ids") List<String> ids);

    @Update("UPDATE public.sync_task_definition\n" +
            "SET name            = #{definition.name},\n" +
            "    description     = #{definition.description},\n" +
            "    update_time     = now(),\n" +
            "    enable          = #{definition.enable},\n" +
            "    cron_start_time = #{definition.cronStartTime},\n" +
            "    cron_end_time   = #{definition.cronEndTime},\n" +
            "    crontab         = #{definition.crontab},\n" +
            "    data_source_id  = #{definition.dataSourceId},\n" +
            "    sync_all        = #{definition.syncAll},\n" +
            "    category_guid   = #{definition.categoryGuid},\n" +
            "    schemas         = #{definition.schemas,typeHandler=io.zeta.metaspace.web.typeHandler.ListStringTypeHandler} \n" +
            "WHERE id = #{definition.id} "
    )
    int update(@Param("definition") SyncTaskDefinition syncTaskDefinition);


    @Update("UPDATE public.sync_task_definition\n" +
            "SET update_time     = now(),\n" +
            "    enable          = #{enable}\n" +
            "WHERE id = #{id} "
    )
    int updateEnable(@Param("id") String id, @Param("enable") boolean enable);

    @Results(id="base", value ={
            @Result(property = "schemas", column = "schemas", typeHandler = ListStringTypeHandler.class)
    })
    @Select("<script>" +
            "SELECT count(*) over() as total , definition.*," +
            "( case definition.data_source_id when 'hive' then 'hive' else  db.source_name end ) as dataSourceName," +
            "( case definition.data_source_id when 'hive' then 'HIVE' else  db.source_type end ) as dataSourceType, " +
            "category.name as categoryName " +
            "FROM " + TABLE_NAME + " definition " +
            "left join data_source db on  db.source_id =  definition.data_source_id " +
            "left join category on  (definition.category_guid = category.guid and category.categorytype = 0 and category.tenantid = #{tenantId}) " +
            "WHERE definition.tenant_id = #{tenantId} " +
            "<if test='null != parameters.query and 0 != parameters.query.length() '>" +
            " and definition.name like '%${parameters.query}%' ESCAPE '/' " +
            "</if> " +
            " order by update_time desc " +
            "<if test='parameters.limit!=-1'>" +
            "limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            "offset ${parameters.offset}" +
            "</if>" +
            "</script>")
    List<SyncTaskDefinition> pageList(@Param("parameters") Parameters parameters, @Param("tenantId") String tenantId) throws SQLException;
    
    @ResultMap("base")
    @Select("select * from " + TABLE_NAME + " where id = #{id}")
    SyncTaskDefinition getById(@Param("id") String id);
}
