package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataassets.*;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableRelation;
import org.apache.atlas.model.metadata.CategoryEntityV2;
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
                               @Param("query")String query);

    List<DataAssets> searchBusinesses(@Param("tenantId")String tenantId,
                                      @Param("userId")String userId,
                                      @Param("isPublic")boolean isPublic,
                                      @Param("isGlobal")boolean isGlobal,
                                      @Param("offset")int offset,
                                      @Param("limit")int limit,
                                      @Param("query")String query);

    List<DataAssets> searchTables(@Param("tenantId")String tenantId,
                                  @Param("userId")String userId,
                                  @Param("isPublic")boolean isPublic,
                                  @Param("isGlobal")boolean isGlobal,
                                  @Param("offset")int offset,
                                  @Param("limit")int limit,
                                  @Param("query")String query);

    DataAssets searchBusinessById(@Param("businessId")String businessId,
                                  @Param("tenantId")String tenantId);

    DataAssets searchTableById(@Param("tableId")String tableId,
                               @Param("tenantId")String tenantId,
                               @Param("businessId")String businessId);

    List<TableInfo> getTableInfos(@Param("businessId")String businessId,
                                  @Param("tenantId")String tenantId,
                                  @Param("offset")int offset,
                                  @Param("limit")int limit);

    List<ColumnInfo> getDeriveColumnInfo(@Param("columnIds")List<String> columnIds,
                                         @Param("tenantId")String tenantId,
                                         @Param("tableId")String tableId);

    List<GroupDeriveTableRelation> getTablePrivileges(@Param("tenantId")String tenantId,
                                                      @Param("tableIds")List<String> tableIds,
                                                      @Param("isPublic")boolean isPublic,
                                                      @Param("isGlobal")boolean isGlobal,
                                                      @Param("userId")String userId);

    List<DomainInfo> getDomainCategory();

    int getThemeNumber(@Param("guid") String guid);

    List<DomainInfo> getDomainCategoryByNotPublicUser(@Param("groupIdList") List<String> groupIdList,
                                                      @Param("userId") String userId,
                                                      @Param("tenantId") String tenantId);

    List<DomainInfo> getThemeByUserGroup(@Param("guid") String guid,
                                         @Param("groupIdList") List<String> groupIdList,
                                         @Param("userId") String userId,
                                         @Param("tenantId") String tenantId);

    List<ThemeInfo> getThemeCategory(@Param("guid") String guid);

    List<String> getBusinessId(@Param("guid") String guid);

    int getTableNumber(@Param("businessList") List<String> businessList);

    List<String> queryBusinessIdByUserGroup(@Param("categoryGuid") String categoryGuid,
                                            @Param("tenantId") String tenantId,
                                            @Param("userId") String userId);

    List<BussinessObject> queryBusiness(@Param("categoryGuid") String categoryGuid,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);

    List<BussinessObject> queryBusinessByUserGroup(@Param("tenantId") String tenantId,
                                                   @Param("categoryGuid") String categoryGuid,
                                                   @Param("userId") String userId, @Param("limit") int limit,
                                                   @Param("offset") int offset);

    CategoryEntityV2 queryCategoryInfo(@Param("guid") String guid);
}
