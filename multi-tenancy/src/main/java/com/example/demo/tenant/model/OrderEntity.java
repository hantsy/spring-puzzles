package com.example.demo.tenant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ORDERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @AttributeOverride(name = "id", column = @Column(name = "CUST_ID"))
    private CustomerId customer;

    // use BigDecimal or Java Money API in the real world application.
    @Column(name = "AMOUNT")
    private Double amount;
}
