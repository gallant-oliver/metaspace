package io.zeta.metaspace.web.service.sourceinfo;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.model.sourceinfo.AnalyticResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForDb;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SourceInfoFileService {
    private final Logger logger  = LoggerFactory.getLogger(SourceInfoFileService.class);
    @Autowired
    private DatabaseDAO databaseDAO;
    @Autowired
    private DatabaseInfoDAO databaseInfoDAO;

    private Map<String,String> categoryMap  = new HashMap<String,String>(){{
        put("贴源层","1");
        put("基础层","2");
        put("通用层","4");
        put("应用层","5");
    }};
   /* //定义的excel表头内容
    private String[] excelTitles = new String[]{"数据层名称","数据库中文名","数据库类型","数据库实例",
            "数据库英文名称","抽取频率","抽取工具","规划包编号","规划包名称","是否保密","保密期限","是否重要",
            "数据库业务Owner姓名","数据库业务Owner部门名称","数据库业务Owner电子邮箱","手机号",
            "数据库技术Owner姓名","数据库技术Owner部门名称","数据库技术Owner电子邮箱","手机号","技术负责人","业务负责人"};
*/
    private List<DatabaseInfoForDb> obtainRepeatAndUnExistedData(List<String[]> excelDataList,Map<String,Integer>  map,List<String> unExistList,List<String> repeatNameList){
        //获取标题头信息 找出各个索引
        String[] titleArray = excelDataList.get(0);
        map = propertyToColumnIndexMap(titleArray);
        int dbEnIndex = map.get("数据库英文名称");
        //获数据库英文名称   用于查询存在或冲突的数据
        List<String> dbEnList = excelDataList.stream().map(p->p[dbEnIndex]).collect(Collectors.toList());
        List<DatabaseInfoForDb> dbList = databaseDAO.findDbInfoByDbName(dbEnList);
        // 查看db-info是否存在，得出库是否存在
        // 根据数据库名查询源信息表，得到是否冲突
        if(CollectionUtils.isEmpty(dbList)){
            logger.info("数据库名检索的数据都不存在");
            unExistList = dbEnList;
        }else{
            unExistList = dbEnList.stream()
                    .filter(p->dbList.stream().anyMatch(v->!p.equals(v.getDatabaseName())))
                    .collect(Collectors.toList());
            repeatNameList = dbEnList.stream()
                    .filter(p->dbList.stream().anyMatch(v->p.equals(v.getDatabaseAlias())))
                    .collect(Collectors.toList());
        }

        return dbList;
    }
    //获取解析冲突结果
    public List<AnalyticResult> getFileParsedResult(List<String[]> excelDataList){
        //获取标题头信息 找出各个索引
        Map<String,Integer> map = new HashMap<>();
        List<String> unExistList = new ArrayList<>();
        List<String> repeatNameList = new ArrayList<>();
        obtainRepeatAndUnExistedData(excelDataList,map,unExistList,repeatNameList);

        //返回比较结果
        int dbEnIndex = map.get("数据库英文名称");
        List<AnalyticResult> results = new ArrayList<>();
        List<String> finalUnExistList = unExistList;
        List<String[]> unExistDbList =  excelDataList.stream().filter(p-> finalUnExistList.contains(p[dbEnIndex])).collect(Collectors.toList());
        List<String> finalRepeatNameList = repeatNameList;
        List<String[]> repeatDbList = excelDataList.stream().filter(p-> finalRepeatNameList.contains(p[dbEnIndex])).collect(Collectors.toList());
        results.addAll(procesExcelResult(unExistDbList,"数据库不存在",map));
        results.addAll(procesExcelResult(repeatDbList,"数据重复",map));

        return results;
    }

    private List<AnalyticResult> procesExcelResult(List<String[]> list,String errorMsg,Map<String,Integer> map){
        List<AnalyticResult> result = new ArrayList<>();
        if(CollectionUtils.isEmpty(list)){
            return result;
        }

        AnalyticResult analyticResult = null;
        for(String[] array : list){
            analyticResult = new AnalyticResult();
            analyticResult.setDatabaseTypeName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库类型",-1) ));
            analyticResult.setDataSourceName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据源",-1)));
            analyticResult.setDatabaseInstanceName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库实例", -1)));
            analyticResult.setDatabaseName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库英文名称" ,-1)));
            analyticResult.setDatabaseAlias(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库中文名",-1)));
            analyticResult.setCategoryName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据层名称", -1 )));
            analyticResult.setErrorMessage(errorMsg);

            result.add(analyticResult);
        }
        return result;
    }

    private String getElementOrDefault(String[] array,int index){
        if(index > -1 && index < array.length){
            return array[index];
        }
        return "";
    }
    private Map<String,Integer> propertyToColumnIndexMap( String[] array){
        Map<String,Integer> result = new HashMap<>();
        for(int i = 0,len = array.length; i < len;i++){
            result.put(array[i],i);
        }
        return result;
    }

    @Transactional
    public int executeImportParsedResult(List<String[]> excelDataList,String annexId, String tenantId) {
        //获取标题头信息 找出各个索引
        Map<String,Integer> map = new HashMap<>();
        List<String> unExistList = new ArrayList<>();
        List<String> repeatNameList = new ArrayList<>();
        List<DatabaseInfoForDb> dbList = obtainRepeatAndUnExistedData(excelDataList,map,unExistList,repeatNameList);

        int dbEnIndex = map.get("数据库英文名称");
        List<String[]> saveDbList =  excelDataList.stream()
                .filter(p-> !unExistList.contains(p[dbEnIndex]) && !repeatNameList.contains(p[dbEnIndex]))
                .collect(Collectors.toList());

        //组装保存数据的参数
        if(CollectionUtils.isEmpty(saveDbList)){
            logger.info("没有要保存的数据库信息");
            return 1;
        }
        User user = AdminUtils.getUserData();
        String username = user.getUsername();
        List<DatabaseInfoPO> saveList = new ArrayList<>();
        DatabaseInfoPO databaseInfo = null;
        for(String[] array : saveDbList){
            String sourceId = UUIDUtils.alphaUUID();
            String categoryId = MapUtils.getString(categoryMap,getElementOrDefault(array,MapUtils.getIntValue(map,"数据层名称",-1)),"");
            databaseInfo = new DatabaseInfoPO();
            databaseInfo.setId(sourceId);
            databaseInfo.setAnnexId(annexId);
            databaseInfo.setCategoryId(categoryId);
            DatabaseInfoForDb databaseInfoForDb = dbList.stream().filter(p->p.getDatabaseName().equalsIgnoreCase(array[dbEnIndex]))
                    .findFirst().orElse(null);
            String databaseId = databaseInfoForDb != null ? databaseInfoForDb.getDatabaseId() : "";
            databaseInfo.setDatabaseId(databaseId);
            databaseInfo.setDatabaseAlias(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库中文名",-1)));
            databaseInfo.setPlanningPackageCode(getElementOrDefault(array,MapUtils.getIntValue(map,"规划包编号",-1)));
            databaseInfo.setPlanningPackageCode(getElementOrDefault(array,MapUtils.getIntValue(map,"规划包名称",-1)));
            databaseInfo.setExtractCycle(getElementOrDefault(array,MapUtils.getIntValue(map,"抽取频率",-1)));
            databaseInfo.setExtractTool(getElementOrDefault(array,MapUtils.getIntValue(map,"抽取工具",-1)));
            databaseInfo.setSecurity("是".equals(getElementOrDefault(array,MapUtils.getIntValue(map,"是否保密",-1))));
            databaseInfo.setSecurityCycle(getElementOrDefault(array,MapUtils.getIntValue(map,"保密期限",-1)));
            databaseInfo.setImportance("是".equals(getElementOrDefault(array,MapUtils.getIntValue(map,"是否重要",-1))));
            databaseInfo.setCreator(username);
            databaseInfo.setStatus("0");
            databaseInfo.setBoName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner姓名",-1)));
            databaseInfo.setBoDepartmentName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner部门名称",-1)));
            databaseInfo.setBoTel(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner手机号",-1)));
            databaseInfo.setBoEmail(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner姓名",-1)));
            databaseInfo.setToName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner姓名",-1)));
            databaseInfo.setToDepartmentName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner部门名称",-1)));
            databaseInfo.setToTel(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner手机号",-1)));
            databaseInfo.setToEmail(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner电子邮箱",-1)));
            databaseInfo.setTechnicalLeader(getElementOrDefault(array,MapUtils.getIntValue(map,"技术负责人",-1)));
            databaseInfo.setBusinessLeader(getElementOrDefault(array,MapUtils.getIntValue(map,"业务负责人",-1)));
            databaseInfo.setTenantId(tenantId);
            saveList.add(databaseInfo);
        }

        //批量保存处理
        databaseInfoDAO.batchInsert(saveList);
        logger.info("文件导入处理完毕。");
        return 1;
    }
}
