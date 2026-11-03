

package com.cmpt276g12.RecQ.turn;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
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

    private final TurnManagementService service =
            new TurnManagementService(queueService, repository);

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
    }

    @Test
    void shouldConfirmOfferedTurn() {

        QueueEntry alice = new QueueEntry(100L, 10L);
        alice.setStatus(QueueStatus.OFFERED);

        when(repository.findById(1L))
                .thenReturn(Optional.of(alice));

        when(repository.save(alice))
                .thenReturn(alice);

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
}
