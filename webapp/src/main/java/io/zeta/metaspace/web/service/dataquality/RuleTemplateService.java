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

import io.zeta.metaspace.model.dataquality2.Report2RuleTemplate;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.dataquality2.RuleTemplateType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.dataquality.RuleTemplateDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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

    public PageResult<RuleTemplate> getRuleTemplate(Integer ruleType, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult();
            List<RuleTemplate> ruleTemplateList = ruleTemplateDAO.getRuleTemplateByCategoryId(ruleType, parameters);
            updateRuleType(ruleTemplateList);
            pageResult.setLists(ruleTemplateList);
            pageResult.setCurrentSize(ruleTemplateList.size());
            if(null != ruleTemplateList && ruleTemplateList.size()>0) {
                pageResult.setTotalSize(ruleTemplateList.get(0).getTotal());
            } else {
                pageResult.setTotalSize(0);
            }
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取规则模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则模板失败");
        }
    }

    public PageResult<RuleTemplate> search(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult<RuleTemplate>();
            List<RuleTemplate> lists = ruleTemplateDAO.searchRuleTemplate(parameters);
            updateRuleType(lists);
            //long totalCount = ruleTemplateDAO.coutSearchRuleTemplate(parameters);
            long totalCount = 0;
            if (lists.size()!=0){
                totalCount = lists.get(0).getTotal();
            }
            pageResult.setLists(lists);
            pageResult.setCurrentSize(lists.size());
            pageResult.setTotalSize(totalCount);
            return pageResult;
        } catch (Exception e) {
            LOG.error("搜索失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public void updateRuleType(List<RuleTemplate> lists) throws AtlasBaseException {
        try {
            Map<Integer, String> ruleTemplateCategoryMap = new HashMap();
            RuleTemplateType.all().stream().forEach(ruleTemplateType -> {
                ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
            });
            for (RuleTemplate ruleTemplate : lists) {
                String ruleTypeName = ruleTemplateCategoryMap.get(ruleTemplate.getRuleType());
                ruleTemplate.setRuleTypeName(ruleTypeName);
            }
        } catch (Exception e) {
            LOG.error("搜索失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public int addReport2RuleType(String ruleExecutionId, List<String> ruleTemplateList) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getAccount();
            Timestamp createTime = new Timestamp(System.currentTimeMillis());
            return ruleTemplateDAO.addReport2RuleTemplate(ruleExecutionId, ruleTemplateList, userId, createTime);
        } catch (Exception e) {
            LOG.error("报告归档失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "报告归档失败");
        }
    }

    public PageResult<Report2RuleTemplate> getReportByRuleType(String templateId, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult();
            List<Report2RuleTemplate> list =  ruleTemplateDAO.getReportByRuleType(templateId, parameters,tenantId);
            long totalSize = 0;
            if(list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            pageResult.setLists(list);
            pageResult.setCurrentSize(list.size());
            pageResult.setTotalSize(totalSize);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询归档报告失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询归档报告失败");
        }
    }

}
