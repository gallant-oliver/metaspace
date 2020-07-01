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
package io.zeta.metaspace.web.service.dataquality;

import io.zeta.metaspace.model.dataquality2.DataTaskIdAndName;
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.dataquality2.RuleTemplateType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.DataStandardDAO;
import io.zeta.metaspace.web.dao.dataquality.RuleDAO;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeansUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleService {

    private static final Logger LOG = LoggerFactory.getLogger(RuleService.class);

    @Autowired
    private RuleDAO ruleDAO;

    @Autowired
    private DataStandardDAO dataStandardDAO;

    @Autowired
    private DataManageService dataManageService;
    @Autowired
    CategoryDAO categoryDAO;

    public int insert(Rule rule,String tenantId) throws AtlasBaseException {
        try {
            rule.setId(UUID.randomUUID().toString());
            rule.setCreateTime(DateUtils.currentTimestamp());
            rule.setUpdateTime(DateUtils.currentTimestamp());
            rule.setCreator(AdminUtils.getUserData().getUserId());
            rule.setDelete(false);
            return ruleDAO.insert(rule,tenantId);
        } catch (Exception e) {
            LOG.error("添加规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加规则失败");
        }
    }

    public Rule getById(String id,String tenantId) throws AtlasBaseException {
        try {
            Rule rule = ruleDAO.getById(id);
            if(Objects.isNull(rule)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到规则详情");
            }
            String path = CategoryRelationUtils.getPath(rule.getCategoryId(),tenantId);
            rule.setPath(path);
            return rule;
        } catch (Exception e) {
            LOG.error("获取规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加规则失败");
        }
    }

    public List<Rule> getByCode(String code,String tenantId) throws AtlasBaseException {
        return ruleDAO.getByCode(code,tenantId);
    }

    public List<Rule> getByCodeV2(String id,String code,String tenantId) throws AtlasBaseException {
        return ruleDAO.getByCodeV2(id,code,tenantId);
    }

    public List<Rule> getByName(String name,String tenantId) throws AtlasBaseException {
        return ruleDAO.getByName(name,tenantId);
    }
    public List<Rule> getByNameV2(String id,String name,String tenantId) throws AtlasBaseException {
        return ruleDAO.getByNameV2(id,name,tenantId);
    }

    @Transactional(rollbackFor=Exception.class)
    public void deleteById(String number) throws AtlasBaseException {
        try {
            Boolean enableStatus = ruleDAO.getEnableStatusById(number);
            if (true == enableStatus) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则已被启用，不允许删除");
            }
            dataStandardDAO.deleteByRuleId(number);
            ruleDAO.deleteById(number);
        } catch (Exception e) {
            LOG.error("删除规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除规则失败");
        }
    }

    public void deleteByIdList(List<String> numberList) throws AtlasBaseException {
        try {
            for (String number : numberList) {
                Boolean enableStatus = ruleDAO.getEnableStatusById(number);
                if (true == enableStatus) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在已被启用规则，不允许删除");
                }
            }
            ruleDAO.deleteByIdList(numberList);
        } catch (Exception e) {
            LOG.error("删除规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除规则失败");
        }
    }

    public int update(Rule rule,String tenantId) throws AtlasBaseException {
        try {
            List<Rule> byCode = getByCodeV2(rule.getId(),rule.getCode(), tenantId);
            if (byCode.size()>0){
                throw new AtlasBaseException("规则编号已存在");
            }
            List<Rule> byName = getByNameV2(rule.getId(),rule.getCode(), tenantId);
            if (byName.size()>0){
                throw new AtlasBaseException("规则名字已存在");
            }
            rule.setUpdateTime(DateUtils.currentTimestamp());
            Rule old = getById(rule.getId(),tenantId);
            BeansUtil.copyPropertiesIgnoreNull(rule, old);
            return ruleDAO.update(old);
        } catch (Exception e) {
            LOG.error("更新规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新规则失败");
        }
    }

    public PageResult<Rule> queryPageByCatetoryId(String categoryId, Parameters params,String tenantId) throws AtlasBaseException {
        try {
            List<Rule> list = queryByCatetoryId(categoryId, params,tenantId);
            PageResult<Rule> pageResult = new PageResult<>();
            long sum = 0;
            if (list.size() != 0) {
                sum = list.get(0).getTotal();
            }
            pageResult.setTotalSize(sum);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则失败");
        }
    }

    private List<Rule> queryByCatetoryId(String categoryId, Parameters params,String tenantId) throws AtlasBaseException {
        try {
            String path = CategoryRelationUtils.getPath(categoryId,tenantId);
            Map<Integer, String> ruleTemplateCategoryMap = new HashMap();
            RuleTemplateType.all().stream().forEach(ruleTemplateType -> {
                ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
            });
            List<Rule> rules = ruleDAO.queryByCatetoryId(categoryId, params, tenantId);
            List<Rule> list = rules
                    .stream()
                    .map(rule -> {
                        rule.setPath(path);
                        String ruleTypeName = ruleTemplateCategoryMap.get(rule.getRuleType());
                        rule.setRuleTypeName(ruleTypeName);
                        return rule;
                    }).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            LOG.error("获取规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则失败");
        }
    }

    public PageResult<Rule> search(Parameters params,String tenantId) throws AtlasBaseException {
        try {
            Map<Integer, String> ruleTemplateCategoryMap = new HashMap();
            RuleTemplateType.all().stream().forEach(ruleTemplateType -> {
                ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
            });
            List<Rule> list = ruleDAO.search(params,tenantId)
                    .stream()
                    .map(rule -> {
                        String path = null;
                        try {
                            path = CategoryRelationUtils.getPath(rule.getCategoryId(),tenantId);
                            rule.setPath(path);
                            String ruleTypeName = ruleTemplateCategoryMap.get(rule.getRuleType());
                            rule.setRuleTypeName(ruleTypeName);
                        } catch (AtlasBaseException e) {
                            LOG.error(e.getMessage(), e);
                        }
                        rule.setPath(path);
                        return rule;
                    }).collect(Collectors.toList());

            PageResult<Rule> pageResult = new PageResult<>();
            long sum = 0;
            if (list.size() != 0) {
                sum = list.get(0).getTotal();
            }
            pageResult.setTotalSize(sum);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("搜索异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索异常");
        }
    }

    public List<CategoryPrivilege> getAll(Integer categoryType,String tenantId) throws AtlasBaseException {
        try {
            List<CategoryPrivilege> resultList = TenantService.defaultTenant.equals(tenantId) ?dataManageService.getAll(categoryType) : dataManageService.getAllByUserGroup(categoryType, tenantId) ;
            for (CategoryPrivilege res : resultList) {
                Integer count = ruleDAO.getCategoryObjectCount(res.getGuid(),tenantId);
                res.setObjectCount(count);
            }
            return resultList;
        } catch (Exception e) {
            LOG.error("获取目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取目录失败");
        }
    }

    public int updateRuleStatus(String id, Boolean enable) throws AtlasBaseException {
        try {
            return ruleDAO.updateRuleStatus(id, enable);
        } catch (Exception e) {
            LOG.error("更新规则状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新规则状态失败");
        }
    }

    public List<RuleTemplate> getAllRuleTemplateList() throws AtlasBaseException {
        try {
            return ruleDAO.getAllRuleTemplateList();
        } catch (Exception e) {
            LOG.error("获取规则模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则模板失败");
        }
    }

    public void deleteCategory(String categoryGuid,String tenantId) throws AtlasBaseException {
        try {
            int count = ruleDAO.getCategoryObjectCount(categoryGuid,tenantId);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该分组下存在关联规则，不允许删除");
            }
            int childrenNum = categoryDAO.queryChildrenNum(categoryGuid,tenantId);
            if (childrenNum > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录下存在子目录");
            }
            dataManageService.deleteCategory(categoryGuid,tenantId,4);
        } catch (Exception e) {
            LOG.error("删除目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    public String getCategoryName(String categoryGuid,String tenantId) {
        return ruleDAO.getCategoryName(categoryGuid,tenantId);
    }

    public String getNameById(String id) {
        return ruleDAO.getNameById(id);
    }

    public List<DataTaskIdAndName> getRuleUsed(List<String> ids){
        if (ids==null||ids.size()==0){
            return new ArrayList<>();
        }
        return ruleDAO.getRuleUsed(ids);
    }
}
