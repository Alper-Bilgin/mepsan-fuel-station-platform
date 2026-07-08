package com.mepsan.station_core_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {

    public static final String QUEUE_REFILL = "station-core.refill.queue";
    public static final String EXCHANGE_REFILL = "mepsan.exchange.refill";

    // DLQ Tanımlamaları
    public static final String DLX_REFILL = "mepsan.exchange.refill.dlx";
    public static final String DLQ_REFILL = "station-core.refill.dlq";

    @Bean
    public Queue refillQueue() {
        return QueueBuilder.durable(QUEUE_REFILL)
                .withArgument("x-dead-letter-exchange", DLX_REFILL)
                .withArgument("x-dead-letter-routing-key", "refill.dlq")
                .build();
    }

    @Bean
    public TopicExchange refillExchange() {
        return new TopicExchange(EXCHANGE_REFILL, true, false);
    }

    @Bean
    public Binding refillBinding(Queue refillQueue, TopicExchange refillExchange) {
        return BindingBuilder.bind(refillQueue).to(refillExchange).with("refill.#");
    }

    // --- DLQ (Dead Letter Queue) Konfigürasyonları ---
    @Bean
    public TopicExchange refillDlx() {
        return new TopicExchange(DLX_REFILL, true, false);
    }

    @Bean
    public Queue refillDlq() {
        return QueueBuilder.durable(DLQ_REFILL).build();
    }

    @Bean
    public Binding refillDlqBinding(Queue refillDlq, TopicExchange refillDlx) {
        return BindingBuilder.bind(refillDlq).to(refillDlx).with("refill.dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}