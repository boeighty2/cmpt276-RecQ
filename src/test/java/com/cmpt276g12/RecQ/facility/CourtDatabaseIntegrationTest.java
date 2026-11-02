package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Exercises JPA persistence and service rules together, without the development seeder. */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:recq-epic3-integration;DB_CLOSE_DELAY=-1",
    "recq.demo.seed=false"
})
@Transactional
class CourtDatabaseIntegrationTest {
    @Autowired CourtRepository courts;
    @Autowired SportRunRepository runs;
    @Autowired CourtService service;

    @Test
    void venueCaseNormalizationPersistsAndSportRunsAreSeparate() {
        var existing = service.createSport("Central Gym", "Volleyball", 1);
        var added = service.createSport("central gym", "Badminton", 2);
        assertEquals(2, added.size());
        assertTrue(added.stream().allMatch(c -> "Central Gym".equals(c.getGymName())));
        assertEquals("Central Gym", courts.findById(added.get(0).getId()).orElseThrow().getGymName());
        assertFalse(added.get(0).isSportOpen());

        service.setSportOpen("Central Gym", "Badminton", true);
        Long firstId = service.activeRunId("Central Gym", "Badminton");
        assertNotNull(firstId);
        service.setSportOpen("Central Gym", "Badminton", true);
        assertEquals(firstId, service.activeRunId("Central Gym", "Badminton"));
        service.setSportOpen("Central Gym", "Badminton", false);
        assertNotNull(runs.findById(firstId).orElseThrow().getClosedAt());
        service.setSportOpen("Central Gym", "Badminton", true);
        assertNotEquals(firstId, service.activeRunId("Central Gym", "Badminton"));
        assertEquals(1, existing.size());
    }

    @Test
    void closedSportMasksPublicAvailabilityWithoutChangingStoredStatus() {
        var first = service.createSport("South Gym", "Pickleball", 2).get(0);
        service.setSportOpen("South Gym", "Pickleball", true);
        service.updateStatus(first.getId(), CourtStatus.OCCUPIED);
        service.setSportOpen("South Gym", "Pickleball", false);
        var response = CourtApiController.CourtResponse.from(service.findCourt(first.getId()));
        assertFalse(response.sportOpen());
        assertEquals(CourtStatus.CLOSED, response.status());
        assertEquals(CourtStatus.OCCUPIED, service.findCourt(first.getId()).getStatus());
    }

    @Test
    void closedSportCannotAssignAnOccupiedCourt() {
        var first = service.createSport("North Gym", "Badminton", 2).get(0);
        assertThrows(IllegalArgumentException.class,
            () -> service.updateStatus(first.getId(), CourtStatus.OCCUPIED));
    }
}
