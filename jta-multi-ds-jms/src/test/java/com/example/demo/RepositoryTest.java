package com.example.demo;

import com.example.demo.customers.CustomerEntity;
import com.example.demo.orders.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RepositoryTest {

    @Autowired
    private JpaRepository<CustomerEntity, Long> customers;

    @Autowired
    private JpaRepository<OrderEntity, Long> orders;

    @Test
    public void dataSourcesExisted() {
        assertThat(customers).isNotNull();
        assertThat(orders).isNotNull();
    }

}
