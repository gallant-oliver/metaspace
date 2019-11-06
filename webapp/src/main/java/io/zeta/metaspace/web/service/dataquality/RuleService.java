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

import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.dataquality2.RuleTemplateType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.DataStandardDAO;
import io.zeta.metaspace.web.dao.dataquality.RuleDAO;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataStandardService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeansUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class RuleService {

    private static final Logger LOG = LoggerFactory.getLogger(RuleService.class);

    @Autowired
    private RuleDAO ruleDAO;

    @Autowired
    private DataStandardDAO dataStandardDAO;

    @Autowired
    private DataManageService dataManageService;

    public int insert(Rule rule) throws AtlasBaseException {
        rule.setId(UUIDUtils.alphaUUID());
        rule.setCreateTime(DateUtils.currentTimestamp());
        rule.setUpdateTime(DateUtils.currentTimestamp());
        rule.setCreator(AdminUtils.getUserData().getUserId());
        rule.setDelete(false);
        return ruleDAO.insert(rule);
    }

    public Rule getById(String id) throws AtlasBaseException {
        try {
            Rule rule = ruleDAO.getById(id);
            if(Objects.isNull(rule)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到规则详情");
            }
            String path = CategoryRelationUtils.getPath(rule.getCategoryId());
            rule.setPath(path);
            return rule;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public List<Rule> getByCode(String code) throws AtlasBaseException {
        return ruleDAO.getByCode(code);
    }

    @Transactional
    public void deleteById(String number) throws AtlasBaseException {
        Boolean enableStatus = ruleDAO.getEnableStatusById(number);
        if(true==enableStatus) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "规则已被启用，不允许删除");
        }
        dataStandardDAO.deleteByRuleId(number);
        ruleDAO.deleteById(number);
    }

    public void deleteByIdList(List<String> numberList) throws AtlasBaseException {
        for (String number : numberList) {
            Boolean enableStatus = ruleDAO.getEnableStatusById(number);
            if(true==enableStatus) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在已被启用规则，不允许删除");
            }
        }
        ruleDAO.deleteByIdList(numberList);
    }

    public int update(Rule rule) throws AtlasBaseException {
        try {
            rule.setUpdateTime(DateUtils.currentTimestamp());
            Rule old = getById(rule.getId());
            BeansUtil.copyPropertiesIgnoreNull(rule, old);
            return ruleDAO.update(old);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public PageResult<Rule> queryPageByCatetoryId(String categoryId, Parameters params) throws AtlasBaseException {
        List<Rule> list = queryByCatetoryId(categoryId, params);
        PageResult<Rule> pageResult = new PageResult<>();
        //long sum = ruleDAO.countByByCatetoryId(categoryId);
        long sum = 0;
        if (list.size()!=0){
            sum = list.get(0).getTotal();
        }
        //pageResult.setOffset(params.getOffset());
        pageResult.setTotalSize(sum);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    private List<Rule> queryByCatetoryId(String categoryId, Parameters params) throws AtlasBaseException {
        String path = CategoryRelationUtils.getPath(categoryId);
        Map<Integer, String> ruleTemplateCategoryMap = new HashMap();
        RuleTemplateType.all().stream().forEach(ruleTemplateType-> {
            ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
        });
        List<Rule> list = ruleDAO.queryByCatetoryId(categoryId, params)
                .stream()
                .map(rule -> {
                    rule.setPath(path);
                    String ruleTypeName = ruleTemplateCategoryMap.get(rule.getRuleType());
                    rule.setRuleTypeName(ruleTypeName);
                    return rule;
                }).collect(Collectors.toList());
        return list;
    }

    public PageResult<Rule> search(Parameters params) {
        Map<Integer, String> ruleTemplateCategoryMap = new HashMap();
        RuleTemplateType.all().stream().forEach(ruleTemplateType-> {
            ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
        });
        List<Rule> list = ruleDAO.search(params)
                .stream()
                .map(rule -> {
                    String path = null;
                    try {
                        path = CategoryRelationUtils.getPath(rule.getCategoryId());
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
        //long sum = ruleDAO.countBySearch(params.getQuery());
        long sum = 0;
        if (list.size()!=0){
            sum = list.get(0).getTotal();
        }
        //pageResult.setOffset(params.getOffset());
        pageResult.setTotalSize(sum);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public List<CategoryPrivilege> getAll(Integer categoryType) throws AtlasBaseException {
        try {
            List<CategoryPrivilege> resultList = dataManageService.getAll(categoryType);
            for (CategoryPrivilege res : resultList) {
                Integer count = ruleDAO.getCategoryObjectCount(res.getGuid());
                res.setObjectCount(count);
            }
            return resultList;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public int updateRuleStatus(String id, Boolean enable) throws AtlasBaseException {
        try {
            if(false == enable) {
                int count = ruleDAO.getRuleUsedCount(id);
                if(count > 0) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前规则已被使用，禁止关闭");
                }
            }
            return ruleDAO.updateRuleStatus(id, enable);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public List<RuleTemplate> getAllRuleTemplateList() throws AtlasBaseException {
        try {
            return ruleDAO.getAllRuleTemplateList();
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public void deleteCategory(String categoryGuid) throws AtlasBaseException {
        try {
            int count = ruleDAO.getCategoryObjectCount(categoryGuid);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该分组下存在关联规则，不允许删除");
            }
            dataManageService.deleteCategory(categoryGuid);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public String getCategoryName(String categoryGuid) {
        return ruleDAO.getCategoryName(categoryGuid);
    }

    public String getNameById(String id) {
        return ruleDAO.getNameById(id);
    }
}
