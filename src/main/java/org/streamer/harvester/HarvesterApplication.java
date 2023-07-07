package org.streamer.harvester;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.streamer.harvester.service.DataHarvesterService;

@Slf4j
@SpringBootApplication
public class HarvesterApplication implements CommandLineRunner {

    @Autowired
    private DataHarvesterService dataHarvesterService;

    public static void main(String[] args) {

        SpringApplication.run(HarvesterApplication.class, args);
    }

    @Override
    public void run(final String... args) {

        dataHarvesterService.processAllIncomingData();

    }

}

