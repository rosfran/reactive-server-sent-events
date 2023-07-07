package org.streamer.harvester.service;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.streamer.harvester.common.Constants;
import org.streamer.harvester.dto.DataModel;
import org.streamer.harvester.dto.VideoStreamingEvent;
import org.streamer.harvester.exception.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * Service that calls all 3 endpoints (systazon, sytflix and sysney) and process its data
 */
@Service
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataHarvesterService {

    @Autowired
    private DataHarvesterCollectorService dataHarvesterCollectorService;

    /**
     * Action to process all the Server Sent Events from VideoStreamer Backend services
     */
    public void processAllIncomingData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT_ORIGINAL_SMALL).withZone(ZoneId.systemDefault());
        final EventStreamerStatistics stats = new EventStreamerStatistics();

        // merge the 3 Flux streaming pipelines into one
        dataHarvesterCollectorService.getSytflix()
                .mergeWith(dataHarvesterCollectorService.getSytazon())
                .mergeWith(dataHarvesterCollectorService.getSysney())
                .take(Duration.ofMillis(20000))     // it must last 20 seconds
                .takeUntil(m -> m != null && m.getData().getUser() != null &&
                        m.getData().getUser().getFirstName().equals("Sytac"))  // and the pipeline must stop on first Sytac user
                .doFirst(() -> {
                    Instant t =  Instant.now();
                    t.adjustInto( Instant.now());
                    stats.setStart(t);
                    System.out.println("Time taken START: " + formatter.format(t));
                })
                .doOnEach(m -> {
                    if (m.get() != null) {
                        log.info("New data: " + m.toString());
                        // process each incoming event
                        try {
                            processIncomingEvent(m.get(), stats);
                        } catch (SSEParserException e) {
                            log.error("Error: "+e);
                            throw new RuntimeException(e);
                        }
                    }
                })
                .doOnComplete(() -> {
                    // print final result in CSV format
                    stats.printFinalResult();
                })
                .doOnCancel(() -> {
                    Instant end = Instant.now();
                    Duration timeElapsed = Duration.between(stats.getStart(), end);
                    stats.setElapsedTime(timeElapsed.toMillis());
                    log.info("Time taken Cancelled FINAL: " + stats.getElapsedTime() + " milliseconds");
                })
                .subscribe(m -> {
                    if (m != null && m.getData().getUser() != null)
                        log.info("INFO: " + m.getData().getUser().getFirstName());
                    else
                        log.info("INFO: " + m);
                });

    }

    /**
     * Print final result in CSV format
     *
     * @param start The initial timestamp captured on the start of the processment - This is used to compute the duration of processing.
     */
    private void printFinalResult(final Instant start, final EventStreamerStatistics stats) {

        stats.printFinalResult();
    }

    /**
     * Process a message event, doing all calculations and printing the logs to the standard output
     *
     * @param videoStreamingEvent The VideoStreamingEvent payload, with info about the Event and the User
     */
    static public void processIncomingEvent(final VideoStreamingEvent videoStreamingEvent, final EventStreamerStatistics stats) throws SSEParserException {

        if (videoStreamingEvent == null)
            throw new SSEParserException("VideoStreaming cannot be null");

        if (Strings.isEmpty(videoStreamingEvent.getId()) )
            throw new SSEParserException("VideoStreaming ID field cannot be null");

        if (Strings.isEmpty(videoStreamingEvent.getEvent()) )
            throw new SSEParserException("VideoStreaming EVENT field cannot be null");

        if (videoStreamingEvent.getData() == null || videoStreamingEvent.getData().getUser() == null ||
                Strings.isEmpty(videoStreamingEvent.getData().getUser().getId()) )
            throw new SSEParserException("VideoStreaming DATA field cannot be null");

        Map<String, String> userValues = videoStreamingEvent.convertToMap();

        DataModel eventData = videoStreamingEvent.getData();

        if (!stats.usersData.containsKey(eventData.getUser().getId())) {
            stats.usersData.put(eventData.getUser().getId(), new ArrayList<>());
            stats.users.add(eventData.getUser().getId());
        }

        stats.getUsersData().get(eventData.getUser().getId()).add(userValues);

        if (eventData.getEventDateDt().getYear() >= 2020) {
            stats.totalShowsReleasedIn2020OrLater++;
        }

        // count total of events
        log.info("total of events: " + stats.countTotalEvents.incrementAndGet());
        // receives an event for a given user that started to watch a movie
        if (videoStreamingEvent.getEvent().equals("stream-started")) {
            if (!stats.usersWatchedMovies.containsKey(eventData.getUser().getId())) {
                stats.usersWatchedMovies.put(eventData.getUser().getId(), new HashMap<>());
            }

            // mapping of showId and amount of stream-started
            Map<String, Integer> userMovies = stats.usersWatchedMovies.get(eventData.getUser().getId());

            if (eventData.getShow().getShowId() != null) {
                userMovies.put(eventData.getShow().getShowId(),
                        userMovies.getOrDefault(eventData.getShow().getShowId(), 0) + 1);
            }

            // count only the streaming-started events
            log.info("stream-started count " + stats.countStartedEvents.incrementAndGet());

            stats.usersWatchedMovies.put(eventData.getUser().getId(), userMovies);
            // receives an event for a given user that ended to watch a movie
        } else if (videoStreamingEvent.getEvent().equals("stream-finished")) {

            // if there is no data for a specific user, create a new map
            if (!stats.usersWatchedMovies.containsKey(eventData.getUser().getId())) {
                stats.usersWatchedMovies.put(eventData.getUser().getId(), new HashMap<>());
            }

            // get the map with the Amount of uncompleted streaming sessions by movie
            Map<String, Integer> userMovies = stats.usersWatchedMovies.get(eventData.getUser().getId());

            // get the count of watched sessions for this movie
            if (eventData.getShow().getShowId() != null ) {
                    userMovies.put(eventData.getShow().getShowId(),
                            userMovies.getOrDefault(eventData.getShow().getShowId(), 0) - 1);
            }

            // if the completed movies count is Zero, it means that it is completed (fully) eatched movie
            if (userMovies.get(eventData.getShow().getShowId()) != null &&
                    userMovies.get(eventData.getShow().getShowId()) == 0) {
                userMovies.remove(eventData.getShow().getShowId());
                stats.usersWatchedMovies.put(eventData.getUser().getId(), userMovies);
                if (!stats.usersCompletedMovies.containsKey(eventData.getUser().getId()))
                    stats.usersCompletedMovies.put(eventData.getUser().getId(), new HashSet<>());

                Set<String> movies = stats.usersCompletedMovies.get(eventData.getUser().getId());

                movies.add(eventData.getShow().getShowId());
                stats.usersCompletedMovies.put(eventData.getUser().getId(), movies);
            }

        }
    }


}


