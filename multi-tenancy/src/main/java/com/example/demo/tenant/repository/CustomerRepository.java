package com.example.demo.tenant.repository;

import com.example.demo.tenant.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository  extends JpaRepository<CustomerEntity, Long> {
}
