
package com.cmpt276g12.RecQ.turn;

import com.cmpt276g12.RecQ.queue.QueueEntry;
import com.cmpt276g12.RecQ.queue.QueueService;
import com.cmpt276g12.RecQ.queue.QueueStatus;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TurnManagementServiceTest {

    @Test
    void shouldCallFirstWaitingParticipant() {

        // Create a fake QueueService for testing
        QueueService queueService = mock(QueueService.class);

        TurnManagementService service =
                new TurnManagementService(queueService);

        // Create a fake participant
        QueueEntry alice = mock(QueueEntry.class);

        when(alice.getId()).thenReturn(1L);
        when(alice.getStatus()).thenReturn(QueueStatus.WAITING);

        // Set up the queue
        when(queueService.getQueue(100L))
                .thenReturn(List.of(alice));

        when(queueService.getNextWaiting(100L))
                .thenReturn(Optional.of(alice));

        // Run the method we want to test
        QueueEntry result = service.callNext(100L);

        // Check the result
        assertSame(alice, result);

        verify(queueService).markOffered(1L);
    }
}

