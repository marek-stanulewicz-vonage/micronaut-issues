package com.test2;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.propagation.PropagatedContext;
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

    @Get("/{serverFilter}/c-reactive2/{clientFilter}")
    public Mono<String> test(String serverFilter, String clientFilter) {
        return Mono.deferContextual(ctx ->
                Mono.just(serverFilter)
                        .publishOn(Schedulers.newSingle("ReactiveController"))
                        .map(sf -> {
                            var reqId = ctx.getOrDefault(REQ_ID_KEY, EMPTY_VALUE);

                            var url = "%s/%s/%s".formatted(clientUrl, clientFilter, reqId);
                            log.info("ReactiveController: URL {}", url);

                            return url;
                        })
                        .flatMap(this::callClientWithPropagation)
                        .map("{\"reqId\": \"%s\"}"::formatted));
    }

    private Mono<String> callClient(String url) {
        return Mono.from(client.retrieve(HttpRequest.GET(url), String.class));
    }

    private Mono<String> callClientWithPropagation(String url) {
        return Mono.deferContextual(ctx -> {
                    var pc = PropagatedContext.getOrEmpty()
                            .plus(new PropagatedContextView(ctx));
                    return Mono.from(pc.propagate(() -> client.retrieve(HttpRequest.GET(url), String.class)));
                });
    }


}
