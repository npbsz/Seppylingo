
        package com.sep.sep_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.sep_backend.model.FriendRequest;
import com.sep.sep_backend.model.User;
import com.sep.sep_backend.repository.FriendRequestRepository;
import com.sep.sep_backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_USER = "testUser";

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @InjectMocks
    private UserController userController;

    private User currentUser;
    private User otherUser;

    // SecurityContext mocks
    private SecurityContext securityContextMock;
    private Authentication authenticationMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // SecurityContext vorbereiten (getAuthenticatedUser() nutzt SecurityContextHolder)
        authenticationMock = mock(Authentication.class);
        Mockito.lenient().when(authenticationMock.getName()).thenReturn(TEST_USER);
        securityContextMock = mock(SecurityContext.class);
        Mockito.lenient().when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        // Setup für den eingeloggten User "testUser"
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername(TEST_USER);
        currentUser.setEmail("test@example.com");
        currentUser.setPassword("encodedPassword");
        currentUser.setFriends(new HashSet<>()); // Set initialisieren
        currentUser.setTotalPoints(100);

        // Ein zweiter User für Freundschaftstests
        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("friendUser");
        otherUser.setEmail("friend@example.com");
        otherUser.setPassword("encodedPassword");
        otherUser.setFriends(new HashSet<>());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- 1. Profil Tests ---

    @Test
    void getMyProfile_Success() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(TEST_USER)))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userRepository, times(1)).findByUsername(TEST_USER);
    }

    @Test
    void getUserProfile_Success() throws Exception {
        when(userRepository.findByUsername("friendUser")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(get("/api/user/friendUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("friendUser")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void searchUsers_Success() throws Exception {
        List<User> allUsers = Arrays.asList(currentUser, otherUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        mockMvc.perform(get("/api/user/search?query=friend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("friendUser")));
    }

    @Test
    void updateProfile_Success() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));

        Map<String, String> updates = new HashMap<>();
        updates.put("englishLevel", "B2");
        updates.put("favoriteGame", "Snake");

        mockMvc.perform(put("/api/user/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.englishLevel", is("B2")))
                .andExpect(jsonPath("$.favoriteGame", is("Snake")));

        verify(userRepository, times(1)).save(currentUser);
    }

    @Test
    void addPoints_Success() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));

        Map<String, Integer> pointsPayload = Map.of("points", 50);

        mockMvc.perform(post("/api/user/score/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pointsPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newScore", is(150)));

        verify(userRepository, times(1)).save(currentUser);
    }

    // --- 2. Freundschafts-System Tests ---

    @Test
    void sendFriendRequest_Success() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("friendUser")).thenReturn(Optional.of(otherUser));
        when(friendRequestRepository.findBySenderAndReceiver(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/user/testUser/friend-request/friendUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Freundschaftsanfrage gesendet!")));

        verify(friendRequestRepository, times(1)).save(any(FriendRequest.class));
    }

    @Test
    void sendFriendRequest_Self_BadRequest() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));

        mockMvc.perform(post("/api/user/testUser/friend-request/testUser")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyFriendRequests_Success() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));

        FriendRequest req = new FriendRequest(otherUser, currentUser);
        // keine req.setId(...) - Methode existiert nicht

        when(friendRequestRepository.findByReceiver(currentUser)).thenReturn(List.of(req));

        mockMvc.perform(get("/api/user/friend-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].senderName", is("friendUser")));
    }

    @Test
    void acceptFriendRequest_Success() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));

        FriendRequest req = new FriendRequest(otherUser, currentUser);
        // keine req.setId(...)

        when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        mockMvc.perform(post("/api/user/friend-requests/10/accept")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Freundschaft bestätigt!")));

        verify(userRepository, times(2)).save(any(User.class));
        verify(friendRequestRepository, times(1)).delete(req);
    }

    @Test
    void acceptFriendRequest_Forbidden_NotReceiver() throws Exception {
        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));

        User thirdUser = new User();
        thirdUser.setId(3L);
        FriendRequest req = new FriendRequest(otherUser, thirdUser);
        // keine req.setId(...)

        when(friendRequestRepository.findById(11L)).thenReturn(Optional.of(req));

        mockMvc.perform(post("/api/user/friend-requests/11/accept")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeFriend_Success() throws Exception {
        currentUser.getFriends().add(otherUser);
        otherUser.getFriends().add(currentUser);

        when(userRepository.findByUsername(TEST_USER)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("friendUser")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(delete("/api/user/testUser/friends/friendUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Verbindung getrennt.")));

        verify(userRepository, times(2)).save(any(User.class));
    }
}
