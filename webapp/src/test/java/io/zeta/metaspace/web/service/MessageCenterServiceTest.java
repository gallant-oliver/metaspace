

package io.zeta.metaspace.web.service;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.MessageCenterDAO;
import org.apache.atlas.exception.AtlasBaseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;

import java.sql.SQLException;
import java.util.*;

/**
 * @author liwenfeng
 * @Description:
 * @date 2022/7/27
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageCenterServiceTest {

    @InjectMocks
    private final MessageCenterService messageCenterService = new MessageCenterService();

    @Mock
    private MessageCenterDAO messageCenterDAO;

    private MockHttpServletResponse response = new MockHttpServletResponse();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getMyMessageListTest() throws AtlasBaseException {
        String tenantId = java.util.UUID.randomUUID().toString();
        String userId = java.util.UUID.randomUUID().toString();
        String search = java.util.UUID.randomUUID().toString();
        Integer type = new Random().nextInt(4);
        Integer status = new Random().nextInt(2);
        long offset = 0;
        long limit = 10;
        try {
            Mockito.when(messageCenterDAO.getMyMessageList(type, tenantId, userId, status, search, offset, limit))
                    .thenReturn(new ArrayList<>());
            PageResult<Map<String, Object>> pageResult = messageCenterService.getMyMessageList(type, tenantId, status, search, offset, limit);
            Assert.assertEquals(0, pageResult.getTotalSize());

            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<String, Object>(){
                {
                    put("name", "messageTest");
                    put("module", "系统管理/用户组管理");
                    put("typeCn", "用户组信息");
                    put("status", 0);
                    put("processCn", "已授权");
                    put("tenantName", "user");
                }
            };
            list.add(map);
            Mockito.when(messageCenterDAO.getMyMessageList(type, tenantId, userId, status, search, offset, limit))
                    .thenReturn(list);
            PageResult<Map<String, Object>> result = messageCenterService.getMyMessageList(type, tenantId, status, search, offset, limit);
            Assert.assertEquals(1, result.getTotalSize());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getMessageDetailTest() {
        String tenantId = java.util.UUID.randomUUID().toString();
        String userId = java.util.UUID.randomUUID().toString();
        String id = null;
        Result result = new Result();
        result = messageCenterService.getMessageDetail(id, tenantId);
        Assert.assertTrue("METASPACE-400-00-89".equals(result.getCode()));
        try {
            id = java.util.UUID.randomUUID().toString();
            Mockito.when(messageCenterDAO.getMessageDetail(id, tenantId, userId))
                    .thenReturn(null);
            result = messageCenterService.getMessageDetail(id, tenantId);
            Assert.assertEquals(0, ((Map) result.getData()).size());

            Mockito.when(messageCenterDAO.getMessageDetail(id, tenantId, userId))
                    .thenReturn(new HashMap<String, Object>() {
                        {
                            put("name", "messageTest");
                            put("module", "系统管理/用户组管理");
                            put("typeCn", "用户组信息");
                            put("status", 0);
                            put("processCn", "已授权");
                            put("tenantName", "user");
                        }
                    });
            result = messageCenterService.getMessageDetail(id, tenantId);
            Assert.assertNotNull(result.getData());

            List<String> idList = new ArrayList<>();
            idList.add(id);
            Mockito.when(messageCenterDAO.batchToRead(idList))
                    .thenReturn(0);
            result = messageCenterService.getMessageDetail(id, tenantId);
            Assert.assertNotNull(result.getData());
        } catch (SQLException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getUnReadNumTest() {
        String tenantId = java.util.UUID.randomUUID().toString();
        String userId = java.util.UUID.randomUUID().toString();
        List<Integer> typeList = new ArrayList<>(Arrays.asList(0, 1, 2, 3, -1));
        Map<String, Object> map = new HashMap<>();
        try {
            for (Integer type : typeList) {
                Mockito.when(messageCenterDAO.getUnReadNum(type, tenantId, userId))
                        .thenReturn(0);
            }
            Result result = messageCenterService.getUnReadNum(tenantId);
            Assert.assertEquals("200", result.getCode());

            typeList.clear();
            typeList.add(4);
            for (Integer type : typeList) {
                Mockito.when(messageCenterDAO.getUnReadNum(type, tenantId, userId))
                        .thenReturn(0);
            }
            Result resultError = messageCenterService.getUnReadNum(tenantId);
            Assert.assertEquals("METASPACE-401-00-004", resultError.getCode());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void batchToReadTest() {
        List<String> ids = new ArrayList<>();
        Result result = null;
        try {
            result = messageCenterService.batchToRead(ids);
        } catch (Exception e) {
            Assert.assertEquals("METASPACE-400-00-89", result.getCode());
        }
        ids.add(java.util.UUID.randomUUID().toString());
        try {
            Mockito.when(messageCenterDAO.batchToRead(ids))
                    .thenReturn(0);
            result = messageCenterService.batchToRead(ids);
            Assert.assertEquals("METASPACE-401-00-005", result.getCode());

            Mockito.when(messageCenterDAO.batchToRead(ids))
                    .thenReturn(1);
            result = messageCenterService.batchToRead(ids);
            Assert.assertEquals(1, result.getData());

            Mockito.when(messageCenterDAO.batchToRead(null))
                    .thenReturn(0);
            result = messageCenterService.batchToRead(ids);
            Assert.assertEquals("METASPACE-401-00-006", result.getCode());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void batchDelteTest() {
        List<String> ids = new ArrayList<>();
        String tenantId = java.util.UUID.randomUUID().toString();
        String userId = java.util.UUID.randomUUID().toString();
        String delAll = null;
        Result result = null;
        try {
            result = messageCenterService.batchDelte(ids, delAll, tenantId);
            Assert.assertEquals("METASPACE-400-00-89", result.getCode());

            result = messageCenterService.batchDelte(null, delAll, tenantId);
            Assert.assertEquals("METASPACE-400-00-89", result.getCode());

            ids.add(java.util.UUID.randomUUID().toString());

            Mockito.when(messageCenterDAO.batchDelte(ids, tenantId, userId, delAll))
                    .thenReturn(0);
            result = messageCenterService.batchDelte(ids, delAll, tenantId);
            Assert.assertEquals("METASPACE-401-00-005", result.getCode());

            Mockito.when(messageCenterDAO.batchDelte(ids, tenantId, userId, delAll))
                    .thenReturn(1);
            result = messageCenterService.batchDelte(ids, delAll, tenantId);
            Assert.assertEquals(1, result.getData());

            Mockito.when(messageCenterDAO.batchDelte(null, tenantId, userId, delAll))
                    .thenReturn(0);
            Assert.assertEquals("METASPACE-401-00-007", result.getCode());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addMessageTest() {
        String tenantId = java.util.UUID.randomUUID().toString();
        MessageEntity message = new MessageEntity();
        message.setId(UUIDUtils.uuid());
        message.setTenantid(tenantId);
        message.setDelete(false);
        try {
            messageCenterDAO.addMessage(message);
            Result result = messageCenterService.addMessage(message, tenantId);
            Assert.assertEquals("200", result.getCode());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            Result result = messageCenterService.addMessage(null, tenantId);
            Assert.assertEquals("METASPACE-401-00-008", result.getCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
