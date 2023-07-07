package org.streamer.harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoStreamingEvent {

    private String id;

    private String event;

    private DataModel data;


    public String toString() {

        StringBuilder res = new StringBuilder();

        if (Strings.isNotBlank(this.id ))
            res.append("id: "+id);

        if ( Strings.isNotBlank(this.event))
            res.append("event: "+ this.event);

        if ( this.data != null ) {
            res.append("data: [ "+this.data.toString()+" ]");
        }
        return res.toString();

    }

    public Map<String,String> convertToMap() {

        Map<String,String> map = this.data.convertToMap();

        if ( Strings.isNotBlank(this.id) ) {
            map.put("id", this.id);
        }

        if ( Strings.isNotBlank(this.event) ) {
            map.put("event_name", this.event);
        }

        return map;
    }



}
