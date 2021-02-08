package io.zeta.metaspace.model.timelimit;

import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.commons.net.ntp.TimeStamp;

import java.util.List;

/**
 * 基于通过查询类派生出的时间限定查询类
 */
public class TimeLimitSearch extends Parameters {

    private String startTime;

    private String endTime;

    private List<String> status;
}
