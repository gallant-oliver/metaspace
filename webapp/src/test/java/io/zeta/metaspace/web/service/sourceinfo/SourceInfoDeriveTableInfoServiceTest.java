

package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveColumnVO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveTableColumnVO;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangrongwen
 * @Description:
 * @date 2022/1/2017:50
 */
@RunWith(MockitoJUnitRunner.class)
public class SourceInfoDeriveTableInfoServiceTest {

    @InjectMocks
    private SourceInfoDeriveTableInfoService sourceInfoDeriveTableInfoService = new SourceInfoDeriveTableInfoService();

    private MockHttpServletResponse response = new MockHttpServletResponse();

    private static SourceInfoDeriveTableColumnVO sourceInfoDeriveTableColumnVO = new SourceInfoDeriveTableColumnVO();

    static {
        sourceInfoDeriveTableColumnVO.setTableNameEn("TestTable");
        sourceInfoDeriveTableColumnVO.setTableNameZh("测试表");
        sourceInfoDeriveTableColumnVO.setCategory("test/test");
        sourceInfoDeriveTableColumnVO.setCreatorName("user");
        sourceInfoDeriveTableColumnVO.setUpdateFrequency("updateFrequency");
        sourceInfoDeriveTableColumnVO.setEtlPolicy("ETL");
        sourceInfoDeriveTableColumnVO.setIncreStandard("standard");
        sourceInfoDeriveTableColumnVO.setIncrementalField("field");
        sourceInfoDeriveTableColumnVO.setCleanRule("cleanRule");
        sourceInfoDeriveTableColumnVO.setFilter("filter");
        sourceInfoDeriveTableColumnVO.setProcedure("procedure");
        sourceInfoDeriveTableColumnVO.setRemark("remark");
        sourceInfoDeriveTableColumnVO.setBusinessHeader("业务目录");
        sourceInfoDeriveTableColumnVO.setBusiness("业务组");
        SourceInfoDeriveColumnVO sourceInfoDeriveColumnVO = new SourceInfoDeriveColumnVO();
        sourceInfoDeriveColumnVO.setColumnNameEn("EnglishName");
        sourceInfoDeriveColumnVO.setColumnNameZh("中文名");
        sourceInfoDeriveColumnVO.setDataType("String");
        sourceInfoDeriveColumnVO.setDataBaseName("DB");
        sourceInfoDeriveColumnVO.setSourceTableNameEn("sourceTable");
        sourceInfoDeriveColumnVO.setSourceTableNameZh("原表");
        sourceInfoDeriveColumnVO.setSourceColumnNameEn("sourceColumn");
        sourceInfoDeriveColumnVO.setSourceColumnNameZh("源字段");
        sourceInfoDeriveColumnVO.setSourceColumnType("String");
        sourceInfoDeriveColumnVO.setGroupField(true);
        sourceInfoDeriveColumnVO.setPrimaryKey(false);
        sourceInfoDeriveColumnVO.setMappingRule("mapRule");
        sourceInfoDeriveColumnVO.setMappingDescribe("mapping");
        sourceInfoDeriveColumnVO.setSecret(true);
        sourceInfoDeriveColumnVO.setSecretPeriod("一年");
        sourceInfoDeriveColumnVO.setImportant(false);
        sourceInfoDeriveColumnVO.setDesensitizationRules("脱敏规则");
        List<ColumnTag> list = new ArrayList<>();
        ColumnTag tag1 = new ColumnTag();
        tag1.setName("tag1");
        list.add(tag1);
        sourceInfoDeriveColumnVO.setTags(list);
        sourceInfoDeriveColumnVO.setPermissionField(false);
        sourceInfoDeriveColumnVO.setRemark("remark");
        List<SourceInfoDeriveColumnVO> list1 = new ArrayList<>();
        list1.add(sourceInfoDeriveColumnVO);
        sourceInfoDeriveTableColumnVO.setSourceInfoDeriveColumnVOS(list1);
    }

    /**
     * 本次单测需要加入vm参数：-Datlas.home=
     *
     * @throws SQLException
     */
    @Test
    public void exportById() throws SQLException {
        SourceInfoDeriveTableInfoService spy = Mockito.spy(sourceInfoDeriveTableInfoService);
        Mockito.doReturn(sourceInfoDeriveTableColumnVO).when(spy).getDeriveTableColumnDetail(Mockito.any(), Mockito.any());
        spy.exportById(response, "tenantId", "tableId");
    }

    /**
     * 本次单测需要加入vm参数：-Datlas.home=
     */
    @Test
    public void downloadTemplate() {
        sourceInfoDeriveTableInfoService.downloadTemplate(response);
    }
}
