package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveColumnInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * 衍生表对应的字段 Mapper 接口
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Mapper
public interface SourceInfoDeriveColumnInfoDAO {

    @Select("select * from source_info_derive_column_info where column_guid in (select column_guid from source_info_derive_table_column_relation where table_id = #{tableId}) order by sort")
    List<SourceInfoDeriveColumnInfo> getDeriveColumnInfoListByTableId(@Param("tableId") String tableId);

    @Delete("delete from source_info_derive_column_info where column_guid in " +
            "(select column_guid from source_info_derive_table_column_relation where table_guid  = #{tableGuid} " +
            "and  column_guid in (select column_guid from source_info_derive_table_column_relation where table_id = #{tableId}) " +
            "group by column_guid having count(1) = 1) ")
    int deleteDeriveColumnInfoByTableId(@Param("tableId") String tableId, @Param("tableGuid") String tableGuid);

    @Select("select * from source_info_derive_column_info where column_guid in (select column_guid from source_info_derive_table_column_relation where table_guid = #{tableGuid})")
    List<SourceInfoDeriveColumnInfo> getDeriveColumnInfoListByTableGuid(@Param("tableGuid") String tableGuid);

    @Delete({"<script>",
            "delete from source_info_derive_column_info where table_guid in ",
            " <foreach item='tableGuid' index='index' collection='tableGuids' separator=',' open='(' close=')'>",
            " #{tableGuid}",
            " </foreach>",
            "</script>"})
    int deleteByTableGuids(@Param("tableGuids") List<String> tableGuids);

    @Insert({"<script>",
            " INSERT INTO source_info_derive_column_info(id, column_guid, column_name_en, column_name_zh, data_type, ",
            " source_column_guid, primary_key, remove_sensitive, mapping_rule, mapping_describe, group_field, permission_field, ",
            " secret, secret_period, important, remark, tenant_id, table_guid, sort) ",
            " VALUES (#{deriveColumn.id},#{deriveColumn.columnGuid},#{deriveColumn.columnNameEn},#{deriveColumn.columnNameZh},",
            " #{deriveColumn.dataType},#{deriveColumn.sourceColumnGuid},#{deriveColumn.primaryKey},#{deriveColumn.removeSensitive},",
            " #{deriveColumn.mappingRule},#{deriveColumn.mappingDescribe},#{deriveColumn.groupField},#{deriveColumn.permissionField},",
            " #{deriveColumn.secret},#{deriveColumn.secretPeriod},#{deriveColumn.important},#{deriveColumn.remark},#{deriveColumn.tenantId},",
            " #{deriveColumn.tableGuid}, #{deriveColumn.sort})",
            "</script>"})
    int add(@Param("deriveColumn") SourceInfoDeriveColumnInfo deriveColumn);

    @Insert({"<script>",
            " INSERT INTO source_info_derive_column_info(id, column_guid, column_name_en, column_name_zh, data_type, ",
            " source_column_guid, primary_key, remove_sensitive, mapping_rule, mapping_describe, group_field, permission_field, ",
            " secret, secret_period, important, remark, tenant_id, table_guid, sort) ",
            " VALUES (#{deriveColumn.id},#{deriveColumn.columnGuid},#{deriveColumn.columnNameEn},#{deriveColumn.columnNameZh},",
            " #{deriveColumn.dataType},#{deriveColumn.sourceColumnGuid},#{deriveColumn.primaryKey},#{deriveColumn.removeSensitive},",
            " #{deriveColumn.mappingRule},#{deriveColumn.mappingDescribe},#{deriveColumn.groupField},#{deriveColumn.permissionField},",
            " #{deriveColumn.secret},#{deriveColumn.secretPeriod},#{deriveColumn.important},#{deriveColumn.remark},#{deriveColumn.tenantId},",
            " #{deriveColumn.tableGuid}, #{deriveColumn.sort})",
            " ON conflict(id) DO UPDATE SET column_guid = excluded.column_guid,column_name_en = excluded.column_name_en,column_name_zh = excluded.column_name_zh,data_type = excluded.data_type,",
            " source_column_guid = excluded.source_column_guid ,primary_key = excluded.primary_key ,remove_sensitive = excluded.remove_sensitive ,mapping_rule = excluded.mapping_rule,",
            " mapping_describe = excluded.mapping_describe,group_field = excluded.group_field,permission_field = excluded.permission_field,secret = excluded.secret,secret_period = excluded.secret_period,important = excluded.important,",
            " remark = excluded.remark,tenant_id = excluded.tenant_id,table_guid= excluded.table_guid,sort= excluded.sort",
            "</script>"})
    int upsert(@Param("deriveColumn") SourceInfoDeriveColumnInfo deriveColumn);

    @Update("update source_info_derive_column_info set table_guid=#{newTableGuid} where table_guid=#{oldTableGuid}")
    void updateColumnInfoByTableGuid(@Param("newTableGuid")String newTableGuid, @Param("oldTableGuid")String oldTableGuid);

    @Update("update source_info_derive_table_column_relation set table_guid=#{newTableGuid} where table_guid=#{oldTableGuid}")
    void updateColumnRelationByTableGuid(@Param("newTableGuid")String newTableGuid, @Param("oldTableGuid")String oldTableGuid);
}
