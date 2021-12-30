package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import io.zeta.metaspace.utils.AESUtils;
import io.zeta.metaspace.web.dao.KafkaConnectorDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.notification.rdbms.KafkaConnectorUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KafkaConnectorService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectorUtil.class);

    @Autowired
    private KafkaConnectorDAO kafkaConnectorDAO;

    public KafkaConnector addKafkaConnector(KafkaConnector kafkaConnector){
        String name = kafkaConnector.getName();
        if(StringUtils.isBlank(name)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "kafka connector名称不能为空");
        }
        KafkaConnector oldKafkaConnector = kafkaConnectorDAO.selectConnectorByName(name);
        if(null != oldKafkaConnector){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "kafka connector " + name + "已经存在");
        }
        kafkaConnector.setId(UUID.randomUUID().toString());
        addToDB(kafkaConnector);
        return kafkaConnector;
    }

    public KafkaConnector addAndStartConnector(KafkaConnector kafkaConnector){
        KafkaConnector connector = addKafkaConnector(kafkaConnector);
        KafkaConnectorUtil.startConnector(connector);
        return connector;
    }

    public List<KafkaConnector> getConnectors(){
        return kafkaConnectorDAO.selectConnectors();
    }

    public KafkaConnector getConnector(String connectorName){
        return kafkaConnectorDAO.selectConnectorByName(connectorName);
    }

    public KafkaConnector.Status getConnectorStatus(String connectorName){
        KafkaConnector.Status connectorStatus = KafkaConnectorUtil.getConnectorStatus(connectorName);
        if(null != connectorStatus){
            return connectorStatus;
        }
        KafkaConnector kafkaConnector = kafkaConnectorDAO.selectConnectorByName(connectorName);
        if(null == kafkaConnector){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "kafka connector " + connectorName + "不存在");
        }else {
            connectorStatus = new  KafkaConnector.Status();
            connectorStatus.setName(connectorName);
            connectorStatus.setConnector(new HashMap<String, Object>(){
                {
                    put("state","NOT ACTIVE");
                }
            });
        }
        return connectorStatus;
    }

    public boolean removeConnector(String connectorName){
        KafkaConnectorUtil.stopConnector(connectorName);
        kafkaConnectorDAO.deleteConnector(connectorName);
        return true;
    }

    public boolean startConnector(String connectorName){
        KafkaConnector kafkaConnector = getKafkaConnector(connectorName);
        return KafkaConnectorUtil.startConnector(kafkaConnector);
    }

    private KafkaConnector getKafkaConnector(String connectorName) {
        KafkaConnector kafkaConnector = kafkaConnectorDAO.selectConnectorByName(connectorName);
        if(null == kafkaConnector){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "kafka connector " + connectorName + "不存在");
        }
        return kafkaConnector;
    }

    public boolean stopConnector(String connectorName){
        KafkaConnector kafkaConnector = kafkaConnectorDAO.selectConnectorByName(connectorName);
        if(null == kafkaConnector){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "kafka connector " + connectorName + "不存在");
        }
        return KafkaConnectorUtil.stopConnector(connectorName);
    }

    public boolean restartConnector(String connectorName){
        KafkaConnector connector = KafkaConnectorUtil.getConnector(connectorName, false);
        if(null != connector){
            KafkaConnectorUtil.restartConnector(connectorName);
        }else{

            KafkaConnector kafkaConnector = getKafkaConnector(connectorName);
            KafkaConnectorUtil.startConnector(kafkaConnector);
            LOG.warn("重启connector {}失败： 因为connector {}原本没有启动。本次操作直接启动了connector {}", connectorName, connectorName, connectorName);
        }
        return true;
    }

    private void addToDB(KafkaConnector connector) {
        KafkaConnector.Config config = connector.getConfig();
        config.setDbType(KafkaConnectorUtil.getRdbmsType(config.getConnectorClass()));
        String dbPassword = config.getDbPassword();
        String passWord = AESUtils.aesEncode(dbPassword);
        config.setDbPassword(passWord);
        kafkaConnectorDAO.insertConnector(connector);
    }
}
