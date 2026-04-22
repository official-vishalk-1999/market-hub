package com.markethub.kafka;

import com.markethub.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryConsumer {

    private final ProductRepository productRepository;

    public InventoryConsumer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

}