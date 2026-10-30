package com.sep.sep_backend.repository;
import com.sep.sep_backend.model.PhraseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhraseRepo extends JpaRepository<PhraseEntity, Long> {
    /*
    Selects a random phrase from the PhraseEntity table
     */
    @Query(value= "SELECT * FROM phrase_entity ORDER BY RANDOM() LIMIT :limit" ,nativeQuery = true)
    List<PhraseEntity> findRandomPhrase(@Param("limit")int limit);
}
