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
 * @date 2019/7/2 20:38
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.SQLException;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/2 20:38
 */
public interface ColumnDAO {


    @Insert({"<script>",
            "insert into column_info(column_guid,column_name,table_guid,type,display_name,display_operator,display_updatetime,status)values",
            "<foreach collection='columnList' item='columnInfo' index='index'  separator=','>",
            "(#{columnInfo.columnId},#{columnInfo.columnName},#{columnInfo.tableId},#{columnInfo.type},#{columnInfo.displayName},#{columnInfo.displayNameOperator},#{columnInfo.displayNameUpdateTime},#{columnInfo.status})",
            "</foreach>",
            "</script>"})
    public int addColumnDisplayInfo(@Param("columnList")List<Column> columnList);

    @Select("select count(*) from columnInfo where table_guid=#{tableGuid} and status='ACTIVE'")
    public int tableColumnExist(@Param("tableGuid")String tableGuid);

    @Select("select column_guid as columnId,column_name as columnName,type, from column_info where table_guid=#{tableGuid} and status='ACTIVE'")
    public List<Column> getColumnInfoList(@Param("tableGuid")String tableGuid);

}
