
package com.sep.sep_backend.repository;

import com.sep.sep_backend.model.FriendRequest;
import com.sep.sep_backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FriendRequestRepositoryTest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        // Sauber starten
        friendRequestRepository.deleteAll();
        userRepository.deleteAll();

        // Benutzer anlegen und speichern
        userA = new User();
        userA.setUsername("alice");
        userA.setEmail("alice@example.com");
        userA.setPassword("password"); // >= 6 Zeichen
        userA.setFriends(new HashSet<>());
        userA = userRepository.save(userA);

        userB = new User();
        userB.setUsername("bob");
        userB.setEmail("bob@example.com");
        userB.setPassword("securepw"); // >= 6 Zeichen
        userB.setFriends(new HashSet<>());
        userB = userRepository.save(userB);

        userC = new User();
        userC.setUsername("charlie");
        userC.setEmail("charlie@example.com");
        userC.setPassword("charliepw"); // >= 6 Zeichen
        userC.setFriends(new HashSet<>());
        userC = userRepository.save(userC);

        // FriendRequests anlegen und speichern:
        // A -> B, C -> B, B -> A
        FriendRequest fr1 = new FriendRequest(userA, userB);
        FriendRequest fr2 = new FriendRequest(userC, userB);
        FriendRequest fr3 = new FriendRequest(userB, userA);

        friendRequestRepository.save(fr1);
        friendRequestRepository.save(fr2);
        friendRequestRepository.save(fr3);
    }

    @Test
    void findByReceiver_returnsAllRequestsForGivenReceiver() {
        // Receiver userB: erwarten 2 Anfragen (von alice und charlie)
        List<FriendRequest> requestsForB = friendRequestRepository.findByReceiver(userB);
        assertNotNull(requestsForB);
        assertEquals(2, requestsForB.size());

        boolean hasAlice = requestsForB.stream().anyMatch(r -> r.getSender().getUsername().equals("alice"));
        boolean hasCharlie = requestsForB.stream().anyMatch(r -> r.getSender().getUsername().equals("charlie"));
        assertTrue(hasAlice);
        assertTrue(hasCharlie);
    }

    @Test
    void findBySenderAndReceiver_returnsPresentForExistingPair() {
        // A -> B existiert
        Optional<FriendRequest> found = friendRequestRepository.findBySenderAndReceiver(userA, userB);
        assertTrue(found.isPresent());
        FriendRequest fr = found.get();
        assertEquals("alice", fr.getSender().getUsername());
        assertEquals("bob", fr.getReceiver().getUsername());
    }

    @Test
    void findBySenderAndReceiver_returnsEmptyForNonExistingPair() {
        // A -> C existiert nicht
        Optional<FriendRequest> notFound = friendRequestRepository.findBySenderAndReceiver(userA, userC);
        assertFalse(notFound.isPresent());
    }
}
