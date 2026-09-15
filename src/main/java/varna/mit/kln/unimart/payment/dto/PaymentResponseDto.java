package varna.mit.kln.unimart.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import varna.mit.kln.unimart.payment.entity.Payment;
import varna.mit.kln.unimart.payment.entity.PaymentStatus;

public class PaymentResponseDto {

    private Integer id;
    private Integer orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String providerReference;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    public PaymentResponseDto() {}

    public PaymentResponseDto(Payment payment) {
        this.id = payment.getId();
        if (payment.getOrder() != null) {
            this.orderId = payment.getOrder().getId();
        }
        this.amount = payment.getAmount();
        this.status = payment.getStatus();
        this.providerReference = payment.getProviderReference();
        this.idempotencyKey = payment.getIdempotencyKey();
        this.createdAt = payment.getCreatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
