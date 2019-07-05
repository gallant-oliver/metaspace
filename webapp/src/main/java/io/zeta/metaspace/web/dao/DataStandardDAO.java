package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DataStandardDAO {

    @Insert("insert into data_standard(id,number,content,description,createtime,updatetime,operator,version,categoryid,delete)" +
            "values(#{id},#{number},#{content},#{description},#{createtime},#{updatetime},#{operator},#{version},#{categoryid},#{delete})")
    public int insert(DataStandard dataStandard);

    @Select("select * from data_standard where id=#{id}")
    public DataStandard get(@Param("id") String id);


    @Select("update data_standard set delete=true where id=#{id}")
    public int delete(@Param("id") String id);

    @Insert({" <script>",
             " update data_standard set delete=true where id in ",
             " <foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public int deleteList(@Param("ids") List<String> ids);

    @Insert({" <script>",
             " select * from data_standard where id in ",
             " <foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public List<DataStandard> queryByIds(@Param("ids") List<String> ids);

    @Select({"<script>",
             " select b.id,b.number,b.description,b.createtime,b.updatetime,c.username as operator,b.version,b.categoryid from ",
             " (select number,max(version) as version from data_standard where categoryid=#categoryId group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " <if test='params != null'>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by #{params.offset} #{params.order}",
             " </if>",
             " <if test='params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    public List<DataStandard> queryByCatetoryId(@Param("categoryId") String categoryId, @Param("params") Parameters params);


    @Select({"<script>",
             " select count(1) from ",
             " (select number,max(version) as version from data_standard where categoryid=#categoryId group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " </script>"})
    public long countByByCatetoryId(@Param("categoryGuid") String categoryGuid);

    @Select({"<script>",
             " select b.id,b.number,b.description,b.createtime,b.updatetime,c.username as operator,b.version,b.categoryid from ",
             " (select number,max(version) as version from data_standard ",
             " <if test=\"query != null and query!=''\">",
             " where (t.number like '%${query}%' ESCAPE '/' or t.content like '%${query}%' ESCAPE '/' or t.description like '%${query}%' ESCAPE '/' or u.username like '%${query}%' ESCAPE '/') ",
             " </if>",
             " group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",

             " <if test='params.sortby != null and params.order != null'>",
             " order by #{params.offset} #{params.order}",
             " </if>",
             " <if test='params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<DataStandard> search(@Param("params") Parameters params);


    @Select({"<script>",
             " select count(1) from ",
             " (select number,max(version) as version from data_standard ",
             " <if test=\"query != null and query!=''\">",
             " where (t.number like '%${query}%' ESCAPE '/' or t.content like '%${query}%' ESCAPE '/' or t.description like '%${query}%' ESCAPE '/' or u.username like '%${query}%' ESCAPE '/') ",
             " </if>",
             " group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " </script>"})
    public long countBySearch(@Param("query") String query);

    @Insert({"<script>",
             "insert into data_standard(id,number,content,description,createtime,updatetime,operator,version,categoryid,delete) values ",
             " <foreach collection='dataList' item='data' index='index' open='(' close=')' separator=','>",
             " #{data.id},#{data.number},#{data.content},#{data.description},#{data.createtime},#{data.updatetime},#{data.operator},#{data.version},#{data.categoryid},#{data.delete}",
             " </foreach>",
             " </script>"})
    int batchInsert(List<DataStandard> dataList);

    @Select({"<script>",
             " select d.id,d.number,d.description,d.createtime,d.updatetime,u.username as operator,d.version,d.categoryid ",
             " from data_standard d inner join users u on d.operator=u.userid ",
             " <if test='params != null'>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by #{params.offset} #{params.order}",
             " </if>",
             " <if test='params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    List<DataStandard> history(String number, Parameters parameters);

    @Select({"<script>",
             " select count(1) from data_standard d inner join users u on d.operator=u.userid ",
             " <if test='params != null'>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by #{params.offset} #{params.order}",
             " </if>",
             " <if test='params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    long countByHistory(String query);
}
