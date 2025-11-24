package com.test;

import static com.test.Config.EMPTY_VALUE;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
        return Mono.just(serverFilter)
                .publishOn(Schedulers.newSingle("ReactiveController"))
                .map(sf -> {
                    var reqIdInMdc = Util.findInMdc();
                    log.info("ReactiveController: MDC {}", reqIdInMdc);

                    var reqIdInPc = Util.findInPc();
                    log.info("ReactiveController: PC {}", reqIdInPc);

                    var reqId = reqIdInMdc.orElse(
                            reqIdInPc.orElse(EMPTY_VALUE));

                    var url = "%s/%s/%s".formatted(clientUrl, clientFilter, reqId);
                    log.info("ReactiveController: URL {}", url);

                    return url;
                })
                .flatMap(url -> Mono.from(client.retrieve(HttpRequest.GET(url), String.class)))
                .map("{\"reqId\": \"%s\"}"::formatted);
    }

}
