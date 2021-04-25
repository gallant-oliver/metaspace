package io.zeta.metaspace.model.dto.indices;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Data
public class DriveIndexLinkNode extends IndexLinkEntity{

    private String indexCode;  //指标标示

    private Timestamp publishTime;

    private String businessCaliber;  //业务口径

    private String technicalCaliber; //技术口径

    private String atomIndexName;//原子指标

    private String qualifierName;//修饰词名称

    private String timeLimitName; //时间限定名称

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriveIndexLinkNode)) return false;
        if (!super.equals(o)) return false;
        DriveIndexLinkNode that = (DriveIndexLinkNode) o;
        return indexCode.equals(that.indexCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), indexCode);
    }
}
