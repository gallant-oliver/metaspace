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

package org.apache.atlas.web.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.gridsum.datahub.auth.service.MetadataService;
import com.gridsum.datahub.jdbc.service.policy.SqlSessionFactory;
import com.gridsum.datahub.task.service.*;
import com.gridsum.datahub.task.util.ContentUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Service;

import com.gridsum.datahub.auth.constant.AuthDataManager;
import com.gridsum.datahub.auth.dock.OlapSchemaDock;
import com.gridsum.datahub.auth.service.PermissionService;
import com.gridsum.datahub.jdbc.data.JdbcDataConstants;
import com.gridsum.datahub.jdbc.data.UserConfigService;
import com.gridsum.datahub.jdbc.service.OlapSchemaService;
import com.gridsum.datahub.jdbc.service.SchemaService;
import com.gridsum.datahub.module.ObjectMapperFactory;
import com.gridsum.datahub.module.auth.AuthUser;
import com.gridsum.datahub.module.auth.GrantDatabase;
import com.gridsum.datahub.module.auth.Permission;
import com.gridsum.datahub.module.exception.DataPlatformException;
import com.gridsum.datahub.module.exception.not.NotBlankException;
import com.gridsum.datahub.module.exception.permission.InsufficientPermissionException;
import com.gridsum.datahub.module.exception.resource.ResourceExistsException;
import com.gridsum.datahub.module.exception.resource.ResourceNotExistsException;
import com.gridsum.datahub.module.history.CacheProgressInfo;
import com.gridsum.datahub.module.history.HistoryInfo;
import com.gridsum.datahub.module.history.QueryType;
import com.gridsum.datahub.module.job.EventJobInfo;
import com.gridsum.datahub.module.job.ExportJobInfo;
import com.gridsum.datahub.module.job.PersistenceJobInfo;
import com.gridsum.datahub.module.job.ScheduleJobInfo;
import com.gridsum.datahub.module.job.UploadJobInfo;
import com.gridsum.datahub.module.request.task.OfflineQueryTaskRequestBody;
import com.gridsum.datahub.module.request.upload.UploadConfig;
import com.gridsum.datahub.module.task.ActionType;
import com.gridsum.datahub.module.task.FileType;
import com.gridsum.datahub.module.task.TaskInfo;
import com.gridsum.datahub.module.task.TaskType;
import com.gridsum.datahub.module.task.TaskUpdateMode;
import com.gridsum.datahub.module.tuple.Triple;
import com.gridsum.datahub.platform.service.JobService;
import com.gridsum.datahub.task.ManagedTaskCreator;
import com.gridsum.datahub.task.config.TaskConfig;
import com.gridsum.datahub.task.job.event.EventJob;
import com.gridsum.datahub.task.job.export.ExportCacheJob;
import com.gridsum.datahub.task.job.export.ExportJob;
import com.gridsum.datahub.task.job.export.ExportOlapJob;
import com.gridsum.datahub.task.job.export.ExportSqlJob;
import com.gridsum.datahub.task.job.persistence.PersistenceJob;
import com.gridsum.datahub.task.job.query.ProgressCache;
import com.gridsum.datahub.task.job.schedule.ScheduleJob;
import com.gridsum.datahub.task.job.upload.UploadJob;
import com.gridsum.datahub.task.service.EventJobService;
import com.gridsum.datahub.task.service.ExportJobService;
import com.gridsum.datahub.task.service.PersistenceJobService;
import com.gridsum.datahub.task.service.ScheduleJobService;
import com.gridsum.datahub.task.service.TaskService;
import com.gridsum.datahub.task.service.UploadJobService;
import com.gridsum.gdp.library.commons.data.schema.Database;
import com.gridsum.gdp.library.commons.data.schema.Table;
import com.gridsum.gdp.library.commons.data.type.DatabaseEngine;
import com.gridsum.gdp.library.commons.utils.StringUtils;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import com.gridsum.olap.engine.CubeManager;
import com.gridsum.olap.engine.RequestBuilder;
import com.gridsum.olap.engine.metadata.cube.Cube;
import com.gridsum.olap.engine.query.OlapQuery;

/**
 * 作业服务实现
 *
 */
public class JobService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);


    private UploadJobService uploadJobService;

    private String generateTaskId(String jobId, boolean isCache) {
        if (!isCache) {
            return UUIDUtils.uuid();
        } else {
            TaskInfo taskInfo = taskService.getByJobId(jobId);
            if (taskInfo != null && !StringUtils.isNULL(taskInfo.getTaskId())) {
                return taskInfo.getTaskId();
            } else {
                return UUIDUtils.uuid();
            }
        }
    }


    public UploadJobInfo newUploadJobInfo(String jobId, UploadConfig uploadConfig) {
        // 从提交的请求中得到任务配置，目标表schema，以及column的mapping关系
        UploadJobInfo uploadJobInfo = toUploadJobInfo(jobId, uploadConfig);
        uploadJobInfo.setJobId(jobId);
        uploadJobInfo.setUid(AuthDataManager.getCurrentUserId());
        if (Strings.isNullOrEmpty(uploadJobInfo.getDatabaseId())) {
            this.schemaService.createUserSchema(AuthDataManager.getCurrentUser().getUsername());
        }
        LOGGER.info("CheckTable: " + "database: " + uploadJobInfo.getDatabase() + ", table: " + uploadJobInfo.getTableName() + ", action = " + uploadJobInfo.getActionType());

        checkTable(uploadJobInfo.getDatabase(), uploadJobInfo.getTableName(), uploadJobInfo.getActionType());

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setJobId(jobId);
        // new task uuid
        taskInfo.setTaskId(UUIDUtils.alphaUUID());
        taskInfo.setState(Service.State.NEW);
        taskInfo.setTaskType(TaskType.UPLOAD);
        taskInfo.setProgress(0F);
        taskInfo.setUid(AuthDataManager.getCurrentUser().getUid());

        uploadJobInfo.setTaskInfo(taskInfo);

        // 数据库中添加数据上传记录
        this.uploadJobService.createUploadJob(uploadJobInfo);

        // 创建UploadJob
        UploadJob uploadJob = this.creator.createUploadJob();
        uploadJob.init(uploadJobInfo);
        try {
            uploadJob.startAsync();
        } catch (Throwable t) {
            LOGGER.error("uploadJob failed ", t);
            throw new DataPlatformException(uploadJob.failureCause());
        }
        return uploadJobInfo;
    }

    /**
     * 从提交的请求中得到任务配置，目标表schema，以及column的mapping关系
     *
     * @param jobId
     * @param uploadConfig
     * @return
     */
    private UploadJobInfo toUploadJobInfo(String jobId, UploadConfig uploadConfig) {
        UploadJobInfo uploadJobInfo = new UploadJobInfo();
        // 设置默认数据库（个人数据库）
        if (Strings.isNullOrEmpty(uploadConfig.getDatabase())) {
            uploadConfig.setDatabase(this.userConfigService.getUserDatabase(AuthDataManager.getCurrentUser().getUsername()));
        }
        // 设置默认数据库ID
        if (Strings.isNullOrEmpty(uploadConfig.getDatabaseId())) {
            Database database = this.schemaService.getDatabase(JdbcDataConstants.DEFAULT_DATABASE_ENGINE, uploadConfig.getDatabase());
            uploadJobInfo.setDatabaseId(database.getId());
        } else {
            uploadJobInfo.setDatabaseId(uploadConfig.getDatabaseId());
        }

        uploadJobInfo.setDatabaseId(uploadConfig.getDatabaseId());
        uploadJobInfo.setDatabase(uploadConfig.getDatabase());
        uploadJobInfo.setFieldDelimiter(uploadConfig.getFieldDelimiter());
        uploadJobInfo.setFileEncode(uploadConfig.getFileEncode());
        uploadJobInfo.setTableName(uploadConfig.getTableName());
        // 原始数据文件临时路径
        String filePath = this.uploadJobService.getPath(jobId);
        uploadJobInfo.setFilePath(filePath);
        uploadJobInfo.setFileEncode(uploadConfig.getFileEncode());
        uploadJobInfo.setIncludeHeaderLine(uploadConfig.isIncludeHeaders());
        uploadJobInfo.setFieldDelimiter(uploadConfig.getFieldDelimiter());
        uploadJobInfo.setFieldDescribe(this.uploadJobService.getAvroSchemaJson(uploadConfig));
        uploadJobInfo.setFileType(uploadConfig.getFileType());
        uploadJobInfo.setSheetName(uploadConfig.getSheetName());
        uploadJobInfo.setActionType(uploadConfig.getActionType());
        uploadJobInfo.setColumns(uploadConfig.getColumns());
        return uploadJobInfo;
    }

    /**
     * 校验是否有数据库完全控制权限
     *
     * @param databaseId
     * @param databaseName
     * @return targetDatabase
     */
    private Database checkAndGetSchema(String databaseId, String databaseName) {
        GrantDatabase grantDatabase = new GrantDatabase();
        grantDatabase.setDatabaseId(databaseId);
        List<GrantDatabase> grantDatabaseList = Collections.singletonList(grantDatabase);
        // PermissionPlatform返回的权限信息不正确
//        // 权限校验
//        boolean hasPermission = permissionService.hasPermissionByUserInfo(AuthDataManager.getCurrentUser(), grantDatabaseList, Permission.CONTROL);
//        if (!hasPermission) {
//            throw InsufficientPermissionException.needControl(Permission.CONTROL);
//        }
        //获取数据库名
        List<Database> allDB = schemaService.findDatabases(JdbcDataConstants.DEFAULT_DATABASE_ENGINE);
        Map<String, Database> srcDatabaseMap = Maps.uniqueIndex(allDB, new Function<Database, String>() {
            @Nullable
            @Override
            public String apply(Database input) {
                return input.getId();
            }
        });
        Database targetDatabase = srcDatabaseMap.get(databaseId);
        if (targetDatabase == null) {
            throw ResourceNotExistsException.notExists("Schema", databaseName);
        }
        return targetDatabase;
    }

    /**
     * 校验此表是否已经存在
     *
     * @param databaseName
     * @param tableName
     */
    private void checkTable(String databaseName, String tableName) {
//        boolean existsTable = schemaService.tableExists(JdbcDataConstants.DEFAULT_DATABASE_ENGINE, databaseName, tableName);
        boolean existsTable = metadataService.existsTable(databaseName, tableName);
        if (existsTable) {
            throw ResourceExistsException.exists("Table", tableName);
        }
    }

    /**
     * 校验此表是否已经存在</p>
     * INSERT，表存在，抛出异常</p>
     * APPEND|OVERWRITE，表不存在，抛出异常</p>
     *
     * @param databaseName
     * @param tableName
     */
    private void checkTable(String databaseName, String tableName, ActionType actionType) {
//        boolean existsTable = schemaService.tableExists(JdbcDataConstants.DEFAULT_DATABASE_ENGINE, databaseName, tableName);
        boolean existsTable = metadataService.existsTable(databaseName, tableName);
        if (existsTable && ActionType.INSERT.equals(actionType)) {
            throw ResourceExistsException.exists("Table", tableName);
        }
        if (!existsTable && !ActionType.INSERT.equals(actionType)) {
            throw ResourceNotExistsException.notExists("Table", tableName);
        }
    }
}
