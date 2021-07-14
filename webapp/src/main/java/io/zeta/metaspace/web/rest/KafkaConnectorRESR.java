package io.zeta.metaspace.web.rest;


import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.notification.rdbms.KafkaConnector;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Path("/kafka/connectors")
@Singleton
@Service
public class KafkaConnectorRESR {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectorRESR.class);

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
            return KafkaConnector.getConnectorUrls();
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Map<String, List<String>> getConnectors() throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnectors()");
            }
            return KafkaConnector.getConnectors();
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("/{connectorName}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public KafkaConnector.Instance getConnector(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnector()");
            }
            return KafkaConnector.getConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    @GET
    @Path("/{connectorName}/config")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Properties getConnectorConfig(@PathParam("connectorName")String connectorName) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.getConnectorConfig()");
            }
            return KafkaConnector.getConnectorConfig(connectorName);
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
            return KafkaConnector.getConnectorStatus(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public KafkaConnector.Instance addConnector(KafkaConnector.Instance instance) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "KafkaConnectorRESR.addConnector()");
            }
            return KafkaConnector.addConnector(instance);
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
            return KafkaConnector.removeConnector(connectorName);
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
            KafkaConnector.pauseConnector(connectorName);
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
            KafkaConnector.resumeConnector(connectorName);
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
            KafkaConnector.restartConnector(connectorName);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return true;
    }
}
