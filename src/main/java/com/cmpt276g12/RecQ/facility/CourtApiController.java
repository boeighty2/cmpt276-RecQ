package com.cmpt276g12.RecQ.facility;

import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only endpoint Epic 2 can use to display up-to-date court availability. */
@RestController
@RequestMapping("/api/courts")
public class CourtApiController {
    private final CourtService service;

    public CourtApiController(CourtService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourtResponse> allCourts() {
        return service.listCourts().stream().map(CourtResponse::from).toList();
    }

    public record CourtResponse(
        Long id,
        String gymName,
        String sportName,
        String sessionName,
        String courtName,
        CourtStatus status,
        boolean sportOpen,
        Instant updatedAt
    ) {
        public static CourtResponse from(Court court) {
            return new CourtResponse(
                court.getId(), court.getGymName(), court.getSportName(),
                court.getSessionName(), court.getCourtName(),
                court.isSportOpen() ? court.getStatus() : CourtStatus.CLOSED,
                court.isSportOpen(), court.getUpdatedAt());
        }
    }
}
