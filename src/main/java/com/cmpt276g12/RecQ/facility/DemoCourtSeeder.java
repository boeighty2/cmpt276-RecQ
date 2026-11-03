package com.cmpt276g12.RecQ.facility;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Fictional venues and courts for local development. Demo sports start closed. */
@Configuration
public class DemoCourtSeeder {
    @Bean
    ApplicationRunner seedDemoCourts(CourtRepository repository,
                                     @Value("${recq.demo.seed:true}") boolean seedEnabled) {
        return args -> {
            if (seedEnabled && repository.count() == 0) {
                repository.saveAll(List.of(
                    closedDemoCourt("Gym A", "Badminton", "Session A · 2:00–4:00 PM", "Court 1"),
                    closedDemoCourt("Gym A", "Badminton", "Session A · 2:00–4:00 PM", "Court 2"),
                    closedDemoCourt("Gym A", "Badminton", "Session A · 2:00–4:00 PM", "Court 3"),
                    closedDemoCourt("Gym A", "Basketball", "Session B · 4:00–6:00 PM", "Court 1"),
                    closedDemoCourt("Gym A", "Basketball", "Session B · 4:00–6:00 PM", "Court 2"),
                    closedDemoCourt("Gym B", "Volleyball", "Session C · 1:00–3:00 PM", "Court 1"),
                    closedDemoCourt("Gym B", "Volleyball", "Session C · 1:00–3:00 PM", "Court 2"),
                    closedDemoCourt("Gym B", "Pickleball", "Session D · 6:00–8:00 PM", "Court 1")
                ));
            }
        };
    }

    private static Court closedDemoCourt(String gym, String sport, String session, String name) {
        Court court = new Court(gym, sport, session, name);
        court.setSportOpen(false);
        return court;
    }
}
