package com.example.greeting.exclude;

import com.example.greeting.GreetingService;
import com.example.greeting.GreetingServiceAutoConfiguration;
import com.example.greeting.app.GreetingApplication;
import com.example.greeting.web.GreetingController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

//@SpringBootApplication
//@EnableAutoConfiguration(exclude = GreetingServiceAutoConfiguration.class)
@SpringBootApplication(exclude = GreetingServiceAutoConfiguration.class)
@ComponentScan(basePackageClasses = {
        ExcludeDefaultGreetingServiceApplication.class,
        GreetingController.class
})
public class ExcludeDefaultGreetingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcludeDefaultGreetingServiceApplication.class, args);
    }

}

@Component
class CustomGreetingService implements GreetingService {
    @Override
    public String hello(String name) {
        return "Say Hello to " + name + ", from CustomGreetingService";
    }
}

