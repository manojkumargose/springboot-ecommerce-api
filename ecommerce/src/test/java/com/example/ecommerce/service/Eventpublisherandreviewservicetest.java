package com.example.ecommerce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherServiceTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private EventPublisherService eventPublisherService;

    @Test
    @DisplayName("Should publish product sync event without error")
    void publishProductSync_Success() {
        assertThatCode(() ->
                eventPublisherService.publishProductSync(1L, "Laptop", 999.99)
        ).doesNotThrowAnyException();

        verify(rabbitTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should publish product view event")
    void publishProductView_Success() {
        assertThatCode(() ->
                eventPublisherService.publishProductView(1L, 42L)
        ).doesNotThrowAnyException();

        verify(rabbitTemplate).convertAndSend(anyString(), any(Object.class));
    }
}