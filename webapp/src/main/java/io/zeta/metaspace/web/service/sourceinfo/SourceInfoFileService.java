package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.sourceinfo.AnalyticResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForDb;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    //定义的excel表头内容
    private String[] excelTitles = new String[]{"数据层名称","数据库中文名","数据库类型","数据库实例",
            "数据库英文名称","抽取频率","抽取工具","规划包编号","规划包名称","是否保密","保密期限","是否重要",
            "数据库业务Owner姓名","数据库业务Owner部门名称","数据库业务Owner电子邮箱","手机号",
            "数据库技术Owner姓名","数据库技术Owner部门名称","数据库技术Owner电子邮箱","手机号","技术负责人","业务负责人"};

    //获取解析冲突结果
    public List<AnalyticResult> getFileParsedResult(List<String[]> excelDataList){
        //获取标题头信息 找出各个索引
        String[] titleArray = excelDataList.get(0);
        Map<String,Integer> map = propertyToColumnIndexMap(titleArray);
        int dbEnIndex = map.get("数据库英文名称");
        //获数据库英文名称   用于查询存在或冲突的数据
        List<String> dbEnList = excelDataList.stream().map(p->p[dbEnIndex]).collect(Collectors.toList());
        List<DatabaseInfoForDb> dbList = databaseDAO.findDbInfoByDbName(dbEnList);
        // 查看db-info是否存在，得出库是否存在
        // 根据数据库名查询源信息表，得到是否冲突
        List<String> unExistList = new ArrayList<>();
        List<String> repeatNameList = new ArrayList<>();
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


        //返回比较结果
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
            analyticResult.setDatabaseTypeName(map.get("数据库类型") == null ? "" : array[map.get("数据库类型")]);
            analyticResult.setDataSourceName(map.get("数据源") == null ? "" : array[map.get("数据源")]);
            analyticResult.setDatabaseInstanceName(map.get("数据库实例") == null ? "" : array[map.get("数据库实例")]);
            analyticResult.setDatabaseName(map.get("数据库英文名称") == null ? "" : array[map.get("数据库英文名称")]);
            analyticResult.setDatabaseAlias(map.get("数据库中文名") == null ? "" : array[map.get("数据库中文名")]);
            analyticResult.setCategoryName(map.get("数据层名称") == null ? "" : array[map.get("数据层名称")]);
            analyticResult.setErrorMessage(errorMsg);

            result.add(analyticResult);
        }
        return result;
    }
    private Map<String,Integer> propertyToColumnIndexMap( String[] array){
        Map<String,Integer> result = new HashMap<>();
        for(int i = 0,len = array.length; i < len;i++){
            result.put(array[i],i);
        }
        return result;
    }
}
