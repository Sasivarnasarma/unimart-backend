package varna.mit.kln.unimart.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;
import varna.mit.kln.unimart.payment.dto.PaymentRequestDto;
import varna.mit.kln.unimart.payment.dto.PaymentResponseDto;
import varna.mit.kln.unimart.payment.entity.Payment;
import varna.mit.kln.unimart.payment.entity.PaymentStatus;
import varna.mit.kln.unimart.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private PaymentService paymentService;

    private User buyer;
    private User seller;
    private Order order;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, orderRepository, userRepository);

        buyer = new User();
        buyer.setId(1);
        buyer.setUniversityEmail("pay_buyer@kln.ac.lk");
        buyer.setFullName("Payment Buyer");

        seller = new User();
        seller.setId(2);
        seller.setUniversityEmail("pay_seller@kln.ac.lk");
        seller.setFullName("Payment Seller");

        order = new Order();
        order.setId(10);
        order.setBuyer(buyer);
        order.setTotalAmount(new BigDecimal("75.00"));
        order.setStatus(OrderStatus.pending);
    }

    @Test
    void processPayment_Success() {
        PaymentRequestDto request = new PaymentRequestDto(10, new BigDecimal("75.00"), "IDEM-KEY-001", "PROV-REF-1");

        when(paymentRepository.findByIdempotencyKey("IDEM-KEY-001")).thenReturn(Optional.empty());
        when(userRepository.findByUniversityEmail("pay_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10)).thenReturn(Optional.empty());

        Payment savedPayment = new Payment(order, new BigDecimal("75.00"), PaymentStatus.success, "PROV-REF-1", "IDEM-KEY-001");
        savedPayment.setId(200);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponseDto response = paymentService.processPayment(request, "pay_buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(200, response.getId());
        assertEquals(10, response.getOrderId());
        assertEquals(PaymentStatus.success, response.getStatus());
        assertEquals("IDEM-KEY-001", response.getIdempotencyKey());
        assertEquals(OrderStatus.paid, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void processPayment_Idempotent_ReturnsExistingPayment() {
        PaymentRequestDto request = new PaymentRequestDto(10, new BigDecimal("75.00"), "IDEM-KEY-001", "PROV-REF-1");

        Payment existingPayment = new Payment(order, new BigDecimal("75.00"), PaymentStatus.success, "PROV-REF-1", "IDEM-KEY-001");
        existingPayment.setId(200);

        when(paymentRepository.findByIdempotencyKey("IDEM-KEY-001")).thenReturn(Optional.of(existingPayment));

        PaymentResponseDto response = paymentService.processPayment(request, "pay_buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(200, response.getId());
        assertEquals("IDEM-KEY-001", response.getIdempotencyKey());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_NonBuyer_ThrowsAccessDeniedException() {
        PaymentRequestDto request = new PaymentRequestDto(10, new BigDecimal("75.00"), "IDEM-KEY-002", "PROV-REF-2");

        when(paymentRepository.findByIdempotencyKey("IDEM-KEY-002")).thenReturn(Optional.empty());
        when(userRepository.findByUniversityEmail("pay_seller@kln.ac.lk")).thenReturn(Optional.of(seller));
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> paymentService.processPayment(request, "pay_seller@kln.ac.lk"));
    }

    @Test
    void processPayment_CancelledOrder_ThrowsIllegalStateException() {
        order.setStatus(OrderStatus.cancelled);
        PaymentRequestDto request = new PaymentRequestDto(10, new BigDecimal("75.00"), "IDEM-KEY-003", "PROV-REF-3");

        when(paymentRepository.findByIdempotencyKey("IDEM-KEY-003")).thenReturn(Optional.empty());
        when(userRepository.findByUniversityEmail("pay_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> paymentService.processPayment(request, "pay_buyer@kln.ac.lk"));
    }

    @Test
    void getPaymentByOrderId_Success() {
        Payment payment = new Payment(order, new BigDecimal("75.00"), PaymentStatus.success, "PROV-REF-1", "IDEM-KEY-001");
        payment.setId(200);

        when(userRepository.findByUniversityEmail("pay_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10)).thenReturn(Optional.of(payment));

        PaymentResponseDto response = paymentService.getPaymentByOrderId(10, "pay_buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(200, response.getId());
        assertEquals(10, response.getOrderId());
    }
}
