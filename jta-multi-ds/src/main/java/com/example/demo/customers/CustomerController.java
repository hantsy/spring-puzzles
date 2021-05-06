package com.example.demo.customers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@RequestMapping("customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customers;

    @GetMapping("")
    public ResponseEntity<List<CustomerEntity>> all() {
        return ok().body(this.customers.findAll());
    }

}
