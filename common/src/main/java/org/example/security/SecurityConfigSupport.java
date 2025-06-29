package org.example.security;

import org.example.config.CustomJwtAuthenticationConverter;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;
import java.util.List;

public class SecurityConfigSupport {

    public static SecurityWebFilterChain buildDefaultSecurityFilterChain(
            ServerHttpSecurity http,
            List<String> permitAllPaths,
            String jwkSetUri,
            List<String> allowedRoles
    ) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        http.authorizeExchange(authorize -> {

            if (permitAllPaths != null) {
                permitAllPaths.forEach(path -> authorize.pathMatchers(path).permitAll());
            }

            authorize.anyExchange().access((authMono, ctx) ->
                    authMono.map(auth -> auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(allowedRoles::contains)
                    ).map(AuthorizationDecision::new)
            );
        });

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                        .jwkSetUri(jwkSetUri)
                )
        );

        return http.build();
    }
}
