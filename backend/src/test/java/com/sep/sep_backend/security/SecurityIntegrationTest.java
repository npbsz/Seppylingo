package com.sep.sep_backend.security;

import com.sep.sep_backend.model.User;
import com.sep.sep_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUpUser() {
        userRepository.deleteAll();
        User u = new User();
        u.setUsername("alice");                 // muss mit @WithMockUser(username=...) übereinstimmen
        u.setEmail("alice@example.com");
        u.setPassword("password");              // Passwort muss Validierungsregeln erfüllen
        u.setFriends(new HashSet<>());
        userRepository.save(u);
    }

    @Test
    void accessAuthEndpoint_withoutToken_shouldAllowed() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().is(400));
    }

    @Test
    void accessExercisesEndpoint_withoutToken_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/exercises/random"))
                .andExpect(status().isOk());
    }

    @Test
    void accessAiEndpoint_withoutToken_shouldBeAllowed() throws Exception {
        mockMvc.perform(post("/api/ai/chat"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessUserEndpoint_withoutToken_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice") // Username muss im DB‑Datensatz existieren
    void accessUserEndpoint_withMockUser_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk());
    }
}
