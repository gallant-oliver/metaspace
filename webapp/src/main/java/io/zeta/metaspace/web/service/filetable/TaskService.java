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

package io.zeta.metaspace.web.service.filetable;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.repository.util.HbaseUtils;
import io.zeta.metaspace.web.model.filetable.TaskInfo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import java.util.NavigableMap;

/**
 * 上传任务服务
 */
@AtlasService
public class TaskService {

    public static final String TASK_TABLE_NAME = "upload_task";


    public void updateTaskProgress(String taskId, Float progress) {
        try (Connection conn = HbaseUtils.getConn();
             Table table = conn.getTable(TableName.valueOf(TASK_TABLE_NAME));) {
            Put put = new Put(taskId.getBytes());
            put.addColumn("info".getBytes(), "progress".getBytes(), String.valueOf(progress).getBytes());
            table.put(put);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateState(String taskId, String state) {
        try (Connection conn = HbaseUtils.getConn();
             Table table = conn.getTable(TableName.valueOf(TASK_TABLE_NAME));) {
            Put put = new Put(taskId.getBytes());
            put.addColumn("info".getBytes(), "state".getBytes(), state.getBytes());
            table.put(put);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public TaskInfo getByTaskId(String taskId) {
        try (Connection conn = HbaseUtils.getConn();
             Table table = conn.getTable(TableName.valueOf(TASK_TABLE_NAME));) {
            Get get = new Get(taskId.getBytes());
            Result result = table.get(get);
            NavigableMap<byte[], byte[]> row = result.getFamilyMap("info".getBytes());
            if (row == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "taskId " + taskId + " 不存在");
            }
            String jobId = new String(row.get("jobId".getBytes()));
            String state = new String(row.get("state".getBytes()));
            float progress = Float.valueOf(new String(row.get("progress".getBytes())));
            TaskInfo ret = new TaskInfo(taskId, jobId, "", state, progress);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createUploadTask(TaskInfo taskInfo) {
        try (Connection conn = HbaseUtils.getConn();
             Table table = conn.getTable(TableName.valueOf(TASK_TABLE_NAME));) {
            Put put = new Put(taskInfo.getTaskId().getBytes());
            put.addColumn("info".getBytes(), "jobId".getBytes(), taskInfo.getJobId().getBytes());
            put.addColumn("info".getBytes(), "taskId".getBytes(), taskInfo.getTaskId().getBytes());
            put.addColumn("info".getBytes(), "progress".getBytes(), String.valueOf(taskInfo.getProgress()).getBytes());
            put.addColumn("info".getBytes(), "state".getBytes(), taskInfo.getState().getBytes());
            put.addColumn("info".getBytes(), "uid".getBytes(), taskInfo.getUid().getBytes());
            table.put(put);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
