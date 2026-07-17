package com.markethub;

import com.markethub.entity.Product;
import com.markethub.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class MarketHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketHubApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(ProductRepository products) {
        return args -> {
                products.save(product("Laptop", 60000, 10));
                products.save(product("Phone", 30000, 10));
                products.save(product("Headphones", 2000, 10));
        };
    }

    private static Product product(String name, double price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        return p;
    }
}
