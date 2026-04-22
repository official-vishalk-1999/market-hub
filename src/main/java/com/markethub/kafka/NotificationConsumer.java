package com.markethub.kafka;

import com.markethub.entity.Notification;
import com.markethub.entity.Product;
import com.markethub.repository.NotificationRepository;
import com.markethub.repository.ProductRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;

    public NotificationConsumer(NotificationRepository notificationRepository,
                                ProductRepository productRepository) {
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void consume(String message) {

        String[] parts = message.split(",");

        Long orderId = Long.parseLong(parts[0]);
        Long productId = Long.parseLong(parts[1]);
        int quantity = Integer.parseInt(parts[2]);

        Product product = productRepository.findById(productId).orElse(null);

        if (product != null) {

            Notification notification = new Notification();

            notification.setMessage(
                    "Order ID " + orderId +
                            " placed for " + product.getName() +
                            " (Qty: " + quantity + ")"
            );

            notification.setCreatedAt(LocalDateTime.now());

            notificationRepository.save(notification);
        }
    }
}