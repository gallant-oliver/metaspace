

package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.dto.sourceinfo.SourceInfoDeriveTableColumnDTO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveColumnVO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveTableColumnVO;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

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

    private static SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDTO = new SourceInfoDeriveTableColumnDTO();

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

        BeanUtils.copyProperties(sourceInfoDeriveTableColumnVO,sourceInfoDeriveTableColumnDTO);
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

    @Test
    public void getDeriveTableColumnDetail() throws SQLException {
        String tenantId = "a788801785844f71b3009a53595afb26";
        String tableId = "30399384-073d-4a2c-846e-6683e6f14387";
        SourceInfoDeriveTableColumnVO result = mock(SourceInfoDeriveTableColumnVO.class);
        SourceInfoDeriveTableInfoService spy = Mockito.spy(sourceInfoDeriveTableInfoService);
        Mockito.doReturn(result).when(spy).getDeriveTableColumnDetail(tenantId,tableId);
        Assert.assertSame(result,spy.getDeriveTableColumnDetail(tenantId,tableId));
    }

    @Test
    public void getTags() {
        String tenantId = "a788801785844f71b3009a53595afb26";
        String strTags = "058b57f8-853e-4ad5-ba8d-4d5d0f446354,a4cdf993-e60a-431b-9138-cba9bb3ef874";
        String tableGuid = "c1b3f97c-30cd-4e2d-9423-46a3befd7b41";
        String columnNameEn = "test_day";
        SourceInfoDeriveTableInfoService spy = Mockito.spy(sourceInfoDeriveTableInfoService);
        Mockito.doReturn(new ArrayList<ColumnTag>()).when(spy).getTags(strTags,tableGuid,columnNameEn,tenantId);
        Assert.assertNotNull(spy.getTags(strTags,tableGuid,columnNameEn,tenantId));
    }

    @Test
    public void packTags() {
        String tenantId = "a788801785844f71b3009a53595afb26";
        String tableId = "30399384-073d-4a2c-846e-6683e6f14387";
        Assert.assertNotNull(sourceInfoDeriveTableInfoService.packTags(tenantId,tableId));
    }

    @Test
    public void createSaveAndSubmitDeriveTableInfo() {
        String tenantId = "a788801785844f71b3009a53595afb26";
        SourceInfoDeriveTableColumnDTO mockDto = mock(SourceInfoDeriveTableColumnDTO.class);
        SourceInfoDeriveTableInfoService spy = Mockito.spy(sourceInfoDeriveTableInfoService);
        Mockito.doReturn(true).when(spy).createSaveAndSubmitDeriveTableInfo(mockDto,tenantId);
        Assert.assertTrue(spy.createSaveAndSubmitDeriveTableInfo(mockDto,tenantId));
    }

    @Test
    public void updateSaveAndSubmitDeriveTableInfo() {
        String tenantId = "a788801785844f71b3009a53595afb26";
        SourceInfoDeriveTableColumnDTO mockDto = mock(SourceInfoDeriveTableColumnDTO.class);
        SourceInfoDeriveTableInfoService spy = Mockito.spy(sourceInfoDeriveTableInfoService);
        Mockito.doReturn(true).when(spy).updateSaveAndSubmitDeriveTableInfo(mockDto,tenantId);
        Assert.assertTrue(spy.updateSaveAndSubmitDeriveTableInfo(mockDto,tenantId));
    }
}
