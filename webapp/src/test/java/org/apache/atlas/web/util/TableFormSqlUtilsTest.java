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

package org.apache.atlas.web.util;

import com.google.common.collect.Lists;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.table.Field;
import io.zeta.metaspace.model.table.TableForm;
import org.testng.annotations.Test;
import io.zeta.metaspace.web.util.TableSqlUtils;

import static org.testng.Assert.assertEquals;

public class TableFormSqlUtilsTest {

    @Test
    public void testFormatSimple() throws AtlasBaseException {
        TableForm tableForm = new TableForm("default", "person", "", 2, "-1",
                                            Lists.newArrayList(new Field("name", null, "string"), new Field("age", null, "int"))
                , false, null, null, null, null, null
        );

        String sql = TableSqlUtils.format(tableForm);
        assertEquals(sql, "CREATE  TABLE default.person (name string,age int)");
    }

    @Test
    public void testFormatExternal() throws AtlasBaseException {
        TableForm tableForm = new TableForm("default", "person", "desc person", 1, "-1",
                                            Lists.newArrayList(new Field("name", null, "string"), new Field("age", null, "int"))
                , true, Lists.newArrayList(new Field("gender", "sex", "int")),
                                            "parquet", "/usr/local/person", ",", "\n"
        );

        String sql = TableSqlUtils.format(tableForm);
        assertEquals(sql, "CREATE EXTERNAL TABLE default.person (name string,age int) COMMENT 'desc person' PARTITIONED BY (gender int COMMENT 'sex')" +
                          " ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n" +
                          "' STORED AS PARQUET LOCATION '/usr/local/person'");
    }

    @Test
    public void testFormatInternal() throws AtlasBaseException {
        TableForm tableForm = new TableForm("default", "person", "desc person", 2, "-1",
                                            Lists.newArrayList(new Field("name", null, "string"), new Field("age", null, "int"))
                , true, Lists.newArrayList(new Field("gender", "sex", "int")),
                                            "parquet", null, null, null
        );

        String sql = TableSqlUtils.format(tableForm);
        assertEquals(sql, "CREATE  TABLE default.person (name string,age int) COMMENT 'desc person' PARTITIONED BY (gender int COMMENT 'sex') STORED AS PARQUET");
    }
}
