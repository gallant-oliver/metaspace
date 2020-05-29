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
package org.apache.atlas.utils;


import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;


public class AtlasEntityUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasEntityUtil.class);

    /*public static boolean hasAnyAttributeUpdate(AtlasEntityType entityType, AtlasEntity currEntity, AtlasEntity entityInStore) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> hasAnyAttributeUpdate(guid={}, typeName={})", currEntity.getGuid(), currEntity.getTypeName());
        }

        boolean ret = false;

        for (AtlasAttribute attribute : entityType.getAllAttributes().values()) {
            String    attrName  = attribute.getName();
            AtlasType attrType  = attribute.getAttributeType();
            Object    currValue = currEntity.getAttribute(attrName);
            Object    oldValue  = entityInStore.getAttribute(attrName);

            if (!attrType.areEqualValues(currValue, oldValue)) {
                ret = true;

                if (LOG.isDebugEnabled()) {
                    LOG.debug("hasAnyAttributeUpdate(guid={}, typeName={}): attribute '{}' is found updated - currentValue={}, newValue={}",
                            currEntity.getGuid(), currEntity.getTypeName(), attrName, currValue, oldValue);
                }

                break;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== hasAnyAttributeUpdate(guid={}, typeName={}): ret={}", currEntity.getGuid(), currEntity.getTypeName(), ret);
        }

        return ret;
    }
*/
    public static String getRelationshipType(Object val) {
        final String ret;

        if (val instanceof AtlasRelatedObjectId) {
            ret = ((AtlasRelatedObjectId) val).getRelationshipType();
        } else if (val instanceof Collection) {
            String elemRelationshipType = null;

            for (Object elem : (Collection) val) {
                elemRelationshipType = getRelationshipType(elem);

                if (elemRelationshipType != null) {
                    break;
                }
            }

            ret = elemRelationshipType;
        } else if (val instanceof Map) {
            Map mapValue = (Map) val;

            if (mapValue.containsKey(AtlasRelatedObjectId.KEY_RELATIONSHIP_TYPE)) {
                Object relTypeName = ((Map) val).get(AtlasRelatedObjectId.KEY_RELATIONSHIP_TYPE);

                ret = relTypeName != null ? relTypeName.toString() : null;
            } else {
                String entryRelationshipType = null;

                for (Object entryVal : mapValue.values()) {
                    entryRelationshipType = getRelationshipType(entryVal);

                    if (entryRelationshipType != null) {
                        break;
                    }
                }

                ret = entryRelationshipType;
            }
        } else {
            ret = null;
        }

        return ret;
    }
}
