package com.test;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;
import static io.micronaut.scheduling.TaskExecutors.BLOCKING;

import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.core.propagation.MutablePropagatedContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

//@ExecuteOn(BLOCKING)
//@ServerFilter("/sf-imperative/*/*")
public class Imperative2ServerFilter {

    private static final Logger log = LoggerFactory.getLogger(Imperative2ServerFilter.class);

    //    @RequestFilter
    public void parseHeader(HttpRequest<?> req, MutablePropagatedContext mutablePropagatedContext) {
        var reqId = Optional.ofNullable(req.getHeaders().get("X-Req-Id")).orElse(EMPTY_VALUE);
        try {
            log.info("Imperative2ServerFilter: {}", reqId);
            MDC.put(Config.REQ_ID_KEY, reqId);
            mutablePropagatedContext.add(new MdcPropagationContext());
        } finally {
            MDC.remove(REQ_ID_KEY);
        }
    }

}
