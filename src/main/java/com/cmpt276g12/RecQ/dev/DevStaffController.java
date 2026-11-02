package com.cmpt276g12.RecQ.dev;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Local development shortcut ONLY. Never activate the dev profile on Render. */
@Controller
@Profile("dev")
public class DevStaffController {
    @GetMapping("/dev/staff-login")
    public String staffLogin(HttpSession session) {
        session.setAttribute("role", "STAFF");
        // Local demo identity; replace with the real signed-in user from Epic 1.
        session.setAttribute("displayName", "Jane Wilson");
        return "redirect:/admin/courts";
    }

    @GetMapping("/dev/participant-login")
    public String participantLogin(HttpSession session) {
        session.setAttribute("role", "PARTICIPANT");
        session.removeAttribute("displayName");
        return "redirect:/api/courts";
    }
}
