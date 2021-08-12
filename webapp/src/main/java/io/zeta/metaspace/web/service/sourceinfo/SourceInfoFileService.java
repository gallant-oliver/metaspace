package io.zeta.metaspace.web.service.sourceinfo;

import com.google.common.base.Joiner;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.datasource.DataSourceTypeInfo;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.sourceinfo.AnalyticResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForDb;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.service.UserGroupService;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SourceInfoFileService {
    private final Logger logger  = LoggerFactory.getLogger(SourceInfoFileService.class);
    @Autowired
    private DatabaseDAO databaseDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private CategoryDAO categoryDao;
    @Autowired
    private SourceInfoDatabaseService sourceInfoDatabaseService;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private UserGroupService userGroupService;

    private final String[] validFields = new String[]{"数据层名称","数据库中文名","数据库类型","数据库英文名称",
            "数据库业务Owner姓名","数据库业务Owner部门名称","数据库业务Owner电子邮箱","手机号",
            "数据库技术Owner姓名","数据库技术Owner部门名称","数据库技术Owner电子邮箱","技术Owner手机号","技术负责人","业务负责人"};
    private Map<String,String> categoryMap  = new HashMap<String,String>();
  
    private String[] tableTitleAttr = {"数据层名称","数据库中文名","数据库类型","数据库实例","数据库英文名称","抽取频率","抽取工具","规划包编号","规划包名称",
            "是否保密","保密期限","是否重要","数据库业务Owner姓名","数据库业务Owner部门名称","数据库业务Owner电子邮箱","手机号","数据库技术Owner姓名","数据库技术Owner部门名称","数据库技术Owner电子邮箱","技术Owner手机号",
            "技术负责人","业务负责人"};

    /**
     * 数据源文件导入模板生成下载
     * @return
     */
    public File exportExcelTemplate(String tenantId){
        try {
            File templateFile = File.createTempFile("template", "."+PoiExcelUtils.XLSX);
            List<String> tableAttributes = Arrays.asList(tableTitleAttr);
            List<String> tableData = new ArrayList<>();
            //模板处理
            categoryMap = getCategoryFromDb(tenantId);
            if(categoryMap != null && !categoryMap.isEmpty()){
                tableData.add(Joiner.on(";").join(categoryMap.keySet()));
            }else{
                tableData.add("层级之间使用-分开");
            }
            tableData.add("数据库中文名");
            List<DataSourceTypeInfo> sourceTypeInfos = dataSourceService.getDataSourceType("dbr");
            if(CollectionUtils.isEmpty(sourceTypeInfos)){
                tableData.add("例如:ORACLE、MYSQL");
            }else{
                tableData.add(sourceTypeInfos.stream().map(v->v.getName()).collect(Collectors.joining(";")) );
            }
            tableData.add("ORACLE数据库类型需要实例");
            tableData.add("数据库的英文定义");
            tableData.add(""); //抽取频率
            tableData.add(""); //抽取工具
            tableData.add(""); //规划包编号
            tableData.add(""); //规划包name
            tableData.add("是;否"); //是否保密
            tableData.add("保密内容为是的话，则需要填写期限"); //保密期限
            tableData.add("是;否"); //"是否重要"
            //数据库业务
            tableData.add(""); //数据库业务Owner姓名
            tableData.add(""); //数据库业务Owner部门名称
            tableData.add(""); //数据库业务Owner电子邮箱
            tableData.add(""); //手机号
            //数据库技术
            tableData.add(""); //数据库技术Owner姓名
            tableData.add(""); //数据库技术Owner部门名称
            tableData.add(""); //数据库技术Owner电子邮箱
            tableData.add(""); //技术Owner手机号

            //"技术负责人","业务负责人"
            List<User> userList = userDAO.getAllUserByValid();
            String users = CollectionUtils.isEmpty(userList) ? "请输入有效的负责人名称" : userList.stream().map(v->v.getUsername()).collect(Collectors.joining(";"));
            tableData.add(users); //技术负责人
            tableData.add(users); //业务负责人
            Workbook wb = PoiExcelUtils.createExcelFileWithDropDown(tableAttributes,tableData,"sheet1");
            FileOutputStream output = new FileOutputStream(templateFile);
            wb.write(output);
            output.flush();
            output.close();
            return templateFile;
        } catch (IOException e) {
            logger.error("生成模板文件异常,{}",e);
            throw new RuntimeException("生成模板文件异常");
        }
    }
    /**
     * 校验excel导入必填字段
     * @param excelDataList
     */
    public List<AnalyticResult> checkExcelField(List<String[]> excelDataList){
        List<AnalyticResult> results = new ArrayList<>();

        String[] titleArray = excelDataList.get(0);
        Map<String,Integer> map = propertyToColumnIndexMap(titleArray);

        //循环遍历校验必填数据 以及中文名称格式
        for (int i = 1,len = excelDataList.size(); i < len;i++ ){
            String[] array = excelDataList.get(i);
            for(String fieldName : validFields){
                String v = getElementOrDefault(array,MapUtils.getIntValue(map,fieldName,-1));
                if(StringUtils.isBlank(v)){
                    String errMsg = "列名["+fieldName+"]的值为空";
                    results.add(setAnalyticResult(errMsg,array, map));
                }
                if("数据库中文名".equals(fieldName) && StringUtils.isNotBlank(v)){
                    if (v.length() > 128){
                        String errMsg = "数据库中文名超过128字符";
                        results.add(setAnalyticResult(errMsg,array, map));
                    }
                    if(!v.matches("^[a-zA-Z0-9_\u4e00-\u9fa5]+$")){
                        String errMsg = "数据库中文名只包含字母数据下划线和中文";
                        results.add(setAnalyticResult(errMsg,array, map));
                    }
                }
            }

        }

        return results;
    }

    private AnalyticResult setAnalyticResult(String errMsg,String[] array,Map<String,Integer> map){
        AnalyticResult analyticResult = new AnalyticResult();
        analyticResult.setDatabaseTypeName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库类型",-1) ));
        analyticResult.setDataSourceName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据源",-1)));
        analyticResult.setDatabaseInstanceName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库实例", -1)));
        analyticResult.setDatabaseName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库英文名称" ,-1)));
        analyticResult.setDatabaseAlias(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库中文名",-1)));
        analyticResult.setCategoryName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据层名称", -1 )));
        analyticResult.setErrorMessage(errMsg);
        return analyticResult;
    }
    /**
     * 获取存在重复名称或者不存在的库信息
     */
    private List<DatabaseInfoForDb> obtainRepeatAndUnExistedData(List<String[]> excelDataList,String tenantId,Map<String,Integer>  map,
                                                                 Map<String,List<String>> resultMap){
        List<String> unExistList = null;
        List<String> repeatNameList = null;
        int dbEnIndex = map.getOrDefault("数据库英文名称", -1);
        int dbZhIndex = map.getOrDefault("数据库中文名", -1);
        int categoryIndex = map.getOrDefault("数据层名称", -1);
        //获数据库英文名称   用于查询db-info表中存在的数据
        List<String> dbEnList = dbEnIndex == -1 ? new ArrayList<>()
                : excelDataList.stream().map(p->p[dbEnIndex]).collect(Collectors.toList());
        List<DatabaseInfoForDb> dbInfoExistList = CollectionUtils.isEmpty(dbEnList) ? new ArrayList<>()
                : databaseDAO.findExistDbName(dbEnList);

        //目录id@中文名格式 用于校验source-info表中是否存在
        List<DatabaseInfoForDb> result = new ArrayList<>(dbInfoExistList);
        if(dbZhIndex != -1){ //存在中文名字段
            List<String> searchDbZHList =  excelDataList.stream()
                    .map(p->p[dbZhIndex])
                    .collect(Collectors.toList());
            //根据租户id和中文查询sourceinfo表，再进一步根据目录筛选
            List<DatabaseInfoForDb> sourceInfoExistList = CollectionUtils.isEmpty(searchDbZHList) ? new ArrayList<>()
                    : databaseDAO.findSourceInfoByDbZHName(searchDbZHList,tenantId); //findDbInfoByDbName

            // 查看db-info是否存在，得出库是否存在
            if(CollectionUtils.isEmpty(dbInfoExistList)){
                logger.info("导入的源信息在 db-info 中都不存在");
                unExistList = dbEnList;
            }else{
                logger.info("导入的源信息在 db-info 存在部分缺失，进行筛选");
                unExistList = dbEnList.stream()
                        .filter(p->dbInfoExistList.stream().anyMatch(v->!p.equals(v.getDatabaseName())))
                        .collect(Collectors.toList());

                if(!CollectionUtils.isEmpty(sourceInfoExistList)){//source-info 表存在重复的数据
                    List<CategoryEntityV2> categoryEntityV2List = categoryDao.queryByTenantId(tenantId);
                    List<String> finalUnexistList = new ArrayList<>(unExistList);
                    List<String> dbZhList =  excelDataList.stream()
                            .filter(p->!finalUnexistList.contains(p[dbEnIndex])) //排除不存在的数据
                            .map(p->(categoryIndex == -1 ? "-1" : categoryMap.getOrDefault(p[categoryIndex],"-1"))+"@"+p[dbZhIndex])
                            .collect(Collectors.toList());

                    repeatNameList = dbZhList.stream()
                            .filter(p->sourceInfoExistList.stream().anyMatch(v->p.equals(v.getCategoryId()+"@"+v.getDatabaseAlias()) && tenantId.equalsIgnoreCase(v.getTenantId()))
                                    || categoryEntityV2List.stream().anyMatch(v->p.equalsIgnoreCase(v.getGuid()+"@"+v.getName())))
                            .collect(Collectors.toList());
                }

            }
        }

        if(!CollectionUtils.isEmpty(unExistList)){
            resultMap.put("unExistList",unExistList);
        }
        if(!CollectionUtils.isEmpty(repeatNameList)){
            resultMap.put("repeatNameList",repeatNameList);
        }

        return result;
    }

    private List<DatabaseInfoForDb> getExcludeExcelData(Map<String,Integer> map,List<String[]> excelDataList,String tenantId,
                                     List<String[]> excelRepeatDataList,Map<String,List<String[]>> resultMap){

        int dbEnIndex = map.getOrDefault("数据库英文名称", -1);
        int dbZhIndex = map.getOrDefault("数据库中文名", -1);
        int categoryIndex = map.getOrDefault("数据层名称", -1);
        //1. 文件内部比较重名 (根据数据库中文名)
        List<String> fileZhNameRepeat = new ArrayList<>();
        Map<String,List<String[]>> fileZhNameRepeatMap = dbZhIndex == -1 ? null
                : excelDataList.stream().collect(Collectors.groupingBy(p->p[dbZhIndex]));
        if(fileZhNameRepeatMap != null && !fileZhNameRepeatMap.isEmpty()){
            for(Map.Entry<String,List<String[]>> entry : fileZhNameRepeatMap.entrySet()){
                if(StringUtils.isNotBlank(entry.getKey()) && entry.getValue().size() > 1){
                    fileZhNameRepeat.add(entry.getKey());
                    excelRepeatDataList.add(entry.getValue().get(0));
                }
            }
        }
        // 排除内部重复的数据进行别的校验
        excelDataList = dbZhIndex == -1 ? excelDataList : excelDataList.stream()
                .filter(p->!fileZhNameRepeat.contains(p[dbZhIndex])).collect(Collectors.toList());

        Map<String,List<String>> resultMidMap = new HashMap<>();
        List<DatabaseInfoForDb> dbList = obtainRepeatAndUnExistedData(excelDataList,tenantId,map,resultMidMap);
        List<String> unExistList = resultMidMap.getOrDefault("unExistList",new ArrayList<>());
        List<String> repeatNameList = resultMidMap.getOrDefault("repeatNameList",new ArrayList<>());

        // 2. 不存在的数据库名
        if(!CollectionUtils.isEmpty(excelDataList)){
            List<String[]> unExistDbList =  excelDataList.stream().filter(p-> unExistList.contains(p[dbEnIndex])).collect(Collectors.toList());
            resultMap.put("unExistDbList",unExistDbList);
        }

        // 3. 源信息表中已存在的记录
        if(!CollectionUtils.isEmpty(excelDataList)){
            List<String[]> repeatDbList = excelDataList.stream()
                    .filter(p-> repeatNameList.contains((categoryIndex == -1 ? "-1" : categoryMap.getOrDefault(p[categoryIndex],"-1"))+"@"+p[dbZhIndex]))
                    .collect(Collectors.toList());
            resultMap.put("repeatDbList",repeatDbList);
        }

        return dbList;
    }
    //获取解析冲突结果
    public List<AnalyticResult> getFileParsedResult(List<String[]> excelDataList,String tenantId){
        List<String[]> excelRepeatDataList = new ArrayList<>();

        String[] titleArray = excelDataList.get(0);
        Map<String,Integer> map = propertyToColumnIndexMap(titleArray);
        categoryMap = getCategoryFromDb(tenantId);
        excelDataList.remove(0);

        Map<String,List<String[]>> resultMap = new HashMap<>();
        getExcludeExcelData(map,excelDataList, tenantId,
                excelRepeatDataList,resultMap);
        List<String[]> unExistDbList = resultMap.getOrDefault("unExistDbList",new ArrayList<>());
        List<String[]> repeatDbList = resultMap.getOrDefault("repeatDbList",new ArrayList<>());

        //返回比较结果
        List<AnalyticResult> results = new ArrayList<>();

        results.addAll(procesExcelResult(excelRepeatDataList,"文件内部数据中文名重复",map));
        results.addAll(procesExcelResult(unExistDbList,"数据库不存在",map));
        results.addAll(procesExcelResult(repeatDbList,"数据重复",map));
        return results;
    }

    private List<AnalyticResult> procesExcelResult(List<String[]> list,String errorMsg,Map<String,Integer> map){
        List<AnalyticResult> result = new ArrayList<>();
        if(CollectionUtils.isEmpty(list)){
            return result;
        }
        for(String[] array : list){
            result.add(setAnalyticResult(errorMsg,array, map));
        }
        return result;
    }

    private String getElementOrDefault(String[] array,int index){
        if(index > -1 && index < array.length){
            return array[index];
        }
        return "";
    }

    /**
     * 目录数据库获取顶级的目录名和guid
     * @return
     */
    private Map<String,String> getCategoryFromDb(String tenantId){
        if(categoryMap != null && !categoryMap.isEmpty()){
            return categoryMap;
        }
        Map<String,String> result = new HashMap<>();
        List<CategoryPrivilege> adminCategory = userGroupService.getAdminCategoryView(0, tenantId);
        //List<CategoryEntityV2> list = categoryDao.queryNameByType(0);
        if(CollectionUtils.isEmpty(adminCategory)){
            return result;
        }

        for(CategoryPrivilege item : adminCategory){
            String name = "";
            if(item.getLevel() == 1){
                name = item.getName();
            }else{//需要组装上级
                List<String> results = new ArrayList<>();
                results.add(item.getName());
                makeCategoryLevelName(adminCategory,item.getParentCategoryGuid(),results);
                Collections.reverse(results);
                name = Joiner.on("-").join(results);
            }
            result.put(name,item.getGuid());
        }
        return result;
    }

    private void makeCategoryLevelName(List<CategoryPrivilege> list,String parentGuid,List<String> results){
       // List<String> results = new ArrayList<>();
        Optional<CategoryPrivilege> itemOpt = list.stream().filter(p->parentGuid.equals(p.getGuid())).findFirst();
        if(itemOpt.isPresent()){
            CategoryPrivilege item = itemOpt.get();
            results.add(item.getName());
            if(item.getLevel() == 1){
                return;
            }else{
                makeCategoryLevelName(list,item.getParentCategoryGuid(),results);
            }
        }
        return;
    }
    private Map<String,Integer> propertyToColumnIndexMap( String[] array){
        Map<String,Integer> result = new HashMap<>();
        for(int i = 0,len = array.length; i < len;i++){
            //增加处理手机号的字段  模板有两个问题
            if("手机号".equals(array[i]) && result.containsKey("手机号") ){
                result.put("技术Owner手机号",i);
                continue;
            }
            result.put(array[i],i);
        }
        return result;
    }
    private List<String[]> removeArrayList(List<String[]> allList, List<String[]> excludeList,int... baseFieldIndex){
        List<String[]> result = new ArrayList<>();
        if(CollectionUtils.isEmpty(excludeList)){
            return allList;
        }
        List<String> filterList = new ArrayList<>();
        for(String[] strArr : excludeList){
            StringBuilder sb = new StringBuilder("");
            for(int i : baseFieldIndex){
                sb.append(i==-1 ? "-1":strArr[i]+"@");
            }
            filterList.add(sb.toString());
        }

        return allList.stream().filter(p->{
                    StringBuilder sb = new StringBuilder("");
                    for(int i : baseFieldIndex){
                        sb.append(i==-1 ? "-1":p[i]+"@");
                    }
                    return !filterList.contains(sb.toString());
                }).collect(Collectors.toList());
    }
    @Transactional
    public Result executeImportParsedResult(List<String[]> excelDataList,String annexId, String tenantId) {
        List<String[]> excelRepeatDataList = new ArrayList<>();
        String[] titleArray = excelDataList.get(0);
        Map<String,Integer> map = propertyToColumnIndexMap(titleArray);

        int dbEnIndex = map.getOrDefault("数据库英文名称", -1);
        int dbZhIndex = map.getOrDefault("数据库中文名", -1);
        int categoryIndex = map.getOrDefault("数据层名称", -1);
        categoryMap = getCategoryFromDb(tenantId);
        excelDataList.remove(0);

        Map<String,List<String[]>> resultMap = new HashMap<>();
        List<DatabaseInfoForDb> dbList = getExcludeExcelData(map,excelDataList, tenantId,
                excelRepeatDataList,resultMap);
        List<String[]> unExistDbList = resultMap.getOrDefault("unExistDbList",new ArrayList<>());
        List<String[]> repeatDbList = resultMap.getOrDefault("repeatDbList",new ArrayList<>());


        List<String[]> saveDbList =  removeArrayList(excelDataList,excelRepeatDataList,dbZhIndex);
        saveDbList = removeArrayList(saveDbList,unExistDbList,dbEnIndex);
        saveDbList = removeArrayList(saveDbList,repeatDbList,categoryIndex,dbZhIndex);

        //组装保存数据的参数
        if(CollectionUtils.isEmpty(saveDbList)){
            logger.info("没有要保存的数据库信息");
            return ReturnUtil.success();
        }

        List<DatabaseInfo> saveList = new ArrayList<>();
        DatabaseInfo databaseInfo = null;
        List<User> userList = userDAO.getAllUserByValid();
        for(String[] array : saveDbList){
            String sourceId = UUIDUtils.alphaUUID();
            String categoryId = MapUtils.getString(categoryMap,getElementOrDefault(array,MapUtils.getIntValue(map,"数据层名称",-1)),"");
            databaseInfo = new DatabaseInfo();
            databaseInfo.setId(sourceId);
            //databaseInfo.setAnnexId(annexId);
            databaseInfo.setCategoryId(categoryId);
            DatabaseInfoForDb databaseInfoForDb = null;
            if(dbEnIndex != -1){
                Optional<DatabaseInfoForDb> itemOpt = dbList.stream().filter(p->p.getDatabaseName().equalsIgnoreCase(array[dbEnIndex])).findFirst();
                if(itemOpt.isPresent()){
                    databaseInfoForDb = itemOpt.get();
                }
            }
            String databaseId = databaseInfoForDb != null ? databaseInfoForDb.getDatabaseId() : "";
            databaseInfo.setDatabaseId(databaseId);
            databaseInfo.setDataSourceId(databaseInfoForDb != null ? databaseInfoForDb.getSourceId() : "");
            databaseInfo.setDatabaseAlias(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库中文名",-1)));
            databaseInfo.setPlanningPackageCode(getElementOrDefault(array,MapUtils.getIntValue(map,"规划包编号",-1)));
            databaseInfo.setPlanningPackageName(getElementOrDefault(array,MapUtils.getIntValue(map,"规划包名称",-1)));
            databaseInfo.setExtractCycle(getElementOrDefault(array,MapUtils.getIntValue(map,"抽取频率",-1)));
            databaseInfo.setExtractTool(getElementOrDefault(array,MapUtils.getIntValue(map,"抽取工具",-1)));
            databaseInfo.setSecurity("是".equals(getElementOrDefault(array,MapUtils.getIntValue(map,"是否保密",-1))));
            databaseInfo.setSecurityCycle(getElementOrDefault(array,MapUtils.getIntValue(map,"保密期限",-1)));
            databaseInfo.setImportance("是".equals(getElementOrDefault(array,MapUtils.getIntValue(map,"是否重要",-1))));
           // databaseInfo.setCreator(username);
            databaseInfo.setBoName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner姓名",-1)));
            databaseInfo.setBoDepartmentName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner部门名称",-1)));
            databaseInfo.setBoTel(getElementOrDefault(array,MapUtils.getIntValue(map,"手机号",-1)));
            databaseInfo.setBoEmail(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner电子邮箱",-1)));
            databaseInfo.setToName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner姓名",-1)));
            databaseInfo.setToDepartmentName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner部门名称",-1)));
            databaseInfo.setToTel(getElementOrDefault(array,MapUtils.getIntValue(map,"技术Owner手机号",-1)));
            databaseInfo.setToEmail(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner电子邮箱",-1)));
            databaseInfo.setTechnicalLeader(convertUsernameToUserId(getElementOrDefault(array,MapUtils.getIntValue(map,"技术负责人",-1)),userList));
            databaseInfo.setBusinessLeader(convertUsernameToUserId(getElementOrDefault(array,MapUtils.getIntValue(map,"业务负责人",-1)),userList));
            //databaseInfo.setTenantId(tenantId);
            saveList.add(databaseInfo);
        }

        //批量保存处理
        Result result =  sourceInfoDatabaseService.addDatabaseInfoList(tenantId,saveList);
        //databaseInfoDAO.batchInsert(saveList);
        logger.info("文件导入处理完毕。");
        return result;
    }

    /**
     * 用户名转换为用户id ，找不到返回原始名
     * @param name
     * @param userList
     * @return
     */
    private String convertUsernameToUserId(String name,List<User> userList){
        if(CollectionUtils.isEmpty(userList)){
            return name;
        }

        Optional<User> user = userList.stream().filter(p-> StringUtils.equalsIgnoreCase(p.getUsername(),name))
                .findFirst();
        if(user == null || !user.isPresent()){
            return name;
        }

        return user.get().getUserId();
    }
}
