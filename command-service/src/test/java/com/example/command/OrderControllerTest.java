package com.example.command;

import com.example.command.dto.OrderRequest;
import com.example.command.dto.OrderStatusUpdateRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Test
    void createOrder_returnsCreatedOrder() {
        OrderService orderService = Mockito.mock(OrderService.class);
        OrderController controller = new OrderController(orderService);

        OrderRequest req = new OrderRequest();
        req.setCustomerId(1L);

        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductId(5L);
        item.setQuantity(2);
        item.setPrice(500.0);
        req.setItems(List.of(item));

        Order fakeOrder = new Order();
        fakeOrder.setId(10L);
        fakeOrder.setStatus("CREATED");
        fakeOrder.setCustomerId(1L);
        when(orderService.createOrder(req)).thenReturn(fakeOrder);

        var response = controller.createOrder(req);

        var order = response.getBody();
        assertNotNull(order);
        assertEquals(10L, order.getId());
        assertEquals("CREATED", order.getStatus());
        assertEquals(1L, order.getCustomerId());

        verify(orderService, times(1)).createOrder(req);
    }

    @Test
    void updateStatus_returnsUpdatedOrder() {
        OrderService orderService = Mockito.mock(OrderService.class);
        OrderController controller = new OrderController(orderService);

        Long orderId = 10L;

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus("SHIPPED");

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setCustomerId(1L);
        updatedOrder.setStatus("SHIPPED");

        when(orderService.updateOrderStatus(orderId, request)).thenReturn(updatedOrder);

        var response = controller.updateStatus(orderId, request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(orderId, body.getId());
        assertEquals("SHIPPED", body.getStatus());

        verify(orderService, times(1)).updateOrderStatus(orderId, request);
    }
}
