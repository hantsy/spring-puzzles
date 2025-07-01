package com.example.demo;

import com.example.demo.ping.PingApplication;
import com.example.demo.pong.PongApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class DemoApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PingApplication.class).run(args);
        new SpringApplicationBuilder(PongApplication.class).web(WebApplicationType.NONE).run(args);
    }

}