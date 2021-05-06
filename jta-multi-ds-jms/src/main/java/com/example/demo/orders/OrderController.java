package com.example.demo.orders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderRepository orders;
    private final OrderService service;

    @GetMapping("")
    public ResponseEntity<List<OrderEntity>> all() {
        return ok().body(this.orders.findAll());
    }

    @PostMapping("")
    public ResponseEntity<Void> save(@RequestBody OrderRequest request) {
        var order = this.service.placeOrder(request);
        log.info("placed order: {}", order);
        return created(URI.create("/orders/" + order.getId())).build();
    }
}

