package com.example.demo.tenant.repository;

import com.example.demo.tenant.model.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface OrderRepository extends R2dbcRepository<Order,Integer> {
}
