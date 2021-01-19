/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.zeta.metaspace.web.util;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.notification.HookNotification;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public abstract class BaseHiveEvent {
    private static final Logger LOG = LoggerFactory.getLogger(BaseHiveEvent.class);

    public static final String HIVE_TYPE_DB             = "hive_db";
    public static final String HIVE_TYPE_TABLE          = "hive_table";
    public static final String HIVE_TYPE_STORAGEDESC    = "hive_storagedesc";
    public static final String HIVE_TYPE_COLUMN         = "hive_column";
    public static final String HIVE_TYPE_PROCESS        = "hive_process";
    public static final String HIVE_TYPE_COLUMN_LINEAGE = "hive_column_lineage";
    public static final String HIVE_TYPE_SERDE          = "hive_serde";
    public static final String HIVE_TYPE_ORDER          = "hive_order";
    public static final String HDFS_TYPE_PATH           = "hdfs_path";
    public static final String HBASE_TYPE_TABLE         = "hbase_table";
    public static final String HBASE_TYPE_NAMESPACE     = "hbase_namespace";
    public static final String AWS_S3_BUCKET            = "aws_s3_bucket";
    public static final String AWS_S3_PSEUDO_DIR        = "aws_s3_pseudo_dir";
    public static final String AWS_S3_OBJECT            = "aws_s3_object";

    public static final String SCHEME_SEPARATOR         = "://";
    public static final String S3_SCHEME                = "s3" + SCHEME_SEPARATOR;
    public static final String S3A_SCHEME               = "s3a" + SCHEME_SEPARATOR;

    public static final String ATTRIBUTE_QUALIFIED_NAME            = "qualifiedName";
    public static final String ATTRIBUTE_NAME                      = "name";
    public static final String ATTRIBUTE_DESCRIPTION               = "description";
    public static final String ATTRIBUTE_OWNER                     = "owner";
    public static final String ATTRIBUTE_CLUSTER_NAME              = "clusterName";
    public static final String ATTRIBUTE_LOCATION                  = "location";
    public static final String ATTRIBUTE_PARAMETERS                = "parameters";
    public static final String ATTRIBUTE_OWNER_TYPE                = "ownerType";
    public static final String ATTRIBUTE_COMMENT                   = "comment";
    public static final String ATTRIBUTE_CREATE_TIME               = "createTime";
    public static final String ATTRIBUTE_LAST_ACCESS_TIME          = "lastAccessTime";
    public static final String ATTRIBUTE_VIEW_ORIGINAL_TEXT        = "viewOriginalText";
    public static final String ATTRIBUTE_VIEW_EXPANDED_TEXT        = "viewExpandedText";
    public static final String ATTRIBUTE_TABLE_TYPE                = "tableType";
    public static final String ATTRIBUTE_TYPE                = "type";
    public static final String ATTRIBUTE_TEMPORARY                 = "temporary";
    public static final String ATTRIBUTE_RETENTION                 = "retention";
    public static final String ATTRIBUTE_DB                        = "db";
    public static final String ATTRIBUTE_STORAGEDESC               = "sd";
    public static final String ATTRIBUTE_PARTITION_KEYS            = "partitionKeys";
    public static final String ATTRIBUTE_COLUMNS                   = "columns";
    public static final String ATTRIBUTE_INPUT_FORMAT              = "inputFormat";
    public static final String ATTRIBUTE_OUTPUT_FORMAT             = "outputFormat";
    public static final String ATTRIBUTE_COMPRESSED                = "compressed";
    public static final String ATTRIBUTE_BUCKET_COLS               = "bucketCols";
    public static final String ATTRIBUTE_NUM_BUCKETS               = "numBuckets";
    public static final String ATTRIBUTE_STORED_AS_SUB_DIRECTORIES = "storedAsSubDirectories";
    public static final String ATTRIBUTE_TABLE                     = "table";
    public static final String ATTRIBUTE_SERDE_INFO                = "serdeInfo";
    public static final String ATTRIBUTE_SERIALIZATION_LIB         = "serializationLib";
    public static final String ATTRIBUTE_SORT_COLS                 = "sortCols";
    public static final String ATTRIBUTE_COL_TYPE                  = "type";
    public static final String ATTRIBUTE_COL_POSITION              = "position";
    public static final String ATTRIBUTE_PATH                      = "path";
    public static final String ATTRIBUTE_NAMESERVICE_ID            = "nameServiceId";
    public static final String ATTRIBUTE_INPUTS                    = "inputs";
    public static final String ATTRIBUTE_OUTPUTS                   = "outputs";
    public static final String ATTRIBUTE_OPERATION_TYPE            = "operationType";
    public static final String ATTRIBUTE_START_TIME                = "startTime";
    public static final String ATTRIBUTE_USER_NAME                 = "userName";
    public static final String ATTRIBUTE_QUERY_TEXT                = "queryText";
    public static final String ATTRIBUTE_QUERY_ID                  = "queryId";
    public static final String ATTRIBUTE_QUERY_PLAN                = "queryPlan";
    public static final String ATTRIBUTE_END_TIME                  = "endTime";
    public static final String ATTRIBUTE_RECENT_QUERIES            = "recentQueries";
    public static final String ATTRIBUTE_QUERY                     = "query";
    public static final String ATTRIBUTE_DEPENDENCY_TYPE           = "depenendencyType";
    public static final String ATTRIBUTE_EXPRESSION                = "expression";
    public static final String ATTRIBUTE_ALIASES                   = "aliases";
    public static final String ATTRIBUTE_URI                       = "uri";
    public static final String ATTRIBUTE_STORAGE_HANDLER           = "storage_handler";
    public static final String ATTRIBUTE_NAMESPACE                 = "namespace";
    public static final String ATTRIBUTE_OBJECT_PREFIX             = "objectPrefix";
    public static final String ATTRIBUTE_BUCKET                    = "bucket";

    public static final String HBASE_STORAGE_HANDLER_CLASS         = "org.apache.hadoop.hive.hbase.HBaseStorageHandler";
    public static final String HBASE_DEFAULT_NAMESPACE             = "default";
    public static final String HBASE_NAMESPACE_TABLE_DELIMITER     = ":";
    public static final String HBASE_PARAM_TABLE_NAME              = "hbase.table.name";
    public static final long   MILLIS_CONVERT_FACTOR               = 1000;

    public static final Map<Integer, String> OWNER_TYPE_TO_ENUM_VALUE = new HashMap<>();


    static {
        OWNER_TYPE_TO_ENUM_VALUE.put(1, "USER");
        OWNER_TYPE_TO_ENUM_VALUE.put(2, "ROLE");
        OWNER_TYPE_TO_ENUM_VALUE.put(3, "GROUP");
    }


    public List<HookNotification> getNotificationMessages() throws Exception {
        return null;
    }

    public static long getTableCreateTime(Table table) {
        return table.getTTable() != null ? (table.getTTable().getCreateTime() * MILLIS_CONVERT_FACTOR) : System.currentTimeMillis();
    }

    public static String getTableOwner(Table table) {
        return table.getTTable() != null ? (table.getOwner()): "";
    }

    public static AtlasObjectId getObjectId(AtlasEntity entity) {
        String        qualifiedName = (String) entity.getAttribute(ATTRIBUTE_QUALIFIED_NAME);
        AtlasObjectId ret           = new AtlasObjectId(entity.getGuid(), entity.getTypeName(), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));

        return ret;
    }

    public static List<AtlasObjectId> getObjectIds(List<AtlasEntity> entities) {
        final List<AtlasObjectId> ret;

        if (CollectionUtils.isNotEmpty(entities)) {
            ret = new ArrayList<>(entities.size());

            for (AtlasEntity entity : entities) {
                ret.add(getObjectId(entity));
            }
        } else {
            ret = Collections.emptyList();
        }

        return ret;
    }

}
