package varna.mit.kln.unimart.payment.service;

import varna.mit.kln.unimart.payment.dto.PaymentRequestDto;
import varna.mit.kln.unimart.payment.dto.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto requestDto, String userEmail);
    PaymentResponseDto getPaymentByOrderId(Integer orderId, String userEmail);
}
