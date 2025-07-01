package com.example.greeting.override;

import com.example.greeting.GreetingService;
import com.example.greeting.exclude.ExcludeDefaultGreetingServiceApplication;
import com.example.greeting.web.GreetingController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
        OverrideDefaultGreetingServiceApplication.class,
        GreetingController.class
})
public class OverrideDefaultGreetingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OverrideDefaultGreetingServiceApplication.class, args);
    }

}

@Component
@Primary
class PrimaryGreetingService implements GreetingService {
    @Override
    public String hello(String name) {
        return "Say Hello to " + name + ", from PrimaryGreetingService";
    }
}

