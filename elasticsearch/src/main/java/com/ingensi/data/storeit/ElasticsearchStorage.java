package com.ingensi.data.storeit;

import com.ingensi.data.storeit.entities.StoredEntity;
import com.ingensi.data.storeit.mapper.GenericMapper;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Elasticsearch storage implementation.
 */
public class ElasticsearchStorage<T extends StoredEntity> implements Storage<T> {
    public static final int MAX_SIZE = Integer.MAX_VALUE;

    private final Client client;
    private final GenericMapper<T> mapper;
    private final String index;
    private final String type;

    /**
     * Main elasticsearch storage constructor.
     *
     * @param client The Elasticsearch Client, used to access and execute queries on the Elasticsearch cluster.
     * @param mapper Builder defining methods to convert entities from/to elasticsearch.
     * @param index  Elasticsearch index to use.
     * @param type   Elasticsearch entity type.
     */
    public ElasticsearchStorage(Client client, GenericMapper<T> mapper, String index, String type) {
        this.client = client;
        this.mapper = mapper;
        this.index = index;
        this.type = type;
    }

    @Override
    public Collection<T> list() {
        return stream().collect(Collectors.toList());
    }

    @Override
    public Stream<T> stream() {
        return extractSearchHitsAsList(
                client.prepareSearch(index)
                        .setTypes(type)
                        .setFrom(0).setSize(MAX_SIZE))
                .stream()
                .map(SearchHit::getSource)
                .map(mapper.getFrom()::build);
    }

    @Override
    public boolean exists(String id) throws StorageException {
        return client.prepareGet(index, type, id)
                .execute()
                .actionGet()
                .isExists();
    }

    @Override
    public void store(T entity) throws StorageException {
        store(entity, entity.getId());
    }

    @Override
    public void store(T entity, String id) throws StorageException {
        if (!exists(id)) {
            createOrUpdate(entity, id);
        } else {
            throw new AlreadyExistsException("Unable to create entity with id " + id + " (already exists)");
        }
    }

    @Override
    public T get(String id) throws StorageException {
        GetResponse response = client.prepareGet(index, type, id)
                .execute()
                .actionGet();

        if (!response.isExists()) {
            throw new NotFoundException("entity with id " + id + " not found");
        }

        return mapper.getFrom().build(response.getSource());
    }

    @Override
    public void update(T entity) throws StorageException {
        update(entity, entity.getId());
    }

    @Override
    public void update(T entity, String id) throws StorageException {
        if (exists(entity.getId())) {
            createOrUpdate(entity, id);
        } else {
            throw new NotFoundException("Unable to update entity " + entity + " (not found)");
        }
    }

    @Override
    public void delete(String id) throws StorageException {
        DeleteResponse response = client.prepareDelete(index, type, id)
                .execute()
                .actionGet();

        if (!response.isFound()) {
            throw new NotFoundException("Unable to delete entity with id " + id + " (not found)");
        }
    }

    private void createOrUpdate(T entity, String id) throws StorageException {
        IndexRequestBuilder requestBuilder = client.prepareIndex(index, type);

        if (id != null) {
            requestBuilder.setId(id);
        }

        IndexResponse response = requestBuilder
                .setSource(mapper.getTo().build(entity))
                .execute()
                .actionGet();

        if (!response.isCreated()) {
            throw new InternalStorageException("Unable to index entity " + entity + " (not created)");
        }
    }

    private Collection<SearchHit> extractSearchHitsAsList(SearchRequestBuilder searchRequestBuilder) {
        return Arrays.asList(searchRequestBuilder
                .execute()
                .actionGet()
                .getHits()
                .hits());
    }
}
