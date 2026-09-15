package varna.mit.kln.unimart.payment.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.payment.dto.PaymentRequestDto;
import varna.mit.kln.unimart.payment.dto.PaymentResponseDto;
import varna.mit.kln.unimart.payment.service.PaymentService;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto requestDto,
            Principal principal) {
        PaymentResponseDto responseDto = paymentService.processPayment(requestDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(
            @PathVariable Integer orderId,
            Principal principal) {
        PaymentResponseDto responseDto = paymentService.getPaymentByOrderId(orderId, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
