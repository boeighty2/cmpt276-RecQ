package com.cmpt276g12.RecQ.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

class StaffAccountModelTest {
    private final StaffAccountModel model = new StaffAccountModel();

    @Test void formatsFullNameAsInitials() {
        assertEquals("JW", StaffAccountModel.initials("Jane Wilson"));
        assertEquals("J", StaffAccountModel.initials("Jordan"));
    }

    @Test void readsSignedInStaffNameFromSession() {
        var request = new MockHttpServletRequest();
        var session = new MockHttpSession();
        session.setAttribute("displayName", "Jane Wilson");
        request.setSession(session);
        assertEquals("JW", model.staffInitials(request));
    }

    @Test void fallsBackToInitialsFromFirstAndLastName() {
        var request = new MockHttpServletRequest();
        var session = new MockHttpSession();
        session.setAttribute("firstName", "Emily");
        session.setAttribute("lastName", "Tsou");
        request.setSession(session);
        assertEquals("ET", model.staffInitials(request));
    }

    @Test void usesDefaultInitialsWithoutSession() {
        assertEquals("ST", model.staffInitials(new MockHttpServletRequest()));
    }
}
