package io.zeta.metaspace.web.metadata;

import io.zeta.metaspace.web.model.TableSchema;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhuxuetong
 * @date 2019-09-05 18:56
 */
public interface IMetaDataProvider {
    void importDatabases(TableSchema tableSchema) throws Exception;
    AtomicInteger getTotalTables();

    AtomicInteger getUpdatedTables();

    AtomicLong getStartTime();

    AtomicLong getEndTime();
}
