package com.learning.movie.dto.sendgrid;

public class EmailAddress {
    private String email;
    private String name;

    public EmailAddress(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public EmailAddress(String email) {
        this.email = email;
    }

    public EmailAddress() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
