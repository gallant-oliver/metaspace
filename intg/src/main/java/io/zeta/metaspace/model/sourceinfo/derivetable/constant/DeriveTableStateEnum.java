package io.zeta.metaspace.model.sourceinfo.derivetable.constant;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DeriveTableStateEnum {

    UN_COMMIT(0, "未提交"),
    COMMIT(1, "已提交");


    private String name;

    private Integer state;

    DeriveTableStateEnum(Integer state, String name) {
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public Integer getState() {
        return state;
    }

    public static String getName(Integer state) {
        List<String> collect = Stream.of(DeriveTableStateEnum.values()).filter(e -> e.getState().equals(state)).map(DeriveTableStateEnum::getName).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            return collect.get(0);
        }
        return null;
    }

}
