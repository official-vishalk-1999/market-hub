package com.markethub.controller;

import com.markethub.entity.Order;
import com.markethub.repository.OrderRepository;
import com.markethub.kafka.OrderProducer;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public OrderController(OrderRepository orderRepository, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        String event = savedOrder.getId() + "," +
                savedOrder.getProductId() + "," +
                savedOrder.getQuantity();

        orderProducer.sendOrderEvent(event);

        return savedOrder;
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }
}