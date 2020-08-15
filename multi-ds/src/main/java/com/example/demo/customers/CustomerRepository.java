package com.example.demo.customers;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository  extends JpaRepository<CustomerEntity, Long> {
}
