package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.RequirementDTO;
import io.zeta.metaspace.model.dto.requirements.ResourceDTO;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author 周磊
 * @version 1.0
 * @date 2021-12-20
 */
@Test
@ContextConfiguration(locations = {"classpath*:/spring-test-config.xml"})
public class RequirementsPublicTenantServiceTest extends AbstractTestNGSpringContextTests {
    @Autowired
    private RequirementsPublicTenantService service;
    
    private static String tableId;
    private static String businessId;
    private static String dataSourceId;
    private static String tenantId;
    private static RequirementDTO requirementDTO;
    
    
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
    
    @BeforeClass
    public void setUp() throws Exception {
        // Atlas.main(null);
        // MockitoAnnotations.initMocks(this);
    }
    
    @AfterMethod
    public void tearDown() {
    }
    
    @Test()
    public void testCreatedApiRequirement() {
        requirementDTO.setName("unit_test_creat");
        requirementDTO.setNum("unit_test_creat");
        requirementDTO.setResourceType(ResourceType.API);
        String requirementId = service.createdResource(requirementDTO);
        Assert.assertTrue(StringUtils.isNotBlank(requirementId));
    }
    
    @Test
    public void testPagedResource() {
        Parameters queryParam = new Parameters();
        queryParam.setOffset(0);
        queryParam.setLimit(10);
        PageResult<ResourceDTO> result = service.pagedResource(tableId, queryParam);
    }
    
    @Test
    public void testGrant() {
    }
    
    @Test
    public void testGetFeedbackResult() {
    }
    
    @Test
    public void testDeleteRequirements() {
    }
    
    @Test
    public void testEditedResource() {
    }
    
    @Test
    public void testQueryColumnsByTableId() {
    }
    
    @Test
    public void testQueryIssuedInfo() {
    }
    
    @Test
    public void testGetListByCreatorPage() {
    }
    
    @Test
    public void testGetDetailBase() {
    }
}