package com.cmpt276g12.RecQ.facility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "recq_courts")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gymName;

    @Column(nullable = false)
    private String sportName;

    @Column(nullable = false)
    private String sessionName;

    @Column(nullable = false)
    private String courtName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtStatus status = CourtStatus.AVAILABLE;

    /** Whether this sport is open at the venue. This does not change individual court statuses.
     *  Nullable for safe migration of existing local H2 demo records: NULL means OPEN. */
    @Column(name = "sport_open")
    private Boolean sportOpen = Boolean.TRUE;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Court() {
        // Required by JPA.
    }

    public Court(String gymName, String sportName, String sessionName, String courtName) {
        this.gymName = gymName;
        this.sportName = sportName;
        this.sessionName = sessionName;
        this.courtName = courtName;
    }

    @PrePersist
    @PreUpdate
    public void setTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getGymName() { return gymName; }
    public String getSportName() { return sportName; }
    public String getSessionName() { return sessionName; }
    public String getCourtName() { return courtName; }
    public CourtStatus getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }
    public boolean isSportOpen() { return !Boolean.FALSE.equals(sportOpen); }
    public void setSportOpen(boolean value) { this.sportOpen = value; }

    void setGymName(String gymName) { this.gymName = gymName; }
    void setSportName(String sportName) { this.sportName = sportName; }

    public void setStatus(CourtStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Court status is required");
        }
        this.status = status;
    }
}
