package com.mepsan.iot_simulator_service.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_SALES = "mepsan.exchange.sales";
    public static final String EXCHANGE_TELEMETRY = "mepsan.exchange.telemetry";
    public static final String EXCHANGE_REFILL = "mepsan.exchange.refill";

    // KRİTİK EKLENTİ: JSON Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public TopicExchange salesExchange() {
        return new TopicExchange(EXCHANGE_SALES, true, false);
    }

    @Bean
    public TopicExchange telemetryExchange() {
        return new TopicExchange(EXCHANGE_TELEMETRY, true, false);
    }

    @Bean
    public TopicExchange refillExchange() {
        return new TopicExchange(EXCHANGE_REFILL, true, false);
    }
}
