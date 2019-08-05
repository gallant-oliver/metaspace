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
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.filter.OperateLogInterceptor;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.dataquality.RuleService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;


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
    private DataManageService dataManageService;


    private void log(String content) {
        request.setAttribute(OperateLogInterceptor.OPERATELOG_OBJECT, "(数据质量规则) " + content);
    }

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
        log(rule.getCode());
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
        log(rule.getCode());
        ruleService.update(rule);
    }

    @DELETE
    @Path("/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByIdList(List<String> idList) throws AtlasBaseException {
        log("批量删除:[" + Joiner.on("、").join(idList) + "]");
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
        log(id);
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
        return dataManageService.getAll(categoryType);
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
    public CategoryPrivilege insert(CategoryInfoV2 categoryInfo) throws Exception {
        log(categoryInfo.getName());
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
    public void delete(@PathParam("categoryGuid") String categoryGuid) throws Exception {
        log(categoryGuid);
        dataManageService.deleteCategory(categoryGuid);
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
    public void update(CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        log(categoryInfo.getName());
        dataManageService.updateCategory(categoryInfo, categoryInfo.getCategoryType());
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{ruleId}/enable")
    public void enableRule(@PathParam("ruleId") String ruleId) throws AtlasBaseException {
        log(ruleId);
        ruleService.updateRuleStatus(ruleId, true);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{ruleId}/disable")
    public void disableRule(@PathParam("ruleId") String ruleId) throws AtlasBaseException {
        log(ruleId);
        ruleService.updateRuleStatus(ruleId, false);
    }

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/ruleTemplate")
    public List<RuleTemplate> disableRule() throws AtlasBaseException {
        return ruleService.getAllRuleTemplateList();
    }

}
