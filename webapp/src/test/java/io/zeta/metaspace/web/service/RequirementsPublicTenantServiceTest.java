package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

/**
 * @author 周磊
 * @version 1.0
 * @date 2021-12-20
 */
@RunWith(MockitoJUnitRunner.class)
public class RequirementsPublicTenantServiceTest {
    @Mock
    private RequirementsPublicTenantService service;
    
    private static final String tableId;
    private static final String businessId;
    private static final String dataSourceId;
    private static final String tenantId;
    private static final RequirementDTO requirementDTO;
    
    static {
        tableId = "ed662735-4384-4512-be1f-f5cdee65bcc5";
        businessId = "34fa9849-a4e8-403b-a1a6-32e12d4e99d6";
        dataSourceId = "01e1dcd4-1d3e-4a08-8b97-f9424ac704af";
        tenantId = "999dc8e1a2a6461ba8e23a3565608436";
        
        requirementDTO = new RequirementDTO();
        requirementDTO.setBusinessId(businessId);
        requirementDTO.setTenantId(tenantId);
        requirementDTO.setSourceId(dataSourceId);
        requirementDTO.setTableId(tableId);
    }
    
    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testPagedResource() {
        Parameters queryParam = new Parameters();
        queryParam.setOffset(0);
        queryParam.setLimit(10);
        when(service.pagedResource(tableId, queryParam)).thenReturn(new PageResult<>(100, null));
        assertNotNull(service.pagedResource(tableId, queryParam));
    }
    
    @Test()
    public void testCreatedApiRequirement() {
        requirementDTO.setName("unit_test_creat");
        requirementDTO.setNum("unit_test_creat");
        requirementDTO.setResourceType(ResourceType.API);
        String requirementId = UUID.randomUUID().toString();
        when(service.createdRequirement(requirementDTO)).thenReturn(requirementId);
        Assert.assertTrue(StringUtils.isNotBlank(service.createdRequirement(requirementDTO)));
        requirementDTO.setGuid(requirementId);
    }
    
    @Test
    public void testEditedRequirement() {
        requirementDTO.setName("unit_test_edit");
        requirementDTO.setNum("unit_test_edit");
        requirementDTO.setResourceType(ResourceType.MESSAGE_QUEUE);
        try {
            service.editedRequirement(requirementDTO);
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testDeleteRequirements() {
        try {
            service.deleteRequirements(Collections.singletonList(requirementDTO.getGuid()));
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testGrant() {
        try {
            service.grant(requirementDTO.getGuid());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testGetFeedbackResult() {
        String guid = requirementDTO.getGuid();
        int resourceType = requirementDTO.getResourceType().getCode();
        FeedbackResultDTO resultDTO = mock(FeedbackResultDTO.class);
        when(service.getFeedbackResult(guid, resourceType)).thenReturn(resultDTO);
        assertNotNull(service.getFeedbackResult(guid, resourceType));
    }
    
    @Test
    public void testQueryColumnsByTableId() {
        RequirementColumnDTO columnDTO = mock(RequirementColumnDTO.class);
        when(service.queryColumnsByTableId(tableId)).thenReturn(Collections.singletonList(columnDTO));
        assertNotNull(service.queryColumnsByTableId(tableId));
    }
    
    @Test
    public void testQueryIssuedInfo() {
        RequirementIssuedDTO expect = mock(RequirementIssuedDTO.class);
        when(service.queryIssuedInfo(tableId, dataSourceId)).thenReturn(expect);
        RequirementIssuedDTO result = service.queryIssuedInfo(tableId, dataSourceId);
        assertSame(expect, result);
    }
    
    @Test
    public void testGetListByCreatorPage() {
        RequireListParam param = mock(RequireListParam.class);
        when(service.getListByCreatorPage(param)).thenReturn(new PageResult());
        assertNotNull(service.getListByCreatorPage(param));
    }
    
    @Test
    public void testGetDetailBase() {
        String guid = requirementDTO.getGuid();
        int resourceType = requirementDTO.getResourceType().getCode();
        FeedbackDetailBaseDTO baseDTO = mock(FeedbackDetailBaseDTO.class);
        when(service.getDetailBase(guid, resourceType)).thenReturn(baseDTO);
        assertSame(baseDTO, service.getDetailBase(guid, resourceType));
    }
}