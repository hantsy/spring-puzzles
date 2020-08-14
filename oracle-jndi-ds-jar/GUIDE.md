# Configure an Oracle JNDI DataSource in an Embedded Tomcat with Spring Boot

For those applications migrated from the web servers to Spring Boot platform, if you were using a managed `DataSource` in a standalone Tomcat server, when coming to Spring Boot based embedded Tomcat, you have to configure a `DataSource` in the embedded Tomcat.

Spring Boot provides some customizers to append additional configurations to the web server.

For Tomcat server, define a `WebServerFactoryCustomizer<TomcatServletWebServerFactory>` bean like this.

```java
@Bean
WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatFactoryCustomizer(){
    return factory-> {

        TomcatContextCustomizer dataSourceContextCustomizer = context -> {
            var resource = new ContextResource();

            resource.setType(DataSource.class.getName());
            resource.setName("jdbc/testDS");
            resource.setAuth("Container");

            // use tomcat-dbcp.
            resource.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
            resource.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
            resource.setProperty("url", "jdbc:oracle:thin:@localhost:1521:xe");
            resource.setProperty("username", "system");
            resource.setProperty("password", "Passw0rd");
            context.getNamingResources().addResource(resource);
        };

        factory.addContextCustomizers(dataSourceContextCustomizer);
    };
}
```

`TomcatServletWebServerFactory` provides a `addContextCustomizers` to add custom TomcatContext which is a mapping to the `Context` in the tomcat *server.xml*. 

To use  common-dbcp2 as connection pool, add `tomcat-dbcp` to the project classpath. `tomcat-dbcp` is updated to use common-dbcp2 in the latest Apache Tomcat 9.

```xml
<dependency>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>tomcat-dbcp</artifactId>
    <version>${tomcat.version}</version>
    <scope>runtime</scope>
</dependency>
```

> I have to tried to use HikariCP as connection pool, as described in [this question](https://stackoverflow.com/questions/24941829/how-to-create-jndi-context-in-spring-boot-with-embedded-tomcat-container) in stackoverflow, it does not work in my example.

Add Jdbc driver as part of the project classpath.

```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <scope>runtime</scope>
</dependency>
```

Add the following configuration in the *application.properties* to tell Spring Boot to autoconfigure a `DataSource` from JNDI resource.

```properties
spring.datasource.jndi-name=java:comp/env/jdbc/testDS
```

Start up the application, it fails due to an exception like this.

```bash
javax.naming.NamingException: No naming context bound to this class loader
...
Failed to instantiate [javax.sql.DataSource]: Factory method 'jndiDataSource' threw exception;
nested exception is javax.naming.NoInitialContextException: Need to specify class name in environment
 or system property, or in an application resource file: java.naming.factory.initial
```

Declare a `TomcatServletWebServerFactory` bean, override the `getTomcatWebServer` to activate naming support in Tomcat.

```java
@Bean
TomcatServletWebServerFactory tomcatFactory() {
    return new TomcatServletWebServerFactory() {
        @Override
        protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
            tomcat.enableNaming();
            return super.getTomcatWebServer(tomcat);
        }
    }
```

Create a profile for integration tests.

```xml
<profile>
    <id>it</id>
    <properties>
        <skip.unit.tests>true</skip.unit.tests>
        <skip.integration.tests>false</skip.integration.tests>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>pre-integration-test</id>
                        <goals>
                            <goal>start</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>post-integration-test</id>
                        <goals>
                            <goal>stop</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

Create an integration test.

```java
class DemoApplicationIT {

    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @DisplayName(" GET '/' should return status 200")
    void getAllPosts() {
        var resEntity = restTemplate.getForEntity("http://localhost:8080/", Post[].class);
        assertThat(resEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var posts = resEntity.getBody();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0].getTitle()).contains("Tomcat");
    }

}
```

Run this test with **it** maven profile.

```bash
mvn verify -Pit
```
Get the complete codes from [spring-playground](https://github.com/hantsy/spring-playground/tree/master/oracle-jndi-ds-jar).


## Reference

* [Use another Web Server](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-use-another-web-server)
* [How to create JNDI context in Spring Boot with Embedded Tomcat Container](https://stackoverflow.com/questions/24941829/how-to-create-jndi-context-in-spring-boot-with-embedded-tomcat-container)