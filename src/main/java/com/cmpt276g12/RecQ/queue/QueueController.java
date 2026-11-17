package com.cmpt276g12.RecQ.queue;
        
import com.cmpt276g12.RecQ.admin.StaffRoleGuard;
import com.cmpt276g12.RecQ.facility.CourtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.cmpt276g12.RecQ.auth.model.AppUser;
import com.cmpt276g12.RecQ.auth.repository.AppUserRepository;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;
    private final CourtService courtService;
    private final StaffRoleGuard staffGuard;

    private final AppUserRepository users;

    public QueueController(QueueService queueService, CourtService courtService,
                        StaffRoleGuard staffGuard, AppUserRepository users) {
        this.queueService = queueService;
        this.courtService = courtService;
        this.staffGuard = staffGuard;
        this.users = users;
    }

    // activeRunId returns null when the sport is closed or has never been opened
    private Long runId(String gym, String sport) {
        Long id = courtService.activeRunId(gym, sport);
        if (id == null) {
            throw new IllegalStateException("Sport is not open");
        }
        return id;
    }

    // staff

    @PostMapping("/join")
    public QueueEntryView join(@RequestParam String gym, @RequestParam String sport,
                            @RequestParam String username, HttpServletRequest request) {
        staffGuard.requireStaff(request);
        Long run = runId(gym, sport);
        String name = username.trim();
        AppUser user = users.findByUsernameIgnoreCase(name)
                .orElseThrow(() -> new NoSuchElementException(
                        "No user with username \"" + name + "\""));
        QueueEntry entry = queueService.joinQueue(run, user.getId());
        return new QueueEntryView(entry.getId(), user.getId(), user.getUsername(),
                entry.getStatus(), entry.getJoinedAt());
    }

    @GetMapping
    public List<QueueEntryView> list(@RequestParam String gym, @RequestParam String sport,
                                    HttpServletRequest request) {
        staffGuard.requireStaff(request);
        List<QueueEntry> entries = queueService.getQueue(runId(gym, sport));
        Map<Long, String> names = users
                .findAllById(entries.stream().map(QueueEntry::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getUsername));
        return entries.stream()
                .map(e -> new QueueEntryView(e.getId(), e.getUserId(),
                        names.getOrDefault(e.getUserId(), "user " + e.getUserId()),
                        e.getStatus(), e.getJoinedAt()))
                .toList();
    }

    @PostMapping("/entries/{entryId}/remove")
    public ResponseEntity<Void> remove(@PathVariable Long entryId, HttpServletRequest request) {
        staffGuard.requireStaff(request);
        queueService.removeEntry(entryId);
        return ResponseEntity.noContent().build();
    }

    // participant (userId is a placeholder until Epic 1 provides a logged-in user)

    @GetMapping("/position")
    public Map<String, Object> position(@RequestParam String gym, @RequestParam String sport,
                                        @RequestParam Long userId) {
        Long run = runId(gym, sport);
        return Map.of(
                "position", queueService.getPosition(run, userId),
                "waitingCount", queueService.getWaitingCount(run));
    }

    // Known gap: anyone with an entry ID can leave that entry. Epic 1 should check ownership
    @PostMapping("/entries/{entryId}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long entryId) {
        queueService.leaveQueue(entryId);
        return ResponseEntity.noContent().build();
    }

    //Epic 2 uses this. no errors for closed sports
    @GetMapping("/count")
    public Map<String, Object> count(@RequestParam String gym, @RequestParam String sport) {
        Long id = courtService.activeRunId(gym, sport);
        if (id == null) {
            return Map.of("queueActive", false, "waitingCount", 0L);
        }
        return Map.of("queueActive", true, "waitingCount", queueService.getWaitingCount(id));
    }

    // errors

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(IllegalStateException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("error", ex.getMessage());
    }
}