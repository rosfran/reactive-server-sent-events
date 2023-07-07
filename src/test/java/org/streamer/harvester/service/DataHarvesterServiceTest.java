package org.streamer.harvester.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import org.streamer.harvester.dto.DataModel;
import org.streamer.harvester.dto.VideoStreamingEvent;
import org.streamer.harvester.exception.SSEParserException;
import org.streamer.harvester.util.TestsUtil;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.streamer.harvester.service.DataHarvesterService.processIncomingEvent;
import static org.streamer.harvester.util.TestsUtil.readFileToListOfEvents;

/**
 * Unit Test for DataHarvesterService methods
 */
@ExtendWith(MockitoExtension.class)
@Slf4j(topic="DataHarvesterServiceTest")
class DataHarvesterServiceTest {

    @InjectMocks
    private DataHarvesterService dataHarvesterService;

    @InjectMocks
    private DataHarvesterCollectorService dataHarvesterCollectorService;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private ObjectMapper objectMapper;

    /**
     * Method under test: {@link DataHarvesterCollectorService#getSysney()} ()}
     */
    @Test
    @DisplayName("Trying to collect data when VideoStreamer service is down")
    void geSysney_should_throwException_when_IsNotUp() {

        assertThrows(Exception.class, () -> {
            dataHarvesterCollectorService.getSysney();

        });

    }

    @Test
    @DisplayName("Consume events 9 events total count")
    void consume_VideoEvents_HappyPath_9_Events_Total() {

        final EventStreamerStatistics stats = new EventStreamerStatistics();

        List<String> events = readFileToListOfEvents(getClass().getClassLoader().
                getResource("data/events_sample.txt").getFile());

        Flux.fromIterable(events)
                .map( m -> {
                    try {
                        return TestsUtil.parseVideoStreamingEvent(m, objectMapper);
                    } catch (SSEParserException e) {
                        throw new RuntimeException(e);
                    }

                })
                .doOnEach(m -> {
                    if (m.get() != null) {
                        log.info("New data: " + m.toString());
                        try {
                            processIncomingEvent(m.get(), stats);
                        } catch (SSEParserException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).subscribe();


        log.info("Total Events: "+String.valueOf(stats.getCountStartedEvents().get()));

        assertEquals( 9,  stats.getCountTotalEvents().get() );

        assertEquals( 3,  stats.getCountStartedEvents().get() );

    }


    @Test
    @DisplayName("Consume events 2 totally watched movie events")
    void consume_VideoEvents_2_Fully_Watched_Movies_9_Events_Total() {

        final EventStreamerStatistics stats = new EventStreamerStatistics();

        List<String> events = readFileToListOfEvents(getClass().getClassLoader().
                getResource("data/events_sample_2_fully_watched.txt").getFile());

        Flux.fromIterable(events)
                .map( m -> {
                    try {
                        return TestsUtil.parseVideoStreamingEvent(m, objectMapper);
                    } catch (SSEParserException e) {
                        throw new RuntimeException(e);
                    }

                })
                .doOnEach(m -> {
                    if (m.get() != null) {
                        log.info("New data: " + m.toString());
                        try {
                            processIncomingEvent(m.get(), stats);
                        } catch (SSEParserException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).subscribe();


        log.info("Total Events: "+String.valueOf(stats.getCountStartedEvents().get()));

        assertEquals( 2,  stats.getUsersCompletedMovies().get("12").size() );

        assertEquals( 12,  stats.getCountTotalEvents().get() );

        assertEquals( 4,  stats.getCountStartedEvents().get() );

    }

    @Test
    @DisplayName("Try to read event with missing ID field")
    void consume_VideoEvents_Error_SSE_Events_Total() {

        final EventStreamerStatistics stats = new EventStreamerStatistics();

        List<String> events = readFileToListOfEvents(getClass().getClassLoader().
                getResource("data/events_sample_error_sse.txt").getFile());

        Flux.fromIterable(events)
                .map( m -> {
                    AtomicReference<VideoStreamingEvent> v = new AtomicReference<>();
                    assertThrows(SSEParserException.class, () -> v.set(TestsUtil.parseVideoStreamingEvent(m, objectMapper)));

                    return v.get();

                })
                .doOnEach(m -> {
                    if (m.get() != null) {
                        log.info("New data: " + m.toString());

                        assertThrows(SSEParserException.class, () -> processIncomingEvent(m.get(), stats) );
                    }
                }).subscribe();

    }

    @Test
    @DisplayName("Try to read event with missing DATA field")
    void consume_VideoEvents_Error_SSE_Events_Missing_Data() {

        final EventStreamerStatistics stats = new EventStreamerStatistics();

        List<String> events = readFileToListOfEvents(getClass().getClassLoader().
                getResource("data/events_sample_error_sse_no_data.txt").getFile());

        Flux.fromIterable(events)
                .map( m -> {
                    AtomicReference<VideoStreamingEvent> v = new AtomicReference<>();
                    assertThrows(RuntimeException.class, () -> v.set(TestsUtil.parseVideoStreamingEvent(m, objectMapper)));

                    return v.get();

                })
                .doOnEach(m -> {
                    if (m.get() != null) {
                        log.info("New data: " + m.toString());

                        assertThrows(SSEParserException.class, () -> processIncomingEvent(m.get(), stats) );
                    }
                }).subscribe();


    }

}
