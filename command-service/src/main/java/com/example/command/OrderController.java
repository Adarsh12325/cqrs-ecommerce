package com.example.command;

import com.example.command.dto.OrderRequest;
import com.example.command.dto.OrderStatusUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(201).body(order);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long orderId,
                                              @RequestBody OrderStatusUpdateRequest request) {
        Order updated = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(updated);
    }
}
