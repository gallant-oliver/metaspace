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
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.dataquality.RuleDAO;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RuleService {

    private static final Logger LOG = LoggerFactory.getLogger(RuleService.class);

    @Autowired
    RuleDAO ruleDAO;

    public int insert(Rule rule) throws AtlasBaseException {
        rule.setId(UUIDUtils.alphaUUID());
        rule.setCreateTime(DateUtils.currentTimestamp());
        rule.setUpdateTime(DateUtils.currentTimestamp());
        rule.setDelete(false);
        return ruleDAO.insert(rule);
    }


    public Rule getById(String id) throws AtlasBaseException {
        Rule rule = ruleDAO.getById(id);
        String path = CategoryRelationUtils.getPath(rule.getCategoryId());
        rule.setPath(path);
        return rule;
    }

    public List<Rule> getByCode(String number) throws AtlasBaseException {
        return ruleDAO.getByCode(number);
    }

    public void deleteById(String number) throws AtlasBaseException {
        ruleDAO.deleteById(number);
    }

    public void deleteByIdList(List<String> numberList) throws AtlasBaseException {
        ruleDAO.deleteByIdList(numberList);
    }

    public int update(Rule rule) throws AtlasBaseException {
        rule.setUpdateTime(DateUtils.currentTimestamp());
        return ruleDAO.update(rule);
    }

    public PageResult<Rule> queryPageByCatetoryId(String categoryId, Parameters parameters) throws AtlasBaseException {
        List<Rule> list = queryByCatetoryId(categoryId, parameters);
        PageResult<Rule> pageResult = new PageResult<>();
        long sum = ruleDAO.countByByCatetoryId(categoryId);
        pageResult.setOffset(parameters.getOffset());
        pageResult.setSum(sum);
        pageResult.setCount(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public List<Rule> queryByCatetoryId(String categoryId, Parameters parameters) throws AtlasBaseException {
        String path = CategoryRelationUtils.getPath(categoryId);
        List<Rule> list = ruleDAO.queryByCatetoryId(categoryId, parameters)
                .stream()
                .map(rule -> {
                    rule.setPath(path);
                    return rule;
                }).collect(Collectors.toList());
        return list;
    }

    public PageResult<Rule> search(Parameters parameters) {
        List<Rule> list = ruleDAO.search(parameters)
                .stream()
                .map(rule -> {
                    String path = null;
                    try {
                        path = CategoryRelationUtils.getPath(rule.getCategoryId());
                    } catch (AtlasBaseException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    rule.setPath(path);
                    return rule;
                }).collect(Collectors.toList());

        PageResult<Rule> pageResult = new PageResult<>();
        long sum = ruleDAO.countBySearch(parameters.getQuery());
        pageResult.setOffset(parameters.getOffset());
        pageResult.setSum(sum);
        pageResult.setCount(list.size());
        pageResult.setLists(list);
        return pageResult;
    }
}
