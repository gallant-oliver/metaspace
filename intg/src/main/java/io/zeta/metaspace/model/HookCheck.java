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

package io.zeta.metaspace.model;

import java.util.List;
import java.util.Map;

/**
 * @author lixiang03
 * @Data 2020/4/14 10:42
 */
public class HookCheck {
    private long kafkaCheck;
    private String hookJar;
    private List<String> hookConfigCheck;
    private Map<String,Boolean> consumerThread;

    public long getKafkaCheck() {
        return kafkaCheck;
    }

    public void setKafkaCheck(long kafkaCheck) {
        this.kafkaCheck = kafkaCheck;
    }

    public String getHookJar() {
        return hookJar;
    }

    public void setHookJar(String hookJar) {
        this.hookJar = hookJar;
    }

    public List<String> getHookConfigCheck() {
        return hookConfigCheck;
    }

    public void setHookConfigCheck(List<String> hookConfigCheck) {
        this.hookConfigCheck = hookConfigCheck;
    }

    public Map<String, Boolean> getConsumerThread() {
        return consumerThread;
    }

    public void setConsumerThread(Map<String, Boolean> consumerThread) {
        this.consumerThread = consumerThread;
    }
}
