package com.example.command;

import com.example.command.events.ProductCreatedEvent;
import com.example.command.events.OrderCreatedEvent;
import com.example.command.events.OrderUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.topics.product}")
    private String productTopic;

    @Value("${app.topics.order}")
    private String orderTopic;

    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishProductCreated(ProductCreatedEvent event) {
        String key = String.valueOf(event.getPayload().getId());
        kafkaTemplate.send(productTopic, key, event);
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = String.valueOf(event.getPayload().getId());
        kafkaTemplate.send(orderTopic, key, event);
    }

    public void publishOrderUpdated(OrderUpdatedEvent event) {
        String key = String.valueOf(event.getPayload().getOrderId());
        kafkaTemplate.send(orderTopic, key, event);
    }
}
