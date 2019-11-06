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
package io.zeta.metaspace.web.rest.dataquality;


import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.DELETE;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import com.google.common.base.Joiner;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.datastandard.DataStandAndRule;
import io.zeta.metaspace.model.datastandard.DataStandardHead;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataStandardService;
import io.zeta.metaspace.web.service.dataquality.RuleService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


/**
 * 规则
 */
@Singleton
@Service
@Path("/dataquality/rule")
public class RuleREST {

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Autowired
    private RuleService ruleService;
    @Autowired
    private DataStandardService dataStandardService;


    @Autowired
    private DataManageService dataManageService;

    private static final Integer CATEGORY_RULE = 4;


    /**
     * 添加规则
     * @param rule
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public void insert(Rule rule) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), rule.getName());
        List<Rule> oldList = ruleService.getByCode(rule.getCode());
        if (!oldList.isEmpty()) {
            throw new AtlasBaseException("规则编号已存在");
        }
        ruleService.insert(rule);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public void update(Rule rule) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), rule.getName());
        ruleService.update(rule);
    }

    @DELETE
    @Path("/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByIdList(List<String> idList) throws AtlasBaseException {
        List<String> ruleNameList = new ArrayList<>();
        for (String ruleId : idList) {
            Rule rule = ruleService.getById(ruleId);
            String ruleName = ruleId;
            if(null != rule) {
                ruleName = rule.getName();
            }
            ruleNameList.add(ruleName);
        }

        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "批量删除:[" + Joiner.on("、").join(ruleNameList) + "]");
        ruleService.deleteByIdList(idList);
    }

    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Rule getById(@PathParam("id") String id) throws AtlasBaseException {
        return ruleService.getById(id);
    }

    /**
     * 规则列表
     * @param categoryId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Rule> queryByCatetoryId(@PathParam("categoryId") String categoryId, Parameters parameters) throws AtlasBaseException {
        return ruleService.queryPageByCatetoryId(categoryId, parameters);
    }

    /**
     * 删除规则
     * @param id
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteById(@PathParam("id") String id) throws AtlasBaseException {
        Rule rule = ruleService.getById(id);
        String ruleName = id;
        if(null != rule) {
            ruleName = rule.getName();
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), ruleName);
        ruleService.deleteById(id);
    }


    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Rule> search(Parameters parameters) throws AtlasBaseException {
        return ruleService.search(parameters);
    }


    /**
     * 获取全部目录列表
     *
     * @param categoryType
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category/{categoryType}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getAll(@PathParam("categoryType") Integer categoryType) throws AtlasBaseException {
        return ruleService.getAll(categoryType);
    }

    /**
     * 添加目录
     *
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/category")
    @OperateType(INSERT)
    public CategoryPrivilege insert(CategoryInfoV2 categoryInfo) throws Exception {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), categoryInfo.getName());
        return dataManageService.createCategory(categoryInfo, categoryInfo.getCategoryType());
    }

    /**
     * 删除目录
     *
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/category/{categoryGuid}")
    @OperateType(DELETE)
    public void delete(@PathParam("categoryGuid") String categoryGuid) throws Exception {
        String categoryName = ruleService.getCategoryName(categoryGuid);
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), null==categoryName?categoryName:categoryGuid);
        ruleService.deleteCategory(categoryGuid);
    }

    /**
     * 修改目录信息
     *
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/category")
    @OperateType(UPDATE)
    public void update(CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), categoryInfo.getName());
        dataManageService.updateCategory(categoryInfo, CATEGORY_RULE);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{ruleId}/enable")
    @OperateType(UPDATE)
    public void enableRule(@PathParam("ruleId") String ruleId) throws AtlasBaseException {
        Rule rule = ruleService.getById(ruleId);
        String ruleName = ruleId;
        if(null != rule) {
            ruleName = rule.getName();
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), ruleName);
        ruleService.updateRuleStatus(ruleId, true);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{ruleId}/disable")
    @OperateType(UPDATE)
    public void disableRule(@PathParam("ruleId") String ruleId) throws AtlasBaseException {
        Rule rule = ruleService.getById(ruleId);
        String ruleName = ruleId;
        if(null != rule) {
            ruleName = rule.getName();
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), ruleName);
        ruleService.updateRuleStatus(ruleId, false);
    }

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/ruleTemplate")
    public List<RuleTemplate> disableRule() throws AtlasBaseException {
        return ruleService.getAllRuleTemplateList();
    }

    /**
     * 更新依赖标准
     * @param dataStandAndTable
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/dataStandard")
    @OperateType(UPDATE)
    public boolean assignRuleToStandard(DataStandAndRule dataStandAndTable) throws AtlasBaseException {
        String ruleName = ruleService.getNameById(dataStandAndTable.getRuleId());
        if(null == ruleName) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "质量规则不存在");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), ruleName);
        try {
            dataStandardService.assignRuleToStandard(dataStandAndTable,ruleName);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取规则依赖标准
     * @param ruleId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/dataStandard/{ruleId}")
    public List<DataStandardHead> getDataStandard(@PathParam("ruleId") String ruleId) throws AtlasBaseException {
        try {
            return dataStandardService.getDataStandardByRule(ruleId);
        } catch (Exception e) {
            throw e;
        }
    }

}
