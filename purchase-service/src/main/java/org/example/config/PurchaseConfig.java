package org.example.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class PurchaseConfig {
    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
