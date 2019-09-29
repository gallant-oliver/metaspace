package io.zeta.metaspace.web.metadata;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhuxuetong
 * @date 2019-08-22 15:08
 */
public class BaseFields {

    public static final String ATTRIBUTE_QUALIFIED_NAME            = "qualifiedName";
    public static final String ATTRIBUTE_NAME                      = "name";
    public static final String ATTRIBUTE_DESCRIPTION               = "description";
    public static final String ATTRIBUTE_OWNER                     = "owner";
    public static final String ATTRIBUTE_CLUSTER_NAME              = "clusterName";
    public static final String ATTRIBUTE_COMMENT                   = "comment";
    public static final String ATTRIBUTE_CREATE_TIME               = "createTime";
    public static final String ATTRIBUTE_DB                        = "db";
    public static final String ATTRIBUTE_TABLE                        = "table";
    public static final String ATTRIBUTE_TABLE_TYPE = "tableType";
    public static final String ATTRIBUTE_RDBMS_TYPE = "rdbms_type";
    public static final String ATTRIBUTE_PLATFORM = "platform";
    public static final String ATTRIBUTE_HOSTNAME = "hostname";
    public static final String ATTRIBUTE_PORT = "port";
    public static final String ATTRIBUTE_PROTOCOL = "protocol";
    public static final String ATTRIBUTE_CONTACT_INFO = "contact_info";
    public static final String ATTRIBUTE_DATABASES = "databases";
    public static final String ATTRIBUTE_PRODOROTHER = "prodOrOther";
    public static final String ATTRIBUTE_INSTANCE = "instance";
    public static final String ATTRIBUTE_TABLES = "tables";
    public static final String ATTRIBUTE_NAME_PATH = "name_path";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_COLUMNS = "columns";
    public static final String ATTRIBUTE_INDEXES = "indexes";
    public static final String ATTRIBUTE_FOREIGN_KEYS = "foreign_keys";
    //column
    public static final String ATTRIBUTE_DATA_TYPE = "data_type";
    public static final String ATTRIBUTE_LENGTH = "length";
    public static final String ATTRIBUTE_DEFAULT_VALUE = "default_value";
    public static final String ATTRIBUTE_ISNULLABLE = "isNullable";
    public static final String ATTRIBUTE_ISPRIMARYKEY = "isPrimaryKey";
    //index
    public static final String ATTRIBUTE_INDEX_TYPE = "index_type";
    public static final String ATTRIBUTE_ISUNIQUE = "isUnique";
    //foreign_key
    public static final String ATTRIBUTE_KEY_COLUMNS = "key_columns";
    public static final String ATTRIBUTE_REFERENCES_TABLE = "references_table";
    public static final String ATTRIBUTE_REFERENCES_COLUMNS = "references_columns";

    public static final String RMDB_INSTANCE = "rdbms_instance";
    public static final String RMDB_DB = "rdbms_db";
    public static final String RMDB_TABLE = "rdbms_table";
    public static final String RDBMS_COLUMN = "rdbms_column";
    public static final String RDBMS_INDEX = "rdbms_index";
    public static final String RDBMS_FOREIGN_KEY = "rdbms_foreign_key";

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

    public static AtlasObjectId getObjectId(AtlasEntity entity) {
        String        qualifiedName = (String) entity.getAttribute(ATTRIBUTE_QUALIFIED_NAME);
        AtlasObjectId ret           = new AtlasObjectId(entity.getGuid(), entity.getTypeName(), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));

        return ret;
    }

}
