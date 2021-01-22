package io.zeta.metaspace.web.dao;


import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.table.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TableTagDAO {
    @Insert("insert into tag(tagid,tagname,tenantid) values(#{tagId},#{tagName},#{tenantId})")
    public int addTag(@Param("tagId") String tagId, @Param("tagName") String tagName, @Param("tenantId") String tenantId);

    @Delete("delete from tag where tagid=#{tagId}")
    public int deleteTag(@Param("tagId") String tagId);

    @Insert("insert into table2tag(tagid,tableguid) values(#{tagId},#{tableGuid})")
    public int addTable2Tag(@Param("tagId") String tagId, @Param("tableGuid") String tableGuid);

    @Delete("delete from table2tag where tableguid=#{tableGuid} and tagid=#{tagId}")
    public int deleteTable2Tag(@Param("tableGuid") String tableGuid, @Param("tagId") String tagId);

    @Select("select count(1) from table2tag where tagid=#{tagid} and tableguid=#{tableguid}")
    public Integer ifTagExists(@Param("tagid") String tagid, @Param("tableguid") String tableguid);

    @Delete("select count(1) from  table2tag where tagid=#{tagId}")
    long getTagUseCount(@Param("tagId") String tagId);

    @Select("select * from tag where tagname=#{tagName} and tenantid=#{tenantId}")
    public Tag getTag(@Param("tagName") String tagName, @Param("tenantId") String tenantId);

    @Delete("delete from table2tag where tableguid=#{tableGuid}")
    public int delAllTable2Tag(@Param("tableGuid") String tableGuid);

    @Select("select tag.tagid as tagid,tag.tagname as tagname from table2tag,tag where table2tag.tagid=tag.tagid and tableguid=#{tableGuid} and tenantid=#{tenantId}")
    public List<Tag> getTable2Tag(@Param("tableGuid") String tableGuid, @Param("tenantId") String tenantId);
}
