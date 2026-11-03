
package com.cmpt276g12.RecQ.turn;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cmpt276g12.RecQ.admin.StaffRoleGuard;
import com.cmpt276g12.RecQ.facility.CourtService;
import com.cmpt276g12.RecQ.queue.QueueEntry;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/turn")
public class TurnController {

    private final TurnManagementService turnService;
    private final CourtService courtService;
    private final StaffRoleGuard staffGuard;

    public TurnController(
            TurnManagementService turnService,
            CourtService courtService,
            StaffRoleGuard staffGuard) {

        this.turnService = turnService;
        this.courtService = courtService;
        this.staffGuard = staffGuard;
    }

    // Staff calls the next participant
    @PostMapping("/next")
    public QueueEntry callNext(
            @RequestParam String gym,
            @RequestParam String sport,
            HttpServletRequest request) {

        staffGuard.requireStaff(request);

        Long runId = courtService.activeRunId(gym, sport);

        if (runId == null) {
            throw new IllegalStateException(
                    "Sport is not open");
        }

        return turnService.callNext(runId);
    }

    // Staff confirms a participant's turn
    @PostMapping("/{entryId}/confirm")
    public QueueEntry confirmTurn(
            @PathVariable Long entryId,
            HttpServletRequest request) {

        staffGuard.requireStaff(request);

        return turnService.confirmTurn(entryId);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(
            IllegalStateException ex) {

        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(
            NoSuchElementException ex) {

        return Map.of("error", ex.getMessage());
    }
}

