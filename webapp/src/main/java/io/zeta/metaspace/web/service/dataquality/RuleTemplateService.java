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

import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.dataquality2.RuleTemplateType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.dataquality.RuleTemplateDAO;
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

@Service
@Transactional
public class RuleTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(RuleTemplateService.class);

    @Autowired
    private RuleTemplateDAO ruleTemplateDAO;

    public long countByCategoryId(Integer categoryId) {
        return ruleTemplateDAO.countByCategoryId(categoryId);
    }

    public List<RuleTemplate> getRuleTemplate(Integer ruleType) throws AtlasBaseException {
        try {
            List<RuleTemplate> ruleTemplateList = ruleTemplateDAO.getRuleTemplateByCategoryId(ruleType);
            updateRuleType(ruleTemplateList);
            return ruleTemplateList;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }

    public PageResult<RuleTemplate> search(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult<RuleTemplate>();
            List<RuleTemplate> lists = ruleTemplateDAO.searchRuleTemplate(parameters);
            updateRuleType(lists);
            long totalCount = ruleTemplateDAO.coutSearchRuleTemplate(parameters);
            pageResult.setLists(lists);
            pageResult.setCurrentSize(lists.size());
            pageResult.setTotalSize(totalCount);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public void updateRuleType(List<RuleTemplate> lists) {
        Map<Integer, String> ruleTemplateCategoryMap = new HashMap();
        RuleTemplateType.all().stream().forEach(ruleTemplateType-> {
            ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
        });
        for (RuleTemplate ruleTemplate : lists) {
            String ruleTypeName = ruleTemplateCategoryMap.get(ruleTemplate.getRuleType());
            ruleTemplate.setRuleTypeName(ruleTypeName);
        }
    }

}
