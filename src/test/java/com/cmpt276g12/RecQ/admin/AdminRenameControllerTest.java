package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.cmpt276g12.RecQ.facility.CourtService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class AdminRenameControllerTest {
    private static MockHttpServletRequest request(String role) {
        var req = new MockHttpServletRequest();
        var session = new MockHttpSession();
        session.setAttribute("role", role);
        req.setSession(session);
        return req;
    }

    @Test
    void staffCanRenameVenue() {
        var service = mock(CourtService.class);
        var controller = new AdminCourtController(service, new StaffRoleGuard());
        assertEquals("redirect:/admin/courts", controller.renameVenue(
            request("STAFF"), "C", "Central Gym", new RedirectAttributesModelMap()));
        verify(service).renameVenue("C", "Central Gym");
    }

    @Test
    void regularUserCannotRenameSport() {
        var service = mock(CourtService.class);
        var controller = new AdminCourtController(service, new StaffRoleGuard());
        var forbidden = assertThrows(ResponseStatusException.class,
            () -> controller.renameSport(request("PARTICIPANT"),
                "C", "Badminton", "Pickleball", new RedirectAttributesModelMap()));
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
        verifyNoInteractions(service);
    }
}
