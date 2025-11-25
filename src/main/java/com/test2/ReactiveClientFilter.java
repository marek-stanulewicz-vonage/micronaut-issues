package com.test2;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;

import com.test.Util;
import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;

@Filter("/client/cf-reactive2/*")
public class ReactiveClientFilter implements HttpClientFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveClientFilter.class);

    @Override
    public Mono<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        var ctxPc = PropagatedContext.get().get(PropagatedContextView.class);

        return Mono.deferContextual(ctx -> Mono.just(request)
                .publishOn(Schedulers.newSingle("ReactiveClientFilter"))
                .map(req -> addHeader(ctx, req))
                .flatMap(req -> Mono.from(chain.proceed(req))))
                .contextWrite(ctxPc.ctx());
    }

    private <T> MutableHttpRequest<T> addHeader(ContextView ctx, MutableHttpRequest<T> request) {
        var reqId = ctx.getOrDefault(REQ_ID_KEY, EMPTY_VALUE);
        return request.header(REQ_ID_KEY, reqId);
    }

}
