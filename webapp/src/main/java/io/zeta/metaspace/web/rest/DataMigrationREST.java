package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.web.postgres.DataMigration;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/dbdata")
@Singleton
@Service
public class DataMigrationREST {
    @Autowired
    private DataMigration dataMigration;

    @POST
    @Path("/migration/sourcedb")
    public Result processSourceDb() {
        dataMigration.processSourceDb();
        return ReturnUtil.success();
    }

    @POST
    @Path("/migration/dbinfo")
    public Result processDbInfo() {
        dataMigration.processSourceDb();
        return ReturnUtil.success();
    }

}
