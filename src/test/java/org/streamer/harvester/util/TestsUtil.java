package org.streamer.harvester.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.streamer.harvester.dto.DataModel;
import org.streamer.harvester.dto.VideoStreamingEvent;
import org.streamer.harvester.exception.SSEParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "TestsUtil")
public class TestsUtil {


    /**
     * Read a file with the Server Sent Events Structure and transform each event into a String to a Strings array
     *
     * @param filePath  File with the events
     * @return  A String list with all the captured events
     */
    public static List<String> readFileToListOfEvents(String filePath) {
        List<String> elements = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    // Empty line indicates the end of an element
                    if (sb.length() > 0) {
                        elements.add(sb.toString());
                        sb.setLength(0); // Clear the StringBuilder
                    }
                } else {
                    // Concatenate non-empty lines
                    sb.append(line).append(System.lineSeparator());
                }
            }

            // Add the last element if there is one
            if (sb.length() > 0) {
                elements.add(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return elements;
    }

    public static VideoStreamingEvent parseVideoStreamingEvent(String m, ObjectMapper objectMapper) throws SSEParserException {

        if (Strings.isEmpty(m) )
            throw new SSEParserException("VideoStreamingMessage cannot be empty.");

        String[] ls = m.split("[\r\n]");

        if (ls.length != 3 )
            throw new SSEParserException("VideoStreamingMessage has a structure problem.");

        VideoStreamingEvent e = new VideoStreamingEvent();
        String id = "", event = "";
        e.setId(id = ls[0].substring(ls[0].indexOf(':')+1));
        log.info("id "+id);
        e.setEvent(event = ls[1].substring(ls[1].indexOf(':')+1));
        log.info( "event "+event);
        try {
            e.setData(objectMapper.readValue(ls[2].substring(ls[2].indexOf(':')+1), DataModel.class));
        } catch (JsonProcessingException ex) {
            throw new SSEParserException(ex.toString());
        }

        return e;
    }


}
