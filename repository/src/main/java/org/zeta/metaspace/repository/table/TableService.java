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

package org.zeta.metaspace.repository.table;


import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.AtlasDiscoveryService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.discovery.AtlasSearchResult;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

@AtlasService
public class TableService {

    private static final Logger LOG = LoggerFactory.getLogger(TableService.class);

    @Inject
    private AtlasDiscoveryService atlasDiscoveryService;


    public String databaseAndTable(String sql) throws Exception {
        Pattern pattern = Pattern.compile("CREATE[\\s\\S]*TABLE[\\s|\\sIF\\sNOT\\sEXISTS\\s]*([\\S]*\\.[\\S]*)");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "sql格式不正确" + sql);
    }

    /**
     * hive元数据同步有时间差
     */
    public String tableId(String database, String tableName) throws Exception {
        String tableId = null;
        for (int i = 0; i < 10; i++) {
            TimeUnit.SECONDS.sleep(1);
            SearchParameters parameters = new SearchParameters();
            parameters.setTypeName("hive_table");
            parameters.setOffset(0);
            parameters.setLimit(10000);
            parameters.setQuery(tableName);
            AtlasSearchResult atlasSearchResult = atlasDiscoveryService.searchWithParameters(parameters);
            List<AtlasEntityHeader> tables = atlasSearchResult.getEntities();
            if (tables != null && !tables.isEmpty()) {
                Optional<AtlasEntityHeader> tableEntity = tables.stream().filter(table -> {
                    String qualifiedName = (String) table.getAttribute("qualifiedName");
                    String[] dbTable = qualifiedName.substring(0, qualifiedName.indexOf("@")).split("\\.");
                    if (dbTable[0].equals(database) && dbTable[1].equals(tableName)) {
                        return true;
                    } else {
                        return false;
                    }
                }).findFirst();
                if (tableEntity.isPresent()) {
                    tableId = tableEntity.get().getGuid();
                    break;
                }
            }
        }
        return tableId;
    }

}
