package com.example.demo;

import com.example.demo.customers.CustomerConfig;
import com.example.demo.orders.OrderConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({CustomerConfig.class, OrderConfig.class})
public class DataSourceTest {

    @Autowired
    @Qualifier("customersDataSource")
    private DataSource custDataSource;

    @Autowired
    @Qualifier("ordersDataSource")
    private DataSource ordersDataSource;

    @Test
    public void dataSourcesExisted() {
        assertThat(custDataSource).isNotNull();
        assertThat(ordersDataSource).isNotNull();
    }

}
