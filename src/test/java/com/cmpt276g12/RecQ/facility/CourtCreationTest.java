package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourtCreationTest {
    private CourtRepository repository;
    private CourtService service;

    @BeforeEach
    void setup() {
        repository = mock(CourtRepository.class);
        service = new CourtService(repository);
        when(repository.saveAll(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createsManualSportWithTheRequestedNumberOfCourts() {
        List<Court> created = service.createSport(" Demo Gym C ", " Badminton ", 6);
        assertEquals(6, created.size());
        for (int i = 0; i < created.size(); i++) {
            Court court = created.get(i);
            assertEquals("Demo Gym C", court.getGymName());
            assertEquals("Badminton", court.getSportName());
            assertEquals("MANUAL", court.getSessionName());
            assertEquals("Court " + (i + 1), court.getCourtName());
            assertEquals(CourtStatus.AVAILABLE, court.getStatus());
            assertFalse(court.isSportOpen());
        }
    }

    @Test
    void usesExistingVenuesCanonicalSpelling() {
        Court existing = new Court("Central Gym", "Volleyball", "MANUAL", "Court 1");
        when(repository.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of(existing));
        List<Court> created = service.createSport("  central   gym ", "Badminton", 2);
        assertTrue(created.stream().allMatch(c -> "Central Gym".equals(c.getGymName())));
    }

    @Test
    void rejectsDuplicateSportIgnoringVenueAndSportCase() {
        when(repository.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of(new Court("Central Gym", "Badminton", "MANUAL", "Court 1")));
        assertThrows(IllegalArgumentException.class,
            () -> service.createSport("central gym", "BADMINTON", 3));
    }

    @Test
    void rejectsInvalidNamesAndCourtCounts() {
        assertThrows(IllegalArgumentException.class, () -> service.createSport("", "Badminton", 2));
        assertThrows(IllegalArgumentException.class, () -> service.createSport("Gym A", "", 2));
        assertThrows(IllegalArgumentException.class, () -> service.createSport("Gym A", "Badminton", 0));
        assertThrows(IllegalArgumentException.class, () -> service.createSport("Gym A", "Badminton", 31));
    }

    @Test
    void sortsCourtNumbersNumerically() {
        when(repository.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of(
                new Court("Gym A", "Badminton", "MANUAL", "Court 10"),
                new Court("Gym A", "Badminton", "MANUAL", "Court 2")));
        assertEquals("Court 2", service.listCourts().get(0).getCourtName());
        assertEquals("Court 10", service.listCourts().get(1).getCourtName());
    }
}
