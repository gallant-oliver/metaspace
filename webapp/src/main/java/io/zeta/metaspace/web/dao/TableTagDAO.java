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

    @Select("select * from tag where tagname like '%#{query}%' limit #{limit} offset #{offset}")
    public List<Tag> getTags(@Param("query") String query,@Param("offset")long offset,@Param("limit")long limit);

    @Insert("insert into table2tag(tagid,tableguid) values(#{tagId},#{tableGuid})")
    public int addTable2Tag(@Param("tagId") String tagId,@Param("tableGuid") String tableGuid);

    @Insert("insert into table(tableguid,tablename,dbname,status) values(#{table.tableId},#{table.databaseName},#{table.tableName},#{table.status})")
    public int addTable(@Param("table") Table table);

    @Delete("delete from table2tag where tableguid=#{tableGuid} and tagid=#{tagId}")
    public List<Tag> deleteTable2Tag(@Param("tableGuid") String tableGuid,@Param("tagId") String tagId);

    @Select("select 1 from tag where tagname=#{tagName}")
    public List<Tag> ifTagExists(String tagName);

    @Select("select 1 from table where tableguid=#{tableGuid}")
    public List<Table> ifTableExists(String tableGuid);
}
