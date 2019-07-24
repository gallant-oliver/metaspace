package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.datastandard.DataStandardQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DataStandardDAO {

    @Insert("insert into data_standard(id,number,content,description,createtime,updatetime,operator,version,categoryid,delete)" +
            "values(#{id},#{number},#{content},#{description},#{createTime},#{updateTime},#{operator},#{version},#{categoryId},#{delete})")
    public int insert(DataStandard dataStandard);

    @Select({" select a.id,a.number,a.content,a.description,a.createtime,a.updatetime,b.username as operator,a.version,a.categoryid,a.delete ",
             " from data_standard a ",
             " inner join users b on a.operator=b.userid ",
             " where a.delete=false and a.id = #{id} "})
    public DataStandard getById(@Param("id") String id);

    @Select({" select a.id,a.number,a.content,a.description,a.createtime,a.updatetime,b.username as operator,a.version,a.categoryid,a.delete ",
             " from data_standard a ",
             " inner join users b on a.operator=b.userid ",
             " where a.delete=false and a.number = #{number} "})
    public List<DataStandard> getByNumber(@Param("number") String number);

    @Select("update data_standard set delete=true where number=#{number}")
    public void deleteByNumber(@Param("number") String number);

    @Insert({" <script>",
             " update data_standard set delete=true where number in ",
             " <foreach collection='numberList' item='number' index='index' open='(' close=')' separator=','>",
             " #{number}",
             " </foreach>",
             " </script>"})
    public void deleteByNumberList(@Param("numberList") List<String> numberList);

    @Select({" <script>",
             " select a.id,a.number,a.content,a.description,a.createtime,a.updatetime,b.username as operator,a.version,a.categoryid,a.delete ",
             " from data_standard a ",
             " inner join users b on a.operator=b.userid ",
             " where a.delete=false and a.id in ",
             " <foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public List<DataStandard> queryByIds(@Param("ids") List<String> ids);

    @Select({" <script>",
             " select a.id,a.number,a.content,a.description,a.createtime,a.updatetime,b.username as operator,a.version,a.categoryid,a.delete ",
             " from data_standard a ",
             " inner join users b on a.operator=b.userid ",
             " where a.delete=false and a.number in ",
             " <foreach collection='numberList' item='number' index='index' open='(' close=')' separator=','>",
             " #{number}",
             " </foreach>",
             " </script>"})
    public List<DataStandard> queryByNumberList(@Param("numberList") List<String> numberList);

    @Select({"<script>",
             " select b.id,b.number,b.content,b.description,b.createtime,b.updatetime,c.username as operator,b.version,b.categoryid,b.delete from ",
             " (select number,max(version) as version from data_standard where delete=false and categoryid=#{categoryId} group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false and b.categoryid=#{categoryId} ",
             " <if test='params != null'>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    public List<DataStandard> queryByCatetoryId(@Param("categoryId") String categoryId, @Param("params") Parameters params);


    @Select({"<script>",
             " select count(1) from ",
             " (select number,max(version) as version from data_standard where delete=false and categoryid=#{categoryId} group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false and b.categoryid=#{categoryId} ",
             " </script>"})
    public long countByByCatetoryId(@Param("categoryId") String categoryId);

    @Select({"<script>",
             " select b.id,b.number,b.content,b.description,b.createtime,b.updatetime,c.username as operator,b.version,b.categoryid,b.delete from ",
             " (select number,max(version) as version from data_standard where delete=false ",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (number like '%${params.query}%' ESCAPE '/' or content like '%${params.query}%' ESCAPE '/' or description like '%${params.query}%' ESCAPE '/') ",
             " </if>",
             " <if test=\"params.categoryId != null and params.categoryId!=''\">",
             " and categoryId=#{params.categoryId} ",
             " </if>",
             " group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false ",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (c.username like '%${params.query}%' ESCAPE '/' or b.number like '%${params.query}%' ESCAPE '/' or b.content like '%${params.query}%' ESCAPE '/' or b.description like '%${params.query}%' ESCAPE '/') ",
             " </if>",
             " <if test=\"params.categoryId != null and params.categoryId!=''\">",
             " and b.categoryId=#{params.categoryId} ",
             " </if>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<DataStandard> search(@Param("params") DataStandardQuery params);

    @Select({"<script>",
             " select count(1) from ",
             " (select number,max(version) as version from data_standard where delete=false ",
             " <if test=\"query != null and query!=''\">",
             " and (number like '%${query}%' ESCAPE '/' or content like '%${query}%' ESCAPE '/' or description like '%${query}%' ESCAPE '/') ",
             " </if>",
             " <if test=\"categoryId != null and categoryId!=''\">",
             " and categoryId=#{categoryId} ",
             " </if>",
             " group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false ",
             " <if test=\"query != null and query!=''\">",
             " and (c.username like '%${query}%' ESCAPE '/' or b.number like '%${query}%' ESCAPE '/' or b.content like '%${query}%' ESCAPE '/' or b.description like '%${query}%' ESCAPE '/') ",
             " </if>",
             " <if test=\"categoryId != null and categoryId!=''\">",
             " and b.categoryId=#{categoryId} ",
             " </if>",
             " </script>"})
    public long countBySearch(@Param("query") String query, @Param("categoryId") String categoryId);

    @Insert({"<script>",
             "insert into data_standard(id,number,content,description,createtime,updatetime,operator,version,categoryid,delete) values ",
             " <foreach collection='dataList' item='data' index='index' separator=','>",
             " ( #{data.id},#{data.number},#{data.content},#{data.description},#{data.createTime},#{data.updateTime},#{data.operator},#{data.version},#{data.categoryId},#{data.delete} )",
             " </foreach>",
             " </script>"})
    int batchInsert(@Param("dataList") List<DataStandard> dataList);

    @Select({"<script>",
             " select d.id,d.number,d.content,d.description,d.createtime,d.updatetime,u.username as operator,d.version,d.categoryid ",
             " from data_standard d inner join users u on d.operator=u.userid where d.delete=false and d.number=#{number} ",
             " <if test='params != null'>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    List<DataStandard> history(@Param("number") String number, @Param("params") Parameters parameters);

    @Select({"select count(1) from data_standard d inner join users u on d.operator=u.userid where d.delete=false and d.number=#{number} "})
    long countByHistory(@Param("number") String number);
}
