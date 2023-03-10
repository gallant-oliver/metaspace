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
import org.apache.atlas.model.instance.AtlasRelationship;
import org.apache.atlas.model.typedef.AtlasRelationshipDef;
import org.apache.atlas.model.typedef.AtlasRelationshipDef.RelationshipCategory;
import org.apache.atlas.model.typedef.AtlasRelationshipEndDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef.Cardinality;
import org.apache.atlas.model.typedef.BaseAtlasBaseTypeDef;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

import static org.apache.atlas.type.AtlasStructType.AtlasAttribute.AtlasRelationshipEdgeDirection;
import static org.apache.atlas.type.AtlasStructType.AtlasAttribute.AtlasRelationshipEdgeDirection.*;

/**
 * class that implements behaviour of an relationship-type.
 */
public class AtlasRelationshipType extends AtlasStructType {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasRelationshipType.class);

    private final AtlasRelationshipDef relationshipDef;
    private final boolean              hasLegacyAttributeEnd;
    private       AtlasEntityType      end1Type;
    private       AtlasEntityType      end2Type;

    public AtlasRelationshipType(AtlasRelationshipDef relationshipDef) {
        super(relationshipDef);

        AtlasRelationshipEndDef end1Def = relationshipDef != null ? relationshipDef.getEndDef1() : null;
        AtlasRelationshipEndDef end2Def = relationshipDef != null ? relationshipDef.getEndDef2() : null;

        this.relationshipDef       = relationshipDef;
        this.hasLegacyAttributeEnd = (end1Def != null && end1Def.getIsLegacyAttribute()) || (end2Def != null && end2Def.getIsLegacyAttribute());
    }

    public AtlasRelationshipType(AtlasRelationshipDef relationshipDef, AtlasTypeRegistry typeRegistry) throws AtlasBaseException {
        this(relationshipDef);

        resolveReferences(typeRegistry);
    }

    public AtlasRelationshipDef getRelationshipDef() { return relationshipDef; }

    public boolean hasLegacyAttributeEnd() {
        return this.hasLegacyAttributeEnd;
    }

    @Override
    void resolveReferences(AtlasTypeRegistry typeRegistry) throws AtlasBaseException {
        super.resolveReferences(typeRegistry);

        if (relationshipDef == null) {
            throw new AtlasBaseException(AtlasErrorCode.INVALID_VALUE, "relationshipDef is null");
        }

        String end1TypeName = relationshipDef.getEndDef1() != null ? relationshipDef.getEndDef1().getType() : null;
        String end2TypeName = relationshipDef.getEndDef2() != null ? relationshipDef.getEndDef2().getType() : null;
        LOG.info("end1TypeName is {},end2TypeName is {}", end1TypeName, end2TypeName);
        if (StringUtils.isEmpty(end1TypeName)) {
            throw new AtlasBaseException(AtlasErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef1", "type");
        } else {
            BaseAtlasType type1 = typeRegistry.getType(end1TypeName);
            if (type1 instanceof AtlasEntityType) {
                end1Type = (AtlasEntityType) type1;
            } else {
                throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_INVALID_END_TYPE, getTypeName(), end1TypeName);
            }
        }
        if (StringUtils.isEmpty(end2TypeName)) {
            throw new AtlasBaseException(AtlasErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef2", "type");
        } else {
            BaseAtlasType type2 = typeRegistry.getType(end2TypeName);

            if (type2 instanceof AtlasEntityType) {
                end2Type = (AtlasEntityType) type2;
            } else {
                throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_INVALID_END_TYPE, getTypeName(), end2TypeName);
            }
        }

        if (StringUtils.isEmpty(relationshipDef.getEndDef1().getName())) {
            throw new AtlasBaseException(AtlasErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef1", "name");
        }

        if (StringUtils.isEmpty(relationshipDef.getEndDef2().getName())) {
            throw new AtlasBaseException(AtlasErrorCode.MISSING_MANDATORY_ATTRIBUTE, "endDef2", "name");
        }
        validateAtlasRelationshipDef(relationshipDef);
    }

    @Override
    void resolveReferencesPhase2(AtlasTypeRegistry typeRegistry) throws AtlasBaseException {
        super.resolveReferencesPhase2(typeRegistry);

        AtlasRelationshipEndDef endDef1           = relationshipDef.getEndDef1();
        AtlasRelationshipEndDef endDef2           = relationshipDef.getEndDef2();
        String                  relationshipLabel = null;

        // if legacyLabel is not specified at both ends, use relationshipDef name as relationship label.
        // if legacyLabel is specified in any one end, use it as the relationship label for both ends (legacy case).
        // if legacyLabel is specified at both ends use the respective end's legacyLabel as relationship label (legacy case).
        if (!endDef1.getIsLegacyAttribute() && !endDef2.getIsLegacyAttribute()) {
            relationshipLabel = relationshipDef.getRelationshipLabel();
        } else if (endDef1.getIsLegacyAttribute() && !endDef2.getIsLegacyAttribute()) {
            relationshipLabel = getLegacyEdgeLabel(end1Type, endDef1.getName());
        } else if (!endDef1.getIsLegacyAttribute() && endDef2.getIsLegacyAttribute()) {
            relationshipLabel = getLegacyEdgeLabel(end2Type, endDef2.getName());
        }

        addRelationshipAttributeToEndType(endDef1, end1Type, end2Type.getTypeName(), typeRegistry, relationshipLabel);

        addRelationshipAttributeToEndType(endDef2, end2Type, end1Type.getTypeName(), typeRegistry, relationshipLabel);

        // add relationship edge direction information
        addRelationshipEdgeDirection();
    }

    private void addRelationshipEdgeDirection() {
        AtlasRelationshipEndDef endDef1 = relationshipDef.getEndDef1();
        AtlasRelationshipEndDef endDef2 = relationshipDef.getEndDef2();

        if (StringUtils.equals(endDef1.getType(), endDef2.getType()) &&
                StringUtils.equals(endDef1.getName(), endDef2.getName())) {

            AtlasAttribute endAttribute = end1Type.getRelationshipAttribute(endDef1.getName());

            endAttribute.setRelationshipEdgeDirection(BOTH);
        } else {
            AtlasAttribute end1Attribute = end1Type.getRelationshipAttribute(endDef1.getName());
            AtlasAttribute end2Attribute = end2Type.getRelationshipAttribute(endDef2.getName());

            //default relationship edge direction is end1 (out) -> end2 (in)
            AtlasRelationshipEdgeDirection end1Direction = OUT;
            AtlasRelationshipEdgeDirection end2Direction = IN;

            if (endDef1.getIsLegacyAttribute() && endDef2.getIsLegacyAttribute()) {
                end2Direction = OUT;
            } else if (!endDef1.getIsLegacyAttribute() && endDef2.getIsLegacyAttribute()) {
                end1Direction = IN;
                end2Direction = OUT;
            }

            end1Attribute.setRelationshipEdgeDirection(end1Direction);
            end2Attribute.setRelationshipEdgeDirection(end2Direction);
        }
    }

    @Override
    public boolean isValidValue(Object obj) {
        if (obj != null) {
            if (obj instanceof AtlasRelationship) {
                return validateRelationship((AtlasRelationship) obj);
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean areEqualValues(Object val1, Object val2, Map<String, String> guidAssignments) {
        final boolean ret;

        if (val1 == null) {
            ret = val2 == null;
        } else if (val2 == null) {
            ret = false;
        } else {
            AtlasRelationship rel1 = getRelationshipFromValue(val1);

            if (rel1 == null) {
                ret = false;
            } else {
                AtlasRelationship rel2 = getRelationshipFromValue(val2);

                if (rel2 == null) {
                    ret = false;
                } else if (!super.areEqualValues(rel1, rel2, guidAssignments)) {
                    ret = false;
                } else {
                    ret = Objects.equals(rel1.getGuid(), rel2.getGuid()) &&
                            Objects.equals(rel1.getEnd1(), rel2.getEnd1()) &&
                            Objects.equals(rel1.getEnd2(), rel2.getEnd2()) &&
                            Objects.equals(rel1.getLabel(), rel2.getLabel()) &&
                            Objects.equals(rel1.getPropagateTags(), rel2.getPropagateTags()) &&
                            Objects.equals(rel1.getStatus(), rel2.getStatus());
                }
            }
        }

        return ret;
    }

    @Override
    public boolean isValidValueForUpdate(Object obj) {
        if (obj != null) {
            if (obj instanceof AtlasRelationship) {
                return validateRelationship((AtlasRelationship) obj);
            } else {
                return false;
            }
        }

        return true;
    }

    public AtlasEntityType getEnd1Type() { return end1Type; }

    public AtlasEntityType getEnd2Type() { return end2Type; }

    /**
     * Validate the fields in the the RelationshipType are consistent with respect to themselves.
     * @param relationship
     * @throws AtlasBaseException
     */
    private boolean validateRelationship(AtlasRelationship relationship) {
        String end1TypeName = relationship.getEnd1() != null ? relationship.getEnd1().getTypeName() : null;
        String end2TypeName = relationship.getEnd2() != null ? relationship.getEnd2().getTypeName() : null;

        if (StringUtils.isNotEmpty(end1TypeName) && StringUtils.isNotEmpty(end2TypeName)) {
            return end1Type.isTypeOrSuperTypeOf(end1TypeName) && end2Type.isTypeOrSuperTypeOf(end2TypeName) && super.isValidValue(relationship);
        }

        return false;
    }

    /**
     * Throw an exception so we can junit easily.
     *
     * This method assumes that the 2 ends are not null.
     *
     * @param relationshipDef
     * @throws AtlasBaseException
     */
    public static void validateAtlasRelationshipDef(AtlasRelationshipDef relationshipDef) throws AtlasBaseException {

        AtlasRelationshipEndDef endDef1              = relationshipDef.getEndDef1();
        AtlasRelationshipEndDef endDef2              = relationshipDef.getEndDef2();
        RelationshipCategory    relationshipCategory = relationshipDef.getRelationshipCategory();
        String                  name                 = relationshipDef.getName();
        boolean                 isContainer1         = endDef1.getIsContainer();
        boolean                 isContainer2         = endDef2.getIsContainer();

        if ((endDef1.getCardinality() == AtlasAttributeDef.Cardinality.LIST) ||
                (endDef2.getCardinality() == AtlasAttributeDef.Cardinality.LIST)) {
            throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_LIST_ON_END, name);
        }
        if (isContainer1 && isContainer2) {
            // we support 0 or 1 of these flags.
            throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_DOUBLE_CONTAINERS, name);
        }
        if ((isContainer1 || isContainer2)) {
            // we have an isContainer defined in an end
            if (relationshipCategory == RelationshipCategory.ASSOCIATION) {
                // associations are not containment relationships - so do not allow an endpoint with isContainer
                throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_ASSOCIATION_AND_CONTAINER, name);
            }
        } else {
            // we do not have an isContainer defined on an end
            if (relationshipCategory == RelationshipCategory.COMPOSITION) {
                // COMPOSITION needs one end to be the container.
                throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_COMPOSITION_NO_CONTAINER, name);
            } else if (relationshipCategory == RelationshipCategory.AGGREGATION) {
                // AGGREGATION needs one end to be the container.
                throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_AGGREGATION_NO_CONTAINER, name);
            }
        }
        if (relationshipCategory == RelationshipCategory.COMPOSITION) {
            // composition children should not be multiple cardinality
            if (endDef1.getCardinality() == AtlasAttributeDef.Cardinality.SET &&
                    !endDef1.getIsContainer()) {
                throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_COMPOSITION_MULTIPLE_PARENTS, name);
            }
            if ((endDef2.getCardinality() == AtlasAttributeDef.Cardinality.SET) &&
                    !endDef2.getIsContainer()) {
                throw new AtlasBaseException(AtlasErrorCode.RELATIONSHIPDEF_COMPOSITION_MULTIPLE_PARENTS, name);
            }
        }
    }

    private void addRelationshipAttributeToEndType(AtlasRelationshipEndDef endDef, AtlasEntityType entityType, String attrTypeName,
                                                   AtlasTypeRegistry typeRegistry, String relationshipLabel) throws AtlasBaseException {

        String attrName = (endDef != null) ? endDef.getName() : null;

        if (StringUtils.isEmpty(attrName)) {
            return;
        }

        AtlasAttribute attribute = entityType.getAttribute(attrName);

        // if relationshipLabel is null, then legacyLabel is mentioned at both ends,
        // use the respective end's legacyLabel as relationshipLabel
        if (relationshipLabel == null) {
            relationshipLabel = getLegacyEdgeLabel(entityType, attrName);
        }
        //attr doesn't exist in type - is a new relationship attribute
        if (attribute == null) {

            if (endDef.getCardinality() == Cardinality.SET) {
                attrTypeName = BaseAtlasBaseTypeDef.getArrayTypeName(attrTypeName);
            }

            attribute = new AtlasAttribute(entityType, new AtlasAttributeDef(attrName, attrTypeName),
                    typeRegistry.getType(attrTypeName), relationshipLabel);

        } else {
            // attribute already exists (legacy attribute which is also a relationship attribute)
            // add relationshipLabel information to existing attribute
            attribute.setRelationshipEdgeLabel(relationshipLabel);
        }

        entityType.addRelationshipAttribute(attrName, attribute);

        entityType.addRelationshipAttributeType(attrName, this);
    }

    private String getLegacyEdgeLabel(AtlasEntityType entityType, String attributeName) {
        String         ret       = null;
        AtlasAttribute attribute = entityType.getAttribute(attributeName);

        if (attribute != null) {
            ret = "__" + attribute.getQualifiedName();
        }

        return ret;
    }

    private AtlasRelationship getRelationshipFromValue(Object val) {
        final AtlasRelationship ret;

        if (val instanceof AtlasRelationship) {
            ret = (AtlasRelationship) val;
        } else if (val instanceof Map) {
            ret = new AtlasRelationship((Map) val);
        } else {
            ret = null;
        }

        return ret;
    }
}
