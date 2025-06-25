package org.example.config;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.*;
//import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
//import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
//import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
//import org.springframework.web.reactive.function.client.WebClient;

//@Configuration
//@ConditionalOnProperty(name = "app.default-client-id")
//public class WebClientConfig {
//
//    @Value("${app.default-client-id}")
//    private String defaultClientRegistrationId;
//
//    @Bean
//    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
//                               ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
//        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
//                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
//        oauth2.setDefaultClientRegistrationId(defaultClientRegistrationId);
//        return WebClient.builder()
//                .filter(oauth2)
//                .build();
//    }
//
//    @Bean
//    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
//            ReactiveClientRegistrationRepository clientRegistrationRepository,
//            ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
//
//        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
//                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
//                        .clientCredentials()
//                        .build();
//
//        DefaultReactiveOAuth2AuthorizedClientManager manager =
//                new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
//
//        manager.setAuthorizedClientProvider(authorizedClientProvider);
//        return manager;
//    }
//
//
//    @Bean
//    public ReactiveOAuth2AuthorizedClientService authorizedClientService(
//            ReactiveClientRegistrationRepository clientRegistrationRepository) {
//        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
//    }
//}