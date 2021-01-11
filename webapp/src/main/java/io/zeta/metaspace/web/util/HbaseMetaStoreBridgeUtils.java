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



import com.google.common.collect.Lists;
import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.web.metadata.MetaStoreBridgeUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.type.AtlasTypeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author lixiang03
 * @Data 2020/10/12 17:36
 */
@Singleton
@Component
public class HbaseMetaStoreBridgeUtils extends MetaStoreBridgeUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HbaseMetaStoreBridgeUtils.class);

    private static final String  HBASE_CLUSTER_NAME    = "atlas.cluster.name";
    private static final String  DEFAULT_CLUSTER_NAME  = "ms";
    private static final String  QUALIFIED_NAME        = "qualifiedName";
    private static final String  NAME                  = "name";
    private static final String  URI                   = "uri";
    private static final String  OWNER                 = "owner";
    private static final String  DESCRIPTION_ATTR      = "description";
    private static final String  CLUSTERNAME           = "clusterName";
    private static final String  NAMESPACE             = "namespace";
    private static final String  TABLE                 = "table";
    private static final String  COLUMN_FAMILIES       = "column_families";

    // table metadata
    private static final String ATTR_TABLE_MAX_FILESIZE              = "maxFileSize";
    private static final String ATTR_TABLE_ISREADONLY                = "isReadOnly";
    private static final String ATTR_TABLE_ISCOMPACTION_ENABLED      = "isCompactionEnabled";
    private static final String ATTR_TABLE_REPLICATION_PER_REGION    = "replicasPerRegion";
    private static final String ATTR_TABLE_DURABLILITY               = "durability";

    // column family metadata
    private static final String ATTR_CF_BLOOMFILTER_TYPE             = "bloomFilterType";
    private static final String ATTR_CF_COMPRESSION_TYPE             = "compressionType";
    private static final String ATTR_CF_COMPACTION_COMPRESSION_TYPE  = "compactionCompressionType";
    private static final String ATTR_CF_ENCRYPTION_TYPE              = "encryptionType";
    private static final String ATTR_CF_KEEP_DELETE_CELLS            = "keepDeletedCells";
    private static final String ATTR_CF_MAX_VERSIONS                 = "maxVersions";
    private static final String ATTR_CF_MIN_VERSIONS                 = "minVersions";
    private static final String ATTR_CF_DATA_BLOCK_ENCODING          = "dataBlockEncoding";
    private static final String ATTR_CF_TTL                          = "ttl";
    private static final String ATTR_CF_BLOCK_CACHE_ENABLED          = "blockCacheEnabled";
    private static final String ATTR_CF_CACHED_BLOOM_ON_WRITE        = "cacheBloomsOnWrite";
    private static final String ATTR_CF_CACHED_DATA_ON_WRITE         = "cacheDataOnWrite";
    private static final String ATTR_CF_CACHED_INDEXES_ON_WRITE      = "cacheIndexesOnWrite";
    private static final String ATTR_CF_EVICT_BLOCK_ONCLOSE          = "evictBlocksOnClose";
    private static final String ATTR_CF_PREFETCH_BLOCK_ONOPEN        = "prefetchBlocksOnOpen";

    private static final String HBASE_NAMESPACE_QUALIFIED_NAME            = "%s@%s";
    private static final String HBASE_TABLE_QUALIFIED_NAME_FORMAT         = "%s:%s@%s";
    private static final String HBASE_COLUMN_FAMILY_QUALIFIED_NAME_FORMAT = "%s:%s.%s@%s";
    private final HBaseAdmin hbaseAdmin;

    @Inject
    public HbaseMetaStoreBridgeUtils(AtlasTypeRegistry typeRegistry,AtlasEntityStore atlasEntityStore,AtlasGraph atlasGraph) throws Exception {
        this.atlasEntityStore = atlasEntityStore;
        Configuration atlasConf         = ApplicationProperties.get();
        this.clusterName   = atlasConf.getString(HBASE_CLUSTER_NAME, DEFAULT_CLUSTER_NAME);
        this.atlasTypeRegistry=typeRegistry;
        this.gremlinQueryProvider = AbstractMetaspaceGremlinQueryProvider.INSTANCE;
        this.graph=atlasGraph;
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();

        LOG.info("checking HBase availability..");

        HBaseAdmin.checkHBaseAvailable(conf);

        LOG.info("HBase is available");

        hbaseAdmin = new HBaseAdmin(conf);
    }


    public void importTable(final HTableDescriptor htd) throws Exception {
        String tableNameStr = htd.getTableName().getNameAsString();

        byte[]                 nsByte       = htd.getTableName().getNamespace();
        String                 nsName       = new String(nsByte);
        NamespaceDescriptor    nsDescriptor = hbaseAdmin.getNamespaceDescriptor(nsName);
        AtlasEntity.AtlasEntityWithExtInfo entity       = createOrUpdateNameSpace(nsDescriptor);
        HColumnDescriptor[]    hcdts        = htd.getColumnFamilies();

        createOrUpdateTable(nsName, tableNameStr, entity.getEntity(), htd, hcdts);
    }

    protected AtlasEntity.AtlasEntityWithExtInfo createOrUpdateNameSpace(NamespaceDescriptor namespaceDescriptor) throws Exception {
        String                 nsName          = namespaceDescriptor.getName();
        String                 nsQualifiedName = getNameSpaceQualifiedName(clusterName, nsName);
        AtlasEntity.AtlasEntityWithExtInfo nsEntity        = findNameSpaceEntityInAtlas(nsQualifiedName);

        if (nsEntity == null) {
            LOG.info("Importing NameSpace: " + nsQualifiedName);

            AtlasEntity entity = getNameSpaceEntity(nsName, null);

            nsEntity = registerInstance(new AtlasEntity.AtlasEntityWithExtInfo(entity));
        } else {
            LOG.info("NameSpace already present in Atlas. Updating it..: " + nsQualifiedName);

            AtlasEntity entity = getNameSpaceEntity(nsName, nsEntity.getEntity());

            nsEntity.setEntity(entity);

            updateInstance(nsEntity);
        }
        return nsEntity;
    }

    protected AtlasEntity.AtlasEntityWithExtInfo createOrUpdateTable(String nameSpace, String tableName, AtlasEntity nameSapceEntity, HTableDescriptor htd, HColumnDescriptor[] hcdts) throws Exception {
        String                 owner            = htd.getOwnerString();
        String                 tblQualifiedName = getTableQualifiedName(clusterName, nameSpace, tableName);
        AtlasEntity.AtlasEntityWithExtInfo ret              = findTableEntityInAtlas(tblQualifiedName);

        if (ret == null) {
            LOG.info("Importing Table: " + tblQualifiedName);

            ret = getTableEntity(nameSpace, tableName, owner, nameSapceEntity, htd, null);

            ret = registerInstance(ret);
        } else {
            LOG.info("Table already present in Atlas. Updating it..: " + tblQualifiedName);

            getTableEntity(nameSpace, tableName, owner, nameSapceEntity, htd, ret);

            updateInstance(ret);
        }

        AtlasEntity tableEntity = ret.getEntity();

        if (tableEntity != null) {
            List<AtlasEntity.AtlasEntityWithExtInfo> cfEntities = createOrUpdateColumnFamilies(nameSpace, tableName, owner, hcdts, ret);

            List<AtlasObjectId> cfIDs = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(cfEntities)) {
                for (AtlasEntity.AtlasEntityWithExtInfo cfEntity : cfEntities) {
                    cfIDs.add(AtlasTypeUtil.getAtlasObjectId(cfEntity.getEntity()));
                }
            }

            tableEntity.setAttribute(COLUMN_FAMILIES, cfIDs);
        }

        return ret;
    }

    protected List<AtlasEntity.AtlasEntityWithExtInfo> createOrUpdateColumnFamilies(String nameSpace, String tableName, String owner, HColumnDescriptor[] hcdts , AtlasEntityWithExtInfo tableRet) throws Exception {
        List<AtlasEntity.AtlasEntityWithExtInfo> ret = new ArrayList<>();
        AtlasEntity tableEntity= tableRet.getEntity();
        if (hcdts != null) {
            AtlasObjectId tableId = AtlasTypeUtil.getAtlasObjectId(tableEntity);
            List<String> columnFamiliesNames = Arrays.stream(hcdts).map(hcdt->hcdt.getNameAsString()).collect(Collectors.toList());
            deleteColumnFamiliesFEntity(tableRet,columnFamiliesNames);

            for (HColumnDescriptor columnFamilyDescriptor : hcdts) {
                String                 cfName          = columnFamilyDescriptor.getNameAsString();
                String                 cfQualifiedName = getColumnFamilyQualifiedName(clusterName, nameSpace, tableName, cfName);
                AtlasEntity.AtlasEntityWithExtInfo cfEntity        = findColumnFamiltyEntityInAtlas(cfQualifiedName);

                if (cfEntity == null) {
                    LOG.info("Importing Column-family: " + cfQualifiedName);

                    AtlasEntity entity = getColumnFamilyEntity(nameSpace, tableName, owner, columnFamilyDescriptor, tableId, null);

                    cfEntity = registerInstance(new AtlasEntity.AtlasEntityWithExtInfo(entity));
                } else {
                    LOG.info("ColumnFamily already present in Atlas. Updating it..: " + cfQualifiedName);

                    AtlasEntity entity = getColumnFamilyEntity(nameSpace, tableName, owner, columnFamilyDescriptor, tableId, cfEntity.getEntity());

                    cfEntity.setEntity(entity);

                    updateInstance(cfEntity);
                }

                ret.add(cfEntity);
            }
        }

        return ret;
    }

    private AtlasEntity.AtlasEntityWithExtInfo findNameSpaceEntityInAtlas(String nsQualifiedName) {
        AtlasEntity.AtlasEntityWithExtInfo ret = null;

        try {
            ret = findEntity(HBaseDataTypes.HBASE_NAMESPACE.getName(), nsQualifiedName);
        } catch (Exception e) {
            // entity doesn't exist in Atlas
            ret = null;
        }

        return ret;
    }

    private AtlasEntity.AtlasEntityWithExtInfo findTableEntityInAtlas(String tableQualifiedName) {
        AtlasEntity.AtlasEntityWithExtInfo ret = null;

        try {
            ret = findEntity(HBaseDataTypes.HBASE_TABLE.getName(), tableQualifiedName);
        } catch (Exception e) {
            // entity doesn't exist in Atlas
            ret = null;
        }

        return ret;
    }

    private AtlasEntity.AtlasEntityWithExtInfo findColumnFamiltyEntityInAtlas(String columnFamilyQualifiedName) {
        AtlasEntity.AtlasEntityWithExtInfo ret = null;

        try {
            ret = findEntity(HBaseDataTypes.HBASE_COLUMN_FAMILY.getName(), columnFamilyQualifiedName);
        } catch (Exception e) {
            // entity doesn't exist in Atlas
            ret = null;
        }

        return ret;
    }

    private AtlasEntity getNameSpaceEntity(String nameSpace, AtlasEntity nsEtity) {
        AtlasEntity ret  = null ;

        if (nsEtity == null) {
            ret = new AtlasEntity(HBaseDataTypes.HBASE_NAMESPACE.getName());
        } else {
            ret = nsEtity;
        }

        String qualifiedName = getNameSpaceQualifiedName(clusterName, nameSpace);

        ret.setAttribute(QUALIFIED_NAME, qualifiedName);
        ret.setAttribute(CLUSTERNAME, clusterName);
        ret.setAttribute(NAME, nameSpace);
        ret.setAttribute(DESCRIPTION_ATTR, nameSpace);

        return ret;
    }

    private AtlasEntityWithExtInfo getTableEntity(String nameSpace, String tableName, String owner, AtlasEntity nameSpaceEntity, HTableDescriptor htd, AtlasEntityWithExtInfo table) {
        if (table == null) {
            table = new AtlasEntityWithExtInfo(new AtlasEntity(HBaseDataTypes.HBASE_TABLE.getName()));
        }
        AtlasEntity ret = table.getEntity();

        String tableQualifiedName = getTableQualifiedName(clusterName, nameSpace, tableName);

        ret.setAttribute(QUALIFIED_NAME, tableQualifiedName);
        ret.setAttribute(CLUSTERNAME, clusterName);
        ret.setAttribute(NAMESPACE, AtlasTypeUtil.getAtlasObjectId(nameSpaceEntity));
        ret.setAttribute(NAME, tableName);
        ret.setAttribute(DESCRIPTION_ATTR, tableName);
        ret.setAttribute(OWNER, owner);
        ret.setAttribute(URI, tableName);
        ret.setAttribute(ATTR_TABLE_MAX_FILESIZE, htd.getMaxFileSize());
        ret.setAttribute(ATTR_TABLE_REPLICATION_PER_REGION, htd.getRegionReplication());
        ret.setAttribute(ATTR_TABLE_ISREADONLY, htd.isReadOnly());
        ret.setAttribute(ATTR_TABLE_ISCOMPACTION_ENABLED, htd.isCompactionEnabled());
        ret.setAttribute(ATTR_TABLE_DURABLILITY, (htd.getDurability() != null ? htd.getDurability().name() : null));

        table.addReferredEntity(nameSpaceEntity);
        return table;
    }

    private AtlasEntity getColumnFamilyEntity(String nameSpace, String tableName, String owner, HColumnDescriptor hcdt, AtlasObjectId tableId, AtlasEntity atlasEntity){
        AtlasEntity ret = null;

        if (atlasEntity == null) {
            ret = new AtlasEntity(HBaseDataTypes.HBASE_COLUMN_FAMILY.getName());
        } else {
            ret = atlasEntity;
        }

        String cfName          = hcdt.getNameAsString();
        String cfQualifiedName = getColumnFamilyQualifiedName(clusterName, nameSpace, tableName, cfName);

        ret.setAttribute(QUALIFIED_NAME, cfQualifiedName);
        ret.setAttribute(CLUSTERNAME, clusterName);
        ret.setAttribute(TABLE, tableId);
        ret.setAttribute(NAME, cfName);
        ret.setAttribute(DESCRIPTION_ATTR, cfName);
        ret.setAttribute(OWNER, owner);
        ret.setAttribute(ATTR_CF_BLOCK_CACHE_ENABLED, hcdt.isBlockCacheEnabled());
        ret.setAttribute(ATTR_CF_BLOOMFILTER_TYPE, (hcdt.getBloomFilterType() != null ? hcdt.getBloomFilterType().name():null));
        ret.setAttribute(ATTR_CF_CACHED_BLOOM_ON_WRITE, hcdt.isCacheBloomsOnWrite());
        ret.setAttribute(ATTR_CF_CACHED_DATA_ON_WRITE, hcdt.isCacheDataOnWrite());
        ret.setAttribute(ATTR_CF_CACHED_INDEXES_ON_WRITE, hcdt.isCacheIndexesOnWrite());
        ret.setAttribute(ATTR_CF_COMPACTION_COMPRESSION_TYPE, (hcdt.getCompactionCompressionType() != null ? hcdt.getCompactionCompressionType().name():null));
        ret.setAttribute(ATTR_CF_COMPRESSION_TYPE, (hcdt.getCompressionType() != null ? hcdt.getCompressionType().name():null));
        ret.setAttribute(ATTR_CF_DATA_BLOCK_ENCODING, (hcdt.getDataBlockEncoding() != null ? hcdt.getDataBlockEncoding().name():null));
        ret.setAttribute(ATTR_CF_ENCRYPTION_TYPE, hcdt.getEncryptionType());
        ret.setAttribute(ATTR_CF_EVICT_BLOCK_ONCLOSE, hcdt.isEvictBlocksOnClose());
        ret.setAttribute(ATTR_CF_KEEP_DELETE_CELLS, ( hcdt.getKeepDeletedCells() != null ? hcdt.getKeepDeletedCells().name():null));
        ret.setAttribute(ATTR_CF_MAX_VERSIONS, hcdt.getMaxVersions());
        ret.setAttribute(ATTR_CF_MIN_VERSIONS, hcdt.getMinVersions());
        ret.setAttribute(ATTR_CF_PREFETCH_BLOCK_ONOPEN, hcdt.isPrefetchBlocksOnOpen());
        ret.setAttribute(ATTR_CF_TTL, hcdt.getTimeToLive());

        return ret;
    }

    /**
     * Construct the qualified name used to uniquely identify a ColumnFamily instance in Atlas.
     * @param clusterName Name of the cluster to which the Hbase component belongs
     * @param nameSpace Name of the Hbase database to which the Table belongs
     * @param tableName Name of the Hbase table
     * @param columnFamily Name of the ColumnFamily
     * @return Unique qualified name to identify the Table instance in Atlas.
     */
    private static String getColumnFamilyQualifiedName(String clusterName, String nameSpace, String tableName, String columnFamily) {
        tableName = stripNameSpace(tableName.toLowerCase());
        return String.format(HBASE_COLUMN_FAMILY_QUALIFIED_NAME_FORMAT, nameSpace.toLowerCase(), tableName, columnFamily.toLowerCase(), clusterName);
    }

    /**
     * Construct the qualified name used to uniquely identify a Table instance in Atlas.
     * @param clusterName Name of the cluster to which the Hbase component belongs
     * @param nameSpace Name of the Hbase database to which the Table belongs
     * @param tableName Name of the Hbase table
     * @return Unique qualified name to identify the Table instance in Atlas.
     */
    private static String getTableQualifiedName(String clusterName, String nameSpace, String tableName) {
        tableName = stripNameSpace(tableName.toLowerCase());
        return String.format(HBASE_TABLE_QUALIFIED_NAME_FORMAT, nameSpace.toLowerCase(), tableName, clusterName);
    }

    /**
     * Construct the qualified name used to uniquely identify a Hbase NameSpace instance in Atlas.
     * @param clusterName Name of the cluster to which the Hbase component belongs
     * @param nameSpace Name of the NameSpace
     * @return Unique qualified name to identify the HBase NameSpace instance in Atlas.
     */
    private static String getNameSpaceQualifiedName(String clusterName, String nameSpace) {
        return String.format(HBASE_NAMESPACE_QUALIFIED_NAME, nameSpace.toLowerCase(), clusterName);
    }

    private static String stripNameSpace(String tableName){
        tableName = tableName.substring(tableName.indexOf(":")+1);

        return tableName;
    }



    @Override
    public void importDatabases(String taskInstanceId, TableSchema tableSchema) throws Exception {
        updatedTables = new AtomicInteger(0);
        startTime = new AtomicLong(System.currentTimeMillis());
        endTime = new AtomicLong(0);
        NamespaceDescriptor[] namespaceDescriptors = hbaseAdmin.listNamespaceDescriptors();
        List<String> namespaceNames = Arrays.stream(namespaceDescriptors).map(namespaceDescriptor -> namespaceDescriptor.getName()).collect(Collectors.toList());
        HTableDescriptor[] htds = hbaseAdmin.listTables();
        List<String> tableNames = Arrays.stream(htds).map(htd -> htd.getTableName().getNameAsString()).collect(Collectors.toList());
        totalTables = new AtomicInteger(namespaceDescriptors.length+htds.length);
        String databaseQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_HBASE_NS_BY_STATE), AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> dbVertices = (List) graph.executeGremlinScript(databaseQuery, false);
        for (AtlasVertex vertex : dbVertices) {
            if (Objects.nonNull(vertex)) {
                List<String> attributes = Lists.newArrayList(NAME, QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
                String databaseInGraph = dbEntity.getAttribute(NAME).toString();
                if (!namespaceNames.contains(databaseInGraph)) {
                    deleteEntity(dbEntity);
                }
            }
        }

        deleteTableEntity(tableNames);

        if (ArrayUtils.isNotEmpty(namespaceDescriptors)) {
            for (NamespaceDescriptor namespaceDescriptor : namespaceDescriptors) {
                createOrUpdateNameSpace(namespaceDescriptor);
                updatedTables.incrementAndGet();
            }
        }

        if (ArrayUtils.isNotEmpty(htds)) {
            for (HTableDescriptor htd : htds) {
                importTable(htd);
                updatedTables.incrementAndGet();
            }
        }
    }

    private void deleteTableEntity( List<String> tableNames) throws AtlasBaseException {
        String tableQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.HBASE_TABLE_BY_STATE), AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(tableQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String> attributes = Lists.newArrayList(NAME, QUALIFIED_NAME);
                AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity tableEntity = dbEntityWithExtInfo.getEntity();
                String tableNameInGraph = tableEntity.getAttribute(NAME).toString();
                if (!tableNames.contains(tableNameInGraph)) {
                    deleteEntity(tableEntity);
                }
            }
        }
    }


    private void deleteColumnFamiliesFEntity( AtlasEntityWithExtInfo tableRet,List<String> columnFamiliesNames) throws AtlasBaseException {
        Map<String, AtlasEntity> referredEntities = tableRet.getReferredEntities();
        for (AtlasEntity entity : referredEntities.values()) {
            if (Objects.nonNull(entity)) {
                String tableNameInGraph = entity.getAttribute(NAME).toString();
                String typeName = entity.getTypeName();
                if (!columnFamiliesNames.contains(tableNameInGraph)&&HBaseDataTypes.HBASE_COLUMN_FAMILY.name().equalsIgnoreCase(typeName)&&"ACTIVE".equals(entity.getStatus().toString())) {
                    deleteEntity(entity);
                }
            }
        }
    }
}
