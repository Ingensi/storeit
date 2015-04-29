/*
 * Copyright (c) 2015 Ingensi, Cyres group.
 *
 * See the LICENSE file for copying permission.
 */

package com.ingensi.data.storeit;

import com.ingensi.data.storeit.entities.StoredEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A common storage. A storage provides methods to access and store an entity type.
 *
 * @param <T> Type of stored entities.
 */
public interface Storage<T extends StoredEntity> {
    /**
     * Get all entities as list.
     *
     * @return A collection of entities.
     */
    Collection<T> list();

    /**
     * Get all entities as stream.
     *
     * @return A collection of entities.
     */
    Stream<T> stream();

    /**
     * Check whether an entity exists from its ID.
     *
     * @param id Id of the entity.
     * @return True if it exists, else false.
     * @throws StorageException In case of error.
     */
    boolean exists(String id) throws StorageException;

    /**
     * Store new entity. The given entity id will be used as ID in the storage.
     *
     * @param entity The entity to store.
     * @throws AlreadyExistsException If ID already exists in the storage.
     * @throws StorageException       When another error appends.
     */
    void store(T entity) throws StorageException;

    /**
     * Bulk store new entities. The given entities ids will be used as IDs in the storage.
     *
     * @param entityList The list of entities to store
     * @throws AlreadyExistsException If an ID already exists in the storage.
     * @throws StorageException       When another error appends.
     */
    void bulk(List<T> entityList) throws StorageException;

    /**
     * Store new entity with custom id.
     *
     * @param entity The entity to store.
     * @param id     ID of the entity.
     * @throws AlreadyExistsException If ID already exists in the storage.
     * @throws StorageException       When another error appends.
     */
    void store(T entity, String id) throws StorageException;

    /**
     * Bulk store new entities with custom id.
     *
     * @param entities The map of ID/entities to store
     * @throws AlreadyExistsException If an ID already exists in the storage.
     * @throws StorageException       When another error appends.
     */
    void bulk(Map<String, T> entities) throws StorageException;

    /**
     * Get an entity from its ID.
     *
     * @param id Id of the entity.
     * @return The retrieved entity.
     * @throws NotFoundException If entity is not found into storage.
     * @throws StorageException  When another error appends.
     */
    T get(String id) throws StorageException;

    /**
     * Update an existing entity. Matching with existing entity is done from the given entity ID.
     *
     * @param entity The entity to update.
     * @throws NotFoundException If entity is not found into storage.
     * @throws StorageException  When another error appends.
     */
    void update(T entity) throws StorageException;

    /**
     * Update an existing entity with custom ID. Matching with existing entity is done from the given entity ID.
     *
     * @param entity The entity to update.
     * @param id     Id of the entity.
     * @throws NotFoundException If entity is not found into storage.
     * @throws StorageException  When another error appends.
     */
    void update(T entity, String id) throws StorageException;

    /**
     * Delete the entity from its ID.
     *
     * @param id Id of the entity to delete.
     * @throws NotFoundException If entity is not found into storage.
     * @throws StorageException  When another error appends.
     */
    void delete(String id) throws StorageException;

}
