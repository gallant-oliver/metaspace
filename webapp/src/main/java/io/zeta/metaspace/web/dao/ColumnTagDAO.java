package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.apigroup.ApiGroupInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.ApiAudit;
import io.zeta.metaspace.model.share.AuditStatusEnum;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import io.zeta.metaspace.model.table.column.tag.ColumnTagRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ColumnTagDAO {

    @Select("SELECT\n" +
            " ct.id,ct.name,ct.tenant_id,ct.modify_time,ctrtc.column_id\n" +
            "FROM\n" +
            " column_tag ct \n" +
            " LEFT JOIN \n" +
            " column_tag_relation_to_column ctrtc \n" +
            " ON ct.id = ctrtc.tag_id\n" +
            "WHERE\n" +
            " ct.tenant_id = #{tenantId}\n" +
            " AND ctrtc.column_id = #{columnId}\n" +
            "GROUP BY ct.id,ct.name,ct.tenant_id,ct.modify_time,ctrtc.column_id")
    List<ColumnTag> getTagListByColumnId(@Param("tenantId") String tenantId,@Param("columnId") String columnId);

    @Select("SELECT\n" +
            " ct.id,ct.name,ct.tenant_id,ct.modify_time" +
            "FROM\n" +
            " column_tag ct \n" +
            "WHERE\n" +
            " ct.tenant_id = #{tenantId}\n" +
            "GROUP BY ct.id,ct.name,ct.tenant_id,ct.modify_time")
    List<ColumnTag> getTagList(@Param("tenantId") String tenantId);

    @Insert("INSERT INTO column_tag ( ID, NAME, tenant_id, create_time, modify_time )\n" +
            "VALUES\n" +
            " (\n" +
            "  #{tag.id},\n" +
            "  #{tag.name},\n" +
            "  #{tag.tenantId},\n" +
            " NOW(),\n" +
            " NOW())")
    void insertColumnTag(@Param("tag") ColumnTag tag);

    @Insert("<script>" +
            "INSERT INTO column_tag_relation_to_column ( ID, tag_id, column_id )\n" +
            "VALUES" +
            " <foreach collection='list' item='relation' index='index'  separator=','>" +
            "(#{relation.id} ,#{relation.tagId} ,#{relation.columnId})"+
            "</foreach>" +
            "</script>")
    void addTagRelationsToColumn(@Param("list") List<ColumnTagRelation> addRelationList);

    @Delete("DELETE \n" +
            "FROM\n" +
            " column_tag_relation_to_column \n" +
            "WHERE\n" +
            " tag_id = #{tagId} \n" +
            " AND column_id = #{columnId}")
    void deleteRelation(@Param("tenantId") String tenantId,@Param("columnId") String columnId, @Param("tagId") String tagId);

    @Select("SELECT COUNT\n" +
            " ( ID ) \n" +
            "FROM\n" +
            " column_tag \n" +
            "WHERE\n" +
            " tenant_id = #{tenantId} \n" +
            " AND NAME = '#{tagName}'")
    int getTagByTagName(@Param("tenantId") String tenantId, @Param("tagName") String tagName);
}

