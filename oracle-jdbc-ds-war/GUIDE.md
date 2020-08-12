❯ docker-compose up
Creating network "oracle-jdbc-ds-war_default" with the default driver
Creating oracle-jdbc-ds-war_oracledb_1 ... done
Attaching to oracle-jdbc-ds-war_oracledb_1
oracledb_1  | ORACLE PASSWORD FOR SYS AND SYSTEM: 11bdb39b98599a8b
oracledb_1  | Specify a password to be used for database accounts. Oracle recommends that the password entered should be at least 8 characters in length, contain at least 1 uppercase character, 1 lower case character and 1 digit [0-9]. Note that the same password will be used for SYS, SYSTEM and PDBADMIN accounts:
oracledb_1  | Confirm the password:
oracledb_1  | Configuring Oracle Listener.
oracledb_1  | Listener configuration succeeded.
oracledb_1  | Configuring Oracle Database XE.
oracledb_1  | Enter SYS user password:
oracledb_1  | *****************
oracledb_1  | Enter SYSTEM user password:
oracledb_1  | *******************
oracledb_1  | Enter PDBADMIN User Password:
oracledb_1  | ***************
oracledb_1  | Prepare for db operation
oracledb_1  | 7% complete
oracledb_1  | Copying database files
oracledb_1  | 29% complete
oracledb_1  | Creating and starting Oracle instance
oracledb_1  | 30% complete
oracledb_1  | 31% complete
oracledb_1  | 34% complete
oracledb_1  | 38% complete
oracledb_1  | 41% complete
oracledb_1  | 43% complete
oracledb_1  | Completing Database Creation
oracledb_1  | 47% complete
oracledb_1  | 50% complete
oracledb_1  | Creating Pluggable Databases
oracledb_1  | 54% complete
oracledb_1  | 71% complete
oracledb_1  | Executing Post Configuration Actions
oracledb_1  | 93% complete
oracledb_1  | Running Custom Scripts
oracledb_1  | 100% complete
oracledb_1  | Database creation complete. For details check the logfiles at:
oracledb_1  |  /opt/oracle/cfgtoollogs/dbca/XE.
oracledb_1  | Database Information:
oracledb_1  | Global Database Name:XE
oracledb_1  | System Identifier(SID):XE
oracledb_1  | Look at the log file "/opt/oracle/cfgtoollogs/dbca/XE/XE.log" for further details.
oracledb_1  |
oracledb_1  | Connect to Oracle Database using one of the connect strings:
oracledb_1  |      Pluggable database: 7044c6afe68b/XEPDB1
oracledb_1  |      Multitenant container database: 7044c6afe68b
oracledb_1  | Use https://localhost:5500/em to access Oracle Enterprise Manager for Oracle Database XE
oracledb_1  | The Oracle base remains unchanged with value /opt/oracle
oracledb_1  | #########################
oracledb_1  | DATABASE IS READY TO USE!
oracledb_1  | #########################
oracledb_1  | The following output is now a tail of the alert.log:
oracledb_1  | 2020-08-11T13:03:34.791118+00:00
oracledb_1  | XEPDB1(3):Resize operation completed for file# 10, old size 358400K, new size 368640K
oracledb_1  | 2020-08-11T13:03:37.308649+00:00
oracledb_1  | XEPDB1(3):CREATE SMALLFILE TABLESPACE "USERS" LOGGING  DATAFILE  '/opt/oracle/oradata/XE/XEPDB1/users01.dbf' SIZE 5M REUSE AUTOEXTEND ON NEXT  1280K MAXSIZE UNLIMITED  EXTENT MANAGEMENT LOCAL  SEGMENT SPACE MANAGEMENT  AUTO
oracledb_1  | XEPDB1(3):Completed: CREATE SMALLFILE TABLESPACE "USERS" LOGGING  DATAFILE  '/opt/oracle/oradata/XE/XEPDB1/users01.dbf' SIZE 5M REUSE AUTOEXTEND ON NEXT  1280K MAXSIZE UNLIMITED  EXTENT MANAGEMENT LOCAL  SEGMENT SPACE MANAGEMENT  AUTO
oracledb_1  | XEPDB1(3):ALTER DATABASE DEFAULT TABLESPACE "USERS"
oracledb_1  | XEPDB1(3):Completed: ALTER DATABASE DEFAULT TABLESPACE "USERS"
oracledb_1  | 2020-08-11T13:03:38.271246+00:00
oracledb_1  | ALTER PLUGGABLE DATABASE XEPDB1 SAVE STATE
oracledb_1  | Completed: ALTER PLUGGABLE DATABASE XEPDB1 SAVE STATE

 docker ps
 CONTAINER ID        IMAGE                       COMMAND                  CREATED             STATUS                    PORTS                              NAMES
 7044c6afe68b        oracle/database:18.4.0-xe   "/bin/sh -c 'exec $O…"   12 minutes ago      Up 12 minutes (healthy)   0.0.0.0:1521->1521/tcp, 0.0.0.0:5500->5500/tcp, 0.0.0.0:8080->8080/tcp   oracle-jdbc-ds-war_oracledb_1

❯ docker exec 7044c6afe68b ./setPassword.sh "Passw0rd"
The Oracle base remains unchanged with value /opt/oracle

SQL*Plus: Release 18.0.0.0.0 - Production on Tue Aug 11 13:14:05 2020
Version 18.4.0.0.0

Copyright (c) 1982, 2018, Oracle.  All rights reserved.


Connected to:
Oracle Database 18c Express Edition Release 18.0.0.0.0 - Production
Version 18.4.0.0.0

SQL>
User altered.

SQL>
User altered.

SQL>
Session altered.

SQL>
User altered.

SQL> Disconnected from Oracle Database 18c Express Edition Release 18.0.0.0.0 - Production
Version 18.4.0.0.0

## Reference

* [Developers Guide For Oracle JDBC 19.7.0.0 on Maven Central](https://www.oracle.com/database/technologies/maven-central-guide.html)
* [Automated Deployment With Cargo and Maven - a Short Primer](https://dzone.com/articles/automated-deployment-cargo-and)
* [Cargo official docs: Maven plugins](https://codehaus-cargo.github.io/cargo/Maven2+plugin.html)
* [Spring Boot Configure DataSource Using JNDI with Example](https://www.java4s.com/spring-boot-tutorials/spring-boot-configure-datasource-using-jndi-with-example/)
* [All in and New GroupIds — Oracle JDBC drivers on Maven Central](https://medium.com/oracledevs/all-in-and-new-groupids-oracle-jdbc-drivers-on-maven-central-a76d545954c6)
* [How to configure Oracle DataSource in Tomcat 9](https://javausecase.com/2017/01/22/how-to-configure-oracle-datasource-in-tomcat-9/)
* Oracle official [JDBC Developer's Guide](https://docs.oracle.com/en/database/oracle/oracle-database/20/jjdbc/)
* [Design and Deploy Tomcat Applications forPlanned, Unplanned Database Downtimes and Runtime Load Balancing with UCP](https://www.oracle.com/technetwork/database/application-development/planned-unplanned-rlb-ucp-tomcat-2265175.pdf)
* [How To: Set Up a Data Source within Tomcat 6.0 using Oracle Universal Connection Pool](https://www.oracle.com/technical-resources/articles/enterprise-manager/ucp-jdbc-tomcat.html)

