package com.test;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;
import static io.micronaut.scheduling.TaskExecutors.BLOCKING;

import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.ClientFilter;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.filter.FilterContinuation;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExecuteOn(BLOCKING)
@ClientFilter("/client/cf-imperative/*")
public class ImperativeClientFilter {

    private static final Logger log = LoggerFactory.getLogger(ImperativeClientFilter.class);

    @RequestFilter
    public void addHeader(MutableHttpRequest<?> req, FilterContinuation<MutableHttpResponse<?>> continuation) {
        var reqIdInMdc = Util.findInMdc();
        log.info("ImperativeClientFilter: MDC {}", reqIdInMdc);

        var reqIdInPc = Util.findInPc();
        log.info("ImperativeClientFilter: PC {}", reqIdInPc);

        var reqId = reqIdInMdc.orElse(
                reqIdInPc.orElse(EMPTY_VALUE));

        req.getHeaders().add(REQ_ID_KEY, reqId);
        continuation.proceed();
    }

}
