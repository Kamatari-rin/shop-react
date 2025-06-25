package org.example.config;

//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
//import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
//import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Configuration
//public class WebClientConfig {
//
//    @Bean
//    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
//        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
//                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
//        oauth2.setDefaultClientRegistrationId("purchase-service");
//        return WebClient.builder()
//                .filter(oauth2)
//                .build();
//    }
//
//    @Bean
//    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
//            ReactiveClientRegistrationRepository clientRegistrationRepository) {
//        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
//                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
//                        .clientCredentials()
//                        .build();
//        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager =
//                new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, null);
//        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
//        return authorizedClientManager;
//    }
//}