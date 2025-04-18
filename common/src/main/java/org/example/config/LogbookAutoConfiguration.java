package org.example.config;

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.*;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.spring.webflux.LogbookWebFilter;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(Logbook.class)
public class LogbookAutoConfiguration {

    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .condition(request -> !request.getPath().startsWith("/actuator"))
                .sink(new DefaultSink(
                        new DefaultHttpLogFormatter(),
                        new HttpLogWriter() {
                            private final DefaultHttpLogWriter delegate = new DefaultHttpLogWriter();

                            @Override
                            public void write(Precorrelation precorrelation, String request) {
                                Span currentSpan = Span.current();
                                if (currentSpan != null) {
                                    MDC.put("traceId", currentSpan.getSpanContext().getTraceId());
                                    MDC.put("spanId", currentSpan.getSpanContext().getSpanId());
                                }
                                delegate.write(precorrelation, request);
                                MDC.remove("traceId");
                                MDC.remove("spanId");
                            }

                            @Override
                            public void write(Correlation correlation, String response) {
                                Span currentSpan = Span.current();
                                if (currentSpan != null) {
                                    MDC.put("traceId", currentSpan.getSpanContext().getTraceId());
                                    MDC.put("spanId", currentSpan.getSpanContext().getSpanId());
                                }
                                delegate.write(correlation, response);
                                MDC.remove("traceId");
                                MDC.remove("spanId");
                            }

                            @Override
                            public boolean isActive() {
                                return delegate.isActive();
                            }
                        }
                ))
                .build();
    }

    @Bean
    public LogbookWebFilter logbookWebFilter(Logbook logbook) {
        return new LogbookWebFilter(logbook);
    }
}