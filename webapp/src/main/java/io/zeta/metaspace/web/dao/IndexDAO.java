package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.po.indices.IndexAtomicPO;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;

public interface IndexDAO {


    @Insert("insert into index_atomic_info()")
    void addAtomicIndex(@Param("iap") IndexAtomicPO iap) throws SQLException;;
}
