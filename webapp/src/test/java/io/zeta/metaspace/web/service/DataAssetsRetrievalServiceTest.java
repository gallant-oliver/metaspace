package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dataassets.DataAssets;
import io.zeta.metaspace.model.result.PageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;


/**
 * @author huangrongwen
 * @Description:
 * @date 2022/3/2214:26
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAssetsRetrievalServiceTest {

    @Mock
    private DataAssetsRetrievalService dataAssetsRetrievalService;

    @Test
    public void search() {
        PageResult<DataAssets> pageResult = new PageResult<>();
        OngoingStubbing<PageResult<DataAssets>> stubbing = when(dataAssetsRetrievalService.search(1, 0, 10, "tenantId", "name")).thenReturn(pageResult);
        assertNotNull(stubbing);
    }
}
