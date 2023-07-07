package org.streamer.harvester.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    @JsonProperty("show_id")
    private String showId;

    private String cast;
    private String country;

    @JsonProperty("date_added")
    private String dateAdded;
    private String description;
    private String director;
    private String duration;

    @JsonProperty("listed_in")
    private String listedIn;
    private String rating;

    @JsonProperty("release_year")
    private String releaseYear;
    private String title;
    private String type;
    private String platform;

}
