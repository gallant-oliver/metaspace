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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/2/13 10:09
 */
package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.web.service.DataManageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


/**
 * 目录
 */
@Singleton
@Service
@Path("/categories")
public class CategoryREST {

    @Autowired
    private DataManageService dataManageService;

    /**
     * 指定分类的目录列表
     *
     * @param categoryType
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{categoryType}")
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
    public CategoryPrivilege insert(CategoryInfoV2 categoryInfo) throws Exception {
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
    @Path("/{categoryGuid}")
    public void delete(@PathParam("categoryGuid") String categoryGuid) throws Exception {
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
    public void update(CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        dataManageService.updateCategory(categoryInfo, categoryInfo.getCategoryType());
    }

}
