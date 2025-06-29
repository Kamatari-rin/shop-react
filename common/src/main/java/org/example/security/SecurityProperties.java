package org.example.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "security-config")
public class SecurityProperties {
    private List<String> permitAllPaths  = new ArrayList<>();
    private String clientId;
    private String jwkSetUri;
    private List<String> allowedRoles = new ArrayList<>();
    private String clientSecret;

    @PostConstruct
    public void validate() {
        if (permitAllPaths.isEmpty()) {
            throw new IllegalStateException("permitAllPaths must not be empty");
        }
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalStateException("clientId must not be null or empty");
        }
        if (jwkSetUri == null || jwkSetUri.trim().isEmpty()) {
            throw new IllegalStateException("jwkSetUri must not be null or empty");
        }
        if (allowedRoles.isEmpty()) {
            throw new IllegalStateException("allowedRoles must not be empty");
        }
    }
}