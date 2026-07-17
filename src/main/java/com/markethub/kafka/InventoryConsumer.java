package com.markethub.kafka;

import com.markethub.repository.ProductRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryConsumer {

    private final ProductRepository products;

    public InventoryConsumer(ProductRepository products) {
        this.products = products;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void consume(String event) {
        String[] parts = event.split(",");
        Long productId = Long.parseLong(parts[0]);
        int quantity = Integer.parseInt(parts[1]);
        products.findById(productId).ifPresent(product -> {
            product.setStock(product.getStock() - quantity);
            products.save(product);
        });
    }
}
