package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.sourceinfo.derivetable.vo.DeriveTableVersion;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 衍生表信息表 Mapper 接口
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Mapper
public interface SourceInfoDeriveTableInfoDAO {

    @Update("update source_info_derive_table_info set version = version + 1 where table_guid = #{tableGuid}")
    int updateVersionByTableGuid(@Param("tableGuid") String tableGuid);

    @Select({"<script>",
            " select *,count(*)over() total from source_info_derive_table_info where version = -1 and tenant_id = #{tenantId} ",
            " <if test='tableName != null '>",
            " and ( table_name_en like #{tableName} or table_name_zh like #{tableName})",
            " </if>",
            " <if test='state != null '>",
            " and state = #{state}",
            " </if>",
            " order by update_time desc,table_name_zh asc",
            " <if test='limit != null and limit != -1'>",
            " limit #{limit} offset #{offset}",
            " </if>",
            "</script>"})
    List<SourceInfoDeriveTableInfo> queryDeriveTableList(@Param("tenantId") String tenantId, @Param("tableName") String tableName,
                                                         @Param("state") Integer state, @Param("offset") int offset, @Param("limit") int limit);

    @Update("update source_info_derive_table_info set version = #{version} where id = #{tableId}")
    int updateVersionByTableId(@Param("tableId") String tableId, @Param("version") int version);

    @Select({"<script>",
            " select version,updater,cast(update_time as varchar) as updateTime,count(*)over() total",
            " from source_info_derive_table_info ",
            " where table_guid = #{tableGuid} and version >= 1 ",
            " order by version ",
            " <if test='limit != null and limit != -1'>",
            " limit #{limit} offset #{offset}",
            " </if>",
            "</script>"})
    List<DeriveTableVersion> queryVersionByTableGuid(@Param("tableGuid") String tableGuid, @Param("offset") int offset, @Param("limit") int limit);

    @Delete({"<script>",
            "delete from source_info_derive_table_info where table_guid in ",
            " <foreach item='tableGuid' index='index' collection='tableGuids' separator=',' open='(' close=')'>",
            " #{tableGuid}",
            " </foreach>",
            "</script>"})
    int deleteByTableGuids(@Param("tableGuids") List<String> tableGuids);

    @Select({"<script>",
            "select distinct table_guid from source_info_derive_table_info where tenant_id = #{tenantId} and table_guid in ",
            " <foreach item='tableGuid' index='index' collection='tableGuids' separator=',' open='(' close=')'>",
            " #{tableGuid}",
            " </foreach>",
            "</script>"})
    List<String> getByGuidsAndTenantId(@Param("tenantId") String tenantId, @Param("tableGuids") List<String> tableGuids);


    @Insert({"<script>",
            " INSERT INTO source_info_derive_table_info",
            " (id, table_guid, table_name_en, table_name_zh, procedure, category_id, db_type, business_id, db_id, source_id,",
            " update_frequency, etl_policy, incre_standard, clean_rule, filter, tenant_id, remark, version,",
            " source_table_guid, creator, create_time, updater, update_time, ddl, dml, state) VALUES ",
            " (#{deriveTable.id},#{deriveTable.tableGuid},#{deriveTable.tableNameEn},#{deriveTable.tableNameZh},",
            " #{deriveTable.procedure},#{deriveTable.categoryId},#{deriveTable.dbType},#{deriveTable.businessId},",
            " #{deriveTable.dbId},#{deriveTable.sourceId},#{deriveTable.updateFrequency},",
            " #{deriveTable.etlPolicy},#{deriveTable.increStandard},#{deriveTable.cleanRule},#{deriveTable.filter},",
            " #{deriveTable.tenantId},#{deriveTable.remark},#{deriveTable.version},#{deriveTable.sourceTableGuid},",
            " #{deriveTable.creator},#{deriveTable.createTime},#{deriveTable.updater},#{deriveTable.updateTime},",
            " #{deriveTable.ddl},#{deriveTable.dml},#{deriveTable.state}) ",
            "</script>"})
    int add(@Param("deriveTable") SourceInfoDeriveTableInfo deriveTable);

    @Insert({"<script>",
            " INSERT INTO source_info_derive_table_info",
            " (id, table_guid, table_name_en, table_name_zh, procedure, category_id, db_type, business_id, db_id, source_id,",
            " update_frequency, etl_policy, incre_standard, clean_rule, filter, tenant_id, remark, version,",
            " source_table_guid, creator, create_time, updater, update_time, ddl, dml, state) VALUES ",
            " (#{deriveTable.id},#{deriveTable.tableGuid},#{deriveTable.tableNameEn},#{deriveTable.tableNameZh},",
            " #{deriveTable.procedure},#{deriveTable.categoryId},#{deriveTable.dbType},#{deriveTable.businessId},",
            " #{deriveTable.dbId},#{deriveTable.sourceId},#{deriveTable.updateFrequency},",
            " #{deriveTable.etlPolicy},#{deriveTable.increStandard},#{deriveTable.cleanRule},#{deriveTable.filter},",
            " #{deriveTable.tenantId},#{deriveTable.remark},#{deriveTable.version},#{deriveTable.sourceTableGuid},",
            " #{deriveTable.creator},#{deriveTable.createTime},#{deriveTable.updater},#{deriveTable.updateTime},",
            " #{deriveTable.ddl},#{deriveTable.dml},#{deriveTable.state}) ",
            " ON conflict(id) DO UPDATE SET table_guid = excluded.table_guid,table_name_en = excluded.table_name_en,",
            " table_name_zh = excluded.table_name_zh,procedure = excluded.procedure,category_id = excluded.category_id,",
            " db_type = excluded.db_type, db_id = excluded.db_id,source_id = excluded.source_id, business_id = excluded.business_id,",
            " update_frequency = excluded.update_frequency,etl_policy = excluded.etl_policy,incre_standard = excluded.incre_standard,",
            " clean_rule = excluded.clean_rule,filter = excluded.filter,tenant_id = excluded.tenant_id,remark = excluded.remark,",
            " version = excluded.version,source_table_guid = excluded.source_table_guid,creator = excluded.creator,create_time = excluded.create_time,",
            " updater = excluded.updater,update_time = excluded.update_time,ddl = excluded.ddl,dml = excluded.dml,state = excluded.state",
            "</script>"})
    int upsert(@Param("deriveTable") SourceInfoDeriveTableInfo deriveTable);

    @Select({"<script>",
            " select id as id, ",
            " table_guid as tableGuid, table_name_en as tableNameEn, table_name_zh as tableNameZh,",
            " procedure as procedure, category_id as categoryId, db_type as dbType, ",
            " business_id as businessId, db_id as dbId, source_id as sourceId,",
            " update_frequency as updateFrequency, etl_policy as etlPolicy, ",
            " incre_standard as increStandard, clean_rule as cleanRule, filter as filter,",
            " tenant_id as tenantId, remark as remark, version as version,",
            " source_table_guid as sourceTableGuid, creator as creator, cast(create_time as varchar) as createTimeStr,",
            " updater as updater, cast(update_time as varchar) as updateTimeStr, ddl as ddl, dml as dml, state as state",
            " from source_info_derive_table_info where id = #{tableId} and tenant_id = #{tenantId}",
            "</script>"})
    SourceInfoDeriveTableInfo getByIdAndTenantId(@Param("tableId") String tableId, @Param("tenantId") String tenantId);

    @Select({"<script>",
            " select id as id, ",
            " table_guid as tableGuid, table_name_en as tableNameEn, table_name_zh as tableNameZh,",
            " procedure as procedure, category_id as categoryId, db_type as dbType, ",
            " business_id as businessId, db_id as dbId, source_id as sourceId,",
            " update_frequency as updateFrequency, etl_policy as etlPolicy, ",
            " incre_standard as increStandard, clean_rule as cleanRule, filter as filter,",
            " tenant_id as tenantId, remark as remark, version as version,",
            " source_table_guid as sourceTableGuid, creator as creator, cast(create_time as varchar) as createTimeStr,",
            " updater as updater, cast(update_time as varchar) as updateTimeStr, ddl as ddl, dml as dml, state as state",
            " from source_info_derive_table_info where id = #{tableId} and table_guid = #{tableGuid} and tenant_id = #{tenantId}",
            "</script>"})
    SourceInfoDeriveTableInfo getByIdAndGuidAndTenantId(@Param("tableId") String tableId, @Param("tableGuid") String tableGuid, @Param("tenantId") String tenantId);
}
