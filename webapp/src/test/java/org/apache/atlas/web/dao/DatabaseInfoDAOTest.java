package org.apache.atlas.web.dao;

import com.google.gson.Gson;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

public class DatabaseInfoDAOTest {

    @Mock
    DatabaseInfoDAO databaseInfoDAO;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void insertTest(){


        databaseInfoDAO.insertDatabaseInfo(this.build());
    }

    private  DatabaseInfoPO build(){
        DatabaseInfoPO dip = new DatabaseInfoPO();
        dip.setId(UUID.randomUUID().toString());
        dip.setUpdater("21");
        dip.setTenantId("18b0c9475b8f4db980f2a86106f76b9a");
        dip.setCreator("21");
        dip.setStatus(Status.FOUNDED.getIntValue()+"");
        dip.setDatabaseId("dbbase1");
        dip.setApproveId("1");
        dip.setDatabaseAlias("插入测试1");
        dip.setBoEmail("test@163.com");
        dip.setBoTel("13788888888");
        dip.setBoName("测试业务对接人");
        dip.setBoDepartmentName("测试部门1");
        dip.setDescription("描述");
        dip.setCategoryId("3");
        dip.setBusinessLeader("业务负责人");
        dip.setImportance(Boolean.TRUE);
        dip.setSecurity(Boolean.TRUE);
        dip.setExtractCycle("三年");
        dip.setExtractTool("工具1");
        dip.setPlanningPackageCode("PACTEST01");
        dip.setPlanningPackageName("规划包01");
        dip.setTechnicalLeader("琪亚娜");
        dip.setSecurityCycle("七年");
        dip.setToEmail("test@163.com");
        dip.setToTel("13788888888");
        dip.setToName("测试业务对接人");
        dip.setToDepartmentName("测试部门1");

        return dip;
    }

    @Test
    public void test2(){
        System.out.println(new Gson().toJson(this.build()));
    }
}
