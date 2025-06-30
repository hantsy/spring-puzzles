# 6 Programming Models You Must Know When Building RESTful Services with Spring Boot

Over the past few years, [Spring Framework](https://spring.io/projects/spring-framework) has rapidly evolved, bringing a wide array of innovations that empower developers to build robust and modern RESTful services with ease. Since Spring 5.0, it has adopted new paradigms and technologies that reflect the changing landscape of software development.

One of the most notable advancements is the adoption of the **[Reactive Streams](https://www.reactive-streams.org/)** standard, which introduces a reactive programming model as a powerful alternative to the traditional servlet-based WebMvc stack. This addition enables developers to build highly scalable, event-driven web applications that can efficiently handle a large number of concurrent connections.

Meanwhile, **Kotlin**’s rise in popularity on the JVM has led Spring to offer first-class support for the language, culminating in seamless integration with [Kotlin Coroutines](https://docs.spring.io/spring-framework/reference/languages/kotlin.html#kotlin-coroutines). This allows developers to write concise, asynchronous, and non-blocking code in a style that closely resembles traditional imperative programming.

Functional programming concepts are also being incorporated into mainstream Java development. Spring has responded by introducing the **[RouterFunction](https://docs.spring.io/spring-framework/reference/web/webflux-functional.html)** API, enabling developers to define web handling in a functional, declarative style.

Additionally, Spring has even backported RouterFunction and the Kotlin DSL to the traditional WebMvc stack, bridging the gap between imperative and reactive programming approaches.

For a developer building RESTful services with Spring Boot, you now have a variety of powerful programming models to choose from, including the WebMvc or WebFlux for the tech stack, annotated controllers or functional router for code styles, etc.

In this article, we will explore **six essential programming models** that every Spring Boot developer should be familiar with when building RESTful services.

## Prerequitions 
I assumed you have installed the following software:

* **Apache Maven 3.9 or 4.0**: We used Maven here, and do not stop you from using Gradle. You can freely convert the Maven POM to Gradle scripts.
* **Java 21**
* **Docker**, especially Docker Desktop for Windows users.

We will use PostgreSQL as the database and a [Docker Compose](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/docker-compose.yml) file to serve the database service at runtime. We will utilize Testcontainers to manage the database service when running test code. We use the same scripts to initialize the database for all projects mentioned in this post, including additional schema and initial scripts. For more information, refer to [schema.sql](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/resources/schema.sql) and [data.sql](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/resources/data.sql). We mainly discuss building RESTful services using different combinations, such as WebMvc or WebFlux, Annotated Controllers or Functional Routers. We will talk about the data persistence options in the future.




  
## WebMvc + Annotated Controllers

This is the typical use case that has existed in Spring for a long time. Let's create a project using https://start.spring.io.

* Project: **Maven**
* Java: **21** 
* Spring Boot: **3.5**
* Dependencies: *Web*, *Data Jdbc*, *Postgres*, *Testcontainers*, *Lombok* etc.




