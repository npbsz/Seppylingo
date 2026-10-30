package com.sep.sep_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotEmpty
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotEmpty
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean agbAccepted;

    @Column(name = "agb_accepted_at")
    private LocalDateTime agbAcceptedAt; // Hier wird die Zeit gespeichert

    @Column(nullable = false)
    private boolean introExamDone = false;

    @Column(nullable = true)
    private Double introExamPercentage;

    @Column(nullable = true, length = 64)
    private String introExamLevel;

    // Gamification
    @Column(columnDefinition = "integer default 0")
    private int totalPoints = 0;
    private String profilePictureUrl;
    private String englishLevel;
    private String learningPreferences;
    private String favoriteGame;

    @ElementCollection
    private List<String> badges;

    // Freunde System
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_friends", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "friend_id"))
    @JsonIgnoreProperties({ "friends", "password", "email", "badges", "totalPoints" })
    private Set<User> friends = new HashSet<>();

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // AGB & Zeitstempel
    public boolean isAgbAccepted() {
        return agbAccepted;
    }

    public void setAgbAccepted(boolean agbAccepted) {
        this.agbAccepted = agbAccepted;
    }

    public LocalDateTime getAgbAcceptedAt() {
        return agbAcceptedAt;
    }

    public void setAgbAcceptedAt(LocalDateTime agbAcceptedAt) {
        this.agbAcceptedAt = agbAcceptedAt;
    }

    // Gamification
    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getEnglishLevel() {
        return englishLevel;
    }

    public void setEnglishLevel(String englishLevel) {
        this.englishLevel = englishLevel;
    }

    public String getLearningPreferences() {
        return learningPreferences;
    }

    public void setLearningPreferences(String learningPreferences) {
        this.learningPreferences = learningPreferences;
    }

    public String getFavoriteGame() {
        return favoriteGame;
    }

    public void setFavoriteGame(String favoriteGame) {
        this.favoriteGame = favoriteGame;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    // Friends
    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    // Intro Exam
    public boolean isIntroExamDone() {
        return introExamDone;
    }

    public void setIntroExamDone(boolean introExamDone) {
        this.introExamDone = introExamDone;
    }

    public Double getIntroExamPercentage() {
        return introExamPercentage;
    }

    public void setIntroExamPercentage(Double introExamPercentage) {
        this.introExamPercentage = introExamPercentage;
    }

    public String getIntroExamLevel() {
        return introExamLevel;
    }

    public void setIntroExamLevel(String introExamLevel) {
        this.introExamLevel = introExamLevel;
    }
}