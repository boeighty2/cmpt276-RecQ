package com.cmpt276g12.RecQ.turn;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cmpt276g12.RecQ.queue.QueueEntry;
import com.cmpt276g12.RecQ.queue.QueueEntryRepository;
import com.cmpt276g12.RecQ.queue.QueueService;
import com.cmpt276g12.RecQ.queue.QueueStatus;

class TurnManagementServiceTest {

    private final QueueService queueService =
            mock(QueueService.class);

    private final QueueEntryRepository repository =
            mock(QueueEntryRepository.class);

    private final TurnOfferRepository offerRepository =
            mock(TurnOfferRepository.class);

    private final TurnManagementService service =
            new TurnManagementService(
                    queueService, repository, offerRepository);

    @Test
    void shouldCallFirstWaitingParticipant() {

        QueueEntry alice = mock(QueueEntry.class);

        when(alice.getId()).thenReturn(1L);
        when(alice.getStatus()).thenReturn(QueueStatus.WAITING);

        when(queueService.getQueue(100L))
                .thenReturn(List.of(alice));

        when(queueService.getNextWaiting(100L))
                .thenReturn(Optional.of(alice));

        QueueEntry result = service.callNext(100L);

        assertSame(alice, result);
        verify(queueService).markOffered(1L);

       
        ArgumentCaptor<TurnOffer> captor =
                ArgumentCaptor.forClass(TurnOffer.class);

        verify(offerRepository).save(captor.capture());

        TurnOffer savedOffer = captor.getValue();

        assertEquals(1L, savedOffer.getQueueEntryId());

        assertEquals(180,
                Duration.between(
                        savedOffer.getOfferedAt(),
                        savedOffer.getExpiresAt()
                ).getSeconds());

        assertFalse(savedOffer.isExpired(
                savedOffer.getOfferedAt()));

        assertTrue(savedOffer.isExpired(
                savedOffer.getExpiresAt()));
    }

    
@Test
void shouldConfirmOfferedTurn() {

    QueueEntry alice = new QueueEntry(100L, 10L);
    alice.setStatus(QueueStatus.OFFERED);

    // Mock the queue entry
    when(repository.findById(1L))
            .thenReturn(Optional.of(alice));

    // Mock Alice's turn offer
    TurnOffer offer = new TurnOffer(1L, Instant.now());

    when(offerRepository.findById(1L))
            .thenReturn(Optional.of(offer));

    when(repository.save(alice))
            .thenReturn(alice);

    // Confirm Alice's turn
    QueueEntry result = service.confirmTurn(1L);

    assertEquals(QueueStatus.CONFIRMED, result.getStatus());

    verify(repository).save(alice);
}


    @Test
    void shouldRejectWaitingParticipant() {

        QueueEntry alice = new QueueEntry(100L, 10L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(alice));

        assertThrows(IllegalStateException.class,
                () -> service.confirmTurn(1L));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectNonexistentEntry() {

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.confirmTurn(999L));
    }
    
@Test
void shouldExpireTurnAndCallNextParticipant() {

    QueueEntry alice = new QueueEntry(100L, 10L);
    alice.setStatus(QueueStatus.OFFERED);

    QueueEntry bob = mock(QueueEntry.class);
    when(bob.getId()).thenReturn(2L);
    when(bob.getStatus()).thenReturn(QueueStatus.WAITING);

    when(repository.findById(1L))
            .thenReturn(Optional.of(alice));

    // Alice was called four minutes ago
    TurnOffer expiredOffer = new TurnOffer(
            1L,
            Instant.now().minus(Duration.ofMinutes(4))
    );

    when(offerRepository.findById(1L))
            .thenReturn(Optional.of(expiredOffer));

    // Bob is the next waiting participant
    when(queueService.getNextWaiting(100L))
            .thenReturn(Optional.of(bob));

    when(queueService.getQueue(100L))
            .thenReturn(List.of(bob));

    // Execute expiration
    Optional<QueueEntry> result = service.expireTurn(1L);

    // Alice should be expired
    assertEquals(QueueStatus.EXPIRED, alice.getStatus());

    // Bob should be selected next
    assertTrue(result.isPresent());
    assertSame(bob, result.get());

    verify(repository).save(alice);
    verify(queueService).markOffered(2L);
    verify(offerRepository).save(any(TurnOffer.class));
}

}
