package com.ingensi.data.storeit;

import java.util.Collection;

/**
 * This interface defines .
 */
public interface Storage<T> {
    /**
     * List all entities.
     *
     * @return A collection of entities.
     */
    Collection<T> list();

    /**
     * Check if an entity exists from its ID.
     *
     * @param id Id of the entity.
     * @return True if it exists, else false.
     * @throws StorageException In case of error.
     */
    boolean exists(String id) throws StorageException;

    /**
     * Create new entity.
     *
     * @param entity The entity to create.
     * @throws StorageException In case of error.
     */
    void create(T entity) throws StorageException;

    /**
     * Create new entity with custom id.
     *
     * @param entity The entity to create.
     * @param id     Id of the entity.
     * @throws StorageException In case of error.
     */
    void create(T entity, String id) throws StorageException;

    /**
     * Get the id.
     *
     * @param id Id of the entity.
     * @return The retrieved entity.
     * @throws StorageException In case of error.
     */
    T get(String id) throws StorageException;


    /**
     * Update the entity.
     *
     * @param entity The entity to update.
     * @param id     Id of the entity.
     * @throws StorageException In case of error.
     */
    void update(T entity, String id) throws StorageException;

    /**
     * Delete the entity.
     *
     * @param id Id of the entity to delete.
     * @throws StorageException In case of error.
     */
    void delete(String id) throws StorageException;

}
