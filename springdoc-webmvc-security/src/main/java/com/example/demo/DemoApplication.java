package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    // @Operation(security = {@SecurityRequirement(name = "httpBasic")})
    public ResponseEntity<?> sayHello(@RequestParam("name") String name) {
        return ok("Hello, " + name);
    }
}

@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/swagger-ui*/**", "/v3/api-docs/**");
    }
}

@Configuration
@SecurityScheme(
        name = "httpBasic", // can be set to anything
        type = SecuritySchemeType.HTTP,
        scheme = "Basic"
)
@OpenAPIDefinition(
        info = @Info(title = "Greeting API", version = "v1"),
        security = {@SecurityRequirement(name = "httpBasic")}//global enablement
)
class SpringDocConfig {

}

