package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourtServiceTest {
    private CourtRepository repository;
    private CourtService service;
    private Court court;

    @BeforeEach
    void setup() {
        repository = mock(CourtRepository.class);
        service = new CourtService(repository);
        court = new Court("Demo Gym", "Badminton", "Demo Session", "Court 1");
        when(repository.findById(1L)).thenReturn(Optional.of(court));
        when(repository.save(court)).thenReturn(court);
    }

    @Test
    void staffCanMarkCourtOccupied() {
        service.updateStatus(1L, CourtStatus.OCCUPIED);
        assertEquals(CourtStatus.OCCUPIED, court.getStatus());
        verify(repository).save(court);
    }

    @Test
    void staffCanCloseAndReopenCourt() {
        service.updateStatus(1L, CourtStatus.CLOSED);
        assertEquals(CourtStatus.CLOSED, court.getStatus());
        service.updateStatus(1L, CourtStatus.AVAILABLE);
        assertEquals(CourtStatus.AVAILABLE, court.getStatus());
    }

    @Test
    void rejectsMissingCourt() {
        assertThrows(NoSuchElementException.class,
            () -> service.updateStatus(99L, CourtStatus.CLOSED));
    }

    @Test
    void rejectsNullStatus() {
        assertThrows(IllegalArgumentException.class,
            () -> service.updateStatus(1L, null));
    }
}
