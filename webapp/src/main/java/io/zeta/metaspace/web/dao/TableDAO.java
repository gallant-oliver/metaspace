package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.TableHeader;
import org.apache.ibatis.annotations.Select;
import java.sql.SQLException;
import java.util.List;

public interface TableDAO {
    @Select("select * fromtabelinfo where tableguid=#{guid}")
    public TableHeader getTableInfoByTableguid(String guid) throws SQLException;

    @Select("select users.username from users,role,role2category,category,table_relation where role2category.roleid=role.roleid and users.roleid=role.roleid and role2category.categoryid=category.guid and category.guid=table_relation.categoryguid and table_relation.tableguid=#{guid}")
    public List<String> getAdminByTableguid(String guid);


}
