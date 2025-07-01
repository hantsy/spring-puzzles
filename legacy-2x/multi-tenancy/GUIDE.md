

curl http://localhost:8080/customers
[{"id":1,"firstName":"Hantsy","lastName":"Bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant1"
[{"id":4,"firstName":"hantsy@tenant1","lastName":"bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant2"
[{"id":4,"firstName":"hantsy@tenant2","lastName":"bai"}]



docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                               NAMES
ee0336d31ed5        postgres            "docker-entrypoint.s…"   22 minutes ago      Up 22 minutes       0.0.0.0:5432->5432/tcp              multi-tenancy_tenant1_1
2065c7594081        mysql:5.7           "docker-entrypoint.s…"   22 minutes ago      Up 22 minutes       0.0.0.0:3306->3306/tcp, 33060/tcp   multi-tenancy_mysql_1
ae37432e7415        postgres            "docker-entrypoint.s…"   22 minutes ago      Up 22 minutes       0.0.0.0:15432->5432/tcp             multi-tenancy_tenant2_1

>docker exec -it 2065c7594081 mysql -u user -p
>Enter password:
>Welcome to the MySQL monitor.  Commands end with ; or \g.
>Your MySQL connection id is 64
>Server version: 5.7.31 MySQL Community Server (GPL)

Copyright (c) 2000, 2020, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> use master
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> show tables;
+--------------------+
| Tables_in_master   |
+--------------------+
| TENANT_DS_CONFIGS  |
| hibernate_sequence |
+--------------------+
2 rows in set (0.01 sec)

mysql> select * from TENANT_DS_CONFIGS
    -> ;
+----+-----------+-------------------------------------------+----------+----------+-------------------+
| ID | TENANT_ID | URL                                       | USERNAME | PASSWORD | DRIVER_CLASS_NAME |
+----+-----------+-------------------------------------------+----------+----------+-------------------+
|  1 | tenant1   | jdbc:postgresql://localhost:5432/tenant1  | user     | password |                   |
|  2 | tenant2   | jdbc:postgresql://localhost:15432/tenant2 | user     | password |                   |
+----+-----------+-------------------------------------------+----------+----------+-------------------+
2 rows in set (0.00 sec)

mysql> exit
Bye

docker exec -it ee0336d31ed5 psql -U user tenant1
psql (12.3 (Debian 12.3-1.pgdg100+1))
Type "help" for help.

tenant1=# select * from customers;
 id |   first_name   | last_name
----+----------------+-----------
  4 | hantsy@tenant1 | bai
(1 row)



docker exec -it ae37432e7415 psql -U user tenant2
psql (12.3 (Debian 12.3-1.pgdg100+1))
Type "help" for help.

tenant2=# select * from customers;
 id |   first_name   | last_name
----+----------------+-----------
  4 | hantsy@tenant2 | bai
(1 row)

## Reference

* [Multitenancy: Switching Databases at runtime with Spring](https://grobmeier.solutions/spring-multitenancy-switch-database-at-runtime.html)
* [Make your Spring Boot application multi-tenant aware in 2 steps](https://fizzylogic.nl/2016/01/24/make-your-spring-boot-application-multi-tenant-aware-in-2-steps/)
* Stackoverflow: [Multi-tenancy: Managing multiple datasources with Spring Data JPA](https://stackoverflow.com/questions/49759672/multi-tenancy-managing-multiple-datasources-with-spring-data-jpa)
*  [Dynamic Multi-Tenancy Using Spring Security and JWTs](https://dzone.com/articles/dynamic-multi-tenancy-using-java-spring-boot-sprin#)
* [Adding tenants without application restart in SaaS style multi-tenant web app with Spring Boot 2 and Spring Security 5](https://sunitkatkar.blogspot.com/2018/05/adding-tenants-without-application.html)
* [Providing Multitenancy with Spring Boot](https://bytefish.de/blog/spring_boot_multitenancy/)
* [Extend Spring Security to Protect Multi-tenant SaaS Applications](https://www.developer.com/java/ent/extend-spring-security-to-protect-multi-tenant-saas-applications.html)
* [Extending Spring Security OAuth for Multi-Tenant](https://www.jamasoftware.com/blog/spring-security-oauth-multi-tenant/)
* [A Guide to Multitenancy in Hibernate 5](https://www.baeldung.com/hibernate-5-multitenancy)
* [Multitenancy Applications with Spring Boot and Flyway](https://reflectoring.io/flyway-spring-boot-multitenancy/)