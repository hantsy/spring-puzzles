package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//see: https://github.com/spring-projects/spring-boot/issues/25059
//@WebListener()
@Component
@RequiredArgsConstructor
@Slf4j
public class GreetingListener implements ServletContextListener {
    private final GreetingService greetingService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        //in a traditional web application it can be set by `webapp/context-param` in the web.xml
        var name = sce.getServletContext().getInitParameter("to");
        log.debug("The init parameter (to) in listener: {}", name);

        var hello = sce.getServletContext().getInitParameter("hello");
        log.debug("The init parameter (hello) in listener: {}", hello);

        var to = greetingService.greetTo(name);
        log.debug("The greeting object in listener: {}", to);
        sce.getServletContext().setAttribute("greeting", to);
    }

}
