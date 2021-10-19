package io.zeta.metaspace.web.service.sourceinfo;

import com.google.common.base.Joiner;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourceTypeInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.AnalyticResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForDb;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.service.UserGroupService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
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

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SourceInfoFileService {
    private final Logger logger  = LoggerFactory.getLogger(SourceInfoFileService.class);
    @Autowired
    private DatabaseDAO databaseDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CategoryDAO categoryDao;
    @Autowired
    private DataSourceDAO dataSourceDAO;
    @Autowired
    private SourceInfoDatabaseService sourceInfoDatabaseService;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private UserGroupService userGroupService;
    /*
     * 正则表达式：验证手机号
     */
    public static final String REGEX_MOBILE = "^(1[3-9])\\d{9}$";

    /*
     * 正则表达式：验证邮箱
     */
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";


    private final String[] validFields = new String[]{"数据层名称","数据库中文名","数据库类型","数据源","数据库英文名称",
            "数据库业务Owner姓名","数据库业务Owner部门名称","数据库业务Owner电子邮箱","业务Owner手机号",
            "数据库技术Owner姓名","数据库技术Owner部门名称","数据库技术Owner电子邮箱","技术Owner手机号","技术负责人","业务负责人"};
    private Map<String,String> categoryMap  = new HashMap<String,String>();
  
    private String[] tableTitleAttr = {"数据层名称","数据库中文名","数据库类型","数据源","数据库实例","数据库英文名称","抽取频率","抽取工具","规划包编号","规划包名称",
            "是否保密","保密期限","是否重要","描述","数据库业务Owner姓名","数据库业务Owner部门名称","数据库业务Owner电子邮箱","业务Owner手机号","数据库技术Owner姓名","数据库技术Owner部门名称","数据库技术Owner电子邮箱","技术Owner手机号",
            "技术负责人","业务负责人"};
    private static final int CHINA_LENGTH = 128;
    private static final int EMAIL_LENGTH = 64;
    /**
     * 校验手机号
     *
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */
    public boolean isMobile(String mobile) {
        if(StringUtils.isBlank(mobile)){
            return false;
        }
        return Pattern.matches(REGEX_MOBILE, mobile);
    }

    /**
     * 校验邮箱
     *
     * @param email
     * @return 校验通过返回true，否则返回false
     */
    public  boolean isEmail(String email) {
        if(StringUtils.isBlank(email)){
            return false;
        }
        return Pattern.matches(REGEX_EMAIL, email);
    }

    /**
     * 数据源文件导入模板生成下载
     * @return
     */
    public Workbook exportExcelTemplate(String tenantId){
       // File templateFile = File.createTempFile("template", "."+PoiExcelUtils.XLSX);
        List<String> tableAttributes = Arrays.asList(tableTitleAttr);
        List<Object> tableData = new ArrayList<>();
        //模板处理
        categoryMap = getCategoryFromDb(tenantId);
        if(categoryMap != null && !categoryMap.isEmpty()){
            tableData.add(new ArrayList<>(categoryMap.keySet()));
        }else{
            tableData.add("层级之间使用-分开");
        }
        tableData.add("数据库中文名");
        List<DataSourceTypeInfo> sourceTypeInfos = dataSourceService.getDataSourceType("dbr");
        List<DataSourceInfo> dataSourceInfos = null;
        if(CollectionUtils.isEmpty(sourceTypeInfos)){
            tableData.add("例如:ORACLE、MYSQL");
        }else{
            List<String> sourceTypeList = sourceTypeInfos.stream().map(v->v.getName()).collect(Collectors.toList());
            tableData.add(sourceTypeList ); //Joiner.on(";").join(sourceTypeList)
            dataSourceInfos = getDataSource(sourceTypeList,tenantId);
        }
        Map<String, Set<String>> dataSourceMap = CollectionUtils.isEmpty(dataSourceInfos) ? null
                : dataSourceInfos.stream().collect(Collectors.groupingBy(DataSourceInfo::getSourceType,
                Collectors.mapping(DataSourceInfo::getSourceName,Collectors.toSet())));
        tableData.add(dataSourceMap);//数据源
        tableData.add("ORACLE数据库类型需要实例");
        tableData.add("数据库的英文定义");
        tableData.add(""); //抽取频率
        tableData.add(""); //抽取工具
        tableData.add(""); //规划包编号
        tableData.add(""); //规划包name
        tableData.add(Arrays.asList("是","否")); //是否保密
        tableData.add("保密内容为是的话，则需要填写期限"); //保密期限
        tableData.add(Arrays.asList("是","否")); //"是否重要"
        tableData.add("");//描述
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
       /* List<User> userList = userDAO.getAllUserByValid();
        List<String> users = CollectionUtils.isEmpty(userList) ? null : userList.stream().map(v->v.getUsername()).collect(Collectors.toList());*/
        List<String> users = getUsers(tenantId);
        tableData.add(users); //技术负责人
        tableData.add(users); //业务负责人
        logger.info("开始生成模板文件...");
        Workbook wb = PoiExcelUtils.createExcelFileWithDropDown(tableAttributes,tableData,"sheet1");
        logger.info("生成模板文件ok...");
        return wb;
       /* FileOutputStream output = new FileOutputStream(templateFile);
        wb.write(output);
        output.flush();
        output.close();
        return templateFile;*/
    }
    private List<String> getUsers(String tenantId){
        Parameters parameters = new Parameters();
        parameters.setQuery("");
        parameters.setLimit(-1);
        parameters.setOffset(0);
        PageResult<User> pageResult = usersService.getUserListV2(tenantId,parameters);
        List<User> userList = pageResult.getLists();
        if(CollectionUtils.isEmpty(userList)){
            return null;
        }

        return userList.stream().map(User::getUsername).collect(Collectors.toList());
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
                    break;
                }
                if("数据库类型".equals(fieldName) && StringUtils.isNotBlank(v)
                        && "oracle".equalsIgnoreCase(v) && StringUtils.isBlank(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库实例",-1))) ){
                    String errMsg = "数据库类型oracle的数据库实例不能为空";
                    results.add(setAnalyticResult(errMsg,array, map));
                    break;
                }
                if("数据库中文名".equals(fieldName) && StringUtils.isNotBlank(v)){
                    if (v.length() > CHINA_LENGTH){
                        String errMsg = "数据库中文名超过"+CHINA_LENGTH+"字符";
                        results.add(setAnalyticResult(errMsg,array, map));
                        break;
                    }
                    if(!v.matches("^[a-zA-Z0-9_\u4e00-\u9fa5]+$")){
                        String errMsg = "数据库中文名只包含字母数据下划线和中文";
                        results.add(setAnalyticResult(errMsg,array, map));
                        break;
                    }
                }

                if( ("数据库业务Owner电子邮箱".equals(fieldName) || "数据库技术Owner电子邮箱".equals(fieldName) )
                        && StringUtils.isNotBlank(v) ){
                    String errMsg = "";
                    if(v.length() > EMAIL_LENGTH){
                        errMsg = fieldName+"超过"+EMAIL_LENGTH+"字符";
                    }
                    if(!isEmail(v)){
                        errMsg = fieldName+"输入格式不正常";
                    }
                    if(StringUtils.isNotBlank(errMsg)){
                        results.add(setAnalyticResult(errMsg,array, map));
                        break;
                    }

                }

                if( ("业务Owner手机号".equals(fieldName) || "技术Owner手机号".equals(fieldName) )
                        && StringUtils.isNotBlank(v) && !isMobile(v)){
                    String errMsg = fieldName+"输入格式不正常";
                    results.add(setAnalyticResult(errMsg,array, map));
                    break;
                }
            }

        }

        return results;
    }

    private AnalyticResult setAnalyticResult(String msgType,String[] array,Map<String,Integer> map){
        AnalyticResult analyticResult = new AnalyticResult();
        String dbType = getElementOrDefault(array,MapUtils.getIntValue(map,"数据库类型",-1) );
        analyticResult.setDatabaseTypeName(dbType);
        analyticResult.setDataSourceName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据源",-1)));
        analyticResult.setDatabaseInstanceName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库实例", -1)));
        analyticResult.setDatabaseName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库英文名称" ,-1)));
        analyticResult.setDatabaseAlias(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库中文名",-1)));
        analyticResult.setCategoryName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据层名称", -1 )));
        String errMsg = "";

        if("1".equals(msgType)){
            errMsg = "excel文件的数据库中文名存在重复";
        } else if("2".equals(msgType)){
            errMsg = "数据库类型["+dbType+"]下的数据库英文名不存在";
        }else  if("3".equals(msgType)){
            errMsg = "数据库类型["+dbType+"]下的数据库名已登记";
        }else{
            errMsg = msgType;
        }
        analyticResult.setErrorMessage(errMsg);
        return analyticResult;
    }
    /**
     * 获取存在重复名称或者不存在的库信息
     */
    private List<DatabaseInfoForDb> obtainRepeatAndUnExistedData(List<String[]> excelDataList,String tenantId,Map<String,Integer>  map,
                                                                 Map<String,List<String>> resultMap){
        List<String> unExistList = null;
        List<String> repeatInCategoryList = null;
        List<String> repeatInSourceInfoList = null;
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
                    : databaseDAO.findSourceInfoByDbZHName(searchDbZHList,null,tenantId); //findDbInfoByDbName

            // 查看db-info是否存在，得出库是否存在
            if(CollectionUtils.isEmpty(dbInfoExistList)){
                logger.info("导入的源信息在 db-info 中都不存在");
                unExistList = dbEnList;
            }else{
                logger.info("导入的源信息在 db-info 存在部分缺失，进行筛选");
                List<String> existDbName = dbInfoExistList.stream().map(DatabaseInfoForDb::getDatabaseName).collect(Collectors.toList());
                unExistList = dbEnList.stream()
                        .filter(p->!existDbName.contains(p))
                        .collect(Collectors.toList());

                List<String> finalUnExistList = new ArrayList<>(unExistList);
                List<CategoryEntityV2> categoryEntityV2List = categoryDao.queryByTenantId(tenantId);
                if(!CollectionUtils.isEmpty(categoryEntityV2List)){
                    List<String> dbZhList =  excelDataList.stream()
                            .filter(p->!finalUnExistList.contains(p[dbEnIndex])) //排除不存在的数据
                            .map(p->(categoryIndex == -1 ? "-1" : categoryMap.getOrDefault(p[categoryIndex],"-1"))+"@"+p[dbZhIndex])
                            .collect(Collectors.toList());
                    repeatInCategoryList = dbZhList.stream()
                            .filter(p->categoryEntityV2List.stream().anyMatch(v->p.equalsIgnoreCase(v.getGuid()+"@"+v.getName())))
                            .collect(Collectors.toList());
                }

                if(!CollectionUtils.isEmpty(sourceInfoExistList)){//source-info 表存在中文名或者英文名重复的数据
                    /*List<String> dbZhList =  excelDataList.stream()
                            .filter(p->!finalUnExistList.contains(p[dbEnIndex])) //排除不存在的数据
                            .map(p->p[dbZhIndex])
                            .collect(Collectors.toList());

                    repeatInSourceInfoList = dbZhList.stream()
                            .filter(p->sourceInfoExistList.stream().anyMatch(v->p.equals(v.getDatabaseAlias()) ) )
                            .collect(Collectors.toList());*/

                    repeatInSourceInfoList = excelDataList.stream()
                            .filter(p->!finalUnExistList.contains(p[dbEnIndex]) &&
                                    sourceInfoExistList.stream().anyMatch(v->p[dbZhIndex].equals(v.getDatabaseAlias()) || p[dbEnIndex].equalsIgnoreCase(v.getDatabaseName()) ) )
                            .map(p->p[dbZhIndex])
                            .collect(Collectors.toList());
                }

            }
        }

        if(!CollectionUtils.isEmpty(unExistList)){
            resultMap.put("unExistList",unExistList);
        }
        if(!CollectionUtils.isEmpty(repeatInCategoryList)){
            resultMap.put("repeatInCategoryList",repeatInCategoryList);
        }
        if(!CollectionUtils.isEmpty(repeatInSourceInfoList)){
            resultMap.put("repeatInSourceInfoList",repeatInSourceInfoList);
        }

        return result;
    }

    /*
     * 处理校验不符合的数据，主要包含以下：
     *  内部文件中文重名、数据库英文名不存在（某个数据库下）、数据库中文或者英文名重复（某个数据库的数据源下）
     *
     */
    private List<DatabaseInfoForDb> getExcludeExcelData(Map<String,Integer> excelHeadMap,List<String[]> excelDataList,String tenantId,
                                                        List<String[]> excelRepeatDataList,Map<String,List<String[]>> resultMap){
        //1. 文件内部比较重名 (根据数据库中文名)
        int dbZhIndex = excelHeadMap.getOrDefault("数据库中文名", -1);
        List<String> fileInnerNameRepeat = new ArrayList<>();
        Map<String,List<String[]>> fileZhNameRepeatMap = dbZhIndex == -1 ? null
                : excelDataList.stream().collect(Collectors.groupingBy(p->p[dbZhIndex]));
        if(fileZhNameRepeatMap != null && !fileZhNameRepeatMap.isEmpty()){
            for(Map.Entry<String,List<String[]>> entry : fileZhNameRepeatMap.entrySet()){
                if(StringUtils.isNotBlank(entry.getKey()) && entry.getValue().size() > 1){
                    fileInnerNameRepeat.add(entry.getKey());
                    excelRepeatDataList.add(entry.getValue().get(0));
                }
            }
        }

        excelDataList = dbZhIndex == -1 ? excelDataList : excelDataList.stream()
                .filter(p->!fileInnerNameRepeat.contains(p[dbZhIndex])).collect(Collectors.toList());
        // 2. 不存在的数据库名
        int dbEnIndex = excelHeadMap.getOrDefault("数据库英文名称", -1);
        int dbTypeIndex = excelHeadMap.getOrDefault("数据库类型", -1);
        //获数据库英文名称   用于查询db-info表中存在的数据
        List<String> dbEnList = dbEnIndex == -1 ? new ArrayList<>()
                : excelDataList.stream().map(p->p[dbEnIndex]).collect(Collectors.toList());
        List<DatabaseInfoForDb> dbInfoExistList = CollectionUtils.isEmpty(dbEnList) ? new ArrayList<>()
                : databaseDAO.findExistDbName(dbEnList);
        if(CollectionUtils.isEmpty(dbInfoExistList)){
            logger.info("导入的excel源信息在 db-info 中都不存在");
            resultMap.put("unExistDbList",excelDataList);
        }else{
            // 判断具体数据库类型下的英文名是否存在
            List<String[]> unExistExcelInfo = CollectionUtils.isEmpty(excelDataList) ? new ArrayList<>() : excelDataList.stream()
                    .filter( p->dbInfoExistList.stream().filter(v->!(v.getDbType().equalsIgnoreCase(p[dbTypeIndex]) && v.getDatabaseName().equalsIgnoreCase(p[dbEnIndex]))).count() ==0 )
                    .collect(Collectors.toList());
            resultMap.put("unExistDbList",unExistExcelInfo);

            excelDataList = CollectionUtils.isEmpty(excelDataList) ? new ArrayList<>() : excelDataList.stream()
                    .filter( p->dbInfoExistList.stream().anyMatch(v->v.getDbType().equalsIgnoreCase(p[dbTypeIndex]) && v.getDatabaseName().equalsIgnoreCase(p[dbEnIndex])) )
                    .collect(Collectors.toList());

            //3. 目录中重复
            int categoryIndex = excelHeadMap.getOrDefault("数据层名称", -1);
            List<CategoryEntityV2> categoryEntityV2List = categoryDao.queryByTenantId(tenantId);
            if(categoryIndex != -1 && !CollectionUtils.isEmpty(categoryEntityV2List)){
               List<String[]> repeatInCategoryList =  CollectionUtils.isEmpty(excelDataList) ? new ArrayList<>() : excelDataList.stream().filter(p->categoryEntityV2List.stream()
                        .anyMatch(v->v.getGuid().equals(categoryMap.getOrDefault(p[categoryIndex],"-1")) && v.getName().equals(p[dbZhIndex]))
                ).collect(Collectors.toList());
                resultMap.put("repeatInCategoryList",repeatInCategoryList);
            }

            excelDataList =  CollectionUtils.isEmpty(excelDataList) ? new ArrayList<>() : excelDataList.stream().filter(p->categoryEntityV2List.stream()
                    .anyMatch(v->!(v.getGuid().equals(categoryMap.getOrDefault(p[categoryIndex],"-1")) && v.getName().equals(p[dbZhIndex])))
            ).collect(Collectors.toList());

            //4. 数据库中文或者英文名重复 SOURCE_INFO 表
            List<String> searchDbZHList =  excelDataList.stream()
                    .map(p->p[dbZhIndex])
                    .collect(Collectors.toList());
            List<String> searchDbEnList =  excelDataList.stream()
                    .map(p->p[dbEnIndex])
                    .collect(Collectors.toList());

            List<DatabaseInfoForDb> sourceInfoExistList = CollectionUtils.isEmpty(searchDbZHList) ? new ArrayList<>()
                    : databaseDAO.findSourceInfoByDbZHName(searchDbZHList,searchDbEnList,tenantId);
            List<String[]> repeatInSourceInfoList =  CollectionUtils.isEmpty(excelDataList) ? new ArrayList<>() : excelDataList.stream().filter(p->sourceInfoExistList.stream()
                    .anyMatch( v-> v.getDbType().equals(p[dbTypeIndex])
                            && (v.getDatabaseName().equals(p[dbEnIndex]) || v.getDatabaseAlias().equals(p[dbZhIndex])) )
            ).collect(Collectors.toList());
            resultMap.put("repeatInSourceInfoList",repeatInSourceInfoList);
        }

        return dbInfoExistList;
    }
    private List<DatabaseInfoForDb> getExcludeExcelData_old(Map<String,Integer> map,List<String[]> excelDataList,String tenantId,
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
        List<String> repeatInCategoryList = resultMidMap.getOrDefault("repeatInCategoryList",new ArrayList<>());
        List<String> repeatInSourceInfoList = resultMidMap.getOrDefault("repeatInSourceInfoList",new ArrayList<>());

        // 2. 不存在的数据库名
        if(!CollectionUtils.isEmpty(excelDataList)){
            List<String[]> unExistDbList =  excelDataList.stream().filter(p-> unExistList.contains(p[dbEnIndex])).collect(Collectors.toList());
            resultMap.put("unExistDbList",unExistDbList);
        }

        // 3. 源信息表中已存在的记录
        if(!CollectionUtils.isEmpty(excelDataList)){
            List<String[]> repeatDbList = excelDataList.stream()
                    .filter(p-> repeatInCategoryList.contains((categoryIndex == -1 ? "-1" : categoryMap.getOrDefault(p[categoryIndex],"-1"))+"@"+p[dbZhIndex])
                    || repeatInSourceInfoList.contains(p[dbZhIndex]) )
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
        List<String[]> repeatInCategoryList = resultMap.getOrDefault("repeatInCategoryList",new ArrayList<>());
        List<String[]> repeatInSourceInfoList = resultMap.getOrDefault("repeatInSourceInfoList",new ArrayList<>());

        List<String[]> repeatDbList = new ArrayList<>();
        repeatDbList.addAll(repeatInCategoryList);
        repeatDbList.addAll(repeatInSourceInfoList);
        //返回比较结果
        List<AnalyticResult> results = new ArrayList<>();

        results.addAll(procesExcelResult(excelRepeatDataList,"1",map));
        results.addAll(procesExcelResult(unExistDbList,"2",map));
        results.addAll(procesExcelResult(repeatDbList,"3",map));
        return results;
    }

    private List<AnalyticResult> procesExcelResult(List<String[]> list,String msgType,Map<String,Integer> map){
        List<AnalyticResult> result = new ArrayList<>();
        if(CollectionUtils.isEmpty(list)){
            return result;
        }
        for(String[] array : list){
            result.add(setAnalyticResult(msgType,array, map));
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
        /*if(categoryMap != null && !categoryMap.isEmpty()){
            return categoryMap;
        }*/
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
        int dbTypeIndex = map.getOrDefault("数据库类型", -1);
        categoryMap = getCategoryFromDb(tenantId);
        excelDataList.remove(0);

        Map<String,List<String[]>> resultMap = new HashMap<>();
        List<DatabaseInfoForDb> dbList = getExcludeExcelData(map,excelDataList, tenantId,
                excelRepeatDataList,resultMap);
        /*List<String[]> unExistDbList = resultMap.getOrDefault("unExistDbList",new ArrayList<>());
        List<String[]> repeatDbList = resultMap.getOrDefault("repeatDbList",new ArrayList<>());*/

        List<String[]> unExistDbList = resultMap.getOrDefault("unExistDbList",new ArrayList<>());
        List<String[]> repeatInCategoryList = resultMap.getOrDefault("repeatInCategoryList",new ArrayList<>());
        List<String[]> repeatInSourceInfoList = resultMap.getOrDefault("repeatInSourceInfoList",new ArrayList<>());



        List<String[]> saveDbList =  removeArrayList(excelDataList,excelRepeatDataList,dbZhIndex);
        saveDbList = removeArrayList(saveDbList,unExistDbList,dbTypeIndex,dbEnIndex);
        saveDbList = removeArrayList(saveDbList,repeatInCategoryList,categoryIndex,dbZhIndex);
        saveDbList = removeArrayList(saveDbList,repeatInSourceInfoList,dbZhIndex);
        saveDbList = removeArrayList(saveDbList,repeatInSourceInfoList,dbEnIndex);

        //组装保存数据的参数
        if(CollectionUtils.isEmpty(saveDbList)){
            logger.info("没有要保存的数据库信息");
            return ReturnUtil.success();
        }

        List<DatabaseInfo> saveList = new ArrayList<>();
        DatabaseInfo databaseInfo = null;
        List<User> userList = userDAO.getAllUserByValid();
        //int dbTypeIndex = map.getOrDefault("数据库类型", -1);
        List<DataSourceTypeInfo> sourceTypeInfos = dataSourceService.getDataSourceType("dbr");
        List<DataSourceInfo> dataSourceInfos = null;
        if(!CollectionUtils.isEmpty(sourceTypeInfos)){
            List<String> sourceTypeList = sourceTypeInfos.stream().map(v->v.getName()).collect(Collectors.toList());
            dataSourceInfos = getDataSource(sourceTypeList,tenantId);
        }

        for(String[] array : saveDbList){
            String sourceId = UUIDUtils.alphaUUID();
            String categoryId = MapUtils.getString(categoryMap,getElementOrDefault(array,MapUtils.getIntValue(map,"数据层名称",-1)),"");
            databaseInfo = new DatabaseInfo();
            databaseInfo.setId(sourceId);
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
            String dbSourceId = "";
            String dbType = getElementOrDefault(array,MapUtils.getIntValue(map,"数据库类型",-1));
            if("oracle".equalsIgnoreCase(dbType) ){
                String instance = getElementOrDefault(array,MapUtils.getIntValue(map,"数据库实例",-1));
                if(StringUtils.isBlank(instance)){
                    return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                            AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("oracel数据库类型下的数据库实例"));
                }
            }

            String dbSourceName = getElementOrDefault(array,MapUtils.getIntValue(map,"数据源",-1));
            if(!CollectionUtils.isEmpty(dataSourceInfos)) {
                Optional<DataSourceInfo> itemOpt =  dataSourceInfos.stream().filter(p->p.getSourceType().equalsIgnoreCase(dbType)
                        && p.getSourceName().equalsIgnoreCase(dbSourceName)).findFirst();
                if(itemOpt.isPresent()){
                    dbSourceId = itemOpt.get().getSourceId();
                }
            }
            if(StringUtils.isBlank(dbSourceId)){
                return new Result("400","数据源内容输入不符合，请检查");
            }
            databaseInfo.setDataSourceId(dbSourceId);
            databaseInfo.setDatabaseAlias(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库中文名",-1)));
            databaseInfo.setPlanningPackageCode(getElementOrDefault(array,MapUtils.getIntValue(map,"规划包编号",-1)));
            databaseInfo.setPlanningPackageName(getElementOrDefault(array,MapUtils.getIntValue(map,"规划包名称",-1)));
            databaseInfo.setExtractCycle(getElementOrDefault(array,MapUtils.getIntValue(map,"抽取频率",-1)));
            databaseInfo.setExtractTool(getElementOrDefault(array,MapUtils.getIntValue(map,"抽取工具",-1)));
            databaseInfo.setSecurity("是".equals(getElementOrDefault(array,MapUtils.getIntValue(map,"是否保密",-1))));
            databaseInfo.setSecurityCycle(getElementOrDefault(array,MapUtils.getIntValue(map,"保密期限",-1)));
            databaseInfo.setImportance("是".equals(getElementOrDefault(array,MapUtils.getIntValue(map,"是否重要",-1))));
            databaseInfo.setDescription(getElementOrDefault(array,MapUtils.getIntValue(map,"描述",-1)));
           // databaseInfo.setCreator(username);

            databaseInfo.setBoName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner姓名",-1)));
            databaseInfo.setBoDepartmentName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner部门名称",-1)));
            String bizMobile = getElementOrDefault(array,MapUtils.getIntValue(map,"业务Owner手机号",-1));
            if(!isMobile(bizMobile)){
                return new Result("400","业务Owner手机号输入格式错误");
            }
            databaseInfo.setBoTel(bizMobile);
            String bizEmail = getElementOrDefault(array,MapUtils.getIntValue(map,"数据库业务Owner电子邮箱",-1));
            if(!isEmail(bizEmail)){
                return new Result("400","数据库业务Owner电子邮箱输入格式错误");
            }
            databaseInfo.setBoEmail(bizEmail);
            databaseInfo.setToName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner姓名",-1)));
            databaseInfo.setToDepartmentName(getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner部门名称",-1)));
            String techMobile = getElementOrDefault(array,MapUtils.getIntValue(map,"技术Owner手机号",-1));
            if(!isMobile(techMobile)){
                return new Result("400","技术Owner手机号输入格式错误");
            }
            databaseInfo.setToTel(techMobile);
            String techEmail = getElementOrDefault(array,MapUtils.getIntValue(map,"数据库技术Owner电子邮箱",-1));
            if(!isEmail(techEmail)){
                return new Result("400","数据库技术Owner电子邮箱输入格式错误");
            }
            databaseInfo.setToEmail(techEmail);
            String techLeader = convertUsernameToUserId(getElementOrDefault(array,MapUtils.getIntValue(map,"技术负责人",-1)),userList);
            if (StringUtils.isBlank(techLeader)){
                return new Result("400","技术负责人输入错误");
            }
            databaseInfo.setTechnicalLeader(techLeader);
            String bizLeader = convertUsernameToUserId(getElementOrDefault(array,MapUtils.getIntValue(map,"业务负责人",-1)),userList);
            if (StringUtils.isBlank(bizLeader)){
                return new Result("400","业务负责人输入错误");
            }
            databaseInfo.setBusinessLeader(bizLeader);
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
            return "";
        }

        return user.get().getUserId();
    }

    private List<DataSourceInfo>  getDataSource(List<String> sourceTypeList,String tenantId){
        List<DataSourceInfo> dataSourceInfoList = dataSourceDAO.queryDataSourceBySourceTypeIn(sourceTypeList,tenantId);
        if(dataSourceInfoList == null){
            dataSourceInfoList = new ArrayList<>();
        }
        if(sourceTypeList.contains("HIVE")){
            DataSourceInfo info = new DataSourceInfo();
            info.setSourceId("hive");
            info.setSourceName("hive");
            info.setSourceType("HIVE");
            dataSourceInfoList.add(info);
        }

        return dataSourceInfoList;
    }
}
