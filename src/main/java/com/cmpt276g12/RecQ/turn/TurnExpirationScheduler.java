
package com.cmpt276g12.RecQ.turn;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cmpt276g12.RecQ.queue.QueueEntry;
import com.cmpt276g12.RecQ.queue.QueueEntryRepository;
import com.cmpt276g12.RecQ.queue.QueueStatus;

@Component
public class TurnExpirationScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(TurnExpirationScheduler.class);

    private final TurnOfferRepository offerRepository;
    private final QueueEntryRepository queueRepository;
    private final TurnManagementService turnService;

    public TurnExpirationScheduler(
            TurnOfferRepository offerRepository,
            QueueEntryRepository queueRepository,
            TurnManagementService turnService) {

        this.offerRepository = offerRepository;
        this.queueRepository = queueRepository;
        this.turnService = turnService;
    }

    @Scheduled(fixedDelay = 10000)
    public void checkExpiredTurns() {

        Instant now = Instant.now();

        for (TurnOffer offer :
                offerRepository.findByExpiresAtLessThanEqual(now)) {

            Long entryId = offer.getQueueEntryId();

            try {
                Optional<QueueEntry> entry =
                        queueRepository.findById(entryId);

                // Ignore already confirmed or expired turns
                if (entry.isEmpty()
                        || entry.get().getStatus() != QueueStatus.OFFERED) {
                    continue;
                }

                // Expire the turn and call the next participant
                turnService.expireTurn(entryId);

            } catch (RuntimeException e) {
                logger.warn(
                        "Failed to process expired turn {}",
                        entryId, e);
            }
        }
    }
}

