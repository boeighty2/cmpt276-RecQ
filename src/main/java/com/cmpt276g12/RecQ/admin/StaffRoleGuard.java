package com.cmpt276g12.RecQ.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Temporary integration contract for Epic 1:
 * successful staff login must store session attribute "role" as STAFF or ADMIN.
 * The code fails closed: no session / unknown role => HTTP 403.
 * Epic 1 should replace or coordinate this with its final authorization layer.
 */
@Component
public class StaffRoleGuard {
    private static final Set<String> STAFF_ROLES = Set.of(
        "STAFF", "ADMIN", "RECREATION_STAFF", "ROLE_STAFF", "ROLE_ADMIN");

    public void requireStaff(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object role = session == null ? null : session.getAttribute("role");
        if (role == null || !STAFF_ROLES.contains(role.toString().toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "A Recreation Staff account is required");
        }
    }
}
