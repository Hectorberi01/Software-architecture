package com.parking.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class ClockConfig {

    // Injectable Clock bean allows deterministic time in tests
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
