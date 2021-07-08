/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.debezium.RdbmsMessage;
import org.apache.atlas.model.notification.AtlasNotificationMessage;
import org.apache.atlas.model.notification.HookNotification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.apache.atlas.notification.AbstractMessageDeserializer;
import org.apache.atlas.notification.AbstractNotification;
import org.apache.atlas.notification.MessageDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;


/**
 * Hook notification message deserializer.
 */
public class RdbmsMessageDeserializer implements MessageDeserializer<RdbmsNotification> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);

    private static final Pattern SQL_DOC_PATTERN = Pattern.compile("(?ms)('(?:''|[^'])*')|--(?!(\\s*\\++\\s*\\S+))\\s.*?$|((/\\*)(?!(\\s*\\++\\s*\\S+)).*?(\\*/))");
    private static final Pattern BLACK_LINE_PATTERN = Pattern.compile("(\n|↵)(?=([^\"]*\"[^\"]*\")*[^\"]*$)(?=([^']*'[^']*')*[^']*$)");

    /**
     * Logger for rdbms notification messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RdbmsMessageDeserializer.class);


    @Override
    public RdbmsNotification deserialize(String messageJson) {
        RdbmsNotification rdbmsNotification = new RdbmsNotification();
        try{
            RdbmsMessage rdbmsMessage = MAPPER.readValue(messageJson, new TypeReference<RdbmsMessage>() {});
            RdbmsMessage.Payload payload = rdbmsMessage.getPayload();
            if(null == payload){
                throw new RuntimeException("rdbms消息有误，payload不能为空");
            }
            RdbmsNotification.RdbmsNotificationType type = null;
            String sql = payload.getOp() != null ? payload.getSource().getQuery() : payload.getDdl();
            sql = SQL_DOC_PATTERN.matcher(sql).replaceAll("$1");
            sql = BLACK_LINE_PATTERN.matcher(sql).replaceAll(" ").trim();
            if(payload.getOp() != null){
                type = RdbmsNotification.RdbmsNotificationType.getTypeByCode(payload.getOp());
                payload.getSource().setQuery(sql);
            }else{
                type = RdbmsNotification.getTypeBySql(sql);
                payload.setDdl(sql);
            }
            rdbmsNotification.setType(type);
            rdbmsNotification.setRdbmsMessage(rdbmsMessage);
        }catch (Exception e){
            LOG.error("消息转化异常, messageJson: {}",messageJson, e);
        }
        return rdbmsNotification;

    }
}