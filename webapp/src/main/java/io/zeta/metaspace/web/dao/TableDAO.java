package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.pojo.TableInfo;
import org.apache.ibatis.annotations.Select;
import java.sql.SQLException;
import java.util.List;

public interface TableDAO {
    @Select("select * from tableinfo where tableguid=#{guid}")
    public TableInfo getTableInfoByTableguid(String guid) throws SQLException;

    @Select("select users.username from users,role,role2category,category,table_relation where role2category.roleid=role.roleid and users.roleid=role.roleid and role2category.categoryid=category.guid and category.guid=table_relation.categoryguid and table_relation.tableguid=#{guid}")
    public List<String> getAdminByTableguid(String guid);

    @Select("select generatetime from table_relation where tableguid=#{guid}")
    public String getDateByTableguid(String guid);

    @Select("select businessinfo.name businessObject,category.name department,businessinfo.submissiontime businessLeader from business2table,businessinfo,category where businessinfo.businessid=business2table.businessid and businessinfo.departmentid=category.guid and business2table.tableguid=#{guid}")
    public List<Table.BusinessObject> getBusinessObjectByTableguid(String guid);


}
