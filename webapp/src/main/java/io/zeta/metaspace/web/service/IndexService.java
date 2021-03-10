package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.indices.IndexFieldDTO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.BeanMapper;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

@Service
public class IndexService {
    private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private DataManageService dataManageService;

    @Autowired
    private UserDAO userDAO;

    public IndexFieldDTO getIndexFieldInfo(String categoryId, String tenantId, int categoryType) throws SQLException {
        CategoryEntityV2 category = dataManageService.getCategory(categoryId, tenantId);
        if(category!=null){
            IndexFieldDTO indexFieldDTO= BeanMapper.map(category, IndexFieldDTO.class);
            String creatorId=category.getCreator();
            String updaterId=category.getUpdater();
            Timestamp createTime=category.getCreateTime();
            Timestamp updateTime=category.getUpdateTime();
            if(StringUtils.isNotEmpty(creatorId)){
                indexFieldDTO.setCreator(userDAO.getUserName(creatorId));
            }
            if(StringUtils.isNotEmpty(updaterId)){
                indexFieldDTO.setUpdater(userDAO.getUserName(updaterId));
            }
            if(!Objects.isNull(createTime)){
                indexFieldDTO.setCreateTime(DateUtils.timestampToString(createTime));
            }
            if(!Objects.isNull(updateTime)){
                indexFieldDTO.setUpdateTime(DateUtils.timestampToString(updateTime));
            }
            return indexFieldDTO;
        }else {
            return null;
        }
    }


}
