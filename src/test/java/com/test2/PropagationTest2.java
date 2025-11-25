package com.test2;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.IMPERATIVE;
import static com.test.Config.REACTIVE;
import static com.test.Config.REQ_ID_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropagationTest2 implements TestPropertyProvider {

    static final String REQ_ID_INPUT = "123-req-id-456";
    static final String REQ_ID_OUTPUT = "456-req-id-789";

    static final StringBuilder wiremockLog = new StringBuilder();

    @SuppressWarnings("resource")
    static final GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.13.2")
            .withExposedPorts(8080)
            .withEnv("WIREMOCK_OPTIONS", "--verbose")
            .withLogConsumer(outputFrame -> wiremockLog.append(outputFrame.getUtf8String()))
            .withStartupTimeout(Duration.ofSeconds(30));

    static Logger log = LoggerFactory.getLogger(PropagationTest2.class);

    {
        wiremock.start();
//        WireMock.configureFor(wiremock.getMappedPort(8080));

//        stubForClientFilter("cf-imperative");
        stubForClientFilter("cf-reactive2");
    }

    static void stubForClientFilter(String clientFilter) {
//        if(false) {
//            stubFor(WireMock.get(urlEqualTo("/client/%s/%s".formatted(clientFilter, EMPTY_VALUE)))
//                    .willReturn(ok().withBody("%s,%s".formatted(EMPTY_VALUE, EMPTY_VALUE))));
//            stub(clientFilter, EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE);
//            stub(clientFilter, REQ_ID_INPUT, EMPTY_VALUE, REQ_ID_OUTPUT, EMPTY_VALUE);
//            stub(clientFilter, EMPTY_VALUE, REQ_ID_INPUT, EMPTY_VALUE, REQ_ID_OUTPUT);
//            stub(clientFilter, REQ_ID_INPUT, REQ_ID_INPUT, REQ_ID_OUTPUT, REQ_ID_OUTPUT);
//        }

        if(true) {
            stubViaReq(() -> stubReq(clientFilter, EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE));
            stubViaReq(() -> stubReqWithHeader(clientFilter, EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE));
            stubViaReq(() -> stubReqWithHeader(clientFilter, REQ_ID_INPUT, EMPTY_VALUE, REQ_ID_OUTPUT, EMPTY_VALUE));
            stubViaReq(() -> stubReqWithHeader(clientFilter, EMPTY_VALUE, REQ_ID_INPUT, EMPTY_VALUE, REQ_ID_OUTPUT));
            stubViaReq(() -> stubReqWithHeader(clientFilter, REQ_ID_INPUT, REQ_ID_INPUT, REQ_ID_OUTPUT, REQ_ID_OUTPUT));
        }
    }

//    static void stub(String clientFilter, String fromController, String fromClientFilter, String out1, String out2) {
//        stubFor(WireMock.get(urlEqualTo("/client/%s/%s".formatted(clientFilter, fromController))).withHeader(REQ_ID_KEY, equalTo(fromClientFilter))
//                .willReturn(ok().withBody("%s,%s".formatted(out1, out2))));
//    }

    @AfterAll
    static void afterAll() {
        wiremock.stop();
        log.info("WireMock log:\n{}", wiremockLog);
    }

    @Inject
    EmbeddedServer server;

    @ParameterizedTest
    @MethodSource("types")
    void shouldPropagate(String sf, String c, String cf) throws Exception {

        var path = "/sf-%s/c-%s/cf-%s".formatted(sf, c, cf);

        log.info("Calling path: {}", path);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(server.getURL() + path))
                .GET()
                .header(REQ_ID_KEY, REQ_ID_INPUT)
                .build();

        var expectedBody = "{\"reqId\": \"%s,%s\"}".formatted(REQ_ID_OUTPUT, REQ_ID_OUTPUT);

        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(req, HttpResponse.BodyHandlers.ofString());
            assertEquals(expectedBody, response.body());
        }
    }

    public static Stream<Arguments> types() {
        return Stream.of(arguments("reactive2", "reactive2", "reactive2"));

//        var types = List.of(IMPERATIVE, REACTIVE);
//
//        List<Arguments> arguments = new ArrayList<>();
//
//        for (var sf : types) {
//            for (var c : types) {
//                for (var cf : types) {
//                    arguments.add(arguments(sf, c, cf));
//                }
//            }
//        }
//
//        return arguments.stream();
    }


    @Override
    public @NonNull Map<String, String> getProperties() {
//        return Map.of();
        return Map.of("client.url", "http://localhost:%s/client".formatted(wiremock.getMappedPort(8080)));
    }

    static void stubViaReq(Supplier<String> bodySrc) {
        try {
            var body = bodySrc.get();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:%s/__admin/mappings".formatted(wiremock.getMappedPort(8080))))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            try (var client = HttpClient.newHttpClient()) {
                var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                log.info("Request body: {}\nresponse: {}", body, resp.body());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String stubReq(String clientFilter, String fromController, String out1, String out2) {
        return """
                {
                  "request" : {
                    "url" : "/client/%s/%s",
                    "method" : "GET"
                  },
                  "response" : {
                    "status" : 200,
                    "body" : "%s,%s"
                  }
                }
                """.formatted(clientFilter, fromController, out1, out2);
    }


    static String stubReqWithHeader(String clientFilter, String fromController, String fromClientFilter, String out1, String out2) {
        return """
                {
                  "request" : {
                    "url" : "/client/%s/%s",
                    "method" : "GET",
                    "headers" : {
                      "X-Req-Id" : {
                        "equalTo" : "%s"
                      }
                    }
                  },
                  "response" : {
                    "status" : 200,
                    "body" : "%s,%s"
                  }
                }
                """.formatted(clientFilter, fromController, fromClientFilter, out1, out2);
    }

}
