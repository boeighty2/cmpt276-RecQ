package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

class StaffAccountControllerTest {
    private final StaffAccountController controller = new StaffAccountController(mock(Environment.class));

    @Test void logoutClearsStaffAndName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "STAFF");
        session.setAttribute("displayName", "Jane Wilson");
        request.setSession(session);
        assertEquals("redirect:/admin/logged-out", controller.logout(request));
        assertTrue(session.isInvalid());
    }

    @Test void logoutWorksWhenNotLoggedIn() {
        assertEquals("redirect:/admin/logged-out", controller.logout(new MockHttpServletRequest()));
    }

    @Test void loggedOutPageIsPublic() {
        var model = new ExtendedModelMap();
        assertEquals("admin/logged-out", controller.loggedOut(model));
    }
}
