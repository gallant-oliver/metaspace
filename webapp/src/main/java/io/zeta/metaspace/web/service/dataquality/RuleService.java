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

import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.DataStandardDAO;
import io.zeta.metaspace.web.dao.dataquality.RuleDAO;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeansUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RuleService {
    
    @Autowired
    private RuleDAO ruleDAO;
    @Autowired
    private DataStandardDAO dataStandardDAO;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private CategoryDAO categoryDAO;
    
    public int insert(Rule rule, String tenantId) throws AtlasBaseException {
        try {
            rule.setId(UUID.randomUUID().toString());
            rule.setCreateTime(DateUtils.currentTimestamp());
            rule.setUpdateTime(DateUtils.currentTimestamp());
            rule.setCreator(AdminUtils.getUserData().getUserId());
            rule.setDelete(false);
            return ruleDAO.insert(rule, tenantId);
        } catch (Exception e) {
            log.error("添加规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加规则失败");
        }
    }
    
    public Rule getById(String id, String tenantId) throws AtlasBaseException {
        try {
            Rule rule = ruleDAO.getRuleTemplate(id, tenantId);
            if (Objects.isNull(rule)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到规则详情");
            }
            String path = CategoryRelationUtils.getPath(rule.getCategoryId(), tenantId);
            rule.setPath(path);
            return rule;
        } catch (Exception e) {
            log.error("获取规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加规则失败");
        }
    }
    
    public List<Rule> getByCode(String code, String tenantId) throws AtlasBaseException {
        return ruleDAO.getByCode(code, tenantId);
    }
    
    private List<Rule> getByCodeV2(String id, String code, String tenantId) throws AtlasBaseException {
        return ruleDAO.getByCodeV2(id, code, tenantId);
    }
    
    public List<Rule> getByName(String name, String tenantId) throws AtlasBaseException {
        return ruleDAO.getByName(name, tenantId);
    }
    
    private List<Rule> getByNameV2(String id, String name, String tenantId) throws AtlasBaseException {
        return ruleDAO.getByNameV2(id, name, tenantId);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String number) throws AtlasBaseException {
        Boolean enableStatus = ruleDAO.getEnableStatusById(number);
        if (true == enableStatus) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则已被启用，不允许删除");
        }
        dataStandardDAO.deleteByRuleId(number);
        ruleDAO.deleteById(number);
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
            log.error("删除规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除规则失败");
        }
    }
    
    public int update(Rule rule,String tenantId) throws AtlasBaseException {
        try {
            List<Rule> byCode = getByCodeV2(rule.getId(),rule.getCode(), tenantId);
            if (byCode.size()>0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"规则编号已存在");
            }
            List<Rule> byName = getByNameV2(rule.getId(),rule.getCode(), tenantId);
            if (byName.size()>0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"规则名字已存在");
            }
            rule.setUpdateTime(DateUtils.currentTimestamp());
            Rule old = getById(rule.getId(),tenantId);
            BeansUtil.copyPropertiesIgnoreNull(rule, old);
            return ruleDAO.update(old);
        }catch (AtlasBaseException e){
            throw e;
        }catch (Exception e) {
            log.error("更新规则失败", e);
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
            log.error("获取规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则失败");
        }
    }
    
    private List<Rule> queryByCatetoryId(String categoryId, Parameters params,String tenantId) throws AtlasBaseException {
        try {
            String path = CategoryRelationUtils.getPath(categoryId,tenantId);
            Map<String, String> ruleTemplateCategoryMap = new HashMap();
            RuleTemplateType.all().stream().forEach(ruleTemplateType -> {
                ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
            });
            List<Rule> rules = ruleDAO.queryByCatetoryId(categoryId, params, tenantId);
            List<Rule> list = rules
                    .stream()
                    .map(rule -> {
                        rule.setPath(path);
                        String ruleTypeName = ruleTemplateCategoryMap.get(String.valueOf(rule.getRuleType()));
                        rule.setRuleTypeName(ruleTypeName);
                        return rule;
                    }).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            log.error("获取规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则失败");
        }
    }
    
    public PageResult<Rule> search(Parameters params,String tenantId) throws AtlasBaseException {
        try {
            Map<String, String> ruleTemplateCategoryMap = new HashMap();
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
                            String ruleTypeName = ruleTemplateCategoryMap.get(String.valueOf(rule.getRuleType()));
                            rule.setRuleTypeName(ruleTypeName);
                        } catch (AtlasBaseException e) {
                            log.error(e.getMessage(), e);
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
            log.error("搜索异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索异常");
        }
    }
    
    public List<CategoryPrivilege> getAll(Integer categoryType,String tenantId) throws AtlasBaseException {
        try {
            List<CategoryPrivilege> resultList = dataManageService.getAllByUserGroup(categoryType, tenantId) ;
            String parentPattern = "^rule_([0-9])";
            for (CategoryPrivilege res : resultList) {
                if (res.getGuid().matches(parentPattern)){
                    res.getPrivilege().setEdit(false);
                    res.getPrivilege().setDelete(false);
                }
                Integer count = ruleDAO.getCategoryObjectCount(res.getGuid(),tenantId);
                res.setObjectCount(count);
            }
            return resultList;
        } catch (Exception e) {
            log.error("获取目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取目录失败");
        }
    }
    
    public int updateRuleStatus(String id, Boolean enable,String tenantId) throws AtlasBaseException {
        try {
            return ruleDAO.updateRuleStatus(id, enable,tenantId);
        } catch (Exception e) {
            log.error("更新规则状态失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新规则状态失败");
        }
    }
    
    public List<RuleTemplate> getAllRuleTemplateList() throws AtlasBaseException {
        try {
            return ruleDAO.getAllRuleTemplateList();
        } catch (Exception e) {
            log.error("获取规则模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则模板失败");
        }
    }
    
    public void deleteCategory(String categoryGuid,String tenantId) throws Exception {
        int count = ruleDAO.getCategoryObjectCount(categoryGuid,tenantId);
        if(count > 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该分组下存在关联规则，不允许删除");
        }
        int childrenNum;
        childrenNum = categoryDAO.queryChildrenNum(categoryGuid,tenantId);
        if (childrenNum > 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录下存在子目录");
        }
        dataManageService.deleteCategory(categoryGuid,tenantId,4);
    }
    
    public String getCategoryName(String categoryGuid,String tenantId) {
        return ruleDAO.getCategoryName(categoryGuid,tenantId);
    }
    
    public String getNameById(String id,String tenantId) {
        return ruleDAO.getNameById(id,tenantId);
    }
    
    public List<DataTaskIdAndName> getRuleUsed(List<String> ids){
        if (ids==null||ids.size()==0){
            return new ArrayList<>();
        }
        return ruleDAO.getRuleUsed(ids);
    }
    
    public List<RuleStatistics> getRuleExecuteStatistics(Timestamp startTime, Timestamp endTime) {
        List<String> systemRules = RuleTemplateType.all().stream().map(RuleTemplateType::getRuleType).collect(Collectors.toList());
        
        List<RuleStatistics> rulesStatistics = ruleDAO.getRulesStatistics(systemRules, startTime, endTime);
        Map<String, RuleStatistics> rulesStatisticsMap =  rulesStatistics.stream().collect(Collectors.toMap(RuleStatistics::getRuleType, t -> t));
        
        return RuleTemplateType.all().stream().map(ruleTemplateType -> {
            RuleStatistics ruleStatistics = rulesStatisticsMap.get(ruleTemplateType.getRuleType());
            if (ruleStatistics == null) {
                ruleStatistics = new RuleStatistics();
                ruleStatistics.setRuleType(ruleTemplateType.getRuleType());
                ruleStatistics.setCount(0);
            }
            ruleStatistics.setRuleName(ruleTemplateType.getStatisticsDisplayName());
            return ruleStatistics;
        }).collect(Collectors.toList());
        
    }
}
