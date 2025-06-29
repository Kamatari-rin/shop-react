package org.example.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
@Slf4j
@Component
public class JwtLoggingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info(">>> JwtLoggingFilter triggered for path: {}", exchange.getRequest().getPath());
        log.info(">>> Incoming headers: {}", exchange.getRequest().getHeaders());
        log.info(">>> Authorization: {}", exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

        return exchange.getPrincipal()
                .doOnNext(principal -> {
                    if (principal instanceof JwtAuthenticationToken token) {
                        Jwt jwt = token.getToken();
                        log.info(">>> JWT subject: {}", jwt.getSubject());
                        log.info(">>> JWT claims: {}", jwt.getClaims());
                        log.info(">>> JWT authorities: {}", token.getAuthorities());
                    } else {
                        log.info(">>> Principal is not JWT: {}", principal.getClass());
                    }
                })
                .then(chain.filter(exchange));
    }

}