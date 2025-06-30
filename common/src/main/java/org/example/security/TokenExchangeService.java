//package org.example.security;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Mono;
//
//@Service
//@ConditionalOnProperty(name = "app.webclient.enabled", havingValue = "true", matchIfMissing = true)
//public class TokenExchangeService {
//    private static final Logger log = LoggerFactory.getLogger(TokenExchangeService.class);
//
//    private final WebClient webClient;
//    private final String clientId;
//    private final String clientSecret;
//    private final String tokenExchangeEndpoint;
//
//    public TokenExchangeService(
//            WebClient.Builder webClientBuilder,
//            @Value("${app.webclient.default-client-registration-id}") String clientId,
//            @Value("${app.webclient.client-secret}") String clientSecret,
//            @Value("${app.webclient.token-exchange-endpoint}") String tokenExchangeEndpoint
//    ) {
//        this.webClient = webClientBuilder.build();
//        this.clientId = clientId;
//        this.clientSecret = clientSecret;
//        this.tokenExchangeEndpoint = tokenExchangeEndpoint;
//    }
//
//    public Mono<String> exchangeToken(String audience) {
//        log.debug("Attempting to exchange token for audience: {}", audience);
//        return ReactiveSecurityContextHolder.getContext()
//                .doOnNext(context -> log.debug("Security context found: {}", context))
//                .switchIfEmpty(Mono.defer(() -> {
//                    log.error("No security context found for audience: {}", audience);
//                    return Mono.error(new SecurityException("No security context found"));
//                }))
//                .map(context -> context.getAuthentication())
//                .doOnNext(auth -> log.debug("Authentication object: {}", auth))
//                .map(auth -> auth.getPrincipal())
//                .doOnNext(principal -> log.debug("Principal: {}", principal))
//                .cast(Jwt.class)
//                .doOnNext(jwt -> log.debug("JWT extracted: sub={}, aud={}, exp={}",
//                        jwt.getSubject(), jwt.getAudience(), jwt.getExpiresAt()))
//                .map(Jwt::getTokenValue)
//                .doOnNext(token -> log.debug("Using subject_token: {}", token))
//                .flatMap(subjectToken -> webClient.post()
//                        .uri(tokenExchangeEndpoint)
//                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                        .bodyValue(new LinkedMultiValueMap<String, String>() {{
//                            add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
//                            add("client_id", clientId);
//                            add("client_secret", clientSecret);
//                            add("subject_token", subjectToken);
//                            add("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
//                            add("requested_token_type", "urn:ietf:params:oauth:token-type:access_token");
//                            add("audience", audience);
//                        }})
//                        .retrieve()
//                        .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
//                                .doOnNext(body -> log.error("Keycloak error response for audience {}: {}", audience, body))
//                                .flatMap(body -> Mono.error(new WebClientResponseException(
//                                        response.statusCode().value(),
//                                        response.statusCode().toString(),
//                                        null, body.getBytes(), null))))
//                        .bodyToMono(TokenExchangeResponse.class)
//                        .map(TokenExchangeResponse::getAccessToken))
//                .doOnSuccess(token -> log.debug("Token exchanged successfully for audience: {}", audience))
//                .doOnError(e -> log.error("Failed to exchange token for audience: {}, error: {}", audience, e.getMessage(), e));
//    }
//
//    private static class TokenExchangeResponse {
//        private String access_token;
//
//        public String getAccessToken() {
//            return access_token;
//        }
//
//        public void setAccess_token(String access_token) {
//            this.access_token = access_token;
//        }
//    }
//}