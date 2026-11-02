package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SportManagementTest {
    private CourtRepository repository;
    private CourtService service;
    private Court badminton1;
    private Court badminton2;
    private Court basketball;

    @BeforeEach
    void setup() {
        repository = mock(CourtRepository.class);
        service = new CourtService(repository);
        badminton1 = new Court("Gym A", "Badminton", "Session 1", "Court 1");
        badminton2 = new Court("Gym A", "Badminton", "Session 2", "Court 2");
        basketball = new Court("Gym A", "Basketball", "Session 1", "Court 1");
        when(repository.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc())
            .thenReturn(List.of(badminton1, badminton2, basketball));
    }

    @Test
    void closingWholeSportPreservesIndividualCourtsAndOtherSports() {
        badminton2.setStatus(CourtStatus.CLOSED);
        assertEquals(2, service.setSportOpen("Gym A", "Badminton", false));
        assertFalse(badminton1.isSportOpen());
        assertFalse(badminton2.isSportOpen());
        assertEquals(CourtStatus.AVAILABLE, badminton1.getStatus());
        assertEquals(CourtStatus.CLOSED, badminton2.getStatus());
        assertTrue(basketball.isSportOpen());
        assertEquals(2, service.setSportOpen("Gym A", "Badminton", true));
        assertTrue(badminton1.isSportOpen());
        assertEquals(CourtStatus.CLOSED, badminton2.getStatus());
    }

    @Test
    void deletionOfSportRemovesAllOfItsSessionsNotOtherSports() {
        assertEquals(2, service.deleteSport("Gym A", "Badminton"));
        verify(repository).deleteAll(List.of(badminton1, badminton2));
        verify(repository, never()).delete(basketball);
    }

    @Test
    void occupiedSportCannotBeDeleted() {
        badminton2.setStatus(CourtStatus.OCCUPIED);
        assertThrows(IllegalArgumentException.class,
            () -> service.deleteSport("Gym A", "Badminton"));
        verify(repository, never()).deleteAll(anyList());
    }

    @Test
    void unknownSportCannotBeManaged() {
        assertThrows(NoSuchElementException.class,
            () -> service.setSportOpen("Gym A", "Tennis", false));
    }

    @Test
    void readOnlyApiMarksClosedSportUnavailableWithoutChangingCourtStatus() {
        badminton1.setSportOpen(false);
        var response = CourtApiController.CourtResponse.from(badminton1);
        assertEquals(CourtStatus.CLOSED, response.status());
        assertFalse(response.sportOpen());
        assertEquals(CourtStatus.AVAILABLE, badminton1.getStatus());
    }
}
