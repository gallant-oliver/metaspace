// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.util;

import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.security.Tenant;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.DbDAO;
import io.zeta.metaspace.web.dao.dataquality.RuleDAO;
import io.zeta.metaspace.web.service.TenantService;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author lixiang03
 * @Data 2020/4/17 17:58
 */
@Component
public class CategoryUtil {
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private DataShareDAO dataShareDAO;
    @Autowired
    private RuleDAO ruleDAO;
    @Autowired
    private DbDAO dbDAO;
    @Autowired
    private TenantService tenantService;
    private static CategoryUtil utils;

    @PostConstruct
    public void init() {
        utils = this;
    }
    private static List<CategoryEntityV2> initCategory = new ArrayList<CategoryEntityV2>(){
        {
            Timestamp createTime = DateUtils.currentTimestamp();
            add(new CategoryEntityV2("1", "贴源层", null, null, null, "2", 0, 1, "1", createTime,null));
            add(new CategoryEntityV2("2","基础层",null,null,"1","4",0,1,"1",createTime,null));
//            add(new CategoryEntityV2("3","规范层",null,null,"2","4",0,1,"1", createTime,null));
            add(new CategoryEntityV2("4","通用层",null,null,"2","5",0,1,"1", createTime,null));
            add(new CategoryEntityV2("5","应用层",null,null,"4",null,0,1,"1", createTime,null));
            add(new CategoryEntityV2("Standard-1", "基础类数据标准","基础类数据标准",null,null,"Standard-2",3,1,"1", createTime,null));
            add(new CategoryEntityV2("Standard-2", "指标类数据标准","指标类数据标准",null,"Standard-1",null,3,1,"1", createTime,null));
            add(new CategoryEntityV2("Standard-3", "参考数据标准","参考数据标准","Standard-1",null,"Standard-4",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-4", "主数据标准","主数据标准","Standard-1","Standard-3","Standard-5",3,2 ,"1", createTime,null));
            add(new CategoryEntityV2("Standard-5", "逻辑数据模型标准","逻辑数据模型标准","Standard-1","Standard-4","Standard-6",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-6", "物理数据模型标准","物理数据模型标准","Standard-1","Standard-5","Standard-7",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-7", "元数据标准","元数据标准","Standard-1","Standard-6","Standard-8",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-8", "公共代码标准","公共代码标准","Standard-1","Standard-7","Standard-9",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-9", "编码标准","编码标准","Standard-1","Standard-8",null,3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-10", "基础指标标准","基础指标标准","Standard-2",null,"Standard-11",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-11", "计算指标标准","计算指标标准","Standard-2","Standard-10",null,3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-12", "业务元数据标准","业务元数据标准","Standard-7",null,"Standard-13",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-13", "技术元数据标准","技术元数据标准","Standard-7","Standard-12","Standard-14",3,2,"1", createTime,null));
            add(new CategoryEntityV2("Standard-14", "管理元数据标准","业管理元数据标准","Standard-7","Standard-13",null,3,2,"1", createTime,null));
            add(new CategoryEntityV2("rule_1" ,"表体积" , "表体积"  ,null,null, "rule_2"   ,4 ,1,"1", createTime,null));
            add(new CategoryEntityV2("rule_2"  ,"空值校验", "空值校验",null,"rule_1", "rule_3",4 ,1,"1", createTime,null));
            add(new CategoryEntityV2("rule_3"  ,"唯一值校验", "唯一值校验",null,"rule_2", "rule_4",4 ,1,"1", createTime,null));
            add(new CategoryEntityV2("rule_4"  ,"重复值校验", "重复值校验",null,"rule_3", "rule_5",4 ,1,"1",  createTime,null));
            add(new CategoryEntityV2("rule_5"  ,"数值型校验", "数值型校验",null,"rule_4", "rule_6",4 ,1,"1",  createTime,null));
            add(new CategoryEntityV2("rule_6"  ,"一致性校验", "一致性校验",null,"rule_5", null,4 ,1,"1",  createTime,null));
            add(new CategoryEntityV2("index_field_default","默认域","默认域",null,null,null,5,1,"1",createTime,"IndexFieldDefault01"));
        }
    };

    private static List<Rule> initRule = new ArrayList<Rule>(){
        {
            Timestamp createTime = DateUtils.currentTimestamp();
            add(new Rule("字段汇总值变化", 1, "", "相比上一周期，字段汇总值变化", createTime, createTime, "20", "rule_5", 14));
            add(new Rule("字段最小值变化", 1, "", "相比上一周期，字段最小值变化", createTime, createTime, "21", "rule_5", 15));
            add(new Rule("字段最大值变化", 1, "", "相比上一周期，字段最大值变化", createTime, createTime, "22", "rule_5", 16));
            add(new Rule("字段平均值变化率", 1, "%", "相比上一周期，字段平均值变化率", createTime, createTime, "23", "rule_5", 6));
            add(new Rule("字段平均值", 1, null, "计算字段平均值", createTime, createTime, "24", "rule_5", 20));
            add(new Rule("字段汇总值", 1, null, "计算字段汇总值", createTime, createTime, "25", "rule_5", 21));
            add(new Rule("表大小变化", 0, "字节", "相比上一周期，表大小变化", createTime, createTime, "1", "rule_1", 3));
            add(new Rule("表行数变化率", 0, "%", "相比上一周期，表行数变化率", createTime, createTime, "2", "rule_1", 0));
            add(new Rule("表行数变化", 0, "行", "相比上一周期，表行数变化", createTime, createTime, "3", "rule_1", 2));
            add(new Rule("表大小变化率", 0, "%", "相比上一周期，表大小变化率", createTime, createTime, "4", "rule_1", 1));
            add(new Rule("当前表行数", 0, "行", "表行数是否符合预期", createTime, createTime, "5", "rule_1", 4));
            add(new Rule("当前表大小", 0, "字节", "表大小是否符合预期", createTime, createTime, "6", "rule_1", 5));
            add(new Rule("字段空值个数/总行数", 1, "%", "计算字段空值行数所占的比例", createTime, createTime, "7", "rule_2", 28));
            add(new Rule("字段空值个数变化率", 1, "%", "相比上一周期，字段空值个数变化率", createTime, createTime, "8", "rule_2", 11));
            add(new Rule("字段空值个数", 1, "个", "计算字段空值个数", createTime, createTime, "9", "rule_2", 25));
            add(new Rule("字段重复值个数/总行数", 1, "%", "计算字段重复值行数所占的比例", createTime, createTime, "15", "rule_4", 29));
            add(new Rule("字段重复值个数变化率", 1, "%", "相比上一周期，字段重复值个数变化率", createTime, createTime, "16", "rule_4", 12));
            add(new Rule("字段空值个数变化", 1, "个", "相比上一周期，字段空值个数变化", createTime, createTime, "10", "rule_2", 18));
            add(new Rule("字段唯一值个数/总行数", 1, "%", "计算字段唯一值行数所占的比例", createTime, createTime, "11", "rule_3", 27));
            add(new Rule("字段唯一值个数变化率", 1, "%", "相比上一周期，字段唯一值个数变化率", createTime, createTime, "12", "rule_3", 10));
            add(new Rule("字段重复值个数", 1, "个", "计算字段重复值个数", createTime, createTime, "17", "rule_4", 26));
            add(new Rule("字段唯一值个数", 1, "个", "计算字段唯一值个数", createTime, createTime, "13", "rule_3", 24));
            add(new Rule("字段唯一值个数变化", 1, "个", "相比上一周期，字段唯一值个数变化", createTime, createTime, "14", "rule_3", 17));
            add(new Rule("字段重复值个数变化", 1, "个", "相比上一周期，字段重复值个数变化", createTime, createTime, "18", "rule_4", 19));
            add(new Rule("字段平均值变化", 1, "", "相比上一周期，字段平均值变化", createTime, createTime, "19", "rule_5", 13));
            add(new Rule("字段最小值", 1, null, "计算字段最小值", createTime, createTime, "26", "rule_5", 22));
            add(new Rule("字段最大值", 1, null, "计算字段最大值", createTime, createTime, "27", "rule_5", 23));
            add(new Rule("字段汇总值变化率", 1, "%", "相比上一周期，字段汇总值变化率", createTime, createTime, "28", "rule_5", 7));
            add(new Rule("字段最小值变化率", 1, "%", "相比上一周期，字段最小值变化率", createTime, createTime, "29", "rule_5", 8));
            add(new Rule("字段最大值变化率", 1, "%", "相比上一周期，字段最大值变化率", createTime, createTime, "30", "rule_5", 9));
            add(new Rule("一致性校验",2,null,"一致性",createTime,createTime,"31","rule_6",31));
        }
    };

    public static final List<String> initTechnicalCategoryId=new ArrayList<String>(){{
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
    }};
    public static final List<String> initDataStandardCategoryId=new ArrayList<String>(){{
        add("Standard-1");
        add("Standard-2");
        add("Standard-3");
        add("Standard-4");
        add("Standard-5");
        add("Standard-6");
        add("Standard-7");
        add("Standard-8");
        add("Standard-9");
        add("Standard-10");
        add("Standard-11");
        add("Standard-12");
        add("Standard-13");
        add("Standard-14");
    }};
    //默认指标域
    public static final String indexFieldId="index_field_default";

    public static final String apiCategoryName = "默认目录";

    public static void initCategorySql(List<Tenant> tenants){
        User user=AdminUtils.getUserData();
        for (Tenant tenant: tenants){
            for(CategoryEntityV2 category:initCategory){
                category.setCreator(user.getUserId());
            }
            utils.categoryDAO.addAll(initCategory,tenant.getTenantId());
            for(Rule rule:initRule){
                rule.setCreator(user.getUserId());
            }
            utils.ruleDAO.insertAll(initRule,tenant.getTenantId());
        }
    }

    public static void initApiCategory(String tenantId,String projectId){
        Timestamp createTime = DateUtils.currentTimestamp();
        String uuid = UUID.randomUUID().toString();
        utils.dataShareDAO.add(new CategoryEntityV2(uuid,"默认目录","默认目录",null,null,null,2,1,"1",createTime,null),projectId,tenantId);
    }

    public static TableRelation getTableRelation(String tableGuid, String tenantId, String categoryGuid) {
        TableRelation tableRelation = new TableRelation();
        tableRelation.setCategoryGuid(StringUtils.isBlank(categoryGuid)? "1": categoryGuid);
        tableRelation.setTenantId(tenantId);
        tableRelation.setGenerateTime(io.zeta.metaspace.web.util.DateUtils.getNow());
        tableRelation.setRelationshipGuid(UUID.randomUUID().toString());
        tableRelation.setTableGuid(tableGuid);
        return tableRelation;
    }
}
