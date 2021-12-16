// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.security.Tenant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @author lixiang03
 * @Data 2020/3/25 15:22
 */
public interface TenantDAO {
    @Select("select id from tenant ")
    List<String> getAllTenantId();

    @Insert("<script>"+
            " insert into tenant(id,name) values " +
            " <foreach collection='tenants' item='tenant' index='index' separator='),(' open='(' close=')'>" +
            " #{tenant.tenantId},#{tenant.projectName}"+
            " </foreach> " +
            " </script>")
    int addTenants(@Param("tenants") List<Tenant> tenants);

    @Select("SELECT name FROM tenant WHERE id = #{id}")
    String selectNameById(String id);

    @Select("<script>" +
            "select DISTINCT id as tenantId,name as projectName from tenant where id in " +
            " <foreach item='item' index='index' collection='list' separator=',' open='(' close=')'>" +
            "   #{item} " +
            " </foreach>" +
            "</script>")
    List<Tenant> selectListByTenantId(@Param("list") Set<String> list);

    @Select("select DISTINCT id as tenantId,name as projectName from tenant")
    List<Tenant> selectAll();
}
