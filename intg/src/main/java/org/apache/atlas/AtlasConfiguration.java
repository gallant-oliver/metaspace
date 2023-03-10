/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas;


import org.apache.commons.configuration.Configuration;

import java.util.function.Function;

/**
 * Enum that encapsulated each property name and its default value.
 */
public enum AtlasConfiguration {
    //web server configuration
    WEBSERVER_MIN_THREADS("atlas.webserver.minthreads", 10),
    WEBSERVER_MAX_THREADS("atlas.webserver.maxthreads", 100),
    WEBSERVER_KEEPALIVE_SECONDS("atlas.webserver.keepalivetimesecs", 60),
    WEBSERVER_QUEUE_SIZE("atlas.webserver.queuesize", 100),
    WEBSERVER_REQUEST_BUFFER_SIZE("atlas.jetty.request.buffer.size", 16192),

    QUERY_PARAM_MAX_LENGTH("atlas.query.param.max.length", 4 * 1024),

    NOTIFICATION_MESSAGE_MAX_LENGTH_BYTES("atlas.notification.message.max.length.bytes", (1000 * 1000)),
    NOTIFICATION_MESSAGE_COMPRESSION_ENABLED("atlas.notification.message.compression.enabled", true),
    NOTIFICATION_SPLIT_MESSAGE_SEGMENTS_WAIT_TIME_SECONDS("atlas.notification.split.message.segments.wait.time.seconds", 15 * 60),
    NOTIFICATION_SPLIT_MESSAGE_BUFFER_PURGE_INTERVAL_SECONDS("atlas.notification.split.message.buffer.purge.interval.seconds", 5 * 60),

    //search configuration
    SEARCH_MAX_LIMIT("atlas.search.maxlimit", 10000),
    SEARCH_DEFAULT_LIMIT("atlas.search.defaultlimit", 100),

    ATLAS_CLUSTER_NAME("atlas.cluster.name", "ms"),
    METASPACE_ADAPTER_DIR("metaspace.adapter.dir", "adapters"),

    //????????????????????????
    LOCK_TIME_OUT_TIME("metaspace.zk.lock.timeout.time",1),

    METASPACE_API_POLY_EFFECTIVE_TIME("metaspace.api.ploy.effective.time", 60*5),
    METASPACE_QUALITY_ENGINE("metaspace.quality.engine", "impala");

    private static final Configuration APPLICATION_PROPERTIES;

    static {
        try {
            APPLICATION_PROPERTIES = ApplicationProperties.get();
        } catch (AtlasException e) {
            throw new RuntimeException(e);
        }
    }

    private final String propertyName;
    private final Object defaultValue;

    AtlasConfiguration(String propertyName, Object defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public int getInt() {
        return APPLICATION_PROPERTIES.getInt(propertyName, Integer.valueOf(defaultValue.toString()).intValue());
    }

    public long getLong() {
        return APPLICATION_PROPERTIES.getLong(propertyName, Long.valueOf(defaultValue.toString()).longValue());
    }

    public boolean getBoolean() {
        return APPLICATION_PROPERTIES.getBoolean(propertyName, Boolean.valueOf(defaultValue.toString()).booleanValue());
    }

    public String getString() {
        return APPLICATION_PROPERTIES.getString(propertyName, defaultValue.toString());
    }

    public Object get() {
        Object value = APPLICATION_PROPERTIES.getProperty(propertyName);
        return value == null ? defaultValue : value;
    }

    public <T> T get(Configuration conf, Function<Object, T> valueOf) {
        Object value = APPLICATION_PROPERTIES.getProperty(propertyName);

        if(value == null){
            if (defaultValue == null){
                return null;
            }
            value = defaultValue;
        }
        return valueOf.apply(value);
    }
}
