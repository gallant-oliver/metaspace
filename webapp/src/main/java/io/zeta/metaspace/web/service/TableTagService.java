package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.web.dao.TableTagDAO;
import io.zeta.metaspace.web.filter.SSOFilter;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
@AtlasService
public class TableTagService {
    private static final Logger LOG = LoggerFactory.getLogger(TableTagService.class);
@Autowired
    TableTagDAO tableTagDAO;
    public String addTag(String tagName,String tenantId) throws AtlasBaseException {
        if(tableTagDAO.ifTagExists(tagName,tenantId)==0) {
            String tagId = UUID.randomUUID().toString();
            tableTagDAO.addTag(tagId, tagName,tenantId);
            return tagId;
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"该标签已存在");
        }
    }
    public void deleteTag(String tagId) {
     tableTagDAO.deleteTag(tagId);
    }

    public List<Tag> getTags(String query,long offset,long limit,String tenantId) {
        List<Tag> tags=null;
        if(Objects.nonNull(query))
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
        if(limit==-1)
            tags = tableTagDAO.getTag(query, offset,tenantId);
            else
            tags = tableTagDAO.getTags(query, offset, limit,tenantId);
        return tags;
    }
    @Transactional
    public void addTable2Tag(Table table, List<String> tagId){
        tableTagDAO.delAllTable2Tag(table.getTableId());
        for (String s : tagId) {
            try {
                tableTagDAO.addTable2Tag(s, table.getTableId());
            }catch (Exception e){
                LOG.error("table "+table.getTableName()+" 添加tag "+s+" 失败,错误信息:"+e.getMessage(),e);
            }
        }

    }
    public void deleteTable2Tag(String tableGuid,String tagId){
        tableTagDAO.deleteTable2Tag(tableGuid,tagId);
    }

}
