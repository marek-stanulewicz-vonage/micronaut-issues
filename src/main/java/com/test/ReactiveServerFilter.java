package com.test;

import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.core.async.propagation.ReactivePropagation;
import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import java.util.HashMap;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Filter("/sf-reactive/*/*")
public class ReactiveServerFilter implements HttpServerFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveServerFilter.class);

    public Mono<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Mono.just(request)
                .publishOn(Schedulers.newSingle("ReactiveServerFilter"))
                .flatMap(req -> {
                    String reqIdFromHeader = req.getHeaders().get(Config.REQ_ID_KEY);
                    String reqId = Optional.ofNullable(reqIdFromHeader).orElse(Config.EMPTY_VALUE);
                    log.info("ReactiveServerFilter: {}", reqId);

                    var mdc = PropagatedContext.getOrEmpty()
                            .find(MdcPropagationContext.class)
                            .orElse(new MdcPropagationContext(new HashMap<>()));
                    mdc.state().put(Config.REQ_ID_KEY, reqId);
                    var pc = PropagatedContext.getOrEmpty().plus(mdc);

                    return Mono.from(ReactivePropagation.propagate(pc, chain.proceed(req)));
                });
    }

}
