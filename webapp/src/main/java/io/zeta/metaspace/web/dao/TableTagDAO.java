package io.zeta.metaspace.web.dao;



import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.table.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
public interface TableTagDAO {
    @Insert("insert into tag(tagid,tagname) values(#{tagId},#{tagName})")
    public int addTag(@Param("tagId") String tagId, @Param("tagName") String tagName);

    @Delete("delete from tag where tagid=#{tagId}")
    public int deleteTag(@Param("tagId") String tagId);

    @Select("select * from tag where tagname like '%'||#{query}||'%' order by tagname limit #{limit} offset #{offset}")
    public List<Tag> getTags(@Param("query") String query,@Param("offset")long offset,@Param("limit")long limit);

    @Select("select * from tag where tagname like '%'||#{query}||'%' order by tagname offset #{offset}")
    public List<Tag> getTag(@Param("query") String query,@Param("offset")long offset);

    @Insert("insert into table2tag(tagid,tableguid) values(#{tagId},#{tableGuid})")
    public int addTable2Tag(@Param("tagId") String tagId,@Param("tableGuid") String tableGuid);

    @Insert("insert into tableinfo(tableguid,tablename,dbname,status,createtime) values(#{table.tableId},#{table.databaseName},#{table.tableName},#{table.status},#{table.createTime})")
    public int addTable(@Param("table") Table table);

    @Delete("delete from table2tag where tableguid=#{tableGuid} and tagid=#{tagId}")
    public int deleteTable2Tag(@Param("tableGuid") String tableGuid,@Param("tagId") String tagId);

    @Select("select 1 from tag where tagname=#{tagName}")
    public List<Integer> ifTagExists(String tagName);

    @Select("select 1 from tableinfo where tableguid=#{tableGuid}")
    public List<Integer> ifTableExists(String tableGuid);

    @Delete("delete from table2tag where tableguid=#{tableGuid}")
    public int delAllTable2Tag(String tableGuid);

    @Select("select tag.tagid as tagid,tag.tagname as tagname from table2tag,tag where table2tag.tagid=tag.tagid and tableguid=#{tableGuid}")
    public List<Tag> getTable2Tag(String tableGuid);
}
