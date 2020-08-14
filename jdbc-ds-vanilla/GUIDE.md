# DataSource in Spring



When using Hibernate, Jdbc or JPA in a Spring application, firstly you have to configure a `java.sql.DataSource` bean.

Spring framework  provides various means to define a `DataSource` bean .

For **H2** like embedded database, Spring provides a specific helper to start an embedded Database quickly.

```java
public DataSource embeddedDataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .build();
}
```

The embedded approach is good for testing or the case do not need to persist the data.

You can also connect an external database via JDBC drivers. 

Here we define a `DriverManagerDataSource` and here set the detailed properties of a **standard** `DataSource` , eg. the essential properties: *url*, *username* and *password*.

```java
public DataSource driverManagerDataSource() {
    DriverManagerDataSource bds = new DriverManagerDataSource();
    bds.setUrl(env.getProperty(ENV_DATASOURCE_URL));
    bds.setUsername(env.getProperty(ENV_DATASOURCE_USERNAME));
    bds.setPassword(env.getProperty(ENV_DATASOURCE_PASSWORD));
    return bds;
}
```

Besides `DriverManagerDataSource` , spring provides other two  variants:

* `SimpleDriverDataSource` is a simple `DataSource` implementation, which will create a new connection instance for every call.
* `SingleConnectionDataSource` is designed for testing purpose, which shares a single connection instance at runtime.

In  a real world application, to improve the performance, using a connection pool is highly recommended. [HikariCP ](https://github.com/brettwooldridge/HikariCP)is very popular in these days, you can defined a **pooled** `HikariDataSource`  instead.

> In a Spring Boot application, when a  `spring-boot-starter-jdbc` or  `spring-boot-starter-data-jpa` is existed in the project classpath, a [HikariCP ](https://github.com/brettwooldridge/HikariCP) based connection pool is created by default. Alternatively, you use Apache [commons-dbcp2](https://commons.apache.org/proper/commons-dbcp/) instead.

For those want to reuse the container managed resources, declaring a bean using JNDI lookup is a good choice.

Define a `JndiObjectFactoryBean` to look up the JNDI `DataSource` via a named resource preconfigured in the application server(Tocmat, etc.).

```java
public DataSource jndiDataSource() throws NamingException {
    JndiObjectFactoryBean ds = new JndiObjectFactoryBean();
    ds.setJndiName(env.getProperty(ENV_DATASOURCE_JNDINAME));
    ds.afterPropertiesSet();

    return (DataSource) ds.getObject();
}
```



