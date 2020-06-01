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

import io.zeta.metaspace.model.security.Tenant;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.CategoryDAO;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * @author lixiang03
 * @Data 2020/4/17 17:58
 */
@Component
public class CategoryUtil {
    @Autowired
    private CategoryDAO categoryDAO;
    private static CategoryUtil utils;

    @PostConstruct
    public void init() {
        utils = this;
    }
    private static List<CategoryEntityV2> initCategory = new ArrayList<CategoryEntityV2>(){
        {
            Timestamp createTime = DateUtils.currentTimestamp();
            add(new CategoryEntityV2("1", "贴源层", null, null, null, "2", 0, 1, "1", createTime));
            add(new CategoryEntityV2("2","基础层",null,null,"1","3",0,1,"1",createTime));
            add(new CategoryEntityV2("3","规范层",null,null,"2","4",0,1,"1", createTime));
            add(new CategoryEntityV2("4","通过层",null,null,"3","5",0,1,"1", createTime));
            add(new CategoryEntityV2("5","应用层",null,null,"4",null,0,1,"1", createTime));
            add(new CategoryEntityV2("Standard-1", "基础类数据标准","基础类数据标准",null,null,"Standard-2",3,1,"1", createTime));
            add(new CategoryEntityV2("Standard-2", "指标类数据标准","指标类数据标准",null,"Standard-1",null,3,1,"1", createTime));
            add(new CategoryEntityV2("Standard-3", "参考数据标准","参考数据标准","Standard-1",null,"Standard-4",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-4", "主数据标准","主数据标准","Standard-1","Standard-3","Standard-5",3,2 ,"1", createTime));
            add(new CategoryEntityV2("Standard-5", "逻辑数据模型标准","逻辑数据模型标准","Standard-1","Standard-4","Standard-6",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-6", "物理数据模型标准","物理数据模型标准","Standard-1","Standard-5","Standard-7",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-7", "元数据标准","元数据标准","Standard-1","Standard-6","Standard-8",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-8", "公共代码标准","公共代码标准","Standard-1","Standard-7","Standard-9",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-9", "编码标准","编码标准","Standard-1","Standard-8",null,3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-10", "基础指标标准","基础指标标准","Standard-2",null,"Standard-11",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-11", "计算指标标准","计算指标标准","Standard-2","Standard-10",null,3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-12", "业务元数据标准","业务元数据标准","Standard-7",null,"Standard-13",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-13", "技术元数据标准","技术元数据标准","Standard-7","Standard-12","Standard-14",3,2,"1", createTime));
            add(new CategoryEntityV2("Standard-14", "管理元数据标准","业管理元数据标准","Standard-7","Standard-13",null,3,2,"1", createTime));
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

    public static void initCategorySql(List<Tenant> tenants){
        for (Tenant tenant: tenants){
            utils.categoryDAO.addAll(initCategory,tenant.getTenantId());
        }
    }
}
