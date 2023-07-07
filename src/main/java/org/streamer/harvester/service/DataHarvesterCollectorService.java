package org.streamer.harvester.service;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.streamer.harvester.common.Constants;
import org.streamer.harvester.dto.DataModel;
import org.streamer.harvester.dto.VideoStreamingEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataHarvesterCollectorService {
    private static final int MAX_RETRY_ATTEMPTS = 60;
    private static final int DELAY_MILLIS = 400;
    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    static final String SYTFLIX_URL = "/sytflix";

    static final String SYTAZON_URL = "/sytazon";

    static final String SYSNEY_URL = "/sysney";

    public Flux<VideoStreamingEvent> getSytflix() {
        ParameterizedTypeReference<ServerSentEvent<DataModel>> type
                = new ParameterizedTypeReference<ServerSentEvent<DataModel>>() {};

        return webClient.get()
                .uri(SYTFLIX_URL)
                .accept(MediaType.TEXT_EVENT_STREAM).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(type)
                .flatMap( m -> {
                    VideoStreamingEvent v = new VideoStreamingEvent();
                    v.setId(m.id());
                    v.setEvent(m.event());
                    v.setData(m.data());
                    return Flux.just(v);
                })
                .doOnError(error -> log.error("getSytflix An error has occurred {}", error.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Something went wrong: " + e.getMessage())))
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)));
    }

    public Flux<VideoStreamingEvent> getSytazon() {
        ParameterizedTypeReference<ServerSentEvent<DataModel>> type
                = new ParameterizedTypeReference<ServerSentEvent<DataModel>>() {};

        return webClient.get()
                .uri(SYTAZON_URL)
                .accept(MediaType.TEXT_EVENT_STREAM).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(type)
                .flatMap( m -> {
                    VideoStreamingEvent v = new VideoStreamingEvent();
                    v.setId(m.id());
                    v.setEvent(m.event());
                    v.setData(m.data());
                    return Flux.just(v);
                })
//                .map(m -> {
//                    try {
//                        log.info("getSytazon Converting... "+m);
//                        return objectMapper.readValue( m, DataModel.class);
//                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
                .doOnError(error -> log.error("getSytazon - An error has occurred {}", error.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Something went wrong: " + e.getMessage())))
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)));
    }


    public Flux<VideoStreamingEvent> getSysney() {
        ParameterizedTypeReference<ServerSentEvent<DataModel>> type
                = new ParameterizedTypeReference<ServerSentEvent<DataModel>>() {};

        return webClient.get()
                .uri(SYSNEY_URL)
                .accept(MediaType.TEXT_EVENT_STREAM).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(type)
                .flatMap( m -> {
                    VideoStreamingEvent v = new VideoStreamingEvent();
                    v.setId(m.id());
                    v.setEvent(m.event());
                    v.setData(m.data());
                    return Flux.just(v);
                })
                .doOnError(error -> log.error("getSysney An error has occurred {}", error.getMessage()))
                .onErrorResume(
                        e -> Mono.error(new RuntimeException("getSysney Something went wrong: " + e.getMessage())))
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)));
    }



    public void getAllHeaders() {
        Flux<Tuple2<ClientResponse, String>> responseFlux = webClient.get()
                .uri(SYTAZON_URL) // Replace with your endpoint URL
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(response -> {
                    Flux<String> bodyFlux = response.bodyToFlux(String.class);
                    return Flux.zip(Mono.just(response), bodyFlux);
                });

        responseFlux.subscribe(tuple -> {
            ClientResponse clientResponse = tuple.getT1();
            String responseBody = tuple.getT2();

            // Accessing specific properties
            int statusCode = clientResponse.statusCode().value();
            HttpHeaders headers = clientResponse.headers().asHttpHeaders();
            String specificHeaderValue = headers.getFirst("specific-header");

            // Print the key/value pairs or perform any other desired operation
            System.out.println("Response Status Code: " + statusCode);
            System.out.println("Response Headers: " + headers);
            System.out.println("Specific Header Value: " + specificHeaderValue);
            System.out.println("Response Body: " + responseBody);
        });

    }

    public void consumeServerSentEvent() {
        ParameterizedTypeReference<ServerSentEvent<String>> type
                = new ParameterizedTypeReference<ServerSentEvent<String>>() {};

        Flux<ServerSentEvent<String>> eventStream = webClient.get()
                .uri(SYTAZON_URL)
                .retrieve()
                .bodyToFlux(type);

        eventStream.subscribe(
                content -> log.info("Time: {} - event: name[{}], id [{}], content[{}] ",
                        LocalTime.now(), content.event(), content.id(), content.data()),
                error -> log.error("Error receiving SSE: {}", error),
                () -> log.info("Completed!!!"));
    }

}
