package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
class PersonController {
    private final PersonRepository personRepository;

    PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping
    public Flux<Person> allPersons() {
        return this.personRepository.findAll();
    }
}

interface PersonRepository extends R2dbcRepository<Person, Integer> {
}

@Table("persons")
record Person(
        @Id Integer id,
        String name
) {
}