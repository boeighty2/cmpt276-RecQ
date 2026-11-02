package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.cmpt276g12.RecQ.facility.Court;
import com.cmpt276g12.RecQ.facility.CourtService;
import com.cmpt276g12.RecQ.facility.CourtStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class AdminCourtEditTest {
    private AdminCourtController controller;
    private CourtService service;
    private Court court;
    private MockHttpServletRequest staff;

    @BeforeEach
    void setup() {
        service = mock(CourtService.class);
        controller = new AdminCourtController(service, new StaffRoleGuard());
        court = new Court("Gym A", "Swimming", "MANUAL", "Court 1");
        when(service.listCourts()).thenReturn(List.of(court));
        staff = withRole("STAFF");
    }

    private static MockHttpServletRequest withRole(String role) {
        var request = new MockHttpServletRequest();
        var session = new MockHttpSession();
        session.setAttribute("role", role);
        request.setSession(session);
        return request;
    }

    @Test
    void venuePageShowsOneConfiguredSport() {
        var model = new ExtendedModelMap();
        assertEquals("admin/venues", controller.dashboard(staff, model));
        @SuppressWarnings("unchecked")
        var venues = (List<AdminCourtController.VenueSummary>) model.get("venues");
        assertEquals(1, venues.get(0).sportCount());
        assertEquals(List.of("Swimming"), venues.get(0).sports());
    }

    @Test
    void legacyDatedUrlRedirectsToCurrentSportPage() {
        // A dated legacy URL is valid when its original session still exists.
        Court legacyCourt = new Court("Gym A", "Swimming", "2026-11-05 14:00–16:00", "Court 1");
        when(service.listCourts()).thenReturn(List.of(legacyCourt));
        assertEquals("redirect:/admin/courts/sport?gym=Gym+A&sport=Swimming",
            controller.session(staff, "Gym A", "Swimming", "2026-11-05 14:00–16:00",
                new ExtendedModelMap()));
    }

    @Test
    void staffCanEditExistingSportCourts() {
        when(service.addSportCourt(1L)).thenReturn(court);
        when(service.removeSportCourt(1L)).thenReturn(court);
        when(service.updateStatus(1L, CourtStatus.CLOSED)).thenReturn(court);
        assertEquals("redirect:/admin/courts/sport", controller.sportAddCourt(
            staff, 1L, new RedirectAttributesModelMap()));
        assertEquals("redirect:/admin/courts/sport", controller.sportDeleteCourt(
            staff, 1L, new RedirectAttributesModelMap()));
        assertEquals("redirect:/admin/courts/sport", controller.sportCourtStatus(
            staff, 1L, CourtStatus.CLOSED, new RedirectAttributesModelMap()));
    }

    @Test
    void participantCannotModifySportCourts() {
        var participant = withRole("PARTICIPANT");
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.sportAddCourt(participant, 1L, new RedirectAttributesModelMap()))
            .getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.sportDeleteCourt(participant, 1L, new RedirectAttributesModelMap()))
            .getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.sportCourtStatus(participant, 1L, CourtStatus.CLOSED,
                new RedirectAttributesModelMap())).getStatusCode());
    }
}
