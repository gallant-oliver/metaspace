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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/5/7 13:43
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.share.Organization;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/5/7 13:43
 */
public interface OrganizationDAO {

    @Insert({"<script>insert into organization values",
             "<foreach item='item' index='index' collection='organizations'",
             "open=' ' separator=',' close=' '>",
             "(#{item.checked},#{item.disable},#{item.id},#{item.isOpen},#{item.isVm},#{item.name},",
             " #{item.open},#{item.pId},#{item.pkid},#{item.ptype},#{item.type},#{item.updateTime})",
             "</foreach>",
             "</script>"})
    public int addOrganizations(@Param("organizations") List<Organization> organizations);

    @Delete("delete from organization")
    public int deleteOrganization();

    @Select({" <script>",
             " select * from organization where pId=#{pId}",
            " <if test=\"query != null and query!=''\">",
             " and name like '%${query}%' ESCAPE '/'",
             " </if>",
             " order by name",
             " <if test='limit!=null and limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " <if test='offset!=null'>",
             " offset #{offset}",
             " </if>",
             " </script>"})
    public List<Organization> getOrganizationByPid(@Param("pId")String pId, @Param("query")String query, @Param("limit")Integer limit, @Param("offset")Integer offset);

    @Select({" <script>",
             " select count(*) from organization where pId=#{pId}",
             " <if test=\"query != null and query!=''\">",
             " and name like '%${query}%' ESCAPE '/'",
             " </if>",
             " </script>"})
    public long countOrganizationByPid(@Param("pId")String pId, @Param("query")String query);

    @Select({" <script>",
             " select * from organization",
             " <if test=\"query != null and query!=''\">",
             " where name like '%${query}%' ESCAPE '/'",
             " </if>",
             " order by name",
             " <if test='limit!=null and limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " <if test='offset!=null'>",
             " offset #{offset}",
             " </if>",
             " </script>"})
    public List<Organization> getOrganizationByName(@Param("query")String query, @Param("limit")Integer limit, @Param("offset")Integer offset);

    @Select({" <script>",
             " select count(1) from organization where name like '%'||#{query}||'%' ESCAPE '/'",
             " </script>"})
    public long countOrganizationByName(@Param("query")String query);

    @Select({"WITH RECURSIVE T(id, name, pid, PATH, DEPTH)  AS" +
            "(SELECT id,name,pid, ARRAY[name] AS PATH, 1 AS DEPTH " +
            "FROM organization WHERE pid=#{pId} " +
            "UNION ALL " +
            "SELECT D.id, D.name, D.pid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH " +
            "FROM organization D JOIN T ON D.pid = T.id) " +
            "SELECT  PATH FROM T WHERE id=#{id} " +
            "ORDER BY PATH"})
    public String getPathById(@Param("pId")String pId, @Param("id")String id);
}
