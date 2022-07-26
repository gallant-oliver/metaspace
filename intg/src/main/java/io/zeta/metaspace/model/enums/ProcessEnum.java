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
 * @date 2019/1/17 14:39
 */
package io.zeta.metaspace.model.enums;

public enum ProcessEnum {


    // 消息流程进度
    PROCESS_APPROVED(0, "已审批"),
    PROCESS_APPROVED_NOT_APPROVED(1, "未审批"),
    PROCESS_APPROVED_AUTHORIZED(2, "已授权"),
    PROCESS_APPROVED_NOT_AUTHORIZED(3, "已移除"),
    PROCESS_APPROVED_DEAL(4, "已处理"),
    PROCESS_APPROVED_FEEDBACK(5, "已反馈"),
    PROCESS_APPROVED_NOT_DEAL(6, "待处理");
    /**
     * 流程状态编号
     */
    public int code;
    /**
     * 流程状态名称
     */
    public String processCn;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getProcessCn() {
        return processCn;
    }

    public void setProcessCn(String processCn) {
        this.processCn = processCn;
    }

    ProcessEnum(int code, String processCn) {
        this.code = code;
        this.processCn = processCn;
    }
}
