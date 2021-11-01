package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GreetingService {
    public Greeting greetTo(String name) {
        var to = StringUtils.hasText(name) ? name : "World";
        return Greeting.of("Hello, " + to + "!");
    }
}
