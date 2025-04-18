package org.example.util;

import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

public final class TracingUtils {
    private static final Logger log = LoggerFactory.getLogger(TracingUtils.class);
    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";

    public static Mono<Void> putTracingContext() {
        return Mono.defer(() -> {
            Span currentSpan = Span.current();
            if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
                String traceId = currentSpan.getSpanContext().getTraceId();
                String spanId = currentSpan.getSpanContext().getSpanId();
                MDC.put(TRACE_ID_KEY, traceId);
                MDC.put(SPAN_ID_KEY, spanId);
                log.debug("TracingUtils: Set MDC [traceId={}, spanId={}]", traceId, spanId);
            } else {
                log.warn("TracingUtils: No valid span found, MDC not set");
            }
            return Mono.empty();
        });
    }

    public static Mono<Void> clearTracingContext() {
        return Mono.defer(() -> {
            log.debug("TracingUtils: Clearing MDC [traceId={}, spanId={}]", MDC.get(TRACE_ID_KEY), MDC.get(SPAN_ID_KEY));
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(SPAN_ID_KEY);
            return Mono.empty();
        });
    }
}