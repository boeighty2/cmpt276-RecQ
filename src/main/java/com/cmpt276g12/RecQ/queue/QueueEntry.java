package com.cmpt276g12.RecQ.queue;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "queue_entries")
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sportRunId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    protected QueueEntry() {}

    public QueueEntry(Long sportRunId, Long userId) {
        this.sportRunId = sportRunId;
        this.userId = userId;
        this.joinedAt = Instant.now();
        this.status = QueueStatus.WAITING;
    }

    public Long getId() { return id; }
    public Long getSportRunId() { return sportRunId; }
    public Long getUserId() { return userId; }
    public Instant getJoinedAt() { return joinedAt; }
    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }
}