package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourtRenameTest {
    private CourtRepository courts;
    private SportRunRepository runs;
    private CourtService service;
    private Court badminton;
    private Court volleyball;
    private SportRun history;

    @BeforeEach
    void prepare() {
        courts = mock(CourtRepository.class);
        runs = mock(SportRunRepository.class);
        service = new CourtService(courts, runs);
        badminton = new Court("C", "Badminton", "MANUAL", "Court 1");
        badminton.setSportOpen(false);
        badminton.setStatus(CourtStatus.CLOSED);
        volleyball = new Court("Demo Gym B", "Volleyball", "MANUAL", "Court 1");
        history = new SportRun("C", "Badminton");
        history.close();
        when(courts.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenAnswer(inv -> List.of(badminton, volleyball));
        when(runs.findAll()).thenAnswer(inv -> List.of(history));
    }

    @Test
    void renameVenuePreservesCourtStatusAndHistoricalRun() {
        assertEquals(1, service.renameVenue("C", "Central Gym"));
        assertEquals("Central Gym", badminton.getGymName());
        assertEquals("Central Gym", history.getGymName());
        assertEquals(CourtStatus.CLOSED, badminton.getStatus());
        assertFalse(badminton.isSportOpen());
        assertNotNull(history.getClosedAt());
        verify(courts).saveAll(anyList());
        verify(runs).saveAll(anyList());
    }

    @Test
    void duplicateVenueIsRejectedAndOriginalDataIsUnchanged() {
        assertThrows(IllegalArgumentException.class,
            () -> service.renameVenue("C", "demo gym b"));
        assertEquals("C", badminton.getGymName());
        verify(courts, never()).saveAll(anyList());
    }

    @Test
    void renameSportPreservesRunAndCourtStatus() {
        assertEquals(1, service.renameSport("C", "Badminton", "Pickleball"));
        assertEquals("Pickleball", badminton.getSportName());
        assertEquals("Pickleball", history.getSportName());
        assertEquals(CourtStatus.CLOSED, badminton.getStatus());
    }

    @Test
    void duplicateSportWithinVenueIsRejected() {
        Court existing = new Court("C", "Pickleball", "MANUAL", "Court 1");
        when(courts.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of(badminton, existing, volleyball));
        assertThrows(IllegalArgumentException.class,
            () -> service.renameSport("C", "Badminton", "pickleball"));
        assertEquals("Badminton", badminton.getSportName());
    }

    @Test
    void caseOnlyRenameIsAllowed() {
        assertEquals(1, service.renameVenue("c", "c"));
        assertEquals("c", badminton.getGymName());
    }
}
