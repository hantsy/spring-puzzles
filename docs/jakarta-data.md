# Integrating Jakarta Data with Spring 

As a Java backend developer, you are likely familiar with the `Repository` pattern and the related facilities provided by popular frameworks such as [Spring Data](https://spring.io/projects/spring-data), [Quarkus ORM Panache](https://quarkus.io/guides/hibernate-orm-panache), and [Micronaut Data](https://micronaut-projects.github.io/micronaut-data/latest/guide/). Each framework has its own advantages and limitations.

[Jakarta Data](https://jakarta.ee/specifications/data/) is a new Jakarta EE specification that aims to create universal interfaces for accessing both relational and non-relational databases.

> [!NOTE] 
> Jakarta Data 1.0 is planned to be included in the upcoming [Jakarta EE 11](https://jakarta.ee/specifications/platform/11/).

Currently, popular Jakarta Persistence providers, including [Hibernate](https://hibernate.org) and [Eclipse Link](https://eclipse.dev/eclipselink/), have implemented this specification in its early stages (since Jakarta Data 1.0 has not been released yet).

In this post, we will use the latest Hibernate version to integrate Jakarta Data into a Spring application.

Generate a simple Spring web project via [Spring Initializr](https://start.spring.io).

* Language: Java 21
* Dependencies: Web, ORM, Lombok, Postgres
* Build: Maven

Or just create a simple Maven Java project.

> [!NOTE]
> Check out the [sample code](https://github.com/hantsy/spring6-sandbox/blob/master/jakarta-data/) used to demonstrate Jakarta Data in this post.

Add the following dependencies to your project.

```xml
// ...
<hibernate.version>6.6.0.Alpha1</hibernate.version>

<dependencies>
    // ...
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>${hibernate.version}</version>
    </dependency>
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-jpamodelgen</artifactId>
        <version>${hibernate.version}</version>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>jakarta.data</groupId>
        <artifactId>jakarta.data-api</artifactId>
        <version>1.0.0-RC1</version>
    </dependency>
</dependencies>
```

Declare a Hibernate `StatelessSession` as a bean.

```java
@Configuration
public class DataConfig {

    @Bean
    public StatelessSession statelessSession(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return entityManagerFactoryBean.getObject().unwrap(SessionFactory.class).openStatelessSession();
    }
}
```

Unlike Spring Data JPA, which depends on general JPA stateful persistence, Hibernate implements Jakarta Data using `StatelessSession`, meaning there is no first-level cache. Every change applied to the database will be flushed immediately.

> [!NOTE] 
> For more information about Jakarta Data support in Hibernate, read the new [Hibernate Data Repositories](https://docs.jboss.org/hibernate/orm/6.6/repositories/html_single/Hibernate_Data_Repositories.html).

Create a simple `@Entity` class for testing purposes.

```java
@Entity
@Table(name = "posts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
```

In the above code, the `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor` annotations are from the [Lombok project](https://projectlombok.org/), which modifies the compiled `Post.class` to generate getters/setters, `equals`/`hashCode`, an inner builder class, a static build method, and two constructors at compile time.

The `@CreationTimestamp` annotation is from Hibernate and sets the current timestamp when inserting an entity instance. Other annotations are from Jakarta Persistence and are simple and easy to understand.

Jakarta Data also provides a series of `Repository` interfaces: `DataRepository`, `BasicRepository`, `CrudRepository`, `PageableRepository`, etc. If you have experience with Spring Data JPA, you should be familiar with the `Repository` interface inheritance in Spring Data projects.

Create a `Repository` interface for the `Post` entity class we just created. Extend it from `CrudRepository`, which has a collection of built-in methods similar to the popular Spring Data `CrudRepository`.

```java
@jakarta.data.repository.Repository
public interface PostRepository extends CrudRepository<Post, UUID> {
}
```

Annotate `PostRepository` with `@Repository` to indicate it is a Jakarta Data Repository interface.

> [!NOTE] 
> The `@Repository` annotation here is from the package `jakarta.data.repository`. Do not use the one provided by Spring.

The Jakarta Data specification requires implementors to process the `Repository` at compile time. The Hibernate annotation processor (from the `hibernate-jpamodelgen` Maven module) will scan the `@Repository` interface and generate the implementation class for the interface.

To make Lombok and other compiler annotation processors work seamlessly, configure them in order under the `configuration/annotationProcessorPaths` node of the Maven compiler plugin.

```xml
<build>
    <finalName>demo</finalName>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <annotationProcessorPath>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </annotationProcessorPath>
                    <annotationProcessorPath>
                        <groupId>org.hibernate.orm</groupId>
                        <artifactId>hibernate-jpamodelgen</artifactId>
                        <version>${hibernate.version}</version>
                    </annotationProcessorPath>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        // ...
```

Next, open a terminal window, switch to the project root, and run the following command to compile the entire project.

```bash
mvn clean compile
```

After the compilation is complete, explore the generated code in the `target/generated-sources/annotations` folder under the project root.

> [!NOTE] 
> If this folder is not recognized by your IDE, add it manually as a Source Set.

Besides the generated meta models for the JPA entity classes, there is a new `PostRepository_.java` in the `com.example.demo.repository` package.

Open it in your editor. It should look like this:

```java 
@Generated("org.hibernate.processor.HibernateProcessor")
public class PostRepository_ implements PostRepository {

	/**
	 * Find {@link Post} by {@link Post#id id}.
	 *
	
 * @see com.example.demo.repository.PostRepository#deleteById(UUID)
	 **/
	@Override
	public void deleteById(@Nonnull UUID id) {
		if (id == null) throw new IllegalArgumentException("Null id");
		var _builder = session.getFactory().getCriteriaBuilder();
		var _query = _builder.createCriteriaDelete(Post.class);
		var _entity = _query.from(Post.class);
		_query.where(
				_builder.equal(_entity.get(Post_.id), id)
		);
		try {
			session.createMutationQuery(_query)
				.executeUpdate();
		}
		catch (NoResultException exception) {
			throw new EmptyResultException(exception.getMessage(), exception);
		}
		catch (NonUniqueResultException exception) {
			throw new jakarta.data.exceptions.NonUniqueResultException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	protected @Nonnull StatelessSession session;

	@Inject
	public PostRepository_(@Nonnull StatelessSession session) {
		this.session = session;
	}

	public @Nonnull StatelessSession session() {
		return session;
	}

	@Override
	public void delete(@Nonnull Post entity) {
		if (entity == null) throw new IllegalArgumentException("Null entity");
		try {
			session.delete(entity);
		}
		catch (StaleStateException exception) {
			throw new OptimisticLockingFailureException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	@Override
	public void deleteAll(@Nonnull List<? extends Post> entities) {
		if (entities == null) throw new IllegalArgumentException("Null entities");
		try {
			for (var _entity : entities) {
				session.delete(_entity);
			}
		}
		catch (StaleStateException exception) {
			throw new OptimisticLockingFailureException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	/**
	 * Find {@link Post}.
	 *
	 * @see com.example.demo.repository.PostRepository#findAll(PageRequest,Order)
	 **/
	@Override
	public Page<Post> findAll(PageRequest pageRequest, Order<Post> sortBy) {
		var _builder = session.getFactory().getCriteriaBuilder();
		var _query = _builder.createQuery(Post.class);
		var _entity = _query.from(Post.class);
		_query.where(
		);
		var _orders = new ArrayList<org.hibernate.query.Order<? super Post>>();
		for (var _sort : sortBy.sorts()) {
			_orders.add(by(Post.class, _sort.property(),
							_sort.isAscending() ? ASCENDING : DESCENDING,
							_sort.ignoreCase()));
		}
		try {
			long _totalResults = 
					pageRequest.requestTotal()
							? session.createSelectionQuery(_query)
									.getResultCount()
							: -1;
			var _results = session.createSelectionQuery(_query)
				.setFirstResult((int) (pageRequest.page()-1) * pageRequest.size())
				.setMaxResults(pageRequest.size())
				.setOrder(_orders)
				.getResultList();
			return new PageRecord(pageRequest, _results, _totalResults);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	@Override
	public Post update(@Nonnull Post entity) {
		if (entity == null) throw new IllegalArgumentException("Null entity");
		try {
			session.update(entity);
			return entity;
		}
		catch (StaleStateException exception) {
			throw new OptimisticLockingFailureException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	@Override
	public Post save(@Nonnull Post entity) {
		if (entity == null) throw new IllegalArgumentException("Null entity");
		try {
			session.upsert(entity);
			return entity;
		}
		catch (StaleStateException exception) {
			throw new OptimisticLockingFailureException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	/**
	 * Find {@link Post}.
	 *
	 * @see com.example.demo.repository.PostRepository#findAll()
	 **/
	@Override
	public Stream<Post> findAll() {
		var _builder = session.getFactory().getCriteriaBuilder();
		var _query = _builder.createQuery(Post.class);
		var _entity = _query.from(Post.class);
		_query.where(
		);
		try {
			return session.createSelectionQuery(_query)
				.getResultStream();
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	@Override
	public List updateAll(@Nonnull List entities) {
		if (entities == null) throw new IllegalArgumentException("Null entities");
		try {
			for (var _entity : entities) {
				session.update(_entity);
			}
			return entities;
		}
		catch (StaleStateException exception) {
			throw new OptimisticLockingFailureException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}


	@Override
	public List saveAll(@Nonnull List entities) {
		if (entities == null) throw new IllegalArgumentException("Null entities");
		try {
			for (var _entity : entities) {
				session.upsert(_entity);
			}
			return entities;
		}
		catch (StaleStateException exception) {
			throw new OptimisticLockingFailureException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	@Override
	public List insertAll(@Nonnull List entities) {
		if (entities == null) throw new IllegalArgumentException("Null entities");
		try {
			for (var _entity : entities) {
				session.insert(_entity);
			}
			return entities;
		}
		catch (ConstraintViolationException exception) {
			throw new EntityExistsException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	/**
	 * Find {@link Post} by {@link Post#id id}.
	 *
	 * @see com.example.demo.repository.PostRepository#findById(UUID)
	 **/
	@Override
	public Optional<Post> findById(@Nonnull UUID id) {
		if (id == null) throw new IllegalArgumentException("Null id");
		try {
			return ofNullable(session.get(Post.class, id));
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	@Override
	public Post insert(@Nonnull Post entity) {
		if (entity == null) throw new IllegalArgumentException("Null entity");
		try {
			session.insert(entity);
			return entity;
		}
		catch (ConstraintViolationException exception) {
			throw new EntityExistsException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}
}
```

As you can see, there is a constructor injection that depends on a Hibernate `StatelessSession` bean.

```java
public class PostRepository_ implements PostRepository {

    // ...
    @Inject
    public PostRepository_(@Nonnull StatelessSession session) {
        this.session = session;
    }
```

In the Jakarta EE/CDI environment, the Jakarta Data `@Repository` can be recognized as CDI beans directly. In a Spring application, we have to declare it as a Spring `@Bean` in the configuration.

```java
@Configuration
public class DataConfig {
    // ...
    @Bean
    public PostRepository postRepository(StatelessSession statelessSession) {
        return new PostRepository_(statelessSession);
    }

    // ...
}
```

Now you can inject a `PostRepository` into other beans freely.

```java
@Autowired
PostRepository posts;

var data = List.of(
        Post.builder().title("test").content("content").status(Status.PENDING_MODERATION).build(),
        Post.builder().title("test1").content("content1").build()
    );
data.forEach(this.posts::insert);

var results = posts.findAll();
assertThat(results.toList().size()).isEqualTo(2);
```

Spring Data JPA allows you to create custom derived queries through a method naming convention. For example, to query all posts into a `List` by a provided status parameter, you can simply add a method like the following to the Repository interface.

```java
public interface PostRepository<Post, UUID> extends JpaRepository {

    List<Post> findByStatus(Status status);
}
```

In the Jakarta Data world, it provides a collection of annotations (`@Query`, `@Save`, `@Insert`, `@Update`, `@Delete`, `@Find`, `@GroupBy`, `@OrderBy`, etc.) to achieve this customization.

```java 
@jakarta.data.repository.Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

    @Find
    @OrderBy("createdAt")
    List<Post> byStatus(Status status);

}    
```

After the project is compiled, it will generate the implementation like this:

```java
/**
 * Find {@link Post} by {@link Post#status status}.
 *
 * @see com.example.demo.repository.PostRepository#byStatus(Status)
**/
@Override
public List<Post> byStatus(Status status) {
    var _builder = session.getFactory().getCriteriaBuilder();
    var _query = _builder.createQuery(Post.class);
    var _entity = _query.from(Post.class);
    _query.where(
            status==null
                ? _entity.get(Post_.status).isNull()
                : _builder.equal(_entity.get(Post_.status), status)
    );
    var _orders = new ArrayList<org.hibernate.query.Order<? super Post>>();
    _orders.add(by(Post.class, "createdAt", ASCENDING, false));
    try {
        return session.createSelectionQuery(_query)
            .setOrder(_orders)
            .getResultList();
    }
    catch (PersistenceException exception) {
        throw new DataException(exception.getMessage(), exception);
    }
}
```

The following is an example of using it:

```java
var resultsByKeyword = posts.byStatus(Status.PENDING_MODERATION);
assertThat(resultsByKeyword.size()).isEqualTo(1);
```

More freely, Jakarta Data allows you to use these annotations to build your repository without extending Repository interfaces.

Create a simple interface `Blogger` and annotate it with `@Repository`.

```java
@Repository
public interface Blogger {

    @Query("""
            SELECT p.id, p.title FROM Post p 
            WHERE p.status = 'PUBLISHED'
            ORDER BY p.createdAt DESC
            """)
    List<PostSummary> allPublishedPosts();

    @Insert
    Post newPost(Post post);
}
```

Compile the project again, and it will generate a `Blogger_` implementation class.

```java
@Generated("org.hibernate.processor.HibernateProcessor")
public class Blogger_ implements Blogger {

    static final String ALL_PUBLISHED_POSTS = "SELECT p.id, p.title FROM Post p\nWHERE p.status = 'PUBLISHED'\nORDER BY p.createdAt DESC\n";


	/**
	 * Execute the query {@value #ALL_PUBLISHED_POSTS}.
	 *
	 * @see com.example.demo.Blogger#allPublishedPosts()
	 **/
	@Override
	public List<PostSummary> allPublishedPosts() {
		try {
			return session.createSelectionQuery(ALL_PUBLISHED_POSTS, PostSummary.class)
				.getResultList();
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}

	protected @Nonnull StatelessSession session;

	@Inject
	public Blogger_(@Nonnull StatelessSession session) {
		this.session = session;
	}

	public @Nonnull StatelessSession session() {
		return session;
	}

	@Override
	public Post newPost(@Nonnull Post post) {
		if (post == null) throw new IllegalArgumentException("Null post");
		try {
			session.insert(post);
			return post;
		}
		catch (ConstraintViolationException exception) {
			throw new EntityExistsException(exception.getMessage(), exception);
		}
		catch (PersistenceException exception) {
			throw new DataException(exception.getMessage(), exception);
		}
	}	
}
```

Declare it as a Spring `@Bean` in the configuration as well.

```java
// in DataConfig.java
@Bean
public Blogger blogger(StatelessSession statelessSession) {
    return new Blogger_(statelessSession);
}
```

The following insert and query example uses this `Blogger` instead.

```java
var data = Post.builder().title("test").content("test content").status(Status.DRAFT).build();
var saved = blogger.newPost(data);
assertThat(this.posts.findById(saved.getId()).isPresent()).isTrue();

saved.setStatus(Status.PUBLISHED);
posts.update(saved);

List<PostSummary> allPublished = blogger.allPublishedPosts();
assertThat(allPublished.size()).isEqualTo(1);
```

To experience more Jakarta Data features yourself, please check the complete [`PostRepositoryTest`](https://github.com/hantsy/spring6-sandbox/blob/master/jakarta-data/src/test/java/com/example/demo/PostRepositoryTest.java).

When I prepared the sample code, I tried to add a `@BeforeEach` hook method as follows to clean up the sample data for every test.

```java
@SneakyThrows
@BeforeEach
public void setup() {
    var deleted = posts.deleteAll();
    log.debug("deleted posts: {}", deleted);
}
```

And add a `deleteAll` method as below into the `PostRepository` interface.

```java
@Delete
@Transactional
long deleteAll();
```

When compiling the project and running the tests, it will throw a Jakarta Data `TransactionException`.

I have tried to configure `HibernateTransactionManager` and `JpaTransactionManager` respectively, but neither resolves the issue. Spring still does not provide transaction support for Hibernate `StatelessSession`, see issue [spring-framework#7184](https://github.com/spring-projects/spring-framework/issues/7184). A possible solution is adding Spring Transaction support for Hibernate `StatelessSession` from scratch, see the useful example code from [this gist](https://gist.github.com/jelies/5181262).

I consulted this question in the Zulip Hibernate users channel, and a Hibernate expert provided an extremely simple solution to overcome this barrier temporarily: just add the property `hibernate.allow_update_outside_transaction=true` to the Hibernate configuration.

Check out the complete [sample project](https://github.com/hantsy/spring6-sandbox/blob/master/jakarta-data/) on my GitHub and explore the new Jakarta Data specification yourself.