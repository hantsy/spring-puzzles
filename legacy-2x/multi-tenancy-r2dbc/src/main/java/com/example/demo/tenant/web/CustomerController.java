package com.example.demo.tenant.web;

import com.example.demo.tenant.repository.CustomerRepository;
import com.example.demo.tenant.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@RequestMapping("customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customers;

    @GetMapping("")
    public ResponseEntity<Flux<Customer>> all() {
        return ok().body(this.customers.findAll());
    }

}
