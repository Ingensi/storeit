# StoreIt - elasticsearch

Elasticsearch StoreIt implementation.

## Prerequisites

Read the main README to build and install maven artifacts.

## How to use?

Here is a concrete example with the following parameters:

* the common elasticsearch client,
* a `GenericMapper` with `from` and `to` methods defined with lambdas. These methods define how to store and retrieve 
(from/to key/value map) data from and to elasticsearch,
* elasticsearch index "app",
* elasticsearch type "user",

```java
Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
Storage<User> storage = new ElasticsearchStorage<>(
    client, 
    new GenericMapper<>(
        map -> new User(
            map.get("username").toString(),
            map.get("firstname").toString(),
            map.get("lastname").toString()),
        user -> ImmutableMap.of(
            "username", user.getUsername(),
            "firstname", user.getFirstname(),
            "lastname", user.getLastname())),
    "app", 
    "user"
);
```