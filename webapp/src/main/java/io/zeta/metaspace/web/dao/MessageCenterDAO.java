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
 * @date 2019/7/24 9:53
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataassets.DataAssets;
import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RuleParameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.usergroup.DBInfo;
import io.zeta.metaspace.web.typeHandler.ListStringTypeHandler;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 9:53
 */
public interface MessageCenterDAO {

    /**
     * 我的消息分页列表
     *
     * @param tenantId
     * @return
     */
    List<Map<String, Object>> getMyMessageList(@Param("type") Integer type, @Param("tenantId") String tenantId, @Param("userId") String userId, @Param("status") Integer status, @Param("search") String search, @Param("offset") long offset, @Param("limit") long limit) throws SQLException;

    /**
     * 获取消息详情
     *
     * @param id
     * @param tenantId
     * @param userId
     * @return
     * @throws SQLException
     */
    Map<String, Object> getMessageDetail(@Param("id") String id, @Param("tenantId") String tenantId, @Param("userId") String userId) throws SQLException;

    /**
     * 获取未读消息数量
     *
     * @param tenantId
     * @param userId
     * @return
     * @throws SQLException
     */
    int getUnReadNum(@Param("type") Integer type, @Param("tenantId") String tenantId, @Param("userId") String userId) throws SQLException;

    /**
     * 批量标记已读
     *
     * @param list
     * @return
     */
    int batchToRead(@Param("list") List<String> list) throws SQLException;

    /**
     * 批量删除消息
     *
     * @param list
     * @return
     */
    int batchDelte(@Param("list") List<String> list, @Param("tenantId") String tenantId, @Param("userId") String userId, @Param("delAll") String delAll) throws SQLException;

    void addMessage(@Param("message") MessageEntity messageEntity) throws SQLException;
}
