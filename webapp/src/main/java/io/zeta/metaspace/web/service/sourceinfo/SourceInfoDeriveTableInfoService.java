package io.zeta.metaspace.web.service.sourceinfo;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.datasource.DataSourceTypeInfo;
import io.zeta.metaspace.model.dto.sourceinfo.DeriveFileDTO;
import io.zeta.metaspace.model.dto.sourceinfo.SourceInfoDeriveTableColumnDTO;
import io.zeta.metaspace.model.enums.FileInfoPath;
import io.zeta.metaspace.model.enums.TableColumnContrast;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableEntity;
import io.zeta.metaspace.model.po.sourceinfo.TableDataSourceRelationPO;
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
import io.zeta.metaspace.web.dao.sourceinfo.SourceInfoDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.rest.BusinessREST;
import io.zeta.metaspace.web.rest.TechnicalREST;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.service.fileinfo.FileInfoService;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * ?????????????????? ???????????????
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Service
public class SourceInfoDeriveTableInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(SourceInfoDeriveTableInfoService.class);
    private static final int LOCK_TIME_OUT_TIME = AtlasConfiguration.LOCK_TIME_OUT_TIME.getInt();

    @Autowired
    private HdfsService hdfsService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SourceInfoDAO sourceInfoDAO;

    @Autowired
    private ZkLockUtils zkLockUtils;

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
    private FileInfoService fileInfoService;
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

    public SourceInfoDeriveTableInfoService() {

    }

    /**
     * ??????-????????????????????????
     *
     * @param sourceInfoDeriveTableColumnDto
     * @param tenantId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createSaveAndSubmitDeriveTableInfo(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId) {
        User user = AdminUtils.getUserData();
        // ???
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = new SourceInfoDeriveTableInfo();
        BeanUtils.copyProperties(sourceInfoDeriveTableColumnDto, sourceInfoDeriveTableInfo);
        // ??????
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        if (CollectionUtils.isEmpty(sourceInfoDeriveColumnInfos)) {
            sourceInfoDeriveColumnInfos = new ArrayList<>();
        }

        // ????????????id???tableGuid
        sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());

        TableInfo tableInfo = tableDAO.selectByDbGuidAndTableName(sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getTableNameEn());
        if (tableInfo != null) {
            sourceInfoDeriveTableInfo.setTableGuid(tableInfo.getTableGuid());
        } else {
            sourceInfoDeriveTableInfo.setTableGuid(UUID.randomUUID().toString());
        }

        if (StringUtils.isBlank(sourceInfoDeriveTableInfo.getCreator())) {
            sourceInfoDeriveTableInfo.setCreator(user.getUserId());
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableInfo.getOperator())) {
            sourceInfoDeriveTableInfo.setOperator(user.getUserId());
        }
        sourceInfoDeriveTableInfo.setCreateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setUpdater(user.getUserId());
        sourceInfoDeriveTableInfo.setUpdateTime(LocalDateTime.now());
        sourceInfoDeriveTableInfo.setTenantId(tenantId);
        sourceInfoDeriveTableInfo.setImportance(sourceInfoDeriveColumnInfos.stream().anyMatch(SourceInfoDeriveColumnInfo::isImportant));
        sourceInfoDeriveTableInfo.setSecurity(sourceInfoDeriveColumnInfos.stream().anyMatch(SourceInfoDeriveColumnInfo::isSecret));
        sourceInfoDeriveTableInfo.setVersion(-1);

        // ???????????????????????????0
        if (!sourceInfoDeriveTableColumnDto.isSubmit()) {
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.UN_COMMIT.getState());
        } else {
            // ?????????????????????????????????1
            // ??????DDL???DML??????
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
            deriveColumnInfo.setDataType(deriveColumnInfo.getDataType().toUpperCase());
        }

        // ???-????????????
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

            // ???????????????column_tag_relation_to_column???
            ProxyUtil.getProxy(SourceInfoDeriveTableInfoService.class).preserveTags(sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveColumnInfos);
        }

        // ???????????????????????????-?????????(???????????????0???????????????????????????????????????????????????????????????1??????????????????????????????????????????????????????????????????)
        if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
            // ??????????????????,??????????????????
            boolean isRelationExist = businessDAO.isRelationExist(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId());
            if (isRelationExist) {
                // ????????????????????????????????????
                businessDAO.updateRelationType(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId(), 1);
            } else {
                businessDAO.insertDerivedTableRelation(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), 1, sourceInfoDeriveTableInfo.getSourceId());
            }

            // ????????????????????????????????????
            businessDAO.updateTechnicalStatus(sourceInfoDeriveTableInfo.getBusinessId(), TechnicalStatus.ADDED.code);
        }
        return true;
    }

    /**
     * ???????????????column_tag_relation_to_column???
     * ??????source_info_derive_table_info??????table_guid???column_info????????????
     * ?????????????????????????????????????????????id,????????????id?????????id?????????column_tag_relation_to_column???
     *
     * @param tableGuid                   source_info_derive_table_info??????table_guid
     * @param sourceInfoDeriveColumnInfos ?????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void preserveTags(String tableGuid, List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        List<Column> columnInfoList = columnDAO.getColumnInfoList(tableGuid);
        List<ColumnTagRelationToColumn> columnTagRelationToColumns = new ArrayList<>();
        if (CollectionUtils.isEmpty(columnInfoList) || CollectionUtils.isEmpty(sourceInfoDeriveColumnInfos)) {
            return;
        }
        Map<String, Object> columnMap = columnInfoList.stream().collect(Collectors.toMap(Column::getColumnName, Column::getColumnId, (key1, key2) -> key1));
        // key????????????????????????
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        result.putAll(columnMap);
        List<String> columnGuids = columnInfoList.stream().map(Column::getColumnId).collect(Collectors.toList());
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (result.containsKey(sourceInfoDeriveColumnInfo.getColumnNameEn())) {
                List<ColumnTagRelationToColumn> columnTagRelations = packTags(result.get(sourceInfoDeriveColumnInfo.getColumnNameEn()).toString(),
                        sourceInfoDeriveColumnInfo.getTags());
                if (!CollectionUtils.isEmpty(columnTagRelations)) {
                    columnTagRelationToColumns.addAll(columnTagRelations);
                }
            }
        }
        columnTagDao.deleteRelationAll(columnGuids);
        if (!CollectionUtils.isEmpty(columnTagRelationToColumns)) {
            sourceInfoDeriveTableInfoDao.addPreserveTags(columnTagRelationToColumns);
        }

    }

    /**
     * ?????????????????????????????????
     *
     * @param columnGuid ?????????ID
     * @param strTags    ?????????????????????
     * @return ????????????????????????????????????
     */
    public List<ColumnTagRelationToColumn> packTags(String columnGuid, String strTags) {
        List<ColumnTagRelationToColumn> columnTagRelationToColumns = new ArrayList<>();
        if (StringUtils.isBlank(strTags) || StringUtils.isBlank(columnGuid)) {
            return columnTagRelationToColumns;
        }
        String[] tags = strTags.split(",");
        if (tags.length > 0) {
            for (int i = 0; i < tags.length; i++) {
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
     * ?????????????????????ID?????????????????????
     *
     * @param strTags ?????????????????????
     * @return ????????????????????????????????????
     */
    public List<ColumnTag> getTags(String strTags, String tableGuid, String columnNameEn, String tenantId) {
        List<ColumnTag> columnTags = new ArrayList<>();
        //1????????????????????????????????????
        if (StringUtils.isNotBlank(tableGuid) && StringUtils.isNotBlank(columnNameEn)) {
            List<Column> columnInfoList = columnDAO.getColumnInfoList(tableGuid);
            for (Column column : columnInfoList) {
                if (columnNameEn.equalsIgnoreCase(column.getColumnName())) {
                    columnTags = columnTagDao.getTagListByColumnId(tenantId, column.getColumnId());
                }
            }
        }
        //2????????????????????????????????????
        if (StringUtils.isNotBlank(strTags)) {
            String[] tags = strTags.split(",");
            List<String> listTags = Arrays.asList(tags);
            List<ColumnTag> tagListById = sourceInfoDeriveTableInfoDao.getTagListById(listTags);
            if (!CollectionUtils.isEmpty(tagListById)) {
                columnTags.addAll(tagListById);
                List<ColumnTag> collect = columnTags.stream().filter(distinctByKey(ColumnTag::getId)).collect(Collectors.toList());
                columnTags.clear();
                columnTags.addAll(collect);
            }
        }
        return columnTags;
    }

    /**
     * ????????????????????????
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
        // ??????????????????????????????
        List<Map<String, String>> listMaps = queryDbNameAndSourceNameByIds(dbId, sourceId);
        Map<String, String> maps = listMaps.stream().collect(Collectors.toMap(e -> e.get("id"), e -> e.get("name")));
        return maps.getOrDefault(dbId, "");
    }

    /**
     * ??????-????????????????????????
     *
     * @param sourceInfoDeriveTableColumnDto
     * @param tenantId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSaveAndSubmitDeriveTableInfo(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId) {
        User user = AdminUtils.getUserData();
        // ?????????id???guid
        String tableId = sourceInfoDeriveTableColumnDto.getId();
        String tableGuid = sourceInfoDeriveTableColumnDto.getTableGuid();

        // ???????????????????????????id
        SourceInfoDeriveTableInfo oldSourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDao.getByIdAndTenantId(tableId, tenantId);

        // ???
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = new SourceInfoDeriveTableInfo();
        BeanUtils.copyProperties(sourceInfoDeriveTableColumnDto, sourceInfoDeriveTableInfo);
        // ???
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();

        // ??????????????????
        for (SourceInfoDeriveColumnInfo deriveColumnInfo : sourceInfoDeriveColumnInfos) {
            deriveColumnInfo.setSort(sourceInfoDeriveColumnInfos.indexOf(deriveColumnInfo));
            deriveColumnInfo.setDataType(deriveColumnInfo.getDataType().toUpperCase());
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
        if (!submitWay(sourceInfoDeriveTableColumnDto, tenantId, tableId, tableGuid, sourceInfoDeriveTableInfo, sourceInfoDeriveColumnInfos)) {
            tableFieldRelationship(sourceInfoDeriveTableColumnDto, tenantId, oldSourceInfoDeriveTableInfo, sourceInfoDeriveTableInfo, sourceInfoDeriveColumnInfos);
        }
        return true;
    }

    /**
     * ????????????
     *
     * @param sourceInfoDeriveTableColumnDto ????????????DTO
     * @param tenantId                       ????????????id
     * @param tableId                        ???id
     * @param tableGuid                      ???guid
     * @param sourceInfoDeriveTableInfo      ???????????????
     * @param sourceInfoDeriveColumnInfos    ??????????????????
     * @return ?????????????????????
     */
    private boolean submitWay(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId, String tableId, String tableGuid, SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo, List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        // ????????????
        if (!sourceInfoDeriveTableColumnDto.isSubmit()) {
            // ????????????????????????
            if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
                // ?????????????????????????????????0????????????id
                this.updateVersionByTableId(tableId, 0);
                sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());
            } else { // ???????????????
                // ??????tableId?????????????????????
                sourceInfoDeriveColumnInfoService.deleteDeriveColumnInfoByTableId(tableId, tableGuid);
                sourceInfoDeriveTableColumnRelationService.deleteDeriveTableColumnRelationByTableId(tableId);
            }
            sourceInfoDeriveTableInfo.setVersion(-1);
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.UN_COMMIT.getState());

        } else {// ????????????????????????
            boolean hasModify = checkUpdateHasModify(sourceInfoDeriveTableInfo, sourceInfoDeriveColumnInfos, tenantId);
            // ????????????????????????
            if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
                if (hasModify) {
                    // ??????????????????????????????+1
                    this.updateVersionByTableGuid(tableGuid);
                    // ?????????????????????????????????1????????????id
                    this.updateVersionByTableId(tableId, 1);
                    sourceInfoDeriveTableInfo.setId(UUID.randomUUID().toString());
                } else {
                    return true;
                }
            } else { // ???????????????
                // ??????tableId?????????????????????
                sourceInfoDeriveColumnInfoService.deleteDeriveColumnInfoByTableId(tableId, tableGuid);
                sourceInfoDeriveTableColumnRelationService.deleteDeriveTableColumnRelationByTableId(tableId);
                if (hasModify) {
                    // ??????????????????????????????+1???????????????
                    this.updateVersionByTableGuid(tableGuid);
                } else {
                    sourceInfoDeriveTableInfoDao.updateVersionToShowByTableGuid(tableGuid);
                    // ??????tableId?????????????????????
                    sourceInfoDeriveTableInfoDao.deleteDeriveTableByTableId(tableId);
                    return true;
                }
            }

            // ???????????????1
            // ??????DDL???DML??????
            // ?????????1
            sourceInfoDeriveTableInfo.setVersion(-1);
            sourceInfoDeriveTableInfo.setState(DeriveTableStateEnum.COMMIT.getState());
            String targetDbName = getDbNameByDbId(sourceInfoDeriveTableInfo.getDbId(), sourceInfoDeriveTableInfo.getSourceId());
            sourceInfoDeriveTableInfo.setDdl(createDDL(sourceInfoDeriveTableColumnDto, targetDbName));
            sourceInfoDeriveTableInfo.setDml(createDML(sourceInfoDeriveTableColumnDto, null, targetDbName));

        }
        return false;
    }

    /**
     * ?????????-????????????
     *
     * @param sourceInfoDeriveTableColumnDto ????????????DTO
     * @param tenantId                       ????????????id
     * @param oldSourceInfoDeriveTableInfo   ?????????????????????
     * @param sourceInfoDeriveTableInfo      ?????????????????????
     * @param sourceInfoDeriveColumnInfos    ??????????????????
     */
    private void tableFieldRelationship(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId, SourceInfoDeriveTableInfo oldSourceInfoDeriveTableInfo, SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo, List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        // ???-????????????
        List<SourceInfoDeriveTableColumnRelation> sourceInfoDeriveTableColumnRelationList = new ArrayList<>(sourceInfoDeriveColumnInfos.size());

        // ?????????????????????column????????????????????????????????????
        List<SourceInfoDeriveColumnInfo> deriveColumnInfoListByTableGuid = sourceInfoDeriveColumnInfoService.getDeriveColumnInfoListByTableGuid(sourceInfoDeriveTableInfo.getTableGuid());

        // ???????????????ID?????????null???????????????????????????relation?????????????????????guid
        // ????????????????????????id???guid???
        List<SourceInfoDeriveColumnInfo> list = new ArrayList<>();
        for (SourceInfoDeriveColumnInfo deriveColumnInfo : sourceInfoDeriveColumnInfos) {
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
            // ?????????-????????????
            SourceInfoDeriveTableColumnRelation sourceInfoDeriveTableColumnRelation = new SourceInfoDeriveTableColumnRelation();
            sourceInfoDeriveTableColumnRelation.setId(UUID.randomUUID().toString());
            sourceInfoDeriveTableColumnRelation.setColumnGuid(deriveColumnInfo.getColumnGuid());
            sourceInfoDeriveTableColumnRelation.setTableId(sourceInfoDeriveTableInfo.getId());
            sourceInfoDeriveTableColumnRelation.setTableGuid(sourceInfoDeriveTableInfo.getTableGuid());
            sourceInfoDeriveTableColumnRelationList.add(sourceInfoDeriveTableColumnRelation);
            if (StringUtils.isNotBlank(deriveColumnInfo.getId())) {
                list.add(deriveColumnInfo);
            }
        }
        sourceInfoDeriveColumnInfos = list;

        this.saveOrUpdate(sourceInfoDeriveTableInfo);
        // ?????????????????????
        if (!CollectionUtils.isEmpty(sourceInfoDeriveColumnInfos)) {
            sourceInfoDeriveColumnInfoService.saveOrUpdateBatch(sourceInfoDeriveColumnInfos);
        }
        // ???????????????column_tag_relation_to_column???
        ProxyUtil.getProxy(SourceInfoDeriveTableInfoService.class).preserveTags(sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos());
        sourceInfoDeriveTableColumnRelationService.saveOrUpdateBatch(sourceInfoDeriveTableColumnRelationList);


        if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
            // ????????????????????????-???????????????
            businessDAO.deleteRelationByBusinessIdAndTableId(oldSourceInfoDeriveTableInfo.getBusinessId(), oldSourceInfoDeriveTableInfo.getTableGuid(), oldSourceInfoDeriveTableInfo.getSourceId(), 1);
            // ??????????????????-?????????(???????????????0???????????????????????????????????????????????????????????????1??????????????????????????????????????????????????????????????????)

            // ??????????????????,??????????????????
            boolean isRelationExist = businessDAO.isRelationExist(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId());
            if (isRelationExist) {
                // ????????????????????????????????????
                businessDAO.updateRelationType(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getSourceId(), 1);
            } else {
                businessDAO.insertDerivedTableRelation(sourceInfoDeriveTableInfo.getBusinessId(), sourceInfoDeriveTableInfo.getTableGuid(), 1, sourceInfoDeriveTableInfo.getSourceId());
            }
        }
    }

    boolean checkUpdateHasModify(SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo, List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos, String tenantId) {
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfoDB = null;
        if (DeriveTableStateEnum.COMMIT.getState().equals(sourceInfoDeriveTableInfo.getState())) {
            // todo ??????????????????????????????-1?????????????????????
            sourceInfoDeriveTableInfoDB = sourceInfoDeriveTableInfoDao.getByGuidAndTenantIdAndVersion(sourceInfoDeriveTableInfo.getTableGuid(), tenantId, -1);
        } else {
            // todo ?????????????????????0?????????????????????
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
        return !deriveColumnInfoListByTableId.containsAll(sourceInfoDeriveColumnInfos) ||
                !sourceInfoDeriveColumnInfos.containsAll(deriveColumnInfoListByTableId);
    }


    /**
     * ??????tableguid????????????
     *
     * @param tableGuid
     * @return
     */
    public boolean updateVersionByTableGuid(String tableGuid) {
        sourceInfoDeriveTableInfoDao.updateVersionByTableGuid(tableGuid);
        return true;
    }

    /**
     * ?????????????????????
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

        // ????????????????????????????????????guid - path
        Map<String, String> technicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, CommonConstant.TECHNICAL_CATEGORY_TYPE, null);
        // ???????????????????????????????????????guid - path
        Map<String, String> businessCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, CommonConstant.BUSINESS_CATEGORY_TYPE, null);

        return sourceInfoDeriveTableInfos.stream().map(sourceInfoDeriveTableInfo -> {
            SourceInfoDeriveTableVO sourceInfoDeriveTableVO = new SourceInfoDeriveTableVO();
            BeanUtils.copyProperties(sourceInfoDeriveTableInfo, sourceInfoDeriveTableVO);
            sourceInfoDeriveTableVO.setUpdateTime(sourceInfoDeriveTableInfo.getUpdateTimeStr());
            sourceInfoDeriveTableVO.setUpdater(sourceInfoDeriveTableInfo.getUpdaterName());
            sourceInfoDeriveTableVO.setBusiness(getBusiness(sourceInfoDeriveTableInfo.getBusinessId(), businessCategoryGuidPathMap));
            sourceInfoDeriveTableVO.setCategory(technicalCategoryGuidPathMap.getOrDefault(sourceInfoDeriveTableInfo.getCategoryId(), ""));
            sourceInfoDeriveTableVO.setQueryDDL(StringUtils.isNotBlank(sourceInfoDeriveTableInfo.getDdl()));
            return sourceInfoDeriveTableVO;
        }).collect(Collectors.toList());
    }

    private boolean updateVersionByTableId(String tableId, int version) {
        return sourceInfoDeriveTableInfoDao.updateVersionByTableId(tableId, version) > 0;
    }

    /**
     * ??????????????????????????????
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
     * ??????tableGuid?????????id????????????
     *
     * @param tenantId
     * @param tableGuids
     * @return
     */
    public List<String> getByGuidsAndTenantId(String tenantId, List<String> tableGuids) {
        return sourceInfoDeriveTableInfoDao.getByGuidsAndTenantId(tenantId, tableGuids);
    }

    /**
     * ??????
     *
     * @param sourceInfoDeriveTableInfo
     * @return
     */
    public boolean save(SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo) {
        return sourceInfoDeriveTableInfoDao.add(sourceInfoDeriveTableInfo) > 0;
    }

    /**
     * ???????????????
     *
     * @param sourceInfoDeriveTableInfo
     * @return
     */
    public boolean saveOrUpdate(SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo) {
        return sourceInfoDeriveTableInfoDao.upsert(sourceInfoDeriveTableInfo) > 0;
    }

    /**
     * ????????????id?????????id????????????
     *
     * @param tableId
     * @param tenantId
     * @return
     */
    public SourceInfoDeriveTableInfo getByIdAndTenantId(String tableId, String tenantId) {
        return sourceInfoDeriveTableInfoDao.getByIdAndTenantId(tableId, tenantId);
    }

    /**
     * ??????????????????
     *
     * @param source ?????????????????????
     * @return
     */
    public List<TechnicalCategory> getTechnicalCategory(boolean source, String tenantId) {
        List<CategoryPrivilege> categories = technicalREST.getCategories("ASC", tenantId);
        // ?????????????????????guid???db_info???source_info????????????????????????????????????????????????
        List<TechnicalCategory> databases = queryDbTypeByCategoryIds(tenantId, categories.stream().map(CategoryPrivilege::getGuid).collect(Collectors.toList()));

        // ??????categoryId -> dbType ???map
        Map<String, TechnicalCategory> categoryIdDbTypeMap = databases.stream().collect(Collectors.toMap(TechnicalCategory::getGuid, e -> e));

        // ??????????????????
        // ??????database?????????dbType??????
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

        // ?????????database????????????
        technicalCategories = getDatabaseCategoryAll(technicalCategories);

        // ?????????????????????
        if (!source) {
            removeSourceAll(technicalCategories);
        }

        setUpAndDown(technicalCategories, null);

        return technicalCategories;
    }

    /**
     * ????????????????????????up???dpwn
     *
     * @param technicalCategories
     * @param parentId            ???id??????????????????
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
     * ????????????????????????????????????????????????
     *
     * @param technicalCategories
     */
    private List<TechnicalCategory> getDatabaseCategoryAll(List<TechnicalCategory> technicalCategories) {
        List<TechnicalCategory> databaseAndParentList = new ArrayList<>();
        // ???????????????????????????
        List<String> databaseCategoryIds = technicalCategories.stream().filter(TechnicalCategory::isDataBase).map(TechnicalCategory::getGuid).collect(Collectors.toList());
        getDatabaseCategoryAllPath(technicalCategories, databaseAndParentList, databaseCategoryIds);
        databaseAndParentList = databaseAndParentList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(BusinessCategory::getGuid))), ArrayList::new));
        return databaseAndParentList;
    }

    /**
     * ??????????????????????????????
     *
     * @param technicalCategories
     */
    private void removeSourceAll(List<TechnicalCategory> technicalCategories) {
        List<String> sourceCategoryList = new ArrayList<>();
        List<String> collect = technicalCategories.stream().filter(e -> "?????????".equals(e.getName())).map(TechnicalCategory::getGuid).collect(Collectors.toList());
        getSourceAll(technicalCategories, sourceCategoryList, collect);
        technicalCategories.removeIf(e -> sourceCategoryList.contains(e.getGuid()));
    }

    /**
     * ????????????????????????????????????????????????
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
     * ????????????????????????????????????????????????
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
     * ??????????????????/???ID????????????????????????
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
     * ??????sourceTableId??????????????????
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
     * ??????????????????????????????????????????
     *
     * @param businessId
     * @param businessCategoryGuidPathMap
     * @return
     */
    private String getBusiness(String businessId, Map<String, String> businessCategoryGuidPathMap) {
        try {
            // ??????????????????id???????????????????????????id
            BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(businessId);
            if (null == businessInfo) {
                return "";
            }
            String s = businessCategoryGuidPathMap.get(businessInfo.getDepartmentId());
            return null == s ? null : (s + "/" + businessInfo.getName());
        } catch (Exception e) {
            LOG.error("????????????????????????");
            return "";
        }
    }

    /**
     * ??????????????????
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
     * ????????????id???????????????????????????id????????????????????????
     *
     * @param tenantId     ??????id
     * @param categoryType ???????????????0??????????????? 1???????????????
     * @return
     */
    public Map<String, String> getCategoryGuidPathMap(String tenantId, int categoryType, String guid) {
        List<CategoryGuidPath> guidPathByTenantIdAndCategoryType = StringUtils.isBlank(guid) ?
                categoryDAO.getGuidPathByTenantIdAndCategoryType(tenantId, categoryType) :
                categoryDAO.getGuidPathByTenantIdAndCategoryTypeAndId(tenantId, categoryType, guid);
        return guidPathByTenantIdAndCategoryType.stream().collect(Collectors.toMap(CategoryGuidPath::getGuid, CategoryGuidPath::getPath));
    }

    /**
     * ??????????????????id???????????????????????????
     *
     * @param categoryId ????????????id
     * @param tenantId
     * @return
     */
    public List<SourceBusinessInfo> getBusinessByCategoryId(String categoryId, String tenantId) {
        Parameters parameters = new Parameters();
        // ?????????
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
            LOG.info("?????? {} ?????????", tableGuid);
            return null;
        }
        List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId, sourceId, schemaId, table.getTableName());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(deriveTableInfoList)) {
            return null;
        }
        // ????????????????????????
        Optional<SourceInfoDeriveTableInfo> deriveTableInfoOpt = deriveTableInfoList.stream()
                .filter(f -> "1".equals(f.getState().toString()))
                .min(Comparator.comparing(SourceInfoDeriveTableInfo::getVersion));
        if (deriveTableInfoOpt.isPresent()) {
            SourceInfoDeriveTableInfo tableInfo = deriveTableInfoOpt.get();
            SourceInfoDeriveTableColumnVO info = new SourceInfoDeriveTableColumnVO();
            BeanUtils.copyProperties(tableInfo, info);
            Map<String, String> technicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, CommonConstant.TECHNICAL_CATEGORY_TYPE, info.getCategoryId());
            info.setCategory(technicalCategoryGuidPathMap.getOrDefault(info.getCategoryId(), ""));
            info.setCreateTime(tableInfo.getCreateTimeStr());

            BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(info.getBusinessId());
            if (null != businessInfo) {
                info.setBusiness(businessInfo.getName());
                info.setBusinessHeaderId(businessInfo.getDepartmentId());
                // ???????????????????????????????????????guid - path
                Map<String, String> businessCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, CommonConstant.BUSINESS_CATEGORY_TYPE, businessInfo.getDepartmentId());
                info.setBusinessHeader(businessCategoryGuidPathMap.getOrDefault(businessInfo.getDepartmentId(), ""));
            }

            // ??????????????????????????????id.name??????
            List<Map<String, String>> maps = queryDbNameAndSourceNameByIds(schemaId, sourceId);
            // id->name??????
            Map<String, String> collect = maps.stream().collect(Collectors.toMap(e -> e.get("id"), e -> e.get("name")));

            info.setDbName(collect.get(schemaId));
            info.setSourceName(collect.get(sourceId));

            return info;
        }
        return null;
    }

    /**
     * ???????????????????????????
     *
     * @param tenantId  ??????ID
     * @param tableGuid ?????????GUID
     * @return ?????????????????????
     */
    public SourceInfoDeriveTableColumnContrastVO queryDeriveTableContrastInfo(String tenantId, String tableGuid) {
        try {
            SourceInfoDeriveTableInfo deriveTableInfo = sourceInfoDeriveTableInfoDao.queryDeriveTableInfoByGuid(tenantId, tableGuid);
            if (deriveTableInfo == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
            }
            List<SourceInfoDeriveColumnInfo> deriveColumnInfoList = sourceInfoDeriveColumnInfoService.getDeriveColumnInfoListByTableId(deriveTableInfo.getId());
            List<Column> columnInfoList = columnDAO.getColumnInfoListByTableGuid(tableGuid);
            if (org.apache.commons.collections.CollectionUtils.isEmpty(deriveColumnInfoList)
                    || org.apache.commons.collections.CollectionUtils.isEmpty(columnInfoList)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
            }
            return deriveContrast(deriveColumnInfoList, columnInfoList);
        } catch (Exception e) {
            LOG.error("?????????????????????", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        }
    }

    /**
     * ?????????????????????
     *
     * @param deriveColumnInfoList ?????????????????????
     * @param columnInfoList       ?????????????????????
     * @return ??????????????????
     */
    public SourceInfoDeriveTableColumnContrastVO deriveContrast(List<SourceInfoDeriveColumnInfo> deriveColumnInfoList, List<Column> columnInfoList) {
        SourceInfoDeriveTableColumnContrastVO contrast = new SourceInfoDeriveTableColumnContrastVO();
        Map<String, SourceInfoDeriveColumnInfo> deriveMap = deriveColumnInfoList.stream().collect(Collectors.toMap(f -> f.getColumnNameEn().toLowerCase(), e -> e, (key1, key2) -> key2));
        Map<String, Column> dataColumnMap = columnInfoList.stream().collect(Collectors.toMap(f -> f.getColumnName().toLowerCase(), e -> e, (key1, key2) -> key2));
        List<DeriveTableColumnContrastVO> currentMetadata = new ArrayList<>();
        List<DeriveTableColumnContrastVO> oldMetadata = new ArrayList<>();
        // ????????????????????????
        for (Column column : columnInfoList) {
            DeriveTableColumnContrastVO current = new DeriveTableColumnContrastVO();
            DeriveTableColumnContrastVO old = new DeriveTableColumnContrastVO();
            current.setColumnName(column.getColumnName());
            current.setType(column.getType());
            current.setDescription(column.getDescription());
            // ????????????
            if (deriveMap.containsKey(column.getColumnName().toLowerCase())) {
                SourceInfoDeriveColumnInfo deriveColumn = deriveMap.get(column.getColumnName().toLowerCase());
                old.setColumnName(deriveColumn.getColumnNameEn());
                old.setType(deriveColumn.getDataType());
                old.setDescription(deriveColumn.getColumnNameZh());
                if (column.getType().equalsIgnoreCase(deriveColumn.getDataType())) {
                    current.setHasChange(false);
                    old.setHasChange(false);
                } else {
                    // ?????????????????????
                    current.setHasChange(true);
                    current.setContrast(TableColumnContrast.FIELD_TYPE_CHANGE.getCode());
                    old.setHasChange(true);
                    old.setContrast(TableColumnContrast.FIELD_TYPE_CHANGE.getCode());
                }
            } else {
                // ????????????????????????
                current.setHasChange(true);
                current.setContrast(TableColumnContrast.FIELD_ADD.getCode());
            }
            currentMetadata.add(current);
            oldMetadata.add(old);
        }
        // ?????????????????????????????????????????????
        List<SourceInfoDeriveColumnInfo> deficiencyList = deriveColumnInfoList.stream().filter(f -> !dataColumnMap.containsKey(f.getColumnNameEn().toLowerCase())).collect(Collectors.toList());
        for (SourceInfoDeriveColumnInfo deficiency : deficiencyList) {
            DeriveTableColumnContrastVO current = new DeriveTableColumnContrastVO();
            DeriveTableColumnContrastVO old = new DeriveTableColumnContrastVO();
            current.setColumnName(deficiency.getColumnNameEn());
            current.setType(deficiency.getDataType());
            current.setDescription(deficiency.getColumnNameZh());
            current.setHasChange(true);
            current.setContrast(TableColumnContrast.FIELD_DELETE.getCode());
            old.setColumnName(deficiency.getColumnNameEn());
            old.setType(deficiency.getDataType());
            old.setDescription(deficiency.getColumnNameZh());
            old.setHasChange(true);
            old.setContrast(TableColumnContrast.FIELD_DELETE.getCode());
            currentMetadata.add(current);
            oldMetadata.add(old);
        }
        contrast.setCurrentMetadata(currentMetadata);
        contrast.setOldMetadata(oldMetadata);
        return contrast;
    }

    /**
     * ????????????id?????????????????????
     *
     * @param tenantId
     * @param tableId
     * @return
     */
    public SourceInfoDeriveTableColumnVO getDeriveTableColumnDetail(String tenantId, String tableId) {
        // ????????????id???????????????
        SourceInfoDeriveTableInfo byId = this.getByIdAndTenantId(tableId, tenantId);
        if (null == byId) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????");
        }

        // ????????????id?????????
        List<SourceInfoDeriveColumnInfo> deriveColumnInfoListByTableId = sourceInfoDeriveColumnInfoService.getDeriveColumnInfoListByTableId(tableId);
        SourceInfoDeriveTableColumnVO sourceInfoDeriveTableColumnVO = new SourceInfoDeriveTableColumnVO();
        BeanUtils.copyProperties(byId, sourceInfoDeriveTableColumnVO);

        BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(sourceInfoDeriveTableColumnVO.getBusinessId());
        if (null != businessInfo) {
            // ???????????????????????????????????????guid - path
            Map<String, String> businessCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, CommonConstant.BUSINESS_CATEGORY_TYPE, businessInfo.getDepartmentId());
            sourceInfoDeriveTableColumnVO.setBusiness(businessInfo.getName());
            sourceInfoDeriveTableColumnVO.setBusinessHeaderId(businessInfo.getDepartmentId());
            sourceInfoDeriveTableColumnVO.setBusinessHeader(businessCategoryGuidPathMap.getOrDefault(businessInfo.getDepartmentId(), ""));
        }

        // ???????????????????????????????????????
        // ????????????????????????????????????guid - path
        Map<String, String> technicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, CommonConstant.TECHNICAL_CATEGORY_TYPE, sourceInfoDeriveTableColumnVO.getCategoryId());

        sourceInfoDeriveTableColumnVO.setCategory(technicalCategoryGuidPathMap.getOrDefault(sourceInfoDeriveTableColumnVO.getCategoryId(), ""));

        // ???????????????????????????????????????
        String dbId = sourceInfoDeriveTableColumnVO.getDbId();
        String sourceId = sourceInfoDeriveTableColumnVO.getSourceId();
        // ??????????????????????????????id.name??????
        List<Map<String, String>> maps = queryDbNameAndSourceNameByIds(dbId, sourceId);
        // id->name??????
        Map<String, String> collect = maps.stream().collect(Collectors.toMap(e -> e.get("id"), e -> e.get("name")));

        sourceInfoDeriveTableColumnVO.setDbName(collect.get(dbId));
        sourceInfoDeriveTableColumnVO.setSourceName(collect.get(sourceId));
        sourceInfoDeriveTableColumnVO.setCreateTime(byId.getCreateTimeStr());
        sourceInfoDeriveTableColumnVO.setUpdateTime(byId.getUpdateTimeStr());

        if (StringUtils.isNotBlank(byId.getSourceTableGuid())) {
            TableInfo sourceTableInfo = tableDAO.getTableInfoByTableguidAndStatus(byId.getSourceTableGuid());
            if (null == sourceTableInfo) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }

            String sourceCategoryId = categoryDAO.queryCategoryIdByGuidByDBId(sourceTableInfo.getDatabaseGuid(), tenantId);
            Map<String, String> sourceTechnicalCategoryGuidPathMap = getCategoryGuidPathMap(tenantId, CommonConstant.TECHNICAL_CATEGORY_TYPE, sourceCategoryId);
            sourceInfoDeriveTableColumnVO.setSourceTable(sourceTableInfo.getTableName());
            // ????????????????????????
            sourceInfoDeriveTableColumnVO.setSourceDbGuid(sourceCategoryId);
            sourceInfoDeriveTableColumnVO.setSourceDb(sourceTechnicalCategoryGuidPathMap.getOrDefault(sourceCategoryId, ""));
        }

        Set<String> sourceTableIdLst = new HashSet<>();
        Set<String> sourceColumnIdList = new HashSet<>();
        Set<String> sourceDbGuidList = new HashSet<>();
        List<Column> columnList = new ArrayList<>();
        List<TableInfo> tableInfoList = new ArrayList<>();
        List<TableDataSourceRelationPO> tableDataSourceRelationPOList = new ArrayList<>();
        deriveColumnInfoListByTableId.stream().forEach(p -> {
            sourceTableIdLst.add(p.getSourceTableGuid());
            sourceColumnIdList.add(p.getSourceColumnGuid());
        });
        if (!CollectionUtils.isEmpty(sourceColumnIdList)) {
            columnList = columnDAO.selectListByColumnGuid(sourceColumnIdList);
        }
        if (!CollectionUtils.isEmpty(sourceTableIdLst)) {
            tableInfoList = tableDAO.selectListByGuid(sourceTableIdLst);
        }
        tableInfoList.stream().forEach(p -> sourceDbGuidList.add(p.getDatabaseGuid()));
        if (!CollectionUtils.isEmpty(sourceDbGuidList)) {
            tableDataSourceRelationPOList = sourceInfoDAO.selectListByTenantIdAndDbId(tenantId, sourceDbGuidList);
        }
        Map<String, Column> columnMap = columnList.stream().collect(Collectors.toMap(Column::getColumnId, column -> column));
        Map<String, TableInfo> tableMap = tableInfoList.stream().collect(Collectors.toMap(TableInfo::getTableGuid, tableInfo -> tableInfo));
        Map<String, TableDataSourceRelationPO> sourceInfoMap = tableDataSourceRelationPOList.stream().collect(Collectors.toMap(TableDataSourceRelationPO::getDatabaseId, tableDataSourceRelationPO -> tableDataSourceRelationPO));
        sourceInfoDeriveTableColumnVO.setSourceInfoDeriveColumnVOS(deriveColumnInfoListByTableId.stream().map(e -> {
            SourceInfoDeriveColumnVO sourceInfoDeriveColumnVO = new SourceInfoDeriveColumnVO();
            BeanUtils.copyProperties(e, sourceInfoDeriveColumnVO);
            sourceInfoDeriveColumnVO.setDataType(sourceInfoDeriveColumnVO.getDataType().toUpperCase());
            sourceInfoDeriveColumnVO.setTags(getTags(e.getTags(), byId.getTableGuid(), sourceInfoDeriveColumnVO.getColumnNameEn(), tenantId));
            if (StringUtils.isBlank(e.getSourceColumnGuid())) {
                return sourceInfoDeriveColumnVO;
            }
            TableInfo tableInfo = tableMap.get(e.getSourceTableGuid());
            if (null == tableInfo) {
                return sourceInfoDeriveColumnVO;
            }
            Column column = columnMap.get(e.getSourceColumnGuid());
            // ???????????????????????????
            TableDataSourceRelationPO tableDataSourceRelationPO = sourceInfoMap.get(tableInfo.getDatabaseGuid());
            if (tableDataSourceRelationPO != null) {
                sourceInfoDeriveColumnVO.setSourceDbGuid(tableDataSourceRelationPO.getCategoryId());
            }
            sourceInfoDeriveColumnVO.setDataBaseName(tableInfo.getDbName());
            sourceInfoDeriveColumnVO.setSourceTableGuid(tableInfo.getTableGuid());
            sourceInfoDeriveColumnVO.setSourceTableNameEn(tableInfo.getTableName());
            sourceInfoDeriveColumnVO.setSourceTableNameZh(tableInfo.getDescription());
            sourceInfoDeriveColumnVO.setSourceColumnNameEn(column.getColumnName());
            sourceInfoDeriveColumnVO.setSourceColumnNameZh(column.getDescription());
            sourceInfoDeriveColumnVO.setSourceColumnType(column.getType());
            return sourceInfoDeriveColumnVO;
        }).collect(Collectors.toList()));
        return sourceInfoDeriveTableColumnVO;
    }

    /**
     * ?????????????????????
     *
     * @param tableGuids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDeriveTable(List<String> tableGuids) {
        // ?????????????????????
        List<SourceInfoDeriveTableInfo> sourceInfoDeriveTableInfos = sourceInfoDeriveTableInfoDao.getDeriveTableInfoByGuids(tableGuids);
        if (!CollectionUtils.isEmpty(sourceInfoDeriveTableInfos)) {
            // ????????????????????????-???????????????
            businessDAO.batchDeleteRelationByBusinessIdsAndTableIds(sourceInfoDeriveTableInfos);
        }

        sourceInfoDeriveTableInfoDao.deleteByTableGuids(tableGuids);
        sourceInfoDeriveColumnInfoService.deleteByTableGuids(tableGuids);
        sourceInfoDeriveTableColumnRelationService.deleteByTableGuids(tableGuids);
        return true;
    }

    /**
     * ??????DDL
     */
    private String createDDL(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String dbName) {
        //oracle????????????????????????????????????????????????hive??????``
        String tableFiled = "";
        if (Constant.HIVE.equalsIgnoreCase(sourceInfoDeriveTableColumnDto.getDbType())) {
            tableFiled = "`";
        }
        if (Constant.ORACLE.equalsIgnoreCase(sourceInfoDeriveTableColumnDto.getDbType())) {
            tableFiled = "\"";
        }
        // ??????????????????????????????
        String lengthStr = "(255)";
        String tableNameEn = sourceInfoDeriveTableColumnDto.getTableNameEn();
        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        StringBuilder tableDDL = new StringBuilder("CREATE TABLE ").append(tableFiled).append(dbName).append(tableFiled).append(".").
                append(tableFiled).append(tableNameEn).append(tableFiled).append("(\r\n");

        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        addTimeField(sourceInfoDeriveColumnInfos);
        StringBuilder columnDDL = new StringBuilder();
        StringBuilder commentDDL = new StringBuilder();
        StringBuilder primaryKeyDDLHeader = new StringBuilder("ALTER TABLE ").append(tableFiled).append(tableNameEn).
                append(tableFiled).append(" ADD PRIMARY KEY (");
        StringBuilder primaryKeyField = new StringBuilder();
        for (int i = 0; i < sourceInfoDeriveColumnInfos.size(); i++) {
            SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = sourceInfoDeriveColumnInfos.get(i);
            String columnNameEn = sourceInfoDeriveColumnInfo.getColumnNameEn();
            String dataType = sourceInfoDeriveColumnInfo.getDataType();
            // ????????????dbType??????????????????
            if (Constant.DATA_LENGTH_TYPE_MAP.get(dbType) != null) {
                dataType = Constant.DATA_LENGTH_TYPE_MAP.get(dbType).contains(dataType.toLowerCase()) ? dataType.concat(lengthStr) : dataType;
            }
            Map<String, String> stringStringMap = Constant.REPLACE_TYPE_MAP.get(dbType);
            if (!CollectionUtils.isEmpty(stringStringMap)) {
                dataType = stringStringMap.getOrDefault(dataType.toLowerCase(), dataType);
            }
            // ????????????????????????????????????
            columnDDL.append(tableFiled).append(columnNameEn).append(tableFiled).append(" ").append(dataType);

            // ????????????????????????
            String columnNameZh = sourceInfoDeriveColumnInfo.getColumnNameZh();
            if (StringUtils.isNotBlank(columnNameZh)) {
                if (Arrays.asList(Constant.HIVE, Constant.MYSQL).contains(dbType.toUpperCase())) {
                    columnDDL.append(" COMMENT '").append(columnNameZh).append("'");
                } else {
                    commentDDL.append("COMMENT ON COLUMN ").append(tableFiled).append(tableNameEn).append(tableFiled).
                            append(".").append(tableFiled).append(columnNameEn).append(tableFiled).append(" IS '").append(columnNameZh).append("';\r\n");
                }
            }
            if (i < sourceInfoDeriveColumnInfos.size() - 1) {
                columnDDL.append(",\r\n");
            }
            // ????????????
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
        return tableDDL.toString().toUpperCase().replaceFirst(dbName.toUpperCase(), dbName);
    }


    /**
     * ??????DML??????
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    private String createDML(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String sourceDbName, String targetDbName) {
        // ?????????????????????????????????-?????????
        String tableNameEn = sourceInfoDeriveTableColumnDto.getTableNameEn();
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        addTimeField(sourceInfoDeriveColumnInfos);
        StringBuilder columnBuilder = new StringBuilder("insert into ").append(targetDbName).append(".").append(tableNameEn).append("\r\n");
        removeTimeField(sourceInfoDeriveColumnInfos);

        columnBuilder.append(this.getMapping(sourceInfoDeriveTableColumnDto, sourceDbName));
        return columnBuilder.toString();
    }

    /**
     * ??????select??????
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    private String getMapping(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String dbName) {
        StringBuilder str = new StringBuilder();
        StringBuilder strColumn = new StringBuilder();
        StringBuilder strSelect = new StringBuilder();
        strColumn.append("(");
        List<String> tableSet = new ArrayList<>();
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSourceColumnNameEn())) {
                continue;
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSourceTableGuid())
                    && !tableSet.contains(sourceInfoDeriveColumnInfo.getSourceTableGuid())) {
                tableSet.add(sourceInfoDeriveColumnInfo.getSourceTableGuid());
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
        // ?????????????????????????????????????????????????????????(??????1????????????????????????)
        if (tableSet.size() == 1) {
            TableEntity sourceTable = tableDAO.selectById(tableSet.get(0));
            if (sourceTable == null) {
                str.append(strSelect).append("\r\n from ").append(";");
            } else {
                str.append(strSelect).append(" \r\n from ").append(sourceTable.getDbName()).append(".").append(sourceTable.getName()).append(";");
            }
        } else {
            str.append(strSelect).append(" \r\n from ").append(";");
        }

        return str.toString();
    }

    private void addTimeField(List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = new SourceInfoDeriveColumnInfo();
        sourceInfoDeriveColumnInfo.setId("delete");
        sourceInfoDeriveColumnInfo.setColumnNameEn(CommonConstant.ETL_DATE);
        sourceInfoDeriveColumnInfo.setColumnNameZh("????????????");
        sourceInfoDeriveColumnInfo.setDataType("timestamp");
        sourceInfoDeriveColumnInfo.setMappingDescribe("?????????????????????");
        sourceInfoDeriveColumnInfos.add(sourceInfoDeriveColumnInfo);
    }

    private void removeTimeField(List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        sourceInfoDeriveColumnInfos.removeIf(e -> Objects.equals("delete", e.getId()));
    }

    public Result checkAddOrEditDeriveTableEntity(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto, String tenantId) {
        if (!checkTableNameDump(sourceInfoDeriveTableColumnDto.getTableNameEn(), sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getId())) {
            return ReturnUtil.error("400", "?????????????????????????????????");
        }

        if (!sourceInfoDeriveTableColumnDto.isSubmit()) {
            return ReturnUtil.success();
        }

        // ???????????????????????????????????????
        String sourceInfoDbTypeKey = "dbr";

        // ?????????????????????
        String timeField = CommonConstant.ETL_DATE;

        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        if (StringUtils.isEmpty(dbType)) {
            return ReturnUtil.error("400", "???????????????????????????");
        }
        // ?????????????????????
        List<DataSourceTypeInfo> dataSourceType = dataSourceService.getDataSourceType(sourceInfoDbTypeKey);
        if (dataSourceType.stream().noneMatch(e -> e.getName().equalsIgnoreCase(dbType))) {
            return ReturnUtil.error("400", CommonConstant.DATA_SOURCE_NOT_PROPERLY_DESCRIBED);
        }

        // ??????????????????
        if (!checkTableOrColumnNameEnPattern(sourceInfoDeriveTableColumnDto.getTableNameEn())) {
            return ReturnUtil.error("400", "?????????????????????????????????");
        }
        // ??????
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();

        // ???????????????etl_date
        if (sourceInfoDeriveColumnInfos.stream().anyMatch(e -> Objects.equals(timeField, e.getColumnNameEn()))) {
            return ReturnUtil.error("400", "???????????????????????????????????????:" + timeField);
        }
        // ?????????????????????
        List<String> errorNames = sourceInfoDeriveColumnInfos.stream().filter(e -> !checkColumnNameEn(e.getColumnNameEn())).map(SourceInfoDeriveColumnInfo::getColumnNameEn).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorNames)) {
            return ReturnUtil.error("400", "???????????????????????????????????????:" + errorNames.toString());
        }
        // ????????????
        List<String> dumpNames = sourceInfoDeriveColumnInfos.stream().collect(Collectors.groupingBy(SourceInfoDeriveColumnInfo::getColumnNameEn, Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dumpNames)) {
            return ReturnUtil.error("400", "???????????????????????????????????????:" + dumpNames.toString());
        }
        // ???????????????????????????????????????
        List<String> dataTypeList = Constant.DATA_TYPE_MAP.get(dbType);
        List<String> errorDbTypes = sourceInfoDeriveColumnInfos.stream().filter(e -> !dataTypeList.contains(e.getDataType().toLowerCase())).map(SourceInfoDeriveColumnInfo::getDataType).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorDbTypes)) {
            return ReturnUtil.error("400", "??????????????????????????????????????????:" + errorDbTypes.toString());
        }
        if (dbType.equals(Constant.ORACLE)) {
            long count = sourceInfoDeriveColumnInfos.stream().filter(e -> Arrays.asList("long", "long raw").contains(e.getDataType())).count();
            if (count > 1) {
                return ReturnUtil.error("400", "???????????????????????????long?????????????????????'long' ??? 'long raw'????????????");
            }
        }
        // ??????????????????
        if (!checkCategoryByGuid(sourceInfoDeriveTableColumnDto.getCategoryId(), tenantId)) {
            return ReturnUtil.error("400", "?????????/?????????????????????");
        }
        // ??????????????????
        if (!checkBusinessByGuid(sourceInfoDeriveTableColumnDto.getBusinessId())) {
            return ReturnUtil.error("400", "??????????????????????????????");
        }
        // ???????????????????????????
        if (!checkDatabaseAndDataSource(sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getSourceId())) {
            return ReturnUtil.error("400", "??????????????????????????????");
        }
        // ????????????

        // ???????????????
        List<String> sourceColumnIds = sourceInfoDeriveColumnInfos.stream().map(SourceInfoDeriveColumnInfo::getSourceColumnGuid).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(sourceColumnIds) && !checkSourceColumnsByGuid(sourceColumnIds)) {
            return ReturnUtil.error("400", "????????????????????????");
        }
        return ReturnUtil.success();
    }

    /**
     * ??????????????????????????????????????????????????????????????????
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
     * ??????tableId???tableGuid
     *
     * @param id
     * @param tenantId
     * @return
     */
    public SourceInfoDeriveTableInfo getTableByIdAndGuid(String id, String tenantId) {
        return sourceInfoDeriveTableInfoDao.getByIdAndGuidAndTenantId(id, tenantId);
    }

    /**
     * ??????categoryid
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
     * ??????businessid
     *
     * @param guid
     * @return
     */
    boolean checkBusinessByGuid(String guid) {
        return StringUtils.isNotBlank(guid) && null != businessDAO.queryBusinessByBusinessId(guid);
    }

    /**
     * ??????sourceTableGuid
     *
     * @param guid
     * @return
     */
    boolean checkSourceTableByGuid(String guid) {
        return StringUtils.isNotBlank(guid) && null != tableDAO.getTableInfoByTableguidAndStatus(guid);

    }

    /**
     * ??????sourceColumnGuid
     *
     * @return
     */
    boolean checkSourceColumnsByGuid(List<String> guids) {
        List<String> sourceColumnInfoList = columnDAO.queryColumnidBycolumnIds(guids);
        return sourceColumnInfoList.containsAll(guids);
    }

    /**
     * ???????????????id????????????id
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
     * ???????????????????????????????????????
     *
     * @param dbType
     * @return
     */
    public Result getDataTypeByDbType(String dbType) {
        // ???????????????????????????????????????
        String sourceInfoDbTypeKey = "dbr";
        // ??????????????????????????????
        List<DataSourceTypeInfo> dataSourceType = dataSourceService.getDataSourceType(sourceInfoDbTypeKey);
        if (dataSourceType.stream().noneMatch(e -> e.getName().equalsIgnoreCase(dbType))) {
            return ReturnUtil.error("400", CommonConstant.DATA_SOURCE_NOT_PROPERLY_DESCRIBED);
        }
        List<String> list = Constant.DATA_TYPE_MAP.get(dbType);
        if (CollectionUtils.isEmpty(list)) {
            return ReturnUtil.error("400", CommonConstant.DATA_SOURCE_NOT_PROPERLY_DESCRIBED);
        }
        return ReturnUtil.success(list.stream().map(String::toUpperCase).collect(Collectors.toList()));
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
            InterProcessMutex lock = zkLockUtils.getInterProcessMutex(CommonConstant.METASPACE_DERIVE_TABLE_IMPORT_LOCK);
            try {
                LOG.info("???????????? : {} {}", Thread.currentThread().getName(), CommonConstant.METASPACE_DERIVE_TABLE_IMPORT_LOCK);
                if (lock.acquire(LOCK_TIME_OUT_TIME, TimeUnit.MINUTES)) {
                    LOG.info("???????????? : {} {}", Thread.currentThread().getName(), CommonConstant.METASPACE_DERIVE_TABLE_IMPORT_LOCK);
                    this.fileUploadSubmit(deriveFileDTO.getFileName(), deriveFileDTO.getPath(), tenantId);
                    fileInfoService.createFileuploadRecord(deriveFileDTO.getPath(), deriveFileDTO.getFileName(), FileInfoPath.DRIVE_TABLE);
                }
                LOG.info("?????????????????????[{}]??????", deriveFileDTO.getFileName());
            } catch (Exception e) {
                error = e.getMessage();
                LOG.error("?????????????????????", e);
            } finally {
                try {
                    lock.release();
                } catch (Exception e) {
                    LOG.error("???????????????", e);
                }
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
            return ProxyUtil.getProxy(SourceInfoDeriveTableInfoService.class).createSaveAndSubmitDeriveTableInfo(sourceInfoDeriveTableColumnDTO, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * ?????????????????????
     *
     * @param fileName
     * @param filePath
     */
    public Result fileUploadDerive(String fileName, String filePath, String tenantId) {
        try {
            if (!(fileName.endsWith(CommonConstant.EXCEL_FORMAT_XLSX) || fileName.endsWith(CommonConstant.EXCEL_FORMAT_XLS))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
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
            return ReturnUtil.error("400", "?????????????????????????????????");
        }

        // ???????????????????????????????????????
        String sourceInfoDbTypeKey = "dbr";

        // ?????????????????????
        String timeField = CommonConstant.ETL_DATE;

        String dbType = sourceInfoDeriveTableColumnDto.getDbType();
        if (StringUtils.isEmpty(dbType)) {
            return ReturnUtil.error("400", "???????????????????????????");
        }
        // ?????????????????????
        List<DataSourceTypeInfo> dataSourceType = dataSourceService.getDataSourceType(sourceInfoDbTypeKey);
        if (dataSourceType.stream().noneMatch(e -> e.getName().equalsIgnoreCase(dbType))) {
            return ReturnUtil.error("400", CommonConstant.DATA_SOURCE_NOT_PROPERLY_DESCRIBED);
        }

        // ??????????????????
        if (!checkTableOrColumnNameEnPattern(sourceInfoDeriveTableColumnDto.getTableNameEn())) {
            return ReturnUtil.error("400", "?????????????????????????????????");
        }
        // ??????
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();

        // ???????????????etl_date
        if (sourceInfoDeriveColumnInfos.stream().anyMatch(e -> Objects.equals(timeField, e.getColumnNameEn()))) {
            return ReturnUtil.error("400", "???????????????????????????????????????:" + timeField);
        }
        // ?????????????????????
        List<String> errorNames = sourceInfoDeriveColumnInfos.stream().filter(e -> !checkColumnNameEn(e.getColumnNameEn())).map(SourceInfoDeriveColumnInfo::getColumnNameEn).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorNames)) {
            return ReturnUtil.error("400", "???????????????????????????????????????:" + errorNames.toString());
        }
        // ????????????
        List<String> dumpNames = sourceInfoDeriveColumnInfos.stream().collect(Collectors.groupingBy(SourceInfoDeriveColumnInfo::getColumnNameEn, Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dumpNames)) {
            return ReturnUtil.error("400", "???????????????????????????????????????:" + dumpNames.toString());
        }
        // ???????????????????????????????????????
        List<String> dataTypeList = Constant.DATA_TYPE_MAP.get(dbType);
        List<String> errorDbTypes = sourceInfoDeriveColumnInfos.stream().filter(e -> !dataTypeList.contains(e.getDataType().toLowerCase())).map(SourceInfoDeriveColumnInfo::getDataType).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorDbTypes)) {
            return ReturnUtil.error("400", "??????????????????????????????????????????:" + errorDbTypes.toString());
        }
        if (dbType.equals(Constant.ORACLE)) {
            long count = sourceInfoDeriveColumnInfos.stream().filter(e -> Arrays.asList("long", "long raw").contains(e.getDataType())).count();
            if (count > 1) {
                return ReturnUtil.error("400", "???????????????????????????long?????????????????????'long' ??? 'long raw'????????????");
            }
        }
        // ??????????????????
        if (!checkCategoryByGuid(sourceInfoDeriveTableColumnDto.getCategoryId(), tenantId)) {
            return ReturnUtil.error("400", "?????????/?????????????????????");
        }
        // ??????????????????
        if (!checkBusinessByGuid(sourceInfoDeriveTableColumnDto.getBusinessId())) {
            return ReturnUtil.error("400", "??????????????????????????????");
        }
        // ???????????????????????????
        if (!checkDatabaseAndDataSource(sourceInfoDeriveTableColumnDto.getDbId(), sourceInfoDeriveTableColumnDto.getSourceId())) {
            return ReturnUtil.error("400", "??????????????????????????????");
        }
        // ???????????????
        List<String> sourceColumnIds = sourceInfoDeriveColumnInfos.stream().map(SourceInfoDeriveColumnInfo::getSourceColumnGuid).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(sourceColumnIds) && !checkSourceColumnsByGuid(sourceColumnIds)) {
            return ReturnUtil.error("400", "????????????????????????");
        }
        return ReturnUtil.success();
    }

    private void fileCheckDb(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        this.getTechnicalCategoryId(sourceInfoDeriveTableColumnDTO, tenantId);

        String userId = userDAO.selectByTenantIdAndName(tenantId, sourceInfoDeriveTableColumnDTO.getCreatorName());
        if (StringUtils.isBlank(userId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????????????????");
        }
        sourceInfoDeriveTableColumnDTO.setCreator(userId);

        this.getBusinessCategoryId(sourceInfoDeriveTableColumnDTO, tenantId);

        this.getBusinessId(sourceInfoDeriveTableColumnDTO, tenantId);

        this.checkColumn(sourceInfoDeriveTableColumnDTO, tenantId);
    }

    /**
     * ????????????????????????
     *
     * @param dataTypeList
     * @param
     * @return
     */
    private Boolean checkDataType(List<String> dataTypeList, SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo) {
        if (StringUtils.isEmpty(sourceInfoDeriveColumnInfo.getDataType())) {
            return false;
        }
        String dataType = sourceInfoDeriveColumnInfo.getDataType();
        int start = dataType.indexOf("(");
        int end = dataType.indexOf(")");
        if (start != -1 && end != -1) {
            dataType = dataType.substring(0, start);
        }
        if (dataTypeList.contains(dataType.toUpperCase())) {
            sourceInfoDeriveColumnInfo.setDataType(dataType);
            return true;
        }
        if (dataType.toLowerCase().contains("decimal") && dataTypeList.contains("DECIMAL")) {
            sourceInfoDeriveColumnInfo.setDataType("decimal");
            return true;
        }
        return false;
    }

    /**
     * ??????????????????
     *
     * @param sourceInfoDeriveTableColumnDTO
     * @param tenantId
     */
    private void checkColumn(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        List<String> dataTypeList = (List<String>) this.getDataTypeByDbType(sourceInfoDeriveTableColumnDTO.getDbType()).getData();
        if (CollectionUtils.isEmpty(dataTypeList)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, CommonConstant.DATA_SOURCE_NOT_PROPERLY_DESCRIBED);
        }
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos();
        List<TechnicalCategory> technicalCategoryList = this.getTechnicalCategory(true, tenantId);
        List<String> dbIdList = new ArrayList<>();
        technicalCategoryList.stream().forEach(p -> dbIdList.add(p.getDbId()));
        List<String> dbNameList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        for (int i = 0; i < sourceInfoDeriveColumnInfos.size(); i++) {
            SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = sourceInfoDeriveColumnInfos.get(i);
            if (!this.checkDataType(dataTypeList, sourceInfoDeriveColumnInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????????????????????????????:" + sourceInfoDeriveColumnInfo.getDataType());
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
        for (int i = 0; i < sourceInfoDeriveColumnInfos.size(); i++) {
            SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo = sourceInfoDeriveColumnInfos.get(i);
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getTagsName())) {
                List<String> tagIdList = new ArrayList<>();
                for (String value : sourceInfoDeriveColumnInfo.getTagsName().split(",")) {
                    if (StringUtils.isNotBlank(value)) {
                        String id = mapColumn.get(value);
                        if (StringUtils.isBlank(id)) {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????????????????????????????:" + value);
                        }
                        tagIdList.add(id);
                    }
                }
                sourceInfoDeriveColumnInfo.setTags(StringUtils.join(tagIdList, ","));
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSourceDbName()) && StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSourceTableNameEn())) {
                continue;
            }
            List<TableInfoDerivePO> collect = tableInfoDerivePOList.stream().filter(p -> p.getDatabaseName().equalsIgnoreCase(sourceInfoDeriveColumnInfo.getSourceDbName()) &&
                    p.getTableName().equalsIgnoreCase(sourceInfoDeriveColumnInfo.getSourceTableNameEn()) &&
                    p.getColumnName().equalsIgnoreCase(sourceInfoDeriveColumnInfo.getSourceColumnNameEn())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                StringBuilder str = new StringBuilder();
                str.append("[").append(sourceInfoDeriveColumnInfo.getSourceDbName()).append("][").append(sourceInfoDeriveColumnInfo.getSourceTableNameEn()).append("][").append(sourceInfoDeriveColumnInfo.getSourceColumnNameEn()).append("]");
                LOG.warn("?????????????????????????????????????????????????????????????????????????????????????????????????????????:" + str.toString());
                sourceInfoDeriveColumnInfo.setSourceTableGuid("");
                sourceInfoDeriveColumnInfo.setSourceColumnGuid("");
                sourceInfoDeriveColumnInfo.setSourceColumnNameEn("");
                return;
            }
            if (collect.size() > 1) {
                StringBuilder str = new StringBuilder();
                str.append("[").append(sourceInfoDeriveColumnInfo.getSourceDbName()).append("][").append(sourceInfoDeriveColumnInfo.getSourceTableNameEn()).append("][").append(sourceInfoDeriveColumnInfo.getSourceColumnNameEn()).append("]");
                LOG.warn("????????????????????????????????????????????????????????????????????????????????????????????????????????????:" + str.toString());
                sourceInfoDeriveColumnInfo.setSourceTableGuid("");
                sourceInfoDeriveColumnInfo.setSourceColumnGuid("");
                sourceInfoDeriveColumnInfo.setSourceColumnNameEn("");
                return;
            }
            sourceInfoDeriveColumnInfo.setSourceTableGuid(collect.get(0).getTableGuid());
            sourceInfoDeriveColumnInfo.setSourceColumnGuid(collect.get(0).getColumnGuid());
            sourceInfoDeriveColumnInfo.setSourceColumnNameEn(collect.get(0).getColumnName());
        }

    }

    private void getBusinessId(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO, String tenantId) {
        List<SourceBusinessInfo> businessByCategoryId = this.getBusinessByCategoryId(sourceInfoDeriveTableColumnDTO.getBusinessCategoryId(), tenantId);
        List<SourceBusinessInfo> list = businessByCategoryId.stream().filter(p -> p.getName().equals(sourceInfoDeriveTableColumnDTO.getBusinessName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????????????????????????????????????????:" + sourceInfoDeriveTableColumnDTO.getBusinessName());
        }
        sourceInfoDeriveTableColumnDTO.setBusinessId(list.get(0).getBusinessId());
    }

    /**
     * ??????????????????????????????
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????????????????????????????????????????");
            }
            if (index == split.length - 1) {
                sourceInfoDeriveTableColumnDTO.setBusinessCategoryId(list.get(0).getGuid());
                break;
            }
            parentGuid = list.get(0).getGuid();
        }
    }

    /**
     * ??????????????????id
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????/??????????????????????????????????????????");
            }
            if (index == split.length - 1) {
                if (StringUtils.isBlank(list.get(0).getDbType())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????/?????????????????????????????????????????????");
                }
                sourceInfoDeriveTableColumnDTO.setCategoryId(list.get(0).getGuid());
                sourceInfoDeriveTableColumnDTO.setDbType(list.get(0).getDbType());
                sourceInfoDeriveTableColumnDTO.setDbId(list.get(0).getDbId());
                sourceInfoDeriveTableColumnDTO.setSourceId(list.get(0).getSourceId());
                return;
            }
            parentGuid = list.get(0).getGuid();
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????/??????????????????????????????????????????");
    }

    /**
     * ????????????
     *
     * @param sourceInfoDeriveTableColumnDTO
     */
    private void fileCheckBlank(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO) {
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getTableNameEn())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????????????????");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getTableNameZh())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????????????????");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getCategoryName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????/???");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getCreatorName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getBusinessCategoryName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
        }
        if (StringUtils.isBlank(sourceInfoDeriveTableColumnDTO.getBusinessName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
        }
        if (CollectionUtils.isEmpty(sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
        }
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos();
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getColumnNameEn())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getColumnNameZh())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getDataType())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????????????????");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getSecretName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
            if (StringUtils.isBlank(sourceInfoDeriveColumnInfo.getImportantName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????????????????");
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param sourceInfoDeriveTableColumnDTO
     */
    private void fileCheckLength(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO) {
        if (sourceInfoDeriveTableColumnDTO.getTableNameEn().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????128???");
        }
        if (sourceInfoDeriveTableColumnDTO.getTableNameZh().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????128???");
        }
        if (sourceInfoDeriveTableColumnDTO.getCategoryName().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????/???????????????128???");
        }
        if (sourceInfoDeriveTableColumnDTO.getCreatorName().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????128???");
        }
        if (sourceInfoDeriveTableColumnDTO.getBusinessCategoryName().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
        }
        if (sourceInfoDeriveTableColumnDTO.getBusinessName().length() > CommonConstant.BUSINESS_LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????200???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getUpdateFrequency()) && sourceInfoDeriveTableColumnDTO.getUpdateFrequency().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getEtlPolicy()) && sourceInfoDeriveTableColumnDTO.getEtlPolicy().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ETL??????????????????128???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getIncreStandard()) && sourceInfoDeriveTableColumnDTO.getIncreStandard().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????128???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getIncrementalField()) && sourceInfoDeriveTableColumnDTO.getIncrementalField().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getCleanRule()) && sourceInfoDeriveTableColumnDTO.getCleanRule().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getFilter()) && sourceInfoDeriveTableColumnDTO.getFilter().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getProcedure()) && sourceInfoDeriveTableColumnDTO.getProcedure().length() > CommonConstant.LENGTH) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????/?????????????????????128???");
        }
        if (StringUtils.isNotBlank(sourceInfoDeriveTableColumnDTO.getRemark()) && sourceInfoDeriveTableColumnDTO.getRemark().length() > 512) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????512???");
        }
        fileCheckColumnLength(sourceInfoDeriveTableColumnDTO.getSourceInfoDeriveColumnInfos());
    }

    /**
     * ?????????????????????
     *
     * @param sourceInfoDeriveColumnInfos ????????????????????????
     */
    private void fileCheckColumnLength(List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        for (SourceInfoDeriveColumnInfo sourceInfoDeriveColumnInfo : sourceInfoDeriveColumnInfos) {
            if (sourceInfoDeriveColumnInfo.getColumnNameEn().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????128???");
            }
            if (sourceInfoDeriveColumnInfo.getColumnNameZh().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????128???");
            }
            if (sourceInfoDeriveColumnInfo.getDataType().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????128???");
            }

            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSourceDbName()) && sourceInfoDeriveColumnInfo.getSourceDbName().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????/???????????????????????????128???");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSourceTableNameEn()) && sourceInfoDeriveColumnInfo.getSourceTableNameEn().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????128???");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSourceColumnNameEn()) && sourceInfoDeriveColumnInfo.getSourceColumnNameEn().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????128???");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getMappingRule()) && sourceInfoDeriveColumnInfo.getMappingRule().length() > CommonConstant.LENGTH_3000) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????3000???");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getMappingDescribe()) && sourceInfoDeriveColumnInfo.getMappingDescribe().length() > CommonConstant.LENGTH_3000) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????3000???");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getSecretPeriod()) && sourceInfoDeriveColumnInfo.getSecretPeriod().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????128???");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getDesensitizationRules()) && sourceInfoDeriveColumnInfo.getDesensitizationRules().length() > CommonConstant.LENGTH) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????????????????128???");
            }
            if (StringUtils.isNotBlank(sourceInfoDeriveColumnInfo.getRemark()) && sourceInfoDeriveColumnInfo.getRemark().length() > 512) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????512???");
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
                sourceInfoDeriveColumnInfo.setPrimaryKey("???".equals(list.get(i)[10]));
                sourceInfoDeriveColumnInfo.setMappingRule(list.get(i)[11]);
                sourceInfoDeriveColumnInfo.setMappingDescribe(list.get(i)[12]);
                sourceInfoDeriveColumnInfo.setSecretName(list.get(i)[13]);
                sourceInfoDeriveColumnInfo.setSecret("???".equals(list.get(i)[13]));
                sourceInfoDeriveColumnInfo.setSecretPeriod(list.get(i)[14]);
                sourceInfoDeriveColumnInfo.setImportantName(list.get(i)[15]);
                sourceInfoDeriveColumnInfo.setImportant("???".equals(list.get(i)[15]));
                sourceInfoDeriveColumnInfo.setDesensitizationRules(list.get(i)[16]);
                sourceInfoDeriveColumnInfo.setTagsName(list.get(i)[17]);
                sourceInfoDeriveColumnInfo.setPermissionField("???".equals(list.get(i)[18]));
                sourceInfoDeriveColumnInfo.setRemark(list.get(i)[19]);
                sourceInfoDeriveColumnInfoList.add(sourceInfoDeriveColumnInfo);
            }
            sourceInfoDeriveTableColumnDto.setSourceInfoDeriveColumnInfos(sourceInfoDeriveColumnInfoList);
        } catch (Exception e) {
            LOG.error("getDeriveDataFile exception is {0}", e);
        }
        return sourceInfoDeriveTableColumnDto;
    }

    public void exportById(HttpServletResponse response, String tenantId, String tableId) {
        SourceInfoDeriveTableColumnVO deriveTableColumnDetail = getDeriveTableColumnDetail(tenantId, tableId);
        if (deriveTableColumnDetail == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        }
        List<SourceInfoDeriveColumnDTO> list = DeriveTableExportUtil.getPojo(deriveTableColumnDetail.getSourceInfoDeriveColumnVOS());
        String templateName = DeriveTableExportUtil.deriveTableTemplate();
        String exportTableName = DeriveTableExportUtil.deriveTableExcelPathName(deriveTableColumnDetail.getTableNameZh());

        ExcelWriter excelWriter = EasyExcelFactory.write(exportTableName).withTemplate(templateName).build();
        WriteSheet writeSheet = EasyExcelFactory.writerSheet().build();
        excelWriter.fill(deriveTableColumnDetail, writeSheet);
        excelWriter.fill(list, writeSheet);
        excelWriter.finish();
        String fileName = null;
        try {
            fileName = java.net.URLEncoder.encode(DeriveTableExportUtil.
                    deriveTableExcelName(deriveTableColumnDetail.getTableNameZh()), CommonConstant.CHARACTER_CODE_UTF);
        } catch (UnsupportedEncodingException e) {
            LOG.error("url encode failed", e);
        }
        File file = DeriveTableExportUtil.deriveTableExport(exportTableName);
        try (InputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            response.setCharacterEncoding(CommonConstant.CHARACTER_CODE_UTF);
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException | InvalidFormatException e) {
            LOG.error("write workbook failed", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????");
        } finally {
            if (file.exists() && !file.delete()) {
                LOG.error("??????????????????" + exportTableName + "???????????????");
            }
        }
    }

    public void downloadTemplate(HttpServletResponse response) {
        File file = DeriveTableExportUtil.deriveTableImportTemplate();
        try (InputStream input = new FileInputStream(file); Workbook workbook = WorkbookFactory.create(input)) {
            String fileName = DeriveTableExportUtil.getDeriveImportTemplate();
            fileName = java.net.URLEncoder.encode(fileName, CommonConstant.CHARACTER_CODE_UTF);
            response.setCharacterEncoding(CommonConstant.CHARACTER_CODE_UTF);
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException | InvalidFormatException e) {
            LOG.error("downloadTemplate failed", e);
            throw new AtlasBaseException("????????????");
        }
    }
}
