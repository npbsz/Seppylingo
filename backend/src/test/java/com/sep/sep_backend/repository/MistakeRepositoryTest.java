package com.sep.sep_backend.repository;

import com.sep.sep_backend.model.Mistake;
import com.sep.sep_backend.model.User;
import com.sep.sep_backend.model.Vocabulary;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
// WICHTIG: Replace.ANY sorgt dafür, dass H2 (In-Memory) verwendet wird,
// damit der Test auch in der Pipeline ohne Docker läuft.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MistakeRepositoryTest {

    @Autowired
    private MistakeRepository mistakeRepository;

    @Autowired
    private UserRepository userRepository; // WICHTIG: Um User für FK-Constraints zu erstellen

    @Autowired
    private EntityManager em;

    private Long userId1;
    private Long userId2;
    private Vocabulary vocab1;
    private Vocabulary vocab2;

    @BeforeEach
    void setUp() {
        // 1. Alles leeren
        mistakeRepository.deleteAll();
        userRepository.deleteAll();
        em.createQuery("delete from Vocabulary").executeUpdate();
        em.flush();

        // 2. USER ERSTELLEN (Wichtig für Foreign Key Constraints!)
        User u1 = new User("user1", "u1@test.de", "password123"); // PW lang genug!
        u1 = userRepository.save(u1);
        userId1 = u1.getId();

        User u2 = new User("user2", "u2@test.de", "password123");
        u2 = userRepository.save(u2);
        userId2 = u2.getId();

        // 3. Vokabeln anlegen (5-Argumente-Konstruktor nutzen!)
        vocab1 = new Vocabulary("der Mann", "the man", "Nomen", "Basics", "A1");
        vocab2 = new Vocabulary("die Frau", "the woman", "Nomen", "Basics", "A1");
        em.persist(vocab1);
        em.persist(vocab2);
        em.flush();

        // 4. Mistakes erstellen (Jetzt mit gültigen User-IDs)
        Mistake m1 = new Mistake(userId1, vocab1);
        Mistake m2 = new Mistake(userId1, vocab2);
        Mistake m3 = new Mistake(userId2, vocab1);

        em.persist(m1);
        em.persist(m2);
        em.persist(m3);

        em.flush();
        em.clear(); // Cache leeren für sauberen Test
    }

    @Test
    void findByUserIdAndVocab_shouldReturnCorrectMistake() {
        Optional<Mistake> found =
                mistakeRepository.findByUserIdAndVocab(userId1, vocab1);

        assertTrue(found.isPresent(), "Mistake sollte gefunden werden");
        assertEquals(userId1, found.get().getUserId());
        assertEquals(vocab1.getId(), found.get().getVocab().getId());
    }

    @Test
    void countByUserId_shouldCountOnlyUsersMistakes() {
        long count1 = mistakeRepository.countByUserId(userId1);
        long count2 = mistakeRepository.countByUserId(userId2);

        assertEquals(2, count1);
        assertEquals(1, count2);
    }

    @Test
    void findAllByUserId_shouldReturnAllMistakesForUser() {
        List<Mistake> list = mistakeRepository.findAllByUserId(userId1);

        assertEquals(2, list.size());
        assertTrue(list.stream().allMatch(m -> m.getUserId().equals(userId1)));
    }

    @Test
    void findRandomMistake_shouldReturnSingleRandomMistake() {
        Page<Mistake> page =
                mistakeRepository.findRandomMistake(userId1, PageRequest.of(0, 1));

        assertEquals(1, page.getContent().size());
        Mistake m = page.getContent().get(0);
        assertEquals(userId1, m.getUserId());
    }
}