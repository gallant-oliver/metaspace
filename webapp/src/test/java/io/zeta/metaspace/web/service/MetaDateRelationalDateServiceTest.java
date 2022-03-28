package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.model.metadata.LineageTrace;
import io.zeta.metaspace.model.metadata.SimpleTaskNode;
import io.zeta.metaspace.model.metadata.TableInfoVo;
import io.zeta.metaspace.model.metadata.TableLineageInfo;
import io.zeta.metaspace.model.po.tableinfo.TableSourceDataBasePO;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class MetaDateRelationalDateServiceTest {

    private MetaDateRelationalDateService metaDateRelationalDateService = new MetaDateRelationalDateService();

    private static SimpleTaskNode simpleTaskNode = new SimpleTaskNode();

    private static List<SimpleTaskNode> simpleTaskNodes = new ArrayList<>();

    static {
        String res = "[\n" +
                "    {\n" +
                "      \"name\": \"tasks-48227\",\n" +
                "      \"state\": \"success\",\n" +
                "      \"inputTable\": {\n" +
                "        \"host\": \"10.201.50.201\",\n" +
                "        \"port\": 3307,\n" +
                "        \"database\": \"test\",\n" +
                "        \"table\": \"test05\",\n" +
                "        \"datasource\": 223,\n" +
                "        \"type\": \"POSTGRESQL\"\n" +
                "      },\n" +
                "      \"outputTable\": {\n" +
                "        \"host\": \"10.201.50.201\",\n" +
                "        \"port\": 3307,\n" +
                "        \"database\": \"myhive\",\n" +
                "        \"table\": \"a\",\n" +
                "        \"datasource\": 223,\n" +
                "        \"type\": \"POSTGRESQL\"\n" +
                "      },\n" +
                "      \"desc\": \"\",\n" +
                "      \"sql\": \"select 1\",\n" +
                "      \"taskType\": \"SHELL\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"tasks-48227\",\n" +
                "      \"state\": \"success\",\n" +
                "      \"inputTable\": {\n" +
                "        \"host\": \"10.201.50.201\",\n" +
                "        \"port\": 3307,\n" +
                "        \"database\": \"myhive\",\n" +
                "        \"table\": \"a\",\n" +
                "        \"datasource\": 223,\n" +
                "        \"type\": \"MYSQL\"\n" +
                "      },\n" +
                "      \"outputTable\": {\n" +
                "        \"host\": \"10.201.50.201\",\n" +
                "        \"port\": 3307,\n" +
                "        \"database\": \"myhive\",\n" +
                "        \"table\": \"c\",\n" +
                "        \"datasource\": 223,\n" +
                "        \"type\": \"MYSQL\"\n" +
                "      },\n" +
                "      \"desc\": \"\",\n" +
                "      \"sql\": \"select 7\",\n" +
                "      \"taskType\": \"SHELL\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"tasks-48227\",\n" +
                "      \"state\": \"success\",\n" +
                "      \"inputTable\": {\n" +
                "        \"host\": \"10.201.50.201\",\n" +
                "        \"port\": 3307,\n" +
                "        \"database\": \"myhive\",\n" +
                "        \"table\": \"A\",\n" +
                "        \"datasource\": 223,\n" +
                "        \"type\": \"POSTGRESQL\"\n" +
                "      },\n" +
                "      \"outputTable\": {\n" +
                "        \"host\": \"10.201.50.201\",\n" +
                "        \"port\": 3307,\n" +
                "        \"database\": \"test\",\n" +
                "        \"table\": \"test05\",\n" +
                "        \"datasource\": 223,\n" +
                "        \"type\": \"POSTGRESQL\"\n" +
                "      },\n" +
                "      \"desc\": \"\",\n" +
                "      \"sql\": \"select 1\",\n" +
                "      \"taskType\": \"SHELL\"\n" +
                "    }\n" +
                "]";
        Gson gson = new Gson();
        simpleTaskNodes = gson.fromJson(res, new TypeToken<List<SimpleTaskNode>>() {
        }.getType());

    }

    @Test
    public void testUnifyDataBase() {
        try {
            MetaDateRelationalDateService.unifyDataBase(simpleTaskNodes);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUpdateExistTableGuid() {
        try {
            TableSourceDataBasePO po = new TableSourceDataBasePO();
            po.setGuid("目标");
            po.setHost("10.201.50.201");
            po.setPort("3307");
            po.setDatabase("test");
            po.setTable("test05");
            po.setType("POSTGRESQL");
            List<TableSourceDataBasePO> basePOS = new ArrayList<>();
            basePOS.add(po);
            MetaDateRelationalDateService.updateExistTableGuid(simpleTaskNodes, basePOS);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUpTaskNode() {
        try {
            TableInfoVo ta = new TableInfoVo();
            ta.setPort("3307");
            ta.setHost("10.201.50.201");
            ta.setDatabase("test");
            ta.setTable("test05");
            ta.setGuid("目标");
            List<TableInfoVo> tableInfoVos = new ArrayList<>();
            tableInfoVos.add(ta);
            Set<LineageTrace> lineageTraceSet = new HashSet<>();
            List<TableLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
            List<TableInfoVo> tableInfoVosCache = new ArrayList<>();
            List<SimpleTaskNode> taskNodeCache = new ArrayList<>();
            HashMap<String, Integer> upDescMap = new HashMap<>();
            upDescMap.put(ta.getGuid(), 0);
            MetaDateRelationalDateService.upTaskNode(simpleTaskNodes, tableInfoVos, 3, lineageTraceSet, lineageEntities, tableInfoVosCache, taskNodeCache, upDescMap);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testDownTaskNode() {
        try {
            TableInfoVo ta = new TableInfoVo();
            ta.setPort("3307");
            ta.setHost("10.201.50.201");
            ta.setDatabase("test");
            ta.setTable("test05");
            ta.setGuid("目标");
            List<TableInfoVo> tableInfoVos = new ArrayList<>();
            tableInfoVos.add(ta);
            Set<LineageTrace> lineageTraceSet = new HashSet<>();
            List<TableLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
            List<TableInfoVo> tableInfoVosCache = new ArrayList<>();
            List<SimpleTaskNode> taskNodeCache = new ArrayList<>();
            HashMap<String, Integer> downDescMap = new HashMap<>();
            downDescMap.put(ta.getGuid(), 0);
            MetaDateRelationalDateService.downTaskNode(simpleTaskNodes, tableInfoVos, 3, lineageTraceSet, lineageEntities, tableInfoVosCache, taskNodeCache, downDescMap);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSetLineageTableInfoVo() {
        try {
            List<TableLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
            String descGuid = "yyds";
            MetaDateRelationalDateService.setLineageTableInfoVo(simpleTaskNodes.get(0), lineageEntities, descGuid);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testJudgeInfiniteLoop() {
        try {
            List<SimpleTaskNode> simpleTaskNodeCache = simpleTaskNodes.subList(0, 1);
            simpleTaskNodeCache.get(0).getInputTable().setGuid("test1");
            simpleTaskNodeCache.get(0).getOutputTable().setGuid("test2");
            MetaDateRelationalDateService.judgeInfiniteLoop(simpleTaskNodeCache, simpleTaskNodes.get(1));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testJudgeAlike() {
        try {
            MetaDateRelationalDateService.judgeAlike(simpleTaskNodes, simpleTaskNodes.get(1));
        } catch (Exception e) {
            fail();
        }
    }
}