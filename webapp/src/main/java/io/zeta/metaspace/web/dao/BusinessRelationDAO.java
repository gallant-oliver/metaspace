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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/2/13 10:31
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.BusinessRelationEntity;
import org.apache.ibatis.annotations.Insert;

import java.sql.SQLException;

public interface BusinessRelationDAO {

    @Insert("insert into business_relation(relationshipGuid,categoryGuid,businessId,path)values(#{relationshipGuid},#{categoryGuid},#{tableGuid},#{path})")
    public int addRelation(BusinessRelationEntity entity) throws SQLException;
}
