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

package org.zeta.metaspace.web.util;

import com.google.common.base.Joiner;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.table.Field;
import org.apache.atlas.model.table.StorageFormat;
import org.apache.atlas.model.table.TableForm;
import org.apache.atlas.model.table.TableType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.List;

public class TableSqlUtils {

    public static String format(TableForm tableForm) throws AtlasBaseException {
        try {
            String database = tableForm.getDatabase();
            String tableName = tableForm.getTableName();
            String comment = tableForm.getComment();
            String expireDate = tableForm.getExpireDate();
            boolean isPartition = tableForm.isPartition();
            List<Field> partitionFields = tableForm.getPartitionFields();
            String storedFormat = tableForm.getStoredFormat();
            String hdfsPath = tableForm.getHdfsPath();
            String fieldsTerminated = tableForm.getFieldsTerminated();
            String lineTerminated = tableForm.getLineTerminated();
            TableType tableTypeEnum = TableType.of(tableForm.getTableType());
            String fieldsLiteral = Joiner.on(",").join(tableForm.getFields());

            StringBuffer sqlFormat = new StringBuffer("CREATE %s TABLE %s.%s (%s)");
            if (StringUtils.isNotBlank(comment)) {
                sqlFormat.append(" COMMENT '" + comment + "'");
            }
            if (isPartition && partitionFields != null) {
                if (partitionFields == null || partitionFields.isEmpty()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "分区表没有分区字段");
                }
                String partitionFieldsLiteral = Joiner.on(",").join(partitionFields);
                sqlFormat.append(" PARTITIONED BY (" + partitionFieldsLiteral + ")");
            }


            if (StringUtils.isNotBlank(fieldsTerminated) || StringUtils.isNotBlank(lineTerminated)) {
                sqlFormat.append(" ROW FORMAT DELIMITED");
            }
            if (StringUtils.isNotBlank(fieldsTerminated)) {
                sqlFormat.append(" FIELDS TERMINATED BY '" + fieldsTerminated + "'");
            }
            if (StringUtils.isNotBlank(lineTerminated)) {
                sqlFormat.append(" LINES TERMINATED BY '" + lineTerminated + "'");
            }


            if (StringUtils.isNotBlank(storedFormat)) {
                storedFormat = storedFormat.toUpperCase();
                StorageFormat.of(storedFormat);
                sqlFormat.append(" STORED AS " + storedFormat);
            }

            if (tableTypeEnum.isExternal()) {
                sqlFormat.append(" LOCATION '" + hdfsPath + "'");
            }

            String sql = String.format(sqlFormat.toString(), tableTypeEnum.getLiteral(),
                                       database, tableName, fieldsLiteral);
            return sql;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, ExceptionUtils.getStackTrace(e));
        }

    }
}
