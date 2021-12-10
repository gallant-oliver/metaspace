package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsApiPO;
import io.zeta.metaspace.model.po.requirements.RequirementsDatabasePO;
import io.zeta.metaspace.model.po.requirements.RequirementsMqPO;
import io.zeta.metaspace.model.share.ApiHead;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.requirements.*;
import io.zeta.metaspace.web.util.AdminUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class RequirementsService {

    @Autowired
    private RequirementsMapper requirementsMapper;
    @Autowired
    private RequirementsApiMapper requirementsApiMapper;
    @Autowired
    private RequirementsResultMapper requirementsResultMapper;
    @Autowired
    private RequirementsDatabaseMapper requirementsDatabaseMapper;
    @Autowired
    private RequirementsMqMapper requirementsMqMapper;

    @Autowired
    DataShareDAO shareDAO;

    public DealDetailDTO getDealDetail(String id) {
        return requirementsResultMapper.queryDealDetail(id);
    }

    public List<ApiCateDTO> getCateategoryApis(String projectId, String categoryId, String search, String tenantId) {
        if (StringUtils.isBlank(projectId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目id不能为空");
        }
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


    public void feedback(RequirementsFeedbackCommit commitInput) {
        //必输参数校验
        String requirementsId = commitInput.getRequirementsId();
        Integer resourceType = commitInput.getResourceType();

        if (StringUtils.isBlank(requirementsId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "需求id不能为空");
        }

        if (Objects.isNull(resourceType)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "资源类型不能为空");
        }

        if (resourceType == ResourceType.API.getCode()) {
            RequirementsApiCommitDTO apiInput = commitInput.getApi();
            if (null == apiInput) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API入参为空");
            }
            String projectId = apiInput.getProjectId();
            String categoryId = apiInput.getCategoryId();
            String apiId = apiInput.getApiId();
            if (StringUtils.isBlank(projectId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目id为空");
            }
            if (StringUtils.isBlank(apiId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API id为空");
            }
            RequirementsApiPO record = new RequirementsApiPO();
            record.setGuid(UUID.randomUUID().toString());
            record.setRequirementsId(requirementsId);
            record.setProjectId(projectId);
            record.setCategoryId(categoryId);
            record.setApiId(apiId);
            record.setDescription(apiInput.getDescription());
            Timestamp now = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
            record.setCreateTime(now);
            record.setUpdateTime(now);
            requirementsApiMapper.insert(record);
        }

        String userId = AdminUtils.getUserData().getUserId();
        if (resourceType == ResourceType.TABLE.getCode()) {
            RequirementsDatabaseCommitDTO databaseInput = commitInput.getDatabase();
            if (null == databaseInput) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "中间库入参不能为空");
            }
            String middleType = databaseInput.getMiddleType();
            String database = databaseInput.getDatabase();
            String tableNameEn = databaseInput.getTableNameEn();
            String tableNameCh = databaseInput.getTableNameCh();
            Integer status = databaseInput.getStatus();
            if (StringUtils.isBlank(middleType)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "中间库类型不能为空");
            }
            if (StringUtils.isBlank(database)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库名称不能为空");
            }
            if (StringUtils.isBlank(tableNameEn)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据表英文名称不能为空");
            }
            if (StringUtils.isBlank(tableNameCh)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据表中文名称不能为空");
            }
            if (null == status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "状态不能为空");
            }

            RequirementsDatabasePO record = new RequirementsDatabasePO();
            record.setGuid(UUID.randomUUID().toString());
            record.setRequirementsId(requirementsId);
            record.setMiddleType(middleType);
            record.setDatabase(database);
            record.setTableNameEn(tableNameEn);
            record.setTableNameCh(tableNameCh);
            record.setTableNameCh(tableNameCh);
            record.setStatus(status);
            record.setDescription(databaseInput.getDescription());
            Timestamp now = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
            record.setCreateTime(now);
            record.setUpdateTime(now);
            record.setCreator(userId);
            requirementsDatabaseMapper.insert(record);
        }


        if (resourceType == ResourceType.MESSAGE_QUEUE.getCode()) {
            RequirementsMqCommitDTO mqInput = commitInput.getMq();
            if (null == mqInput) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "消息队列入参不能为空");
            }
            String mqNameEn = mqInput.getMqNameEn();
            String mqNameCh = mqInput.getMqNameCh();
            String format = mqInput.getFormat();
            Integer status = mqInput.getStatus();
            if (StringUtils.isBlank(mqNameEn)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "消息队列英文名称不能为空");
            }
            if (StringUtils.isBlank(mqNameCh)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "消息队列中文名称不能为空");
            }
            if (StringUtils.isBlank(format)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "格式不能为空");
            }
            if (null == status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "状态不能为空");
            }

            RequirementsMqPO record = new RequirementsMqPO();
            record.setGuid(UUID.randomUUID().toString());
            record.setRequirementsId(requirementsId);
            record.setMqNameEn(mqNameEn);
            record.setMqNameCh(mqNameCh);
            record.setFormat(format);
            record.setStatus(status);
            record.setDescription(mqInput.getDescription());
            Timestamp now = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
            record.setCreateTime(now);
            record.setUpdateTime(now);
            record.setCreator(userId);
            requirementsMqMapper.insert(record);
        }


    }
}
