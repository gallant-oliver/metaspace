package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.datastandard.*;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DataStandardDAO {
    
    @Insert("insert into data_standard " +
            "(id,number,description,createtime,updatetime,operator,categoryid,delete,tenantid," +
            " name,standard_type,data_length,allowable_value_flag,allowable_value,standard_level,data_type) " +
            "values " +
            "(#{dataStandard.id},#{dataStandard.number},#{dataStandard.description}," +
            " #{dataStandard.createTime},#{dataStandard.updateTime},#{dataStandard.operator}," +
            " #{dataStandard.categoryId},#{dataStandard.delete},#{tenantId},#{dataStandard.name}," +
            " #{dataStandard.standardType},#{dataStandard.dataLength},#{dataStandard.allowableValueFlag}," +
            " #{dataStandard.allowableValue},#{dataStandard.standardLevel},#{dataStandard.dataType})"
    )
    int insert(@Param("dataStandard") DataStandard dataStandard, @Param("tenantId") String tenantId);
    
    /**
     * 根据ID更新版本号
     */
    @Update({
            "UPDATE data_standard SET version=#{version} WHERE id=#{id}"
    })
    int updateVersion(@Param("id") String id, @Param("version") int version);
    
    @Select({" select a.id,a.number,a.name,a.description,a.createtime,a.updatetime,b.username as operator,a.version,a.categoryid,a.delete ",
            " from data_standard a ",
            " inner join users b on a.operator=b.userid ",
            " where a.delete=false and a.id = #{id} "})
    DataStandard getById(@Param("id") String id);
    
    /**
     * 根据标准编码查询最大历史版本号
     */
    @Select({
            "SELECT MAX(version) FROM data_standard WHERE number=#{number}"
    })
    int getMaxHistoryVersion(@Param("number") String number);
    
    @Select({" select count(1) from data_standard where delete=false and number = #{number} and tenantid=#{tenantId}"})
    Long getCountByNumber(@Param("number") String number, @Param("tenantId") String tenantId);
    
    @Update("update data_standard set delete=true where number=#{number} and delete=false and tenantid=#{tenantId}")
    void deleteByNumber(@Param("number") String number, @Param("tenantId") String tenantId);
    
    @Insert({" <script>",
            " update data_standard set delete=true where tenantid=#{tenantId} and number in ",
            " <foreach collection='numberList' item='number' index='index' open='(' close=')' separator=','>",
            " #{number}",
            " </foreach>",
            " </script>"})
    public void deleteByNumberList(@Param("numberList") List<String> numberList, @Param("tenantId") String tenantId);
    
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
    List<DataStandard> queryByNumberList(@Param("numberList") List<String> numberList, @Param("tenantId") String tenantId);
    
    @Select({"<script>",
            " select count(1)over() total,b.id,b.number,b.name,b.description,b.createtime,b.updatetime,",
            " c.username as operator,b.version,b.categoryid,b.standard_type,b.data_type,b.data_length,",
            " b.allowable_value_flag,b.allowable_value,b.standard_level ",
            " from data_standard b inner join users c on b.operator=c.userid ",
            " where b.delete=false and b.version=0 and b.categoryid=#{categoryId} and b.tenantid=#{tenantId} ",
            " <if test='params != null'>",
            " <if test=\"params.sortby != null and params.sortby!='' and params.order !=null and params.order !=''\">",
            " order by ${params.sortby} ${params.order}",
            " </if>",
            " <if test='params.limit != null and params.limit != -1'>",
            " limit #{params.limit} offset #{params.offset}",
            " </if>",
            " </if>",
            " </script>"})
    List<DataStandard> queryByCatetoryId(@Param("categoryId") String categoryId,
                                         @Param("params") Parameters params,
                                         @Param("tenantId") String tenantId);

    @Select({"<script>",
             " select count(1) from ",
             " (select number,max(version) as version from data_standard where delete=false and categoryid=#{categoryId} and tenantid=#{tenantId} group by number) as a ",
             " inner join data_standard b on a.number=b.number and a.version=b.version ",
             " inner join users c on b.operator=c.userid ",
             " where b.delete=false and b.categoryid=#{categoryId} and b.tenantid=#{tenantId}",
             " </script>"})
    public long countByByCatetoryId(@Param("categoryId") String categoryId,@Param("tenantId")String tenantId);
    
    @Select({"<script>",
            " select count(1)over() total,b.id,b.number,b.name,b.description,b.createtime,b.updatetime,",
            " c.username as operator,b.version,b.categoryid,b.standard_type,b.data_type,b.data_length,",
            " b.allowable_value_flag,b.allowable_value,b.standard_level ",
            " from data_standard b inner join users c on b.operator=c.userid ",
            " where b.delete=false and b.version=0 and b.tenantid=#{tenantId}",
            " <if test=\"params.query != null and params.query!=''\">",
            " and (c.username like concat('%',#{params.query},'%') ESCAPE '/' ",
            "      or b.number like concat('%',#{params.query},'%') ESCAPE '/' ",
            "      or b.name like concat('%',#{params.query},'%') ESCAPE '/' ",
            "      or b.description like concat('%',#{params.query},'%') ESCAPE '/') ",
            " </if>",
            " <if test=\"params.categoryId != null and params.categoryId!=''\">",
            " and b.categoryId=#{params.categoryId} ",
            " </if>",
            " <if test=\"params.sortby != null and params.sortby!='' and params.order !=null and params.order !=''\">",
            " order by ${params.sortby} ${params.order}",
            " </if>",
            " <if test='params.limit != null and params.limit != -1'>",
            " limit #{params.limit} offset #{params.offset}",
            " </if>",
            " </script>"})
    List<DataStandard> search(@Param("params") DataStandardQuery params, @Param("tenantId") String tenantId);


    @Insert({"<script>",
             "insert into data_standard(id,number,content,description,createtime,updatetime,operator,version,categoryid,delete,tenantid) values ",
             " <foreach collection='dataList' item='data' index='index' separator=','>",
             " ( #{data.id},#{data.number},#{data.content},#{data.description},#{data.createTime},#{data.updateTime},#{data.operator},#{data.version},#{data.categoryId},#{data.delete},#{tenantId} )",
             " </foreach>",
             " </script>"})
    int batchInsert(@Param("dataList") List<DataStandard> dataList,@Param("tenantId") String tenantId);
    
    @Select({"<script>",
            " select count(1)over() total,b.id,b.number,b.name,b.description,b.createtime,b.updatetime,",
            " u.username as operator,b.version,b.categoryid,b.standard_type,b.data_type,b.data_length,",
            " b.allowable_value_flag,b.allowable_value,b.standard_level ",
            " from data_standard b inner join users u on b.operator=u.userid ",
            " where b.delete=false and b.number=#{number} and b.tenantId=#{tenantId}",
            " <if test=\"params.query != null and params.query!=''\">",
            " and (u.username like concat('%',#{params.query},'%') ESCAPE '/' ",
            // "      or b.number like concat('%',#{params.query},'%') ESCAPE '/' ",
            "      or b.name like concat('%',#{params.query},'%') ESCAPE '/' ",
            "      or b.description like concat('%',#{params.query},'%') ESCAPE '/') ",
            " </if>",
            " order by version",
            " <if test='params.limit != null and params.limit != -1'>",
            " limit #{params.limit} offset #{params.offset}",
            " </if>",
            " </script>"})
    List<DataStandard> queryHistoryData(@Param("number") String number,
                                        @Param("params") Parameters params,
                                        @Param("tenantId") String tenantId);

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
             " inner join data_quality_rule_template r on s.ruleid=r.id " +
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
