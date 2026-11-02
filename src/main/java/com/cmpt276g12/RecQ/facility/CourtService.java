package com.cmpt276g12.RecQ.facility;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.cmpt276g12.RecQ.queue.QueueService;

@Service
public class CourtService {
    private static final int MAX_COURTS_PER_SPORT = 30;
    private final CourtRepository repository;
    private final SportRunRepository runRepository;

    @Autowired
    public CourtService(CourtRepository repository, SportRunRepository runRepository) {
        this.repository = repository;
        this.runRepository = runRepository;
    }

    // Used by the existing unit tests, which mock only the CourtRepository.
    CourtService(CourtRepository repository) {
        this(repository, null);
    }

    @Transactional(readOnly = true)
    public List<Court> listCourts() {
        return repository.findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc()
            .stream().sorted(Comparator.comparing(Court::getGymName)
                .thenComparing(Court::getSportName)
                .thenComparing(Court::getSessionName)
                .thenComparingInt(c -> courtNumber(c.getCourtName()))
                .thenComparing(Court::getCourtName))
            .toList();
    }

    @Transactional
    public Court updateStatus(long id, CourtStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("A new court status is required");
        }
        Court court = getCourt(id);
        if (!court.isSportOpen() && newStatus == CourtStatus.OCCUPIED) {
            throw new IllegalArgumentException("Open the sport before marking a court occupied.");
        }
        court.setStatus(newStatus);
        return repository.save(court);
    }

    /** Configure a sport once per venue. No scheduled time is required:
     *  staff explicitly opens and closes each live drop-in period. */
    @Transactional
    public List<Court> createSport(String gymInput, String sportInput, int courtCount) {
        String gym = requiredName(gymInput, "Venue / location");
        String sport = requiredName(sportInput, "Sport");
        if (courtCount < 1 || courtCount > MAX_COURTS_PER_SPORT) {
            throw new IllegalArgumentException("Number of courts must be between 1 and 30.");
        }
        List<Court> existingCourts = listCourts();
        if (existingCourts.stream().anyMatch(c -> c.getGymName().equalsIgnoreCase(gym)
                && c.getSportName().equalsIgnoreCase(sport))) {
            throw new IllegalArgumentException("This sport already exists at this venue. "
                + "Open it to manage its courts, or use Manage to add more courts.");
        }
        // Use the existing venue's spelling so case-only variations cannot create duplicate cards.
        String canonicalGym = existingCourts.stream()
            .filter(c -> c.getGymName().equalsIgnoreCase(gym))
            .map(Court::getGymName).findFirst().orElse(gym);
        List<Court> added = new ArrayList<>();
        for (int i = 1; i <= courtCount; i++) {
            Court court = new Court(canonicalGym, sport, "MANUAL", "Court " + i);
            court.setSportOpen(false); // Default CLOSED until staff explicitly opens.
            added.add(court);
        }
        return repository.saveAll(added);
    }

    /** Physical courts belong to the venue+sport, not to a dated schedule. */
    @Transactional
    public Court addSportCourt(long existingCourtId) {
        Court anchor = getCourt(existingCourtId);
        List<Court> courts = sportCourts(anchor.getGymName(), anchor.getSportName());
        if (courts.size() >= MAX_COURTS_PER_SPORT) {
            throw new IllegalArgumentException("A sport can have at most 30 courts at a venue.");
        }
        int number = 1;
        while (containsCourtName(courts, "Court " + number)) number++;
        Court created = new Court(anchor.getGymName(), anchor.getSportName(),
            anchor.getSessionName(), "Court " + number);
        created.setSportOpen(anchor.isSportOpen());
        return repository.save(created);
    }

    @Transactional
    public Court removeSportCourt(long courtId) {
        Court court = getCourt(courtId);
        if (court.getStatus() == CourtStatus.OCCUPIED) {
            throw new IllegalArgumentException("An occupied court cannot be deleted.");
        }
        if (sportCourts(court.getGymName(), court.getSportName()).size() <= 1) {
            throw new IllegalArgumentException("A sport must keep at least one court.");
        }
        repository.delete(court);
        return court;
    }

    /** Current live run ID is reserved for Epic 4 queue integration. */
    @Transactional(readOnly = true)
    public Long activeRunId(String gym, String sport) {
        if (runRepository == null) return null;
        return runRepository.findFirstByGymNameIgnoreCaseAndSportNameIgnoreCaseAndClosedAtIsNullOrderByOpenedAtDesc(
            gym, sport).map(SportRun::getId).orElse(null);
    }

    /** Open/close all courts for a sport at one venue.
     *  Individual court statuses remain unchanged for later reopening. */
    @Transactional
    public int setSportOpen(String venueName, String sportName, boolean open) {
        List<Court> selected = sportCourts(venueName, sportName);
        boolean wasOpen = selected.get(0).isSportOpen();
        if (wasOpen == open) return selected.size(); // No duplicate opening records.
        for (Court court : selected) court.setSportOpen(open);
        repository.saveAll(selected);
        if (runRepository != null) {
            if (open) {
                // A new run per opening: queue rows should reference THIS id, not yesterday's run.
                runRepository.save(new SportRun(selected.get(0).getGymName(),
                    selected.get(0).getSportName()));
            } else {
                runRepository.findFirstByGymNameIgnoreCaseAndSportNameIgnoreCaseAndClosedAtIsNullOrderByOpenedAtDesc(
                    selected.get(0).getGymName(), selected.get(0).getSportName())
                    .ifPresent(run -> {
                        if (queueService != null) queueService.closeQueue(run.getId());
                        run.close();
                        runRepository.save(run);
                    });
            }
        }
        return selected.size();
    }

    /** Rename a venue without deleting its courts or replacing their IDs.
     *  Also retain the venue label on previous/current run records. */
    @Transactional
    public int renameVenue(String oldNameInput, String newNameInput) {
        String oldName = requiredName(oldNameInput, "Current venue name");
        String newName = requiredName(newNameInput, "New venue name");
        List<Court> all = listCourts();
        List<Court> selected = all.stream()
            .filter(c -> c.getGymName().equalsIgnoreCase(oldName)).toList();
        if (selected.isEmpty()) {
            throw new NoSuchElementException("Venue not found: " + oldName);
        }
        if (all.stream().anyMatch(c -> !c.getGymName().equalsIgnoreCase(oldName)
                && c.getGymName().equalsIgnoreCase(newName))) {
            throw new IllegalArgumentException("A venue with that name already exists.");
        }
        if (selected.get(0).getGymName().equals(newName)) return selected.size();
        // Update both historical/active run labels and courts in one transaction.
        if (runRepository != null) {
            List<SportRun> runs = runRepository.findAll().stream()
                .filter(run -> run.getGymName().equalsIgnoreCase(oldName)).toList();
            runs.forEach(run -> run.renameVenue(newName));
            runRepository.saveAll(runs);
        }
        selected.forEach(c -> c.setGymName(newName));
        repository.saveAll(selected);
        return selected.size();
    }

    /** Delete all configured sports and courts at one venue. Only permitted after all
     *  sports are closed and no court is occupied. Epic 4 integration must check
     *  pending queue entries before enabling this in production. */
    @Transactional
    public int deleteVenue(String venueInput) {
        String venue = requiredName(venueInput, "Venue");
        List<Court> selected = listCourts().stream()
            .filter(c -> c.getGymName().equalsIgnoreCase(venue)).toList();
        if (selected.isEmpty()) {
            throw new NoSuchElementException("Venue not found: " + venue);
        }
        if (selected.stream().anyMatch(c -> c.getStatus() == CourtStatus.OCCUPIED)) {
            throw new IllegalArgumentException("Cannot delete a venue with occupied courts.");
        }
        if (selected.stream().anyMatch(Court::isSportOpen)) {
            throw new IllegalArgumentException("Close every sport at this venue before deleting it.");
        }
        if (runRepository != null) {
            List<SportRun> runs = runRepository.findAll().stream()
                .filter(run -> run.getGymName().equalsIgnoreCase(venue)
                    && run.getClosedAt() == null).toList();
            runs.forEach(SportRun::close);
            runRepository.saveAll(runs);
        }
        repository.deleteAll(selected);
        // Historical run records are intentionally retained for auditing.
        return selected.size();
    }

    /** Rename only the sport at one venue; it must not merge with another sport. */
    @Transactional
    public int renameSport(String gymInput, String oldSportInput, String newSportInput) {
        String gym = requiredName(gymInput, "Venue");
        String oldSport = requiredName(oldSportInput, "Current sport name");
        String newSport = requiredName(newSportInput, "New sport name");
        List<Court> selected = sportCourts(gym, oldSport);
        if (listCourts().stream().anyMatch(c -> c.getGymName().equalsIgnoreCase(gym)
                && !c.getSportName().equalsIgnoreCase(oldSport)
                && c.getSportName().equalsIgnoreCase(newSport))) {
            throw new IllegalArgumentException("That sport already exists at this venue.");
        }
        if (selected.get(0).getSportName().equals(newSport)) return selected.size();
        if (runRepository != null) {
            List<SportRun> runs = runRepository.findAll().stream()
                .filter(run -> run.getGymName().equalsIgnoreCase(gym)
                    && run.getSportName().equalsIgnoreCase(oldSport)).toList();
            runs.forEach(run -> run.renameSport(newSport));
            runRepository.saveAll(runs);
        }
        selected.forEach(c -> c.setSportName(newSport));
        repository.saveAll(selected);
        return selected.size();
    }

/** Delete the selected sport and all its courts at this venue.
 *  Blocks deletion when any court is occupied. Epic 4 must later add a
 *  pending-queue check before production integration. */
    @Transactional
    public int deleteSport(String venueName, String sportName) {
        List<Court> selected = sportCourts(venueName, sportName);
        if (selected.stream().anyMatch(c -> c.getStatus() == CourtStatus.OCCUPIED)) {
            throw new IllegalArgumentException(
                "Cannot delete this sport while any of its courts are occupied.");
        }
        if (runRepository != null) {
            runRepository.findFirstByGymNameIgnoreCaseAndSportNameIgnoreCaseAndClosedAtIsNullOrderByOpenedAtDesc(
                selected.get(0).getGymName(), selected.get(0).getSportName())
                .ifPresent(run -> {
                    if (queueService != null && queueService.hasActiveEntries(run.getId())) {
                        throw new IllegalArgumentException(
                            "Cannot delete this sport while people are queued. Close it first to clear the queue.");
                    }
                    run.close();
                    runRepository.save(run);
                });
        }
        repository.deleteAll(selected);
        // Keep the CLOSED run audit history; the sport may be re-created later.
        return selected.size();
    }

    private List<Court> sportCourts(String venueName, String sportName) {
        String gym = requiredName(venueName, "Venue / location");
        String sport = requiredName(sportName, "Sport");
        List<Court> selected = listCourts().stream()
            .filter(c -> c.getGymName().equalsIgnoreCase(gym)
                && c.getSportName().equalsIgnoreCase(sport)).toList();
        if (selected.isEmpty()) {
            throw new NoSuchElementException("Sport not found at this venue: " + sport);
        }
        return selected;
    }

    @Transactional(readOnly = true)
    public Court findCourt(long id) {
        return getCourt(id);
    }

    private Court getCourt(long id) {
        return repository.findById(id).orElseThrow(
            () -> new NoSuchElementException("Court not found: " + id));
    }

    private static boolean containsCourtName(List<Court> courts, String name) {
        return courts.stream().anyMatch(c -> c.getCourtName().equalsIgnoreCase(name));
    }

    private static int courtNumber(String name) {
        if (name.startsWith("Court ")) {
            try {
                return Integer.parseInt(name.substring(6));
            } catch (NumberFormatException ignored) {
                // Future integration may provide non-numeric court labels.
            }
        }
        return Integer.MAX_VALUE;
    }

    private static String requiredName(String value, String label) {
        String cleaned = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (cleaned.isBlank() || cleaned.length() > 100) {
            throw new IllegalArgumentException(label + " must be between 1 and 100 characters.");
        }
        return cleaned;
    }
    
    private QueueService queueService; // null in unit tests that build CourtService by hand

    @Autowired(required = false)
    void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }
}
