package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.datasource.DataSourceTypeInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.CategorycateQueryResult;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.Constant;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.DeriveTableStateEnum;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.MetadataDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.BusinessCategory;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.CategoryGuidPath;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceBusinessInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.DeriveTableVersion;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceColumnEntity;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveColumnVO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveTableColumnVO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveTableVO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceTableEntity;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.TechnicalCategory;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.DbDAO;
import io.zeta.metaspace.web.dao.SourceInfoDeriveTableInfoDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.rest.BusinessREST;
import io.zeta.metaspace.web.rest.TechnicalREST;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveColumnInfo;
import io.zeta.metaspace.model.dto.sourceinfo.SourceInfoDeriveTableColumnDTO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableColumnRelation;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 衍生表信息表 服务实现类
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Service
public class SourceInfoDeriveTableInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(SourceInfoDeriveTableInfoService.class);

    private SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDao;
    private DbDAO dbDao;
    private TableDAO tableDAO;
    private ColumnDAO columnDAO;
    private BusinessDAO businessDAO;
    private CategoryDAO categoryDAO;
    private SourceInfoDeriveColumnInfoService sourceInfoDeriveColumnInfoService;
    private SourceInfoDeriveTableColumnRelationService sourceInfoDeriveTableColumnRelationService;
    private BusinessREST businessREST;
    private TechnicalREST technicalREST;
    private DataSourceService dataSourceService;

    @Autowired
    public SourceInfoDeriveTableInfoService(SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDao, DbDAO dbDao, BusinessDAO businessDAO,
                                            TableDAO tableDAO, ColumnDAO columnDAO, BusinessREST businessREST, TechnicalREST technicalREST,
                                            CategoryDAO categoryDAO, SourceInfoDeriveColumnInfoService sourceInfoDeriveColumnInfoService,
                                            SourceInfoDeriveTableColumnRelationService sourceInfoDeriveTableColumnRelationService,
                                            DataSourceService dataSourceService) {
        this.sourceInfoDeriveTableInfoDao = sourceInfoDeriveTableInfoDao;
        this.dbDao = dbDao;
        this.tableDAO = tableDAO;
        this.columnDAO = columnDAO;
        this.businessDAO = businessDAO;
        this.categoryDAO = categoryDAO;
        this.businessREST = businessREST;
        this.technicalREST = technicalREST;
        this.dataSourceService = dataSourceService;
        this.sourceInfoDeriveColumnInfoService = sourceInfoDeriveColumnInfoService;
        this.sourceInfoDeriveTableColumnRelationService = sourceInfoDeriveTableColumnRelationService;
    }

    /**
     * 新建-保存或保存并提交
     *
     * @param sourceInfoDeriveTableColumnDto
     * @param tenantId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createSaveAndSubmitDeriveTableInfo(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId) {
        User user = AdminUtils.getUserData();
        // 表
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = new SourceInfoDeriveTableInfo();
        BeanUtils.copyProperties(sourceInfoDeriveTableColumnDto, sourceInfoDeriveTableInfo);
        // 字段
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();

        // 英文名设置为小写
        sourceInfoDeriveTableInfo.setTableNameEn(sourceInfoDeriveTableInfo.getTableNameEn().toLowerCase());
        // 设置主键id和tableGuid
        sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());
        sourceInfoDeriveTableInfo.setTableGuid(UUID.randomUUID().toString());
        sourceInfoDeriveTableInfo.setCreator(user.getUserId());
        sourceInfoDeriveTableInfo.setCreateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setUpdater(user.getUserId());
        sourceInfoDeriveTableInfo.setUpdateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setTenantId(tenantId);
        sourceInfoDeriveTableInfo.setVersion(-1);

        // 操作是保存，状态是0
        if (!sourceInfoDeriveTableColumnDto.isSubmit()) {
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.UN_COMMIT.getState());
        } else {
            // 保存并提交，设置状态是1
            // 生成DDL和DML语句
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.COMMIT.getState());
            sourceInfoDeriveTableInfo.setDdl(createDDL(sourceInfoDeriveTableColumnDto, getDbNameByDbId(sourceInfoDeriveTableInfo.getDbId(), sourceInfoDeriveTableInfo.getSourceId())));
            sourceInfoDeriveTableInfo.setDml(createDML(sourceInfoDeriveTableColumnDto));
        }

        sourceInfoDeriveColumnInfos.forEach(deriveColumnInfo -> {
            deriveColumnInfo.setId(UUID.randomUUID().toString());
            deriveColumnInfo.setColumnGuid(UUID.randomUUID().toString());
            deriveColumnInfo.setTableGuid(sourceInfoDeriveTableInfo.getTableGuid());
            deriveColumnInfo.setTenantId(tenantId);
        });


        // 表-字段关系
        List<SourceInfoDeriveTableColumnRelation> sourceInfoDeriveTableColumnRelationList = sourceInfoDeriveColumnInfos.stream().map(e -> {
            SourceInfoDeriveTableColumnRelation sourceInfoDeriveTableColumnRelation = new SourceInfoDeriveTableColumnRelation();
            sourceInfoDeriveTableColumnRelation.setId(UUID.randomUUID().toString());
            sourceInfoDeriveTableColumnRelation.setColumnGuid(e.getColumnGuid());
            sourceInfoDeriveTableColumnRelation.setTableId(sourceInfoDeriveTableInfo.getId());
            sourceInfoDeriveTableColumnRelation.setTableGuid(sourceInfoDeriveTableInfo.getTableGuid());
            return sourceInfoDeriveTableColumnRelation;
        }).collect(Collectors.toList());

        this.save(sourceInfoDeriveTableInfo);
        sourceInfoDeriveColumnInfoService.saveBatch(sourceInfoDeriveColumnInfos);
        sourceInfoDeriveTableColumnRelationService.saveBatch(sourceInfoDeriveTableColumnRelationList);
        return true;
    }

    private String getDbNameByDbId(String dbId, String sourceId) {
        // 获取数据库的名称对应
        List<Map<String, String>> listMaps = queryDbNameAndSourceNameByIds(dbId, sourceId);
        Map<String, String> maps = listMaps.stream().collect(Collectors.toMap(e -> e.get("id"), e -> e.get("name")));
        return maps.getOrDefault(dbId, "");
    }

    /**
     * 编辑-保存或保存并提交
     *
     * @param sourceInfoDeriveTableColumnDto
     * @param tenantId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSaveAndSubmitDeriveTableInfo(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId) {
        User user = AdminUtils.getUserData();
        // 原表的id和guid
        String tableId = sourceInfoDeriveTableColumnDto.getId();
        String tableGuid = sourceInfoDeriveTableColumnDto.getTableGuid();
        // 表
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = new SourceInfoDeriveTableInfo();
        BeanUtils.copyProperties(sourceInfoDeriveTableColumnDto, sourceInfoDeriveTableInfo);
        // 列
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        // 表英文名设置为小写
        sourceInfoDeriveTableInfo.setTableNameEn(sourceInfoDeriveTableInfo.getTableNameEn().toLowerCase());
        sourceInfoDeriveTableInfo.setCreateTime(LocalDateTime.parse(sourceInfoDeriveTableColumnDto.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        sourceInfoDeriveTableInfo.setUpdater(user.getUserId());
        sourceInfoDeriveTableInfo.setUpdateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setTenantId(tenantId);

        // 操作保存
        if (!sourceInfoDeriveTableColumnDto.isSubmit()) {
            // 如果上次是已提交
            if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
                // 把当前数据的版本设置为0，设置新id
                this.updateVersionByTableId(tableId, 0);
                sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());
            } else { // 上次是保存
                // 删除tableId对应的列和关系
                sourceInfoDeriveColumnInfoService.deleteDeriveColumnInfoByTableId(tableId, tableGuid);
                sourceInfoDeriveTableColumnRelationService.deleteDeriveTableColumnRelationByTableId(tableId);
            }
            sourceInfoDeriveTableInfo.setVersion(-1);
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.UN_COMMIT.getState());

        } else {// 操作是保存并提交
            boolean hasModify = checkUpdateHasModify(sourceInfoDeriveTableInfo, sourceInfoDeriveColumnInfos, tenantId);
            // 如果上次是已提交
            if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
                if (hasModify) {
                    // 先修改该表的所有版本+1
                    this.updateVersionByTableGuid(tableGuid);
                    // 把当前数据的版本设置为1，设置新id
                    this.updateVersionByTableId(tableId, 1);
                    sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());
                } else {
                    return true;
                }
            } else { // 上次是保存
                // 删除tableId对应的列和关系
                sourceInfoDeriveColumnInfoService.deleteDeriveColumnInfoByTableId(tableId, tableGuid);
                sourceInfoDeriveTableColumnRelationService.deleteDeriveTableColumnRelationByTableId(tableId);
                if (hasModify) {
                    // 先修改该表的所有版本+1，然后新增
                    this.updateVersionByTableGuid(tableGuid);
                } else {
                    sourceInfoDeriveTableInfoDao.updateVersionToShowByTableGuid(tableGuid);
                    // 删除tableId对应的列和关系
                    sourceInfoDeriveTableInfoDao.deleteDeriveTableByTableId(tableId);
                    return true;
                }
            }

            // 设置状态是1
            // 生成DDL和DML语句
            // 版本是1
            sourceInfoDeriveTableInfo.setVersion(-1);
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.COMMIT.getState());
            sourceInfoDeriveTableInfo.setDdl(createDDL(sourceInfoDeriveTableColumnDto, getDbNameByDbId(sourceInfoDeriveTableInfo.getDbId(), sourceInfoDeriveTableInfo.getSourceId())));
            sourceInfoDeriveTableInfo.setDml(createDML(sourceInfoDeriveTableColumnDto));

        }
        // 表-字段关系
        List<SourceInfoDeriveTableColumnRelation> sourceInfoDeriveTableColumnRelationList = new ArrayList<>(sourceInfoDeriveColumnInfos.size());

        // 查询该表的所有column，和需要新增的列进行对比
        List<SourceInfoDeriveColumnInfo> deriveColumnInfoListByTableGuid = sourceInfoDeriveColumnInfoService.getDeriveColumnInfoListByTableGuid(sourceInfoDeriveTableInfo.getTableGuid());

        // 已存在的将ID设置为null，稍后要进行剔除，relation中添加已存在的guid
        // 不存在的重新设置id和guid。
        sourceInfoDeriveColumnInfos = sourceInfoDeriveColumnInfos.stream().peek(deriveColumnInfo -> {
            deriveColumnInfo.setTenantId(tenantId);
            deriveColumnInfo.setTableGuid(sourceInfoDeriveTableInfo.getTableGuid());
            List<SourceInfoDeriveColumnInfo> collect = deriveColumnInfoListByTableGuid.stream().filter(e -> e.equals(deriveColumnInfo)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                deriveColumnInfo.setId(null);
                deriveColumnInfo.setColumnGuid(collect.get(0).getColumnGuid());
            } else {
                deriveColumnInfo.setId(UUID.randomUUID().toString());
                deriveColumnInfo.setColumnGuid(UUID.randomUUID().toString());
            }
            // 生成表-字段关系
            SourceInfoDeriveTableColumnRelation sourceInfoDeriveTableColumnRelation = new SourceInfoDeriveTableColumnRelation();
            sourceInfoDeriveTableColumnRelation.setId(UUID.randomUUID().toString());
            sourceInfoDeriveTableColumnRelation.setColumnGuid(deriveColumnInfo.getColumnGuid());
            sourceInfoDeriveTableColumnRelation.setTableId(sourceInfoDeriveTableInfo.getId());
            sourceInfoDeriveTableColumnRelation.setTableGuid(sourceInfoDeriveTableInfo.getTableGuid());
            sourceInfoDeriveTableColumnRelationList.add(sourceInfoDeriveTableColumnRelation);
        }).filter(deriveColumnInfo -> StringUtils.isNotBlank(deriveColumnInfo.getId())).collect(Collectors.toList());

        this.saveOrUpdate(sourceInfoDeriveTableInfo);
        // 有新增的列入库
        if (!CollectionUtils.isEmpty(sourceInfoDeriveColumnInfos)) {
            sourceInfoDeriveColumnInfoService.saveOrUpdateBatch(sourceInfoDeriveColumnInfos);
        }
        sourceInfoDeriveTableColumnRelationService.saveOrUpdateBatch(sourceInfoDeriveTableColumnRelationList);
        return true;
    }

    boolean checkUpdateHasModify(SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo, List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos, String tenantId) {
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfoDB = null;
        if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
            // todo 上次是保存并提交，和-1版本的进行比较
            sourceInfoDeriveTableInfoDB = sourceInfoDeriveTableInfoDao.getByGuidAndTenantIdAndVersion(sourceInfoDeriveTableInfo.getTableGuid(), tenantId, -1);
        } else {
            // todo 上次是保存，和0版本的进行比较
            sourceInfoDeriveTableInfoDB = sourceInfoDeriveTableInfoDao.getByGuidAndTenantIdAndVersion(sourceInfoDeriveTableInfo.getTableGuid(), tenantId, 0);
        }
        if (!sourceInfoDeriveTableInfo.equals(sourceInfoDeriveTableInfoDB)) {
            return true;
        }
        List<SourceInfoDeriveColumnInfo> deriveColumnInfoListByTableId = sourceInfoDeriveColumnInfoService.getDeriveColumnInfoListByTableId(sourceInfoDeriveTableInfoDB.getId());
        sourceInfoDeriveColumnInfos.forEach(deriveColumnInfo -> {
            deriveColumnInfo.setTenantId(tenantId);
            deriveColumnInfo.setTableGuid(sourceInfoDeriveTableInfo.getTableGuid());
        });
        if (!deriveColumnInfoListByTableId.containsAll(sourceInfoDeriveColumnInfos) ||
                !sourceInfoDeriveColumnInfos.containsAll(deriveColumnInfoListByTableId)) {
            return true;
        }
        return false;
    }


    /**
     * 根据tableguid修改版本
     *
     * @param tableGuid
     * @return
     */
    public boolean updateVersionByTableGuid(String tableGuid) {
        sourceInfoDeriveTableInfoDao.updateVersionByTableGuid(tableGuid);
        return true;
    }

    /**
     * 查询衍生表列表
     *
     * @param tenantId
     * @param tableName
     * @param state
     * @param offset
     * @param limit
     * @return
     */
    public List<SourceInfoDeriveTableVO> queryDeriveTableList(String tenantId, String tableName, Integer state, int offset, int limit) {
        tableName = StringUtils.isEmpty(tableName) ? null : "%" + tableName + "%";
        List<SourceInfoDeriveTableInfo> sourceInfoDeriveTableInfos = sourceInfoDeriveTableInfoDao.queryDeriveTableList(tenantId, tableName, state, offset, limit);

        // 获取租户下所有的技术目录guid - path
        int TECHNIACL_CATEGORY_TYPE = 0;
        Map<String, String> technicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, TECHNIACL_CATEGORY_TYPE, null);
        // 获取该租户下所有的业务目录guid - path
        int BUSINESS_CATEGORY_TYPE = 1;
        Map<String, String> businessCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, BUSINESS_CATEGORY_TYPE, null);

        List<SourceInfoDeriveTableVO> sourceInfoDeriveTableVOS = sourceInfoDeriveTableInfos.stream().map(sourceInfoDeriveTableInfo -> {
            SourceInfoDeriveTableVO sourceInfoDeriveTableVO = new SourceInfoDeriveTableVO();
            BeanUtils.copyProperties(sourceInfoDeriveTableInfo, sourceInfoDeriveTableVO);
            sourceInfoDeriveTableVO.setUpdateTime(sourceInfoDeriveTableInfo.getUpdateTimeStr());
            sourceInfoDeriveTableVO.setUpdater(sourceInfoDeriveTableInfo.getUpdaterName());
            sourceInfoDeriveTableVO.setBusiness(getBusiness(sourceInfoDeriveTableInfo.getBusinessId(), businessCategoryGuidPathMap));
            sourceInfoDeriveTableVO.setCategory(technicalCategoryGuidPathMap.getOrDefault(sourceInfoDeriveTableInfo.getCategoryId(), ""));
            sourceInfoDeriveTableVO.setQueryDDL(StringUtils.isNotBlank(sourceInfoDeriveTableInfo.getDdl()));
            return sourceInfoDeriveTableVO;
        }).collect(Collectors.toList());
        return sourceInfoDeriveTableVOS;
    }

    private boolean updateVersionByTableId(String tableId, int version) {
        return sourceInfoDeriveTableInfoDao.updateVersionByTableId(tableId, version) > 0;
    }

    /**
     * 查看衍生表的历史版本
     *
     * @param tableGuid
     * @param offset
     * @param limit
     * @return
     */
    public PageResult<DeriveTableVersion> getDeriveTableVersion(String tableGuid, int offset, int limit) {
        PageResult<DeriveTableVersion> pageResult = new PageResult<>();
        List<DeriveTableVersion> deriveTableVersions = sourceInfoDeriveTableInfoDao.queryVersionByTableGuid(tableGuid, offset, limit);
        pageResult.setCurrentSize(deriveTableVersions.size());
        pageResult.setOffset(offset);
        pageResult.setTotalSize(CollectionUtils.isEmpty(deriveTableVersions) ? 0 : deriveTableVersions.get(0).getTotal());
        pageResult.setLists(deriveTableVersions);
        return pageResult;
    }

    /**
     * 根据tableGuid和租户id获取信息
     *
     * @param tenantId
     * @param tableGuids
     * @return
     */
    public List<String> getByGuidsAndTenantId(String tenantId, List<String> tableGuids) {
        return sourceInfoDeriveTableInfoDao.getByGuidsAndTenantId(tenantId, tableGuids);
    }

    /**
     * 保存
     *
     * @param sourceInfoDeriveTableInfo
     * @return
     */
    public boolean save(SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo) {
        return sourceInfoDeriveTableInfoDao.add(sourceInfoDeriveTableInfo) > 0;
    }

    /**
     * 保存或修改
     *
     * @param sourceInfoDeriveTableInfo
     * @return
     */
    public boolean saveOrUpdate(SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo) {
        return sourceInfoDeriveTableInfoDao.upsert(sourceInfoDeriveTableInfo) > 0;
    }

    /**
     * 根据主键id和租户id获取信息
     *
     * @param tableId
     * @param tenantId
     * @return
     */
    public SourceInfoDeriveTableInfo getByIdAndTenantId(String tableId, String tenantId) {
        return sourceInfoDeriveTableInfoDao.getByIdAndTenantId(tableId, tenantId);
    }

    /**
     * 组合技术目录
     *
     * @param source 是否读取贴源层
     * @return
     */
    public List<TechnicalCategory> getTechnicalCategory(boolean source, String tenantId) {
        List<CategoryPrivilege> categories = technicalREST.getCategories("ASC", tenantId);
        // 根据技术目录的guid在db_info和source_info表中查询出哪些是已经登记的数据库
        List<TechnicalCategory> databases = queryDbTypeByCategoryIds(tenantId, categories.stream().map(CategoryPrivilege::getGuid).collect(Collectors.toList()));

        // 转成categoryId -> dbType 的map
        Map<String, TechnicalCategory> categoryIdDbTypeMap = databases.stream().collect(Collectors.toMap(TechnicalCategory::getGuid, e -> e));

        // 遍历到新对象
        // 设置database属性和dbType属性
        List<TechnicalCategory> technicalCategories = categories.stream().map(e -> {
            TechnicalCategory technicalCategory = new TechnicalCategory();
            BeanUtils.copyProperties(e, technicalCategory);
            TechnicalCategory technicalCategory1 = categoryIdDbTypeMap.get(technicalCategory.getGuid());
            if (null != technicalCategory1) {
                technicalCategory.setDbType(technicalCategory1.getDbType());
                technicalCategory.setDataBase(StringUtils.isNotBlank(technicalCategory.getDbType()));
                technicalCategory.setDbId(technicalCategory1.getDbId());
                technicalCategory.setSourceId(technicalCategory1.getSourceId());
            }
            return technicalCategory;
        }).collect(Collectors.toList());

        // 过滤出database和父目录
        technicalCategories = getDatabaseCategoryAll(technicalCategories);

        // 是否读取贴源层
        if (!source) {
            removeSourceAll(technicalCategories);
        }

        setUpAndDown(technicalCategories, null);

        return technicalCategories;
    }

    /**
     * 给所有的平级设置up和dpwn
     *
     * @param technicalCategories
     * @param parentId            父id，找到子节点
     */
    public void setUpAndDown(List<TechnicalCategory> technicalCategories, String parentId) {
        List<TechnicalCategory> collect = technicalCategories.stream().filter(e -> Objects.equals(e.getParentCategoryGuid(), parentId)).sorted(Comparator.comparing(TechnicalCategory::getGuid)).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            TechnicalCategory technicalCategory = collect.get(i);
            technicalCategory.setUpBrotherCategoryGuid(i == 0 ? null : collect.get(i - 1).getGuid());
            technicalCategory.setDownBrotherCategoryGuid(i == collect.size() - 1 ? null : collect.get(i + 1).getGuid());
            setUpAndDown(technicalCategories, technicalCategory.getGuid());
        }
    }

    /**
     * 获取所有数据库挂载目录以及父目录
     *
     * @param technicalCategories
     */
    private List<TechnicalCategory> getDatabaseCategoryAll(List<TechnicalCategory> technicalCategories) {
        List<TechnicalCategory> databaseAndParentList = new ArrayList<>();
        // 和数据库关联的目录
        List<String> databaseCategoryIds = technicalCategories.stream().filter(TechnicalCategory::isDataBase).map(TechnicalCategory::getGuid).collect(Collectors.toList());
        getDatabaseCategoryAllPath(technicalCategories, databaseAndParentList, databaseCategoryIds);
        databaseAndParentList = databaseAndParentList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(BusinessCategory::getGuid))), ArrayList::new));
        return databaseAndParentList;
    }

    /**
     * 移除贴源层以及子目录
     *
     * @param technicalCategories
     */
    private void removeSourceAll(List<TechnicalCategory> technicalCategories) {
        List<String> sourceCategoryList = new ArrayList<>();
        List<String> collect = technicalCategories.stream().filter(e -> "贴源层".equals(e.getName())).map(TechnicalCategory::getGuid).collect(Collectors.toList());
        getSourceAll(technicalCategories, sourceCategoryList, collect);
        technicalCategories.removeIf(e -> sourceCategoryList.contains(e.getGuid()));
    }

    /**
     * 获取所有数据库挂载目录以及父目录
     *
     * @param allList
     * @param databaseAndParentList
     * @param categoryIds
     */
    private void getDatabaseCategoryAllPath(List<TechnicalCategory> allList, List<TechnicalCategory> databaseAndParentList, List<String> categoryIds) {
        List<TechnicalCategory> collect = allList.stream().filter(e -> categoryIds.contains(e.getGuid())).collect(Collectors.toList());
        databaseAndParentList.addAll(collect);
        List<String> parentCategoryIds = collect.stream().map(TechnicalCategory::getParentCategoryGuid).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(parentCategoryIds)) {
            getDatabaseCategoryAllPath(allList, databaseAndParentList, parentCategoryIds);
        }
    }

    /**
     * 获取所有贴源层以及贴源层的子目录
     *
     * @param allList
     * @param sourceCategoryList
     * @param parentCategoryIds
     */
    private void getSourceAll(List<TechnicalCategory> allList, List<String> sourceCategoryList, List<String> parentCategoryIds) {
        sourceCategoryList.addAll(parentCategoryIds);
        List<TechnicalCategory> collect = allList.stream().filter(e -> parentCategoryIds.contains(e.getParentCategoryGuid())).collect(Collectors.toList());
        List<String> categoryIds = collect.stream().map(TechnicalCategory::getGuid).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(categoryIds)) {
            getSourceAll(allList, sourceCategoryList, categoryIds);
        }
    }

    /**
     * 根据源数据层/库ID查询对应的数据表
     *
     * @param categoryId
     * @return
     */
    public List<SourceTableEntity> getSourceTableByCategoryId(String categoryId) {
        List<TableInfo> tableInfos = tableDAO.getTableInfoByCategoryId(categoryId);
        return tableInfos.stream().map(e -> {
            SourceTableEntity sourceTableEntity = new SourceTableEntity();
            sourceTableEntity.setCategoryGuid(categoryId);
            sourceTableEntity.setTableGuid(e.getTableGuid());
            sourceTableEntity.setTableName(e.getTableName());
            return sourceTableEntity;
        }).collect(Collectors.toList());
    }

    /**
     * 根据sourceTableId查询对应字段
     *
     * @param sourceTableId
     * @return
     */
    public List<SourceColumnEntity> getColumnInfoByTableId(String sourceTableId) {
        TableInfo tableInfo = tableDAO.getTableInfoByTableguidAndStatus(sourceTableId);
        List<Column> columnInfoListByTableGuid = columnDAO.getColumnInfoListByTableGuid(sourceTableId);
        return columnInfoListByTableGuid.stream().map(e -> {
            SourceColumnEntity sourceColumnEntity = new SourceColumnEntity();
            sourceColumnEntity.setSourceColumnGuid(e.getColumnId());
            sourceColumnEntity.setSourceColumnNameEn(e.getColumnName());
            sourceColumnEntity.setSourceColumnNameZh(e.getDescription());
            sourceColumnEntity.setSourceColumnType(e.getType());
            sourceColumnEntity.setSourceTableGuid(tableInfo.getTableGuid());
            sourceColumnEntity.setSourceTableNameEn(tableInfo.getTableName());
            sourceColumnEntity.setSourceTableNameZh(tableInfo.getDescription());
            sourceColumnEntity.setDataBaseName(tableInfo.getDbName());
            return sourceColumnEntity;
        }).collect(Collectors.toList());
    }

    private List<TechnicalCategory> queryDbTypeByCategoryIds(String tenantId, List<String> categoryIds) {
        return dbDao.queryDbTypeByCategoryIds(tenantId, categoryIds);
    }


    /**
     * 获取拼接的业务目录和业务对象
     *
     * @param businessId
     * @param businessCategoryGuidPathMap
     * @return
     */
    private String getBusiness(String businessId, Map<String, String> businessCategoryGuidPathMap) {
        try {
            // 根据业务对象id获取关联的业务目录id
            BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(businessId);
            if (null == businessInfo) {
                return "";
            }
            String s = businessCategoryGuidPathMap.get(businessInfo.getDepartmentId());
            return null == s ? null : (s + "/" + businessInfo.getName());
        } catch (Exception e) {
            LOG.error("拼接业务目录异常");
            return "";
        }
    }

    /**
     * 获取业务目录
     *
     * @param tenantId
     * @return
     */
    public List<BusinessCategory> getBusinessCategory(String tenantId) {
        List<CategorycateQueryResult> categories = businessREST.getCategories("ASC", 1,tenantId);
        return categories.stream().map(e -> {
            BusinessCategory businessCategory = new BusinessCategory();
            BeanUtils.copyProperties(e, businessCategory);
            return businessCategory;
        }).collect(Collectors.toList());
    }

    /**
     * 根据租户id获取和目录类型获取id对应的目录全路径
     *
     * @param tenantId     租户id
     * @param categoryType 目录类型，0：技术目录 1：业务目录
     * @return
     */
    public Map<String, String> getCategoryGuidPathMap(String tenantId, int categoryType, String guid) {
        List<CategoryGuidPath> guidPathByTenantIdAndCategoryType = StringUtils.isBlank(guid) ?
                categoryDAO.getGuidPathByTenantIdAndCategoryType(tenantId, categoryType) :
                categoryDAO.getGuidPathByTenantIdAndCategoryTypeAndId(tenantId, categoryType, guid);
        return guidPathByTenantIdAndCategoryType.stream().collect(Collectors.toMap(CategoryGuidPath::getGuid, CategoryGuidPath::getPath));
    }

    /**
     * 根据业务目录id获取关联的业务对象
     *
     * @param categoryId 业务目录id
     * @param tenantId
     * @return
     */
    public List<SourceBusinessInfo> getBusinessByCategoryId(String categoryId, String tenantId) {
        Parameters parameters = new Parameters();
        // 不分页
        parameters.setLimit(-1);
        parameters.setOffset(0);
        PageResult<BusinessInfoHeader> businessListWithCondition = businessREST.getBusinessListWithCondition(categoryId, parameters, tenantId);
        List<BusinessInfoHeader> businessInfoHeaders = businessListWithCondition.getLists();
        return businessInfoHeaders.stream().map(e -> {
            SourceBusinessInfo sourceBusinessInfo = new SourceBusinessInfo();
            BeanUtils.copyProperties(e, sourceBusinessInfo);
            return sourceBusinessInfo;
        }).collect(Collectors.toList());
    }


    public SourceInfoDeriveTableColumnVO queryDeriveTableInfo(String tenantId, String sourceId, String schemaId, String tableGuid) {
        Table table = tableDAO.getDbAndTableName(tableGuid);
        if(table == null){
            LOG.info("该表 {} 不存在",tableGuid);
            return null;
        }
        List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId,sourceId,schemaId,table.getTableName());
        if(org.apache.commons.collections.CollectionUtils.isEmpty(deriveTableInfoList)){
            return null;
        }
        Optional<SourceInfoDeriveTableInfo> deriveTableInfoOpt = deriveTableInfoList.stream().sorted(Comparator.comparing(SourceInfoDeriveTableInfo::getVersion).reversed()).findFirst();
        if(deriveTableInfoOpt.isPresent()){
            SourceInfoDeriveTableInfo tableInfo = deriveTableInfoOpt.get();
            SourceInfoDeriveTableColumnVO info = new SourceInfoDeriveTableColumnVO();
            BeanUtils.copyProperties(tableInfo,info);
            int TECHNIACL_CATEGORY_TYPE = 0;
            Map<String, String> technicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, TECHNIACL_CATEGORY_TYPE, info.getCategoryId());
            info.setCategory(technicalCategoryGuidPathMap.getOrDefault(info.getCategoryId(), ""));

            BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(info.getBusinessId());
            if (null != businessInfo) {
                info.setBusiness(businessInfo.getName());
                info.setBusinessHeaderId(businessInfo.getDepartmentId());
                // 获取该租户下所有的业务目录guid - path
                int BUSINESS_CATEGORY_TYPE = 1;
                Map<String, String> businessCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, BUSINESS_CATEGORY_TYPE, businessInfo.getDepartmentId());
                info.setBusinessHeader(businessCategoryGuidPathMap.getOrDefault(businessInfo.getDepartmentId(), ""));
            }

            // 获取数据库、数据源的id.name对应
            List<Map<String, String>> maps = queryDbNameAndSourceNameByIds(schemaId, sourceId);
            // id->name对应
            Map<String, String> collect = maps.stream().collect(Collectors.toMap(e -> e.get("id"), e -> e.get("name")));

            info.setDbName(collect.get(schemaId));
            info.setSourceName(collect.get(sourceId));

            return info;
        }
        return null;
    }

    /**
     * 根据主键id查询衍生表详情
     *
     * @param tenantId
     * @param tableId
     * @return
     */
    public SourceInfoDeriveTableColumnVO getDeriveTableColumnDetail(String tenantId, String tableId) throws SQLException {

        // 根据主键id查询衍生表
        SourceInfoDeriveTableInfo byId = this.getByIdAndTenantId(tableId, tenantId);
        if (null == byId) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "衍生表不存在");
        }

        // 查询主键id查询列
        List<SourceInfoDeriveColumnInfo> deriveColumnInfoListByTableId = sourceInfoDeriveColumnInfoService.getDeriveColumnInfoListByTableId(tableId);
        SourceInfoDeriveTableColumnVO sourceInfoDeriveTableColumnVO = new SourceInfoDeriveTableColumnVO();
        BeanUtils.copyProperties(byId, sourceInfoDeriveTableColumnVO);

        TableInfo sourceTableInfo = tableDAO.getTableInfoByTableguidAndStatus(byId.getSourceTableGuid());
        if (null == sourceTableInfo) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "原表不存在");
        }
        List<Column> sourceColumnInfoList = columnDAO.getColumnInfoListByTableGuid(byId.getSourceTableGuid());
        Map<String, Column> idColumnMap = sourceColumnInfoList.stream().collect(Collectors.toMap(Column::getColumnId, e -> e));

        String sourceCategoryId = categoryDAO.queryCategoryIdByGuidByDBId(sourceTableInfo.getDatabaseGuid(), tenantId);
        BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(sourceInfoDeriveTableColumnVO.getBusinessId());
        // 设置表的技术目录和业务目录
        // 获取租户下所有的技术目录guid - path
        int TECHNIACL_CATEGORY_TYPE = 0;
        Map<String, String> technicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, TECHNIACL_CATEGORY_TYPE, sourceInfoDeriveTableColumnVO.getCategoryId());
        Map<String, String> sourceTechnicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, TECHNIACL_CATEGORY_TYPE, sourceCategoryId);
        // 获取该租户下所有的业务目录guid - path
        int BUSINESS_CATEGORY_TYPE = 1;
        Map<String, String> businessCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, BUSINESS_CATEGORY_TYPE, businessInfo.getDepartmentId());
        if (null != businessInfo) {
            sourceInfoDeriveTableColumnVO.setBusiness(businessInfo.getName());
            sourceInfoDeriveTableColumnVO.setBusinessHeaderId(businessInfo.getDepartmentId());
            sourceInfoDeriveTableColumnVO.setBusinessHeader(businessCategoryGuidPathMap.getOrDefault(businessInfo.getDepartmentId(), ""));
        }

        sourceInfoDeriveTableColumnVO.setCategory(technicalCategoryGuidPathMap.getOrDefault(sourceInfoDeriveTableColumnVO.getCategoryId(), ""));
        sourceInfoDeriveTableColumnVO.setSourceTable(sourceTableInfo.getTableName());
        // 获取源数据层、库
        sourceInfoDeriveTableColumnVO.setSourceDbGuid(sourceCategoryId);
        sourceInfoDeriveTableColumnVO.setSourceDb(sourceTechnicalCategoryGuidPathMap.getOrDefault(sourceCategoryId, ""));

        // 开始设置数据库和数据源名称
        String dbId = sourceInfoDeriveTableColumnVO.getDbId();
        String sourceId = sourceInfoDeriveTableColumnVO.getSourceId();
        // 获取数据库、数据源的id.name对应
        List<Map<String, String>> maps = queryDbNameAndSourceNameByIds(dbId, sourceId);
        // id->name对应
        Map<String, String> collect = maps.stream().collect(Collectors.toMap(e -> e.get("id"), e -> e.get("name")));

        sourceInfoDeriveTableColumnVO.setDbName(collect.get(dbId));
        sourceInfoDeriveTableColumnVO.setSourceName(collect.get(sourceId));
        sourceInfoDeriveTableColumnVO.setCreateTime(byId.getCreateTimeStr());
        sourceInfoDeriveTableColumnVO.setUpdateTime(byId.getUpdateTimeStr());

        sourceInfoDeriveTableColumnVO.setSourceInfoDeriveColumnVOS(deriveColumnInfoListByTableId.stream().map(e -> {
            Column column = idColumnMap.get(e.getSourceColumnGuid());
            SourceInfoDeriveColumnVO sourceInfoDeriveColumnVO = new SourceInfoDeriveColumnVO();
            BeanUtils.copyProperties(e, sourceInfoDeriveColumnVO);
            sourceInfoDeriveColumnVO.setDataBaseName(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : sourceTableInfo.getDbName());
            sourceInfoDeriveColumnVO.setSourceTableGuid(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : sourceTableInfo.getTableGuid());
            sourceInfoDeriveColumnVO.setSourceTableNameEn(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : sourceTableInfo.getTableName());
            sourceInfoDeriveColumnVO.setSourceTableNameZh(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : sourceTableInfo.getDescription());
            sourceInfoDeriveColumnVO.setSourceColumnNameEn(null == column ? null : column.getColumnName());
            sourceInfoDeriveColumnVO.setSourceColumnNameZh(null == column ? null : column.getDescription());
            sourceInfoDeriveColumnVO.setSourceColumnType(null == column ? null : column.getType());
            return sourceInfoDeriveColumnVO;
        }).collect(Collectors.toList()));
        return sourceInfoDeriveTableColumnVO;
    }

    /**
     * 删除衍生表记录
     *
     * @param tableGuids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDeriveTable(List<String> tableGuids) {
        sourceInfoDeriveTableInfoDao.deleteByTableGuids(tableGuids);
        sourceInfoDeriveColumnInfoService.deleteByTableGuids(tableGuids);
        sourceInfoDeriveTableColumnRelationService.deleteByTableGuids(tableGuids);
        return true;
    }

    /**
     * 构造DDL
     */
    private String createDDL(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String dbName) {
        // 必须有长度的拼接长度
        String lengthStr = "(255)";
        String tableNameEn = sourceInfoDeriveTableColumnDto.getTableNameEn();
        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        StringBuilder tableDDL = new StringBuilder("CREATE TABLE ").append(dbName).append(".").append(tableNameEn).append("(\r\n");

        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        addTimeField(sourceInfoDeriveColumnInfos);
        StringBuilder columnDDL = new StringBuilder();
        StringBuilder commentDDL = new StringBuilder();
        StringBuilder primaryKeyDDLHeader = new StringBuilder("ALTER TABLE ").append(tableNameEn).append(" ADD PRIMARY KEY (");
        StringBuilder primaryKeyField = new StringBuilder();
        for (int i = 0; i < sourceInfoDeriveColumnInfos.size(); i++) {
            SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = sourceInfoDeriveColumnInfos.get(i);
            String columnNameEn = sourceInfoDeriveColumnInfo.getColumnNameEn();
            String dataType = sourceInfoDeriveColumnInfo.getDataType();
            // 对特殊的dbType进行拼接长度

            dataType = Constant.DATA_LENGTH_TYPE_MAP.get(dbType).contains(dataType) ? dataType.concat(lengthStr) : dataType;
            Map<String, String> stringStringMap = Constant.REPLACE_TYPE_MAP.get(dbType);
            if (!CollectionUtils.isEmpty(stringStringMap)) {
                dataType = stringStringMap.getOrDefault(dataType, dataType);
            }
            // 最后一个字段不用拼接逗号
            columnDDL.append(columnNameEn).append(" ").append(dataType);

            // 有中文名添加注释
            String columnNameZh = sourceInfoDeriveColumnInfo.getColumnNameZh();
            if (StringUtils.isNotBlank(columnNameZh)) {
                if (Arrays.asList(Constant.HIVE, Constant.MYSQL).contains(dbType.toUpperCase())) {
                    columnDDL.append(" COMMENT '").append(columnNameZh).append("'");
                } else {
                    commentDDL.append("COMMENT ON COLUMN ").append(tableNameEn).append(".").append(columnNameEn).append(" IS '").append(columnNameZh).append("';\r\n");
                }
            }
            if (i < sourceInfoDeriveColumnInfos.size() - 1) {
                columnDDL.append(",\r\n");
            }
            // 添加主键
            if (!Objects.equals(dbType, Constant.HIVE) && sourceInfoDeriveColumnInfo.isPrimaryKey()) {
                primaryKeyField.append(columnNameEn).append(",");
            }
        }
        tableDDL.append(columnDDL).append(")");
        StringBuilder tableComment = new StringBuilder();
        String tableNameZh = sourceInfoDeriveTableColumnDto.getTableNameZh();
        if (StringUtils.isNotBlank(tableNameZh)) {
            if (Arrays.asList(Constant.HIVE, Constant.MYSQL).contains(dbType.toUpperCase())) {
                tableComment.append("\r\nCOMMENT '").append(tableNameZh).append("'");
                tableDDL.append(tableComment);
            } else {
                tableComment.append("COMMENT ON TABLE ").append(dbName).append(".").append(tableNameEn).append(" IS '").append(tableNameZh).append("'");
                tableDDL.append(";\r\n").append(tableComment);
            }
        }
        tableDDL.append(";\r\n");
        if (StringUtils.isNotBlank(primaryKeyField.toString())) {
            primaryKeyDDLHeader.append(primaryKeyField.substring(0, primaryKeyField.length() - 1)).append(");\r\n");
            tableDDL.append(primaryKeyDDLHeader);
        }
        tableDDL.append(commentDDL);
        removeTimeField(sourceInfoDeriveColumnInfos);
        return tableDDL.toString();
    }


    /**
     * 构建DML语句
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    private String createDML(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto) {
        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        // 数据库类型获取数据类型-替换值
        Map<String, Object> stringObjectMap = Constant.REPLACE_DATE_MAP.get(dbType);
        String tableNameEn = sourceInfoDeriveTableColumnDto.getTableNameEn();
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        addTimeField(sourceInfoDeriveColumnInfos);
        StringBuilder columnBuilder = new StringBuilder("insert into ").append(tableNameEn).append("(\r\n");
        StringBuilder valueBuilder = new StringBuilder("values (\r\n");
        for (int i = 0; i < sourceInfoDeriveColumnInfos.size(); i++) {
            SourceInfoDeriveColumnInfo e = sourceInfoDeriveColumnInfos.get(i);
            String columnNameEn = e.getColumnNameEn();
            String dataType = e.getDataType();
            boolean primaryKey = e.isPrimaryKey();
            boolean groupField = e.isGroupField();
            boolean removeSensitive = e.isRemoveSensitive();
            boolean important = e.isImportant();
            boolean secret = e.isSecret();
            boolean permissionField = e.isPermissionField();
            String secretPeriod = e.getSecretPeriod();
            String mappingRule = e.getMappingRule();
            String mappingDescribe = e.getMappingDescribe();
            String remark = e.getRemark();
            columnBuilder.append(columnNameEn);
            if (i < sourceInfoDeriveColumnInfos.size() - 1) {
                columnBuilder.append(",");
            }
            StringBuilder remarkBuilder = new StringBuilder();
            if (primaryKey)
                remarkBuilder.append("主键;");
            if (groupField)
                remarkBuilder.append("分组字段;");
            if (important)
                remarkBuilder.append("重要;");
            if (removeSensitive)
                remarkBuilder.append("脱敏;");
            if (permissionField)
                remarkBuilder.append("权限字段;");
            if (secret)
                remarkBuilder.append("保密;");
            if (StringUtils.isNotBlank(secretPeriod))
                remarkBuilder.append("保密期限:").append(secretPeriod).append(";");
            if (StringUtils.isNotBlank(mappingRule))
                remarkBuilder.append("映射规则:").append(mappingRule).append(";");
            if (StringUtils.isNotBlank(mappingDescribe))
                remarkBuilder.append("映射说明:").append(mappingDescribe).append(";");
            if (StringUtils.isNotBlank(remark))
                remarkBuilder.append("备注:").append(remark).append(";");
            if (StringUtils.isNotEmpty(remarkBuilder.toString()))
                columnBuilder.append(" --  ").append(remarkBuilder);
            columnBuilder.append("\r\n");
            valueBuilder.append(stringObjectMap.get(dataType));
            if (i < sourceInfoDeriveColumnInfos.size() - 1) {
                valueBuilder.append(",\r\n");
            }
        }
        removeTimeField(sourceInfoDeriveColumnInfos);
        columnBuilder.append(")").append(valueBuilder).append(");");
        return columnBuilder.toString();
    }

    private void addTimeField(List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = new SourceInfoDeriveColumnInfo();
        sourceInfoDeriveColumnInfo.setId("delete");
        sourceInfoDeriveColumnInfo.setColumnNameEn("etl_date");
        sourceInfoDeriveColumnInfo.setColumnNameZh("抽数时间");
        sourceInfoDeriveColumnInfo.setDataType("timestamp");
        sourceInfoDeriveColumnInfo.setMappingDescribe("每行的抽取时间");
        sourceInfoDeriveColumnInfos.add(sourceInfoDeriveColumnInfo);
    }

    private void removeTimeField(List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        sourceInfoDeriveColumnInfos.removeIf(e -> Objects.equals("delete", e.getId()));
    }

    public Result checkAddOrEditDeriveTableEntity(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId) throws SQLException {

        // 源信息登记数据源类型的参数
        String sourceInfoDbTypeKey = "dbr";

        // 默认的时间字段
        String timeField = "etl_date";

        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        if (StringUtils.isEmpty(dbType)) {
            return ReturnUtil.error("400", "数据源类型不能为空");
        }
        // 校验数据源类型
        List<DataSourceTypeInfo> dataSourceType = dataSourceService.getDataSourceType(sourceInfoDbTypeKey);
        if (dataSourceType.stream().noneMatch(e -> e.getName().equalsIgnoreCase(dbType))) {
            return ReturnUtil.error("400", "数据源类型不符合规范");
        }

        if (!checkTableNameDump(sourceInfoDeriveTableColumnDto.getTableNameEn(), sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getId())) {
            return ReturnUtil.error("400", "目标库下表英文名已存在");
        }
        // 检验表英文名
        if (!checkTableOrColumnNameEnPattern(sourceInfoDeriveTableColumnDto.getTableNameEn())) {
            return ReturnUtil.error("400", "衍生表英文名不符合规范");
        }
        // 检验更新频率
        if (StringUtils.isEmpty(sourceInfoDeriveTableColumnDto.getUpdateFrequency())) {
            return ReturnUtil.error("400", "更新频率不能为空");
        }
        // 字段
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();

        // 校验是否有etl_date
        if (sourceInfoDeriveColumnInfos.stream().anyMatch(e -> Objects.equals(timeField, e.getColumnNameEn()))) {
            return ReturnUtil.error("400", "衍生表字段不能包含默认字段:" + timeField);
        }
        // 校验字段英文名
        List<String> errorNames = sourceInfoDeriveColumnInfos.stream().filter(e -> !checkColumnNameEn(e.getColumnNameEn())).map(SourceInfoDeriveColumnInfo::getColumnNameEn).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorNames)) {
            return ReturnUtil.error("400", "衍生表字段英文名不符合规范:" + errorNames.toString());
        }
        // 校验重名
        List<String> dumpNames = sourceInfoDeriveColumnInfos.stream().collect(Collectors.groupingBy(SourceInfoDeriveColumnInfo::getColumnNameEn, Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dumpNames)) {
            return ReturnUtil.error("400", "衍生表字段英文名不允许重复:" + dumpNames.toString());
        }
        // 根据数据源类型校验数据类型
        List<String> dataTypeList = Constant.DATA_TYPE_MAP.get(dbType);
        List<String> errorDbTypes = sourceInfoDeriveColumnInfos.stream().filter(e -> !dataTypeList.contains(e.getDataType())).map(SourceInfoDeriveColumnInfo::getDataType).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorDbTypes)) {
            return ReturnUtil.error("400", "衍生表字段数据类型不符合规范:" + errorDbTypes.toString());
        }
        if (dbType.equals(Constant.ORACLE)) {
            long count = sourceInfoDeriveColumnInfos.stream().filter(e -> Arrays.asList("long", "long raw").contains(e.getDataType())).count();
            if (count > 1) {
                return ReturnUtil.error("400", "一张表最多存在一个long类型数据，包含'long' 和 'long raw'数据类型");
            }
        }
        // 校验技术目录
        if (!checkCategoryByGuid(sourceInfoDeriveTableColumnDto.getCategoryId(), tenantId)) {
            return ReturnUtil.error("400", "目标层/库为空或不存在");
        }
        // 校验业务对象
        if (!checkBusinessByGuid(sourceInfoDeriveTableColumnDto.getBusinessId())) {
            return ReturnUtil.error("400", "业务对象为空或不存在");
        }
        // 校验数据库和数据源
        if (!checkDatabaseAndDataSource(sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getSourceId())) {
            return ReturnUtil.error("400", "数据库或数据源不存在");
        }
        // 校验源表
        if (!checkSourceTableByGuid(sourceInfoDeriveTableColumnDto.getSourceTableGuid())) {
            return ReturnUtil.error("400", "源表不存在");
        }
        // 校验源字段
        List<String> sourceColumnIds = sourceInfoDeriveColumnInfos.stream().map(SourceInfoDeriveColumnInfo::getSourceColumnGuid).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(sourceColumnIds) && !checkSourceColumnsByGuid(sourceColumnIds)) {
            return ReturnUtil.error("400", "部分源字段不存在");
        }
        return ReturnUtil.success();
    }

    /**
     * 校验目标库下已登记的衍生表和目标库已存在的表
     *
     * @param tableName
     * @param dbId
     * @return
     */
    public boolean checkTableNameDump(String tableName, String dbId, String tableId) {
        int count = sourceInfoDeriveTableInfoDao.checkTableNameDump(tableName, dbId, tableId);
        return count == 0;
    }

    /**
     * 校验tableId和tableGuid
     *
     * @param id
     * @param tenantId
     * @return
     */
    public SourceInfoDeriveTableInfo getTableByIdAndGuid(String id, String guid, String tenantId) {
        return sourceInfoDeriveTableInfoDao.getByIdAndGuidAndTenantId(id, guid, tenantId);
    }

    /**
     * 校验categoryid
     *
     * @param guid
     * @param tenantId
     * @return
     * @throws SQLException
     */
    boolean checkCategoryByGuid(String guid, String tenantId) throws SQLException {
        return StringUtils.isNotBlank(guid) && null != categoryDAO.queryByGuidV2(guid, tenantId);
    }

    /**
     * 校验businessid
     *
     * @param guid
     * @return
     */
    boolean checkBusinessByGuid(String guid) {
        return StringUtils.isNotBlank(guid) && null != businessDAO.queryBusinessByBusinessId(guid);
    }

    /**
     * 校验sourceTableGuid
     *
     * @param guid
     * @return
     */
    boolean checkSourceTableByGuid(String guid) {
        return StringUtils.isNotBlank(guid) && null != tableDAO.getTableInfoByTableguidAndStatus(guid);

    }

    /**
     * 校验sourceColumnGuid
     *
     * @return
     */
    boolean checkSourceColumnsByGuid(List<String> guids) {
        List<String> sourceColumnInfoList = columnDAO.queryColumnidBycolumnIds(guids);
        return sourceColumnInfoList.containsAll(guids);
    }

    /**
     * 校验数据库id和数据源id
     *
     * @return
     */
    boolean checkDatabaseAndDataSource(String dbId, String sourceId) {
        if (StringUtils.isEmpty(dbId) || StringUtils.isEmpty(sourceId)) {
            return false;
        }
        List<Map<String, String>> maps = queryDbNameAndSourceNameByIds(dbId, sourceId);
        return maps.size() == 2;
    }


    public boolean checkTableOrColumnNameEnPattern(String name) {
        return name.matches(Constant.PATTERN);
    }

    public boolean checkColumnNameEn(String name) {
        return checkTableOrColumnNameEnPattern(name) && !Constant.HIVE_KEYWORD.contains(name.toUpperCase());
    }

    private List<Map<String, String>> queryDbNameAndSourceNameByIds(String dbId, String sourceId) {
        List<Map<String, String>> maps = dbDao.queryDbNameAndSourceNameByIds(dbId, sourceId);
        if ("HIVE".equalsIgnoreCase(sourceId)) {
            Map<String, String> hiveHashMap = new HashMap<>();
            hiveHashMap.put("id", "hive");
            hiveHashMap.put("name", "hive");
            maps.add(hiveHashMap);
        }
        return maps;
    }

    /**
     * 根据数据库类型获取数据类型
     *
     * @param dbType
     * @return
     */
    public Result getDataTypeByDbType(String dbType) {
        // 源信息登记数据源类型的参数
        String sourceInfoDbTypeKey = "dbr";
        // 校验数据源类型，支持
        List<DataSourceTypeInfo> dataSourceType = dataSourceService.getDataSourceType(sourceInfoDbTypeKey);
        if (dataSourceType.stream().noneMatch(e -> e.getName().equalsIgnoreCase(dbType))) {
            return ReturnUtil.error("400", "数据源类型不符合规范");
        }
        List<String> list = Constant.DATA_TYPE_MAP.get(dbType);
        if (CollectionUtils.isEmpty(list)) {
            return ReturnUtil.error("400", "数据源类型不符合规范");
        }
        return ReturnUtil.success(list);
    }
}
