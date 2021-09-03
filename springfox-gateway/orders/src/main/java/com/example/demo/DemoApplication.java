package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

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

record Order(UUID orderNumber, BigDecimal amount,  String customerName) {
}

@RestController
@RequestMapping("/")
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
class SwaggerConfig {
    @Bean
    public Docket openApi() {
        return new Docket(DocumentationType.OAS_30)
               // .groupName("Orders API Docs")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example"))
                .paths(PathSelectors.regex("/.*"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Orders API")
                .description("Description of Orders api")
                .version("1.0")
                .build();
    }
}