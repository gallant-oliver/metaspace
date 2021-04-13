package io.zeta.metaspace.model.dataquality2;
@FunctionalInterface
public interface WarnFunction<R, P, ET, T> {
    R apply(P p, ET et, T t);
}
