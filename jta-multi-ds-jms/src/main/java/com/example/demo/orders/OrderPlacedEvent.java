package com.example.demo.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class OrderPlacedEvent implements Serializable {
    private Long orderId;
}
