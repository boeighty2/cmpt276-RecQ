
package com.cmpt276g12.RecQ.turn;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "turn_offers")
public class TurnOffer {

    @Id
    private Long queueEntryId;

    @Column(nullable = false)
    private Instant offeredAt;

    @Column(nullable = false)
    private Instant expiresAt;

    protected TurnOffer() {
        // Required by JPA
    }

    public TurnOffer(Long queueEntryId, Instant offeredAt) {
        this.queueEntryId = queueEntryId;
        this.offeredAt = offeredAt;
        this.expiresAt = offeredAt.plus(Duration.ofMinutes(3));
    }

    public Long getQueueEntryId() {
        return queueEntryId;
    }

    public Instant getOfferedAt() {
        return offeredAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}

