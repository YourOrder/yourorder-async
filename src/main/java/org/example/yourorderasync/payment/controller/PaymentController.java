package org.example.yourorderasync.payment.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourorderasync.payment.dto.PaymentResponse;
import org.example.yourorderasync.payment.entity.PaymentEntity;
import org.example.yourorderasync.payment.service.PaymentService;
import org.example.yourorderasync.payment.status.PaymentStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public List<PaymentResponse> getPayments(@RequestParam(required = false) PaymentStatus status) {
        return paymentService.getPayments(status).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/{paymentId}/confirm")
    public PaymentResponse confirmPayment(@PathVariable UUID paymentId) {
        return toResponse(paymentService.confirmPayment(paymentId));
    }

    @PostMapping("/{paymentId}/cancel")
    public PaymentResponse cancelPayment(@PathVariable UUID paymentId) {
        return toResponse(paymentService.cancelPayment(paymentId));
    }

    private PaymentResponse toResponse(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getCompanyId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getProcessedAt()
        );
    }
}
