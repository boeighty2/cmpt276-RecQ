package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourtVenueDeleteTest {
    private CourtRepository repository;
    private CourtService service;
    private Court badminton;
    private Court volleyball;

    @BeforeEach
    void setup() {
        repository = mock(CourtRepository.class);
        service = new CourtService(repository);
        badminton = new Court("Gym A", "Badminton", "MANUAL", "Court 1");
        volleyball = new Court("Gym A", "Volleyball", "MANUAL", "Court 1");
        badminton.setSportOpen(false);
        volleyball.setSportOpen(false);
        when(repository.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of(badminton, volleyball));
    }

    @Test
    void deleteClosedEmptyVenueRemovesAllItsCourts() {
        assertEquals(2, service.deleteVenue("gym a"));
        verify(repository).deleteAll(List.of(badminton, volleyball));
    }

    @Test
    void rejectVenueWithOccupiedCourt() {
        badminton.setStatus(CourtStatus.OCCUPIED);
        assertThrows(IllegalArgumentException.class, () -> service.deleteVenue("Gym A"));
        verify(repository, never()).deleteAll(anyList());
    }

    @Test
    void rejectVenueWithOpenSport() {
        badminton.setSportOpen(true);
        assertThrows(IllegalArgumentException.class, () -> service.deleteVenue("Gym A"));
        verify(repository, never()).deleteAll(anyList());
    }

    @Test
    void rejectMissingVenue() {
        assertThrows(java.util.NoSuchElementException.class,
            () -> service.deleteVenue("Another Gym"));
        verify(repository, never()).deleteAll(anyList());
    }
}
