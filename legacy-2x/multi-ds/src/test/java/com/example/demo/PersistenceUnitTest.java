package com.example.demo;

import com.example.demo.customers.CustomerConfig;
import com.example.demo.orders.OrderConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({CustomerConfig.class, OrderConfig.class})
public class PersistenceUnitTest {

    @PersistenceContext(unitName = "customers")
    private EntityManager custEntityManager;

    @PersistenceContext(unitName = "orders")
    private EntityManager ordersEntityManager;

    @PersistenceUnit(unitName = "customers")
    private EntityManagerFactory custEntityManagerFactory;

    @PersistenceUnit(unitName = "orders")
    private EntityManagerFactory ordersEntityManagerFactory;

    @Test
    public void persistenceUnitExisted() {
        assertThat(custEntityManager).isNotNull();
        assertThat(ordersEntityManager).isNotNull();
        assertThat(custEntityManagerFactory).isNotNull();
        assertThat(ordersEntityManagerFactory).isNotNull();
    }
}
