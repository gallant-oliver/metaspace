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
 * @date 2018/12/4 19:22
 */
package io.zeta.metaspace.utils;

/*
 * @description
 * @author sunhaoning
 * @date 2018/12/4 19:22
 */
public class MetaspaceGremlin3QueryProvider extends MetaspaceGremlinQueryProvider {

    @Override
    public String getQuery(final MetaspaceGremlinQuery gremlinQuery) {
        // In case any overrides are necessary, a specific switch case can be added here to
        // return Gremlin 3 specific query otherwise delegate to super.getQuery
        switch (gremlinQuery) {

            case LINEAGE_DEPTH:
                return "g.withSack(0).V().has('__guid','%s').choose(inE().hasLabel('%s'),repeat(inE('%s').outV().outE('%s').inV()" +
                        ".sack(sum).by(constant(1)).simplePath()).emit().sack(),constant(0)).max().toList()";

            case LINEAGE_DEPTH_V2:
                return "g.withSack(0).V().has('__guid','%s').choose(inE().hasLabel('%s').outV().outE().hasLabel('%s'),repeat(inE('%s').outV().outE('%s').inV()" +
                       ".sack(sum).by(constant(1)).simplePath()).emit().sack(),constant(0)).max().toList()";

            case DIRECT_ENTITY_NUM:
                return "g.withSack(0).V().has('__guid','%s').inE('%s').outV().outE('%s').inV().count().toList()";

            case FULL_COLUMN_LINEAGE:
                return "g.V().has('__guid','%s').outE('__hive_table.columns').inV().repeat(__.inE('%s').as('e1').outV().outE('%s').as('e2').inV().simplePath()).emit().select('e1', 'e2').toList()";

            case PARTIAL_COLUMN_LINEAGE:
                return "g.V().has('__guid','%s').outE('__hive_table.columns').inV().repeat(__.inE('%s').as('e1').outV().outE('%s').as('e2').inV()).times(%s).emit().select('e1', 'e2').toList()";

            case FULL_COLUMN_RELATED_TABLE:
                return "g.V().has('__guid','%s').outE('__hive_table.columns').inV().repeat(__.inE('%s').as('e1').outV().outE('%s').as('e2').inV()).emit().select('e2').inV().outE().inV().dedup().by('__guid').has('__typeName','hive_table').values('__guid').toList()";

            case PARTIAL_COLUMN_RELATED_TABLE:
                return "g.V().has('__guid','%s').outE('__hive_table.columns').inV().repeat(__.inE('%s').as('e1').outV().outE('%s').as('e2').inV()).times(%s).emit().select('e2').inV().outE().inV().dedup().by('__guid').has('__typeName','hive_table').values('__guid').toList()";

            case DB_TABLE_BY_QUERY:
                /*return "g.V().has('__typeName','hive_db').as('a').inE().outV().has('__typeName','hive_table').group().by(outE('__hive_table.db').inV()).toList()";*/
                /*select选择 coalesce关联一对多*/
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').order().by('__timestamp').range(%s,%s).as('db').coalesce(inE().outV().has('__typeName','hive_table').as('table').select('db','table'),select('db','db')).toList()";
            case FULL_DB_TABLE:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').order().by('__timestamp').as('db').coalesce(inE().outV().has('__typeName','hive_table').as('table').select('db','table'),select('db','db')).toList()";

            case DB_TOTAL_NUM_BY_QUERY:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').dedup().count().toList()";
            case DB_ACTIVE_TOTAL_NUM_BY_QUERY:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__state','ACTIVE').has('__guid').dedup().count().toList()";

            case TABLE_GUID_QUERY:
                //g.tx().commit();
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name','%s').inE().outV().has('__typeName','hive_table').has('Asset.name','%s').values('__guid').toList()";

            case TABLE_DB_BY_QUERY:
                return "g.V().has('__typeName', 'hive_table').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').has('hive_table.temporary',false).order().by('__timestamp').dedup().range(%s, %s).as('table').outE().inV().has('__typeName','hive_db').as('db').select('table','db').toList()";
            case FULL_TABLE_DB:
                return "g.V().has('__typeName', 'hive_table').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').has('hive_table.temporary',false).order().by('__timestamp').dedup().as('table').outE().inV().has('__typeName','hive_db').as('db').select('table','db').toList()";

            case TABLE_COUNT_BY_QUEERY:
                return "g.V().has('__typeName', 'hive_table').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('hive_table.temporary',false).has('__guid').dedup().count().toList();";
            case COLUMN_TABLE_DB_BY_QUERY:
                return "g.V().has('__typeName', 'hive_column').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').order().by('__timestamp').dedup().range(%s,%s).as('column').inE().outV().has('__typeName','hive_table').as('table').outE().inV().has('__typeName','hive_db').as('db').select('column','table','db').toList()";
            case FULL_COLUMN_TABLE_DB:
                return "g.V().has('__typeName', 'hive_column').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').order().by('__timestamp').dedup().as('column').inE().outV().has('__typeName','hive_table').as('table').outE().inV().has('__typeName','hive_db').as('db').select('column','table','db').toList()";
            case COLUMN_COUNT_BY_QUERY:
                return "g.V().has('__typeName', 'hive_column').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').dedup().count().toList()";

            case ALL_TABLE:
                return "g.V().has('__typeName', 'hive_table').has('__guid').order().by('__timestamp').dedup().toList()";

            case DATABASE_BY_QUERY:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').order().by('__timestamp').dedup().range(%s,%s).toList()";
            case ACTIVE_DATABASE_BY_QUERY:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__state','ACTIVE').has('__guid').order().by('__timestamp').dedup().range(%s,%s).toList()";
            case FULL_DATABASE:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__guid').order().by('__timestamp').dedup().toList()";
            case FULL_ACTIVE_DATABASE:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).has('__state','ACTIVE').has('__guid').order().by('__timestamp').dedup().toList()";
            case TABLE_BY_DB:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('__guid','%s').inE().outV().has('__typeName','hive_table').has('hive_table.temporary',false).has('__guid').order().by('__timestamp').dedup().range(%s,%s).toList()";
            case FULL_TABLE:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('__guid','%s').inE().outV().has('__typeName','hive_table').has('hive_table.temporary',false).has('__guid').order().by('__timestamp').dedup().toList()";
            case TABLE_TOTAL_BY_DB:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('__guid','%s').inE().outV().has('__typeName','hive_table').has('hive_table.temporary',false).has('__guid').dedup().count().toList()";

            case DB_TABLE_BY_STATE:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('Asset.name', '%s').inE().outV().has('__typeName','hive_table').has('__guid').has('__state', '%s').order().by('__timestamp').dedup().toList()";
            case RDBMS_DB_TABLE_BY_STATE:
                return "g.tx().commit();g.V().has('__typeName','rdbms_instance').has('__guid', '%s').inE().outV().has('__typeName','rdbms_db').has('Asset.name', '%s').inE().outV().has('__typeName','rdbms_table').has('__guid').has('__state', '%s').order().by('__timestamp').dedup().toList()";
            case RDBMS_DB_TABLE_COLUMN_BY_STATE:
                return "g.tx().commit();g.V().has('__typeName','rdbms_instance').has('__guid', '%s').inE().outV().has('__typeName','rdbms_db').has('Asset.name', '%s').inE().outV().has('__typeName','rdbms_table').has('Asset.name', '%s').inE().outV().has('__typeName','rdbms_column').has('__guid').has('__state', '%s').order().by('__timestamp').dedup().toList()";
            case RDBMS_DB_TABLE_FOREIGNKEY_BY_STATE:
                return "g.tx().commit();g.V().has('__typeName','rdbms_instance').has('__guid', '%s').inE().outV().has('__typeName','rdbms_db').has('Asset.name', '%s').inE().outV().has('__typeName','rdbms_table').has('Asset.name', '%s').inE().outV().has('__typeName','rdbms_foreign_key').has('__guid').has('__state', '%s').order().by('__timestamp').dedup().toList()";
            case RDBMS_DB_TABLE_INDEX_BY_STATE:
                return "g.tx().commit();g.V().has('__typeName','rdbms_instance').has('__guid', '%s').inE().outV().has('__typeName','rdbms_db').has('Asset.name', '%s').inE().outV().has('__typeName','rdbms_table').has('Asset.name', '%s').inE().outV().has('__typeName','rdbms_index').has('__guid').has('__state', '%s').order().by('__timestamp').dedup().toList()";


            case FULL_DB_BY_STATE:
                return "g.tx().commit();g.V().has('__typeName','hive_db').has('__guid').has('__state', '%s').order().by('__timestamp').dedup().toList()";
            case FULL_RDBMS_DB_BY_STATE:
                return "g.tx().commit();g.V().has('__typeName','rdbms_instance').has('__guid', '%s').inE().outV().has('__typeName','rdbms_db').has('__guid').has('__state', '%s').order().by('__timestamp').dedup().toList()";


            case COLUMN_INFO_MAP:
                return "g.V().has('__guid', '%s').outE('__hive_table.columns').has('__state','ACTIVE').inV().valueMap('__guid', 'Asset.name', '__state','hive_column.type', '__modificationTimestamp').toList()";

            case COLUMN_INFO:
                return "g.V().has('__guid', '%s').outE('__hive_table.columns').has('__state','ACTIVE').inV().valueMap('__guid', 'Asset.name').toList()";

            case COLUMN_NAME_LIST:
                return "g.V().has('__guid', '%s').outE('__hive_table.columns').has('__state','ACTIVE').inV().order().by('Asset.name').valueMap('Asset.name').toList()";


            case TABLE_COLUMN_LIST:
                return "g.V().has('__guid', '%s').outE('__hive_table.columns').inV().has('__state','ACTIVE').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).range(%s,%s).order().by('%s',%s).valueMap('__guid', 'Asset.name','hive_column.type','hive_column.displayChineseText', 'hive_column.displayTextUpdateTime').toList()";

            case FULL_TABLE_COLUMN_LIST:
                return "g.V().has('__guid', '%s').outE('__hive_table.columns').inV().has('__state','ACTIVE').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).order().by('%s',%s).valueMap('__guid', 'Asset.name','hive_column.type','hive_column.displayChineseText', 'hive_column.displayTextUpdateTime').toList()";

            case FULL_TABLE_COLUMN_COUNT:
                return "g.V().has('__guid', '%s').outE('__hive_table.columns').inV().has('__state','ACTIVE').has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*%s.*')).count().toList()";
        }
        return null;
    }
}
