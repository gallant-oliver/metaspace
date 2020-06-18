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
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dataquality2.DataTaskIdAndName;
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.datastandard.DataStandAndRule;
import io.zeta.metaspace.model.datastandard.DataStandardHead;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.share.APIIdAndName;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataStandardService;
import io.zeta.metaspace.web.service.dataquality.RuleService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.ImportCategory;
import org.apache.atlas.model.metadata.MoveCategory;
import org.apache.atlas.model.metadata.SortCategory;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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

    private static final Logger LOG = LoggerFactory.getLogger(RuleREST.class);

    private static final int MAX_EXCEL_FILE_SIZE = 10*1024*1024;

    /**
     * 添加规则
     * @param rule
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public void insert(Rule rule, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), rule.getName());
        List<Rule> oldList = ruleService.getByCode(rule.getCode(),tenantId);
        if (!oldList.isEmpty()) {
            throw new AtlasBaseException("规则编号已存在");
        }
        List<Rule> oldListByName = ruleService.getByName(rule.getName(),tenantId);
        if (!oldListByName.isEmpty()) {
            throw new AtlasBaseException("规则名字已存在");
        }
        ruleService.insert(rule,tenantId);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public void update(Rule rule,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), rule.getName());
        ruleService.update(rule,tenantId);
    }

    @DELETE
    @Path("/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByIdList(List<String> idList,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        List<String> ruleNameList = new ArrayList<>();
        for (String ruleId : idList) {
            Rule rule = ruleService.getById(ruleId,tenantId);
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
    public Rule getById(@PathParam("id") String id,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return ruleService.getById(id,tenantId);
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
    public PageResult<Rule> queryByCatetoryId(@PathParam("categoryId") String categoryId, Parameters parameters,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return ruleService.queryPageByCatetoryId(categoryId, parameters,tenantId);
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
    public void deleteById(@PathParam("id") String id,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        Rule rule = ruleService.getById(id,tenantId);
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
    public PageResult<Rule> search(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return ruleService.search(parameters,tenantId);
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
    public List<CategoryPrivilege> getAll(@PathParam("categoryType") Integer categoryType,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return ruleService.getAll(categoryType,tenantId);
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
    public CategoryPrivilege insert(CategoryInfoV2 categoryInfo,@HeaderParam("tenantId")String tenantId) throws Exception {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), categoryInfo.getName());
        return dataManageService.createCategory(categoryInfo, categoryInfo.getCategoryType(),tenantId);
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
    public void delete(@PathParam("categoryGuid") String categoryGuid,@HeaderParam("tenantId")String tenantId) throws Exception {
        String categoryName = ruleService.getCategoryName(categoryGuid,tenantId);
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), null==categoryName?categoryName:categoryGuid);
        ruleService.deleteCategory(categoryGuid,tenantId);
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
    public void update(CategoryInfoV2 categoryInfo,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), categoryInfo.getName());
        dataManageService.updateCategory(categoryInfo, CATEGORY_RULE,tenantId);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{ruleId}/enable")
    @OperateType(UPDATE)
    public void enableRule(@PathParam("ruleId") String ruleId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        Rule rule = ruleService.getById(ruleId,tenantId);
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
    public void disableRule(@PathParam("ruleId") String ruleId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        Rule rule = ruleService.getById(ruleId,tenantId);
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
    public boolean assignRuleToStandard(DataStandAndRule dataStandAndTable,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        String ruleName = ruleService.getNameById(dataStandAndTable.getRuleId());
        if(null == ruleName) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "质量规则不存在");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), ruleName);
        try {
            dataStandardService.assignRuleToStandard(dataStandAndTable,ruleName,tenantId);
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
    public List<DataStandardHead> getDataStandard(@PathParam("ruleId") String ruleId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return dataStandardService.getDataStandardByRule(ruleId,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("{id}/used")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getRuleUsed(@PathParam("id") String id)
            throws Exception
    {
        try {
            List<DataTaskIdAndName> used = ruleService.getRuleUsed(id);
            return ReturnUtil.success(used);
        }catch (Exception e){
            LOG.error("获取使用规则任务失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e, "获取使用规则任务失败："+e.getMessage());
        }
    }

    /**
     * 导出目录
     * @param ids
     * @return
     * @throws Exception
     */
    @POST
    @Path("/export/selected")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDownloadURL(List<String> ids) throws Exception {
        //全局导出
        if (ids==null||ids.size()==0){
            DownloadUri uri = new DownloadUri();
            String downURL = request.getRequestURL().toString() + "/" + "all";
            uri.setDownloadUri(downURL);
            return  ReturnUtil.success(uri);
        }
        DownloadUri downloadUri = ExportDataPathUtils.generateURL(request.getRequestURL().toString(), ids);
        return ReturnUtil.success(downloadUri);
    }

    @GET
    @Path("/export/selected/{downloadId}")
    @Valid
    public void exportSelected(@PathParam("downloadId") String downloadId,@QueryParam("tenantId")String tenantId) throws Exception {
        File exportExcel;
        //全局导出
        String all = "all";
        if (all.equals(downloadId)){
            exportExcel = dataManageService.exportExcelAll(CATEGORY_RULE,tenantId);
        }else{
            List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            exportExcel = dataManageService.exportExcel(ids, CATEGORY_RULE,tenantId);
        }
        try {
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        } finally {
            exportExcel.delete();
        }
    }
    public static String filename(String filePath) throws UnsupportedEncodingException {
        String filename = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1);
        filename = URLEncoder.encode(filename, "UTF-8");
        return filename;
    }


    /**
     * 上传文件并校验
     * @param categoryId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws Exception
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadCategory(@FormDataParam("categoryId") String categoryId,
                                 @DefaultValue("false")@FormDataParam("all") boolean all, @FormDataParam("direction")String direction,
                                 @HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            HttpRequestContext.get().auditLog(ModuleEnum.RULEMANAGE.getAlias(),  name);
            file = ExportDataPathUtils.fileCheck(name,fileInputStream);
            String upload;
            if (all){
                upload = dataManageService.uploadAllCategory(file,CATEGORY_RULE,tenantId);
            }else{
                upload = dataManageService.uploadCategory(categoryId,direction,file,CATEGORY_RULE,tenantId);
            }
            HashMap<String, String> map = new HashMap<String, String>() {{
                put("upload", upload);
            }};
            return ReturnUtil.success(map);
        } catch (AtlasBaseException e) {
            LOG.error("导入失败",e);
            throw e;
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 根据文件导入目录
     * @param upload
     * @param importCategory
     * @return
     * @throws Exception
     */
    @POST
    @Path("/import/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result importCategory(@PathParam("upload")String upload, ImportCategory importCategory, @HeaderParam("tenantId")String tenantId) throws Exception {
        File file = null;
        try {
            String categoryId = importCategory.getCategoryId();
            String name;
            if (importCategory.isAll()){
                name="全部";
            }else if (categoryId==null||categoryId.length()==0){
                name="一级目录";
            }else{
                name  = dataManageService.getCategoryNameById(categoryId,tenantId);
            }

            HttpRequestContext.get().auditLog(ModuleEnum.RULEMANAGE.getAlias(),  "导入目录:"+name+","+importCategory.getDirection());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            if (importCategory.isAll()){
                dataManageService.importAllCategory(file,CATEGORY_RULE,tenantId);
            }else{
                dataManageService.importCategory(categoryId,importCategory.getDirection(), file,CATEGORY_RULE,tenantId);
            }
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            LOG.error("导入失败",e);
            throw e;
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }
    /**
     * 变更目录结构
     * @param moveCategory
     * @throws Exception
     */
    @POST
    @Path("/move/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result moveCategory(MoveCategory moveCategory, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            if(moveCategory.getGuid()==null){
                HttpRequestContext.get().auditLog(ModuleEnum.RULEMANAGE.getAlias(), "变更目录结构：all");
            }else{
                CategoryEntityV2 category = dataManageService.getCategory(moveCategory.getGuid(), tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.RULEMANAGE.getAlias(), "变更目录结构："+category.getName());
            }
            dataManageService.moveCategories(moveCategory,CATEGORY_RULE,tenantId);
            return ReturnUtil.success();
        }catch (AtlasBaseException e){
            LOG.error("变更目录结构失败",e);
            throw e;
        }catch (Exception e){
            LOG.error("变更目录结构失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"变更目录结构失败");
        }
    }

    /**
     * 获取排序后的目录
     * @param sort
     * @param order
     * @param guid
     * @return
     * @throws Exception
     */
    @GET
    @Path("/sort/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result sortCategory(@QueryParam("sort")String sort, @DefaultValue("asc")@QueryParam("order")String order,
                               @QueryParam("guid")String guid,@HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            SortCategory sortCategory = new SortCategory();
            sortCategory.setSort(sort);
            sortCategory.setOrder(order);
            sortCategory.setGuid(guid);
            List<RoleModulesCategories.Category> categories = dataManageService.sortCategory(sortCategory, CATEGORY_RULE, tenantId);
            return ReturnUtil.success(categories);
        }catch (Exception e){
            LOG.error("目录排序并变更结构失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"目录排序并变更结构失败");
        }
    }

    @GET
    @Path("/download/category/template")
    @Valid
    public void downloadCategoryTemplate() throws Exception {
        String homeDir = System.getProperty("atlas.home");
        String filePath = homeDir + "/conf/category_template.xlsx";
        String fileName = filename(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }
}
