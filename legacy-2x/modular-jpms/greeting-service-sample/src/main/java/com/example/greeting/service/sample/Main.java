package com.example.greeting.service.sample;

import com.example.greeting.service.api.GreetingService;

import java.util.ServiceLoader;

public class Main {
    public static void main(String[] args) {
        var service = ServiceLoader.load(GreetingService.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("GreetingService was not found."));

        service.hello("Consumer Sample");
    }
}
