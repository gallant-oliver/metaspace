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

package io.zeta.metaspace.web.rest;

import com.alibaba.druid.sql.SQLUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.table.Table;
import io.zeta.metaspace.model.table.TableForm;
import io.zeta.metaspace.model.table.TableSql;
import io.zata.metaspace.repository.table.TableService;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.web.util.Servlets;
import io.zeta.metaspace.web.util.TableSqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("table")
@Singleton
@Service
public class TableREST {

    private static final Logger LOG = LoggerFactory.getLogger(TableREST.class);

    @Inject
    private TableService tableService;

    @POST
    @Path("/create/form")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table formCreate(TableForm request) throws Exception {
        String sql = TableSqlUtils.format(request);
        if (HiveJdbcUtils.tableExists(request.getDatabase(), request.getTableName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表 " + request.getDatabase() + "." + request.getTableName() + " 已存在");
        }
        HiveJdbcUtils.execute("CREATE DATABASE IF NOT EXISTS " + request.getDatabase());
        HiveJdbcUtils.execute(sql);
        String tablId = tableService.tableId(request.getDatabase(), request.getTableName());
        Table ret = new Table(tablId);
        return ret;
    }


    @POST
    @Path("/create/sql")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table sqlCreate(TableSql sql) throws Exception {
        String[] split = tableService.databaseAndTable(sql.getSql()).split("\\.");
        String database = split[0];
        String tableName = split[1];
        if (HiveJdbcUtils.tableExists(database, tableName)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表 " + database + "." + tableName + " 已存在");
        }
        HiveJdbcUtils.execute("CREATE DATABASE IF NOT EXISTS " + database);
        HiveJdbcUtils.execute(sql.getSql());
        String tableId = tableService.tableId(database, tableName);
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
        if (formatedSql.contains("STORE") && !formatedSql.contains("STORED")) {//druid's bug
            formatedSql = formatedSql.replace("STORE", "STORED");
        }
        return Response.status(200).entity(formatedSql).build();
    }

}

