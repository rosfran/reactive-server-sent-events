package org.streamer.harvester.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    private String gender;

    @JsonProperty("ip_address")
    private String ipAddress;
    private String country;

    @JsonProperty("last_name")
    private String lastName;

}
