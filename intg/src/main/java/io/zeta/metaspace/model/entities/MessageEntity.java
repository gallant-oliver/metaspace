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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/24 9:54
 */
package io.zeta.metaspace.model.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.enums.MessagePush;
import lombok.Data;

import java.sql.Timestamp;

/*
 * @description
 * @author liwenfeng
 * @date 2022/7/5 9:54
 */
@Data
public class MessageEntity {

    /**
     * id
     */
    private String id;

    /**
     * 标题内容
     */
    private String name;

    /**
     * 消息类型（0：资源审核信息、1：用户组信息、2：数据服务、3：需求审批）
     */
    private Integer type;

    /**
     * 所属模块（系统管理/用户组管理）
     */
    private String module;

    /**
     * 0未读、1已读
     */
    private Integer status;

    /**
     * 租户id
     */
    private String tenantid;

    /**
     * 创建人（邮箱）
     */
    private String createUser;

    /**
     * 流程进度(0-已审批、1-未审批、2-已授权、3-已移除、4-已处理、5-已反馈、6-待处理
     */
    private Integer process;

    /**
     * 操作时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp operationTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    /**
     * 是否删除
     */
    private Boolean delete;

    /**
     * 总数
     */
    @JsonIgnore
    private int total;

    public MessageEntity(Integer type, String name, String module){
        this.type = type;
        this.name = name;
        this.module = module;
    }

    public MessageEntity(Integer type, String name, String module, Integer process){
        this.type = type;
        this.name = name;
        this.module = module;
        this.process = process;
    }

    public MessageEntity(MessagePush messagePush,String name,Integer process){
        this.type = messagePush.type;
        this.name = name;
        this.module = messagePush.module;
        this.process = process;
    }

    public MessageEntity(){
    }

}
