package com.ingensi.data.storeit;

import com.ingensi.data.storeit.entities.StorableEntity;
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

/**
 * Elasticsearch storage implementation.
 */
public class ElasticsearchStorage<T extends StorableEntity> implements Storage<T> {
    public static final int MAX_SIZE = 100;

    private final Client client;
    private final Builder<T> builder;
    private final String index;
    private final String type;

    /**
     * Main elasticsearch storage constructor.
     *
     * @param client  The Elasticsearch Client, used to access and execute queries on the Elasticsearch cluster.
     * @param builder Builder defining methods to convert entities from/to elasticsearch.
     * @param index   Elasticsearch index to use.
     * @param type    Elasticsearch entity type.
     */
    public ElasticsearchStorage(Client client, Builder<T> builder, String index, String type) {
        this.client = client;
        this.builder = builder;
        this.index = index;
        this.type = type;
    }

    @Override
    public Collection<T> list() {
        return extractSearchHitsAsList(
                client.prepareSearch(index)
                        .setTypes(type)
                        .setFrom(0).setSize(MAX_SIZE))
                .stream()
                .map(SearchHit::getSource)
                .map(builder.getFrom()::build)
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String id) throws StorageException {
        return client.prepareGet(index, type, id)
                .execute()
                .actionGet()
                .isExists();
    }

    @Override
    public void create(T entity) throws StorageException {
        create(entity, null);
    }

    @Override
    public void create(T entity, String id) throws StorageException {
        if (id == null || !exists(id)) {
            createOrUpdate(entity, id);
        } else {
            throw new StorageException("Unable to create entity with id " + id + " (not found)");
        }
    }

    @Override
    public T get(String id) throws StorageException {
        GetResponse response = client.prepareGet(index, type, id)
                .execute()
                .actionGet();

        if (!response.isExists()) {
            throw new StorageException("entity with id " + id + " not found");
        }

        return builder.getFrom().build(response.getSource());
    }

    @Override
    public void update(T entity, String id) throws StorageException {
        if (exists(id)) {
            createOrUpdate(entity, id);
        } else {
            throw new StorageException("Unable to update entity with id " + id + " (not found)");
        }
    }

    @Override
    public void delete(String id) throws StorageException {
        DeleteResponse response = client.prepareDelete(index, type, id)
                .execute()
                .actionGet();

        if (!response.isFound()) {
            throw new StorageException("Unable to delete entity with id " + id + " (not found)");
        }
    }

    private void createOrUpdate(T entity, String id) throws StorageException {
        IndexRequestBuilder requestBuilder = client.prepareIndex(index, type);

        if (id != null) {
            requestBuilder.setId(id);
        }

        IndexResponse response = requestBuilder
                .setSource(builder.getTo().build(entity))
                .execute()
                .actionGet();

        if (!response.isCreated()) {
            throw new StorageException("Unable to index entity " + entity + " (not created)");
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
