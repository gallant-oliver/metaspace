package io.zeta.metaspace.model.enums;

import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 数据质量-任务管理-任务执行状态
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-29
 */
public enum TaskExecuteStatus {
    NOT_RUNNING(0, "未执行"),
    RUNNING(1, "执行中"),
    FINISHED(2, "成功"),
    FAILED(3, "失败"),
    CANCEL(4, "取消"),
    ;
    
    @Getter
    private int code;
    @Getter
    private String desc;
    
    TaskExecuteStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static TaskExecuteStatus parseByCode(int code) {
        return Stream.of(TaskExecuteStatus.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElse(null);
    }
    
    public static TaskExecuteStatus parseByDesc(String desc) {
        return Stream.of(TaskExecuteStatus.values())
                .filter(obj -> Objects.equals(desc, obj.getDesc()))
                .findFirst()
                .orElse(null);
    }
}
