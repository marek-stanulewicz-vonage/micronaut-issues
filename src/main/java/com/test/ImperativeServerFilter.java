package com.test;

import static io.micronaut.scheduling.TaskExecutors.BLOCKING;

import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.filter.FilterContinuation;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@ExecuteOn(BLOCKING)
@ServerFilter("/sf-imperative/*/*")
public class ImperativeServerFilter {

    private static final Logger log = LoggerFactory.getLogger(ImperativeServerFilter.class);

    @RequestFilter
    public void parseHeader(HttpRequest<?> req, FilterContinuation<MutableHttpResponse<?>> continuation) {
        Optional.ofNullable(req.getHeaders().get("X-Req-Id"))
                .ifPresentOrElse(id -> {
                    try {
                        log.info("ImperativeServerFilter: {}", id);
                        MDC.put("reqId", id);
                        try (PropagatedContext.Scope ignore = PropagatedContext.getOrEmpty().plus(new MdcPropagationContext())
                                .propagate()) {
                            continuation.proceed();
                        }
                    } finally {
                        MDC.remove("reqId");
                    }
                }, continuation::proceed);
    }

}
