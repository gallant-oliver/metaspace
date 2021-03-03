package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.po.indices.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
            " insert into index_derive_modifier_relation(derive_index_id, modifier_id)values",
            " <foreach item='idmrPO' index='index' collection='idmrPOS' separator=',' close=';'>",
            " (#{idmrPO.deriveIndexId},#{idmrPO.modifierId})",
            " </foreach>",
            " </script>"})
    void addDeriveCompositeRelations(@Param("idcrPOS")List<IndexDeriveCompositeRelationPO> idcrPOS) throws SQLException;
    /**
     *根据名称或者标识查询原子指标
     */
    @Select("select * from index_atomic_info where tenant_id=#{tenantId} and (index_name=#{indexName} or index_identification=#{indexIdentification})")
    IndexAtomicPO getAtomicIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexName")String indexName, @Param("indexIdentification")String indexIdentification);
    /**
     *根据名称或者标识查询派生指标
     */
    @Select("select * from index_derive_info where tenant_id=#{tenantId} and (index_name=#{indexName} or index_identification=#{indexIdentification})")
    IndexAtomicPO getDeriveIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexName")String indexName, @Param("indexIdentification")String indexIdentification);
    /**
     *根据名称或者标识查询复合指标
     */
    @Select("select * from index_composite_info where tenant_id=#{tenantId} and (index_name=#{indexName} or index_identification=#{indexIdentification})")
    IndexAtomicPO getCompositeIndexByNameOrIdentification(@Param("tenantId")String tenantId, @Param("indexName")String indexName, @Param("indexIdentification")String indexIdentification);
}
