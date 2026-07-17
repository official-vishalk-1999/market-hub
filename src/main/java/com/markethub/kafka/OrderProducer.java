package com.markethub.kafka;

import com.markethub.entity.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, String> kafka;

    public OrderProducer(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    public void publish(Order order) {
        kafka.send("order-events", order.getProductId() + "," + order.getQuantity());
    }
}
