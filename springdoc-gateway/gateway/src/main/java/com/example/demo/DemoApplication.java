package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@OpenAPIDefinition(tags = {@Tag(name = "greeting")},
        info = @Info(title = "Greeting API", version = "v1")
)
class SpringDocConfig {
}

//@Bean
//public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
//    return new OpenAPI()
//            .components(new Components())
//            .info(new io.swagger.v3.oas.models.info.Info().title("Gateway API").version(appVersion)
//                    .license(new License().name("Apache 2.0").url("http://springdoc.org")));
//}

@Configuration
class SwaggerConfig {

    @Bean
    public List<GroupedOpenApi> apis(SwaggerUiConfigParameters swaggerUiConfigParameters) {
        swaggerUiConfigParameters.addGroup("orders");
        swaggerUiConfigParameters.addGroup("customers");
        return List.of(
                GroupedOpenApi.builder().group("orders").pathsToMatch("/orders/**").build(),
                GroupedOpenApi.builder().group("customers").pathsToMatch("/customers/**").build()
        );
    }
}