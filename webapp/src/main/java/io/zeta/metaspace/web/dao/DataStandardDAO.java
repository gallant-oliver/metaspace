package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.datastandard.DataStandAndRule;
import io.zeta.metaspace.model.datastandard.DataStandAndTable;
import io.zeta.metaspace.model.datastandard.DataStandToRule;
import io.zeta.metaspace.model.datastandard.DataStandToTable;
import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.datastandard.DataStandardHead;
import io.zeta.metaspace.model.datastandard.DataStandardQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import javax.ws.rs.DELETE;

public interface DataStandardDAO {

    @Insert("insert into data_standard(id,number,content,description,createtime,updatetime,operator,version,categoryid,delete,tenantid)" +
            "values(#{dataStandard.id},#{dataStandard.number},#{dataStandard.content},#{dataStandard.description},#{dataStandard.createTime},#{dataStandard.updateTime},#{dataStandard.operator},#{dataStandard.version},#{dataStandard.categoryId},#{dataStandard.delete},#{tenantId})")
    public int insert(@Param("dataStandard") DataStandard dataStandard,@Param("tenantId")String tenantId);

    @Insert("insert into data_standard(id,number,content,description,createtime,updatetime,operator,categoryid,delete,version,tenantid)" +
            "values(#{dataStandard.id},#{dataStandard.number},#{dataStandard.content},#{dataStandard.description},#{dataStandard.createTime},#{dataStandard.updateTime},#{dataStandard.operator},#{dataStandard.categoryId},#{dataStandard.delete},(select max(version) from data_standard where number=#{dataStandard.number} and tenantid=#{tenantId} GROUP BY number)+1,#{tenantId})")
    public int update(@Param("dataStandard") DataStandard dataStandard,@Param("tenantId")String tenantId);

    @Select({" select a.id,a.number,a.content,a.description,a.createtime,a.updatetime,b.username as operator,a.version,a.categoryid,a.delete ",
             " from data_standard a ",
             " inner join users b on a.operator=b.userid ",
             " where a.delete=false and a.id = #{id} "})
    public DataStandard getById(@Param("id") String id);

    @Select({" select a.id,a.number,a.content,a.description,a.createtime,a.updatetime,b.username as operator,a.version,a.categoryid,a.delete ",
             " from data_standard a ",
             " inner join users b on a.operator=b.userid ",
             " where a.delete=false and a.number = #{number} and a.tenantid=#{tenantId}"})
    public List<DataStandard> getByNumber(@Param("number") String number,@Param("tenantId")String tenantId);

    @Select("update data_standard set delete=true where number=#{number} and tenantid=#{tenantId}")
    public void deleteByNumber(@Param("number") String number,@Param("tenantId")String tenantId);

    @Insert({" <script>",
             " update data_standard set delete=true where tenantid=#{tenantId} and number in ",
             " <foreach collection='numberList' item='number' index='index' open='(' close=')' separator=','>",
             " #{number}",
             " </foreach>",
             " </script>"})
    public void deleteByNumberList(@Param("numberList") List<String> numberList,@Param("tenantId")String tenantId);

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
             " where a.delete=false and tenantid=#{tenantId} and a.number in ",
             " <foreach collection='numberList' item='number' index='index' open='(' close=')' separator=','>",
             " #{number}",
             " </foreach>",
             " </script>"})
    public List<DataStandard> queryByNumberList(@Param("numberList") List<String> numberList,@Param("tenantId") String tenantId);

    @Select({"<script>",
             " select count(1)over() total,b.id,b.number,b.content,b.description,b.createtime,b.updatetime,c.username as operator,b.version,b.categoryid,b.delete from ",
             " (select number,max(version) as version from data_standard where delete=false and categoryid=#{categoryId} and tenantid=#{tenantId} group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false and b.categoryid=#{categoryId} and b.tenantid=#{tenantId} ",
             " <if test='params != null'>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    public List<DataStandard> queryByCatetoryId(@Param("categoryId") String categoryId, @Param("params") Parameters params,@Param("tenantId")String tenantId);


    @Select({"<script>",
             " select count(1) from ",
             " (select number,max(version) as version from data_standard where delete=false and categoryid=#{categoryId} and tenantid=#{tenantId} group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false and b.categoryid=#{categoryId} and b.tenantid=#{tenantId}",
             " </script>"})
    public long countByByCatetoryId(@Param("categoryId") String categoryId,@Param("tenantId")String tenantId);

    @Select({"<script>",
             " select count(1)over() total,b.id,b.number,b.content,b.description,b.createtime,b.updatetime,c.username as operator,b.version,b.categoryid,b.delete from ",
             " (select number,max(version) as version from data_standard where delete=false and tenantid=#{tenantId} ",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (number like '%${params.query}%' ESCAPE '/' or content like '%${params.query}%' ESCAPE '/' or description like '%${params.query}%' ESCAPE '/') ",
             " </if>",
             " <if test=\"params.categoryId != null and params.categoryId!=''\">",
             " and categoryId=#{params.categoryId} ",
             " </if>",
             " group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false and b.tenantid=#{tenantId}",
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
    public List<DataStandard> search(@Param("params") DataStandardQuery params,@Param("tenantId")String tenantId);


    @Insert({"<script>",
             "insert into data_standard(id,number,content,description,createtime,updatetime,operator,version,categoryid,delete,tenantid) values ",
             " <foreach collection='dataList' item='data' index='index' separator=','>",
             " ( #{data.id},#{data.number},#{data.content},#{data.description},#{data.createTime},#{data.updateTime},#{data.operator},#{data.version},#{data.categoryId},#{data.delete},#{tenantId} )",
             " </foreach>",
             " </script>"})
    int batchInsert(@Param("dataList") List<DataStandard> dataList,@Param("tenantId") String tenantId);

    @Select({"<script>",
             " select count(*)over() total,d.id,d.number,d.content,d.description,d.createtime,d.updatetime,u.username as operator,d.version,d.categoryid ",
             " from data_standard d inner join users u on d.operator=u.userid where d.delete=false and d.number=#{number} and d.tenantId=#{tenantId}",
             " and (u.username like '%${query}%' ESCAPE '/' or d.number like '%${query}%' ESCAPE '/' or d.content like '%${query}%' ESCAPE '/' or d.description like '%${query}%' ESCAPE '/') ",
             " order by version",
             " <if test='limit != null and limit != -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    List<DataStandard> history(@Param("number") String number, @Param("limit") Integer limit, @Param("offset") Integer offset, @Param("query") String query,@Param("tenantId")String tenantId);

    @Delete({"delete from data_standard2table where tableGuid=#{tableGuid}"})
    int deleteByTableId(@Param("tableGuid") String tableGuid);

    @Delete({"<script>" +
             "delete from data_standard2table where number=#{number} and tableguid in " +
             "(select tableguid from tableinfo where dbname in " +
             " <foreach item='item' index='index' collection='databases'" +
             " open='(' separator=',' close=')'>" +
             " #{item}" +
             " </foreach>)" +
             "</script>"})
    int deleteStandard2TableByNumber(@Param("number") String number,@Param("databases")List<String> databases);

    @Insert("insert into data_standard2table(number,tableguid,createtime,operator) values(#{number},#{dataStandAndTable.tableGuid},#{dataStandAndTable.createTime},#{dataStandAndTable.operator})")
    public int assignTableToStandard(@Param("number") String number,@Param("dataStandAndTable")DataStandAndTable dataStandAndTable);

    @Delete({"delete from data_standard2data_quality_rule where ruleId=#{ruleId}"})
    int deleteByRuleId(@Param("ruleId") String ruleId);

    @Delete({"delete from data_standard2data_quality_rule where number=#{number} and ruleid in " +
             "(select id from data_quality_rule where tenantid=#{tenantId})"})
    int deleteStandard2RuleByRuleId(@Param("number") String number,@Param("tenantId")String tenantId);

    @Insert("insert into data_standard2data_quality_rule(number,ruleid,createtime,operator) values(#{number},#{dataStandAndRule.ruleId},#{dataStandAndRule.createTime},#{dataStandAndRule.operator})")
    public int assignRuleToStandard(@Param("number") String number,@Param("dataStandAndRule") DataStandAndRule dataStandAndRule);

    @Select("select b.content from " +
            " (select number,max(version) as version from data_standard where number=#{number} and delete=false and tenantid=#{tenantId} group by number) as a " +
            " inner join data_standard b on a.number=b.number and a.version=b.version and delete=false and tenantid=#{tenantId}")
    String getContentByNumber(@Param("number") String number,@Param("tenantId")String tenantId);

    //注
    @Select({"<script>",
             "select count(*)over() total,s.tableguid,t.tablename,s.createtime,u.username as operator from " +
             " data_standard2table as s " +
             " inner join tableinfo t on s.tableguid=t.tableguid " +
             " inner join users u on s.operator=u.userid " +
             " where s.number=#{number} and t.dbname in " +
             " <foreach item='item' index='index' collection='databases'" +
             " open='(' separator=',' close=')'>" +
             " #{item}" +
             " </foreach> " +
             " <if test='params != null'>",
             " <if test='params.sortby != null'>",
             " order by ${params.sortby} ",
             "<if test='params.order != null'>",
             "${params.order}",
             "</if>",
             " </if>",
             " <if test='params.limit != 0 and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    List<DataStandToTable> getTableByNumber(@Param("number") String number,@Param("params")Parameters parameters,@Param("databases")List<String> databases);

    @Select({"<script>",
             "select count(*)over() total,s.ruleid,r.name as ruleName,s.createtime,u.username as operator from " +
             " data_standard2data_quality_rule as s " +
             " inner join data_quality_rule r on s.ruleid=r.id " +
             " inner join users u on s.operator=u.userid " +
             " where s.number=#{number} and r.tenantid=#{tenantId}",
             " <if test='params != null'>",
             " <if test='params.sortby != null'>",
             " order by ${params.sortby} ",
             "<if test='params.order != null'>",
             "${params.order}",
             "</if>",
             " </if>",
             " <if test='params.limit != 0 and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    List<DataStandToRule> getRuleByNumber(@Param("number") String number,@Param("params")Parameters parameters,@Param("tenantId")String tenantId);

    @Select({"<script>",
             " select b.id,b.number,b.content,b.description,b.categoryid from ",
             " (select d.number,max(d.version) as version from data_standard d join data_standard2table t on d.number=t.number where d.delete=false and t.tableguid=#{tableGuid} and d.tenantid=#{tenantId} group by d.number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " where b.delete=false and b.tenantid=#{tenantId}",
             " </script>"})
    public List<DataStandardHead> getDataStandardByTableGuid(@Param("tableGuid") String tableGuid,@Param("tenantId")String tenantId);

    @Select({"<script>",
             " select b.id,b.number,b.content,b.description,b.categoryid from ",
             " (select d.number,max(d.version) as version from data_standard d join data_standard2data_quality_rule t on d.number=t.number where d.delete=false and t.ruleid=#{ruleId} and d.tenantid=#{tenantId} group by d.number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " where b.delete=false and b.tenantid=#{tenantId}",
             " </script>"})
    public List<DataStandardHead> getDataStandardByRuleId(@Param("ruleId") String ruleId,@Param("tenantId")String tenantId);

    @Select({"<script>",
             " select b.id,b.number,b.content,b.description,b.categoryid from ",
             " (select number,max(version) as version from data_standard where delete=false and categoryid=#{categoryId} and tenantid=#{tenantId} group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " where b.delete=false and b.categoryid=#{categoryId} and b.tenantid=#{tenantId}",
             " </script>"})
    public List<DataStandardHead> getStandardByCategoyrId(@Param("categoryId") String categoryId,@Param("tenantId") String tenantId);


}
