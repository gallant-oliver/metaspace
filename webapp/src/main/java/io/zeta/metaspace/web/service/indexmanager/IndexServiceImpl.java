package io.zeta.metaspace.web.service.indexmanager;

import io.zeta.metaspace.model.datasource.DataSourceBody;
import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.model.enums.IndexType;
import io.zeta.metaspace.model.po.indices.*;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.TenantService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service("indexService")
public class IndexServiceImpl implements IndexService{
    private static final Logger LOG = LoggerFactory.getLogger(IndexServiceImpl.class);

    @Autowired
    private DataManageService dataManageService;

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private IndexDAO indexDAO;
    @Autowired
    private UserGroupDAO userGroupDAO;
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private DataSourceDAO dataSourceDAO;
    @Autowired
    private TableDAO tableDAO;
    @Autowired
    private TenantService tenantService;

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
            addDeriveModifierRelations(idp,modifiers);
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
            List<String> deriveIds=indexDTO.getDependentIndices();
            addDeriveCompositeRelations(icp,deriveIds);
            iard=BeanMapper.map(icp, IndexResposeDTO.class);
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        return iard;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDeriveCompositeRelations(IndexCompositePO icp, List<String> deriveIds) throws SQLException {
        String indexId=icp.getIndexId();
        indexDAO.addCompositeIndex(icp);
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
    public void addDeriveModifierRelations(IndexDerivePO idp, List<String> modifiers) throws Exception{
        String indexId=idp.getIndexId();
        indexDAO.addDeriveIndex(idp);
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
    public IndexResposeDTO editIndex(IndexDTO indexDTO, String tenantId) throws SQLException {
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
            iap.setUpdater(user.getUserId());
            iap.setUpdateTime(timestamp);
            indexDAO.editAtomicIndex(iap);
            iard=BeanMapper.map(iap, IndexResposeDTO.class);
        }else if(indexType == IndexType.INDEXDERIVE.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getDeriveIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexDerivePO idp=BeanMapper.map(indexDTO,IndexDerivePO.class);
            idp.setUpdater(user.getUserId());
            idp.setUpdateTime(timestamp);
            //获取已经存在的派生指标与修饰词关系
            List<IndexDeriveModifierRelationPO> modifierRelations=indexDAO.getDeriveModifierRelations(idp.getIndexId());
            editDerivIndex(idp,indexDTO.getModifiers(),modifierRelations);
            iard=BeanMapper.map(idp, IndexResposeDTO.class);
        }else if(indexType == IndexType.INDEXCOMPOSITE.getValue()){
            //名称和标识重名校验
            IndexAtomicPO exits=indexDAO.getCompositeIndexByNameOrIdentification(tenantId,indexDTO);
            if(!Objects.isNull(exits)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexCompositePO icp=BeanMapper.map(indexDTO,IndexCompositePO.class);
            icp.setUpdater(user.getUserId());
            icp.setUpdateTime(timestamp);
            List<IndexDeriveCompositeRelationPO> compositeRelations=indexDAO.getDeriveCompositeRelations(icp.getIndexId());
            editCompositeIndex(icp,indexDTO.getDependentIndices(),compositeRelations);
            iard=BeanMapper.map(icp, IndexResposeDTO.class);
        }else{
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        return iard;
    }

    @Transactional(rollbackFor = Exception.class)
    public void editDerivIndex(IndexDerivePO idp, List<String> modifiers, List<IndexDeriveModifierRelationPO> modifierRelations) throws SQLException {
        //1.编辑派生指标
        indexDAO.editDerivIndex(idp);
        //2.编辑派生指标与修饰词关系
        if(CollectionUtils.isEmpty(modifierRelations)){
            if(!CollectionUtils.isEmpty(modifiers)){
                //增加派生指标与修饰词关系
                List<IndexDeriveModifierRelationPO> idmrPOS = getDeriveModifierRelationPOS(idp.getIndexId(), modifiers);
                indexDAO.addDeriveModifierRelations(idmrPOS);
            }
        }else{
            if(!CollectionUtils.isEmpty(modifiers)){
                List<String> exits=modifierRelations.stream().map(x->x.getModifierId()).distinct().collect(Collectors.toList());
                //增加的派生指标与修饰词关系
                List<String> adds = modifiers.stream().filter(x -> !exits.contains(x)).distinct().collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(adds)){
                    List<IndexDeriveModifierRelationPO> addPOS = getDeriveModifierRelationPOS(idp.getIndexId(), adds);
                    indexDAO.addDeriveModifierRelations(addPOS);
                }
                //删除的派生指标与修饰词关系
                List<String> dels = exits.stream().filter(x -> !modifiers.contains(x)).distinct().collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(dels)){
                    indexDAO.deleteDeriveModifierRelationsByDeriveModifierId(idp.getIndexId(),dels);
                }
            }else{
                //删除已存在的派生指标与修饰词关系
                indexDAO.deleteDeriveModifierRelationsByDeriveId(idp.getIndexId());
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void editCompositeIndex(IndexCompositePO icp, List<String> dependentIndices, List<IndexDeriveCompositeRelationPO> compositeRelations) throws SQLException {
        //1.编辑复合指标
        indexDAO.editCompositeIndex(icp);
        //2.编辑复合指标与派生指标关系
        if(CollectionUtils.isEmpty(compositeRelations)){
            if(!CollectionUtils.isEmpty(dependentIndices)){
                //增加派生指标与修饰词关系
                List<IndexDeriveCompositeRelationPO> idcrPOS = getDeriveCompositeRelationPOS(icp.getIndexId(), dependentIndices);
                indexDAO.addDeriveCompositeRelations(idcrPOS);
            }
        }else{
            if(!CollectionUtils.isEmpty(dependentIndices)){
                List<String> exits=compositeRelations.stream().map(x->x.getCompositeIndexId()).distinct().collect(Collectors.toList());
                //增加的派生指标与修饰词关系
                List<String> adds = dependentIndices.stream().filter(x -> !exits.contains(x)).distinct().collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(adds)){
                    List<IndexDeriveCompositeRelationPO> addPOS = getDeriveCompositeRelationPOS(icp.getIndexId(), adds);
                    indexDAO.addDeriveCompositeRelations(addPOS);
                }
                //删除的派生指标与修饰词关系
                List<String> dels = exits.stream().filter(x -> !dependentIndices.contains(x)).distinct().collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(dels)){
                    indexDAO.deleteDeriveCompositeRelationsByDeriveCompositeId(icp.getIndexId(),dels);
                }
            }else{
                //删除已存在的派生指标与修饰词关系
                indexDAO.deleteDeriveCompositeRelationsByDeriveId(icp.getIndexId());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteIndex(DeleteRequestDTO<DeleteIndexInfoDTO> deleteList, String tenantId) {
        if(!CollectionUtils.isEmpty((Collection<DeleteIndexInfoDTO>) deleteList)){
            Map<Integer,List<String>> deleteMap=new HashMap<>();
            ((Collection<DeleteIndexInfoDTO>) deleteList).forEach(x->{
                List<String> deleteIds = deleteMap.get(x.getIndexType());
                if(CollectionUtils.isEmpty(deleteIds)){
                    deleteIds=new ArrayList<>();
                    deleteMap.put(x.getIndexType(),deleteIds);
                }
                deleteIds.add(x.getIndexId());
            });
            deleteIndexMap(deleteMap);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndexMap(Map<Integer, List<String>> deleteMap) {
        if(!CollectionUtils.isEmpty(deleteMap)){
            deleteMap.forEach((k,v)->{
                if(k==IndexType.INDEXATOMIC.getValue()){
                    indexDAO.deleteAtomicIndices(v);
                }else if(k==IndexType.INDEXDERIVE.getValue()){
                    indexDAO.deleteDeriveIndices(v);
                }else if(k==IndexType.INDEXCOMPOSITE.getValue()){
                    indexDAO.deleteCompositeIndices(v);
                }else{
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
                }
            });
        }
    }

    /**
     * 获取可选指标列表
     * @param indexType 指标类型
     * @param categoryType 目录类型  5  指标域
     * @param tenantId 租户id
     * @return
     */
    public List<OptionalIndexDTO> getOptionalIndex(int indexType, int categoryType, String tenantId) {
        //1.获取当前租户下用户所属用户组
        User user= AdminUtils.getUserData();
        List<UserGroup> groups=userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
        List<OptionalIndexDTO> optionalIndexDTOS=null;
        if(!CollectionUtils.isEmpty(groups)){
            //2.获取被授权给用户组的目录
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<String> indexFieldIds=categoryDAO.getCategorysByGroup(groupIds,categoryType,tenantId);
            //3.获取目录下的已发布的指标
            if(!CollectionUtils.isEmpty(indexFieldIds)){
                //已发布
                int indexState=2;
                if(indexType==IndexType.INDEXATOMIC.getValue()){
                    List<IndexAtomicPO> atomicPOS = indexDAO.getAtomicByIndexFields(indexFieldIds, tenantId, indexState);
                    if(!CollectionUtils.isEmpty(atomicPOS)){
                        optionalIndexDTOS=atomicPOS.stream().map(x->BeanMapper.map(x,OptionalIndexDTO.class)).distinct().collect(Collectors.toList());
                    }
                }else if(indexType==IndexType.INDEXDERIVE.getValue()){
                    List<IndexDerivePO> derivePOS = indexDAO.getDeriveByIndexFields(indexFieldIds, tenantId, indexState);
                    if(!CollectionUtils.isEmpty(derivePOS)){
                        optionalIndexDTOS=derivePOS.stream().map(x->BeanMapper.map(x,OptionalIndexDTO.class)).distinct().collect(Collectors.toList());
                    }
                }else if(indexType==IndexType.INDEXCOMPOSITE.getValue()){
                    List<IndexCompositePO> compositePOS = indexDAO.getCompositeByIndexFields(indexFieldIds, tenantId, indexState);
                    if(!CollectionUtils.isEmpty(compositePOS)){
                        optionalIndexDTOS=compositePOS.stream().map(x->BeanMapper.map(x,OptionalIndexDTO.class)).distinct().collect(Collectors.toList());
                    }
                }
            }
        }
        return optionalIndexDTOS;
    }

    /**
     * 获取可选数据源列表
     * @param tenantId
     * @return
     */
    public List<OptionalDataSourceDTO> getOptionalDataSource(String tenantId) {
        //1.获取当前租户下用户所属用户组
        User user= AdminUtils.getUserData();
        List<UserGroup> groups=userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
        List<OptionalDataSourceDTO> odsds=new ArrayList<>();
        OptionalDataSourceDTO ods=new OptionalDataSourceDTO();
        ods.setSourceId("hive");
        ods.setSourceName("hive");
        odsds.add(ods);
        if(!CollectionUtils.isEmpty(groups)){
            //2. 获取被授权给用户组的数据源
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<DataSourceBody>  dataSourceBodies=dataSourceDAO.getDataSourcesByGroups(groupIds,tenantId);
            if(!CollectionUtils.isEmpty(dataSourceBodies)){
                List<OptionalDataSourceDTO> rdbms=dataSourceBodies.stream().map(x->BeanMapper.map(x,OptionalDataSourceDTO.class)).collect(Collectors.toList());
                odsds.addAll(rdbms);
            }
        }
        return odsds;
    }

    public List<String> getOptionalDb(String dataSourceId,String tenantId) {
        List<String> dataBases=null;
        if("hive".equalsIgnoreCase(dataSourceId)){
            dataBases = tenantService.getDatabase(tenantId);
        }else{
            dataBases=tableDAO.getOptionalDbBySourceId(dataSourceId,"ACTIVE");
        }

        return dataBases;
    }

    @Override
    public Object getObjectDetail(String objectId, String type, int version) {
        return null;
    }

    @Override
    public void changeObjectStatus(String objectId, String type, int version, String approveResult) {

    }
}
