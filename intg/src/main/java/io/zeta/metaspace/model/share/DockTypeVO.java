package io.zeta.metaspace.model.share;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: 数据服务对接类型
 * @date 2022/8/2511:37
 */
@Data
public class DockTypeVO {
    private int dockType;

    public DockTypeVO(int dockType) {
        this.dockType = dockType;
    }
}
