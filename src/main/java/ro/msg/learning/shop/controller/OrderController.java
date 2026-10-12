package ro.msg.learning.shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.msg.learning.shop.dto.request.OrderCreateRequest;
import ro.msg.learning.shop.dto.response.OrderResponse;
import ro.msg.learning.shop.mapper.OrderMapper;
import ro.msg.learning.shop.model.Order;
import ro.msg.learning.shop.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.createOrder(orderMapper.toModel(request));
        return orderMapper.toResponse(order);
    }
}
