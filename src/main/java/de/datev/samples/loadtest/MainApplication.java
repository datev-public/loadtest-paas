package de.datev.samples.loadtest;

import de.datev.samples.loadtest.config.LoadTestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MainApplication {

    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    @Autowired
    private LoadTestConfiguration configuration;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startup() {

        log.info("MainApplication ApplicationReadyEvent");
        if (configuration.isShowConfigOnStartup()) {
            log.info(configuration.toString());
        }
    }
}
