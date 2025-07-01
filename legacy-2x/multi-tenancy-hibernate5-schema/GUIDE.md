C:\Users\hantsy>curl http://localhost:8080/customers
[{"id":1,"firstName":"Hantsy","lastName":"Bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant1"
[{"id":2,"firstName":"hantsy@tenant1","lastName":"bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant2"
[{"id":3,"firstName":"hantsy@tenant2","lastName":"bai"}]



docker exec -it 480b5cedaf4e -U user tenant
OCI runtime exec failed: exec failed: container_linux.go:349: starting container process caused "exec: \"-U\": executable file not found in $PATH": unknown
failed to resize tty, using default size

C:\Users\hantsy>docker exec -it 480b5cedaf4e psql -U user tenant
psql (12.3 (Debian 12.3-1.pgdg100+1))
Type "help" for help.

tenant=# \dt+
                       List of relations
 Schema |   Name    | Type  | Owner |    Size    | Description
--------+-----------+-------+-------+------------+-------------
 public | customers | table | user  | 16 kB      |
 public | orders    | table | user  | 8192 bytes |
(2 rows)

tenant=# show search_path;
   search_path
-----------------
 "$user", public
(1 row)

tenant=# set search_path to tenant1;
SET
tenant=# \dt+
                       List of relations
 Schema  |   Name    | Type  | Owner |    Size    | Description
---------+-----------+-------+-------+------------+-------------
 tenant1 | customers | table | user  | 16 kB      |
 tenant1 | orders    | table | user  | 8192 bytes |
(2 rows)

tenant=# select * from customers;
 id |   first_name   | last_name
----+----------------+-----------
  3 | hantsy@tenant1 | bai
(1 row)

tenant=# set search_path to tenant2;
SET
tenant=# select * from customers;
 id |   first_name   | last_name
----+----------------+-----------
  3 | hantsy@tenant2 | bai
(1 row)

tenant=#



curl http://localhost:8080/customers
[{"id":1,"firstName":"Hantsy","lastName":"Bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant2"
[{"id":7,"firstName":"hantsy@tenant2","lastName":"bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant1"
[{"id":7,"firstName":"hantsy@tenant1","lastName":"bai"}]


## Reference

* [Hibernate's User Guide #  multi-tenancy](https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#multitenacy)
* [A Guide to Multitenancy in Hibernate 5](https://www.baeldung.com/hibernate-5-multitenancy)
* [Multitenancy Applications with Spring Boot and Flyway](https://reflectoring.io/flyway-spring-boot-multitenancy/)
* [Multi-Tenancy Implementation for Spring Boot + Hibernate Projects](https://dzone.com/articles/spring-boot-hibernate-multitenancy-implementation#)
* [Multi-Tenancy Using JPA, Spring, and Hibernate (Part 1)](https://dzone.com/articles/multi-tenancy-using-jpa-spring-and-hibernate-part#)
* [Hibernate database schema multitenancy](https://vladmihalcea.com/hibernate-database-schema-multitenancy/)
* [Using Hibernate and Spring to Build Multi-Tenant Java Apps ](https://www.citusdata.com/blog/2018/02/13/using-hibernate-and-spring-to-build-multitenant-java-apps/)
* [Multi-Tenancy Implementation using Spring Boot + Hibernate](https://medium.com/swlh/multi-tenancy-implementation-using-spring-boot-hibernate-6a8e3ecb251a)  _\*_
* [How to Build a Multitenant Application: A Hibernate Tutorial](https://www.toptal.com/hibernate/build-multitenant-java-hibernate)

