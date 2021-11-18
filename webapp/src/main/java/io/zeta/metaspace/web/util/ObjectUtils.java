package io.zeta.metaspace.web.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author 周磊
 * @version 1.0
 * @date 2021-11-18
 */
@Slf4j
@UtilityClass
public class ObjectUtils {
    
    /**
     * 对指定对象进行指定的逻辑判断,为true做出指定的动作,为false抛出异常
     *
     * @param t            待处理的对象
     * @param predicate    指定的逻辑判断
     * @param errorMessage 异常信息
     * @param thenConsumer 为true指定的动作
     */
    public <T> void isTrueThenElseException(T t, Predicate<T> predicate, String errorMessage, Consumer<T> thenConsumer) {
        isTrueThenElseThen(t, predicate, thenConsumer, v -> {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, errorMessage);
        });
    }
    
    /**
     * 对指定对象进行指定的逻辑判断,为true做出指定的动作,为false做出另外的指定动作
     *
     * @param t            待处理的对象
     * @param predicate    指定的逻辑判断
     * @param trueConsumer 为true指定的动作
     * @param elseConsumer 为false指定的动作
     */
    public <T> void isTrueThenElseThen(T t, Predicate<T> predicate, Consumer<T> trueConsumer, Consumer<T> elseConsumer) {
        if (Objects.requireNonNull(predicate).test(t)) {
            Objects.requireNonNull(trueConsumer).accept(t);
        } else {
            Objects.requireNonNull(elseConsumer).accept(t);
        }
    }
    
    /**
     * 对指定对象进行指定的逻辑判断,为true做出指定的动作
     *
     * @param t            待处理的对象
     * @param thenConsumer 为true指定的动作
     */
    public <T> void isTrueThen(T t, Predicate<T> predicate, Consumer<T> thenConsumer) {
        if (Objects.requireNonNull(predicate).test(t)) {
            Objects.requireNonNull(thenConsumer).accept(t);
        }
    }
}
