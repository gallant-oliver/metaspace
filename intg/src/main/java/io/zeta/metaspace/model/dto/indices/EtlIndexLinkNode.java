package io.zeta.metaspace.model.dto.indices;


import lombok.Data;

import java.util.Objects;


/**
 * 任务调度的节点类型信息
 */
@Data
public class EtlIndexLinkNode extends IndexLinkEntity{

    private String instanceName;//实例名称

    private String definitionName; //工作流名称

    private long endTime; //最后执行时间

    private String projectName; //项目名称

    private String typeName; //类型名称

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EtlIndexLinkNode)) return false;
        if (!super.equals(o)) return false;
        EtlIndexLinkNode that = (EtlIndexLinkNode) o;
        return projectName.equals(that.projectName) &&
                typeName.equals(that.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectName, typeName);
    }
}



