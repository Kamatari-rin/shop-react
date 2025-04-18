package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import jakarta.annotation.PostConstruct;

public class MDCContextLifter {
    private static final Logger log = LoggerFactory.getLogger(MDCContextLifter.class);
    public static final String MDC_CONTEXT_KEY = "MDC";

    @PostConstruct
    public void init() {
        log.debug("MDCContextLifter: Initializing Reactor hook for MDC propagation");
        Hooks.onEachOperator(MDCContextLifter.class.getName(), Operators.lift((scannable, coreSubscriber) ->
                new CoreSubscriber<Object>() {
                    @Override
                    public void onSubscribe(org.reactivestreams.Subscription s) {
                        coreSubscriber.onSubscribe(s);
                    }

                    @Override
                    public void onNext(Object t) {
                        Context context = coreSubscriber.currentContext();
                        if (context.hasKey(MDC_CONTEXT_KEY)) {
                            MDC.setContextMap(context.get(MDC_CONTEXT_KEY));
                            log.trace("MDCContextLifter: Restored MDC [traceId={}, spanId={}]",
                                    MDC.get(TracingUtils.TRACE_ID_KEY), MDC.get(TracingUtils.SPAN_ID_KEY));
                        } else {
                            MDC.clear();
                            log.trace("MDCContextLifter: No MDC context found, cleared MDC");
                        }
                        coreSubscriber.onNext(t);
                    }

                    @Override
                    public void onError(Throwable t) {
                        coreSubscriber.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        coreSubscriber.onComplete();
                    }

                    @Override
                    public Context currentContext() {
                        return coreSubscriber.currentContext();
                    }
                }));
    }
}