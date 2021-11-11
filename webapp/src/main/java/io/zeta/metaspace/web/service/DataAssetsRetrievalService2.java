package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dataassets.DomainInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.DataAssetsRetrievalDAO;
import org.apache.atlas.AtlasException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/11/9 17:27
 * @Description 数据资产检索
 */

@Service
public class DataAssetsRetrievalService2 {

    @Autowired
    DataAssetsRetrievalDAO dataAssetsRetrievalDAO;

    @Autowired
    DataAssetsRetrievalService dataAssetsRetrievalService;

    public PageResult<DomainInfo> getThemeDomains(int offset, int limit, String tenantId) throws AtlasException {
        List<DomainInfo> domainList;
        PageResult<DomainInfo> pageResult = new PageResult<>();
        int totalSize = 0;
        boolean isPublicTenant = dataAssetsRetrievalService.isPublicTenant(tenantId);
        boolean isPublicUser = dataAssetsRetrievalService.isGlobalUser();
        domainList = null;

        if (domainList.size() != 0) {
//            totalSize = domainList.get(0).getTotal();
        }


        pageResult.setTotalSize(totalSize);
        pageResult.setLists(domainList);
        pageResult.setCurrentSize(domainList.size());
        return pageResult;
    }
}
