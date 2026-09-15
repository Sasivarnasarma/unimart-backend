package varna.mit.kln.unimart.order.controller;

import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.order.dto.OrderRequestDto;
import varna.mit.kln.unimart.order.dto.OrderResponseDto;
import varna.mit.kln.unimart.order.dto.OrderStatusUpdateDto;
import varna.mit.kln.unimart.order.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto requestDto,
            Principal principal) {
        OrderResponseDto responseDto = orderService.createOrder(requestDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(Principal principal) {
        List<OrderResponseDto> orders = orderService.getUserOrders(principal.getName());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Integer id,
            Principal principal) {
        OrderResponseDto responseDto = orderService.getOrderById(id, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody OrderStatusUpdateDto statusUpdateDto,
            Principal principal) {
        OrderResponseDto responseDto = orderService.updateOrderStatus(id, statusUpdateDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
