package io.zeta.metaspace.web.service.sourceinfo;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.datasource.DataSourceTypeInfo;
import io.zeta.metaspace.model.dto.sourceinfo.DeriveFileDTO;
import io.zeta.metaspace.model.dto.sourceinfo.SourceInfoDeriveTableColumnDTO;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.po.tableinfo.TableInfoDerivePO;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.CategorycateQueryResult;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.Constant;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.DeriveTableStateEnum;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.ColumnTagRelationToColumn;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveColumnInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableColumnRelation;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.*;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.rest.BusinessREST;
import io.zeta.metaspace.web.rest.TechnicalREST;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DeriveTableExportUtil;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public final int BUSINESS_CATEGORY_TYPE = 1;

    public final int TECHNIACL_CATEGORY_TYPE = 0;

    @Autowired
    private HdfsService hdfsService;

    @Autowired
    private UserDAO userDAO;

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
    private GroupDeriveTableRelationDAO relationDAO;
    private ColumnTagDAO columnTagDao;

    @Autowired
    public SourceInfoDeriveTableInfoService(SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDao, GroupDeriveTableRelationDAO relationDAO, DbDAO dbDao, BusinessDAO businessDAO,
                                            TableDAO tableDAO, ColumnDAO columnDAO, BusinessREST businessREST, TechnicalREST technicalREST,
                                            CategoryDAO categoryDAO, SourceInfoDeriveColumnInfoService sourceInfoDeriveColumnInfoService,
                                            SourceInfoDeriveTableColumnRelationService sourceInfoDeriveTableColumnRelationService,
                                            DataSourceService dataSourceService, ColumnTagDAO columnTagDao) {
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
        this.relationDAO = relationDAO;
        this.columnTagDao = columnTagDao;
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
        if (CollectionUtils.isEmpty(sourceInfoDeriveColumnInfos)) {
            sourceInfoDeriveColumnInfos = new ArrayList<>();
        }

        // 设置主键id和tableGuid
        sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());

        TableInfo tableInfo = tableDAO.selectByDbGuidAndTableName(sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getTableNameEn());
        if (tableInfo != null) {
            sourceInfoDeriveTableInfo.setTableGuid(tableInfo.getTableGuid());
        } else {
            sourceInfoDeriveTableInfo.setTableGuid(UUID.randomUUID().toString());
        }

        if(StringUtils.isBlank(sourceInfoDeriveTableInfo.getCreator())){
            sourceInfoDeriveTableInfo.setCreator(user.getUserId());
        }
        if(StringUtils.isBlank(sourceInfoDeriveTableInfo.getOperator())){
            sourceInfoDeriveTableInfo.setOperator(user.getUserId());
        }
        sourceInfoDeriveTableInfo.setCreateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setUpdater(user.getUserId());
        sourceInfoDeriveTableInfo.setUpdateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setTenantId(tenantId);
        sourceInfoDeriveTableInfo.setImportance(sourceInfoDeriveColumnInfos.stream().anyMatch(SourceInfoDeriveColumnInfo::isImportant));
        sourceInfoDeriveTableInfo.setSecurity(sourceInfoDeriveColumnInfos.stream().anyMatch(SourceInfoDeriveColumnInfo::isSecret));
        sourceInfoDeriveTableInfo.setVersion(-1);

        // 操作是保存，状态是0
        if (!sourceInfoDeriveTableColumnDto.isSubmit()) {
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.UN_COMMIT.getState());
        } else {
            // 保存并提交，设置状态是1
            // 生成DDL和DML语句
            String targetDbName = getDbNameByDbId(sourceInfoDeriveTableInfo.getDbId(), sourceInfoDeriveTableInfo.getSourceId());
            String sourceDbName = "";
            if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDto.getSourceTableGuid())) {
                sourceDbName = tableDAO.getTableInfoByTableguidAndStatus(sourceInfoDeriveTableColumnDto.getSourceTableGuid()).getDbName();
            }
            sourceInfoDeriveTableInfo.setDdl(createDDL(sourceInfoDeriveTableColumnDto, targetDbName));
            sourceInfoDeriveTableInfo.setDml(createDML(sourceInfoDeriveTableColumnDto, sourceDbName, targetDbName));
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.COMMIT.getState());
        }

        for (SourceInfoDeriveColumnInfo deriveColumnInfo : sourceInfoDeriveColumnInfos) {
            deriveColumnInfo.setSort(sourceInfoDeriveColumnInfos.indexOf(deriveColumnInfo));
            deriveColumnInfo.setId(UUID.randomUUID().toString());
            deriveColumnInfo.setColumnGuid(UUID.randomUUID().toString());
            deriveColumnInfo.setTableGuid(sourceInfoDeriveTableInfo.getTableGuid());
            deriveColumnInfo.setTenantId(tenantId);
        }

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
        if (!CollectionUtils.isEmpty(sourceInfoDeriveColumnInfos)) {
            sourceInfoDeriveColumnInfoService.saveBatch(sourceInfoDeriveColumnInfos);
            sourceInfoDeriveTableColumnRelationService.saveBatch(sourceInfoDeriveTableColumnRelationList);

            // 标签同步到column_tag_relation_to_column表
            preserveTags(sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveColumnInfos);
        }

        // 提交：新增业务对象-表关系(关联类型：0通过业务对象挂载功能挂载到该业务对象的表；1通过衍生表登记模块登记关联到该业务对象上的表)
        if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
            // 关系是否存在,不存在则插入
            boolean isRelationExist = businessDAO.isRelationExist(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId());
            if (isRelationExist) {
                // 更新关联类型为衍生表关联
                businessDAO.updateRelationType(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId(), 1);
            } else {
                businessDAO.insertDerivedTableRelation(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), 1, sourceInfoDeriveTableInfo.getSourceId());
            }

            // 更新业务对象技术信息状态
            businessDAO.updateTechnicalStatus(sourceInfoDeriveTableInfo.getBusinessId(), TechnicalStatus.ADDED.code);
        }
        return true;
    }

    /**
     * 标签同步到column_tag_relation_to_column表
     * 根据source_info_derive_table_info表的table_guid去column_info表查询，
     * 如果存在，根据列名称找到对应的id,然后把列id和标签id维护到column_tag_relation_to_column表
     *
     * @param tableGuid                   source_info_derive_table_info表的table_guid
     * @param sourceInfoDeriveColumnInfos 列集合
     */
    public void preserveTags(String tableGuid, List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        boolean isExist = columnDAO.tableColumnExist(tableGuid) > 0;
        if (isExist) {
            for (SourceInfoDeriveColumnInfo deriveColumnInfo : sourceInfoDeriveColumnInfos) {
                // 根据列名称找到对应的id
                String columnGuid = columnDAO.getColumnGuid(tableGuid, deriveColumnInfo.getSourceColumnNameEn());
                String tags = deriveColumnInfo.getTags();
                List<ColumnTagRelationToColumn> columnTagRelationToColumns = packTags(columnGuid, tags);
                if (columnTagRelationToColumns != null) {
                    sourceInfoDeriveTableInfoDao.addPreserveTags(columnTagRelationToColumns);
                }
            }
        }

    }

    /**
     * 将字符串标签转为实体类
     *
     * @param columnGuid 源表列ID
     * @param strTags    字符串数组标签
     * @return 返回当前列对应的标签列表
     */
    public List<ColumnTagRelationToColumn> packTags(String columnGuid, String strTags) {
        List<ColumnTagRelationToColumn> columnTagRelationToColumns = new ArrayList<>();
        if (StringUtils.isBlank(strTags) || StringUtils.isBlank(columnGuid)) {
            return null;
        }
        String[] tags = strTags.split(",");
        if (tags.length > 0) {
            for (int i = 0; i < tags.length; i++) {
                if (sourceInfoDeriveTableInfoDao.judgeExitTags(tags[i]) > 0) {
                    continue;
                }
                if (sourceInfoDeriveTableInfoDao.judgeExitRepetitionTags(columnGuid, tags[i]) > 0) {
                    continue;
                }
                ColumnTagRelationToColumn columnTagRelationToColumn = new ColumnTagRelationToColumn();
                columnTagRelationToColumn.setId(UUID.randomUUID().toString());
                columnTagRelationToColumn.setColumnId(columnGuid);
                columnTagRelationToColumn.setTagId(tags[i]);
                columnTagRelationToColumns.add(columnTagRelationToColumn);
            }
        }
        return columnTagRelationToColumns;
    }

    /**
     * 根据字符串标签ID获取对应的名称
     *
     * @param strTags 字符串数组标签
     * @return 返回当前列对应的标签列表
     */
    public List<ColumnTag> getTags(String strTags, String sourceColumnId, String tenantId) {
        List<ColumnTag> columnTags = new ArrayList<>();
        //1、查询源目标列关联的标签
        if (StringUtils.isNotBlank(sourceColumnId)) {
            columnTags = columnTagDao.getTagListByColumnId(tenantId, sourceColumnId);
        }
        //2、查询衍生表列关联的标签
        if (StringUtils.isNotBlank(strTags)) {
            String[] tags = strTags.split(",");
            List<String> listTags = Arrays.asList(tags);
            List<ColumnTag> tagListById = sourceInfoDeriveTableInfoDao.getTagListById(listTags);
            if (tagListById != null && tagListById.size() > 0) {
                columnTags.addAll(tagListById);
                List<ColumnTag> collect = columnTags.stream().filter(distinctByKey(ColumnTag::getId)).collect(Collectors.toList());
                columnTags.clear();
                columnTags.addAll(collect);
            }
        }
        return columnTags;
    }

    /**
     * 自定义条件过滤器
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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

        // 查询旧的关联的源表id
        SourceInfoDeriveTableInfo oldSourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDao.getByIdAndTenantId(tableId, tenantId);

        // 表
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = new SourceInfoDeriveTableInfo();
        BeanUtils.copyProperties(sourceInfoDeriveTableColumnDto, sourceInfoDeriveTableInfo);
        // 列
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();

        // 设置字段顺序
        for (SourceInfoDeriveColumnInfo deriveColumnInfo : sourceInfoDeriveColumnInfos) {
            deriveColumnInfo.setSort(sourceInfoDeriveColumnInfos.indexOf(deriveColumnInfo));
        }

        TableInfo tableInfo = tableDAO.selectByDbGuidAndTableName(sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableInfo.getTableNameEn());
        if (tableInfo != null) {
            sourceInfoDeriveTableInfo.setTableGuid(tableInfo.getTableGuid());
        }
        sourceInfoDeriveTableInfo.setCreateTime(LocalDateTime.parse(sourceInfoDeriveTableColumnDto.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        sourceInfoDeriveTableInfo.setUpdater(user.getUserId());
        sourceInfoDeriveTableInfo.setUpdateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setOperator(user.getUserId());
        sourceInfoDeriveTableInfo.setTenantId(tenantId);
        boolean important = sourceInfoDeriveColumnInfos.stream().anyMatch(SourceInfoDeriveColumnInfo::isImportant);
        boolean security = sourceInfoDeriveColumnInfos.stream().anyMatch(SourceInfoDeriveColumnInfo::isSecret);
        if (!important) {
            relationDAO.deleteRelationImportantPrivilegeByTableId(tenantId, oldSourceInfoDeriveTableInfo.getTableGuid());
        }
        if (!security) {
            relationDAO.deleteRelationSecurityPrivilegeByTableId(tenantId, oldSourceInfoDeriveTableInfo.getTableGuid());
        }
        sourceInfoDeriveTableInfo.setImportance(important);
        sourceInfoDeriveTableInfo.setSecurity(security);
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
            if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDto.getSourceTableGuid())) {
                String targetDbName = getDbNameByDbId(sourceInfoDeriveTableInfo.getDbId(), sourceInfoDeriveTableInfo.getSourceId());
                String sourceDbName = tableDAO.getTableInfoByTableguidAndStatus(sourceInfoDeriveTableColumnDto.getSourceTableGuid()).getDbName();
                sourceInfoDeriveTableInfo.setDdl(createDDL(sourceInfoDeriveTableColumnDto, targetDbName));
                sourceInfoDeriveTableInfo.setDml(createDML(sourceInfoDeriveTableColumnDto, sourceDbName, targetDbName));
            }

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
        // 标签同步到column_tag_relation_to_column表
        preserveTags(sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveColumnInfos);
        sourceInfoDeriveTableColumnRelationService.saveOrUpdateBatch(sourceInfoDeriveTableColumnRelationList);


        if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
            // 删除旧的业务对象-表关联关系
            businessDAO.deleteRelationByBusinessIdAndTableId(oldSourceInfoDeriveTableInfo.getBusinessId(), oldSourceInfoDeriveTableInfo.getTableGuid(), oldSourceInfoDeriveTableInfo.getSourceId(), 1);
            // 新增业务对象-表关系(关联类型：0通过业务对象挂载功能挂载到该业务对象的表；1通过衍生表登记模块登记关联到该业务对象上的表)

            // 关系是否存在,不存在则插入
            boolean isRelationExist = businessDAO.isRelationExist(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId());
            if (isRelationExist) {
                // 更新关联类型为衍生表关联
                businessDAO.updateRelationType(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId(), 1);
            } else {
                businessDAO.insertDerivedTableRelation(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), 1, sourceInfoDeriveTableInfo.getSourceId());
            }
        }
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
            sourceColumnEntity.setSourceColumnType(e.getType() != null ? e.getType().toLowerCase() : "");
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
        List<CategorycateQueryResult> categories = businessREST.getCategories("ASC", 1, tenantId);
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
        if (table == null) {
            LOG.info("该表 {} 不存在", tableGuid);
            return null;
        }
        List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId, sourceId, schemaId, table.getTableName());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(deriveTableInfoList)) {
            return null;
        }
        Optional<SourceInfoDeriveTableInfo> deriveTableInfoOpt = deriveTableInfoList.stream().sorted(Comparator.comparing(SourceInfoDeriveTableInfo::getVersion).reversed()).findFirst();
        if (deriveTableInfoOpt.isPresent()) {
            SourceInfoDeriveTableInfo tableInfo = deriveTableInfoOpt.get();
            SourceInfoDeriveTableColumnVO info = new SourceInfoDeriveTableColumnVO();
            BeanUtils.copyProperties(tableInfo, info);
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

        BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(sourceInfoDeriveTableColumnVO.getBusinessId());
        if (null != businessInfo) {
            // 获取该租户下所有的业务目录guid - path
            Map<String, String> businessCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, BUSINESS_CATEGORY_TYPE, businessInfo.getDepartmentId());
            sourceInfoDeriveTableColumnVO.setBusiness(businessInfo.getName());
            sourceInfoDeriveTableColumnVO.setBusinessHeaderId(businessInfo.getDepartmentId());
            sourceInfoDeriveTableColumnVO.setBusinessHeader(businessCategoryGuidPathMap.getOrDefault(businessInfo.getDepartmentId(), ""));
        }

        // 设置表的技术目录和业务目录
        // 获取租户下所有的技术目录guid - path
        Map<String, String> technicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, TECHNIACL_CATEGORY_TYPE, sourceInfoDeriveTableColumnVO.getCategoryId());

        sourceInfoDeriveTableColumnVO.setCategory(technicalCategoryGuidPathMap.getOrDefault(sourceInfoDeriveTableColumnVO.getCategoryId(), ""));

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

        if (StringUtils.isNotBlank(byId.getSourceTableGuid())) {
            TableInfo sourceTableInfo = tableDAO.getTableInfoByTableguidAndStatus(byId.getSourceTableGuid());
            if (null == sourceTableInfo) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "原表不存在");
            }

            String sourceCategoryId = categoryDAO.queryCategoryIdByGuidByDBId(sourceTableInfo.getDatabaseGuid(), tenantId);
            Map<String, String> sourceTechnicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, TECHNIACL_CATEGORY_TYPE, sourceCategoryId);
            sourceInfoDeriveTableColumnVO.setSourceTable(sourceTableInfo.getTableName());
            // 获取源数据层、库
            sourceInfoDeriveTableColumnVO.setSourceDbGuid(sourceCategoryId);
            sourceInfoDeriveTableColumnVO.setSourceDb(sourceTechnicalCategoryGuidPathMap.getOrDefault(sourceCategoryId, ""));
        }

        sourceInfoDeriveTableColumnVO.setSourceInfoDeriveColumnVOS(deriveColumnInfoListByTableId.stream().map(e -> {
            SourceInfoDeriveColumnVO sourceInfoDeriveColumnVO = new SourceInfoDeriveColumnVO();
            BeanUtils.copyProperties(e, sourceInfoDeriveColumnVO);
            if (StringUtils.isNotBlank(e.getSourceColumnGuid())) {
                TableInfo tableInfo = tableDAO.getTableInfoByTableguidAndStatus(e.getSourceTableGuid());
                if (null == tableInfo) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "原表不存在");
                }
                Column column = columnDAO.getColumnInfoByColumnGuid(e.getSourceColumnGuid());

                String columnSourceCategoryId = categoryDAO.queryCategoryIdByGuidByDBId(tableInfo.getDatabaseGuid(), tenantId);
                // 列获取源数据层、库
                sourceInfoDeriveColumnVO.setSourceDbGuid(columnSourceCategoryId);

                sourceInfoDeriveColumnVO.setDataBaseName(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : tableInfo.getDbName());
                sourceInfoDeriveColumnVO.setSourceTableGuid(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : tableInfo.getTableGuid());
                sourceInfoDeriveColumnVO.setSourceTableNameEn(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : tableInfo.getTableName());
                sourceInfoDeriveColumnVO.setSourceTableNameZh(StringUtils.isBlank(sourceInfoDeriveColumnVO.getSourceColumnGuid()) ? null : tableInfo.getDescription());
                sourceInfoDeriveColumnVO.setSourceColumnNameEn(null == column ? null : column.getColumnName());
                sourceInfoDeriveColumnVO.setSourceColumnNameZh(null == column ? null : column.getDescription());
                sourceInfoDeriveColumnVO.setSourceColumnType(null == column ? null : column.getType());
            }
            sourceInfoDeriveColumnVO.setTags(getTags(e.getTags(), sourceInfoDeriveColumnVO.getSourceColumnGuid(), tenantId));
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
        // 查询衍生表信息
        List<SourceInfoDeriveTableInfo> sourceInfoDeriveTableInfos = sourceInfoDeriveTableInfoDao.getDeriveTableInfoByGuids(tableGuids);
        if (!CollectionUtils.isEmpty(sourceInfoDeriveTableInfos)) {
            // 删除旧的业务对象-表关联关系
            businessDAO.batchDeleteRelationByBusinessIdsAndTableIds(sourceInfoDeriveTableInfos);
        }

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
        if ("hive".equalsIgnoreCase(sourceInfoDeriveTableColumnDto.getDbType())) {
            tableDDL.append("\r\nrow format delimited fields terminated by '\\001' stored as orc");
        }
        tableDDL.append(";\r\n");
        if (StringUtils.isNotBlank(primaryKeyField.toString())) {
            primaryKeyDDLHeader.append(primaryKeyField.substring(0, primaryKeyField.length() - 1)).append(");\r\n");
            tableDDL.append(primaryKeyDDLHeader);
        }
        tableDDL.append(commentDDL);
        removeTimeField(sourceInfoDeriveColumnInfos);
        return tableDDL.toString().toUpperCase();
    }


    /**
     * 构建DML语句
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    private String createDML(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String sourceDbName, String targetDbName) {
        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        // 数据库类型获取数据类型-替换值
        Map<String, Object> stringObjectMap = Constant.REPLACE_DATE_MAP.get(dbType);
        String tableNameEn = sourceInfoDeriveTableColumnDto.getTableNameEn();
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        addTimeField(sourceInfoDeriveColumnInfos);
        StringBuilder columnBuilder = new StringBuilder("insert into ").append(targetDbName).append(".").append(tableNameEn).append("\r\n");
        removeTimeField(sourceInfoDeriveColumnInfos);

        columnBuilder.append(this.getMapping(sourceInfoDeriveTableColumnDto, sourceDbName));
        return columnBuilder.toString();
    }

    /**
     * 获取select映射
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    private String getMapping(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String dbName) {
        StringBuilder str = new StringBuilder();
        StringBuilder strColumn = new StringBuilder();
        StringBuilder strSelect = new StringBuilder();
        strColumn.append("(");
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos()) {
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSourceColumnNameEn())) {
                continue;
            }
            strColumn.append(sourceInfoDeriveColumnInfo.getColumnNameEn()).append(", ");
            strSelect.append(sourceInfoDeriveColumnInfo.getSourceColumnNameEn()).append(" as ").append(sourceInfoDeriveColumnInfo.getColumnNameEn()).append(",");
        }
        if ("(".equals(strColumn.toString())) {
            return "";
        }
        strColumn = new StringBuilder(strColumn.substring(0, strColumn.length() - 2));
        strColumn.append(")\r\n");
        str.append(strColumn).append("select \r\n");
        strSelect = new StringBuilder(strSelect.substring(0, strSelect.length() - 1));
        str.append(strSelect).append("\r\n from " + dbName + "." + sourceInfoDeriveTableColumnDto.getSourceTableNameEn()).append(";");
        return str.toString();
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
        if (!checkTableNameDump(sourceInfoDeriveTableColumnDto.getTableNameEn(), sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getId())) {
            return ReturnUtil.error("400", "目标库下表英文名已存在");
        }

        if (!sourceInfoDeriveTableColumnDto.isSubmit()) {
            return ReturnUtil.success();
        }

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

        // 检验表英文名
        if (!checkTableOrColumnNameEnPattern(sourceInfoDeriveTableColumnDto.getTableNameEn())) {
            return ReturnUtil.error("400", "衍生表英文名不符合规范");
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
//        if (!checkSourceTableByGuid(sourceInfoDeriveTableColumnDto.getSourceTableGuid())) {
//            return ReturnUtil.error("400", "源表不存在");
//        }
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
    public SourceInfoDeriveTableInfo getTableByIdAndGuid(String id, String tenantId) {
        return sourceInfoDeriveTableInfoDao.getByIdAndGuidAndTenantId(id, tenantId);
    }

    /**
     * 校验categoryid
     *
     * @param guid
     * @param tenantId
     * @return
     * @throws SQLException
     */
    boolean checkCategoryByGuid(String guid, String tenantId) {
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

    public List<DeriveFileDTO> fileUploadDeriveBatch(List<DeriveFileDTO> deriveFileDTOList, String tenantId) {
        for (DeriveFileDTO fileDTO : deriveFileDTOList) {
            Result result = this.fileUploadDerive(fileDTO.getFileName(), fileDTO.getPath(), tenantId);
            fileDTO.setDescription(result.getMessage());
        }
        Iterator<DeriveFileDTO> it = deriveFileDTOList.iterator();
        while (it.hasNext()) {
            DeriveFileDTO item = it.next();
            if ("success".equals(item.getDescription())) {
                it.remove();
            }
        }
        return deriveFileDTOList;
    }

    public Result fileUploadSubmitBatch(List<DeriveFileDTO> deriveFileDTOList, String tenantId) {
        String error = "";
        for (DeriveFileDTO deriveFileDTO : deriveFileDTOList) {
            try {
                this.fileUploadSubmit(deriveFileDTO.getFileName(), deriveFileDTO.getPath(), tenantId);
            } catch (Exception e) {
                error = e.getMessage();
            }
        }
        if (StringUtils.isNotBlank(error)) {
            return ReturnUtil.error("400", error);
        }
        return ReturnUtil.success(true);
    }

    public Boolean fileUploadSubmit(String fileName, String filePath, String tenantId) {
        try {
            SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO = this.getDeriveDataFile(fileName, filePath);
            this.fileCheckBlank(sourceInfoDeriveTableColumnDTO);
            this.fileCheckLength(sourceInfoDeriveTableColumnDTO);
            this.fileCheckDb(sourceInfoDeriveTableColumnDTO, tenantId);
            this.checkDeriveTableEntityFile(sourceInfoDeriveTableColumnDTO, tenantId);
            sourceInfoDeriveTableColumnDTO.setSubmit(true);
            sourceInfoDeriveTableColumnDTO.setFileName(fileName);
            sourceInfoDeriveTableColumnDTO.setFilePath(filePath);
            return this.createSaveAndSubmitDeriveTableInfo(sourceInfoDeriveTableColumnDTO, tenantId);
        } catch (Exception e) {
            LOG.error("fileUploadSubmit exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 衍生表文件上传
     *
     * @param fileName
     * @param filePath
     */
    public Result fileUploadDerive(String fileName, String filePath, String tenantId) {
        try {
            if (!(fileName.endsWith(CommonConstant.EXCEL_FORMAT_XLSX) || fileName.endsWith(CommonConstant.EXCEL_FORMAT_XLS))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式不正确");
            }
            SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO = this.getDeriveDataFile(fileName, filePath);
            this.fileCheckBlank(sourceInfoDeriveTableColumnDTO);
            this.fileCheckLength(sourceInfoDeriveTableColumnDTO);
            this.fileCheckDb(sourceInfoDeriveTableColumnDTO, tenantId);
            return this.checkDeriveTableEntityFile(sourceInfoDeriveTableColumnDTO, tenantId);
        } catch (AtlasBaseException e) {
            return ReturnUtil.error("400", e.getMessage());
        }
    }


    private Result checkDeriveTableEntityFile(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId) {
        if (!checkTableNameDump(sourceInfoDeriveTableColumnDto.getTableNameEn(), sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getId())) {
            return ReturnUtil.error("400", "目标库下表英文名已存在");
        }

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

        // 检验表英文名
        if (!checkTableOrColumnNameEnPattern(sourceInfoDeriveTableColumnDto.getTableNameEn())) {
            return ReturnUtil.error("400", "目标表英文名不符合规范");
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
        // 校验源字段
        List<String> sourceColumnIds = sourceInfoDeriveColumnInfos.stream().map(SourceInfoDeriveColumnInfo::getSourceColumnGuid).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(sourceColumnIds) && !checkSourceColumnsByGuid(sourceColumnIds)) {
            return ReturnUtil.error("400", "部分源字段不存在");
        }
        return ReturnUtil.success();
    }

    private void fileCheckDb(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        this.getTechnicalCategoryId(sourceInfoDeriveTableColumnDTO, tenantId);

        String userId = userDAO.selectByTenantIdAndName(tenantId, sourceInfoDeriveTableColumnDTO.getCreatorName());
        if (StringUtils.isBlank(userId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中设计人所填写的内容在系统中不存在");
        }
        sourceInfoDeriveTableColumnDTO.setCreator(userId);

        this.getBusinessCategoryId(sourceInfoDeriveTableColumnDTO, tenantId);

        this.getBusinessId(sourceInfoDeriveTableColumnDTO, tenantId);

        this.checkColumn(sourceInfoDeriveTableColumnDTO, tenantId);
    }

    /**
     * 校验字段信息
     *
     * @param sourceInfoDeriveTableColumnDTO
     * @param tenantId
     */
    private void checkColumn(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        List<String> dataTypeList = (List<String>) this.getDataTypeByDbType(sourceInfoDeriveTableColumnDTO.getDbType()).getData();
        if (CollectionUtils.isEmpty(dataTypeList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源类型不符合规范");
        }
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos();

        List<TechnicalCategory> technicalCategoryList = this.getTechnicalCategory(false, tenantId);
        List<String> dbIdList = new ArrayList<>();
        technicalCategoryList.stream().forEach(p -> dbIdList.add(p.getDbId()));
        List<String> dbNameList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (!dataTypeList.contains(sourceInfoDeriveColumnInfo.getDataType())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中目标字段类型所填写的内容在系统中不存在:" + sourceInfoDeriveColumnInfo.getDataType());
            }
            dbNameList.add(sourceInfoDeriveColumnInfo.getSourceDbName());
            tableNameList.add(sourceInfoDeriveColumnInfo.getSourceTableNameEn());
        }
        if (CollectionUtils.isEmpty(dbNameList) && CollectionUtils.isEmpty(tableNameList)) {
            return;
        }
        List<TableInfoDerivePO> tableInfoDerivePOList = tableDAO.selectByNameAndDbGuid(dbNameList, tableNameList, dbIdList);
        List<ColumnTag> columnTags = columnTagDao.selectListByTenantId(tenantId);
        Map<String, String> mapColumn = new HashMap<>();
        if (!CollectionUtils.isEmpty(columnTags)) {
            mapColumn = columnTags.stream().collect(Collectors.toMap(ColumnTag::getName, ColumnTag::getId));
        }
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSourceDbName()) && StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSourceTableNameEn())) {
                continue;
            }
            List<TableInfoDerivePO> collect = tableInfoDerivePOList.stream().filter(p -> p.getDatabaseName().equals(sourceInfoDeriveColumnInfo.getSourceDbName()) &&
                    p.getTableName().equals(sourceInfoDeriveColumnInfo.getSourceTableNameEn()) &&
                    p.getColumnName().equals(sourceInfoDeriveColumnInfo.getSourceColumnNameEn())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中源库英文名、源表英文名或者源字段英文名所填写的内容在系统中不存在:" + sourceInfoDeriveColumnInfo.getSourceDbName());
            }
            if (collect.size() > 1) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中源库英文名、源表英文名或者源字段英文名所填写的内容在系统中存在多个:" + sourceInfoDeriveColumnInfo.getSourceDbName());
            }
            sourceInfoDeriveColumnInfo.setSourceTableGuid(collect.get(0).getTableGuid());

            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getTagsName())) {
                List<String> tagIdList = new ArrayList<>();
                for (String value : sourceInfoDeriveColumnInfo.getTagsName().split(",")) {
                    if (StringUtils.isNotBlank(value)) {
                        String id = mapColumn.get(value);
                        if (StringUtils.isBlank(id)) {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中目标字段标签所填写的内容在系统中不存在:" + value);
                        }
                        tagIdList.add(id);
                    }
                }
                sourceInfoDeriveColumnInfo.setTags(StringUtils.join(tagIdList, ","));
            }
        }
    }

    private void getBusinessId(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        List<SourceBusinessInfo> businessByCategoryId = this.getBusinessByCategoryId(sourceInfoDeriveTableColumnDTO.getBusinessCategoryId(), tenantId);
        List<SourceBusinessInfo> list = businessByCategoryId.stream().filter(p -> p.getName().equals(sourceInfoDeriveTableColumnDTO.getBusinessName())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(list)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中业务对象所填写的内容在系统中不存在:" + sourceInfoDeriveTableColumnDTO.getBusinessName());
        }
        sourceInfoDeriveTableColumnDTO.setBusinessId(list.get(0).getBusinessId());
    }

    /**
     * 判断业务目录是否存在
     *
     * @param sourceInfoDeriveTableColumnDTO
     * @param tenantId
     */
    private void getBusinessCategoryId(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        List<BusinessCategory> businessCategoryList = this.getBusinessCategory(tenantId);
        String[] split = sourceInfoDeriveTableColumnDTO.getBusinessCategoryName().split("/");
        String parentGuid = "";
        for (int i = 0; i < split.length; i++) {
            int index = i;
            String parentGuidLambda = parentGuid;
            List<BusinessCategory> list;
            if (i == 0) {
                list = businessCategoryList.stream().filter(p -> p.getLevel() == index + 1 && p.getName().equals(split[index])).collect(Collectors.toList());
            } else {
                list = businessCategoryList.stream().filter(p -> p.getLevel() == index + 1 && p.getParentCategoryGuid().equals(parentGuidLambda) && p.getName().equals(split[index])).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(list)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中业务目录所填写的内容在系统中不存在");
            }
            if (index == split.length - 1) {
                sourceInfoDeriveTableColumnDTO.setBusinessCategoryId(list.get(0).getGuid());
                break;
            }
            parentGuid = list.get(0).getGuid();
        }
    }

    /**
     * 获取技术目录id
     *
     * @param sourceInfoDeriveTableColumnDTO
     * @param tenantId
     */
    private void getTechnicalCategoryId(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        List<TechnicalCategory> technicalCategoryList = this.getTechnicalCategory(true, tenantId);
        String[] split = sourceInfoDeriveTableColumnDTO.getCategoryName().split("/");
        String parentGuid = "";
        for (int i = 0; i < split.length; i++) {
            int index = i;
            String parentGuidLambda = parentGuid;
            List<TechnicalCategory> list;
            if (i == 0) {
                list = technicalCategoryList.stream().filter(p -> p.getLevel() == index + 1 && p.getName().equals(split[index])).collect(Collectors.toList());
            } else {
                list = technicalCategoryList.stream().filter(p -> p.getLevel() == index + 1 && p.getParentCategoryGuid().equals(parentGuidLambda) && p.getName().equals(split[index])).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(list)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中目标表所属层级/库所填写的内容在系统中不存在");
            }
            if (index == split.length - 1) {
                if (StringUtils.isBlank(list.get(0).getDbType())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中目标表所属层级/库所填写的内容还没有登记数据库");
                }
                sourceInfoDeriveTableColumnDTO.setCategoryId(list.get(0).getGuid());
                sourceInfoDeriveTableColumnDTO.setDbType(list.get(0).getDbType());
                sourceInfoDeriveTableColumnDTO.setDbId(list.get(0).getDbId());
                sourceInfoDeriveTableColumnDTO.setSourceId(list.get(0).getSourceId());
                return;
            }
            parentGuid = list.get(0).getGuid();
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中目标表所属层级/库所填写的内容在系统中不存在");
    }

    /**
     * 必填校验
     *
     * @param sourceInfoDeriveTableColumnDTO
     */
    private void fileCheckBlank(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO) {
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getTableNameEn())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标表英文名");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getTableNameZh())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标表中文名");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getCategoryName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标表所属层级/库");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getCreatorName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：设计人");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getBusinessCategoryName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：业务目录");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getBusinessName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：业务对象");
        }
        if (CollectionUtils.isEmpty(sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填信息：字段映射");
        }
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos();
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getColumnNameEn())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标字段英文名");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getColumnNameZh())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标字段中文名");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getDataType())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标字段类型");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSecretName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标字段是否保密");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getImportantName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "缺少必填字段：目标字段是否重要");
            }
        }
    }

    /**
     * 校验字段长度
     *
     * @param sourceInfoDeriveTableColumnDTO
     */
    private void fileCheckLength(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO) {
        if (sourceInfoDeriveTableColumnDTO.getTableNameEn().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目标表英文名不能超过128位");
        }
        if (sourceInfoDeriveTableColumnDTO.getTableNameZh().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目标表中文名不能超过128位");
        }
        if (sourceInfoDeriveTableColumnDTO.getCategoryName().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目标表所属层级/库不能超过128位");
        }
        if (sourceInfoDeriveTableColumnDTO.getCreatorName().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "设计人不能超过128位");
        }
        if (sourceInfoDeriveTableColumnDTO.getBusinessCategoryName().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务目录不能超过128位");
        }
        if (sourceInfoDeriveTableColumnDTO.getBusinessName().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "业务对象不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getUpdateFrequency()) && sourceInfoDeriveTableColumnDTO.getUpdateFrequency().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新频率不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getEtlPolicy()) && sourceInfoDeriveTableColumnDTO.getEtlPolicy().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ETL策略不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getIncreStandard()) && sourceInfoDeriveTableColumnDTO.getIncreStandard().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "增量抽取标准不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getIncrementalField()) && sourceInfoDeriveTableColumnDTO.getIncrementalField().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "增量字段不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getCleanRule()) && sourceInfoDeriveTableColumnDTO.getCleanRule().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "清洗规则不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getFilter()) && sourceInfoDeriveTableColumnDTO.getFilter().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "过滤条件不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getProcedure()) && sourceInfoDeriveTableColumnDTO.getProcedure().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存储过程名/脚本名不能超过128位");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getRemark()) && sourceInfoDeriveTableColumnDTO.getRemark().length() > 512) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "备注不能超过512位");
        }
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos();
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (sourceInfoDeriveColumnInfo.getColumnNameEn().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目标字段英文名不能超过128位");
            }
            if (sourceInfoDeriveColumnInfo.getColumnNameZh().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目标字段中文名不能超过128位");
            }
            if (sourceInfoDeriveColumnInfo.getDataType().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目标字段类型不能超过128位");
            }

            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSourceDbName()) && sourceInfoDeriveColumnInfo.getSourceDbName().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "源库/系统英文名不能超过128位");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSourceTableNameEn()) && sourceInfoDeriveColumnInfo.getSourceTableNameEn().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "源表英文名不能超过128位");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSourceColumnNameEn()) && sourceInfoDeriveColumnInfo.getSourceColumnNameEn().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "源字段英文名不能超过128位");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getMappingRule()) && sourceInfoDeriveColumnInfo.getMappingRule().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "映射规则不能超过128位");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getMappingDescribe()) && sourceInfoDeriveColumnInfo.getMappingDescribe().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "映射说明不能超过128位");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSecretPeriod()) && sourceInfoDeriveColumnInfo.getSecretPeriod().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "保密期限不能超过128位");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getDesensitizationRules()) && sourceInfoDeriveColumnInfo.getDesensitizationRules().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目标字段脱敏规则不能超过128位");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getRemark()) && sourceInfoDeriveColumnInfo.getRemark().length() > 512) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "备注不能超过512位");
            }
        }
    }

    public SourceInfoDeriveTableColumnDTO getDeriveDataFile(String fileName, String filePath) {
        SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto = new SourceInfoDeriveTableColumnDTO();
        try {
            InputStream input = hdfsService.getFileInputStream(filePath);
            List<String[]> list = PoiExcelUtils.readExcelFile(input, fileName, 0, 20);
            sourceInfoDeriveTableColumnDto.setTableNameEn(list.get(1)[1]);
            sourceInfoDeriveTableColumnDto.setTableNameZh(list.get(1)[3]);
            sourceInfoDeriveTableColumnDto.setCategoryName(list.get(1)[5]);
            sourceInfoDeriveTableColumnDto.setCreatorName(list.get(1)[7]);
            sourceInfoDeriveTableColumnDto.setUpdateFrequency(list.get(2)[1]);
            sourceInfoDeriveTableColumnDto.setEtlPolicy(list.get(2)[3]);
            sourceInfoDeriveTableColumnDto.setIncreStandard(list.get(2)[5]);
            sourceInfoDeriveTableColumnDto.setIncrementalField(list.get(2)[7]);
            sourceInfoDeriveTableColumnDto.setCleanRule(list.get(3)[1]);
            sourceInfoDeriveTableColumnDto.setFilter(list.get(3)[3]);
            sourceInfoDeriveTableColumnDto.setProcedure(list.get(3)[5]);
            sourceInfoDeriveTableColumnDto.setRemark(list.get(4)[1]);
            sourceInfoDeriveTableColumnDto.setBusinessCategoryName(list.get(6)[1]);
            sourceInfoDeriveTableColumnDto.setBusinessName(list.get(6)[3]);
            List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfoList = new ArrayList<>();
            for (int i = 9; i < list.size(); i++) {
                SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = new SourceInfoDeriveColumnInfo();
                sourceInfoDeriveColumnInfo.setColumnNameEn(list.get(i)[0]);
                sourceInfoDeriveColumnInfo.setColumnNameZh(list.get(i)[1]);
                sourceInfoDeriveColumnInfo.setDataType(list.get(i)[2]);
                sourceInfoDeriveColumnInfo.setSourceDbName(list.get(i)[3]);
                sourceInfoDeriveColumnInfo.setSourceTableNameEn(list.get(i)[4]);
                sourceInfoDeriveColumnInfo.setSourceTableNameZh(list.get(i)[5]);
                sourceInfoDeriveColumnInfo.setSourceColumnNameEn(list.get(i)[6]);
                sourceInfoDeriveColumnInfo.setPrimaryKeyName(list.get(i)[10]);
                if ("是".equals(list.get(i)[10])) {
                    sourceInfoDeriveColumnInfo.setPrimaryKey(true);
                } else {
                    sourceInfoDeriveColumnInfo.setPrimaryKey(false);
                }
                sourceInfoDeriveColumnInfo.setMappingRule(list.get(i)[11]);
                sourceInfoDeriveColumnInfo.setMappingDescribe(list.get(i)[12]);
                sourceInfoDeriveColumnInfo.setSecretName(list.get(i)[13]);
                if ("是".equals(list.get(i)[13])) {
                    sourceInfoDeriveColumnInfo.setSecret(true);
                } else {
                    sourceInfoDeriveColumnInfo.setSecret(false);
                }
                sourceInfoDeriveColumnInfo.setSecretPeriod(list.get(i)[14]);
                sourceInfoDeriveColumnInfo.setImportantName(list.get(i)[15]);
                if ("是".equals(list.get(i)[15])) {
                    sourceInfoDeriveColumnInfo.setImportant(true);
                } else {
                    sourceInfoDeriveColumnInfo.setImportant(false);
                }
                sourceInfoDeriveColumnInfo.setDesensitizationRules(list.get(i)[16]);
                sourceInfoDeriveColumnInfo.setTagsName(list.get(i)[17]);
                if ("是".equals(list.get(i)[18])) {
                    sourceInfoDeriveColumnInfo.setPermissionField(true);
                } else {
                    sourceInfoDeriveColumnInfo.setPermissionField(false);
                }
                sourceInfoDeriveColumnInfo.setRemark(list.get(i)[19]);
                sourceInfoDeriveColumnInfoList.add(sourceInfoDeriveColumnInfo);
            }
            sourceInfoDeriveTableColumnDto.setSourceInfoDeriveColumnInfos(sourceInfoDeriveColumnInfoList);
        } catch (Exception e) {
            LOG.error("getDeriveDataFile exception is {}", e);
        }
        return sourceInfoDeriveTableColumnDto;
    }

    public void exportById(HttpServletResponse response, String tenantId, String tableId) {
        SourceInfoDeriveTableColumnVO deriveTableColumnDetail = null;
        try {
            deriveTableColumnDetail = getDeriveTableColumnDetail(tenantId, tableId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (deriveTableColumnDetail == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该衍生表不存在");
        }
        List<SourceInfoDeriveColumnDTO> list = DeriveTableExportUtil.getPojo(deriveTableColumnDetail.getSourceInfoDeriveColumnVOS());
        String templateName = DeriveTableExportUtil.deriveTableTemplate();
        String exportTableName = DeriveTableExportUtil.deriveTableExcelPathName(deriveTableColumnDetail.getTableNameZh());

        ExcelWriter excelWriter = EasyExcel.write(exportTableName).withTemplate(templateName).build();
        WriteSheet writeSheet = EasyExcel.writerSheet().build();
        excelWriter.fill(deriveTableColumnDetail, writeSheet);
        excelWriter.fill(list, writeSheet);
        excelWriter.finish();
        String fileName = null;
        try {
            fileName = java.net.URLEncoder.encode(DeriveTableExportUtil.
                    deriveTableExcelName(deriveTableColumnDetail.getTableNameZh()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File file = DeriveTableExportUtil.deriveTableExport(exportTableName);
        try {
            InputStream inputStream = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(inputStream);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导出失败");
        } finally {
            if (file.exists() && !file.delete()) {
                LOG.error("衍生表导出文" + exportTableName + "未实时删除");
            }
        }
    }

    public void downloadTemplate(HttpServletResponse response) {
        File file = DeriveTableExportUtil.deriveTableImportTemplate();
        try {
            InputStream input = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(input);
            String fileName = DeriveTableExportUtil.getDeriveImportTemplate();
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            throw new AtlasBaseException("导出失败");
        }
    }
}
