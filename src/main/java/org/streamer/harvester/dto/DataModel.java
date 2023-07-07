package org.streamer.harvester.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.streamer.harvester.common.Constants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.streamer.harvester.util.Utils.zoneIdsMap;


@Data
@Slf4j(topic = "DataModel")
@NoArgsConstructor
@AllArgsConstructor
public class DataModel {

    private Show show;

    @JsonProperty("event_date")
    private String eventDate;

    private ZonedDateTime eventDateDt;

    private User user;

    public String toString() {
        List<String> res = new ArrayList<>();

        if ( this.user != null ) {
            if (Strings.isNotBlank(user.getId()) ) {
                res.add("UserID: "+ user.getId());
            }

            if (Strings.isNotBlank(user.getFirstName()) ) {
                res.add("UserName: "+ user.getFirstName());
            }

            if (Strings.isNotBlank(user.getLastName()) ) {
                res.add("UserSurname: "+ user.getLastName());
            }

            if (user.getDateOfBirth() != null ) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_OF_BIRTH_FORMAT)
                        .withZone(ZoneId.systemDefault());

                LocalDate dateOfBirth = LocalDate.parse(user.getDateOfBirth(), formatter);

                res.add("UserAge: "+  Period.between(dateOfBirth, LocalDate.now()).getYears());
            }
        }

        if (this.show != null) {

            if ( Strings.isNotBlank(this.show.getShowId()))
                res.add("Show.ID: "+this.show.getShowId());

            if ( Strings.isNotBlank(this.show.getTitle()))
                res.add("Show.Title: "+this.show.getTitle());

            if ( Strings.isNotBlank(this.show.getCountry()))
                res.add("Show.Country: "+this.show.getCountry());
            if ( Strings.isNotBlank(this.show.getPlatform()))
                res.add("Show.Platform: "+this.show.getPlatform());
            if (Strings.isNotBlank(this.show.getCast())) {
                String cast = this.show.getCast();
                if ( this.show.getCast().indexOf(",") != -1)
                    cast = this.show.getCast().substring(0, this.show.getCast().indexOf(","));

                res.add("Cast: "+cast);
            }
        }

        if ( Strings.isNotBlank(this.eventDate ) ) {
            ZoneId zoneId = zoneIdsMap.
                    getOrDefault(Strings.isNotBlank(getShow().getCountry()) ?
                            getShow().getCountry() : "US", ZoneId.systemDefault()); // Zone information

            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT_ORIGINAL)
                    .withZone(zoneId);

            LocalDateTime dateTime = LocalDateTime.parse(getEventDate(), formatter1);
            ZonedDateTime origDate = dateTime.atZone(zoneId); // add zone information

            log.info("original date: " + formatter1.format(origDate));
            ZonedDateTime dateInAmsterdam = origDate.withZoneSameInstant(ZoneId.of("CET"));

            this.eventDateDt = dateInAmsterdam;
            log.info("date on Amsterdam: " + formatter1.format(dateInAmsterdam));
            res.add("EventDate: "+formatter1.format(dateInAmsterdam));
        }

        return res.stream().collect(Collectors.joining(","));
    }


    public Map<String,String> convertToMap() {
        Map<String,String> map = new HashMap<>();

        if ( this.user != null ) {
            if (Strings.isNotBlank(user.getId()) ) {
                map.put("user_id",user.getId());
            }

            if (Strings.isNotBlank(user.getFirstName()) ) {
                map.put("user_name", user.getFirstName());
            }

            if (Strings.isNotBlank(user.getLastName()) ) {
                map.put("user_lastname", user.getLastName());
            }

            if (user.getDateOfBirth() != null ) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_OF_BIRTH_FORMAT)
                        .withZone(ZoneId.systemDefault());

                LocalDate dateOfBirth = LocalDate.parse(user.getDateOfBirth(), formatter);

                map.put("user_age", String.valueOf(Period.between(dateOfBirth, LocalDate.now()).getYears()));
            }
        }

        if (this.show != null) {

            if ( Strings.isNotBlank(this.show.getShowId()))
                map.put("show_id",this.show.getShowId());

            if ( Strings.isNotBlank(this.show.getTitle()))
                map.put("show_title",this.show.getTitle());

            if ( Strings.isNotBlank(this.show.getCountry()))
                map.put("show_country", this.show.getCountry());

            if ( Strings.isNotBlank(this.show.getPlatform()))
                map.put("show_platform", this.show.getPlatform());

            if (Strings.isNotBlank(this.show.getCast())) {
                String cast = this.show.getCast();
                if ( this.show.getCast().indexOf(",") != -1)
                    cast = this.show.getCast().substring(0, this.show.getCast().indexOf(","));

                map.put("show_cast", cast);
            }
        }

        if ( Strings.isNotBlank(this.eventDate ) ) {
            ZoneId zoneId = zoneIdsMap.
                    getOrDefault(Strings.isNotBlank(getShow().getCountry()) ?
                            getShow().getCountry() : "US", ZoneId.systemDefault()); // Zone information

            DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT_ORIGINAL)
                    .withZone(zoneId);

            LocalDateTime dateTime = LocalDateTime.parse(getEventDate(), dataFormatter);
            ZonedDateTime origDate = dateTime.atZone(zoneId); // add zone information

            log.info("original date: " + dataFormatter.format(origDate));
            ZonedDateTime dateInAmsterdam = origDate.withZoneSameInstant(ZoneId.of("CET"));

            this.eventDateDt = dateInAmsterdam;

            log.info("date on Amsterdam: " + dataFormatter.format(dateInAmsterdam));
            map.put("event_date", dataFormatter.format(dateInAmsterdam));
        }

        return map;
    }



}
