package com.mepsan.telemetry_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_TELEMETRY = "mepsan.exchange.telemetry";
    public static final String QUEUE_TELEMETRY = "telemetry-service.telemetry.queue";

    @Bean
    public TopicExchange telemetryExchange() {
        return new TopicExchange(EXCHANGE_TELEMETRY, true, false);
    }

    @Bean
    public Queue telemetryQueue() {
        return QueueBuilder.durable(QUEUE_TELEMETRY).build();
    }

    @Bean
    public Binding telemetryBinding(Queue telemetryQueue, TopicExchange telemetryExchange) {
        return BindingBuilder.bind(telemetryQueue).to(telemetryExchange).with("telemetry.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
