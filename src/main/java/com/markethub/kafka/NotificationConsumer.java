package com.markethub.kafka;

import com.markethub.entity.Notification;
import com.markethub.entity.Product;
import com.markethub.repository.NotificationRepository;
import com.markethub.repository.ProductRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final NotificationRepository notifications;
    private final ProductRepository products;

    public NotificationConsumer(NotificationRepository notifications, ProductRepository products) {
        this.notifications = notifications;
        this.products = products;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void consume(String event) {
        String[] parts = event.split(",");
        Long productId = Long.parseLong(parts[0]);
        int quantity = Integer.parseInt(parts[1]);
        String name = products.findById(productId).map(Product::getName).orElse("Unknown");

        Notification notification = new Notification();
        notification.setMessage("Product - " + name + ", Quantity Ordered - " + quantity);
        notifications.save(notification);
    }
}