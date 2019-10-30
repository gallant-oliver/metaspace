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

package io.zeta.metaspace.web.util;

import com.google.common.base.Joiner;
import org.apache.atlas.Atlas;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.table.Field;
import io.zeta.metaspace.model.table.StorageFormat;
import io.zeta.metaspace.model.table.TableForm;
import io.zeta.metaspace.model.table.TableType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.*;

public class TableSqlUtils {

    public static String format(TableForm tableForm) throws AtlasBaseException {
        try {
            keyWordIdentify(tableForm);
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
            //\n在java中会被当做空格，使用isNotBlank会返回false
            if (StringUtils.isNotEmpty(lineTerminated)) {
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
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, ExceptionUtils.getStackTrace(e));
        }
    }

    public static void keyWordIdentify(TableForm tableForm) throws AtlasBaseException {
        List<String> columnNameList = new ArrayList<>();
        tableForm.getFields().forEach(field -> columnNameList.add(field.getColumnName()));
        Set<String> keyWordSet = new HashSet<>();
        keyWordSet.addAll(Arrays.asList(keyWords));
        Set<String> hitSet = new HashSet<>();
        for (String columnName : columnNameList) {
            if(keyWordSet.contains(columnName.toUpperCase())) {
                hitSet.add(columnName);
            }
        }
        if(hitSet.size() > 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "字段集合中包含hive关键字:[" + Joiner.on(",").join(hitSet) + "]");
        }
    }

    private static String[] keyWords = {"ADD","ADMIN","AFTER","ALL","ALTER","ANALYZE","AND","ARCHIVE","ARRAY","AS","ASC","AUTHORIZATION","BEFORE","BETWEEN",
            "BIGINT","BINARY","BOOLEAN","BOTH","BUCKET","BUCKETS","BY","CASCADE","CASE","CAST","CHANGE","CHAR","CLUSTER",
            "CLUSTERED","CLUSTERSTATUS","COLLECTION","COLUMN","COLUMNS","COMMENT","COMPACT","COMPACTIONS","COMPUTE",
            "CONCATENATE","CONF","CONTINUE","CREATE","CROSS","CUBE","CURRENT","CURRENT_DATE","CURRENT_TIMESTAMP","CURSOR",
            "DATA","DATABASE","DATABASES","DATE","DATETIME","DAY","DBPROPERTIES","DECIMAL","DEFERRED","DEFINED","DELETE",
            "DELIMITED","DEPENDENCY","DESC","DESCRIBE","DIRECTORIES","DIRECTORY","DISABLE","DISTINCT","DISTRIBUTE",
            "DOUBLE","DROP","ELEM_TYPE","ELSE","ENABLE","END","ESCAPED","EXCHANGE","EXCLUSIVE","EXISTS","EXPLAIN","EXPORT",
            "EXTENDED","EXTERNAL","FALSE","FETCH","FIELDS","FILE","FILEFORMAT","FIRST","FLOAT","FOLLOWING","FOR","FORMAT",
            "FORMATTED","FROM","FULL","FUNCTION","FUNCTIONS","GRANT","GROUP","GROUPING","HAVING","HOLD_DDLTIME","HOUR",
            "IDXPROPERTIES","IF","IGNORE","IMPORT","IN","INDEX","INDEXES","INNER","INPATH","INPUTDRIVER","INPUTFORMAT",
            "INSERT","INT","INTERSECT","INTERVAL","INTO","IS","ITEMS","JAR","JOIN","KEYS","KEY_TYPE","LATERAL","LEFT","LESS",
            "LIKE","LIMIT","LINES","LOAD","LOCAL","LOCATION","LOCK","LOCKS","LOGICAL","LONG","MACRO","MAP","MAPJOIN",
            "MATERIALIZED","MINUS","MINUTE","MONTH","MORE","MSCK","NONE","NOSCAN","NOT","NO_DROP","NULL","OF","OFFLINE","ON",
            "OPTION","OR","ORDER","OUT","OUTER","OUTPUTDRIVER","OUTPUTFORMAT","OVER","OVERWRITE","OWNER","PARTIALSCAN",
            "PARTITION","PARTITIONED","PARTITIONS","PERCENT","PLUS","PRECEDING","PRESERVE","PRETTY","PRINCIPALS",
            "PROCEDURE","PROTECTION","PURGE","RANGE","READ","READONLY","READS","REBUILD","RECORDREADER","RECORDWRITER",
            "REDUCE","REGEXP","RELOAD","RENAME","REPAIR","REPLACE","RESTRICT","REVOKE","REWRITE","RIGHT","RLIKE","ROLE","ROLES",
            "ROLLUP","ROW","ROWS","SCHEMA","SCHEMAS","SECOND","SELECT","SEMI","SERDE","SERDEPROPERTIES","SERVER","SET","SETS",
            "SHARED","SHOW","SHOW_DATABASE","SKEWED","SMALLINT","SORT","SORTED","SSL","STATISTICS","STORED","STREAMTABLE",
            "STRING","STRUCT","TABLE","TABLES","TABLESAMPLE","TBLPROPERTIES","TEMPORARY","TERMINATED","THEN","TIMESTAMP",
            "TINYINT","TO","TOUCH","TRANSACTIONS","TRANSFORM","TRIGGER","TRUE","TRUNCATE","UNARCHIVE","UNBOUNDED","UNDO",
            "UNION","UNIONTYPE","UNIQUEJOIN","UNLOCK","UNSET","UNSIGNED","UPDATE","URI","USE","USER","USING","UTC",
            "UTCTIMESTAMP","VALUES","VALUE_TYPE","VARCHAR","VIEW","WHEN","WHERE","WHILE","WINDOW","WITH","YEAR"};
}
