package com.example.demo;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;
import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}


@RestController
@RequestMapping("/foo")
class FooController {

    @PostMapping
    public ResponseEntity create(@RequestBody CreateFooCommand data) {

        return ok().build();
    }
}

record CreateFooCommand(String data) {
}


@RestController
@RequestMapping("/bar")
class BarController {

    @PostMapping
    public ResponseEntity create(@RequestBody CreateBarCommand data) {

        return ok().build();
    }
}

record CreateBarCommand(String data) {
}


@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(
    name = "basicAuth",
    type = HTTP,
    scheme = "basic"
)
class SpringDocConfig {

    @Bean
    public GroupedOpenApi fooOpenApi() {
        return GroupedOpenApi.builder()
            .group("foo")
            .pathsToMatch("/foo")
            .addOpenApiCustomizer(openApi ->
                openApi.info(new Info().title("Foo API").version("v1.0"))
                    .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
            )
            .build();
    }

    @Bean
    public GroupedOpenApi barOpenApi() {
        return GroupedOpenApi.builder()
            .group("bar")
            .pathsToMatch("/bar")
            .addOpenApiCustomizer(openApi ->
                openApi.info(new Info().title("Bar API").version("v1.0"))
                    .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                    .components(
                        new Components()
                            .addSecuritySchemes("apiKey",
                                new SecurityScheme()
                                    .name("X-API-AEY")
                                    .in(SecurityScheme.In.HEADER)
                                    .type(SecurityScheme.Type.APIKEY)
                            )
                    )
            )
            .build();
    }
}