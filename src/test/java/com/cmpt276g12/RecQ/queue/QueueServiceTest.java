package com.cmpt276g12.RecQ.queue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QueueServiceTest {

    @Autowired
    QueueService service;

    @Test
    void fifoOrderIsKept() {
        service.joinQueue(1L, 10L);
        service.joinQueue(1L, 20L);
        service.joinQueue(1L, 30L);

        List<Long> userIds = service.getQueue(1L).stream()
                .map(QueueEntry::getUserId).toList();

        assertEquals(List.of(10L, 20L, 30L), userIds);
    }

    @Test
    void duplicateJoinIsRejected() {
        service.joinQueue(2L, 10L);
        assertThrows(IllegalStateException.class, () -> service.joinQueue(2L, 10L));
    }

    @Test
    void positionIsOneBased() {
        service.joinQueue(3L, 10L);
        service.joinQueue(3L, 20L);

        assertEquals(1, service.getPosition(3L, 10L));
        assertEquals(2, service.getPosition(3L, 20L));
    }

    @Test
    void positionUpdatesWhenSomeoneAheadLeaves() {
        QueueEntry first = service.joinQueue(4L, 10L);
        service.joinQueue(4L, 20L);

        service.leaveQueue(first.getId());

        assertEquals(1, service.getPosition(4L, 20L));
    }

    @Test
    void removedEntryCannotBeRemovedTwice() {
        QueueEntry e = service.joinQueue(5L, 10L);
        service.removeEntry(e.getId());
        assertThrows(IllegalStateException.class, () -> service.removeEntry(e.getId()));
    }

    @Test
    void nextWaitingIsFrontOfLine() {
        service.joinQueue(6L, 10L);
        service.joinQueue(6L, 20L);

        assertEquals(10L, service.getNextWaiting(6L).orElseThrow().getUserId());
        assertEquals(2, service.getWaitingCount(6L));
    }

    @Test
    void closeQueueClearsActiveEntries() {
        service.joinQueue(7L, 10L);
        service.joinQueue(7L, 20L);
        assertTrue(service.hasActiveEntries(7L));

        service.closeQueue(7L);

        assertFalse(service.hasActiveEntries(7L));
        assertTrue(service.getQueue(7L).isEmpty());
    }
}