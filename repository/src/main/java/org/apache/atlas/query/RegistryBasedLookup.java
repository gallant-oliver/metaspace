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

package org.apache.atlas.query;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.TypeCategory;
import org.apache.atlas.model.typedef.BaseAtlasBaseTypeDef;
import org.apache.atlas.repository.Constants;
import org.apache.atlas.type.*;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static org.apache.atlas.discovery.SearchContext.MATCH_ALL_CLASSIFIED;
import static org.apache.atlas.discovery.SearchContext.MATCH_ALL_NOT_CLASSIFIED;
import static org.apache.atlas.model.discovery.SearchParameters.ALL_CLASSIFICATIONS;
import static org.apache.atlas.model.discovery.SearchParameters.NO_CLASSIFICATIONS;

class RegistryBasedLookup implements Lookup {
    private static final Set<String> SYSTEM_ATTRIBUTES = new HashSet<>(
            Arrays.asList(Constants.GUID_PROPERTY_KEY,
                    Constants.MODIFIED_BY_KEY,
                    Constants.CREATED_BY_KEY,
                    Constants.STATE_PROPERTY_KEY,
                    Constants.TIMESTAMP_PROPERTY_KEY,
                    Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY,
                    Constants.HOME_ID_KEY
            ));

    private static final Map<String, String> NUMERIC_ATTRIBUTES = new HashMap<String, String>() {{
            put(BaseAtlasBaseTypeDef.ATLAS_TYPE_SHORT, "");
            put(BaseAtlasBaseTypeDef.ATLAS_TYPE_INT, "");
            put(BaseAtlasBaseTypeDef.ATLAS_TYPE_LONG, "L");
            put(BaseAtlasBaseTypeDef.ATLAS_TYPE_FLOAT, "f");
            put(BaseAtlasBaseTypeDef.ATLAS_TYPE_DOUBLE, "d");
            put(BaseAtlasBaseTypeDef.ATLAS_TYPE_BIGINTEGER, "");
            put(BaseAtlasBaseTypeDef.ATLAS_TYPE_BIGDECIMAL, "");
        }};

    private final AtlasTypeRegistry typeRegistry;

    public RegistryBasedLookup(AtlasTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    @Override
    public BaseAtlasType getType(String typeName) throws AtlasBaseException {
        BaseAtlasType ret;

        if (typeName.equalsIgnoreCase(ALL_CLASSIFICATIONS)) {
            ret = MATCH_ALL_CLASSIFIED;
        } else if (typeName.equalsIgnoreCase(NO_CLASSIFICATIONS)) {
            ret = MATCH_ALL_NOT_CLASSIFIED;
        } else {
            ret = typeRegistry.getType(typeName);
        }

        return ret;
    }

    @Override
    public String getQualifiedName(GremlinQueryComposer.Context context, String name) throws AtlasBaseException {
        AtlasEntityType et = context.getActiveEntityType();
        if (et == null) {
            return "";
        }

        if(isSystemAttribute(name)) {
            return name;
        } else {
            return et.getQualifiedAttributeName(name);
        }
    }

    private boolean isSystemAttribute(String s) {
        return SYSTEM_ATTRIBUTES.contains(s);
    }

    @Override
    public boolean isPrimitive(GremlinQueryComposer.Context context, String attributeName) {
        AtlasEntityType et = context.getActiveEntityType();
        if(et == null) {
            return false;
        }

        if(isSystemAttribute(attributeName)) {
            return true;
        }

        BaseAtlasType at = et.getAttributeType(attributeName);
        if(at == null) {
            return false;
        }

        TypeCategory tc = at.getTypeCategory();
        if (isPrimitiveUsingTypeCategory(tc)) return true;

        if ((tc != null) && (tc == TypeCategory.ARRAY)) {
            AtlasArrayType ct = ((AtlasArrayType)at);
            return isPrimitiveUsingTypeCategory(ct.getElementType().getTypeCategory());
        }

        if ((tc != null) && (tc == TypeCategory.MAP)) {
            AtlasMapType ct = ((AtlasMapType)at);
            return isPrimitiveUsingTypeCategory(ct.getValueType().getTypeCategory());
        }

        return false;
    }

    private boolean isPrimitiveUsingTypeCategory(TypeCategory tc) {
        return ((tc != null) && (tc == TypeCategory.PRIMITIVE || tc == TypeCategory.ENUM));
    }

    @Override
    public String getRelationshipEdgeLabel(GremlinQueryComposer.Context context, String attributeName) {
        AtlasEntityType et = context.getActiveEntityType();
        if(et == null) {
            return "";
        }

        AtlasStructType.AtlasAttribute attr = et.getAttribute(attributeName);
        return (attr != null) ? attr.getRelationshipEdgeLabel() : "";
    }

    @Override
    public boolean hasAttribute(GremlinQueryComposer.Context context, String typeName) {
        return (context.getActiveEntityType() != null) &&
                (isSystemAttribute(typeName) || context.getActiveEntityType().getAttribute(typeName) != null);
    }

    @Override
    public boolean doesTypeHaveSubTypes(GremlinQueryComposer.Context context) {
        return (context.getActiveEntityType() != null && context.getActiveEntityType().getAllSubTypes().size() > 0);
    }

    @Override
    public String getTypeAndSubTypes(GremlinQueryComposer.Context context) {
        String[] str = context.getActiveEntityType() != null ?
                        context.getActiveEntityType().getTypeAndAllSubTypes().toArray(new String[]{}) :
                        new String[]{};
        if(str.length == 0) {
            return null;
        }

        String[] quoted = new String[str.length];
        for (int i = 0; i < str.length; i++) {
            quoted[i] = IdentifierHelper.getQuoted(str[i]);
        }

        return StringUtils.join(quoted, ",");
    }

    @Override
    public boolean isTraitType(String typeName) {
        BaseAtlasType t = null;
        try {
            if (typeName.equalsIgnoreCase(ALL_CLASSIFICATIONS)) {
                t = MATCH_ALL_CLASSIFIED;
            } else if (typeName.equalsIgnoreCase(NO_CLASSIFICATIONS)) {
                t = MATCH_ALL_NOT_CLASSIFIED;
            } else {
                t = typeRegistry.getType(typeName);
            }

        } catch (AtlasBaseException e) {
            return false;
        }

        return isTraitType(t);
    }

    private boolean isTraitType(BaseAtlasType t) {
        return (t != null && t.getTypeCategory() == TypeCategory.CLASSIFICATION);
    }

    @Override
    public String getTypeFromEdge(GremlinQueryComposer.Context context, String item) {
        AtlasEntityType et = context.getActiveEntityType();
        if(et == null) {
            return "";
        }

        AtlasStructType.AtlasAttribute attr = et.getAttribute(item);
        if(attr == null) {
            return null;
        }

        BaseAtlasType at = attr.getAttributeType();
        switch (at.getTypeCategory()) {
            case ARRAY:
                AtlasArrayType arrType = ((AtlasArrayType)at);
                return getCollectionElementType(arrType.getElementType());

            case MAP:
                AtlasMapType mapType = ((AtlasMapType)at);
                return getCollectionElementType(mapType.getValueType());
            default:
                return context.getActiveEntityType().getAttribute(item).getTypeName();
        }
    }

    private String getCollectionElementType(BaseAtlasType elemType) {
        if(elemType.getTypeCategory() == TypeCategory.OBJECT_ID_TYPE) {
            return ((AtlasBuiltInTypes.AtlasObjectIdType)elemType).getObjectType();
        } else {
            return elemType.getTypeName();
        }
    }

    @Override
    public boolean isDate(GremlinQueryComposer.Context context, String attributeName) {
        AtlasEntityType et = context.getActiveEntityType();
        if (et == null) {
            return false;
        }

        BaseAtlasType attr = et.getAttributeType(attributeName);
        return attr != null && attr.getTypeName().equals(BaseAtlasBaseTypeDef.ATLAS_TYPE_DATE);
    }

    @Override
    public boolean isNumeric(GremlinQueryComposer.Context context, String attrName) {
        AtlasEntityType et = context.getActiveEntityType();
        if (et == null) {
            return false;
        }

        BaseAtlasType attr = et.getAttributeType(attrName);
        boolean ret = attr != null && NUMERIC_ATTRIBUTES.containsKey(attr.getTypeName());
        if(ret) {
            context.setNumericTypeFormatter(NUMERIC_ATTRIBUTES.get(attr.getTypeName()));
        }

        return ret;
    }
}
