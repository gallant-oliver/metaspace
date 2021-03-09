package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dto.indices.IndexDTO;
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
            " <if test='indexDTO.indexId != null'>",
            " and index_id != #{indexDTO.indexId} ",
            " </if>",
            "</script>"})
    IndexAtomicPO getAtomicIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexDTO") IndexDTO indexDTO);
    /**
     *校验派生指标名称或者标识是否已存在
     */
    @Select({"<script>" ,
            " select * from index_derive_info where tenant_id=#{tenantId} and (index_name=#{indexDTO.indexName} or index_identification=#{indexDTO.indexIdentification})",
            " <if test='indexDTO.indexId != null'>",
            " and index_id != #{indexDTO.indexId} ",
            " </if>",
            "</script>"})
    IndexAtomicPO getDeriveIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexDTO") IndexDTO indexDTO);
    /**
     *校验复合指标名称或者标识是否已存在
     */
    @Select({"<script>" ,
            " select * from index_composite_info where tenant_id=#{tenantId} and (index_name=#{indexDTO.indexName} or index_identification=#{indexDTO.indexIdentification})",
            " <if test='indexDTO.indexId != null'>",
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
            "technical_caliber=#{iap.technicalCaliber}, technical_leader={iap.technicalLeader}, updater=#{iap.updater}, update_time=#{iap.updateTime} where index_id=#{iap.indexId} and version=#{iap.version}")
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
}
