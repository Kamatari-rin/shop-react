package org.example.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "app.webclient", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class WebClientAutoConfiguration {

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrations(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>();
        properties.getRegistration().forEach((key, registration) -> {
            var provider = properties.getProvider().get(registration.getProvider());
            registrations.add(ClientRegistration.withRegistrationId(key)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .tokenUri(provider.getTokenUri())
                    .build());
        });
        return new InMemoryReactiveClientRegistrationRepository(registrations);
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clients,
            ReactiveOAuth2AuthorizedClientService clientService) {

        var provider = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clients, clientService);
        var delegate = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        provider.setAuthorizedClientProvider(delegate);
        return provider;
    }

    @Bean
    public WebClient.Builder webClientBuilder(ReactiveOAuth2AuthorizedClientManager manager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth2.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder().filter(oauth2);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
