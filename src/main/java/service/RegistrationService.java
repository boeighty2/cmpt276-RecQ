package ca.example.login.service;

import ca.example.login.model.AppUser;
import ca.example.login.model.Role;
import ca.example.login.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final String ADMIN_INVITE_CODE = "1234";

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository users,
                               PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String email, String username, String password,
                         Role requestedRole, String inviteCode) {

        if (users.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }

        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }

        Role role = Role.USER;

        if (requestedRole == Role.ADMIN) {
            if (!ADMIN_INVITE_CODE.equals(inviteCode)) {
                throw new IllegalArgumentException("Invalid admin invite code.");
            }
            role = Role.ADMIN;
        }

        String hash = passwordEncoder.encode(password);

        users.save(new AppUser(
            email.trim().toLowerCase(),
            username.trim(),
            hash,
            role
        ));
    }
}
