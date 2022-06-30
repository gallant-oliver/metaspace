package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.api.ApiTestInfoVO;
import org.apache.atlas.AtlasException;
import org.mockito.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

/**
 * @author huangrongwen
 * @Description:
 * @date 2022/3/2213:56
 */
@RunWith(MockitoJUnitRunner.class)
public class DataShareServiceTest {


    @Mock
    private DataShareService dataShareService;

    @Test
    public void testApi() throws AtlasException {
        ApiTestInfoVO apiTestInfoVO = new ApiTestInfoVO();
        apiTestInfoVO.setPageSize(10);
        apiTestInfoVO.setPageNum(1);
        apiTestInfoVO.setApiId("api_id");
        apiTestInfoVO.setVersion("1.1");
        Result result = new Result();
        result.setMessage("测试成功");
        OngoingStubbing<Result> aReturn = when(dataShareService.testApi(apiTestInfoVO)).thenReturn(result);
        assertNotNull(aReturn);
    }
}
