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

package org.apache.atlas.web.rest;

import com.alibaba.druid.sql.SQLUtils;
import org.apache.atlas.model.table.Table;
import org.apache.atlas.model.table.TableForm;
import org.apache.atlas.model.table.TableSql;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.apache.atlas.web.util.Servlets;
import org.apache.atlas.web.util.TableSqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("table")
@Singleton
@Service
public class TableREST {

    private static final Logger LOG = LoggerFactory.getLogger(TableREST.class);

    @POST
    @Path("/create/form")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Table formCreate(TableForm request) throws Exception {
        String sql = TableSqlUtils.format(request);
        HiveJdbcUtils.execute(sql);
        String tableId = null;
        Table ret = new Table(tableId);
        return ret;
    }

    @POST
    @Path("/create/sql")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Table sqlCreate(TableSql sql) throws Exception {
        HiveJdbcUtils.execute(sql.getSql());
        String tableId = null;
        Table ret = new Table(tableId);
        return ret;
    }

    /**
     * 将表单拼接成sql
     *
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Path("/sql/format")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Response sqlFormat(TableForm request) throws Exception {
        String sql = TableSqlUtils.format(request);
        String formatedSql = SQLUtils.formatHive(sql);
        return Response.status(200).entity(formatedSql).build();
    }

}

