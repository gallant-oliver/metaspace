package org.zeta.metaspace.repository.tablestat;

import io.zata.metaspace.repository.tablestat.TableStatService;
import io.zeta.metaspace.model.table.TableStat;
import io.zeta.metaspace.model.table.TableStatRequest;
import org.testng.annotations.Test;

import java.util.List;

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
public class TableStatServiceTest {

    private TableStatService service = new TableStatService();

    @Test
    public void testQuery() throws Exception {
        String tableId = "1ae96c73ee8c4034a20e3990c2d0df8e";
        TableStatRequest request = new TableStatRequest(tableId, "1", "2018-01-01", "2018-02-01", 0, 10);
        List<TableStat> query = service.query(request).getRight();
        query.forEach(System.out::println);
    }

}