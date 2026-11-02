package com.cmpt276g12.RecQ.facility;

import jakarta.persistence.*;
import java.time.Instant;

/** One distinct operating period, starting when staff clicks Open Sport.
 *  Epic 4 will attach a fresh FIFO queue to its id. */
@Entity
@Table(name="recq_sport_runs")
public class SportRun {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String gymName;
    @Column(nullable=false) private String sportName;
    @Column(nullable=false) private Instant openedAt;
    private Instant closedAt;
    protected SportRun() {}
    public SportRun(String gymName, String sportName) {
        this.gymName = gymName;
        this.sportName = sportName;
        this.openedAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getGymName() { return gymName; }
    public String getSportName() { return sportName; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getClosedAt() { return closedAt; }
    void renameVenue(String name) { this.gymName = name; }
    void renameSport(String name) { this.sportName = name; }
    public void close() { if (closedAt == null) closedAt = Instant.now(); }
}
