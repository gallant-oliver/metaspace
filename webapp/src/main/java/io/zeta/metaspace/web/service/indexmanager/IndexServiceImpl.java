package io.zeta.metaspace.web.service.indexmanager;

import com.google.gson.Gson;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveType;
import io.zeta.metaspace.model.approvegroup.ApproveGroupListAndSearchResult;
import io.zeta.metaspace.model.approvegroup.ApproveGroupParas;
import io.zeta.metaspace.model.datasource.DataSourceBody;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.model.enums.IndexState;
import io.zeta.metaspace.model.enums.IndexType;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.TableInfoId;
import io.zeta.metaspace.model.modifiermanage.Data;
import io.zeta.metaspace.model.modifiermanage.Qualifier;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.po.indices.*;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.model.timelimit.TimelimitEntity;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.service.timelimit.TimeLimitServiceImp;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service("indexService")
public class IndexServiceImpl implements IndexService {
    private static final Logger LOG = LoggerFactory.getLogger(IndexServiceImpl.class);

    private static Configuration conf;

    public static String tmpFilePath;

    static {
        try {
            conf = ApplicationProperties.get();
            tmpFilePath = System.getProperty("java.io.tmpdir");
            if (tmpFilePath.endsWith(String.valueOf(File.separatorChar))) {
                tmpFilePath = tmpFilePath + "metaspace";
            } else {
                tmpFilePath = tmpFilePath + File.separatorChar + "metaspace";
            }
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }


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

    @Autowired
    private TimeLimitDAO timeLimitDAO;

    @Autowired
    private QualifierService qualifierService;

    @Autowired
    private ApproveGroupService approveGroupService;

    @Autowired
    private TimeLimitServiceImp timeLimitServiceImp;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ApproveGroupDAO approveGroupDAO;

    @Autowired
    private QualifierDAO qualifierDAO;

    //????????????    ?????????
    private static final int CATEGORY_TYPE = 5;

    @Override
    public IndexFieldDTO getIndexFieldInfo(String categoryId, String tenantId, int categoryType) throws SQLException {
        CategoryEntityV2 category = dataManageService.getCategory(categoryId, tenantId);
        if (category != null) {
            IndexFieldDTO indexFieldDTO = BeanMapper.map(category, IndexFieldDTO.class);
            String creatorId = category.getCreator();
            String updaterId = category.getUpdater();
            if (StringUtils.isNotEmpty(creatorId)) {
                indexFieldDTO.setCreator(userDAO.getUserName(creatorId));
            }
            if (StringUtils.isNotEmpty(updaterId)) {
                indexFieldDTO.setUpdater(userDAO.getUserName(updaterId));
            }
            return indexFieldDTO;
        } else {
            return null;
        }
    }

    /**
     * ????????????
     *
     * @param indexDTO
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public IndexResposeDTO addIndex(IndexDTO indexDTO, String tenantId) {
        int indexType = indexDTO.getIndexType();
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        IndexResposeDTO iard = null;
        if (indexType == IndexType.INDEXATOMIC.getValue()) {
            //???????????????????????????
            IndexAtomicPO exits = indexDAO.getAtomicIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
            }
            IndexAtomicPO iap = BeanMapper.map(indexDTO, IndexAtomicPO.class);
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
                LOG.error("????????????????????????", e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
            iard = BeanMapper.map(iap, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
            //???????????????????????????
            IndexAtomicPO exits = indexDAO.getDeriveIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
            }
            IndexDerivePO idp = BeanMapper.map(indexDTO, IndexDerivePO.class);
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
                addDeriveModifierRelations(idp, modifiers);
            } catch (Exception e) {
                LOG.error("????????????????????????", e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
            iard = BeanMapper.map(idp, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
            //???????????????????????????
            IndexAtomicPO exits = indexDAO.getCompositeIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
            }
            IndexCompositePO icp = BeanMapper.map(indexDTO, IndexCompositePO.class);
            icp.setIndexId(UUID.randomUUID().toString());
            icp.setTenantId(tenantId);
            icp.setIndexState(1);
            icp.setVersion(0);
            icp.setCreator(user.getUserId());
            icp.setCreateTime(timestamp);
            icp.setUpdateTime(timestamp);
            List<String> deriveIds = indexDTO.getDependentIndices();
            try {
                addDeriveCompositeRelations(icp, deriveIds);
            } catch (SQLException e) {
                LOG.error("????????????????????????", e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
            iard = BeanMapper.map(icp, IndexResposeDTO.class);
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
        return iard;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDeriveCompositeRelations(IndexCompositePO icp, List<String> deriveIds) throws SQLException {
        String indexId = icp.getIndexId();
        indexDAO.addCompositeIndex(icp);
        List<IndexDeriveCompositeRelationPO> idcrPOS = getDeriveCompositeRelationPOS(indexId, deriveIds);
        if (!CollectionUtils.isEmpty(idcrPOS)) {
            indexDAO.addDeriveCompositeRelations(idcrPOS);
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param indexId
     * @return
     */
    private List<IndexDeriveCompositeRelationPO> getDeriveCompositeRelationPOS(String indexId, List<String> deriveIds) {
        List<IndexDeriveCompositeRelationPO> idcrPOS = null;
        if (!CollectionUtils.isEmpty(deriveIds)) {
            idcrPOS = new ArrayList<>();
            for (String deriveId : deriveIds) {
                IndexDeriveCompositeRelationPO idcr = new IndexDeriveCompositeRelationPO();
                idcr.setCompositeIndexId(indexId);
                idcr.setDeriveIndexId(deriveId);
                idcrPOS.add(idcr);
            }
        }
        return idcrPOS;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDeriveModifierRelations(IndexDerivePO idp, List<String> modifiers) throws Exception {
        String indexId = idp.getIndexId();
        indexDAO.addDeriveIndex(idp);
        List<IndexDeriveModifierRelationPO> idmrPOS = getDeriveModifierRelationPOS(indexId, modifiers);
        if (!CollectionUtils.isEmpty(idmrPOS)) {
            indexDAO.addDeriveModifierRelations(idmrPOS);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param indexId
     * @param modifiers
     * @return
     */
    private List<IndexDeriveModifierRelationPO> getDeriveModifierRelationPOS(String indexId, List<String> modifiers) {
        List<IndexDeriveModifierRelationPO> idmrPOS = null;
        if (!CollectionUtils.isEmpty(modifiers)) {
            idmrPOS = new ArrayList<>();
            for (String modifierId : modifiers) {
                IndexDeriveModifierRelationPO idmrPO = new IndexDeriveModifierRelationPO();
                idmrPO.setDeriveIndexId(indexId);
                idmrPO.setModifierId(modifierId);
                idmrPOS.add(idmrPO);
            }
        }
        return idmrPOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IndexResposeDTO editIndex(IndexDTO indexDTO, String tenantId) {
        int indexType = indexDTO.getIndexType();
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        IndexResposeDTO iard = null;
        if (indexType == IndexType.INDEXATOMIC.getValue()) {
            //???????????????????????????
            IndexAtomicPO exits = indexDAO.getAtomicIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
            }
            IndexAtomicPO iap = BeanMapper.map(indexDTO, IndexAtomicPO.class);
            iap.setUpdater(user.getUserId());
            iap.setUpdateTime(timestamp);
            indexDAO.editAtomicIndex(iap);
            indexDAO.moveAtomicIndex(iap.getIndexId(), tenantId, iap.getIndexFieldId());
            iard = BeanMapper.map(iap, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
            //???????????????????????????
            IndexAtomicPO exits = indexDAO.getDeriveIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
            }
            IndexDerivePO idp = BeanMapper.map(indexDTO, IndexDerivePO.class);
            idp.setUpdater(user.getUserId());
            idp.setUpdateTime(timestamp);
            List<String> dependentIndices = indexDTO.getDependentIndices();
            if (!CollectionUtils.isEmpty(dependentIndices)) {
                idp.setIndexAtomicId(dependentIndices.get(0));
            }
            //???????????????????????????????????????????????????
            List<IndexDeriveModifierRelationPO> modifierRelations = indexDAO.getDeriveModifierRelations(idp.getIndexId());
            editDerivIndex(idp, indexDTO.getModifiers(), modifierRelations);
            indexDAO.moveDerivIndex(idp.getIndexId(), tenantId, idp.getIndexFieldId());
            iard = BeanMapper.map(idp, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
            //???????????????????????????
            IndexAtomicPO exits = indexDAO.getCompositeIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
            }
            IndexCompositePO icp = BeanMapper.map(indexDTO, IndexCompositePO.class);
            icp.setUpdater(user.getUserId());
            icp.setUpdateTime(timestamp);
            List<IndexDeriveCompositeRelationPO> compositeRelations = indexDAO.getDeriveCompositeRelations(icp.getIndexId());
            editCompositeIndex(icp, indexDTO.getDependentIndices(), compositeRelations);
            indexDAO.moveCompositeIndex(icp.getIndexId(), tenantId, icp.getIndexFieldId());
            iard = BeanMapper.map(icp, IndexResposeDTO.class);
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
        return iard;
    }

    @Transactional(rollbackFor = Exception.class)
    public void editDerivIndex(IndexDerivePO idp, List<String> modifiers, List<IndexDeriveModifierRelationPO> modifierRelations) {
        //1.??????????????????
        indexDAO.editDerivIndex(idp);
        try {
            //2.????????????????????????????????????
            if (CollectionUtils.isEmpty(modifierRelations)) {
                if (!CollectionUtils.isEmpty(modifiers)) {
                    //????????????????????????????????????
                    List<IndexDeriveModifierRelationPO> idmrPOS = getDeriveModifierRelationPOS(idp.getIndexId(), modifiers);
                    indexDAO.addDeriveModifierRelations(idmrPOS);
                }
            } else {
                if (!CollectionUtils.isEmpty(modifiers)) {
                    List<String> exits = modifierRelations.stream().map(x -> x.getModifierId()).distinct().collect(Collectors.toList());
                    //???????????????????????????????????????
                    List<String> adds = modifiers.stream().filter(x -> !exits.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(adds)) {
                        List<IndexDeriveModifierRelationPO> addPOS = getDeriveModifierRelationPOS(idp.getIndexId(), adds);
                        indexDAO.addDeriveModifierRelations(addPOS);
                    }
                    //???????????????????????????????????????
                    List<String> dels = exits.stream().filter(x -> !modifiers.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(dels)) {
                        indexDAO.deleteDeriveModifierRelationsByDeriveModifierId(idp.getIndexId(), dels);
                    }
                } else {
                    //????????????????????????????????????????????????
                    indexDAO.deleteDeriveModifierRelationsByDeriveId(idp.getIndexId());
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            LOG.error("????????????????????????", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }


    }

    @Transactional(rollbackFor = Exception.class)
    public void editCompositeIndex(IndexCompositePO icp, List<String> dependentIndices, List<IndexDeriveCompositeRelationPO> compositeRelations) {
        //1.??????????????????
        indexDAO.editCompositeIndex(icp);
        //2.???????????????????????????????????????
        try {
            if (CollectionUtils.isEmpty(compositeRelations)) {
                if (!CollectionUtils.isEmpty(dependentIndices)) {
                    //????????????????????????????????????
                    List<IndexDeriveCompositeRelationPO> idcrPOS = getDeriveCompositeRelationPOS(icp.getIndexId(), dependentIndices);
                    indexDAO.addDeriveCompositeRelations(idcrPOS);
                }
            } else {
                if (!CollectionUtils.isEmpty(dependentIndices)) {
                    List<String> exits = compositeRelations.stream().map(x -> x.getCompositeIndexId()).distinct().collect(Collectors.toList());
                    //???????????????????????????????????????
                    List<String> adds = dependentIndices.stream().filter(x -> !exits.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(adds)) {
                        List<IndexDeriveCompositeRelationPO> addPOS = getDeriveCompositeRelationPOS(icp.getIndexId(), adds);
                        indexDAO.addDeriveCompositeRelations(addPOS);
                    }
                    //???????????????????????????????????????
                    List<String> dels = exits.stream().filter(x -> !dependentIndices.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(dels)) {
                        indexDAO.deleteDeriveCompositeRelationsByDeriveCompositeId(icp.getIndexId(), dels);
                    }
                } else {
                    //????????????????????????????????????????????????
                    indexDAO.deleteDeriveCompositeRelationsByDeriveId(icp.getIndexId());
                }
            }
        } catch (SQLException e) {
            LOG.error("????????????????????????", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<IndexInfoPO> deleteIndex(List<DeleteIndexInfoDTO> deleteList, String tenantId) {
        List<IndexInfoPO> indexInfoPOS = null;
        if (!CollectionUtils.isEmpty(deleteList)) {
            List<String> indexIds = deleteList.stream().map(x -> x.getIndexId()).collect(Collectors.toList());
            indexInfoPOS = indexDAO.getIndexNamesByIds(indexIds, tenantId);
            deleteIndexs(deleteList, tenantId);
        }
        return indexInfoPOS;
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteIndexs(List<DeleteIndexInfoDTO> deleteList, String tenantId) {
        if (!CollectionUtils.isEmpty(deleteList)) {

            Map<Integer, List<String>> deleteMap = new HashMap<>();
            deleteList.forEach(x -> {
                List<String> deleteIds = deleteMap.get(x.getIndexType());
                if (CollectionUtils.isEmpty(deleteIds)) {
                    deleteIds = new ArrayList<>();
                    deleteMap.put(x.getIndexType(), deleteIds);
                }
                deleteIds.add(x.getIndexId());
            });
            deleteIndexMap(deleteMap);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteIndexMap(Map<Integer, List<String>> deleteMap) {
        if (!CollectionUtils.isEmpty(deleteMap)) {
            deleteMap.forEach((k, v) -> {
                if (k == IndexType.INDEXATOMIC.getValue()) {
                    indexDAO.deleteAtomicIndices(v);
                } else if (k == IndexType.INDEXDERIVE.getValue()) {
                    indexDAO.deleteDeriveIndices(v);
                } else if (k == IndexType.INDEXCOMPOSITE.getValue()) {
                    indexDAO.deleteCompositeIndices(v);
                } else {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
                }
            });
        }
    }

    /**
     * ????????????????????????
     *
     * @param indexType    ????????????
     * @param categoryType ????????????  5  ?????????
     * @param tenantId     ??????id
     * @return
     */
    @Override
    public List<OptionalIndexDTO> getOptionalIndex(int indexType, int categoryType, String tenantId) {
        //1.??????????????????????????????????????????
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        List<OptionalIndexDTO> optionalIndexDTOS = null;
        if (!CollectionUtils.isEmpty(groups)) {
            //2.????????????????????????????????????
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<String> indexFieldIds = categoryDAO.getCategorysByGroup(groupIds, categoryType, tenantId);
            indexFieldIds.add("index_field_default");
            //3.????????????????????????????????????
            if (!CollectionUtils.isEmpty(indexFieldIds)) {
                //?????????
                int indexState = 2;
                if (indexType == IndexType.INDEXATOMIC.getValue()) {
                    List<IndexAtomicPO> atomicPOS = indexDAO.getAtomicByIndexFields(indexFieldIds, tenantId, indexState);
                    if (!CollectionUtils.isEmpty(atomicPOS)) {
                        optionalIndexDTOS = atomicPOS.stream().map(x -> BeanMapper.map(x, OptionalIndexDTO.class)).distinct().collect(Collectors.toList());
                    }
                } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
                    List<IndexDerivePO> derivePOS = indexDAO.getDeriveByIndexFields(indexFieldIds, tenantId, indexState);
                    if (!CollectionUtils.isEmpty(derivePOS)) {
                        optionalIndexDTOS = derivePOS.stream().map(x -> BeanMapper.map(x, OptionalIndexDTO.class)).distinct().collect(Collectors.toList());
                    }
                } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
                    List<IndexCompositePO> compositePOS = indexDAO.getCompositeByIndexFields(indexFieldIds, tenantId, indexState);
                    if (!CollectionUtils.isEmpty(compositePOS)) {
                        optionalIndexDTOS = compositePOS.stream().map(x -> BeanMapper.map(x, OptionalIndexDTO.class)).distinct().collect(Collectors.toList());
                    }
                }
            }
        }
        return optionalIndexDTOS;
    }

    /**
     * ???????????????????????????
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<OptionalDataSourceDTO> getOptionalDataSource(String tenantId) {
        //1.??????????????????????????????????????????
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        List<OptionalDataSourceDTO> odsds = new ArrayList<>();
        OptionalDataSourceDTO ods = new OptionalDataSourceDTO();
        ods.setSourceId("hive");
        ods.setSourceName(DataSourceType.HIVE.getName());
        ods.setSourceType(DataSourceType.HIVE.getName());
        odsds.add(ods);
        if (!CollectionUtils.isEmpty(groups)) {
            //2. ???????????????????????????????????????
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<DataSourceBody> dataSourceBodies = dataSourceDAO.getDataSourcesByGroups(groupIds, tenantId);
            if (!CollectionUtils.isEmpty(dataSourceBodies)) {
                //??????id??????
                List<DataSourceBody> unique = dataSourceBodies.stream().collect(
                        Collectors.collectingAndThen(
                                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DataSourceBody::getSourceId))), ArrayList::new));
                List<OptionalDataSourceDTO> rdbms = unique.stream().map(x -> BeanMapper.map(x, OptionalDataSourceDTO.class)).collect(Collectors.toList());
                odsds.addAll(rdbms);
            }
        }
        return odsds;
    }

    @Override
    public List<String> getOptionalDb(String dataSourceId, String tenantId) {
        List<String> dataBases = null;
        if ("hive".equalsIgnoreCase(dataSourceId)) {
            dataBases = tenantService.getDatabase(tenantId);
        } else {
            dataBases = tableDAO.getOptionalDbBySourceId(dataSourceId, "ACTIVE");
        }

        return dataBases;
    }

    @Override
    public List<OptionalTableDTO> getOptionalTable(String dataSourceId, String dbName) {
        List<TableInfo> tableInfos = tableDAO.getTableByDataSourceAndDb(dataSourceId, dbName, "ACTIVE");
        List<OptionalTableDTO> optionalTableDTOS = null;
        if (!CollectionUtils.isEmpty(tableInfos)) {
            optionalTableDTOS = tableInfos.stream().map(x -> BeanMapper.map(x, OptionalTableDTO.class)).collect(Collectors.toList());
        }
        return optionalTableDTOS;
    }

    @Override
    public List<OptionalColumnDTO> getOptionalColumn(String tableId) {
        List<Column> columnInfoList = columnDAO.getColumnInfoList(tableId);
        List<OptionalColumnDTO> optionalColumnDTOS = null;
        if (!CollectionUtils.isEmpty(columnInfoList)) {
            optionalColumnDTOS = columnInfoList.stream().map(x -> BeanMapper.map(x, OptionalColumnDTO.class)).collect(Collectors.toList());
        }
        return optionalColumnDTOS;
    }


    /**
     * ??????????????????
     *
     * @param indexId
     * @param indexType
     * @param version
     * @param tenantId
     */
    @Override
    public IndexLinkDto getIndexLink(String indexId, int indexType, String version, String tenantId) {
        List<IndexLinkEntity> nodes = new LinkedList<>();
        List<IndexLinkRelation> relations = new LinkedList<>();
        boolean status = true;
        if (indexType == IndexType.INDEXCOMPOSITE.getValue()) { //????????????
            IndexCompositePO compositeIndexPO = indexDAO.getCompositeIndexPO(indexId, Integer.parseInt(version), tenantId); //????????????

            List<IndexDerivePO> dependentDeriveIndex = indexDAO.getDependentDeriveIndex(indexId, tenantId);//????????????????????????
            if (dependentDeriveIndex != null && dependentDeriveIndex.size() > 0) {
                for (IndexDerivePO po : dependentDeriveIndex) {

                    IndexAtomicPO indexAtomicPO = indexDAO.getDependentAtomicIndex(po.getIndexAtomicId(), tenantId);
                    if (indexAtomicPO == null) {
                        continue; //????????????
                    }
                    status = getLinkByAutoMaticIndex(indexAtomicPO, tenantId, po.getIndexId(), nodes, relations);
                    getLinkByDriveIndex(po, indexAtomicPO, compositeIndexPO.getIndexId(), tenantId, nodes, relations, status ? "0" : "2");
                }
            }
            getLinkByComplexIndex(compositeIndexPO, nodes, status ? "0" : "2");
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) { //????????????
            IndexDerivePO deriveIndexPO = indexDAO.getDeriveIndexPO(indexId, Integer.parseInt(version), tenantId);
            IndexAtomicPO indexAtomicPO = indexDAO.getDependentAtomicIndex(deriveIndexPO.getIndexAtomicId(), tenantId);
            if (indexAtomicPO != null) {
                status = getLinkByAutoMaticIndex(indexAtomicPO, tenantId, deriveIndexPO.getIndexId(), nodes, relations);
            }
            getLinkByDriveIndex(deriveIndexPO, indexAtomicPO, null, tenantId, nodes, relations, status ? "0" : "2");
        } else if (indexType == IndexType.INDEXATOMIC.getValue()) { //????????????
            IndexAtomicPO atomicIndexPO = indexDAO.getAtomicIndexPO(indexId, Integer.parseInt(version), tenantId);
            getLinkByAutoMaticIndex(atomicIndexPO, tenantId, null, nodes, relations);
        } else {  //????????????????????????
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
        IndexLinkDto indexLinkDto = new IndexLinkDto();
        indexLinkDto.setNodes(nodes);
        indexLinkDto.setRelations(relations);
        return indexLinkDto;
    }


    public void getLinkByComplexIndex(IndexCompositePO index, List<IndexLinkEntity> nodes, String status) {
        ComplexIndexLinkNode node = new ComplexIndexLinkNode();
        String indexId = index.getIndexId();   //????????????ID
        // ??????????????????????????????
        node.setId(indexId); //??????ID
        node.setNodeName(index.getIndexName()); //????????????
        node.setIndexCode(index.getIndexIdentification());
        node.setPublishTime(index.getPublishTime());
        node.setNodeType("5"); //????????????
        node.setNodeStatus(status);//????????????????????????????????????
        node.setExpress(index.getExpression());//????????????????????????????????????
        node.setBusinessCaliber(index.getBusinessCaliber());
        node.setTechnicalCaliber(index.getTechnicalCaliber());
        nodes.add(node);
    }


    public void getLinkByDriveIndex(IndexDerivePO deriveIndex, IndexAtomicPO indexAtomicPO, String parentId, String tenantId, List<IndexLinkEntity> nodes, List<IndexLinkRelation> relations, String status) {

        DriveIndexLinkNode driverNode;
        try {
            //??????????????????????????????
            driverNode = new DriveIndexLinkNode();
            driverNode.setId(deriveIndex.getIndexId()); //??????ID
            driverNode.setNodeName(deriveIndex.getIndexName()); //????????????
            driverNode.setPublishTime(deriveIndex.getPublishTime());
            driverNode.setNodeType("4"); //????????????
            driverNode.setAtomIndexName(indexAtomicPO == null ? "" : indexAtomicPO.getIndexName()); //???????????????????????????
            driverNode.setNodeStatus(status);//????????????????????????????????????
            driverNode.setIndexCode(deriveIndex.getIndexIdentification());
            driverNode.setBusinessCaliber(deriveIndex.getBusinessCaliber());
            driverNode.setTechnicalCaliber(deriveIndex.getTechnicalCaliber());
            List<Qualifier> qualifiers = indexDAO.getModifiers(deriveIndex.getIndexId(), tenantId);
            if (!CollectionUtils.isEmpty(qualifiers)) {   //???????????????
                String collect = qualifiers.stream().map(x -> x.getName()).collect(Collectors.joining(","));
                driverNode.setQualifierName(collect);
            }
            TimelimitEntity timeLimitById = timeLimitDAO.getTimeLimitById(deriveIndex.getTimeLimitId(), tenantId);
            if (timeLimitById != null) {
                driverNode.setTimeLimitName(timeLimitById.getName()); //???????????????????????????
            }
            nodes.add(driverNode);
            if (parentId != null) {
                IndexLinkRelation relation = new IndexLinkRelation();
                relation.setFrom(driverNode.getId());
                relation.setTo(parentId);
                relations.add(relation);
            }
        } catch (Exception e) {
            LOG.error("GET DriverIndex NODE fail", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
        }
    }


    public boolean getLinkByAutoMaticIndex(IndexAtomicPO indexAtomic, String tenantId, String parentId, List<IndexLinkEntity> nodes, List<IndexLinkRelation> relations) {
        DataSourceInfo dataSourceInfo = null;
        String table = "";
        String col = "";
        try {
            if (!"hive".equals(indexAtomic.getSourceId())) {
                dataSourceInfo = dataSourceDAO.getDataSourceInfo(indexAtomic.getSourceId());
            }
            TableInfo tableInfoByTableguid = tableDAO.getTableInfoByTableguid(indexAtomic.getTableId());
            if (tableInfoByTableguid != null) {
                table = tableInfoByTableguid.getTableName();
            }
            Column columnInfoByGuid = columnDAO.getColumnInfoByGuid(indexAtomic.getColumnId());
            if (columnInfoByGuid != null) {
                col = columnInfoByGuid.getColumnName();
            }
        } catch (Exception e) {
            LOG.error("??????????????????????????????", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????" + e.getMessage());
        }
        boolean result = true;
        if (conf.getBoolean("etl.indexLink.enable")) {  //????????????????????????
            //????????????????????????????????????ETL??????
            result = getLinkByEtl(indexAtomic, tenantId, nodes, relations, dataSourceInfo, table, col);
        }
        try {
            AutoMaticIndexLinkNode autoMaticIndexLinkNode = new AutoMaticIndexLinkNode();
            autoMaticIndexLinkNode.setId(indexAtomic.getIndexId()); //??????ID
            autoMaticIndexLinkNode.setBusinessCaliber(indexAtomic.getBusinessCaliber());
            autoMaticIndexLinkNode.setNodeName(indexAtomic.getIndexName()); //????????????
            autoMaticIndexLinkNode.setIndexCode(indexAtomic.getIndexIdentification()); //????????????
            autoMaticIndexLinkNode.setPublishTime(indexAtomic.getPublishTime());
            autoMaticIndexLinkNode.setNodeType("3"); //????????????
            autoMaticIndexLinkNode.setNodeStatus(result ? "0" : "2");//????????????????????????????????????
            autoMaticIndexLinkNode.setTechnicalCaliber(indexAtomic.getTechnicalCaliber());
            autoMaticIndexLinkNode.setDataFrom(dataSourceInfo == null ? "hive" + "-" + indexAtomic.getDbName() + "-" + table + "-" + col : dataSourceInfo.getSourceName() + "-" + dataSourceInfo.getDatabase() + "-" + table + "-" + col);
            nodes.add(autoMaticIndexLinkNode);
            IndexLinkRelation relation = new IndexLinkRelation();
            relation.setFrom(autoMaticIndexLinkNode.getId());
            relation.setTo(parentId);
            relations.add(relation);
        } catch (Exception e) {
            LOG.error("GET AutoMaticIndex NODE fail", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????" + e.getMessage());
        }
        return result;
    }

    /**
     * ETL???????????????????????????????????????????????????????????????????????????????????????tree???
     *
     * @param indexAtomic
     * @param tenantId
     * @param
     * @param dataSourceInfo
     * @return
     */
    public boolean getLinkByEtl(IndexAtomicPO indexAtomic, String tenantId, List<IndexLinkEntity> nodes, List<IndexLinkRelation> relations, DataSourceInfo dataSourceInfo, String table, String col) {
        boolean resultStatus = true;
        try {
            HashMap<String, Object> hashMap = new HashMap<>();   //http header
            hashMap.put("token", AdminUtils.getSSOTicket());
            hashMap.put("User-Agent", "Chrome");
            hashMap.put("tenant-id", tenantId);
            Map<String, Object> postBody = new HashMap<>();//post body
            if (dataSourceInfo != null) {
                postBody.put("dbType", dataSourceInfo.getSourceType().toLowerCase());
                postBody.put("ip", dataSourceInfo.getIp());
                postBody.put("port", dataSourceInfo.getPort());
                if (DataSourceType.POSTGRESQL.getName().equals(dataSourceInfo.getSourceType()) || DataSourceType.SQLSERVER.getName().equals(dataSourceInfo.getSourceType())) {
                    postBody.put("dbName", dataSourceInfo.getDatabase());
                } else {
                    postBody.put("dbName", indexAtomic.getDbName());
                }
            } else {
                postBody.put("dbType", indexAtomic.getSourceId());
                postBody.put("dbName", indexAtomic.getDbName());
            }
            postBody.put("tableName", table);
            postBody.put("columnName", col);
            Gson gson = new Gson();
            String json = gson.toJson(postBody); //??????body
            String string = OKHttpClient.doPost(conf.getString("etl.indexlink.address"), hashMap, null, json);
            LOG.info("ETL return data is =>" + string);
            Map<String, Object> result = gson.fromJson(string, HashMap.class);
            Map<String, Object> dataMap = (Map) result.get("data");
            if (dataMap == null) {  //??? ETL ??????
                LOG.info("ETL return data is null => indexId=" + indexAtomic.getIndexId());
                return resultStatus;

            }
            Map<String, Object> valueMap = gson.fromJson(dataMap.get("value").toString(), HashMap.class);
            Map<String, Object> linkInfoMap = (Map<String, Object>) valueMap.get("dataLinkNodeMap"); //???????????????????????????????????????
            String root = valueMap.get("rootName").toString(); //???????????????????????????????????????
            if (linkInfoMap != null) { //????????????????????????
                Set<Map.Entry<String, Object>> entries = linkInfoMap.entrySet();//key : ??????ID??? value: ??????????????????????????????
                for (Map.Entry<String, Object> en : entries) {
                    String key = en.getKey();//??????????????????ID
                    Map<String, Object> nodeInfo = (Map<String, Object>) en.getValue();  //?????????????????????
                    EtlIndexLinkNode entity = new EtlIndexLinkNode();
                    entity.setId(key);
                    entity.setNodeName(key);
                    entity.setInstanceName(nodeInfo.get("processInstanceName") == null ? "" : nodeInfo.get("processInstanceName").toString());
                    entity.setDefinitionName(nodeInfo.get("processDefinitionName") == null ? "" : nodeInfo.get("processDefinitionName").toString());
                    entity.setNodeStatus(nodeInfo.get("state") == null ? "" : String.valueOf(((Double) nodeInfo.get("state")).longValue()));
                    if (key.equals(root) && !"0".equals(entity.getNodeStatus())) { //etl ???????????????????????????????????????????????????????????????????????????
                        resultStatus = false;
                    }
                    entity.setProjectName(nodeInfo.get("projectName") == null ? "" : nodeInfo.get("projectName").toString());
                    entity.setTypeName(nodeInfo.get("nodeType") == null ? "" : nodeInfo.get("nodeType").toString());
                    entity.setNodeType("1"); //??????????????????
                    Long endTime = null;
                    if (nodeInfo.get("endTime") != null) {
                        endTime = ((Double) nodeInfo.get("endTime")).longValue();
                    }
                    entity.setEndTime(endTime); //????????????
                    nodes.add(entity);  //??????????????????
                    List<String> preNode = (List<String>) nodeInfo.get("preTaskList");
                    for (String from : preNode) {
                        IndexLinkRelation relation = new IndexLinkRelation();
                        relation.setFrom(from);
                        relation.setTo(key);
                        relations.add(relation);
                    }
                }
                //etlNode link to autoIndex
                IndexLinkRelation relation = new IndexLinkRelation();
                relation.setFrom(root);
                relation.setTo(indexAtomic.getIndexId());
                relations.add(relation);
            }
            return resultStatus;  //??????????????????????????????
        } catch (Exception e) {
            LOG.error("GET ETL NODE fail", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
        }
    }


    @Override
    public IndexInfoDTO getIndexInfo(String indexId, int indexType, int version, int categoryType, String tenantId) {
        IndexInfoDTO indexInfoDTO = null;
        if (indexType == IndexType.INDEXATOMIC.getValue()) {  //????????????
            IndexInfoPO indexInfoPO = indexDAO.getAtomicIndexInfoPO(indexId, version, categoryType, tenantId);
            if (!Objects.isNull(indexInfoPO)) {
                indexInfoDTO = BeanMapper.map(indexInfoPO, IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXATOMIC.getValue());
            }
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {  //????????????
            IndexInfoPO indexInfoPO = indexDAO.getDeriveIndexInfoPO(indexId, version, categoryType, tenantId);
            if (!Objects.isNull(indexInfoPO)) {
                indexInfoDTO = BeanMapper.map(indexInfoPO, IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXDERIVE.getValue());
                //???????????????????????????
                IndexAtomicPO indexAtomicPO = indexDAO.getDependentAtomicIndex(indexInfoPO.getIndexAtomicId(), tenantId);
                List<IndexAtomicPO> indexAtomicPOs = new ArrayList<>();
                indexAtomicPOs.add(indexAtomicPO);
                if (!CollectionUtils.isEmpty(indexAtomicPOs)) {
                    List<DependentIndex> dependentIndices = indexAtomicPOs.stream().map(x -> BeanMapper.map(x, DependentIndex.class)).collect(Collectors.toList());
                    indexInfoDTO.setDependentIndices(dependentIndices);
                } else {
                    indexInfoDTO.setDependentIndices(new ArrayList<>());
                }
                //???????????????
                List<Qualifier> qualifiers = indexDAO.getModifiers(indexInfoPO.getIndexId(), tenantId);
                if (!CollectionUtils.isEmpty(qualifiers)) {
                    List<Modifier> modifiers = qualifiers.stream().map(x -> BeanMapper.map(x, Modifier.class)).collect(Collectors.toList());
                    indexInfoDTO.setModifiers(modifiers);
                } else {
                    indexInfoDTO.setModifiers(new ArrayList<>());
                }
            }
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) { //????????????
            IndexInfoPO indexInfoPO = indexDAO.getCompositeIndexInfoPO(indexId, version, categoryType, tenantId);
            if (!Objects.isNull(indexInfoPO)) {
                indexInfoDTO = BeanMapper.map(indexInfoPO, IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXCOMPOSITE.getValue());
                //???????????????????????????
                List<IndexDerivePO> indexDerivePOS = indexDAO.getDependentDeriveIndex(indexInfoPO.getIndexId(), tenantId);
                if (!CollectionUtils.isEmpty(indexDerivePOS)) {
                    List<DependentIndex> dependentIndices = indexDerivePOS.stream().map(x -> BeanMapper.map(x, DependentIndex.class)).collect(Collectors.toList());
                    indexInfoDTO.setDependentIndices(dependentIndices);
                } else {
                    indexInfoDTO.setDependentIndices(new ArrayList<>());
                }
            }
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
        if (!Objects.isNull(indexInfoDTO)) {
            //hive?????????????????????
            if ("hive".equalsIgnoreCase(indexInfoDTO.getSourceId())) {
                indexInfoDTO.setSourceName(DataSourceType.HIVE.getName());
            }
            //?????????????????????
            List<User> users = approveDAO.getApproveUsers(indexInfoDTO.getApprovalGroupId());
            if (!CollectionUtils.isEmpty(users)) {
                List<ApprovalGroupMember> approvalGroupMembers = users.stream().map(x -> BeanMapper.map(x, ApprovalGroupMember.class)).collect(Collectors.toList());
                indexInfoDTO.setApprovalGroupMembers(approvalGroupMembers);
            } else {
                indexInfoDTO.setApprovalGroupMembers(new ArrayList<>());
            }
        }
        return indexInfoDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void indexSendApprove(List<PublishIndexDTO> dtoList, String tenantId) throws AtlasBaseException {
        List<ApproveItem> approveItems = new ArrayList<>();
        for (PublishIndexDTO pid : dtoList) {
            if (Objects.isNull(pid.getIndexId()) || Objects.isNull(pid.getIndexName()) || Objects.isNull(ApproveType.getApproveTypeByCode(pid.getApproveType())) || Objects.isNull(pid.getApprovalGroupId())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????");
            }
            ApproveItem approveItem = new ApproveItem();
            approveItem.setId(UUID.randomUUID().toString());
            approveItem.setObjectId(pid.getIndexId());
            approveItem.setObjectName(pid.getIndexName());
            approveItem.setBusinessType(pid.getIndexType() + "");
            approveItem.setApproveType(pid.getApproveType());
            approveItem.setApproveGroup(pid.getApprovalGroupId());
            User user = AdminUtils.getUserData();
            approveItem.setSubmitter(user.getUserId());
            Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
            approveItem.setCommitTime(timeStamp);
            approveItem.setModuleId(ModuleEnum.NORMDESIGN.getId() + "");
            approveItem.setVersion(pid.getVersion());
            approveItem.setTenantId(tenantId);
            approveItems.add(approveItem);
        }
        batchSendApprove(approveItems, tenantId);

    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSendApprove(List<ApproveItem> approveItems, String tenantId) {
        if (!CollectionUtils.isEmpty(approveItems)) {
            for (ApproveItem approveItem : approveItems) {
                approveServiceImpl.addApproveItem(approveItem);
                indexDAO.updatePublishInfo(approveItem, tenantId, IndexState.APPROVAL.getValue());
            }
        }
    }


    @Override
    public List<IndexInfoDTO> publishHistory(String indexId, PageQueryDTO pageQueryDTO, int categoryType, String tenantId) {

        int indexType = pageQueryDTO.getIndexType();
        int offset = pageQueryDTO.getOffset();
        int limit = pageQueryDTO.getLimit();
        List<IndexInfoDTO> indexInfoDTOs = null;
        if (indexType == IndexType.INDEXATOMIC.getValue()) {
            List<IndexInfoPO> indexInfoPOs = indexDAO.getAtomicIndexHistory(indexId, categoryType, offset, limit, tenantId);
            indexInfoDTOs = indexInfoPOs.stream().map(x -> {
                IndexInfoDTO indexInfoDTO = BeanMapper.map(x, IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXATOMIC.getValue());
                return indexInfoDTO;
            }).collect(Collectors.toList());
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
            List<IndexInfoPO> indexInfoPOs = indexDAO.getDeriveIndexHistory(indexId, categoryType, offset, limit, tenantId);
            if (!CollectionUtils.isEmpty(indexInfoPOs)) {
                List<String> dependentIndexIds = indexInfoPOs.stream().map(x -> x.getIndexAtomicId()).distinct().collect(Collectors.toList());
                //????????????????????????
                List<IndexAtomicPO> indexAtomicPOS = indexDAO.getAtomicIndexInfoPOs(dependentIndexIds, tenantId);
                Map<String, DependentIndex> maps = indexAtomicPOS.stream().map(x -> BeanMapper.map(x, DependentIndex.class)).collect(Collectors.toMap(DependentIndex::getIndexId, Function.identity(), (key1, key2) -> key2));
                //????????????????????????
                List<Qualifier> qualifiers = indexDAO.getModifiers(indexId, tenantId);
                List<Modifier> modifiers = null;
                if (!CollectionUtils.isEmpty(qualifiers)) {
                    modifiers = qualifiers.stream().map(x -> BeanMapper.map(x, Modifier.class)).collect(Collectors.toList());
                }
                List<Modifier> finalModifiers = modifiers;
                indexInfoDTOs = indexInfoPOs.stream().map(x -> {
                    IndexInfoDTO indexInfoDTO = BeanMapper.map(x, IndexInfoDTO.class);
                    indexInfoDTO.setIndexType(IndexType.INDEXDERIVE.getValue());
                    DependentIndex dependentIndex = maps.get(x.getIndexAtomicId());
                    if (!Objects.isNull(dependentIndex)) {
                        List<DependentIndex> dependentIndices = new ArrayList<>();
                        dependentIndices.add(dependentIndex);
                        //??????????????????
                        indexInfoDTO.setDependentIndices(dependentIndices);
                    }
                    //???????????????
                    indexInfoDTO.setModifiers(finalModifiers);
                    return indexInfoDTO;
                }).collect(Collectors.toList());
            }
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
            List<IndexInfoPO> indexInfoPOs = indexDAO.getCompositeIndexHistory(indexId, categoryType, offset, limit, tenantId);
            if (!CollectionUtils.isEmpty(indexInfoPOs)) {
                List<IndexDerivePO> indexDerivePOS = indexDAO.getDependentDeriveIndex(indexId, tenantId);
                List<DependentIndex> dependentIndices = null;
                if (!CollectionUtils.isEmpty(indexDerivePOS)) {
                    dependentIndices = indexDerivePOS.stream().map(x -> BeanMapper.map(x, DependentIndex.class)).collect(Collectors.toList());
                }
                List<DependentIndex> finalDependentIndices = dependentIndices;
                indexInfoDTOs = indexInfoPOs.stream().map(x -> {
                    IndexInfoDTO indexInfoDTO = BeanMapper.map(x, IndexInfoDTO.class);
                    indexInfoDTO.setIndexType(IndexType.INDEXCOMPOSITE.getValue());
                    indexInfoDTO.setDependentIndices(finalDependentIndices);
                    return indexInfoDTO;
                }).collect(Collectors.toList());
            }
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
        if (!CollectionUtils.isEmpty(indexInfoDTOs)) {
            List<String> approvalGroupIds = indexInfoDTOs.stream().map(x -> x.getApprovalGroupId()).distinct().collect(Collectors.toList());
            Map<String, List<ApprovalGroupMember>> usersMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(approvalGroupIds)) {
                for (String str : approvalGroupIds) {
                    List<User> approveUsers = approveDAO.getApproveUsers(str);
                    if (!CollectionUtils.isEmpty(approveUsers)) {
                        List<ApprovalGroupMember> approvalGroupMembers = approveUsers.stream().map(u -> BeanMapper.map(u, ApprovalGroupMember.class)).collect(Collectors.toList());
                        usersMap.put(str, approvalGroupMembers);
                    }
                }
            }
            //?????????????????????
            indexInfoDTOs.forEach(x -> {
                String approvalGroupId = x.getApprovalGroupId();
                if (StringUtils.isNotEmpty(approvalGroupId)) {
                    List<ApprovalGroupMember> approvalGroupMembers = usersMap.get(approvalGroupId);
                    if (!CollectionUtils.isEmpty(approvalGroupMembers)) {
                        x.setApprovalGroupMembers(approvalGroupMembers);
                    }
                }
            });
        }
        return indexInfoDTOs;
    }

    @Override
    public List<IndexInfoDTO> pageQuery(PageQueryDTO pageQueryDTO, int categoryType, String tenantId) throws Exception {
        List<IndexInfoPO> indexInfoPOS = indexDAO.pageQuery(pageQueryDTO, categoryType, tenantId);
        List<CategoryPrivilege> categoryPrivileges = dataManageService.getAllByUserGroup(categoryType, tenantId);
        Map<String, Boolean> contentEdits = new HashMap<>();

        if (!CollectionUtils.isEmpty(categoryPrivileges)) {
            categoryPrivileges.forEach(x -> {
                String guid = x.getGuid();
                boolean contentEdit = x.getPrivilege().isCreateRelation();
                contentEdits.put(guid, contentEdit);
            });
        }
        List<IndexInfoDTO> indexInfoDTOS = null;
        if (!CollectionUtils.isEmpty(indexInfoPOS)) {
            indexInfoDTOS = indexInfoPOS.stream().map(x -> {
                IndexInfoDTO indexInfoDTO = BeanMapper.map(x, IndexInfoDTO.class);
                if (contentEdits.size() > 0) {
                    indexInfoDTO.setContentEdit(contentEdits.get(indexInfoDTO.getIndexFieldId()));
                }
                return indexInfoDTO;
            }).collect(Collectors.toList());
        }
        return indexInfoDTOS;
    }


    @Override
    public List<String> getIndexIds(List<String> indexFields, String tenantId, int state1, int state2) {
        List<String> indexIds = indexDAO.getIndexIds(indexFields, tenantId, state1, state2);
        return indexIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndexByIndexFieldId(List<String> guids, String tenantId) {
        indexDAO.deleteAtomicByIndexFieldIds(guids, tenantId);
        indexDAO.deleteDeriveByIndexFieldIds(guids, tenantId);
        indexDAO.deleteCompositeByIndexFieldIds(guids, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeIndexToAnotherIndexField(List<String> sourceGuids, String tenantId, String targetGuid) {
        indexDAO.updateAtomicIndexFieldIds(sourceGuids, tenantId, targetGuid);
        indexDAO.updateDeriveIndexFieldIds(sourceGuids, tenantId, targetGuid);
        indexDAO.updateCompositeIndexFieldIds(sourceGuids, tenantId, targetGuid);
    }

    @Override
    public Object getObjectDetail(String objectId, String type, int version, String tenantId) {
        IndexInfoDTO indexInfo = null;
        if (StringUtils.isNumeric(type)) {
            int indexType = Integer.parseInt(type);
            indexInfo = getIndexInfo(objectId, indexType, version, 5, tenantId);
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
        return indexInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeObjectStatus(String approveResult, String tenantId, List<ApproveItem> items) {
        if (!CollectionUtils.isEmpty(items)) {
            for (ApproveItem item : items) {
                int indexType = Integer.parseInt(item.getBusinessType());
                String objectId = item.getObjectId();
                int version = item.getVersion();
                String approveType = item.getApproveType();

                if (approveType.equals(ApproveType.PUBLISH.getCode())) {
                    //??????
                    if (approveResult.equals(ApproveOperate.APPROVE.getCode())) {
                        //??????
                        editIndexState(objectId, indexType, version, tenantId, IndexState.PUBLISH.getValue());
                    } else if (approveResult.equals(ApproveOperate.REJECTED.getCode())||approveResult.equals(ApproveOperate.CANCEL.getCode())) {
                        //??????,??????????????????
                        if (version == 0) {
                            editIndexState(objectId, indexType, version, tenantId, IndexState.CREATE.getValue());
                        } else {
                            editIndexState(objectId, indexType, version, tenantId, IndexState.OFFLINE.getValue());
                        }
                    }
                } else if (approveType.equals(ApproveType.OFFLINE.getCode())) {
                    //??????
                    if (approveResult.equals(ApproveOperate.APPROVE.getCode())) {
                        //??????
                        try {
                            offlineApprove(objectId, indexType, version, tenantId, IndexState.OFFLINE.getValue());
                        } catch (Exception e) {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
                        }
                    } else if (approveResult.equals(ApproveOperate.REJECTED.getCode())) {
                        //??????,??????????????????
                        editIndexState(objectId, indexType, version, tenantId, IndexState.PUBLISH.getValue());
                    }
                }
            }
        }
    }

    private void editIndexState(String indexId, int indexType, int version, String tenantId, int state) {
        if (indexType == IndexType.INDEXATOMIC.getValue()) {
            indexDAO.editAtomicState(indexId, version, tenantId, state);
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
            indexDAO.editDeriveState(indexId, version, tenantId, state);
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
            indexDAO.editCompositeState(indexId, version, tenantId, state);
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void offlineApprove(String indexId, int indexType, int version, String tenantId, int state) throws SQLException {
        editIndexState(indexId, indexType, version, tenantId, state);
        //??????+1
        if (indexType == IndexType.INDEXATOMIC.getValue()) {
            IndexAtomicPO indexAtomicPO = indexDAO.getAtomicIndexPO(indexId, version, tenantId);
            indexAtomicPO.setVersion(indexAtomicPO.getVersion() + 1);
            indexDAO.addAtomicIndex(indexAtomicPO);
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
            IndexDerivePO indexDerivePO = indexDAO.getDeriveIndexPO(indexId, version, tenantId);
            indexDerivePO.setVersion(indexDerivePO.getVersion() + 1);
            indexDAO.addDeriveIndex(indexDerivePO);
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
            IndexCompositePO indexCompositePO = indexDAO.getCompositeIndexPO(indexId, version, tenantId);
            indexCompositePO.setVersion(indexCompositePO.getVersion() + 1);
            indexDAO.addCompositeIndex(indexCompositePO);
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
    }


    /**
     * ????????????excel
     *
     * @param tenantId
     * @return
     */
    public XSSFWorkbook downLoadExcelDerive(String tenantId) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet("????????????");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellType(CellType.STRING);
            cell.setCellValue("??????????????????*");
            row.createCell(1).setCellValue("????????????");
            row.createCell(2).setCellValue("?????????(?????????-??????)");
            row.createCell(3).setCellValue("????????????*");
            row.createCell(4).setCellValue("????????????*");
            row.createCell(5).setCellValue("??????");
            row.createCell(6).setCellValue("??????????????????");
            row.createCell(7).setCellValue("?????????*");
            row.createCell(8).setCellValue("????????????*");
            row.createCell(9).setCellValue("???????????????*");
            row.createCell(10).setCellValue("????????????");
            row.createCell(11).setCellValue("???????????????");
            row.createCell(12).setCellValue("????????????*");

            //??????????????????
            this.setXSSFDataValidation(this.getAtomicName(tenantId), 0, 0, sheet, workbook);
            //????????????
            this.setXSSFDataValidation(this.getTimeLimitName(tenantId), 1, 1, sheet, workbook);

            //???????????????????????????
            CellRangeAddressList regions = new CellRangeAddressList(0, 1000, 6, 6);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"???", "???"});
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);

            //????????????????????????
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 9, 9, sheet, workbook);

            //????????????????????????
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 11, 11, sheet, workbook);

            //?????????????????????
            this.setXSSFDataValidation(this.getApproveGroupByModuleId(tenantId), 12, 12, sheet, workbook);
        } catch (Exception e) {
            LOG.error("downLoadExcel exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, "??????????????????");
        }
        return workbook;
    }


    /**
     * ????????????excel
     *
     * @param tenantId
     * @return
     */
    public XSSFWorkbook downLoadExcelComposite(String tenantId) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet("????????????");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellType(CellType.STRING);
            cell.setCellValue("??????????????????(?????????-??????)*");
            row.createCell(1).setCellValue("????????????*");
            row.createCell(2).setCellValue("????????????*");
            row.createCell(3).setCellValue("??????");
            row.createCell(4).setCellValue("??????????????????");
            row.createCell(5).setCellValue("?????????*");
            row.createCell(6).setCellValue("???????????????*");
            row.createCell(7).setCellValue("????????????*");
            row.createCell(8).setCellValue("???????????????*");
            row.createCell(9).setCellValue("????????????");
            row.createCell(10).setCellValue("???????????????");
            row.createCell(11).setCellValue("????????????*");

            //???????????????????????????
            CellRangeAddressList regions = new CellRangeAddressList(0, 1000, 4, 4);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"???", "???"});
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);

            //????????????????????????
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 8, 8, sheet, workbook);

            //????????????????????????
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 10, 10, sheet, workbook);

            //?????????????????????
            this.setXSSFDataValidation(this.getApproveGroupByModuleId(tenantId), 11, 11, sheet, workbook);
        } catch (Exception e) {
            LOG.error("downLoadExcel exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, "??????????????????");
        }
        return workbook;
    }

    private void setXSSFDataValidation(List<String> list, Integer firstCol, Integer lastCol, XSSFSheet sheet, XSSFWorkbook workbook) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
            int sheetTotal = workbook.getNumberOfSheets();
            //????????????sheet???
            String hiddenSheetName = "hiddenSheet" + firstCol;
            XSSFSheet hiddenSheet = workbook.createSheet(hiddenSheetName);
            //????????????????????????
            for (int i = 0; i < list.size(); i++) {
                XSSFRow row1 = hiddenSheet.createRow(i);
                XSSFCell cell1 = row1.createCell(0);
                cell1.setCellValue(list.get(i));
            }
            String strFormula = hiddenSheetName + "!$A$1:$A$" + list.size();
            XSSFDataValidationConstraint constraint = new XSSFDataValidationConstraint(DataValidationConstraint.ValidationType.LIST, strFormula);
            // ????????????????????????????????????????????????,?????????????????????????????????????????????????????????????????????
            CellRangeAddressList regions = new CellRangeAddressList(1, 1000, firstCol, lastCol);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(constraint, regions);
            validation.setShowErrorBox(false);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);
            //????????????sheet????????????
            workbook.setSheetHidden(sheetTotal, true);
        }
    }

    /**
     * ????????????excel
     *
     * @param tenantId
     * @return
     */
    public XSSFWorkbook downLoadExcelAtom(String tenantId) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet("????????????");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellType(CellType.STRING);
            cell.setCellValue("????????????*");
            row.createCell(1).setCellValue("????????????*");
            row.createCell(2).setCellValue("??????");
            row.createCell(3).setCellValue("??????????????????");
            row.createCell(4).setCellValue("?????????*");
            row.createCell(5).setCellValue("?????????*");
            row.createCell(6).setCellValue("?????????*");
            row.createCell(7).setCellValue("?????????*");
            row.createCell(8).setCellValue("??????*");
            row.createCell(9).setCellValue("????????????*");
            row.createCell(10).setCellValue("???????????????*");
            row.createCell(11).setCellValue("????????????");
            row.createCell(12).setCellValue("???????????????");
            row.createCell(13).setCellValue("????????????*");

            //???????????????????????????
            CellRangeAddressList regions = new CellRangeAddressList(0, 1000, 3, 3);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"???", "???"});
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);

            List<String> list = new ArrayList<>();
            //??????????????????
            this.setXSSFDataValidation(this.getDataSourceByTenantId(tenantId), 5, 5, sheet, workbook);

            //????????????????????????
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 10, 10, sheet, workbook);

            //????????????????????????
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 12, 12, sheet, workbook);

            //?????????????????????
            this.setXSSFDataValidation(this.getApproveGroupByModuleId(tenantId), 13, 13, sheet, workbook);
        } catch (Exception e) {
            LOG.error("downLoadExcel exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, "??????????????????");
        }
        return workbook;
    }

    private List<String> getDataSourceByTenantId(String tenantId) {
        List<String> list = new ArrayList<>();
        List<OptionalDataSourceDTO> optionalDataSourceDTOList = this.getOptionalDataSource(tenantId);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(optionalDataSourceDTOList)) {
            return list;
        }
        optionalDataSourceDTOList.stream().forEach(optionalDataSourceDTO -> list.add(optionalDataSourceDTO.getSourceName()));
        return list;
    }

    private List<String> getUserListByTenantId(String tenantId) {
        List<String> list = new ArrayList<>();
        PageResult<User> pageResult = usersService.getUserListV2(tenantId, new Parameters());
        List<User> userList = pageResult.getLists();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(userList)) {
            return list;
        }
        userList.stream().forEach(user -> list.add(user.getUsername()));
        return list;
    }

    private List<String> getApproveGroupByModuleId(String tenantId) {
        List<String> list = new ArrayList<>();
        ApproveGroupParas approveGroupParas = new ApproveGroupParas();
        approveGroupParas.setModuleId("13");
        approveGroupParas.setLimit(-1);
        approveGroupParas.setSortBy("createTime");
        approveGroupParas.setOrder("desc");
        PageResult<ApproveGroupListAndSearchResult> pageResult = approveGroupService.getApproveGroupByModuleId(approveGroupParas, tenantId);
        List<ApproveGroupListAndSearchResult> approveGroupListAndSearchResultList = pageResult.getLists();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(approveGroupListAndSearchResultList)) {
            return list;
        }
        approveGroupListAndSearchResultList.stream().forEach(ApproveGroupListAndSearchResult -> list.add(ApproveGroupListAndSearchResult.getName()));
        return list;
    }

    /**
     * ????????????
     *
     * @param tenantId
     * @return
     */
    private List<String> getAtomicName(String tenantId) {
        List<String> list = new ArrayList<>();
        List<OptionalIndexDTO> optionalIndexDTOList = this.getOptionalIndex(IndexType.INDEXATOMIC.getValue(), CATEGORY_TYPE, tenantId);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(optionalIndexDTOList)) {
            return list;
        }
        optionalIndexDTOList.stream().forEach(optionalIndexDTO -> list.add(optionalIndexDTO.getIndexName()));
        return list;
    }

    /**
     * ????????????
     *
     * @param tenantId
     * @return
     */
    private List<String> getTimeLimitName(String tenantId) {
        List<String> list = new ArrayList<>();
        TimeLimitSearch timeLimitSearch = new TimeLimitSearch();
        timeLimitSearch.setLimit(-1);
        PageResult<TimelimitEntity> pageResult = timeLimitServiceImp.search(timeLimitSearch, tenantId);
        List<TimelimitEntity> timelimitEntityList = pageResult.getLists();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(timelimitEntityList)) {
            return list;
        }
        timelimitEntityList.stream().forEach(timelimitEntity -> list.add(timelimitEntity.getName()));
        return list;
    }

    @Override
    public String uploadExcelAtom(String tenantId, File file) throws Exception {
        this.checkDataAtom(this.getAtomIndexData(file));
        return ExportDataPathUtils.transferTo(file);
    }

    @Override
    public String uploadExcelDerive(String tenantId, File file) throws Exception {
        this.checkDataDerive(this.getDeriveIndexData(file));
        return ExportDataPathUtils.transferTo(file);
    }

    @Override
    public String uploadExcelComposite(String tenantId, File file) throws Exception {
        this.checkDataComposite(this.getCompositeIndexData(file));
        return ExportDataPathUtils.transferTo(file);
    }

    /**
     * ????????????????????????
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<IndexTemplateAtomDTO> getAtomIndexData(File file) throws Exception {
        List<String[]> list = PoiExcelUtils.readExcelFile(file, 0, 14);
        List<IndexTemplateAtomDTO> indexTemplateAtomList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            IndexTemplateAtomDTO indexTemplateAtom = new IndexTemplateAtomDTO();
            indexTemplateAtom.setName(list.get(i)[0]);
            indexTemplateAtom.setIdentification(list.get(i)[1]);
            indexTemplateAtom.setDescription(list.get(i)[2]);
            indexTemplateAtom.setCentral(list.get(i)[3]);
            indexTemplateAtom.setField(list.get(i)[4]);
            indexTemplateAtom.setSource(list.get(i)[5]);
            indexTemplateAtom.setDbName(list.get(i)[6]);
            indexTemplateAtom.setTableName(list.get(i)[7]);
            indexTemplateAtom.setColumnName(list.get(i)[8]);
            indexTemplateAtom.setBusinessCaliber(list.get(i)[9]);
            indexTemplateAtom.setBusinessLeader(list.get(i)[10]);
            indexTemplateAtom.setTechnicalCaliber(list.get(i)[11]);
            indexTemplateAtom.setTechnicalLeader(list.get(i)[12]);
            indexTemplateAtom.setApprove(list.get(i)[13]);
            if (i == 0) {
                if (!indexTemplateAtom.checkTitle()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
                }
                continue;
            }
            String checkResult = indexTemplateAtom.checkFieldsIsNull();
            if (StringUtils.isNotBlank(checkResult)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, checkResult);
            }
            indexTemplateAtomList.add(indexTemplateAtom);
        }
        return indexTemplateAtomList;
    }

    /**
     * ??????????????????-??????
     */
    private void checkDataAtom(List<IndexTemplateAtomDTO> indexTemplateAtomList) throws Exception {
        if (CollectionUtils.isEmpty(indexTemplateAtomList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
        }
        Set<String> nameSet = new HashSet<>();
        Set<String> identificationSet = new HashSet<>();
        indexTemplateAtomList.stream().forEach(indexTemplateAtom -> {
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateAtom.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            if (indexTemplateAtom.getName().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (!Pattern.matches("^[a-z0-9_]+$", indexTemplateAtom.getIdentification())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????????????????????????????_???");
            }
            if (indexTemplateAtom.getIdentification().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (indexTemplateAtom.getDescription().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????300???");
            }
            if (indexTemplateAtom.getBusinessCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateAtom.getBusinessCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            if (indexTemplateAtom.getTechnicalCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (StringUtils.isNotBlank(indexTemplateAtom.getTechnicalCaliber()) && !Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateAtom.getTechnicalCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            nameSet.add(indexTemplateAtom.getName());
            identificationSet.add(indexTemplateAtom.getIdentification());
        });
        if (indexTemplateAtomList.size() != nameSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (indexTemplateAtomList.size() != identificationSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (indexTemplateAtomList.size() > 100) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????100");
        }
    }


    /**
     * ????????????????????????
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<IndexTemplateDeriveDTO> getDeriveIndexData(File file) throws Exception {
        List<IndexTemplateDeriveDTO> indexTemplateDeriveDTOList = new ArrayList<>();
        List<String[]> list = PoiExcelUtils.readExcelFile(file, 0, 13);
        for (int i = 0; i < list.size(); i++) {
            IndexTemplateDeriveDTO indexTemplateDeriveDTO = new IndexTemplateDeriveDTO();
            indexTemplateDeriveDTO.setIndexAtomicName(list.get(i)[0]);
            indexTemplateDeriveDTO.setTimeLimitName(list.get(i)[1]);
            indexTemplateDeriveDTO.setModifiersName(list.get(i)[2]);
            indexTemplateDeriveDTO.setIndexName(list.get(i)[3]);
            indexTemplateDeriveDTO.setIndexIdentification(list.get(i)[4]);
            indexTemplateDeriveDTO.setDescription(list.get(i)[5]);
            if ("???".equals(list.get(i)[6])) {
                indexTemplateDeriveDTO.setCentral(true);
            } else {
                indexTemplateDeriveDTO.setCentral(false);
            }
            indexTemplateDeriveDTO.setIndexFieldName(list.get(i)[7]);
            indexTemplateDeriveDTO.setBusinessCaliber(list.get(i)[8]);
            indexTemplateDeriveDTO.setBusinessLeaderName(list.get(i)[9]);
            indexTemplateDeriveDTO.setTechnicalCaliber(list.get(i)[10]);
            indexTemplateDeriveDTO.setTechnicalLeaderName(list.get(i)[11]);
            indexTemplateDeriveDTO.setApprovalGroupName(list.get(i)[12]);
            if (i == 0) {
                if (!indexTemplateDeriveDTO.checkTitle()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
                }
                continue;
            }
            String checkResult = indexTemplateDeriveDTO.checkFieldsIsNull();
            if (StringUtils.isNotBlank(checkResult)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, checkResult);
            }
            indexTemplateDeriveDTOList.add(indexTemplateDeriveDTO);
        }
        return indexTemplateDeriveDTOList;
    }

    /**
     * ??????????????????-??????
     *
     * @param indexTemplateDeriveDTOList
     */
    private void checkDataDerive(List<IndexTemplateDeriveDTO> indexTemplateDeriveDTOList) throws Exception {
        if (CollectionUtils.isEmpty(indexTemplateDeriveDTOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
        }
        Set<String> nameSet = new HashSet<>();
        Set<String> identificationSet = new HashSet<>();
        indexTemplateDeriveDTOList.stream().forEach(indexTemplateDeriveDTO -> {
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateDeriveDTO.getIndexName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            if (indexTemplateDeriveDTO.getIndexName().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (!Pattern.matches("^[a-z0-9_]+$", indexTemplateDeriveDTO.getIndexIdentification())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????????????????????????????_???");
            }
            if (indexTemplateDeriveDTO.getIndexIdentification().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (indexTemplateDeriveDTO.getDescription().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????300???");
            }
            if (indexTemplateDeriveDTO.getBusinessCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateDeriveDTO.getBusinessCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            if (indexTemplateDeriveDTO.getTechnicalCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (StringUtils.isNotBlank(indexTemplateDeriveDTO.getTechnicalCaliber()) && !Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateDeriveDTO.getTechnicalCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            nameSet.add(indexTemplateDeriveDTO.getIndexName());
            identificationSet.add(indexTemplateDeriveDTO.getIndexIdentification());
        });
        if (indexTemplateDeriveDTOList.size() != nameSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (indexTemplateDeriveDTOList.size() != identificationSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (indexTemplateDeriveDTOList.size() > 100) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????100");
        }
    }


    /**
     * ????????????????????????
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<IndexTemplateCompositeDTO> getCompositeIndexData(File file) throws Exception {
        List<String[]> list = PoiExcelUtils.readExcelFile(file, 0, 12);
        List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            IndexTemplateCompositeDTO indexTemplateCompositeDTO = new IndexTemplateCompositeDTO();
            indexTemplateCompositeDTO.setDependentIndicesName(list.get(i)[0]);
            indexTemplateCompositeDTO.setIndexName(list.get(i)[1]);
            indexTemplateCompositeDTO.setIndexIdentification(list.get(i)[2]);
            indexTemplateCompositeDTO.setDescription(list.get(i)[3]);
            if ("???".equals(list.get(i)[4])) {
                indexTemplateCompositeDTO.setCentral(true);
            } else {
                indexTemplateCompositeDTO.setCentral(false);
            }
            indexTemplateCompositeDTO.setIndexFieldName(list.get(i)[5]);
            indexTemplateCompositeDTO.setExpression(list.get(i)[6]);
            indexTemplateCompositeDTO.setBusinessCaliber(list.get(i)[7]);
            indexTemplateCompositeDTO.setBusinessLeaderName(list.get(i)[8]);
            indexTemplateCompositeDTO.setTechnicalCaliber(list.get(i)[9]);
            indexTemplateCompositeDTO.setTechnicalLeaderName(list.get(i)[10]);
            indexTemplateCompositeDTO.setApprovalGroupName(list.get(i)[11]);
            if (i == 0) {
                if (!indexTemplateCompositeDTO.checkTitle()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
                }
                continue;
            }
            String checkResult = indexTemplateCompositeDTO.checkFieldsIsNull();
            if (StringUtils.isNotBlank(checkResult)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, checkResult);
            }
            indexTemplateCompositeDTOList.add(indexTemplateCompositeDTO);
        }
        return indexTemplateCompositeDTOList;
    }

    /**
     * ??????????????????-??????
     *
     * @param indexTemplateCompositeDTOList
     */
    private void checkDataComposite(List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) throws Exception {
        if (CollectionUtils.isEmpty(indexTemplateCompositeDTOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
        }
        Set<String> nameSet = new HashSet<>();
        Set<String> identificationSet = new HashSet<>();
        indexTemplateCompositeDTOList.stream().forEach(indexTemplateCompositeDTO -> {
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateCompositeDTO.getIndexName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            if (indexTemplateCompositeDTO.getIndexName().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (!Pattern.matches("^[a-z0-9_]+$", indexTemplateCompositeDTO.getIndexIdentification())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????????????????????????????_???");
            }
            if (indexTemplateCompositeDTO.getIndexIdentification().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (indexTemplateCompositeDTO.getDescription().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????300???");
            }
            if (indexTemplateCompositeDTO.getExpression().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????300???");
            }
            if (indexTemplateCompositeDTO.getBusinessCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateCompositeDTO.getBusinessCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            if (indexTemplateCompositeDTO.getTechnicalCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (StringUtils.isNotBlank(indexTemplateCompositeDTO.getTechnicalCaliber()) && !Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateCompositeDTO.getTechnicalCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            nameSet.add(indexTemplateCompositeDTO.getIndexName());
            identificationSet.add(indexTemplateCompositeDTO.getIndexIdentification());
        });
        if (indexTemplateCompositeDTOList.size() != nameSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (indexTemplateCompositeDTOList.size() != identificationSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (indexTemplateCompositeDTOList.size() > 100) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????100");
        }
    }

    /**
     * ????????????????????????
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void importBatchAtomIndex(File file, String tenantId) throws Exception {
        if (!file.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        List<IndexTemplateAtomDTO> indexTemplateAtomDTOList = this.getAtomIndexData(file);
        List<IndexInfoDTO> indexInfoDTOList = new ArrayList<>();
        //??????????????????
        List<String> nameList = new ArrayList<>();
        List<String> identificationList = new ArrayList<>();
        Set<String> fieldSet = new HashSet<>(16);
        Set<String> groupSet = new HashSet<>(16);

        indexTemplateAtomDTOList.stream().forEach(indexTemplateAtomDTO -> {
            nameList.add(indexTemplateAtomDTO.getName());
            identificationList.add(indexTemplateAtomDTO.getIdentification());
            fieldSet.add(indexTemplateAtomDTO.getField());
            groupSet.add(indexTemplateAtomDTO.getApprove());
            IndexInfoDTO indexInfoDTO = new IndexInfoDTO();
            indexInfoDTO.setIndexName(indexTemplateAtomDTO.getName());
            indexInfoDTO.setIndexIdentification(indexTemplateAtomDTO.getIdentification());
            indexInfoDTO.setDescription(indexTemplateAtomDTO.getDescription());
            if ("???".equals(indexTemplateAtomDTO.getCentral())) {
                indexInfoDTO.setCentral(true);
            } else {
                indexInfoDTO.setCentral(false);
            }
            indexInfoDTO.setIndexFieldName(indexTemplateAtomDTO.getField());
            indexInfoDTO.setSourceName(indexTemplateAtomDTO.getSource());
            indexInfoDTO.setDbName(indexTemplateAtomDTO.getDbName());
            indexInfoDTO.setTableName(indexTemplateAtomDTO.getTableName());
            indexInfoDTO.setColumnName(indexTemplateAtomDTO.getColumnName());
            indexInfoDTO.setBusinessCaliber(indexTemplateAtomDTO.getBusinessCaliber());
            indexInfoDTO.setBusinessLeaderName(indexTemplateAtomDTO.getBusinessLeader());
            indexInfoDTO.setTechnicalCaliber(indexTemplateAtomDTO.getTechnicalCaliber());
            indexInfoDTO.setTechnicalLeaderName(indexTemplateAtomDTO.getTechnicalLeader());
            indexInfoDTO.setApprovalGroupName(indexTemplateAtomDTO.getApprove());
            indexInfoDTOList.add(indexInfoDTO);
        });
        List<IndexAtomicPO> indexAtomicPOList = indexDAO.selectAtomListByIndexNameOrIdentification(tenantId, nameList, identificationList);
        if (!CollectionUtils.isEmpty(indexAtomicPOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }
        //?????????????????????
        //?????????
        this.getField(tenantId, fieldSet, indexInfoDTOList);
        //?????????
        this.getDatasourceId(tenantId, indexInfoDTOList);
        //??????????????? ???????????????
        this.getUserId(tenantId, indexInfoDTOList);
        //????????????
        this.getApproveGroupId(tenantId, groupSet, indexInfoDTOList);
        ProxyUtil.getProxy(IndexServiceImpl.class).addBatchAtomIndex(indexInfoDTOList, tenantId);
    }


    /**
     * ?????????~??????-??????
     *
     * @param tenantId
     * @param indexInfoDTOList
     * @throws Exception
     */
    private void getDatasourceId(String tenantId, List<IndexInfoDTO> indexInfoDTOList) throws Exception {
        Set<String> sourceNameList = new HashSet<>(16);
        Set<String> dbNameList = new HashSet<>(16);
        Set<String> tableNameList = new HashSet<>(16);
        Set<String> columnNameList = new HashSet<>(16);
        for (IndexInfoDTO indexInfoDTO : indexInfoDTOList) {
            sourceNameList.add(indexInfoDTO.getSourceName());
            dbNameList.add(indexInfoDTO.getDbName());
            tableNameList.add(indexInfoDTO.getTableName());
            columnNameList.add(indexInfoDTO.getColumnName());
        }
        List<String> hiveDbList = tenantService.getDatabase(tenantId);
        List<TableInfoId> tableInfoIdList = tableDAO.selectListByName(tenantId, sourceNameList, hiveDbList, dbNameList, tableNameList, columnNameList);
        if (CollectionUtils.isEmpty(tableInfoIdList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
        }
        AtomicReference<Integer> count = new AtomicReference<>(0);
        indexInfoDTOList.stream().forEach(indexInfoDTO -> {
            tableInfoIdList.stream().forEach(tableInfoId -> {
                if (indexInfoDTO.getSourceName().equalsIgnoreCase(tableInfoId.getSourceName())
                        && indexInfoDTO.getDbName().equalsIgnoreCase(tableInfoId.getDbname())
                        && indexInfoDTO.getTableName().equalsIgnoreCase(tableInfoId.getTableName())
                        && indexInfoDTO.getColumnName().equalsIgnoreCase(tableInfoId.getColumnName())) {
                    indexInfoDTO.setSourceId(tableInfoId.getSourceId());
                    indexInfoDTO.setTableId(tableInfoId.getTableGuid());
                    indexInfoDTO.setColumnId(tableInfoId.getColumnGuid());
                    count.updateAndGet(v -> v + 1);
                }
            });
        });
        if (!count.get().equals(indexInfoDTOList.size())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
    }

    /**
     * ????????????????????????
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void importBatchDeriveIndex(File file, String tenantId) throws Exception {
        if (!file.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList = this.getDeriveIndexData(file);
        //??????????????????
        Set<String> atomIndexName = new HashSet<>(16);
        Set<String> timeLimitName = new HashSet<>(16);
        Set<String> modifiersName = new HashSet<>(16);
        List<String> nameList = new ArrayList<>();
        List<String> identificationList = new ArrayList<>();
        Set<String> fieldSet = new HashSet<>(16);
        Set<String> groupSet = new HashSet<>(16);
        indexTemplateAtomDTOList.stream().forEach(indexTemplateAtomDTO -> {
            atomIndexName.add(indexTemplateAtomDTO.getIndexAtomicName());
            if (StringUtils.isNotBlank(indexTemplateAtomDTO.getTimeLimitName())) {
                timeLimitName.add(indexTemplateAtomDTO.getTimeLimitName());
            }
            indexTemplateAtomDTO.setModifiersNameList(this.getModifiers(indexTemplateAtomDTO.getModifiersName()));
            modifiersName.addAll(indexTemplateAtomDTO.getModifiersNameList());
            nameList.add(indexTemplateAtomDTO.getIndexName());
            identificationList.add(indexTemplateAtomDTO.getIndexIdentification());
            fieldSet.add(indexTemplateAtomDTO.getIndexFieldName());
            groupSet.add(indexTemplateAtomDTO.getApprovalGroupName());
        });
        List<IndexDerivePO> indexAtomicPOList = indexDAO.selectDeriveListByNameAndIdentification(tenantId, nameList, identificationList);
        if (!CollectionUtils.isEmpty(indexAtomicPOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }
        //?????????????????????
        //??????????????????
        this.getAtomIndex(tenantId, atomIndexName, indexTemplateAtomDTOList);
        //????????????
        if (!CollectionUtils.isEmpty(timeLimitName)) {
            this.getTimeLimitByName(tenantId, timeLimitName, indexTemplateAtomDTOList);
        }
        //?????????
        if (!CollectionUtils.isEmpty(modifiersName)) {
            this.getModifierId(tenantId, modifiersName, indexTemplateAtomDTOList);
        }
        //?????????
        this.getFieldDerive(tenantId, fieldSet, indexTemplateAtomDTOList);
        //??????????????? ???????????????
        this.getUserIdDerive(tenantId, indexTemplateAtomDTOList);
        //????????????
        this.getApproveGroupIdDerive(tenantId, groupSet, indexTemplateAtomDTOList);
        ProxyUtil.getProxy(IndexServiceImpl.class).addBatchDeriveIndex(tenantId, indexTemplateAtomDTOList);
    }


    /**
     * ????????????????????????
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void importBatchCompositeIndex(File file, String tenantId) throws Exception {
        if (!file.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList = this.getCompositeIndexData(file);
        //??????????????????
        Set<String> deriveIndexName = new HashSet<>(16);
        List<String> nameList = new ArrayList<>();
        List<String> identificationList = new ArrayList<>();
        Set<String> fieldSet = new HashSet<>(16);
        Set<String> groupSet = new HashSet<>(16);

        indexTemplateCompositeDTOList.stream().forEach(indexTemplateCompositeDTO -> {
            List<String> dependentIndicesNameS = new ArrayList<>();
            for (String s : indexTemplateCompositeDTO.getDependentIndicesName().split("-")) {
                deriveIndexName.add(s);
                dependentIndicesNameS.add(s);
            }
            indexTemplateCompositeDTO.setDependentIndicesNameS(dependentIndicesNameS);
            nameList.add(indexTemplateCompositeDTO.getIndexName());
            identificationList.add(indexTemplateCompositeDTO.getIndexIdentification());
            fieldSet.add(indexTemplateCompositeDTO.getIndexFieldName());
            groupSet.add(indexTemplateCompositeDTO.getApprovalGroupName());
        });

        List<IndexCompositePO> indexCompositePOList = indexDAO.selectCompositeListByNameAndIdentification(tenantId, nameList, identificationList);
        if (!CollectionUtils.isEmpty(indexCompositePOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }

        //?????????????????????
        //??????????????????
        this.getCompositeIndex(tenantId, deriveIndexName, indexTemplateCompositeDTOList);
        //?????????
        this.getFieldComposite(tenantId, fieldSet, indexTemplateCompositeDTOList);
        //??????????????? ???????????????
        this.getUserIdComposite(tenantId, indexTemplateCompositeDTOList);
        //????????????
        this.getApproveGroupIdComposite(tenantId, groupSet, indexTemplateCompositeDTOList);
        ProxyUtil.getProxy(IndexServiceImpl.class).addBatchCompositeIndex(tenantId, indexTemplateCompositeDTOList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBatchAtomIndex(List<IndexInfoDTO> indexInfoDTOList, String tenantId) throws Exception {
        List<IndexAtomicPO> indexAtomicPOList = new ArrayList<>();
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        for (IndexInfoDTO indexDTO : indexInfoDTOList) {
            IndexAtomicPO iap = BeanMapper.map(indexDTO, IndexAtomicPO.class);
            iap.setIndexId(UUID.randomUUID().toString());
            iap.setTenantId(tenantId);
            iap.setIndexState(1);
            iap.setVersion(0);
            iap.setCreator(user.getUserId());
            iap.setCreateTime(timestamp);
            iap.setUpdateTime(timestamp);
            indexAtomicPOList.add(iap);
        }
        indexDAO.insertAtomicIndexList(indexAtomicPOList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBatchDeriveIndex(String tenantId, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<IndexDerivePO> indexDerivePOList = new ArrayList<>();
        List<IndexDeriveModifierRelationPO> indexDeriveModifierRelationPOList = new ArrayList<>();
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            IndexDerivePO idp = BeanMapper.map(indexTemplateDeriveDTO, IndexDerivePO.class);
            idp.setIndexId(UUID.randomUUID().toString());
            idp.setTenantId(tenantId);
            idp.setIndexState(1);
            idp.setVersion(0);
            idp.setCreator(user.getUserId());
            idp.setCreateTime(timestamp);
            idp.setUpdateTime(timestamp);
            indexDerivePOList.add(idp);
            for (String modifier : indexTemplateDeriveDTO.getModifiers()) {
                IndexDeriveModifierRelationPO indexDeriveModifierRelationPO = new IndexDeriveModifierRelationPO();
                indexDeriveModifierRelationPO.setDeriveIndexId(idp.getIndexId());
                indexDeriveModifierRelationPO.setModifierId(modifier);
                indexDeriveModifierRelationPOList.add(indexDeriveModifierRelationPO);
            }
        }
        indexDAO.addDeriveIndexList(indexDerivePOList);
        if (!CollectionUtils.isEmpty(indexDeriveModifierRelationPOList)) {
            indexDAO.addDeriveModifierRelations(indexDeriveModifierRelationPOList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBatchCompositeIndex(String tenantId, List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) throws Exception {
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<IndexCompositePO> indexCompositePOList = new ArrayList<>();
        List<IndexDeriveCompositeRelationPO> indexDeriveCompositeRelationPOList = new ArrayList<>();
        for (IndexTemplateCompositeDTO indexTemplateCompositeDTO : indexTemplateCompositeDTOList) {
            IndexCompositePO icp = BeanMapper.map(indexTemplateCompositeDTO, IndexCompositePO.class);
            icp.setIndexId(UUID.randomUUID().toString());
            icp.setTenantId(tenantId);
            icp.setIndexState(1);
            icp.setVersion(0);
            icp.setCreator(user.getUserId());
            icp.setCreateTime(timestamp);
            icp.setUpdateTime(timestamp);
            indexCompositePOList.add(icp);
            for (String s : indexTemplateCompositeDTO.getDependentIndicesId()) {
                IndexDeriveCompositeRelationPO indexDeriveCompositeRelationPO = new IndexDeriveCompositeRelationPO();
                indexDeriveCompositeRelationPO.setCompositeIndexId(icp.getIndexId());
                indexDeriveCompositeRelationPO.setDeriveIndexId(s);
                indexDeriveCompositeRelationPOList.add(indexDeriveCompositeRelationPO);
            }
        }
        indexDAO.addCompositeIndexList(indexCompositePOList);
        indexDAO.addDeriveCompositeRelations(indexDeriveCompositeRelationPOList);
    }

    /**
     * ????????????-??????
     *
     * @param tenantId
     * @param groupSet
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getApproveGroupIdDerive(String tenantId, Set<String> groupSet, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        List<ApproveGroupListAndSearchResult> approveGroupListAndSearchResultList = approveGroupDAO.selectListByName(tenantId, groupSet);
        if (groupSet.size() != approveGroupListAndSearchResultList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????");
        }
        Map<String, String> map = approveGroupListAndSearchResultList.stream().collect(Collectors.toMap(ApproveGroupListAndSearchResult::getName, ApproveGroupListAndSearchResult::getId));
        indexTemplateAtomDTOList.stream().forEach(indexTemplateDeriveDTO -> indexTemplateDeriveDTO.setApprovalGroupId(map.get(indexTemplateDeriveDTO.getApprovalGroupName())));
    }

    /**
     * ????????????-??????
     *
     * @param tenantId
     * @param groupSet
     * @param indexTemplateCompositeDTOList
     * @throws Exception
     */
    private void getApproveGroupIdComposite(String tenantId, Set<String> groupSet, List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) throws Exception {
        List<ApproveGroupListAndSearchResult> approveGroupListAndSearchResultList = approveGroupDAO.selectListByName(tenantId, groupSet);
        if (groupSet.size() != approveGroupListAndSearchResultList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????");
        }
        Map<String, String> map = approveGroupListAndSearchResultList.stream().collect(Collectors.toMap(ApproveGroupListAndSearchResult::getName, ApproveGroupListAndSearchResult::getId));
        indexTemplateCompositeDTOList.stream().forEach(indexTemplateCompositeDTO -> indexTemplateCompositeDTO.setApprovalGroupId(map.get(indexTemplateCompositeDTO.getApprovalGroupName())));
    }

    /**
     * ?????????????????????????????????-??????
     *
     * @param tenantId
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getUserIdDerive(String tenantId, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        Parameters parameters = new Parameters();
        parameters.setOffset(0);
        parameters.setLimit(-1);
        PageResult<User> pageResult = usersService.getUserListV2(tenantId, parameters);
        List<User> userList = pageResult.getLists();
        if (CollectionUtils.isEmpty(userList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
        }
        Map<String, String> map = userList.stream().collect(Collectors.toMap(User::getUsername, User::getUserId));
        indexTemplateAtomDTOList.stream().forEach(indexTemplateDeriveDTO -> {
            indexTemplateDeriveDTO.setBusinessLeader(map.get(indexTemplateDeriveDTO.getBusinessLeaderName()));
            indexTemplateDeriveDTO.setTechnicalLeader(map.get(indexTemplateDeriveDTO.getTechnicalLeaderName()));
            if (StringUtils.isBlank(indexTemplateDeriveDTO.getBusinessLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
            if (StringUtils.isNotBlank(indexTemplateDeriveDTO.getTechnicalLeaderName()) && StringUtils.isBlank(indexTemplateDeriveDTO.getTechnicalLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
        });
    }


    /**
     * ?????????????????????????????????-??????
     *
     * @param tenantId
     * @param indexTemplateCompositeDTOList
     * @throws Exception
     */
    private void getUserIdComposite(String tenantId, List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) throws Exception {
        Parameters parameters = new Parameters();
        parameters.setOffset(0);
        parameters.setLimit(-1);
        PageResult<User> pageResult = usersService.getUserListV2(tenantId, parameters);
        List<User> userList = pageResult.getLists();
        if (CollectionUtils.isEmpty(userList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
        }
        Map<String, String> map = userList.stream().collect(Collectors.toMap(User::getUsername, User::getUserId));
        indexTemplateCompositeDTOList.stream().forEach(indexTemplateCompositeDTO -> {
            indexTemplateCompositeDTO.setBusinessLeader(map.get(indexTemplateCompositeDTO.getBusinessLeaderName()));
            indexTemplateCompositeDTO.setTechnicalLeader(map.get(indexTemplateCompositeDTO.getTechnicalLeaderName()));
            if (StringUtils.isBlank(indexTemplateCompositeDTO.getBusinessLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
            if (StringUtils.isNotBlank(indexTemplateCompositeDTO.getTechnicalLeaderName()) && StringUtils.isBlank(indexTemplateCompositeDTO.getTechnicalLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
        });
    }

    /**
     * ?????????-??????
     *
     * @param tenantId
     * @param fieldSet
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getFieldDerive(String tenantId, Set<String> fieldSet, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        User user = AdminUtils.getUserData();
        List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        List<CategoryEntityV2> categoryEntityV2List = categoryDAO.selectGuidByTenantIdAndGroupIdAndName(fieldSet, tenantId, userGroupIds);
        if (categoryEntityV2List.size() != fieldSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }
        Map<String, String> map = categoryEntityV2List.stream().collect(Collectors.toMap(CategoryEntityV2::getName, CategoryEntityV2::getGuid));
        indexTemplateAtomDTOList.stream().forEach(indexTemplateDeriveDTO -> indexTemplateDeriveDTO.setIndexFieldId(map.get(indexTemplateDeriveDTO.getIndexFieldName())));
    }

    /**
     * ?????????-??????
     *
     * @param tenantId
     * @param fieldSet
     * @param indexTemplateCompositeDTOList
     * @throws Exception
     */
    private void getFieldComposite(String tenantId, Set<String> fieldSet, List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) throws Exception {
        User user = AdminUtils.getUserData();
        List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        List<CategoryEntityV2> categoryEntityV2List = categoryDAO.selectGuidByTenantIdAndGroupIdAndName(fieldSet, tenantId, userGroupIds);
        if (categoryEntityV2List.size() != fieldSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }
        Map<String, String> map = categoryEntityV2List.stream().collect(Collectors.toMap(CategoryEntityV2::getName, CategoryEntityV2::getGuid));
        indexTemplateCompositeDTOList.stream().forEach(indexTemplateCompositeDTO -> indexTemplateCompositeDTO.setIndexFieldId(map.get(indexTemplateCompositeDTO.getIndexFieldName())));
    }

    /**
     * ???????????????
     *
     * @return
     */
    private List<String> getModifiers(String modifiers) {
        List<String> modifierList = new ArrayList<>();
        String[] str = modifiers.split("-");
        for (String s : str) {
            if (StringUtils.isNotBlank(s)) {
                modifierList.add(s);
            }
        }
        return modifierList;
    }

    /**
     * ???????????????-??????
     *
     * @param tenantId
     * @param modifiersName
     * @param indexTemplateAtomDTOList
     */
    private void getModifierId(String tenantId, Set<String> modifiersName, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) {
        List<Data> dataList = qualifierDAO.getQualifierListByName(tenantId, modifiersName);
        if (modifiersName.size() != dataList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }
        indexTemplateAtomDTOList.stream().forEach(indexTemplateDeriveDTO -> indexTemplateDeriveDTO.setModifiers(this.getModifierIdList(dataList, indexTemplateDeriveDTO)));
    }

    /**
     * ???????????????ID??????
     *
     * @param dataList
     * @param indexTemplateDeriveDTO
     * @return
     */
    private List<String> getModifierIdList(List<Data> dataList, IndexTemplateDeriveDTO indexTemplateDeriveDTO) {
        List<String> list = new ArrayList<>();
        Map<String, String> map = dataList.stream().collect(Collectors.toMap(Data::getName, Data::getId));
        indexTemplateDeriveDTO.getModifiersNameList().stream().forEach(str -> list.add(map.get(str)));
        return list;
    }

    /**
     * ????????????????????????
     *
     * @param tenantId
     * @param atomIndexName
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getAtomIndex(String tenantId, Set<String> atomIndexName, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        List<IndexAtomicPO> indexAtomicPOList = indexDAO.selectAtomListByName(tenantId, atomIndexName);
        if (atomIndexName.size() != indexAtomicPOList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }
        Map<String, String> map = indexAtomicPOList.stream().collect(Collectors.toMap(IndexAtomicPO::getIndexName, IndexAtomicPO::getIndexId));
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            indexTemplateDeriveDTO.setIndexAtomicId(map.get(indexTemplateDeriveDTO.getIndexAtomicName()));
        }
    }

    /**
     * ??????????????????????????????-??????
     *
     * @param tenantId
     * @param deriveIndexName
     * @param indexTemplateCompositeDTOList
     */
    private void getCompositeIndex(String tenantId, Set<String> deriveIndexName, List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) {
        List<IndexDerivePO> indexDerivePOList = indexDAO.selectDeriveByName(tenantId, deriveIndexName);
        if (deriveIndexName.size() != indexDerivePOList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }
        indexTemplateCompositeDTOList.stream().forEach(indexTemplateCompositeDTO -> indexTemplateCompositeDTO.setDependentIndicesId(this.getCompositeIndexIdList(indexDerivePOList, indexTemplateCompositeDTO)));
    }

    /**
     * ????????????????????????ID??????
     *
     * @param indexDerivePOList
     * @param indexTemplateCompositeDTO
     * @return
     */
    private List<String> getCompositeIndexIdList(List<IndexDerivePO> indexDerivePOList, IndexTemplateCompositeDTO indexTemplateCompositeDTO) {
        List<String> list = new ArrayList<>();
        Map<String, String> map = indexDerivePOList.stream().collect(Collectors.toMap(IndexDerivePO::getIndexName, IndexDerivePO::getIndexId));
        indexTemplateCompositeDTO.getDependentIndicesNameS().stream().forEach(str -> list.add(map.get(str)));
        return list;
    }

    /**
     * ????????????-??????
     *
     * @param tenantId
     * @param timeLimitName
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getTimeLimitByName(String tenantId, Set<String> timeLimitName, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        List<TimelimitEntity> timeLimitEntityList = timeLimitDAO.getTimeLimitListByName(tenantId, timeLimitName);
        if (timeLimitEntityList.size() != timeLimitName.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        }
        Map<String, String> map = timeLimitEntityList.stream().collect(Collectors.toMap(TimelimitEntity::getName, TimelimitEntity::getId));
        indexTemplateAtomDTOList.stream().forEach(indexTemplateDeriveDTO -> indexTemplateDeriveDTO.setTimeLimitId(map.get(indexTemplateDeriveDTO.getTimeLimitName())));
    }

    /**
     * ?????????-??????
     *
     * @param tenantId
     * @param fieldSet
     * @param indexInfoDTOList
     * @throws Exception
     */
    private void getField(String tenantId, Set<String> fieldSet, List<IndexInfoDTO> indexInfoDTOList) throws Exception {
        User user = AdminUtils.getUserData();
        List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        List<CategoryEntityV2> categoryEntityV2List = categoryDAO.selectGuidByTenantIdAndGroupIdAndName(fieldSet, tenantId, userGroupIds);
        if (categoryEntityV2List.size() != fieldSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
        }
        Map<String, String> map = categoryEntityV2List.stream().collect(Collectors.toMap(CategoryEntityV2::getName, CategoryEntityV2::getGuid));
        indexInfoDTOList.stream().forEach(indexInfoDTO -> indexInfoDTO.setIndexFieldId(map.get(indexInfoDTO.getIndexFieldName())));
    }

    /**
     * ????????????-??????
     *
     * @param tenantId
     * @param groupSet
     * @param indexInfoDTOList
     * @throws Exception
     */
    private void getApproveGroupId(String tenantId, Set<String> groupSet, List<IndexInfoDTO> indexInfoDTOList) throws Exception {
        List<ApproveGroupListAndSearchResult> approveGroupListAndSearchResultList = approveGroupDAO.selectListByName(tenantId, groupSet);
        if (groupSet.size() != approveGroupListAndSearchResultList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????");
        }
        Map<String, String> map = approveGroupListAndSearchResultList.stream().collect(Collectors.toMap(ApproveGroupListAndSearchResult::getName, ApproveGroupListAndSearchResult::getId));
        indexInfoDTOList.stream().forEach(indexInfoDTO -> indexInfoDTO.setApprovalGroupId(map.get(indexInfoDTO.getApprovalGroupName())));
    }

    /**
     * ?????????????????????????????????-??????
     *
     * @param tenantId
     * @param indexInfoDTOList
     * @throws Exception
     */
    private void getUserId(String tenantId, List<IndexInfoDTO> indexInfoDTOList) throws Exception {
        Parameters parameters = new Parameters();
        parameters.setOffset(0);
        parameters.setLimit(-1);
        PageResult<User> pageResult = usersService.getUserListV2(tenantId, parameters);
        List<User> userList = pageResult.getLists();
        if (CollectionUtils.isEmpty(userList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
        }
        Map<String, String> map = userList.stream().collect(Collectors.toMap(User::getUsername, User::getUserId));
        for (IndexInfoDTO indexDTO : indexInfoDTOList) {
            indexDTO.setBusinessLeader(map.get(indexDTO.getBusinessLeaderName()));
            indexDTO.setTechnicalLeader(map.get(indexDTO.getTechnicalLeaderName()));
            if (StringUtils.isBlank(indexDTO.getBusinessLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
            if (StringUtils.isNotBlank(indexDTO.getTechnicalLeaderName()) && StringUtils.isBlank(indexDTO.getTechnicalLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
        }
    }
}
