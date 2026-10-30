package com.sep.sep_backend.repository;

import com.sep.sep_backend.model.Mistake;
import com.sep.sep_backend.model.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface MistakeRepository extends JpaRepository<Mistake, Long> {

    @Query("select m from Mistake m where m.userId = :userId and m.vocab = :v")
    Optional<Mistake> findByUserIdAndVocab(@Param("userId") Long userId,
                                           @Param("v") Vocabulary v);

    @Query("select count(m) from Mistake m where m.userId = :userId")
    long countByUserId(@Param("userId") Long userId);   // <- long!

    // Alle Fehler eines Users
    @Query("select m from Mistake m where m.userId = :userId")
    List<Mistake> findAllByUserId(@Param("userId") Long userId);

    // Zufällige Fehlerkarte (Page mit 1 Element)
    @Query("select m from Mistake m where m.userId = :userId order by function('random')")
    Page<Mistake> findRandomMistake(@Param("userId") Long userId,
                                    Pageable pageable);

    // Für Reset
    void deleteAllByUserId(Long userId);

    // Für „Fehler im Fehlermodus korrigiert“
    void deleteByUserIdAndVocab(Long userId, Vocabulary vocab);
}
