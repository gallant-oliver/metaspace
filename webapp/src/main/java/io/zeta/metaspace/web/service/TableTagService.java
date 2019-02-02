package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.web.dao.TableTagDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
@AtlasService
public class TableTagService {
@Autowired
    TableTagDAO tableTagDAO;
    public void addTag(String tagName) throws AtlasBaseException {
        if(tableTagDAO.ifTagExists(tagName).size()==0) {
            String tagId = UUID.randomUUID().toString();
            tableTagDAO.addTag(tagId, tagName);
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"该标签已存在");
        }
    }
    public void deleteTag(String tagId) throws AtlasBaseException {
     tableTagDAO.deleteTag(tagId);
    }

    public List<Tag> getTags(String query,long offset,long limit) {
        List<Tag> tags = tableTagDAO.getTags(query, offset, limit);
        return tags;
    }
    @Transactional
    public void addTable2Tag(Table table, String tagId){
        if (tableTagDAO.ifTableExists(table.getTableId()).size()==0) {
            tableTagDAO.addTable(table);
        }
        tableTagDAO.addTable2Tag(tagId,table.getTableId());
    }
    public void deleteTable2Tag(String tableGuid,String tagId){
        tableTagDAO.deleteTable2Tag(tableGuid,tagId);
    }

}
