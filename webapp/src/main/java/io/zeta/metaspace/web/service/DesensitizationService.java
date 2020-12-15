package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.datasource.DataSourceHead;
import io.zeta.metaspace.model.desensitization.DesensitizationAlgorithm;
import io.zeta.metaspace.model.desensitization.DesensitizationAlgorithmInfo;
import io.zeta.metaspace.model.desensitization.DesensitizationAlgorithmTest;
import io.zeta.metaspace.model.desensitization.DesensitizationRule;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiPolyInfo;
import io.zeta.metaspace.web.dao.DesensitizationDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DesensitizationService {


    @Autowired
    private DesensitizationDAO desensitizationDAO;

    public void checkDuplicateName(String id, String name, String tenantId) {
        if (desensitizationDAO.countByName(id, name, tenantId) != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则名称已存在");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int createDesensitizationRule(DesensitizationRule desensitizationRule, String tenantId) {
        try {
            DesensitizationAlgorithm algorithm = desensitizationRule.getType();
            if (algorithm.getParamNames().length != desensitizationRule.getParams().size()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数数目不正确");
            }
            if (algorithm.getVerifyParam() != null && !algorithm.getVerifyParam().apply(desensitizationRule.getParams())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确:" + desensitizationRule.getParams().toString());
            }

            String userId = AdminUtils.getUserData().getUserId();
            desensitizationRule.setCreateTime(new Timestamp(System.currentTimeMillis()));
            desensitizationRule.setUpdateTime(desensitizationRule.getCreateTime());

            return desensitizationDAO.insert(userId, desensitizationRule, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "脱敏规则创建失败 : " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDesensitizationRule(DesensitizationRule desensitizationRule, String tenantId) {
        try {
            if (StringUtils.isEmpty(desensitizationRule.getId()) || StringUtils.isEmpty(desensitizationRule.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            if (desensitizationDAO.getRule(desensitizationRule.getId()) == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则不存在");
            }
            checkDuplicateName(desensitizationRule.getId(), desensitizationRule.getName(), tenantId);

            DesensitizationAlgorithm algorithm = desensitizationRule.getType();
            if (algorithm.getVerifyParam() != null && !algorithm.getVerifyParam().apply(desensitizationRule.getParams())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确:" + desensitizationRule.getParams().toString());
            }
            return desensitizationDAO.update(desensitizationRule);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "脱敏规则更新失败 : " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDesensitizationRuleEnable(String ruleId, Boolean enable, String tenantId) {
        try {
            if (StringUtils.isEmpty(ruleId) || enable == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            DesensitizationRule rule = desensitizationDAO.getRule(ruleId);
            if (rule == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则不存在");
            }

            return desensitizationDAO.updateEnable(ruleId, enable);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "脱敏规则更新启用禁用失败 : " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int deletedDesensitizationRule(List<String> ruleIds, String tenantId) {
        try {
            if (ruleIds != null && !ruleIds.isEmpty()) {
                for (String ruleId : ruleIds) {
                    if (desensitizationDAO.getRule(ruleId) == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则不存在 id : " + ruleId);
                    }
                }
            }
            return desensitizationDAO.delete(ruleIds);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "脱敏规则删除失败 : " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public DesensitizationRule getDesensitizationRule(String id) {
        try {
            if (desensitizationDAO.getRule(id) == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则不存在 id : " + id);
            }
            return desensitizationDAO.getRule(id);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "脱敏规则获取失败 : " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public PageResult<DesensitizationRule> getDesensitizationRuleList(Parameters parameters, Boolean enable, String tenantId) {
        try {
            PageResult<DesensitizationRule> pageResult = new PageResult<>();
            List<DesensitizationRule> result = desensitizationDAO.getRules(parameters, enable, tenantId);
            pageResult.setCurrentSize(result.size());
            pageResult.setLists(result);
            pageResult.setTotalSize(result.size() == 0 ? 0 : result.get(0).getTotal());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "脱敏规则获取失败 : " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<ApiPolyInfo> getDesensitizationApiPolyInfoList(String ruleId, Parameters parameters, String status, String tenantId) {
        try {
            PageResult<ApiPolyInfo> pageResult = new PageResult<>();
            List<ApiPolyInfo> result = desensitizationDAO.getApiPolyInfoList(ruleId, AdminUtils.getUserData().getUserId(), tenantId, parameters, status, AtlasConfiguration.METASPACE_API_POLY_EFFECTIVE_TIME.getLong());
            pageResult.setCurrentSize(result.size());
            pageResult.setLists(result);
            pageResult.setTotalSize(result.size() == 0 ? 0 : result.get(0).getTotal());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "脱敏规则获取关联API失败 : " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public List<DesensitizationAlgorithmInfo> getDesensitizationAlgorithm() {
        return Arrays.stream(DesensitizationAlgorithm.values())
                .collect(Collectors.groupingBy(DesensitizationAlgorithm::getType))
                .entrySet()
                .stream()
                .map(e -> new DesensitizationAlgorithmInfo(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }


    @Transactional(rollbackFor = Exception.class)
    public Object testDesensitizationAlgorithm(DesensitizationAlgorithmTest desensitizationAlgorithmTest) {
        try {
            DesensitizationAlgorithm algorithm = desensitizationAlgorithmTest.getType();
            if (desensitizationAlgorithmTest.getType() == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "脱敏算法不存在");
            }
            if (algorithm.getParamNames().length != desensitizationAlgorithmTest.getParams().size()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数数目不正确");
            }

            if (algorithm.getVerifyParam() != null && !algorithm.getVerifyParam().apply(desensitizationAlgorithmTest.getParams())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确:" + desensitizationAlgorithmTest.getParams().toString());
            }
            return algorithm.getHandle().apply(desensitizationAlgorithmTest.getField(), desensitizationAlgorithmTest.getParams());

        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "测试脱敏规则失败:" + e.getMessage());
        }
    }

}

