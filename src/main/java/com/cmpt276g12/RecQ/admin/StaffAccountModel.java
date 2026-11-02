package com.cmpt276g12.RecQ.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Shared staff menu data for Epic 3 pages.
 * Integration with Epic 1: provide the authenticated user's display name as
 * session attribute "displayName" (or "firstName" and "lastName").
 * An unknown name uses "ST" (staff), never a hard-coded person's initials.
 */
@ControllerAdvice(assignableTypes = AdminCourtController.class)
public class StaffAccountModel {

    @ModelAttribute("staffInitials")
    public String staffInitials(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return "ST";
        Object displayName = session.getAttribute("displayName");
        if (displayName == null || displayName.toString().isBlank()) {
            Object first = session.getAttribute("firstName");
            Object last = session.getAttribute("lastName");
            if (first != null || last != null) {
                displayName = (first == null ? "" : first.toString()) + " "
                    + (last == null ? "" : last.toString());
            }
        }
        return initials(displayName == null ? "" : displayName.toString());
    }

    static String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "ST";
        String[] parts = fullName.strip().split("\\s+");
        String start = firstCodePoint(parts[0]);
        if (start.isEmpty()) return "ST";
        if (parts.length == 1) return start;
        String end = firstCodePoint(parts[parts.length - 1]);
        return (start + end).toUpperCase(java.util.Locale.ROOT);
    }

    private static String firstCodePoint(String word) {
        if (word.isEmpty()) return "";
        return new String(Character.toChars(word.codePointAt(0)))
            .toUpperCase(java.util.Locale.ROOT);
    }
}
