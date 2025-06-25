package org.example.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@Conditional(SecurityConfigCondition.class)
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final SecurityProperties props;

    public SecurityConfig(SecurityProperties props) {
        this.props = props;
        log.debug("SecurityConfig initialized with the following settings:");
        log.debug("permitAllPaths: {}", props.getPermitAllPaths());
        log.debug("clientId: {}", props.getClientId());
        log.debug("jwkSetUri: {}", props.getJwkSetUri());
        log.debug("allowedRoles: {}", props.getAllowedRoles());
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return SecurityConfigSupport.buildDefaultSecurityFilterChain(
                http,
                props.getPermitAllPaths(),
                props.getClientId(),
                props.getJwkSetUri(),
                props.getAllowedRoles()
        );
    }

    @PostConstruct
    public void postConstruct() {
        log.debug(">>> SecurityConfig ACTIVATED");
    }
}