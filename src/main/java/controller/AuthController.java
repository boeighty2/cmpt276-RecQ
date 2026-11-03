package ca.example.login.controller;

import ca.example.login.model.Role;
import ca.example.login.service.RegistrationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegistrationForm form,
                           BindingResult result, Model model) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }
        if (result.hasErrors()) return "register";

        try {
            registrationService.register(form.getEmail(), form.getUsername(), form.getPassword(),
                form.getRole(), form.getAdminInviteCode());
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    public static class RegistrationForm {
        @NotBlank @Email @Size(max = 100)
        private String email = "";
        @NotBlank @Size(min = 3, max = 50)
        private String username = "";
        @NotBlank @Size(min = 8, max = 72)
        private String password = "";
        @NotBlank
        private String confirmPassword = "";
        private Role role = Role.USER;
        private String adminInviteCode = "";

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getAdminInviteCode() { return adminInviteCode; }
        public void setAdminInviteCode(String adminInviteCode) { this.adminInviteCode = adminInviteCode; }
    }
}
