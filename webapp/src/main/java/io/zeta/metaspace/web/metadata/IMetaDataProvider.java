package io.zeta.metaspace.web.metadata;

import io.zeta.metaspace.model.TableSchema;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhuxuetong
 * @date 2019-09-05 18:56
 */
public interface IMetaDataProvider {
    void importDatabases(String taskInstanceId, TableSchema tableSchema) throws Exception;
}
