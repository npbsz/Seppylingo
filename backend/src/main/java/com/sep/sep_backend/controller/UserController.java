package com.sep.sep_backend.controller;

import com.sep.sep_backend.model.FriendRequest;
import com.sep.sep_backend.model.User;
import com.sep.sep_backend.repository.FriendRequestRepository;
import com.sep.sep_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile() {
        User user = getAuthenticatedUser();
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserProfile(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        user.setPassword(null);
        user.setEmail(null);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        List<User> allUsers = userRepository.findAll();
        List<User> results = allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
        results.forEach(u -> {
            u.setPassword(null);
            u.setEmail(null);
            u.setFriends(null); // Keine Freundeslisten bei der Suche exponieren
        });
        return ResponseEntity.ok(results);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@RequestBody Map<String, String> updates) {
        User user = getAuthenticatedUser();
        if (updates.containsKey("englishLevel"))
            user.setEnglishLevel(updates.get("englishLevel"));
        if (updates.containsKey("profilePictureUrl"))
            user.setProfilePictureUrl(updates.get("profilePictureUrl"));
        if (updates.containsKey("learningPreferences"))
            user.setLearningPreferences(updates.get("learningPreferences"));
        if (updates.containsKey("favoriteGame"))
            user.setFavoriteGame(updates.get("favoriteGame"));
        userRepository.save(user);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/score/add")
    public ResponseEntity<?> addPoints(@RequestBody Map<String, Integer> payload) {
        User user = getAuthenticatedUser();
        int pointsToAdd = payload.getOrDefault("points", 0);
        user.setTotalPoints(user.getTotalPoints() + pointsToAdd);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Points updated", "newScore", user.getTotalPoints()));
    }

    // Freundesystem

    // 1. Freundschaftsanfrage senden
    @PostMapping("/{username}/friend-request/{receiverName}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable String username, @PathVariable String receiverName) {
        User sender = userRepository.findByUsername(username).orElseThrow();
        User receiver = userRepository.findByUsername(receiverName).orElseThrow();

        if (sender.equals(receiver))
            return ResponseEntity.badRequest().body("Du kannst dir selbst keine Anfrage schicken.");
        if (sender.getFriends().contains(receiver))
            return ResponseEntity.badRequest().body("Ihr seid schon Freunde.");

        // Prüfen ob schon eine Anfrage offen ist
        if (friendRequestRepository.findBySenderAndReceiver(sender, receiver).isPresent()) {
            return ResponseEntity.badRequest().body("Anfrage bereits gesendet.");
        }

        friendRequestRepository.save(new FriendRequest(sender, receiver));
        return ResponseEntity.ok(Map.of("message", "Freundschaftsanfrage gesendet!"));
    }

    // 2. Meine offenen Anfragen anzeigen (die ich bekommen habe)
    @GetMapping("/friend-requests")
    public ResponseEntity<List<Map<String, Object>>> getMyFriendRequests() {
        User me = getAuthenticatedUser();
        List<FriendRequest> requests = friendRequestRepository.findByReceiver(me);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FriendRequest req : requests) {
            Map<String, Object> map = new HashMap<>();
            map.put("requestId", req.getId());
            map.put("senderName", req.getSender().getUsername());
            map.put("senderAvatar", req.getSender().getProfilePictureUrl());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // 3. Anfrage annehmen
    @PostMapping("/friend-requests/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Anfrage nicht gefunden"));

        User me = getAuthenticatedUser();
        // Sicherheitscheck: Darf ich das überhaupt annehmen?
        if (!request.getReceiver().getId().equals(me.getId())) {
            return ResponseEntity.status(403).body("Nicht deine Anfrage.");
        }

        User sender = request.getSender();

        // Jetzt wirklich Freunde werden
        me.getFriends().add(sender);
        sender.getFriends().add(me);

        userRepository.save(me);
        userRepository.save(sender);

        // Anfrage löschen, da erledigt
        friendRequestRepository.delete(request);

        return ResponseEntity.ok(Map.of("message", "Freundschaft bestätigt!"));
    }

    // 4. Anfrage ablehnen
    @DeleteMapping("/friend-requests/{requestId}")
    public ResponseEntity<?> declineFriendRequest(@PathVariable Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId).orElseThrow();
        User me = getAuthenticatedUser();

        if (!request.getReceiver().getId().equals(me.getId())) {
            return ResponseEntity.status(403).body("Nicht deine Anfrage.");
        }

        friendRequestRepository.delete(request);
        return ResponseEntity.ok(Map.of("message", "Anfrage abgelehnt."));
    }

    // 5. Freund entfernen (Mit ID-Vergleich)
    @DeleteMapping("/{username}/friends/{friendName}")
    public ResponseEntity<?> removeFriend(@PathVariable String username, @PathVariable String friendName) {
        User user = userRepository.findByUsername(username).orElseThrow();
        User friend = userRepository.findByUsername(friendName).orElseThrow();

        // beidseitiges Löschen sicherstellen
        boolean removed1 = user.getFriends().removeIf(f -> f.getId().equals(friend.getId()));
        boolean removed2 = friend.getFriends().removeIf(f -> f.getId().equals(user.getId()));

        userRepository.save(user);
        userRepository.save(friend);

        if (removed1 || removed2) {
            return ResponseEntity.ok(Map.of("message", "Verbindung getrennt."));
        } else {
            return ResponseEntity.ok(Map.of("message", "Waren gar keine Freunde/Bereits gelöscht."));
        }
    }
}