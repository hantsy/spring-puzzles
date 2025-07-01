package com.example.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Configuration
class Jackson2ObjectMapperConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return customizer -> customizer
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnUnknownProperties(false)
                .indentOutput(true);
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class SampleDataInit implements ApplicationRunner {
    private final UserAccountRepository userAccounts;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userAccounts.deleteAllInBatch();

        var saved = userAccounts.save(
                UserAccount.builder()
                        .email("foo@bar.com")
                        .password("test")
                        .build()
        );

        log.debug("user saved: {}", saved);
    }
}

@Setter
@Getter
class CreateUserAccountCommand implements Serializable {
    @NotBlank
    @Email
    @UniqueEmail
    private String email;

    @NotBlank
    private String password;

    public CreateUserAccountCommand() {
    }

    public CreateUserAccountCommand(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserAccoutController {
    private final UserAccountRepository userAccounts;

    @GetMapping
    public ResponseEntity<List<UserAccount>> all() {
        return ok(userAccounts.findAll());
    }

    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody CreateUserAccountCommand data, ServletUriComponentsBuilder uriComponentsBuilder) {
        var saved = userAccounts.save(UserAccount.builder().email(data.getEmail()).password(data.getPassword()).build());
        return ResponseEntity.created(uriComponentsBuilder.path("/users/{id}").build(saved.getId())).build();
    }

    @GetMapping("{id}")
    public ResponseEntity<UserAccount> byId(@PathVariable Long id) {
        return userAccounts.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

@RequiredArgsConstructor
class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    private final UserAccountRepository userAccounts;

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.hasText(value)) {
            return !userAccounts.existsByEmail(value);
        }

        // empty or blank text, skip
        return true;
    }
}

@Constraint(validatedBy = UniqueEmailValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@interface UniqueEmail {

    public String message() default "There is already user with this email!";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};

}


interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Boolean existsByEmail(String email);
}

@Entity
@Getter
@Setter
@Builder()
@NoArgsConstructor
@AllArgsConstructor
class UserAccount implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    private String email;
    private String password;

    @Version
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return getEmail().equals(that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }
}