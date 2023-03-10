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
 * @date 2019/9/12 17:36
 */
package io.zeta.metaspace.web.rest;

import com.alibaba.druid.sql.SQLUtils;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.discovery.MetaspaceGremlinService;
import io.zeta.metaspace.model.table.Table;
import io.zeta.metaspace.model.table.TableForm;
import io.zeta.metaspace.model.table.TableSql;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.web.service.TableService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.TableSqlUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;

@Path("table")
@Singleton
@Service
public class TableREST {


    @Autowired
    private TableService tableService;
    @Inject
    private MetaspaceGremlinService metaspaceGremlinService;

    private static final int MAX_EXCEL_FILE_SIZE = 1024*1024;

    @POST
    @Path("/create/form")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table formCreate(TableForm request) throws Exception {
        String sql = TableSqlUtils.format(request);
        if (AdapterUtils.getHiveAdapterSource().getNewAdapterExecutor().tableExists(AdminUtils.getUserName(), request.getDatabase(), request.getTableName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表 " + request.getDatabase() + "." + request.getTableName() + " 已存在");
        }
        tableService.execute("CREATE DATABASE IF NOT EXISTS " + request.getDatabase());
        tableService.execute(sql);
        String tablId = metaspaceGremlinService.getGuidByDBAndTableName(request.getDatabase(), request.getTableName());
        Table ret = new Table(tablId);
        return ret;
    }

    @POST
    @Path("/create/sql")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table sqlCreate(TableSql sql) throws Exception {
        try {
            String[] split = tableService.databaseAndTable(sql.getSql()).split("\\.");
            int length = 2;
            if (split.length < length) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库名和表名不能为空");
            }
            String database = split[0];
            String tableName = split[1];
            if (AdapterUtils.getHiveAdapterSource().getNewAdapterExecutor().tableExists(AdminUtils.getUserName(), database, tableName)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表 " + database + "." + tableName + " 已存在");
            }
            tableService.execute("CREATE DATABASE IF NOT EXISTS " + database);
            tableService.execute(sql.getSql());
            String tableId = metaspaceGremlinService.getGuidByDBAndTableName(database, tableName);
            Table ret = new Table(tableId);
            return ret;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "创建离线表失败");
        }
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
        //druid's bug
        String store = "STORE";
        String stored = "STORED";
        if (formatedSql.contains(store) && !formatedSql.contains(stored)) {
            formatedSql = formatedSql.replace(store, stored);
        }
        return Response.status(200).entity(formatedSql).build();
    }

    @POST
    @Path("/sql/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map importSql(@FormDataParam("file") InputStream fileInputStream,
                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name =URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            String fileFormat = ".sql";
            if(name.endsWith(fileFormat)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式需为.sql");
            }
            file = new File(name);
            FileUtils.copyInputStreamToFile(fileInputStream, file);
            if(file.length() > MAX_EXCEL_FILE_SIZE) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过1M");
            }
            return tableService.importSql(file);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "创建离线表失败");
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

}
