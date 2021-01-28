package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.web.dao.TableTagDAO;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@AtlasService
public class TableTagService {
    private static final Logger LOG = LoggerFactory.getLogger(TableTagService.class);
    @Autowired
    TableTagDAO tableTagDAO;

    @Transactional(rollbackFor = Exception.class)
    public void addTag(String guid, String tagName, String tenantId) throws AtlasBaseException {
        String tagId = UUID.randomUUID().toString();
        Tag tag = tableTagDAO.getTag(tagName, tenantId);
        if (tag == null) {
            tableTagDAO.addTag(tagId, tagName, tenantId);
        }else {
            tagId = tag.getTagId();
        }
        if(tableTagDAO.ifTagExists(tagId,guid)>0){
            throw new AtlasBaseException("标签不能重复添加");
        }
        tableTagDAO.addTable2Tag(tagId, guid);
    }

    public void deleteTag(String guid, String tagId) {
        tableTagDAO.deleteTable2Tag(guid, tagId);
        if (tableTagDAO.getTagUseCount(tagId) == 0) {
            tableTagDAO.deleteTag(tagId);
        }
    }

    public List<Tag> getTags(String guid,String tenantId) {
        return tableTagDAO.getTable2Tag(guid,tenantId);
    }

}
