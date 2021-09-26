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
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.ColumnMetadata;
import io.zeta.metaspace.model.metadata.ColumnQuery;
import io.zeta.metaspace.model.metadata.TableMetadata;
import io.zeta.metaspace.model.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/18 10:33
 */
public interface MetadataHistoryDAO {

    @Insert({"insert into table_metadata_history(guid,name,creator,updater,create_time,update_time,database_name,table_type,partition_table,table_format,store_location,description,status,version)",
            "values(#{metadata.guid},#{metadata.name},#{metadata.creator},#{metadata.updater},#{metadata.createTime},#{metadata.updateTime},#{metadata.databaseName},#{metadata.tableType},#{metadata.partitionTable},#{metadata.tableFormat},#{metadata.storeLocation},#{metadata.description},#{metadata.status},",
            "COALESCE((select max(version)+1 from table_metadata_history where guid=#{metadata.guid} GROUP BY guid),1))"})
    public int addTableMetadata(@Param("metadata")TableMetadata metadata);

    @Select("select max(version) from table_metadata_history where guid=#{tableGuid}")
    public int getTableVersion(@Param("tableGuid")String tableGuid);

    @Select("select count(*) from table_metadata_history where guid=#{metadata.guid} and create_time=#{metadata.createTime} and update_time=#{metadata.updateTime}")
    public int getSameUpdateEntityCount(@Param("metadata")TableMetadata metadata);

    @Select({" <script>",
            " select guid,name,creator,updater,create_time as createTime, update_time as updateTime,database_name as databaseName,table_type as tableType,partition_table as partitionTable,table_format as tableFormat,store_location as storeLocation,description,version,status,",
            " count(*)over() as total",
            " from table_metadata_history where",
            " guid=#{guid} and version!=(select max(version) from table_metadata_history where guid=#{guid})",
            " order by version desc" ,
            " <if test='limit != null and limit!=-1'>",
            " limit #{limit}",
            " </if>",
            " <if test='offset != null'>",
            " offset #{offset}",
            " </if>",
            " </script>"})
    public List<TableMetadata> getTableMetadataList(@Param("guid")String tableGuid, @Param("limit")int limit, @Param("offset") int offset);

    @Insert({"<script>",
            "insert into column_metadata_history(guid,name,type,table_guid,description,status,partition_field,creator,updater,create_time,update_time,version)values",
            "<foreach collection='metadataList' item='metadata' index='index'  separator=','>",
            "(#{metadata.guid},#{metadata.name},#{metadata.type},#{metadata.tableGuid},#{metadata.description},#{metadata.status},#{metadata.partitionField},",
            "#{metadata.creator},#{metadata.updater},#{metadata.createTime},#{metadata.updateTime},#{metadata.version})",
            "</foreach>",
            "</script>"})
    public int addColumnMetadata(@Param("metadataList")List<ColumnMetadata> metadataList);

    @Select({" <script>",
            " select guid,name,table_guid as tableGuid,type,description,version,status,partition_field as partitionField,",
            " creator,updater,create_time as createTime,update_time as updateTime,",
            " count(*)over() as total",
            " from column_metadata_history where",
            " table_guid=#{guid} and version=#{version}",
            " <if test=\"query!=null and query.columnName != null and query.columnName!=''\">",
            " and name like concat('%',#{query.columnName},'%') ESCAPE '/'",
            " </if>",
            " <if test=\"query!=null and query.type != null and query.type!=''\">",
            " and type like concat('%',#{query.type},'%') ESCAPE '/'",
            " </if>",
            " <if test=\"query!=null and query.description != null and query.description!=''\">",
            " and description like concat('%',#{query.description},'%') ESCAPE '/'",
            " </if>",
            " order by version desc" ,
            " </script>"})
    public List<ColumnMetadata> getColumnMetadataList(@Param("guid")String tableGuid, @Param("version")Integer version, @Param("query")ColumnQuery.ColumnFilter query);

    @Select({"select guid,name,creator,updater,create_time as createTime, update_time as updateTime,database_name as databaseName,table_type as tableType,partition_table as partitionTable,table_format as tableFormat,store_location as storeLocation,description,version,status",
            " from table_metadata_history where guid=#{guid} and version=#{version}"})
    public TableMetadata getTableMetadata(@Param("guid")String tableGuid, @Param("version")Integer version);

    @Select({"select guid,name,creator,updater,create_time as createTime, update_time as updateTime,database_name as databaseName,table_type as tableType,partition_table as partitionTable,table_format as tableFormat,store_location as storeLocation,description,version,status",
            " from table_metadata_history where guid=#{guid} and version=(select max(version) from table_metadata_history where guid=#{guid})"})
    public TableMetadata getLastTableMetadata(@Param("guid")String tableGuid);

    @Select({" select guid,name,table_guid as tableGuid,type,description,version,status,partition_field as partitionField,",
            " creator,updater,create_time as createTime,update_time as updateTime",
            " from column_metadata_history where table_guid=#{guid} and version=(select max(version) from column_metadata_history where table_guid=#{guid})"})
    public List<ColumnMetadata> getLastColumnMetadata(@Param("guid")String tableGuid);

    @Select({" <script>",
            " select guid,name,table_guid as tableGuid,type,description,version,status,partition_field as partitionField,",
            " creator,updater,create_time as createTime,update_time as updateTime",
            " from column_metadata_history where",
            " table_guid=#{guid} and version=#{version}",
            " </script>"})
    public List<ColumnMetadata> getColumnMetadata(@Param("guid")String tableGuid, @Param("version")Integer version);

    @Select({"select guid,name,creator,updater,create_time as createTime, update_time as updateTime,database_name as databaseName,table_type as tableType,partition_table as partitionTable,table_format as tableFormat,store_location as storeLocation,description,version,status",
            " from table_metadata_history where guid=#{guid} order by version desc "})
    List<TableMetadata> getTableMetadataByGuid(@Param("guid")String tableGuid);
}
