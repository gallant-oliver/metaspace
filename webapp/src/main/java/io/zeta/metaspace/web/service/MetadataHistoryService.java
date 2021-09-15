// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/9/19 11:18
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.metadata.ColumnMetadata;
import io.zeta.metaspace.model.metadata.TableMetadata;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.MetadataHistoryDAO;
import io.zeta.metaspace.web.dao.SourceInfoDeriveTableInfoDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDatabaseService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDeriveTableInfoService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoFileService;
import io.zeta.metaspace.web.util.Base64Utils;
import io.zeta.metaspace.web.util.EntityUtil;
import io.zeta.metaspace.web.util.NoticeCenterUtil;
import io.zeta.metaspace.web.util.ParamUtil;
import io.zeta.metaspace.web.util.office.word.WordExport;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStoreV2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/19 11:18
 */

@Service
@Slf4j
public class MetadataHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataHistoryService.class);

    @Autowired
    private MetadataHistoryDAO metadataDAO;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private AtlasEntityStoreV2 entityStore;
    private String partitionAttribute = "partitionKeys";
    @Autowired
    private SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDao;
    @Autowired
    private BusinessDAO businessDAO;
    @Autowired
    DatabaseInfoDAO databaseInfoDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private SourceInfoDeriveTableInfoService sourceInfoDeriveTableInfoService;
    @Autowired
    private SourceInfoFileService sourceInfoFileService;
    @Autowired
    private SourceInfoDatabaseService sourceInfoDatabaseService;
    @Autowired
    private MetadataHistoryDAO metadataHistoryDAO;


    public Set<String> getTableGuid(List<AtlasEntity> entities) {
        Set<String> tableSet = new HashSet<>();
        Boolean hiveAtlasEntityAll = dataManageService.getHiveAtlasEntityAll(entities);
        for (AtlasEntity entity : entities) {
            if (dataManageService.getOutputFromProcesses(entity) && hiveAtlasEntityAll) {
                continue;
            }
            String typeName = entity.getTypeName();
            if("hive_table".equals(typeName)) {
                tableSet.add(entity.getGuid());
            } else if("hive_column".equals(typeName) || "hive_storagedesc".equals(typeName)) {
                AtlasRelatedObjectId table = (AtlasRelatedObjectId)entity.getRelationshipAttribute("table");
                if(null != table) {
                    tableSet.add(table.getGuid());
                }
            }
        }
        log.debug("tableSet is {}", tableSet);
        return tableSet;
    }

    @Transactional(rollbackFor=Exception.class)
    public void storeHistoryMetadata(List<AtlasEntity> entities) throws AtlasBaseException {
        try {
            Set<String> tableSet = getTableGuid(entities);
            for(String tableGuid : tableSet) {
                AtlasEntity.AtlasEntityWithExtInfo info = entityStore.getById(tableGuid);
                if (null != info) {
                    AtlasEntity entity = info.getEntity();
                    List<String> partitionKeyList = EntityUtil.extractPartitionKeyInfo(entity);
                    TableMetadata tableMetadata = generateTableMetadata(entity);
                    log.info("storeHistoryMetadata AtlasEntity is {},name is {}", entity, tableMetadata.getName());
                    int sameCount = metadataDAO.getSameUpdateEntityCount(tableMetadata);
                    if(sameCount > 0) {
                        continue;
                    }
                    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
                    Map<String, AtlasEntity> referrencedEntities = info.getReferredEntities();
                    for (String guid : referrencedEntities.keySet()) {
                        AtlasEntity referrencedEntity = referrencedEntities.get(guid);
                        String typeName = referrencedEntity.getTypeName();
                        if ("hive_column".equals(typeName) && AtlasEntity.Status.ACTIVE == referrencedEntity.getStatus()) {
                            ColumnMetadata columnMetadata = generateColumnMetadata(tableGuid, referrencedEntity, partitionKeyList);
                            columnMetadataList.add(columnMetadata);
                        } else if ("hive_storagedesc".equals(typeName)) {
                            String location = getEntityAttribute(referrencedEntity, "location");
                            tableMetadata.setStoreLocation(location);
                            //格式
                            String inputFormat = getEntityAttribute(referrencedEntity, "inputFormat");
                            if (Objects.nonNull(inputFormat)) {
                                String[] fullFormat = inputFormat.split("\\.");
                                tableMetadata.setTableFormat(fullFormat[fullFormat.length - 1]);
                            }
                        }
                    }
                    if (this.getTableCompareResult(tableGuid, tableMetadata) && this.getColumnCompareResult(tableGuid, columnMetadataList)) {
                        continue;
                    }
                    metadataDAO.addTableMetadata(tableMetadata);
                    int version = metadataDAO.getTableVersion(tableGuid);
                    columnMetadataList.forEach(columnMetadata -> columnMetadata.setVersion(version));
                    if (columnMetadataList.size() > 0) {
                        metadataDAO.addColumnMetadata(columnMetadataList);
                    }

                    //查询表的元数据历史，若只有一条则代表第一次添加不需要发送邮件处理，否则需要发送邮件以及对应的字段对比信息（最新的两次数据对比结果）
                    List<TableMetadata> tableMetadataList = metadataDAO.getTableMetadataByGuid(tableGuid);
                    if( CollectionUtils.isEmpty(tableMetadataList) || tableMetadataList.size() == 1 ){
                        log.info("storeHistoryMetadata AtlasEntity name is {},首次添加，不需要邮件通知。", tableMetadata.getName());
                        continue;
                    }
                    sendNoticeByEmail(tableMetadataList);
                }
            }
        } catch (Exception e) {
            log.error("storeHistoryMetadata exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    //元数据有变化，需要邮件通知 （开启一个线程处理,目前只有 hive源存在对比）
    private void sendNoticeByEmail(List<TableMetadata> tableMetadataList) {
        log.info("元数据有变化，查找邮件发送地址.");
        String tableGuid = tableMetadataList.get(0).getGuid();
        String sourceId = "hive";
        Map<String,Object> paragraphMap = new HashMap<>();
        paragraphMap.put("dbType",sourceId);
        paragraphMap.put("dbName",sourceId);
        List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDao.getDeriveTableByGuid(sourceId,tableGuid);
        String[] contacts = null;
        if(!CollectionUtils.isEmpty(deriveTableInfoList)){
            Optional<SourceInfoDeriveTableInfo> deriveTableInfoOpt = deriveTableInfoList.stream().sorted(Comparator.comparing(SourceInfoDeriveTableInfo::getVersion).reversed()).findFirst();
            if(deriveTableInfoOpt.isPresent()) {
                SourceInfoDeriveTableInfo tableInfo = deriveTableInfoOpt.get();
                paragraphMap.put("deriveTableDesigner",tableInfo.getCreatorName());
                BusinessInfo businessInfo = businessDAO.queryBusinessByBusinessId(tableInfo.getBusinessId());
                if(StringUtils.isNotBlank(tableInfo.getCreator())){
                    //获取衍生表设计人的邮箱地址
                    User user = userDAO.getUser(tableInfo.getCreator());
                    String email = user.getAccount();
                    log.info("衍生表设计人的邮箱地址:{}",email);
                    if(sourceInfoFileService.isEmail(email)){
                        contacts = new String[]{email};
                    }
                }
                if (null != businessInfo) {
                    // 获取该租户下所有的业务目录guid - path
                    int BUSINESS_CATEGORY_TYPE = 1;
                    Map<String, String> businessCategoryGuidPathMap = sourceInfoDeriveTableInfoService.getCategoryGuidPathMap(tableInfo.getTenantId(), BUSINESS_CATEGORY_TYPE, businessInfo.getDepartmentId());
                    paragraphMap.put("businessPath",businessCategoryGuidPathMap.getOrDefault(businessInfo.getDepartmentId(), ""));
                }

                List<DatabaseInfoBO> currentSourceInfoList = databaseInfoDAO.getLastDatabaseInfoByDatabaseId(tableInfo.getDbId(),tableInfo.getTenantId(),sourceId);
                if(!CollectionUtils.isEmpty(currentSourceInfoList)){
                    Optional<DatabaseInfoBO> databaseInfoOpt =  currentSourceInfoList.stream().sorted(Comparator.comparing(DatabaseInfoBO::getVersion).reversed()).findFirst();
                    if(databaseInfoOpt.isPresent()) {
                        DatabaseInfoBO databaseInfoBO = databaseInfoOpt.get();
                        if (Boolean.FALSE.equals(ParamUtil.isNull(databaseInfoBO))&&Boolean.TRUE.equals(ParamUtil.isNull(databaseInfoBO.getCategoryId()))){
                            databaseInfoBO.setCategoryId(databaseInfoDAO.getParentCategoryIdById(databaseInfoBO.getId()));
                        }
                        paragraphMap.put("bizLeader",databaseInfoBO.getBusinessLeaderName());
                        paragraphMap.put("techenicalPath",databaseInfoBO.getStatus().equals(Status.ACTIVE.getIntValue()+"")?
                                sourceInfoDatabaseService.getActiveInfoAllPath(databaseInfoBO.getCategoryId(),tableInfo.getTenantId() ):sourceInfoDatabaseService.getAllPath(databaseInfoBO.getId(),tableInfo.getTenantId()));

                        if(contacts == null){
                            User user = userDAO.getUser(databaseInfoBO.getBusinessLeaderId());
                            String email = user.getAccount();
                            log.info("数据库业务负责人的邮箱地址:{}",email);
                            if(sourceInfoFileService.isEmail(email)){
                                contacts = new String[]{email};
                            }
                        }
                    }
                }
            }
        }

        if(contacts == null){
            log.info("要发送的邮件地址为空。");
            return;
        }

        //组装列的变更信息
        List<Map<String,String>> excelMapList = new ArrayList<>();
        TableMetadata newTableMetaData = tableMetadataList.get(0);
        TableMetadata oldTableMetaData = tableMetadataList.get(1);
        List<ColumnMetadata> currentMetadata = metadataHistoryDAO.getColumnMetadata(tableGuid,newTableMetaData.getVersion());
        List<ColumnMetadata> oldMetadata = metadataHistoryDAO.getColumnMetadata(tableGuid, oldTableMetaData.getVersion());
        if(CollectionUtils.isNotEmpty(oldMetadata) && CollectionUtils.isNotEmpty(currentMetadata)){
            for (ColumnMetadata item : oldMetadata){
                Map<String,String> map = new HashMap<>();
                Optional<ColumnMetadata> filterDataOpt = currentMetadata.stream().filter(p->item.getName().equalsIgnoreCase(p.getName())).findFirst();
                if(!filterDataOpt.isPresent()){
                    map.put("changeColumn",item.getName());
                    map.put("changeType","列删除");
                    excelMapList.add(map);
                    continue;
                }
                if(!StringUtils.equalsIgnoreCase(item.getType(),filterDataOpt.get().getType())){//比较类型
                    map.put("changeColumn",item.getName());
                    map.put("changeType","列类型变更");
                    map.put("beforeInfo",item.getType());
                    map.put("afterInfo",filterDataOpt.get().getType());
                    excelMapList.add(map);
                    continue;
                }
            }

            //查看是否存在添加的列
            List<String> oldColumns = oldMetadata.stream().map(ColumnMetadata::getName).collect(Collectors.toList());
            List<ColumnMetadata> addColumnList =  currentMetadata.stream().filter(out->!oldColumns.contains(out.getName())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(addColumnList)){
                for (ColumnMetadata item : addColumnList){
                    Map<String,String> map = new HashMap<>();
                    map.put("changeColumn",item.getName());
                    map.put("changeType","列添加");
                    excelMapList.add(map);
                }
            }
        }

        File file =  null;
        try {
            file = File.createTempFile("tmp_"+System.currentTimeMillis(),".docx");
            String targetPath = file.getAbsolutePath();
            WordExport.fillDataTemplate(paragraphMap,excelMapList,
                    MetadataHistoryService.class.getResource("/tpl/table_change_template.docx").getPath(),targetPath);
            log.info("模板生成完毕..");
            String attachmentBase64content= Base64Utils.fileToBase64(targetPath), fileName="元数据版本差异清单.docx", content="元数据有变化，请查看附件检查详情信息";
            log.info("转换base64 sucess.");
            NoticeCenterUtil.sendEmail(attachmentBase64content, fileName, content,contacts);
            log.info("邮件发送 sucess.");
        } catch (IOException e) {
            log.error("生成模板文件出错:{}",e);
        }finally {
            if(file != null && file.exists()){
                file.delete();
            }
        }

    }

    /**
     * 校验字段和上一版本是否完全一致
     * @param tableGuid
     * @param columnMetadataList
     * @return
     */
    private Boolean getColumnCompareResult(String tableGuid, List<ColumnMetadata> columnMetadataList) {
        List<ColumnMetadata> columnMetadataListHistory = metadataDAO.getLastColumnMetadata(tableGuid);
        if (CollectionUtils.isEmpty(columnMetadataListHistory)) {
            return false;
        }
        if (columnMetadataList.size() != columnMetadataListHistory.size()) {
            return false;
        }
        int i = 0;
        for (ColumnMetadata columnMetadata : columnMetadataListHistory) {
            for (ColumnMetadata metadata : columnMetadataList) {
                if (columnMetadata.compareColumn(metadata)) {
                    i++;
                }
            }
        }
        if (columnMetadataList.size() == i) {
            return true;
        }
        return false;
    }

    /**
     * 比较表内容是否一致
     * @param tableGuid
     * @param tableMetadata
     * @return
     */
    private Boolean getTableCompareResult(String tableGuid, TableMetadata tableMetadata) {
        TableMetadata tableMetadataListHistory = metadataDAO.getLastTableMetadata(tableGuid);
        if (tableMetadataListHistory == null) {
            return false;
        }
        return tableMetadataListHistory.compareTable(tableMetadata);
    }


    public TableMetadata generateTableMetadata(AtlasEntity entity) {
        String guid = entity.getGuid();
        String name = getEntityAttribute(entity, "name");
        AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
        String dbName = relatedObject.getDisplayText();
        String creator = entity.getCreatedBy();
        String updater = entity.getUpdatedBy();
        Timestamp createTime = new Timestamp(entity.getCreateTime().getTime());
        Timestamp updateTime = new Timestamp(entity.getUpdateTime().getTime());
        String tableType = getEntityAttribute(entity, "tableType");
        tableType = tableType.contains("EXTERNAL")?"EXTERNAL_TABLE":"INTERNAL_TABLE";
        Boolean isPartitionTable = extractPartitionInfo(entity);
        String tableFormat = "";
        String storeLocation = "";
        String description = getEntityAttribute(entity, "description");
        String status = entity.getStatus().name();

        TableMetadata metadata = new TableMetadata(guid,dbName,name,creator,updater, createTime, updateTime,tableType,isPartitionTable,tableFormat,storeLocation,description,status);



        return metadata;
    }

    public ColumnMetadata generateColumnMetadata(String tableGuid, AtlasEntity entity, List<String> partitionKeys) {
        String guid = entity.getGuid();
        String name = getEntityAttribute(entity, "name");
        String type = getEntityAttribute(entity, "type");
        String description = getEntityAttribute(entity, "commnet");
        Boolean isPartitionKey = partitionKeys.contains(guid)?true:false;
        String status = entity.getStatus().name();
        String creator = entity.getCreatedBy();
        String updater = entity.getUpdatedBy();
        Timestamp createTime = new Timestamp(entity.getCreateTime().getTime());
        Timestamp updateTime = new Timestamp(entity.getUpdateTime().getTime());

        ColumnMetadata metadata = new ColumnMetadata(guid, name, type, tableGuid, description, status, isPartitionKey, creator, updater, createTime, updateTime);
        return metadata;
    }

    public String getEntityAttribute(AtlasEntity entity, String attributeName) {
        if (entity.hasAttribute(attributeName) && Objects.nonNull(entity.getAttribute(attributeName))) {
            return entity.getAttribute(attributeName).toString();
        } else {
            return null;
        }
    }

    public boolean extractPartitionInfo(AtlasEntity entity) {
        if (entity.hasAttribute(partitionAttribute) && Objects.nonNull(entity.getAttribute(partitionAttribute))) {
            return true;
        } else {
            return false;
        }
    }

    public AtlasRelatedObjectId getRelatedDB(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        String db = "db";
        if (entity.hasRelationshipAttribute(db) && Objects.nonNull(entity.getRelationshipAttribute(db))) {
            Object obj = entity.getRelationshipAttribute(db);
            if (obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId) obj;
            }
        }
        return objectId;
    }
}
