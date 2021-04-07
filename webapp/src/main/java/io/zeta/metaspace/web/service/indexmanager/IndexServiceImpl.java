package io.zeta.metaspace.web.service.indexmanager;

import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveType;
import io.zeta.metaspace.model.datasource.DataSourceBody;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.model.enums.IndexState;
import io.zeta.metaspace.model.enums.IndexType;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.modifiermanage.Qualifier;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.po.indices.*;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeanMapper;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private ColumnDAO columnDAO;
    @Autowired
    private ApproveDAO approveDAO;
    @Autowired
    private ApproveService approveServiceImpl;

    @Override
    public IndexFieldDTO getIndexFieldInfo(String categoryId, String tenantId, int categoryType) throws SQLException {
        CategoryEntityV2 category = dataManageService.getCategory(categoryId, tenantId);
        if(category!=null){
            IndexFieldDTO indexFieldDTO= BeanMapper.map(category, IndexFieldDTO.class);
            String creatorId=category.getCreator();
            String updaterId=category.getUpdater();
            if(StringUtils.isNotEmpty(creatorId)){
                indexFieldDTO.setCreator(userDAO.getUserName(creatorId));
            }
            if(StringUtils.isNotEmpty(updaterId)){
                indexFieldDTO.setUpdater(userDAO.getUserName(updaterId));
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public IndexResposeDTO addIndex(IndexDTO indexDTO, String tenantId) {
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
            try {
                indexDAO.addAtomicIndex(iap);
            } catch (SQLException e) {
                LOG.error("添加原子指标失败",e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加原子指标失败");
            }
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
            try {
                addDeriveModifierRelations(idp,modifiers);
            } catch (Exception e) {
                LOG.error("添加派生指标失败",e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加派生指标失败");
            }
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
            try {
                addDeriveCompositeRelations(icp,deriveIds);
            } catch (SQLException e) {
                LOG.error("添加复合指标失败",e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加复合指标失败");
            }
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
        if(!CollectionUtils.isEmpty(deriveIds)){
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IndexResposeDTO editIndex(IndexDTO indexDTO, String tenantId){
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
            List<String> dependentIndices = indexDTO.getDependentIndices();
            if(!CollectionUtils.isEmpty(dependentIndices)){
                idp.setIndexAtomicId(dependentIndices.get(0));
            }
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
    public void editDerivIndex(IndexDerivePO idp, List<String> modifiers, List<IndexDeriveModifierRelationPO> modifierRelations){
        //1.编辑派生指标
        indexDAO.editDerivIndex(idp);
        try{
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
        }catch (SQLException e){
            LOG.error(e.getMessage());
            LOG.error("编辑派生指标失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }


    }

    @Transactional(rollbackFor = Exception.class)
    public void editCompositeIndex(IndexCompositePO icp, List<String> dependentIndices, List<IndexDeriveCompositeRelationPO> compositeRelations) {
        //1.编辑复合指标
        indexDAO.editCompositeIndex(icp);
        //2.编辑复合指标与派生指标关系
        try{
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
        } catch (SQLException e) {
           LOG.error("编辑复合指标失败",e);
           throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndex(List<DeleteIndexInfoDTO> deleteList, String tenantId) {
        if(!CollectionUtils.isEmpty(deleteList)){
            Map<Integer,List<String>> deleteMap=new HashMap<>();
            deleteList.forEach(x->{
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
    @Override
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
    @Override
    public List<OptionalDataSourceDTO> getOptionalDataSource(String tenantId) {
        //1.获取当前租户下用户所属用户组
        User user= AdminUtils.getUserData();
        List<UserGroup> groups=userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
        List<OptionalDataSourceDTO> odsds=new ArrayList<>();
        OptionalDataSourceDTO ods=new OptionalDataSourceDTO();
        ods.setSourceId("hive");
        ods.setSourceName(DataSourceType.HIVE.getName());
        ods.setSourceType(DataSourceType.HIVE.getName());
        odsds.add(ods);
        if(!CollectionUtils.isEmpty(groups)){
            //2. 获取被授权给用户组的数据源
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<DataSourceBody>  dataSourceBodies=dataSourceDAO.getDataSourcesByGroups(groupIds,tenantId);
            if(!CollectionUtils.isEmpty(dataSourceBodies)){
                //根据id去重
                List<DataSourceBody> unique=dataSourceBodies.stream().collect(
                        Collectors.collectingAndThen(
                                Collectors.toCollection(()->new TreeSet<>(Comparator.comparing(DataSourceBody::getSourceId))),ArrayList::new));
                List<OptionalDataSourceDTO> rdbms=unique.stream().map(x->BeanMapper.map(x,OptionalDataSourceDTO.class)).collect(Collectors.toList());
                odsds.addAll(rdbms);
            }
        }
        return odsds;
    }

    @Override
    public List<String> getOptionalDb(String dataSourceId, String tenantId) {
        List<String> dataBases=null;
        if("hive".equalsIgnoreCase(dataSourceId)){
            dataBases = tenantService.getDatabase(tenantId);
        }else{
            dataBases=tableDAO.getOptionalDbBySourceId(dataSourceId,"ACTIVE");
        }

        return dataBases;
    }

    @Override
    public List<OptionalTableDTO> getOptionalTable(String dataSourceId, String dbName) {
        List<TableInfo> tableInfos=tableDAO.getTableByDataSourceAndDb(dataSourceId,dbName,"ACTIVE");
        List<OptionalTableDTO> optionalTableDTOS=null;
        if(!CollectionUtils.isEmpty(tableInfos)){
            optionalTableDTOS = tableInfos.stream().map(x -> BeanMapper.map(x, OptionalTableDTO.class)).collect(Collectors.toList());
        }
        return optionalTableDTOS;
    }

    @Override
    public List<OptionalColumnDTO> getOptionalColumn(String tableId) {
        List<Column> columnInfoList = columnDAO.getColumnInfoList(tableId);
        List<OptionalColumnDTO> optionalColumnDTOS=null;
        if(!CollectionUtils.isEmpty(columnInfoList)){
            optionalColumnDTOS = columnInfoList.stream().map(x -> BeanMapper.map(x, OptionalColumnDTO.class)).collect(Collectors.toList());
        }
        return optionalColumnDTOS;
    }

    @Override
    public IndexInfoDTO getIndexInfo(String indexId, int indexType,int version,int categoryType, String tenantId) {
        IndexInfoDTO indexInfoDTO=null;
        if(indexType==IndexType.INDEXATOMIC.getValue()){
            IndexInfoPO indexInfoPO=indexDAO.getAtomicIndexInfoPO(indexId,version,categoryType,tenantId);
            if(!Objects.isNull(indexInfoPO)){
                indexInfoDTO=BeanMapper.map(indexInfoPO,IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXATOMIC.getValue());
            }
        }else if(indexType==IndexType.INDEXDERIVE.getValue()){
            IndexInfoPO indexInfoPO=indexDAO.getDeriveIndexInfoPO(indexId,version,categoryType,tenantId);
            if(!Objects.isNull(indexInfoPO)){
                indexInfoDTO=BeanMapper.map(indexInfoPO,IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXDERIVE.getValue());
                //添加依赖的原子指标
                List<IndexAtomicPO> indexAtomicPOs=indexDAO.getDependentAtomicIndex(indexInfoPO.getIndexAtomicId(),tenantId);
                if(!CollectionUtils.isEmpty(indexAtomicPOs)){
                    List<DependentIndex> dependentIndices=indexAtomicPOs.stream().map(x->BeanMapper.map(x,DependentIndex.class)).collect(Collectors.toList());
                    indexInfoDTO.setDependentIndices(dependentIndices);
                }else{
                    indexInfoDTO.setDependentIndices(new ArrayList<>());
                }
                //添加修饰词
                List<Qualifier> qualifiers=indexDAO.getModifiers(indexInfoPO.getIndexId(),tenantId);
                if(!CollectionUtils.isEmpty(qualifiers)){
                    List<Modifier> modifiers=qualifiers.stream().map(x->BeanMapper.map(x,Modifier.class)).collect(Collectors.toList());
                    indexInfoDTO.setModifiers(modifiers);
                }else{
                    indexInfoDTO.setModifiers(new ArrayList<>());
                }
            }
        }else if(indexType==IndexType.INDEXCOMPOSITE.getValue()){
            IndexInfoPO indexInfoPO=indexDAO.getCompositeIndexInfoPO(indexId,version,categoryType,tenantId);
            if(!Objects.isNull(indexInfoPO)){
                indexInfoDTO=BeanMapper.map(indexInfoPO,IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXCOMPOSITE.getValue());
                //添加依赖的派生指标
                List<IndexDerivePO> indexDerivePOS=indexDAO.getDependentDeriveIndex(indexInfoPO.getIndexId(),tenantId);
                if(!CollectionUtils.isEmpty(indexDerivePOS)){
                    List<DependentIndex> dependentIndices=indexDerivePOS.stream().map(x->BeanMapper.map(x,DependentIndex.class)).collect(Collectors.toList());
                    indexInfoDTO.setDependentIndices(dependentIndices);
                }else{
                    indexInfoDTO.setDependentIndices(new ArrayList<>());
                }
            }
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        if(!Objects.isNull(indexInfoDTO)){
            //hive数据源单独处理
            if("hive".equalsIgnoreCase(indexInfoDTO.getSourceId())){
                indexInfoDTO.setSourceName(DataSourceType.HIVE.getName());
            }
            //添加审批组成员
            List<User> users=approveDAO.getApproveUsers(indexInfoDTO.getApprovalGroupId());
            if(!CollectionUtils.isEmpty(users)){
                List<ApprovalGroupMember> approvalGroupMembers=users.stream().map(x->BeanMapper.map(x, ApprovalGroupMember.class)).collect(Collectors.toList());
                indexInfoDTO.setApprovalGroupMembers(approvalGroupMembers);
            }else {
                indexInfoDTO.setApprovalGroupMembers(new ArrayList<>());
            }
        }
        return indexInfoDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void indexSendApprove(List<PublishIndexDTO> dtoList, String tenantId) throws AtlasBaseException{
        List<ApproveItem> approveItems=new ArrayList<>();
        for (PublishIndexDTO pid : dtoList) {
            if(Objects.isNull(pid.getIndexId())||Objects.isNull(pid.getIndexName())|| Objects.isNull(ApproveType.getApproveTypeByCode(pid.getApproveType()))||Objects.isNull(pid.getApprovalGroupId())){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
            }
            ApproveItem approveItem=new ApproveItem();
            approveItem.setId(UUID.randomUUID().toString());
            approveItem.setObjectId(pid.getIndexId());
            approveItem.setObjectName(pid.getIndexName());
            approveItem.setBusinessType(pid.getIndexType()+"");
            approveItem.setApproveType(pid.getApproveType());
            approveItem.setApproveGroup(pid.getApprovalGroupId());
            User user= AdminUtils.getUserData();
            approveItem.setSubmitter(user.getUserId());
            Timestamp timeStamp=new Timestamp(System.currentTimeMillis());
            approveItem.setCommitTime(timeStamp);
            approveItem.setModuleId(ModuleEnum.NORMDESIGN.getId()+"");
            approveItem.setVersion(pid.getVersion());
            approveItem.setTenantId(tenantId);
            approveItems.add(approveItem);
        }
        batchSendApprove(approveItems,tenantId);

    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSendApprove(List<ApproveItem> approveItems, String tenantId) {
        if(!CollectionUtils.isEmpty(approveItems)){
            for(ApproveItem approveItem:approveItems){
                approveServiceImpl.addApproveItem(approveItem);
                indexDAO.updatePublishInfo(approveItem,tenantId,IndexState.APPROVAL.getValue());
            }
        }
    }


    @Override
    public List<IndexInfoDTO> publishHistory(String indexId, PageQueryDTO pageQueryDTO, int categoryType, String tenantId) {

        int indexType=pageQueryDTO.getIndexType();
        int offset=pageQueryDTO.getOffset();
        int limit=pageQueryDTO.getLimit();
        List<IndexInfoDTO> indexInfoDTOs=null;
        if(indexType==IndexType.INDEXATOMIC.getValue()){
            List<IndexInfoPO> indexInfoPOs=indexDAO.getAtomicIndexHistory(indexId,categoryType,offset,limit,tenantId);
            indexInfoDTOs=indexInfoPOs.stream().map(x->{
                IndexInfoDTO indexInfoDTO=BeanMapper.map(x,IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXATOMIC.getValue());
                return indexInfoDTO;
            }).collect(Collectors.toList());
        }else if(indexType==IndexType.INDEXDERIVE.getValue()){
            List<IndexInfoPO> indexInfoPOs=indexDAO.getDeriveIndexHistory(indexId,categoryType,offset,limit,tenantId);
            if(!CollectionUtils.isEmpty(indexInfoPOs)){
                List<String> dependentIndexIds = indexInfoPOs.stream().map(x -> x.getIndexAtomicId()).distinct().collect(Collectors.toList());
                //获取依赖原子指标
                List<IndexAtomicPO> indexAtomicPOS=indexDAO.getAtomicIndexInfoPOs(dependentIndexIds,tenantId);
                Map<String, DependentIndex> maps = indexAtomicPOS.stream().map(x->BeanMapper.map(x,DependentIndex.class)).collect(Collectors.toMap(DependentIndex::getIndexId, Function.identity(), (key1, key2) -> key2));
                //获取依赖的修饰词
                List<Qualifier> qualifiers=indexDAO.getModifiers(indexId,tenantId);
                List<Modifier> modifiers=null;
                if(!CollectionUtils.isEmpty(qualifiers)){
                    modifiers=qualifiers.stream().map(x->BeanMapper.map(x,Modifier.class)).collect(Collectors.toList());
                }
                List<Modifier> finalModifiers = modifiers;
                indexInfoDTOs = indexInfoPOs.stream().map(x -> {
                    IndexInfoDTO indexInfoDTO = BeanMapper.map(x, IndexInfoDTO.class);
                    indexInfoDTO.setIndexType(IndexType.INDEXDERIVE.getValue());
                    DependentIndex dependentIndex = maps.get(x.getIndexAtomicId());
                    if (!Objects.isNull(dependentIndex)) {
                        List<DependentIndex> dependentIndices = new ArrayList<>();
                        dependentIndices.add(dependentIndex);
                        //添加指标依赖
                        indexInfoDTO.setDependentIndices(dependentIndices);
                    }
                    //添加修饰词
                    indexInfoDTO.setModifiers(finalModifiers);
                    return indexInfoDTO;
                }).collect(Collectors.toList());
            }
        }else if(indexType==IndexType.INDEXCOMPOSITE.getValue()){
            List<IndexInfoPO> indexInfoPOs=indexDAO.getCompositeIndexHistory(indexId,categoryType,offset,limit,tenantId);
            if(!CollectionUtils.isEmpty(indexInfoPOs)){
                List<IndexDerivePO> indexDerivePOS = indexDAO.getDependentDeriveIndex(indexId, tenantId);
                List<DependentIndex> dependentIndices=null;
                if(!CollectionUtils.isEmpty(indexDerivePOS)){
                    dependentIndices=indexDerivePOS.stream().map(x->BeanMapper.map(x,DependentIndex.class)).collect(Collectors.toList());
                }
                List<DependentIndex> finalDependentIndices = dependentIndices;
                indexInfoDTOs = indexInfoPOs.stream().map(x -> {
                    IndexInfoDTO indexInfoDTO = BeanMapper.map(x, IndexInfoDTO.class);
                    indexInfoDTO.setIndexType(IndexType.INDEXCOMPOSITE.getValue());
                    indexInfoDTO.setDependentIndices(finalDependentIndices);
                    return indexInfoDTO;
                }).collect(Collectors.toList());
            }
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        if(!CollectionUtils.isEmpty(indexInfoDTOs)){
            List<String> approvalGroupIds = indexInfoDTOs.stream().map(x -> x.getApprovalGroupId()).distinct().collect(Collectors.toList());
            Map<String,List<ApprovalGroupMember>> usersMap=new HashMap<>();
            if(!CollectionUtils.isEmpty(approvalGroupIds)){
                for(String str:approvalGroupIds){
                    List<User> approveUsers = approveDAO.getApproveUsers(str);
                    if(!CollectionUtils.isEmpty(approveUsers)){
                        List<ApprovalGroupMember> approvalGroupMembers=approveUsers.stream().map(u->BeanMapper.map(u,ApprovalGroupMember.class)).collect(Collectors.toList());
                        usersMap.put(str,approvalGroupMembers);
                    }
                }
            }
            //添加审批组成员
            indexInfoDTOs.forEach(x->{
                String approvalGroupId = x.getApprovalGroupId();
                if(StringUtils.isNotEmpty(approvalGroupId)){
                    List<ApprovalGroupMember> approvalGroupMembers= usersMap.get(approvalGroupId);
                    if(!CollectionUtils.isEmpty(approvalGroupMembers)){
                        x.setApprovalGroupMembers(approvalGroupMembers);
                    }
                }
            });
        }
        return indexInfoDTOs;
    }

    @Override
    public List<IndexInfoDTO> pageQuery(PageQueryDTO pageQueryDTO, int categoryType, String tenantId) throws Exception {
        List<IndexInfoPO> indexInfoPOS=indexDAO.pageQuery(pageQueryDTO,categoryType,tenantId);
        List<IndexInfoDTO> indexInfoDTOS=null;
        if(!CollectionUtils.isEmpty(indexInfoPOS)){
            indexInfoDTOS = indexInfoPOS.stream().map(x -> BeanMapper.map(x, IndexInfoDTO.class)).collect(Collectors.toList());
        }
        return indexInfoDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndexByIndexFieldId(String guid, String tenantId) {
        indexDAO.deleteAtomicByIndexFieldId(guid,tenantId);
        indexDAO.deleteDeriveByIndexFieldId(guid,tenantId);
        indexDAO.deleteCompositeByIndexFieldId(guid,tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeIndexToAnotherIndexField(String sourceGuid, String tenantId, String targetGuid) {
        indexDAO.updateAtomicIndexFieldId(sourceGuid,tenantId,targetGuid);
        indexDAO.updateDeriveIndexFieldId(sourceGuid,tenantId,targetGuid);
        indexDAO.updateCompositeIndexFieldId(sourceGuid,tenantId,targetGuid);
    }

    @Override
    public Object getObjectDetail(String objectId, String type, int version,String tenantId) {
        IndexInfoDTO indexInfo=null;
        if(StringUtils.isNumeric(type)){
            int indexType=Integer.parseInt(type);
            indexInfo = getIndexInfo(objectId, indexType, version, 5, tenantId);
        }else{
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        return indexInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeObjectStatus(String approveResult, String tenantId, List<ApproveItem> items) {
        if(!CollectionUtils.isEmpty(items)){
            for(ApproveItem item:items){
                int indexType=Integer.parseInt(item.getBusinessType());
                String objectId=item.getObjectId();
                int version=item.getVersion();
                String approveType=item.getApproveType();

                if(approveType.equals(ApproveType.PUBLISH.getCode())){
                    //发布
                    if(approveResult.equals(ApproveOperate.APPROVE.getCode())){
                        //通过
                        editIndexState(objectId,indexType,version,tenantId,IndexState.PUBLISH.getValue());
                    }else if(approveResult.equals(ApproveOperate.REJECTED.getCode())){
                        //驳回,回退指标状态
                        if(version==0){
                            editIndexState(objectId,indexType,version,tenantId,IndexState.CREATE.getValue());
                        }else{
                            editIndexState(objectId,indexType,version,tenantId,IndexState.OFFLINE.getValue());
                        }
                    }
                }else if(approveType.equals(ApproveType.OFFLINE.getCode())){
                    //下线
                    if(approveResult.equals(ApproveOperate.APPROVE.getCode())){
                        //通过
                        try {
                            offlineApprove(objectId,indexType,version,tenantId,IndexState.OFFLINE.getValue());
                        }catch (Exception e){
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e);
                        }
                    }else if(approveResult.equals(ApproveOperate.REJECTED.getCode())){
                        //驳回,回退指标状态
                        editIndexState(objectId,indexType,version,tenantId,IndexState.PUBLISH.getValue());
                    }
                }
            }
        }
    }

    private void editIndexState(String indexId, int indexType, int version, String tenantId, int state) {
        if(indexType==IndexType.INDEXATOMIC.getValue()){
            indexDAO.editAtomicState(indexId,version,tenantId,state);
        }else if(indexType==IndexType.INDEXDERIVE.getValue()){
            indexDAO.editDeriveState(indexId,version,tenantId,state);
        }else if(indexType==IndexType.INDEXCOMPOSITE.getValue()){
            indexDAO.editCompositeState(indexId,version,tenantId,state);
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void offlineApprove(String indexId, int indexType, int version, String tenantId, int state) throws SQLException {
        editIndexState(indexId,indexType,version,tenantId,state);
        //版本+1
        if(indexType==IndexType.INDEXATOMIC.getValue()){
            IndexAtomicPO indexAtomicPO=indexDAO.getAtomicIndexPO(indexId,version,tenantId);
            indexAtomicPO.setVersion(indexAtomicPO.getVersion()+1);
            indexDAO.addAtomicIndex(indexAtomicPO);
        }else if(indexType==IndexType.INDEXDERIVE.getValue()){
            IndexDerivePO indexDerivePO=indexDAO.getDeriveIndexPO(indexId,version,tenantId);
            indexDerivePO.setVersion(indexDerivePO.getVersion()+1);
            indexDAO.addDeriveIndex(indexDerivePO);
        }else if(indexType==IndexType.INDEXCOMPOSITE.getValue()){
            IndexCompositePO indexCompositePO=indexDAO.getCompositeIndexPO(indexId,version,tenantId);
            indexCompositePO.setVersion(indexCompositePO.getVersion()+1);
            indexDAO.addCompositeIndex(indexCompositePO);
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
    }
}
