package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WarningGroupDAO {

    @Insert({" insert into warning_group(id,name,type,contacts,category_id,description,create_time,update_time,creator,delete) ",
             " values(#{id},#{name},#{type},#{contacts},#{categoryId},#{description},#{createTime},#{updateTime},#{creator},#{delete})"})
    public int insert(WarningGroup warningGroup);

    @Insert(" update warning_group set name=#{name},type=#{type},contacts=#{contacts},category_id=#{categoryId},description=#{description},update_time=#{updateTime}")
    public int update(WarningGroup warningGroup);

    @Select({" select a.id,a.name,a.type,a.contacts,a.category_id as categoryId,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false and a.id=#{id}"})
    public WarningGroup getById(@Param("id") String id);

    @Select({" select a.id,a.name,a.type,a.contacts,a.category_id as categoryId,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false and a.name = #{name} "})
    public WarningGroup getByName(@Param("name") String name);

    @Select("update warning_group set delete=true where id=#{id}")
    public void deleteById(@Param("id") String id);

    @Insert({" <script>",
             " update warning_group set delete=true where id in ",
             " <foreach collection='idList' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public void deleteByIdList(@Param("idList") List<String> idList);


    @Select({"<script>",
             " select a.id,a.name,a.type,a.contacts,a.category_id as categoryId,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false ",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (a.name like '%${params.query}%' ESCAPE '/' ) ",
             " </if>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<WarningGroup> search(@Param("params") Parameters params);

    @Select({"<script>",
             " select count(1) ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false ",
             " <if test=\"query != null and query!=''\">",
             " and (a.name like '%${query}%' ESCAPE '/' ) ",
             " </if>",
             " </script>"})
    public long countBySearch(@Param("query") String query);

}
