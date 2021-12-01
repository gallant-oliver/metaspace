package io.zeta.metaspace.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * @ClassName ThreadPoolUtil
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/6/11 15:30
 * @Version 1.0
 */
public class ThreadPoolUtil {


    /** 线程池保持ALIVE状态线程数 */
    public static final int CORE_POOL_SIZE = 20;

    /** 线程池最大线程数 */
    public static final int MAX_POOL_SIZE = 50;

    /** 空闲线程回收时间 */
    public static final int KEEP_ALIVE_TIME = 1000;

    /** 线程池等待队列 */
    public static final int BLOCKING_QUEUE_SIZE = 2000;

    /**
     * 线程池拒绝策略
     */
    public static final RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

    /** 业务请求异步处理线程池 */
    private static final ThreadPoolExecutor processExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(BLOCKING_QUEUE_SIZE),handler);

    /**
     * 元数据采集专用线程池
      */
    private static final ThreadPoolExecutor metadataExecutor = new ThreadPoolExecutor(
            10, 20, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(BLOCKING_QUEUE_SIZE),handler);

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return processExecutor;
    }

    public static ThreadPoolExecutor getThreadPoolExecutorMetadata() {
        return metadataExecutor;
    }

    public static int getMaxQueueSize() {
        return BLOCKING_QUEUE_SIZE;
    }
}
