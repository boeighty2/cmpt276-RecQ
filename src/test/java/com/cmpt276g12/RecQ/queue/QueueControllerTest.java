package com.cmpt276g12.RecQ.queue;

import com.cmpt276g12.RecQ.auth.model.Role;
import com.cmpt276g12.RecQ.auth.service.RegistrationService;
import com.cmpt276g12.RecQ.facility.CourtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QueueControllerTest {

    @Autowired MockMvc mvc;
    @Autowired CourtService courtService;
    @Autowired RegistrationService registration;

    private void openSport(String gym, String sport) {
        courtService.createSport(gym, sport, 2);
        courtService.setSportOpen(gym, sport, true);
    }

    @Test
    void joinWithoutStaffRoleIsForbidden() throws Exception {
        mvc.perform(post("/api/queue/join")
                        .param("gym", "Nowhere").param("sport", "Nothing")
                        .param("username", "alice"))
                .andExpect(status().isForbidden());
    }

    @Test
    void joinClosedSportIsConflict() throws Exception {
        mvc.perform(post("/api/queue/join")
                        .sessionAttr("role", "STAFF")
                        .param("gym", "Nowhere").param("sport", "Nothing")
                        .param("username", "alice"))
                .andExpect(status().isConflict());
    }

    @Test
    void countForClosedSportIsOkAndInactive() throws Exception {
        mvc.perform(get("/api/queue/count")
                        .param("gym", "Nowhere").param("sport", "Nothing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queueActive").value(false))
                .andExpect(jsonPath("$.waitingCount").value(0));
    }

    @Test
    void staffCanCheckInByUsernameAndSeeItInTheList() throws Exception {
        openSport("CI Gym", "Badminton");
        registration.register("alice@example.com", "alice", "password123", Role.USER, "");

        mvc.perform(post("/api/queue/join").sessionAttr("role", "STAFF")
                        .param("gym", "CI Gym").param("sport", "Badminton")
                        .param("username", "Alice"))   // case-insensitive
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.status").value("WAITING"));

        mvc.perform(get("/api/queue").sessionAttr("role", "STAFF")
                        .param("gym", "CI Gym").param("sport", "Badminton"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void checkInUnknownUsernameIsNotFound() throws Exception {
        openSport("CI Gym 2", "Badminton");
        mvc.perform(post("/api/queue/join").sessionAttr("role", "STAFF")
                        .param("gym", "CI Gym 2").param("sport", "Badminton")
                        .param("username", "ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkInSameUserTwiceIsConflict() throws Exception {
        openSport("CI Gym 3", "Badminton");
        registration.register("bob@example.com", "bob", "password123", Role.USER, "");
        for (int i = 0; i < 2; i++) {
            var result = mvc.perform(post("/api/queue/join").sessionAttr("role", "STAFF")
                    .param("gym", "CI Gym 3").param("sport", "Badminton")
                    .param("username", "bob"));
            result.andExpect(status().is(i == 0 ? 200 : 409));
        }
    }
}