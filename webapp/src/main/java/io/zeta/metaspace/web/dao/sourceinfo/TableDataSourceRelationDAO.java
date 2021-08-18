package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.po.sourceinfo.TableDataSourceRelationPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TableDataSourceRelationDAO {

    @Insert("<script>" +
            " INSERT INTO table_data_source_relation (id,category_id,table_id,data_source_id,create_time,tenant_id,update_time) values" +
            " <foreach item='item' index='index' collection='list' separator=','> " +
            "   ( #{item.id}, #{item.categoryId}, #{item.tableId}, #{item.dataSourceId}, NOW(), #{item.tenantId}, NOW())" +
            " </foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<TableDataSourceRelationPO> list);

    @Delete("<script>" +
            " delete from table_data_source_relation WHERE tenant_id = #{tenantId} AND category_id in" +
            " <foreach item='id' index='index' collection='list' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            "</script>")
    int deleteByCategoryIdList(@Param("tenantId") String tenantId, @Param("list") List<String> categoryIdList);
}
