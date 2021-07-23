package io.zeta.metaspace.web.service.sourceinfo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SourceInfoFileService {
    //定义的excel表头内容
    private String[] excelTitles = new String[]{"数据层名称","数据库中文名","数据库类型","数据库实例",
            "数据库英文名称","抽取频率","抽取工具","规划包编号","规划包名称","是否保密","保密期限","是否重要",
            "数据库业务Owner姓名","数据库业务Owner部门名称","数据库业务Owner电子邮箱","手机号",
            "数据库技术Owner姓名","数据库技术Owner部门名称","数据库技术Owner电子邮箱","手机号","技术负责人","业务负责人"};

    //TODO
    public void getFileParsedResult(List<String[]> excelDataList){
        //获取标题头信息 找出各个索引
        String[] titleArray = excelDataList.get(0);
        Map<String,Integer> map = propertyToColumnIndexMap(titleArray);
        int dbEnIndex = map.get("数据库英文名称");
        //获数据库英文名称   用于查询存在或冲突的数据
        List<String> dbEnList = excelDataList.stream().map(p->p[dbEnIndex]).collect(Collectors.toList());
        //根据excel头信息获取每个字段的值

        //查看db-info是否存在，得出库是否存在
        List<String> unExistList = new ArrayList<>();
        //根据数据库名查询源信息表，得到是否冲突
        List<String> repeatNameList = new ArrayList<>();
        //返回比较结果
        excelDataList.stream().filter(p->unExistList.contains(p[dbEnIndex])).collect(Collectors.toList());
        excelDataList.stream().filter(p->repeatNameList.contains(p[dbEnIndex])).collect(Collectors.toList());

    }

    private Map<String,Integer> propertyToColumnIndexMap( String[] array){
        Map<String,Integer> result = new HashMap<>();
        for(int i = 0,len = array.length; i < len;i++){
            result.put(array[i],i);
        }
        return result;
    }
}
