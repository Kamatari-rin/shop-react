package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.webflux.LogbookWebFilter;

@Configuration
public class LogbookConfig {
    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .condition(request -> !request.getPath().startsWith("/actuator"))
                .build();
    }

    @Bean
    public LogbookWebFilter logbookWebFilter(Logbook logbook) {
        return new LogbookWebFilter(logbook);
    }
}