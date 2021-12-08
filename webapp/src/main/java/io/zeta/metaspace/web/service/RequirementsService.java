package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.ApiCateDTO;
import io.zeta.metaspace.model.dto.requirements.DealDetailDTO;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.ApiHead;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.requirements.RequirementsApiMapper;
import io.zeta.metaspace.web.dao.requirements.RequirementsResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RequirementsService {

    @Autowired
    private RequirementsApiMapper requirementsApiMapper;

    @Autowired
    private RequirementsResultMapper requirementsResultMapper;

    @Autowired
    DataShareDAO shareDAO;

    public DealDetailDTO getDealDetail(String id) {
        return requirementsResultMapper.queryDealDetail(id);
    }

    public List<ApiCateDTO> getCateategoryApis(String projectId, String categoryId, String search, String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setLimit(-1);
        List<ApiCateDTO> apiList = new ArrayList<>();
        String query = search;
        if (StringUtils.isNotBlank(query)) {
            parameters.setQuery(query.replaceAll("_", "/_").replaceAll("%", "/%"));
        }
        if (StringUtils.isBlank(categoryId)) {
            categoryId = null;
        }

        List<ApiHead> apiHeads;
        try {
            apiHeads = shareDAO.searchApi(parameters, projectId, categoryId, null, null, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询API列表异常：" + e.getMessage());
        }
        if (CollectionUtils.isNotEmpty(apiHeads)) {
            for (ApiHead head : apiHeads) {
                ApiCateDTO dto = new ApiCateDTO();
                dto.setId(head.getId());
                dto.setName(head.getName());
                apiList.add(dto);
            }
        }
        return apiList;
    }

    public List<ApiCateDTO> getCateategories(String projectId, String search, String tenantId) {
        List<ApiCateDTO> cateList;
        try {
            cateList = shareDAO.getCategories(projectId, tenantId, search);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询项目目录列表异常：" + e.getMessage());
        }
        return cateList;
    }


}
