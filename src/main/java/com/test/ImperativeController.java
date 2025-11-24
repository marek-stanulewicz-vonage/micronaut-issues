package com.test;

import static com.test.Config.EMPTY_VALUE;
import static io.micronaut.scheduling.TaskExecutors.BLOCKING;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExecuteOn(BLOCKING)
@Controller
public class ImperativeController {

    private static final Logger log = LoggerFactory.getLogger(ImperativeController.class);

    private final HttpClient client;
    private final String clientUrl;

    public ImperativeController(HttpClient client,
            @Value("${client.url}") String clientUrl
    ) {
        this.client = client;
        this.clientUrl = clientUrl;
    }

    @Get("/{serverFilter}/c-imperative/{clientFilter}")
    public String test(String serverFilter, String clientFilter) {

        var reqIdInMdc = Util.findInMdc();
        log.info("ImperativeController: MDC {}", reqIdInMdc);

        var reqIdInPc = Util.findInPc();
        log.info("ImperativeController: PC {}", reqIdInPc);

        var reqId = reqIdInMdc.orElse(
                reqIdInPc.orElse(EMPTY_VALUE));

        var url = "%s/%s/%s".formatted(clientUrl, clientFilter, reqId);
        log.info("ImperativeController: URL {}", url);

        var reqIdFromClient = client.toBlocking().retrieve(HttpRequest.GET(url), String.class);

        return "{\"reqId\": \"%s\"}".formatted(reqIdFromClient);
    }

}
