package com.test;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Filter("/client/cf-reactive/*")
public class ReactiveClientFilter implements HttpClientFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveClientFilter.class);

    @Override
    public Mono<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        return Mono.just(request)
                .publishOn(Schedulers.newSingle("ReactiveClientFilter"))
                .map(this::addHeader)
                .flatMap(req -> Mono.from(chain.proceed(req)));
    }

    private <T> MutableHttpRequest<T> addHeader(MutableHttpRequest<T> request) {
        var reqIdInMdc = Util.findInMdc();
        log.info("ReactiveClientFilter: MDC {}", reqIdInMdc);

        var reqIdInPc = Util.findInPc();
        log.info("ReactiveClientFilter: PC {}", reqIdInPc);

        var reqId = reqIdInMdc.orElse(
                reqIdInPc.orElse(EMPTY_VALUE));

        return request.header(REQ_ID_KEY, reqId);
    }

}
