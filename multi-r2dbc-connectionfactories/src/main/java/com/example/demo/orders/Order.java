package com.example.demo.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.io.Serializable;

// see: https://r2dbc.io/spec/0.8.2.RELEASE/spec/html/#datatypes.mapping.numeric
// and https://www.postgresql.org/docs/10/datatype-numeric.html
@Table(value = "ORDERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    @Id
    @Column(value = "ID")
    private Integer id;

    @Column(value = "CUST_ID")
    private Long customerId;

    // use BigDecimal or Java Money API in the real world application.
    @Column(value = "AMOUNT")
    private Double amount;
}
