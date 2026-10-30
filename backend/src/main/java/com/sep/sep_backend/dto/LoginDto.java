package com.sep.sep_backend.dto;

import jakarta.validation.constraints.NotEmpty;

public class LoginDto {

    // No-arg Konstruktor, für Tests
    public LoginDto(){}

    // All-arg Konstruktor, für Tests
    public LoginDto(String username, String password){
        this.username = username;
        this.password = password;
    }


    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
