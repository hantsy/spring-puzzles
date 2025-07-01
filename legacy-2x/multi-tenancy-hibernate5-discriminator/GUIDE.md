curl http://localhost:8080/customers
[{"tenantId":"public","id":1,"firstName":"Hantsy","lastName":"Bai"},{"tenantId":"t1","id":3,"firstName":"Hantsy@t1","lastName":"Bai"},{"tenantId":"t2","id":4,"firstName":"Hantsy@t2","lastName":"Bai"},{"tenantId":"public","id":5,"firstName":"Hantsy","lastName":"Bai"},{"tenantId":"t1","id":7,"firstName":"Hantsy@t1","lastName":"Bai"},{"tenantId":"t2","id":8,"firstName":"Hantsy@t2","lastName":"Bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:t1"
[{"tenantId":"t1","id":3,"firstName":"Hantsy@t1","lastName":"Bai"},{"tenantId":"t1","id":7,"firstName":"Hantsy@t1","lastName":"Bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:t2"
[{"tenantId":"t2","id":4,"firstName":"Hantsy@t2","lastName":"Bai"},{"tenantId":"t2","id":8,"firstName":"Hantsy@t2","lastName":"Bai"}]
C:\Users\hantsy>docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                    NAMES
0d21a2c902ea        postgres            "docker-entrypoint.s…"   50 minutes ago      Up 50 minutes       0.0.0.0:5432->5432/tcp   multi-tenancy-hibernate5-discriminator_tenant_1

C:\Users\hantsy>docker exec -it 0d21a2c902ea psql -U user tenant
psql (12.3 (Debian 12.3-1.pgdg100+1))
Type "help" for help.

tenant=# \dt
         List of relations
 Schema |   Name    | Type  | Owner
--------+-----------+-------+-------
 public | customers | table | user
 public | orders    | table | user
(2 rows)

tenant=# select * from customers;
 id | tenant_id | first_name | last_name
----+-----------+------------+-----------
  1 | public    | Hantsy     | Bai
  3 | t1        | Hantsy@t1  | Bai
  4 | t2        | Hantsy@t2  | Bai
  5 | public    | Hantsy     | Bai
  7 | t1        | Hantsy@t1  | Bai
  8 | t2        | Hantsy@t2  | Bai
(6 rows)

tenant=#

17:05:50,893 INFO  [stdout] (ServerService Thread Pool -- 10) 2020-08-24 17:05:50.890  INFO 15516 --- [read Pool -- 10] com.example.demo.DemoApplication         : sending an email: Hel
lo, Spring
17:05:50,906 INFO  [stdout] (DefaultMessageListenerContainer-1) 2020-08-24 17:05:50.905  INFO 15516 --- [enerContainer-1] com.example.demo.GreetingReceiver        : received greeting m
essage: Hello, Spring


## Reference

* [Hibernate's User Guide #  multi-tenancy](https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#multitenacy)

* [A Guide to Multitenancy in Hibernate 5](https://www.baeldung.com/hibernate-5-multitenancy)

* [Multi-Tenancy Implementation for Spring Boot + Hibernate Projects](https://dzone.com/articles/spring-boot-hibernate-multitenancy-implementation#)

* [Multi-Tenancy Using JPA, Spring, and Hibernate (Part 1)](https://dzone.com/articles/multi-tenancy-using-jpa-spring-and-hibernate-part#)

* [Hibernate database schema multitenancy](https://vladmihalcea.com/hibernate-database-schema-multitenancy/)

* [Using Hibernate and Spring to Build Multi-Tenant Java Apps ](https://www.citusdata.com/blog/2018/02/13/using-hibernate-and-spring-to-build-multitenant-java-apps/)

* [Multi-Tenancy Implementation using Spring Boot + Hibernate](https://medium.com/swlh/multi-tenancy-implementation-using-spring-boot-hibernate-6a8e3ecb251a)  _\*_

* [How to Build a Multitenant Application: A Hibernate Tutorial](https://www.toptal.com/hibernate/build-multitenant-java-hibernate)

* [spring-framework/issues/25125#Allow customization of JPA EntityManager before use](https://github.com/spring-projects/spring-framework/issues/25125#) 

* [spring-framework/issues/21061#Add way to enable Session filter for Hibernate 5+ [SPR-16518\]](https://github.com/spring-projects/spring-framework/issues/21061#)  
* [Introduce EntityManager initialization callbacks ](https://github.com/kenny5he/spring-framework/commit/6860eda32bbe163a9af8b550798bc5cf48b38d3c)
