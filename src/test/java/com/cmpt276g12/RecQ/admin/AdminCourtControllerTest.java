package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

class AdminCourtControllerTest {
    private AdminCourtController controller;
    private CourtService service;
    private MockHttpServletRequest staff;

    @BeforeEach
    void setup() {
        service = mock(CourtService.class);
        controller = new AdminCourtController(service, new StaffRoleGuard());
        staff = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "STAFF");
        staff.setSession(session);
        when(service.listCourts()).thenReturn(List.of(
            new Court("Demo Gym A", "Badminton", "Session A", "Court 1"),
            new Court("Demo Gym A", "Badminton", "Session A", "Court 2"),
            new Court("Demo Gym B", "Volleyball", "Session B", "Court 1")));
    }

    @Test
    void staffSeesVenuesNotFlatCourtList() {
        var model = new ExtendedModelMap();
        assertEquals("admin/venues", controller.dashboard(staff, model));
        @SuppressWarnings("unchecked")
        var venues = (List<AdminCourtController.VenueSummary>) model.get("venues");
        assertEquals(2, venues.size());
        assertEquals("Demo Gym A", venues.get(0).name());
        assertEquals(2, venues.get(0).total());
    }

    @Test
    void venueShowsSports() {
        var model = new ExtendedModelMap();
        assertEquals("admin/venue", controller.venue(staff, "Demo Gym A", model));
        assertEquals("Demo Gym A", model.get("gym"));
    }

    @Test
    void legacySessionLinkRedirectsToSport() {
        var model = new ExtendedModelMap();
        assertEquals("redirect:/admin/courts/sport?gym=Demo+Gym+A&sport=Badminton",
            controller.session(staff, "Demo Gym A", "Badminton", "Session A", model));
    }

    @Test
    void invalidLegacySessionReturnsNotFound() {
        var error = assertThrows(ResponseStatusException.class,
            () -> controller.session(staff, "Demo Gym A", "Volleyball", "Session A", new ExtendedModelMap()));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void participantCannotOpenVenueOrSession() {
        MockHttpServletRequest participant = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "PARTICIPANT");
        participant.setSession(session);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.venue(participant, "Demo Gym A", new ExtendedModelMap())).getStatusCode());
    }

    @Test
    void statusUpdateRedirectsToCurrentSportPage() {
        Court selected = new Court("Demo Gym A", "Badminton", "MANUAL", "Court 1");
        when(service.updateStatus(1L, CourtStatus.CLOSED)).thenReturn(selected);
        var messages = new RedirectAttributesModelMap();
        assertEquals("redirect:/admin/courts/sport",
            controller.sportCourtStatus(staff, 1L, CourtStatus.CLOSED, messages));
        assertEquals("Demo Gym A", messages.get("gym"));
        assertEquals("Badminton", messages.get("sport"));
    }
}
