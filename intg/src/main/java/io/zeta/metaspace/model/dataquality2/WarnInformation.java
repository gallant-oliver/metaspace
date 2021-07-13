package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class WarnInformation {

    @JsonIgnore
    private String taskId;
    /**
     * 告警状态:0-无告警,1-告警中；2-已关闭
     */
    private int status;

    /**
     * 告警编号：告警id + "_" + 告警级别编号
     */
    private String warnNo;

    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 子任务序列
     */
    private String sequence;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 告警时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp warnTime;

    /**
     * 告警级别：0-普通；1-黄色；2-红色
     */
    private int warnGrade;
    /**
     * 告警组
     */
    private List<TaskWarningHeader.WarningGroupHeader> warnGroupNames;
    /**
     * 规则描述
     */
    private String ruleDescription;
    /**
     * scope 与 type 组合判断规则的类型 scope=0-表;scope=1-字段; scope=2且type=31:一致性 scope=2且type=32:自定义
     */
    private int scope;
    /**
     * 规则类型
     */
    private int type;
    /**
     * 告警校验类型：0-固定值；1-波动值
     */
    private int checkType;
    /**
     * 告警校验类型为波动值时，checkMaxValue表示告警校验最大值；告警校验类型为固定值，checkMaxValue表示告警校验值
     */
    private Float checkMaxValue;
    /**
     * 告警校验类型为波动值时，checkMinValue表示告警校验最小值；告警校验类型为固定值，checkMinValue无意义
     */
    private Float checkMinValue;
    /**
     * 规则中SQL语句
     */
    @JsonIgnore
    private String sql;
    /**
     * 校验对象：自定义校验为规则中SQL语句；其他校验为数据源信息
     */
    private Object object;

    /**
     * 计算结果
     */
    private float result;

    /**
     * 结果单位
     */
    private String unit;

    @JsonIgnore
    private String objectId;

    @JsonIgnore
    private long total;
}
