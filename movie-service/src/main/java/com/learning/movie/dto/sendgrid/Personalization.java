package com.learning.movie.dto.sendgrid;

import java.util.List;

public class Personalization {
    private List<EmailAddress> to;
    private String subject;

    public List<EmailAddress> getTo() {
        return to;
    }

    public void setTo(List<EmailAddress> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
