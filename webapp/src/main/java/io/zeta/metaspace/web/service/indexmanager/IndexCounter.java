package io.zeta.metaspace.web.service.indexmanager;

import io.zeta.metaspace.web.util.IndexCounterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class IndexCounter {

    public Set<String> keys() {
        Set<String> keySet = IndexCounterUtils.INDEX_MAP.keySet();
        return new HashSet<>(keySet);
    }

    public Map<String, Long> getAllIndexes() {
        Map<String, Long> map = new HashMap<>();
        Set<Map.Entry<String, AtomicLong>> entrySet = IndexCounterUtils.INDEX_MAP.entrySet();
        for (Map.Entry<String, AtomicLong> entry : entrySet) {
            map.put(entry.getKey(), entry.getValue().get());
        }
        return map;
    }

    public Map<String, Long> getIndexByKeys(Set<String> keys) {
        Map<String, Long> indexMap = new HashMap<>();
        if (CollectionUtils.isEmpty(keys)) {
            return indexMap;
        }
        for (String key : keys) {
            String trimKey = key.trim();
            if (IndexCounterUtils.INDEX_MAP.containsKey(trimKey)) {
                indexMap.put(trimKey, IndexCounterUtils.INDEX_MAP.get(trimKey).get());
            }
        }
        return indexMap;
    }

    /**
     * 根据数据源类型获取对应的指标key-成功
     *
     * @param type
     * @return
     */
    public String getSuccessKeyByType(String type) {
        if (StringUtils.isBlank(type) || !IndexCounterUtils.SOURCE_TYPE_SUCCESS_MAP.containsKey(type)) {
            log.warn("主键为【" + type + "】的数据源类型不存在，已忽略该指标的统计");
            return "";
        }
        return IndexCounterUtils.SOURCE_TYPE_SUCCESS_MAP.get(type);
    }

    /**
     * 根据数据源类型获取对应的指标key-失败
     *
     * @param type
     * @return
     */
    public String getFailKeyByType(String type) {
        if (StringUtils.isBlank(type) || !IndexCounterUtils.SOURCE_TYPE_FAIL_MAP.containsKey(type)) {
            log.warn("主键为【" + type + "】的数据源类型不存在，已忽略该指标的统计");
            return "";
        }
        return IndexCounterUtils.SOURCE_TYPE_FAIL_MAP.get(type);
    }

    /**
     * 对应的指标+1 成功
     *
     * @param key
     */
    public void plusOneSuccess(String key) {
        this.plusOne(getSuccessKeyByType(key));
        this.plusOne(IndexCounterUtils.METASPACE_METADATA_TASK_SUCCESS_COUNT);
    }

    /**
     * 对应的指标+1 失败
     *
     * @param key
     */
    public void plusOneFail(String key) {
        this.plusOne(getFailKeyByType(key));
        this.plusOne(IndexCounterUtils.METASPACE_METADATA_TASK_FAIL_COUNT);
    }

    /**
     * 对应的指标+1
     *
     * @param key
     */
    public void plusOne(String key) {
        if (StringUtils.isBlank(key) || !IndexCounterUtils.INDEX_MAP.containsKey(key)) {
            log.warn("主键为【" + key + "】的指标不存在，已忽略该指标的统计");
            return;
        }
        IndexCounterUtils.INDEX_MAP.get(key).getAndIncrement();
    }
}
