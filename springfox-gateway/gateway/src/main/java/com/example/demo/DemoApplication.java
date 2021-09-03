package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
@RequestMapping("/hello")
class GreetingController {

    @GetMapping
    public ResponseEntity<?> sayHello(@RequestParam("name") String name) {
        return ok("Hello, " + name);
    }
}

@Configuration
class SwaggerConfig {
    @Bean
    public Docket openApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("Gateway Endpoints")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example"))
                .paths(PathSelectors.regex("/.*"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Gateway API")
                .description("Description of Gateway api")
                .version("1.0")
                .build();
    }

    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider(InMemorySwaggerResourcesProvider defaultResourcesProvider) {
        return () -> {
            SwaggerResource customersResource = new SwaggerResource();
            customersResource.setName("Customers Endpoints");
            customersResource.setSwaggerVersion("3.0");
            // can not set the rewrite prefix in the SwaggerResource execution.
            customersResource.setUrl("/customers/v3/api-docs");
            //customersResource.setUrl("http://localhost:8001/v3/api-docs");

            SwaggerResource ordersResource = new SwaggerResource();
            ordersResource.setName("Orders Endpoints");
            ordersResource.setSwaggerVersion("3.0");
            ordersResource.setUrl("/orders/v3/api-docs");
            //ordersResource.setUrl("http://localhost:8002/v3/api-docs");

            List<SwaggerResource> resources = new ArrayList<>(defaultResourcesProvider.get());
            resources.add(customersResource);
            resources.add(ordersResource);
            return resources;
        };
    }
}