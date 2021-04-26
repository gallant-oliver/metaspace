package io.zeta.metaspace.model.dto.indices;

import lombok.Data;

import java.util.Objects;

/**
 * 指标链路图的关系信息
 */
@Data
public class IndexLinkRelation {

  private String from;   //关系起始点

  private String to;    //关系的终点

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IndexLinkRelation relation = (IndexLinkRelation) o;
    return Objects.equals(from, relation.from) &&
            Objects.equals(to, relation.to);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }

//    private String driction; //方向暂无

}
