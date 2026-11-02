package com.cmpt276g12.RecQ.queue;

import com.cmpt276g12.RecQ.facility.CourtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QueueCourtIntegrationTest {

    @Autowired CourtService courtService;
    @Autowired QueueService queueService;

    @Test
    void closingSportClearsQueue() {
        courtService.createSport("Test Gym", "Badminton", 2);
        courtService.setSportOpen("Test Gym", "Badminton", true);
        Long run = courtService.activeRunId("Test Gym", "Badminton");
        queueService.joinQueue(run, 10L);

        courtService.setSportOpen("Test Gym", "Badminton", false);

        assertFalse(queueService.hasActiveEntries(run));
    }

    @Test
    void cannotDeleteSportWithQueuedPeople() {
        courtService.createSport("Test Gym 2", "Badminton", 2);
        courtService.setSportOpen("Test Gym 2", "Badminton", true);
        Long run = courtService.activeRunId("Test Gym 2", "Badminton");
        queueService.joinQueue(run, 10L);

        assertThrows(IllegalArgumentException.class,
                () -> courtService.deleteSport("Test Gym 2", "Badminton"));
    }
}