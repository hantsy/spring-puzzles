module greeting.webapp.test {
    requires greeting.application;

    requires spring.web;
    requires spring.core;
    requires spring.beans;
    requires spring.context;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.webflux;

    requires spring.test;
    requires spring.boot.test;
    requires spring.boot.test.autoconfigure;

    requires org.junit.jupiter;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.junit.jupiter.engine;
    requires org.junit.platform.commons;
    requires org.assertj.core;
    requires org.mockito.junit.jupiter;
    requires transitive net.bytebuddy;

    exports com.example.greeting.application.test;

    opens  com.example.greeting.application.test;
}