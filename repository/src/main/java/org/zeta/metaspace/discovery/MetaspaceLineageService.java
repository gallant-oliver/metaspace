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
 * @date 2018/12/4 19:30
 */
package org.zeta.metaspace.discovery;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.springframework.stereotype.Service;
import org.zeta.metaspace.model.metadata.Database;
import org.zeta.metaspace.model.result.PageResult;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2018/12/4 19:30
 */

public interface MetaspaceLineageService {

    AtlasLineageInfo getColumnLineageInfo(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException;

    Integer getLineageDepth(String guid, AtlasLineageInfo.LineageDirection direction) throws AtlasBaseException;

    Integer getEntityDirectNum(String entityGuid, AtlasLineageInfo.LineageDirection direction) throws AtlasBaseException;

    List<String> getColumnRelatedTable(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException;

    PageResult<Database> getAllDBAndTable(int limit, int offset) throws AtlasBaseException;
}
