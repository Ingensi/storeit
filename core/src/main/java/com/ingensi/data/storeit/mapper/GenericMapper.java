/*
 * Copyright (c) 2015 Ingensi, Cyres group.
 *
 * See the LICENSE file for copying permission.
 */

package com.ingensi.data.storeit.mapper;


import com.ingensi.data.storeit.entities.StoredEntity;

import java.util.Map;

/**
 * A Mapper provides methods to build and store entities from/to storage.
 * A storage needs to know how to:
 * <ul>
 * <li>Store an entity (entity TO storage)</li>
 * <li>Build and entity from data (storage TO entity)</li>
 * </ul>
 * <p>
 * A Mapper is a simple generic class parametrized by the Entity type to store and build. It defines two
 * {@link FunctionalInterface} {@link GenericMapper.From} and {@link GenericMapper.To} allowing developer to define
 * mapping through lambdas. A Mapper stored an instance of these two methods. They will be invoked by the storage to
 * generate and store entities.
 * </p>
 * <p>
 * As data handled by a storage are formatted as key/values (Map), from and to methods will define mapping from an
 * entity to a map, and from a map to an entity.
 * </p>
 * <p>
 * To learn more about {@link FunctionalInterface} see
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/FunctionalInterface.html">Java SE8 API doc</a>.
 * </p>
 *
 * @param <T> Type of entity to map with storage.
 */
public class GenericMapper<T extends StoredEntity> {
    private final From<T> from;
    private final To<T> to;

    /**
     * Main Mapper constructor.
     *
     * @param from Mapping definition (map TO entity)
     * @param to   Mapping definition (entity TO map)
     */
    public GenericMapper(From<T> from, To<T> to) {
        this.from = from;
        this.to = to;
    }

    public From<T> getFrom() {
        return from;
    }

    public To<T> getTo() {
        return to;
    }

    /**
     * This {@link FunctionalInterface} declares the From build method header.
     *
     * @param <T> Entity type to map.
     */
    @FunctionalInterface
    public interface From<T> {
        /**
         * The build method definition, allowing to instantiate an Entity from a Map.
         *
         * @param source The source Map.
         * @return The built entity.
         */
        T build(Map<String, Object> source);
    }

    /**
     * This {@link FunctionalInterface} declares the To build method header.
     *
     * @param <T> Entity type to map.
     */
    @FunctionalInterface
    public interface To<T> {
        /**
         * The build method definition, allowing to generate a Map from an Entity.
         *
         * @param source The source Entity.
         * @return The resulting Map.
         */
        Map<String, Object> build(T source);
    }
}
