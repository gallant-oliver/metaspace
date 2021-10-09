package io.zeta.metaspace.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ThreadPoolUtil
 * @Descroption 元数据采集专用线程池
 * @Author Lvmengliang
 * @Date 2021/6/11 15:30
 * @Version 1.0
 */
public class CollectThreadPoolUtil {


    /** 线程池保持ALIVE状态线程数 */
    public static final int CORE_POOL_SIZE = 10;

    /** 线程池最大线程数 */
    public static final int MAX_POOL_SIZE = 25;

    /** 空闲线程回收时间 */
    public static final int KEEP_ALIVE_TIME = 1000;

    /** 线程池等待队列 */
    public static final int BLOCKING_QUEUE_SIZE = 1500;

    /** 业务请求异步处理线程池 */
    private static final ThreadPoolExecutor tableProcessExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(BLOCKING_QUEUE_SIZE));

    private static final ThreadPoolExecutor schemaProcessExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(BLOCKING_QUEUE_SIZE));

    public static ThreadPoolExecutor getTableCollectThreadPoolExecutor() {
        return tableProcessExecutor;
    }

    public static ThreadPoolExecutor getSchemaCollectThreadPoolExecutor() {
        return schemaProcessExecutor;
    }

}
