package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.modifiermanage.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.*;
import org.stringtemplate.v4.ST;

import java.util.List;

public interface QualifierDAO {
    //批量添加修饰词
    @Insert("<script>" +
            " insert into qualifier(id,name,mark,creator,create_time,update_user,update_time,\"desc\",tenantId,typeId) values " +
            " <foreach item='qualifier' index='index' collection='qualifiers' separator='),(' open='(' close=')'>" +
            " #{qualifier.id},#{qualifier.name},#{qualifier.mark},#{qualifier.creator},#{qualifier.createTime},#{qualifier.updateUser},#{qualifier.updateTime},#{qualifier.desc},#{tenantId},#{qualifier.typeId} " +
            " </foreach>" +
            " </script>")
    public int saveQualifier(@Param("qualifiers") List<Qualifier> qualifiers, @Param("tenantId") String tenantId);

    //判断批量添加修饰词标识是否重复
    @Select("<script>" +
            " select a.id " +
            " from qualifier a " +
            " where a.tenantid=#{tenantId} and a.typeid=#{typeId} and a.mark in " +
            " <foreach item='mark' index='index' collection='markList' separator=',' open='(' close=')'> " +
            " #{mark} " +
            " </foreach>" +
            " </script>")
    public List<Qualifier> getIdByMark(@Param("markList") List<String> markList, @Param("typeId") String typeId, @Param("tenantId") String tenantId);

    //判断批量添加修饰词名字是否重复
    @Select("<script>" +
            " select a.id " +
            " from qualifier a " +
            " where a.tenantid=#{tenantId} and a.typeid=#{typeId} and a.name in " +
            " <foreach item='name' index='index' collection='nameList' separator=',' open='(' close=')'> " +
            " #{name} " +
            " </foreach>" +
            " </script>")
    public List<Qualifier> getIdByName(@Param("nameList") List<String> nameList, @Param("typeId") String typeId, @Param("tenantId") String tenantId);

    //批量删除修饰词
    @Delete("<script>" +
            " delete from qualifier where id in " +
            " <foreach item='id' index='index' collection='Ids' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int deleteQualifierByIds(@Param("Ids") List<String> Ids);

    //编辑修饰词
    @Update("update qualifier set name=#{qualifier.name},mark=#{qualifier.mark},typeid=#{qualifier.typeId},update_user=#{qualifier.updateUser},update_time=#{qualifier.updateTime},\"desc\"=#{qualifier.desc} where id=#{qualifier.id} and tenantId=#{tenantId} ")
    public int updateQualifierByIds(@Param("qualifier") Qualifier qualifier, @Param("tenantId") String tenantId);

    //判断编辑修饰词名字或标识是否重复
    @Select({"<script>"+
            "select tmp.id from ( select id from qualifier  where " +
            "<choose>" +
            "<when test=\"name!=null and name!='' \">" +
            " name = #{name} " +
            "</when>" +
            "<otherwise>"+
            " mark = #{mark} " +
            "</otherwise>"+
            "</choose>" +
            "and typeid=#{typeId} and tenantid=#{tenantId}) tmp where tmp.id!=#{id}"+
            " </script>"})
    public List<Qualifier> getIdsByNameOrMark(@Param("id") String id, @Param("name") String name, @Param("mark") String mark, @Param("typeId") String typeId, @Param("tenantId") String tenantId);

    //获取修饰词列表
    @Select({"<script>",
            " select count(*) over() total,qualifier.id,qualifier.mark,qualifier.name,qualifier.desc,qualifier.creator,qualifier.typeId,qualifier.create_time as createTime",
            " from qualifier ",
            " where ",
            " qualifier.tenantId=#{tenantId} and qualifier.typeId=#{params.typeId} ",
            " <if test=\"params.query != null and params.query!=''\">",
            " and (qualifier.name like '%${params.query}%' ESCAPE '/' or qualifier.mark like '%${params.query}%' ESCAPE '/')",
            " </if>",
            " <if test='params.startTime!=null and params.endTime!=null'>",
            " and create_time between #{params.startTime} and #{params.endTime} ",
            " </if>",
            "<choose>",
            "<when test=\"params.sortby!=null and params.sortby!='' \">",
            " order by ${params.sortby} ",
            "</when>",
            "<otherwise>",
            " order by createTime ",
            "</otherwise>",
            "</choose>",
            " <if test=\"params.order!='' and params.order!=null\">",
            " ${params.order} ",
            " </if>",
            " <if test='params.limit!=null and params.limit!= -1'>",
            " limit #{params.limit}",
            " </if>",
            " <if test='params.offset!=null'>",
            " offset #{params.offset}",
            " </if>",
            " </script>"})
    public List<Data> getQualifierList(@Param("params") QualifierParameters params, @Param("tenantId") String tenantId);

    //获取全部修饰词
    @Select({"<script>",
            " select count(*) over() total,qualifier.id,qualifier.mark,qualifier.name",
            " from qualifier ",
            " where ",
            " qualifier.tenantId=#{tenantId} ",
            " <if test=\"params.query != null and params.query!=''\">",
            " and (qualifier.name like '%${params.query}%' ESCAPE '/' )",
            " </if>",
            " order by create_time ",
            " <if test='params.limit!=null and params.limit!= -1'>",
            " limit #{params.limit}",
            " </if>",
            " <if test='params.offset!=null'>",
            " offset #{params.offset}",
            " </if>",
            " </script>"})
    public List<Data> getAllQualifierList(@Param("params") QualifierParameters params, @Param("tenantId") String tenantId);


    //获取修饰词引用列表
    @Select({"<script>",
            " select count(*) over() total,qualifier.name qualifierName,index_derive_info.index_name indexName,index_derive_info.business_leader interfaceUser",
            " from qualifier",
            " join index_derive_modifier_relation",
            " on qualifier.id = index_derive_modifier_relation.modifier_id",
            " join index_derive_info",
            " on index_derive_modifier_relation.derive_index_id = index_derive_info.index_id",
            " where qualifier.tenantId=#{tenantId}",
            " and qualifier.id=#{id}",
            " <if test='limit!=null and limit!= -1 '>",
            " limit #{limit}",
            " </if>",
            " <if test='offset != null'>",
            " offset #{offset}",
            " </if>",
            " </script>"})
    public List<ReferenceIndex> getQualifierRelationListById(@Param("id") String id, @Param("tenantId") String tenantId, @Param("limit") int limit, @Param("offset") int offset);

    //添加修饰词类型
    @Insert({"<script>",
            " insert into qualifier_type(type_id,type_name,type_mark,creator,create_time,update_user,update_time,type_desc,tenantid) values ",
            " (#{qualifierType.qualifierTypeId},#{qualifierType.qualifierTypeName},#{qualifierType.qualifierTypeMark},#{qualifierType.creator},#{qualifierType.createTime},#{qualifierType.updateUser},#{qualifierType.updateTime},#{qualifierType.qualifierTypeDesc},#{tenantId}) ",
            " </script>"})
    public int addQualifierType(@Param("qualifierType") QualifierType qualifierType, @Param("tenantId") String tenantId);


    @Select(" select type_id as \"id\",type_name as \"name\",type_mark as mark,creator,create_time createTime,update_user updateUser,update_time updateTime,type_desc as desc" +
            " from qualifier_type " +
            " where tenantId=#{tenantId} and type_id=#{typeId}")
    public List<Data> getQualifierTypeByIds(@Param("typeId") String typeId, @Param("tenantId") String tenantId);

    //判断添加的修饰词类型的名称或标识是否重复
    @Select({"<script>",
            " select type_id ",
            " from qualifier_type where ",
            "<choose>",
            "<when test=\"name!=null and name!=''\">" ,
            " type_name = #{name} ",
            "</when>",
            "<otherwise>",
            " type_mark = #{mark} ",
            "</otherwise>",
            "</choose>",
            " and tenantid=#{tenantId}",
            " </script>"})
    public List<QualifierType> getIdByTypeNameOrMark(@Param("name") String name, @Param("mark") String mark, @Param("tenantId") String tenantId);

    //编辑修饰词类型
    @Update(" update qualifier_type " +
            " set type_name=#{qualifierType.qualifierTypeName},type_mark=#{qualifierType.qualifierTypeMark},update_user=#{qualifierType.updateUser},update_time=#{qualifierType.updateTime},type_desc=#{qualifierType.qualifierTypeDesc} " +
            " where type_id=#{qualifierType.qualifierTypeId}" +
            " and tenantId=#{tenantId}")
    public int updateQualifierTypeByIds(@Param("qualifierType") QualifierType qualifierType, @Param("tenantId") String tenantId);

    @Select({"<script>",
            "select tmp.type_id from",
            "(select type_id from qualifier_type ",
            " where  ",
            "<choose>",
            "<when test=\"name!=null and name!=''\">" ,
            " type_name = #{name} ",
            "</when>",
            "<otherwise>",
            " type_mark = #{mark} ",
            "</otherwise>",
            "</choose>",
            "and tenantid=#{tenantId}) tmp where type_id!=#{id}",
            "</script>"})
    public List<QualifierType> getIdsByTypeNameOrMark(@Param("id") String id, @Param("name") String name, @Param("mark") String mark, @Param("tenantId") String tenantId);

    //获取修饰词目录
    @Select({"<script>",
            " select qua_temp.count count,qualifier_type.type_id as \"id\",qualifier_type.type_mark mark,qualifier_type.type_name as \"name\",qualifier_type.type_desc as desc ",
            " from qualifier_type ",
            " left join (select typeid,count(*) from qualifier where tenantId=#{tenantId} GROUP BY typeid) qua_temp " ,
            " on qua_temp.typeid = qualifier_type.type_id " ,
            " where qualifier_type.tenantId=#{tenantId} ",
            " order by qualifier_type.create_time",
            " </script>"})
    public List<Data> getQualifierTypeList(@Param("tenantId") String tenantId);

    //删除修饰词目录
    @Delete({"<script>" ,
            " delete from qualifier_type where tenantid=#{tenantId} and type_id in ",
            " <foreach item='typeId' index='index' collection='typeIds' separator=',' open='(' close=')'>",
            " #{typeId} ",
            " </foreach>",
            " </script>"})
    public int deleteQualifierTypeByIds(@Param("typeIds") List<String> typeIds, @Param("tenantId") String tenantId);

    @Delete("<script>" +
            " delete from qualifier where tenantid=#{tenantId} and typeid in " +
            " <foreach item='id' index='index' collection='Ids' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int removeQualifierByIds(@Param("Ids") List<String> Ids, @Param("tenantId") String tenantId);
}
