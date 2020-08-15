package com.example.demo.orders;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "orders")
@Data
public class OrderEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @AttributeOverride(name = "id", column = @Column(name = "cust_id"))
    private CustomerId customerId;

    // use BigDecimal or Java Money API in the real world application.
    private Double amount;
}
