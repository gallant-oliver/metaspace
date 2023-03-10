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
package org.apache.atlas.repository.store.graph.v2;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.typedef.BaseAtlasBaseTypeDef;
import org.apache.atlas.model.typedef.AtlasStructDef;
import org.apache.atlas.query.AtlasDSL;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.AtlasDefStore;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Abstract typedef-store for v1 format.
 */
  abstract class BaseAtlasAbstractDefStoreV2<T extends BaseAtlasBaseTypeDef> implements AtlasDefStore<T> {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAtlasAbstractDefStoreV2.class);

    protected final AtlasTypeDefGraphStoreV2 typeDefStore;
    protected final AtlasTypeRegistry        typeRegistry;

    private static final String  NAME_REGEX            = "[a-zA-Z][a-zA-Z0-9_ ]*";
    private static final String  INTERNAL_NAME_REGEX   = "__" + NAME_REGEX;
    private static final Pattern NAME_PATTERN          = Pattern.compile(NAME_REGEX);
    private static final Pattern INTERNAL_NAME_PATTERN = Pattern.compile(INTERNAL_NAME_REGEX);

    public static final String ALLOW_RESERVED_KEYWORDS = "atlas.types.allowReservedKeywords";

    public BaseAtlasAbstractDefStoreV2(AtlasTypeDefGraphStoreV2 typeDefStore, AtlasTypeRegistry typeRegistry) {
        this.typeDefStore = typeDefStore;
        this.typeRegistry = typeRegistry;
    }

    public void validateType(BaseAtlasBaseTypeDef typeDef) throws AtlasBaseException {
        if (!isValidName(typeDef.getName())) {
            throw new AtlasBaseException(AtlasErrorCode.TYPE_NAME_INVALID_FORMAT, typeDef.getName(), typeDef.getCategory().name());
        }

        try {
            final boolean allowReservedKeywords = ApplicationProperties.get().getBoolean(ALLOW_RESERVED_KEYWORDS, true);

            if (!allowReservedKeywords && typeDef instanceof AtlasStructDef) {
                final List<AtlasStructDef.AtlasAttributeDef> attributeDefs = ((AtlasStructDef) typeDef).getAttributeDefs();
                for (AtlasStructDef.AtlasAttributeDef attrDef : attributeDefs) {
                    if (AtlasDSL.Parser.isKeyword(attrDef.getName())) {
                        throw new AtlasBaseException(AtlasErrorCode.ATTRIBUTE_NAME_INVALID, attrDef.getName(), typeDef.getCategory().name());
                    }
                }
            }
        } catch (AtlasException e) {
            LOG.error("Exception while loading configuration ", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_ERROR, "Could not load configuration");
        }
    }

    public boolean isValidName(String typeName) {
        return NAME_PATTERN.matcher(typeName).matches() || INTERNAL_NAME_PATTERN.matcher(typeName).matches();
    }

    @Override
    public void deleteByName(String name, AtlasVertex preDeleteResult) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AtlasAbstractDefStoreV1.deleteByName({}, {})", name, preDeleteResult);
        }

        AtlasVertex vertex = (preDeleteResult == null) ? preDeleteByName(name) : preDeleteResult;

        typeDefStore.deleteTypeVertex(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AtlasAbstractDefStoreV1.deleteByName({}, {})", name, preDeleteResult);
        }
    }

    @Override
    public void deleteByGuid(String guid, AtlasVertex preDeleteResult) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AtlasAbstractDefStoreV1.deleteByGuid({}, {})", guid, preDeleteResult);
        }

        AtlasVertex vertex = (preDeleteResult == null) ? preDeleteByGuid(guid) : preDeleteResult;

        typeDefStore.deleteTypeVertex(vertex);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AtlasAbstractDefStoreV1.deleteByGuid({}, {})", guid, preDeleteResult);
        }
    }
}
