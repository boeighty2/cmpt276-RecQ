package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;

class StaffRoleGuardTest {
    private final StaffRoleGuard guard = new StaffRoleGuard();

    @Test
    void acceptsStaffSession() {
        var request = new MockHttpServletRequest();
        var session = new MockHttpSession();
        session.setAttribute("role", "STAFF");
        request.setSession(session);
        assertDoesNotThrow(() -> guard.requireStaff(request));
    }

    @Test
    void rejectsRegularUser() {
        var request = new MockHttpServletRequest();
        var session = new MockHttpSession();
        session.setAttribute("role", "PARTICIPANT");
        request.setSession(session);
        var error = assertThrows(ResponseStatusException.class,
            () -> guard.requireStaff(request));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void rejectsNotLoggedIn() {
        var request = new MockHttpServletRequest();
        var error = assertThrows(ResponseStatusException.class,
            () -> guard.requireStaff(request));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }
}
