package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.sync.SyncTaskInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SyncTaskInstanceDAO {

    String TABLE_NAME = "sync_task_instance";

    @Insert("INSERT INTO " + TABLE_NAME + " (id, name, executor, status, start_time,update_time,definition_id ,log) " +
            " VALUES (#{task.id},#{task.name},#{task.executor},#{task.status,typeHandler=org.apache.ibatis.type.EnumTypeHandler},now(),now(),#{task.definitionId},now() || ' 创建成功')")
    int insert(@Param("task") SyncTaskInstance syncTaskInstance);

    @Delete({"<script>",
            "DELETE FROM " + TABLE_NAME + " WHERE id in",
            "<foreach collection='ids' item='itemId' index='index' separator=',' open='(' close=')'>",
            "#{itemId}",
            "</foreach>",
            "</script>"})
    int delete(@Param("ids") List<String> ids);

    @Update("UPDATE " + TABLE_NAME + " SET status = #{status} , log = log || '\n' || now() || ' ' || #{log}, update_time = now() where id = #{id}")
    int updateStatusAndAppendLog(@Param("id") String id, @Param("status") SyncTaskInstance.Status status, @Param("log") String log);

    @Select("<script>" +
            "SELECT count(*) over() as total , task.id,task.definition_id,task.name,task.executor,task.start_time,task.update_time,task.status FROM " + TABLE_NAME + " task " +
            "WHERE definition_id = #{definitionId} " +
            "<if test='null != parameters.query '>" +
            " and name like '%${parameters.query}%' ESCAPE '/' " +
            "</if> " +
            "<if test='null != status '>" +
            " and status = #{status} " +
            "</if> " +
            "<if test='parameters.limit!=-1'>" +
            "limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            "offset ${parameters.offset}" +
            "</if>" +
            "</script>")
    List<SyncTaskInstance> pageList(@Param("definitionId") String definitionId, @Param("parameters") Parameters parameters, @Param("status") SyncTaskInstance.Status status);

    @Select("select task.id,task.definition_id,task.name,task.executor,task.start_time,task.update_time,task.status  from " + TABLE_NAME + " task where id = #{id}")
    SyncTaskInstance getById(@Param("id") String id);

    @Select("select log  from " + TABLE_NAME + " where id = #{id}")
    String getLog(@Param("id") String id);
}
