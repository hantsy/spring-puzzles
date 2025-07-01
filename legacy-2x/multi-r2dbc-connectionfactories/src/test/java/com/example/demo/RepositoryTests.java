package com.example.demo;

import com.example.demo.customers.Customer;
import com.example.demo.customers.CustomerRepository;
import com.example.demo.orders.Order;
import com.example.demo.orders.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RepositoryTests {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    R2dbcRepository<Customer, Long> customers;

    @Autowired
    R2dbcRepository<Order, Integer> orders;

    @Test
    public void testCustomerRepositoryExists() {
        assertThat(this.customerRepository).isNotNull();
    }

    @Test
    public void testOrderRepositoryExists() {
        assertThat(this.orderRepository).isNotNull();
    }

    @Test
    public void testCustomersExists() {
        assertThat(this.customers).isNotNull();
    }

    @Test
    public void testOrdersExists() {
        assertThat(this.orders).isNotNull();
    }
}
