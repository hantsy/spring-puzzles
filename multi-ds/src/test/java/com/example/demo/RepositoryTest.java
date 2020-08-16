package com.example.demo;

import com.example.demo.customers.CustomerConfig;
import com.example.demo.customers.CustomerEntity;
import com.example.demo.orders.OrderConfig;
import com.example.demo.orders.OrderEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({CustomerConfig.class, OrderConfig.class})
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
