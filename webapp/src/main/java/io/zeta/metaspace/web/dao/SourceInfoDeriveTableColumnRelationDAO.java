package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableColumnRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 衍生表和字段的关联关系 Mapper 接口
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Mapper
public interface SourceInfoDeriveTableColumnRelationDAO {

    @Delete("delete from source_info_derive_table_column_relation where table_id = #{tableId}")
    int deleteDeriveTableColumnRelationByTableId(@Param("tableId") String tableId);

    @Delete({"<script>",
            "delete from source_info_derive_table_column_relation where table_guid in ",
            " <foreach item='tableGuid' index='index' collection='tableGuids' separator=',' open='(' close=')'>",
            " #{tableGuid}",
            " </foreach>",
            "</script>"})
    int deleteByTableGuids(@Param("tableGuids") List<String> tableGuids);

    @Insert({"<script>",
            "INSERT INTO source_info_derive_table_column_relation(id, table_id, column_guid, table_guid) VALUES (#{tableColumnRelation.id},#{tableColumnRelation.tableId},#{tableColumnRelation.columnGuid},#{tableColumnRelation.tableGuid})",
            "</script>"})
    int add(@Param("tableColumnRelation") SourceInfoDeriveTableColumnRelation tableColumnRelation);

    @Insert({"<script>",
            " INSERT INTO source_info_derive_table_column_relation",
            " (id, table_id, column_guid, table_guid) VALUES",
            " (#{tableColumnRelation.id},#{tableColumnRelation.tableId},#{tableColumnRelation.columnGuid},#{tableColumnRelation.tableGuid})",
            " ON conflict(id) DO UPDATE SET table_id = excluded.table_id,column_guid = excluded.column_guid,table_guid = excluded.table_guid ",
            "</script>"})
    int upsert(@Param("tableColumnRelation") SourceInfoDeriveTableColumnRelation tableColumnRelation);

}
