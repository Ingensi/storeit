package com.ingensi.data.storeit;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ingensi.data.storeit.entities.StoredEntity;
import com.ingensi.data.storeit.mapper.GenericMapper;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for Elasticsearch storage client.
 */
public class ElasticsearchStorageTest {
    final Client client = mock(Client.class);
    final GenericMapper<FakeEntity> mapper = new GenericMapper<>(
            e -> new FakeEntity(e.get("id").toString()),
            e -> ImmutableMap.of("id", e.getId())
    );


    @Before
    public void setUp() throws Exception {
        reset(client);
    }

    @Test
    public void shouldListEntitiesAsCollection() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // a mocked response returning a SearchHits containing two hits
        SearchResponse response = mock(SearchResponse.class);
        SearchHit hit1 = mock(SearchHit.class);
        when(hit1.getSource()).thenReturn(ImmutableMap.of("id", "1234"));
        SearchHit hit2 = mock(SearchHit.class);
        when(hit2.getSource()).thenReturn(ImmutableMap.of("id", "1234"));
        SearchHit[] hits = new SearchHit[]{hit1, hit2};
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.hits()).thenReturn(hits);
        when(response.getHits()).thenReturn(searchHits);

        // a mocked client avoiding NPE with request builder
        ListenableActionFuture<SearchResponse> action = mock(ListenableActionFuture.class);
        when(action.actionGet()).thenReturn(response);
        SearchRequestBuilder reqBuilder = getMockedSearchRequestBuilder();
        when(reqBuilder.execute()).thenReturn(action);

        // a mocked elasticsearch client
        when(client.prepareSearch(anyString())).thenReturn(reqBuilder);

        // a storage getting mocked types, a basic lambda (map search hit to new fakeEntity on ID), and index/type names
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // WHEN
        Collection<FakeEntity> output = storage.list();

        // THEN
        assertThat(output).isEqualTo(Lists.newArrayList(
                mapper.getFrom().build(hit1.getSource()),
                mapper.getFrom().build(hit2.getSource())
        ));

        verify(client, times(1)).prepareSearch(index);
        verify(reqBuilder, times(1)).setTypes(type);
        verify(reqBuilder, times(1)).setSize(ElasticsearchStorage.MAX_SIZE);
        verify(reqBuilder, times(1)).setFrom(0);
        verify(reqBuilder, times(1)).execute();
    }

    @Test
    public void shouldListEntitiesAsStream() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // a mocked response returning a SearchHits containing two hits
        SearchResponse response = mock(SearchResponse.class);
        SearchHit hit1 = mock(SearchHit.class);
        when(hit1.getSource()).thenReturn(ImmutableMap.of("id", "1234"));
        SearchHit hit2 = mock(SearchHit.class);
        when(hit2.getSource()).thenReturn(ImmutableMap.of("id", "1234"));
        SearchHit[] hits = new SearchHit[]{hit1, hit2};
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.hits()).thenReturn(hits);
        when(response.getHits()).thenReturn(searchHits);

        // a mocked client avoiding NPE with request builder
        ListenableActionFuture<SearchResponse> action = mock(ListenableActionFuture.class);
        when(action.actionGet()).thenReturn(response);
        SearchRequestBuilder reqBuilder = getMockedSearchRequestBuilder();
        when(reqBuilder.execute()).thenReturn(action);

        // a mocked elasticsearch client
        when(client.prepareSearch(anyString())).thenReturn(reqBuilder);

        // a storage getting mocked types, a basic lambda (map search hit to new fakeEntity on ID), and index/type names
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // WHEN
        Stream<FakeEntity> output = storage.stream();

        // THEN
        assertThat(output.collect(Collectors.toList())).isEqualTo(Lists.newArrayList(
                mapper.getFrom().build(hit1.getSource()),
                mapper.getFrom().build(hit2.getSource())
        ));

        verify(client, times(1)).prepareSearch(index);
        verify(reqBuilder, times(1)).setTypes(type);
        verify(reqBuilder, times(1)).setSize(ElasticsearchStorage.MAX_SIZE);
        verify(reqBuilder, times(1)).setFrom(0);
        verify(reqBuilder, times(1)).execute();
    }

    @Test
    public void shouldTestIfEntityExistsAndReturnTrue() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";
        String id = "123456";

        // an elasticsearch getresponse returning true on isExists
        GetResponse response = mock(GetResponse.class);
        when(response.isExists()).thenReturn(true);

        // an elasticsearch client return a mocked request builder with mocked action and previously defined response injected
        ListenableActionFuture<GetResponse> action = mock(ListenableActionFuture.class);
        when(action.actionGet()).thenReturn(response);
        GetRequestBuilder reqBuilder = mock(GetRequestBuilder.class);
        when(reqBuilder.execute()).thenReturn(action);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(reqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // WHEN
        boolean output = storage.exists(id);

        // THEN
        assertThat(output).isTrue();

        verify(client, times(1)).prepareGet(index, type, id);
        verify(reqBuilder, times(1)).execute();
    }

    @Test
    public void shouldStoreAnEntity() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // INDEX MOCKING: a mocked index response saying that entity indexed
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.isCreated()).thenReturn(true);

        // INDEX MOCKING: a mocked index request builder
        ListenableActionFuture<IndexResponse> indexAction = mock(ListenableActionFuture.class);
        when(indexAction.actionGet()).thenReturn(indexResponse);
        IndexRequestBuilder indexReqBuilder = getMockedIndexRequestBuilder();
        when(indexReqBuilder.execute()).thenReturn(indexAction);
        when(client.prepareIndex(anyString(), anyString())).thenReturn(indexReqBuilder);

        // when creating an entity with custom ID, we have to mock the id check part
        // GET MOCKING:a mocked get response saying that entity does not already exist
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.isExists()).thenReturn(false);

        // GET MOCKING: a mocked get request builder
        ListenableActionFuture<GetResponse> getAction = mock(ListenableActionFuture.class);
        when(getAction.actionGet()).thenReturn(getResponse);
        GetRequestBuilder getReqBuilder = mock(GetRequestBuilder.class);
        when(getReqBuilder.execute()).thenReturn(getAction);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(getReqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // an entity
        FakeEntity entity = new FakeEntity("123456789");

        // WHEN
        storage.store(entity);

        // THEN
        verify(client, times(1)).prepareIndex(index, type);
        verify(indexReqBuilder, times(1)).setId(entity.getId());
        verify(indexReqBuilder, times(1)).setSource(mapper.getTo().build(entity));
        verify(indexReqBuilder, times(1)).execute();
    }

    @Test
    public void shouldStoreAnEntityWithCustomId() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // INDEX MOCKING: a mocked index response saying that entity indexed
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.isCreated()).thenReturn(true);

        // INDEX MOCKING: a mocked index request builder
        ListenableActionFuture<IndexResponse> indexAction = mock(ListenableActionFuture.class);
        when(indexAction.actionGet()).thenReturn(indexResponse);
        IndexRequestBuilder indexReqBuilder = getMockedIndexRequestBuilder();
        when(indexReqBuilder.execute()).thenReturn(indexAction);
        when(client.prepareIndex(anyString(), anyString())).thenReturn(indexReqBuilder);

        // when creating an entity with custom ID, we have to mock the id check part
        // GET MOCKING:a mocked get response saying that entity does not already exist
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.isExists()).thenReturn(false);

        // GET MOCKING: a mocked get request builder
        ListenableActionFuture<GetResponse> getAction = mock(ListenableActionFuture.class);
        when(getAction.actionGet()).thenReturn(getResponse);
        GetRequestBuilder getReqBuilder = mock(GetRequestBuilder.class);
        when(getReqBuilder.execute()).thenReturn(getAction);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(getReqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // an entity
        FakeEntity entity = new FakeEntity("123456789");
        // a custom entity id
        String id = "fakeId";

        // WHEN
        storage.store(entity, id);

        // THEN
        verify(client, times(1)).prepareIndex(index, type);
        verify(indexReqBuilder, times(1)).setId(id);
        verify(indexReqBuilder, times(1)).setSource(mapper.getTo().build(entity));
        verify(indexReqBuilder, times(1)).execute();
    }

    @Test
    public void shouldThrowAnAlreadyExistsExceptionWhenCreatingAnEntityWithCustomIdButItAlreadyExists() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // GET MOCKING:a mocked get response saying that entity already exists
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.isExists()).thenReturn(true);

        // GET MOCKING: a mocked get request builder
        ListenableActionFuture<GetResponse> getAction = mock(ListenableActionFuture.class);
        when(getAction.actionGet()).thenReturn(getResponse);
        GetRequestBuilder getReqBuilder = mock(GetRequestBuilder.class);
        when(getReqBuilder.execute()).thenReturn(getAction);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(getReqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // an entity
        FakeEntity entity = new FakeEntity("123456789");

        try {
            // WHEN
            storage.store(entity, entity.getId());
            throw fail("should throw a AlreadyExistsException");
        } catch (AlreadyExistsException e) {
            // THEN
            verify(client, times(1)).prepareGet(index, type, entity.getId());
            verify(getReqBuilder, times(1)).execute();
        }
    }

    @Test
    public void shouldThrowAnInternalStorageExceptionWhenEntityCreationFails() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // INDEX MOCKING: a mocked index response saying that entity indexation failed
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.isCreated()).thenReturn(false);

        // INDEX MOCKING: a mocked index request builder
        ListenableActionFuture<IndexResponse> indexAction = mock(ListenableActionFuture.class);
        when(indexAction.actionGet()).thenReturn(indexResponse);
        IndexRequestBuilder indexReqBuilder = getMockedIndexRequestBuilder();
        when(indexReqBuilder.execute()).thenReturn(indexAction);
        when(client.prepareIndex(anyString(), anyString())).thenReturn(indexReqBuilder);

        // when creating an entity with custom ID, we have to mock the id check part
        // GET MOCKING:a mocked get response saying that entity does not already exist
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.isExists()).thenReturn(false);

        // GET MOCKING: a mocked get request builder
        ListenableActionFuture<GetResponse> getAction = mock(ListenableActionFuture.class);
        when(getAction.actionGet()).thenReturn(getResponse);
        GetRequestBuilder getReqBuilder = mock(GetRequestBuilder.class);
        when(getReqBuilder.execute()).thenReturn(getAction);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(getReqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // an entity
        FakeEntity entity = new FakeEntity("123456789");

        try {
            // WHEN
            storage.store(entity);
            throw fail("should throw a InternalStorageException");
        } catch (InternalStorageException e) {
            // THEN
            verify(client, times(1)).prepareIndex(index, type);
            verify(indexReqBuilder, times(1)).setSource(mapper.getTo().build(entity));
            verify(indexReqBuilder, times(1)).execute();
        }
    }

    @Test
    public void shouldGetAnEntity() throws Exception {
        // GIVEN
        // a fake index, type name and id
        String index = "fakeindex";
        String type = "faketype";
        String id = "123456789";

        // a mocked response returning that entity exists
        GetResponse response = mock(GetResponse.class);
        when(response.isExists()).thenReturn(true);
        when(response.getSource()).thenReturn(ImmutableMap.of("id", id));

        // a mocked get request builder
        ListenableActionFuture<GetResponse> action = mock(ListenableActionFuture.class);
        when(action.actionGet()).thenReturn(response);
        GetRequestBuilder reqBuilder = mock(GetRequestBuilder.class);
        when(reqBuilder.execute()).thenReturn(action);

        // a mocked client
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(reqBuilder);

        // a storage getting mocked types, a basic lambda (map search hit to new fakeEntity on ID), and index/type names
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );
        // WHEN
        FakeEntity entity = storage.get(id);

        // THEN
        assertThat(entity).isEqualTo(new FakeEntity(id));

        verify(client, times(1)).prepareGet(index, type, id);
        verify(reqBuilder, times(1)).execute();
    }

    @Test
    public void shouldThrowANotFoundExceptionStorageExceptionWhenGettingAnEntityThatDoesNotExist() throws Exception {
        // GIVEN
        // a fake index, type name and id
        String index = "fakeindex";
        String type = "faketype";
        String id = "123456789";

        // a mocked response returning that entity exists
        GetResponse response = mock(GetResponse.class);
        when(response.isExists()).thenReturn(false);

        // a mocked get request builder
        ListenableActionFuture<GetResponse> action = mock(ListenableActionFuture.class);
        when(action.actionGet()).thenReturn(response);
        GetRequestBuilder reqBuilder = mock(GetRequestBuilder.class);
        when(reqBuilder.execute()).thenReturn(action);

        // a mocked client
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(reqBuilder);

        // a storage getting mocked types, a basic lambda (map search hit to new fakeEntity on ID), and index/type names
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );
        try {
            // WHEN
            storage.get(id);
            throw fail("should throw a NotFoundException");
        } catch (NotFoundException e) {
            // THEN
            verify(client, times(1)).prepareGet(index, type, id);
            verify(reqBuilder, times(1)).execute();
        }
    }

    @Test
    public void shouldUpdateAnEntity() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // INDEX MOCKING: a mocked index response saying that entity indexed
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.isCreated()).thenReturn(true);

        // INDEX MOCKING: a mocked index request builder
        ListenableActionFuture<IndexResponse> indexAction = mock(ListenableActionFuture.class);
        when(indexAction.actionGet()).thenReturn(indexResponse);
        IndexRequestBuilder indexReqBuilder = getMockedIndexRequestBuilder();
        when(indexReqBuilder.execute()).thenReturn(indexAction);
        when(client.prepareIndex(anyString(), anyString())).thenReturn(indexReqBuilder);

        // when updating an entity, we have to mock the id check part
        // GET MOCKING:a mocked get response saying that entity exists
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.isExists()).thenReturn(true);

        // GET MOCKING: a mocked get request builder
        ListenableActionFuture<GetResponse> getAction = mock(ListenableActionFuture.class);
        when(getAction.actionGet()).thenReturn(getResponse);
        GetRequestBuilder getReqBuilder = mock(GetRequestBuilder.class);
        when(getReqBuilder.execute()).thenReturn(getAction);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(getReqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // an entity
        FakeEntity entity = new FakeEntity("123456789");

        // WHEN
        storage.update(entity);

        // THEN
        verify(client, times(1)).prepareIndex(index, type);
        verify(indexReqBuilder, times(1)).setId(entity.getId());
        verify(indexReqBuilder, times(1)).setSource(mapper.getTo().build(entity));
        verify(indexReqBuilder, times(1)).execute();
    }

    @Test
    public void shouldUpdateAnEntityWithCustomId() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";
        String id = "123456789";

        // INDEX MOCKING: a mocked index response saying that entity indexed
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.isCreated()).thenReturn(true);

        // INDEX MOCKING: a mocked index request builder
        ListenableActionFuture<IndexResponse> indexAction = mock(ListenableActionFuture.class);
        when(indexAction.actionGet()).thenReturn(indexResponse);
        IndexRequestBuilder indexReqBuilder = getMockedIndexRequestBuilder();
        when(indexReqBuilder.execute()).thenReturn(indexAction);
        when(client.prepareIndex(anyString(), anyString())).thenReturn(indexReqBuilder);

        // when updating an entity, we have to mock the id check part
        // GET MOCKING:a mocked get response saying that entity exists
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.isExists()).thenReturn(true);

        // GET MOCKING: a mocked get request builder
        ListenableActionFuture<GetResponse> getAction = mock(ListenableActionFuture.class);
        when(getAction.actionGet()).thenReturn(getResponse);
        GetRequestBuilder getReqBuilder = mock(GetRequestBuilder.class);
        when(getReqBuilder.execute()).thenReturn(getAction);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(getReqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // an entity
        FakeEntity entity = new FakeEntity("123456789");

        // WHEN
        storage.update(entity, id);

        // THEN
        verify(client, times(1)).prepareIndex(index, type);
        verify(indexReqBuilder, times(1)).setId(id);
        verify(indexReqBuilder, times(1)).setSource(mapper.getTo().build(entity));
        verify(indexReqBuilder, times(1)).execute();
    }

    @Test
    public void shouldThrowANotFoundExceptionWhenUpdatingAnEntityWithUnknownId() throws Exception {
        // GIVEN
        // a fake index and type names
        String index = "fakeindex";
        String type = "faketype";

        // GET MOCKING:a mocked get response saying that entity does not exist
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.isExists()).thenReturn(false);

        // GET MOCKING: a mocked get request builder
        ListenableActionFuture<GetResponse> getAction = mock(ListenableActionFuture.class);
        when(getAction.actionGet()).thenReturn(getResponse);
        GetRequestBuilder getReqBuilder = mock(GetRequestBuilder.class);
        when(getReqBuilder.execute()).thenReturn(getAction);
        when(client.prepareGet(anyString(), anyString(), anyString())).thenReturn(getReqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // an entity
        FakeEntity entity = new FakeEntity("123456789");

        try {
            // WHEN
            storage.update(entity);
            throw fail("should throw a NotFoundException");
        } catch (NotFoundException e) {
            // THEN
            verify(client, times(1)).prepareGet(index, type, entity.getId());
            verify(getReqBuilder, times(1)).execute();
        }
    }

    @Test
    public void shouldDeleteAnEntity() throws Exception {
        // GIVEN
        // a fake index, a type and an entity id
        String index = "fakeindex";
        String type = "faketype";
        String id = "123456789";

        // a mocked delete response saying that entity has been deleted
        DeleteResponse response = mock(DeleteResponse.class);
        when(response.isFound()).thenReturn(true);

        // a mocked delete request builder
        ListenableActionFuture<DeleteResponse> action = mock(ListenableActionFuture.class);
        when(action.actionGet()).thenReturn(response);
        DeleteRequestBuilder reqBuilder = mock(DeleteRequestBuilder.class);
        when(reqBuilder.execute()).thenReturn(action);

        // a mocked elasticsearch client
        when(client.prepareDelete(anyString(), anyString(), anyString())).thenReturn(reqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        // WHEN
        storage.delete(id);

        // THEN
        verify(client, times(1)).prepareDelete(index, type, id);
        verify(reqBuilder, times(1)).execute();
    }

    @Test
    public void shouldThrowANotFoundExceptionWhenDeletingAnEntityThatDoesNotExist() throws Exception {
        // GIVEN
        // a fake index, a type and an entity id
        String index = "fakeindex";
        String type = "faketype";
        String id = "123456789";

        // a mocked delete response saying that entity has been deleted
        DeleteResponse response = mock(DeleteResponse.class);
        when(response.isFound()).thenReturn(false);

        // a mocked delete request builder
        ListenableActionFuture<DeleteResponse> action = mock(ListenableActionFuture.class);
        when(action.actionGet()).thenReturn(response);
        DeleteRequestBuilder reqBuilder = mock(DeleteRequestBuilder.class);
        when(reqBuilder.execute()).thenReturn(action);

        // a mocked elasticsearch client
        when(client.prepareDelete(anyString(), anyString(), anyString())).thenReturn(reqBuilder);

        // an elasticsearch storage
        ElasticsearchStorage<FakeEntity> storage = new ElasticsearchStorage<>(
                client,
                mapper,
                index,
                type
        );

        try {
            // WHEN
            storage.delete(id);
            throw fail("should throw a NotFoundException");
        } catch (NotFoundException e) {
            // THEN
            verify(client, times(1)).prepareDelete(index, type, id);
            verify(reqBuilder, times(1)).execute();
        }
    }

    private IndexRequestBuilder getMockedIndexRequestBuilder() {
        IndexRequestBuilder reqBuilder = mock(IndexRequestBuilder.class);
        when(reqBuilder.setSource(anyMapOf(String.class, Object.class))).thenReturn(reqBuilder);
        when(reqBuilder.setId(anyString())).thenReturn(reqBuilder);
        return reqBuilder;
    }

    private SearchRequestBuilder getMockedSearchRequestBuilder() {
        SearchRequestBuilder reqBuilder = mock(SearchRequestBuilder.class);
        when(reqBuilder.setTypes(anyString())).thenReturn(reqBuilder);
        when(reqBuilder.setFrom(anyInt())).thenReturn(reqBuilder);
        when(reqBuilder.setSize(anyInt())).thenReturn(reqBuilder);
        return reqBuilder;
    }

    /**
     * Class declaration of a storableEntity.
     */
    private class FakeEntity implements StoredEntity {
        private final String id;

        private FakeEntity(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FakeEntity)) return false;
            FakeEntity that = (FakeEntity) o;
            return Objects.equal(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }
}
