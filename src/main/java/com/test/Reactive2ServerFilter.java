package com.test;

import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.core.async.propagation.ReactorPropagation;
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

//@Filter("/sf-reactive/*/*")
public class Reactive2ServerFilter implements HttpServerFilter {

    private static final Logger log = LoggerFactory.getLogger(Reactive2ServerFilter.class);

    public Mono<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Mono.just(request)
                .publishOn(Schedulers.newSingle("ReactiveServerFilter"))
                .map(req -> {
                    String reqIdFromHeader = req.getHeaders().get(Config.REQ_ID_KEY);
                    String reqId = Optional.ofNullable(reqIdFromHeader).orElse(Config.EMPTY_VALUE);
                    log.info("Reactive2ServerFilter: {}", reqId);

                    var mdc = PropagatedContext.getOrEmpty()
                            .find(MdcPropagationContext.class)
                            .orElse(new MdcPropagationContext(new HashMap<>()));
                    mdc.state().put(Config.REQ_ID_KEY, reqId);
                    return PropagatedContext.getOrEmpty().plus(mdc);
                })
                .flatMap(pc -> Mono.from(chain.proceed(request))
                        .contextWrite(ctx -> ReactorPropagation.addPropagatedContext(ctx, pc)));
    }

}
