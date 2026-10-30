package com.sep.sep_backend.repository;

import com.sep.sep_backend.model.FriendRequest;
import com.sep.sep_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    // Finde alle Anfragen, die ICH bekommen habe
    List<FriendRequest> findByReceiver(User receiver);
    
    // Prüfen, ob schon eine Anfrage existiert (damit man nicht spammen kann)
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
}