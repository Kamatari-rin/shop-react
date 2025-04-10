package org.example.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class CartServiceConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.newConnection()
                                .responseTimeout(Duration.ofMillis(5000)) // Аналог readTimeout
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Аналог connectTimeout
                ));
    }
}