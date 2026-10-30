package com.sep.sep_backend.repository;

import com.sep.sep_backend.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    @Query(value = "SELECT * FROM Vocabulary order by RANDOM() LIMIT :limit", nativeQuery = true)
    List<Vocabulary> findRandomVocabs(@Param("limit")int limit);
    @Query(value = "SELECT * FROM vocabulary ORDER BY random() LIMIT 1", nativeQuery = true)
    Optional<Vocabulary> findRandom();


    // Diese Methode findet alle Wörter, die zu einem bestimmten Thema gehören.
    // Spring Data JPA baut die SQL-Query automatisch aus dem Methodennamen.
    List<Vocabulary> findByTheme(String theme);

    // Diese Methode holt eine Liste aller einzigartigen Themennamen aus der
    // Datenbank

    @Query("SELECT DISTINCT v.theme FROM Vocabulary v")
    List<String> findAllDistinctThemes();

}