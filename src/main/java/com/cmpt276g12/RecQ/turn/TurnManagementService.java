

package com.cmpt276g12.RecQ.turn;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    private final TurnOfferRepository turnOfferRepository;

    public TurnManagementService(
            QueueService queueService,
            QueueEntryRepository queueEntryRepository,
            TurnOfferRepository turnOfferRepository) {

        this.queueService = queueService;
        this.queueEntryRepository = queueEntryRepository;
        this.turnOfferRepository = turnOfferRepository;
    }

    // 1. Call the next waiting participant
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

        // Start the 3-minute confirmation window
        TurnOffer offer = new TurnOffer(
                next.getId(),
                Instant.now()
        );

        turnOfferRepository.save(offer);

        return next;
    }

    // 2. Confirm an offered turn
    @Transactional
    public QueueEntry confirmTurn(Long entryId) {

        QueueEntry entry = queueEntryRepository.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Queue entry not found"));

        if (entry.getStatus() != QueueStatus.OFFERED) {
            throw new IllegalStateException(
                    "Only offered turns can be confirmed");
        }

        TurnOffer offer = turnOfferRepository.findById(entryId)
                .orElseThrow(() -> new IllegalStateException(
                        "Turn offer not found"));

        if (offer.isExpired(Instant.now())) {
            throw new IllegalStateException(
                    "Turn offer has expired");
        }

        entry.setStatus(QueueStatus.CONFIRMED);

        return queueEntryRepository.save(entry);
    }

    // 3. Expire a turn and call the next participant
    @Transactional
    public Optional<QueueEntry> expireTurn(Long entryId) {

        QueueEntry entry = queueEntryRepository.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Queue entry not found"));

        if (entry.getStatus() != QueueStatus.OFFERED) {
            throw new IllegalStateException(
                    "Only offered turns can expire");
        }

        TurnOffer offer = turnOfferRepository.findById(entryId)
                .orElseThrow(() -> new IllegalStateException(
                        "Turn offer not found"));

        if (!offer.isExpired(Instant.now())) {
            throw new IllegalStateException(
                    "Turn has not expired yet");
        }

        // Mark the current participant as expired
        entry.setStatus(QueueStatus.EXPIRED);
        queueEntryRepository.save(entry);

        Long sportRunId = entry.getSportRunId();

        // No more participants waiting
        if (queueService.getNextWaiting(sportRunId).isEmpty()) {
            return Optional.empty();
        }

        // Call the next waiting participant
        return Optional.of(callNext(sportRunId));
    }
}


