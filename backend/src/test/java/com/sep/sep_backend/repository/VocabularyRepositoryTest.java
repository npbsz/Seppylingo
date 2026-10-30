package com.sep.sep_backend.repository;

import com.sep.sep_backend.model.Vocabulary;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class VocabularyRepositoryTest {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private EntityManager em; // Wichtig für flush/clear

    @Test
    void findRandom_returnsVocabulary() {
        // 1. Arrange: Vokabel anlegen
        // Nutze deinen aktuellen Konstruktor (5 Argumente)
        Vocabulary v = new Vocabulary("Tisch", "table", "Nomen", "Zuhause", "A1");
        vocabularyRepository.save(v);

        // WICHTIG: Daten in die DB schreiben und Cache leeren, damit findAll() wirklich die DB fragt
        em.flush();
        em.clear();

        // 2. Act
        List<Vocabulary> all = vocabularyRepository.findAll();

        // 3. Assert
        assertFalse(all.isEmpty(), "Die Datenbank sollte nach dem Speichern nicht leer sein");

        // Prüfung, ob unser Wort dabei ist
        boolean found = all.stream().anyMatch(vocab -> vocab.getWordDE().equals("Tisch"));
        assertTrue(found, "Das gespeicherte Wort 'Tisch' sollte gefunden werden");
    }
}