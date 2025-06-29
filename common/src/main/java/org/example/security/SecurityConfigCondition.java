package org.example.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SecurityConfigCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();

        boolean isEnabled = Boolean.parseBoolean(env.getProperty("security-config.enabled", "false"));

        SecurityProperties props = new SecurityProperties();
        Binder binder = Binder.get(context.getEnvironment());
        binder.bind("security-config", Bindable.ofInstance(props));

        boolean hasAllRequiredProps =
                props.getPermitAllPaths() != null &&
                        !props.getPermitAllPaths().isEmpty() &&
                        props.getJwkSetUri() != null &&
                        props.getAllowedRoles() != null &&
                        !props.getAllowedRoles().isEmpty();

        if (!isEnabled) {
            logger.info("SecurityConfig disabled via 'security-config.enabled=false'");
        } else if (!hasAllRequiredProps) {
            logger.warn("SecurityConfig not activated: missing or empty required security-config properties");
            logger.warn("Checked properties: permit-all-paths={}, jwk-set-uri={}, allowed-roles={}",
                    props.getPermitAllPaths(),
                    props.getJwkSetUri(),
                    props.getAllowedRoles());
        } else {
            logger.info("SecurityConfig activated with all required properties");
        }

        return isEnabled && hasAllRequiredProps;
    }
}