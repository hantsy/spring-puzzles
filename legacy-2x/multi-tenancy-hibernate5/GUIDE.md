C:\Users\hantsy>curl http://localhost:8080/customers
[{"id":1,"firstName":"Hantsy","lastName":"Bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant1"
[{"id":2,"firstName":"hantsy@tenant1","lastName":"bai"}]
C:\Users\hantsy>curl http://localhost:8080/customers -H "X-TenantId:tenant2"
[{"id":3,"firstName":"hantsy@tenant2","lastName":"bai"}]



docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                               NAMES
215e7c874231        postgres            "docker-entrypoint.s…"   21 minutes ago      Up 21 minutes       0.0.0.0:15432->5432/tcp             multi-tenancy-hibernate5_tenant2_1
6bb57e64e4bd        mysql:5.7           "docker-entrypoint.s…"   21 minutes ago      Up 21 minutes       0.0.0.0:3306->3306/tcp, 33060/tcp   multi-tenancy-hibernate5_mysql_1
a3221d0ce388        postgres            "docker-entrypoint.s…"   21 minutes ago      Up 21 minutes       0.0.0.0:5432->5432/tcp              multi-tenancy-hibernate5_tenant1_1



docker exec -it 6bb57e64e4bd mysql -u user -p
Enter password:
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 42
Server version: 5.7.31 MySQL Community Server (GPL)

Copyright (c) 2000, 2020, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> use master
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> show tables
    -> ;
+--------------------+
| Tables_in_master   |
+--------------------+
| TENANT_DS_CONFIGS  |
| hibernate_sequence |
+--------------------+
2 rows in set (0.01 sec)

mysql> select * from TENANT_DS_CONFIGS;
+----+-----------+-------------------------------------------+----------+----------+-------------------+
| ID | TENANT_ID | URL                                       | USERNAME | PASSWORD | DRIVER_CLASS_NAME |
+----+-----------+-------------------------------------------+----------+----------+-------------------+
|  1 | tenant1   | jdbc:postgresql://localhost:5432/tenant1  | user     | password |                   |
|  2 | tenant2   | jdbc:postgresql://localhost:15432/tenant2 | user     | password |                   |
+----+-----------+-------------------------------------------+----------+----------+-------------------+
2 rows in set (0.00 sec)

mysql> quit
Bye



docker exec -it 215e7c874231 psql -U user tenant2
psql (12.3 (Debian 12.3-1.pgdg100+1))
Type "help" for help.

tenant2=# select * from customers;
 id |   first_name   | last_name
----+----------------+-----------
  4 | hantsy@tenant2 | bai
(1 row)

tenant2=# quit

C:\Users\hantsy>docker exec -it a3221d0ce388  psql -U user tenant1
psql (12.3 (Debian 12.3-1.pgdg100+1))
Type "help" for help.

tenant1=# select * from customers;
 id |   first_name   | last_name
----+----------------+-----------
  3 | hantsy@tenant1 | bai
(1 row)

tenant1=# quit


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

