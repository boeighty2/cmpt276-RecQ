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

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;
    private final CourtService courtService;
    private final StaffRoleGuard staffGuard;

    public QueueController(QueueService queueService, CourtService courtService,
                           StaffRoleGuard staffGuard) {
        this.queueService = queueService;
        this.courtService = courtService;
        this.staffGuard = staffGuard;
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
    public QueueEntry join(@RequestParam String gym, @RequestParam String sport,
                           @RequestParam Long userId, HttpServletRequest request) {
        staffGuard.requireStaff(request);
        return queueService.joinQueue(runId(gym, sport), userId);
    }

    @GetMapping
    public List<QueueEntry> list(@RequestParam String gym, @RequestParam String sport,
                                 HttpServletRequest request) {
        staffGuard.requireStaff(request);
        return queueService.getQueue(runId(gym, sport));
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

    // public (For epic 2)

    @GetMapping("/count")
    public Map<String, Long> count(@RequestParam String gym, @RequestParam String sport) {
        return Map.of("waitingCount", queueService.getWaitingCount(runId(gym, sport)));
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