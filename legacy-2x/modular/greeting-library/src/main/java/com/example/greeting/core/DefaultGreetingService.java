package com.example.greeting.core;

import java.time.Instant;

public class DefaultGreetingService implements GreetingService {
    @Override
    public String hello(String name) {
        String message = "Say Hello to " + name + " at " + Instant.now() + ", from DefaultGreetingService";
        System.out.println(message);

        return message;
    }
}
