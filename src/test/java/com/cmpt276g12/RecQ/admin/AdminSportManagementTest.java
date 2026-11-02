package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.cmpt276g12.RecQ.facility.Court;
import com.cmpt276g12.RecQ.facility.CourtService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class AdminSportManagementTest {
    private CourtService service;
    private AdminCourtController controller;
    private MockHttpServletRequest staff;
    private MockHttpServletRequest participant;

    @BeforeEach
    void setup() {
        service = mock(CourtService.class);
        controller = new AdminCourtController(service, new StaffRoleGuard());
        Court court = new Court("Gym A", "Badminton", "Session 1", "Court 1");
        when(service.listCourts()).thenReturn(List.of(court));
        staff = new MockHttpServletRequest();
        MockHttpSession staffSession = new MockHttpSession();
        staffSession.setAttribute("role", "STAFF");
        staff.setSession(staffSession);
        participant = new MockHttpServletRequest();
        MockHttpSession userSession = new MockHttpSession();
        userSession.setAttribute("role", "PARTICIPANT");
        participant.setSession(userSession);
    }

    @Test
    void staffCanCloseAndDeleteSports() {
        var messages = new RedirectAttributesModelMap();
        assertEquals("redirect:/admin/courts/venue", controller.changeSportState(
            staff, "Gym A", "Badminton", false, messages));
        verify(service).setSportOpen("Gym A", "Badminton", false);
        assertEquals("Gym A", messages.get("gym"));
        assertEquals("redirect:/admin/courts/venue", controller.deleteSport(
            staff, "Gym A", "Badminton", new RedirectAttributesModelMap()));
        verify(service).deleteSport("Gym A", "Badminton");
    }

    @Test
    void staffVenuePageIncludesSportState() {
        var model = new ExtendedModelMap();
        assertEquals("admin/venue", controller.venue(staff, "Gym A", model));
        @SuppressWarnings("unchecked")
        var sports = (List<AdminCourtController.SportSummary>) model.get("sportItems");
        assertNotNull(sports);
        assertEquals(1, sports.size());
        assertEquals("Badminton", sports.get(0).name());
        assertTrue(sports.get(0).open());
    }

    @Test
    void participantCannotChangeOrDeleteSport() {
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.changeSportState(participant, "Gym A", "Badminton", false,
                new RedirectAttributesModelMap())).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.deleteSport(participant, "Gym A", "Badminton",
                new RedirectAttributesModelMap())).getStatusCode());
        verifyNoInteractions(service);
    }
}
