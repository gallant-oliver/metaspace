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
package org.apache.atlas.type;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasStruct;
import org.apache.atlas.model.typedef.AtlasStructDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef.Cardinality;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE;
import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF;
import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF;

/**
 * class that implements behaviour of a struct-type.
 */
public class AtlasStructType extends BaseAtlasType {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasStructType.class);

    private final AtlasStructDef structDef;

    protected Map<String, AtlasAttribute> allAttributes  = Collections.emptyMap();
    protected Map<String, AtlasAttribute> uniqAttributes = Collections.emptyMap();

    public AtlasStructType(AtlasStructDef structDef) {
        super(structDef);

        this.structDef = structDef;
    }

    public AtlasStructType(AtlasStructDef structDef, AtlasTypeRegistry typeRegistry) throws AtlasBaseException {
        super(structDef);

        this.structDef = structDef;

        this.resolveReferences(typeRegistry);
    }

    public AtlasStructDef getStructDef() { return structDef; }

    public BaseAtlasType getAttributeType(String attributeName) {
        AtlasAttribute attribute = getAttribute(attributeName);

        return attribute != null ? attribute.getAttributeType() : null;
    }

    public AtlasAttributeDef getAttributeDef(String attributeName) {
        AtlasAttribute attribute = getAttribute(attributeName);

        return attribute != null ? attribute.getAttributeDef() : null;
    }

    @Override
    void resolveReferences(AtlasTypeRegistry typeRegistry) throws AtlasBaseException {
        Map<String, AtlasAttribute> a = new HashMap<>();

        for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
            BaseAtlasType attrType  = typeRegistry.getType(attributeDef.getTypeName());
            AtlasAttribute attribute = new AtlasAttribute(this, attributeDef, attrType);

            Cardinality cardinality = attributeDef.getCardinality();

            if (cardinality == Cardinality.LIST || cardinality == Cardinality.SET) {
                if (!(attrType instanceof AtlasArrayType)) {
                    throw new AtlasBaseException(AtlasErrorCode.INVALID_ATTRIBUTE_TYPE_FOR_CARDINALITY,
                            getTypeName(), attributeDef.getName());
                }

                AtlasArrayType arrayType = (AtlasArrayType)attrType;

                arrayType.setMinCount(attributeDef.getValuesMinCount());
                arrayType.setMaxCount(attributeDef.getValuesMaxCount());
            }

            //check if attribute type is not classification
            if (attrType instanceof AtlasArrayType) {
                attrType = ((AtlasArrayType) attrType).getElementType();
            } else if (attrType instanceof AtlasMapType) {
                attrType = ((AtlasMapType) attrType).getValueType();
            }

            if (attrType instanceof AtlasClassificationType) {
                throw new AtlasBaseException(AtlasErrorCode.ATTRIBUTE_TYPE_INVALID, getTypeName(), attributeDef.getName());
            }

            a.put(attributeDef.getName(), attribute);
        }

        resolveConstraints(typeRegistry);

        this.allAttributes  = Collections.unmodifiableMap(a);
        this.uniqAttributes = getUniqueAttributes(this.allAttributes);
    }

    private void resolveConstraints(AtlasTypeRegistry typeRegistry) throws AtlasBaseException {
        for (AtlasAttributeDef attributeDef : getStructDef().getAttributeDefs()) {
            if (CollectionUtils.isEmpty(attributeDef.getConstraints())) {
                continue;
            }

            for (AtlasConstraintDef constraint : attributeDef.getConstraints()) {
                if (constraint.isConstraintType(CONSTRAINT_TYPE_OWNED_REF)) {
                    AtlasEntityType attrType = getReferencedEntityType(typeRegistry.getType(attributeDef.getTypeName()));

                    if (attrType == null) {
                        throw new AtlasBaseException(AtlasErrorCode.CONSTRAINT_OWNED_REF_ATTRIBUTE_INVALID_TYPE,
                                getTypeName(), attributeDef.getName(), CONSTRAINT_TYPE_OWNED_REF, attributeDef.getTypeName());
                    }
                } else if (constraint.isConstraintType(CONSTRAINT_TYPE_INVERSE_REF)) {
                    AtlasEntityType attrType = getReferencedEntityType(typeRegistry.getType(attributeDef.getTypeName()));

                    if (attrType == null) {
                        throw new AtlasBaseException(AtlasErrorCode.CONSTRAINT_INVERSE_REF_ATTRIBUTE_INVALID_TYPE,
                                getTypeName(), attributeDef.getName(), CONSTRAINT_TYPE_INVERSE_REF,
                                attributeDef.getTypeName());
                    }

                    String inverseRefAttrName = AtlasTypeUtil.getStringValue(constraint.getParams(), CONSTRAINT_PARAM_ATTRIBUTE);

                    if (StringUtils.isBlank(inverseRefAttrName)) {
                        throw new AtlasBaseException(AtlasErrorCode.CONSTRAINT_MISSING_PARAMS,
                                getTypeName(), attributeDef.getName(),
                                CONSTRAINT_PARAM_ATTRIBUTE, CONSTRAINT_TYPE_INVERSE_REF,
                                String.valueOf(constraint.getParams()));
                    }

                    AtlasAttributeDef inverseRefAttrDef = attrType.getStructDef().getAttribute(inverseRefAttrName);

                    if (inverseRefAttrDef == null) {
                        throw new AtlasBaseException(AtlasErrorCode.CONSTRAINT_INVERSE_REF_INVERSE_ATTRIBUTE_NON_EXISTING,
                                getTypeName(), attributeDef.getName(),
                                CONSTRAINT_TYPE_INVERSE_REF, attrType.getTypeName(), inverseRefAttrName);
                    }

                    AtlasEntityType inverseRefAttrType = getReferencedEntityType(typeRegistry.getType(inverseRefAttrDef.getTypeName()));

                    if (inverseRefAttrType == null) {
                        throw new AtlasBaseException(AtlasErrorCode.CONSTRAINT_INVERSE_REF_INVERSE_ATTRIBUTE_INVALID_TYPE,
                                getTypeName(), attributeDef.getName(),
                                CONSTRAINT_TYPE_INVERSE_REF, attrType.getTypeName(), inverseRefAttrName);
                    }
                }
            }
        }
    }

    @Override
    void resolveReferencesPhase2(AtlasTypeRegistry typeRegistry) throws AtlasBaseException {
        super.resolveReferencesPhase2(typeRegistry);
        for (AtlasAttribute attribute : allAttributes.values()) {
            if (attribute.getInverseRefAttributeName() == null) {
                continue;
            }
            // Set the inverse reference attribute.
            BaseAtlasType referencedType       = typeRegistry.getType(attribute.getAttributeDef().getTypeName());
            AtlasEntityType referencedEntityType = getReferencedEntityType(referencedType);
            AtlasAttribute  inverseReference     = referencedEntityType.getAttribute(attribute.getInverseRefAttributeName());

            attribute.setInverseRefAttribute(inverseReference);
        }
    }

    @Override
    public AtlasStruct createDefaultValue() {
        AtlasStruct ret = new AtlasStruct(structDef.getName());

        populateDefaultValues(ret);

        return  ret;
    }

    @Override
    public Object createDefaultValue(Object defaultValue) {
        AtlasStruct ret = new AtlasStruct(structDef.getName());

        populateDefaultValues(ret);

        return  ret;
    }

    public Map<String, AtlasAttribute> getAllAttributes() {
        return allAttributes;
    }

    public Map<String, AtlasAttribute> getUniqAttributes() {
        return uniqAttributes;
    }

    public AtlasAttribute getAttribute(String attributeName) {
        return allAttributes.get(attributeName);
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            if (obj instanceof AtlasStruct) {
                AtlasStruct structObj = (AtlasStruct) obj;

                for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                    if (!isAssignableValue(structObj.getAttribute(attributeDef.getName()), attributeDef)) {
                        return false;
                    }
                }
            } else if (obj instanceof Map) {
                Map map = AtlasTypeUtil.toStructAttributes((Map) obj);

                for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                    if (!isAssignableValue(map.get(attributeDef.getName()), attributeDef)) {
                        // no value for non-optinal attribute
                        return false;
                    }
                }
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        boolean ret = true;

        if (val1 == null) {
            ret = val2 == null;
        } else if (val2 == null) {
            ret = false;
        } else {
            AtlasStruct structVal1 = getStructFromValue(val1);

            if (structVal1 == null) {
                ret = false;
            } else {
                AtlasStruct structVal2 = getStructFromValue(val2);

                if (structVal2 == null) {
                    ret = false;
                } else if (!StringUtils.equalsIgnoreCase(structVal1.getTypeName(), structVal2.getTypeName())) {
                    ret = false;
                } else {
                    for (AtlasAttribute attribute : getAllAttributes().values()) {
                        Object attrValue1 = structVal1.getAttribute(attribute.getName());
                        Object attrValue2 = structVal2.getAttribute(attribute.getName());

                        if (!attribute.getAttributeType().areEqualValues(attrValue1, attrValue2, guidAssignments)) {
                            ret = false;

                            break;
                        }
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public boolean isValidValueForUpdate(Object obj) {
        if (obj != null) {
            Map<String, Object> attributes;

            if (obj instanceof AtlasStruct) {
                AtlasStruct structObj = (AtlasStruct) obj;
                attributes = structObj.getAttributes();

            } else if (obj instanceof Map) {
                attributes = AtlasTypeUtil.toStructAttributes((Map) obj);

            } else {
                return false;
            }

            if (MapUtils.isNotEmpty(attributes)) {
                for (Map.Entry<String, Object> e : attributes.entrySet()) {
                    String            attrName  = e.getKey();
                    Object            attrValue = e.getValue();
                    AtlasAttributeDef attrDef   = structDef.getAttribute(attrName);

                    if (attrValue == null || attrDef == null) {
                        continue;
                    }

                    if (!isAssignableValueForUpdate(attrValue, attrDef)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public Object getNormalizedValue(Object obj) {
        Object ret = null;

        if (obj != null) {
            if (isValidValue(obj)) {
                if (obj instanceof AtlasStruct) {
                    normalizeAttributeValues((AtlasStruct) obj);
                    ret = obj;
                } else if (obj instanceof Map) {
                    normalizeAttributeValues((Map) obj);
                    ret = obj;
                }
            }
        }

        return ret;
    }

    @Override
    public Object getNormalizedValueForUpdate(Object obj) {
        Object ret = null;

        if (obj != null) {
            if (isValidValueForUpdate(obj)) {
                if (obj instanceof AtlasStruct) {
                    normalizeAttributeValuesForUpdate((AtlasStruct) obj);
                    ret = obj;
                } else if (obj instanceof Map) {
                    normalizeAttributeValuesForUpdate((Map) obj);
                    ret = obj;
                }
            }
        }

        return ret;
    }

    @Override
    public boolean validateValue(Object obj, String objName, List<String> messages) {
        boolean ret = true;

        if (obj != null) {
            if (obj instanceof AtlasStruct) {
                AtlasStruct structObj = (AtlasStruct) obj;

                for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                    String         attrName  = attributeDef.getName();
                    AtlasAttribute attribute = allAttributes.get(attributeDef.getName());

                    if (attribute != null) {
                        BaseAtlasType dataType  = attribute.getAttributeType();
                        Object    value     = structObj.getAttribute(attrName);
                        String    fieldName = objName + "." + attrName;

                        if (value != null) {
                            ret = dataType.validateValue(value, fieldName, messages) && ret;
                        } else if (!attributeDef.getIsOptional()) {
                            // if required attribute is null, check if attribute value specified in relationship
                            if (structObj instanceof AtlasEntity) {
                                AtlasEntity entityObj = (AtlasEntity) structObj;

                                if (entityObj.getRelationshipAttribute(attrName) == null) {
                                    ret = false;
                                    messages.add(fieldName + ": mandatory attribute value missing in type " + getTypeName());
                                }
                            } else {
                                ret = false;
                                messages.add(fieldName + ": mandatory attribute value missing in type " + getTypeName());
                            }
                        }
                    }
                }
            } else if (obj instanceof Map) {
                Map attributes             = AtlasTypeUtil.toStructAttributes((Map)obj);
                Map relationshipAttributes = AtlasTypeUtil.toRelationshipAttributes((Map)obj);

                for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                    String             attrName  = attributeDef.getName();
                    AtlasAttribute     attribute = allAttributes.get(attributeDef.getName());

                    if (attribute != null) {
                        BaseAtlasType dataType  = attribute.getAttributeType();
                        Object    value     = attributes.get(attrName);
                        String    fieldName = objName + "." + attrName;

                        if (value != null) {
                            ret = dataType.validateValue(value, fieldName, messages) && ret;
                        } else if (!attributeDef.getIsOptional()) {
                            // if required attribute is null, check if attribute value specified in relationship
                            if (MapUtils.isEmpty(relationshipAttributes) || !relationshipAttributes.containsKey(attrName)) {
                                ret = false;
                                messages.add(fieldName + ": mandatory attribute value missing in type " + getTypeName());
                            }
                        }
                    }
                }
            } else {
                ret = false;
                messages.add(objName + "=" + obj + ": invalid value for type " + getTypeName());
            }
        }

        return ret;
    }

    @Override
    public boolean validateValueForUpdate(Object obj, String objName, List<String> messages) {
        boolean             ret        = true;
        Map<String, Object> attributes = null;

        if (obj != null) {
            if (obj instanceof AtlasStruct) {
                AtlasStruct structObj = (AtlasStruct) obj;
                attributes = structObj.getAttributes();

            } else if (obj instanceof Map) {
                attributes = AtlasTypeUtil.toStructAttributes((Map) obj);

            } else {
                ret = false;
                messages.add(objName + "=" + obj + ": invalid value for type " + getTypeName());
            }

            if (MapUtils.isNotEmpty(attributes)) {
                for (Map.Entry<String, Object> e : attributes.entrySet()) {
                    String         attrName  = e.getKey();
                    Object         attrValue = e.getValue();
                    AtlasAttribute attribute = allAttributes.get(attrName);

                    if (attrValue == null) {
                        continue;
                    }

                    if (attribute != null) {
                        BaseAtlasType dataType  = attribute.getAttributeType();
                        String    fieldName = objName + "." + attrName;

                        ret = dataType.validateValueForUpdate(attrValue, fieldName, messages) && ret;
                    }
                }
            }
        }

        return ret;
    }

    public void normalizeAttributeValues(AtlasStruct obj) {
        if (obj != null) {
            for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                String attributeName = attributeDef.getName();

                if (obj.hasAttribute(attributeName)) {
                    Object attributeValue = getNormalizedValue(obj.getAttribute(attributeName), attributeDef);

                    obj.setAttribute(attributeName, attributeValue);
                } else if (!attributeDef.getIsOptional()) {
                    obj.setAttribute(attributeName, createDefaultValue(attributeDef));
                }
            }
        }
    }

    public void normalizeAttributeValuesForUpdate(AtlasStruct obj) {
        if (obj != null) {
            for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                String attributeName = attributeDef.getName();

                if (obj.hasAttribute(attributeName)) {
                    Object attributeValue = getNormalizedValueForUpdate(obj.getAttribute(attributeName), attributeDef);
                    obj.setAttribute(attributeName, attributeValue);
                }
            }
        }
    }

    public void normalizeAttributeValues(Map<String, Object> obj) {
        if (obj != null) {
            for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                String attributeName = attributeDef.getName();

                if (obj.containsKey(attributeName)) {
                    Object attributeValue = getNormalizedValue(obj.get(attributeName), attributeDef);

                    obj.put(attributeName, attributeValue);
                } else if (!attributeDef.getIsOptional()) {
                    obj.put(attributeName, createDefaultValue(attributeDef));
                }
            }
        }
    }

    public void normalizeAttributeValuesForUpdate(Map<String, Object> obj) {
        if (obj != null) {
            for (AtlasAttributeDef attrDef : structDef.getAttributeDefs()) {
                String attrName  = attrDef.getName();
                Object attrValue = obj.get(attrName);

                if (obj.containsKey(attrName)) {
                    attrValue = getNormalizedValueForUpdate(attrValue, attrDef);
                    obj.put(attrName, attrValue);
                }
            }
        }
    }

    public void populateDefaultValues(AtlasStruct obj) {
        if (obj != null) {
            Map<String, Object> attributes = obj.getAttributes();

            if (attributes == null) {
                attributes = new HashMap<>();
            }

            for (AtlasAttributeDef attributeDef : structDef.getAttributeDefs()) {
                if (!attributeDef.getIsOptional()) {
                    attributes.put(attributeDef.getName(), createDefaultValue(attributeDef));
                }
            }

            obj.setAttributes(attributes);
        }
    }

    private Object createDefaultValue(AtlasAttributeDef attributeDef) {
        Object ret = null;

        if (attributeDef != null) {
            AtlasAttribute attribute = allAttributes.get(attributeDef.getName());

            if (attribute != null) {
                BaseAtlasType dataType = attribute.getAttributeType();

                ret = dataType.createDefaultValue(attributeDef.getDefaultValue());
            }
        }

        return ret;
    }

    private boolean isAssignableValue(Object value, AtlasAttributeDef attributeDef) {
        boolean ret = true;

        if (value != null) {
            AtlasAttribute attribute = allAttributes.get(attributeDef.getName());

            if (attribute != null) {
                BaseAtlasType attrType = attribute.getAttributeType();

                if (!attrType.isValidValue(value)) {
                    // invalid value
                    ret = false;
                }
            }
        } else if (!attributeDef.getIsOptional()) {
            // mandatory attribute not present
            ret = false;
        }

        return ret;
    }

    private boolean isAssignableValueForUpdate(Object value, AtlasAttributeDef attributeDef) {
        boolean ret = true;

        if (value != null) {
            AtlasAttribute attribute = allAttributes.get(attributeDef.getName());

            if (attribute != null) {
                BaseAtlasType attrType = attribute.getAttributeType();

                if (!attrType.isValidValueForUpdate(value)) {
                    // invalid value
                    ret = false;
                }
            }
        }

        return ret;
    }

    private Object getNormalizedValue(Object value, AtlasAttributeDef attributeDef) {
        AtlasAttribute attribute = allAttributes.get(attributeDef.getName());

        if (attribute != null) {
            BaseAtlasType attrType = attribute.getAttributeType();

            if (value == null) {
                if (!attributeDef.getIsOptional()) {
                    return attrType.createDefaultValue();
                }
            } else {
                return attrType.getNormalizedValue(value);
            }
        }

        return null;
    }

    private Object getNormalizedValueForUpdate(Object value, AtlasAttributeDef attributeDef) {
        AtlasAttribute attribute = allAttributes.get(attributeDef.getName());

        if (attribute != null) {
            BaseAtlasType attrType = attribute.getAttributeType();

            if (value != null) {
                return attrType.getNormalizedValueForUpdate(value);
            }
        }

        return null;
    }

    public String getQualifiedAttributeName(String attrName) throws AtlasBaseException {
        if ( allAttributes.containsKey(attrName)) {
            return allAttributes.get(attrName).getQualifiedName();
        }

        throw new AtlasBaseException(AtlasErrorCode.UNKNOWN_ATTRIBUTE, attrName, structDef.getName());
    }

    public String getQualifiedAttributePropertyKey(String attrName) throws AtlasBaseException {
        if ( allAttributes.containsKey(attrName)) {
            return allAttributes.get(attrName).getVertexPropertyName();
        }

        throw new AtlasBaseException(AtlasErrorCode.UNKNOWN_ATTRIBUTE, attrName, structDef.getName());
    }

    AtlasEntityType getReferencedEntityType(BaseAtlasType type) {
        if (type instanceof AtlasArrayType) {
            type = ((AtlasArrayType)type).getElementType();
        }

        if (type instanceof AtlasMapType) {
            type = ((AtlasMapType)type).getValueType();
        }

        return type instanceof AtlasEntityType ? (AtlasEntityType)type : null;
    }

    protected Map<String, AtlasAttribute> getUniqueAttributes(Map<String, AtlasAttribute> attributes) {
        Map<String, AtlasAttribute> ret = new HashMap<>();

        if (MapUtils.isNotEmpty(attributes)) {
            for (AtlasAttribute attribute : attributes.values()) {
                if (attribute.getAttributeDef().getIsUnique()) {
                    ret.put(attribute.getName(), attribute);
                }
            }
        }

        return Collections.unmodifiableMap(ret);
    }

    private AtlasStruct getStructFromValue(Object val) {
        final AtlasStruct ret;

        if (val instanceof AtlasStruct) {
            ret = (AtlasStruct) val;
        } else if (val instanceof Map) {
            ret = new AtlasStruct((Map) val);
        } else if (val instanceof String) {
            Map map = BaseAtlasType.fromJson(val.toString(), Map.class);

            if (map == null) {
                ret = null;
            } else {
                ret = new AtlasStruct((Map) val);
            }
        } else {
            ret = null;
        }

        return ret;
    }

    public static class AtlasAttribute {
        private final AtlasStructType          definedInType;
        private final BaseAtlasType attributeType;
        private final AtlasAttributeDef        attributeDef;
        private final String                   qualifiedName;
        private final String                   vertexPropertyName;
        private final boolean                  isOwnedRef;
        private final String                   inverseRefAttributeName;
        private AtlasAttribute                 inverseRefAttribute;
        private String                         relationshipEdgeLabel;
        private AtlasRelationshipEdgeDirection relationshipEdgeDirection;

        public AtlasAttribute(AtlasStructType definedInType, AtlasAttributeDef attrDef, BaseAtlasType attributeType, String relationshipLabel) {
            this.definedInType            = definedInType;
            this.attributeDef             = attrDef;
            this.attributeType            = attributeType.getTypeForAttribute();
            this.qualifiedName            = getQualifiedAttributeName(definedInType.getStructDef(), attributeDef.getName());
            this.vertexPropertyName       = encodePropertyKey(this.qualifiedName);
            this.relationshipEdgeLabel    = getRelationshipEdgeLabel(relationshipLabel);
            boolean isOwnedRef            = false;
            String  inverseRefAttribute   = null;

            if (CollectionUtils.isNotEmpty(attributeDef.getConstraints())) {
                for (AtlasConstraintDef constraint : attributeDef.getConstraints()) {
                    if (constraint.isConstraintType(CONSTRAINT_TYPE_OWNED_REF)) {
                        isOwnedRef = true;
                    }

                    if (constraint.isConstraintType(CONSTRAINT_TYPE_INVERSE_REF)) {
                        Object val = constraint.getParam(CONSTRAINT_PARAM_ATTRIBUTE);

                        if (val != null) {
                            inverseRefAttribute = val.toString();
                        }
                    }
                }
            }

            this.isOwnedRef                = isOwnedRef;
            this.inverseRefAttributeName   = inverseRefAttribute;
            this.relationshipEdgeDirection = AtlasRelationshipEdgeDirection.OUT;
        }

        public AtlasAttribute(AtlasStructType definedInType, AtlasAttributeDef attrDef, BaseAtlasType attributeType) {
            this(definedInType, attrDef, attributeType, null);
        }

        public AtlasStructType getDefinedInType() { return definedInType; }

        public AtlasStructDef getDefinedInDef() { return definedInType.getStructDef(); }

        public BaseAtlasType getAttributeType() {
            return attributeType;
        }

        public AtlasAttributeDef getAttributeDef() {
            return attributeDef;
        }

        public String getName() { return attributeDef.getName(); }

        public String getTypeName() { return attributeDef.getTypeName(); }

        public String getQualifiedName() { return qualifiedName; }

        public String getVertexPropertyName() { return vertexPropertyName; }

        public boolean isOwnedRef() { return isOwnedRef; }

        public String getInverseRefAttributeName() { return inverseRefAttributeName; }

        public AtlasAttribute getInverseRefAttribute() { return inverseRefAttribute; }

        public void setInverseRefAttribute(AtlasAttribute inverseAttr) { inverseRefAttribute = inverseAttr; }

        public String getRelationshipEdgeLabel() { return relationshipEdgeLabel; }

        public void setRelationshipEdgeLabel(String relationshipEdgeLabel) { this.relationshipEdgeLabel = relationshipEdgeLabel; }

        public AtlasRelationshipEdgeDirection getRelationshipEdgeDirection() { return relationshipEdgeDirection; }

        public void setRelationshipEdgeDirection(AtlasRelationshipEdgeDirection relationshipEdgeDirection) {
            this.relationshipEdgeDirection = relationshipEdgeDirection;
        }

        public static String getEdgeLabel(String property) {
            return "__" + property;
        }

        public static String encodePropertyKey(String key) {
            if (StringUtils.isBlank(key)) {
                return key;
            }

            for (String[] strMap : RESERVED_CHAR_ENCODE_MAP) {
                key = key.replace(strMap[0], strMap[1]);
            }

            return key;
        }

        public static String decodePropertyKey(String key) {
            if (StringUtils.isBlank(key)) {
                return key;
            }

            for (String[] strMap : RESERVED_CHAR_ENCODE_MAP) {
                key = key.replace(strMap[1], strMap[0]);
            }

            return key;
        }

        public static String escapeIndexQueryValue(Collection<String> values) {
            StringBuilder sb = new StringBuilder();

            sb.append(BRACE_OPEN_CHAR);

            if (CollectionUtils.isNotEmpty(values)) {
                Iterator<String> iter = values.iterator();

                sb.append(escapeIndexQueryValue(iter.next()));

                while (iter.hasNext()) {
                    sb.append(SPACE_CHAR).append(escapeIndexQueryValue(iter.next()));
                }
            }

            sb.append(BRACE_CLOSE_CHAR);

            return sb.toString();
        }

        public static String escapeIndexQueryValue(String value) {
            String ret = value;

            if (StringUtils.containsAny(value, IDX_QRY_OFFENDING_CHARS)) {
                boolean isQuoteAtStart = value.charAt(0) == DOUBLE_QUOTE_CHAR;
                boolean isQuoteAtEnd   = value.charAt(value.length() - 1) == DOUBLE_QUOTE_CHAR;

                if (!isQuoteAtStart) {
                    if (!isQuoteAtEnd) {
                        ret = DOUBLE_QUOTE_CHAR + value + DOUBLE_QUOTE_CHAR;
                    } else {
                        ret = DOUBLE_QUOTE_CHAR + value;
                    }
                } else if (!isQuoteAtEnd) {
                    ret = value + DOUBLE_QUOTE_CHAR;
                }
            }

            return ret;
        }

        private String getRelationshipEdgeLabel(String relationshipLabel) {
            return (relationshipLabel == null) ? getEdgeLabel(vertexPropertyName) : relationshipLabel;
        }

        private static String getQualifiedAttributeName(AtlasStructDef structDef, String attrName) {
            final String typeName = structDef.getName();
            return attrName.contains(".") ? attrName : String.format("%s.%s", typeName, attrName);
        }

        // Keys copied from org.janusgraph.graphdb.types.system.SystemTypeManager.RESERVED_CHARS
        // JanusGraph checks that these chars are not part of any keys hence encoding
        // also including Titan reserved characters to support migrated property keys
        private static String[][] RESERVED_CHAR_ENCODE_MAP = new String[][]{
                new String[] {"{", "_o"},
                new String[] {"}", "_c"},
                new String[] {"\"", "_q"},
                //titan reserved character
                new String[] {"$", "_d"},
                //titan reserved characters
                new String[] {"%", "_p"},
        };

        private static final char[] IDX_QRY_OFFENDING_CHARS = { '@', '/', ' ', '-' };
        private static final char   BRACE_OPEN_CHAR         = '(';
        private static final char   BRACE_CLOSE_CHAR        = ')';
        private static final char   DOUBLE_QUOTE_CHAR       = '"';
        private static final char   SPACE_CHAR              = ' ';

        public enum AtlasRelationshipEdgeDirection { IN, OUT, BOTH }
    }
}
