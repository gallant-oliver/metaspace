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
 * @date 2019/9/18 10:33
 */
package io.zeta.metaspace.metadata.dao;

import io.zeta.metaspace.metadata.model.ColumnMetadata;
import io.zeta.metaspace.metadata.model.TableMetadata;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.share.APIInfoHeader;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/18 10:33
 */
public interface MetadataDAO {

    @Insert({"insert into table_metadata_history(guid,name,creator,update_time,database_name,table_type,partition_table,table_format,store_location,description,version,status)",
             "values(#{metadata.guid},#{metadata.name},#{metadata.creator},#{metadata.updateTime},#{metadata.databaseName},#{metadata.tableType},#{metadata.partitionTable},#{metadata.tableFormat},#{metadata.storeLocation},#{metadata.description},#{metadata.version},#{metadata.status})"})
    public int addTableMetadata(@Param("metadata")TableMetadata metadata);

    @Select({" <script>",
             " select guid,name,creator,update_time as updateTime,database_name as databaseName,table_type as tableType,partition_table as partitionTable,table_format as tableFormat,store_location as storeLocation,description,version,status,",
             " count(*)over as total",
             " from table_metadata_history where",
             " guid=#{guid}",
             " order by version desc" ,
             " <if test='limit != null and limit!=-1'>",
             " limit #{limit}",
             " </if>",
             " <if test='offset != null'>",
             " offset #{offset}",
             " </if>",
             " </script>"})
    public List<TableMetadata> getTableMetadataList(@Param("guid")String tableGuid, @Param("limit")int limit, @Param("offset") int offset);

    @Insert("insert into column_metadata_history(guid,name,type,table_guid,description,version,status)values(#{metadata.guid},#{metadata.name},#{metadata.type},#{metadata.tableGuid},,#{metadata.description},#{metadata.version},#{metadata.status})")
    public int addColumnMetadata(@Param("metadata")ColumnMetadata metadata);

    @Select({" <script>",
             " select guid,name,table_guid as tableGuid,type,description,version,status,",
             " count(*)over as total",
             " from table_metadata_history where",
             " guid=#{guid}",
             " order by version desc" ,
             " <if test='limit != null and limit!=-1'>",
             " limit #{limit}",
             " </if>",
             " <if test='offset != null'>",
             " offset #{offset}",
             " </if>",
             " </script>"})
    public List<TableMetadata> getColumnMetadataList(@Param("guid")String tableGuid, @Param("limit")int limit, @Param("offset") int offset);
}
