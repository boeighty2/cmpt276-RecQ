package com.cmpt276g12.RecQ.facility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

class DemoCourtSeederTest {
    @Test
    void newDemoSportsStartClosed() throws Exception {
        CourtRepository repo = mock(CourtRepository.class);
        when(repo.count()).thenReturn(0L);

        new DemoCourtSeeder().seedDemoCourts(repo, true)
            .run(new DefaultApplicationArguments(new String[0]));

        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Iterable<Court>> captor = ArgumentCaptor.forClass((Class) Iterable.class);
        verify(repo).saveAll(captor.capture());
        List<Court> seeded = new ArrayList<>();
        captor.getValue().forEach(seeded::add);

        assertEquals(8, seeded.size());
        assertEquals(List.of("Gym A", "Gym B"),
                seeded.stream().map(Court::getGymName).distinct().sorted().toList());
        assertTrue(seeded.stream().allMatch(court -> !court.isSportOpen()));
        assertTrue(seeded.stream().allMatch(court -> court.getStatus() == CourtStatus.AVAILABLE));
    }

    @Test
    void existingCourtsAreNotReseededOrChanged() throws Exception {
        CourtRepository repo = mock(CourtRepository.class);
        when(repo.count()).thenReturn(1L);

        new DemoCourtSeeder().seedDemoCourts(repo, true)
            .run(new DefaultApplicationArguments(new String[0]));

        verify(repo, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void seedingCanBeDisabled() throws Exception {
        CourtRepository repo = mock(CourtRepository.class);

        new DemoCourtSeeder().seedDemoCourts(repo, false)
            .run(new DefaultApplicationArguments(new String[0]));

        verify(repo, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
