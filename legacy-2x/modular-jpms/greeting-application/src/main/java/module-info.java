//import com.example.greeting.application.GreetingApplication;

module greeting.application {
    requires greeting.service;
    requires greeting.service.impl;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires spring.core;
    requires java.sql;
    requires com.fasterxml.jackson.databind;

    opens com.example.greeting.application to spring.core, greeting.application.test;

    exports com.example.greeting.application;
}