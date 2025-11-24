package com.test;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

@Controller
public class ReactiveController {

    private static final Logger log = LoggerFactory.getLogger(ReactiveController.class);

    private final HttpClient client;
    private final String clientUrl;

    public ReactiveController(HttpClient client,
            @Value("${client.url}") String clientUrl
    ) {
        this.client = client;
        this.clientUrl = clientUrl;
    }

    @Get("/{serverFilter}/c-reactive/{clientFilter}")
    public Mono<String> test(String serverFilter, String clientFilter) {
        return Mono.fromCallable(() -> {
                    var reqIdInMdc = Util.findInMdc();
                    log.info("ReactiveController: MDC {}", reqIdInMdc);

                    var reqIdInPc = Util.findInPc();
                    log.info("ReactiveController: PC {}", reqIdInPc);

                    return reqIdInMdc.orElse(
                            reqIdInPc.orElse(EMPTY_VALUE));
                })
                .flatMap(reqId -> Mono.from(client.retrieve(HttpRequest.GET("%s/%s".formatted(clientUrl, reqId)), String.class)))
                .map("{\"reqId\": \"%s\"}"::formatted);
    }

}
