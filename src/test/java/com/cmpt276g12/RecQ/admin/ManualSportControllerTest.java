package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.cmpt276g12.RecQ.facility.Court;
import com.cmpt276g12.RecQ.facility.CourtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

class ManualSportControllerTest {
    private static MockHttpServletRequest withRole(String role) {
        var request = new MockHttpServletRequest();
        var session = new MockHttpSession();
        session.setAttribute("role", role);
        request.setSession(session);
        return request;
    }

    @Test
    void staffSeesCourtsAcrossFormerDateSessionsInOneSport() {
        CourtService service = mock(CourtService.class);
        when(service.listCourts()).thenReturn(List.of(
            new Court("East Gym", "Badminton", "2026-10-17 12:22–14:22", "Court 1"),
            new Court("East Gym", "Badminton", "MANUAL", "Court 2")));
        var controller = new AdminCourtController(service, new StaffRoleGuard());
        var model = new ExtendedModelMap();
        assertEquals("admin/sport-courts", controller.sportCourts(
            withRole("STAFF"), "East Gym", "Badminton", model));
        assertEquals(2, ((List<?>) model.get("courts")).size());
        assertFalse(model.containsAttribute("scheduleDisplay"));
    }

    @Test
    void participantCannotOpenAdminSportPage() {
        var controller = new AdminCourtController(mock(CourtService.class), new StaffRoleGuard());
        var error = assertThrows(ResponseStatusException.class, () -> controller.sportCourts(
            withRole("PARTICIPANT"), "East Gym", "Badminton", new ExtendedModelMap()));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }
}
