package org.streamer.harvester.util;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    // list of Zones used to convert data/time structures
    public final static Map<String, ZoneId> zoneIdsMap = new HashMap<>()  {
        {
            put("PT", ZoneId.of("UTC"));
            put("Portugal", ZoneId.of("UTC"));
            put("CA", ZoneId.of("America/Toronto"));
            put("Canada", ZoneId.of("America/Toronto"));
            put("US", ZoneId.of("America/Los_Angeles"));
            put("United States", ZoneId.of("America/Los_Angeles"));
            put("RU", ZoneId.of("Europe/Moscow"));
            put("Russia", ZoneId.of("Europe/Moscow"));
            put("ID", ZoneId.of("Asia/Jakarta"));
            put("Indonesia", ZoneId.of("Asia/Jakarta"));
            put("CN", ZoneId.of("Asia/Shanghai"));
            put("China", ZoneId.of("Asia/Shanghai"));
        }
    };

}
