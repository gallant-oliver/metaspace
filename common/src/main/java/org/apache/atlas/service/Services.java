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
package org.apache.atlas.service;

import org.apache.atlas.annotation.AtlasService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.List;

import static org.apache.atlas.AtlasConstants.ATLAS_MIGRATION_MODE_FILENAME;
import static org.apache.atlas.AtlasConstants.ATLAS_SERVICES_ENABLED;

/**
 * Utility for starting and stopping all services.
 */
@AtlasService
@Profile("!test")
public class Services {
    public static final Logger LOG = LoggerFactory.getLogger(Services.class);
    private final List<Service> services;
    private final boolean       servicesEnabled;

    @Inject
    public Services(List<Service> services, Configuration configuration) {
        this.services               = services;
        this.servicesEnabled        = configuration.getBoolean(ATLAS_SERVICES_ENABLED, true);
    }

    @PostConstruct
    public void start() {
        try {
            if(servicesEnabled) {
                for (Service svc : services) {
                    LOG.info("Starting service {}", svc.getClass().getName());
                    svc.start();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop() {
        if (servicesEnabled) {
            for (int idx = services.size() - 1; idx >= 0; idx--) {
                Service svc = services.get(idx);
                try {
                    LOG.info("Stopping service {}", svc.getClass().getName());
                    svc.stop();
                } catch (Throwable e) {
                    LOG.warn("Error stopping service {}", svc.getClass().getName(), e);
                }
            }
        }
    }
}
