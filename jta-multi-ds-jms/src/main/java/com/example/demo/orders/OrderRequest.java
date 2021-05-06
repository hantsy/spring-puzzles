package com.example.demo.orders;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderRequest implements Serializable {
    Long customerId;
    Double amount = 1.0;
}
