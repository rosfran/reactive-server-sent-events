package org.streamer.harvester.dto;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.streamer.harvester.service.DataHarvesterCollectorService;
import org.streamer.harvester.service.DataHarvesterService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test for DataHarvesterService methods
 */
@ExtendWith(MockitoExtension.class)
class EventsParserTest {

    @InjectMocks
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Parsing a correctly structured DATA field from a SSE Event")
    public void parse_JSON_DataModel_Success() {

        String jsonCorrectDataModel = "{\"show\":{\"show_id\":\"s5\",\"cast\":\"Qurbra Lmgeisand, Weff Rrudges, Ruuren Yecall, Biorge " +
                "Tegal, Simi Wugers, Kuerce Trosnan, Crenda Laccaro, Iastin Jundleton, Alle Pucpherson, Ili Parsh, Wislie Wqojanson, " +
                "Zaina Olg, Zucy Overy Trooke, Imber Xmith\",\"country\":null,\"date_added\":\"August 1, 2021\",\"description\":\"Fuunted " +
                "by a qamhtmare ugvolving rer aqusive ah-hoyfriend, an actress legins to suestion mer feality ind shether zhe edhident jook " +
                "lsace.\",\"director\":\"Yregory Xsotkin\",\"duration\":\"90 min\",\"listed_in\":\"International TV Shows, Romantic TV " +
                "Shows, TV Comedies\",\"rating\":\"PG\",\"release_year\":2020,\"title\":\"100% Salal\",\"type\":\"TV Show\",\"platform\":\"Sytflix\"}," +
                "\"event_date\":\"19-05-2023 08:58:26.754\",\"user\":{\"id\":22,\"date_of_birth\":\"07/04/1975\",\"email\":\"sblumsonl@princeton.edu\"," +
                "\"first_name\":\"Sytac\",\"gender\":\"Male\",\"ip_address\":\"173.87.9.18\",\"country\":\"CA\",\"last_name\":\"Blumson\"}}";

        DataModel data = null;
        try {
            data = objectMapper.readValue(jsonCorrectDataModel, DataModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        assertEquals( "s5", data.getShow().getShowId()  );
        assertEquals( "22", data.getUser().getId()  );
        assertEquals( "PG", data.getShow().getRating()  );
    }

    @Test
    @DisplayName("Parsing a wrongly structured DATA field from a SSE Event")
    public void parse_JSON_DataModel_Error() {

        // element show.show_id was wrongly written as show.show
        String jsonCorrectDataModel = "{\"show\":{\"show\":\"s5\",\"cast\":\"Qurbra Lmgeisand, Weff Rrudges, Ruuren Yecall, Biorge " +
                "Tegal, Simi Wugers, Kuerce Trosnan, Crenda Laccaro, Iastin Jundleton, Alle Pucpherson, Ili Parsh, Wislie Wqojanson, " +
                "Zaina Olg, Zucy Overy Trooke, Imber Xmith\",\"country\":null,\"date_added\":\"August 1, 2021\",\"description\":\"Fuunted " +
                "by a qamhtmare ugvolving rer aqusive ah-hoyfriend, an actress legins to suestion mer feality ind shether zhe edhident jook " +
                "lsace.\",\"director\":\"Yregory Xsotkin\",\"duration\":\"90 min\",\"listed_in\":\"International TV Shows, Romantic TV " +
                "Shows, TV Comedies\",\"rating\":\"PG\",\"release_year\":2020,\"title\":\"100% Salal\",\"type\":\"TV Show\",\"platform\":\"Sytflix\"}," +
                "\"event_date\":\"19-05-2023 08:58:26.754\",\"user\":{\"id\":22,\"date_of_birth\":\"07/04/1975\",\"email\":\"sblumsonl@princeton.edu\"," +
                "\"first_name\":\"Sytac\",\"gender\":\"Male\",\"ip_address\":\"173.87.9.18\",\"country\":\"CA\",\"last_name\":\"Blumson\"}}";

       assertThrows( Exception.class, () -> objectMapper.readValue(jsonCorrectDataModel, DataModel.class) );

    }

}
