package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataassets.BussinessObject;
import io.zeta.metaspace.model.dataassets.DomainInfo;
import io.zeta.metaspace.model.dataassets.ThemeInfo;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/11/10 15:59
 * @Description
 */
public interface DataAssetsRetrievalDAO {


    List<DomainInfo> getDemainCategory();

    int getThemeNumber(@Param("guid") String guid);

    List<DomainInfo> getDemainCategoryByNotPublicUser(@Param("groupIdList") List<String> groupIdList, @Param("userId") String userId, @Param("tenantId") String tenantId);

    List<DomainInfo> getThemeByUserGroup(@Param("guid") String guid, @Param("groupIdList") List<String> groupIdList, @Param("userId") String userId, @Param("tenantId") String tenantId);

    List<ThemeInfo> getThemeCategory(@Param("guid") String guid);

    List<String> getBusinessId(@Param("guid") String guid);

    int getTableNumber(@Param("businessList") List<String> businessList);

    List<String> queryBusinessIdByUsergroup(@Param("categoryGuid") String categoryGuid, @Param("tenantId") String tenantId, @Param("userId") String userId);

    List<BussinessObject> queryBusiness(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset);

    List<BussinessObject> queryBusinessByUsergroup(@Param("tenantId") String tenantId, @Param("categoryGuid") String categoryGuid, @Param("userId") String userId, @Param("limit") int limit, @Param("offset") int offset);

    CategoryEntityV2 queryCategoryInfo(@Param("guid") String guid);
}
