package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManualSportLifecycleTest {
    private CourtRepository courts;
    private SportRunRepository runs;
    private CourtService service;
    private Court court1;
    private Court court2;

    @BeforeEach
    void setup() {
        courts = mock(CourtRepository.class);
        runs = mock(SportRunRepository.class);
        service = new CourtService(courts, runs);
        court1 = new Court("East Gym", "Badminton", "MANUAL", "Court 1");
        court2 = new Court("East Gym", "Badminton", "MANUAL", "Court 2");
        court1.setSportOpen(false);
        court2.setSportOpen(false);
        when(courts.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of(court1, court2));
    }

    @Test
    void addingSportDoesNotRequireDateAndDefaultsClosed() {
        when(courts.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of());
        when(courts.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        var created = service.createSport("East Gym", "Volleyball", 4);
        assertEquals(4, created.size());
        assertTrue(created.stream().noneMatch(Court::isSportOpen));
        assertTrue(created.stream().allMatch(c -> "MANUAL".equals(c.getSessionName())));
    }

    @Test
    void aSportCannotBeDuplicatedAtTheSameVenue() {
        assertThrows(IllegalArgumentException.class, () -> service.createSport("east gym", "BADMINTON", 5));
    }

    @Test
    void eachReopenCreatesASeparateRunAndClosePreservesCourtStatuses() {
        assertEquals(2, service.setSportOpen("East Gym", "Badminton", true));
        verify(runs, times(1)).save(any(SportRun.class));
        assertTrue(court1.isSportOpen());
        // No duplicate run if already open.
        service.setSportOpen("East Gym", "Badminton", true);
        verify(runs, times(1)).save(any(SportRun.class));
        var active = new SportRun("East Gym", "Badminton");
        when(runs.findFirstByGymNameIgnoreCaseAndSportNameIgnoreCaseAndClosedAtIsNullOrderByOpenedAtDesc(
            "East Gym", "Badminton")).thenReturn(Optional.of(active));
        court2.setStatus(CourtStatus.CLOSED);
        service.setSportOpen("East Gym", "Badminton", false);
        assertNotNull(active.getClosedAt());
        assertEquals(CourtStatus.CLOSED, court2.getStatus());
        assertFalse(court1.isSportOpen());
        service.setSportOpen("East Gym", "Badminton", true);
        // two saved starts + close saves (3 total)
        verify(runs, times(3)).save(any(SportRun.class));
    }

    @Test
    void deletingOccupiedCourtIsRejected() {
        court2.setStatus(CourtStatus.OCCUPIED);
        when(courts.findById(2L)).thenReturn(Optional.of(court2));
        assertThrows(IllegalArgumentException.class, () -> service.removeSportCourt(2L));
    }
}
