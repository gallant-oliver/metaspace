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
import io.zeta.metaspace.model.modifiermanage.QualifierParameters;
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
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeanMapper;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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

    //目录类型    指标域
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
     * 添加指标
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
            //名称和标识重名校验
            IndexAtomicPO exits = indexDAO.getAtomicIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
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
                LOG.error("添加原子指标失败", e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加原子指标失败");
            }
            iard = BeanMapper.map(iap, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
            //名称和标识重名校验
            IndexAtomicPO exits = indexDAO.getDeriveIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
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
                LOG.error("添加派生指标失败", e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加派生指标失败");
            }
            iard = BeanMapper.map(idp, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
            //名称和标识重名校验
            IndexAtomicPO exits = indexDAO.getCompositeIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
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
                LOG.error("添加复合指标失败", e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加复合指标失败");
            }
            iard = BeanMapper.map(icp, IndexResposeDTO.class);
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
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
     * 组装复合指标与派生指标关系
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
     * 组装派生指标与修饰词关系
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
            //名称和标识重名校验
            IndexAtomicPO exits = indexDAO.getAtomicIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexAtomicPO iap = BeanMapper.map(indexDTO, IndexAtomicPO.class);
            iap.setUpdater(user.getUserId());
            iap.setUpdateTime(timestamp);
            indexDAO.editAtomicIndex(iap);
            indexDAO.moveAtomicIndex(iap.getIndexId(), tenantId, iap.getIndexFieldId());
            iard = BeanMapper.map(iap, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {
            //名称和标识重名校验
            IndexAtomicPO exits = indexDAO.getDeriveIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexDerivePO idp = BeanMapper.map(indexDTO, IndexDerivePO.class);
            idp.setUpdater(user.getUserId());
            idp.setUpdateTime(timestamp);
            List<String> dependentIndices = indexDTO.getDependentIndices();
            if (!CollectionUtils.isEmpty(dependentIndices)) {
                idp.setIndexAtomicId(dependentIndices.get(0));
            }
            //获取已经存在的派生指标与修饰词关系
            List<IndexDeriveModifierRelationPO> modifierRelations = indexDAO.getDeriveModifierRelations(idp.getIndexId());
            editDerivIndex(idp, indexDTO.getModifiers(), modifierRelations);
            indexDAO.moveDerivIndex(idp.getIndexId(), tenantId, idp.getIndexFieldId());
            iard = BeanMapper.map(idp, IndexResposeDTO.class);
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) {
            //名称和标识重名校验
            IndexAtomicPO exits = indexDAO.getCompositeIndexByNameOrIdentification(tenantId, indexDTO);
            if (!Objects.isNull(exits)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或标识已经存在");
            }
            IndexCompositePO icp = BeanMapper.map(indexDTO, IndexCompositePO.class);
            icp.setUpdater(user.getUserId());
            icp.setUpdateTime(timestamp);
            List<IndexDeriveCompositeRelationPO> compositeRelations = indexDAO.getDeriveCompositeRelations(icp.getIndexId());
            editCompositeIndex(icp, indexDTO.getDependentIndices(), compositeRelations);
            indexDAO.moveCompositeIndex(icp.getIndexId(), tenantId, icp.getIndexFieldId());
            iard = BeanMapper.map(icp, IndexResposeDTO.class);
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        return iard;
    }

    @Transactional(rollbackFor = Exception.class)
    public void editDerivIndex(IndexDerivePO idp, List<String> modifiers, List<IndexDeriveModifierRelationPO> modifierRelations) {
        //1.编辑派生指标
        indexDAO.editDerivIndex(idp);
        try {
            //2.编辑派生指标与修饰词关系
            if (CollectionUtils.isEmpty(modifierRelations)) {
                if (!CollectionUtils.isEmpty(modifiers)) {
                    //增加派生指标与修饰词关系
                    List<IndexDeriveModifierRelationPO> idmrPOS = getDeriveModifierRelationPOS(idp.getIndexId(), modifiers);
                    indexDAO.addDeriveModifierRelations(idmrPOS);
                }
            } else {
                if (!CollectionUtils.isEmpty(modifiers)) {
                    List<String> exits = modifierRelations.stream().map(x -> x.getModifierId()).distinct().collect(Collectors.toList());
                    //增加的派生指标与修饰词关系
                    List<String> adds = modifiers.stream().filter(x -> !exits.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(adds)) {
                        List<IndexDeriveModifierRelationPO> addPOS = getDeriveModifierRelationPOS(idp.getIndexId(), adds);
                        indexDAO.addDeriveModifierRelations(addPOS);
                    }
                    //删除的派生指标与修饰词关系
                    List<String> dels = exits.stream().filter(x -> !modifiers.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(dels)) {
                        indexDAO.deleteDeriveModifierRelationsByDeriveModifierId(idp.getIndexId(), dels);
                    }
                } else {
                    //删除已存在的派生指标与修饰词关系
                    indexDAO.deleteDeriveModifierRelationsByDeriveId(idp.getIndexId());
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            LOG.error("编辑派生指标失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }


    }

    @Transactional(rollbackFor = Exception.class)
    public void editCompositeIndex(IndexCompositePO icp, List<String> dependentIndices, List<IndexDeriveCompositeRelationPO> compositeRelations) {
        //1.编辑复合指标
        indexDAO.editCompositeIndex(icp);
        //2.编辑复合指标与派生指标关系
        try {
            if (CollectionUtils.isEmpty(compositeRelations)) {
                if (!CollectionUtils.isEmpty(dependentIndices)) {
                    //增加派生指标与修饰词关系
                    List<IndexDeriveCompositeRelationPO> idcrPOS = getDeriveCompositeRelationPOS(icp.getIndexId(), dependentIndices);
                    indexDAO.addDeriveCompositeRelations(idcrPOS);
                }
            } else {
                if (!CollectionUtils.isEmpty(dependentIndices)) {
                    List<String> exits = compositeRelations.stream().map(x -> x.getCompositeIndexId()).distinct().collect(Collectors.toList());
                    //增加的派生指标与修饰词关系
                    List<String> adds = dependentIndices.stream().filter(x -> !exits.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(adds)) {
                        List<IndexDeriveCompositeRelationPO> addPOS = getDeriveCompositeRelationPOS(icp.getIndexId(), adds);
                        indexDAO.addDeriveCompositeRelations(addPOS);
                    }
                    //删除的派生指标与修饰词关系
                    List<String> dels = exits.stream().filter(x -> !dependentIndices.contains(x)).distinct().collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(dels)) {
                        indexDAO.deleteDeriveCompositeRelationsByDeriveCompositeId(icp.getIndexId(), dels);
                    }
                } else {
                    //删除已存在的派生指标与修饰词关系
                    indexDAO.deleteDeriveCompositeRelationsByDeriveId(icp.getIndexId());
                }
            }
        } catch (SQLException e) {
            LOG.error("编辑复合指标失败", e);
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
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
                }
            });
        }
    }

    /**
     * 获取可选指标列表
     *
     * @param indexType    指标类型
     * @param categoryType 目录类型  5  指标域
     * @param tenantId     租户id
     * @return
     */
    @Override
    public List<OptionalIndexDTO> getOptionalIndex(int indexType, int categoryType, String tenantId) {
        //1.获取当前租户下用户所属用户组
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        List<OptionalIndexDTO> optionalIndexDTOS = null;
        if (!CollectionUtils.isEmpty(groups)) {
            //2.获取被授权给用户组的目录
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<String> indexFieldIds = categoryDAO.getCategorysByGroup(groupIds, categoryType, tenantId);
            //3.获取目录下的已发布的指标
            if (!CollectionUtils.isEmpty(indexFieldIds)) {
                //已发布
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
     * 获取可选数据源列表
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<OptionalDataSourceDTO> getOptionalDataSource(String tenantId) {
        //1.获取当前租户下用户所属用户组
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        List<OptionalDataSourceDTO> odsds = new ArrayList<>();
        OptionalDataSourceDTO ods = new OptionalDataSourceDTO();
        ods.setSourceId("hive");
        ods.setSourceName(DataSourceType.HIVE.getName());
        ods.setSourceType(DataSourceType.HIVE.getName());
        odsds.add(ods);
        if (!CollectionUtils.isEmpty(groups)) {
            //2. 获取被授权给用户组的数据源
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            List<DataSourceBody> dataSourceBodies = dataSourceDAO.getDataSourcesByGroups(groupIds, tenantId);
            if (!CollectionUtils.isEmpty(dataSourceBodies)) {
                //根据id去重
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
     * 获取指标链路
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
        if (indexType == IndexType.INDEXCOMPOSITE.getValue()) { //复合指标
            IndexCompositePO compositeIndexPO = indexDAO.getCompositeIndexPO(indexId, Integer.parseInt(version), tenantId); //复合指标

            List<IndexDerivePO> dependentDeriveIndex = indexDAO.getDependentDeriveIndex(indexId, tenantId);//获取依赖派生指标
            if (dependentDeriveIndex != null && dependentDeriveIndex.size() > 0) {
                for (IndexDerivePO po : dependentDeriveIndex) {

                    IndexAtomicPO indexAtomicPO = indexDAO.getDependentAtomicIndex(po.getIndexAtomicId(), tenantId);
                    if (indexAtomicPO == null) {
                        continue; //依赖删除
                    }
                    status = getLinkByAutoMaticIndex(indexAtomicPO, tenantId, po.getIndexId(), nodes, relations);
                    getLinkByDriveIndex(po, indexAtomicPO, compositeIndexPO.getIndexId(), tenantId, nodes, relations, status ? "0" : "2");
                }
            }
            getLinkByComplexIndex(compositeIndexPO, nodes, status ? "0" : "2");
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) { //派生指标
            IndexDerivePO deriveIndexPO = indexDAO.getDeriveIndexPO(indexId, Integer.parseInt(version), tenantId);
            IndexAtomicPO indexAtomicPO = indexDAO.getDependentAtomicIndex(deriveIndexPO.getIndexAtomicId(), tenantId);
            if (indexAtomicPO != null) {
                status = getLinkByAutoMaticIndex(indexAtomicPO, tenantId, deriveIndexPO.getIndexId(), nodes, relations);
            }
            getLinkByDriveIndex(deriveIndexPO, indexAtomicPO, null, tenantId, nodes, relations, status ? "0" : "2");
        } else if (indexType == IndexType.INDEXATOMIC.getValue()) { //原子指标
            IndexAtomicPO atomicIndexPO = indexDAO.getAtomicIndexPO(indexId, Integer.parseInt(version), tenantId);
            getLinkByAutoMaticIndex(atomicIndexPO, tenantId, null, nodes, relations);
        } else {  //不支持的指标类型
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        IndexLinkDto indexLinkDto = new IndexLinkDto();
        indexLinkDto.setNodes(nodes);
        indexLinkDto.setRelations(relations);
        return indexLinkDto;
    }


    public void getLinkByComplexIndex(IndexCompositePO index, List<IndexLinkEntity> nodes, String status) {
        ComplexIndexLinkNode node = new ComplexIndexLinkNode();
        String indexId = index.getIndexId();   //复合指标ID
        // 生成复合指标节点关系
        node.setId(indexId); //指标ID
        node.setNodeName(index.getIndexName()); //指标名称
        node.setIndexCode(index.getIndexIdentification());
        node.setPublishTime(index.getPublishTime());
        node.setNodeType("5"); //复合指标
        node.setNodeStatus(status);//指标类型节点默认执行成功
        node.setExpress(index.getExpression());//指标类型节点默认执行成功
        node.setBusinessCaliber(index.getBusinessCaliber());
        node.setTechnicalCaliber(index.getTechnicalCaliber());
        nodes.add(node);
    }


    public void getLinkByDriveIndex(IndexDerivePO deriveIndex, IndexAtomicPO indexAtomicPO, String parentId, String tenantId, List<IndexLinkEntity> nodes, List<IndexLinkRelation> relations, String status) {

        DriveIndexLinkNode driverNode;
        try {
            //生成派生指标类型节点
            driverNode = new DriveIndexLinkNode();
            driverNode.setId(deriveIndex.getIndexId()); //指标ID
            driverNode.setNodeName(deriveIndex.getIndexName()); //指标名称
            driverNode.setPublishTime(deriveIndex.getPublishTime());
            driverNode.setNodeType("4"); //派生指标
            driverNode.setAtomIndexName(indexAtomicPO == null ? "" : indexAtomicPO.getIndexName()); //依赖的原子指标名称
            driverNode.setNodeStatus(status);//指标类型节点默认执行成功
            driverNode.setIndexCode(deriveIndex.getIndexIdentification());
            driverNode.setBusinessCaliber(deriveIndex.getBusinessCaliber());
            driverNode.setTechnicalCaliber(deriveIndex.getTechnicalCaliber());
            List<Qualifier> qualifiers = indexDAO.getModifiers(deriveIndex.getIndexId(), tenantId);
            if (!CollectionUtils.isEmpty(qualifiers)) {   //添加修饰词
                String collect = qualifiers.stream().map(x -> x.getName()).collect(Collectors.joining(","));
                driverNode.setQualifierName(collect);
            }
            TimelimitEntity timeLimitById = timeLimitDAO.getTimeLimitById(deriveIndex.getTimeLimitId(), tenantId);
            if (timeLimitById != null) {
                driverNode.setTimeLimitName(timeLimitById.getName()); //依赖的时间限定名称
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取指标链路失败");
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
            LOG.error("获取原子指标信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取指标链路失败" + e.getMessage());
        }
        boolean result = true;
        if (conf.getBoolean("etl.indexLink.enable")) {  //是否集成任务调度
            //依据原子指标获取任务调度ETL信息
            result = getLinkByEtl(indexAtomic, tenantId, nodes, relations, dataSourceInfo, table, col);
        }
        try {
            AutoMaticIndexLinkNode autoMaticIndexLinkNode = new AutoMaticIndexLinkNode();
            autoMaticIndexLinkNode.setId(indexAtomic.getIndexId()); //指标ID
            autoMaticIndexLinkNode.setBusinessCaliber(indexAtomic.getBusinessCaliber());
            autoMaticIndexLinkNode.setNodeName(indexAtomic.getIndexName()); //指标名称
            autoMaticIndexLinkNode.setIndexCode(indexAtomic.getIndexIdentification()); //指标标识
            autoMaticIndexLinkNode.setPublishTime(indexAtomic.getPublishTime());
            autoMaticIndexLinkNode.setNodeType("3"); //原子指标
            autoMaticIndexLinkNode.setNodeStatus(result ? "0" : "2");//指标类型节点默认执行成功
            autoMaticIndexLinkNode.setTechnicalCaliber(indexAtomic.getTechnicalCaliber());
            autoMaticIndexLinkNode.setDataFrom(dataSourceInfo == null ? "hive" + "-" + indexAtomic.getDbName() + "-" + table + "-" + col : dataSourceInfo.getSourceName() + "-" + dataSourceInfo.getDatabase() + "-" + table + "-" + col);
            nodes.add(autoMaticIndexLinkNode);
            IndexLinkRelation relation = new IndexLinkRelation();
            relation.setFrom(autoMaticIndexLinkNode.getId());
            relation.setTo(parentId);
            relations.add(relation);
        } catch (Exception e) {
            LOG.error("GET AutoMaticIndex NODE fail", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取指标链路失败" + e.getMessage());
        }
        return result;
    }

    /**
     * ETL节点不在本系统维护，不涉及状态，数据的维护变更，暂不维护进tree中
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
            String json = gson.toJson(postBody); //请求body
            String string = OKHttpClient.doPost(conf.getString("etl.indexlink.address"), hashMap, null, json);
            LOG.info("ETL return data is =>" + string);
            Map<String, Object> result = gson.fromJson(string, HashMap.class);
            Map<String, Object> dataMap = (Map) result.get("data");
            if (dataMap == null) {  //无 ETL 数据
                LOG.info("ETL return data is null => indexId=" + indexAtomic.getIndexId());
                return resultStatus;

            }
            Map<String, Object> valueMap = gson.fromJson(dataMap.get("value").toString(), HashMap.class);
            Map<String, Object> linkInfoMap = (Map<String, Object>) valueMap.get("dataLinkNodeMap"); //获取调度节点信息与关系信息
            String root = valueMap.get("rootName").toString(); //获取调度节点信息与关系信息
            if (linkInfoMap != null) { //原子指标加工链路
                Set<Map.Entry<String, Object>> entries = linkInfoMap.entrySet();//key : 任务ID， value: 任务详情与父任务指针
                for (Map.Entry<String, Object> en : entries) {
                    String key = en.getKey();//任务调度执行ID
                    Map<String, Object> nodeInfo = (Map<String, Object>) en.getValue();  //节点的执行信息
                    EtlIndexLinkNode entity = new EtlIndexLinkNode();
                    entity.setId(key);
                    entity.setNodeName(key);
                    entity.setInstanceName(nodeInfo.get("processInstanceName") == null ? "" : nodeInfo.get("processInstanceName").toString());
                    entity.setDefinitionName(nodeInfo.get("processDefinitionName") == null ? "" : nodeInfo.get("processDefinitionName").toString());
                    entity.setNodeStatus(nodeInfo.get("state") == null ? "" : String.valueOf(((Double) nodeInfo.get("state")).longValue()));
                    if (key.equals(root) && !"0".equals(entity.getNodeStatus())) { //etl 失败，指标节点成为受到影响的节点，修改上游节点状态
                        resultStatus = false;
                    }
                    entity.setProjectName(nodeInfo.get("projectName") == null ? "" : nodeInfo.get("projectName").toString());
                    entity.setTypeName(nodeInfo.get("nodeType") == null ? "" : nodeInfo.get("nodeType").toString());
                    entity.setNodeType("1"); //采集类型节点
                    Long endTime = null;
                    if (nodeInfo.get("endTime") != null) {
                        endTime = ((Double) nodeInfo.get("endTime")).longValue();
                    }
                    entity.setEndTime(endTime); //结束时间
                    nodes.add(entity);  //封装任务节点
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
            return resultStatus;  //用于确认上游节点状态
        } catch (Exception e) {
            LOG.error("GET ETL NODE fail", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取指标链路失败");
        }
    }


    @Override
    public IndexInfoDTO getIndexInfo(String indexId, int indexType, int version, int categoryType, String tenantId) {
        IndexInfoDTO indexInfoDTO = null;
        if (indexType == IndexType.INDEXATOMIC.getValue()) {  //原子指标
            IndexInfoPO indexInfoPO = indexDAO.getAtomicIndexInfoPO(indexId, version, categoryType, tenantId);
            if (!Objects.isNull(indexInfoPO)) {
                indexInfoDTO = BeanMapper.map(indexInfoPO, IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXATOMIC.getValue());
            }
        } else if (indexType == IndexType.INDEXDERIVE.getValue()) {  //派生指标
            IndexInfoPO indexInfoPO = indexDAO.getDeriveIndexInfoPO(indexId, version, categoryType, tenantId);
            if (!Objects.isNull(indexInfoPO)) {
                indexInfoDTO = BeanMapper.map(indexInfoPO, IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXDERIVE.getValue());
                //添加依赖的原子指标
                IndexAtomicPO indexAtomicPO = indexDAO.getDependentAtomicIndex(indexInfoPO.getIndexAtomicId(), tenantId);
                List<IndexAtomicPO> indexAtomicPOs = new ArrayList<>();
                indexAtomicPOs.add(indexAtomicPO);
                if (!CollectionUtils.isEmpty(indexAtomicPOs)) {
                    List<DependentIndex> dependentIndices = indexAtomicPOs.stream().map(x -> BeanMapper.map(x, DependentIndex.class)).collect(Collectors.toList());
                    indexInfoDTO.setDependentIndices(dependentIndices);
                } else {
                    indexInfoDTO.setDependentIndices(new ArrayList<>());
                }
                //添加修饰词
                List<Qualifier> qualifiers = indexDAO.getModifiers(indexInfoPO.getIndexId(), tenantId);
                if (!CollectionUtils.isEmpty(qualifiers)) {
                    List<Modifier> modifiers = qualifiers.stream().map(x -> BeanMapper.map(x, Modifier.class)).collect(Collectors.toList());
                    indexInfoDTO.setModifiers(modifiers);
                } else {
                    indexInfoDTO.setModifiers(new ArrayList<>());
                }
            }
        } else if (indexType == IndexType.INDEXCOMPOSITE.getValue()) { //复合指标
            IndexInfoPO indexInfoPO = indexDAO.getCompositeIndexInfoPO(indexId, version, categoryType, tenantId);
            if (!Objects.isNull(indexInfoPO)) {
                indexInfoDTO = BeanMapper.map(indexInfoPO, IndexInfoDTO.class);
                indexInfoDTO.setIndexType(IndexType.INDEXCOMPOSITE.getValue());
                //添加依赖的派生指标
                List<IndexDerivePO> indexDerivePOS = indexDAO.getDependentDeriveIndex(indexInfoPO.getIndexId(), tenantId);
                if (!CollectionUtils.isEmpty(indexDerivePOS)) {
                    List<DependentIndex> dependentIndices = indexDerivePOS.stream().map(x -> BeanMapper.map(x, DependentIndex.class)).collect(Collectors.toList());
                    indexInfoDTO.setDependentIndices(dependentIndices);
                } else {
                    indexInfoDTO.setDependentIndices(new ArrayList<>());
                }
            }
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
        if (!Objects.isNull(indexInfoDTO)) {
            //hive数据源单独处理
            if ("hive".equalsIgnoreCase(indexInfoDTO.getSourceId())) {
                indexInfoDTO.setSourceName(DataSourceType.HIVE.getName());
            }
            //添加审批组成员
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
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
                //获取依赖原子指标
                List<IndexAtomicPO> indexAtomicPOS = indexDAO.getAtomicIndexInfoPOs(dependentIndexIds, tenantId);
                Map<String, DependentIndex> maps = indexAtomicPOS.stream().map(x -> BeanMapper.map(x, DependentIndex.class)).collect(Collectors.toMap(DependentIndex::getIndexId, Function.identity(), (key1, key2) -> key2));
                //获取依赖的修饰词
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
                        //添加指标依赖
                        indexInfoDTO.setDependentIndices(dependentIndices);
                    }
                    //添加修饰词
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
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
            //添加审批组成员
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
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
                    //发布
                    if (approveResult.equals(ApproveOperate.APPROVE.getCode())) {
                        //通过
                        editIndexState(objectId, indexType, version, tenantId, IndexState.PUBLISH.getValue());
                    } else if (approveResult.equals(ApproveOperate.REJECTED.getCode())) {
                        //驳回,回退指标状态
                        if (version == 0) {
                            editIndexState(objectId, indexType, version, tenantId, IndexState.CREATE.getValue());
                        } else {
                            editIndexState(objectId, indexType, version, tenantId, IndexState.OFFLINE.getValue());
                        }
                    }
                } else if (approveType.equals(ApproveType.OFFLINE.getCode())) {
                    //下线
                    if (approveResult.equals(ApproveOperate.APPROVE.getCode())) {
                        //通过
                        try {
                            offlineApprove(objectId, indexType, version, tenantId, IndexState.OFFLINE.getValue());
                        } catch (Exception e) {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
                        }
                    } else if (approveResult.equals(ApproveOperate.REJECTED.getCode())) {
                        //驳回,回退指标状态
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void offlineApprove(String indexId, int indexType, int version, String tenantId, int state) throws SQLException {
        editIndexState(indexId, indexType, version, tenantId, state);
        //版本+1
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型错误");
        }
    }


    /**
     * 派生指标excel
     *
     * @param tenantId
     * @return
     */
    public XSSFWorkbook downLoadExcelDerive(String tenantId) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet("派生指标");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("依赖原子指标*");
            row.createCell(1).setCellValue("时间限定");
            row.createCell(2).setCellValue("修饰词");
            row.createCell(3).setCellValue("指标名称*");
            row.createCell(4).setCellValue("指标标识*");
            row.createCell(5).setCellValue("描述");
            row.createCell(6).setCellValue("是否核心指标");
            row.createCell(7).setCellValue("指标域*");
            row.createCell(8).setCellValue("业务口径*");
            row.createCell(9).setCellValue("业务负责人*");
            row.createCell(10).setCellValue("技术口径");
            row.createCell(11).setCellValue("技术负责人");
            row.createCell(12).setCellValue("审批管理*");

            //依赖原子指标
            this.setXSSFDataValidation(this.getAtomicName(tenantId), 0, 0, sheet);
            //时间限定
            this.setXSSFDataValidation(this.getTimeLimitName(tenantId), 1, 1, sheet);

            //是否核心指标下拉框
            CellRangeAddressList regions = new CellRangeAddressList(0, 1000, 6, 6);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"是", "否"});
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);

            //业务负责人下拉框
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 9, 9, sheet);

            //技术负责人下拉框
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 11, 11, sheet);

            //审批管理下拉框
            this.setXSSFDataValidation(this.getApproveGroupByModuleId(tenantId), 12, 12, sheet);
        } catch (Exception e) {
            LOG.error("downLoadExcel exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, "下载模板失败");
        }
        return workbook;
    }


    /**
     * 复合指标excel
     *
     * @param tenantId
     * @return
     */
    public XSSFWorkbook downLoadExcelComposite(String tenantId) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet("复合指标");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("依赖派生指标*");
            row.createCell(1).setCellValue("指标名称*");
            row.createCell(2).setCellValue("指标标识*");
            row.createCell(3).setCellValue("描述");
            row.createCell(4).setCellValue("是否核心指标");
            row.createCell(5).setCellValue("指标域*");
            row.createCell(6).setCellValue("设定表达式*");
            row.createCell(7).setCellValue("业务口径*");
            row.createCell(8).setCellValue("业务负责人*");
            row.createCell(9).setCellValue("技术口径");
            row.createCell(10).setCellValue("技术负责人");
            row.createCell(11).setCellValue("审批管理*");

            //是否核心指标下拉框
            CellRangeAddressList regions = new CellRangeAddressList(0, 1000, 4, 4);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"是", "否"});
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);

            //业务负责人下拉框
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 8, 8, sheet);

            //技术负责人下拉框
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 10, 10, sheet);

            //审批管理下拉框
            this.setXSSFDataValidation(this.getApproveGroupByModuleId(tenantId), 11, 11, sheet);
        } catch (Exception e) {
            LOG.error("downLoadExcel exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, "下载模板失败");
        }
        return workbook;
    }

    private void setXSSFDataValidation(List<String> list, Integer firstCol, Integer lastCol, XSSFSheet sheet) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
            CellRangeAddressList regions = new CellRangeAddressList(0, 1000, firstCol, lastCol);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(list.toArray(new String[list.size()]));
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
            validation.setShowErrorBox(false);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);
        }
    }

    /**
     * 原子指标excel
     *
     * @param tenantId
     * @return
     */
    public XSSFWorkbook downLoadExcelAtom(String tenantId) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet("原子指标");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("指标名称*");
            row.createCell(1).setCellValue("指标标识*");
            row.createCell(2).setCellValue("描述");
            row.createCell(3).setCellValue("是否核心指标");
            row.createCell(4).setCellValue("指标域*");
            row.createCell(5).setCellValue("数据源*");
            row.createCell(6).setCellValue("数据库*");
            row.createCell(7).setCellValue("数据表*");
            row.createCell(8).setCellValue("字段*");
            row.createCell(9).setCellValue("业务口径*");
            row.createCell(10).setCellValue("业务负责人*");
            row.createCell(11).setCellValue("技术口径");
            row.createCell(12).setCellValue("技术负责人");
            row.createCell(13).setCellValue("审批管理*");

            //是否核心指标下拉框
            CellRangeAddressList regions = new CellRangeAddressList(0, 1000, 3, 3);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"是", "否"});
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);

            List<String> list = new ArrayList<>();
            //数据源下拉框
            this.setXSSFDataValidation(this.getDataSourceByTenantId(tenantId), 5, 5, sheet);

            //业务负责人下拉框
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 10, 10, sheet);

            //技术负责人下拉框
            this.setXSSFDataValidation(this.getUserListByTenantId(tenantId), 12, 12, sheet);

            //审批管理下拉框
            this.setXSSFDataValidation(this.getApproveGroupByModuleId(tenantId), 13, 13, sheet);
        } catch (Exception e) {
            LOG.error("downLoadExcel exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, "下载模板失败");
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
     * 原子指标
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
     * 时间限定
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

    /**
     * 修饰词
     *
     * @param tenantId
     * @return
     */
    private List<String> getQualifierName(String tenantId) {
        List<String> list = new ArrayList<>();
        QualifierParameters qualifierParameters = new QualifierParameters();
        qualifierParameters.setLimit(-1);
        PageResult<Data> pageResult = qualifierService.getAllQualifierList(qualifierParameters, tenantId);
        List<Data> dataList = pageResult.getLists();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(dataList)) {
            return list;
        }
        dataList.stream().forEach(data -> list.add(data.getName()));
        return list;
    }

    @Override
    public String uploadExcelAtom(String tenantId, File file) throws Exception {
        this.getAtomIndexData(file);
        return ExportDataPathUtils.transferTo(file);
    }

    @Override
    public String uploadExcelDerive(String tenantId, File file) throws Exception {
        this.getDeriveIndexData(file);
        return ExportDataPathUtils.transferTo(file);
    }

    @Override
    public String uploadExcelComposite(String tenantId, File file) throws Exception {
        this.getCompositeIndexData(file);
        return ExportDataPathUtils.transferTo(file);
    }

    /**
     * 获取原子指标数据
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<IndexTemplateAtomDTO> getAtomIndexData(File file) throws Exception {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
        Sheet sheet = xssfWorkbook.getSheetAt(0);
        List<IndexTemplateAtomDTO> indexTemplateAtomList = new ArrayList<>();
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            IndexTemplateAtomDTO indexTemplateAtom = new IndexTemplateAtomDTO();
            indexTemplateAtom.setName(row.getCell(0) == null ? "" : row.getCell(0).getStringCellValue().trim());
            indexTemplateAtom.setIdentification(row.getCell(1) == null ? "" : row.getCell(1).getStringCellValue().trim());
            indexTemplateAtom.setDescription(row.getCell(2) == null ? "" : row.getCell(2).getStringCellValue().trim());
            indexTemplateAtom.setCentral(row.getCell(3) == null ? "" : row.getCell(3).getStringCellValue().trim());
            indexTemplateAtom.setField(row.getCell(4) == null ? "" : row.getCell(4).getStringCellValue().trim());
            indexTemplateAtom.setSource(row.getCell(5) == null ? "" : row.getCell(5).getStringCellValue().trim());
            indexTemplateAtom.setDbName(row.getCell(6) == null ? "" : row.getCell(6).getStringCellValue().trim());
            indexTemplateAtom.setTableName(row.getCell(7) == null ? "" : row.getCell(7).getStringCellValue().trim());
            indexTemplateAtom.setColumnName(row.getCell(8) == null ? "" : row.getCell(8).getStringCellValue().trim());
            indexTemplateAtom.setBusinessCaliber(row.getCell(9) == null ? "" : row.getCell(9).getStringCellValue().trim());
            indexTemplateAtom.setBusinessLeader(row.getCell(10) == null ? "" : row.getCell(10).getStringCellValue().trim());
            indexTemplateAtom.setTechnicalCaliber(row.getCell(11) == null ? "" : row.getCell(11).getStringCellValue().trim());
            indexTemplateAtom.setTechnicalLeader(row.getCell(12) == null ? "" : row.getCell(12).getStringCellValue().trim());
            indexTemplateAtom.setApprove(row.getCell(13) == null ? "" : row.getCell(13).getStringCellValue().trim());
            String checkResult = indexTemplateAtom.checkFieldsIsNull();
            if (StringUtils.isNotBlank(checkResult)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, checkResult);
            }
            indexTemplateAtomList.add(indexTemplateAtom);
        }
        if (CollectionUtils.isEmpty(indexTemplateAtomList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内容不能为空");
        }
        Set<String> nameSet = new HashSet<>();
        Set<String> identificationSet = new HashSet<>();
        indexTemplateAtomList.stream().forEach(indexTemplateAtom -> {
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateAtom.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称仅支持中文、英文和数字");
            }
            if (indexTemplateAtom.getName().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称不能超过128位");
            }
            if (!Pattern.matches("^[a-z0-9_]+$", indexTemplateAtom.getIdentification())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识仅支持小写英文、数字和“_”");
            }
            if (indexTemplateAtom.getIdentification().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识不能超过128位");
            }
            if (indexTemplateAtom.getDescription().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "描述不能超过300位");
            }
            if (indexTemplateAtom.getBusinessCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务口径不能超过128位");
            }
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateAtom.getBusinessCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务口径仅支持中文、英文和数字");
            }
            if (indexTemplateAtom.getTechnicalCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术口径不能超过128位");
            }
            if (StringUtils.isNotBlank(indexTemplateAtom.getTechnicalCaliber()) && !Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateAtom.getTechnicalCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术口径仅支持中文、英文和数字");
            }
            nameSet.add(indexTemplateAtom.getName());
            identificationSet.add(indexTemplateAtom.getIdentification());
        });
        if (indexTemplateAtomList.size() != nameSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称存在重复数据");
        }
        if (indexTemplateAtomList.size() != identificationSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识存在重复数据");
        }
        if (indexTemplateAtomList.size() > 100) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "单次指标批量导入上限为100");
        }
        return indexTemplateAtomList;
    }


    /**
     * 获取原子指标数据
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<IndexTemplateDeriveDTO> getDeriveIndexData(File file) throws Exception {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
        Sheet sheet = xssfWorkbook.getSheetAt(0);
        List<IndexTemplateDeriveDTO> indexTemplateDeriveDTOList = new ArrayList<>();
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            IndexTemplateDeriveDTO indexTemplateDeriveDTO = new IndexTemplateDeriveDTO();
            indexTemplateDeriveDTO.setIndexAtomicName(row.getCell(0) == null ? "" : row.getCell(0).getStringCellValue().trim());
            indexTemplateDeriveDTO.setTimeLimitName(row.getCell(1) == null ? "" : row.getCell(1).getStringCellValue().trim());
            indexTemplateDeriveDTO.setModifiersName(row.getCell(2) == null ? "" : row.getCell(2).getStringCellValue().trim());
            indexTemplateDeriveDTO.setIndexName(row.getCell(3) == null ? "" : row.getCell(3).getStringCellValue().trim());
            indexTemplateDeriveDTO.setIndexIdentification(row.getCell(4) == null ? "" : row.getCell(4).getStringCellValue().trim());
            indexTemplateDeriveDTO.setDescription(row.getCell(5) == null ? "" : row.getCell(5).getStringCellValue().trim());
            String centralName = row.getCell(6) == null ? "" : row.getCell(6).getStringCellValue().trim();
            if ("是".equals(centralName)) {
                indexTemplateDeriveDTO.setCentral(true);
            } else {
                indexTemplateDeriveDTO.setCentral(false);
            }
            indexTemplateDeriveDTO.setIndexFieldName(row.getCell(7) == null ? "" : row.getCell(7).getStringCellValue().trim());
            indexTemplateDeriveDTO.setBusinessCaliber(row.getCell(8) == null ? "" : row.getCell(8).getStringCellValue().trim());
            indexTemplateDeriveDTO.setBusinessLeaderName(row.getCell(9) == null ? "" : row.getCell(9).getStringCellValue().trim());
            indexTemplateDeriveDTO.setTechnicalCaliber(row.getCell(10) == null ? "" : row.getCell(10).getStringCellValue().trim());
            indexTemplateDeriveDTO.setTechnicalLeaderName(row.getCell(11) == null ? "" : row.getCell(11).getStringCellValue().trim());
            indexTemplateDeriveDTO.setApprovalGroupName(row.getCell(12) == null ? "" : row.getCell(12).getStringCellValue().trim());
            String checkResult = indexTemplateDeriveDTO.checkFieldsIsNull();
            if (StringUtils.isNotBlank(checkResult)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, checkResult);
            }
            indexTemplateDeriveDTOList.add(indexTemplateDeriveDTO);
        }
        if (CollectionUtils.isEmpty(indexTemplateDeriveDTOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内容不能为空");
        }
        Set<String> nameSet = new HashSet<>();
        Set<String> identificationSet = new HashSet<>();
        indexTemplateDeriveDTOList.stream().forEach(indexTemplateDeriveDTO -> {
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateDeriveDTO.getIndexName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称仅支持中文、英文和数字");
            }
            if (indexTemplateDeriveDTO.getIndexName().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称不能超过128位");
            }
            if (!Pattern.matches("^[a-z0-9_]+$", indexTemplateDeriveDTO.getIndexIdentification())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识仅支持小写英文、数字和“_”");
            }
            if (indexTemplateDeriveDTO.getIndexIdentification().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识不能超过128位");
            }
            if (indexTemplateDeriveDTO.getDescription().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "描述不能超过300位");
            }
            if (indexTemplateDeriveDTO.getBusinessCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务口径不能超过128位");
            }
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateDeriveDTO.getBusinessCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务口径仅支持中文、英文和数字");
            }
            if (indexTemplateDeriveDTO.getTechnicalCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术口径不能超过128位");
            }
            if (StringUtils.isNotBlank(indexTemplateDeriveDTO.getTechnicalCaliber()) && !Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateDeriveDTO.getTechnicalCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术口径仅支持中文、英文和数字");
            }
            nameSet.add(indexTemplateDeriveDTO.getIndexName());
            identificationSet.add(indexTemplateDeriveDTO.getIndexIdentification());
        });
        if (indexTemplateDeriveDTOList.size() != nameSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称存在重复数据");
        }
        if (indexTemplateDeriveDTOList.size() != identificationSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识存在重复数据");
        }
        if (indexTemplateDeriveDTOList.size() > 100) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "单次指标批量导入上限为100");
        }
        return indexTemplateDeriveDTOList;
    }


    /**
     * 获取复合指标数据
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<IndexTemplateCompositeDTO> getCompositeIndexData(File file) throws Exception {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
        Sheet sheet = xssfWorkbook.getSheetAt(0);
        List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList = new ArrayList<>();
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            IndexTemplateCompositeDTO indexTemplateCompositeDTO = new IndexTemplateCompositeDTO();
            indexTemplateCompositeDTO.setDependentIndicesName(row.getCell(0) == null ? "" : row.getCell(0).getStringCellValue().trim());
            indexTemplateCompositeDTO.setIndexName(row.getCell(1) == null ? "" : row.getCell(1).getStringCellValue().trim());
            indexTemplateCompositeDTO.setIndexIdentification(row.getCell(2) == null ? "" : row.getCell(2).getStringCellValue().trim());
            indexTemplateCompositeDTO.setDescription(row.getCell(3) == null ? "" : row.getCell(3).getStringCellValue().trim());
            String centralName = row.getCell(4) == null ? "" : row.getCell(4).getStringCellValue().trim();
            if ("是".equals(centralName)) {
                indexTemplateCompositeDTO.setCentral(true);
            } else {
                indexTemplateCompositeDTO.setCentral(false);
            }
            indexTemplateCompositeDTO.setIndexFieldName(row.getCell(5) == null ? "" : row.getCell(5).getStringCellValue().trim());
            indexTemplateCompositeDTO.setExpression(row.getCell(6) == null ? "" : row.getCell(6).getStringCellValue().trim());
            indexTemplateCompositeDTO.setBusinessCaliber(row.getCell(7) == null ? "" : row.getCell(7).getStringCellValue().trim());
            indexTemplateCompositeDTO.setBusinessLeaderName(row.getCell(8) == null ? "" : row.getCell(8).getStringCellValue().trim());
            indexTemplateCompositeDTO.setTechnicalCaliber(row.getCell(9) == null ? "" : row.getCell(9).getStringCellValue().trim());
            indexTemplateCompositeDTO.setTechnicalLeaderName(row.getCell(10) == null ? "" : row.getCell(10).getStringCellValue().trim());
            indexTemplateCompositeDTO.setApprovalGroupName(row.getCell(11) == null ? "" : row.getCell(11).getStringCellValue().trim());
            String checkResult = indexTemplateCompositeDTO.checkFieldsIsNull();
            if (StringUtils.isNotBlank(checkResult)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, checkResult);
            }
            indexTemplateCompositeDTOList.add(indexTemplateCompositeDTO);
        }
        if (CollectionUtils.isEmpty(indexTemplateCompositeDTOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内容不能为空");
        }
        Set<String> nameSet = new HashSet<>();
        Set<String> identificationSet = new HashSet<>();
        indexTemplateCompositeDTOList.stream().forEach(indexTemplateCompositeDTO -> {
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateCompositeDTO.getIndexName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称仅支持中文、英文和数字");
            }
            if (indexTemplateCompositeDTO.getIndexName().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称不能超过128位");
            }
            if (!Pattern.matches("^[a-z0-9_]+$", indexTemplateCompositeDTO.getIndexIdentification())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识仅支持小写英文、数字和“_”");
            }
            if (indexTemplateCompositeDTO.getIndexIdentification().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识不能超过128位");
            }
            if (indexTemplateCompositeDTO.getDescription().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "描述不能超过300位");
            }
            if (indexTemplateCompositeDTO.getExpression().length() > 300) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "设定表达式不能超过300位");
            }
            if (indexTemplateCompositeDTO.getBusinessCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务口径不能超过128位");
            }
            if (!Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateCompositeDTO.getBusinessCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务口径仅支持中文、英文和数字");
            }
            if (indexTemplateCompositeDTO.getTechnicalCaliber().length() > 128) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术口径不能超过128位");
            }
            if (StringUtils.isNotBlank(indexTemplateCompositeDTO.getTechnicalCaliber()) && !Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", indexTemplateCompositeDTO.getTechnicalCaliber())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术口径仅支持中文、英文和数字");
            }
            nameSet.add(indexTemplateCompositeDTO.getIndexName());
            identificationSet.add(indexTemplateCompositeDTO.getIndexIdentification());
        });
        if (indexTemplateCompositeDTOList.size() != nameSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称存在重复数据");
        }
        if (indexTemplateCompositeDTOList.size() != identificationSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标标识存在重复数据");
        }
        if (indexTemplateCompositeDTOList.size() > 100) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "单次指标批量导入上限为100");
        }
        return indexTemplateCompositeDTOList;
    }

    /**
     * 批量导入原子指标
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void importBatchAtomIndex(File file, String tenantId) throws Exception {
        if (!file.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        List<IndexTemplateAtomDTO> indexTemplateAtomDTOList = this.getAtomIndexData(file);
        List<IndexDTO> indexDTOList = new ArrayList<>();
        //数据重复校验
        List<String> nameList = new ArrayList<>();
        List<String> identificationList = new ArrayList<>();
        Set<String> fieldSet = new HashSet<>(16);
        Set<String> groupSet = new HashSet<>(16);
        indexTemplateAtomDTOList.stream().forEach(indexTemplateAtomDTO -> {
            nameList.add(indexTemplateAtomDTO.getName());
            identificationList.add(indexTemplateAtomDTO.getIdentification());
            fieldSet.add(indexTemplateAtomDTO.getField());
            groupSet.add(indexTemplateAtomDTO.getApprove());
            IndexDTO indexDTO = new IndexDTO();
            indexDTO.setIndexName(indexTemplateAtomDTO.getName());
            indexDTO.setIndexIdentification(indexTemplateAtomDTO.getIdentification());
            indexDTO.setDescription(indexTemplateAtomDTO.getDescription());
            if ("是".equals(indexTemplateAtomDTO.getCentral())) {
                indexDTO.setCentral(true);
            } else {
                indexDTO.setCentral(false);
            }
            indexDTO.setIndexFieldName(indexTemplateAtomDTO.getField());
            indexDTO.setSourceName(indexTemplateAtomDTO.getSource());
            indexDTO.setDbName(indexTemplateAtomDTO.getDbName());
            indexDTO.setTableName(indexTemplateAtomDTO.getTableName());
            indexDTO.setColumnName(indexTemplateAtomDTO.getColumnName());
            indexDTO.setBusinessCaliber(indexTemplateAtomDTO.getBusinessCaliber());
            indexDTO.setBusinessLeaderName(indexTemplateAtomDTO.getBusinessLeader());
            indexDTO.setTechnicalCaliber(indexTemplateAtomDTO.getTechnicalCaliber());
            indexDTO.setTechnicalLeaderName(indexTemplateAtomDTO.getTechnicalLeader());
            indexDTO.setApprovalGroupName(indexTemplateAtomDTO.getApprove());
            indexDTOList.add(indexDTO);
        });
        List<IndexAtomicPO> indexAtomicPOList = indexDAO.selectAtomListByIndexNameOrIdentification(tenantId, nameList, identificationList);
        if (!CollectionUtils.isEmpty(indexAtomicPOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或者指标标识已经存在");
        }
        //数据有效性校验
        //指标域
        this.getField(tenantId, fieldSet, indexDTOList);
        //数据源
        this.getDatasourceId(tenantId, indexDTOList);
        //业务负责人 技术负责人
        this.getUserId(tenantId, indexDTOList);
        //审批管理
        this.getApproveGroupId(tenantId, groupSet, indexDTOList);
        indexAtomicPOList = new ArrayList<>();
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        for (IndexDTO indexDTO : indexDTOList) {
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

    /**
     * 批量导入派生指标
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void importBatchDeriveIndex(File file, String tenantId) throws Exception {
        if (!file.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList = this.getDeriveIndexData(file);
        //数据重复校验
        Set<String> atomIndexName = new HashSet<>(16);
        Set<String> timeLimitName = new HashSet<>(16);
        Set<String> modifiersName = new HashSet<>(16);
        List<String> nameList = new ArrayList<>();
        List<String> identificationList = new ArrayList<>();

        Set<String> fieldSet = new HashSet<>(16);
        Set<String> groupSet = new HashSet<>(16);
        indexTemplateAtomDTOList.stream().forEach(indexTemplateAtomDTO -> {
            atomIndexName.add(indexTemplateAtomDTO.getIndexAtomicName());
            timeLimitName.add(indexTemplateAtomDTO.getTimeLimitName());
            indexTemplateAtomDTO.setModifiersNameList(this.getModifiers(indexTemplateAtomDTO.getModifiersName()));
            modifiersName.addAll(indexTemplateAtomDTO.getModifiersNameList());
            nameList.add(indexTemplateAtomDTO.getIndexName());
            identificationList.add(indexTemplateAtomDTO.getIndexIdentification());
            fieldSet.add(indexTemplateAtomDTO.getIndexFieldName());
            groupSet.add(indexTemplateAtomDTO.getApprovalGroupName());
        });

        List<IndexDerivePO> indexAtomicPOList = indexDAO.selectDeriveListByNameAndIdentification(tenantId, nameList, identificationList);
        if (!CollectionUtils.isEmpty(indexAtomicPOList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或者指标标识已经存在");
        }

        //数据有效性校验
        //依赖原子指标
        this.getAtomIndex(tenantId, atomIndexName, indexTemplateAtomDTOList);
        //时间限定
        this.getTimeLimitByName(tenantId, timeLimitName, indexTemplateAtomDTOList);
        //修饰词
        this.getModifierId(tenantId, modifiersName, indexTemplateAtomDTOList);
        //指标域
        this.getFieldDerive(tenantId, fieldSet, indexTemplateAtomDTOList);
        //业务负责人 技术负责人
        this.getUserIdDerive(tenantId, indexTemplateAtomDTOList);
        //审批管理
        this.getApproveGroupIdDerive(tenantId, groupSet, indexTemplateAtomDTOList);

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
        dataManageService.addBatchDeriveIndex(indexDerivePOList, indexDeriveModifierRelationPOList);
    }


    /**
     * 批量导入复合指标
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void importBatchCompositeIndex(File file, String tenantId) throws Exception {
        if (!file.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList = this.getCompositeIndexData(file);
        //数据重复校验
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标名称或者指标标识已经存在");
        }

        //数据有效性校验
        //依赖派生指标
        this.getCompositeIndex(tenantId, deriveIndexName, indexTemplateCompositeDTOList);
        //指标域
        this.getFieldComposite(tenantId, fieldSet, indexTemplateCompositeDTOList);
        //业务负责人 技术负责人
        this.getUserIdComposite(tenantId, indexTemplateCompositeDTOList);
        //审批管理
        this.getApproveGroupIdComposite(tenantId, groupSet, indexTemplateCompositeDTOList);

        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        indexCompositePOList = new ArrayList<>();
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
            IndexDeriveCompositeRelationPO indexDeriveCompositeRelationPO = new IndexDeriveCompositeRelationPO();
            for (String s : indexTemplateCompositeDTO.getDependentIndicesId()) {
                indexDeriveCompositeRelationPO.setCompositeIndexId(icp.getIndexId());
                indexDeriveCompositeRelationPO.setDeriveIndexId(s);
                indexDeriveCompositeRelationPOList.add(indexDeriveCompositeRelationPO);
            }
        }
        dataManageService.addBatchCompositeIndex(indexCompositePOList, indexDeriveCompositeRelationPOList);
    }

    /**
     * 审批管理-派生
     *
     * @param tenantId
     * @param groupSet
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getApproveGroupIdDerive(String tenantId, Set<String> groupSet, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        List<ApproveGroupListAndSearchResult> approveGroupListAndSearchResultList = approveGroupDAO.selectListByName(tenantId, groupSet);
        if (groupSet.size() != approveGroupListAndSearchResultList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审批管理名称不存在");
        }
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            for (ApproveGroupListAndSearchResult approveGroupListAndSearchResult : approveGroupListAndSearchResultList) {
                if (indexTemplateDeriveDTO.getApprovalGroupName().equals(approveGroupListAndSearchResult.getName())) {
                    indexTemplateDeriveDTO.setApprovalGroupId(approveGroupListAndSearchResult.getId());
                }
            }
        }
    }


    /**
     * 审批管理-复合
     *
     * @param tenantId
     * @param groupSet
     * @param indexTemplateCompositeDTOList
     * @throws Exception
     */
    private void getApproveGroupIdComposite(String tenantId, Set<String> groupSet, List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) throws Exception {
        List<ApproveGroupListAndSearchResult> approveGroupListAndSearchResultList = approveGroupDAO.selectListByName(tenantId, groupSet);
        if (groupSet.size() != approveGroupListAndSearchResultList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审批管理名称不存在");
        }
        for (IndexTemplateCompositeDTO indexTemplateCompositeDTO : indexTemplateCompositeDTOList) {
            for (ApproveGroupListAndSearchResult approveGroupListAndSearchResult : approveGroupListAndSearchResultList) {
                if (indexTemplateCompositeDTO.getApprovalGroupName().equals(approveGroupListAndSearchResult.getName())) {
                    indexTemplateCompositeDTO.setApprovalGroupId(approveGroupListAndSearchResult.getId());
                }
            }
        }
    }


    /**
     * 业务负责人、技术负责人-派生
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取业务负责人信息失败");
        }
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            for (User user : userList) {
                if (indexTemplateDeriveDTO.getBusinessLeaderName().equals(user.getUsername())) {
                    indexTemplateDeriveDTO.setBusinessLeader(user.getUserId());
                }
                if (indexTemplateDeriveDTO.getTechnicalLeaderName().equals(user.getUsername())) {
                    indexTemplateDeriveDTO.setTechnicalLeader(user.getUserId());
                }
            }
            if (StringUtils.isBlank(indexTemplateDeriveDTO.getBusinessLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务负责人不存在");
            }
            if (StringUtils.isNotBlank(indexTemplateDeriveDTO.getTechnicalLeaderName()) && StringUtils.isBlank(indexTemplateDeriveDTO.getTechnicalLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术负责人不存在");
            }
        }
    }


    /**
     * 业务负责人、技术负责人-复合
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取业务负责人信息失败");
        }
        for (IndexTemplateCompositeDTO indexTemplateCompositeDTO : indexTemplateCompositeDTOList) {
            for (User user : userList) {
                if (indexTemplateCompositeDTO.getBusinessLeaderName().equals(user.getUsername())) {
                    indexTemplateCompositeDTO.setBusinessLeader(user.getUserId());
                }
                if (indexTemplateCompositeDTO.getTechnicalLeaderName().equals(user.getUsername())) {
                    indexTemplateCompositeDTO.setTechnicalLeader(user.getUserId());
                }
            }
            if (StringUtils.isBlank(indexTemplateCompositeDTO.getBusinessLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务负责人不存在");
            }
            if (StringUtils.isNotBlank(indexTemplateCompositeDTO.getTechnicalLeaderName()) && StringUtils.isBlank(indexTemplateCompositeDTO.getTechnicalLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术负责人不存在");
            }
        }
    }

    /**
     * 指标域-派生
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域名称不存在");
        }
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            for (CategoryEntityV2 categoryEntityV2 : categoryEntityV2List) {
                if (indexTemplateDeriveDTO.getIndexFieldName().equals(categoryEntityV2.getName())) {
                    indexTemplateDeriveDTO.setIndexFieldId(categoryEntityV2.getGuid());
                }
            }
        }
    }

    /**
     * 指标域-复合
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域名称不存在");
        }
        for (IndexTemplateCompositeDTO indexTemplateCompositeDTO : indexTemplateCompositeDTOList) {
            for (CategoryEntityV2 categoryEntityV2 : categoryEntityV2List) {
                if (indexTemplateCompositeDTO.getIndexFieldName().equals(categoryEntityV2.getName())) {
                    indexTemplateCompositeDTO.setIndexFieldId(categoryEntityV2.getGuid());
                }
            }
        }
    }

    /**
     * 获取修饰词
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
     * 获取修饰词
     *
     * @param tenantId
     * @param modifiersName
     * @param indexTemplateAtomDTOList
     */
    private void getModifierId(String tenantId, Set<String> modifiersName, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) {
        List<Data> dataList = qualifierDAO.getQualifierListByName(tenantId, modifiersName);
        if (modifiersName.size() != dataList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词不存在");
        }
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            List<String> list = new ArrayList<>();
            for (String s : indexTemplateDeriveDTO.getModifiersNameList()) {
                for (Data data : dataList) {
                    if (s.equals(data.getName())) {
                        list.add(data.getId());
                    }
                }
            }
            indexTemplateDeriveDTO.setModifiers(list);
        }
    }

    /**
     * 获取原子指标依赖
     *
     * @param tenantId
     * @param atomIndexName
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getAtomIndex(String tenantId, Set<String> atomIndexName, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        List<IndexAtomicPO> indexAtomicPOList = indexDAO.selectAtomListByName(tenantId, atomIndexName);
        if (atomIndexName.size() != indexAtomicPOList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "依赖原子指标不存在");
        }
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            for (IndexAtomicPO indexAtomicPO : indexAtomicPOList) {
                if (indexTemplateDeriveDTO.getIndexAtomicName().equals(indexAtomicPO.getIndexName())) {
                    indexTemplateDeriveDTO.setIndexAtomicId(indexAtomicPO.getIndexId());
                }
            }
        }
    }

    /**
     * @param tenantId
     * @param deriveIndexName
     * @param indexTemplateCompositeDTOList
     */
    private void getCompositeIndex(String tenantId, Set<String> deriveIndexName, List<IndexTemplateCompositeDTO> indexTemplateCompositeDTOList) {
        List<IndexDerivePO> indexDerivePOList = indexDAO.selectDeriveByName(tenantId, deriveIndexName);
        if (deriveIndexName.size() != indexDerivePOList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "依赖派生指标不存在");
        }
        for (IndexTemplateCompositeDTO indexTemplateCompositeDTO : indexTemplateCompositeDTOList) {
            List<String> dependentIndicesIdList = new ArrayList<>();
            for (String dependentIndicesName : indexTemplateCompositeDTO.getDependentIndicesNameS()) {
                for (IndexDerivePO indexDerivePO : indexDerivePOList) {
                    if (dependentIndicesName.equals(indexDerivePO.getIndexName())) {
                        dependentIndicesIdList.add(indexDerivePO.getIndexId());
                    }
                }
            }
            indexTemplateCompositeDTO.setDependentIndicesId(dependentIndicesIdList);
        }
    }

    /**
     * 时间限定
     *
     * @param tenantId
     * @param timeLimitName
     * @param indexTemplateAtomDTOList
     * @throws Exception
     */
    private void getTimeLimitByName(String tenantId, Set<String> timeLimitName, List<IndexTemplateDeriveDTO> indexTemplateAtomDTOList) throws Exception {
        List<TimelimitEntity> timeLimitEntityList = timeLimitDAO.getTimeLimitListByName(tenantId, timeLimitName);
        if (timeLimitEntityList.size() != timeLimitName.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "时间限定不存在");
        }
        for (IndexTemplateDeriveDTO indexTemplateDeriveDTO : indexTemplateAtomDTOList) {
            for (TimelimitEntity timelimitEntity : timeLimitEntityList) {
                if (indexTemplateDeriveDTO.getTimeLimitName().equals(timelimitEntity.getName())) {
                    indexTemplateDeriveDTO.setTimeLimitId(timelimitEntity.getId());
                }
            }
        }
    }

    /**
     * 指标域
     *
     * @param tenantId
     * @param fieldSet
     * @param indexDTOList
     * @throws Exception
     */
    private void getField(String tenantId, Set<String> fieldSet, List<IndexDTO> indexDTOList) throws Exception {
        User user = AdminUtils.getUserData();
        List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        List<CategoryEntityV2> categoryEntityV2List = categoryDAO.selectGuidByTenantIdAndGroupIdAndName(fieldSet, tenantId, userGroupIds);
        if (categoryEntityV2List.size() != fieldSet.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域名称不存在");
        }
        for (IndexDTO indexDTO : indexDTOList) {
            for (CategoryEntityV2 categoryEntityV2 : categoryEntityV2List) {
                if (indexDTO.getIndexFieldName().equals(categoryEntityV2.getName())) {
                    indexDTO.setIndexFieldId(categoryEntityV2.getGuid());
                }
            }
        }
    }

    /**
     * 审批管理
     *
     * @param tenantId
     * @param groupSet
     * @param indexDTOList
     * @throws Exception
     */
    private void getApproveGroupId(String tenantId, Set<String> groupSet, List<IndexDTO> indexDTOList) throws Exception {
        List<ApproveGroupListAndSearchResult> approveGroupListAndSearchResultList = approveGroupDAO.selectListByName(tenantId, groupSet);
        if (groupSet.size() != approveGroupListAndSearchResultList.size()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审批管理名称不存在");
        }
        for (IndexDTO indexDTO : indexDTOList) {
            for (ApproveGroupListAndSearchResult approveGroupListAndSearchResult : approveGroupListAndSearchResultList) {
                if (indexDTO.getApprovalGroupName().equals(approveGroupListAndSearchResult.getName())) {
                    indexDTO.setApprovalGroupId(approveGroupListAndSearchResult.getId());
                }
            }
        }
    }

    /**
     * 业务负责人、技术负责人
     *
     * @param tenantId
     * @param indexDTOList
     * @throws Exception
     */
    private void getUserId(String tenantId, List<IndexDTO> indexDTOList) throws Exception {
        Parameters parameters = new Parameters();
        parameters.setOffset(0);
        parameters.setLimit(-1);
        PageResult<User> pageResult = usersService.getUserListV2(tenantId, parameters);
        List<User> userList = pageResult.getLists();
        if (CollectionUtils.isEmpty(userList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取业务负责人信息失败");
        }
        for (IndexDTO indexDTO : indexDTOList) {
            for (User user : userList) {
                if (indexDTO.getBusinessLeaderName().equals(user.getUsername())) {
                    indexDTO.setBusinessLeader(user.getUserId());
                }
                if (indexDTO.getTechnicalLeaderName().equals(user.getUsername())) {
                    indexDTO.setTechnicalLeader(user.getUserId());
                }
            }
            if (StringUtils.isBlank(indexDTO.getBusinessLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务负责人不存在");
            }
            if (StringUtils.isNotBlank(indexDTO.getTechnicalLeaderName()) && StringUtils.isBlank(indexDTO.getTechnicalLeader())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "技术负责人不存在");
            }
        }
    }

    /**
     * 数据源-字段
     *
     * @param tenantId
     * @param indexDTOList
     * @throws Exception
     */
    private void getDatasourceId(String tenantId, List<IndexDTO> indexDTOList) throws Exception {
        List<String> hiveDbList = null;
        for (IndexDTO indexDTO : indexDTOList) {
            if ("hive".equalsIgnoreCase(indexDTO.getSourceName())) {
                if (CollectionUtils.isEmpty(hiveDbList)) {
                    hiveDbList = tenantService.getDatabase(tenantId);
                }
                TableInfoId tableInfoId = tableDAO.selectHiveIdByName(hiveDbList, indexDTO.getDbName(), indexDTO.getTableName(), indexDTO.getColumnName());
                if (Objects.isNull(tableInfoId)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, String.format("数据源hive-字段(%s)信息匹配失败", indexDTO.getColumnName()));
                }
                indexDTO.setSourceId("hive");
                indexDTO.setTableId(tableInfoId.getTableGuid());
                indexDTO.setColumnId(tableInfoId.getColumnGuid());
            } else {
                TableInfoId tableInfoId = tableDAO.selectRdbmsIdByName(tenantId, indexDTO.getSourceName(), indexDTO.getDbName(), indexDTO.getTableName(), indexDTO.getColumnName());
                if (Objects.isNull(tableInfoId)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, String.format("数据源-字段(%s)信息匹配失败", indexDTO.getColumnName()));
                }
                indexDTO.setSourceId(tableInfoId.getSourceId());
                indexDTO.setTableId(tableInfoId.getTableGuid());
                indexDTO.setColumnId(tableInfoId.getColumnGuid());
            }
        }
    }

}
