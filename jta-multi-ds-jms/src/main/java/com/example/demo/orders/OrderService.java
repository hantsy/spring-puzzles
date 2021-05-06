package com.example.demo.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orders;
    private final JmsTemplate jmsTemplate;

    @Transactional
    public OrderEntity placeOrder(OrderRequest info){
        var entity = OrderEntity.builder()
                .customer(new CustomerId(info.getCustomerId()))
                .amount(info.getAmount())
                .build();
        var savedEntity = this.orders.save(entity);
        this.jmsTemplate.convertAndSend("ORDERS", new OrderPlacedEvent(savedEntity.getId()));

        return savedEntity;
    }
}
