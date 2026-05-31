package org.example.yourorderasync.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.yourorderasync.kafka.event.OrderCreatedEvent;
import org.example.yourorderasync.kafka.event.StockReservedEvent;
import org.example.yourorderasync.payment.entity.PaymentEntity;
import org.example.yourorderasync.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final Map<UUID, OrderCreatedEvent> pendingOrders = new ConcurrentHashMap<>();
    private final Set<UUID> reservedOrders = ConcurrentHashMap.newKeySet();

    @KafkaListener(
            topics = "${kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onOrderCreated(String message) throws Exception {
        OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
        log.info("Received order.created: orderId={}", event.orderId());

        pendingOrders.put(event.orderId(), event);
        processPaymentWhenStockReserved(event.orderId());
    }

    @KafkaListener(
            topics = "${kafka.topics.stock-reserved}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onStockReserved(String message) throws Exception {
        StockReservedEvent event = objectMapper.readValue(message, StockReservedEvent.class);
        log.info("Received stock.reserved: orderId={}", event.orderId());

        reservedOrders.add(event.orderId());
        processPaymentWhenStockReserved(event.orderId());
    }

    private void processPaymentWhenStockReserved(UUID orderId) {
        OrderCreatedEvent event = pendingOrders.get(orderId);
        if (event == null || !reservedOrders.contains(orderId)) {
            return;
        }

        PaymentEntity payment = paymentService.createPendingPayment(event);
        log.info("Payment is waiting for admin confirmation: paymentId={}, orderId={}", payment.getId(), event.orderId());

        pendingOrders.remove(orderId);
        reservedOrders.remove(orderId);
    }
}
