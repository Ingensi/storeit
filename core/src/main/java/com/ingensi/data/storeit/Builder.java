package com.ingensi.data.storeit;


import com.ingensi.data.storeit.entities.StorableEntity;

import java.util.Map;

/**
 * Created by tmarmin on 3/31/15.
 */
public class Builder<T extends StorableEntity> {
    private final From<T> from;
    private final To<T> to;

    public Builder(From<T> from, To<T> to) {
        this.from = from;
        this.to = to;
    }

    public From<T> getFrom() {
        return from;
    }

    public To<T> getTo() {
        return to;
    }

    @FunctionalInterface
    public interface From<T> {
        T build(Map<String, Object> source);
    }

    @FunctionalInterface
    public interface To<T> {
        Map<String, Object> build(T source);
    }
}
