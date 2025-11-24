package com.test;

import static com.test.Config.REQ_ID_KEY;
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
        Optional.ofNullable(req.getHeaders().get(REQ_ID_KEY))
                .ifPresentOrElse(id -> {
                    try {
                        log.info("ImperativeServerFilter: {}", id);
                        MDC.put(REQ_ID_KEY, id);
                        try (PropagatedContext.Scope ignore = PropagatedContext.getOrEmpty().plus(new MdcPropagationContext())
                                .propagate()) {
                            continuation.proceed();
                        }
                    } finally {
                        MDC.remove(REQ_ID_KEY);
                    }
                }, continuation::proceed);
    }

}
