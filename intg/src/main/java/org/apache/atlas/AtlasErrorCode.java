/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.Arrays;

public enum AtlasErrorCode {
    NO_SEARCH_RESULTS(204, "METASPACE-204-00-001", " 给定搜索过滤器{0}没有产生任何结果"),
    DATA_ACCESS_SAVE_FAILED(204, "METASPACE-204-00-002", " 保存失败:{0}没有更新"),

    UNKNOWN_TYPE(400, "METASPACE-400-00-001", " {1}的未知类型{0}.{2}"),
    CIRCULAR_REFERENCE(400, "METASPACE-400-00-002", " {0}:无效的超类型-循环引用回self {1}"),
    INCOMPATIBLE_SUPERTYPE(400, "METASPACE-400-00-003", " {0}:不兼容的超型{1}"),
    UNKNOWN_CONSTRAINT(400, "METASPACE-400-00-004", " {0},{1}:未知约束{1}"),
    UNSUPPORTED_CONSTRAINT(400, "METASPACE-400-00-005", " {0},{1}:{2}约束不支持"),
    CONSTRAINT_NOT_SATISFIED(400, "METASPACE-400-00-006", " {0},{1}:{2}不兼容属性类型{3}"),
    CONSTRAINT_MISSING_PARAMS(400, "METASPACE-400-00-007", " {0},{1}:无效约束,{3}中缺少参数{2},params = {4}"),
    CONSTRAINT_NOT_EXIST(400, "METASPACE-400-00-008", " {0},{1}:无效约束,{2} {3},{4}不存在"),
    CONSTRAINT_NOT_MATCHED(400, "METASPACE-400-00-009", " {0},{1}:无效约束,{2}{3}的数据类型,{4}应该是{5},但发现{6}"),
    UNKNOWN_TYPENAME(400, "METASPACE-400-00-00A", " {0}:未知/无效typename"),
    CONSTRAINT_NOT_SUPPORTED_ON_MAP_TYPE(400, "METASPACE-400-00-00B", " {0},{1}: map类型{2}不支持的约束"),
    CANNOT_ADD_MANDATORY_ATTRIBUTE(400, "METASPACE-400-00-00C", " {0},{1}:不能添加强制属性"),
    ATTRIBUTE_DELETION_NOT_SUPPORTED(400, "METASPACE-400-00-00D", " {0},{1}:属性删除不支持"),
    SUPERTYPE_REMOVAL_NOT_SUPPORTED(400, "METASPACE-400-00-00E", " 超类型删除不支持"),
    UNEXPECTED_TYPE(400, "METASPACE-400-00-00F", " 预期类型{0};发现{1}"),
    TYPE_MATCH_FAILED(400, "METASPACE-400-00-010", " 假设类型{0}不匹配{1}"),
    INVALID_TYPE_DEFINITION(400, "METASPACE-400-00-011", " 无效类型定义{0}"),
    INVALID_ATTRIBUTE_TYPE_FOR_CARDINALITY(400, "METASPACE-400-00-012", " 属性{0}的基数,{1}需要一个列表或设置类型"),
    ATTRIBUTE_UNIQUE_INVALID(400, "METASPACE-400-00-013", " 具有唯一属性{1}的类型{0}不存在"),
    TYPE_NAME_INVALID(400, "METASPACE-400-00-014", " 类型{0}名称{1}不存在"),
    TYPE_CATEGORY_INVALID(400, "METASPACE-400-00-015", " 类型类别{0}无效"),
    PATCH_NOT_APPLICABLE_FOR_TYPE(400, "METASPACE-400-00-016", " {0} -{1}类型的无效补丁"),
    PATCH_FOR_UNKNOWN_TYPE(400, "METASPACE-400-00-017", " {0} -补丁引用未知类型{1}"),
    PATCH_INVALID_DATA(400, "METASPACE-400-00-018", " {0} - patch数据对于{1}类型无效"),
    TYPE_NAME_INVALID_FORMAT(400, "METASPACE-400-00-019", " {0}:{1}的无效名称,姓名必须由字母、数字或'_'字符组成"),
    ATTRIBUTE_NAME_INVALID(400, "METASPACE-400-00-020", " {0}:无效的名字,属性名不能包含查询关键字"),
    INVALID_PARAMETERS(400, "METASPACE-400-00-01A", " 无效的参数:{0}"),
    CLASSIFICATION_ALREADY_ASSOCIATED(400, "METASPACE-400-00-01B", " 实例{0}已经关联到分类{1}"),
    CONSTRAINT_INVERSE_REF_ATTRIBUTE_INVALID_TYPE(400, "METASPACE-400-00-01C", " {0},{1}:无效的{2}约束,属性{3}不是实体类型"),
    CONSTRAINT_INVERSE_REF_INVERSE_ATTRIBUTE_NON_EXISTING(400, "METASPACE-400-00-01D", " {0},{1}:无效的{2}约束,{3}逆属性,{4}不存在"),
    CONSTRAINT_INVERSE_REF_INVERSE_ATTRIBUTE_INVALID_TYPE(400, "METASPACE-400-00-01E", " {0},{1}:无效的{2}约束,{3}逆属性,{4}不是实体类型"),
    CONSTRAINT_OWNED_REF_ATTRIBUTE_INVALID_TYPE(400, "METASPACE-400-00-01F", " {0},{1}:无效的{2}约束,属性{3}不是实体类型"),
    CANNOT_MAP_ATTRIBUTE(400, "METASPACE-400-00-020", " 不能映射属性:{0}的类型:{1}从顶点"),
    INVALID_OBJECT_ID(400, "METASPACE-400-00-021", " ObjectId无效{0}"),
    UNRESOLVED_REFERENCES_FOUND(400, "METASPACE-400-00-022", " 未解决的引用:byId = {0};byUniqueAttributes = {1} "),
    UNKNOWN_ATTRIBUTE(400, "METASPACE-400-00-023", " 类型{1}没有找到{0}属性"),
    SYSTEM_TYPE(400, "METASPACE-400-00-024", " {0}是一个系统类型"),
    INVALID_STRUCT_VALUE(400, "METASPACE-400-00-025", " 没有有效的struct值{0}"),
    INSTANCE_LINEAGE_INVALID_PARAMS(400, "METASPACE-400-00-026", " 传递给{0}:{1}的沿袭查询参数无效"),
    ATTRIBUTE_UPDATE_NOT_SUPPORTED(400, "METASPACE-400-00-027", " {0},{1}:属性更新不支持"),
    INVALID_VALUE(400, "METASPACE-400-00-028", " 无效的值:{0}"),
    BAD_REQUEST(400, "METASPACE-400-00-029", " {0}"),
    PARAMETER_PARSING_FAILED(400, "METASPACE-400-00-02A", " 参数解析失败在:{0}"),
    MISSING_MANDATORY_ATTRIBUTE(400, "METASPACE-400-00-02B", " 强制性字段{0},{1}有空/空值"),
    RELATIONSHIPDEF_INSUFFICIENT_ENDS(400, "METASPACE-400-00-02C", " 在没有2个结束的情况下尝试创建relationshipDef{0}"),
    RELATIONSHIPDEF_DOUBLE_CONTAINERS(400, "METASPACE-400-00-02D", " 将两端都作为容器尝试创建relationshipDef {0}"),
    RELATIONSHIPDEF_UNSUPPORTED_ATTRIBUTE_TYPE(400, "METASPACE-400-00-02F", " 在def{1}关系上不能设置类型为{0}的属性,因为它不是原始类型"),
    RELATIONSHIPDEF_ASSOCIATION_AND_CONTAINER(400, "METASPACE-400-00-030", " 试图创建关联relationshipDef{0},其末端指定isContainer"),
    RELATIONSHIPDEF_COMPOSITION_NO_CONTAINER(400, "METASPACE-400-00-031", " 在没有指定isContainer的情况下尝试创建复合关系定义{0}"),
    RELATIONSHIPDEF_AGGREGATION_NO_CONTAINER(400, "METASPACE-400-00-032", " 尝试创建聚合relationshipDef{0},但未指定结束符isContainer"),
    RELATIONSHIPDEF_COMPOSITION_MULTIPLE_PARENTS(400, "METASPACE-400-00-033", " 复合关系定义{0}只能有一个父类;所以不能在子结点上设置基数"),
    RELATIONSHIPDEF_LIST_ON_END(400, "METASPACE-400-00-034", " 关系定义{0}不能有一个列表基数在结束"),
    RELATIONSHIPDEF_INVALID_END_TYPE(400, "METASPACE-400-00-035", " {0}端类型{1}无效"),
    INVALID_RELATIONSHIP_END_TYPE(400, "METASPACE-400-00-036", " 无效的relationshipDef:{0}:结束类型1:{1},结束类型2:{2}"),
    RELATIONSHIPDEF_INVALID_END1_UPDATE(400, "METASPACE-400-00-037", " 对relationshipDef{0}的更新无效:新的end1{1},现有的end1{2}"),
    RELATIONSHIPDEF_INVALID_END2_UPDATE(400, "METASPACE-400-00-038", " 对relationshipDef{0}的更新无效:新的end2{1},现有的end2{2}"),
    RELATIONSHIPDEF_INVALID_CATEGORY_UPDATE(400, "METASPACE-400-00-039", " 关系{0}更新无效:new relationshipDef category {1}, existing relationshipDef category{2}"),
    RELATIONSHIPDEF_INVALID_NAME_UPDATE(400, "METASPACE-400-00-040", " 无效的relationshipDef重新命名为关系guid{0}:新的名称{1},现有的名称{2}"),
    RELATIONSHIPDEF_END1_NAME_INVALID(400, "METASPACE-400-00-041", " {0}:无效的end1名称,名称不能包含查询关键字"),
    RELATIONSHIPDEF_END2_NAME_INVALID(400, "METASPACE-400-00-042", " {0}:无效的end2名称,名称不能包含查询关键字"),
    RELATIONSHIPDEF_NOT_DEFINED(400, "METASPACE-400-00-043", " 属性{0}和{1}之间没有定义关系"),
    RELATIONSHIPDEF_INVALID(400, "METASPACE-400-00-044", " 无效的relationshipDef:{0}"),
    RELATIONSHIP_INVALID_ENDTYPE(400, "METASPACE-400-00-045", " 关系属性'{0}'的实体类型无效:指定的实体(guid={1})类型为'{2}',但预期类型为' {3}'"),
    UNKNOWN_CLASSIFICATION(400, "METASPACE-400-00-046", " {0}:未知/无效的分类"),
    INVALID_SEARCH_PARAMS(400, "METASPACE-400-00-047", " 没有找到搜索参数,下列其中一项必须在请求中指定;typeName, classification, termName or queryText"),
    INVALID_RELATIONSHIP_ATTRIBUTE(400, "METASPACE-400-00-048", " 期望属性{0}是一个关系,但发现类型{1}"),
    INVALID_RELATIONSHIP_TYPE(400, "METASPACE-400-00-049", " 无效实体类型'{0}',guid '{1}'在关系搜索"),
    INVALID_IMPORT_ATTRIBUTE_TYPE_CHANGED(400, "METASPACE-400-00-050", " {0}属性,{1}是{2}型,导入属性类型为{3}"),
    ENTITYTYPE_REMOVAL_NOT_SUPPORTED(400, "METASPACE-400-00-051", " 实体类型不能从ClassificationDef '{0} '中删除"),
    CLASSIFICATIONDEF_INVALID_ENTITYTYPES(400, "METASPACE-400-00-052", " 在实体类型中,ClassificationDef '{0} '具有无效的' {1}'"),
    CLASSIFICATIONDEF_PARENTS_ENTITYTYPES_DISJOINT(400, "METASPACE-400-00-053", " 分类定义'{0}'有实体类型不相交的超类型;例如,两个没有继承关系的超类型指定了不同的非空实体类型列表,这意味着孩子不能遵守父母双方规定的限制"),
    CLASSIFICATIONDEF_ENTITYTYPES_NOT_PARENTS_SUBSET(400, "METASPACE-400-00-054", " ClassificationDef '{0} '有实体类型'{1}'这不是它的超类型实体类型的子集"),
    INVALID_ENTITY_FOR_CLASSIFICATION(400, "METASPACE-400-00-055", " 实体(guid= ' {0} ',typename= '{1} ')不能通过分类'{2}'进行分类,因为'{1}'不在ClassificationDef的限制中"),
    SAVED_SEARCH_CHANGE_USER(400, "METASPACE-400-00-056", " 保存搜索{0}不能从用户{1}移动到{2}"),
    INVALID_QUERY_PARAM_LENGTH(400, "METASPACE-400-00-057", " 查询参数{0}的长度超过限制"),
    INVALID_QUERY_LENGTH(400, "METASPACE-400-00-058", " 查询长度无效,更新{0}以更改限制"),

    // DSL related error codes
    INVALID_DSL_QUERY(400, "METASPACE-400-00-059", " 无效的DSL查询:{0}|原因:{1},更多信息请参考METASPACE DSL语法"),
    INVALID_DSL_GROUPBY(400, "METASPACE-400-00-05A", " DSL语义错误——GroupBy属性{0}是非原始的"),
    INVALID_DSL_UNKNOWN_TYPE(400, "METASPACE-400-00-05B", " DSL语义错误-没有找到{0}类型"),
    INVALID_DSL_UNKNOWN_CLASSIFICATION(400, "METASPACE-400-00-05C", " DSL语义错误-没有找到{0}分类"),
    INVALID_DSL_UNKNOWN_ATTR_TYPE(400, "METASPACE-400-00-05D", " DSL语义错误-类型{1}没有找到{0}属性"),
    INVALID_DSL_ORDERBY(400, "METASPACE-400-00-05E", " DSL语义错误——OrderBy属性{0}是非原始的"),
    INVALID_DSL_FROM(400, "METASPACE-400-00-05F", " DSL语义错误——来源{0}不是一个有效的实体/分类类型"),
    INVALID_DSL_SELECT_REFERRED_ATTR(400, "METASPACE-400-00-060", " DSL语义错误- Select子句有多个引用属性{0}"),
    INVALID_DSL_SELECT_INVALID_AGG(400, "METASPACE-400-00-061", " DSL语义错误- Select子句对引用属性{0}具有聚合"),
    INVALID_DSL_SELECT_ATTR_MIXING(400, "METASPACE-400-00-062", " DSL语义错误- Select子句具有简单且引用的属性"),
    INVALID_DSL_HAS_ATTRIBUTE(400, "METASPACE-400-00-063", " DSL语义错误——类型{1}不存在{0}属性"),
    INVALID_DSL_QUALIFIED_NAME(400, "METASPACE-400-00-064", " DSL语义错误——{0}的限定名失败!"),
    INVALID_DSL_QUALIFIED_NAME2(400, "METASPACE-400-00-065", " DSL语义错误——类型{1}的{0}的限定名称失败,原因:{2}"),
    INVALID_DSL_DUPLICATE_ALIAS(400, "METASPACE-400-00-066", " DSL语义错误-找到的重复别名:'{0}'类型'{1}'已经存在"),
    INVALID_DSL_INVALID_DATE(400, "METASPACE-400-00-067", " DSL语义错误-日期格式:{0}."),
    INVALID_DSL_HAS_PROPERTY(400, "METASPACE-400-00-068", " DSL语义错误-属性需要是一个基本类型:{0}"),
    RELATIONSHIP_UPDATE_END_CHANGE_NOT_ALLOWED(404, "METASPACE-400-00-069", " 不允许更改关系结束,关系类型={},关系guid={},结束guid={},更新结束guid={}"),
    RELATIONSHIP_UPDATE_TYPE_CHANGE_NOT_ALLOWED(404, "METASPACE-400-00-06A", " 不允许更改关系类型,relationship-guid = { },电流式= { },新型= { } "),
    CLASSIFICATION_UPDATE_FROM_PROPAGATED_ENTITY(400, "METASPACE-400-00-06B", " 传播实体不允许更新分类{0}"),
    CLASSIFICATION_DELETE_FROM_PROPAGATED_ENTITY(400, "METASPACE-400-00-06C", " 从传播的实体中不允许删除分类{0}"),
    CLASSIFICATION_NOT_ASSOCIATED_WITH_ENTITY(400, "METASPACE-400-00-06D", " 分类{0}与实体无关"),
    UNKNOWN_GLOSSARY_TERM(400, "METASPACE-400-00-06E", " {0}:未知/无效术语表术语"),
    INVALID_CLASSIFICATION_PARAMS(400, "METASPACE-400-00-06F", " 实体{0}操作传递的分类参数无效:{1}"),
    PROPAGATED_CLASSIFICATION_NOT_ASSOCIATED_WITH_ENTITY(400, "METASPACE-400-00-070", " 传播分类{0}与实体{2}无关,与实体{1}相关"),
    INVALID_BLOCKED_PROPAGATED_CLASSIFICATION(400, "METASPACE-400-00-071", " 无效的传播分类:{0}与entityGuid:{1}添加到阻塞传播分类"),
    MISSING_MANDATORY_ANCHOR(400, "METASPACE-400-00-072", " 强制锚属性缺失"),
    MISSING_MANDATORY_QUALIFIED_NAME(400, "METASPACE-400-00-073", " 缺少Mandatory qualifiedName属性"),
    INVALID_PARTIAL_UPDATE_ATTR(400, "METASPACE-400-00-074", " 无效属性{0}部分更新{1}"),
    INVALID_PARTIAL_UPDATE_ATTR_VAL(400, "METASPACE-400-00-075", " 部分更新{0}无效attrVal,期望={1}找到{2}"),
    MISSING_TERM_ID_FOR_CATEGORIZATION(400, "METASPACE-400-00-076", " 术语guid在添加到类别时不能为空/空"),
    INVALID_NEW_ANCHOR_GUID(400, "METASPACE-400-00-077", " 新锚guid不能为空/null"),
    TERM_DISSOCIATION_MISSING_RELATION_GUID(400, "METASPACE-400-00-078", " 缺失的强制性属性,termas关系guid"),
    GLOSSARY_QUALIFIED_NAME_CANT_BE_DERIVED(400, "METASPACE-400-00-079", " 属性qualifiedName和name丢失,未能获得术语表的唯一名称"),
    GLOSSARY_TERM_QUALIFIED_NAME_CANT_BE_DERIVED(400, "METASPACE-400-00-07A", " 属性qualifiedName, name和glossary名称都不存在,未能为术语表术语派生唯一名称"),
    GLOSSARY_CATEGORY_QUALIFIED_NAME_CANT_BE_DERIVED(400, "METASPACE-400-00-07B", " 属性qualifiedName, name和glossary名称都不存在,未能为术语表类别派生唯一名称"),
    RELATIONSHIP_END_IS_NULL(400, "METASPACE-400-00-07D", " 关系结束无效,期望{0},但为空"),
    INVALID_TERM_RELATION_TO_SELF(400, "METASPACE-400-00-07E", " 无效的术语关系:术语不能与自我有关系"),
    INVALID_CHILD_CATEGORY_DIFFERENT_GLOSSARY(400, "METASPACE-400-00-07F", " 无效的子类别关系:子类别(guid ={0})属于不同的术语表"),
    INVALID_TERM_DISSOCIATION(400, "METASPACE-400-00-080", " 考虑到relationshipGuid({0})对于term (guid={1})和entity(guid={2})是无效的"),
    ATTRIBUTE_TYPE_INVALID(400, "METASPACE-400-00-081", " {0},{1}:无效的属性类型,属性不能是类型分类"),
    MISSING_CATEGORY_DISPLAY_NAME(400, "METASPACE-400-00-082", " 类别名称为空/空"),
    INVALID_DISPLAY_NAME(400, "METASPACE-400-00-083", " 名称不能包含以下特殊字符('@','.')"),
    TERM_HAS_ENTITY_ASSOCIATION(400, "METASPACE-400-00-086", " 术语(guid={0})不能删除,因为它已经被分配给{1}实体"),
    INVALID_TIMEBOUNDRY_TIMEZONE(400, "METASPACE-400-00-87A", " 无效的时区{0}"),
    INVALID_TIMEBOUNDRY_START_TIME(400, "METASPACE-400-00-87B", " 无效的开始时间{0}"),
    INVALID_TIMEBOUNDRY_END_TIME(400, "METASPACE-400-00-87C", " 无效的endTime {0}"),
    INVALID_TIMEBOUNDRY_DATERANGE(400, "METASPACE-400-00-87D", " 无效的dateRange: startTime{0}必须在endTime{1}之前"),
    PROPAGATED_CLASSIFICATION_REMOVAL_NOT_SUPPORTED(400, "METASPACE-400-00-87E", " 不支持删除从实体{1}传播的分类{0}"),

    INVALID_PARAMS(400, "METASPACE-400-00-88", " {0}"),

    UNAUTHORIZED_ACCESS(403, "METASPACE-403-00-001", " {0}未被授权执行{1}"),

    // All Not found enums go here
    TYPE_NAME_NOT_FOUND(404, "METASPACE-404-00-001", " 假设typename{0}无效"),
    TYPE_GUID_NOT_FOUND(404, "METASPACE-404-00-002", " 给定类型guid{0}无效"),
    NO_CLASSIFICATIONS_FOUND_FOR_ENTITY(404, "METASPACE-404-00-003", " 没有与实体关联的分类:{0}"),
    EMPTY_RESULTS(404, "METASPACE-404-00-004", " 没有发现{0}的结果"),
    INSTANCE_GUID_NOT_FOUND(404, "METASPACE-404-00-005", " 给定实例guid{0}无效/未找到"),
    INSTANCE_LINEAGE_QUERY_FAILED(404, "METASPACE-404-00-006", " 实例沿袭查询失败{0}"),
    INSTANCE_CRUD_INVALID_PARAMS(404, "METASPACE-404-00-007", " 无效的实例创建/updation参数传递:{0}"),
    CLASSIFICATION_NOT_FOUND(404, "METASPACE-404-00-008", " 假设分类{0}无效"),
    INSTANCE_BY_UNIQUE_ATTRIBUTE_NOT_FOUND(404, "METASPACE-404-00-009", " 具有唯一属性{1}的实例{0}不存在"),
    REFERENCED_ENTITY_NOT_FOUND(404, "METASPACE-404-00-00A", " 没有找到引用的实体{0}"),
    INSTANCE_NOT_FOUND(404, "ATLAS-404-00-00B", " 给定实例无效/未找到:{0}"),
    RELATIONSHIP_GUID_NOT_FOUND(404, "METASPACE-404-00-00C", " 给定关系guid{0}无效/未找到"),
    RELATIONSHIP_CRUD_INVALID_PARAMS(404, "METASPACE-404-00-00D", " 无效的关系创建/updation参数传递:{0}"),
    RELATIONSHIPDEF_END_TYPE_NAME_NOT_FOUND(404, "METASPACE-404-00-00E", " 无法找到RelationshipDef {0} endDef typename{0}"),
    RELATIONSHIP_ALREADY_DELETED(404, "METASPACE-404-00-00F", " 试图删除已删除的关系:{0}"),
    INVALID_ENTITY_GUID_FOR_CLASSIFICATION_UPDATE(404, "METASPACE-404-00-010", " 不允许更新分类的实体id"),
    INSTANCE_GUID_NOT_DATASET(404, "METASPACE-404-00-011", " 给定实例guid{0}不是数据集"),
    INSTANCE_GUID_DELETED(404, "METASPACE-404-00-012", " 给定实例guid{0}已被删除"),
    NO_PROPAGATED_CLASSIFICATIONS_FOUND_FOR_ENTITY(404, "METASPACE-404-00-013", " 没有与实体关联的传播分类:{0}"),

    // All data conflict errors go here
    TYPE_ALREADY_EXISTS(409, "METASPACE-409-00-001", " 给定类型{0}已经存在"),
    TYPE_HAS_REFERENCES(409, "METASPACE-409-00-002", " 给定类型{0}有引用"),
    INSTANCE_ALREADY_EXISTS(409, "METASPACE-409-00-003", " 更新实体失败:{0}"),
    RELATIONSHIP_ALREADY_EXISTS(409, "METASPACE-409-00-004", " {0}在实体{1}和{2}之间已经存在关系"),
    TYPE_HAS_RELATIONSHIPS(409, "METASPACE-409-00-005", " 给定类型{0}具有关联关系"),
    SAVED_SEARCH_ALREADY_EXISTS(409, "METASPACE-409-00-006", " 为用户{1}已经存在名为{0}的搜索"),
    GLOSSARY_ALREADY_EXISTS(409, "METASPACE-409-00-007", " 有资格名称{0}的术语表已经存在"),
    GLOSSARY_TERM_ALREADY_EXISTS(409, "METASPACE-409-00-009", " 术语表术语与限定名称{0}已经存在"),
    GLOSSARY_CATEGORY_ALREADY_EXISTS(409, "METASPACE-409-00-00A", " 术语表类别和限定名称{0}已经存在"),

    // All internal errors go here
    INTERNAL_UNKNOWN_ERROR(500, "METASPACE-500-00-000", " 服务器内部错误"),
    INTERNAL_ERROR(500, "METASPACE-500-00-001", " 内部服务器错误{0}"),
    INDEX_CREATION_FAILED(500, "METASPACE-500-00-002", " {0}索引创建失败"),
    INDEX_ROLLBACK_FAILED(500, "METASPACE-500-00-003", " {0}的索引回滚失败"),
    DISCOVERY_QUERY_FAILED(500, "METASPACE-500-00-004", " 发现查询失败{0}"),
    FAILED_TO_OBTAIN_TYPE_UPDATE_LOCK(500, "METASPACE-500-00-005", " 没拿到锁;另一种类型的更新可能正在进行中,请再试一次"),
    FAILED_TO_OBTAIN_IMPORT_EXPORT_LOCK(500, "METASPACE-500-00-006", " 另一项进口或出口正在进行中,请再试一次"),
    NOTIFICATION_FAILED(500, "METASPACE-500-00-007", " 未通知{0}更改{1}"),
    FAILED_TO_OBTAIN_GREMLIN_SCRIPT_ENGINE(500, "METASPACE-500-00-008", " 未能获得gremlin脚本引擎:{0}"),
    JSON_ERROR_OBJECT_MAPPER_NULL_RETURNED(500, "METASPACE-500-00-009", " objectmap,类返回空值:{0}"),
    GREMLIN_SCRIPT_EXECUTION_FAILED(500, "METASPACE-500-00-00A", " 脚本执行失败:{0}"),
    CURATOR_FRAMEWORK_UPDATE(500, "METASPACE-500-00-00B", " ActiveInstanceState,更新导致异常"),
    QUICK_START(500, "METASPACE-500-00-00C", " 未能运行QuickStart: {0}"),
    EMBEDDED_SERVER_START(500, "METASPACE-500-00-00D", " EmbeddedServer,开始:失败了!"),
    STORM_TOPOLOGY_UTIL(500, "METASPACE-500-00-00E", " StormTopologyUtil:{0}"),
    SQOOP_HOOK(500, "METASPACE-500-00-00F", " SqoopHook:{0}"),
    HIVE_HOOK(500, "METASPACE-500-00-010", " HiveHook:{0}"),
    HIVE_HOOK_METASTORE_BRIDGE(500, "METASPACE-500-00-011", " HiveHookMetaStoreBridge:{0}"),
    DATA_ACCESS_LOAD_FAILED(500, "METASPACE-500-00-013", " 加载失败:{0}"),
    ENTITY_NOTIFICATION_FAILED(500, "METASPACE-500-00-014", " 操作通知失败:{0}:{1}"),
    CONF_LOAD_ERROE(506, "METASPACE-506-00-001", " 服务器配置错误: {0}");

    private String errorCode;
    private String errorMessage;
    private Response.Status httpCode;

    private static final Logger LOG = LoggerFactory.getLogger(AtlasErrorCode.class);

    AtlasErrorCode(int httpCode, String errorCode, String errorMessage) {
        this.httpCode = Response.Status.fromStatusCode(httpCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

    }

    public String getFormattedErrorMessage(String... params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("<== AtlasErrorCode.getMessage(%s)", Arrays.toString(params)));
        }

        MessageFormat mf = new MessageFormat(errorMessage);
        String result = mf.format(params);

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("==> AtlasErrorCode.getMessage(%s): %s", Arrays.toString(params), result));
        }
        return result;
    }

    public Response.Status getHttpCode() {
        return httpCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
