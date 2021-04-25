package io.zeta.metaspace.model.dto.indices;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Data
public class AutoMaticIndexLinkNode extends IndexLinkEntity{

    private String indexCode;  //指标标示

    private Timestamp publishTime;

    private String dataFrom;

    private String businessCaliber;  //业务口径

    private String technicalCaliber; //技术口径

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AutoMaticIndexLinkNode)) return false;
        if (!super.equals(o)) return false;
        AutoMaticIndexLinkNode that = (AutoMaticIndexLinkNode) o;
        return indexCode.equals(that.indexCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), indexCode);
    }
}
