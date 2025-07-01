package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DataSourceTest {

    @Autowired
    @Qualifier("customersDataSource")
    private DataSource customersDataSource;

    @Autowired
    @Qualifier("ordersDataSource")
    private DataSource ordersDataSource;

    @Test
    public void dataSourcesExisted() {
        assertThat(customersDataSource).isNotNull();
        assertThat(ordersDataSource).isNotNull();
    }

}
