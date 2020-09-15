package com.example.demo.tenant.repository;

import com.example.demo.tenant.model.Customer;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CustomerRepository extends R2dbcRepository<Customer, Long> {
}
