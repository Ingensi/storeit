# StoreIt

Light Java library used to store entities to various storage.

## How to use!

StoreIt provides an abstraction layer to store, get, list, update and delete all kind of entities.
 
To allow an entity to be stored into a StoreIt storage, it has to implement `StoredEntity` interface, to provides a 
`getId()` method.

### Build with maven

Form the root package, install StoredIt locally:

```shell
mvn install
```

### Add dependency in your project

Add dependency in your `pom.xml` according to the storage your want to use:

```xml
<dependency>
    <groupId>com.ingensi.data</groupId>
    <artifactId>storeit-elasticsearch</artifactId>
    <version>${storeit.version}</version>
</dependency>
```

### Store your first entities

To store an entity with `StoreIt`, you need a `StoredEntity` class. Here is an example with a `User` class:

```java
public class User implements StoredEntity {
    private final String username;
    private final String firstname;
    private final String lastname;
    
    public User(String username, String firstname, String lastname) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getFirstname() {
        return firstname;
    }
    
    public String getLastname() {
        return lastname;
    }
    
    @Override
    public String getId() {
        return username;
    }
}

```

Then you are able to create, get, list, update or delete User into various storage by instantiating a `Storage<User>`:

```java
Storage<User> storage = new xxxStorage<>(
    // DEPENDS ON THE STORAGE IMPLEMENTATION
);

// store new user
storage.store(new User("fbar", "foo", "bar"));
// list all users
Collection<User> users = storage.list();
// update user
storage.store(new User("fbar", "Jason", "Wilson"));
// get specific user
User bob = storage.get("bob");
// delete a user
storage.delete("fbar");
```

## Structure and implementations

Each implemented module contains its own specific documentation.

## Implement your own `Storage`

Look at existing implementations to learn how to implement your own `Storage`. Don't forget to contribute :)