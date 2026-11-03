
package com.cmpt276g12.RecQ.turn;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cmpt276g12.RecQ.queue.QueueEntry;
import com.cmpt276g12.RecQ.queue.QueueEntryRepository;
import com.cmpt276g12.RecQ.queue.QueueStatus;

class TurnExpirationSchedulerTest {

    private final TurnOfferRepository offerRepository =
            mock(TurnOfferRepository.class);

    private final QueueEntryRepository queueRepository =
            mock(QueueEntryRepository.class);

    private final TurnManagementService turnService =
            mock(TurnManagementService.class);

    private final TurnExpirationScheduler scheduler =
            new TurnExpirationScheduler(
                    offerRepository,
                    queueRepository,
                    turnService
            );

    @Test
    void shouldProcessExpiredTurn() {

        // Alice was called 4 minutes ago
        TurnOffer offer = new TurnOffer(
                1L,
                Instant.now().minus(Duration.ofMinutes(4))
        );

        QueueEntry alice = new QueueEntry(100L, 10L);
        alice.setStatus(QueueStatus.OFFERED);

        when(offerRepository.findByExpiresAtLessThanEqual(
                any(Instant.class)))
                .thenReturn(List.of(offer));

        when(queueRepository.findById(1L))
                .thenReturn(Optional.of(alice));

        // Run scheduler manually
        scheduler.checkExpiredTurns();

        // The scheduler should process Alice
        verify(turnService).expireTurn(1L);
    }

    @Test
    void shouldIgnoreConfirmedTurn() {

        TurnOffer offer = new TurnOffer(
                1L,
                Instant.now().minus(Duration.ofMinutes(4))
        );

        QueueEntry alice = new QueueEntry(100L, 10L);
        alice.setStatus(QueueStatus.CONFIRMED);

        when(offerRepository.findByExpiresAtLessThanEqual(
                any(Instant.class)))
                .thenReturn(List.of(offer));

        when(queueRepository.findById(1L))
                .thenReturn(Optional.of(alice));

        scheduler.checkExpiredTurns();

        // Confirmed participants must not expire
        verify(turnService, never()).expireTurn(1L);
    }
}
