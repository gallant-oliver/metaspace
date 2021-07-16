// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.datasource.*;
import io.zeta.metaspace.model.metadata.ConnectorEntity;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.APIIdAndName;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupAndPrivilege;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;


public interface ConnectorDAO {

    @Select("SELECT id, connector_name, connector_class, type, db_ip, db_port, pdb_name, connector_unit, user_name, pass_word FROM connector " +
            "WHERE is_deleted = FALSE AND db_ip = #{dbIp} AND db_port = #{dbPort} AND connector_unit = #{dbName}")
    ConnectorEntity getConnector(@Param("dbIp")String dbIp, @Param("dbPort")String dbPort, @Param("dbName")String dbName);
}
