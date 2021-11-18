package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataassets.ColumnInfo;
import io.zeta.metaspace.model.dataassets.DataAssets;
import io.zeta.metaspace.model.dataassets.TableInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/11/10 15:59
 * @Description
 */
public interface DataAssetsRetrievalDAO {
    List<DataAssets> searchAll(@Param("tenantId")String tenantId,
                               @Param("userId")String userId,
                               @Param("isPublic")boolean isPublic,
                               @Param("isGlobal")boolean isGlobal,
                               @Param("offset")int offset,
                               @Param("limit")int limit,
                               @Param("name")String name);

    List<DataAssets> searchBusinesses(@Param("tenantId")String tenantId,
                                      @Param("userId")String userId,
                                      @Param("isPublic")boolean isPublic,
                                      @Param("isGlobal")boolean isGlobal,
                                      @Param("offset")int offset,
                                      @Param("limit")int limit,
                                      @Param("name")String name);

    List<DataAssets> searchTables(@Param("tenantId")String tenantId,
                                  @Param("userId")String userId,
                                  @Param("isPublic")boolean isPublic,
                                  @Param("isGlobal")boolean isGlobal,
                                  @Param("offset")int offset,
                                  @Param("limit")int limit,
                                  @Param("name")String name);

    DataAssets searchBusinessById(@Param("businessId")String businessId,
                                  @Param("tenantId")String tenantId);

    DataAssets searchTableById(@Param("tableId")String tableId,
                               @Param("tenantId")String tenantId);

    List<TableInfo> getTableInfos(@Param("businessId")String businessId,
                                  @Param("tenantId")String tenantId,
                                  @Param("offset")int offset,
                                  @Param("limit")int limit);

    List<ColumnInfo> getDeriveColumnInfo(@Param("columnIds")List<String> columnIds,
                                         @Param("tenantId")String tenantId,
                                         @Param("tableId")String tableId);
}
