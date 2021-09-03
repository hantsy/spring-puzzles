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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

record Order(UUID orderNumber, BigDecimal amount, String customerName) {
}

@RestController
@RequestMapping("/")
@CrossOrigin
class OrderController {

    @GetMapping
    public ResponseEntity<?> all() {
        var orders = List.of(
                new Order(UUID.randomUUID(), new BigDecimal("100.0"), "Hantsy"),
                new Order(UUID.randomUUID(), new BigDecimal("50.0"), "Spring")
        );
        return ok(orders);
    }
}

@Configuration
@OpenAPIDefinition(tags = {@Tag(name = "orders")},
        info = @Info(title = "Orders API", version = "v1")
)
class SpringDocConfig {
}