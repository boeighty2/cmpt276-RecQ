package com.cmpt276g12.RecQ.queue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QueueControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void joinWithoutStaffRoleIsForbidden() throws Exception {
        mvc.perform(post("/api/queue/join")
                        .param("gym", "Nowhere").param("sport", "Nothing").param("userId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void joinClosedSportIsConflict() throws Exception {
        mvc.perform(post("/api/queue/join")
                        .sessionAttr("role", "STAFF")
                        .param("gym", "Nowhere").param("sport", "Nothing").param("userId", "1"))
                .andExpect(status().isConflict());
    }
}