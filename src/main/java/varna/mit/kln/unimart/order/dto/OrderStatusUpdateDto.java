package varna.mit.kln.unimart.order.dto;

import jakarta.validation.constraints.NotNull;
import varna.mit.kln.unimart.order.entity.OrderStatus;

public class OrderStatusUpdateDto {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    public OrderStatusUpdateDto() {}

    public OrderStatusUpdateDto(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
