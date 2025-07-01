package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

record Customer(String name) {
}

@RestController
@RequestMapping("/")
@CrossOrigin
class CustomerController {

    @GetMapping
    public ResponseEntity<?> all() {
        var customers = List.of(
                new Customer("Hantsy"),
                new Customer("Spring")
        );
        return ok(customers);
    }
}

@Configuration
@OpenAPIDefinition(tags = {@Tag(name = "customers")},
        info = @Info(title = "Customers API", version = "v1")
)
class SpringDocConfig {
}