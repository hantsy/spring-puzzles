package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "ORDERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity extends AbstractEntity {

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
