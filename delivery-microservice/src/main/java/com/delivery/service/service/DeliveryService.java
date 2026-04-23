package com.delivery.service.service;

import com.delivery.service.dto.DeliveryResponse;
import com.delivery.service.dto.DeliveryStatusUpdatedEvent;
import com.delivery.service.dto.OrderCancelledEvent;
import com.delivery.service.dto.OrderPlacedEvent;
import com.delivery.service.model.Delivery;
import com.delivery.service.repository.DeliveryRepository;
import com.shared.definitions.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class DeliveryService {
    private static final String[] DRIVERS = {"Carlos M.", "Sarah J.", "Mike C.", "Priya P.", "James W."};
    private static final String[] PHONES = {"+1-555-0101", "+1-555-0102", "+1-555-0103", "+1-555-0104", "+1-555-0105"};

    private final DeliveryRepository deliveryRepository;
    private final RabbitTemplate rabbitTemplate;

    public DeliveryService(DeliveryRepository deliveryRepository, RabbitTemplate rabbitTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.queues.orderPlaced:order.placed.queue}")
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Processing order placed event: orderId={}, customerId={}", event.orderId(), event.customerId());
        if (deliveryRepository.findByOrderId(event.orderId()).isPresent()) {
            log.debug("Delivery already exists for orderId={}, skipping", event.orderId());
            return;
        }
        int idx = (int) (Math.random() * DRIVERS.length);
        Delivery delivery = new Delivery();
        delivery.setOrderId(event.orderId());
        delivery.setCustomerId(event.customerId());
        delivery.setRestaurantId(event.restaurantId());
        delivery.setStatus(Delivery.DeliveryStatus.ASSIGNED);
        delivery.setDriverName(DRIVERS[idx]);
        delivery.setDriverPhone(PHONES[idx]);
        deliveryRepository.save(delivery);
        log.info("Delivery assigned: orderId={}, driver={}", event.orderId(), DRIVERS[idx]);
    }

    @RabbitListener(queues = "${app.queues.orderCancelled:order.cancelled.queue}")
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Processing order cancelled event: orderId={}", event.orderId());
        deliveryRepository.findByOrderId(event.orderId()).ifPresent(delivery -> {
            delivery.setStatus(Delivery.DeliveryStatus.FAILED);
            deliveryRepository.save(delivery);
            log.info("Delivery marked FAILED due to order cancellation: orderId={}", event.orderId());
        });
    }

    @Transactional
    public DeliveryResponse updateStatus(Long deliveryId, String status) {
        log.info("Updating delivery status: deliveryId={}, newStatus={}", deliveryId, status);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        Delivery.DeliveryStatus newStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
        delivery.setStatus(newStatus);
        Delivery saved = deliveryRepository.save(delivery);

        rabbitTemplate.convertAndSend("delivery.events", "delivery.status.updated",
                new DeliveryStatusUpdatedEvent(
                        saved.getId(),
                        saved.getOrderId(),
                        saved.getCustomerId(),
                        saved.getStatus().name(),
                        LocalDateTime.now()
                )
        );

        log.info("Delivery status updated: deliveryId={}, orderId={}, status={}", saved.getId(), saved.getOrderId(), newStatus);
        return DeliveryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .map(DeliveryResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .map(DeliveryResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
    }
}
