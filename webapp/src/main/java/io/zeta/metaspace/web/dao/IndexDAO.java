package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.dto.indices.IndexDTO;
import io.zeta.metaspace.model.dto.indices.PageQueryDTO;
import io.zeta.metaspace.model.modifiermanage.Qualifier;
import io.zeta.metaspace.model.po.indices.*;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.util.List;

public interface IndexDAO {

    /**
     * 添加原子指标
     */
    @Insert("insert into index_atomic_info(index_id, index_name, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, source_id,db_name, table_id, column_id, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time, update_time) " +
            "values(#{iap.indexId},#{iap.indexName},#{iap.indexIdentification},#{iap.description},#{iap.central},#{iap.indexFieldId},#{iap.tenantId},#{iap.approvalGroupId},#{iap.indexState},#{iap.version},#{iap.sourceId},#{iap.dbName},#{iap.tableId},#{iap.columnId},#{iap.businessCaliber},#{iap.businessLeader},#{iap.technicalCaliber},#{iap.technicalLeader},#{iap.creator},#{iap.createTime},#{iap.updateTime})")
    void addAtomicIndex(@Param("iap") IndexAtomicPO iap) throws SQLException;
    /**
     *添加派生指标
     */
    @Insert("insert into index_derive_info(index_id, index_atomic_id, time_limit_id, index_name, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time, update_time) " +
            "values(#{idp.indexId},#{idp.indexAtomicId},#{idp.timeLimitId},#{idp.indexName},#{idp.indexIdentification},#{idp.description},#{idp.central},#{idp.indexFieldId},#{idp.tenantId},#{idp.approvalGroupId},#{idp.indexState},#{idp.version},#{idp.businessCaliber},#{idp.businessLeader},#{idp.technicalCaliber},#{idp.technicalLeader},#{idp.creator},#{idp.createTime},#{idp.updateTime})")
    void addDeriveIndex(@Param("idp") IndexDerivePO idp) throws SQLException;
    /**
     *添加复合指标
     */
    @Insert("insert into index_composite_info(index_id, index_name, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, expression, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time, update_time) " +
            "values(#{icp.indexId},#{icp.indexName},#{icp.indexIdentification},#{icp.description},#{icp.central},#{icp.indexFieldId},#{icp.tenantId},#{icp.approvalGroupId},#{icp.indexState},#{icp.version},#{icp.expression},#{icp.businessCaliber},#{icp.businessLeader},#{icp.technicalCaliber},#{icp.technicalLeader},#{icp.creator},#{icp.createTime},#{icp.updateTime})")
    void addCompositeIndex(@Param("icp") IndexCompositePO icp) throws SQLException;
    /**
     *添加派生指标与修饰词关系
     */
    @Update({" <script>",
            " insert into index_derive_modifier_relation(derive_index_id, modifier_id)values",
            " <foreach item='idmrPO' index='index' collection='idmrPOS' separator=',' close=';'>",
            " (#{idmrPO.deriveIndexId},#{idmrPO.modifierId})",
            " </foreach>",
            " </script>"})
    void addDeriveModifierRelations(@Param("idmrPOS") List<IndexDeriveModifierRelationPO> idmrPOS) throws SQLException;
    /**
     *添加派生指标与复合指标关系
     */
    @Update({" <script>",
            " insert into index_derive_composite_relation(derive_index_id, composite_index_id)values",
            " <foreach item='idcrPO' index='index' collection='idcrPOS' separator=',' close=';'>",
            " (#{idcrPO.deriveIndexId},#{idcrPO.compositeIndexId})",
            " </foreach>",
            " </script>"})
    void addDeriveCompositeRelations(@Param("idcrPOS")List<IndexDeriveCompositeRelationPO> idcrPOS) throws SQLException;
    /**
     *校验原子指标名称或者标识是否已存在
     */
    @Select({"<script>" ,
            " select * from index_atomic_info where tenant_id=#{tenantId} and (index_name=#{indexDTO.indexName} or index_identification=#{indexDTO.indexIdentification})",
            " <if test=\"indexDTO.indexId != null and indexDTO.indexId !=''\">",
            " and index_id != #{indexDTO.indexId} ",
            " </if>",
            "</script>"})
    IndexAtomicPO getAtomicIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexDTO") IndexDTO indexDTO);
    /**
     *校验派生指标名称或者标识是否已存在
     */
    @Select({"<script>" ,
            " select * from index_derive_info where tenant_id=#{tenantId} and (index_name=#{indexDTO.indexName} or index_identification=#{indexDTO.indexIdentification})",
            " <if test=\"indexDTO.indexId != null and indexDTO.indexId !=''\">",
            " and index_id != #{indexDTO.indexId} ",
            " </if>",
            "</script>"})
    IndexAtomicPO getDeriveIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexDTO") IndexDTO indexDTO);
    /**
     *校验复合指标名称或者标识是否已存在
     */
    @Select({"<script>" ,
            " select * from index_composite_info where tenant_id=#{tenantId} and (index_name=#{indexDTO.indexName} or index_identification=#{indexDTO.indexIdentification})",
            " <if test=\"indexDTO.indexId != null and indexDTO.indexId !=''\">",
            " and index_id != #{indexDTO.indexId} ",
            " </if>",
            "</script>"})
    IndexAtomicPO getCompositeIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexDTO") IndexDTO indexDTO);

    /**
     * 编辑原子指标
     */
    @Update("update index_atomic_info set index_name=#{iap.indexName}, index_identification=#{iap.indexIdentification}, description=#{iap.description}, central=#{iap.central}, " +
            "index_field_id=#{iap.indexFieldId}, approval_group_id=#{iap.approvalGroupId}, source_id=#{iap.sourceId}, " +
            "db_name=#{iap.dbName}, table_id=#{iap.tableId}, column_id=#{iap.columnId}, business_caliber=#{iap.businessCaliber}, business_leader=#{iap.businessLeader}, " +
            "technical_caliber=#{iap.technicalCaliber}, technical_leader=#{iap.technicalLeader}, updater=#{iap.updater}, update_time=#{iap.updateTime} where index_id=#{iap.indexId} and version=#{iap.version}")
    int editAtomicIndex(@Param("iap") IndexAtomicPO iap);

    /**
     *根据派生指标id获取派生指标与修饰词关系
     */
    @Select("select * from index_derive_modifier_relation where derive_index_id=#{indexId}")
    List<IndexDeriveModifierRelationPO> getDeriveModifierRelations(@Param("indexId") String indexId);
    /**
     *根据复合指标id获取复合指标与派生指标关系
     */
    @Select("select * from index_derive_composite_relation where composite_index_id=#{indexId}")
    List<IndexDeriveCompositeRelationPO> getDeriveCompositeRelations(@Param("indexId") String indexId);
    /**
     *编辑派生指标
     */
    @Update("update index_derive_info set index_atomic_id=#{idp.indexAtomicId},time_limit_id=#{idp.timeLimitId},index_name=#{idp.indexName}, index_identification=#{idp.indexIdentification}, description=#{idp.description}, central=#{idp.central}, " +
            "index_field_id=#{idp.indexFieldId}, approval_group_id=#{idp.approvalGroupId},  " +
            "business_caliber=#{idp.businessCaliber}, business_leader=#{idp.businessLeader}, " +
            "technical_caliber=#{idp.technicalCaliber}, technical_leader={idp.technicalLeader}, updater=#{idp.updater}, update_time=#{idp.updateTime} where index_id=#{idp.indexId} and version=#{idp.version")
    void editDerivIndex(@Param("idp") IndexDerivePO idp);
    /**
     *编辑复合指标
     */
    @Update("update index_derive_info set index_name=#{icp.indexName}, index_identification=#{icp.indexIdentification}, description=#{icp.description}, central=#{icp.central}, " +
            "index_field_id=#{icp.indexFieldId}, approval_group_id=#{icp.approvalGroupId}, expression=#{icp.expression}, " +
            "business_caliber=#{icp.businessCaliber}, business_leader=#{icp.businessLeader}, " +
            "technical_caliber=#{icp.technicalCaliber}, technical_leader={icp.technicalLeader}, updater=#{icp.updater}, update_time=#{icp.updateTime} where index_id=#{icp.indexId} and version=#{icp.version")
    void editCompositeIndex(@Param("icp") IndexCompositePO icp);
    /**
     *删除派生指标的其所有修饰词
     */
    @Delete("delete from index_derive_modifier_relation where derive_index_id=#{deriveIndexId}")
    void deleteDeriveModifierRelationsByDeriveId(@Param("deriveIndexId") String deriveIndexId);
    /**
     *根据派生指标id与修饰词id进行部分删除
     */
    @Delete({" <script>",
            " delete from index_derive_modifier_relation where derive_index_id=#{deriveIndexId} and modifier_id in",
            " <foreach item='delId' index='index' collection='delIds' separator=',' open='(' close=')'>",
            " #{delId} ",
            " </foreach>",
            " </script>"})
    void deleteDeriveModifierRelationsByDeriveModifierId(@Param("deriveIndexId") String deriveIndexId,@Param("delIds") List<String> delIds);

    /**
     * 删除复合指标的所有派生指标
     */
    @Delete("delete from index_derive_composite_relation where composite_index_id=#{compositeIndexId}")
    void deleteDeriveCompositeRelationsByDeriveId(@Param("compositeIndexId") String compositeIndexId);

    /**
     * 根据复合指标id和派生指标id进行部分删除
     */
    @Delete({" <script>",
            " delete from index_derive_composite_relation where composite_index_id=#{compositeIndexId} and derive_index_id in",
            " <foreach item='delId' index='index' collection='delIds' separator=',' open='(' close=')'>",
            " #{delId} ",
            " </foreach>",
            " </script>"})
    void deleteDeriveCompositeRelationsByDeriveCompositeId(@Param("compositeIndexId") String compositeIndexId,@Param("delIds")  List<String> delIds);
    /**
     * 批量删除原子指标
     */
    @Delete({" <script>",
            " delete from index_atomic_info where index_id in",
            " <foreach item='delId' index='index' collection='delIds' separator=',' open='(' close=')'>",
            " #{delId} ",
            " </foreach>",
            " </script>"})
    void deleteAtomicIndices(@Param("delIds") List<String> delIds);
    /**
     * 批量删除派生指标
     */
    @Delete({" <script>",
            " delete from index_derive_info where index_id in",
            " <foreach item='delId' index='index' collection='delIds' separator=',' open='(' close=')'>",
            " #{delId} ",
            " </foreach>",
            " </script>"})
    void deleteDeriveIndices(@Param("delIds") List<String> delIds);
    /**
     * 批量删除复合指标
     */
    @Delete({" <script>",
            " delete from index_composite_info where index_id in",
            " <foreach item='delId' index='index' collection='delIds' separator=',' open='(' close=')'>",
            " #{delId} ",
            " </foreach>",
            " </script>"})
    void deleteCompositeIndices(@Param("delIds") List<String> delIds);

    @Select({" <script>",
            " select * from index_atomic_info where tenant_id=#{tenantId} and index_state=#{indexState} and index_field_id in ",
            " <foreach item='indexFieldId' index='index' collection='indexFieldIds' separator=',' open='(' close=')'>",
            " #{indexFieldId} ",
            " </foreach>",
            " order by update_time desc ",
            " </script>"})
    List<IndexAtomicPO> getAtomicByIndexFields(@Param("indexFieldIds") List<String> indexFieldIds, @Param("tenantId") String tenantId, @Param("indexState") int indexState);
    @Select({" <script>",
            " select * from index_derive_info where tenant_id=#{tenantId} and index_state=#{indexState} and index_field_id in ",
            " <foreach item='indexFieldId' index='index' collection='indexFieldIds' separator=',' open='(' close=')'>",
            " #{indexFieldId} ",
            " </foreach>",
            " order by update_time desc ",
            " </script>"})
    List<IndexDerivePO> getDeriveByIndexFields(@Param("indexFieldIds") List<String> indexFieldIds, @Param("tenantId") String tenantId, @Param("indexState") int indexState);
    @Select({" <script>",
            " select * from index_composite_info where tenant_id=#{tenantId} and index_state=#{indexState} and index_field_id in ",
            " <foreach item='indexFieldId' index='index' collection='indexFieldIds' separator=',' open='(' close=')'>",
            " #{indexFieldId} ",
            " </foreach>",
            " order by update_time desc ",
            " </script>"})
    List<IndexCompositePO> getCompositeByIndexFields(@Param("indexFieldIds") List<String> indexFieldIds, @Param("tenantId") String tenantId, @Param("indexState") int indexState);
    /**
     *根据指标域id删除原子指标
     */
    @Delete("delete from index_atomic_info where index_field_id=#{guid} and tenant_id=#{tenantId}")
    void deleteAtomicByIndexFieldId(@Param("guid") String guid,@Param("tenantId") String tenantId);
    /**
     *根据指标域id删除派生指标
     */
    @Delete("delete from index_derive_info where index_field_id=#{guid} and tenant_id=#{tenantId}")
    void deleteDeriveByIndexFieldId(@Param("guid") String guid,@Param("tenantId") String tenantId);
    /**
     *根据指标域id删除复合指标
     */
    @Delete("delete from index_composite_info where index_field_id=#{guid} and tenant_id=#{tenantId}")
    void deleteCompositeByIndexFieldId(@Param("guid") String guid,@Param("tenantId") String tenantId);
    @Update("update index_atomic_info set index_field_id=#{targetGuid} where index_field_id=#{sourceGuid} and  tenant_id=#{tenantId}")
    void updateAtomicIndexFieldId(@Param("sourceGuid") String sourceGuid,@Param("tenantId")  String tenantId,@Param("targetGuid")  String targetGuid);
    @Update("update index_derive_info set index_field_id=#{targetGuid} where index_field_id=#{sourceGuid} and  tenant_id=#{tenantId}")
    void updateDeriveIndexFieldId(@Param("sourceGuid") String sourceGuid,@Param("tenantId")  String tenantId,@Param("targetGuid")  String targetGuid);
    @Update("update index_composite_info set index_field_id=#{targetGuid} where index_field_id=#{sourceGuid} and  tenant_id=#{tenantId}")
    void updateCompositeIndexFieldId(@Param("sourceGuid") String sourceGuid,@Param("tenantId")  String tenantId,@Param("targetGuid")  String targetGuid);
    @Select({" <script>",
            " select iai.*,ca.name as indexFieldName,ag.name as approvalGroupName,ds.source_name as sourceName,ti.tablename as tableName,ci.column_name as columnName, " ,
            " bl.username as businessLeaderName,tl.username as technicalLeaderName, c.username as creatorName,u.username as updaterName,p.username as publisherName " ,
            " from index_atomic_info iai " ,
            " left join category ca on iai.index_field_id=ca.guid and ca.categorytype=#{categoryType} and ca.tenantid=#{tenantId} " ,
            " left join approval_group ag on iai.approval_group_id=ag.id   and ag.tenantid=#{tenantId}" ,
            " left join data_source ds on iai.source_id=ds.source_id  and ds.tenantid=#{tenantId}" ,
            " left join tableinfo ti on iai.table_id=ti.tableguid and iai.source_id= ti.source_id and iai.db_name=ti.dbname" ,
            " left join column_info ci on iai.column_id=ci.column_guid " ,
            " left join users bl on iai.business_leader=bl.userid " ,
            " left join users tl on iai.technical_leader=tl.userid " ,
            " left join users c on iai.creator=c.userid " ,
            " left join users u on iai.updater=u.userid " ,
            " left join users p on iai.publisher=p.userid " ,
            " where iai.index_id=#{indexId} and iai.version=#{version} ",
            " and iai.tenant_id=#{tenantId}",
            " </script>"})

    IndexInfoPO getAtomicIndexInfoPO(@Param("indexId")String indexId,@Param("version")int version,@Param("categoryType")int categoryType, @Param("tenantId")String tenantId);
    @Select({" <script>",
            " select iai.*,ca.name as indexFieldName,ag.name as approvalGroupName, t.name as timeLimitName," ,
            " bl.username as businessLeaderName,tl.username as technicalLeaderName, c.username as creatorName,u.username as updaterName,p.username as publisherName " ,
            " from index_derive_info iai " ,
            " left join time_limit t on iai.time_limit_id=t.id and t.tenantid=#{tenantId} " ,
            " left join category ca on iai.index_field_id=ca.guid and ca.categorytype=#{categoryType} and ca.tenantid=#{tenantId} " ,
            " left join approval_group ag on iai.approval_group_id=ag.id and ag.tenantid=#{tenantId} " ,
            " left join users bl on iai.business_leader=bl.userid " ,
            " left join users tl on iai.technical_leader=tl.userid " ,
            " left join users c on iai.creator=c.userid " ,
            " left join users u on iai.updater=u.userid " ,
            " left join users p on iai.publisher=p.userid " ,
            " where iai.index_id=#{indexId} and iai.version=#{version} ",
            " and iai.tenant_id=#{tenantId}   ",
            " </script>"})
    IndexInfoPO getDeriveIndexInfoPO(@Param("indexId")String indexId,@Param("version")int version,@Param("categoryType")int categoryType, @Param("tenantId")String tenantId);
    @Select({" <script>",
            " select iai.*,ca.name as indexFieldName,ag.name as approvalGroupName, " ,
            " bl.username as businessLeaderName,tl.username as technicalLeaderName, c.username as creatorName,u.username as updaterName,p.username as publisherName " ,
            " from index_composite_info iai " ,
            " left join category ca on iai.index_field_id=ca.guid and ca.categorytype=#{categoryType} and ca.tenantid=#{tenantId} " ,
            " left join approval_group ag on iai.approval_group_id=ag.id and ag.tenantid=#{tenantId} " ,
            " left join users bl on iai.business_leader=bl.userid " ,
            " left join users tl on iai.technical_leader=tl.userid " ,
            " left join users c on iai.creator=c.userid " ,
            " left join users u on iai.updater=u.userid " ,
            " left join users p on iai.publisher=p.userid " ,
            " where iai.index_id=#{indexId} and iai.version=#{version} ",
            " and iai.tenant_id=#{tenantId} ",
            " </script>"})
    IndexInfoPO getCompositeIndexInfoPO(@Param("indexId")String indexId,@Param("version")int version,@Param("categoryType")int categoryType, @Param("tenantId")String tenantId);

    @Select({" <script>",
            " select count(1)over() total,iai.*,ca.name as indexFieldName,ag.name as approvalGroupName,ds.source_name as sourceName,ti.tablename as tableName,ci.column_name as columnName, " ,
            " bl.username as businessLeaderName,tl.username as technicalLeaderName, c.username as creatorName,u.username as updaterName,p.username as publisherName" ,
            " from index_atomic_info iai " ,
            " left join category ca on iai.index_field_id=ca.guid and ca.categorytype=#{categoryType} and ca.tenantid=#{tenantId} " ,
            " left join approval_group ag on iai.approval_group_id=ag.id and ag.tenantid=#{tenantId} " ,
            " left join data_source ds on iai.source_id=ds.source_id and ds.tenantid=#{tenantId} " ,
            " left join tableinfo ti on iai.table_id=ti.tableguid and iai.source_id= ti.source_id and iai.db_name=ti.dbname" ,
            " left join column_info ci on iai.column_id=ci.column_guid " ,
            " left join users bl on iai.business_leader=bl.userid " ,
            " left join users tl on iai.technical_leader=tl.userid " ,
            " left join users c on iai.creator=c.userid " ,
            " left join users u on iai.updater=u.userid " ,
            " left join users p on iai.publisher=p.userid " ,
            " where iai.index_id=#{indexId} ",
            " and iai.tenant_id=#{tenantId} ",
            " and iai.index_state in (2,3) ",
            " order by iai.version ",
            " limit #{limit}",
            " offset #{offset}",
            " </script>"})
    List<IndexInfoPO> getAtomicIndexHistory(@Param("indexId")String indexId,@Param("categoryType")int categoryType,@Param("offset")int offset,@Param("limit")int limit, @Param("tenantId")String tenantId);
    @Select({" <script>",
            " select count(1)over() total,iai.*,ca.name as indexFieldName,ag.name as approvalGroupName, t.name as timeLimitName, " ,
            " bl.username as businessLeaderName,tl.username as technicalLeaderName, c.username as creatorName,u.username as updaterName,p.username as publisherName " ,
            " from index_derive_info iai " ,
            " left join time_limit t on iai.time_limit_id=t.id and t.tenantid=#{tenantId} " ,
            " left join category ca on iai.index_field_id=ca.guid and ca.categorytype=#{categoryType} and ca.tenantid=#{tenantId} " ,
            " left join approval_group ag on iai.approval_group_id=ag.id and ag.tenantid=#{tenantId} " ,
            " left join users bl on iai.business_leader=bl.userid " ,
            " left join users tl on iai.technical_leader=tl.userid " ,
            " left join users c on iai.creator=c.userid " ,
            " left join users u on iai.updater=u.userid " ,
            " left join users p on iai.publisher=p.userid " ,
            " where iai.index_id=#{indexId} ",
            " and iai.tenant_id=#{tenantId}  ",
            " and iai.index_state in (2,3) ",
            " order by iai.version ",
            " limit #{limit}",
            " offset #{offset}",
            " </script>"})
    List<IndexInfoPO> getDeriveIndexHistory(@Param("indexId")String indexId,@Param("categoryType")int categoryType,@Param("offset")int offset,@Param("limit")int limit, @Param("tenantId")String tenantId);
    @Select({" <script>",
            " select count(1)over() total,iai.*,ca.name as indexFieldName,ag.name as approvalGroupName, " ,
            " bl.username as businessLeaderName,tl.username as technicalLeaderName, c.username as creatorName,u.username as updaterName,p.username as publisherName " ,
            " from index_composite_info iai " ,
            " left join category ca on iai.index_field_id=ca.guid and ca.categorytype=#{categoryType} and ca.tenantid=#{tenantId} " ,
            " left join approval_group ag on iai.approval_group_id=ag.id and ag.tenantid=#{tenantId} " ,
            " left join users bl on iai.business_leader=bl.userid " ,
            " left join users tl on iai.technical_leader=tl.userid " ,
            " left join users c on iai.creator=c.userid " ,
            " left join users u on iai.updater=u.userid " ,
            " left join users p on iai.publisher=p.userid " ,
            " where iai.index_id=#{indexId} ",
            " and iai.tenant_id=#{tenantId} ",
            " and iai.index_state in (2,3) ",
            " order by iai.version ",
            " limit #{limit}",
            " offset #{offset}",
            " </script>"})
    List<IndexInfoPO> getCompositeIndexHistory(@Param("indexId")String indexId,@Param("categoryType")int categoryType,@Param("offset")int offset,@Param("limit")int limit, @Param("tenantId")String tenantId);

    /**
     *获取依赖的原子指标
     */
    @Select("select * from index_atomic_info iai where iai.index_id=#{indexAtomicId} and iai.tenant_id=#{tenantId} order by iai.version desc limit 1 ")
    List<IndexAtomicPO> getDependentAtomicIndex(@Param("indexAtomicId")String indexAtomicId, @Param("tenantId")String tenantId);

    /**
     * 获取派生指标所依赖的修饰词
     */
    @Select({" <script>",
            " select * from qualifier where tenantid=#{tenantId} and id in " ,
            " ( " ,
            " select  modifier_id from index_derive_modifier_relation where derive_index_id=#{indexId}" ,
            " ) " ,
            " </script>"})
    List<Qualifier> getModifiers(@Param("indexId")String indexId,@Param("tenantId") String tenantId);
    /**
     * 复合指标依赖的派生指标
     */
    @Select({" <script>",
            " select * from index_derive_info where tenant_id=#{tenantId} and index_id in " ,
            " ( " ,
            " select  derive_index_id from index_derive_composite_relation where composite_index_id=#{indexId}" ,
            " ) " ,
            " </script>"})
    List<IndexDerivePO> getDependentDeriveIndex(@Param("indexId")String indexId, @Param("tenantId")String tenantId);

    @Select({" <script>",
            " select * from index_atomic_info as a where tenant_id=#{tenantId} ",
            " and version=( select max(b.version) from index_atomic_info as b where a.index_id=b.index_id) ",
            " and  index_id in ",
            " <foreach item='indexId' index='index' collection='indexIds' separator=',' open='(' close=')'>",
            " #{indexId} ",
            " </foreach> ",
            " </script>"})
    List<IndexAtomicPO> getAtomicIndexInfoPOs(@Param("indexIds")List<String> indexIds, @Param("tenantId")String tenantId);
    @Update("update index_atomic_info set index_state=#{state} " +
            " where index_id=#{indexId} and version=#{version} and tenant_id=#{tenantId} ")
    void editAtomicState(@Param("indexId")String indexId,@Param("version") int version,@Param("tenantId") String tenantId,@Param("state") int state);
    @Update("update index_derive_info set index_state=#{state} " +
            " where index_id=#{indexId} and version=#{version} and tenant_id=#{tenantId} ")
    void editDeriveState(@Param("indexId")String indexId,@Param("version") int version,@Param("tenantId") String tenantId,@Param("state") int state);
    @Update("update index_composite_info set index_state=#{state} " +
            " where index_id=#{indexId} and version=#{version} and tenant_id=#{tenantId} ")
    void editCompositeState(@Param("indexId")String indexId,@Param("version") int version,@Param("tenantId") String tenantId,@Param("state") int state);
    @Select(" select * from index_atomic_info where index_id=#{indexId} and version=#{version} and tenant_id=#{tenantId}")
    IndexAtomicPO getAtomicIndexPO(@Param("indexId")String indexId, @Param("version")int version, @Param("tenantId")String tenantId);
    @Select(" select * from index_derive_info where index_id=#{indexId} and version=#{version} and tenant_id=#{tenantId}")
    IndexDerivePO getDeriveIndexPO(@Param("indexId")String indexId, @Param("version")int version, @Param("tenantId")String tenantId);
    @Select(" select * from index_composite_info where index_id=#{indexId} and version=#{version} and tenant_id=#{tenantId}")
    IndexCompositePO getCompositeIndexPO(@Param("indexId")String indexId, @Param("version")int version, @Param("tenantId")String tenantId);

    @Select({"<script>" ,
            " <if test='pageQueryDTO.indexType == 1'>",
            " select count(1)over() total,T.*,bl.username as businessLeaderName, c.username as creatorName,u.username as updaterName,ca.name as indexFieldName from ",
            " (select index_id, index_name,1 as indexType, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time,updater, update_time ",
            " ,row_number() over(partition by index_id order by version desc) as rn ",
            " from index_atomic_info) T ",
            " </if>",
            " <if test='pageQueryDTO.indexType == 2'>",
            " select count(1)over() total,T.*,bl.username as businessLeaderName, c.username as creatorName,u.username as updaterName,ca.name as indexFieldName from ",
            " (select index_id, index_name,2 as indexType, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time,updater, update_time ",
            " ,row_number() over(partition by index_id order by version desc) as rn ",
            " from index_derive_info) T ",
            " </if>",
            " <if test='pageQueryDTO.indexType == 3'>",
            " select count(1)over() total,T.*,bl.username as businessLeaderName, c.username as creatorName,u.username as updaterName,ca.name as indexFieldName from ",
            " (select index_id, index_name,3 as indexType, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time,updater, update_time ",
            " ,row_number() over(partition by index_id order by version desc) as rn ",
            " from index_composite_info) T ",
            " </if>",
            " <if test='pageQueryDTO.indexType == 4'>",
            " WITH RECURSIVE T (index_id, index_name,indexType, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time,updater, update_time,rn) AS ",
            " (",
            " select index_id, index_name,1 as indexType, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time,updater, update_time ",
            " ,row_number() over(partition by index_id order by version desc) as rn ",
            " from index_atomic_info where  tenant_id=#{tenantId} and index_field_id in ",
            " <foreach item='indexFieldId' index='index' collection='pageQueryDTO.indexFieldIds' separator=',' open='(' close=')'>",
            " #{indexFieldId} ",
            " </foreach> ",
            " UNION  ",
            " select index_id, index_name,2 as indexType, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time,updater, update_time ",
            " ,row_number() over(partition by index_id order by version desc) as rn ",
            " from index_derive_info where  tenant_id=#{tenantId} and index_field_id in ",
            " <foreach item='indexFieldId' index='index' collection='pageQueryDTO.indexFieldIds' separator=',' open='(' close=')'>",
            " #{indexFieldId} ",
            " </foreach> ",
            " UNION  ",
            " select index_id, index_name,3 as indexType, index_identification, description, central, index_field_id, tenant_id, approval_group_id, index_state, version, business_caliber, business_leader, technical_caliber, technical_leader, creator, create_time,updater, update_time ",
            " ,row_number() over(partition by index_id order by version desc) as rn ",
            " from index_composite_info where tenant_id=#{tenantId} and index_field_id in ",
            " <foreach item='indexFieldId' index='index' collection='pageQueryDTO.indexFieldIds' separator=',' open='(' close=')'>",
            " #{indexFieldId} ",
            " </foreach> ",
            " ) ",
            " select count(1)over() total,T.*,bl.username as businessLeaderName, c.username as creatorName,u.username as updaterName,ca.name as indexFieldName from T ",
            " </if>",
            " left join category ca on T.index_field_id=ca.guid and ca.categorytype=#{categoryType} and ca.tenantid=#{tenantId} " ,
            " left join users bl on T.business_leader=bl.userid " ,
            " left join users c on T.creator=c.userid " ,
            " left join users u on T.updater=u.userid " ,
            " where T.tenant_id=#{tenantId} and T.rn=1 and T.index_field_id in  ",
            " <foreach item='indexFieldId' index='index' collection='pageQueryDTO.indexFieldIds' separator=',' open='(' close=')'>",
            " #{indexFieldId} ",
            " </foreach> ",
            " <if test='pageQueryDTO.startTime != null'>",
            " and T.update_time>=#{pageQueryDTO.startTime}",
            " </if>",
            " <if test='pageQueryDTO.endTime != null'>",
            " and T.update_time &lt;= #{pageQueryDTO.endTime}",
            " </if>",
            " <if test='pageQueryDTO.central == true'>",
            " and T.central=#{pageQueryDTO.central}",
            " </if>",
            " <if test=\"pageQueryDTO.searchContent != null and pageQueryDTO.searchContent !=''\">",
            " and (T.index_name like '%${pageQueryDTO.searchContent}%' or T.index_identification like '%${pageQueryDTO.searchContent}%' )",
            " </if>",
            " <if test='pageQueryDTO.indexStates != null and pageQueryDTO.indexStates.size > 0'>",
            " and T.index_state in ",
            " <foreach item='indexState' index='index' collection='pageQueryDTO.indexStates' separator=',' open='(' close=')'>",
            " #{indexState} ",
            " </foreach> ",
            " </if>",
            " <if test=\"pageQueryDTO.order == 'asc'.toString() \">",
            " order by T.update_time asc ",
            " </if>",
            " <if test=\"pageQueryDTO.order == 'desc'.toString()\">",
            " order by T.update_time desc ",
            " </if>",
            " limit #{pageQueryDTO.limit}",
            " offset #{pageQueryDTO.offset}",
            "</script>"})
    List<IndexInfoPO> pageQuery(@Param("pageQueryDTO")PageQueryDTO pageQueryDTO,@Param("categoryType") int categoryType,@Param("tenantId") String tenantId) throws Exception;


    @Update({" <script>",
            " <if test=\"approveItem.businessType == '1'.toString()\">",
            " update index_atomic_info  ",
            " </if>",
            " <if test=\"approveItem.businessType == '2'.toString()\">",
            " update index_derive_info  ",
            " </if>",
            " <if test=\"approveItem.businessType == '3'.toString()\">",
            " update index_composite_info  ",
            " </if>",
            " set index_state=#{state},publisher=#{approveItem.submitter},publish_time=#{approveItem.commitTime} ",
            " where  index_id=#{approveItem.objectId} and version=#{approveItem.version} and tenant_id=#{tenantId} ",
            " </script>"})
    void updatePublishInfo(@Param("approveItem")ApproveItem approveItem, @Param("tenantId")String tenantId,@Param("state") int state);
}
