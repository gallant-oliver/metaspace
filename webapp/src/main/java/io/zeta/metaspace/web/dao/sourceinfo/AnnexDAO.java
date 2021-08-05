package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.sourceinfo.Annex;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnexDAO {

    @Insert("INSERT INTO annex(\n" +
            "annex_id, file_name, file_type, path,file_size, create_time, modify_time)\n" +
            " VALUES (#{item.annexId},#{item.fileName}, #{item.fileType}, #{item.path},#{item.fileSize}, NOW(), NOW() ) ")
    int save(@Param("item") Annex item);

    @Select("SELECT annex_id as annexId, file_name as fileName, file_type as fileType, path,file_size AS fileSize \n" +
            " FROM public.annex where annex_id=#{annexId} limit 1;")
    Annex selectByAnnexId(@Param("annexId") String annexId);
}
