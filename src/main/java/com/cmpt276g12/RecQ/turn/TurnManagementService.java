
package com.cmpt276g12.RecQ.turn;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cmpt276g12.RecQ.queue.QueueEntry;
import com.cmpt276g12.RecQ.queue.QueueService;
import com.cmpt276g12.RecQ.queue.QueueStatus;

@Service
public class TurnManagementService {

    private final QueueService queueService;

    public TurnManagementService(QueueService queueService) {
        this.queueService = queueService;
    }

    @Transactional
    public QueueEntry callNext(Long sportRunId) {

        // Prevent calling another user while a turn is pending
        boolean hasPendingTurn = queueService.getQueue(sportRunId)
                .stream()
                .anyMatch(entry ->
                        entry.getStatus() == QueueStatus.OFFERED);

        if (hasPendingTurn) {
            throw new IllegalStateException(
                    "A participant has already been called");
        }

        // Find the first waiting participant
        QueueEntry next = queueService.getNextWaiting(sportRunId)
                .orElseThrow(() -> new IllegalStateException(
                        "No participants are waiting"));

        // Change WAITING to OFFERED
        queueService.markOffered(next.getId());

        return next;
    }
}
