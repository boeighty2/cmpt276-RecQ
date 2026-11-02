package com.cmpt276g12.RecQ.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/** Local session logout endpoint until Epic 1's authentication is integrated. */
@Controller
public class StaffAccountController {
    private final Environment environment;

    public StaffAccountController(Environment environment) {
        this.environment = environment;
    }

    @PostMapping("/admin/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/admin/logged-out";
    }

    @GetMapping("/admin/logged-out")
    public String loggedOut(Model model) {
        model.addAttribute("devMode", environment.acceptsProfiles(Profiles.of("dev")));
        return "admin/logged-out";
    }
}
