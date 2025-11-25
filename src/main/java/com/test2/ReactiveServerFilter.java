package com.test2;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Filter("/sf-reactive2/*/*")
public class ReactiveServerFilter implements HttpServerFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveServerFilter.class);

    public Mono<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Mono.just(request)
                .publishOn(Schedulers.newSingle("ReactiveServerFilter"))
                .map(req -> {
                    String reqIdFromHeader = req.getHeaders().get(REQ_ID_KEY);
                    String reqId = Optional.ofNullable(reqIdFromHeader).orElse(EMPTY_VALUE);
                    log.info("ReactiveServerFilter: {}", reqId);
                    return reqId;
                }).flatMap(reqId -> Mono.from(chain.proceed(request))
                        .contextWrite(ctx -> ctx.put(REQ_ID_KEY, reqId)));
    }

}
