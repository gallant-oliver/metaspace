package io.zeta.metaspace.web.service;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import io.zeta.metaspace.model.table.column.tag.ColumnTagRelation;
import io.zeta.metaspace.utils.StringUtil;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ColumnTagService {

    private static final Logger LOG = LoggerFactory.getLogger(ColumnTagService.class);

    @Autowired
    private ColumnTagDAO columnTagDAO;

    public Result getColumnTag(String tenantId, String columnId) {

        return ReturnUtil.success((columnId==null||columnId.isEmpty())?
                columnTagDAO.getTagList(tenantId):
                columnTagDAO.getTagListByColumnId(tenantId,columnId));
    }

    public Result createTag(String tenantId, String tagName) {
        columnTagDAO.insertColumnTag(this.buildColumnTag(tagName,tenantId));
        return ReturnUtil.success();
    }

    private ColumnTag buildColumnTag(String tagName,String tenantId){
        ColumnTag columnTag = new ColumnTag();

        columnTag.setId(UUID.randomUUID().toString());
        columnTag.setName(tagName);
        columnTag.setTenantId(tenantId);

        return columnTag;
    }

    public Result createTagRelationToColumn(String tenantId, String columnId, List<String> tagList) {
        Map<String,ColumnTag> columnTagMap = columnTagDAO.getTagListByColumnId(tenantId,columnId).stream().collect(Collectors.toMap(ColumnTag::getId, Function.identity(),(key1,key2)->key2));
        List<ColumnTagRelation> addRelationList = new ArrayList<>();
        tagList.forEach(tagId->{
            if (!columnTagMap.containsKey(tagId)){
                ColumnTagRelation columnTagRelation = new ColumnTagRelation();
                columnTagRelation.setColumnId(columnId);
                columnTagRelation.setTagId(tagId);
                columnTagRelation.setId(UUID.randomUUID().toString());
                addRelationList.add(columnTagRelation);
            }
        });
        columnTagDAO.addTagRelationsToColumn(addRelationList);
        return ReturnUtil.success();
    }

    public Result deleteTagRelationToColumn(String tenantId,String columnId,String tagId){
        columnTagDAO.deleteRelation(tenantId,columnId,tagId);
        return ReturnUtil.success();
    }

    public Result checkDuplicateNameTag(String tenantId,String tagName){
        return ReturnUtil.success(columnTagDAO.getTagByTagName(tenantId,tagName)>0);
    }

}
