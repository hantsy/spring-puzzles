package com.example.demo.orders;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class EventLogger {

    private OrderPlacedEvent lastEvent;

    @JmsListener(destination = "ORDERS")
    public void receiveEvents(OrderPlacedEvent event) {
        log.info("received event: {}", event);
        this.lastEvent = event;
    }

    public void reset() {
        this.lastEvent = null;
    }

}
