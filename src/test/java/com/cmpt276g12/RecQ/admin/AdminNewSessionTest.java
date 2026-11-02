package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

class AdminNewSessionTest {
    private AdminCourtController controller;
    private CourtService service;
    private MockHttpServletRequest staff;

    @BeforeEach
    void setup() {
        service = mock(CourtService.class);
        controller = new AdminCourtController(service, new StaffRoleGuard());
        staff = requestWithRole("STAFF");
        when(service.listCourts()).thenReturn(List.of(
            new Court("Gym A", "Badminton", "Session 1", "Court 1")));
    }

    private MockHttpServletRequest requestWithRole(String role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", role);
        request.setSession(session);
        return request;
    }

    private NewSessionForm validForm() {
        NewSessionForm form = new NewSessionForm();
        form.setGym("Gym A");
        form.setSport("Pickleball");
        form.setCourtCount("3");
        return form;
    }

    @Test
    void staffCanOpenCreationFormWithPrefilledVenue() {
        var model = new ExtendedModelMap();
        assertEquals("admin/new-session", controller.newSession(staff, "Gym A", model));
        assertEquals("Gym A", ((NewSessionForm) model.get("form")).getGym());
    }

    @Test
    void staffCreatesSessionAndRedirectsToNewCourts() {
        when(service.createSport(anyString(), anyString(), anyInt())).thenReturn(List.of(
                new Court("Gym A", "Pickleball", "MANUAL", "Court 1")));
        var messages = new RedirectAttributesModelMap();
        assertEquals("redirect:/admin/courts/sport", controller.createSession(
            staff, validForm(), new ExtendedModelMap(), messages));
        assertEquals("Gym A", messages.get("gym"));
        assertEquals("Pickleball", messages.get("sport"));
    }

    @Test
    void validationErrorKeepsUserOnForm() {
        var form = validForm();
        form.setCourtCount("not-a-number");
        var model = new ExtendedModelMap();
        assertEquals("admin/new-session", controller.createSession(
            staff, form, model, new RedirectAttributesModelMap()));
        assertTrue(model.containsAttribute("error"));
    }

    @Test
    void participantCannotViewOrSubmitForm() {
        var participant = requestWithRole("PARTICIPANT");
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.newSession(participant, null, new ExtendedModelMap())).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
            () -> controller.createSession(participant, validForm(), new ExtendedModelMap(),
                new RedirectAttributesModelMap())).getStatusCode());
    }
}
