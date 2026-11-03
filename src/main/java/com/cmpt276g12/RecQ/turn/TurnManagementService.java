
package com.cmpt276g12.RecQ.turn;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cmpt276g12.RecQ.queue.QueueEntry;
import com.cmpt276g12.RecQ.queue.QueueEntryRepository;
import com.cmpt276g12.RecQ.queue.QueueService;
import com.cmpt276g12.RecQ.queue.QueueStatus;

@Service
public class TurnManagementService {

    private final QueueService queueService;
    private final QueueEntryRepository queueEntryRepository;

    public TurnManagementService(
            QueueService queueService,
            QueueEntryRepository queueEntryRepository) {

        this.queueService = queueService;
        this.queueEntryRepository = queueEntryRepository;
    }

    // Call the next waiting participant
    @Transactional
    public QueueEntry callNext(Long sportRunId) {

        boolean hasPendingTurn = queueService.getQueue(sportRunId)
                .stream()
                .anyMatch(entry ->
                        entry.getStatus() == QueueStatus.OFFERED);

        if (hasPendingTurn) {
            throw new IllegalStateException(
                    "A participant has already been called");
        }

        QueueEntry next = queueService.getNextWaiting(sportRunId)
                .orElseThrow(() -> new IllegalStateException(
                        "No participants are waiting"));

        queueService.markOffered(next.getId());

        return next;
    }

    // Confirm an offered turn
    @Transactional
    public QueueEntry confirmTurn(Long entryId) {

        // Find the queue entry
        QueueEntry entry = queueEntryRepository.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Queue entry not found"));

        // Only offered turns can be confirmed
        if (entry.getStatus() != QueueStatus.OFFERED) {
            throw new IllegalStateException(
                    "Only offered turns can be confirmed");
        }

        // Update the participant's status
        entry.setStatus(QueueStatus.CONFIRMED);
        return queueEntryRepository.save(entry);
    }
}
