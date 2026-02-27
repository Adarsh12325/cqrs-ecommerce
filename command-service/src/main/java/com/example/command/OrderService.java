package com.example.command;

import com.example.command.dto.OrderRequest;
import com.example.command.dto.OrderStatusUpdateRequest;
import com.example.command.events.OrderCreatedEvent;
import com.example.command.events.OrderUpdatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaEventProducer eventProducer;

    public OrderService(OrderRepository orderRepository, KafkaEventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus("CREATED");

        var items = request.getItems().stream()
                .map(i -> new Order.OrderItem(i.getProductId(), i.getQuantity(), i.getPrice()))
                .collect(Collectors.toList());
        order.setItems(items);

        Order saved = orderRepository.save(order);

        OrderCreatedEvent.Payload payload =
                new OrderCreatedEvent.Payload(saved.getId(), saved.getCustomerId(), saved.getStatus(), saved.getItems());
        eventProducer.publishOrderCreated(new OrderCreatedEvent(payload));

        return saved;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(request.getStatus());
        Order saved = orderRepository.save(order);

        OrderUpdatedEvent.Payload payload =
                new OrderUpdatedEvent.Payload(saved.getId(), saved.getStatus());
        eventProducer.publishOrderUpdated(new OrderUpdatedEvent(payload));

        return saved;
    }
}
