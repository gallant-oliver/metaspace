package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.Constant;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.DeriveTableStateEnum;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.BusinessCategory;
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
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveColumnInfo;
import io.zeta.metaspace.model.dto.sourceinfo.SourceInfoDeriveTableColumnDTO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableColumnRelation;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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

    @Autowired
    public SourceInfoDeriveTableInfoService(SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDao, DbDAO dbDao, BusinessDAO businessDAO,
                                            TableDAO tableDAO, ColumnDAO columnDAO, BusinessREST businessREST, TechnicalREST technicalREST,
                                            CategoryDAO categoryDAO, SourceInfoDeriveColumnInfoService sourceInfoDeriveColumnInfoService,
                                            SourceInfoDeriveTableColumnRelationService sourceInfoDeriveTableColumnRelationService) {
        this.sourceInfoDeriveTableInfoDao = sourceInfoDeriveTableInfoDao;
        this.dbDao = dbDao;
        this.tableDAO = tableDAO;
        this.columnDAO = columnDAO;
        this.businessDAO = businessDAO;
        this.categoryDAO = categoryDAO;
        this.businessREST = businessREST;
        this.technicalREST = technicalREST;
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
            sourceInfoDeriveTableInfo.setDdl(createDDL(sourceInfoDeriveTableColumnDto));
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
            // 如果上次是已提交
            if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
                // 先修改该表的所有版本+1
                this.updateVersionByTableGuid(tableGuid);
                // 把当前数据的版本设置为1，设置新id
                this.updateVersionByTableId(tableId, 1);
                sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());
            } else { // 上次是保存
                // 先修改该表的所有版本+1，然后新增
                this.updateVersionByTableGuid(tableGuid);
                // 删除tableId对应的列和关系
                sourceInfoDeriveColumnInfoService.deleteDeriveColumnInfoByTableId(tableId, tableGuid);
                sourceInfoDeriveTableColumnRelationService.deleteDeriveTableColumnRelationByTableId(tableId);
            }

            // 设置状态是1
            // 生成DDL和DML语句
            // 版本是1
            sourceInfoDeriveTableInfo.setVersion(-1);
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.COMMIT.getState());
            sourceInfoDeriveTableInfo.setDdl(createDDL(sourceInfoDeriveTableColumnDto));
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
        }).filter(deriveColumnInfo -> !StringUtils.isEmpty(deriveColumnInfo.getId())).collect(Collectors.toList());

        this.saveOrUpdate(sourceInfoDeriveTableInfo);
        // 有新增的列入库
        if (!CollectionUtils.isEmpty(sourceInfoDeriveColumnInfos)) {
            sourceInfoDeriveColumnInfoService.saveOrUpdateBatch(sourceInfoDeriveColumnInfos);
        }
        sourceInfoDeriveTableColumnRelationService.saveOrUpdateBatch(sourceInfoDeriveTableColumnRelationList);
        return true;
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
        List<SourceInfoDeriveTableVO> sourceInfoDeriveTableVOS = sourceInfoDeriveTableInfos.stream().map(sourceInfoDeriveTableInfo -> {
            SourceInfoDeriveTableVO sourceInfoDeriveTableVO = new SourceInfoDeriveTableVO();
            BeanUtils.copyProperties(sourceInfoDeriveTableInfo, sourceInfoDeriveTableVO);
            sourceInfoDeriveTableVO.setBusiness(getBusiness(sourceInfoDeriveTableInfo.getBusinessId(), tenantId));
            sourceInfoDeriveTableVO.setCategory(getCategory(sourceInfoDeriveTableInfo.getCategoryId(), tenantId));
            sourceInfoDeriveTableVO.setState(DeriveTableStateEnum.getName(sourceInfoDeriveTableInfo.getState()));
            sourceInfoDeriveTableVO.setQueryDDL(!StringUtils.isEmpty(sourceInfoDeriveTableInfo.getDdl()));
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
     * 删除衍生表
     *
     * @param tableGuids
     * @return
     */
    public boolean deleteByTableGuids(List<String> tableGuids) {
        return sourceInfoDeriveTableInfoDao.deleteByTableGuids(tableGuids) > 0;
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
                technicalCategory.setDataBase(!StringUtils.isEmpty(technicalCategory.getDbType()));
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

        // 看所有节点的兄弟节点是否存在，不存在设置为null
        List<String> allCategoryIds = technicalCategories.stream().map(TechnicalCategory::getGuid).collect(Collectors.toList());
        technicalCategories.forEach(e -> {
            e.setUpBrotherCategoryGuid(allCategoryIds.contains(e.getUpBrotherCategoryGuid()) ? e.getUpBrotherCategoryGuid() : null);
            e.setDownBrotherCategoryGuid(allCategoryIds.contains(e.getDownBrotherCategoryGuid()) ? e.getDownBrotherCategoryGuid() : null);
        });
        return technicalCategories;
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
     * @param tenantId
     * @return
     */
    private String getBusiness(String businessId, String tenantId) {
        // 根据业务对象id获取关联的业务目录id
        BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(businessId);
        // 获取该租户下所有的业务目录
        List<BusinessCategory> businessCategoryList = getBusinessCategory(tenantId);
        return getBusinessCategoryParentPath(businessCategoryList, businessInfo);
    }

    /**
     * 获取拼接的技术目录
     *
     * @param categoryId
     * @param tenantId
     * @return
     */
    private String getCategory(String categoryId, String tenantId) {
        List<TechnicalCategory> technicalCategoryList = getTechnicalCategory(false, tenantId);
        LinkedList<String> technicalCategoryPaths = new LinkedList<>();
        getCategoryPath(technicalCategoryList, technicalCategoryPaths, categoryId);
        StringBuilder path = new StringBuilder();
        technicalCategoryPaths.forEach(e -> path.append(e).append("/"));
        // 去掉最后一个/
        return path.substring(0, path.length() - 1);
    }

    /**
     * 获取拼接的业务目录和业务对象
     *
     * @param businessCategoryList 所有的业务目录
     * @param businessInfo         业务对象
     * @return
     */
    private String getBusinessCategoryParentPath(List<BusinessCategory> businessCategoryList, BusinessInfo businessInfo) {
        LinkedList<String> businessCategoryPaths = new LinkedList<>();
        getCategoryPath(businessCategoryList, businessCategoryPaths, businessInfo.getDepartmentId());
        StringBuilder path = new StringBuilder();
        businessCategoryPaths.forEach(e -> path.append(e).append("/"));
        path.append(businessInfo.getName());
        return path.toString();
    }

    /**
     * 根据目录id在目录中递归获取父级name
     *
     * @param categoryList
     * @param categoryPaths
     * @param categoryId
     */
    private static void getCategoryPath(List<? extends BusinessCategory> categoryList, LinkedList<String> categoryPaths, String categoryId) {
        List<BusinessCategory> collect = categoryList.stream().filter(e -> categoryId.equals(e.getGuid())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            BusinessCategory businessCategory = collect.get(0);
            categoryPaths.addFirst(businessCategory.getName());
            if (!StringUtils.isEmpty(businessCategory.getParentCategoryGuid())) {
                getCategoryPath(categoryList, categoryPaths, businessCategory.getParentCategoryGuid());
            }
        }
    }

    /**
     * 获取业务目录
     *
     * @param tenantId
     * @return
     */
    public List<BusinessCategory> getBusinessCategory(String tenantId) {
        List<CategoryPrivilege> categories = businessREST.getCategories("ASC", tenantId);
        return categories.stream().map(e -> {
            BusinessCategory businessCategory = new BusinessCategory();
            BeanUtils.copyProperties(e, businessCategory);
            return businessCategory;
        }).collect(Collectors.toList());
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

        TableInfo sourceTableInfo = tableDAO.getTableInfoByTableguidAndStatus(byId.getSourceTableGuid());
        List<Column> sourceColumnInfoList = columnDAO.getColumnInfoListByTableGuid(byId.getSourceTableGuid());

        Map<String, Column> idColumnMap = sourceColumnInfoList.stream().collect(Collectors.toMap(Column::getColumnId, e -> e));
        // 查询主键id查询列
        List<SourceInfoDeriveColumnInfo> deriveColumnInfoListByTableId = sourceInfoDeriveColumnInfoService.getDeriveColumnInfoListByTableId(tableId);
        SourceInfoDeriveTableColumnVO sourceInfoDeriveTableColumnVO = new SourceInfoDeriveTableColumnVO();
        BeanUtils.copyProperties(byId, sourceInfoDeriveTableColumnVO);

        // 设置表的技术目录和业务目录
        CategoryPrivilege categoryPrivilege = categoryDAO.queryByGuidV2(sourceInfoDeriveTableColumnVO.getCategoryId(), tenantId);
        CategoryPrivilege categoryPrivilegeSource = categoryDAO.queryByGuidByDBId(sourceTableInfo.getDatabaseGuid(), tenantId);
        BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(sourceInfoDeriveTableColumnVO.getBusinessId());
        CategoryPrivilege businessHeaderInfo = categoryDAO.queryByGuidV2(businessInfo.getDepartmentId(), tenantId);

        sourceInfoDeriveTableColumnVO.setCategory(categoryPrivilege.getName());
        sourceInfoDeriveTableColumnVO.setBusiness(businessInfo.getName());
        sourceInfoDeriveTableColumnVO.setBusinessHeaderId(businessHeaderInfo.getGuid());
        sourceInfoDeriveTableColumnVO.setBusinessHeader(businessHeaderInfo.getName());
        sourceInfoDeriveTableColumnVO.setSourceTable(sourceTableInfo.getTableName());
        // 获取源数据层、库
        sourceInfoDeriveTableColumnVO.setSourceDbGuid(categoryPrivilegeSource.getGuid());
        sourceInfoDeriveTableColumnVO.setSourceDb(categoryPrivilegeSource.getName());

        // 开始设置数据库和数据源名称
        String dbId = sourceInfoDeriveTableColumnVO.getDbId();
        String sourceId = sourceInfoDeriveTableColumnVO.getSourceId();
        // 获取数据库、数据源的id.name对应
        List<Map<String, String>> maps = dbDao.queryDbNameAndSourceNameByIds(dbId, sourceId);
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
            sourceInfoDeriveColumnVO.setDataBaseName(sourceTableInfo.getDbName());
            sourceInfoDeriveColumnVO.setSourceTableGuid(sourceTableInfo.getTableGuid());
            sourceInfoDeriveColumnVO.setSourceTableNameEn(sourceTableInfo.getTableName());
            sourceInfoDeriveColumnVO.setSourceTableNameZh(sourceTableInfo.getDescription());
            sourceInfoDeriveColumnVO.setSourceColumnNameEn(column.getColumnName());
            sourceInfoDeriveColumnVO.setSourceColumnNameZh(column.getDescription());
            sourceInfoDeriveColumnVO.setSourceColumnType(column.getType());
            return sourceInfoDeriveColumnVO;
        }).collect(Collectors.toList()));
        return sourceInfoDeriveTableColumnVO;
    }

    /**
     * 删除衍生表记录
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
    private String createDDL(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto) {
        String tableNameEn = sourceInfoDeriveTableColumnDto.getTableNameEn();
        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        StringBuilder tableDDL = new StringBuilder("CREATE TABLE ").append(tableNameEn).append("(\r\n");

        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        StringBuilder columnDDL = new StringBuilder();
        StringBuilder commentDDL = new StringBuilder();
        StringBuilder primaryKeyDDLHeader = new StringBuilder("ALTER TABLE ").append(tableNameEn).append(" ADD PRIMARY KEY (");
        StringBuilder primaryKeyField = new StringBuilder();
        for (int i = 0; i < sourceInfoDeriveColumnInfos.size(); i++) {
            SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = sourceInfoDeriveColumnInfos.get(i);
            String columnNameEn = sourceInfoDeriveColumnInfo.getColumnNameEn();
            String dataType = sourceInfoDeriveColumnInfo.getDataType();
            // 对特殊的dbType进行拼接长度
            dataType = Constant.DATA_LENGTH_TYPE_MAP.get(dbType).contains(dataType) ? dataType.concat("(255)") : dataType;
            dataType = Constant.HIVE_TYPE_REPLACE.getOrDefault(dataType, dataType);
            // 最后一个字段不用拼接逗号
            columnDDL.append(columnNameEn).append(" ").append(dataType);

            // 有中文名添加注释
            String columnNameZh = sourceInfoDeriveColumnInfo.getColumnNameZh();
            if (!StringUtils.isEmpty(columnNameZh)) {
                if (Objects.equals(dbType, "ORACLE")) {
                    commentDDL.append("COMMENT ON COLUMN ").append(tableNameEn).append(".").append(columnNameEn).append(" IS '").append(columnNameZh).append("';\r\n");
                } else {
                    columnDDL.append(" COMMENT '").append(columnNameZh).append("'");
                }
            }
            if (i < sourceInfoDeriveColumnInfos.size() - 1) {
                columnDDL.append(",\r\n");
            }
            // 添加主键
            if (Objects.equals(dbType, "ORACLE") && sourceInfoDeriveColumnInfo.isPrimaryKey()) {
                primaryKeyField.append(columnNameEn).append(",");
            }
        }
        tableDDL.append(columnDDL).append(");\r\n");
        if (!StringUtils.isEmpty(primaryKeyField.toString())) {
            primaryKeyDDLHeader.append(primaryKeyField.substring(0, primaryKeyField.length() - 1)).append(");\r\n");
            tableDDL.append(primaryKeyDDLHeader);
        }
        tableDDL.append(commentDDL);
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
        StringBuilder columnBuilder = new StringBuilder("insert into ").append(tableNameEn).append("(\r\n");
        StringBuilder valueBuilder = new StringBuilder("values (\r\n");
        sourceInfoDeriveColumnInfos.forEach(e -> {

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
            columnBuilder.append(columnNameEn).append(", --");
            if (primaryKey)
                columnBuilder.append("主键;");
            if (groupField)
                columnBuilder.append("分组字段;");
            if (important)
                columnBuilder.append("重要;");
            if (removeSensitive)
                columnBuilder.append("脱敏;");
            if (permissionField)
                columnBuilder.append("权限字段;");
            if (secret)
                columnBuilder.append("保密;");
            if (!StringUtils.isEmpty(secretPeriod))
                columnBuilder.append("保密期限:").append(secretPeriod).append(";");
            if (!StringUtils.isEmpty(mappingRule))
                columnBuilder.append("映射规则:").append(mappingRule).append(";");
            if (!StringUtils.isEmpty(mappingDescribe))
                columnBuilder.append("映射说明:").append(mappingDescribe).append(";");
            if (!StringUtils.isEmpty(remark))
                columnBuilder.append("备注:").append(remark).append(";");
            columnBuilder.append("\r\n");
            valueBuilder.append(stringObjectMap.get(dataType)).append(",\r\n");
        });
        columnBuilder.append(")").append(valueBuilder).append(")");
        return columnBuilder.toString();
    }

}
