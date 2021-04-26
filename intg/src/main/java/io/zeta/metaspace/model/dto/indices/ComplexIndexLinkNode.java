package io.zeta.metaspace.model.dto.indices;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;


@Data
public class ComplexIndexLinkNode extends IndexLinkEntity {

    private Timestamp publishTime; //发布时间

    private String express; //表达式

    private String businessCaliber;  //业务口径

    private String technicalCaliber; //技术口径

    private String indexCode;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplexIndexLinkNode)) return false;
        if (!super.equals(o)) return false;
        ComplexIndexLinkNode that = (ComplexIndexLinkNode) o;
        return Objects.equals(indexCode, that.indexCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), indexCode);
    }
}
