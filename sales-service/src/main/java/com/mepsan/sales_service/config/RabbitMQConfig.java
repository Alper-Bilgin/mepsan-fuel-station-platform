package com.mepsan.sales_service.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_SALES = "mepsan.exchange.sales";
    public static final String EXCHANGE_WAIT = "mepsan.exchange.sales.wait";

    public static final String QUEUE_SALES = "sales-service.sales.queue";
    public static final String QUEUE_WAIT = "sales-service.sales.wait.queue";

    // --- Ana Kuyruk ---
    @Bean
    public Queue salesQueue() {
        return QueueBuilder.durable(QUEUE_SALES)
                .withArgument("x-dead-letter-exchange", EXCHANGE_WAIT)
                .withArgument("x-dead-letter-routing-key", "sales.wait")
                .build();
    }

    // --- Bekleme (Retry) Kuyruğu (5 saniye TTL) ---
    @Bean
    public Queue salesWaitQueue() {
        return QueueBuilder.durable(QUEUE_WAIT)
                .withArgument("x-dead-letter-exchange", EXCHANGE_SALES)
                .withArgument("x-dead-letter-routing-key", "sales.retry")
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public TopicExchange salesExchange() {
        return new TopicExchange(EXCHANGE_SALES, true, false);
    }

    @Bean
    public TopicExchange salesWaitExchange() {
        return new TopicExchange(EXCHANGE_WAIT, true, false);
    }

    // Simülatörden gelen 'sales.#' ve bekleyip dönen 'sales.retry' bu kuyruğa düşer
    @Bean
    public Binding salesBinding(Queue salesQueue, TopicExchange salesExchange) {
        return BindingBuilder.bind(salesQueue).to(salesExchange).with("sales.#");
    }

    @Bean
    public Binding waitBinding(Queue salesWaitQueue, TopicExchange salesWaitExchange) {
        return BindingBuilder.bind(salesWaitQueue).to(salesWaitExchange).with("sales.wait");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
