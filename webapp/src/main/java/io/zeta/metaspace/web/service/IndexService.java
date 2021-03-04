package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.indices.IndexResposeDTO;
import io.zeta.metaspace.model.dto.indices.IndexDTO;
import io.zeta.metaspace.model.dto.indices.IndexFieldDTO;
import io.zeta.metaspace.model.enums.IndexType;
import io.zeta.metaspace.model.po.indices.*;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.IndexDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeanMapper;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class IndexService {
    private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private DataManageService dataManageService;

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private IndexDAO indexDAO;

    public IndexFieldDTO getIndexFieldInfo(String categoryId, String tenantId, int categoryType) throws SQLException {
        CategoryEntityV2 category = dataManageService.getCategory(categoryId, tenantId);
        if(category!=null){
            IndexFieldDTO indexFieldDTO= BeanMapper.map(category, IndexFieldDTO.class);
            String creatorId=indexFieldDTO.getCreator();
            String updaterId=indexFieldDTO.getUpdater();
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

    /**
     * 添加指标
     * @param indexDTO
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public IndexResposeDTO addIndex(IndexDTO indexDTO, String tenantId) throws Exception {
        int indexType=indexDTO.getIndexType();
        User user= AdminUtils.getUserData();
        Timestamp timestamp=new Timestamp(System.currentTimeMillis());
        IndexResposeDTO iard=null;
        if(indexType == IndexType.INDEXATOMIC.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getAtomicIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexAtomicPO iap=BeanMapper.map(indexDTO,IndexAtomicPO.class);
            iap.setIndexId(UUID.randomUUID().toString());
            iap.setTenantId(tenantId);
            iap.setIndexState(1);
            iap.setVersion(0);
            iap.setCreator(user.getUserId());
            iap.setCreateTime(timestamp);
            iap.setUpdateTime(timestamp);
            indexDAO.addAtomicIndex(iap);
            iard=BeanMapper.map(iap, IndexResposeDTO.class);
        }else if(indexType == IndexType.INDEXDERIVE.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getDeriveIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexDerivePO idp=BeanMapper.map(indexDTO,IndexDerivePO.class);
            idp.setIndexId(UUID.randomUUID().toString());
            idp.setTenantId(tenantId);
            idp.setIndexState(1);
            idp.setVersion(0);
            idp.setCreator(user.getUserId());
            idp.setCreateTime(timestamp);
            idp.setUpdateTime(timestamp);
            idp.setIndexAtomicId(indexDTO.getDependentIndices().get(0));
            List<String> modifiers = indexDTO.getModifiers();
            indexDAO.addDeriveIndex(idp);
            addDeriveModifierRelations(idp.getIndexId(),modifiers);
            iard=BeanMapper.map(idp, IndexResposeDTO.class);
        }else if(indexType == IndexType.INDEXCOMPOSITE.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getCompositeIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexCompositePO icp=BeanMapper.map(indexDTO,IndexCompositePO.class);
            icp.setIndexId(UUID.randomUUID().toString());
            icp.setTenantId(tenantId);
            icp.setIndexState(1);
            icp.setVersion(0);
            icp.setCreator(user.getUserId());
            icp.setCreateTime(timestamp);
            icp.setUpdateTime(timestamp);
            indexDAO.addCompositeIndex(icp);
            List<String> deriveIds=indexDTO.getDependentIndices();
            addDeriveCompositeRelations(icp.getIndexId(),deriveIds);
            iard=BeanMapper.map(icp, IndexResposeDTO.class);
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域类型错误");
        }
        return iard;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDeriveCompositeRelations(String indexId, List<String> deriveIds) throws SQLException {
        List<IndexDeriveCompositeRelationPO> idcrPOS=getDeriveCompositeRelationPOS(indexId,deriveIds);
        if(!CollectionUtils.isEmpty(idcrPOS)){
            indexDAO.addDeriveCompositeRelations(idcrPOS);
        }
    }
    /**
     * 组装复合指标与派生指标关系
     * @param indexId
     * @return
     */
    private List<IndexDeriveCompositeRelationPO> getDeriveCompositeRelationPOS(String indexId, List<String> deriveIds) {
        List<IndexDeriveCompositeRelationPO> idcrPOS=null;
        if(!CollectionUtils.isEmpty(idcrPOS)){
            idcrPOS=new ArrayList<>();
            for(String deriveId:deriveIds){
                IndexDeriveCompositeRelationPO idcr=new IndexDeriveCompositeRelationPO();
                idcr.setCompositeIndexId(indexId);
                idcr.setDeriveIndexId(deriveId);
                idcrPOS.add(idcr);
            }
        }
        return idcrPOS;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDeriveModifierRelations(String indexId, List<String> modifiers) throws Exception{
        List<IndexDeriveModifierRelationPO> idmrPOS=getDeriveModifierRelationPOS(indexId,modifiers);
        if(!CollectionUtils.isEmpty(idmrPOS)){
            indexDAO.addDeriveModifierRelations(idmrPOS);
        }
    }

    /**
     * 组装派生指标与修饰词关系
     * @param indexId
     * @param modifiers
     * @return
     */
    private List<IndexDeriveModifierRelationPO> getDeriveModifierRelationPOS(String indexId, List<String> modifiers) {
        List<IndexDeriveModifierRelationPO> idmrPOS=null;
        if(!CollectionUtils.isEmpty(modifiers)){
            idmrPOS=new ArrayList<>();
            for(String modifierId:modifiers){
                IndexDeriveModifierRelationPO idmrPO=new IndexDeriveModifierRelationPO();
                idmrPO.setDeriveIndexId(indexId);
                idmrPO.setModifierId(modifierId);
                idmrPOS.add(idmrPO);
            }
        }
        return idmrPOS;
    }

    @Transactional(rollbackFor = Exception.class)
    public IndexResposeDTO editIndex(IndexDTO indexDTO, String tenantId) {
        int indexType=indexDTO.getIndexType();
        User user= AdminUtils.getUserData();
        Timestamp timestamp=new Timestamp(System.currentTimeMillis());
        IndexResposeDTO iard=null;
        if(indexType == IndexType.INDEXATOMIC.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getAtomicIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
        }else if(indexType == IndexType.INDEXDERIVE.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getDeriveIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
        }else if(indexType == IndexType.INDEXCOMPOSITE.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getCompositeIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
        }else{
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域类型错误");
        }
        return null;
    }
}
