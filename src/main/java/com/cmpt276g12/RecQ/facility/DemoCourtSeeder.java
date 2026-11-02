package com.cmpt276g12.RecQ.facility;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Fictional entries for testing the venue -> sport -> court navigation. */
@Configuration
public class DemoCourtSeeder {
    @Bean
    ApplicationRunner seedDemoCourts(CourtRepository repository,
                                     @Value("${recq.demo.seed:true}") boolean seedEnabled) {
        return args -> {
            if (seedEnabled && repository.count() == 0) {
                repository.saveAll(List.of(
                    new Court("Demo Gym A", "Badminton", "Session A · 2:00–4:00 PM", "Court 1"),
                    new Court("Demo Gym A", "Badminton", "Session A · 2:00–4:00 PM", "Court 2"),
                    new Court("Demo Gym A", "Badminton", "Session A · 2:00–4:00 PM", "Court 3"),
                    new Court("Demo Gym A", "Basketball", "Session B · 4:00–6:00 PM", "Court 1"),
                    new Court("Demo Gym A", "Basketball", "Session B · 4:00–6:00 PM", "Court 2"),
                    new Court("Demo Gym B", "Volleyball", "Session C · 1:00–3:00 PM", "Court 1"),
                    new Court("Demo Gym B", "Volleyball", "Session C · 1:00–3:00 PM", "Court 2"),
                    new Court("Demo Gym B", "Pickleball", "Session D · 6:00–8:00 PM", "Court 1")
                ));
            }
        };
    }
}
