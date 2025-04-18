package org.example.config;

import org.example.util.MDCContextLifter;
import org.example.util.TracingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class TracingFilter implements WebFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(TracingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("TracingFilter: Processing request {}", exchange.getRequest().getPath());
        return TracingUtils.putTracingContext()
                .then(Mono.defer(() -> {
                    String traceId = MDC.get(TracingUtils.TRACE_ID_KEY);
                    String spanId = MDC.get(TracingUtils.SPAN_ID_KEY);
                    log.debug("TracingFilter: MDC set for request {} [traceId={}, spanId={}]", exchange.getRequest().getPath(), traceId, spanId);
                    return chain.filter(exchange)
                            .contextWrite(context -> {
                                log.trace("TracingFilter: Writing MDC to Reactor context [traceId={}, spanId={}]", traceId, spanId);
                                return context.put(MDCContextLifter.MDC_CONTEXT_KEY, MDC.getCopyOfContextMap());
                            });
                }))
                .doFinally(signal -> {
                    log.debug("TracingFilter: Clearing MDC for request {}", exchange.getRequest().getPath());
                    TracingUtils.clearTracingContext();
                });
    }

    @Override
    public int getOrder() {
        return 1;
    }
}