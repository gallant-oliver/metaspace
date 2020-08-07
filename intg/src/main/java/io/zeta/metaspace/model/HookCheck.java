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
    private boolean kafkaCheck;
    private String kafkaMessage;
    private long kafkaNumber;
    private boolean hookJar;
    private String hookJarMessage;
    private boolean hookConfigCheck;
    private String hookConfigMessage;
    private boolean consumerThread;
    private String threadMessage;

    public boolean isKafkaCheck() {
        return kafkaCheck;
    }

    public void setKafkaCheck(boolean kafkaCheck) {
        this.kafkaCheck = kafkaCheck;
    }

    public String getKafkaMessage() {
        return kafkaMessage;
    }

    public void setKafkaMessage(String kafkaMessage) {
        this.kafkaMessage = kafkaMessage;
    }

    public long getKafkaNumber() {
        return kafkaNumber;
    }

    public void setKafkaNumber(long kafkaNumber) {
        this.kafkaNumber = kafkaNumber;
    }

    public boolean isHookJar() {
        return hookJar;
    }

    public void setHookJar(boolean hookJar) {
        this.hookJar = hookJar;
    }

    public String getHookJarMessage() {
        return hookJarMessage;
    }

    public void setHookJarMessage(String hookJarMessage) {
        this.hookJarMessage = hookJarMessage;
    }

    public boolean isHookConfigCheck() {
        return hookConfigCheck;
    }

    public void setHookConfigCheck(boolean hookConfigCheck) {
        this.hookConfigCheck = hookConfigCheck;
    }

    public String getHookConfigMessage() {
        return hookConfigMessage;
    }

    public void setHookConfigMessage(String hookConfigMessage) {
        this.hookConfigMessage = hookConfigMessage;
    }

    public boolean isConsumerThread() {
        return consumerThread;
    }

    public void setConsumerThread(boolean consumerThread) {
        this.consumerThread = consumerThread;
    }

    public String getThreadMessage() {
        return threadMessage;
    }

    public void setThreadMessage(String threadMessage) {
        this.threadMessage = threadMessage;
    }
}
