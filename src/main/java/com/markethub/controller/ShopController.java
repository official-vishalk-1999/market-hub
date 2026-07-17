package com.markethub.controller;

import com.markethub.entity.Order;
import com.markethub.entity.Product;
import com.markethub.kafka.OrderProducer;
import com.markethub.repository.OrderRepository;
import com.markethub.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ShopController {

    private final ProductRepository products;
    private final OrderRepository orders;
    private final OrderProducer producer;

    public ShopController(ProductRepository products, OrderRepository orders, OrderProducer producer) {
        this.products = products;
        this.orders = orders;
        this.producer = producer;
    }

    @GetMapping("/products")
    @Cacheable("products")
    public List<Product> list() {
        return products.findAll();
    }

    @PostMapping("/orders")
    public Order place(@RequestBody Order order) {
        Order saved = orders.save(order);
        producer.publish(saved);
        return saved;
    }
}
