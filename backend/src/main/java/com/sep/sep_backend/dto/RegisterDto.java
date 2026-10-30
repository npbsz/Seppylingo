package com.sep.sep_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class RegisterDto {

    @NotEmpty
    @Size(min = 3, max = 50)
    private String username;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    @Size(min = 6)
    private String password;

    private boolean agbAccepted;

    public RegisterDto() {
    }
    public RegisterDto(String testuser, String mail, String password123) {
        this.username = testuser;
        this.email = mail;
        this.password = password123;
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

    public boolean isAgbAccepted() {
        return agbAccepted;
    }

    public void setAgbAccepted(boolean agbAccepted) {
        this.agbAccepted = agbAccepted;
    }
}