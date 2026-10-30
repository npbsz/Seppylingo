package com.sep.sep_backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vocab_mistake", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id","vocab_id"})
})
public class Mistake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vocab_id")
    private Vocabulary vocab;

    @Column(name = "wrong_count", nullable = false)
    private int wrongCount = 1;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt = Instant.now();

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt = Instant.now();

    protected Mistake() {}

    public Mistake(Long userId, Vocabulary vocab) {
        this.userId = userId;
        this.vocab  = vocab;
    }

    public void bump() {
        this.wrongCount++;
        this.lastSeenAt = Instant.now();
    }

    // Getter
    public Long getId()        { return id; }
    public Long getUserId()    { return userId; }
    public Vocabulary getVocab(){ return vocab; }
    public int getWrongCount() { return wrongCount; }
    public Instant getFirstSeenAt() { return firstSeenAt; }
    public Instant getLastSeenAt()  { return lastSeenAt; }
}
