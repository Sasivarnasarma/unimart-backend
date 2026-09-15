package varna.mit.kln.unimart.payment.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;
import varna.mit.kln.unimart.payment.dto.PaymentRequestDto;
import varna.mit.kln.unimart.payment.dto.PaymentResponseDto;
import varna.mit.kln.unimart.payment.entity.Payment;
import varna.mit.kln.unimart.payment.entity.PaymentStatus;
import varna.mit.kln.unimart.payment.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto, String userEmail) {
        Optional<Payment> existingIdempotentPayment = paymentRepository.findByIdempotencyKey(requestDto.getIdempotencyKey());
        if (existingIdempotentPayment.isPresent()) {
            return new PaymentResponseDto(existingIdempotentPayment.get());
        }

        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + requestDto.getOrderId()));

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the buyer can make payment for this order");
        }

        Optional<Payment> existingOrderPayment = paymentRepository.findByOrderId(order.getId());
        if (existingOrderPayment.isPresent()) {
            return new PaymentResponseDto(existingOrderPayment.get());
        }

        if (order.getStatus() == OrderStatus.cancelled) {
            throw new IllegalStateException("Cannot process payment for a cancelled order");
        }

        BigDecimal paymentAmount = requestDto.getAmount() != null ? requestDto.getAmount() : order.getTotalAmount();
        String providerRef = requestDto.getProviderReference() != null ? requestDto.getProviderReference() : "TXN-" + UUID.randomUUID().toString();

        Payment payment = new Payment(
                order,
                paymentAmount,
                PaymentStatus.success,
                providerRef,
                requestDto.getIdempotencyKey()
        );

        order.setStatus(OrderStatus.paid);
        orderRepository.save(order);

        Payment savedPayment = paymentRepository.save(payment);
        return new PaymentResponseDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Integer orderId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isSeller = order.getListing() != null && order.getListing().getSeller() != null
                && order.getListing().getSeller().getId().equals(user.getId());

        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException("You are not authorized to view payment details for this order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + orderId));

        return new PaymentResponseDto(payment);
    }
}
