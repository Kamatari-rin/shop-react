package org.example.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CommonServiceLogger {

    @Value("${logging.aspects.enabled:true}")
    private boolean loggingEnabled;

    @Value("${logging.aspects.level:INFO}")
    private String loggingLevel;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..))")
    public void controllerMethods() {}

    @Pointcut("within(@org.springframework.stereotype.Service *) && execution(public * *(..))")
    public void serviceMethods() {}

    @Pointcut("within(@org.springframework.stereotype.Repository *) && execution(public * *(..))")
    public void repositoryMethods() {}

    @Pointcut("controllerMethods() || serviceMethods() || repositoryMethods()")
    public void applicationMethods() {}

    @Before("applicationMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        if (!loggingEnabled) return;
        Logger logger = getLogger(joinPoint);
        log(logger, "Вход в метод: {}.{} с аргументами: {}", joinPoint);
    }

    @AfterReturning(pointcut = "applicationMethods()", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, Object result) {
        if (!loggingEnabled) return;
        Logger logger = getLogger(joinPoint);
        log(logger, "Метод: {}.{} выполнен успешно. Результат: {}", joinPoint, result);
    }

    @AfterThrowing(pointcut = "applicationMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        if (!loggingEnabled) return;
        Logger logger = getLogger(joinPoint);
        logger.error("[{}] Метод: {}.{} выбросил исключение: {}",
                getLayer(joinPoint),
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                exception.getMessage(),
                exception);
    }

    private Logger getLogger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getTarget().getClass());
    }

    private String getLayer(JoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        if (targetClass.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class)) {
            return "CONTROLLER";
        } else if (targetClass.isAnnotationPresent(org.springframework.stereotype.Service.class)) {
            return "SERVICE";
        } else if (targetClass.isAnnotationPresent(org.springframework.stereotype.Repository.class)) {
            return "REPOSITORY";
        }
        return "UNKNOWN";
    }

    private void log(Logger logger, String message, JoinPoint joinPoint, Object... args) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] fullArgs = new Object[]{getLayer(joinPoint), className, methodName};
        Object[] combinedArgs = new Object[fullArgs.length + args.length];
        System.arraycopy(fullArgs, 0, combinedArgs, 0, fullArgs.length);
        System.arraycopy(args, 0, combinedArgs, fullArgs.length, args.length);

        switch (loggingLevel.toUpperCase()) {
            case "DEBUG":
                logger.debug("[{}] " + message, combinedArgs);
                break;
            case "WARN":
                logger.warn("[{}] " + message, combinedArgs);
                break;
            case "ERROR":
                logger.error("[{}] " + message, combinedArgs);
                break;
            case "INFO":
            default:
                logger.info("[{}] " + message, combinedArgs);
                break;
        }
    }
}