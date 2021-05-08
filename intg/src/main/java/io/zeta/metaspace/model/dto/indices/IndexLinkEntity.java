package io.zeta.metaspace.model.dto.indices;


import lombok.Data;

import java.util.Objects;

/**
 * 指标链路图的实体节点信息基类
 */
@Data
public abstract class IndexLinkEntity {

    protected String id; //节点ID

    protected String nodeType;//节点类型1:采集任务 2. 质量任务 3. 原子指标 4.派生指标 5.复合指标

    protected String nodeStatus; //任务状态state: 0(成功)，1（失败），2（未执行）

    protected String nodeName; //任务名称

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexLinkEntity entity = (IndexLinkEntity) o;
        return id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
