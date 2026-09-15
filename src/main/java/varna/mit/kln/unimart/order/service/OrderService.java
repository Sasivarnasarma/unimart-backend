package varna.mit.kln.unimart.order.service;

import java.util.List;
import varna.mit.kln.unimart.order.dto.OrderRequestDto;
import varna.mit.kln.unimart.order.dto.OrderResponseDto;
import varna.mit.kln.unimart.order.dto.OrderStatusUpdateDto;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto requestDto, String userEmail);
    List<OrderResponseDto> getUserOrders(String userEmail);
    OrderResponseDto getOrderById(Integer id, String userEmail);
    OrderResponseDto updateOrderStatus(Integer id, OrderStatusUpdateDto statusUpdateDto, String userEmail);
}
