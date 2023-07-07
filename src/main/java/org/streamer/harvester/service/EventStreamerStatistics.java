package org.streamer.harvester.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j(topic = "EventStreamerStatistics")
public class EventStreamerStatistics {
    // List of all users data (video streaming metadata)
    Map<String, List<Map<String, String>>> usersData = new ConcurrentHashMap<String, List<Map<String, String>>>();// List of all unique users
    SortedSet<String> users = new TreeSet<String>();

    Instant start = Instant.now();
    /**
     * Maps all users with their watched movies
     */
    Map<String, Map<String, Integer>> usersWatchedMovies = new ConcurrentHashMap<String, Map<String, Integer>>();
    /**
     * Map with a list of completed movies - using the User.ID as a key
     */
    Map<String, Set<String>> usersCompletedMovies = new ConcurrentHashMap<String, Set<String>>();
    long elapsedTime = 0;
    int totalShowsReleasedIn2020OrLater = 0;
    AtomicInteger countStartedEvents = new AtomicInteger();
    AtomicInteger countTotalEvents = new AtomicInteger();

    public EventStreamerStatistics() {
    }

    /**
     * Print final result in CSV format
     *
     */
    void printFinalResult() {
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(this.start, end);
        log.info("Time taken FINAL: " + timeElapsed.toMillis() + " milliseconds");

        this.elapsedTime = timeElapsed.toMillis();
        int count = 1;
        System.out.println("line, user_id, user_name, user_lastname, user_age, show_id, show_title, show_platform, show_cast, event_date, event_name");
        for (final String userId : users) {
            List<Map<String, String>> allResultsPerUser = usersData.get(userId);

            for (final Map<String, String> event : allResultsPerUser) {
                System.out.printf("%d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n", count++, event.get("user_id"),
                        event.get("user_name"), event.get("user_lastname"), event.get("user_age"),
                        event.get("show_id"), event.get("show_title"), event.get("show_platform"),
                        event.get("show_cast"), event.get("event_date"), event.get("event_name"));
            }
        }

        System.out.printf("Total Shows Released in 2020 or later: %d\n", this.totalShowsReleasedIn2020OrLater);
        System.out.printf("Total elapsed time: %d milliseconds\n", this.elapsedTime);

        count = 1;
        System.out.println("Total of Fully Watched Movies (started and ended in a sequence)");
        System.out.println("line, user_id, user_name, success_events");
        for (final String userId : users) {
            final List<Map<String, String>> allResultsPerUser = usersData.get(userId);

            if (allResultsPerUser != null && allResultsPerUser.size() > 0) {
                Map<String, String> event = allResultsPerUser.get(0);
                System.out.printf("%d, %s, %s, %d\n", count++, event.get("user_id"),
                        event.get("user_name"), usersCompletedMovies.getOrDefault(event.get("user_id"), new HashSet<String>()).size());
            }
        }

        System.out.printf("Percentage of Started Stream versus the total of Stream Events  %d/%d \n",
                this.countStartedEvents.get(), this.countTotalEvents.get());
    }
}