package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.source.CodeInfo;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeSourceInfoStatusDAO {

    @Select("SELECT code,name FROM code_source_info_status")
    List<CodeInfo> getAll();
}
