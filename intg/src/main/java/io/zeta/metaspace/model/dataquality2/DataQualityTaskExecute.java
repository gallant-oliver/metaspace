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
 * @date 2019/7/26 10:43
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @description
 * @author sunhaoning
 * @date 2019/7/26 10:43
 */
@Data
public class DataQualityTaskExecute {
    private String id;
    private String taskId;
    private Float percent;
    private Integer executeStatus;
    private String executor;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp executeTime;
    private String closer;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp closeTime;
    private Integer costTime;
    private Integer generalWarningCount;
    private Integer orangeWarningCount;
    private Integer redWarningCount;
    private Integer ruleErrorCount;
    private Integer warningStatus;
    private Integer errorStatus;
    private String number;
    private Integer counter;
}
