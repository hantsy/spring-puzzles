package com.example.demo.orders;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class CustomerId {
    private Long id;
}
