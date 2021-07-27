package io.zeta.metaspace.web.rest;


import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import io.zeta.metaspace.web.service.KafkaConnectorService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.notification.rdbms.KafkaConnectorUtil;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;
import java.util.Map;


@Path("/kafka/connectors")
@Singleton
@Service
public class KafkaConnectorRESR {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectorRESR.class);

    private KafkaConnectorService kafkaConnectorService;
    
    @GET
    @Path("/urls")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public List<String> getConnectorUrls() throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnectorUrls()");
            }
            return KafkaConnectorUtil.getConnectorUrls();
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public List<KafkaConnector> getConnectors() throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnectors()");
            }
            return kafkaConnectorService.getConnectors();
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("active")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Map<String, List<String>> getActiveConnectors() throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnectors()");
            }
            return KafkaConnectorUtil.getConnectors();
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("/{connectorName}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public KafkaConnector getConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnector()");
            }
            return kafkaConnectorService.getConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("/{connectorName}/status")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public KafkaConnector.Status getConnectorStatus(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnectorStatus()");
            }
            return kafkaConnectorService.getConnectorStatus(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public KafkaConnector addConnector(KafkaConnector connector) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.addConnector()");
            }
           return kafkaConnectorService.addKafkaConnector(connector);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @DELETE
    @Path("/{connectorName}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public boolean removeConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.removeConnector()");
            }
            return kafkaConnectorService.removeConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @PUT
    @Path("/{connectorName}/pause")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public boolean pauseConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.pauseConnector()");
            }
            KafkaConnectorUtil.pauseConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return true;
    }


    @PUT
    @Path("/{connectorName}/resume")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public boolean resumeConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.resumeConnector()");
            }
            KafkaConnectorUtil.resumeConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return true;
    }

    @PUT
    @Path("/{connectorName}/start")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public boolean startConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.startConnector()");
            }
            kafkaConnectorService.startConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return true;
    }

    @PUT
    @Path("/{connectorName}/stop")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public boolean stopConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.stopConnector()");
            }
            kafkaConnectorService.stopConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return true;
    }

    @PUT
    @Path("/{connectorName}/restart")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public boolean restartConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.restartConnector()");
            }
            return kafkaConnectorService.restartConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
