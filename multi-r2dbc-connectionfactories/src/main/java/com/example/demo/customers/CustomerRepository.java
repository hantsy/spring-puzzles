package com.example.demo.customers;

import com.example.demo.orders.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CustomerRepository extends R2dbcRepository<Customer, Long> {
}
