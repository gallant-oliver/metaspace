package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.TableExtInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.DeriveTableVersion;
import org.apache.ibatis.annotations.*;

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
            " select t1.id as id, ",
            " t1.table_guid as tableGuid, t1.table_name_en as tableNameEn, t1.table_name_zh as tableNameZh,",
            " t1.procedure as procedure, t1.category_id as categoryId, t1.db_type as dbType, ",
            " t1.business_id as businessId, t1.db_id as dbId, t1.source_id as sourceId,",
            " t1.update_frequency as updateFrequency, t1.etl_policy as etlPolicy, ",
            " t1.incre_standard as increStandard, t1.clean_rule as cleanRule, t1.filter as filter,",
            " t1.tenant_id as tenantId, t1.remark as remark, t1.version as version,",
            " t1.source_table_guid as sourceTableGuid, t1.creator as creator, cast(t1.create_time as varchar) as createTimeStr,",
            " t1.updater as updater, cast(t1.update_time as varchar) as updateTimeStr, t1.ddl as ddl, t1.dml as dml, t1.state as state,",
            " t2.username as creatorName, t3.username as updaterName,count(*)over() as total ",
            " from source_info_derive_table_info t1",
            " left join users t2 on t1.creator = t2.userid",
            " left join users t3 on t1.updater = t3.userid",
            " where t1.version = -1 and t1.tenant_id = #{tenantId} ",
            " <if test='tableName != null '>",
            " and ( t1.table_name_en like #{tableName} or t1.table_name_zh like #{tableName})",
            " </if>",
            " <if test='state != null '>",
            " and t1.state = #{state}",
            " </if>",
            " order by t1.update_time desc,t1.table_name_zh asc",
            " <if test='limit != null and limit != -1'>",
            " limit #{limit} offset #{offset}",
            " </if>",
            "</script>"})
    List<SourceInfoDeriveTableInfo> queryDeriveTableList(@Param("tenantId") String tenantId, @Param("tableName") String tableName,
                                                         @Param("state") Integer state, @Param("offset") int offset, @Param("limit") int limit);

    @Update("update source_info_derive_table_info set version = #{version} where id = #{tableId}")
    int updateVersionByTableId(@Param("tableId") String tableId, @Param("version") int version);

    @Select({"<script>",
            " select t1.version,t2.username as updater,cast(t1.update_time as varchar) as updateTime,count(*)over() as total",
            " from source_info_derive_table_info t1 left join users t2 on t1.updater = t2.userid",
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
            " source_table_guid, operator, file_name, file_path, incremental_field, creator, create_time, updater, update_time, ddl, dml, state,importance,security) VALUES ",
            " (#{deriveTable.id},#{deriveTable.tableGuid},#{deriveTable.tableNameEn},#{deriveTable.tableNameZh},",
            " #{deriveTable.procedure},#{deriveTable.categoryId},#{deriveTable.dbType},#{deriveTable.businessId},",
            " #{deriveTable.dbId},#{deriveTable.sourceId},#{deriveTable.updateFrequency},",
            " #{deriveTable.etlPolicy},#{deriveTable.increStandard},#{deriveTable.cleanRule},#{deriveTable.filter},",
            " #{deriveTable.tenantId},#{deriveTable.remark},#{deriveTable.version},#{deriveTable.sourceTableGuid},",
            " #{deriveTable.operator},#{deriveTable.fileName},#{deriveTable.filePath},#{deriveTable.incrementalField},#{deriveTable.creator},#{deriveTable.createTime},#{deriveTable.updater},#{deriveTable.updateTime},",
            " #{deriveTable.ddl},#{deriveTable.dml},#{deriveTable.state},#{deriveTable.importance},#{deriveTable.security}) ",
            "</script>"})
    int add(@Param("deriveTable") SourceInfoDeriveTableInfo deriveTable);

    @Insert({"<script>",
            " INSERT INTO source_info_derive_table_info",
            " (id, table_guid, table_name_en, table_name_zh, procedure, category_id, db_type, business_id, db_id, source_id,",
            " update_frequency, etl_policy, incre_standard, clean_rule, filter, tenant_id, remark, version,",
            " source_table_guid, operator, file_name, file_path, incremental_field, creator, create_time, updater, update_time, ddl, dml, state,importance,security) VALUES ",
            " (#{deriveTable.id},#{deriveTable.tableGuid},#{deriveTable.tableNameEn},#{deriveTable.tableNameZh},",
            " #{deriveTable.procedure},#{deriveTable.categoryId},#{deriveTable.dbType},#{deriveTable.businessId},",
            " #{deriveTable.dbId},#{deriveTable.sourceId},#{deriveTable.updateFrequency},",
            " #{deriveTable.etlPolicy},#{deriveTable.increStandard},#{deriveTable.cleanRule},#{deriveTable.filter},",
            " #{deriveTable.tenantId},#{deriveTable.remark},#{deriveTable.version},#{deriveTable.sourceTableGuid},",
            " #{deriveTable.operator},#{deriveTable.fileName},#{deriveTable.filePath},#{deriveTable.incrementalField},#{deriveTable.creator},#{deriveTable.createTime},#{deriveTable.updater},#{deriveTable.updateTime},",
            " #{deriveTable.ddl},#{deriveTable.dml},#{deriveTable.state},#{deriveTable.importance},#{deriveTable.security}) ",
            " ON conflict(id) DO UPDATE SET table_guid = excluded.table_guid,table_name_en = excluded.table_name_en,",
            " table_name_zh = excluded.table_name_zh,procedure = excluded.procedure,category_id = excluded.category_id,",
            " db_type = excluded.db_type, db_id = excluded.db_id,source_id = excluded.source_id, business_id = excluded.business_id,",
            " update_frequency = excluded.update_frequency,etl_policy = excluded.etl_policy,incre_standard = excluded.incre_standard,",
            " clean_rule = excluded.clean_rule,filter = excluded.filter,tenant_id = excluded.tenant_id,remark = excluded.remark,",
            " version = excluded.version,source_table_guid = excluded.source_table_guid,operator = excluded.operator,",
            " file_name = excluded.file_name,file_path = excluded.file_path,incremental_field = excluded.incremental_field,creator = excluded.creator,create_time = excluded.create_time,",
            " updater = excluded.updater,update_time = excluded.update_time,ddl = excluded.ddl,dml = excluded.dml,state = excluded.state,importance=excluded.importance,security=excluded.security",
            "</script>"})
    int upsert(@Param("deriveTable") SourceInfoDeriveTableInfo deriveTable);

    @Select({"<script>",
            " select t1.id as id, ",
            " t1.table_guid as tableGuid, t1.table_name_en as tableNameEn, t1.table_name_zh as tableNameZh,",
            " t1.procedure as procedure, t1.category_id as categoryId, t1.db_type as dbType, ",
            " t1.business_id as businessId, t1.db_id as dbId, t1.source_id as sourceId,",
            " t1.update_frequency as updateFrequency, t1.etl_policy as etlPolicy, ",
            " t1.incre_standard as increStandard, t1.clean_rule as cleanRule, t1.filter as filter,",
            " t1.tenant_id as tenantId, t1.remark as remark, t1.version as version,",
            " t1.source_table_guid as sourceTableGuid, t1.operator as operator, t1.file_name as fileName, t1.file_path as filePath,",
            " t1.incremental_field as incrementalField, t1.creator as creator, cast(t1.create_time as varchar) as createTimeStr,",
            " t1.updater as updater, cast(t1.update_time as varchar) as updateTimeStr, t1.ddl as ddl, t1.dml as dml, t1.state as state,",
            " t2.username as creatorName, t3.username as updaterName ",
            " from source_info_derive_table_info t1",
            " left join users t2 on t1.creator = t2.userid",
            " left join users t3 on t1.updater = t3.userid",
            " where t1.id = #{tableId} and t1.tenant_id = #{tenantId}",
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
            " source_table_guid as sourceTableGuid, operator as operator, file_name as fileName, file_path as filePath,",
            " incremental_field as incrementalField, creator as creator, cast(create_time as varchar) as createTimeStr,",
            " updater as updater, cast(update_time as varchar) as updateTimeStr, ddl as ddl, dml as dml, state as state",
            " from source_info_derive_table_info where id = #{tableId} and tenant_id = #{tenantId}",
            "</script>"})
    SourceInfoDeriveTableInfo getByIdAndGuidAndTenantId(@Param("tableId") String tableId, @Param("tenantId") String tenantId);

    @Select({"<script>",
            " select id as id, ",
            " table_guid as tableGuid, table_name_en as tableNameEn, table_name_zh as tableNameZh,",
            " procedure as procedure, category_id as categoryId, db_type as dbType, ",
            " business_id as businessId, db_id as dbId, source_id as sourceId,",
            " update_frequency as updateFrequency, etl_policy as etlPolicy, ",
            " incre_standard as increStandard, clean_rule as cleanRule, filter as filter,",
            " tenant_id as tenantId, remark as remark, version as version,",
            " source_table_guid as sourceTableGuid, operator as operator, file_name as fileName, file_path as filePath,",
            " incremental_field as incrementalField, creator as creator, cast(create_time as varchar) as createTimeStr,",
            " updater as updater, cast(update_time as varchar) as updateTimeStr, ddl as ddl, dml as dml, state as state",
            " from source_info_derive_table_info where table_guid = #{tableGuid} and tenant_id = #{tenantId} and version = #{version}",
            "</script>"})
    SourceInfoDeriveTableInfo getByGuidAndTenantIdAndVersion(@Param("tableGuid") String tableGuid, @Param("tenantId") String tenantId, @Param("version") int version);

    @Delete("delete from source_info_derive_table_info where id = #{tableId}")
    int deleteDeriveTableByTableId(@Param("tableId") String tableId);

    @Update("update source_info_derive_table_info set version = -1 where version = 0 and table_guid = #{tableGuid}")
    int updateVersionToShowByTableGuid(@Param("tableGuid") String tableGuid);

    @Select({"<script>",
            " select count(1) from source_info_derive_table_info where db_id = #{dbId} and table_name_en = #{tableName} and version = -1 ",
            " <if test='id != null'>",
            " and id != #{id}",
            " </if>",
            "</script>"})
    int checkTableNameDump(@Param("tableName") String tableName, @Param("dbId") String dbId, @Param("id") String id);

    @Select({"<script>",
            " select t1.id as id, ",
            " t1.table_guid as tableGuid, t1.table_name_en as tableNameEn, t1.table_name_zh as tableNameZh,",
            " t1.procedure as procedure, t1.category_id as categoryId, t1.db_type as dbType, ",
            " t1.business_id as businessId, t1.db_id as dbId, t1.source_id as sourceId,",
            " t1.update_frequency as updateFrequency, t1.etl_policy as etlPolicy, ",
            " t1.incre_standard as increStandard, t1.clean_rule as cleanRule, t1.filter as filter,",
            " t1.tenant_id as tenantId, t1.remark as remark, t1.version as version,",
            " t1.source_table_guid as sourceTableGuid, t1.operator as operator, t1.file_name as fileName, t1.file_path as filePath,",
            " t1.incremental_field as incrementalField, t1.creator as creator, cast(t1.create_time as varchar) as createTimeStr,",
            " t1.updater as updater, cast(t1.update_time as varchar) as updateTimeStr, t1.ddl as ddl, t1.dml as dml, t1.state as state,",
            " t2.username as creatorName, t3.username as updaterName ",
            " from source_info_derive_table_info t1",
            " left join users t2 on t1.creator = t2.userid",
            " left join users t3 on t1.updater = t3.userid",
            " where t1.tenant_id = #{tenantId} and t1.source_id=#{sourceId}  and t1.db_id=#{schemaId} and t1.table_name_en=#{tableName}",
            "</script>"})
    List<SourceInfoDeriveTableInfo> getDeriveTableByIdAndTenantId( @Param("tenantId")String tenantId, @Param("sourceId")String sourceId,
                                                                   @Param("schemaId") String schemaId,@Param("tableName") String tableName );

    @Select({"<script>",
            " select t1.id as id, ",
            " t1.table_guid as tableGuid, t1.table_name_en as tableNameEn, t1.table_name_zh as tableNameZh,",
            " t1.procedure as procedure, t1.category_id as categoryId, t1.db_type as dbType, ",
            " t1.business_id as businessId, t1.db_id as dbId, t1.source_id as sourceId,",
            " t1.update_frequency as updateFrequency, t1.etl_policy as etlPolicy, ",
            " t1.incre_standard as increStandard, t1.clean_rule as cleanRule, t1.filter as filter,",
            " t1.tenant_id as tenantId, t1.remark as remark, t1.version as version,",
            " t1.source_table_guid as sourceTableGuid, t1.operator as operator, t1.file_name as fileName, t1.file_path as filePath,",
            " t1.incremental_field as incrementalField, t1.creator as creator, cast(t1.create_time as varchar) as createTimeStr,",
            " t1.updater as updater, cast(t1.update_time as varchar) as updateTimeStr, t1.ddl as ddl, t1.dml as dml, t1.state as state,",
            " (select t2.username from users t2 where t2.userid= t1.creator limit 1)  as creatorName ",
            " from source_info_derive_table_info t1",
            " where  t1.source_id=#{sourceId} and t1.table_guid=#{tableGuid}",
            "</script>"})
    List<SourceInfoDeriveTableInfo> getDeriveTableByGuid(@Param("sourceId")String sourceId,
                                                         @Param("tableGuid") String tableGuid );
    @Select("SELECT " +
            "id, importance, security " +
            "FROM " +
            "source_info_derive_table_info " +
            "WHERE " +
            "table_guid = #{tableGuid} " +
            "AND version=-1 LIMIT 1")
    SourceInfoDeriveTableInfo getByNameAndDbGuid(@Param("tableGuid") String tableGuid, @Param("tenantId") String tenantId);

    @Select("select importance,security from source_info_derive_table_info where table_guid=#{tableGuid} and version=-1")
    List<TableExtInfo> getImportanceInfo( @Param("tableGuid") String tableGuid,@Param("tenantId") String tenantId);

    @Select("<script>" +
            "select business_id businessId, table_guid tableGuid, source_id sourceId " +
            "from source_info_derive_table_info " +
            "where state=1 and table_guid in " +
            " <foreach item='tableGuid' index='index' collection='tableGuids' separator=',' open='(' close=')'>" +
            " #{tableGuid}" +
            " </foreach>" +
            "</script>")
    List<SourceInfoDeriveTableInfo> getDeriveTableInfoByGuids(@Param("tableGuids")List<String> tableGuids);

    @Select("SELECT * FROM source_info_derive_table_info WHERE db_id = #{dbId} AND table_name_en = #{tableName} and version=-1")
    SourceInfoDeriveTableInfo selectByDbAndTableName(@Param("dbId") String dbId, @Param("tableName") String tableName);

    @Update("update source_info_derive_table_info SET table_guid = #{tableGuid},update_time = now() WHERE db_id = #{dbId} AND table_name_en = #{tableName}")
    int updateByDbAndTableName(@Param("dbId") String dbId, @Param("tableName") String tableName, @Param("tableGuid") String tableGuid);

    @Select("SELECT * FROM source_info_derive_table_info WHERE table_guid = #{tableGuid} and version=-1")
    SourceInfoDeriveTableInfo selectByTableGuid(@Param("tableGuid") String table_guid);

    @Update("update source_info_derive_table_info set table_name_en = #{tableName},update_time = now(), " +
            "ddl=#{ddl}, dml=#{dml} " +
            "WHERE table_guid = #{tableGuid}")
    int updateByTableGuid(@Param("tableGuid") String table_guid, @Param("tableName") String tableName, @Param("ddl") String ddl, @Param("dml") String dml);
}
