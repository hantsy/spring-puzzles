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
class SwaggerConfig {
    @Bean
    public Docket openApi() {
        return new Docket(DocumentationType.OAS_30)
                //.groupName("Customers API Docs")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example"))
                .paths(PathSelectors.regex("/.*"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Customers API")
                .description("Description of Customers API")
                .version("1.0")
                .build();
    }
}