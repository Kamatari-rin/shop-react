package org.example.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final String clientId;

    public CustomJwtAuthenticationConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = new HashSet<>();

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientAccess != null && clientAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> clientRoles = (List<String>) clientAccess.get("roles");
                authorities.addAll(clientRoles.stream()
                        .filter(role -> role.startsWith("ROLE_"))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));
            }
        }

        AbstractAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);
        return Mono.just(token);
    }
}