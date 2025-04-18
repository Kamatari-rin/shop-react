package org.example.config;

import org.example.util.MDCContextLifter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class TracingAutoConfiguration {
    @Bean
    @Order(1)
    public TracingFilter tracingFilter() {
        return new TracingFilter();
    }

    @Bean
    public MDCContextLifter mdcContextLifter() {
        return new MDCContextLifter();
    }
}